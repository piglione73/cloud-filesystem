package com.paviasystem.cloudfilesystemold.test.drivers;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.paviasystem.cloudfilesystemold.Utils;
import com.paviasystem.cloudfilesystemold.blocks.ByteReader;
import com.paviasystem.cloudfilesystemold.blocks.ByteReaderUtils;
import com.paviasystem.cloudfilesystemold.blocks.ByteWriter;
import com.paviasystem.cloudfilesystemold.blocks.drivers.BlobStoreDriver;
import com.paviasystem.cloudfilesystemold.blocks.drivers.BlobStoreDriver.FileMetaData;
import com.paviasystem.cloudfilesystemold.referenceimpl.MemoryBlobStore;

public class BlobStoreDriverTest {

	MemoryBlobStore bs;
	BlobStoreDriver dut;

	@Before
	public void before() {
		bs = new MemoryBlobStore();
		dut = new BlobStoreDriver(bs);
	}

	@Test
	public void test_ListReadWriteDelete() throws Exception {
		byte[] buffer = "Hello".getBytes(StandardCharsets.UTF_8);

		try (ByteWriter w = dut.writeLogBlob("ABC")) {
			w.write(buffer, 0, buffer.length);
		}

		try (ByteWriter w = dut.writeLogBlob("ABCD")) {
			w.write(buffer, 0, buffer.length);
		}

		FileMetaData meta = new FileMetaData();
		meta.latestLogBlobLsn = 57;
		meta.latestLogBlobRandomId = "XYZ";
		try (ByteWriter w = dut.writeFileBlob("ABCD", meta)) {
			w.write(buffer, 0, buffer.length);
		}

		ArrayList<String> listFiles = Utils.toList(dut.listFileBlobs());
		assertEquals(1, listFiles.size());
		assertTrue(listFiles.contains("ABCD"));

		ArrayList<String> listLogs = Utils.toList(dut.listLogBlobs());
		assertEquals(2, listLogs.size());
		assertTrue(listLogs.contains("ABC"));
		assertTrue(listLogs.contains("ABCD"));

		try (ByteReader r = dut.readLogBlob("ABC")) {
			byte[] buffer2 = ByteReaderUtils.readAll(r);
			assertArrayEquals(buffer, buffer2);
		}

		try (ByteReader r = dut.readLogBlob("ABCD")) {
			byte[] buffer2 = ByteReaderUtils.readAll(r);
			assertArrayEquals(buffer, buffer2);
		}

		FileMetaData meta2 = new FileMetaData();
		try (ByteReader r = dut.readFileBlob("ABCD", meta2)) {
			byte[] buffer2 = ByteReaderUtils.readAll(r);
			assertArrayEquals(buffer, buffer2);
			assertEquals(meta.latestLogBlobLsn, meta2.latestLogBlobLsn);
			assertEquals(meta.latestLogBlobRandomId, meta2.latestLogBlobRandomId);
		}

		dut.deleteFileBlob("ABCD");
		dut.deleteLogBlob("ABCD");

		listFiles = Utils.toList(dut.listFileBlobs());
		assertEquals(0, listFiles.size());

		listLogs = Utils.toList(dut.listLogBlobs());
		assertEquals(1, listLogs.size());
		assertTrue(listLogs.contains("ABC"));
	}

}
