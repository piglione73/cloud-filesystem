package com.paviasystem.cloudfilesystem.blocks.drivers;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import com.paviasystem.cloudfilesystem.blocks.Index;
import com.paviasystem.cloudfilesystem.blocks.drivers.data.DirectoryFileIndexEntry;
import com.paviasystem.cloudfilesystem.blocks.drivers.data.FileBlobIndexEntry;
import com.paviasystem.cloudfilesystem.blocks.drivers.data.LogBlobIndexEntry;

public class IndexDriver {
	private Index index;

	public IndexDriver(Index index) {
		this.index = index;
	}

	public Iterable<DirectoryFileIndexEntry> listChildrenDirectoryFileEntries(String absolutePath) {
		TODO;
	}

	public DirectoryFileIndexEntry readDirectoryFileEntry(String absolutePath) {
		TODO;
	}

	public void writeDirectoryFileEntry(DirectoryFileIndexEntry entry) {
		TODO;
	}

	public void deleteDirectoryFileEntry(String absolutePath) {
		TODO;
	}

	public void updateDirectoryFileEntry(String oldAbsolutePath, String newAbsolutePath) {
		TODO;
	}

	public void createFileBlobEntry(FileBlobIndexEntry blobEntry) {
		TODO;
	}

	public FileBlobIndexEntry readFileBlobEntry(String fileBlobName) {
		TODO;
	}

	/**
	 * Given a DirectoryFileIndexEntry, finds the associated FileBlobIndexEntry
	 * possibly resolving soft links.
	 * 
	 * @param fileEntry
	 * @return Returns the FileBlobIndexEntry; never returns null
	 * @throws Exception
	 *             Throws an Exception if it cannot find the FileBlobIndexEntry
	 */
	public FileBlobIndexEntry getFileBlobEntry(DirectoryFileIndexEntry fileEntry) throws Exception {
		if (!fileEntry.isFile)
			throw new Exception("Not a file: " + fileEntry.absolutePath);

		// Recursively follow soft link, if soft link
		if (fileEntry.isSoftLink) {
			DirectoryFileIndexEntry linkedFileEntry = readDirectoryFileEntry(fileEntry.targetAbsolutePath);
			if (linkedFileEntry == null)
				throw new Exception("Broken soft link: " + fileEntry.absolutePath);

			return getFileBlobEntry(linkedFileEntry);
		}

		// If regular file, get file blob index entry
		FileBlobIndexEntry blobEntry = readFileBlobEntry(fileEntry.fileBlobName);
		if (blobEntry == null)
			throw new Exception("Missing blob: " + fileEntry.absolutePath + " --> " + fileEntry.fileBlobName);

		return blobEntry;
	}


	public boolean updateFileBlobEntry(String fileBlobName, long latestLogBlobLsn, String latestLogBlobRandomId, long logBlobLsn, String logBlobRandomId, long newLength, Date date) {
		TODO;
	}

	public LinkedList<LogBlobIndexEntry> readLogBlobEntries(String fileBlobName, long lsn1, String randomId1, long lsn2, String randomId2) {
		//First, read all log blob index entries >= lsn1 and <= lsn2 and arrange them in a two-level map, for subsequent faster lookups
		HashMap<Long, HashMap<String, LogBlobIndexEntry>> entries = new HashMap<>();
		for (LogBlobIndexEntry entry : index.readLogBlobEntries(fileBlobName, lsn1, lsn2)) {
			HashMap<String, LogBlobIndexEntry> map = entries.get(entry.logBlobLsn);
			if (map == null) {
				map = new HashMap<>();
				entries.put(entry.logBlobLsn, map);
			}

			map.put(entry.logBlobRandomId, entry);
		}

		/*
		 * Then start from lsn2/randomId2 and proceed backwards up to
		 * lsn1/randomId1, removing all log entries that are not in the chain.
		 */
		LinkedList<LogBlobIndexEntry> chain = new LinkedList<>();
		long lsn = lsn2;
		String id = randomId2;
		while (lsn >= lsn1) {
			//Find lsn/id
			HashMap<String, LogBlobIndexEntry> randomIds = entries.get(lsn);
			if (randomIds != null) {
				//LSN found
				LogBlobIndexEntry entry = randomIds.get(id);
				if (entry != null) {
					//Entry found: add to chain...
					chain.addFirst(entry);

					//...and prepare to move to previous entry
					lsn--;
					id = entry.previousLogBlobRandomId;
				} else {
					//Entry not found: end
					break;
				}
			} else {
				//LSN not found: end
				break;
			}
		}

		/*
		 * Now we have the chain in LSN order. Let's return it
		 */
		return chain;
	}

	public void writeLogBlobEntry(LogBlobIndexEntry lbie) {
		TODO;
	}
}
