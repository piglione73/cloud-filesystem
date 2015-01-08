package com.paviasystem.cloudfilesystemold.blocks.data;

public class IndexEntry {
	public String key1, key2;
	public String data1, data2, data3, data4, data5;

	public IndexEntry clone() {
		IndexEntry ret = new IndexEntry();
		ret.key1 = key1;
		ret.key2 = key2;
		ret.data1 = data1;
		ret.data2 = data2;
		ret.data3 = data3;
		ret.data4 = data4;
		ret.data5 = data5;
		return ret;
	}
}
