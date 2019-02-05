package nexusvault.cli;

import java.util.Arrays;

public final class CommandInfo {

	public static interface Builder1 {
		CommandInfo.Builder2 setName(String name);
	}

	public static interface Builder2 {
		CommandInfo.Builder3 setDescription(String name);

		CommandInfo.Builder2 setNameShort(String name);
	}

	public static interface Builder3 {
		CommandInfo.Builder4 setRequired(boolean isCommandRequired);
	}

	public static interface Builder4 {
		CommandInfo.Builder5 setArguments(boolean areArgumentsOptional);

		CommandInfo.BuilderEnd setNoArguments();
	}

	public static interface Builder5 {
		CommandInfo.Builder6 setNumberOfArguments(int number);

		CommandInfo.Builder6 setNumberOfArgumentsUnlimited();
	}

	public static interface Builder6 {
		CommandInfo.BuilderEnd setNamesOfArguments(String... names);
	}

	public static interface BuilderEnd {
		CommandInfo build();
	}

	private static final class Builder implements CommandInfo.Builder1, CommandInfo.Builder2, CommandInfo.Builder3, CommandInfo.Builder4, CommandInfo.Builder5,
			CommandInfo.Builder6, CommandInfo.BuilderEnd {

		private String[] argumentNames;
		private boolean argumentOptional;
		private String description;
		private String name;
		private String nameShort;
		private int numberOfArgs = 0;
		private boolean required;

		@Override
		public CommandInfo build() {
			return new CommandInfo(this);
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

	}

	private static final int NUMBER_OF_ARGUMENTS_UNLIMITED = -1;

	public static CommandInfo.Builder1 newInfo() {
		return new Builder();
	}

	private final String[] argumentNames;

	private final boolean argumentOptional;

	private final String description;

	private final String name;

	private final String nameShort;

	private final int numberOfArgs;

	private final boolean required;

	private CommandInfo(CommandInfo.Builder builder) {
		this.name = builder.name;
		this.nameShort = builder.nameShort;
		this.description = builder.description;
		this.required = builder.required;
		this.numberOfArgs = builder.numberOfArgs;
		this.argumentOptional = builder.argumentOptional;
		this.argumentNames = builder.argumentNames;
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
		final CommandInfo other = (CommandInfo) obj;
		if (!Arrays.equals(argumentNames, other.argumentNames)) {
			return false;
		}
		if (argumentOptional != other.argumentOptional) {
			return false;
		}
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (nameShort == null) {
			if (other.nameShort != null) {
				return false;
			}
		} else if (!nameShort.equals(other.nameShort)) {
			return false;
		}
		if (numberOfArgs != other.numberOfArgs) {
			return false;
		}
		if (required != other.required) {
			return false;
		}
		return true;
	}

	public String[] getArgumentNames() {
		return this.argumentNames;
	}

	public String getCommandDescription() {
		return this.description;
	}

	public boolean hasCommandDescription() {
		return (this.description != null) && !this.description.isEmpty();
	}

	public String getCommandName() {
		return this.name;
	}

	public boolean hasCommandName() {
		return (this.name != null) && !this.name.isEmpty();
	}

	public String getCommandNameShort() {
		return this.nameShort;
	}

	public boolean hasCommandNameShort() {
		return (this.nameShort != null) && !this.nameShort.isEmpty();
	}

	public int getNumberOfArguments() {
		return this.numberOfArgs;
	}

	public boolean isNumberOfArgumentsUnlimited() {
		return this.numberOfArgs == NUMBER_OF_ARGUMENTS_UNLIMITED;
	}

	public boolean hasArguments() {
		return (this.numberOfArgs > 0) || (this.numberOfArgs == NUMBER_OF_ARGUMENTS_UNLIMITED);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + Arrays.hashCode(argumentNames);
		result = (prime * result) + (argumentOptional ? 1231 : 1237);
		result = (prime * result) + ((description == null) ? 0 : description.hashCode());
		result = (prime * result) + ((name == null) ? 0 : name.hashCode());
		result = (prime * result) + ((nameShort == null) ? 0 : nameShort.hashCode());
		result = (prime * result) + numberOfArgs;
		result = (prime * result) + (required ? 1231 : 1237);
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
		final StringBuilder builder = new StringBuilder().append("CommandInfo");
		builder.append("[name=").append(name);
		builder.append(", nameShort=").append(nameShort);
		builder.append(", description=").append(description);
		builder.append(", required=").append(required);

		if (hasArguments()) {
			builder.append(", argumentOptional=");
			builder.append(argumentOptional);
			builder.append(", numberOfArgs=");
			builder.append(numberOfArgs);
			builder.append(", argumentNames=");
			builder.append(Arrays.toString(argumentNames));
		}

		builder.append("]");
		return builder.toString();
	}

}