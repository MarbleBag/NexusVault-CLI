package nexusvault.cli.plugin.show;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import nexusvault.archive.IdxFileLink;
import nexusvault.cli.App;
import nexusvault.cli.Command;
import nexusvault.cli.ConsoleSystem.Level;
import nexusvault.cli.plugin.AbstPlugIn;
import nexusvault.cli.plugin.search.SearchPlugIn;

public final class ShowPlugIn extends AbstPlugIn {

	private static interface ShowAble {
		String getTrigger();

		void show();
	}

	private static final class ShowSearchResults implements ShowAble {
		@Override
		public String getTrigger() {
			return "search";
		}

		@Override
		public void show() {
			App.getInstance().getConsole().println(Level.CONSOLE, () -> {
				final Map<Path, Set<IdxFileLink>> searchResults = App.getInstance().getPlugIn(SearchPlugIn.class).getLastSearchResults();
				final StringBuilder b = new StringBuilder();
				for (final Entry<Path, Set<IdxFileLink>> entry : searchResults.entrySet()) {
					b.append("Archive: ").append(entry.getKey()).append("\n");
					if (entry.getValue().isEmpty()) {
						b.append("No search entries\n");
					} else {
						for (final IdxFileLink fileLink : entry.getValue()) {
							b.append(fileLink.fullName()).append("\n");
						}
					}
				}
				return b.toString();
			});
		}

	}

	private List<ShowAble> showables;

	public ShowPlugIn() {
		final List<Command> cmds = new ArrayList<>();

		if (!App.getInstance().getAppConfig().getHeadlessMode()) {
			cmds.add(new ShowCmd());
		}

		setCommands(cmds);
	}

	@Override
	public void initialize() {
		super.initialize();
		showables = new ArrayList<>();

		showables.add(new ShowSearchResults());
	}

	@Override
	public void deinitialize() {
		super.deinitialize();
		showables = null;
	}

	public void show(String arg0) {
		for (final ShowAble showable : showables) {
			if (showable.getTrigger().equals(arg0)) {
				showable.show();
				return;
			}
		}
	}

	public void showHelp() {
		sendMsg(() -> {
			final StringBuilder builder = new StringBuilder("Usable arguments: ");
			final List<String> triggers = showables.stream().map(ShowAble::getTrigger).collect(Collectors.toList());
			builder.append(String.join(", ", triggers));
			return builder.toString();
		});
	}

}
