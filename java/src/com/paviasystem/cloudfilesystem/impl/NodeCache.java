package com.paviasystem.cloudfilesystem.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.paviasystem.cloudfilesystem.Blob;
import com.paviasystem.cloudfilesystem.BlobStore;
import com.paviasystem.cloudfilesystem.Utils;

public class NodeCache implements BlobStore {

	Path cachePath;

	public NodeCache(Path cachePath) throws IOException {
		this.cachePath = cachePath;

		Files.createDirectories(cachePath);
	}

	private Path getBlobPath(String blobName) {
		return cachePath.resolve(blobName);
	}

	@Override
	public Blob get(String blobName) throws Exception {
		Path blobPath = getBlobPath(blobName);
	}

	@Override
	public void set(String blobName, Blob blob) throws Exception {
		Path blobPath = getBlobPath(blobName);
		if (blob == null) {
			Files.deleteIfExists(blobPath);
			return;
		}

		try (SeekableByteChannel channel = Files.newByteChannel(blobPath, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
			ByteBuffer metaData = ByteBuffer.allocate(64);
			metaData.putLong(blob.latestLogSequenceNumber);

			metaData.flip();
			channel.write(metaData);

			Utils.copyAll(blob.bytes, channel);
		}
	}

}
