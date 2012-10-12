package com.paviasystem.cloudfs.log;

import com.paviasystem.cloudfs.Direction;
import com.paviasystem.cloudfs.log.FileSystemLogRecord.Type;

public interface FileSystemLog {

	/**
	 * Searches for the first log record that satisfies a given set of
	 * conditions.
	 * 
	 * @param filePath
	 *            Restricts the search to log records associated to this file
	 *            path
	 * @param direction
	 *            Direction of the search.
	 * @param types
	 *            If not null, restricts the search to log records of these
	 *            types.
	 * @return If found, returns the record; otherwise returns null.
	 */
	FileSystemLogRecord findFirst(String filePath, Direction direction, Type[] types);
}
