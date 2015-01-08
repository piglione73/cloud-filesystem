package com.paviasystem.cloudfilesystemold.blocks;

import java.util.HashMap;

public interface BlobStore {
	Iterable<String> list(String prefix);

	ByteReader read(String key, HashMap<String, String> outMetaData);

	ByteWriter write(String key, HashMap<String, String> metaData);

	void delete(String key);
}
