package nexusvault.cli.core.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class CommandDescription {

	public static interface Builder1 {
		CommandDescription.Builder2 setCommandName(String name);
	}

	public static interface Builder2 {
		CommandDescription.Builder2 addAlternativeNames(String name);

		CommandDescription.Builder3 setDescription(String name);
	}

	public static interface Builder3 {
		CommandDescription.Builder4 addNamedArgument(ArgumentDescription argInfo);

		CommandDescription.BuilderEnd setNoNamedArguments();
	}

	public static interface Builder4 {
		CommandDescription.Builder4 addNamedArgument(ArgumentDescription argInfo);

		CommandDescription.Builder4 ignoreUnnamedArguments();

		CommandDescription.BuilderEnd namedArgumentsDone();
	}

	public static interface Builder5 {
		CommandDescription.BuilderEnd setNoUnnamedArguments();

		CommandDescription.BuilderEnd setUnnamedArgumentNames(String names);
	}

	public static interface BuilderEnd {
		CommandDescription build();
	}

	private static class BuilderData {
		protected String name;
		protected final List<String> altNames = new ArrayList<>();
		protected String description;
		protected boolean ignoreNoOptions;
		protected final List<ArgumentDescription> args = new ArrayList<>();

	}

	public final static class CompactBuilder extends BuilderData {

		public CommandDescription build() {
			return new CommandDescription(this);
		}

		public CompactBuilder setCommandName(String name) {
			this.name = name;
			return this;
		}

		public CompactBuilder addAlternativeNames(String name) {
			if (name == null || name.isEmpty() || name.isBlank()) {
				throw new IllegalArgumentException();
			}
			this.altNames.add(name);
			return this;
		}

		public CompactBuilder setDescription(String description) {
			this.description = description;
			return this;
		}

		public CompactBuilder addNamedArgument(ArgumentDescription argInfo) {
			if (argInfo == null) {
				throw new IllegalArgumentException();
			}
			this.args.add(argInfo);
			return this;
		}

		public CompactBuilder ignoreUnnamedArguments(boolean value) {
			this.ignoreNoOptions = value;
			return this;
		}

	}

	private static final class Builder extends BuilderData implements CommandDescription.Builder1, CommandDescription.Builder2, CommandDescription.Builder3,
			CommandDescription.Builder4, CommandDescription.BuilderEnd {

		@Override
		public CommandDescription build() {
			return new CommandDescription(this);
		}

		@Override
		public Builder2 setCommandName(String name) {
			this.name = name;
			return this;
		}

		@Override
		public Builder2 addAlternativeNames(String name) {
			if (name == null || name.isEmpty() || name.isBlank()) {
				throw new IllegalArgumentException();
			}
			this.altNames.add(name);
			return this;
		}

		@Override
		public Builder3 setDescription(String description) {
			this.description = description;
			return this;
		}

		@Override
		public BuilderEnd setNoNamedArguments() {
			return this;
		}

		@Override
		public BuilderEnd namedArgumentsDone() {
			return this;
		}

		@Override
		public Builder4 addNamedArgument(ArgumentDescription argInfo) {
			if (argInfo == null) {
				throw new IllegalArgumentException();
			}
			this.args.add(argInfo);
			return this;
		}

		@Override
		public Builder4 ignoreUnnamedArguments() {
			this.ignoreNoOptions = true;
			return this;
		}

	}

	public static CommandDescription.Builder1 newInfo() {
		return new Builder();
	}

	private final String name;
	private final String description;
	private final ArgumentDescription[] args;
	private final boolean ignoreNonOptions;

	private CommandDescription(BuilderData builderData) {
		this.name = builderData.name.strip();
		this.description = builderData.description;
		this.args = builderData.args.toArray(n -> new ArgumentDescription[n]);
		this.ignoreNonOptions = builderData.ignoreNoOptions;

		if (this.name.isEmpty() || this.name.isBlank()) {
			throw new IllegalArgumentException();
		}
		if (this.description == null) {
			throw new IllegalArgumentException();
		}
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
		final CommandDescription other = (CommandDescription) obj;
		return Arrays.equals(this.args, other.args) && Objects.equals(this.description, other.description) && Objects.equals(this.name, other.name);
	}

	public String getCommandDescription() {
		return this.description;
	}

	public boolean hasCommandDescription() {
		return this.description != null && !this.description.isEmpty();
	}

	public String getCommandName() {
		return this.name;
	}

	public boolean hasCommandName() {
		return this.name != null && !this.name.isEmpty();
	}

	public int getNumberOfArguments() {
		return this.args.length;
	}

	public ArgumentDescription[] getArgs() {
		return this.args;
	}

	public boolean hasArguments() {
		return getNumberOfArguments() > 0;
	}

	public boolean ignoreNonOptions() {
		return this.ignoreNonOptions;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(this.args);
		result = prime * result + Objects.hash(this.description, this.name);
		return result;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder().append("CommandInfo");
		builder.append("[name=").append(this.name);
		builder.append(", description=").append(this.description);

		if (hasArguments()) {
			builder.append(", ignoreNOP=").append(this.ignoreNonOptions);
			builder.append(", numberOfArgs=").append(getNumberOfArguments());
			builder.append(", args=").append(Arrays.toString(this.args));
		}

		builder.append("]");
		return builder.toString();
	}

}