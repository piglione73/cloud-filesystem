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
		FileBlobIndexEntry blobEntry = index.readFileBlobEntry(fileEntry.fileBlobName);
		if (blobEntry == null)
			throw new Exception("Missing blob: " + fileEntry.absolutePath + " --> " + fileEntry.fileBlobName);

		return blobEntry;
	}

}
