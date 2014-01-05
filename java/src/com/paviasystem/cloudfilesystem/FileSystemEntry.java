package com.paviasystem.cloudfilesystem;

import java.util.Date;

/**
 * An entry in a filesystem listing. Can be a directory entry or a file entry.
 * 
 * @author Roberto
 * 
 */
public class FileSystemEntry {
	public final FileSystem fileSystem;
	public final boolean isFile;
	public final String absolutePath;
	public final Date timestamp;
	public final long length;

	FileSystemEntry(FileSystem fileSystem, boolean isFile, String absolutePath,
			Date timestamp, long length) {
		this.fileSystem = fileSystem;
		this.isFile = isFile;
		this.absolutePath = Path.normalize(absolutePath);
		this.timestamp = timestamp;
		this.length = length;
	}
}
