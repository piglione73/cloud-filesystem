package com.paviasystem.cloudfilesystem.test;

import java.util.ArrayList;

import org.junit.Test;
import static org.junit.Assert.*;

import com.paviasystem.cloudfilesystem.*;
import com.paviasystem.cloudfilesystem.referenceimpl.*;

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

		x.addDirectorySetItemEntry(0, "Item 0", 1);
		x.addFileSetLengthEntry(1, 10);
		x.addDirectorySetItemEntry(0, "Item 1", 2);
		x.addFileSetLengthEntry(2, 20);
		x.addDirectorySetItemEntry(0, "Item 2", 3);
		x.addFileSetLengthEntry(3, 10);
		x.addFileSetLengthEntry(3, 20);
		x.addFileSetLengthEntry(3, 30);
		x.addDirectoryDeleteItemEntry(0, "Item 2");

		LogEntry[] y = toArray(x.read(0, 0));

		assertEquals(4, y.length);
		assertEquals("Item 0", y[0].itemName);
		assertEquals(1, y[0].itemNodeNumber);
		assertEquals("Item 1", y[1].itemName);
		assertEquals(2, y[1].itemNodeNumber);

		y = toArray(x.read(0, 1));

		assertEquals(3, y.length);
		assertEquals("Item 1", y[0].itemName);
		assertEquals(2, y[0].itemNodeNumber);
		assertEquals("Item 2", y[1].itemName);
		assertEquals(3, y[1].itemNodeNumber);

		y = toArray(x.read(3, 6));

		assertEquals(2, y.length);
		assertEquals(20, y[0].length);
		assertEquals(30, y[1].length);

		fail("Insert more tests!");
	}
}
