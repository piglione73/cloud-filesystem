package com.paviasystem.cloudfilesystem.test.index;

import com.paviasystem.cloudfilesystem.referenceimpl.MemoryIndex;

public class MemoryIndexTest extends GenericIndexTest {
	public MemoryIndexTest() {
		index = new MemoryIndex();
	}
}
