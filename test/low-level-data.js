"use strict";

var assert = require("assert");
var Data = require("../src/low-level-data.js");

describe("LowLevelData", function() {
	it("must serialize/deserialize", function() {
		var d = new Data(new Buffer("Hello"), 57, "ABC");
		var serialized = d.toBuffer();
		var deserialized = Data.fromBuffer(serialized);
		
		assert.strictEqual(deserialized.previousLogIndex, 57);
		assert.strictEqual(deserialized.previousLogID, "ABC");
		assert.ok(deserialized.bytes.equals(new Buffer("Hello")));
	});

	it("must serialize/deserialize with missing log info", function() {
		var d = new Data(new Buffer("Hello"));
		var serialized = d.toBuffer();
		var deserialized = Data.fromBuffer(serialized);
		
		assert.strictEqual(deserialized.previousLogIndex, null);
		assert.strictEqual(deserialized.previousLogID, null);
		assert.ok(deserialized.bytes.equals(new Buffer("Hello")));
	});
});
