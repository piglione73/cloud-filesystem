package com.paviasystem.cloudfilesystem;

import java.util.LinkedList;

import com.paviasystem.cloudfilesystem.blocks.Index;
import com.paviasystem.cloudfilesystem.blocks.data.DirectoryFileIndexEntry;
import com.paviasystem.cloudfilesystem.blocks.data.FileBlobIndexEntry;
import com.paviasystem.cloudfilesystem.blocks.data.LogBlobIndexEntry;

class Utils {

	/**
	 * Given a DirectoryFileIndexEntry, finds the associated FileBlobIndexEntry
	 * possibly resolving soft links.
	 * 
	 * @param index
	 * @param fileEntry
	 * @return Returns the FileBlobIndexEntry; never returns null
	 * @throws Exception
	 *             Throws an Exception if it cannot find the FileBlobIndexEntry
	 */
	public static FileBlobIndexEntry getFileBlobIndexEntry(Index index, DirectoryFileIndexEntry fileEntry) throws Exception {
		if (!fileEntry.isFile)
			throw new Exception("Not a file: " + fileEntry.absolutePath);

		// Recursively follow soft link, if soft link
		if (fileEntry.isSoftLink) {
			DirectoryFileIndexEntry linkedFileEntry = index.readDirectoryFileEntry(fileEntry.targetAbsolutePath);
			if (linkedFileEntry == null)
				throw new Exception("Broken soft link: " + fileEntry.absolutePath);

			return getFileBlobIndexEntry(index, linkedFileEntry);
		}

		// If regular file, get file blob index entry
		FileBlobIndexEntry blobEntry = index.readFileBlobEntry(fileEntry.blobName);
		if (blobEntry == null)
			throw new Exception("Missing blob: " + fileEntry.absolutePath + " --> " + fileEntry.blobName);

		return blobEntry;
	}

	/**
	 * Given a FileBlobIndexEntry, returns the list of log entries that have to
	 * be applied. Reads log lob entries starting from logBlobNameFrom1/2
	 * (excluded) up to logBlobNameTo (included). Since log entries are stored
	 * as a linked list in reverse order, the method actually starts from
	 * logBlobNameTo and proceeds backwards up to (and excluding)
	 * logBlobNameFrom1 or logBlobNameFrom2, whichever comes first. Since a log
	 * cleaner thread might be active, it is not guaranteed that the method can
	 * reach logBlobNameFrom1/2.
	 * 
	 * @param index
	 * @param logBlobNameFrom1
	 * @param logBlobNameFrom2
	 * @param logBlobNameTo
	 * @return
	 */
	public static LinkedList<LogBlobIndexEntry> getLogBlobIndexEntries(Index index, String logBlobNameFrom1, String logBlobNameFrom2, String logBlobNameTo) {
		LinkedList<LogBlobIndexEntry> entries = new LinkedList<LogBlobIndexEntry>();

		//Read backwards up to and excluding logBlobNameFrom
		String nextLogBlobNameToRead = logBlobNameTo;
		while (true) {
			LogBlobIndexEntry logBlobEntry = index.readLogBlobEntry(nextLogBlobNameToRead);
			if (logBlobEntry == null) {
				//If not found, stop
				break;
			}

			//If found, store
			entries.addFirst(logBlobEntry);

			//Then prepare to read the subsequent element
			nextLogBlobNameToRead = logBlobEntry.previousLogBlobName;

			//If we reached the end, stop
			if (nextLogBlobNameToRead == null || nextLogBlobNameToRead.trim().isEmpty()) {
				//Reached the first log entry, so we cannot proceed any further
				break;
			} else if (nextLogBlobNameToRead.equals(logBlobNameFrom1) || nextLogBlobNameToRead.equals(logBlobNameFrom2)) {
				//Reached logBlobNameFrom1/2, so we are required to stop
				break;
			}
		}

		return entries;
	}
}
