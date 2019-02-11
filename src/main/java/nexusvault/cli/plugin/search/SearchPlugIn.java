package nexusvault.cli.plugin.search;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import nexusvault.archive.IdxDirectory;
import nexusvault.archive.IdxEntry;
import nexusvault.archive.IdxEntryNotAFile;
import nexusvault.archive.IdxEntryNotFound;
import nexusvault.archive.IdxFileLink;
import nexusvault.archive.util.ArchivePath;
import nexusvault.archive.util.IdxDirectoryTraverser;
import nexusvault.archive.util.IdxEntryVisitor.EntryFilterResult;
import nexusvault.archive.util.ReportingIdxFileCollector;
import nexusvault.cli.App;
import nexusvault.cli.Command;
import nexusvault.cli.plugin.AbstPlugIn;
import nexusvault.cli.plugin.archive.ArchivePlugIn;
import nexusvault.cli.plugin.archive.SourcedVaultReader;

public final class SearchPlugIn extends AbstPlugIn {

	private final static String REPORT_FILE = "search_result.txt";

	private final Map<Path, Set<IdxFileLink>> searchResults = new HashMap<>();
	private boolean loadFromFile = true;

	public SearchPlugIn() {
		final List<Command> cmds = new ArrayList<>();
		cmds.add(new NavigatorSearchArchiveCmd());
		setCommands(cmds);
	}

	public Map<Path, Set<IdxFileLink>> getLastSearchResults() {
		if (loadFromFile) {
			sendMsg("No active search found. Load previous search from " + REPORT_FILE);
			return rebuildReportReferences();
		}
		return Collections.unmodifiableMap(searchResults);
	}

	public void searchArchive(SearchRequest request) {
		final ArchivePlugIn archivePlugIn = App.getInstance().getPlugIn(ArchivePlugIn.class);
		final List<SourcedVaultReader> vaults = archivePlugIn.loadArchives();
		if (vaults.isEmpty()) {
			sendMsg("No vaults are loaded. Use 'help' to learn how to load them");
			return;
		}

		searchResults.clear();

		final ArchivePath path = archivePlugIn.getPathWithinArchives();

		int maxResults = request.getMaxResults();
		int resultsFound = 0;
		for (final SourcedVaultReader vault : vaults) {
			sendMsg("Scanning archive: " + vault.getSource());
			final IdxDirectory rootDirectory = vault.getReader().getRootFolder();
			if (path.isResolvable(rootDirectory)) {
				final IdxEntry resolvedEntry = path.resolve(rootDirectory);
				final Set<IdxFileLink> results = search(resolvedEntry, request.getPattern(), maxResults);
				maxResults -= results.size();
				resultsFound += results.size();
				searchResults.put(vault.getSource(), Collections.unmodifiableSet(results));
			}
		}

		loadFromFile = false;
		sendMsg(String.format("Found a total amount of %d files which match at least one search criteria", resultsFound));

		try {
			writeReport();
		} catch (final IOException e) {
			sendMsg("An error occured:");
			sendMsg(e.getMessage());
		}
	}

	public void writeReport() throws IOException {
		sendMsg(String.format("Saving search results to file %s ...", REPORT_FILE));

		final List<String> linesToWrite = new LinkedList<>();
		for (final Entry<Path, Set<IdxFileLink>> searchEntry : searchResults.entrySet()) {
			// linesToWrite.add(searchEntry.getKey().toString());
			// linesToWrite.add(searchEntry.getValue().size() + "");
			searchEntry.getValue().stream().map(f -> f.fullName()).sorted().forEach(linesToWrite::add);
		}

		final Path reportFile = App.getInstance().getAppConfig().getReportFolder().resolve(REPORT_FILE);
		Files.createDirectories(reportFile.getParent());
		Files.write(reportFile, linesToWrite, Charset.forName("UTF8"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
				StandardOpenOption.WRITE);

		sendMsg("... done.");
	}

	private Map<Path, Set<IdxFileLink>> rebuildReportReferences() {
		final ArchivePlugIn archivePlugIn = App.getInstance().getPlugIn(ArchivePlugIn.class);
		final List<SourcedVaultReader> vaults = archivePlugIn.loadArchives();
		if (vaults.isEmpty()) {
			sendMsg("No vaults are loaded. Use 'help' to learn how to load them");
			return Collections.emptyMap();
		}

		final Map<Path, Set<IdxFileLink>> result = new HashMap<>();
		for (final SourcedVaultReader vault : vaults) {
			result.put(vault.getSource(), new HashSet<>());
		}

		List<String> reportSearchResults = null;
		try {
			reportSearchResults = loadReport();
		} catch (final NoSuchFileException e1) {
			sendMsg("No previous executed search found at");
			sendMsg(e1.getMessage());
			return Collections.emptyMap();
		} catch (final IOException e0) {
			sendMsg("An error occured while loading search results from report file:");
			sendMsg(e0.getClass().toString());
			sendMsg(e0.getMessage());
			return Collections.emptyMap();
		}

		for (final String fileName : reportSearchResults) {
			for (final SourcedVaultReader vault : vaults) {
				final IdxFileLink file = findFile(vault.getReader().getRootFolder(), fileName);
				if (file != null) {
					result.get(vault.getSource()).add(file);
					break;
				}
			}
		}

		return result;
	}

	private List<String> loadReport() throws IOException {
		final List<String> result = new LinkedList<>();
		final Path reportFile = App.getInstance().getAppConfig().getReportFolder().resolve(REPORT_FILE);
		try (Stream<String> stream = Files.lines(reportFile, Charset.forName("UTF8"))) {
			stream.forEach(result::add);
		}
		return result;
	}

	private IdxFileLink findFile(IdxDirectory root, String fileName) {
		try {
			return root.getFile(fileName);
		} catch (IdxEntryNotFound | IdxEntryNotAFile e) {
			return null;
		}
	}

	private Set<IdxFileLink> search(IdxEntry root, List<Pattern> regex, int maxResults) {
		if (root.isFile()) {
			for (final Pattern pattern : regex) {
				if (pattern.matcher(root.fullName()).find()) {
					return Collections.singleton(root.asFile());
				}
			}
			return Collections.emptySet();
		}

		final IdxDirectory rootFolder = root.asDirectory();
		final int numberOfFiles = rootFolder.countSubTree();

		final Predicate<IdxFileLink> fileTester = (file) -> {
			for (final Pattern p : regex) {
				if (p.matcher(file.fullName()).find()) {
					return true;
				}
			}
			return false;
		};

		final ReportingIdxFileCollector.ReportListener listener = new ReportingIdxFileCollector.ReportListener() {

			final int reportAfterNFiles = Math.max(1, Math.min(2000, numberOfFiles / 20));
			private int seenFiles = 0;
			private int reportIn = 0;
			private int foundFiles = 0;

			@Override
			public void visitedFile(IdxFileLink file, boolean predicateTest, EntryFilterResult visitorResult) {
				seenFiles += 1;
				reportIn += 1;
				foundFiles += predicateTest ? 1 : 0;
				if (reportIn >= reportAfterNFiles) {
					final float percentage = (seenFiles / (numberOfFiles + 0f)) * 100;
					final String msg = String.format("Processed files %d of %d (%.2f%%). Found: %d", seenFiles, numberOfFiles, percentage, foundFiles);
					reportIn = 0;
					sendMsg(msg);
				}
			}

		};

		final ReportingIdxFileCollector collector = new ReportingIdxFileCollector(fileTester, maxResults);
		collector.setListener(listener);

		IdxDirectoryTraverser.visitEntries(rootFolder, collector);

		final List<IdxFileLink> matchingFiles = collector.getAndClearResult();

		sendMsg(String.format("Processed files %d of %d (100%%). Found %d files which match at least one search criteria", numberOfFiles, numberOfFiles,
				matchingFiles.size()));

		return new HashSet<>(matchingFiles);
	}

}
