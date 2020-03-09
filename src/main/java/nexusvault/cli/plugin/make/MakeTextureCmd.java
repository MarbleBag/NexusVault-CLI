package nexusvault.cli.plugin.make;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import nexusvault.cli.App;
import nexusvault.cli.core.cmd.AbstractCommandHandler;
import nexusvault.cli.core.cmd.ArgumentDescription;
import nexusvault.cli.core.cmd.Arguments;
import nexusvault.cli.core.cmd.CommandDescription;
import nexusvault.cli.core.cmd.CommandFormatException;
import nexusvault.format.tex.TexType;
import nexusvault.format.tex.TextureImage;
import nexusvault.format.tex.TextureImageFormat;
import nexusvault.format.tex.util.TextureImageAwtConverter;
import nexusvault.format.tex.util.TextureMipMapGenerator;

final class MakeTextureCmd extends AbstractCommandHandler {

	@Override
	public CommandDescription getCommandDescription() {
		// @formatter:off
		return CommandDescription.newInfo()
				.setCommandName("make-texture")
				.setDescription("Creates a ws compatible texture file")
				.addNamedArgument(
							ArgumentDescription.newInfo()
							.setName("type").setDescription("WS specific texture type.")
							.setRequired(true).setArguments(false).setNumberOfArguments(1).setNamesOfArguments("name").build()
						)
				.addNamedArgument(
							ArgumentDescription.newInfo()
							.setName("filename").setDescription("Name of the created file. If not set, the name of the generated file will be equal to the name of -texture-1")
							.setRequired(false).setArguments(false).setNumberOfArguments(1).setNamesOfArguments("name").build()
						)
				.addNamedArgument(
							ArgumentDescription.newInfo()
							.setName("texture-1").setDescription("Full path to texture. Use quotation marks when the path contains spaces.")
							.setRequired(true).setArguments(false).setNumberOfArguments(1).setNamesOfArguments("path").build()
						)
				.addNamedArgument(
						ArgumentDescription.newInfo()
						.setName("mipmaps").setDescription("How many mip maps the texture should contain. Maximal number of mip maps is 13.")
						.setRequired(false).setArguments(false).setNumberOfArguments(1).setNamesOfArguments("number").build()
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

		final var texType = getTexType(args);
		final var imagePaths = getImageFiles(args);
		final var fileName = getFileName(args, imagePaths);
		final var mipmaps = getMipMaps(args);

		final var images = buildImages(imagePaths);

		if (mipmaps > 0) {
			final var newImages = TextureMipMapGenerator.buildMipMaps(images.get(0), mipmaps);
			images.clear();
			for (final var image : newImages) {
				images.add(image);
			}
		}

		final var textureWriter = nexusvault.format.tex.TextureWriter.buildDefault();
		final var textureBinary = textureWriter.write(texType, images.toArray((n) -> new TextureImage[n]));

		final var outputFolder = getOutputFolder();
		final var outputFile = outputFolder.resolve(fileName + ".tex");

		try {
			Files.createDirectories(outputFolder);
			try (var writer = Files.newByteChannel(outputFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
				writer.write(textureBinary);
			}
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private List<TextureImage> buildImages(List<Path> paths) {
		final var result = new ArrayList<TextureImage>();
		for (final var path : paths) {
			// TODO
			result.add(getTextureImage(path));
		}
		return result;
	}

	private TextureImage getTextureImage(Path path) {
		final var bufferedImage = getBufferedImage(path);
		// TODO TextureImageFormat may not work for all textures.
		return TextureImageAwtConverter.convertToTextureImage(TextureImageFormat.ARGB, bufferedImage);
	}

	private BufferedImage getBufferedImage(Path path) {
		try (var stream = Files.newInputStream(path, StandardOpenOption.READ)) {
			final var img = ImageIO.read(stream);

			BufferedImage result;
			if (img.getColorModel().hasAlpha()) {
				result = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
			} else {
				result = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
			}

			final var g2d = result.createGraphics();
			g2d.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null);
			g2d.dispose();

			return result;
		} catch (final IOException e) {
			throw new CommandFormatException(e);
		}
	}

	private int getMipMaps(Arguments args) {
		if (!args.isNamedArgumentSet("mipmaps")) {
			return 0;
		}
		final var str = args.getArgumentByName("mipmaps").getValue();
		return Integer.parseInt(str);
	}

	private TexType getTexType(Arguments args) {
		return getTexType(args.getArgumentByName("type").getValue());
	}

	private List<Path> getImageFiles(Arguments args) {
		final var files = new ArrayList<String>();
		if (args.isNamedArgumentSet("texture-1")) {
			files.add(args.getArgumentByName("texture-1").getValue());
		}

		final var paths = new ArrayList<Path>();
		for (final var textureValue : files) {
			// TODO validate
			paths.add(Path.of(textureValue));
		}

		return paths;
	}

	private String getFileName(Arguments args, List<Path> imagePaths) {
		if (args.isNamedArgumentSet("filename")) {
			return args.getArgumentByName("filename").getValue();
		}

		var fileName = imagePaths.get(0).getFileName().toString();
		final var fileExt = fileName.lastIndexOf('.');
		if (fileExt >= 0) {
			fileName = fileName.substring(0, fileExt);
		}
		return fileName;
	}

	public Path getOutputFolder() {
		return App.getInstance().getAppConfig().getOutputPath().resolve("make");
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
	public String onHelp() {
		final var builder = new StringBuilder();

		builder.append("Available texture types: ");
		final var excludedTypes = EnumSet.of(TexType.UNKNOWN, TexType.JPEG_TYPE_1, TexType.JPEG_TYPE_2, TexType.JPEG_TYPE_3);
		final var availableTypes = EnumSet.complementOf(excludedTypes);
		final var types = availableTypes.stream().map(String::valueOf).map(String::toLowerCase).collect(Collectors.joining(", "));
		builder.append(types).append('\n');
		builder.append("Example:\n");
		builder.append("make-texture -type argb_1 -mipmaps 11 -texture-1 \"C:\\Nexusvault\\aurin_f_color.png\"");

		return builder.toString();
	}

}
