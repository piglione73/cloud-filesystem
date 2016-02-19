"use strict";



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
				callback(null, list.map(x => {
					return {
						name: x.entryName,
						type: x.entryType,
						isFile: x.entryType == "F",
						isDirectory: x.entryType == "D"
					};
				}));				
			});
		});
	}
	
	createDirectory(path, callback) {
		var self = this;
		
		/*
		Start from root (node "0") and ensure all path parts exist.
		throw new Error("TODO");
		*/
		var pathParts = PathUtils.splitPath(path);
		var currentNodeNumber = "0";
		
		nextPart();
		
		function nextPart() {
			var currentPathPart = pathParts.shift();
			if(!currentPathPart){
				//End
				callback();
				return;
			}
			
			//Ensure that directory currentPathPart exists in directory currentNodeNumber
			NodeUtils.readNode(self.store, currentNodeNumber, (err, node) => {
				if(err) {
					callback(err);
					return;
				}
				
				var list = NodeUtils.parseDirectoryNode(node);
				var entry = list.find(x => x.name == currentPathPart && x.type == "D");
				if(entry) {
					//Exists! Let's just proceed to next path part
					currentNodeNumber = entry.nodeNumber;
					nextPart();
					return;
				}
				else {
					//Not found. Create a new node
					var newNodeNumber = NodeUtils.createUniqueNodeNumber();

					//Add the new directory entry to the listing
					var logRecord = LogRecord.createEntry_AddEntry(currentPathPart, "D", newNodeNumber);
					writeLowLevel(self.store, currentNodeNumber, logRecord.toBuffer(), err => {
						//Directory created in the listing. Let's proceed to next path part.
						currentNodeNumber = newNodeNumber;
						nextPart();
						return;
					});
				}
			});
		}
	}
	
	renameDirectory(path, newName, callback) {
		throw new Error("TODO");
	}
	
	moveDirectory(path, newPath, callback) {
		throw new Error("TODO");
	}

	removeDirectory(path, callback) {
		throw new Error("TODO");
	}
	
	openFile(path, mode, callback) {
		throw new Error("TODO");
	}
	
	closeFile(fd, callback) {
		throw new Error("TODO");
	}
	
	writeFile(fd, destOffset, buffer, callback) {
		throw new Error("TODO");
	} 
	
	readFile(fd, startOffset, length, callback) {
		throw new Error("TODO");
	}
	
}


module.exports = CloudFS;
var NodeUtils = require("./node-utils.js");
var PathUtils = require("./path-utils.js");
var LogRecord = require("./log-record.js");
var writeLowLevel = require("./low-level-writer.js");
