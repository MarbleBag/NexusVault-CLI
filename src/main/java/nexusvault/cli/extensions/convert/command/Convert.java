package nexusvault.cli.extensions.convert.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import nexusvault.archive.IdxPath;
import nexusvault.cli.core.App;
import nexusvault.cli.core.cmd.AbstractCommandHandler;
import nexusvault.cli.core.cmd.Argument;
import nexusvault.cli.core.cmd.ArgumentDescription;
import nexusvault.cli.core.cmd.Arguments;
import nexusvault.cli.core.cmd.CommandDescription;
import nexusvault.cli.extensions.archive.ArchiveExtension;
import nexusvault.cli.extensions.convert.ConversionRequest;
import nexusvault.cli.extensions.convert.ConverterArgs;
import nexusvault.cli.extensions.convert.ConverterExtension;
import nexusvault.cli.extensions.convert.resource.ArchiveResource;
import nexusvault.cli.extensions.convert.resource.FileResource;
import nexusvault.cli.extensions.convert.resource.Resource;

public final class Convert extends AbstractCommandHandler {

	@Override
	public CommandDescription getCommandDescription() {
		final var cmdBuilder = new CommandDescription.CompactBuilder();
		cmdBuilder.setCommandName("convert");
		cmdBuilder.addAlternativeNames("export");
		cmdBuilder.setDescription("Converts files from one format to another.");
		for (final var arg : App.getInstance().getExtension(ConverterExtension.class).getCLIOptions()) {
			cmdBuilder.addNamedArgument(arg);
		}

		// @formatter:off
		cmdBuilder.addNamedArgument(
				ArgumentDescription.newInfo()
				.setName("binary")
				.setDescription("move files unprocessed")
				.setRequired(false)
				.setNoArguments()
				.build()
			);
		cmdBuilder.addNamedArgument(
				ArgumentDescription.newInfo()
				.setName("converters")
				.setNameShort("cvts")
				.setDescription("")
				.setRequired(false)
				.setArguments(true)
				.setNumberOfArgumentsUnlimited()
				.setNamesOfArguments("ext->id")
				.build()
			);
		cmdBuilder.addNamedArgument(
				ArgumentDescription.newInfo()
				.setName("file")
				.setDescription("")
				.setRequired(false)
				.setArguments(true)
				.setNumberOfArgumentsUnlimited()
				.setNamesOfArguments("path")
				.build()
			);
		//@formatter:on

		return cmdBuilder.build();
	}

	@Override
	public void onCommand(Arguments args) {
		if (args.getUnnamedArgumentSize() == 0) {
			sendMsg(() -> String.format("At least one file path is required"));
			return;
		}

		final var targets = new LinkedList<Resource>();
		boolean allFilesFound = true;

		for (final var strPath : args.getUnnamedArgs()) {
			final var path = Paths.get(strPath);
			if (Files.exists(path)) {
				targets.add(new FileResource(path));
			} else {
				sendMsg(() -> String.format("File not found: %s", path));
				allFilesFound = false;
			}
		}

		if (!allFilesFound) {
			sendMsg(() -> "Missing files. Abort convertion.");
			return;
		}

		final var options = new ConverterArgs(Arrays.stream(args.getNamedArgs()).collect(Collectors.toMap(Argument::getName, Argument::getValues)));
		final var requests = targets.stream().map(e -> new ConversionRequest(e, null)).collect(Collectors.toList());
		final var results = App.getInstance().getExtension(ConverterExtension.class).convert(requests, options, null);

		if (results.stream().anyMatch(e -> e.isFailed())) {
			sendMsg(() -> {
				final StringBuilder msg = new StringBuilder();
				msg.append("Unable to convert file(s)'\n");
				for (final var result : results) {
					if (result.isFailed()) {
						msg.append(result.getRequest().input.getFilePath()).append("\n");
						msg.append("->").append(result.getError().getClass());
						msg.append(" : ").append(result.getError().getMessage());
						msg.append("\n");

						var cause = result.getError().getCause();
						while (cause != null) {
							msg.append("\t\t->").append(cause.getClass()).append(" : ").append(cause.getMessage()).append("\n");
							cause = cause.getCause();
						}
					}
				}
				return msg.toString();
			});
		}
	}

	private List<Resource> collectResources(Arguments args) throws IOException {

		final var possibleFiles = getTargetedFiles(args);

		final var resources = new LinkedList<Resource>();
		final var nonexistingFiles = new LinkedList<String>();
		if (!possibleFiles.isEmpty()) {
			var missingFiles = collectFromFileSystem(possibleFiles, nonexistingFiles, resources);
			missingFiles = collectFromArchives(missingFiles, nonexistingFiles, resources);

		}

		return resources;
	}

	protected LinkedList<String> getTargetedFiles(Arguments args) {
		final var possibleFiles = new LinkedList<String>();
		if (args.getUnnamedArgumentSize() > 0) {
			for (final var arg : args.getUnnamedArgs()) {
				possibleFiles.add(arg);
			}
		}
		if (args.isNamedArgumentSet("file")) {
			for (final var arg : args.getArgumentByName("file").getValues()) {
				possibleFiles.add(arg);
			}
		}
		return possibleFiles;
	}

	protected List<String> collectFromArchives(List<String> possibleFiles, List<String> nonexistingFiles, List<Resource> resources) {
		if (possibleFiles.isEmpty()) {
			return Collections.emptyList();
		}

		final var missingFiles = new LinkedList<String>();

		final var archiveExtension = App.getInstance().getExtension(ArchiveExtension.class);
		final var archiveContainers = archiveExtension.getArchives();
		if (archiveContainers.isEmpty()) {
			sendMsg("No vaults are loaded. Use 'help' to learn how to load them");
		} else {
			for (final var possiblePath : possibleFiles) {
				final var idxPath = IdxPath.createPathFrom(possiblePath);
				for (final var container : archiveContainers) {
					final var optionalEntry = idxPath.tryToResolve(container.getArchive().getRootDirectory());
					if (optionalEntry.isPresent()) {
						final var entry = optionalEntry.get();
						if (entry.isFile()) {
							resources.add(new ArchiveResource(entry.asFile()));
						} else {
							missingFiles.add(possiblePath);
						}
					}
				}
			}
		}

		return missingFiles;
	}

	protected List<String> collectFromFileSystem(List<String> possibleFiles, List<String> nonexistingFiles, List<Resource> resources) throws IOException {

		final var missingFiles = new LinkedList<String>();

		final var pathVariations = new Path[] { App.getInstance().getAppConfig().getOutputPath(), App.getInstance().getAppConfig().getApplicationPath() };
		for (final var possibleFile : possibleFiles) {
			var path = Paths.get(possibleFile);

			if (!Files.exists(path)) {
				if (path.isAbsolute()) {
					missingFiles.add(possibleFile);
					continue;
				}

				var doesExist = false;
				for (final var pathPrefix : pathVariations) {
					final var tmpPath = pathPrefix.resolve(path);
					if (Files.exists(tmpPath)) {
						path = tmpPath;
						doesExist = true;
						break;
					}
				}

				if (!doesExist) {
					nonexistingFiles.add(possibleFile);
					continue;
				}
			}

			if (Files.isRegularFile(path)) {
				resources.add(new FileResource(path));
			} else if (Files.isDirectory(path)) {
				final var scanDirectories = new LinkedList<Path>();
				scanDirectories.add(path);
				for (final var nextDir : scanDirectories) {
					try (var dirs = Files.newDirectoryStream(nextDir)) {
						for (final var pathInDir : dirs) {
							if (Files.isRegularFile(pathInDir)) {
								resources.add(new FileResource(pathInDir));
							} else if (Files.isDirectory(pathInDir)) {
								scanDirectories.add(pathInDir);
							}
						}
					}
				}
			} else {
				missingFiles.add(possibleFile);
			}
		}

		return missingFiles;
	}

	@Override
	public String onHelp() {
		final var extension = App.getInstance().getExtension(ConverterExtension.class);
		final var builder = new StringBuilder();
		builder.append("Supported file conversions:").append("\n");
		for (final var fileExtension : extension.getSupportedFileExtensions()) {
			final var converters = new HashSet<>(extension.getConverterIdsForFileExtensions(fileExtension));
			final var preferredConverter = extension.getConverterIdForFileExtension(fileExtension);
			converters.remove(preferredConverter);
			if (preferredConverter != null) {
				converters.add("[" + preferredConverter + "]");
			}
			builder.append("\t").append(fileExtension).append(" - > ").append(String.join(", ", converters));
		}
		return builder.toString();
	}

}
