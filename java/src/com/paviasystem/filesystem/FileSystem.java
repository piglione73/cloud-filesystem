package com.paviasystem.filesystem;

import java.util.ArrayList;

import com.paviasystem.cloudfilesystemold.data.FileSystemEntry;

public interface FileSystem {

	/**
	 * List files and directories
	 * 
	 * @param absolutePath
	 *            Absolute path of the directory whose children are desired
	 * @return Returns a list of file system entries
	 */
	ArrayList<FileSystemEntry> listDirectory(String absolutePath) throws Exception;

	FileSystemEntry getEntry(String absolutePath) throws Exception;

	void createDirectory(String absolutePath) throws Exception;

	void deleteDirectory(String absolutePath) throws Exception;

	File open(String absolutePath, boolean allowCreate, boolean allowOpen, boolean truncate) throws Exception;

	void rename(String oldAbsolutePath, String newAbsolutePath) throws Exception;

	void deleteFile(String absolutePath) throws Exception;

}
