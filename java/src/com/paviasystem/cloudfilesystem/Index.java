package com.paviasystem.cloudfilesystem;

import java.util.ArrayList;

public interface Index {

	ArrayList<IndexEntry> listEntries(String absolutePath);

	IndexEntry getEntry(String absolutePath);

	void createDirectoryEntry(String absolutePath);

	void createFileEntry(String absolutePath, String blobName);

	void deleteEntry(String absolutePath);
}
