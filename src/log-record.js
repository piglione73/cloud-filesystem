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
	
	static createEntry_AddEntry(entryName, entryType, nodeNumber, isoTimestamp) {
		var rec = new LogRecord();
		rec.type = "DAE";
		rec.entryName = entryName;
		rec.entryType = entryType;
		rec.nodeNumber = nodeNumber;
		rec.isoTimestamp = isoTimestamp;
		return rec;
	}
	
	static createEntry_TouchEntry() {
		var rec = new LogRecord();
		rec.type = "DTE";
	}
	
	static createEntry_RemoveEntry() {
		var rec = new LogRecord();
		rec.type = "DRE";
	}
	
	toBuffer() {
		var json = JSON.stringify({
			t: this.type,
			b: this.bytes,
			l: this.length,
			to: this.targetOffset,
			en: this.entryName,
			et: this.entryType,
			nn: this.nodeNumber,
			ts: this.isoTimestamp
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
		if(obj.ts !== undefined)
			rec.isoTimestamp = obj.ts;
			
		return rec;
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

function formatEntry(rec) {
	return rec.entryType + rec.nodeNumber.toString() + "|" + rec.isoTimestamp + "|" + rec.entryName;
}

function applyAddEntry(buf) {
	var lines = buf.toString().split("\n");
	var found = false;
	for(var i = 0; i < lines.length; i++) {
		//Line example: F58|20150115T11:12:23.456|File1.txt
		var line = lines[i];
		var parts = line.split("|");
		var entryType = line.substring(0, 1);
		var entryName = parts[2];
		if(this.entryType == entryType && this.entryName == entryName) {
			//Found: change it
			lines[i] = formatEntry(this);
			found = true;
			break;
		}
	}
	
	if(!found)
		lines.push(formatEntry(this));
		
	return new Buffer(lines.join("\n"));
}


module.exports = LogRecord;
