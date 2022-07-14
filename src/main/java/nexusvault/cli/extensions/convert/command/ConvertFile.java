package nexusvault.cli.extensions.convert.command;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.stream.Collectors;

import nexusvault.cli.core.App;
import nexusvault.cli.core.AutoInstantiate;
import nexusvault.cli.core.cmd.AbstractCommandHandler;
import nexusvault.cli.core.cmd.Argument;
import nexusvault.cli.core.cmd.Arguments;
import nexusvault.cli.core.cmd.CommandDescription;
import nexusvault.cli.extensions.convert.ConversionRequest;
import nexusvault.cli.extensions.convert.ConverterArgs;
import nexusvault.cli.extensions.convert.ConverterExtension;
import nexusvault.cli.extensions.convert.resource.FileResource;
import nexusvault.cli.extensions.convert.resource.Resource;

@AutoInstantiate
public final class ConvertFile extends AbstractCommandHandler {

	@Override
	public CommandDescription getCommandDescription() {
		final var cmdBuilder = new CommandDescription.CompactBuilder();
		cmdBuilder.setCommandName("convert");
		cmdBuilder.addAlternativeNames("convert-file");
		cmdBuilder.setDescription("Converts files from one format to another.");
		for (final var arg : App.getInstance().getExtension(ConverterExtension.class).getCLIOptions()) {
			cmdBuilder.addNamedArgument(arg);
		}
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

		final var alternativeLookupPaths = new Path[] { App.getInstance().getAppConfig().getOutputPath(),
				App.getInstance().getAppConfig().getApplicationPath() };

		searchFiles: for (final var strPath : args.getUnnamedArgs()) {
			final var path = Path.of(strPath);

			if (Files.exists(path)) {
				targets.add(new FileResource(path));
				continue; // done
			}

			if (!path.isAbsolute()) {
				for (final var parentPath : alternativeLookupPaths) {
					final var newPath = parentPath.resolve(path);
					if (Files.exists(newPath)) {
						targets.add(new FileResource(newPath));
						continue searchFiles; // done
					}
				}
			}

			sendMsg(() -> String.format("File not found: %s", strPath));
			allFilesFound = false;
		}

		if (!allFilesFound) {
			sendMsg(() -> "Missing files. Abort convertion.");
			return;
		}

		final var options = new ConverterArgs(Arrays.stream(args.getNamedArgs()).collect(Collectors.toMap(Argument::getName, Argument::getValues)));

		final var requests = targets.stream().map(e -> {
			// output will be created right next to the input file
			final var outputDir = e.getFilePath().resolveSibling("");
			return new ConversionRequest(e, outputDir);
		}).collect(Collectors.toList());

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
