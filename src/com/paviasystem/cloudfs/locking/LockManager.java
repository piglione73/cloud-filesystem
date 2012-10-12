package com.paviasystem.cloudfs.locking;

import java.util.Date;

public interface LockManager {
	String lock(String resource, long offset, long len, Date expiration);

	void unlock(String lockID);

	void refreshLock(String lockID, Date expiration);

}
