package com.paviasystem.cloudfilesystem;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import net.fusejna.DirectoryFiller;
import net.fusejna.ErrorCodes;
import net.fusejna.FuseException;
import net.fusejna.StructFuseFileInfo.FileInfoWrapper;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.ModeWrapper;
import net.fusejna.types.TypeMode.NodeType;
import net.fusejna.util.FuseFilesystemAdapterFull;

import com.paviasystem.cloudfilesystem.referenceimpl.MemoryIndex;

public class FileSystemFuseProxy extends FuseFilesystemAdapterFull {
	final static Object handlesSync = new Object();
	static long nextFileHandle = 1;
	final static HashMap<Long, File> fileHandles = new HashMap<Long, File>();

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

	@Override
	public int create(String path, ModeWrapper mode, FileInfoWrapper info) {
		File f = fs.open(path, true);
		registerHandle(info, f);

		return 0;
	}

	@Override
	public int open(String path, FileInfoWrapper info) {
		File f = fs.open(path, false);
		registerHandle(info, f);

		return 0;
	}

	private void registerHandle(FileInfoWrapper info, File f) {
		synchronized (handlesSync) {
			long fileHandle = nextFileHandle++;
			fileHandles.put(fileHandle, f);
			info.fh(fileHandle);
		}
	}

	private File getHandle(FileInfoWrapper info) {
		synchronized (handlesSync) {
			return fileHandles.get(info.fh());
		}
	}

	private void releaseHandle(FileInfoWrapper info) {
		synchronized (handlesSync) {
			fileHandles.remove(info.fh());
		}
	}

	@Override
	public int fsync(String path, int datasync, FileInfoWrapper info) {
		File f = getHandle(info);
		f.flush();
		return 0;
	}

	@Override
	public int release(String path, FileInfoWrapper info) {
		releaseHandle(info);
		return 0;
	}

	@Override
	public int read(String path, ByteBuffer buffer, long size, long offset,
			FileInfoWrapper info) {
		File f = getHandle(info);

		byte[] buf = new byte[(int) Math.min(size, 64 * 1024)];
		int totalBytesRead = 0;
		int remainingBytesToRead = (int) size;
		long curFileOffset = offset;

		while (remainingBytesToRead > 0) {
			int bytesRead = f.read(buf, 0, remainingBytesToRead, curFileOffset);
			if (bytesRead == 0) {
				// No more bytes in file
				break;
			} else {
				// Read something
				totalBytesRead += bytesRead;
				remainingBytesToRead -= bytesRead;
				curFileOffset += bytesRead;
				buffer.put(buf, 0, bytesRead);
			}
		}

		return totalBytesRead;
	}

	@Override
	public int write(String path, ByteBuffer buffer, long size, long offset,
			FileInfoWrapper info) {
		File f = getHandle(info);

		byte[] buf = new byte[(int) Math.min(size, 64 * 1024)];
		int totalBytesWritten = 0;
		int remainingBytesToWrite = (int) size;
		long curOffset = offset;

		while (remainingBytesToWrite > 0) {
			int bytesFromBuffer = (int) Math.min(buf.length,
					remainingBytesToWrite);
			buffer.get(buf, 0, bytesFromBuffer);
			f.write(buf, 0, bytesFromBuffer, curOffset);

			// Advance counters
			curOffset += bytesFromBuffer;
			totalBytesWritten += bytesFromBuffer;
			remainingBytesToWrite -= bytesFromBuffer;
		}

		return totalBytesWritten;
	}

	@Override
	public int rename(String path, String newName) {
		// TODO Auto-generated method stub
		return super.rename(path, newName);
	}

	@Override
	public int truncate(String path, long offset) {
		// TODO Auto-generated method stub
		return super.truncate(path, offset);
	}

	@Override
	public int unlink(String path) {
		// TODO Auto-generated method stub
		return super.unlink(path);
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
