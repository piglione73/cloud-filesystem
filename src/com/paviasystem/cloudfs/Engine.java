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
