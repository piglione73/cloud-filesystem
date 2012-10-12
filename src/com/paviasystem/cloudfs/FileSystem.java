package com.paviasystem.cloudfs;

import java.util.ArrayList;

import com.paviasystem.cloudfs.log.FileSystemLog;
import com.paviasystem.cloudfs.storage.Storage;

/**
 * The cloud filesystem object. Provides access to the cloud filesystem.
 */
public class FileSystem {
	private Storage storage;
	private FileSystemLog[] logPipeline;

	/**
	 * Tests if a file exists.
	 * 
	 * @param filePath
	 *            Absolute file path to test.
	 * @return true if found.
	 */
	public boolean existsFile(String filePath) {
		return Engine.existsFile(filePath, storage, logPipeline);
	}

	/**
	 * Tests if a directory exists.
	 * 
	 * @param directory
	 *            Absolute directory to test.
	 * @return true if found.
	 */
	public boolean existsDirectory(String directory) {
		return Engine.existsDirectory(directory, storage, logPipeline);
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
		return Engine.list(directory, storage, logPipeline);
	}

	/**
	 * Creates a directory and all the missing directories along the parent
	 * axis.
	 * 
	 * @param directory
	 *            Absolute directory path.
	 */
	public void createDirectory(String directory) {
		Engine.createDirectory(directory, storage, logPipeline);
	}

	/**
	 * Deletes a directory and all its content, recursively.
	 * 
	 * @param directory
	 *            Absolute directory to delete.
	 */
	public void deleteDirectory(String directory) {
		Engine.deleteDirectory(directory, storage, logPipeline);
	}

	/**
	 * Opens a file for read/write access.
	 * 
	 * @param filePath
	 *            Absolute file path.
	 * @param createIfMissing
	 *            true to create the file when not found.
	 * @return Returns the file or null if the file is not found.
	 */
	public FileSystemFile open(String filePath, boolean createIfMissing) {
		return Engine.openFile(filePath, createIfMissing, storage, logPipeline);
	}

}
