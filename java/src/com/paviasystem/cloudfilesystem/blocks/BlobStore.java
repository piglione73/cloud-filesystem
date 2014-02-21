package com.paviasystem.cloudfilesystem.blocks;

import java.util.HashMap;


public interface BlobStore {
	ByteReader read(String name);

	ByteWriter write(String name, HashMap<String, String> metaData);

	void delete(String name);

	Iterable<String> list(String prefix);
}
