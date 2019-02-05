package nexusvault.cli.plugin.archive;

import nexusvault.cli.App;
import nexusvault.cli.Command;
import nexusvault.cli.CommandArguments;
import nexusvault.cli.CommandInfo;

final class NavigatorChangeDirectoryCmd implements Command {

	@Override
	public CommandInfo getCommandInfo() {
		// @formatter:off
		return CommandInfo.newInfo()
				.setName("cd")
				.setDescription("list the content of TODO")
				.setRequired(false)
				.setArguments(false)
				.setNumberOfArguments(1)
				.setNamesOfArguments("path")
			    .build();
		//@formatter:on
	}

	// @Override
	// public void onCmd(CmdArgContainer args) {
	// final ArchiveReader archive = resources.getArchive();
	// if (archive == null) {
	// throw new ArchiveNotSetException("Archive not set. Use 'help' to determine how to set the wildstar archive.");
	// }
	//
	// final ArchivePath archivePath = resources.getInnerArchivePath();
	//
	// final String target = args.getArg(0);
	// if (File.separator.equals(target)) {
	// archivePath.toRoot();
	// }
	//
	// final ArchivePath newArchivePath = archivePath.copy();
	//
	// final String[] steps = target.split(Pattern.quote(File.separator));
	// try {
	// for (final String s : steps) {
	// newArchivePath.resolve(s).resolve(archive.getRootFolder());
	// archivePath.setTo(newArchivePath);
	// }
	// } catch (final IdxEntryNotFound e) {
	// throw new InvalidFilePathException(String.format("Entry '%s' not found.", e.getMessage()));
	// }
	// if (!archive.getRootFolder().isDir()) {
	// throw new InvalidFilePathException("Path within the archive does not point to a directory");
	// }
	// sendMsg("Set path to " + archivePath);
	// }

	@Override
	public void onCommand(CommandArguments args) {
		final String target = args.getArg(0);
		App.getInstance().getPlugIn(ArchivePlugIn.class).changeDirectory(target);
	}

	@Override
	public void onHelp(CommandArguments args) {
		// TODO Auto-generated method stub

	}

}
