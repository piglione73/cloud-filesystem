package com.paviasystem.cloudfilesystem;

import java.util.Date;

import com.paviasystem.cloudfilesystem.FileSystem;
import com.paviasystem.cloudfilesystem.Path;

/**
 * An entry in a filesystem listing. Can be a directory entry or a file entry.
 * 
 * @author Roberto
 * 
 */
public class FileSystemEntry {
	public final FileSystem fileSystem;
	public final String absolutePath;
	public final boolean isFile;
	public final boolean isSoftLink;
	public final String targetAbsolutePath;
	public final Date creationTimestamp, lastEditTimestamp;
	public final long length;

	public FileSystemEntry(FileSystem fileSystem, String absolutePath, boolean isFile, boolean isSoftLink, String targetAbsolutePath, Date creationTimestamp, Date lastEditTimestamp, long length) {
		this.fileSystem = fileSystem;
		this.absolutePath = Path.normalize(absolutePath);
		this.isFile = isFile;
		this.isSoftLink = isSoftLink;
		this.targetAbsolutePath = Path.normalize(targetAbsolutePath);
		this.creationTimestamp = creationTimestamp;
		this.lastEditTimestamp = lastEditTimestamp;
		this.length = length;
	}
}
