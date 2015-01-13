package com.paviasystem.cloudfilesystem.referenceimpl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

import com.paviasystem.cloudfilesystem.Blob;
import com.paviasystem.cloudfilesystem.BlobStore;

public class MemoryBlobStore implements BlobStore {

	HashMap<String, Blob> blobs = new HashMap<String, Blob>();

	Blob clone(Blob blob) throws IOException {
		MemorySeekableByteChannel bytes = new MemorySeekableByteChannel();
		blob.bytes.position(0);
		ByteBuffer chunk = ByteBuffer.allocate(64 * 1024);
		for (int read = blob.bytes.read(chunk); read != -1; read = blob.bytes.read(chunk)) {
			chunk.flip();
			bytes.write(chunk);
			chunk.clear();
		}

		bytes.position(0);
		return new Blob(blob.latestLogSequenceNumber, bytes);
	}

	@Override
	public Blob get(String blobName) throws Exception {
		Blob blob = blobs.get(blobName);
		if (blob == null)
			return null;

		return clone(blob);
	}

	@Override
	public void set(String blobName, Blob blob) throws Exception {
		if (blob != null)
			blobs.put(blobName, clone(blob));
		else {
			Blob old = blobs.remove(blobName);
			if (old != null)
				old.close();
		}
	}

}
