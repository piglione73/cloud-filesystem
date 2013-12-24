package com.paviasystem.cloudfilesystem;

import java.util.Date;

/**
 * An entry in a filesystem listing. Can be a directory entry or a file entry.
 * 
 * @author Roberto
 * 
 */
public class FileSystemEntry {
	final FileSystem fileSystem;
	final boolean isFile;
	final String absolutePath;
	final Date timestamp;

	FileSystemEntry(FileSystem fileSystem, boolean isFile, String absolutePath, Date timestamp) {
		this.fileSystem = fileSystem;
		this.isFile = isFile;
		this.absolutePath = Path.normalize(absolutePath);
		this.timestamp = timestamp;
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

	public Date getTimestamp() {
		return timestamp;
	}
}
