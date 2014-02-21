package com.paviasystem.cloudfsobsolete;

import com.paviasystem.cloudfsobsolete.log.FileSystemLog;
import com.paviasystem.cloudfsobsolete.log.FileSystemLogRecord;
import com.paviasystem.cloudfsobsolete.log.FileSystemLogRecord.Type;

/**
 * The log engine, where all log operations are implemented.
 * 
 * @author Roberto
 * 
 */
final class Log {

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
	 * @param logPipeline
	 *            The log pipeline in which to search.
	 * @return If found, returns the record; otherwise returns null.
	 */
	public static FileSystemLogRecord findFirst(String filePath, Direction direction, Type[] types, FileSystemLog[] logPipeline) {
		if (filePath == null)
			throw new IllegalArgumentException("filePath");
		if (logPipeline == null || logPipeline.length == 0)
			throw new IllegalArgumentException("logPipeline");

		// Apply the direction and search in the logPipeline
		if (direction == Direction.Ascending) {
			// From oldest to newest
			for (FileSystemLog log : logPipeline) {
				FileSystemLogRecord rec = log.findFirst(filePath, direction, types);
				if (rec != null) {
					/*
					 * Record found. It is guaranteed that subsequent elements
					 * in the log pipeline (for the same filePath) are all with
					 * a higher timestamp, so this is really the FIRST in
					 * ascending order.
					 */
					return rec;
				}
			}
		} else if (direction == Direction.Descending) {
			// From newest to oldest
			for (int i = logPipeline.length - 1; i >= 0; i--) {
				FileSystemLog log = logPipeline[i];
				FileSystemLogRecord rec = log.findFirst(filePath, direction, types);
				if (rec != null) {
					/*
					 * Record found. It is guaranteed that previous elements in
					 * the log pipeline (for the same filePath) are all with a
					 * lower timestamp, so this is really the FIRST in
					 * descending order.
					 */
					return rec;
				}
			}
		} else
			throw new IllegalStateException("Unexpected direction: " + direction);

		// We found nothing
		return null;
	}

	/**
	 * Writes a log record into the last element of the log pipeline.
	 * 
	 * @param record
	 * @param logPipeline
	 */
	public static void write(FileSystemLogRecord record, FileSystemLog[] logPipeline) {
		if (record == null)
			throw new IllegalArgumentException("record");
		if (logPipeline == null || logPipeline.length == 0)
			throw new IllegalArgumentException("logPipeline");

		//Last element in the pipeline
		FileSystemLog last = logPipeline[logPipeline.length - 1];

		//Forward the request
		last.write(record);
	}
}
