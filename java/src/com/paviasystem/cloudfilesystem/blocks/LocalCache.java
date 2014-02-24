package com.paviasystem.cloudfilesystem.blocks;

import java.util.Date;

public interface LocalCache {
	ByteWriter openSequentialWriter(String category, String name);
}
