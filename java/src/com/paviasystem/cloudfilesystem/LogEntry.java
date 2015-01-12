package com.paviasystem.cloudfilesystem;

import java.nio.ByteBuffer;

public class LogEntry {
	public static final byte FILE_SETLENGTH = 0;
	public static final byte FILE_WRITE = 1;

	public byte type;
	public long position;
	public boolean positionFromEnd;
	public long length;
	public ByteBuffer bytes;
}
