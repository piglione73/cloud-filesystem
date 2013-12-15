package com.paviasystem.cloudfs.locking;

import com.paviasystem.cloudfs.Action;

public interface LockManager {

	/**
	 * Executes an action while holding a lock on a directory.
	 * 
	 * @param directory
	 * @param action
	 */
	void withDirectoryLock(String directory, Action action);

	/**
	 * Executes an action while holding a lock on a file.
	 * 
	 * @param filePath
	 * @param action
	 */
	void withFileLock(String filePath, Action action);
}
