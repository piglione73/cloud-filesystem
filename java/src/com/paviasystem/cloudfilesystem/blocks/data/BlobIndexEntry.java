package com.paviasystem.cloudfilesystem.blocks.data;

import java.util.Date;

public class BlobIndexEntry {
	public String blobName;
	public long LSN;
	public long length;
	public Date creationTimestamp;
	public Date lastEditTimestamp;
}
