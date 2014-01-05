package com.paviasystem.cloudfilesystem;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

public class StreamUtils {
	public static interface Writer {
		void write(DataOutput out) throws IOException;
	}

	public static byte[] getBytes(Writer writer) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				DataOutputStream dos = new DataOutputStream(baos)) {
			writer.write(dos);
			dos.flush();
			baos.flush();
			return baos.toByteArray();
		} catch (IOException exc) {
			return new byte[0];
		}
	}
}
