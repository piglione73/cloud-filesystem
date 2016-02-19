"use strict";


class PathUtils {
	
	static splitPath(path) {
		return (path || "").replace(/\\/g, "/").split("/").filter(x => x != "");
	}
	
}


module.exports = PathUtils;
