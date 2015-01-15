package com.paviasystem.cloudfilesystem.components;

import java.nio.ByteBuffer;

public class LogEntry {
	public static final byte FILE_SETLENGTH = 0;
	public static final byte FILE_WRITE = 1;

	public static final byte DIRECTORY_SETITEM = 10;
	public static final byte DIRECTORY_DELETEITEM = 11;

	// Key
	public long logSequenceNumber;

	// Reference node
	public long nodeNumber;

	// Type
	public byte type;

	// Data for file entries
	public long position;
	public boolean positionFromEnd;
	public long length;
	public ByteBuffer bytes;

	// Data for directory entries
	public String itemName;
	public boolean itemIsDirectory;
	public long itemNodeNumber;

	public LogEntry(long logSequenceNumber, long nodeNumber, byte type, long position, boolean positionFromEnd, long length, ByteBuffer bytes, String itemName, boolean itemIsDirectory, long itemNodeNumber) {
		this.logSequenceNumber = logSequenceNumber;
		this.nodeNumber = nodeNumber;
		this.type = type;
		this.position = position;
		this.positionFromEnd = positionFromEnd;
		this.length = length;
		this.bytes = bytes;
		this.itemName = itemName;
		this.itemIsDirectory = itemIsDirectory;
		this.itemNodeNumber = itemNodeNumber;
	}
}
