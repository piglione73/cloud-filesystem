package com.paviasystem.cloudfilesystem;

public interface BlobStore {
	Blob get(String blobName);

	void set(String blobName, Blob blob);
}
