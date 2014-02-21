package com.paviasystem.cloudfilesystem.blocks.data;

import java.util.ArrayList;

import com.paviasystem.cloudfilesystem.blocks.ByteReader;
import com.paviasystem.cloudfilesystem.blocks.ByteWriter;

public class LogBlob {
	public ArrayList<LogBlobPart> parts;

	public void writeInto(ByteWriter writer) throws Exception {
		for (LogBlobPart part : parts)
			part.writeInto(writer);
	}

	public static LogBlob readFrom(ByteReader reader) throws Exception {
		LogBlob lb = new LogBlob();
		lb.parts = new ArrayList<LogBlobPart>();

		for (LogBlobPart part = LogBlobPart.readFrom(reader); part != null; part = LogBlobPart.readFrom(reader))
			lb.parts.add(part);

		return lb;
	}

}
