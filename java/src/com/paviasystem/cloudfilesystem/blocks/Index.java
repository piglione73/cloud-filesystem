package com.paviasystem.cloudfilesystem.blocks;

import com.paviasystem.cloudfilesystem.blocks.data.BlobIndexEntry;
import com.paviasystem.cloudfilesystem.blocks.data.DirectoryFileIndexEntry;

public interface Index {
	DirectoryFileIndexEntry readDirectoryFileEntry(String absolutePath);

	void writeDirectoryFileEntry(DirectoryFileIndexEntry entry);

	BlobIndexEntry readBlobEntry(String blobName);

	void writeBlobEntry(BlobIndexEntry entry);
	/*
	 * ArrayList<IndexEntry> listEntries(String absolutePath);
	 * 
	 * IndexEntry getEntry(String absolutePath);
	 * 
	 * void createDirectoryEntry(String absolutePath);
	 * 
	 * IndexEntry createFileEntry(String absolutePath, String blobName);
	 * 
	 * void updateEntry(String absolutePath, long length, Date timestamp);
	 * 
	 * void updateEntry(String absolutePath, String newAbsolutePath);
	 * 
	 * void deleteEntry(String absolutePath);
	 */
}
