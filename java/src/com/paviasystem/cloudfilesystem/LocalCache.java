package com.paviasystem.cloudfilesystem;

public interface LocalCache {

	void writeFile(String folderName, String fileName, byte[] fileBytes);

}
