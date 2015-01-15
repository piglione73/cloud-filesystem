package com.paviasystem.cloudfilesystem.impl;

public class DirectoryNodeItem implements Comparable<DirectoryNodeItem> {
	public static enum Type {
		FILE, DIRECTORY
	}

	public String name;
	public Type type;
	public long nodeNumber;

	public DirectoryNodeItem(String name, Type type, long nodeNumber) {
		this.name = name;
		this.type = type;
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
