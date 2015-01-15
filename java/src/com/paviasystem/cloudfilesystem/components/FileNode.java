package com.paviasystem.cloudfilesystem.components;


public class FileNode extends Node {
	public final Blob blob;

	public FileNode(long nodeNumber, Blob blob) {
		super(nodeNumber);
		this.blob = blob;
	}

	@Override
	public void close() throws Exception {
		blob.close();
	}
}
