package net.earthcomputer.meme.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

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

	public static void download(URL from, File to) throws IOException {
		OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(to));
		copyStreams(from.openConnection().getInputStream(), outputStream);
		outputStream.flush();
		outputStream.close();
	}

}
