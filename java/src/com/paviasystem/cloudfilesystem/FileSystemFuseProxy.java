package com.paviasystem.cloudfilesystem;

import java.util.ArrayList;

import net.fusejna.DirectoryFiller;
import net.fusejna.ErrorCodes;
import net.fusejna.FuseException;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.ModeWrapper;
import net.fusejna.types.TypeMode.NodeType;
import net.fusejna.util.FuseFilesystemAdapterFull;

import com.paviasystem.cloudfilesystem.referenceimpl.MemoryIndex;

public class FileSystemFuseProxy extends FuseFilesystemAdapterFull {
	public static void main(final String... args) throws FuseException {
		if (args.length != 1) {
			System.err.println("Usage: FileSystemFuseProxy <mountpoint>");
			System.exit(1);
		}

		FileSystem cfs = new CloudFileSystem(new MemoryIndex());
		new FileSystemFuseProxy(cfs).log(true).mount(args[0]);
	}

	FileSystem fs;

	public FileSystemFuseProxy(FileSystem fs) {
		this.fs = fs;
	}

	@Override
	public int getattr(final String path, final StatWrapper stat) {
		if (path.equals("/")) {
			stat.setMode(NodeType.DIRECTORY, true, true, true);
			return 0;
		}

		FileSystemEntry entry = fs.getEntry(path);
		if (entry != null) {
			NodeType nt = entry.isFile() ? NodeType.FILE : NodeType.DIRECTORY;
			stat.setMode(nt, true, true, true);
			stat.setAllTimesMillis(entry.timestamp.getTime());
			return 0;
		}

		return -ErrorCodes.ENOENT();
	}

	@Override
	public int readdir(final String path, final DirectoryFiller filler) {
		ArrayList<FileSystemEntry> list = fs.list(path);
		for (FileSystemEntry entry : list)
			filler.add(entry.getAbsolutePath());

		return 0;
	}

	@Override
	public int mkdir(String path, ModeWrapper mode) {
		fs.createDirectory(path);
		return 0;
	}

	@Override
	public int rmdir(String path) {
		fs.deleteDirectory(path);
		return 0;
	}

	// private final String filename = "/hello.txt";
	// private final String contents = "Hello World!\n";
	//
	// @Override
	// public int read(final String path, final ByteBuffer buffer,
	// final long size, final long offset, final FileInfoWrapper info) {
	//
	// // Compute substring that we are being asked to read
	// final String s = contents.substring(
	// (int) offset,
	// (int) Math.max(offset,
	// Math.min(contents.length() - offset, offset + size)));
	// buffer.put(s.getBytes());
	// return s.getBytes().length;
	// }

}