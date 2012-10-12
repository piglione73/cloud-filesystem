package com.paviasystem.cloudfs;

import java.util.ArrayList;

import com.paviasystem.cloudfs.log.FileSystemLog;
import com.paviasystem.cloudfs.log.FileSystemLogRecord;
import com.paviasystem.cloudfs.log.FileSystemLogRecord.Type;
import com.paviasystem.cloudfs.storage.Storage;

/**
 * The filesystem engine, where all operations are implemented.
 */
final class Engine {

	static boolean existsFile(String filePath, Storage storage, FileSystemLog[] logPipeline) {
		/*
		 * We want to know if the file exists right now.
		 * 
		 * We browse through the log pipeline looking for evidence as to whether
		 * or not the file exists. There is no need to browse through the entire
		 * log.
		 * 
		 * If the latest log entry is "Remove", then we can say the file does
		 * not exist. If the latest log entry is "Write" or "SetLength", then we
		 * can say the file exists.
		 * 
		 * If no entry about the file is found in the logs, then we hit the
		 * storage to see if the file exists.
		 */
		// Look for Remove/SetLength/Write from the end
		FileSystemLogRecord logRec = Log.findFirst(filePath, Direction.Descending, new Type[] {
			Type.Remove,
			Type.SetLength,
			Type.Write }, logPipeline);

		if (logRec != null) {
			// Interesting log record found
			// Let's analyze
			if (logRec.getType() == Type.Remove) {
				// Last operation on file = Remove. The file does not exist
				// anymore
				return false;
			} else if (logRec.getType() == Type.SetLength) {
				// Last operation on file = SetLength. The file exists
				return true;
			} else if (logRec.getType() == Type.Write) {
				// Last operation on file = Write. The file exists
				return true;
			} else {
				// This may not happen, unless we have a coding error
				throw new IllegalStateException("Unexpected log record type: " + logRec.getType());
			}
		} else {
			// Nothing in the log related to the file
			// Let's query the storage directly
			return storage.existsKey(Path.normalize(filePath));
		}
	}

	static boolean existsDirectory(String directory, Storage storage, FileSystemLog[] logPipeline) {
		/*
		 * We want to know if the directory exists right now.
		 * 
		 * We browse through the log pipeline looking for evidence as to whether
		 * or not the directory exists. There is no need to browse through the
		 * entire log.
		 * 
		 * If the latest log entry is "RemoveDirectory", then we can say the
		 * directory does not exist. If the latest log entry is
		 * "CreateDirectory", then we can say the directory exists.
		 * 
		 * If no entry about the directory is found in the logs, then we hit the
		 * storage to see if the directory exists.
		 */
		// Look for Remove/SetLength/Write from the end
		FileSystemLogRecord logRec = Log.findFirst(directory, Direction.Descending, new Type[] {
			Type.RemoveDirectory,
			Type.CreateDirectory }, logPipeline);

		if (logRec != null) {
			// Interesting log record found
			// Let's analyze
			if (logRec.getType() == Type.RemoveDirectory) {
				// Last operation on directory = RemoveDirectory. The directory
				// does not exist anymore
				return false;
			} else if (logRec.getType() == Type.CreateDirectory) {
				// Last operation on directory = CreateDirectory. The directory
				// exists
				return true;
			} else {
				// This may not happen, unless we have a coding error
				throw new IllegalStateException("Unexpected log record type: " + logRec.getType());
			}
		} else {
			// Nothing in the log related to the directory
			// Let's query the storage directly
			return storage.existsKey(Path.normalize(directory) + "/");
		}
	}

	static ArrayList<FileSystemEntry> list(String directory, Storage storage, FileSystemLog[] logPipeline) {
	}

	static void createDirectory(String directory, Storage storage, FileSystemLog[] logPipeline) {
		TODO;
	}

	static void deleteDirectory(String directory, Storage storage, FileSystemLog[] logPipeline) {
		TODO;
	}

	static FileSystemFile openFile(String filePath, boolean createIfMissing, Storage storage, FileSystemLog[] logPipeline) {
	}

}
