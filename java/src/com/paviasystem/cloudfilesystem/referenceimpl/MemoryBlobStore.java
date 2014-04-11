package com.paviasystem.cloudfilesystem.referenceimpl;

import java.util.HashMap;

import com.paviasystem.cloudfilesystem.blocks.BlobStore;
import com.paviasystem.cloudfilesystem.blocks.ByteReader;
import com.paviasystem.cloudfilesystem.blocks.ByteWriter;

public class MemoryBlobStore implements BlobStore {

	@Override
	public Iterable<String> list(String prefix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ByteReader read(String key, HashMap<String, String> outMetaData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ByteWriter write(String key, HashMap<String, String> metaData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(String key) {
		// TODO Auto-generated method stub
		
	}

}
