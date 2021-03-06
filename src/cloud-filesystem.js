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
						if(err) {
							callback(err);
							return;
						}
						
						//Directory created in the listing. Let's proceed to next path part.
						currentNodeNumber = newNodeNumber;
						nextPart();
						return;
					});
				}
			});
		}
	}
	
	rename(path, newName, callback) {
		var self = this;
		
		//Get the parent path
		var parentPath = PathUtils.getParent(path);
		var entryName = PathUtils.getLastPart(path);
		
		//Get the node number associated to the given parent path
		NodeUtils.getNodeNumber(self.store, "D", parentPath, (err, nodeNumber) => {
			if(err) {
				callback(err);
				return;
			}
			
			//Write a log record that renames "entryName" to "newName"
			var logRecord = LogRecord.createEntry_RenameEntry(entryName, newName);
			writeLowLevel(self.store, nodeNumber, logRecord.toBuffer(), err => {
				if(err) {
					callback(err);
					return;
				}
				
				//End
				callback();
			});
		});
	}
	
	move(path, newContainerPath, callback) {
		var self = this;
		
		//Get the parent path
		var parentPath = PathUtils.getParent(path);
		var entryName = PathUtils.getLastPart(path);
		
		//Get the node number associated to the given parent path
		NodeUtils.getNodeNumber(self.store, "D", parentPath, (err, nodeNumber) => {
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
				var entry = list.find(x => x.entryName == entryName);
				if(!entry) {
					//Not found. End.
					callback();
					return;
				}
				
				//Get the node number associated to the given new container path
				NodeUtils.getNodeNumber(self.store, "D", newContainerPath, (err, destNodeNumber) => {
					if(err) {
						callback(err);
						return;
					}

					//Write a log record that adds the new entry to destNodeNumber
					var logRecord = LogRecord.createEntry_AddEntry(entry.entryName, entry.entryType, entry.nodeNumber);
					writeLowLevel(self.store, destNodeNumber, logRecord.toBuffer(), err => {
						if(err) {
							callback(err);
							return;
						}
						
						//Write a log record that removes the old entry from nodeNumber
						var logRecord = LogRecord.createEntry_RemoveEntry(entry.entryName);
						writeLowLevel(self.store, nodeNumber, logRecord.toBuffer(), err => {
							if(err) {
								callback(err);
								return;
							}
						
							//End
							callback();
						});
					});
				});
			});
		});
	}

	remove(path, callback) {
		var self = this;
		
		//Get the parent path
		var parentPath = PathUtils.getParent(path);
		var entryName = PathUtils.getLastPart(path);
		
		//Get the node number associated to the given parent path
		NodeUtils.getNodeNumber(self.store, "D", parentPath, (err, nodeNumber) => {
			if(err) {
				callback(err);
				return;
			}
			
			//Write a log record that removes "entryName" from nodeNumber
			var logRecord = LogRecord.createEntry_RemoveEntry(entryName, "D");
			writeLowLevel(self.store, nodeNumber, logRecord.toBuffer(), err => {
				if(err) {
					callback(err);
					return;
				}
				
				//End
				callback();
			});
		});
	}
	
	openFile(path, mode, callback) {
		var self = this;
		
		//Get the parent path
		var parentPath = PathUtils.getParent(path);
		var entryName = PathUtils.getLastPart(path);
		
		//Get the node number associated to the given parent path
		NodeUtils.getNodeNumber(self.store, "D", parentPath, (err, nodeNumber) => {
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
				var entry = list.find(x => x.entryName == entryName);
				
				if(entry) {
					//Found. Open it.
					callback(null, { nodeNumber: x.nodeNumber });
					return;
				}
				else {
					//Not found. Create a new node
					var newNodeNumber = NodeUtils.createUniqueNodeNumber();

					//Add the new directory entry to the listing
					var logRecord = LogRecord.createEntry_AddEntry(entryName, "F", newNodeNumber);
					writeLowLevel(self.store, nodeNumber, logRecord.toBuffer(), err => {
						if(err) {
							callback(err);
							return;
						}
						
						//Directory entry created in the listing. Let's now open the file
						callback(null, { nodeNumber: newNodeNumber });
						return;
					});
				}
			});
		});
	}
	
	closeFile(fd, callback) {
		delete fd.nodeNumber;
		callback();
	}
	
	writeFile(fd, destOffset, buffer, callback) {
		if(fd.nodeNumber === null || fd.nodeNumber === undefined) {
			callback(new Error("The file is closed."));
			return;
		}
		
		//Add a log record that writes the bytes
		var logRecord = LogRecord.createEntry_WriteBytes(buffer, destOffset);
		writeLowLevel(this.store, fd.nodeNumber, logRecord.toBuffer(), err => {
			if(err) {
				callback(err);
				return;
			}

			callback();
		});
	}
	
	readFile(fd, startOffset, length, callback) {
		if(fd.nodeNumber === null || fd.nodeNumber === undefined) {
			callback(new Error("The file is closed."));
			return;
		}
		
		//Read the node
		NodeUtils.readNode(this.store, fd.nodeNumber, (err, node) => {
			if(err) {
				callback(err);
				return;
			}
			
			//Extract from startOffset
			var data = node.slice(startOffset || 0, length > 0? startOffset + length : node.length);
			callback(null, data);
		});
	}
	
}


module.exports = CloudFS;
var NodeUtils = require("./node-utils.js");
var PathUtils = require("./path-utils.js");
var LogRecord = require("./log-record.js");
var writeLowLevel = require("./low-level-writer.js");
