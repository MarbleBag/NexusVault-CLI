package nexusvault.cli.plugin.export;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import nexusvault.cli.App;
import nexusvault.cli.core.cmd.AbstractCommandHandler;
import nexusvault.cli.core.cmd.Arguments;
import nexusvault.cli.core.cmd.CommandDescription;
import nexusvault.cli.plugin.export.ExportPlugIn.ExportConfig;

final class ExportFileCmd extends AbstractCommandHandler {
	@Override
	public CommandDescription getCommandDescription() {
		// @formatter:off
		return CommandDescription.newInfo()
				.setCommandName("convert-file")
				.setDescription("Reads and converts files, which were previously extracted from an archive, but not converted yet")
				.setNoNamedArguments()
				.build();
		//@formatter:on
	}

	@Override
	public void onCommand(Arguments args) {
		if (args.getUnnamedArgumentSize() == 0) {
			sendMsg(() -> String.format("At least one file path is required"));
			return;
		}

		final List<Path> targets = new LinkedList<>();
		boolean allFilesFound = true;
		final var nArgs = args.getUnnamedArgs();
		for (final String nArg : nArgs) {
			final Path p = Paths.get(nArg);
			if (Files.exists(p)) {
				targets.add(p);
			} else {
				sendMsg(() -> String.format("Unable to convert file. Not found: %s", p));
				allFilesFound = false;
			}
		}

		if (!allFilesFound) {
			sendMsg(() -> "Missing files. Abort convertion.");
			return;
		}

		final ExportConfig exportConfig = new ExportConfig();
		App.getInstance().getPlugIn(ExportPlugIn.class).exportPath(targets, exportConfig);
	}

	@Override
	public String onHelp() {
		final Set<String> supportedFileTypes = new HashSet<>();
		for (final Exporter exporter : App.getInstance().getPlugIn(ExportPlugIn.class).getExporters()) {
			supportedFileTypes.addAll(exporter.getAcceptedFileEndings());
		}

		final var builder = new StringBuilder();
		builder.append("Supported file types: ").append(String.join(", ", supportedFileTypes));
		return builder.toString();
	}
}