"use strict";

var assert = require("assert");
var write = require("../src/low-level-writer.js");
var read = require("../src/low-level-reader.js");
var Data = require("../src/low-level-data.js");
var StoreBase = require("../src/store-base.js");
var StoreAWS = require("../src/store-aws.js");
var storeAWSConfig = require("./store-aws-config.json");


describe("Low-level reader/writer on StoreBase", test(() => new StoreBase()));
describe("Low-level reader/writer on StoreAWS", test(() => new StoreAWS(storeAWSConfig.bucketName, storeAWSConfig.bucketRegion, storeAWSConfig.bucketPrefix, storeAWSConfig.tableName, storeAWSConfig.tableRegion)));


function test(storeSupplier) {
	return function() {
		var store = storeSupplier();
		testWriteConsistently(store);

		store = storeSupplier();
		testReadConsistently(store);
	};
}

function testWriteConsistently(store) {
    it("must write consistently", function(done) {
		this.timeout(10000);
		
		store.setBytes("AAA", null, function(status) {
			assert.equal(status, StoreBase.OK);
			store.cleanLogInfo("AAA", function(status) {
				assert.equal(status, StoreBase.OK);

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
									assert.strictEqual(data.index, null);
									assert.strictEqual(data.id, null);
									assert.ok(data.bytes.equals(new Buffer("Hello")));
									
									store.getBytes("AAA|" + index2 + "|" + id2, function(status, bytes) {
										assert.equal(status, StoreBase.OK);
										var data = Data.fromBuffer(bytes);
										
										assert.strictEqual(data.index, index1);
										assert.strictEqual(data.id, id1);
										assert.ok(data.bytes.equals(new Buffer("Hello 2")));
										
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

function testReadConsistently(store) {
    it("must read consistently", function(done) {
		this.timeout(20000);
		
		//Clean all
		store.setBytes("AAA", null, function(status) {
			assert.equal(status, StoreBase.OK);
			store.cleanLogInfo("AAA", function(status) {
				assert.equal(status, StoreBase.OK);
				
				//Read nothing
				read(store, "AAA", function(status, data) {
					assert.equal(status, StoreBase.OK);
					assert.equal(data.base, null);
					assert.equal(data.logRecs.length, 0);
					
					//Now let's setup some base, saying that log records have been incorporated up to index 3
					var base = new Data(new Buffer("Hello"), 3, "ABCDEFGHJK");
					store.setBytes("AAA", base.toBuffer(), function(status) {
						assert.equal(status, StoreBase.OK);
						
						//If we read back, we will only find the base
						read(store, "AAA", function(status, data) {
							assert.equal(status, StoreBase.OK);
							assert.ok(data.base.equals(new Buffer("Hello")));
							assert.equal(data.logRecs.length, 0);
							
							//Now write 5 log records
							write(store, "AAA", new Buffer("Log 1"), function(status) {
								write(store, "AAA", new Buffer("Log 2"), function(status) {
									write(store, "AAA", new Buffer("Log 3"), function(status) {
										write(store, "AAA", new Buffer("Log 4"), function(status) {
											write(store, "AAA", new Buffer("Log 5"), function(status) {
												//Now read back, we must find the base and two log records (4 and 5)
												read(store, "AAA", function(status, data) {
													assert.equal(status, StoreBase.OK);
													assert.ok(data.base.equals(new Buffer("Hello")));
													assert.equal(data.logRecs.length, 2);
													assert.ok(data.logRecs[0].equals(new Buffer("Log 4")));
													assert.ok(data.logRecs[1].equals(new Buffer("Log 5")));
													
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
			});
		});
	});
}
