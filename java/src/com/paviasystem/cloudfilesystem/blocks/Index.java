package com.paviasystem.cloudfilesystem.blocks;

import com.paviasystem.cloudfilesystem.blocks.data.DirectoryFileIndexEntry;
import com.paviasystem.cloudfilesystem.blocks.data.FileBlobIndexEntry;
import com.paviasystem.cloudfilesystem.blocks.data.LogBlobIndexEntry;

public interface Index {
	Iterable<DirectoryFileIndexEntry> listChildrenDirectoryFileEntries(String absolutePath);

	DirectoryFileIndexEntry readDirectoryFileEntry(String absolutePath);

	void writeDirectoryFileEntry(DirectoryFileIndexEntry entry);

	void deleteDirectoryFileEntry(String absolutePath);

	void updateDirectoryFileEntry(String oldAbsolutePath, String newAbsolutePath);

	FileBlobIndexEntry readFileBlobEntry(String fileBlobName);

	void createFileBlobEntry(FileBlobIndexEntry blobEntry);

	boolean updateFileBlobEntry(FileBlobIndexEntry entry, String latestLogBlobNameToReplace);

	LogBlobIndexEntry readLogBlobEntry(String logBlobName);

	void writeLogBlobEntry(LogBlobIndexEntry entry);
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
