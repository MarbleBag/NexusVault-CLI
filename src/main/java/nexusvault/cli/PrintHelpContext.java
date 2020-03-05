package nexusvault.cli;

import java.io.PrintWriter;

public final class PrintHelpContext {

	private final PrintWriter writer;
	private String footer;
	private String header;
	private final String cmdName;
	private int descPadding;
	private int leftPadding;
	private int width;

	public PrintHelpContext(String cmdName, PrintWriter writer) {
		if (writer == null) {
			throw new IllegalArgumentException("'writer' must not be null");
		}
		if (cmdName == null) {
			throw new IllegalArgumentException("'cmdName' must not be null");
		}
		this.cmdName = cmdName;
		this.writer = writer;
	}

	public PrintWriter getWriter() {
		return this.writer;
	}

	public int getWidth() {
		return this.width;
	}

	public int getLeftPadding() {
		return this.leftPadding;
	}

	public int getDescPadding() {
		return this.descPadding;
	}

	public String getCommandName() {
		return this.cmdName;
	}

	public String getHeader() {
		return this.header;
	}

	public String getFooter() {
		return this.footer;
	}

	public void setFooter(String footer) {
		this.footer = footer;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public void setDescPadding(int descPadding) {
		this.descPadding = descPadding;
	}

	public void setLeftPadding(int leftPadding) {
		this.leftPadding = leftPadding;
	}

	public void setWidth(int width) {
		this.width = width;
	}

}