package com.paviasystem.cloudfilesystem.referenceimpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.paviasystem.cloudfilesystem.blocks.BlobStore;
import com.paviasystem.cloudfilesystem.blocks.ByteReader;
import com.paviasystem.cloudfilesystem.blocks.ByteWriter;

public class MemoryBlobStore implements BlobStore {

	private static class Item {
		public byte[] bytes = new byte[0];
		public HashMap<String, String> meta = new HashMap<>();
	}

	private static class ItemReader implements ByteReader {
		private ByteArrayInputStream byteStream;

		public ItemReader(Item item) {
			byteStream = new ByteArrayInputStream(item.bytes);
		}

		@Override
		public void close() throws Exception {
			byteStream.close();
		}

		@Override
		public int read(byte[] buffer, int bufferOffset, int maxBytesToRead) {
			return byteStream.read(buffer, bufferOffset, maxBytesToRead);
		}
	}

	private static class ItemWriter implements ByteWriter {
		private Item item;
		private ByteArrayOutputStream byteStream;

		public ItemWriter(Item item) {
			this.item = item;
			byteStream = new ByteArrayOutputStream();
		}

		@Override
		public void close() throws Exception {
			item.bytes = byteStream.toByteArray();
			byteStream.close();
		}

		@Override
		public void write(byte[] buffer, int bufferOffset, int numBytesToWrite) {
			byteStream.write(buffer, bufferOffset, numBytesToWrite);
		}
	}

	private HashMap<String, Item> blobs = new HashMap<>();

	@Override
	public Iterable<String> list(String prefix) {
		ArrayList<String> ret = new ArrayList<>();
		for (String key : blobs.keySet()) {
			if (prefix != null && !key.startsWith(prefix))
				continue;

			ret.add(key);
		}

		return ret;
	}

	@Override
	public ByteReader read(String key, HashMap<String, String> outMetaData) {
		Item blob = blobs.get(key);
		if (blob == null)
			return null;

		if (outMetaData != null) {
			for (Map.Entry<String, String> metaEntry : blob.meta.entrySet())
				outMetaData.put(metaEntry.getKey(), metaEntry.getValue());
		}

		return new ItemReader(blob);
	}

	@Override
	public ByteWriter write(String key, HashMap<String, String> metaData) {
		Item blob = new Item();
		blobs.put(key, blob);

		if (metaData != null) {
			for (Map.Entry<String, String> metaEntry : metaData.entrySet())
				blob.meta.put(metaEntry.getKey(), metaEntry.getValue());
		}

		return new ItemWriter(blob);
	}

	@Override
	public void delete(String key) {
		blobs.remove(key);
	}

}
