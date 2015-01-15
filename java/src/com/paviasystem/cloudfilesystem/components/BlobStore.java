package com.paviasystem.cloudfilesystem.components;

public interface BlobStore {
	Blob get(String blobName) throws Exception;

	void set(String blobName, Blob blob) throws Exception;
}
