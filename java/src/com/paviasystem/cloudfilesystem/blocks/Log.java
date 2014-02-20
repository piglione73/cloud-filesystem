package com.paviasystem.cloudfilesystem.blocks;

import java.io.DataOutputStream;
import java.util.Date;

import com.paviasystem.cloudfilesystem.blocks.data.CacheLogEntry;

public interface Log {

	DataOutputStream createWriteEntryStream(String blobName);

	Iterable<CacheLogEntry> list(String blobName, Date threshold);
}
