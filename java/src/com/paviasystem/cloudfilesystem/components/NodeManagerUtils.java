package com.paviasystem.cloudfilesystem.components;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.paviasystem.cloudfilesystem.components.DirectoryNodeItem.Type;

public class NodeManagerUtils {

	private static final ByteBuffer zeros64K = ByteBuffer.allocate(64 * 1024);

	public static <T> T getNode(long nodeNumber, BlobStore localCache, BlobStore blobStore, Log log, BiFunction<Long, Blob, T> blobToNodeFunc, Function<T, Blob> nodeToBlobFunc, BiConsumer<LogEntry, T> applyLogEntryFunc) throws Exception {
		String blobName = Utils.padLeft(nodeNumber);

		// First, let's try to read from the local cache
		Blob blob = localCache.get(blobName);

		// If not found, then let's hit the blob store
		if (blob == null)
			blob = blobStore.get(blobName);

		// If not found, then it doesn't mean the node doesn't exist. It just
		// means its snapshot is empty
		if (blob == null)
			blob = new Blob(-1, Utils.createTempFileChannel());

		// Convert the blob to the desired node type
		T node = blobToNodeFunc.apply(nodeNumber, blob);

		// See what log entries we must apply to advance to the most up-to-date
		// version
		Iterable<LogEntry> logEntries = log.read(nodeNumber, blob.latestLogSequenceNumber + 1);

		// Apply the logEntries
		for (LogEntry logEntry : logEntries)
			applyLogEntryFunc.accept(logEntry, node);

		// Save the latest version in cache
		setNode(localCache, nodeNumber, node, nodeToBlobFunc);

		return node;
	}

	public static <T> void setNode(BlobStore destination, long nodeNumber, T node, Function<T, Blob> nodeToBlobFunc) throws Exception {
		String blobName = Utils.padLeft(nodeNumber);
		Blob latestBlob = nodeToBlobFunc.apply(node);
		destination.set(blobName, latestBlob);
	}

	public static FileNode blobToFileNode(long nodeNumber, Blob blob) {
		return new FileNode(nodeNumber, blob);
	}

	public static Blob fileNodeToBlob(FileNode fileNode) {
		return fileNode != null ? fileNode.blob : null;
	}

	public static void applyLogEntryToFileNode(LogEntry logEntry, FileNode fileNode) {
		try {
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
		} catch (IOException exc) {
			throw new RuntimeException(exc);
		}
	}

	public static DirectoryNode blobToDirectoryNode(long nodeNumber, Blob blob) {
		try {
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
		} catch (IOException exc) {
			throw new RuntimeException(exc);
		}
	}

	public static Blob directoryNodeToBlob(DirectoryNode dirNode) {
		if(dirNode==null)
			return null;
		
		try {
			Blob blob = new Blob(dirNode.latestLogSequenceNumber, Utils.createTempFileChannel());
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

			return blob;
		} catch (IOException exc) {
			throw new RuntimeException(exc);
		}
	}

	public static void applyLogEntryToDirectoryNode(LogEntry logEntry, DirectoryNode dirNode) {
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
