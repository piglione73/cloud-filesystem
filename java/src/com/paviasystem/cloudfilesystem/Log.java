package com.paviasystem.cloudfilesystem;

import java.util.stream.Stream;

public interface Log {

	Stream<LogEntry> read(long nodeNumber, long logSequenceNumberFrom);

}
