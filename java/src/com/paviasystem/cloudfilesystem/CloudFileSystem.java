package com.paviasystem.cloudfilesystem;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class CloudFileSystem implements FileSystem {
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
			ret.add(new FileSystemEntry(this, entry.isFile, entry.absolutePath, entry.timestamp));

		return ret;
	}

	@Override
	public FileSystemEntry getEntry(String absolutePath) {
		// File system entries are in a strongly-consistent file system index
		absolutePath = Path.normalize(absolutePath);
		IndexEntry entry = index.getEntry(absolutePath);
		if (entry != null)
			return new FileSystemEntry(this, entry.isFile, entry.absolutePath, entry.timestamp);

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
	public File open(String absolutePath, boolean allowCreate, boolean allowOpen, boolean truncate) {
		if (!allowCreate && !allowOpen) {
			//We can do nothing, so we open no file
			return null;
		}

		absolutePath = Path.normalize(absolutePath);

		//See if the file exists
		IndexEntry entry = index.getEntry(absolutePath);
		if (entry != null) {
			//The file exists. Can we open it?
			if (allowOpen)
				return new MyFile(entry, truncate);
		} else {
			//The file does not exist. Can we create it?
			if (allowCreate) {
				//Let's create a new entry
				String blobName = UUID.randomUUID().toString();
				entry = index.createFileEntry(absolutePath, blobName);
				return new MyFile(entry, true);
			}
		}

		//We could not open/create
		return null;
	}

	private class MyFile implements File {
		IndexEntry entry;

		public MyFile(IndexEntry entry, boolean truncate) {
			this.entry = entry;

			//Truncate if necessary
			if (truncate)
				setLength(0);
		}

		@Override
		public void flush() {
			//Atomic flush from the local cache to the log
			//Combine local cache entries into a single blob but write multiple log records atomically (???)
			TODO;
		}

		@Override
		public int read(byte[] buffer, int bufferOffset, int bytesToRead, long fileOffset) {
			//Combine the blob, the log and the local cache in order to have the current situation
			//At the end, asynchronously update the blob and clean the log, if convenient
			TODO;
		}

		@Override
		public void setLength(long newLength) {
			//Write into the local cache until a flush is requested
			long sequence1 = new Date().getTime();
			long sequence2 = sequence.incrementAndGet();
			String cacheFileName = getCacheFileName(sequence1, sequence2);
			byte[] cacheFileBytes = CacheFile.SetLengthEntry.getBytes(newLength);
			localCache.writeFile(entry.blobName, cacheFileName, cacheFileBytes);
		}

		@Override
		public void write(byte[] buffer, int bufferOffset, int bytesToWrite, long fileOffset) {
			//Write into the local cache until a flush is requested
			long sequence1 = new Date().getTime();
			long sequence2 = sequence.incrementAndGet();
			String cacheFileName = getCacheFileName(sequence1, sequence2);
			byte[] cacheFileBytes = CacheFile.WriteBytesEntry.getBytes(buffer, bufferOffset, bytesToWrite, fileOffset);
			localCache.writeFile(entry.blobName, cacheFileName, cacheFileBytes);
		}

		private String getCacheFileName(long sequence1, long sequence2) {
			return pad(Long.toString(sequence1), 20) + "-" + pad(Long.toString(sequence1), 20);
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

	private static class CacheFile {
		public static class SetLengthEntry {
			public static byte[] getBytes(final long newLength) {
				return StreamUtils.getBytes(new StreamUtils.Writer() {
					@Override
					public void write(DataOutputStream dos) {
						try {
							dos.writeChar('L');
							dos.writeLong(newLength);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
			}
		}

		public static class WriteBytesEntry {
			public static byte[] getBytes(final byte[] buffer, final int bufferOffset, final int bytesToWrite, final long fileOffset) {
				return StreamUtils.getBytes(new StreamUtils.Writer() {
					@Override
					public void write(DataOutputStream dos) {
						try {
							dos.writeChar('W');
							dos.writeLong(fileOffset);
							dos.writeInt(bytesToWrite);
							dos.write(buffer, bufferOffset, bytesToWrite);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
			}
		}
	}
}
