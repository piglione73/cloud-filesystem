"use strict";


class NodeUtils {

	static createUniqueNodeNumber() {
		return randomstring.generate({
			length: 20,
			charset: 'hex'
		});
	}
	
	static getNodeNumber(store, nodeType, path, callback) {
		if(nodeType != "F" && nodeType != "D") {
			callback(new Error("The node type must be F or D."));
			return;
		}
		
		//The first N-1 parts are directories, the last part is nodeType
		var pathParts = PathUtils.splitPath(path);
		
		//Exceptions
		if(pathParts.length == 0) {
			if(nodeType == "F") {
				callback(new Error("Cannot determine the node number: 'root file' was required, but only the 'root directory' exists in a filesystem."));
				return;
			}
			else {
				//Root directory: the node number is "0"
				callback(null, "0");
				return;
			}
		}

		//Start the search from the root directory listing
		var nodeNumber = "0";
		
		//Search recursively
		search();
		
		function search() {
			if(pathParts.length == 0) {
				//Search terminated
				callback(null, nodeNumber);
				return;
			}
			
			//Read the directory listing
			NodeUtils.readNode(store, nodeNumber, (err, node) => {
				if(err) {
					callback(err);
					return;
				}
				
				var list = NodeUtils.parseDirectoryNode(node);

				//Look for pathParts[0] in list
				var partType = pathParts.length == 1? nodeType : "D";
				var partName = pathParts[0];
				var entry = list.find(x => x.entryName == partName && x.entryType == partType);
				
				if(entry) {
					//Found. Proceed to next level recursively
					nodeNumber = entry.nodeNumber;
					pathParts.shift();
					search();
				}
				else {
					//Not found
					callback(null, null);
					return;
				}
			});
		}
	}
	
	static readNode(store, nodeNumber, callback) {
		//Read the base and the log records
		//{ base: Buffer, logRecs: [ Buffer, Buffer, ... ] }
		readLowLevel(store, nodeNumber, (err, data) => {
			if(err) {
				callback(err);
				return;
			}
			
			//Combine the base and the log records
			var bytes = data.base || new Buffer(0);
			data.logRecs.forEach(logRecBuf => {
				var logRec = LogRecord.fromBuffer(logRecBuf);
				bytes = logRec.applyToNode(bytes);
			});
			
			//At the end, we have the up-to-date situation
			callback(null, bytes);
		});
	}
	
	static parseDirectoryNode(buf) {
		var str = buf.toString();
		var lines = str ? buf.toString().split("\n") : [];
		var entries = lines.map(parseEntry);
		return entries;
		
		function parseEntry(line) {
			//Line example: F58|20150115T11:12:23.456|File1.txt
			var parts = line.split("|");
			var entryType = line.substring(0, 1);
			var entryName = parts[2];
			var nodeNumber = parts[0].substring(1);
			var isoTimestamp = parts[1];
			
			return { entryType, entryName, nodeNumber, isoTimestamp };
		}
	}


	
}


module.exports = NodeUtils;

var readLowLevel = require("./low-level-reader.js");
var PathUtils = require("./path-utils.js");
var randomstring = require("randomstring");
var LogRecord = require("./log-record.js");

