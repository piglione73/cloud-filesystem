package com.paviasystem.cloudfilesystem.blocks.drivers.data;

public class LogBlobKey {
	public long logBlobLsn;
	public String logBlobRandomId;

	public LogBlobKey(long logBlobLsn, String logBlobRandomId) {
		this.logBlobLsn = logBlobLsn;
		this.logBlobRandomId = logBlobRandomId;
	}
}
