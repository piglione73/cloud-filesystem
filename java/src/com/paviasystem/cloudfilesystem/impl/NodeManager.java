package com.paviasystem.cloudfilesystem.impl;

import java.nio.ByteBuffer;
import java.util.stream.Stream;

import com.paviasystem.cloudfilesystem.Blob;
import com.paviasystem.cloudfilesystem.BlobStore;
import com.paviasystem.cloudfilesystem.Log;
import com.paviasystem.cloudfilesystem.LogEntry;
import com.paviasystem.cloudfilesystemold.Utils;

public class NodeManager {

	BlobStore blobStore;
	Log log;
	BlobStore localCache;

	/**
	 * Read the most up-to-date version of a given file node, by combining
	 * information taken from the blob store, the local cache and the log.
	 * 
	 * @param nodeNumber
	 * @return
	 */
	public FileNode getFileNode(long nodeNumber) {
		// First, let's try to read from the local cache
		String blobName = Utils.padLeft(nodeNumber);
		Blob blob = localCache.get(blobName);

		// If not found, then let's hit the blob store
		if (blob == null)
			blob = blobStore.get(blobName);

		/*
		 * If not found, then it doesn't mean the file doesn't exist. It just
		 * means we don't have a snapshot to start from, so we only use the log.
		 */
		if (blob == null) {
			blob = new Blob();
			blob.latestLogSequenceNumber = -1;
			blob.bytes = createEmptyFileChannel();
		}

		/*
		 * Now, we know from what snapshot to start. Let's apply log records to
		 * it in order to get the most up-to-date version of this file blob.
		 */
		Stream<LogEntry> logEntries = log.read(nodeNumber, blob.latestLogSequenceNumber + 1);

		logEntries.forEachOrdered(logEntry -> {
			// Let's directly modify the blob
				if (logEntry.type == LogEntry.FILE_SETLENGTH) {
					// Change the file channel size. When extending, new bytes
					// are set to zero
					if (blob.bytes.size() < logEntry.length) {
						blob.bytes.position(blob.bytes.size());
						int bytesToAppend = logEntry.length - blob.bytes.size();
						blob.bytes.write(ByteBuffer.allocate(bytesToAppend));
					} else if (blob.bytes.size() > logEntry.length)
						blob.bytes.truncate(logEntry.length);
				} else if (logEntry.type == LogEntry.FILE_WRITE) {
					blob.bytes.position(logEntry.positionFromEnd ? blob.bytes.size() - logEntry.position : logEntry.position);
					blob.bytes.write(logEntry.bytes);
				}
			});

	}

	/**
	 * Read the most up-to-date version of a given directory node, by combining
	 * information taken from the blob store, the local cache and the log.
	 * 
	 * @param nodeNumber
	 * @return
	 */
	public DirectoryNode getDirectoryNode(long nodeNumber) {

	}
}
