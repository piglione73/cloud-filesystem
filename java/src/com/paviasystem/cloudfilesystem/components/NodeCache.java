package com.paviasystem.cloudfilesystem.components;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

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
	public synchronized Blob get(String blobName) throws Exception {
		Path blobPath = getBlobPath(blobName);
		if (!Files.exists(blobPath))
			return null;

		try (SeekableByteChannel channel = Files.newByteChannel(blobPath, StandardOpenOption.READ)) {
			// Metadata
			ByteBuffer metaData = Utils.readFrom(channel, 8);
			long lsn = metaData.getLong();

			// Data
			FileChannel blobChannel = Utils.createTempFileChannel();
			Utils.copyAll(channel, blobChannel);

			return new Blob(lsn, blobChannel);
		}
	}

	@Override
	public synchronized void set(String blobName, Blob blob) throws Exception {
		Path blobPath = getBlobPath(blobName);
		if (blob == null) {
			Files.deleteIfExists(blobPath);
			return;
		}

		try (SeekableByteChannel channel = Files.newByteChannel(blobPath, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
			// Metadata
			ByteBuffer metaData = ByteBuffer.allocate(64);
			metaData.putLong(blob.latestLogSequenceNumber);

			metaData.flip();
			channel.write(metaData);

			// Data
			blob.bytes.position(0);
			Utils.copyAll(blob.bytes, channel);
		}
	}

}
