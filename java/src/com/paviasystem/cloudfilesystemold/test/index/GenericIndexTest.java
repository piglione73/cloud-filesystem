package com.paviasystem.cloudfilesystemold.test.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.junit.Before;
import org.junit.Test;

import com.paviasystem.cloudfilesystemold.blocks.Index;
import com.paviasystem.cloudfilesystemold.blocks.data.IndexEntry;

public class GenericIndexTest {
	protected Index index;

	private static ArrayList<IndexEntry> toSortedList(Iterable<IndexEntry> list) {
		ArrayList<IndexEntry> ret = new ArrayList<>();
		for (IndexEntry x : list)
			ret.add(x);

		Collections.sort(ret, new Comparator<IndexEntry>() {
			@Override
			public int compare(IndexEntry arg0, IndexEntry arg1) {
				int k1 = arg0.key1.compareTo(arg1.key1);
				int k2 = arg0.key2.compareTo(arg1.key2);
				if (k1 != 0)
					return k1;
				else
					return k2;
			}
		});

		return ret;
	}

	@Before
	public void genericTest_Before() {
		if (index == null)
			return;

		//Delete all
		ArrayList<IndexEntry> list = toSortedList(index.list(null, null, null));
		for (IndexEntry x : list)
			index.delete(x.key1, x.key2);
	}

	@Test
	public void genericTest_ReadWriteListDelete() {
		if (index == null)
			return;

		assertEquals(0, toSortedList(index.list(null, null, null)).size());
		assertNull(index.read("A", "B"));

		IndexEntry ie = new IndexEntry();
		ie.key1 = "A";
		ie.key2 = "B";
		ie.data1 = "C";
		ie.data2 = "D";
		ie.data3 = "E";
		ie.data4 = "F";
		ie.data5 = "G";

		index.write(ie);

		ie = index.read("A", "B");
		assertNotNull(ie);
		assertEquals("A", ie.key1);
		assertEquals("B", ie.key2);
		assertEquals("C", ie.data1);
		assertEquals("D", ie.data2);
		assertEquals("E", ie.data3);
		assertEquals("F", ie.data4);
		assertEquals("G", ie.data5);

		ArrayList<IndexEntry> list = toSortedList(index.list(null, null, null));
		assertEquals(1, list.size());

		ie = list.get(0);
		assertEquals("A", ie.key1);
		assertEquals("B", ie.key2);
		assertEquals("C", ie.data1);
		assertEquals("D", ie.data2);
		assertEquals("E", ie.data3);
		assertEquals("F", ie.data4);
		assertEquals("G", ie.data5);

		for (char k1 = 'A'; k1 <= 'Z'; k1++) {
			for (char k2 = 'Z'; k2 >= 'A'; k2--) {
				for (char d = 'A'; d <= 'Z'; d++) {
					ie = new IndexEntry();
					ie.key1 = "" + k1;
					ie.key2 = "" + k2;
					ie.data1 = "" + d;
					ie.data2 = "" + d;
					ie.data3 = "" + d;
					ie.data4 = "" + d;
					ie.data5 = "" + d;

					index.write(ie);
				}
			}
		}

		list = toSortedList(index.list(null, null, null));
		assertEquals(26 * 26, list.size());

		ie = list.get(0);
		assertEquals("A", ie.key1);
		assertEquals("A", ie.key2);
		assertEquals("Z", ie.data1);
		assertEquals("Z", ie.data2);
		assertEquals("Z", ie.data3);
		assertEquals("Z", ie.data4);
		assertEquals("Z", ie.data5);

		ie = list.get(1);
		assertEquals("A", ie.key1);
		assertEquals("B", ie.key2);
		assertEquals("Z", ie.data1);
		assertEquals("Z", ie.data2);
		assertEquals("Z", ie.data3);
		assertEquals("Z", ie.data4);
		assertEquals("Z", ie.data5);

		ie = list.get(2);
		assertEquals("A", ie.key1);
		assertEquals("C", ie.key2);
		assertEquals("Z", ie.data1);
		assertEquals("Z", ie.data2);
		assertEquals("Z", ie.data3);
		assertEquals("Z", ie.data4);
		assertEquals("Z", ie.data5);

		list = toSortedList(index.list("X", null, null));
		assertEquals(26, list.size());

		ie = list.get(0);
		assertEquals("X", ie.key1);
		assertEquals("A", ie.key2);
		assertEquals("Z", ie.data1);
		assertEquals("Z", ie.data2);
		assertEquals("Z", ie.data3);
		assertEquals("Z", ie.data4);
		assertEquals("Z", ie.data5);

		ie = list.get(1);
		assertEquals("X", ie.key1);
		assertEquals("B", ie.key2);
		assertEquals("Z", ie.data1);
		assertEquals("Z", ie.data2);
		assertEquals("Z", ie.data3);
		assertEquals("Z", ie.data4);
		assertEquals("Z", ie.data5);

		ie = list.get(2);
		assertEquals("X", ie.key1);
		assertEquals("C", ie.key2);
		assertEquals("Z", ie.data1);
		assertEquals("Z", ie.data2);
		assertEquals("Z", ie.data3);
		assertEquals("Z", ie.data4);
		assertEquals("Z", ie.data5);

		list = toSortedList(index.list("X", "C", null));
		assertEquals(24, list.size());

		ie = list.get(0);
		assertEquals("X", ie.key1);
		assertEquals("C", ie.key2);
		assertEquals("Z", ie.data1);
		assertEquals("Z", ie.data2);
		assertEquals("Z", ie.data3);
		assertEquals("Z", ie.data4);
		assertEquals("Z", ie.data5);

		ie = list.get(1);
		assertEquals("X", ie.key1);
		assertEquals("D", ie.key2);
		assertEquals("Z", ie.data1);
		assertEquals("Z", ie.data2);
		assertEquals("Z", ie.data3);
		assertEquals("Z", ie.data4);
		assertEquals("Z", ie.data5);

		ie = list.get(2);
		assertEquals("X", ie.key1);
		assertEquals("E", ie.key2);
		assertEquals("Z", ie.data1);
		assertEquals("Z", ie.data2);
		assertEquals("Z", ie.data3);
		assertEquals("Z", ie.data4);
		assertEquals("Z", ie.data5);

		list = toSortedList(index.list("X", null, "X"));
		assertEquals(24, list.size());

		ie = list.get(0);
		assertEquals("X", ie.key1);
		assertEquals("A", ie.key2);
		assertEquals("Z", ie.data1);
		assertEquals("Z", ie.data2);
		assertEquals("Z", ie.data3);
		assertEquals("Z", ie.data4);
		assertEquals("Z", ie.data5);

		ie = list.get(1);
		assertEquals("X", ie.key1);
		assertEquals("B", ie.key2);
		assertEquals("Z", ie.data1);
		assertEquals("Z", ie.data2);
		assertEquals("Z", ie.data3);
		assertEquals("Z", ie.data4);
		assertEquals("Z", ie.data5);

		ie = list.get(2);
		assertEquals("X", ie.key1);
		assertEquals("C", ie.key2);
		assertEquals("Z", ie.data1);
		assertEquals("Z", ie.data2);
		assertEquals("Z", ie.data3);
		assertEquals("Z", ie.data4);
		assertEquals("Z", ie.data5);

		list = toSortedList(index.list("X", "C", "E"));
		assertEquals(3, list.size());

		ie = list.get(0);
		assertEquals("X", ie.key1);
		assertEquals("C", ie.key2);
		assertEquals("Z", ie.data1);
		assertEquals("Z", ie.data2);
		assertEquals("Z", ie.data3);
		assertEquals("Z", ie.data4);
		assertEquals("Z", ie.data5);

		ie = list.get(1);
		assertEquals("X", ie.key1);
		assertEquals("D", ie.key2);
		assertEquals("Z", ie.data1);
		assertEquals("Z", ie.data2);
		assertEquals("Z", ie.data3);
		assertEquals("Z", ie.data4);
		assertEquals("Z", ie.data5);

		ie = list.get(2);
		assertEquals("X", ie.key1);
		assertEquals("E", ie.key2);
		assertEquals("Z", ie.data1);
		assertEquals("Z", ie.data2);
		assertEquals("Z", ie.data3);
		assertEquals("Z", ie.data4);
		assertEquals("Z", ie.data5);

		list = toSortedList(index.list("Q", null, null));
		assertEquals(26, list.size());
		for (IndexEntry x : list)
			index.delete(x.key1, x.key2);

		list = toSortedList(index.list(null, null, null));
		assertEquals(25 * 26, list.size());

		assertNull(index.read("Q", "Q"));
		assertNotNull(index.read("X", "Q"));
	}

	@Test
	public void genericTest_Update() {
		if (index == null)
			return;

		assertEquals(0, toSortedList(index.list(null, null, null)).size());

		for (char k1 = 'A'; k1 <= 'C'; k1++) {
			for (char k2 = 'C'; k2 >= 'A'; k2--) {
				for (char d = 'A'; d <= 'Z'; d++) {
					IndexEntry ie = new IndexEntry();
					ie.key1 = "" + k1;
					ie.key2 = "" + k2;
					ie.data1 = "" + d;
					ie.data2 = "" + d;
					ie.data3 = "" + d;
					ie.data4 = "" + d;
					ie.data5 = "" + d;

					index.write(ie);
				}
			}
		}

		ArrayList<IndexEntry> list = toSortedList(index.list(null, null, null));
		assertEquals(3 * 3, list.size());

		for (IndexEntry x : list)
			assertTrue(index.update(x.key1, x.key2, x.key1 + "X", x.key2 + "Y"));

		for (IndexEntry x : list)
			assertFalse(index.update(x.key1, x.key2, x.key1 + "X", x.key2 + "Y"));

		list = toSortedList(index.list(null, null, null));
		assertEquals(3 * 3, list.size());

		IndexEntry ie = list.get(0);
		assertEquals("AX", ie.key1);
		assertEquals("AY", ie.key2);
		assertEquals("Z", ie.data1);
		assertEquals("Z", ie.data2);
		assertEquals("Z", ie.data3);
		assertEquals("Z", ie.data4);
		assertEquals("Z", ie.data5);

		ie = list.get(1);
		assertEquals("AX", ie.key1);
		assertEquals("BY", ie.key2);
		assertEquals("Z", ie.data1);
		assertEquals("Z", ie.data2);
		assertEquals("Z", ie.data3);
		assertEquals("Z", ie.data4);
		assertEquals("Z", ie.data5);

		ie = list.get(2);
		assertEquals("AX", ie.key1);
		assertEquals("CY", ie.key2);
		assertEquals("Z", ie.data1);
		assertEquals("Z", ie.data2);
		assertEquals("Z", ie.data3);
		assertEquals("Z", ie.data4);
		assertEquals("Z", ie.data5);

		ie = list.get(3);
		assertEquals("BX", ie.key1);
		assertEquals("AY", ie.key2);
		assertEquals("Z", ie.data1);
		assertEquals("Z", ie.data2);
		assertEquals("Z", ie.data3);
		assertEquals("Z", ie.data4);
		assertEquals("Z", ie.data5);

		ie = list.get(4);
		assertEquals("BX", ie.key1);
		assertEquals("BY", ie.key2);
		assertEquals("Z", ie.data1);
		assertEquals("Z", ie.data2);
		assertEquals("Z", ie.data3);
		assertEquals("Z", ie.data4);
		assertEquals("Z", ie.data5);

		ie = list.get(5);
		assertEquals("BX", ie.key1);
		assertEquals("CY", ie.key2);
		assertEquals("Z", ie.data1);
		assertEquals("Z", ie.data2);
		assertEquals("Z", ie.data3);
		assertEquals("Z", ie.data4);
		assertEquals("Z", ie.data5);

		ie = list.get(6);
		assertEquals("CX", ie.key1);
		assertEquals("AY", ie.key2);
		assertEquals("Z", ie.data1);
		assertEquals("Z", ie.data2);
		assertEquals("Z", ie.data3);
		assertEquals("Z", ie.data4);
		assertEquals("Z", ie.data5);

		ie = list.get(7);
		assertEquals("CX", ie.key1);
		assertEquals("BY", ie.key2);
		assertEquals("Z", ie.data1);
		assertEquals("Z", ie.data2);
		assertEquals("Z", ie.data3);
		assertEquals("Z", ie.data4);
		assertEquals("Z", ie.data5);

		ie = list.get(8);
		assertEquals("CX", ie.key1);
		assertEquals("CY", ie.key2);
		assertEquals("Z", ie.data1);
		assertEquals("Z", ie.data2);
		assertEquals("Z", ie.data3);
		assertEquals("Z", ie.data4);
		assertEquals("Z", ie.data5);

		assertFalse(index.update("CX", "BY", "A", "A", "A", "B", "C", "D"));
		assertFalse(index.update("CX", "BY", "Z", "A", "AA", "BB", "CC", "DD"));
		assertTrue(index.update("CX", "BY", "Z", "Z", "AAA", "BBB", "CCC", "DDD"));

		list = toSortedList(index.list(null, null, null));
		assertEquals(3 * 3, list.size());

		ie = list.get(0);
		assertEquals("AX", ie.key1);
		assertEquals("AY", ie.key2);
		assertEquals("Z", ie.data1);
		assertEquals("Z", ie.data2);
		assertEquals("Z", ie.data3);
		assertEquals("Z", ie.data4);
		assertEquals("Z", ie.data5);

		ie = list.get(1);
		assertEquals("AX", ie.key1);
		assertEquals("BY", ie.key2);
		assertEquals("Z", ie.data1);
		assertEquals("Z", ie.data2);
		assertEquals("Z", ie.data3);
		assertEquals("Z", ie.data4);
		assertEquals("Z", ie.data5);

		ie = list.get(2);
		assertEquals("AX", ie.key1);
		assertEquals("CY", ie.key2);
		assertEquals("Z", ie.data1);
		assertEquals("Z", ie.data2);
		assertEquals("Z", ie.data3);
		assertEquals("Z", ie.data4);
		assertEquals("Z", ie.data5);

		ie = list.get(3);
		assertEquals("BX", ie.key1);
		assertEquals("AY", ie.key2);
		assertEquals("Z", ie.data1);
		assertEquals("Z", ie.data2);
		assertEquals("Z", ie.data3);
		assertEquals("Z", ie.data4);
		assertEquals("Z", ie.data5);

		ie = list.get(4);
		assertEquals("BX", ie.key1);
		assertEquals("BY", ie.key2);
		assertEquals("Z", ie.data1);
		assertEquals("Z", ie.data2);
		assertEquals("Z", ie.data3);
		assertEquals("Z", ie.data4);
		assertEquals("Z", ie.data5);

		ie = list.get(5);
		assertEquals("BX", ie.key1);
		assertEquals("CY", ie.key2);
		assertEquals("Z", ie.data1);
		assertEquals("Z", ie.data2);
		assertEquals("Z", ie.data3);
		assertEquals("Z", ie.data4);
		assertEquals("Z", ie.data5);

		ie = list.get(6);
		assertEquals("CX", ie.key1);
		assertEquals("AY", ie.key2);
		assertEquals("Z", ie.data1);
		assertEquals("Z", ie.data2);
		assertEquals("Z", ie.data3);
		assertEquals("Z", ie.data4);
		assertEquals("Z", ie.data5);

		ie = list.get(7);
		assertEquals("CX", ie.key1);
		assertEquals("BY", ie.key2);
		assertEquals("AAA", ie.data1);
		assertEquals("BBB", ie.data2);
		assertEquals("CCC", ie.data3);
		assertEquals("DDD", ie.data4);
		assertEquals("Z", ie.data5);

		ie = list.get(8);
		assertEquals("CX", ie.key1);
		assertEquals("CY", ie.key2);
		assertEquals("Z", ie.data1);
		assertEquals("Z", ie.data2);
		assertEquals("Z", ie.data3);
		assertEquals("Z", ie.data4);
		assertEquals("Z", ie.data5);
	}
}
