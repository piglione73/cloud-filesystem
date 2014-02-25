package com.paviasystem.cloudfilesystem;

import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import com.paviasystem.cloudfilesystem.blocks.AbsoluteByteReader;
import com.paviasystem.cloudfilesystem.blocks.AbsoluteByteWriter;
import com.paviasystem.cloudfilesystem.blocks.BlobStore;
import com.paviasystem.cloudfilesystem.blocks.ByteReader;
import com.paviasystem.cloudfilesystem.blocks.ByteReaderUtils;
import com.paviasystem.cloudfilesystem.blocks.ByteWriter;
import com.paviasystem.cloudfilesystem.blocks.Index;
import com.paviasystem.cloudfilesystem.blocks.LazyMirroring;
import com.paviasystem.cloudfilesystem.blocks.LocalCache;
import com.paviasystem.cloudfilesystem.blocks.LockManager;
import com.paviasystem.cloudfilesystem.blocks.data.DirectoryFileIndexEntry;
import com.paviasystem.cloudfilesystem.blocks.data.FileBlobIndexEntry;
import com.paviasystem.cloudfilesystem.blocks.data.LogBlobIndexEntry;
import com.paviasystem.cloudfilesystem.blocks.data.LogBlobPart;
import com.paviasystem.cloudfilesystem.data.FileSystemEntry;

public class CloudFileSystem implements FileSystem {
	final static String LOCAL_CACHE_BLOB = "blob";
	final static String LOCAL_CACHE_OPS = "ops";

	final BlobStore blobStore;
	final Index index;
	final LocalCache localCache;
	final LockManager lockManager;
	final LazyMirroring lazyMirroring;

	public CloudFileSystem(Index index) {
		this.blobStore = null;
		this.index = index;
		this.localCache = null;
		this.lockManager = null;
		this.lazyMirroring = null;
	}

	@Override
	public ArrayList<FileSystemEntry> listDirectory(String absolutePath) {
		// File system entries are in a strongly-consistent file system index
		absolutePath = Path.normalize(absolutePath);
		Iterable<DirectoryFileIndexEntry> entries = index.listChildrenDirectoryFileEntries(absolutePath);
		ArrayList<FileSystemEntry> ret = new ArrayList<FileSystemEntry>();
		for (DirectoryFileIndexEntry entry : entries)
			ret.add(toFileSystemEntry(entry));

		return ret;
	}

	private FileSystemEntry toFileSystemEntry(DirectoryFileIndexEntry entry) {
		return new FileSystemEntry(this, entry.absolutePath, entry.isFile, entry.isSoftLink, entry.targetAbsolutePath);
	}

	@Override
	public FileSystemEntry getEntry(String absolutePath) {
		// File system entries are in a strongly-consistent file system index
		absolutePath = Path.normalize(absolutePath);
		DirectoryFileIndexEntry entry = index.readDirectoryFileEntry(absolutePath);
		if (entry != null)
			return toFileSystemEntry(entry);
		else
			return null;
	}

	@Override
	public void createDirectory(String absolutePath) {
		absolutePath = Path.normalize(absolutePath);

		DirectoryFileIndexEntry entry = new DirectoryFileIndexEntry();
		entry.absolutePath = absolutePath;
		entry.isFile = false;
		entry.isSoftLink = false;

		index.writeDirectoryFileEntry(entry);
	}

	@Override
	public void deleteDirectory(String absolutePath) {
		absolutePath = Path.normalize(absolutePath);
		index.deleteDirectoryFileEntry(absolutePath);
	}

	@Override
	public void rename(String oldAbsolutePath, String newAbsolutePath) {
		oldAbsolutePath = Path.normalize(oldAbsolutePath);
		newAbsolutePath = Path.normalize(newAbsolutePath);
		index.updateDirectoryFileEntry(oldAbsolutePath, newAbsolutePath);
	}

	@Override
	public void deleteFile(String absolutePath) {
		absolutePath = Path.normalize(absolutePath);
		index.deleteDirectoryFileEntry(absolutePath);
	}

	@Override
	public File open(String absolutePath, boolean allowCreate, boolean allowOpen, boolean truncate) throws Exception {
		if (!allowCreate && !allowOpen) {
			// We can do nothing, so we open no file
			return null;
		}

		absolutePath = Path.normalize(absolutePath);

		// See if the file exists
		DirectoryFileIndexEntry fileEntry = index.readDirectoryFileEntry(absolutePath);
		if (fileEntry != null) {
			// The file exists. Can we open it?
			if (fileEntry.isFile && allowOpen) {
				//Get blob entry also
				FileBlobIndexEntry blobEntry = Utils.getFileBlobIndexEntry(index, fileEntry);
				return new MyFile(blobEntry.fileBlobName, truncate);
			}
		} else {
			// The file does not exist. Can we create it?
			if (allowCreate) {
				// Let's create a new entry (file + blob)
				fileEntry = new DirectoryFileIndexEntry();
				fileEntry.absolutePath = absolutePath;
				fileEntry.isFile = true;
				fileEntry.isSoftLink = false;
				fileEntry.blobName = UUID.randomUUID().toString();

				FileBlobIndexEntry blobEntry = new FileBlobIndexEntry();
				blobEntry.fileBlobName = fileEntry.blobName;
				blobEntry.latestLogBlobName = "";
				blobEntry.length = 0;
				blobEntry.creationTimestamp = new Date();
				blobEntry.lastEditTimestamp = blobEntry.creationTimestamp;

				index.createFileBlobEntry(blobEntry);
				index.writeDirectoryFileEntry(fileEntry);

				return new MyFile(blobEntry.fileBlobName, truncate);
			}
		}

		// We could not open/create
		return null;
	}

	private class MyFile implements File {
		String blobName;
		ByteWriter operationsLog;

		public MyFile(String blobName, boolean truncate) throws Exception {
			this.blobName = blobName;
			this.operationsLog = null;

			// Truncate if necessary
			if (truncate)
				setLength(0);
		}

		private void ensureOperationsLog() {
			if (operationsLog == null)
				operationsLog = localCache.openSequentialWriter(blobName, LOCAL_CACHE_OPS);
		}

		private void flushAndCloseOperationsLog() throws Exception {
			operationsLog.close();
			operationsLog = null;
		}

		@Override
		public void close() throws Exception {
			flush();
		}

		@Override
		public void flush() throws Exception {
			// Atomic flush from the local cache to the log
			//First, flush the log into the local cache
			flushAndCloseOperationsLog();

			// Then, copy the log parts into the blob store
			String logBlobName = UUID.randomUUID().toString();
			try (ByteReader reader = localCache.openSequentialReader(blobName, LOCAL_CACHE_OPS)) {
				try (ByteWriter writer = blobStore.write(logBlobName, null)) {
					ByteReaderUtils.copy(reader, writer);
				}
			}

			/*
			 * Now the tricky part: we must register the newly-written log blob
			 * into the index so as it points to the previous log entry in a
			 * consistent manner.
			 * 
			 * We must try the process and repeat it until we manage to update the index consistently.
			 */
			//Register the log blob in the index
			LogBlobIndexEntry lbie = new LogBlobIndexEntry();
			lbie.logBlobName = logBlobName;
			lbie.fileBlobName=blobName;
			lbie.previousLogBlobName=?;
			lbie.creationTimestamp = new Date();
			
			index.writeLogBlobIndexEntry(lbie);
			
			//Then update the file blob entry
			???;
			
			//At the end, delete the operations log
			localCache.delete(blobName, LOCAL_CACHE_OPS);
		}

		@Override
		public int read(byte[] buffer, int bufferOffset, int bytesToRead, long fileOffset) throws Exception {
			// Ensure the localcache blob is up-to-date, then read from the
			// localcache blob
			updateLocalCacheBlob();

			try (AbsoluteByteReader blob = localCache.openAbsoluteReader(blobName, LOCAL_CACHE_BLOB)) {
				return blob.read(buffer, bufferOffset, bytesToRead, fileOffset);
			}
		}

		@Override
		public void setLength(final long newLength) throws Exception {
			// Write into the local cache until a flush is requested
			ensureOperationsLog();
			LogBlobPart part = LogBlobPart.createSetLengthPart(newLength);
			part.writeInto(operationsLog);
		}

		@Override
		public void write(final byte[] buffer, final int bufferOffset, final int bytesToWrite, final long fileOffset) throws Exception {
			//Get the bytes to write
			byte[] bytes = Arrays.copyOfRange(buffer, bufferOffset, bufferOffset + bytesToWrite);

			// Write into the local cache until a flush is requested
			ensureOperationsLog();
			LogBlobPart part = LogBlobPart.createWritePart(fileOffset, bytes);
			part.writeInto(operationsLog);
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
			Date localCacheBlobTs = localCache.getTimestamp(blobName, LOCAL_CACHE_BLOB, null);

			try (AbsoluteByteWriter lcw = localCache.openAbsoluteWriter(blobName, LOCAL_CACHE_BLOB)) {
				boolean first = true;
				for (LogEntry le : log.list(entry.blobName, localCacheBlobTs)) {
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
