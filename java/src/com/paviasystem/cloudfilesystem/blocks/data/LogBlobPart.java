package com.paviasystem.cloudfilesystem.blocks.data;

import com.paviasystem.cloudfilesystem.blocks.ByteReader;
import com.paviasystem.cloudfilesystem.blocks.ByteWriter;

public class LogBlobPart {
	public static final char SET_LENGTH = 'L';
	public static final char WRITE = 'W';

	public char type;
	public long newLength;
	public long destOffset;
	public byte[] bytes;

	public void writeInto(ByteWriter writer) {
		if(type==SET_LENGTH) {
			byte[] buf = new byte[9];
			buf[0] = 'L';
		}
		else if(type==WRITE) {
			byte[] buf = new byte[13];
			buf[0] = 'W';
		}
	}

	public static LogBlobPart readFrom(ByteReader reader) {
	}
}
