package com.paviasystem.cloudfilesystem.blocks.drivers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import com.paviasystem.cloudfilesystem.blocks.Index;
import com.paviasystem.cloudfilesystem.blocks.data.LogBlobIndexEntry;

public class IndexDriver implements Index {
	Index index;

	public IndexDriver(Index index) {
		this.index = index;
	}
	
	public LinkedList<LogBlobIndexEntry> readLogBlobEntries(String fileBlobName, long lsn1, String randomId1, long lsn2, String randomId2) {
		//First, read all log blob index entries >= lsn1 and <= lsn2 and arrange them in a two-level map, for subsequent faster lookups
		HashMap<Long, HashMap<String, LogBlobIndexEntry>> entries = new HashMap<>();
		for(LogBlobIndexEntry entry:index.readLogBlobEntries(fileBlobName, lsn1, lsn2)){
			HashMap<String, LogBlobIndexEntry> map = entries.get(entry.logBlobLsn);
			if(map == null) {
				map = new HashMap<>();
				entries.put(entry.logBlobLsn, map);
			}
			
			map.put(entry.logBlobRandomId, entry);
		}
		
		/*Then start from lsn2/randomId2 and proceed backwards up to lsn1/randomId1, removing all log entries that are not in the chain.
		 */
		LinkedList<LogBlobIndexEntry> chain = new LinkedList<>();
		long lsn = lsn2;
		String id = randomId2;
		while() {
			
		}
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
