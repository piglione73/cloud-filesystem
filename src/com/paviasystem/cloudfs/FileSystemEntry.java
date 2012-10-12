package com.paviasystem.cloudfs;

public class FileSystemEntry {
	FileSystem fileSystem;
	boolean isFile;
	String absolutePath;

	public FileSystem getFileSystem() {
		return fileSystem;
	}

	public boolean isFile() {
		return isFile;
	}

	public String getAbsolutePath() {
		return absolutePath;
	}

}
