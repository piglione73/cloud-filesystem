package com.paviasystem.cloudfilesystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
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
import com.paviasystem.cloudfilesystem.blocks.drivers.BlobStoreDriver;
import com.paviasystem.cloudfilesystem.blocks.drivers.BlobStoreDriver.FileMetaData;
import com.paviasystem.cloudfilesystem.blocks.drivers.data.DirectoryFileIndexEntry;
import com.paviasystem.cloudfilesystem.blocks.drivers.data.FileBlobIndexEntry;
import com.paviasystem.cloudfilesystem.blocks.drivers.data.LogBlobIndexEntry;
import com.paviasystem.cloudfilesystem.blocks.drivers.data.LogBlobKey;
import com.paviasystem.cloudfilesystem.blocks.drivers.data.LogBlobPart;
import com.paviasystem.cloudfilesystem.blocks.drivers.IndexDriver;
import com.paviasystem.cloudfilesystem.blocks.drivers.LocalCacheDriver;
import com.paviasystem.cloudfilesystem.data.FileSystemEntry;

public class CloudFileSystem implements FileSystem {
	final BlobStoreDriver blobStore;
	final IndexDriver index;
	final LocalCacheDriver localCache;
	final LockManager lockManager;
	final LazyMirroring lazyMirroring;

	public CloudFileSystem(BlobStore blobStore, Index index, LocalCache localCache) {
		this.blobStore = new BlobStoreDriver(blobStore);
		this.index = new IndexDriver(index);
		this.localCache = new LocalCacheDriver(localCache);
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
				// Get blob entry also
				FileBlobIndexEntry blobEntry = index.getFileBlobEntry(fileEntry);
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
				fileEntry.fileBlobName = UUID.randomUUID().toString();

				FileBlobIndexEntry blobEntry = new FileBlobIndexEntry();
				blobEntry.fileBlobName = fileEntry.fileBlobName;
				blobEntry.latestLogBlobLsn = -1;
				blobEntry.latestLogBlobRandomId = null;
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
		String fileBlobName;
		ByteWriter operationsLog;

		public MyFile(String fileBlobName, boolean truncate) throws Exception {
			this.fileBlobName = fileBlobName;
			this.operationsLog = null;

			// Truncate if necessary
			if (truncate)
				setLength(0);
		}

		private void ensureOperationsLog() {
			if (operationsLog == null)
				operationsLog = localCache.openLogWriter(fileBlobName);
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
			// First, flush the log into the local cache
			flushAndCloseOperationsLog();

			// Then, copy the log parts into the blob store. Meanwhile, read the
			// log parts and determine the offset of the lowest unaffected byte
			// (so we can later consistently determine the new file length)
			String logBlobRandomId = UUID.randomUUID().toString();
			long lowestUnaffectedOffset = 0;
			try (ByteReader reader = localCache.openLogReader(fileBlobName)) {
				try (ByteWriter writer = blobStore.writeLogBlob(logBlobRandomId)) {
					for (LogBlobPart part = LogBlobPart.readFrom(reader); part != null; part = LogBlobPart.readFrom(reader)) {
						// Measure...
						if (part.type == LogBlobPart.SET_LENGTH)
							lowestUnaffectedOffset = Math.max(lowestUnaffectedOffset, part.newLength);
						else if (part.type == LogBlobPart.WRITE)
							lowestUnaffectedOffset = Math.max(lowestUnaffectedOffset, part.destOffset + part.bytes.length);

						// ... and write
						part.writeInto(writer);
					}
				}
			}

			/*
			 * Now the tricky part: we must register the newly-written log blob
			 * into the index so as it points to the previous log entry in a
			 * consistent manner.
			 * 
			 * We must try the process and repeat it until we manage to update
			 * the index consistently.
			 */
			while (true) {
				// Let's see what is the current log entry, so we attach our new
				// log blob index entry to it
				FileBlobIndexEntry blobEntry = index.readFileBlobEntry(fileBlobName);

				// Determine the new length by combining the current length and
				// the measurements made on the log parts
				long newLength = Math.max(lowestUnaffectedOffset, blobEntry.length);

				// Register the log blob in the index and attach it to the
				// latest log blob name
				LogBlobIndexEntry lbie = new LogBlobIndexEntry();
				lbie.logBlobLsn = blobEntry.latestLogBlobLsn + 1;
				lbie.logBlobRandomId = logBlobRandomId;
				lbie.fileBlobName = fileBlobName;
				lbie.previousLogBlobRandomId = blobEntry.latestLogBlobRandomId;
				lbie.creationTimestamp = new Date();

				index.writeLogBlobEntry(lbie);

				// Then update the file blob entry, but only if it hasn't
				// changed
				if (index.updateFileBlobEntry(fileBlobName, blobEntry.latestLogBlobLsn, blobEntry.latestLogBlobRandomId, lbie.logBlobLsn, lbie.logBlobRandomId, newLength, new Date())) {
					// Ok, we consistently updated the file blob entry, so we
					// are done
					break;
				}
			}

			// At the end, delete the operations log
			localCache.deleteLog(fileBlobName);
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
			// Get the bytes to write
			byte[] bytes = Arrays.copyOfRange(buffer, bufferOffset, bufferOffset + bytesToWrite);

			// Write into the local cache until a flush is requested
			ensureOperationsLog();
			LogBlobPart part = LogBlobPart.createWritePart(fileOffset, bytes);
			part.writeInto(operationsLog);
		}

		@Override
		public int read(byte[] buffer, int bufferOffset, int bytesToRead, long fileOffset) throws Exception {
			// Ensure the localcache blob is up-to-date, then read from the
			// localcache blob
			updateLocalCacheBlob();

			try (AbsoluteByteReader blob = localCache.openBlobReader(fileBlobName)) {
				return blob.read(buffer, bufferOffset, bytesToRead, fileOffset);
			}
		}

		private void updateLocalCacheBlob() throws Exception {
			try (AbsoluteByteWriter writer = localCache.openBlobWriter(fileBlobName)) {
				/*
				 * The purpose of this method is to update the local cache blob
				 * by incorporating all the log records written after the local
				 * cache blob was last updated.
				 * 
				 * This operation is not always possible. In case it is not
				 * possible, we resort to the blob store directly.
				 */
				//What is the latest log record incorporated into the local cache blob?
				LogBlobKey localCacheLatestLogBlobKey = localCache.getLatestLogBlobKey(fileBlobName);
				long localCacheLatestLogBlobLsn = localCacheLatestLogBlobKey.logBlobLsn;
				String localCacheLatestLogBlobRandomId = localCacheLatestLogBlobKey.logBlobRandomId;

				//What is the latest log record written into the file?
				FileBlobIndexEntry fileBlobEntry = index.readFileBlobEntry(fileBlobName);
				long latestLogBlobLsn = fileBlobEntry.latestLogBlobLsn;
				String latestLogBlobRandomId = fileBlobEntry.latestLogBlobRandomId;

				//Let's decide what to do next
				if (localCacheLatestLogBlobLsn >= 0) {
					/*
					 * We have a local cache blob and it incorporates all the
					 * log records from the beginning up to and including
					 * localCacheLatestLogBlobLsn. Then, we must incorporate all
					 * the log records from localCacheLatestLogBlobLsn+1 up to
					 * latestLogBlobLsn.
					 * 
					 * However, this is not always possible. A log-cleaning
					 * thread could have deleted the
					 * "localCacheLatestLogBlobLsn+1" log record. In that case
					 * we would have to use the blob store.
					 * 
					 * We optimistically try to read the log records and then,
					 * if at least one is missing, we use the blob store.
					 */
					LinkedList<LogBlobIndexEntry> logBlobEntries = index.readLogBlobEntries(fileBlobName, localCacheLatestLogBlobLsn, localCacheLatestLogBlobRandomId, latestLogBlobLsn, latestLogBlobRandomId);

					/*
					 * The first might or might not be
					 * localCacheLatestLogBlobLsn. Discard it! We want the first
					 * item to be localCacheLatestLogBlobLsn+1
					 */
					while (!logBlobEntries.isEmpty() && logBlobEntries.peekFirst().logBlobLsn <= localCacheLatestLogBlobLsn)
						logBlobEntries.removeFirst();

					/*
					 * Now, if the first item is localCacheLatestLogBlobLsn+1,
					 * we can simply start from the local cache
					 */
					if (!logBlobEntries.isEmpty() && logBlobEntries.peekFirst().logBlobLsn == localCacheLatestLogBlobLsn + 1) {
						//Start from the local cache
						updateLocalCacheBlob_StartFromLocalCacheBlob(writer, logBlobEntries);
					} else {
						//We don't have all the necessary log records, so let's start from the blob store
						updateLocalCacheBlob_StartFromBlobStore(writer, logBlobEntries, latestLogBlobLsn, latestLogBlobRandomId);
					}
				} else {
					/*
					 * We don't have a local cache blob, so we must start from
					 * the blob store
					 */
					updateLocalCacheBlob_StartFromBlobStore(writer, null, latestLogBlobLsn, latestLogBlobRandomId);
				}
			}
		}

		private void updateLocalCacheBlob_StartFromLocalCacheBlob(AbsoluteByteWriter writer, LinkedList<LogBlobIndexEntry> logBlobEntries) throws Exception {
			// Apply all the log entries to the local cache
			for (LogBlobIndexEntry logBlobEntry : logBlobEntries) {
				// Apply each part of this log entry
				try (ByteReader reader = blobStore.readLogBlob(logBlobEntry.logBlobRandomId)) {
					for (LogBlobPart part = LogBlobPart.readFrom(reader); part != null; part = LogBlobPart.readFrom(reader))
						part.applyTo(writer);
				}
			}

			//Finally, update the latest local cache log blob key
			LogBlobIndexEntry latestEntry = logBlobEntries.peekLast();
			localCache.setLatestLogBlobKey(fileBlobName, new LogBlobKey(latestEntry.logBlobLsn, latestEntry.logBlobRandomId));
		}

		private void updateLocalCacheBlob_StartFromBlobStore(AbsoluteByteWriter writer, LinkedList<LogBlobIndexEntry> logBlobEntries, long latestLogBlobLsn, String latestLogBlobRandomId) throws Exception {
			//Read the file blob
			FileMetaData meta = new FileMetaData();
			try (ByteReader reader = blobStore.readFileBlob(fileBlobName, meta)) {
				//Copy to local cache
				ByteReaderUtils.copy(reader, writer);
			}

			//Then determine which log records must be applied
			if (logBlobEntries == null) {
				//If not specified, then we read here
				logBlobEntries = index.readLogBlobEntries(fileBlobName, meta.latestLogBlobLsn, meta.latestLogBlobRandomId, latestLogBlobLsn, latestLogBlobRandomId);
			} else {
				//If specified, we discard those already applied (i.e., those up to and including meta.latestLogBlobLsn)
				while (!logBlobEntries.isEmpty() && logBlobEntries.peekFirst().logBlobLsn <= meta.latestLogBlobLsn)
					logBlobEntries.removeFirst();
			}

			//Finally, we apply the entries into the local cache
			updateLocalCacheBlob_StartFromLocalCacheBlob(writer, logBlobEntries);
		}

	}
}
