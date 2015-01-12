package com.paviasystem.cloudfilesystem.impl;

public class DirectoryNodeItem implements Comparable<DirectoryNodeItem> {
	public String name;
	public long nodeNumber;

	public DirectoryNodeItem(String name, long nodeNumber) {
		super();
		this.name = name;
		this.nodeNumber = nodeNumber;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof DirectoryNodeItem))
			return false;
		else
			return this.name.equals(((DirectoryNodeItem) other).name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public int compareTo(DirectoryNodeItem other) {
		return this.name.compareTo(other.name);
	}
}
