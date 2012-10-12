package com.paviasystem.cloudfs;

/**
 * An entry in a filesystem listing. Can be a directory entry or a file entry.
 * 
 * @author Roberto
 * 
 */
public class FileSystemEntry {
	FileSystem fileSystem;
	boolean isFile;
	String absolutePath;

	FileSystemEntry(FileSystem fileSystem, boolean isFile, String absolutePath) {
		this.fileSystem = fileSystem;
		this.isFile = isFile;
		this.absolutePath = absolutePath;
	}

	/**
	 * The filesystem this entry belongs to.
	 */
	public FileSystem getFileSystem() {
		return fileSystem;
	}

	/**
	 * True if file, false if directory.
	 */
	public boolean isFile() {
		return isFile;
	}

	/**
	 * The absolute path of this entry.
	 */

	public String getAbsolutePath() {
		return absolutePath;
	}
}
