package nexusvault.cli.core.cmd;

public final class Argument {
	private final org.apache.commons.cli.Option option;

	public Argument(org.apache.commons.cli.Option option) {
		this.option = option;
	}

	public String getName() {
		if (this.option.getLongOpt() == null) {
			return this.option.getOpt();
		} else {
			return this.option.getLongOpt();
		}
	}

	public boolean hasName(String name) {
		if (name == null) {
			throw new IllegalArgumentException();
		}
		return name.equalsIgnoreCase(this.option.getOpt()) || name.equalsIgnoreCase(this.option.getLongOpt());
	}

	public String[] getValues() {
		final var values = this.option.getValues();
		return values == null ? new String[0] : values;
	}

	public String getValue() {
		return this.option.getValue();
	}
}