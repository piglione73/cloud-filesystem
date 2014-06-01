package com.paviasystem.cloudfilesystem.test.localcache;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.junit.Before;
import org.junit.Test;

import com.paviasystem.cloudfilesystem.blocks.AbsoluteByteReader;
import com.paviasystem.cloudfilesystem.blocks.AbsoluteByteWriter;
import com.paviasystem.cloudfilesystem.blocks.ByteReader;
import com.paviasystem.cloudfilesystem.blocks.ByteReaderUtils;
import com.paviasystem.cloudfilesystem.blocks.ByteWriter;
import com.paviasystem.cloudfilesystem.blocks.LocalCache;
import com.paviasystem.cloudfilesystem.blocks.data.LocalCacheEntry;

public class GenericLocalCacheTest {
	protected LocalCache localCache;

	private static ArrayList<LocalCacheEntry> toSortedList(Iterable<LocalCacheEntry> list) {
		ArrayList<LocalCacheEntry> ret = new ArrayList<>();
		for (LocalCacheEntry x : list)
			ret.add(x);

		Collections.sort(ret, new Comparator<LocalCacheEntry>() {
			@Override
			public int compare(LocalCacheEntry arg0, LocalCacheEntry arg1) {
				int k1 = arg0.category.compareTo(arg1.category);
				int k2 = arg0.name.compareTo(arg1.name);
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
		if (localCache == null)
			return;

		// Delete all
		ArrayList<LocalCacheEntry> list = toSortedList(localCache.list());
		for (LocalCacheEntry x : list)
			localCache.delete(x.category, x.name);
	}

	@Test
	public void genericTest_ReadWriteListDelete() throws Exception {
		if (localCache == null)
			return;

		assertEquals(0, toSortedList(localCache.list()).size());
		assertNull(localCache.openAbsoluteReader("A", "B"));
		assertNull(localCache.openSequentialReader("A", "B"));

		try (AbsoluteByteWriter w = localCache.openAbsoluteWriter("A", "B")) {
			byte[] buffer = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes(StandardCharsets.US_ASCII);
			w.write(buffer, 3, 2, 0);
			w.write(buffer, 0, 5, 2);
			w.write(buffer, 9, 5, 4);
			w.setLength(7);

			// Final result: DEABJKL
		}

		ArrayList<LocalCacheEntry> list = toSortedList(localCache.list());
		assertEquals(1, list.size());
		assertEquals("A", list.get(0).category);
		assertEquals("B", list.get(0).name);

		try (ByteReader r = localCache.openSequentialReader("A", "B")) {
			byte[] buffer = ByteReaderUtils.readAll(r);
			assertArrayEquals("DEABJKL".getBytes(StandardCharsets.US_ASCII), buffer);
		}

		try (ByteWriter w = localCache.openSequentialWriter("A", "B")) {
			byte[] buffer = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes(StandardCharsets.US_ASCII);
			w.write(buffer, 10, 3);

			// Final result: KLMBJKL
		}

		try (ByteReader r = localCache.openSequentialReader("A", "B")) {
			byte[] buffer = ByteReaderUtils.readAll(r);
			assertArrayEquals("KLMBJKL".getBytes(StandardCharsets.US_ASCII), buffer);
		}

		try (AbsoluteByteReader r = localCache.openAbsoluteReader("A", "B")) {
			byte[] buffer = new byte[5];
			r.read(buffer, 0, 1, 0);
			r.read(buffer, 1, 1, 1);
			r.read(buffer, 2, 1, 2);
			r.read(buffer, 3, 2, 3);
			assertArrayEquals("KLMBJ".getBytes(StandardCharsets.US_ASCII), buffer);
		}

		localCache.delete("A", "B");

		list = toSortedList(localCache.list());
		assertEquals(0, list.size());
	}
}
