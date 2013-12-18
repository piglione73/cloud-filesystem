package com.paviasystem.cloudfilesystem;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import net.fusejna.DirectoryFiller;
import net.fusejna.ErrorCodes;
import net.fusejna.FuseException;
import net.fusejna.StructFuseFileInfo.FileInfoWrapper;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.NodeType;
import net.fusejna.util.FuseFilesystemAdapterFull;

public class FileSystemFuseProxy extends FuseFilesystemAdapterFull {
	public static void main(final String... args) throws FuseException {
		if (args.length != 1) {
			System.err.println("Usage: FileSystemFuseProxy <mountpoint>");
			System.exit(1);
		}

		FileSystem cfs = new CloudFileSystem();
		new FileSystemFuseProxy(cfs).log(true).mount(args[0]);
	}

	FileSystem fs;

	public FileSystemFuseProxy(FileSystem fs) {
		this.fs = fs;
	}

	@Override
	public int readdir(final String path, final DirectoryFiller filler) {
		ArrayList<FileSystemEntry> list = fs.list(path);
		for (FileSystemEntry entry : list)
			filler.add(entry.getAbsolutePath());

		return 0;
	}

	@Override
	public int getattr(final String path, final StatWrapper stat) {
		FileSystemEntry entry = fs.getEntry(path);
		if (entry != null) {
			NodeType nt = entry.isFile() ? NodeType.FILE : NodeType.DIRECTORY;
			stat.setMode(nt, true, true, true);
			return 0;
		}

		return -ErrorCodes.ENOENT();
	}

	private final String filename = "/hello.txt";
	private final String contents = "Hello World!\n";

	@Override
	public int read(final String path, final ByteBuffer buffer,
			final long size, final long offset, final FileInfoWrapper info) {
		// Compute substring that we are being asked to read
		final String s = contents.substring(
				(int) offset,
				(int) Math.max(offset,
						Math.min(contents.length() - offset, offset + size)));
		buffer.put(s.getBytes());
		return s.getBytes().length;
	}

}
