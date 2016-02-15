function write(store, key, buffer, callback) {
    /*
    Write a new log record, ensuring consistency. No two simultaneous writes must conflict. They must be consistently and reliably
    serialized.

    Step 1: Get the highest log record index (L) ever written for this key, with its associated random id (R).
    */
    var { L, R } = store.getHighestLogRecordIndex(key);
	
	//Step 2: prepare the new log record index (L2) and id (R2)
	var L2 = L + 1;
	var R2 = utils.randomString(10);
	
	//Step 3: write the bytes into the store
	store.writeBlob(key + "|" + L2 + "|" + R2, buffer);
	
	//Step 4: consistently update the highest log record index
	store.setHighestLogRecordIndex(key, L, L2, R2);
}
