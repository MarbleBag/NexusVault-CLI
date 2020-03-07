package nexusvault.cli.plugin.export;

import nexusvault.archive.IdxPath;

public final class PathUtil {
	public static String getFolder(IdxPath path) {
		return path.getParent().getFullName();
	}

	public static String getNameWithoutExtension(IdxPath path) {
		final String name = path.getLastName();
		final int idx = name.lastIndexOf(".");
		return idx > 0 ? name.substring(0, idx) : name;
	}

	public static String getExtension(IdxPath path) {
		final String name = path.getLastName();
		final int idx = name.lastIndexOf(".");
		return idx > 0 ? name.substring(idx + 1) : "";
	}

	public static String getFullName(IdxPath path) {
		return path.getFullName();
	}
}