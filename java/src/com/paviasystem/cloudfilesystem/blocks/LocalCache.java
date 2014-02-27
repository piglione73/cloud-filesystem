package com.paviasystem.cloudfilesystem.blocks;

import java.util.Date;

public interface LocalCache {
	ByteWriter openSequentialWriter(String category, String name);

	ByteReader openSequentialReader(String category, String name);

	AbsoluteByteReader openAbsoluteReader(String category, String name);

	AbsoluteByteWriter openAbsoluteWriter(String category, String name);

	void delete(String category, String name);

	Date getTimestamp(String category, String name);
}
