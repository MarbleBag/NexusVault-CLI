package nexusvault.cli.extensions.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

public final class SearchRequest {

	private final List<Pattern> pattern = new ArrayList<>();
	private int maxSearchResults = Integer.MAX_VALUE;
	private Properties properties;

	public void setPattern(List<Pattern> regex) {
		this.pattern.clear();
		this.pattern.addAll(regex);
	}

	public void setMaxResults(int maxSearchResults) {
		this.maxSearchResults = maxSearchResults;
	}

	public List<Pattern> getPattern() {
		return Collections.unmodifiableList(this.pattern);
	}

	public int getMaxResults() {
		return this.maxSearchResults;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public Properties getProperties() {
		return this.properties;
	}

}
