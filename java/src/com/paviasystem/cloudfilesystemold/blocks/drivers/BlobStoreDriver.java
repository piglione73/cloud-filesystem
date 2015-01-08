package com.paviasystem.cloudfilesystemold.blocks.drivers;

import java.util.HashMap;
import java.util.Iterator;

import com.paviasystem.cloudfilesystemold.blocks.BlobStore;
import com.paviasystem.cloudfilesystemold.blocks.ByteReader;
import com.paviasystem.cloudfilesystemold.blocks.ByteWriter;

public class BlobStoreDriver {
	final static String LOG_PREFIX = "log/";
	final static String FILE_PREFIX = "files/";

	private BlobStore blobStore;

	public BlobStoreDriver(BlobStore blobStore) {
		this.blobStore = blobStore;
	}

	private Iterable<String> listBlobs(final String prefix) {
		Iterable<String> keys = blobStore.list(prefix);
		final Iterator<String> it = keys.iterator();

		return new Iterable<String>() {
			@Override
			public Iterator<String> iterator() {
				return new Iterator<String>() {
					@Override
					public boolean hasNext() {
						return it.hasNext();
					}

					@Override
					public String next() {
						String x = it.next();
						return x.substring(prefix.length());
					}

					@Override
					public void remove() {
						it.remove();
					}
				};
			}
		};
	}

	private static String getLogBlobKey(String randomId) {
		return LOG_PREFIX + randomId;
	}

	public Iterable<String> listLogBlobs() {
		return listBlobs(LOG_PREFIX);
	}

	public ByteReader readLogBlob(String randomId) {
		HashMap<String, String> dummy = new HashMap<>();
		return blobStore.read(getLogBlobKey(randomId), dummy);
	}

	public ByteWriter writeLogBlob(String randomId) {
		HashMap<String, String> dummy = new HashMap<>();
		return blobStore.write(getLogBlobKey(randomId), dummy);
	}

	public void deleteLogBlob(String randomId) {
		blobStore.delete(getLogBlobKey(randomId));
	}

	public static class FileMetaData {
		final static String LatestLogBlobLsn = "LatestLogLSN";
		final static String LatestLogBlobRandomId = "LatestLogId";

		public long latestLogBlobLsn = -1;
		public String latestLogBlobRandomId = null;

		private HashMap<String, String> toMap() {
			HashMap<String, String> map = new HashMap<>();
			map.put(LatestLogBlobLsn, Long.toString(latestLogBlobLsn));
			map.put(LatestLogBlobRandomId, latestLogBlobRandomId);

			return map;
		}

		private void fromMap(HashMap<String, String> map) {
			String lsn = map.get(LatestLogBlobLsn);
			if (lsn != null)
				latestLogBlobLsn = Long.parseLong(lsn);

			latestLogBlobRandomId = map.get(LatestLogBlobRandomId);
		}
	}

	private static String getFileBlobKey(String fileBlobName) {
		return FILE_PREFIX + fileBlobName;
	}

	public Iterable<String> listFileBlobs() {
		return listBlobs(FILE_PREFIX);
	}

	public ByteReader readFileBlob(String fileBlobName, FileMetaData outMetaData) {
		HashMap<String, String> meta = new HashMap<>();
		ByteReader reader = blobStore.read(getFileBlobKey(fileBlobName), meta);

		if (outMetaData != null)
			outMetaData.fromMap(meta);

		return reader;
	}

	public ByteWriter writeFileBlob(String fileBlobName, FileMetaData metaData) {
		return blobStore.write(getFileBlobKey(fileBlobName), metaData != null ? metaData.toMap() : null);
	}

	public void deleteFileBlob(String fileBlobName) {
		blobStore.delete(getFileBlobKey(fileBlobName));
	}
}
