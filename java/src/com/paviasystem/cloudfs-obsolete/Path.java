package com.paviasystem.cloudfs;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

/**
 * Utilities for path manipulation.
 */
public final class Path {
	private static final String[] emptyPath = new String[0];

	private Path() {
	}

	/**
	 * Normalizes a path: from "A/B/ /C/" to "A/B/C". From "/A/B" to "A/B". From
	 * "/" to "". A normalized path never starts with a slash and never ends
	 * with a slash. Its parts contain no leading/trailing blanks.
	 * 
	 * @param path
	 * @return
	 */
	public static String normalize(String path) {
		return compose(decompose(path));
	}

	/**
	 * Decomposes a path into its parts. Blank parts are ignored. Example: from
	 * "/A/  B/  /C/" to ["A", "B", "C"].
	 * 
	 * @param path
	 * @return
	 */
	public static String[] decompose(String path) {
		if (path == null)
			return emptyPath;

		String[] parts = path.split("/");
		ArrayList<String> ret = new ArrayList<String>();
		for (String part : parts) {
			String x = part.trim();
			if (x.isEmpty())
				continue;

			ret.add(x);
		}

		String[] arr = new String[ret.size()];
		return ret.toArray(arr);
	}

	/**
	 * Composes a path from its parts. The resulting path is in a normalized
	 * form.
	 * 
	 * @param parts
	 * @return
	 */
	public static String compose(String[] parts) {
		if (parts == null)
			return "";

		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String part : parts) {
			String x = part;
			if (x != null) {
				x = x.trim();
				if (!x.isEmpty()) {
					if (!first)
						sb.append('/');

					sb.append(x);
					first = false;
				}
			}
		}

		return sb.toString();
	}

	/**
	 * Determines if a directory is the root directory.
	 * 
	 * @param directory
	 * @return
	 */
	public static boolean isRoot(String directory) {
		return decompose(directory).length == 0;
	}

	/**
	 * Determines the parent path.
	 * 
	 * @param path
	 * @return The parent path, or null if the path has no parent (i.e., if it
	 *         is the root directory)
	 */
	public static String getParent(String path) {
		if (path == null)
			return null;

		String[] parts = decompose(path);
		if (parts.length == 0) {
			// It is root --> no parent
			return null;
		} else {
			// Non-root --> the parent is calculated by dropping the last part
			String[] parentParts = Arrays.copyOf(parts, parts.length - 1);
			return compose(parentParts);
		}
	}

	public static class UnitTests {
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

}
