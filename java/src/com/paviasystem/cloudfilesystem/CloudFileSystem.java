package com.paviasystem.cloudfilesystem;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import com.paviasystem.cloudfilesystem.blocks.BlobStore;

public class CloudFileSystem implements FileSystem {
	final static String LOCAL_CACHE_BLOB = "blob";
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
	public ArrayList<FileSystemEntry> listDirectory(String absolutePath) {
		// File system entries are in a strongly-consistent file system index
		absolutePath = Path.normalize(absolutePath);
		ArrayList<IndexEntry> entries = index.listEntries(absolutePath);
		ArrayList<FileSystemEntry> ret = new ArrayList<FileSystemEntry>();
		for (IndexEntry entry : entries)
			ret.add(toFileSystemEntry(entry));

		return ret;
	}

	private FileSystemEntry toFileSystemEntry(IndexEntry indexEntry) {
		return new FileSystemEntry(this, indexEntry.isFile,
				indexEntry.absolutePath, indexEntry.timestamp,
				indexEntry.length);
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
	public void rename(String oldAbsolutePath, String newAbsolutePath) {
		oldAbsolutePath = Path.normalize(oldAbsolutePath);
		newAbsolutePath = Path.normalize(newAbsolutePath);
		index.updateEntry(oldAbsolutePath, newAbsolutePath);
	}

	@Override
	public void deleteFile(String absolutePath) {
		absolutePath = Path.normalize(absolutePath);
		index.deleteEntry(absolutePath);
	}

	@Override
	public File open(String absolutePath, boolean allowCreate,
			boolean allowOpen, boolean truncate) throws Exception {
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

		public MyFile(IndexEntry entry, boolean truncate) throws Exception {
			this.entry = entry;

			// Truncate if necessary
			if (truncate)
				setLength(0);
		}

		@Override
		public void flush() throws IOException {
			// Atomic flush from the local cache to the log
			// Combine local cache entries into a single log record, then delete
			// the
			// local cache entries
			ArrayList<String> cacheFileNames = new ArrayList<String>();

			// While writing into the log, we keep track of the resulting blob
			// length
			long newLen = entry.length;

			// Create a stream for writing a new entry into the log
			try (DataOutputStream logEntryStream = log
					.createWriteEntryStream(entry.blobName)) {
				// Iterate over the cache entries
				for (CacheLogEntry ce : localCache.list(entry.blobName,
						LOCAL_CACHE_OPS)) {
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
		public int read(byte[] buffer, int bufferOffset, int bytesToRead,
				long fileOffset) throws Exception {
			// Ensure the localcache blob is up-to-date, then read from the
			// localcache blob
			updateLocalCacheBlob();

			try (LocalCacheReader lcr = localCache.read(entry.blobName,
					LOCAL_CACHE_BLOB, null)) {
				return lcr.read(buffer, bufferOffset, bytesToRead, fileOffset);
			}
		}

		@Override
		public void setLength(final long newLength) throws Exception {
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

			try (LocalCacheWriter lcw = localCache.write(entry.blobName,
					LOCAL_CACHE_OPS, cacheFileName)) {
				lcw.write(cacheFileBytes, 0, cacheFileBytes.length, 0);
			}
		}

		@Override
		public void write(final byte[] buffer, final int bufferOffset,
				final int bytesToWrite, final long fileOffset) throws Exception {
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

			try (LocalCacheWriter lcw = localCache.write(entry.blobName,
					LOCAL_CACHE_OPS, cacheFileName)) {
				lcw.write(cacheFileBytes, 0, cacheFileBytes.length, 0);
			}
		}

		private String getCacheFileName(long sequence1, long sequence2) {
			return pad(Long.toString(sequence1), 20) + "-"
					+ pad(Long.toString(sequence2), 20);
		}

		private String pad(String s, int len) {
			int slen = s.length();
			StringBuilder sb = new StringBuilder(len);
			for (int i = 0; i < slen; i++)
				sb.append('0');

			sb.append(s);
			return sb.toString();
		}

		private void updateLocalCacheBlob() {
			/*
			 * Combine the local cache blob and the log records, if possible.
			 * Otherwise, get the blob from the blob store and combine it with
			 * the log records. At the end, the local cache blob is up-to-date
			 * with the current file contents. Log records must be taken
			 * directly from the log, because the local cache log records only
			 * reflect what we wrote into the file, not what others wrote.
			 */
			Date localCacheBlobTs = localCache.getTimestamp(entry.blobName,
					LOCAL_CACHE_BLOB, null);

			try (LocalCacheWriter lcw = localCache.write(entry.blobName,
					LOCAL_CACHE_BLOB, null)) {
				boolean first = true;
				for (CacheLogEntry le : log.list(entry.blobName,
						localCacheBlobTs)) {
					if (first) {
						first = false;

						// If the timestamp of the first log entry returned is
						// before localCacheBlobTs, then we are sure that our
						// local
						// cache blob contains enough information. Otherwise,
						// we'll
						// have to hit the blob store if we want to read
						// meaningful
						// data. In fact, if the timestamp of the first log
						// entry
						// returned is after localCacheBlobTs, then we cannot be
						// sure that we are not losing data (maybe, the log has
						// been
						// cleaned and our local cache blob has fallen behind
						// the
						// log clean horizon)
						if (localCacheBlobTs.after(le.timestamp)) {
							// We can use the local cache blob as a starting
							// point
							// We just go on
						} else {
							// We cannot use the local cache blob as a starting
							// point, we must use the blob store
							updateLocalCacheBlobFromBlobStore(lcw);
							return;
						}
					}

					// Let's apply the log entry to the local cache blob
					applyLogEntryToLocalCacheBlob(le, lcw);
				}
			}
		}

	}
}
