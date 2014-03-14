package com.paviasystem.cloudfilesystem.blocks;

import java.util.Date;

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

	boolean updateFileBlobEntry(String fileBlobName, long oldLatestLogBlobLsn, String oldLatestLogBlobRandomId, long newLatestLogBlobLsn, String newLatestLogBlobRandomId, long newLength, Date newLastEditTimestamp);

	LogBlobIndexEntry readLogBlobEntry(String logBlobName);

	void writeLogBlobEntry(LogBlobIndexEntry entry);
}
