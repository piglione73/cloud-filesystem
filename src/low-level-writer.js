"use strict";

var randomstring = require("randomstring");

function write(store, key, bytes, callback) {
	/*
	Write a new log record, ensuring consistency. No two simultaneous writes must conflict or be lost. They must be consistently and reliably
	serialized.

	Step 1: Get the highest log record index (L) ever written for this key, with its associated random id (R). This becomes our "previous log record".
	*/
	var previousLogRec = store.getHighestLogRecord(key);

	//Step 2: prepare the new log record index (L2) and id (R2)
	var L2 = L + 1;
	var R2 = randomstring.generate(10);

	//Step 3: write the bytes into the store
	store.writeBytes(key + "|" + L2 + "|" + R2, bytes);

	//Step 4: consistently update the highest log record index
	store.setHighestLogRecordIndex(key, L, L2, R2);
}
