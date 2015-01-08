package com.paviasystem.cloudfilesystemold.referenceimpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.paviasystem.cloudfilesystemold.blocks.AbsoluteByteReader;
import com.paviasystem.cloudfilesystemold.blocks.AbsoluteByteWriter;
import com.paviasystem.cloudfilesystemold.blocks.ByteReader;
import com.paviasystem.cloudfilesystemold.blocks.ByteWriter;
import com.paviasystem.cloudfilesystemold.blocks.LocalCache;
import com.paviasystem.cloudfilesystemold.blocks.data.LocalCacheEntry;

public class MemoryLocalCache implements LocalCache {

	HashMap<String, HashMap<String, ArrayList<Byte>>> objs = new HashMap<>();

	ArrayList<Byte> get(String category, String name) {
		HashMap<String, ArrayList<Byte>> categoryListing = objs.get(category);
		if (categoryListing == null)
			return null;

		ArrayList<Byte> item = categoryListing.get(name);
		if (item == null)
			return null;

		return item;
	}

	ArrayList<Byte> allocate(String category, String name) {
		HashMap<String, ArrayList<Byte>> categoryListing = objs.get(category);
		if (categoryListing == null) {
			categoryListing = new HashMap<>();
			objs.put(category, categoryListing);
		}

		ArrayList<Byte> item = categoryListing.get(name);
		if (item == null) {
			item = new ArrayList<>();
			categoryListing.put(name, item);
		}

		return item;
	}

	@Override
	public ByteWriter openSequentialWriter(String category, String name) {
		final ArrayList<Byte> bytes = allocate(category, name);
		return new ByteWriter() {
			int fileOffset = 0;

			@Override
			public void close() throws Exception {
			}

			@Override
			public void write(byte[] buffer, int bufferOffset, int numBytesToWrite) {
				while (numBytesToWrite-- > 0) {
					while (bytes.size() <= fileOffset)
						bytes.add((byte) 0);
					bytes.set(fileOffset++, buffer[bufferOffset++]);
				}
			}
		};
	}

	@Override
	public ByteReader openSequentialReader(String category, String name) {
		final ArrayList<Byte> bytes = get(category, name);
		if (bytes == null)
			return null;

		return new ByteReader() {
			int bytesOffset = 0;

			@Override
			public void close() throws Exception {
			}

			@Override
			public int read(byte[] buffer, int bufferOffset, int maxBytesToRead) {
				int bytesRead = 0;
				while (bytesRead < maxBytesToRead && bytesOffset < bytes.size() && bufferOffset < buffer.length) {
					buffer[bufferOffset++] = bytes.get(bytesOffset++);
					bytesRead++;
				}

				return bytesRead;
			}
		};
	}

	@Override
	public AbsoluteByteReader openAbsoluteReader(String category, String name) {
		final ArrayList<Byte> bytes = get(category, name);
		if (bytes == null)
			return null;

		return new AbsoluteByteReader() {
			@Override
			public void close() throws Exception {
			}

			@Override
			public int read(byte[] buffer, int bufferOffset, int maxBytesToRead, long fileOffset) {
				int bytesRead = 0;
				for (int i = 0; i < maxBytesToRead && fileOffset < bytes.size() && bufferOffset < buffer.length; i++) {
					buffer[bufferOffset++] = bytes.get((int) fileOffset++);
					bytesRead++;
				}

				return bytesRead;
			}
		};
	}

	@Override
	public AbsoluteByteWriter openAbsoluteWriter(String category, String name) {
		final ArrayList<Byte> bytes = allocate(category, name);
		return new AbsoluteByteWriter() {
			@Override
			public void close() throws Exception {
			}

			@Override
			public void write(byte[] buffer, int bufferOffset, int numBytesToWrite, long fileOffset) {
				while (numBytesToWrite-- > 0) {
					while (bytes.size() <= fileOffset)
						bytes.add((byte) 0);

					bytes.set((int) fileOffset++, buffer[bufferOffset++]);
				}
			}

			@Override
			public void setLength(long newLength) {
				while (bytes.size() < newLength)
					bytes.add((byte) 0);
				while (bytes.size() > newLength)
					bytes.remove(bytes.size() - 1);
			}
		};
	}

	@Override
	public void delete(String category, String name) {
		HashMap<String, ArrayList<Byte>> categoryListing = objs.get(category);
		if (categoryListing == null)
			return;

		categoryListing.remove(name);
	}

	@Override
	public Iterable<LocalCacheEntry> list() {
		ArrayList<LocalCacheEntry> list = new ArrayList<>();
		for (Map.Entry<String, HashMap<String, ArrayList<Byte>>> x : objs.entrySet()) {
			String category = x.getKey();
			for (String name : x.getValue().keySet()) {
				LocalCacheEntry lce = new LocalCacheEntry();
				lce.category = category;
				lce.name = name;
				lce.lastAccessTimestamp = new Date();

				list.add(lce);
			}
		}

		return list;
	}

}
