package com.paviasystem.cloudfilesystem.test.drivers;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

import com.paviasystem.cloudfilesystem.blocks.data.IndexEntry;
import com.paviasystem.cloudfilesystem.blocks.drivers.IndexDriver;
import com.paviasystem.cloudfilesystem.blocks.drivers.data.DirectoryFileIndexEntry;
import com.paviasystem.cloudfilesystem.blocks.drivers.data.FileBlobIndexEntry;
import com.paviasystem.cloudfilesystem.blocks.drivers.data.LogBlobIndexEntry;

public class IndexDriverTest extends IndexDriver {

	public IndexDriverTest() {
		super(null);
	}

	@Test
	public void test_convertToDirectoryFileIndexEntry() throws Exception {
		IndexEntry ie = new IndexEntry();
		ie.key1 = Type_DirectoryOrFile;
		ie.key2 = "A/B/C";
		ie.data1 = "";
		ie.data2 = "";

		DirectoryFileIndexEntry entry = convertToDirectoryFileIndexEntry(ie);
		assertEquals("A/B/C", entry.absolutePath);
		assertEquals(false, entry.isFile);
		assertEquals(false, entry.isSoftLink);
		assertEquals("", entry.fileBlobName);
		assertEquals("", entry.targetAbsolutePath);

		ie.key1 = Type_DirectoryOrFile;
		ie.key2 = "A/B/C";
		ie.data1 = Flag_IsFile;
		ie.data2 = "BLOB";

		entry = convertToDirectoryFileIndexEntry(ie);
		assertEquals("A/B/C", entry.absolutePath);
		assertEquals(true, entry.isFile);
		assertEquals(false, entry.isSoftLink);
		assertEquals("BLOB", entry.fileBlobName);
		assertEquals("", entry.targetAbsolutePath);

		ie.key1 = Type_DirectoryOrFile;
		ie.key2 = "A/B/C";
		ie.data1 = Flag_IsSoftLink;
		ie.data2 = "C/D/E";

		entry = convertToDirectoryFileIndexEntry(ie);
		assertEquals("A/B/C", entry.absolutePath);
		assertEquals(false, entry.isFile);
		assertEquals(true, entry.isSoftLink);
		assertEquals("", entry.fileBlobName);
		assertEquals("C/D/E", entry.targetAbsolutePath);

		ie.key1 = Type_DirectoryOrFile;
		ie.key2 = "A/B/C";
		ie.data1 = Flag_IsFile + Flag_IsSoftLink;
		ie.data2 = "C/D/E";

		entry = convertToDirectoryFileIndexEntry(ie);
		assertEquals("A/B/C", entry.absolutePath);
		assertEquals(true, entry.isFile);
		assertEquals(true, entry.isSoftLink);
		assertEquals("", entry.fileBlobName);
		assertEquals("C/D/E", entry.targetAbsolutePath);
	}

	@Test
	public void test_convertDirectoryFileIndexEntryToIndexEntry() throws Exception {
		DirectoryFileIndexEntry entry = new DirectoryFileIndexEntry();
		entry.absolutePath = "A/B/C";
		entry.isFile = false;
		entry.isSoftLink = false;
		entry.fileBlobName = "";
		entry.targetAbsolutePath = "";

		IndexEntry ie = convertToIndexEntry(entry);
		assertEquals(Type_DirectoryOrFile, ie.key1);
		assertEquals("A/B/C", ie.key2);
		assertEquals("", ie.data1);
		assertEquals("", ie.data2);

		entry = new DirectoryFileIndexEntry();
		entry.absolutePath = "A/B/C";
		entry.isFile = true;
		entry.isSoftLink = false;
		entry.fileBlobName = "BLOB";
		entry.targetAbsolutePath = "";

		ie = convertToIndexEntry(entry);
		assertEquals(Type_DirectoryOrFile, ie.key1);
		assertEquals("A/B/C", ie.key2);
		assertEquals(Flag_IsFile, ie.data1);
		assertEquals("BLOB", ie.data2);

		entry = new DirectoryFileIndexEntry();
		entry.absolutePath = "A/B/C";
		entry.isFile = true;
		entry.isSoftLink = true;
		entry.fileBlobName = "";
		entry.targetAbsolutePath = "C/D/E";

		ie = convertToIndexEntry(entry);
		assertEquals(Type_DirectoryOrFile, ie.key1);
		assertEquals("A/B/C", ie.key2);
		assertEquals(Flag_IsFile + Flag_IsSoftLink, ie.data1);
		assertEquals("C/D/E", ie.data2);
	}

	@Test
	public void test_convertToFileBlobIndexEntry() throws Exception {
		IndexEntry ie = new IndexEntry();
		ie.key1 = Type_FileBlob;
		ie.key2 = "BLOB";
		ie.data1 = "0000000000000000057";
		ie.data2 = "XYZ";
		ie.data3 = "9223372036854775807";
		ie.data4 = "1970-01-01T00:00:00.000";
		ie.data5 = "1970-01-01T00:00:01.000";

		FileBlobIndexEntry entry = convertToFileBlobIndexEntry(ie);
		assertEquals("BLOB", entry.fileBlobName);
		assertEquals(57, entry.latestLogBlobLsn);
		assertEquals("XYZ", entry.latestLogBlobRandomId);
		assertEquals(Long.MAX_VALUE, entry.length);
		assertEquals(new Date(0), entry.lastEditTimestamp);
		assertEquals(new Date(1000), entry.creationTimestamp);
	}

	@Test
	public void test_convertFileBlobIndexEntryToIndexEntry() throws Exception {
		FileBlobIndexEntry entry = new FileBlobIndexEntry();
		entry.fileBlobName = "BLOB";
		entry.latestLogBlobLsn = 57;
		entry.latestLogBlobRandomId = "XYZ";
		entry.length = Long.MAX_VALUE;
		entry.lastEditTimestamp = new Date(0);
		entry.creationTimestamp = new Date(1000);

		IndexEntry ie = convertToIndexEntry(entry);
		assertEquals(Type_FileBlob, ie.key1);
		assertEquals("BLOB", ie.key2);
		assertEquals("0000000000000000057", ie.data1);
		assertEquals("XYZ", ie.data2);
		assertEquals("9223372036854775807", ie.data3);
		assertEquals("1970-01-01T00:00:00.000", ie.data4);
		assertEquals("1970-01-01T00:00:01.000", ie.data5);
	}

	@Test
	public void test_convertToLogBlobIndexEntry() throws Exception {
		IndexEntry ie = new IndexEntry();
		ie.key1 = Type_LogBlob;
		ie.key2 = "BLOB";
		ie.data1 = "0000000000000000057";
		ie.data2 = "XYZ";
		ie.data3 = "QWE";
		ie.data4 = "1970-01-01T00:00:09.999";

		LogBlobIndexEntry entry = convertToLogBlobIndexEntry(ie);
		assertEquals("BLOB", entry.fileBlobName);
		assertEquals(57, entry.logBlobLsn);
		assertEquals("XYZ", entry.logBlobRandomId);
		assertEquals("QWE", entry.previousLogBlobRandomId);
		assertEquals(new Date(9999), entry.creationTimestamp);
	}

	@Test
	public void test_convertLogBlobIndexEntryToIndexEntry() throws Exception {
		LogBlobIndexEntry entry = new LogBlobIndexEntry();
		entry.fileBlobName = "BLOB";
		entry.logBlobLsn = 57;
		entry.logBlobRandomId = "XYZ";
		entry.previousLogBlobRandomId = "QWE";
		entry.creationTimestamp = new Date(9999);

		IndexEntry ie = convertToIndexEntry(entry);
		assertEquals(Type_LogBlob, ie.key1);
		assertEquals("BLOB", ie.key2);
		assertEquals("0000000000000000057", ie.data1);
		assertEquals("XYZ", ie.data2);
		assertEquals("QWE", ie.data3);
		assertEquals("1970-01-01T00:00:09.999", ie.data4);
	}
}
