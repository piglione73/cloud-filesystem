package com.paviasystem.cloudfilesystem;

public interface Log {

	Iterable<LogEntry> read(long nodeNumber, long logSequenceNumberFrom);

}
