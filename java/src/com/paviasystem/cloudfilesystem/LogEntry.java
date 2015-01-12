package com.paviasystem.cloudfilesystem;

import java.nio.ByteBuffer;

public class LogEntry {
	public static final byte FILE_SETLENGTH = 0;
	public static final byte FILE_WRITE = 1;

	public static final byte DIRECTORY_SETITEM = 10;
	public static final byte DIRECTORY_DELETEITEM = 11;

	// Key
	public long logSequenceNumber;

	// Type
	public byte type;

	// Data for file entries
	public long position;
	public boolean positionFromEnd;
	public long length;
	public ByteBuffer bytes;

	// Data for directory entries
	public String itemName;
	public long itemNodeNumber;
}
