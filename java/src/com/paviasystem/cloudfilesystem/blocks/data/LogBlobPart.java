package com.paviasystem.cloudfilesystem.blocks.data;

import java.nio.ByteBuffer;

import com.paviasystem.cloudfilesystem.blocks.ByteReader;
import com.paviasystem.cloudfilesystem.blocks.ByteReaderUtils;
import com.paviasystem.cloudfilesystem.blocks.ByteWriter;

public class LogBlobPart {
	public static final byte SET_LENGTH = 'L';
	public static final byte WRITE = 'W';

	public byte type;
	public long newLength;
	public long destOffset;
	public byte[] bytes;

	public void writeInto(ByteWriter writer) throws Exception {
		byte[] buf;

		if (type == SET_LENGTH)
			buf = ByteBuffer.allocate(9).put(type).putLong(newLength).array();
		else if (type == WRITE)
			buf = ByteBuffer.allocate(13).put(type).putLong(destOffset).putInt(bytes.length).array();
		else
			throw new Exception("Invalid type");

		writer.write(buf, 0, buf.length);
	}

	public static LogBlobPart readFrom(ByteReader reader) throws Exception {
		byte[] buf = new byte[13];
		ByteBuffer bytes = ByteBuffer.wrap(buf);

		//Read type	
		if (!ByteReaderUtils.readExact(reader, buf, 0, 1))
			return null;

		LogBlobPart part = new LogBlobPart();
		part.type = bytes.get();

		//Read the rest, based on type
		if (part.type == SET_LENGTH)
			part.newLength = bytes.getLong();
		else if (part.type == WRITE) {
			part.destOffset = bytes.getLong();
			part.bytes = new byte[bytes.getInt()];
			if (!ByteReaderUtils.readExact(reader, part.bytes, 0, part.bytes.length))
				return null;
		} else
			throw new Exception("Invalid type");

		return part;
	}
}