package com.paviasystem.cloudfilesystem.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.nio.ByteBuffer;
import java.nio.file.Files;

import org.junit.Test;

import com.paviasystem.cloudfilesystem.Blob;
import com.paviasystem.cloudfilesystem.BlobStore;
import com.paviasystem.cloudfilesystem.Utils;
import com.paviasystem.cloudfilesystem.impl.NodeCache;
import com.paviasystem.cloudfilesystem.referenceimpl.MemoryBlobStore;
import com.paviasystem.cloudfilesystem.referenceimpl.MemorySeekableByteChannel;

public class BlobStoreTest {
	@Test
	public void test01_MemoryBlobStore() throws Exception {
		MemoryBlobStore bs = new MemoryBlobStore();
		test(bs);
	}

	@Test
	public void test02_NodeCache() throws Exception {
		NodeCache bs = new NodeCache(Files.createTempDirectory("NodeCacheTest"));
		test(bs);
	}

	private void test(BlobStore bs) throws Exception {
		check(bs, "aaa");
		check(bs, "bbb");
		check(bs, "bbb");

		byte[] aaa = "aaa".getBytes();
		byte[] bbb = "bbb".getBytes();
		byte[] ccc = "ccc".getBytes();

		MemorySeekableByteChannel aaaCh = new MemorySeekableByteChannel();
		aaaCh.write(ByteBuffer.wrap(aaa));

		MemorySeekableByteChannel bbbCh = new MemorySeekableByteChannel();
		bbbCh.write(ByteBuffer.wrap(bbb));

		MemorySeekableByteChannel cccCh = new MemorySeekableByteChannel();
		cccCh.write(ByteBuffer.wrap(ccc));

		bs.set("aaa", new Blob(1, aaaCh));
		bs.set("bbb", new Blob(2, bbbCh));
		bs.set("ccc", new Blob(3, cccCh));

		check(bs, "aaa", 1, aaa);
		check(bs, "bbb", 2, bbb);
		check(bs, "ccc", 3, ccc);

		bs.set("bbb", null);

		check(bs, "aaa", 1, aaa);
		check(bs, "bbb");
		check(bs, "ccc", 3, ccc);
	}

	public static void check(BlobStore bs, String key, int lsn, byte[] bytes) throws Exception {
		try (Blob b = bs.get(key)) {
			assertNotNull(b);
			assertEquals(lsn, b.latestLogSequenceNumber);
			assertEquals(bytes.length, b.bytes.size());

			b.bytes.position(0);
			ByteBuffer buf = Utils.readFrom(b.bytes, (int) b.bytes.size());
			assertArrayEquals(bytes, buf.array());
		}
	}

	private void check(BlobStore bs, String key) throws Exception {
		Blob b = bs.get(key);
		assertNull(b);
	}

}
