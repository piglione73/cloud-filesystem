"use strict";

var assert = require("assert");
var write = require("../src/low-level-writer.js");
var Data = require("../src/low-level-data.js");
var StoreBase = require("../src/store-base.js");
var StoreAWS = require("../src/store-aws.js");


describe("StoreBase", test(() => new StoreBase()));
describe("StoreAWS", test(() => new StoreAWS()));


function test(storeSupplier) {
	return function() {
		var store = storeSupplier();
		testWriteConsistently(store);
	};
}

function testWriteConsistently(store) {
    it("must write consistently", function() {
		write(store, "AAA", new Buffer("Hello"), function(status) {
			assert.equal(status, StoreBase.OK);
			
			store.getLogInfo("AAA", function(status, index1, id1) {
				assert.equal(status, StoreBase.OK);
				assert.equal(index1, 1);
				assert.equal(id1.length, 10);
				
				write(store, "AAA", new Buffer("Hello 2"), function(status) {
					assert.equal(status, StoreBase.OK);
					
					store.getLogInfo("AAA", function(status, index2, id2) {
						assert.equal(status, StoreBase.OK);
						assert.equal(index2, 2);
						assert.equal(id2.length, 10);
						
						//Now let's check the bytes
						store.getBytes("AAA|" + index1 + "|" + id1, function(status, bytes) {
							assert.equal(status, StoreBase.OK);
							var data = Data.fromBuffer(bytes);
							assert.strictEqual(data.previousLogIndex, null);
							assert.strictEqual(data.previousLogID, null);
							assert.ok(data.bytes.equals(new Buffer("Hello")));
							
							store.getBytes("AAA|" + index2 + "|" + id2, function(status, bytes) {
								assert.equal(status, StoreBase.OK);
								var data = Data.fromBuffer(bytes);
								
								assert.strictEqual(data.previousLogIndex, index1);
								assert.strictEqual(data.previousLogID, id1);
								assert.ok(data.bytes.equals(new Buffer("Hello 2")));
							});
						});
					});
				});
			});
		});
    });
}