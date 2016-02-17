"use strict";

var StoreBase = require("./store-base.js");
var Data = require("./low-level-data.js");

function read(store, key, callback) {
	/*
	Reads the base version and all the subsequent log records written afterwards.
	
	Calls callback(status, { base: Buffer, logRecs: [ Buffer, Buffer, ... ] }).
	*/
	start();
	
	
	function start() {
		//Let's read the base
		store.getBytes(key, onBaseReceived());
	}
	
	function onBaseReceived() {
		return function(status, buffer) {
			if(status == StoreBase.OK) {
				//Base found, deserialize and proceed with log
				var data = Data.fromBuffer(buffer);
				startReadLog(data.bytes, data.index, data.id);
			}
			else if(status == StoreBase.NotFound) {
				//Base not found, proceed with log
				startReadLog(null, null, null);
			}
			else {
				//Other error
				callback(status);
			}
		};
	}
	
	function startReadLog(base, lowestIndex, lowestID) {
		//First, read the maximum log record
		store.getLogInfo(key, onLogInfoReceived(base, lowestIndex, lowestID));
	}
	
	function onLogInfoReceived(base, lowestIndex, lowestID) {
		return function(status, highestIndex, highestID) {
			if(status == StoreBase.NotFound) {
				//No log record ever written, so we are finished
				callback(StoreBase.OK, { base: base, logRecs: [] });
			}
			else if(status == StoreBase.OK) {
				//At least one log record found. We must read from lowestIndex (excluded) to highestIndex (included)
				//Due to eventual consistency issues, those log records might not be visible yet in the store.
				//However, we are 100% sure they eventually be visible, so we wait until we manage to read them all.
				//We start from the highest and we follow the linked list of log records backwards.
				readLogRecord(base, highestIndex, highestID, lowestIndex, []);
			}
		};
	}

	function getLogKey(index, id) {
		return key + "|" + index + "|" + id;
	}
	
	function readLogRecord(base, index, id, stopIndex, accumulatedLogRecs) {
		//Stop condition
		if(index == stopIndex) {
			//We read all the interesting log records. We are done.
			callback(StoreBase.OK, { base: base, logRecs: accumulatedLogRecs });
			return;
		}
		
		//Let's try hard to read the key/index/id log record, then we move to the previous
		store.getBytes(getLogKey(index, id), function(status, buffer) {
			if(status == StoreBase.NotFound) {
				//Not **YET** found. Try again.
				readLogRecord(base, index, id, stopIndex, accumulatedLogRecs);
				return;
			}
			else if(status == StoreBase.OK) {
				//Found. Deserialize and accumulate.
				var data = Data.fromBuffer(buffer);
				var previousIndex = data.index;
				var previousID = data.id;
				var bytes = data.bytes;
				
				accumulatedLogRecs.unshift(bytes);
				
				//Then proceed to previous
				readLogRecord(base, previousIndex, previousID, stopIndex, accumulatedLogRecs);
			}
			else {
				//Other error
				callback(status);
			}
		});
	}

}

module.exports = read;

