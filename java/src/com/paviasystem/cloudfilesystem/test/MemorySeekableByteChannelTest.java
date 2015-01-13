package com.paviasystem.cloudfilesystem.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.paviasystem.cloudfilesystem.referenceimpl.MemorySeekableByteChannel;

public class MemorySeekableByteChannelTest {
	@Test
	public void test01_Write() throws Exception {
		try (MemorySeekableByteChannel x = new MemorySeekableByteChannel()) {
			assertEquals(0, x.position());
			assertEquals(0, x.size());
			assertArrayEquals(new byte[] {}, x.toByteArray());

			x.write(ByteBuffer.wrap(new byte[] { 0, 1, 2, 3, 4 }));
			assertEquals(5, x.position());
			assertEquals(5, x.size());
			assertArrayEquals(new byte[] { 0, 1, 2, 3, 4 }, x.toByteArray());

			x.position(3);
			assertEquals(3, x.position());
			assertEquals(5, x.size());

			x.write(ByteBuffer.wrap(new byte[] { 33, 34, 35 }));
			assertEquals(6, x.position());
			assertEquals(6, x.size());
			assertArrayEquals(new byte[] { 0, 1, 2, 33, 34, 35 }, x.toByteArray());

			x.position(10);
			assertEquals(10, x.position());
			assertEquals(6, x.size());

			x.write(ByteBuffer.wrap(new byte[] { 40, 41, 42 }));
			assertEquals(13, x.position());
			assertEquals(13, x.size());
			assertArrayEquals(new byte[] { 0, 1, 2, 33, 34, 35, 0, 0, 0, 0, 40, 41, 42 }, x.toByteArray());

			x.position(2);
			assertEquals(2, x.position());
			assertEquals(13, x.size());

			x.write(ByteBuffer.wrap(new byte[] { 12, 13 }));
			assertEquals(4, x.position());
			assertEquals(13, x.size());
			assertArrayEquals(new byte[] { 0, 1, 12, 13, 34, 35, 0, 0, 0, 0, 40, 41, 42 }, x.toByteArray());
		}
	}

	@Test
	public void test02_Read() throws Exception {
		try (MemorySeekableByteChannel x = new MemorySeekableByteChannel()) {
			x.write(ByteBuffer.wrap(new byte[] { 0, 1, 2, 3, 4 }));
			assertEquals(5, x.position());
			assertEquals(5, x.size());
			assertArrayEquals(new byte[] { 0, 1, 2, 3, 4 }, x.toByteArray());

			ByteBuffer buf = ByteBuffer.allocate(10);
			buf.position(0).limit(10);
			assertArrayEquals(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, buf.array());

			x.position(0);
			assertEquals(5, x.read(buf));
			assertEquals(5, x.position());
			assertEquals(5, x.size());
			assertArrayEquals(new byte[] { 0, 1, 2, 3, 4, 0, 0, 0, 0, 0 }, buf.array());

			buf.position(5).limit(7);
			x.position(3);
			assertEquals(2, x.read(buf));
			assertEquals(5, x.position());
			assertEquals(5, x.size());
			assertArrayEquals(new byte[] { 0, 1, 2, 3, 4, 3, 4, 0, 0, 0 }, buf.array());

			buf.position(5).limit(7);
			x.position(1);
			assertEquals(2, x.read(buf));
			assertEquals(3, x.position());
			assertEquals(5, x.size());
			assertArrayEquals(new byte[] { 0, 1, 2, 3, 4, 1, 2, 0, 0, 0 }, buf.array());

			buf.position(5).limit(7);
			x.position(10);
			assertEquals(-1, x.read(buf));
			assertEquals(10, x.position());
			assertEquals(5, x.size());
			assertArrayEquals(new byte[] { 0, 1, 2, 3, 4, 1, 2, 0, 0, 0 }, buf.array());
		}
	}
}
