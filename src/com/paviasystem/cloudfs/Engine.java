package com.paviasystem.cloudfs;

import java.util.ArrayList;

import com.paviasystem.cloudfs.log.FileSystemLog;
import com.paviasystem.cloudfs.storage.Storage;

/**
 * The filesystem engine, where all operations are implemented.
 */
final class Engine {
	private Engine() {
	}

	static boolean existsFile(String filePath, Storage storage, FileSystemLog[] logPipeline) {
		/*
		 * We want to know if the file exists right now.
		 * 
		 * We browse through the log pipeline looking for evidence that the file
		 * does or does not exist. There is no need to browse through the entire
		 * log.
		 * 
		 * If the latest log entry is "Remove", then we can say the file does
		 * not exist. If the latest log entry is "Write" or "SetLength", then we
		 * can say the file exists.
		 * 
		 * If no entry about the file is found in the logs, then we hit the
		 * storage to see if the file exists.
		 */
	}

	static boolean existsDirectory(String directory, Storage storage, FileSystemLog[] logPipeline) {
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
