package com.paviasystem.cloudfilesystem.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.paviasystem.cloudfilesystem.Blob;
import com.paviasystem.cloudfilesystem.BlobStore;

public class NodeCache implements BlobStore {

	Path cachePath;
	
	public NodeCache(Path cachePath) throws IOException {
		this.cachePath=cachePath;
		
		Files.createDirectories(cachePath);
	}
	
	@Override
	public Blob get(String blobName) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void set(String blobName, Blob blob) throws Exception {
	}

}
