package com.paviasystem.cloudfilesystem.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.junit.Test;

import com.paviasystem.cloudfilesystem.Log;
import com.paviasystem.cloudfilesystem.LogEntry;
import com.paviasystem.cloudfilesystem.referenceimpl.MemoryLog;

public class LogTest {

	LogEntry[] toArray(Iterable<LogEntry> list) {
		ArrayList<LogEntry> list2 = new ArrayList<LogEntry>();
		list.forEach(x -> list2.add(x));
		return list2.toArray(new LogEntry[list2.size()]);
	}

	@Test
	public void test01_MemoryLog() throws Exception {
		MemoryLog x = new MemoryLog();
		test(x);
	}

	private void test(Log x) {
		assertEquals(0, toArray(x.read(1, 0)).length);

		x.addDirectorySetItemEntry(0, "Item 0", false, 1);
		x.addFileSetLengthEntry(1, 10);
		x.addDirectorySetItemEntry(0, "Item 1", true, 2);
		x.addFileSetLengthEntry(2, 20);
		x.addDirectorySetItemEntry(0, "Item 2", false, 3);
		x.addFileSetLengthEntry(3, 10);
		x.addFileSetLengthEntry(3, 20);
		x.addFileSetLengthEntry(3, 30);
		x.addDirectoryDeleteItemEntry(0, "Item 2");
		x.addFileWriteEntry(3, 57, true, ByteBuffer.wrap(new byte[] { 0, 1, 2, 3, 4 }));

		LogEntry[] y = toArray(x.read(0, 0));

		assertEquals(4, y.length);
		assertEquals("Item 0", y[0].itemName);
		assertEquals(1, y[0].itemNodeNumber);
		assertFalse(y[0].itemIsDirectory);
		assertEquals("Item 1", y[1].itemName);
		assertEquals(2, y[1].itemNodeNumber);
		assertTrue(y[1].itemIsDirectory);

		y = toArray(x.read(0, 1));

		assertEquals(3, y.length);
		assertEquals("Item 1", y[0].itemName);
		assertEquals(2, y[0].itemNodeNumber);
		assertEquals("Item 2", y[1].itemName);
		assertEquals(3, y[1].itemNodeNumber);

		y = toArray(x.read(3, 6));

		assertEquals(3, y.length);
		assertEquals(LogEntry.FILE_SETLENGTH, y[0].type);
		assertEquals(20, y[0].length);
		assertEquals(30, y[1].length);
		assertEquals(3, y[2].nodeNumber);
		assertEquals(57, y[2].position);
		assertEquals(true, y[2].positionFromEnd);
		assertEquals(LogEntry.FILE_WRITE, y[2].type);
		assertArrayEquals(new byte[] { 0, 1, 2, 3, 4 }, y[2].bytes.array());
	}
}
