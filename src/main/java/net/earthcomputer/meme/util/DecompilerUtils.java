package net.earthcomputer.meme.util;

import java.io.File;

import com.google.common.base.Throwables;

import net.earthcomputer.meme.enigma.CommandMain;

public class DecompilerUtils {

	private DecompilerUtils() {
	}

	public static void createDeobfuscatedJar(File obfuscatedJar, File mappings, File outputJar) {
		try {
			CommandMain.main(new String[] { "deobfuscate", obfuscatedJar.getAbsolutePath(), outputJar.getAbsolutePath(),
					mappings.getAbsolutePath() });
		} catch (Exception e) {
			System.err.println("Exception creating deobfuscated jar");
			throw Throwables.propagate(e);
		}
	}

	public static void decompile(File obfuscatedJar, File mappings, File outputFolder) {
		try {
			CommandMain.main(new String[] { "decompile", obfuscatedJar.getAbsolutePath(),
					outputFolder.getAbsolutePath(), mappings.getAbsolutePath() });
		} catch (Exception e) {
			System.err.println("Exception decompiling jar");
			throw Throwables.propagate(e);
		}
	}

	public static void createObfuscatedJar(File deobfuscatedJar, File mappings, File outputJar) {
		try {
			CommandMain.main(new String[] { "reobfuscate", deobfuscatedJar.getAbsolutePath(),
					outputJar.getAbsolutePath(), mappings.getAbsolutePath() });
		} catch (Exception e) {
			System.err.println("Exception creating obfuscated jar");
			throw Throwables.propagate(e);
		}
	}

}
