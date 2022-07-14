package nexusvault.cli.core.cmd;

import java.util.Arrays;

public final class ArgumentDescription {

	public static interface Builder1 {
		ArgumentDescription.Builder2 setName(String name);
	}

	public static interface Builder2 {
		ArgumentDescription.Builder3 setDescription(String name);

		ArgumentDescription.Builder2 setNameShort(String name);
	}

	public static interface Builder3 {
		ArgumentDescription.Builder4 setRequired(boolean isCommandRequired);
	}

	public static interface Builder4 {
		ArgumentDescription.Builder5 setArguments(boolean areArgumentsOptional);

		ArgumentDescription.BuilderEnd setNoArguments();
	}

	public static interface Builder5 {
		ArgumentDescription.Builder6 setNumberOfArguments(int number);

		ArgumentDescription.Builder6 setNumberOfArgumentsUnlimited();

		ArgumentDescription.Builder5 setValueSeparator();

		ArgumentDescription.Builder5 setValueSeparator(char separator);
	}

	public static interface Builder6 {
		ArgumentDescription.BuilderEnd setNamesOfArguments(String... names);

		ArgumentDescription build();
	}

	public static interface BuilderEnd {
		ArgumentDescription build();
	}

	private static class BuilderData {
		protected String[] argumentNames = new String[0];
		protected boolean argumentOptional;
		protected String description;
		protected String name;
		protected String nameShort;
		protected int numberOfArgs = 0;
		protected boolean required;
		protected char valueSeparator;
	}

	public static final class CompactBuilder extends BuilderData {
		public ArgumentDescription build() {
			return new ArgumentDescription(this);
		}

		public CompactBuilder isArgumentOptional(boolean areArgumentsOptional) {
			this.argumentOptional = areArgumentsOptional;
			return this;
		}

		public CompactBuilder setDescription(String description) {
			this.description = description;
			return this;
		}

		public CompactBuilder setName(String name) {
			this.name = name;
			return this;
		}

		public CompactBuilder setNameShort(String nameShort) {
			this.nameShort = nameShort;
			return this;
		}

		public CompactBuilder setNamesOfArguments(String... names) {
			this.argumentNames = names == null ? new String[0] : names;
			return this;
		}

		public CompactBuilder setNumberOfArguments(int number) {
			if (number <= 0) {
				throw new IllegalArgumentException(
						String.format("Command was defined to have arguments. Expected number of arguments: 1 or greater than 1, was %d", number));
			}
			this.numberOfArgs = number;
			return this;
		}

		public CompactBuilder setNumberOfArgumentsUnlimited() {
			this.numberOfArgs = NUMBER_OF_ARGUMENTS_UNLIMITED;
			return this;
		}

		public CompactBuilder setRequired(boolean isCommandRequired) {
			this.required = isCommandRequired;
			return this;
		}

		public CompactBuilder setValueSeparator() {
			this.valueSeparator = '=';
			return this;
		}

		public CompactBuilder setValueSeparator(char separator) {
			this.valueSeparator = separator;
			return this;
		}
	}

	private static final class Builder extends BuilderData implements ArgumentDescription.Builder1, ArgumentDescription.Builder2, ArgumentDescription.Builder3,
			ArgumentDescription.Builder4, ArgumentDescription.Builder5, ArgumentDescription.Builder6, ArgumentDescription.BuilderEnd {

		@Override
		public ArgumentDescription build() {
			return new ArgumentDescription(this);
		}

		@Override
		public Builder5 setArguments(boolean areArgumentsOptional) {
			this.argumentOptional = areArgumentsOptional;
			return this;
		}

		@Override
		public Builder3 setDescription(String description) {
			this.description = description;
			return this;
		}

		@Override
		public Builder2 setName(String name) {
			this.name = name;
			return this;
		}

		@Override
		public Builder2 setNameShort(String nameShort) {
			this.nameShort = nameShort;
			return this;
		}

		@Override
		public BuilderEnd setNamesOfArguments(String... names) {
			this.argumentNames = names == null ? new String[0] : names;
			return this;
		}

		@Override
		public BuilderEnd setNoArguments() {
			return this;
		}

		@Override
		public Builder6 setNumberOfArguments(int number) {
			if (number <= 0) {
				throw new IllegalArgumentException(
						String.format("Command was defined to have arguments. Expected number of arguments: 1 or greater than 1, was %d", number));
			}
			this.numberOfArgs = number;
			return this;
		}

		@Override
		public Builder6 setNumberOfArgumentsUnlimited() {
			this.numberOfArgs = NUMBER_OF_ARGUMENTS_UNLIMITED;
			return this;
		}

		@Override
		public Builder4 setRequired(boolean isCommandRequired) {
			this.required = isCommandRequired;
			return this;
		}

		@Override
		public Builder5 setValueSeparator() {
			this.valueSeparator = '=';
			return this;
		}

		@Override
		public Builder5 setValueSeparator(char separator) {
			this.valueSeparator = separator;
			return this;
		}

	}

	private static final int NUMBER_OF_ARGUMENTS_UNLIMITED = -1;

	public static ArgumentDescription.Builder1 newInfo() {
		return new Builder();
	}

	private final String[] argumentNames;

	private final boolean argumentOptional;

	private final String description;

	private final String name;

	private final String nameShort;

	private final int numberOfArgs;

	private final boolean required;

	private final char valueSeparator;

	private ArgumentDescription(ArgumentDescription.BuilderData builder) {
		this.name = builder.name;
		this.nameShort = builder.nameShort;
		this.description = builder.description;
		this.required = builder.required;
		this.numberOfArgs = builder.numberOfArgs;
		this.argumentOptional = builder.argumentOptional;
		this.argumentNames = builder.argumentNames;
		this.valueSeparator = builder.valueSeparator;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ArgumentDescription other = (ArgumentDescription) obj;
		if (!Arrays.equals(this.argumentNames, other.argumentNames)) {
			return false;
		}
		if (this.argumentOptional != other.argumentOptional) {
			return false;
		}
		if (this.description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!this.description.equals(other.description)) {
			return false;
		}
		if (this.name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!this.name.equals(other.name)) {
			return false;
		}
		if (this.nameShort == null) {
			if (other.nameShort != null) {
				return false;
			}
		} else if (!this.nameShort.equals(other.nameShort)) {
			return false;
		}
		if (this.numberOfArgs != other.numberOfArgs) {
			return false;
		}
		if (this.required != other.required) {
			return false;
		}
		return true;
	}

	public boolean hasValueSeparator() {
		return this.valueSeparator > 0;
	}

	public char getValueSeparator() {
		return this.valueSeparator;
	}

	public String[] getArgumentNames() {
		return this.argumentNames;
	}

	public String getDescription() {
		return this.description;
	}

	public boolean hasDescription() {
		return this.description != null && !this.description.isEmpty();
	}

	public String getName() {
		return this.name;
	}

	public boolean hasName() {
		return this.name != null && !this.name.isEmpty();
	}

	public String getNameShort() {
		return this.nameShort;
	}

	public boolean hasNameShort() {
		return this.nameShort != null && !this.nameShort.isEmpty();
	}

	public int getNumberOfArguments() {
		return this.numberOfArgs;
	}

	public boolean isNumberOfArgumentsUnlimited() {
		return this.numberOfArgs == NUMBER_OF_ARGUMENTS_UNLIMITED;
	}

	public boolean hasArguments() {
		return this.numberOfArgs > 0 || this.numberOfArgs == NUMBER_OF_ARGUMENTS_UNLIMITED;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(this.argumentNames);
		result = prime * result + (this.argumentOptional ? 1231 : 1237);
		result = prime * result + (this.description == null ? 0 : this.description.hashCode());
		result = prime * result + (this.name == null ? 0 : this.name.hashCode());
		result = prime * result + (this.nameShort == null ? 0 : this.nameShort.hashCode());
		result = prime * result + this.numberOfArgs;
		result = prime * result + (this.required ? 1231 : 1237);
		return result;
	}

	public boolean isArgumentOptional() {
		return this.argumentOptional;
	}

	public boolean isCommandRequired() {
		return this.required;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder().append("ArgumentInfo");
		builder.append("[name=").append(this.name);
		builder.append(", nameShort=").append(this.nameShort);
		builder.append(", description=").append(this.description);
		builder.append(", required=").append(this.required);

		if (hasArguments()) {
			builder.append(", argumentOptional=");
			builder.append(this.argumentOptional);
			builder.append(", numberOfArgs=");
			builder.append(this.numberOfArgs);
			builder.append(", argumentNames=");
			builder.append(Arrays.toString(this.argumentNames));
		}

		builder.append("]");
		return builder.toString();
	}

}