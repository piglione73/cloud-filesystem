package com.paviasystem.cloudfilesystem.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.TreeSet;

import com.paviasystem.cloudfilesystem.Blob;
import com.paviasystem.cloudfilesystem.BlobStore;
import com.paviasystem.cloudfilesystem.LogEntry;
import com.paviasystem.cloudfilesystem.Utils;
import com.paviasystem.cloudfilesystem.impl.DirectoryNodeItem.Type;

public class NodeManagerUtils {

	private static final ByteBuffer zeros64K = ByteBuffer.allocate(64 * 1024);

	public static Blob getNodeBlob(BlobStore localCache, BlobStore blobStore, long nodeNumber) throws Exception {
		// First, let's try to read from the local cache
		String blobName = Utils.padLeft(nodeNumber);
		Blob blob = localCache.get(blobName);

		// If not found, then let's hit the blob store
		if (blob == null)
			blob = blobStore.get(blobName);

		/*
		 * If not found, then it doesn't mean the node doesn't exist. It just
		 * means its snapshot is empty
		 */
		if (blob == null)
			blob = new Blob(-1, Utils.createTempFileChannel());

		return blob;
	}

	public static FileNode getLatestFileNodeSnapshot(BlobStore localCache, BlobStore blobStore, long nodeNumber) throws Exception {
		Blob blob = getNodeBlob(localCache, blobStore, nodeNumber);
		return new FileNode(nodeNumber, blob);
	}

	public static void setLatestFileNodeSnapshot(BlobStore destination, long nodeNumber, FileNode fileNode) throws Exception {
		String blobName = Utils.padLeft(nodeNumber);
		if (fileNode != null)
			destination.set(blobName, fileNode.blob);
		else
			destination.set(blobName, null);
	}

	public static void applyLogEntryToFileNode(LogEntry logEntry, FileNode fileNode) throws IOException {
		SeekableByteChannel bytes = fileNode.blob.bytes;
		long size = bytes.size();

		// Here's the latest log seq. number incorporated into this node
		fileNode.blob.latestLogSequenceNumber = logEntry.logSequenceNumber;

		// Let's directly modify the bytes
		if (logEntry.type == LogEntry.FILE_SETLENGTH) {
			// Change the size. When extending, new bytes
			// are set to zero
			if (size < logEntry.length) {
				// Extend by appending null bytes
				bytes.position(size);
				long bytesToAppend = logEntry.length - size;

				while (bytesToAppend > 0) {
					/*
					 * Append in chunks (using the pre-allocated zeros64K
					 * buffer). We use a separate copy to avoid multi-thread
					 * interference
					 */
					ByteBuffer zeros;
					synchronized (zeros64K) {
						zeros = zeros64K.duplicate();
					}

					int chunkSize = (int) Math.min(zeros.capacity(), bytesToAppend);
					zeros.limit(chunkSize);
					bytes.write(zeros);
					bytesToAppend -= chunkSize;
				}
			} else if (size > logEntry.length) {
				// Truncate
				bytes.truncate(logEntry.length);
			}
		} else if (logEntry.type == LogEntry.FILE_WRITE) {
			// Update the blob bytes
			bytes.position(logEntry.positionFromEnd ? size - logEntry.position : logEntry.position);
			bytes.write(logEntry.bytes);
		} else
			throw new IllegalArgumentException("logEntry");
	}

	public static DirectoryNode getLatestDirectoryNodeSnapshot(BlobStore localCache, BlobStore blobStore, long nodeNumber) throws Exception {
		Blob blob = getNodeBlob(localCache, blobStore, nodeNumber);

		long lsn = blob.latestLogSequenceNumber;

		// Read all into ByteBuffer
		ByteBuffer buf = Utils.readFrom(blob.bytes, (int) blob.bytes.size());

		// Deserialize the listing
		TreeSet<DirectoryNodeItem> listing = new TreeSet<DirectoryNodeItem>();
		while (buf.position() < buf.limit()) {
			int binaryNameLength = buf.getInt();
			byte[] binaryName = new byte[binaryNameLength];
			buf.get(binaryName);
			String name = new String(binaryName, StandardCharsets.UTF_8);
			boolean itemIsDirectory = (buf.get() == 1);
			long itemNodeNumber = buf.getLong();

			DirectoryNodeItem item = new DirectoryNodeItem(name, itemIsDirectory ? Type.DIRECTORY : Type.FILE, itemNodeNumber);
			listing.add(item);
		}

		return new DirectoryNode(nodeNumber, lsn, listing);
	}

	public static void setLatestDirectoryNodeSnapshot(BlobStore destination, long nodeNumber, DirectoryNode dirNode) throws IOException, Exception {
		String blobName = Utils.padLeft(nodeNumber);
		if (dirNode == null) {
			destination.set(blobName, null);
			return;
		}

		try (Blob blob = new Blob(dirNode.latestLogSequenceNumber, Utils.createTempFileChannel())) {
			SeekableByteChannel bytes = blob.bytes;
			for (DirectoryNodeItem item : dirNode.listing) {
				// Serialize each DirectoryNodeItem
				ByteBuffer buf = ByteBuffer.allocate(1024);
				byte[] binaryName = item.name.getBytes(StandardCharsets.UTF_8);
				buf.putInt(binaryName.length);
				buf.put(binaryName);
				buf.put(item.type == Type.DIRECTORY ? (byte) 1 : (byte) 0);
				buf.putLong(item.nodeNumber);

				// Write
				buf.flip();
				bytes.write(buf);
			}

			destination.set(blobName, blob);
		}
	}

	public static void applyLogEntryToDirectoryNode(LogEntry logEntry, DirectoryNode dirNode) throws IOException {
		// Here's the latest log seq. number incorporated into this node
		dirNode.latestLogSequenceNumber = logEntry.logSequenceNumber;

		// Let's directly modify the listing
		if (logEntry.type == LogEntry.DIRECTORY_SETITEM) {
			// Add or edit item
			dirNode.listing.add(new DirectoryNodeItem(logEntry.itemName, logEntry.itemIsDirectory ? Type.DIRECTORY : Type.FILE, logEntry.itemNodeNumber));
		} else if (logEntry.type == LogEntry.DIRECTORY_DELETEITEM) {
			// Delete item by name (other members are ignored by
			// DirectoryNodeItem.equals)
			dirNode.listing.remove(new DirectoryNodeItem(logEntry.itemName, Type.FILE, 0));
		} else
			throw new IllegalArgumentException("logEntry");
	}
}
