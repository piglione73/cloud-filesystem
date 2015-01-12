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
		// Get the latest node snapshot
		Blob blob = NodeManagerUtils.getLatestNodeSnapshot(localCache, blobStore, nodeNumber);

		/*
		 * Now, we know from what snapshot to start. Let's apply log records to
		 * it in order to get the most up-to-date version of this file blob.
		 */
		Stream<LogEntry> logEntries = log.read(nodeNumber, blob.latestLogSequenceNumber + 1);
		logEntries.forEachOrdered(logEntry -> NodeManagerUtils.applyLogEntryToFileNodeBlob(logEntry, blob));
		
		/*
		 * Now that we have the most up-to-date snapshot, let's save in in cache
		 */
		NodeManagerUtils.cacheLatestNodeSnapshot(localCache,nodeNumber, blob);
		
		//Return the file node
		FileNode node = new FileNode(nodeNumber, blob);
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
