"use strict";


/*
This is the reference implementation of the underlying store. It stores bytes and log information in memory.
*/
class StoreBase {

    constructor() {
        this.store = {};
        this.logInfo = {};
    }

    getBytes(key, callback) {
        /*
        Reads bytes associated to a given key and calls the callback.
    
        The callback is called as callback(status, bytes).
        */
        var bytes = this.store[key];
        if (bytes)
            callback(RetCodes.OK, bytes);
        else
            callback(RetCodes.NotFound);
    }

    setBytes(key, bytes, callback) {
        /*
        Write bytes associated to a given key.

        The callback is called as callback(status).
        */
		if(bytes)
			this.store[key] = bytes;
		else
			delete this.store[key];
			
        callback(RetCodes.OK);
    }

    cleanLogInfo(key, callback) {
        /*
        Removes the log info associated to a given key.

        Calls callback(status).
        */
		delete this.logInfo[key];
        callback(RetCodes.OK);
    }

    getLogInfo(key, callback) {
        /*
        Gets the log info associated to a given key.

        Calls callback(status, index, id).
        */
        var info = this.logInfo[key];
        if (info)
            callback(RetCodes.OK, info.index, info.id);
        else
            callback(RetCodes.NotFound);
    }

    insertLogInfo(key, newIndex, newID, callback) {
        /*
        Consistently insert a log info when it does not exist for the given key.
        The index is set to newIndex and the id is set to newID.

        Calls callback(status).
        */
        this.getLogInfo(key, (status, index, id) => {
            if (status == RetCodes.OK) {
                //The log info exists, so we cannot insert
                callback(RetCodes.AlreadyPresent);
            }
            else if (status == RetCodes.NotFound) {
                //The log info does not exist, so we can insert
                this.logInfo[key] = {
                    index: newIndex,
                    id: newID
                };

                callback(RetCodes.OK);
            }
            else {
                //Other error
                callback(status);
            }
        });
    }

    updateLogInfo(key, existingIndex, newIndex, newID, callback) {
        /*
        Consistently updates the log info associated to a given key. The update is only performed 
        if the index is equal to existingIndex. The index is set to newIndex and the id is set to newID.

        Calls callback(status).
        */
        this.getLogInfo(key, (status, index, id) => {
            if (status == RetCodes.OK) {
                //The log info exists
                if (index == existingIndex) {
                    //Index matches, so we can update
                    this.logInfo[key] = {
                        index: newIndex,
                        id: newID
                    };
                    callback(RetCodes.OK);
                }
                else {
                    //Index does not match
                    callback(RetCodes.NotFoundOrIndexDoesNotMatch);
                }
            }
            else if (status == RetCodes.NotFound) {
                //The log info does not exist, so we cannot update
                callback(RetCodes.NotFoundOrIndexDoesNotMatch);
            }
            else {
                //Other error
                callback(status);
            }
        });
    }
}


module.exports = StoreBase;
var RetCodes = require("./return-codes.js");
