package com.paviasystem.cloudfsobsolete;

import java.util.ArrayList;

import com.paviasystem.cloudfsobsolete.locking.LockManager;
import com.paviasystem.cloudfsobsolete.log.FileSystemLog;
import com.paviasystem.cloudfsobsolete.log.FileSystemLogRecord;
import com.paviasystem.cloudfsobsolete.log.FileSystemLogRecord.Type;
import com.paviasystem.cloudfsobsolete.storage.Storage;

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
		// TODO: no operation
		return new ArrayList<FileSystemEntry>();
	}

	static void createDirectory(final String directory, Storage storage, final FileSystemLog[] logPipeline, LockManager lockManager) {
		/*
		 * A directory can be created only if it does not exist. The operation
		 * only affects the log.
		 * 
		 * Make sure the parent directory exists.
		 * 
		 * Then, holding a lock on the parent directory, create the requested
		 * directory.
		 */
		// Make sure the parent directory exists, recursively
		if (Path.isRoot(directory)) {
			// The root directory cannot be created: it exists implicitly
			return;
		} else {
			// It is not the root directory, so it has a parent; let's make sure
			// the parent exists
			String parentDirectory = Path.getParent(directory);
			createDirectory(parentDirectory, storage, logPipeline, lockManager);

			// Here, we are sure all the ancestor directories exist
			// Let's lock the parent
			lockManager.withDirectoryLock(parentDirectory, new Action() {
				@Override
				public void run() {
					// While holding a lock on the parent, create the requested
					// directory
					FileSystemLogRecord record = FileSystemLogRecord.createDirectory(directory);
					Log.write(record, logPipeline);
				}
			});
		}
	}

	static void deleteDirectory(final String directory, final Storage storage, final FileSystemLog[] logPipeline, final LockManager lockManager) {
		/*
		 * A directory can be deleted always. If not empty, the content must be
		 * deleted recursively. Hold a lock on directory for the entire
		 * operation. Hold a lock on each file while deleting it: we must make
		 * sure we cannot delete an open file.
		 */
		lockManager.withDirectoryLock(directory, new Action() {
			@Override
			public void run() throws Exception {
				// List the directory content
				ArrayList<FileSystemEntry> list = list(directory, storage, logPipeline);

				// Delete each element, recursively
				for (FileSystemEntry entry : list) {
					if (entry.isFile()) {
						// If file, remove it
						deleteFile(entry.getAbsolutePath(), logPipeline, lockManager);
					} else {
						// If directory, descend recursively...
						deleteDirectory(directory, storage, logPipeline, lockManager);
					}
				}

				// Now the directory is empty: let's delete it
				FileSystemLogRecord record = FileSystemLogRecord.removeDirectory(directory);
				Log.write(record, logPipeline);
			}
		});
	}

	static void deleteFile(final String filePath, final FileSystemLog[] logPipeline, final LockManager lockManager) {
		/*
		 * While holding a lock on the file, remove it
		 */
		lockManager.withFileLock(filePath, new Action() {
			@Override
			public void run() throws Exception {
				FileSystemLogRecord record = FileSystemLogRecord.remove(filePath);
				Log.write(record, logPipeline);
			}
		});
	}

	static FileSystemFile openFile(String filePath, boolean createIfMissing, Storage storage, FileSystemLog[] logPipeline) {
		// TODO: no operation
		return null;
	}

}
