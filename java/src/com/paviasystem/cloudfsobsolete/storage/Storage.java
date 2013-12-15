package com.paviasystem.cloudfs.storage;

public interface Storage {

	/**
	 * Returns true if the given key exists.
	 * 
	 * @param key
	 * @return
	 */
	boolean existsKey(String key);

}
