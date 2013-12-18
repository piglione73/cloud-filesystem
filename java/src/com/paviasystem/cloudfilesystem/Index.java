package com.paviasystem.cloudfilesystem;

import java.util.ArrayList;

public interface Index {

	ArrayList<IndexEntry> list(String absolutePath);

	IndexEntry getEntry(String absolutePath);

}
