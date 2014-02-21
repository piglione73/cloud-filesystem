package com.paviasystem.cloudfilesystem.blocks;

import com.paviasystem.cloudfilesystem.blocks.data.DirectoryFileIndexEntry;
import com.paviasystem.cloudfilesystem.blocks.data.FileBlobIndexEntry;
import com.paviasystem.cloudfilesystem.blocks.data.LogBlobIndexEntry;

public interface Index {
	DirectoryFileIndexEntry readDirectoryFileEntry(String absolutePath);

	void writeDirectoryFileEntry(DirectoryFileIndexEntry entry);

	FileBlobIndexEntry readFileBlobEntry(String fileBlobName);

	void writeFileBlobEntry(FileBlobIndexEntry entry);

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
