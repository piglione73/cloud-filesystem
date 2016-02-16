"use strict";

var OFFSET_INDEX = 0;
var OFFSET_ID = 16;
var OFFSET_BYTES = 64;

class Data {
	constructor(bytes, previousLogIndex, previousLogID) {
		this.bytes = bytes;
		this.previousLogIndex = previousLogIndex;
		this.previousLogID = previousLogID;
	}
	
	toBuffer() {
		var buf = new Buffer(this.bytes.length + OFFSET_BYTES);
		buf.fill(0, OFFSET_INDEX, OFFSET_BYTES);
		var logInd = this.previousLogIndex !== null && this.previousLogIndex !== undefined? this.previousLogIndex.toString() : "";
		var logID = this.previousLogID || "";
		buf.write(logInd, OFFSET_INDEX);
		buf.write(logID, OFFSET_ID);
		this.bytes.copy(buf, OFFSET_BYTES);
		
		return buf;
	}
	
	static fromBuffer(buffer) {
		var bytes = buffer.slice(OFFSET_BYTES);
		var logInd = buffer.toString(undefined, OFFSET_INDEX, OFFSET_ID - OFFSET_INDEX).replace(/\u0000/g, "");
		var logID = buffer.toString(undefined, OFFSET_ID, OFFSET_BYTES - OFFSET_ID).replace(/\u0000/g, "");
		
		return new Data(bytes, logInd ? parseInt(logInd) : null, logID || null);
	}
}

module.exports = Data;

