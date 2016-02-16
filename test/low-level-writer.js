"use strict";

var assert = require("assert");
var write = require("../src/low-level-writer.js");
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
			
			store.getLogInfo("AAA", function(status, index, id) {
				assert.equal(status, StoreBase.OK);
				assert.equal(index, 1);
				assert.equal(id.length, 10);
				
				write(store, "AAA", new Buffer("Hello 2"), function(status) {
					assert.equal(status, StoreBase.OK);
					
					store.getLogInfo("AAA", function(status, index, id) {
						assert.equal(status, StoreBase.OK);
						assert.equal(index, 2);
						assert.equal(id.length, 10);
					});
				});
			});
		});
    });
}
