package com.paviasystem.cloudfilesystem.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.TreeSet;

import org.junit.Test;

import com.paviasystem.cloudfilesystem.Blob;
import com.paviasystem.cloudfilesystem.LogEntry;
import com.paviasystem.cloudfilesystem.Utils;
import com.paviasystem.cloudfilesystem.impl.DirectoryNode;
import com.paviasystem.cloudfilesystem.impl.DirectoryNodeItem;
import com.paviasystem.cloudfilesystem.impl.DirectoryNodeItem.Type;
import com.paviasystem.cloudfilesystem.impl.FileNode;
import com.paviasystem.cloudfilesystem.impl.NodeManagerUtils;
import com.paviasystem.cloudfilesystem.referenceimpl.MemoryBlobStore;
import com.paviasystem.cloudfilesystem.referenceimpl.MemorySeekableByteChannel;

public class NodeManagerUtilsTest {
	@Test
	public void test01_FileGetSet() throws Exception {
		MemoryBlobStore bs1 = new MemoryBlobStore();
		MemoryBlobStore bs2 = new MemoryBlobStore();

		NodeManagerUtils.setLatestFileNodeSnapshot(bs1, 1, new FileNode(1, Blob.from(10, "Node 1 - BS1")));
		NodeManagerUtils.setLatestFileNodeSnapshot(bs2, 1, new FileNode(1, Blob.from(20, "Node 1 - BS2")));

		FileNode node = NodeManagerUtils.getLatestFileNodeSnapshot(bs1, bs2, 1);
		assertEquals(1, node.nodeNumber);
		assertEquals(10, node.blob.latestLogSequenceNumber);
		assertEquals("Node 1 - BS1", node.blob.toString());

		node = NodeManagerUtils.getLatestFileNodeSnapshot(bs2, bs1, 1);
		assertEquals(1, node.nodeNumber);
		assertEquals(20, node.blob.latestLogSequenceNumber);
		assertEquals("Node 1 - BS2", node.blob.toString());

		NodeManagerUtils.setLatestFileNodeSnapshot(bs2, 1, null);

		node = NodeManagerUtils.getLatestFileNodeSnapshot(bs2, bs1, 1);
		assertEquals(1, node.nodeNumber);
		assertEquals(10, node.blob.latestLogSequenceNumber);
		assertEquals("Node 1 - BS1", node.blob.toString());
	}

	@Test
	public void test02_DirectoryGetSet() throws Exception {
		MemoryBlobStore bs1 = new MemoryBlobStore();
		MemoryBlobStore bs2 = new MemoryBlobStore();

		TreeSet<DirectoryNodeItem> listing1 = new TreeSet<DirectoryNodeItem>();
		listing1.add(new DirectoryNodeItem("Item 1", Type.FILE, 2));
		listing1.add(new DirectoryNodeItem("Item 2", Type.DIRECTORY, 3));
		listing1.add(new DirectoryNodeItem("Item 3", Type.FILE, 4));

		TreeSet<DirectoryNodeItem> listing2 = new TreeSet<DirectoryNodeItem>();
		listing2.add(new DirectoryNodeItem("Item 11", Type.FILE, 22));
		listing2.add(new DirectoryNodeItem("Item 22", Type.DIRECTORY, 33));

		NodeManagerUtils.setLatestDirectoryNodeSnapshot(bs1, 1, new DirectoryNode(1, 10, listing1));
		NodeManagerUtils.setLatestDirectoryNodeSnapshot(bs2, 1, new DirectoryNode(1, 20, listing2));

		DirectoryNode node = NodeManagerUtils.getLatestDirectoryNodeSnapshot(bs1, bs2, 1);
		assertEquals(1, node.nodeNumber);
		assertEquals(10, node.latestLogSequenceNumber);
		assertEquals(3, node.listing.size());
		DirectoryNodeItem[] y = node.listing.toArray(new DirectoryNodeItem[3]);
		assertEquals("Item 1", y[0].name);
		assertEquals(2, y[0].nodeNumber);
		assertEquals(Type.FILE, y[0].type);
		assertEquals("Item 2", y[1].name);
		assertEquals(3, y[1].nodeNumber);
		assertEquals(Type.DIRECTORY, y[1].type);
		assertEquals("Item 3", y[2].name);
		assertEquals(4, y[2].nodeNumber);
		assertEquals(Type.FILE, y[2].type);

		node = NodeManagerUtils.getLatestDirectoryNodeSnapshot(bs2, bs1, 1);
		assertEquals(1, node.nodeNumber);
		assertEquals(20, node.latestLogSequenceNumber);
		assertEquals(2, node.listing.size());
		y = node.listing.toArray(new DirectoryNodeItem[3]);
		assertEquals("Item 11", y[0].name);
		assertEquals(22, y[0].nodeNumber);
		assertEquals(Type.FILE, y[0].type);
		assertEquals("Item 22", y[1].name);
		assertEquals(33, y[1].nodeNumber);
		assertEquals(Type.DIRECTORY, y[1].type);

		NodeManagerUtils.setLatestDirectoryNodeSnapshot(bs2, 1, null);

		node = NodeManagerUtils.getLatestDirectoryNodeSnapshot(bs2, bs1, 1);
		assertEquals(1, node.nodeNumber);
		assertEquals(10, node.latestLogSequenceNumber);
		assertEquals(3, node.listing.size());
		y = node.listing.toArray(new DirectoryNodeItem[3]);
		assertEquals("Item 1", y[0].name);
		assertEquals(2, y[0].nodeNumber);
		assertEquals(Type.FILE, y[0].type);
		assertEquals("Item 2", y[1].name);
		assertEquals(3, y[1].nodeNumber);
		assertEquals(Type.DIRECTORY, y[1].type);
		assertEquals("Item 3", y[2].name);
		assertEquals(4, y[2].nodeNumber);
		assertEquals(Type.FILE, y[2].type);
	}

	@Test
	public void test03_DirectoryApply() throws Exception {
		TreeSet<DirectoryNodeItem> listing = new TreeSet<DirectoryNodeItem>();
		DirectoryNode dirNode = new DirectoryNode(1, 2, listing);

		NodeManagerUtils.applyLogEntryToDirectoryNode(new LogEntry(100, 1, LogEntry.DIRECTORY_SETITEM, 0, false, 0, null, "Item1", false, 10), dirNode);

		assertEquals(100, dirNode.latestLogSequenceNumber);
		DirectoryNodeItem[] y = listing.toArray(new DirectoryNodeItem[0]);
		assertEquals(1, y.length);
		assertEquals("Item1", y[0].name);
		assertEquals(10, y[0].nodeNumber);
		assertEquals(Type.FILE, y[0].type);

		NodeManagerUtils.applyLogEntryToDirectoryNode(new LogEntry(101, 1, LogEntry.DIRECTORY_SETITEM, 0, false, 0, null, "Item0", true, 11), dirNode);

		assertEquals(101, dirNode.latestLogSequenceNumber);
		y = listing.toArray(new DirectoryNodeItem[0]);
		assertEquals(2, y.length);
		assertEquals("Item0", y[0].name);
		assertEquals(11, y[0].nodeNumber);
		assertEquals(Type.DIRECTORY, y[0].type);
		assertEquals("Item1", y[1].name);
		assertEquals(10, y[1].nodeNumber);
		assertEquals(Type.FILE, y[1].type);

		NodeManagerUtils.applyLogEntryToDirectoryNode(new LogEntry(102, 1, LogEntry.DIRECTORY_DELETEITEM, 0, false, 0, null, "ItemXXX", false, 0), dirNode);

		assertEquals(102, dirNode.latestLogSequenceNumber);
		y = listing.toArray(new DirectoryNodeItem[0]);
		assertEquals(2, y.length);
		assertEquals("Item0", y[0].name);
		assertEquals(11, y[0].nodeNumber);
		assertEquals(Type.DIRECTORY, y[0].type);
		assertEquals("Item1", y[1].name);
		assertEquals(10, y[1].nodeNumber);
		assertEquals(Type.FILE, y[1].type);

		NodeManagerUtils.applyLogEntryToDirectoryNode(new LogEntry(103, 1, LogEntry.DIRECTORY_DELETEITEM, 0, false, 0, null, "Item0", false, 0), dirNode);

		assertEquals(103, dirNode.latestLogSequenceNumber);
		y = listing.toArray(new DirectoryNodeItem[0]);
		assertEquals(1, y.length);
		assertEquals("Item1", y[0].name);
		assertEquals(10, y[0].nodeNumber);
		assertEquals(Type.FILE, y[0].type);

		NodeManagerUtils.applyLogEntryToDirectoryNode(new LogEntry(104, 1, LogEntry.DIRECTORY_DELETEITEM, 0, false, 0, null, "Item1", false, 0), dirNode);

		assertEquals(104, dirNode.latestLogSequenceNumber);
		y = listing.toArray(new DirectoryNodeItem[0]);
		assertEquals(0, y.length);
	}

	@Test
	public void test04_FileApply() throws Exception {
		MemorySeekableByteChannel bytes = new MemorySeekableByteChannel();
		Blob blob = new Blob(100, bytes);
		FileNode fileNode = new FileNode(1, blob);

		check(blob, 100, new byte[] {});

		NodeManagerUtils.applyLogEntryToFileNode(new LogEntry(101, 1, LogEntry.FILE_SETLENGTH, 0, false, 10, null, null, false, 0), fileNode);
		check(blob, 101, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });

		NodeManagerUtils.applyLogEntryToFileNode(new LogEntry(102, 1, LogEntry.FILE_WRITE, 2, false, 0, ByteBuffer.wrap(new byte[] { 1, 2, 3 }), null, false, 0), fileNode);
		check(blob, 102, new byte[] { 0, 0, 1, 2, 3, 0, 0, 0, 0, 0 });

		NodeManagerUtils.applyLogEntryToFileNode(new LogEntry(103, 1, LogEntry.FILE_WRITE, 8, false, 0, ByteBuffer.wrap(new byte[] { 1, 2, 3 }), null, false, 0), fileNode);
		check(blob, 103, new byte[] { 0, 0, 1, 2, 3, 0, 0, 0, 1, 2, 3 });

		NodeManagerUtils.applyLogEntryToFileNode(new LogEntry(104, 1, LogEntry.FILE_WRITE, 0, true, 0, ByteBuffer.wrap(new byte[] { 100, 101, 102 }), null, false, 0), fileNode);
		check(blob, 104, new byte[] { 0, 0, 1, 2, 3, 0, 0, 0, 1, 2, 3, 100, 101, 102 });

		NodeManagerUtils.applyLogEntryToFileNode(new LogEntry(105, 1, LogEntry.FILE_WRITE, 8, true, 0, ByteBuffer.wrap(new byte[] { 9, 9, 9, 9 }), null, false, 0), fileNode);
		check(blob, 105, new byte[] { 0, 0, 1, 2, 3, 0, 9, 9, 9, 9, 3, 100, 101, 102 });

		NodeManagerUtils.applyLogEntryToFileNode(new LogEntry(106, 1, LogEntry.FILE_SETLENGTH, 0, false, 4, null, null, false, 0), fileNode);
		check(blob, 106, new byte[] { 0, 0, 1, 2 });

		NodeManagerUtils.applyLogEntryToFileNode(new LogEntry(107, 1, LogEntry.FILE_SETLENGTH, 0, false, 10, null, null, false, 0), fileNode);
		check(blob, 107, new byte[] { 0, 0, 1, 2, 0, 0, 0, 0, 0, 0 });
	}

	private void check(Blob blob, long lsn, byte[] bytes) throws IOException {
		assertEquals(lsn, blob.latestLogSequenceNumber);
		SeekableByteChannel channel = blob.bytes;
		channel.position(0);
		ByteBuffer buf = Utils.readFrom(channel, (int) channel.size());
		assertArrayEquals(bytes, buf.array());
	}
}
