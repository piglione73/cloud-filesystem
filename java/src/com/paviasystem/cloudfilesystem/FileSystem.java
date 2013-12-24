package com.paviasystem.cloudfilesystem;

import java.util.ArrayList;

public interface FileSystem {

	/**
	 * List files and directories
	 * 
	 * @param absolutePath
	 *            Absolute path of the directory whose children are desired
	 * @return Returns a list of file system entries
	 */
	ArrayList<FileSystemEntry> list(String absolutePath);

	FileSystemEntry getEntry(String absolutePath);

	void createDirectory(String absolutePath);

	void deleteDirectory(String absolutePath);

	File open(String absolutePath, boolean allowCreate, boolean allowOpen, boolean truncate);

}
