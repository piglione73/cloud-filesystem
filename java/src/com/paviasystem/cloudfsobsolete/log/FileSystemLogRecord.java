package com.paviasystem.cloudfs.log;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import com.paviasystem.cloudfs.Path;

public class FileSystemLogRecord {
	private static AtomicInteger timestamp2Counter = new AtomicInteger(0);

	public static enum Type {
		Write, SetLength, Remove, CreateDirectory, RemoveDirectory
	}

	public static FileSystemLogRecord write(String path, long offset, long length, byte[] bytes) {
		return new FileSystemLogRecord(new Date(), Type.Write, path, offset, length, bytes);
	}

	public static FileSystemLogRecord setLength(String path, long length) {
		return new FileSystemLogRecord(new Date(), Type.SetLength, path, 0, length, null);
	}

	public static FileSystemLogRecord remove(String path) {
		return new FileSystemLogRecord(new Date(), Type.Remove, path, 0, 0, null);
	}

	public static FileSystemLogRecord createDirectory(String path) {
		return new FileSystemLogRecord(new Date(), Type.CreateDirectory, path, 0, 0, null);
	}

	public static FileSystemLogRecord removeDirectory(String path) {
		return new FileSystemLogRecord(new Date(), Type.RemoveDirectory, path, 0, 0, null);
	}

	Date timestamp1;
	int timestamp2;
	Type type;
	String path;
	long offset;
	long length;
	byte[] bytes;

	private FileSystemLogRecord(Date timestamp, Type type, String path, long offset, long length, byte[] bytes) {
		this.timestamp1 = timestamp;
		this.timestamp2 = timestamp2Counter.getAndIncrement();
		this.type = type;
		this.path = Path.normalize(path);
		this.offset = offset;
		this.length = length;
		this.bytes = bytes;
	}

	public Date getTimestamp1() {
		return timestamp1;
	}

	public int getTimestamp2() {
		return timestamp2;
	}

	public Type getType() {
		return type;
	}

	public String getPath() {
		return Path.normalize(path);
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