package nexusvault.cli.plugin.show;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import nexusvault.cli.plugin.AbstractPlugIn;

public final class ShowPlugIn extends AbstractPlugIn {

	static interface ShowAble {
		String getTrigger();

		void show();
	}

	private List<ShowAble> showables;

	public ShowPlugIn() {

	}

	@Override
	public void initialize() {
		setCommands(new ShowCmd());
		setArguments();

		this.showables = new ArrayList<>();
		this.showables.add(new ShowSearchResults());
		this.showables.add(new ShowFileProperties());

		super.initialize();
	}

	@Override
	public void deinitialize() {
		super.deinitialize();
		this.showables = null;
	}

	public void show(String arg0) {
		for (final ShowAble showable : this.showables) {
			if (showable.getTrigger().equals(arg0)) {
				showable.show();
				return;
			}
		}
	}

	public void showHelp() {
		sendMsg(() -> {
			final StringBuilder builder = new StringBuilder("Usable arguments: ");
			final List<String> triggers = this.showables.stream().map(ShowAble::getTrigger).collect(Collectors.toList());
			builder.append(String.join(", ", triggers));
			return builder.toString();
		});
	}

}
