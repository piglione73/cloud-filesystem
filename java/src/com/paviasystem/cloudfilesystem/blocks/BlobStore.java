package com.paviasystem.cloudfilesystem.blocks;

import java.util.HashMap;

import com.paviasystem.cloudfilesystem.ByteReader;
import com.paviasystem.cloudfilesystem.ByteWriter;

public interface BlobStore {
	ByteReader read(String blobName);

	ByteWriter write(String blobName, HashMap<String, String> metaData);

	void delete(String blobName);

	Iterable<String> list();
}
