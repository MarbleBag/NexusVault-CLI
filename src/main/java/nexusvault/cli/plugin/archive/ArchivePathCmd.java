package nexusvault.cli.plugin.archive;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import nexusvault.cli.App;
import nexusvault.cli.Command;
import nexusvault.cli.CommandArguments;
import nexusvault.cli.CommandInfo;
import nexusvault.cli.exception.FilePathNotSetException;

final class ArchivePathCmd implements Command {

	@Override
	public CommandInfo getCommandInfo() {
		// @formatter:off
		return CommandInfo.newInfo()
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
	public void onCommand(CommandArguments args) {
		if (args.getNumberOfArguments() == 0) {
			throw new FilePathNotSetException("Path to archive not set");
		}

		final Path[] paths = new Path[args.getNumberOfArguments()];
		for (int i = 0; i < paths.length; ++i) {
			final String arg = args.getArg(i);
			paths[i] = Paths.get(arg);

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
	public void onHelp(CommandArguments args) {
		// TODO Auto-generated method stub
	}
	//
	// private final static String TRIGGER = "archive";
	// private final AppProperties properties;
	// private final AppResources resources;
	//
	// public CmdArchivePath(AppConsole logger, AppProperties properties, AppResources resources) {
	// super(logger);
	// this.properties = properties;
	// this.resources = resources;
	// }
	//
	// @Override
	// public String getTriggerWord() {
	// return TRIGGER;
	// }
	//
	// @Override
	// public Option getOption() {
	// final Option opt = Option.builder("a").longOpt(TRIGGER).argName("path").hasArg(true).required(false)
	// .desc("ws root directory or archive/index file path").build();
	// return opt;
	// }
	//
	// @Override
	// public void onCmd(CmdArgContainer args) {
	// final String arg0 = args.getArg(0);
	// if ((arg0 == null) || arg0.isEmpty()) {
	// throw new FilePathNotSetException("Path to archive not set");
	// }
	//
	// Path archivePath;
	// if ("reload".equalsIgnoreCase(arg0)) {
	// archivePath = properties.getArchivePath();
	// } else {
	// archivePath = Paths.get(arg0);
	// }
	//
	// if (Files.isDirectory(archivePath)) {
	// if (archivePath.endsWith("WildStar")) {
	// archivePath = archivePath.resolve("Patch").resolve("ClientData.index");
	// } else if (archivePath.endsWith("Patch")) {
	// archivePath = archivePath.resolve("ClientData.index");
	// } else {
	// throw new InvalidFilePathException("Incomplete path to wildstar archive: " + archivePath);
	// }
	// }
	//
	// if (!Files.exists(archivePath)) {
	// throw new FileNotFoundException("Wildstar archive not found: " + archivePath);
	// }
	//
	// if (!Files.isReadable(archivePath)) {
	// throw new FileNotReadableException("Wildstar archive not readable: " + archivePath);
	// }
	//
	// properties.setArchivePath(archivePath);
	//
	// try {
	// final ArchiveReader archive = new ArchiveReader();
	// archive.readArchive(archivePath);
	// resources.setArchive(archive);
	// } catch (final IOException e) {
	// throw new ArchiveCanNotBeReadException(e);
	// }
	// }
	//
	// @Override
	// public void onHelp(CmdArgContainer args) {
	// // TODO Auto-generated method stub
	//
	// }

}
