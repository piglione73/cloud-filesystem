package com.paviasystem.cloudfilesystem.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.paviasystem.cloudfilesystem.Blob;
import com.paviasystem.cloudfilesystem.BlobStore;
import com.paviasystem.cloudfilesystem.LogEntry;
import com.paviasystem.cloudfilesystem.Utils;

public class NodeManagerUtils {

	private static ByteBuffer zeros64K = ByteBuffer.allocate(64 * 1024);

	public static Blob getLatestNodeSnapshot(BlobStore localCache, BlobStore blobStore, long nodeNumber) {
		// First, let's try to read from the local cache
		String blobName = Utils.padLeft(nodeNumber);
		Blob blob = localCache.get(blobName);

		// If not found, then let's hit the blob store
		if (blob == null)
			blob = blobStore.get(blobName);

		/*
		 * If not found, then it doesn't mean the node doesn't exist. It just
		 * means we don't have a snapshot to start from, so we only use the log.
		 */
		if (blob == null)
			blob = new Blob(-1,Utils.createTempFileChannel();

		return blob;
	}

	public static void cacheLatestNodeSnapshot(BlobStore localCache, long nodeNumber, Blob blob) {
		String blobName = Utils.padLeft(nodeNumber);
		localCache.set(blobName, blob);
	}

	public static void applyLogEntryToFileNodeBlob(LogEntry logEntry, Blob fileNodeBlob) throws IOException {
		FileChannel bytes = fileNodeBlob.bytes;
		long size = bytes.size();

		// Let's directly modify the bytes
		if (logEntry.type == LogEntry.FILE_SETLENGTH) {
			// Change the size. When extending, new bytes
			// are set to zero
			if (size < logEntry.length) {
				// Extend by appending null bytes
				bytes.position(size);
				long bytesToAppend = logEntry.length - size;

				while (bytesToAppend > 0) {
					// Append in chunks (using the pre-allocated zeros64K
					// buffer)
					int chunkSize = (int) Math.min(zeros64K.capacity(), bytesToAppend);
					zeros64K.limit(chunkSize);
					bytes.write(zeros64K);
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
}
