package com.paviasystem.cloudfilesystem.blocks.data;

import java.io.DataOutput;
import java.io.IOException;

public class LogEntry {
	public final static char SET_LENGTH = 'L';
	public final static char WRITE_BYTES = 'W';

	public String name;
	public char type;
	public long start;
	public long length;
	public byte[] bytes;

	public void writeInto(DataOutput out) throws IOException {
		if (type == SET_LENGTH) {
			out.writeChar('L');
			out.writeLong(length);
		} else if (type == WRITE_BYTES) {
			out.writeChar('W');
			out.writeLong(start);
			out.writeInt(bytes.length);
			out.write(bytes);
		}
	}

	public static LogEntry createSetLength(String name, long length) {
		LogEntry ce = new LogEntry();
		ce.name = name;
		ce.type = SET_LENGTH;
		ce.length = 0;
		return ce;
	}

	public static LogEntry createWriteBytes(String name, byte[] buffer,
			int bufferOffset, int bytesToWrite, long destFileOffset) {
		LogEntry ce = new LogEntry();
		ce.name = name;
		ce.type = WRITE_BYTES;
		ce.start = destFileOffset;
		ce.length = bytesToWrite;
		ce.bytes = new byte[bytesToWrite];
		System.arraycopy(buffer, bufferOffset, ce.bytes, 0, bytesToWrite);
		return ce;
	}
}