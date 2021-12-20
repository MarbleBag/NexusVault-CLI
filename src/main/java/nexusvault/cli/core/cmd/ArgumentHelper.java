package nexusvault.cli.core.cmd;

public final class ArgumentHelper {
	private ArgumentHelper() {
	}

	public static boolean toBoolean(String value, boolean originalValue) {
		if (value == null) {
			return !originalValue;
		}
    
		switch (value.toUpperCase()) {
			case "N":
			case "NO":
			case "OFF":
			case "F":
			case "FALSE":
				return false;
			case "Y":
			case "YES":
			case "ON":
			case "T":
			case "TRUE":
				return true;
			default:
				return !originalValue;
		}
	}

	public static boolean toBoolean(String value) {
		return toBoolean(value, true);
	}

	public static boolean toBoolean(int value) {
		return value > 0;
	}

}
