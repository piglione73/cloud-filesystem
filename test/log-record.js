"use strict";

var assert = require("assert");
var LogRecord = require("../src/log-record.js");

describe.only("Log record", function() {
	it("must serialize/deserialize/apply FileSetLength", function() {
		var r = LogRecord.createEntry_SetLength(10);
		testSerialization(r);
		
		var buf = new Buffer("1234567890ABCDEFG");
		var buf2 = r.applyToFileNode(buf);
		assert.ok(buf2.equals(new Buffer("1234567890")), "The buffer must be truncated");
		
		buf = new Buffer("123");
		buf2 = r.applyToFileNode(buf);
		assert.ok(buf2.equals(new Buffer("123\u0000\u0000\u0000\u0000\u0000\u0000\u0000")), "The buffer must be padded with zeros");
	});

	it("must serialize/deserialize/apply FileWriteBytes", function() {
		var r = LogRecord.createEntry_WriteBytes(new Buffer("Hello"), 10);
		testSerialization(r);
		
		var buf = new Buffer("1234567890ABCDEFG");
		var buf2 = r.applyToFileNode(buf);
		assert.ok(buf2.equals(new Buffer("1234567890HelloFG")), "The buffer must be modified");
		
		buf = new Buffer("123");
		buf2 = r.applyToFileNode(buf);
		assert.ok(buf2.equals(new Buffer("123\u0000\u0000\u0000\u0000\u0000\u0000\u0000Hello")), "The buffer must be padded with zeros and modified");
	});

});


function testSerialization(rec) {
	var ser = rec.toBuffer();
	var deser = LogRecord.fromBuffer(ser);
	assert.deepStrictEqual(deser, rec, "Serialization must preserve the LogRecord");
}
