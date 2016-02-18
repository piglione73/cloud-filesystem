"use strict";


class NodeUtils {

	static getNodeNumber(store, nodeType, path, callback) {
		if(nodeType != "F" && nodeType != "D") {
			callback(new Error("The node type must be F or D."));
			return;
		}
		
		//The first N-1 parts are directories, the last part is nodeType
		var pathParts = splitPath(path);
		
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
				var entry = list.find(x => x.name == partName && x.type == partType);
				
				if(entry) {
					//Found. Proceed to next level recursively
					nodeNumber = entry.nodeNumber;
					pathParts.shift();
					search();
				}
				else {
					//Not found
					callback(new Error("Path " + path + " not found."));
					return;
				}
			});
		}
	}
	
}

function splitPath(path) {
	return (path || "").replace(/\\/g, "/").split("/").filter(x => x != "");
}


module.exports = NodeUtils;
