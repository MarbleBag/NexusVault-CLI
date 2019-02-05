package nexusvault.cli.plugin.archive;

import nexusvault.cli.App;
import nexusvault.cli.Command;
import nexusvault.cli.CommandArguments;
import nexusvault.cli.CommandInfo;

final class NavigatorListDirectoryContentCmd implements Command {

	@Override
	public CommandInfo getCommandInfo() {
		// @formatter:off
		return CommandInfo.newInfo()
				.setName("ls")
				.setDescription("list the content of TODO")
				.setRequired(false)
				.setNoArguments()
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
	// final IdxEntry selectedEntry = archivePath.resolve(archive.getRootFolder());
	//
	// if (selectedEntry.isFile()) {
	// sendMsg("File: " + selectedEntry.fullName());
	// } else {
	// for (final IdxEntry child : selectedEntry.asDirectory().getChilds()) {
	// if (child.isDir()) {
	// sendMsg("Dir: " + child.fullName());
	// } else if (child.isFile()) {
	// sendMsg("File: " + child.fullName());
	// }
	// }
	// }
	// }

	@Override
	public void onCommand(CommandArguments args) {
		App.getInstance().getPlugIn(ArchivePlugIn.class).listDirectoryContent();
	}

	@Override
	public void onHelp(CommandArguments args) {
		// TODO Auto-generated method stub

	}

}
