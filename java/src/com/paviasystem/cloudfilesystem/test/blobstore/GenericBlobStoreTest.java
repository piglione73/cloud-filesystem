package com.paviasystem.cloudfilesystem.test.blobstore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.paviasystem.cloudfilesystem.blocks.BlobStore;

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
	public void genericTest() {
		if (blobStore == null)
			return;

		assertEquals(0, toSortedList(blobStore.list(null)).size());

		assertNull(blobStore.read("A", null));
		blobStore.write("A", null);
		assertNotNull(blobStore.read("A", null));

		ArrayList<String> list = toSortedList(blobStore.list(null));
		assertEquals(1, list.size());
		assertEquals("A", list.get(0));

	}

}
