package net.earthcomputer.meme.util.launcher;

import java.io.File;
import java.util.Set;

public class Profile implements Comparable<Profile> {

	public String name;
	public File gameDir;
	public String lastVersionId;
	public String javaDir;
	public String javaArgs;
	public Resolution resolution;
	public Set<MinecraftReleaseType> allowedReleaseTypes;
	public String playerUUID;
	public Boolean useHopperCrashService;
	public LauncherVisibilityRule launcherVisibilityOnGameClose;

	@Override
	public int compareTo(Profile o) {
		if (o == null) {
			return -1;
		}
		return name.compareTo(o.name);
	}
	
	public static class Resolution {
		public int width;
		public int height;
	}

}
