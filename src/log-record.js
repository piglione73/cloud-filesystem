"use strict";



class LogRecord {

	static createEntry_SetLength(newLength) {
		var rec = new LogRecord();
		rec.type = "FSL";
		rec.length = newLength;
		return rec;
	}
	
	static createEntry_WriteBytes(bytes, targetOffset) {
		var rec = new LogRecord();
		rec.type = "FWB";
		rec.bytes = bytes;
		rec.targetOffset = targetOffset;
		return rec;
	}
	
	static createEntry_AddEntry(entryName, entryType, nodeNumber) {
		var rec = new LogRecord();
		rec.type = "DAE";
		rec.entryName = entryName;
		rec.entryType = entryType;
		rec.nodeNumber = nodeNumber;
		return rec;
	}
	
	static createEntry_TouchEntry(entryName, entryType) {
		var rec = new LogRecord();
		rec.type = "DTE";
		rec.entryName = entryName;
		rec.entryType = entryType;
		return rec;
	}
	
	static createEntry_RemoveEntry(entryName, entryType) {
		var rec = new LogRecord();
		rec.type = "DRE";
		rec.entryName = entryName;
		rec.entryType = entryType;
		return rec;
	}
	
	toBuffer() {
		var json = JSON.stringify({
			t: this.type,
			b: this.bytes,
			l: this.length,
			to: this.targetOffset,
			en: this.entryName,
			et: this.entryType,
			nn: this.nodeNumber
		});
		
		return new Buffer(json);
	}
	
	static fromBuffer(buffer) {
		var json = buffer.toString();
		var obj = JSON.parse(json);
		
		var rec = new LogRecord();
		rec.type = obj.t;
		if(obj.l !== undefined)
			rec.length = obj.l;
		if(obj.to !== undefined)
			rec.targetOffset = obj.to;
		if(obj.b !== undefined)
			rec.bytes = new Buffer(obj.b.data);
		if(obj.en !== undefined)
			rec.entryName = obj.en;
		if(obj.et !== undefined)
			rec.entryType = obj.et;
		if(obj.nn !== undefined)
			rec.nodeNumber = obj.nn;
			
		return rec;
	}
	
	applyToNode(buf) {
		if(this.type == "FSL")
			return applySetLen.call(this, buf);
		else if(this.type == "FWB")
			return applyWriteBytes.call(this, buf);
		else if(this.type == "DAE")
			return applyAddEntry.call(this, buf);
		else if(this.type == "DTE")
			return applyTouchEntry.call(this, buf);
		else if(this.type == "DRE")
			return applyRemoveEntry.call(this, buf);
		else
			throw new Error("Invalid log record type: " + JSON.stringify(this));
	}
	
	applyToFileNode(buf) {
		if(this.type == "FSL")
			return applySetLen.call(this, buf);
		else if(this.type == "FWB")
			return applyWriteBytes.call(this, buf);
		else
			throw new Error("Invalid log record for a file node: " + JSON.stringify(this));
	}
	
	applyToDirectoryNode(buf) {
		if(this.type == "DAE")
			return applyAddEntry.call(this, buf);
		else if(this.type == "DTE")
			return applyTouchEntry.call(this, buf);
		else if(this.type == "DRE")
			return applyRemoveEntry.call(this, buf);
		else
			throw new Error("Invalid log record for a directory node: " + JSON.stringify(this));
	}
	
}

function applySetLen(buf) {
	if(this.length < 0)
		throw new Error("Invalid log record: " + JSON.stringify(this));
	else if(this.length <= buf.length)
		return buf.slice(0, this.length);
	else {
		//Expand and pad
		var newBuf = new Buffer(this.length);
		buf.copy(newBuf);
		newBuf.fill(0, buf.length);
		return newBuf;
	}
}

function applyWriteBytes(buf) {
	var newLen = Math.max(buf.length, this.targetOffset + this.bytes.length);
	var newBuf = new Buffer(newLen);
	buf.copy(newBuf);
	newBuf.fill(0, buf.length);
	this.bytes.copy(newBuf, this.targetOffset);
	return newBuf;
}


function formatEntry(x) {
	return x.entryType + x.nodeNumber.toString() + "|" + x.entryName;
}

function parseDirNode(buf) {
	return NodeUtils.parseDirectoryNode(buf);
}

function formatDirNode(entries) {
	var lines = entries.map(formatEntry);
	var str = lines.join("\n");
	return new Buffer(str);
}


function applyAddEntry(buf) {
	var entries = parseDirNode(buf);
	var index = entries.findIndex(x => x.entryName == this.entryName);
	if(index == -1) {
		//Add
		entries.push({
			entryName: this.entryName,
			entryType: this.entryType,
			nodeNumber: this.nodeNumber,
			isoTimestamp: this.isoTimestamp
		});
	}
	else {
		//Modify
		var e = entries[index];
		e.nodeNumber = this.nodeNumber;
		e.entryType = this.entryType;
	}
	
	return formatDirNode(entries);
}

function applyRemoveEntry(buf) {
	var entries = parseDirNode(buf);
	var index = entries.findIndex(x => x.entryName == this.entryName);
	if(index != -1) {
		//Remove
		entries.splice(index, 1);
	}
	
	return formatDirNode(entries);
}

function applyTouchEntry(buf) {
	var entries = parseDirNode(buf);
	
	//No operation
	return formatDirNode(entries);
}


module.exports = LogRecord;
var NodeUtils = require("./node-utils.js");
