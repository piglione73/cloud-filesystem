package com.paviasystem.cloudfilesystem;

import java.util.ArrayList;
import java.util.Date;

public interface Index {

	ArrayList<IndexEntry> listEntries(String absolutePath);

	IndexEntry getEntry(String absolutePath);

	void createDirectoryEntry(String absolutePath);

	IndexEntry createFileEntry(String absolutePath, String blobName);

	void updateEntry(String absolutePath, long length, Date timestamp);

	void updateEntry(String absolutePath, String newAbsolutePath);

	void deleteEntry(String absolutePath);
}
