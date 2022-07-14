package nexusvault.cli.extensions.stitcher.command;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

import nexusvault.cli.core.App;
import nexusvault.cli.core.AutoInstantiate;
import nexusvault.cli.core.cmd.AbstractCommandHandler;
import nexusvault.cli.core.cmd.ArgumentDescription;
import nexusvault.cli.core.cmd.Arguments;
import nexusvault.cli.core.cmd.CommandDescription;

@AutoInstantiate
public final class Stitch extends AbstractCommandHandler {

	@Override
	public CommandDescription getCommandDescription() {
		// @formatter:off
		final var builder = CommandDescription.newInfo()
				.setCommandName("stitch")
				.setDescription("Stitches a texture together")
				.addNamedArgument(ArgumentDescription.newInfo()
						.setName("path")
						.setDescription("path to folders or images")
						.setRequired(true)
						.setArguments(false)
						.setNumberOfArgumentsUnlimited()
						.build())
				.namedArgumentsDone();
		//@formatter:on

		return builder.build();
	}

	@Override
	public void onCommand(Arguments args) {
		final var lookupPaths = new Path[] { App.getInstance().getAppConfig().getOutputPath(), App.getInstance().getAppConfig().getApplicationPath() };
		final var targets = new HashSet<Path>();
		boolean allFilesFound = true;
		searchFiles: for (final var value : args.getArgumentByName("path").getValues()) {
			final var path = Path.of(value);
			if (Files.exists(path)) {
				targets.add(path);
				continue;
			}

			if (!path.isAbsolute()) {
				for (final var parentPath : lookupPaths) {
					final var newPath = parentPath.resolve(path);
					if (Files.exists(newPath)) {
						targets.add(newPath);
						continue searchFiles; // done
					}
				}
			}

			sendMsg(() -> String.format("File not found: %s", value));
			allFilesFound = false;
		}

		// TODO Auto-generated method stub

	}

	@Override
	public String onHelp() {
		// TODO Auto-generated method stub
		return null;
	}
}