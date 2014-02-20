package com.paviasystem.cloudfilesystem.blocks;

import java.util.ArrayList;
import java.util.Date;

import com.paviasystem.cloudfilesystem.blocks.data.IndexEntry;


public interface Index {

	ArrayList<IndexEntry> listEntries(String absolutePath);

	IndexEntry getEntry(String absolutePath);

	void createDirectoryEntry(String absolutePath);

	IndexEntry createFileEntry(String absolutePath, String blobName);

	void updateEntry(String absolutePath, long length, Date timestamp);

	void updateEntry(String absolutePath, String newAbsolutePath);

	void deleteEntry(String absolutePath);
}
