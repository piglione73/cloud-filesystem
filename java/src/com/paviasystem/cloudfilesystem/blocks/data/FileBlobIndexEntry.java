package com.paviasystem.cloudfilesystem.blocks.data;

import java.util.Date;

public class FileBlobIndexEntry {
	public String fileBlobName;
	public String latestLogBlobName;
	public long length;
	public Date creationTimestamp;
	public Date lastEditTimestamp;
}
