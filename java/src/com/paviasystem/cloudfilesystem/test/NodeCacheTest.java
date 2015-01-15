package com.paviasystem.cloudfilesystem.test;

import static org.junit.Assert.assertNull;

import java.nio.ByteBuffer;
import java.nio.file.Files;

import org.junit.Test;

import com.paviasystem.cloudfilesystem.components.Blob;
import com.paviasystem.cloudfilesystem.components.NodeCache;
import com.paviasystem.cloudfilesystem.referenceimpl.MemorySeekableByteChannel;

public class NodeCacheTest {
	@Test
	public void test01() throws Exception {
		NodeCache x = new NodeCache(Files.createTempDirectory("NodeCacheTest"));
		assertNull(x.get("1"));
		assertNull(x.get("2"));
		assertNull(x.get("3"));

		byte[] aaa = "aaa".getBytes();
		byte[] bbb = "bbb".getBytes();
		byte[] ccc = "ccc".getBytes();

		MemorySeekableByteChannel aaaCh = new MemorySeekableByteChannel();
		aaaCh.write(ByteBuffer.wrap(aaa));

		MemorySeekableByteChannel bbbCh = new MemorySeekableByteChannel();
		bbbCh.write(ByteBuffer.wrap(bbb));

		MemorySeekableByteChannel cccCh = new MemorySeekableByteChannel();
		cccCh.write(ByteBuffer.wrap(ccc));

		x.set("1", new Blob(10, aaaCh));
		x.set("2", new Blob(20, bbbCh));
		x.set("3", new Blob(30, cccCh));

		BlobStoreTest.check(x, "1", 10, aaa);
		BlobStoreTest.check(x, "2", 20, bbb);
		BlobStoreTest.check(x, "3", 30, ccc);

		x.set("2", null);

		BlobStoreTest.check(x, "1", 10, aaa);
		assertNull(x.get("2"));
		BlobStoreTest.check(x, "3", 30, ccc);
	}
}
