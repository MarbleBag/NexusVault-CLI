package nexusvault.cli.extensions.convert.converter.tbl;

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
import nexusvault.format.tbl.converter.CSV;

@AutoInstantiate
public final class Tbl2CsvFactory implements ConverterFactory {

	private String cellDelimiter = ";";

	private Tbl2CsvFactory(ConverterExtension extension, InitializationHelper helper) {
		helper.addCommandHandler(new CommandHandler() {
			@Override
			public CommandDescription getCommandDescription() {
				// @formatter:off
				return CommandDescription.newInfo()
						.setCommandName("convert-tbl2csv")
						.setDescription("Config for the tbl2csv converter.")
						.addNamedArgument(ArgumentDescription.newInfo()
								.setName("delimiter")
								.setDescription("???")
								.setRequired(false)
								.setArguments(false)
								.setNumberOfArguments(1)
								.setNamesOfArguments("char")
								.build())
						.namedArgumentsDone()
						.build();
				//@formatter:on
			}

			@Override
			public void onCommand(Arguments args) {
				if (args.isNamedArgumentSet("delimiter")) {
					setCellDelimiter(args.getArgumentByName("delimiter").getValue());
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
		return "tbl2csv";
	}

	@Override
	public Set<String> getAcceptedFileExtensions() {
		return Collections.singleton("tbl");
	}

	public void setCellDelimiter(String str) {
		this.cellDelimiter = str;
	}

	public String getCellDelimiter() {
		return this.cellDelimiter;
	}

	@Override
	public Converter createConverter(ConverterOptions options) {
		return new Tbl2Csv(new CSV(options.getOrElse("delimiter", getCellDelimiter())));
	}

}
