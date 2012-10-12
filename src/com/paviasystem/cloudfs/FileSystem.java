package com.paviasystem.cloudfs;

import java.util.ArrayList;

/**
 * The cloud filesystem object. Provides access to the cloud filesystem.
 */
public class FileSystem {
	/**
	 * Tests if a file exists.
	 * 
	 * @param filePath
	 *            Absolute file path to test.
	 * @return true if found.
	 */
	public boolean existsFile(String filePath) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Tests if a directory exists.
	 * 
	 * @param directory
	 *            Absolute directory to test.
	 * @return true if found.
	 */
	public boolean existsDirectory(String directory) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the list of all entries (files and directories) in a given
	 * directory.
	 * 
	 * @param directory
	 *            The absolute directory in which to look.
	 * @return List of files/directories.
	 */
	public ArrayList<FileSystemEntry> list(String directory) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Creates a directory and all the missing directories along the parent
	 * axis.
	 * 
	 * @param directory
	 *            Absolute directory path.
	 */
	public void createDirectory(String directory) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Deletes a directory and all its content, recursively.
	 * 
	 * @param directory
	 *            Absolute directory to delete.
	 */
	public void deleteDirectory(String directory) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Opens a file for read/write access.
	 * 
	 * @param filePath
	 *            Absolute file path.
	 * @param createIfMissing
	 *            true to create the file when not found
	 * @return
	 */
	public FileSystemFile open(String filePath, boolean createIfMissing) {
		throw new UnsupportedOperationException();
	}

}
