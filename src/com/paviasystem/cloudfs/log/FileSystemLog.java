package com.paviasystem.cloudfs.log;

import java.util.ArrayList;
import java.util.Date;

public interface FileSystemLog {
	void write(FileSystemLogRecord record);

	ArrayList<FileSystemLogRecord> read(Date from1, int from2, Date to1, int to2, String path);
}
