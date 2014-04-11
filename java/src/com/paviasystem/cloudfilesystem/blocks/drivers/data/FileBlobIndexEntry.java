package com.paviasystem.cloudfilesystem.blocks.drivers.data;

import java.util.Date;

public class FileBlobIndexEntry {
	public String fileBlobName;
	public long latestLogBlobLsn;
	public String latestLogBlobRandomId;
	public long length;
	public Date creationTimestamp;
	public Date lastEditTimestamp;
}
