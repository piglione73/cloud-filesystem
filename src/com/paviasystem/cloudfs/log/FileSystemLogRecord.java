package com.paviasystem.cloudfs.log;

import java.util.Date;

public class FileSystemLogRecord {
	public static enum Type {
		WriteBytes, SetLength, Remove
	}

	public static FileSystemLogRecord createWriteBytes(String path, long offset, long length, byte[] bytes) {
		return new FileSystemLogRecord(new Date(), Type.WriteBytes, path, offset, length, bytes);
	}

	public static FileSystemLogRecord createSetLength(String path, long length) {
		return new FileSystemLogRecord(new Date(), Type.SetLength, path, 0, length, null);
	}

	public static FileSystemLogRecord createRemove(String path) {
		return new FileSystemLogRecord(new Date(), Type.Remove, path, 0, 0, null);
	}

	Date timestamp;
	Type type;
	String path;
	long offset;
	long length;
	byte[] bytes;

	private FileSystemLogRecord(Date timestamp, Type type, String path, long offset, long length, byte[] bytes) {
		this.timestamp = timestamp;
		this.type = type;
		this.path = path;
		this.offset = offset;
		this.length = length;
		this.bytes = bytes;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public Type getType() {
		return type;
	}

	public String getPath() {
		return path;
	}

	public long getOffset() {
		return offset;
	}

	public long getLength() {
		return length;
	}

	public byte[] getBytes() {
		return bytes;
	}

}
