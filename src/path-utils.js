"use strict";


class PathUtils {
	
	static joinPath(parts) {
		return parts.join("/");
	}
	
	static splitPath(path) {
		return (path || "").replace(/\\/g, "/").split("/").filter(x => x != "");
	}
	
	static getParent(path) {
		var allParts = PathUtils.splitPath(path);
		allParts.splice(allParts.length - 1, 1);
		return PathUtils.joinPath(allParts);
	}
	
	static getLastPart(path) {
		var allParts = PathUtils.splitPath(path);
		return allParts[allParts.length - 1];
	}
	
}


module.exports = PathUtils;
