"use strict";

var assert = require("assert");
var StoreBase = require("../src/store-base.js");
var StoreAWS = require("../src/store-aws.js");
var storeAWSConfig = require("./store-aws-config.json");
var RetCodes = require("../src/return-codes.js");

describe("Data store: StoreBase", test(() => new StoreBase()));
describe("Data store: StoreAWS", test(() => new StoreAWS(storeAWSConfig.bucketName, storeAWSConfig.bucketRegion, storeAWSConfig.bucketPrefix, storeAWSConfig.tableName, storeAWSConfig.tableRegion)));


function test(storeSupplier) {
	return function() {
		var store = storeSupplier();
		testStoreBytes(store);
		
		store = storeSupplier();
		testLog(store);
	};
}

function testStoreBytes(store) {
    it("must store bytes", function(done) {
		this.timeout(10000);
		
		store.setBytes("AAA", null, function(status) {
			assert.equal(status, RetCodes.OK);
			store.setBytes("BBB", null, function(status) {
				assert.equal(status, RetCodes.OK);
						
				store.getBytes("AAA", function(status, bytes) {
					assert.equal(status, RetCodes.NotFound);
					assert.equal(bytes, undefined);

					store.setBytes("AAA", new Buffer("aaa"), function(status) {
						assert.equal(status, RetCodes.OK);

						store.getBytes("AAA", function(status, bytes) {
							assert.equal(status, RetCodes.OK);
							assert.ok(bytes.equals(new Buffer("aaa")));

							store.setBytes("AAA", new Buffer("aaa2"), function(status) {
								assert.equal(status, RetCodes.OK);

								store.getBytes("AAA", function(status, bytes) {
									assert.equal(status, RetCodes.OK);
									assert.ok(bytes.equals(new Buffer("aaa2")));

									store.getBytes("BBB", function(status, bytes) {
										assert.equal(status, RetCodes.NotFound);
										assert.equal(bytes, undefined);
										
										done();
									});
								});
							});
						});
					});
				});
			});
		});
    });
}

function testLog(store) {
    it("must handle log info consistently", function(done) {
		this.timeout(10000);
		
		store.cleanLogInfo("AAA", function(status) {
            assert.equal(status, RetCodes.OK);
		
			store.getLogInfo("AAA", function(status, index, id) {
				assert.equal(status, RetCodes.NotFound);
				assert.equal(index, undefined);
				assert.equal(id, undefined);

				store.updateLogInfo("AAA", 0, 1, "ABC", function(status) {
					assert.equal(status, RetCodes.NotFoundOrIndexDoesNotMatch);

					store.insertLogInfo("AAA", 1, "ABC", function(status) {
						assert.equal(status, RetCodes.OK);

						store.insertLogInfo("AAA", 1, "ABC", function(status) {
							assert.equal(status, RetCodes.AlreadyPresent);

							store.updateLogInfo("AAA", 0, 1, "ABC", function(status) {
								assert.equal(status, RetCodes.NotFoundOrIndexDoesNotMatch);

								store.updateLogInfo("AAA", 1, 2, "CDE", function(status) {
									assert.equal(status, RetCodes.OK);

									store.getLogInfo("AAA", function(status, index, id) {
										assert.equal(status, RetCodes.OK);
										assert.equal(index, 2);
										assert.equal(id, "CDE");
										
										done();
									});
								});
							});
						});
					});
				});
			});
		});
    });
}


