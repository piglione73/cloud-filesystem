package com.paviasystem.cloudfilesystem;

import java.util.Date;

public class IndexEntry {
	public final boolean isFile;
	public final String absolutePath;
	public final Date timestamp;
	public final String blobName;

	public IndexEntry(String absolutePath, boolean isFile, Date timestamp, String blobName) {
		this.absolutePath = absolutePath;
		this.isFile = isFile;
		this.blobName = blobName;
		this.timestamp = timestamp;
	}
}
