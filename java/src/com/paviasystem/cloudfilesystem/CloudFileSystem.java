package com.paviasystem.cloudfilesystem;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class CloudFileSystem implements FileSystem {
	final static String LOCAL_CACHE_OPS = "ops";

	final BlobStore blobStore;
	final Index index;
	final LocalCache localCache;
	final Log log;
	final LockManager lockManager;
	final LazyMirroring lazyMirroring;

	static AtomicLong sequence = new AtomicLong(0);

	public CloudFileSystem(Index index) {
		this.blobStore = null;
		this.index = index;
		this.localCache = null;
		this.log = null;
		this.lockManager = null;
		this.lazyMirroring = null;
	}

	@Override
	public ArrayList<FileSystemEntry> list(String absolutePath) {
		// File system entries are in a strongly-consistent file system index
		absolutePath = Path.normalize(absolutePath);
		ArrayList<IndexEntry> entries = index.listEntries(absolutePath);
		ArrayList<FileSystemEntry> ret = new ArrayList<FileSystemEntry>();
		for (IndexEntry entry : entries)
			ret.add(toFileSystemEntry(entry));

		return ret;
	}

	private FileSystemEntry toFileSystemEntry(IndexEntry indexEntry) {
		return new FileSystemEntry(this, indexEntry.isFile, indexEntry.absolutePath,
				indexEntry.timestamp, indexEntry.length);
	}

	@Override
	public FileSystemEntry getEntry(String absolutePath) {
		// File system entries are in a strongly-consistent file system index
		absolutePath = Path.normalize(absolutePath);
		IndexEntry entry = index.getEntry(absolutePath);
		if (entry != null)
			return toFileSystemEntry(entry);
		else
		return null;
	}

	@Override
	public void createDirectory(String absolutePath) {
		absolutePath = Path.normalize(absolutePath);
		index.createDirectoryEntry(absolutePath);
	}

	@Override
	public void deleteDirectory(String absolutePath) {
		absolutePath = Path.normalize(absolutePath);
		index.deleteEntry(absolutePath);
	}

	@Override
	public File open(String absolutePath, boolean allowCreate,
			boolean allowOpen, boolean truncate) {
		if (!allowCreate && !allowOpen) {
			// We can do nothing, so we open no file
			return null;
		}

		absolutePath = Path.normalize(absolutePath);

		// See if the file exists
		IndexEntry entry = index.getEntry(absolutePath);
		if (entry != null) {
			// The file exists. Can we open it?
			if (allowOpen)
				return new MyFile(entry, truncate);
		} else {
			// The file does not exist. Can we create it?
			if (allowCreate) {
				// Let's create a new entry
				String blobName = UUID.randomUUID().toString();
				entry = index.createFileEntry(absolutePath, blobName);
				return new MyFile(entry, true);
			}
		}

		// We could not open/create
		return null;
	}

	private class MyFile implements File {
		IndexEntry entry;

		public MyFile(IndexEntry entry, boolean truncate) {
			this.entry = entry;

			// Truncate if necessary
			if (truncate)
				setLength(0);
		}

		@Override
		public void flush() {
			// Atomic flush from the local cache to the log
			// Combine local cache entries into a single log record, then delete
			// the
			// local cache entries
			ArrayList<String> cacheFileNames = new ArrayList<String>();

			// While writing into the log, we keep track of the resulting blob
			// length
			long newLen = entry.length;

			// Create a stream for writing into the log
			try (DataOutputStream logEntryStream = log.createWriteEntryStream()) {
				// Iterate over the cache entries
				Iterator<CacheLogEntry> ceIt = localCache.list(entry.blobName,
						LOCAL_CACHE_OPS);
				while (ceIt.hasNext()) {
					CacheLogEntry ce = ceIt.next();

					// Write each cache entry into the single log entry
					ce.writeInto(logEntryStream);

					// Keep its name for later removal
					cacheFileNames.add(ce.name);

					// Keep track of the resulting blob len
					if (ce.type == CacheLogEntry.SET_LENGTH) {
						// A SET_LENGTH entry specifies the new len. No
						// computation required
						newLen = ce.length;
					} else if (ce.type == CacheLogEntry.WRITE_BYTES) {
						// A WRITE_BYTES entry specifies an interval of bytes
						// written. The
						// last byte written determines the newLen
						long byteAfterlastByte = ce.start + ce.length;

						// Enlarge the newLen so as to be at least
						// "byteAfterlastByte"
						newLen = Math.max(newLen, byteAfterlastByte);
					}
				}
			}

			// At the end, delete the cache entries...
			for (String cacheFileName : cacheFileNames)
				localCache.remove(entry.blobName, LOCAL_CACHE_OPS,
						cacheFileName);

			// ... and update the IndexEntry with the new len and the new
			// timestamp
			index.updateEntry(entry.absolutePath, newLen, new Date());
		}

		@Override
		public int read(byte[] buffer, int bufferOffset, int bytesToRead, long fileOffset) {
			//Combine the blob, the log and the local cache in order to have the current situation
			//At the end, asynchronously update the blob and clean the log, if convenient
			TODO;
		}

		@Override
		public void setLength(final long newLength) {
			// Write into the local cache until a flush is requested
			long sequence1 = new Date().getTime();
			long sequence2 = sequence.incrementAndGet();
			final String cacheFileName = getCacheFileName(sequence1, sequence2);
			byte[] cacheFileBytes = StreamUtils
					.getBytes(new StreamUtils.Writer() {
						@Override
						public void write(DataOutput out) throws IOException {
							CacheLogEntry ce = CacheLogEntry.createSetLength(
									cacheFileName, newLength);
							ce.writeInto(out);
						}
					});

			localCache.write(entry.blobName, LOCAL_CACHE_OPS, cacheFileName,
					cacheFileBytes);
		}

		@Override
		public void write(final byte[] buffer, final int bufferOffset,
				final int bytesToWrite, final long fileOffset) {
			// Write into the local cache until a flush is requested
			long sequence1 = System.currentTimeMillis();
			long sequence2 = sequence.incrementAndGet();
			final String cacheFileName = getCacheFileName(sequence1, sequence2);
			byte[] cacheFileBytes = StreamUtils
					.getBytes(new StreamUtils.Writer() {
						@Override
						public void write(DataOutput out) throws IOException {
							CacheLogEntry ce = CacheLogEntry.createWriteBytes(
									cacheFileName, buffer, bufferOffset,
									bytesToWrite, fileOffset);
							ce.writeInto(out);
						}
					});

			localCache.write(entry.blobName, LOCAL_CACHE_OPS, cacheFileName,
					cacheFileBytes);
		}

		private String getCacheFileName(long sequence1, long sequence2) {
			return pad(Long.toString(sequence1), 20) + "-"
					+ pad(Long.toString(sequence1), 20);
		}

		private String pad(String s, int len) {
			int slen = s.length();
			StringBuilder sb = new StringBuilder(len);
			for (int i = 0; i < slen; i++)
				sb.append('0');

			sb.append(s);
			return sb.toString();
		}
	}
}
