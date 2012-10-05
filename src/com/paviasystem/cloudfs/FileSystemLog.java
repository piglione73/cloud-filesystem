package com.paviasystem.cloudfs;

public interface FileSystemLog {
	void write(String path, long offset, long len, byte[] bytes);

	void remove(String path);

	void beginTransaction();

	void commit();

	void rollback();
}
