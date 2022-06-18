package nexusvault.cli.extensions.search;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nexusvault.cli.core.App;
import nexusvault.cli.core.AutoInstantiate;
import nexusvault.cli.core.extension.AbstractExtension;
import nexusvault.cli.extensions.archive.ArchiveExtension;
import nexusvault.cli.extensions.archive.NexusArchiveContainer;
import nexusvault.cli.extensions.show.property.FileProperties;
import nexusvault.vault.IdxEntry;
import nexusvault.vault.IdxEntry.IdxFileLink;
import nexusvault.vault.IdxPath;
import nexusvault.vault.NexusArchive;
import nexusvault.vault.util.IdxDirectoryTraverser;
import nexusvault.vault.util.IdxDirectoryTraverser.IdxEntryVisitor.EntryFilterResult;
import nexusvault.vault.util.ReportingIdxFileCollector;

@AutoInstantiate
public final class SearchExtension extends AbstractExtension {

	private static final Logger logger = LogManager.getLogger(SearchExtension.class);

	private final static String REPORT_FILE = "search_result.txt";

	@Override
	protected void initializeExtension(InitializationHelper initializationHelper) {

	}

	@Override
	protected void deinitializeExtension() {
	}

	public List<IdxPath> getLastSearchResults() {
		try {
			return loadReport();
		} catch (final NoSuchFileException e1) {
			sendMsg("No previous executed search found at");
			sendMsg(e1.getMessage());
			return Collections.emptyList();
		} catch (final IOException e0) {
			sendMsg("An error occured while loading search results from report file:");
			sendMsg(e0.getClass().toString());
			sendMsg(e0.getMessage());
			return Collections.emptyList();
		}
	}

	public void searchArchive(SearchRequest request) {
		final var archiveExtension = App.getInstance().getExtension(ArchiveExtension.class);

		final List<NexusArchiveContainer> archives = archiveExtension.getArchives();
		if (archives.isEmpty()) {
			sendMsg("No vaults are loaded. Use 'help' to learn how to load them");
			return;
		}

		final List<IdxPath> searchResults = new LinkedList<>();

		final IdxPath path = archiveExtension.getPathWithinArchives();

		int maxResults = request.getMaxResults();
		int resultsFound = 0;
		for (final NexusArchiveContainer archive : archives) {
			sendMsg("Scanning archive: " + archive.getSource());
			final var entry = archive.find(path);
			if (entry.isPresent()) {
				try {
					final Set<IdxFileLink> results = search(archive.getArchive(), entry.get(), request, maxResults);
					maxResults -= results.size();
					resultsFound += results.size();
					results.stream().map(IdxFileLink::getPath).forEach(searchResults::add);
				} catch (final IOException e) {
					logger.catching(e); // TODO
				}
			}
		}

		sendMsg(String.format("Found a total amount of %d files which match at least one search criteria", resultsFound));

		try {
			writeReport(searchResults);
		} catch (final IOException e) {
			sendMsg("An error occured:");
			sendMsg(e.getMessage());
		}
	}

	private boolean checkProperties(Properties properties, IdxFileLink file) {
		final var fileProperties = FileProperties.getProperties(file);

		// for (final var category : fileProperties.values()) {
		// for (final var property : properties.entrySet()) {
		// final var value = category.get(property.getKey());
		// if (property.getValue() instanceof Boolean) {
		// if ((Boolean) property.getValue()) {
		// if (value != null) {
		// continue;
		// } else {
		// remove = true;
		// break;
		// }
		// } else {
		// if (value == null) {
		// continue;
		// } else {
		// remove = true;
		// break;
		// }
		// }
		// }
		// }
		// if (remove) {
		// break;
		// }
		// }

		final var bla = fileProperties.get("Texture");
		if (bla == null) {
			return false;
		}

		final var sides = bla.get("Sides");
		final var depth = bla.get("Depth");
		if (Integer.parseInt(sides) == 1 && Integer.parseInt(depth) == 1) {
			return false;
		}
		return true;
	}

	public void writeReport(Collection<IdxPath> searchResults) throws IOException {
		sendMsg(String.format("Saving search results to file %s ...", REPORT_FILE));

		final List<String> linesToWrite = new LinkedList<>();
		searchResults.stream().map(IdxPath::getFullName).sorted().forEach(linesToWrite::add);

		final Path reportFile = App.getInstance().getAppConfig().getReportFolder().resolve(REPORT_FILE);
		Files.createDirectories(reportFile.getParent());
		Files.write(reportFile, linesToWrite, Charset.forName("UTF8"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
				StandardOpenOption.WRITE);

		sendMsg("... done.");
	}

	private List<IdxPath> loadReport() throws IOException {
		final List<IdxPath> result = new ArrayList<>();
		final Path reportFile = App.getInstance().getAppConfig().getReportFolder().resolve(REPORT_FILE);
		try (Stream<String> stream = Files.lines(reportFile, Charset.forName("UTF8"))) {
			stream.map(IdxPath::createPathFrom).forEach(result::add);
		}
		return result;
	}

	private Set<IdxFileLink> search(NexusArchive archive, IdxEntry startPoint, SearchRequest request, int maxResults) throws IOException {
		if (startPoint.isFile()) {
			for (final Pattern pattern : request.getPattern()) {
				if (pattern.matcher(startPoint.getFullName()).find()) {
					return Collections.singleton(startPoint.asFile());
				}
			}
			return Collections.emptySet();
		}

		final var directory = startPoint.asDirectory();
		final int numberOfFiles = directory.countNodesInSubTree();

		final Predicate<IdxFileLink> fileTester = (file) -> {
			var matchingPath = false;
			for (final Pattern p : request.getPattern()) {
				if (p.matcher(file.getFullName()).find()) {
					matchingPath = true;
					break;
				}
			}

			if (!matchingPath) {
				return false;
			}

			if (request.getProperties() != null) {
				return checkProperties(request.getProperties(), file);
			}

			return true;
		};

		final ReportingIdxFileCollector.ReportListener listener = new ReportingIdxFileCollector.ReportListener() {

			final int reportAfterNFiles = Math.max(1, Math.min(2000, numberOfFiles / 20));
			private int seenFiles = 0;
			private int reportIn = 0;
			private int foundFiles = 0;

			@Override
			public void visitedFile(IdxFileLink file, boolean predicateTest, EntryFilterResult visitorResult) {
				this.seenFiles += 1;
				this.reportIn += 1;
				this.foundFiles += predicateTest ? 1 : 0;
				if (this.reportIn >= this.reportAfterNFiles) {
					final float percentage = this.seenFiles / (numberOfFiles + 0f) * 100;
					final String msg = String.format("Processed files %d of %d (%.2f%%). Found: %d", this.seenFiles, numberOfFiles, percentage,
							this.foundFiles);
					this.reportIn = 0;
					sendMsg(msg);
				}
			}
		};

		final ReportingIdxFileCollector collector = new ReportingIdxFileCollector(fileTester, maxResults);
		collector.setListener(listener);

		IdxDirectoryTraverser.visitEntries(directory, collector);

		final List<IdxFileLink> matchingFiles = collector.getAndClearResult();

		sendMsg(String.format("Processed files %d of %d (100%%). Found %d files which match at least one search criteria", numberOfFiles, numberOfFiles,
				matchingFiles.size()));

		return new HashSet<>(matchingFiles);
	}

}
