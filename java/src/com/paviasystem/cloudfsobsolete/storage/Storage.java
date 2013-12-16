package com.paviasystem.cloudfsobsolete.storage;

public interface Storage {

	/**
	 * Returns true if the given key exists.
	 * 
	 * @param key
	 * @return
	 */
	boolean existsKey(String key);

}
