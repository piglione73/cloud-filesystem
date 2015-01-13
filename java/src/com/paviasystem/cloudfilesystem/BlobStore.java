package com.paviasystem.cloudfilesystem;

public interface BlobStore {
	Blob get(String blobName) throws Exception;

	void set(String blobName, Blob blob) throws Exception;
}
