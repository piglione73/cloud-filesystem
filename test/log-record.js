"use strict";

var assert = require("assert");
var LogRecord = require("../src/log-record.js");

describe("Log record", function() {
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

	it("must serialize/deserialize/apply DirectoryAddEntry", function() {
		var r = LogRecord.createEntry_AddEntry("File1.txt", "F", 57);
		testSerialization(r);
		var buf = new Buffer("");
		var buf2 = r.applyToDirectoryNode(buf);
		assert.equal(buf2.toString(), "F57|File1.txt", "The buffer must be modified");

		r = LogRecord.createEntry_AddEntry("Dir1", "D", 58);
		testSerialization(r);
		var buf = buf2;
		var buf2 = r.applyToDirectoryNode(buf);
		assert.equal(buf2.toString(), "F57|File1.txt\nD58|Dir1", "The buffer must be modified");

		r = LogRecord.createEntry_AddEntry("File1.txt", "F", 58);
		testSerialization(r);
		var buf = buf2;
		var buf2 = r.applyToDirectoryNode(buf);
		assert.equal(buf2.toString(), "F58|File1.txt\nD58|Dir1", "The buffer must be modified");
	});

	it("must serialize/deserialize/apply DirectoryTouchEntry", function() {
		var r = LogRecord.createEntry_AddEntry("File1.txt", "F", 57);
		testSerialization(r);
		var buf = new Buffer("");
		var buf2 = r.applyToDirectoryNode(buf);
		assert.equal(buf2.toString(), "F57|File1.txt", "The buffer must be modified");

		r = LogRecord.createEntry_AddEntry("Dir1", "D", 58);
		testSerialization(r);
		var buf = buf2;
		var buf2 = r.applyToDirectoryNode(buf);
		assert.equal(buf2.toString(), "F57|File1.txt\nD58|Dir1", "The buffer must be modified");

		r = LogRecord.createEntry_TouchEntry("File1.txt");
		testSerialization(r);
		var buf = buf2;
		var buf2 = r.applyToDirectoryNode(buf);
		assert.equal(buf2.toString(), "F57|File1.txt\nD58|Dir1", "The buffer must be modified");
	});

	it("must serialize/deserialize/apply DirectoryRemoveEntry", function() {
		var r = LogRecord.createEntry_AddEntry("File1.txt", "F", 57);
		testSerialization(r);
		var buf = new Buffer("");
		var buf2 = r.applyToDirectoryNode(buf);
		assert.equal(buf2.toString(), "F57|File1.txt", "The buffer must be modified");

		r = LogRecord.createEntry_AddEntry("Dir1", "D", 58);
		testSerialization(r);
		var buf = buf2;
		var buf2 = r.applyToDirectoryNode(buf);
		assert.equal(buf2.toString(), "F57|File1.txt\nD58|Dir1", "The buffer must be modified");

		r = LogRecord.createEntry_RemoveEntry("File1.txt");
		testSerialization(r);
		var buf = buf2;
		var buf2 = r.applyToDirectoryNode(buf);
		assert.equal(buf2.toString(), "D58|Dir1", "The buffer must be modified");

		r = LogRecord.createEntry_RemoveEntry("Dir1");
		testSerialization(r);
		var buf = buf2;
		var buf2 = r.applyToDirectoryNode(buf);
		assert.equal(buf2.toString(), "", "The buffer must be modified");
	});

	it("must serialize/deserialize/apply DirectoryRenameEntry", function() {
		var r = LogRecord.createEntry_AddEntry("File1.txt", "F", 57);
		testSerialization(r);
		var buf = new Buffer("");
		var buf2 = r.applyToDirectoryNode(buf);
		assert.equal(buf2.toString(), "F57|File1.txt", "The buffer must be modified");

		r = LogRecord.createEntry_AddEntry("Dir1", "D", 58);
		testSerialization(r);
		var buf = buf2;
		var buf2 = r.applyToDirectoryNode(buf);
		assert.equal(buf2.toString(), "F57|File1.txt\nD58|Dir1", "The buffer must be modified");

		r = LogRecord.createEntry_RenameEntry("File1.txt", "File2.txt");
		testSerialization(r);
		var buf = buf2;
		var buf2 = r.applyToDirectoryNode(buf);
		assert.equal(buf2.toString(), "F57|File2.txt\nD58|Dir1", "The buffer must be modified");
	});
});


function testSerialization(rec) {
	var ser = rec.toBuffer();
	var deser = LogRecord.fromBuffer(ser);
	assert.deepStrictEqual(deser, rec, "Serialization must preserve the LogRecord");
}
