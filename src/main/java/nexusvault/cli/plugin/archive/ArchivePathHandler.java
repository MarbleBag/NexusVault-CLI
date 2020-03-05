package nexusvault.cli.plugin.archive;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import nexusvault.cli.App;
import nexusvault.cli.core.cmd.Argument;
import nexusvault.cli.core.cmd.ArgumentDescription;
import nexusvault.cli.core.cmd.ArgumentHandler;
import nexusvault.cli.core.cmd.Arguments;
import nexusvault.cli.core.cmd.CommandDescription;
import nexusvault.cli.core.cmd.CommandFormatException;
import nexusvault.cli.core.cmd.CommandHandler;
import nexusvault.cli.core.exception.FilePathNotSetException;

final class ArchivePathHandler implements ArgumentHandler, CommandHandler {

	@Override
	public CommandDescription getCommandDescription() {
		// @formatter:off
		return CommandDescription.newInfo()
				.setCommandName("archive-path")
				.setDescription(
						"a directory containing one or multiple archive files, or a path pointing to a single archive file. " +
						"Multiple pathes can be specified. In case the path points to a WS root directory, it automatically changes and points to the 'patch' folder."
						)
				.addNamedArgument(getArgumentDescription())
				.namedArgumentsDone()
				.build();
		//@formatter:on
	}

	@Override
	public ArgumentDescription getArgumentDescription() {
		// @formatter:off
		return ArgumentDescription.newInfo()
				.setName("archive")
				.setNameShort("a")
				.setDescription(
						"a directory containing one or multiple archive files, or a path pointing to a single archive file. " +
						"Multiple pathes can be specified. In case the path points to a WS root directory, it automatically changes and points to the 'patch' folder."
						)
				.setRequired(false)
				.setArguments(false)
				.setNumberOfArgumentsUnlimited()
				.setNamesOfArguments("path,...")
				.build();
		//@formatter:on
	}

	@Override
	public void onCommand(Arguments args) {
		if (args.getUnnamedArgumentSize() == 0) {
			throw new CommandFormatException("");
		}
		execute(args.getUnnamedArgs());
	}

	@Override
	public void execute(Argument arg) {
		execute(arg.getValues());
	}

	public void execute(String[] args) {
		if (args.length == 0) {
			throw new FilePathNotSetException("Path to archive not set");
		}

		final Path[] paths = new Path[args.length];
		for (int i = 0; i < paths.length; ++i) {
			paths[i] = Paths.get(args[i]);

			if (paths[i].endsWith("WildStar") && Files.isDirectory(paths[i])) {
				final Path patchFolder = paths[i].resolve("Patch");
				if (Files.exists(patchFolder)) {
					paths[i] = patchFolder;
				}
			}
		}

		App.getInstance().getPlugInSystem().getPlugIn(ArchivePlugIn.class).setArchivePaths(Arrays.asList(paths));
	}

	@Override
	public String onHelp() {
		return null;
	}

}
