package com.paviasystem.cloudfilesystem.components;

import java.io.IOException;
import java.util.Optional;

import com.paviasystem.cloudfilesystem.Path;
import com.paviasystem.cloudfilesystem.components.DirectoryNodeItem.Type;
import com.paviasystem.cloudfilesystem.exceptions.DirectoryNotFoundException;

public class NodeManager {

	final BlobStore blobStore;
	final BlobStore localCache;
	final Log log;

	public NodeManager(BlobStore blobStore, BlobStore localCache, Log log) {
		this.blobStore = blobStore;
		this.localCache = localCache;
		this.log = log;
	}

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

	public DirectoryNode getDirectoryNode(String absolutePath) throws Exception {
		return getDirectoryNode(Path.decompose(absolutePath));
	}

	public DirectoryNode getDirectoryNode(String[] pathParts) throws Exception {
		// Node 0 is the root directory
		DirectoryNode curDir = getDirectoryNode(0);

		// Now follow the tree of nodes down to the given path
		for (String pathPart : pathParts) {
			// Look for pathPart in the listing
			Optional<DirectoryNodeItem> listingItem = curDir.listing.stream().filter(item -> item.name.equals(pathPart)).findFirst();
			curDir.close();

			// It must exist and be a directory
			if (!listingItem.isPresent())
				throw new DirectoryNotFoundException(pathParts, "Directory not found");

			DirectoryNodeItem listingItemValue = listingItem.get();
			if (listingItemValue.type != Type.DIRECTORY)
				throw new DirectoryNotFoundException(pathParts, "Not a directory");

			// Ok, so let's navigate to it and repeat
			curDir = getDirectoryNode(listingItemValue.nodeNumber);
		}

		return curDir;
	}
}
