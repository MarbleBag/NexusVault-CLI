package nexusvault.cli.extensions.archive.command;

import nexusvault.cli.core.App;
import nexusvault.cli.core.AutoInstantiate;
import nexusvault.cli.core.cmd.AbstractCommandHandler;
import nexusvault.cli.core.cmd.Arguments;
import nexusvault.cli.core.cmd.CommandDescription;
import nexusvault.cli.core.cmd.CommandHandler;
import nexusvault.cli.extensions.archive.ArchiveExtension;

@AutoInstantiate
public final class ListDirectoryContent extends AbstractCommandHandler implements CommandHandler {

	@Override
	public CommandDescription getCommandDescription() {
		// @formatter:off
		return CommandDescription.newInfo()
				.setCommandName("ls")
				.setDescription("Lists the contents of the currently selected folder")
				.setNoNamedArguments()
			    .build();
		//@formatter:on
	}

	@Override
	public void onCommand(Arguments args) {
		final var extension = App.getInstance().getExtension(ArchiveExtension.class);

		final var archiveWrappers = extension.getArchives();
		if (archiveWrappers.isEmpty()) {
			sendMsg("No vaults are loaded. Use 'help' to learn how to load them");
			return;
		}

		final var path = extension.getPathWithinArchives();

		for (final var archiveWrapper : archiveWrappers) {
			final var rootFolder = archiveWrapper.getArchive().getRootDirectory();
			if (!path.isResolvable(rootFolder)) {
				continue;
			}

			sendMsg("Archive: '" + archiveWrapper.getSource() + "'");

			final var resolvedEntry = path.resolve(rootFolder);
			if (resolvedEntry.isFile()) {
				sendMsg("\tFile: " + resolvedEntry.getFullName());
			} else {
				for (final var child : resolvedEntry.asDirectory().getChilds()) {
					if (child.isDir()) {
						sendMsg("\tDir: " + child.getFullName());
					} else {
						sendMsg("\tFile: " + child.getFullName());
					}
				}
			}
		}
	}

	@Override
	public String onHelp() {
		return null;
	}

}
