package nexusvault.cli.core;

import java.nio.file.Path;

public final class PathUtil {
	private PathUtil() {
	}

	public static Path addFileExtension(Path path, String extension) {
		final var addedFileExtension = addFileExtension(path.getFileName().toString(), extension);
		return path.resolveSibling(addedFileExtension);
	}

	public static Path replaceFileExtension(Path path, String extension) {
		final var replaceFileExtension = replaceFileExtension(path.getFileName().toString(), extension);
		return path.resolveSibling(replaceFileExtension);
	}

	public static String addFileExtension(String file, String extension) {
		if (extension != null && !extension.isBlank()) {
			return file + "." + extension;
		}
		return file;
	}

	public static String replaceFileExtension(String file, String extension) {
		return addFileExtension(getFileName(file), extension);
	}

	public static Path addFileNameSuffix(Path path, String fileName) {
		final var fullFileName = getFullFileName(path);
		final var extension = getFileExtension(fullFileName);
		final var currentFileName = getFileName(fullFileName);
		return path.resolveSibling(addFileExtension(currentFileName + fileName, extension));
	}

	public static Path addFileNamePrefix(Path path, String fileName) {
		final var fullFileName = getFullFileName(path);
		final var extension = getFileExtension(fullFileName);
		final var currentFileName = getFileName(fullFileName);
		return path.resolveSibling(addFileExtension(fileName + currentFileName, extension));
	}

	public static Path replaceFileName(Path path, String fileName) {
		final var extension = getFileExtension(path);
		return path.resolveSibling(addFileExtension(fileName, extension));
	}

	public static String getFullFileName(Path path) {
		return getFullFileName(path.getFileName().toString());
	}

	public static String getFullFileName(String fullFileName) {
		return fullFileName;
	}

	public static String getFileName(Path path) {
		return getFileName(getFullFileName(path));
	}

	public static String getFileName(String fullFileName) {
		final int idx = fullFileName.lastIndexOf(".");
		return idx > 0 ? fullFileName.substring(0, idx) : fullFileName;
	}

	public static String getFileExtension(Path path) {
		return getFileExtension(path.getFileName().toString());
	}

	public static String getFileExtension(String fullFileName) {
		final int idx = fullFileName.lastIndexOf(".");
		return idx > 0 ? fullFileName.substring(idx + 1) : "";
	}
}
