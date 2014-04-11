package com.paviasystem.cloudfilesystem.test.blobstore;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.paviasystem.cloudfilesystem.blocks.BlobStore;
import com.paviasystem.cloudfilesystem.blocks.ByteReader;
import com.paviasystem.cloudfilesystem.blocks.ByteReaderUtils;
import com.paviasystem.cloudfilesystem.blocks.ByteWriter;

public class GenericBlobStoreTest {
	protected BlobStore blobStore;

	private static ArrayList<String> toSortedList(Iterable<String> list) {
		ArrayList<String> ret = new ArrayList<>();
		for (String x : list)
			ret.add(x);

		Collections.sort(ret);
		return ret;
	}

	@Before
	public void genericTest_Before() {
		if (blobStore == null)
			return;

		//Delete all
		ArrayList<String> list = toSortedList(blobStore.list(null));
		for (String x : list)
			blobStore.delete(x);
	}

	@Test
	public void genericTest_ReadWriteDeleteList() throws Exception {
		if (blobStore == null)
			return;

		assertEquals(0, toSortedList(blobStore.list(null)).size());

		try (ByteReader x = blobStore.read("A", null)) {
			assertNull(x);
		}

		try (ByteWriter x = blobStore.write("A", null)) {
		}

		try (ByteWriter x = blobStore.write("B", null)) {
		}

		try (ByteReader x = blobStore.read("A", null)) {
			assertNotNull(x);
			byte[] bytes = ByteReaderUtils.readAll(x);
			assertEquals(0, bytes.length);
		}

		ArrayList<String> list = toSortedList(blobStore.list(null));
		assertEquals(2, list.size());
		assertEquals("A", list.get(0));
		assertEquals("B", list.get(1));

		byte[] bufHelloWorld = "Hello world!".getBytes();
		HashMap<String, String> meta = new HashMap<String, String>();
		meta.put("A", "aaa");
		meta.put("B", "bbb");

		try (ByteWriter x = blobStore.write("A", meta)) {
			x.write(bufHelloWorld, 0, bufHelloWorld.length);
		}

		try (ByteReader x = blobStore.read("A", null)) {
			assertNotNull(x);
			byte[] bytes = ByteReaderUtils.readAll(x);
			assertEquals(bufHelloWorld.length, bytes.length);
			assertArrayEquals(bufHelloWorld, bytes);
		}

		HashMap<String, String> outMeta = new HashMap<String, String>();
		try (ByteReader x = blobStore.read("A", outMeta)) {
			assertNotNull(x);
			byte[] bytes = ByteReaderUtils.readAll(x);
			assertEquals(bufHelloWorld.length, bytes.length);
			assertArrayEquals(bufHelloWorld, bytes);
		}

		assertEquals(2, outMeta.size());
		assertEquals("aaa", outMeta.get("A"));
		assertEquals("bbb", outMeta.get("B"));

		list = toSortedList(blobStore.list(null));
		assertEquals(2, list.size());
		assertEquals("A", list.get(0));
		assertEquals("B", list.get(1));

		blobStore.delete("A");

		list = toSortedList(blobStore.list(null));
		assertEquals(1, list.size());
		assertEquals("B", list.get(0));

		blobStore.delete("B");

		list = toSortedList(blobStore.list(null));
		assertEquals(0, list.size());
	}

}
