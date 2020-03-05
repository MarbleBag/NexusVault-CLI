package nexusvault.cli.plugin.make;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import nexusvault.cli.App;
import nexusvault.cli.core.cmd.ArgumentDescription;
import nexusvault.cli.core.cmd.Arguments;
import nexusvault.cli.core.cmd.CommandDescription;
import nexusvault.cli.plugin.AbstractCommandHandler;
import nexusvault.cli.plugin.export.PathUtil;
import nexusvault.format.tex.TextureImage;
import nexusvault.format.tex.util.TextureMipMapGenerator;

final class MakeTextureCmd extends AbstractCommandHandler {

	@Override
	public CommandDescription getCommandDescription() {
		// @formatter:off
		return CommandDescription.newInfo()
				.setCommandName("make-tex")
				.setDescription("Creates a ws compatible texture file")
				.addNamedArgument(
							ArgumentDescription.newInfo()
							.setName("type").setDescription("")
							.setRequired(true).setArguments(false).setNumberOfArguments(1).setNamesOfArguments("type").build()
						)
				.addNamedArgument(
							ArgumentDescription.newInfo()
							.setName("filename").setDescription("Nname of the created file. If not set, the name of the file will be equal to the name of the first passed image file")
							.setRequired(false).setArguments(false).setNumberOfArguments(1).setNamesOfArguments("name").build()
						)
				.namedArgumentsDone()
//				.setRequired(false)
//				.setArguments(false)
//				.setNumberOfArgumentsUnlimited()
//				.setNamesOfArguments("textype filename texture-path-1 [channel selection RGBA], texture-path-2 [RGBA], mipmaps ...")
				.build();
		//@formatter:on
	}

	@Override
	public void onCommand(Arguments args) {
		if (args.getUnnamedArgumentSize() < 2) {
			throw new IllegalArgumentException(); // TODO
		}

		final var texType = getTexType(args.getUnnamedArgs()[0]);
		final var fileName = args.isNamedArgumentSet("filename") ? args.getArgumentByName("filename").getValue() : null;

		for (int i = 2; i < args.getNumberOfArguments(); ++i) {
			if (isValidPath(args.getArg(i))) {
				if (i + 1 < args.getNumberOfArguments() && isValidChannelSelection(args.getArg(i + 1))) {
					// TODO
					i += 1;
				} else {
					// TODO
				}
			}
		}

		int numberOfMipMaps = 0;
		if (isValidNumber(args.getArg(args.getNumberOfArguments() - 1))) {
			numberOfMipMaps = parseNumber(args.getArg(args.getNumberOfArguments() - 1));
		}

		final var images = new ArrayList<TextureImage>();

		if (numberOfMipMaps > 0) {
			final var newImages = TextureMipMapGenerator.buildMipMaps(images.get(0), numberOfMipMaps);
			images.clear();
			for (final var image : newImages) {
				images.add(image);
			}
		}

		final var textureWriter = nexusvault.format.tex.TextureWriter.buildDefault();
		final var textureBinary = textureWriter.write(texType, images.toArray((n) -> new TextureImage[n]));

		final var outputFolder = getOutputFolder();
		Files.createDirectories(outputFolder);
		final var outputFile = outputFolder.resolve(fileName + ".tex");

		try (var writer = Files.newByteChannel(outputFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
			writer.write(textureBinary);
		}

		final String imageName = PathUtil.getNameWithoutExtension(dataName);

		// textureWriter.write(texType, null)

		// TODO Auto-generated method stub

	}

	public Path getOutputFolder() {
		return App.getInstance().getAppConfig().getOutputPath().resolve("make");
	}

	private int parseNumber(String arg) {
		// TODO Auto-generated method stub
		return 0;
	}

	private boolean isValidNumber(String arg) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean isValidPath(String arg) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean isValidChannelSelection(String arg) {
		// TODO Auto-generated method stub
		return false;
	}

	private nexusvault.format.tex.TexType getTexType(String argument) {
		if (argument == null || argument.isBlank() || argument.isEmpty()) {
			return null;
		}
		argument = argument.toUpperCase();

		final var texTypes = nexusvault.format.tex.TexType.values();
		for (final var texType : texTypes) {
			if (texType.name().toUpperCase().equals(argument)) {
				return texType;
			}
		}
		return null;
	}

	@Override
	public String onHelp(Arguments args) {
		// TODO
		return null;
	}

}
