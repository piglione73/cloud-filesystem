package com.paviasystem.cloudfilesystem.blocks.data;

import java.util.Date;

public class IndexEntry {
	public final boolean isFile;
	public final String absolutePath;
	public final Date timestamp;
	public final String blobName;
	public final long length;

	public IndexEntry(String absolutePath, boolean isFile, Date timestamp,
			long length, String blobName) {
		this.absolutePath = absolutePath;
		this.isFile = isFile;
		this.timestamp = timestamp;
		this.length = length;
		this.blobName = blobName;
	}
}
