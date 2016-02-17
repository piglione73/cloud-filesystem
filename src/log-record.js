"use strict";


class LogRecord {

	static createEntry_SetLength(newLength) {
		var rec = new LogRecord();
		rec.t = "FSL";
		rec.l = newLength;
		return rec;
	}
	
	static createEntry_WriteBytes() {
		var rec = new LogRecord();
		rec.t = "FWB";
	}
	
	static createEntry_AddEntry() {
		var rec = new LogRecord();
		rec.t = "DAE";
	}
	
	static createEntry_TouchEntry() {
		var rec = new LogRecord();
		rec.t = "DTE";
	}
	
	static createEntry_RemoveEntry() {
		var rec = new LogRecord();
		rec.t = "DRE";
	}
	
	toBuffer() {
		var json = JSON.stringify(this);
		return new Buffer(json);
	}
	
	static fromBuffer(buffer) {
		var json = buffer.toString();
		var obj = JSON.parse(json);
		
		var rec = new LogRecord();
		rec.t = obj.t;
		if(obj.l !== undefined)
			rec.l = obj.l;
			
		return rec;
	}
	
	applyToFileNode(buf) {
		if(this.t == "FSL") {
			//File Set Length
			if(this.l < 0)
				throw new Error("Invalid log record: " + JSON.stringify(this));
			else if(this.l <= buf.length)
				return buf.slice(0, this.l);
			else {
				//Expand and pad
				var newBuf = new Buffer(this.l);
				buf.copy(newBuf);
				newBuf.fill(0, buf.length);
				return newBuf;
			}
		}
	}
	
	applyToDirectoryNode() {
	}
	
}

module.exports = LogRecord;
