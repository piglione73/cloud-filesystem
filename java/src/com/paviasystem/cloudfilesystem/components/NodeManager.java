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
		return NodeManagerUtils.getNode(nodeNumber, localCache, blobStore, log, NodeManagerUtils::blobToFileNode, NodeManagerUtils::fileNodeToBlob, NodeManagerUtils::applyLogEntryToFileNode);
	}

	public void setFileNode(long nodeNumber, FileNode fileNode) throws Exception {
		NodeManagerUtils.setNode(blobStore, nodeNumber, fileNode, NodeManagerUtils::fileNodeToBlob);
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
		return NodeManagerUtils.getNode(nodeNumber, localCache, blobStore, log, NodeManagerUtils::blobToDirectoryNode, NodeManagerUtils::directoryNodeToBlob, NodeManagerUtils::applyLogEntryToDirectoryNode);
	}

	public void setDirectoryNode(long nodeNumber, DirectoryNode dirNode) throws Exception {
		NodeManagerUtils.setNode(blobStore, nodeNumber, dirNode, NodeManagerUtils::directoryNodeToBlob);
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
