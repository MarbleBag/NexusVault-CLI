package nexusvault.cli.extensions.convert.converter.m3;

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
public final class M32GltfFactory implements ConverterFactory {

	private boolean includeTextures = true;

	private M32GltfFactory(ConverterExtension extension, InitializationHelper helper) {
		helper.addCommandHandler(new CommandHandler() {
			@Override
			public CommandDescription getCommandDescription() {
				// @formatter:off
				return CommandDescription.newInfo()
						.setCommandName("config-m32gltf")
						.setDescription("Config for the m32gltf converter.")
						.addNamedArgument(ArgumentDescription.newInfo()
								.setName("texture")
								.setDescription("???")
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
				if (args.isNamedArgumentSet("texture")) {
					final var split = args.getArgumentByName("texture").getValue();
					switch (split.toUpperCase()) {
						case "NO":
						case "OFF":
							M32GltfFactory.this.includeTextures = false;
							break;
						case "YES":
						case "ON":
							M32GltfFactory.this.includeTextures = true;
							break;
						default:
							M32GltfFactory.this.includeTextures = !M32GltfFactory.this.includeTextures;
					}
				}
			}

			@Override
			public String onHelp() {
				return null;
			}
		});
	}

	@Override
	public String getId() {
		return "m32gltf";
	}

	@Override
	public int getPriority() {
		return 3;
	}

	@Override
	public Set<String> getAcceptedFileExtensions() {
		return Collections.singleton("m3");
	}

	@Override
	public Converter createConverter(ConverterOptions options) {
		return new M32Gltf(this.includeTextures);
	}

}
