package com.paviasystem.cloudfilesystem.exceptions;

import com.paviasystem.cloudfilesystem.Path;

public class DirectoryNotFoundException extends Exception {
	private static final long serialVersionUID = -2295053274630772252L;

	String[] pathParts;

	public DirectoryNotFoundException(String[] pathParts, String message) {
		super(Path.compose(pathParts) + " - " + message);
		this.pathParts = pathParts;
	}

}
