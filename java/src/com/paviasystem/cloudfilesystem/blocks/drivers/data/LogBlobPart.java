package com.paviasystem.cloudfilesystem.blocks.drivers.data;

import java.nio.ByteBuffer;

import com.paviasystem.cloudfilesystem.blocks.AbsoluteByteWriter;
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

	/**
	 * Apply the changes specified in this LogBlobPart to an AbsoluteByteWriter.
	 * 
	 * @param writer
	 * @throws Exception
	 */
	public void applyTo(AbsoluteByteWriter writer) throws Exception {
		if (type == SET_LENGTH)
			writer.setLength(newLength);
		else if (type == WRITE)
			writer.write(bytes, 0, bytes.length, destOffset);
		else
			throw new Exception("Invalid type");
	}

	/**
	 * Serialize into a ByteWriter.
	 * 
	 * @param writer
	 * @throws Exception
	 */
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

	/**
	 * Deserialize from a ByteReader
	 * 
	 * @param reader
	 * @return
	 * @throws Exception
	 */
	public static LogBlobPart readFrom(ByteReader reader) throws Exception {
		byte[] buf = new byte[13];
		ByteBuffer bytes = ByteBuffer.wrap(buf);

		// Read type
		if (!ByteReaderUtils.readExact(reader, buf, 0, 1))
			return null;

		LogBlobPart part = new LogBlobPart();
		part.type = bytes.get();

		// Read the rest, based on type
		if (part.type == SET_LENGTH){
			if (!ByteReaderUtils.readExact(reader, buf, 1, 8))
				return null;
			
			part.newLength = bytes.getLong();
		}
		else if (part.type == WRITE) {
			if (!ByteReaderUtils.readExact(reader, buf, 1, 12))
				return null;
			
			part.destOffset = bytes.getLong();
			part.bytes = new byte[bytes.getInt()];
			if (!ByteReaderUtils.readExact(reader, part.bytes, 0, part.bytes.length))
				return null;
		} else
			throw new Exception("Invalid type");

		return part;
	}

	public static LogBlobPart createSetLengthPart(long newLength) {
		LogBlobPart part = new LogBlobPart();
		part.type = SET_LENGTH;
		part.newLength = newLength;

		return part;
	}

	public static LogBlobPart createWritePart(long destOffset, byte[] bytes) {
		LogBlobPart part = new LogBlobPart();
		part.type = WRITE;
		part.destOffset = destOffset;
		part.bytes = bytes;

		return part;
	}

}
