package com.paviasystem.cloudfilesystem.impl;

import java.io.IOException;

import com.paviasystem.cloudfilesystem.components.BlobStore;
import com.paviasystem.cloudfilesystem.components.Log;
import com.paviasystem.cloudfilesystem.components.LogEntry;

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
	 * @throws IOException
	 */
	public FileNode getFileNode(long nodeNumber) throws Exception {
		// Get the latest node snapshot
		FileNode node = NodeManagerUtils.getLatestFileNodeSnapshot(localCache, blobStore, nodeNumber);

		/*
		 * Now, we know from what snapshot to start. Let's apply log records to
		 * it in order to get the most up-to-date version of this node.
		 */
		Iterable<LogEntry> logEntries = log.read(nodeNumber, node.blob.latestLogSequenceNumber + 1);
		for (LogEntry logEntry : logEntries)
			NodeManagerUtils.applyLogEntryToFileNode(logEntry, node);

		/*
		 * Now that we have the most up-to-date snapshot, let's save it in cache
		 */
		NodeManagerUtils.setLatestFileNodeSnapshot(localCache, nodeNumber, node);

		// Return the node
		return node;
	}

	/**
	 * Read the most up-to-date version of a given directory node, by combining
	 * information taken from the blob store, the local cache and the log.
	 * 
	 * @param nodeNumber
	 * @return
	 * @throws Exception
	 */
	public DirectoryNode getDirectoryNode(long nodeNumber) throws Exception {
		// Get the latest node snapshot
		DirectoryNode node = NodeManagerUtils.getLatestDirectoryNodeSnapshot(localCache, blobStore, nodeNumber);

		/*
		 * Now, we know from what snapshot to start. Let's apply log records to
		 * it in order to get the most up-to-date version of this node.
		 */
		Iterable<LogEntry> logEntries = log.read(nodeNumber, node.latestLogSequenceNumber + 1);
		for (LogEntry logEntry : logEntries)
			NodeManagerUtils.applyLogEntryToDirectoryNode(logEntry, node);

		/*
		 * Now that we have the most up-to-date snapshot, let's save it in cache
		 */
		NodeManagerUtils.setLatestDirectoryNodeSnapshot(localCache, nodeNumber, node);

		// Return the node
		return node;
	}
}
