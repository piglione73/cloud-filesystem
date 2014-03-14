package com.paviasystem.cloudfilesystem.blocks.data;

import java.util.Date;

public class LogBlobIndexEntry {
	public long logBlobLsn;
	public String logBlobRandomId;
	public String fileBlobName;
	public String previousLogBlobRandomId;
	public Date creationTimestamp;
}
