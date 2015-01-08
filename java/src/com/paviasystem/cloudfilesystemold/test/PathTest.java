package com.paviasystem.cloudfilesystemold.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.paviasystem.filesystem.Path;

public class PathTest {
	@Test
	public void compose() {
		assertEquals("", Path.compose(null));
		assertEquals("", Path.compose(new String[] {}));
		assertEquals("", Path.compose(new String[] { "   " }));
		assertEquals("a", Path.compose(new String[] {
			"   ",
			"   a   " }));
		assertEquals("a/bbb/ccc", Path.compose(new String[] {
			"   ",
			"   a   ",
			"bbb   ",
			null,
			"      ccc   " }));
		assertEquals("a/bbb/ccc/Ddd", Path.compose(new String[] {
			"   ",
			"   a   ",
			"bbb   ",
			"      ccc   ",
			"Ddd" }));
	}

	@Test
	public void decompose() {
		assertArrayEquals(new String[] {}, Path.decompose(null));
		assertArrayEquals(new String[] {}, Path.decompose(""));
		assertArrayEquals(new String[] {}, Path.decompose("   "));
		assertArrayEquals(new String[] { "a" }, Path.decompose("a"));
		assertArrayEquals(new String[] { "a" }, Path.decompose("/a"));
		assertArrayEquals(new String[] { "a" }, Path.decompose("   a   "));
		assertArrayEquals(new String[] { "a" }, Path.decompose("   /    a    "));
		assertArrayEquals(new String[] {
			"a",
			"BBB",
			"Ccc" }, Path.decompose("a/BBB/Ccc"));
		assertArrayEquals(new String[] {
			"a",
			"BBB",
			"Ccc" }, Path.decompose("/a/BBB//Ccc"));
		assertArrayEquals(new String[] {
			"a",
			"BBB",
			"Ccc" }, Path.decompose("   ///   //  /a/ BBB //  Ccc    "));
	}

	@Test
	public void normalize() {
		assertEquals("", Path.normalize(null));
		assertEquals("", Path.normalize(""));
		assertEquals("", Path.normalize("   "));
		assertEquals("a", Path.normalize("   a"));
		assertEquals("a", Path.normalize(" /  a"));
		assertEquals("a/BB", Path.normalize(" /  a /// /    /   BB  "));
		assertEquals("a/B/C DE", Path.normalize(" a/B/C DE//////"));
	}

	@Test
	public void isRoot() {
		assertTrue(Path.isRoot(null));
		assertTrue(Path.isRoot(""));
		assertTrue(Path.isRoot("  /   /   /"));
		assertFalse(Path.isRoot("  / a  /   /"));
		assertFalse(Path.isRoot("/a/b/"));
		assertFalse(Path.isRoot("/a/b"));
		assertFalse(Path.isRoot("/a"));
		assertTrue(Path.isRoot("/"));
	}

	@Test
	public void getParent() {
		assertEquals(null, Path.getParent(null));
		assertEquals(null, Path.getParent(""));
		assertEquals(null, Path.getParent("     "));
		assertEquals(null, Path.getParent("  /   /   "));
		assertEquals("", Path.getParent("  / a  /   "));
		assertEquals("b", Path.getParent(" b / a  /   "));
		assertEquals("b/a/CC", Path.getParent(" b / a  /   CC/qwerty"));
	}
}
