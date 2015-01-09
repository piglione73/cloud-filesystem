package com.paviasystem.cloudfilesystem;

import java.nio.channels.FileChannel;

public class Blob {
	public long latestLogSequenceNumber;
	public FileChannel bytes;
}
