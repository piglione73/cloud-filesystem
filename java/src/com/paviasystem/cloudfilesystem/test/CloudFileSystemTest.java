package com.paviasystem.cloudfilesystem.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.paviasystem.cloudfilesystem.CloudFileSystem;
import com.paviasystem.cloudfilesystem.Utils;
import com.paviasystem.cloudfilesystem.blocks.drivers.IndexDriver;
import com.paviasystem.cloudfilesystem.blocks.drivers.data.DirectoryFileIndexEntry;
import com.paviasystem.cloudfilesystem.referenceimpl.MemoryBlobStore;
import com.paviasystem.cloudfilesystem.referenceimpl.MemoryIndex;
import com.paviasystem.cloudfilesystem.referenceimpl.MemoryLocalCache;

public class CloudFileSystemTest {
	CloudFileSystem fs;
	MemoryBlobStore blobStore;
	MemoryIndex index;
	IndexDriver indexDriver;
	MemoryLocalCache localCache;

	@Before
	public void setup() {
		blobStore = new MemoryBlobStore();
		index = new MemoryIndex();
		indexDriver = new IndexDriver(index);
		localCache = new MemoryLocalCache();
		fs = new CloudFileSystem(blobStore, index, localCache);
	}

	@Test
	public void test01_AllEmpty() {
		assertTrue(fs.listDirectory("/").isEmpty());
	}

	@Test
	public void test02_CreateDirectory() {
		fs.createDirectory("/A");

		ArrayList<DirectoryFileIndexEntry> dirEntries = Utils.toList(indexDriver.listChildrenDirectoryFileEntries("/"));
		assertEquals(1, dirEntries.size());
		DirectoryFileIndexEntry dirEntry = dirEntries.get(0);
		assertEquals("A", dirEntry.absolutePath);
		assertEquals("", dirEntry.fileBlobName);
		assertEquals(false, dirEntry.isFile);
		assertEquals(false, dirEntry.isSoftLink);
		assertEquals("", dirEntry.targetAbsolutePath);

		fs.deleteDirectory("A");
		dirEntries = Utils.toList(indexDriver.listChildrenDirectoryFileEntries("/"));
		assertEquals(0, dirEntries.size());
	}

	@Test
	public void test03_CreateSubDirectory() {
		fs.createDirectory("/A");
		fs.createDirectory("/A/1");
		fs.createDirectory("/A/2");
		fs.createDirectory("/B");
		fs.createDirectory("/B/3");
		fs.createDirectory("/B/4");

		ArrayList<DirectoryFileIndexEntry> dirEntries = Utils.toList(indexDriver.listChildrenDirectoryFileEntries("/"));
		assertEquals(2, dirEntries.size());

		DirectoryFileIndexEntry dirEntry = dirEntries.get(0);
		assertEquals("A", dirEntry.absolutePath);
		assertEquals("", dirEntry.fileBlobName);
		assertEquals(false, dirEntry.isFile);
		assertEquals(false, dirEntry.isSoftLink);
		assertEquals("", dirEntry.targetAbsolutePath);

		dirEntry = dirEntries.get(1);
		assertEquals("B", dirEntry.absolutePath);
		assertEquals("", dirEntry.fileBlobName);
		assertEquals(false, dirEntry.isFile);
		assertEquals(false, dirEntry.isSoftLink);
		assertEquals("", dirEntry.targetAbsolutePath);

		dirEntries = Utils.toList(indexDriver.listChildrenDirectoryFileEntries("/A"));
		assertEquals(2, dirEntries.size());

		dirEntry = dirEntries.get(0);
		assertEquals("A/1", dirEntry.absolutePath);
		assertEquals("", dirEntry.fileBlobName);
		assertEquals(false, dirEntry.isFile);
		assertEquals(false, dirEntry.isSoftLink);
		assertEquals("", dirEntry.targetAbsolutePath);

		dirEntry = dirEntries.get(1);
		assertEquals("A/2", dirEntry.absolutePath);
		assertEquals("", dirEntry.fileBlobName);
		assertEquals(false, dirEntry.isFile);
		assertEquals(false, dirEntry.isSoftLink);
		assertEquals("", dirEntry.targetAbsolutePath);

		dirEntries = Utils.toList(indexDriver.listChildrenDirectoryFileEntries("/B"));
		assertEquals(2, dirEntries.size());

		dirEntry = dirEntries.get(0);
		assertEquals("B/3", dirEntry.absolutePath);
		assertEquals("", dirEntry.fileBlobName);
		assertEquals(false, dirEntry.isFile);
		assertEquals(false, dirEntry.isSoftLink);
		assertEquals("", dirEntry.targetAbsolutePath);

		dirEntry = dirEntries.get(1);
		assertEquals("B/4", dirEntry.absolutePath);
		assertEquals("", dirEntry.fileBlobName);
		assertEquals(false, dirEntry.isFile);
		assertEquals(false, dirEntry.isSoftLink);
		assertEquals("", dirEntry.targetAbsolutePath);

	}
}
