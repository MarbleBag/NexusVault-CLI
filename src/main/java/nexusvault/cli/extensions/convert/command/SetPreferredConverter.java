package nexusvault.cli.extensions.convert.command;

import java.util.HashSet;

import nexusvault.cli.core.App;
import nexusvault.cli.core.AutoInstantiate;
import nexusvault.cli.core.cmd.AbstractCommandHandler;
import nexusvault.cli.core.cmd.ArgumentDescription;
import nexusvault.cli.core.cmd.Arguments;
import nexusvault.cli.core.cmd.CommandDescription;
import nexusvault.cli.extensions.convert.ConverterExtension;

@AutoInstantiate
public final class SetPreferredConverter extends AbstractCommandHandler {

	@Override
	public CommandDescription getCommandDescription() {
		// @formatter:off
		return CommandDescription.newInfo()
				.setCommandName("preferred-converter")
				.setDescription("Sets the preferred converter for a given file format")
				.addNamedArgument(ArgumentDescription.newInfo()
						.setName("extension")
						.setNameShort("ext")
						.setDescription("File extension")
						.setRequired(true)
						.setArguments(false)
						.setNumberOfArguments(1)
						.setNamesOfArguments("extension")
						.build())
				.addNamedArgument(ArgumentDescription.newInfo()
						.setName("id")
						.setDescription("converter id")
						.setRequired(true)
						.setArguments(false)
						.setNumberOfArguments(1)
						.setNamesOfArguments("id")
						.build())
				.namedArgumentsDone()
				.build();
		//@formatter:on
	}

	@Override
	public void onCommand(Arguments args) {
		final var extension = args.getArgumentByName("extension");
		final var id = args.getArgumentByName("id");
		final var converterExtension = App.getInstance().getExtension(ConverterExtension.class);
		try {
			converterExtension.setPreferredConverterIdForFileExtension(extension.getValue(), id.getValue());
		} catch (final Exception e) {
			sendMsg("An error occured");
			sendMsg(e.getMessage());
		}
	}

	@Override
	public String onHelp() {
		final var extension = App.getInstance().getExtension(ConverterExtension.class);
		final var builder = new StringBuilder();
		builder.append("File converter:").append("\n");
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
