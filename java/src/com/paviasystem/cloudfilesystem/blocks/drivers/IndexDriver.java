package com.paviasystem.cloudfilesystem.blocks.drivers;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import com.paviasystem.cloudfilesystem.Utils;
import com.paviasystem.cloudfilesystem.blocks.Index;
import com.paviasystem.cloudfilesystem.blocks.data.IndexEntry;
import com.paviasystem.cloudfilesystem.blocks.drivers.data.DirectoryFileIndexEntry;
import com.paviasystem.cloudfilesystem.blocks.drivers.data.FileBlobIndexEntry;
import com.paviasystem.cloudfilesystem.blocks.drivers.data.LogBlobIndexEntry;

public class IndexDriver {
	protected static final String Type_DirectoryOrFile = "DF";
	protected static final String Type_FileBlob = "FB";
	protected static final String Type_LogBlob = "LB";

	protected static final String Flag_IsFile = "F";
	protected static final String Flag_IsSoftLink = "L";

	private Index index;

	public IndexDriver(Index index) {
		this.index = index;
	}

	protected static DirectoryFileIndexEntry convertToDirectoryFileIndexEntry(IndexEntry ie) throws Exception {
		if (!ie.key1.equals(Type_DirectoryOrFile))
			throw new Exception("Wrong object type: " + ie.key1);

		DirectoryFileIndexEntry entry = new DirectoryFileIndexEntry();
		entry.absolutePath = ie.key2;
		entry.isFile = ie.data1.contains(Flag_IsFile);
		entry.isSoftLink = ie.data1.contains(Flag_IsSoftLink);
		entry.fileBlobName = entry.isFile && !entry.isSoftLink ? ie.data2 : "";
		entry.targetAbsolutePath = entry.isSoftLink ? ie.data2 : "";

		return entry;
	}

	protected static IndexEntry convertToIndexEntry(DirectoryFileIndexEntry entry) {
		IndexEntry ie = new IndexEntry();
		ie.key1 = Type_DirectoryOrFile;
		ie.key2 = entry.absolutePath;
		ie.data1 = (entry.isFile ? Flag_IsFile : "") + (entry.isSoftLink ? Flag_IsSoftLink : "");
		ie.data2 = entry.isSoftLink ? entry.targetAbsolutePath : entry.fileBlobName;
		return ie;
	}

	protected static FileBlobIndexEntry convertToFileBlobIndexEntry(IndexEntry ie) throws Exception {
		if (!ie.key1.equals(Type_FileBlob))
			throw new Exception("Wrong object type: " + ie.key1);

		FileBlobIndexEntry entry = new FileBlobIndexEntry();
		entry.fileBlobName = ie.key2;
		entry.latestLogBlobLsn = Long.parseLong(ie.data1);
		entry.latestLogBlobRandomId = ie.data2;
		entry.length = Long.parseLong(ie.data3);
		entry.creationTimestamp = new Date();
		entry.lastEditTimestamp = new Date();

		return entry;
	}

	protected static IndexEntry convertToIndexEntry(FileBlobIndexEntry entry) {
		IndexEntry ie = new IndexEntry();
		ie.key1 = Type_FileBlob;
		ie.key2 = entry.fileBlobName;
		ie.data1 = Utils.padLeft(entry.latestLogBlobLsn);
		ie.data2 = entry.latestLogBlobRandomId;
		ie.data3 = Utils.padLeft(entry.length);
		ie.data4 = entry.creationTimestamp.toString();
		ie.data5 = entry.lastEditTimestamp.toString();
		return ie;
	}

	public Iterable<DirectoryFileIndexEntry> listChildrenDirectoryFileEntries(String absolutePath) {
		String key2Prefix = absolutePath + "/";
		Iterable<IndexEntry> ies = index.list(Type_DirectoryOrFile, key2Prefix);
		final Iterator<IndexEntry> it = ies.iterator();

		return new Iterable<DirectoryFileIndexEntry>() {
			@Override
			public Iterator<DirectoryFileIndexEntry> iterator() {
				return new Iterator<DirectoryFileIndexEntry>() {
					@Override
					public boolean hasNext() {
						return it.hasNext();
					}

					@Override
					public DirectoryFileIndexEntry next() {
						try {
							return convertToDirectoryFileIndexEntry(it.next());
						} catch (Exception e) {
							e.printStackTrace();
							return null;
						}
					}

					@Override
					public void remove() {
						it.remove();
					}
				};
			}
		};
	}

	public DirectoryFileIndexEntry readDirectoryFileEntry(String absolutePath) throws Exception {
		IndexEntry ie = index.read(Type_DirectoryOrFile, absolutePath);
		if (ie == null)
			return null;

		return convertToDirectoryFileIndexEntry(ie);
	}

	public void writeDirectoryFileEntry(DirectoryFileIndexEntry entry) {
		if (entry == null)
			return;

		IndexEntry ie = convertToIndexEntry(entry);
		index.write(ie);
	}

	public void deleteDirectoryFileEntry(String absolutePath) {
		index.delete(Type_DirectoryOrFile, absolutePath);
	}

	public void updateDirectoryFileEntry(String oldAbsolutePath, String newAbsolutePath) {
		index.updateKey(Type_DirectoryOrFile, oldAbsolutePath, Type_DirectoryOrFile, newAbsolutePath);
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
