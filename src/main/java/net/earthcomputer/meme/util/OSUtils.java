package net.earthcomputer.meme.util;

public class OSUtils {

	private OSUtils() {
	}
	
	private static final EnumOS OS;
	
	static {
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.contains("win")) {
			OS = EnumOS.WINDOWS;
		} else if (osName.contains("mac")) {
			OS = EnumOS.OSX;
		} else if (osName.contains("solaris") || osName.contains("sunos")) {
			OS = EnumOS.SOLARIS;
		} else if (osName.contains("linux") || osName.contains("unix")) {
			OS = EnumOS.LINUX;
		} else {
			OS = EnumOS.UNKNOWN;
		}
	}
	
	public static EnumOS getOS() {
		return OS;
	}
	
	public static boolean isWindows() {
		return OS == EnumOS.WINDOWS;
	}
	
	public static boolean isLinux() {
		return OS == EnumOS.LINUX;
	}
	
	public static boolean isMac() {
		return OS == EnumOS.OSX;
	}
	
	public static boolean isSolaris() {
		return OS == EnumOS.SOLARIS;
	}
	
	public static enum EnumOS {
		WINDOWS, LINUX, OSX, SOLARIS, UNKNOWN
	}
	
}
