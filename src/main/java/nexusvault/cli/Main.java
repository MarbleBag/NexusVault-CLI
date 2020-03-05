package nexusvault.cli;

import java.io.IOException;

public final class Main {

	public static void main(String[] args) throws IOException {
		final App app = new App();
		app.initializeApp();

		try {
			app.startApp(args);
			// app.runCommands(args);
			// if (app.isHeadlessMode())
			// return; //TODO

			// app.processConsole();
		} finally {
			app.closeApp();
		}
	}

}
