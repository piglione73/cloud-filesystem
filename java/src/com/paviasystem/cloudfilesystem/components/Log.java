package com.paviasystem.cloudfilesystem.components;

import java.nio.ByteBuffer;

public interface Log {
	Iterable<LogEntry> read(long nodeNumber, long logSequenceNumberFrom);

	long add(long nodeNumber, byte type, long position, boolean positionFromEnd, long length, ByteBuffer bytes, String itemName, boolean itemIsDirectory, long itemNodeNumber);

	default long addFileSetLengthEntry(long nodeNumber, long length) {
		return add(nodeNumber, LogEntry.FILE_SETLENGTH, 0, false, length, null, null, false, 0);
	}

	default long addFileWriteEntry(long nodeNumber, long position, boolean positionFromEnd, ByteBuffer bytes) {
		return add(nodeNumber, LogEntry.FILE_WRITE, position, positionFromEnd, 0, bytes, null, false, 0);
	}

	default long addDirectorySetItemEntry(long nodeNumber, String itemName, boolean itemIsDirectory, long itemNodeNumber) {
		return add(nodeNumber, LogEntry.DIRECTORY_SETITEM, 0, false, 0, null, itemName, itemIsDirectory, itemNodeNumber);
	}

	default long addDirectoryDeleteItemEntry(long nodeNumber, String itemName) {
		return add(nodeNumber, LogEntry.DIRECTORY_DELETEITEM, 0, false, 0, null, itemName, false, 0);
	}
}
