"use strict";


function write(store, key, bytes, callback) {
	/*
	Write a new log record, ensuring consistency. No two simultaneous writes must conflict or be lost. 
	They must be consistently and reliably serialized.
	*/
	start();
	
	function start() {
		/*
		Get the highest log record index ever written for this key, with its associated random id. 
		This becomes our "existing log record".
		*/
		store.getLogInfo(key, onLogInfoReceived());
	}

	function onLogInfoReceived() {
		return function(status, existingIndex, existingID) {
			if(status == RetCodes.OK) {
				//We have the existing log record info, so we must consistently update it
				consistentUpdate(existingIndex, existingID);
			}
			else if(status == RetCodes.NotFound) {
				//No log record has been written for this key yet, so we must consistently insert a new one
				consistentInsert();				
			}
			else {
				//Other error
				callback(status);
			}			
		};
	}
	
	function getLogKey(index, id) {
		return key + "|" + index + "|" + id;
	}
	
	function consistentInsert() {
		/*
		In case we are in the "insert" scenario, we write a new log record with index 1.
		First the bytes, then the record.
		*/
		step1();
		
		function step1() {
			var newID = randomstring.generate(10);
			var newIndex = 1;
			store.setBytes(getLogKey(newIndex, newID), new Data(bytes).toBuffer(), step2(newIndex, newID));
		}
		
		function step2(newIndex, newID) {
			return function(status) {
				if(status == RetCodes.OK) {
					//Bytes written, now write the log record, hoping nobody else has written a log record meanwhile
					store.insertLogInfo(key, newIndex, newID, step3());
				}
				else {
					//Other error
					callback(status);
				}
			};
		}
		
		function step3() {
			return function(status) {
				if(status == RetCodes.OK) {
					//Log written, we are done
					callback(RetCodes.OK);
				}
				else if(status == RetCodes.AlreadyPresent) {
					//Someone wrote a new log record meanwhile, so we have to retry the whole process
					start();
				}
				else {
					//Other error
					callback(status);
				}
			};
		}
	}
	
	function consistentUpdate(existingIndex, existingID) {
		/*
		In case we are in the "update" scenario, we write a new log record with index existingIndex + 1.
		First the bytes, then the record.
		*/
		step1();
		
		function step1() {
			var newID = randomstring.generate(10);
			var newIndex = existingIndex + 1;
			store.setBytes(getLogKey(newIndex, newID), new Data(bytes, existingIndex, existingID).toBuffer(), step2(newIndex, newID));
		}
		
		function step2(newIndex, newID) {
			return function(status) {
				if(status == RetCodes.OK) {
					//Bytes written, now write the log record, hoping nobody else has written a log record meanwhile
					store.updateLogInfo(key, existingIndex, newIndex, newID, step3());
				}
				else {
					//Other error
					callback(status);
				}
			};
		}
		
		function step3() {
			return function(status) {
				if(status == RetCodes.OK) {
					//Log written, we are done
					callback(RetCodes.OK);
				}
				else if(status == RetCodes.AlreadyPresent) {
					//Someone wrote a new log record meanwhile, so we have to retry the whole process
					start();
				}
				else {
					//Other error
					callback(status);
				}
			};
		}
	}
	
}

module.exports = write;
var randomstring = require("randomstring");
var StoreBase = require("./store-base.js");
var Data = require("./low-level-data.js");
var RetCodes = require("./return-codes.js");

