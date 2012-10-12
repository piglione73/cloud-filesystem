package com.paviasystem.cloudfs;

public class FileSystemFile {
	/**
	 * Closes the file and releases all associated locks, if any. After closing
	 * the file, all subsequent I/O operations will cause an exception to be
	 * thrown.
	 */
	public void close() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Reads bytes from the file.
	 * 
	 * @param offset
	 *            Offset in the file where to start reading.
	 * @param length
	 *            Number of bytes to read.
	 * @return Bytes read.
	 */
	public byte[] read(long offset, long length) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Writes bytes into the file.
	 * 
	 * @param offset
	 *            Offset in the file where to start writing.
	 * @param bytes
	 *            Bytes to write.
	 */
	public void write(long offset, byte[] bytes) {
		throw new UnsupportedOperationException();
	}
}
