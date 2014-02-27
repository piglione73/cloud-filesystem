package com.paviasystem.cloudfilesystem.blocks;

import java.util.HashMap;

public interface BlobStore {
	HashMap<String, String> readMeta(String blobName);

	ByteReader read(String name);

	ByteWriter write(String name, HashMap<String, String> metaData);

	void delete(String name);

	Iterable<String> list(String prefix);

}
