package nexusvault.cli.extensions.convert.converter.tex;

import java.util.Collections;
import java.util.Set;

import nexusvault.cli.core.AutoInstantiate;
import nexusvault.cli.core.cmd.ArgumentDescription;
import nexusvault.cli.core.cmd.Arguments;
import nexusvault.cli.core.cmd.CommandDescription;
import nexusvault.cli.core.cmd.CommandHandler;
import nexusvault.cli.core.extension.AbstractExtension.InitializationHelper;
import nexusvault.cli.extensions.convert.Converter;
import nexusvault.cli.extensions.convert.ConverterExtension;
import nexusvault.cli.extensions.convert.ConverterFactory;
import nexusvault.cli.extensions.convert.ConverterOptions;

@AutoInstantiate
public final class Tex2PngFactory implements ConverterFactory {

	private boolean splitImage = true;

	private Tex2PngFactory(ConverterExtension extension, InitializationHelper helper) {
		helper.addCommandHandler(new CommandHandler() {
			@Override
			public CommandDescription getCommandDescription() {
				// @formatter:off
				return CommandDescription.newInfo()
						.setCommandName("convert-tex2png")
						.setDescription("Config for the tex2png converter.")
						.addNamedArgument(ArgumentDescription.newInfo()
								.setName("split")
								.setDescription("Toggles the splitting of textures when exported exporter. By default, this is on. Can be set directly to 'on' or 'off'. Some textures use their color and alpha channels to store different information, like normal maps, masks and roughness. If set to on, the exporter will export the original image, then images containing specific sub-information")
								.setRequired(false)
								.setArguments(false)
								.setNumberOfArguments(1)
								.setNamesOfArguments("on/off")
								.build())
						.namedArgumentsDone()
						.build();
				//@formatter:on
			}

			@Override
			public void onCommand(Arguments args) {
				if (args.hasUnnamedArgValue("split")) {
					final var split = args.getArgumentByName("split").getValue();
					switch (split.toUpperCase()) {
						case "NO":
						case "OFF":
							Tex2PngFactory.this.splitImage = false;
							break;
						case "YES":
						case "ON":
							Tex2PngFactory.this.splitImage = true;
							break;
						default:
							Tex2PngFactory.this.splitImage = !Tex2PngFactory.this.splitImage;
					}
				}
			}

			@Override
			public String onHelp() {
				final String msg = String.format("Splitting of textures is set to %s, use 'on' or 'off' to directly set the value",
						Tex2PngFactory.this.splitImage);
				return msg;
			}
		});
	}

	@Override
	public String getId() {
		return "tex2png";
	}

	@Override
	public Set<String> getAcceptedFileExtensions() {
		return Collections.singleton("tex");
	}

	@Override
	public Converter createConverter(ConverterOptions options) {
		return new Tex2Png(this.splitImage);
	}

}
