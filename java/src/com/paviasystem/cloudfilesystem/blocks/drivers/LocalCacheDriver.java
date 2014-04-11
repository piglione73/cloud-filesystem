package com.paviasystem.cloudfilesystem.blocks.drivers;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import com.paviasystem.cloudfilesystem.blocks.AbsoluteByteReader;
import com.paviasystem.cloudfilesystem.blocks.AbsoluteByteWriter;
import com.paviasystem.cloudfilesystem.blocks.ByteReader;
import com.paviasystem.cloudfilesystem.blocks.ByteReaderUtils;
import com.paviasystem.cloudfilesystem.blocks.ByteWriter;
import com.paviasystem.cloudfilesystem.blocks.LocalCache;
import com.paviasystem.cloudfilesystem.blocks.drivers.data.LogBlobKey;

public class LocalCacheDriver {
	final static String BLOB = "blob";
	final static String OPS = "ops";
	final static String LATEST_LOG_BLOB_KEY = "latest";

	private LocalCache localCache;

	public LocalCacheDriver(LocalCache localCache) {
		this.localCache = localCache;
	}

	public ByteWriter openLogWriter(String fileBlobName) {
		return localCache.openSequentialWriter(fileBlobName, OPS);
	}

	public ByteReader openLogReader(String fileBlobName) {
		return localCache.openSequentialReader(fileBlobName, OPS);
	}

	public void deleteLog(String fileBlobName) {
		localCache.delete(fileBlobName, OPS);
	}

	public AbsoluteByteReader openBlobReader(String fileBlobName) {
		return localCache.openAbsoluteReader(fileBlobName, BLOB);
	}

	public AbsoluteByteWriter openBlobWriter(String fileBlobName) {
		return localCache.openAbsoluteWriter(fileBlobName, BLOB);
	}

	public LogBlobKey getLatestLogBlobKey(String fileBlobName) throws Exception {
		try (ByteReader reader = localCache.openSequentialReader(fileBlobName, LATEST_LOG_BLOB_KEY)) {
			byte[] buf = new byte[13];
			ByteBuffer bytes = ByteBuffer.wrap(buf);

			// Read LSN and randomid len
			if (!ByteReaderUtils.readExact(reader, buf, 0, 12))
				return new LogBlobKey(-1, null);

			long lsn = bytes.getLong();
			int len = bytes.getInt();
			if (len <= 0)
				return new LogBlobKey(lsn, null);

			//Now read "len" bytes
			byte[] randomIdBytes = new byte[len];
			if (!ByteReaderUtils.readExact(reader, randomIdBytes, 0, randomIdBytes.length))
				return new LogBlobKey(-1, null);

			String randomId = new String(randomIdBytes, StandardCharsets.UTF_8);

			//Wrap as LogBlobKey
			return new LogBlobKey(lsn, randomId);
		}
	}

	public void setLatestLogBlobKey(String fileBlobName, LogBlobKey key) throws Exception {
		try (ByteWriter writer = localCache.openSequentialWriter(fileBlobName, LATEST_LOG_BLOB_KEY)) {
			ByteBuffer bytes = ByteBuffer.allocate(256);

			//LSN
			bytes.putLong(key.logBlobLsn);

			//Len of logBlobRandomId and logBlobRandomId bytes
			if (key.logBlobRandomId == null)
				bytes.putInt(-1);
			else {
				byte[] buf = key.logBlobRandomId.getBytes(StandardCharsets.UTF_8);
				bytes.putInt(buf.length);
				bytes.put(buf);
			}

			//Write into writer
			byte[] bytesToWrite = bytes.array();
			writer.write(bytesToWrite, 0, bytes.limit());
		}
	}

}
