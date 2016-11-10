package net.earthcomputer.meme.util.launcher;

public enum LauncherVisibilityRule {

	// @formatter:off
	HIDE_LAUNCHER("Hide launcher and re-open when game closes"),
	CLOSE_LAUNCHER("Close launcher when game starts"),
	DO_NOTHING("Keep the launcher open");
	// @formatter:on

	private String name;

	private LauncherVisibilityRule(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

}
