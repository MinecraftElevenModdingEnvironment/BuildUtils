package net.earthcomputer.meme.util.launcher;

import net.earthcomputer.meme.util.OSUtils.EnumOS;

public class CompatibilityRule {

	public Action action;
	public OSRestriction os;

	public static enum Action {
		ALLOW, DISALLOW
	}

	public static class OSRestriction {
		public EnumOS name;
		public String version;
		public String arch;
	}

}
