package com.paviasystem.cloudfilesystem;

import java.util.ArrayList;
import java.util.Arrays;

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
}
