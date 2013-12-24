package com.paviasystem.cloudfilesystem;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class StreamUtils {
	public static interface Writer {
		void write(DataOutputStream dos);
	}

	public static byte[] getBytes(Writer writer) {
		try {
			ByteArrayOutputStream baos = null;
			DataOutputStream dos = null;
			try {
				baos = new ByteArrayOutputStream();
				dos = new DataOutputStream(baos);
				writer.write(dos);
				dos.flush();
				baos.flush();
				return baos.toByteArray();
			} finally {
				if (dos != null)
					dos.close();
				if (baos != null)
					baos.close();
			}
		} catch (IOException exc) {
			return new byte[0];
		}
	}
}
