package com.paviasystem.cloudfilesystem.blocks.drivers.data;

public class DirectoryFileIndexEntry {
	public String absolutePath;
	public boolean isFile;
	public boolean isSoftLink;
	public String fileBlobName;
	public String targetAbsolutePath;
}
