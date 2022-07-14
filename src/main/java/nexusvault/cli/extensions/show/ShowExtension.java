package nexusvault.cli.extensions.show;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import nexusvault.cli.core.App;
import nexusvault.cli.core.AutoInstantiate;
import nexusvault.cli.core.extension.AbstractExtension;
import nexusvault.cli.extensions.show.property.ShowFileProperties;
import nexusvault.cli.extensions.show.search.ShowSearchResults;

@AutoInstantiate
public final class ShowExtension extends AbstractExtension {

	public static interface Showable {
		String getTrigger();

		void show(App app);
	}

	private List<Showable> showables;

	public ShowExtension() {

	}

	@Override
	protected void initializeExtension(InitializationHelper helper) {
		this.showables = new ArrayList<>();
		this.showables.add(new ShowSearchResults());
		this.showables.add(new ShowFileProperties());
	}

	@Override
	protected void deinitializeExtension() {
		this.showables = null;
	}

	public void show(String arg0) {
		for (final Showable showable : this.showables) {
			if (showable.getTrigger().equals(arg0)) {
				showable.show(getApp());
				return;
			}
		}
	}

	public void showHelp() {
		sendMsg(() -> {
			final StringBuilder builder = new StringBuilder("Usable arguments: ");
			final List<String> triggers = this.showables.stream().map(Showable::getTrigger).collect(Collectors.toList());
			builder.append(String.join(", ", triggers));
			return builder.toString();
		});
	}

}
