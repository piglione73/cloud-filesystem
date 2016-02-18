"use strict";

var NodeUtils = require("./node-utils.js");


class CloudFS {

	constructor(store) {
		this.store = store;
	}
	
	discardAll(callback) {
		/*
		Simply wipe the root directory node (0)
		*/
		this.store.cleanLogInfo("0", err => {
			if(err) {
				callback(err);
				return;
			}
			
			this.store.setBytes("0", null, callback);
		});
	}
	
	list(path, callback) {
		//Get the node number associated to the given path
		NodeUtils.getNodeNumber(this.store, "D", path, (err, nodeNumber) => {
			if(err) {
				callback(err);
				return;
			}
			
			//Read it
			NodeUtils.readNode(this.store, nodeNumber, (err, node) => {
				if(err) {
					callback(err);
					return;
				}
				
				//Parse it
				var list = NodeUtils.parseDirectoryNode(node);
				
				//Return it
				callback(null, list);				
			});
		});
	}
	
}


module.exports = CloudFS;
