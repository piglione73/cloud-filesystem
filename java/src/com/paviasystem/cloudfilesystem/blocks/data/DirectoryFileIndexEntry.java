package com.paviasystem.cloudfilesystem.blocks.data;

public class DirectoryFileIndexEntry {
	public String absolutePath;
	public boolean isFile;
	public boolean isSoftLink;
	public String blobName;
	public String targetAbsolutePath;
}
