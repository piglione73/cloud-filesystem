package com.paviasystem.cloudfilesystem.impl;

import java.util.TreeSet;

public class DirectoryNode extends Node {

	public long latestLogSequenceNumber;
	public final TreeSet<DirectoryNodeItem> listing;

	public DirectoryNode(long nodeNumber, long latestLogSequenceNumber, TreeSet<DirectoryNodeItem> listing) {
		super(nodeNumber);
		this.latestLogSequenceNumber = latestLogSequenceNumber;
		this.listing = listing;
	}

	@Override
	public void close() throws Exception {
	}
}
