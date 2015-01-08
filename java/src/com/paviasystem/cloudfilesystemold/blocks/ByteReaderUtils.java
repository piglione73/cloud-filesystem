package com.paviasystem.cloudfilesystemold.blocks;

import java.io.ByteArrayOutputStream;

public class ByteReaderUtils {
	public static boolean readExact(ByteReader reader, byte[] buffer, int offset, int exactNumBytesToRead) {
		int bytesToRead = exactNumBytesToRead;
		int destOff = offset;

		while (bytesToRead > 0) {
			int bytesRead = reader.read(buffer, destOff, bytesToRead);
			if (bytesRead < 0) {
				// End of stream, so we could not read everything as requested
				return false;
			} else {
				destOff += bytesRead;
				bytesToRead -= bytesRead;
			}
		}

		// OK, we read exactly "exactNumBytesToRead"
		return true;
	}

	public static void copy(ByteReader reader, AbsoluteByteWriter writer) {
		byte[] buf = new byte[65536];
		long destOffset = 0;

		for (int bytesRead = reader.read(buf, 0, buf.length); bytesRead >= 0; bytesRead = reader.read(buf, 0, buf.length)) {
			writer.write(buf, 0, bytesRead, destOffset);
			destOffset += bytesRead;
		}
	}

	public static byte[] readAll(ByteReader reader) throws Exception {
		byte[] buf = new byte[65536];

		try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
			for (int bytesRead = reader.read(buf, 0, buf.length); bytesRead > 0; bytesRead = reader.read(buf, 0, buf.length)) {
				byteStream.write(buf, 0, bytesRead);
			}

			return byteStream.toByteArray();
		}
	}
}
