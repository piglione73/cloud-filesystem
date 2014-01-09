package com.paviasystem.cloudfilesystem;

import java.io.DataOutputStream;
import java.util.Date;

public interface Log {

	DataOutputStream createWriteEntryStream(String blobName);

	Iterable<CacheLogEntry> list(String blobName, Date threshold);
}
