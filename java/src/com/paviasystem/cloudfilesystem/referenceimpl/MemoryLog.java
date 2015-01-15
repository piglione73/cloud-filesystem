package com.paviasystem.cloudfilesystem.referenceimpl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Stream;

import com.paviasystem.cloudfilesystem.components.Log;
import com.paviasystem.cloudfilesystem.components.LogEntry;

public class MemoryLog implements Log {

	ArrayList<LogEntry> entries = new ArrayList<LogEntry>();

	@Override
	public synchronized Iterable<LogEntry> read(long nodeNumber, long logSequenceNumberFrom) {
		return new Iterable<LogEntry>() {
			@Override
			public Iterator<LogEntry> iterator() {
				Stream<LogEntry> filteredEntries = entries.stream().filter(e -> e.nodeNumber == nodeNumber && e.logSequenceNumber >= logSequenceNumberFrom);
				Stream<LogEntry> sortedEntries = filteredEntries.sorted((e1, e2) -> Long.compare(e1.logSequenceNumber, e2.logSequenceNumber));

				return sortedEntries.iterator();
			}
		};
	}

	@Override
	public synchronized long add(long nodeNumber, byte type, long position, boolean positionFromEnd, long length, ByteBuffer bytes, String itemName, boolean itemisDirectory, long itemNodeNumber) {
		long logSequenceNumber = entries.size();
		LogEntry logEntry = new LogEntry(logSequenceNumber, nodeNumber, type, position, positionFromEnd, length, bytes, itemName, itemisDirectory, itemNodeNumber);
		entries.add(logEntry);
		return logSequenceNumber;
	}

}
