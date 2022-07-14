package nexusvault.cli.extensions.archive.command;

import java.io.IOException;

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

		final var archives = extension.getArchives();
		if (archives.isEmpty()) {
			sendMsg("No vaults are loaded. Use 'help' to learn how to load them");
			return;
		}

		final var path = extension.getPathWithinArchives();

		for (final var archive : archives) {
			final var entry = archive.find(path);
			if (entry.isEmpty()) {
				continue;
			}

			sendMsg("Archive: '" + archive.getSource() + "'");

			final var resolvedEntry = entry.get();
			if (resolvedEntry.isFile()) {
				sendMsg("\tFile: " + resolvedEntry.getFullName());
			} else {
				try {
					for (final var child : resolvedEntry.asDirectory().getEntries()) {
						if (child.isDirectory()) {
							sendMsg("\tDir: " + child.getFullName());
						} else {
							sendMsg("\tFile: " + child.getFullName());
						}
					}
				} catch (final IOException e) {
					sendMsg("\tUnable to load childs: " + e.getMessage());
				}
			}
		}
	}

	@Override
	public String onHelp() {
		return null;
	}

}
