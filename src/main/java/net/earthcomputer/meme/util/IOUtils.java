package net.earthcomputer.meme.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {

	private IOUtils() {
	}

	public static void copyStreams(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[8192];
		int amtRead;
		while ((amtRead = in.read(buffer)) != -1) {
			out.write(buffer, 0, amtRead);
		}
	}

}
