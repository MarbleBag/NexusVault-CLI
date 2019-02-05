package nexusvault.cli.plugin.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public final class SearchRequest {

	private final List<Pattern> pattern = new ArrayList<>();
	private int maxSearchResults = Integer.MAX_VALUE;

	public void setPattern(List<Pattern> regex) {
		pattern.clear();
		pattern.addAll(regex);
	}

	public void setMaxResults(int maxSearchResults) {
		this.maxSearchResults = maxSearchResults;
	}

	public List<Pattern> getPattern() {
		return Collections.unmodifiableList(pattern);
	}

	public int getMaxResults() {
		return maxSearchResults;
	}

}
