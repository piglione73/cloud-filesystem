package com.paviasystem.cloudfilesystem.impl;

public abstract class Node implements AutoCloseable {
	public final long nodeNumber;

	public Node(long nodeNumber) {
		this.nodeNumber = nodeNumber;
	}
}
