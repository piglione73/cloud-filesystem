"use strict";

var assert = require("assert");

var StoreBase = require("../src/store-base.js");
var StoreAWS = require("../src/store-aws.js");
var CloudFS = require("../src/cloud-filesystem.js");

var storeAWSConfig = require("./store-aws-config.json");


describe("Cloud Filesystem", function() {
	describe("StoreBase", test(() => new StoreBase()));
	describe("StoreAWS", test(() => new StoreAWS(storeAWSConfig.bucketName, storeAWSConfig.bucketRegion, storeAWSConfig.bucketPrefix, storeAWSConfig.tableName, storeAWSConfig.tableRegion)));
});

function test(storeSupplier) {
	return function() {
		var store = storeSupplier();
		var cfs = new CloudFS(store);

		runTests(cfs, store);
	};
}



function runTests(cfs, store) {
	it("must properly discard all data", function(done) {
	    this.timeout(10000);
		cfs.discardAll(err => {
			assert.ifError(err);
			cfs.list("", (err, list) => {
				assert.ifError(err);
				assert.equal(list.length, 0, "The root directory must be empty");
				done();
			});
		});
	});
	
	it("must create directories", runOnCleanFS(done => {
		cfs.createDirectory("dir1", err => {
			assert.ifError(err);
			cfs.list("", (err, list) => {
				assert.ifError(err);
				assert.equal(list.length, 1, "The root directory must contain one dir");
				assert.equal(list[0].name, "dir1");
				assert.ok(!list[0].isFile);
				assert.ok(list[0].isDirectory);

				cfs.createDirectory("dir2", function(err) {
					assert.ifError(err);
					cfs.createDirectory("dir3/dir4", function(err) {
						assert.ifError(err);
						cfs.list("/", (err, list) => {
							assert.ifError(err);
							assert.equal(list.length, 3, "The root directory must contain 3 dirs");
							assert.equal(list[0].name, "dir1");
							assert.ok(list[0].isDirectory);
							assert.equal(list[1].name, "dir2");
							assert.ok(list[1].isDirectory);
							assert.equal(list[2].name, "dir3");
							assert.ok(list[2].isDirectory);
							
							cfs.list("/dir1", (err, list) => {
								assert.ifError(err);
								assert.equal(list.length, 0, "/dir1 must be empty");
							
								cfs.list("/dir3", (err, list) => {
									assert.ifError(err);
									assert.equal(list.length, 1, "/dir3 must contain dir4");
									assert.equal(list[0].name, "dir4");
									assert.ok(list[0].isDirectory);
									
									done();
								});
							});
						});
					});
				});
			});
		});
	}));
	
	it("must rename directories", runOnCleanFS(done => {
		cfs.createDirectory("/a/b/c", err => {
			assert.ifError(err);
			cfs.createDirectory("/d/e", err => {
				assert.ifError(err);
				cfs.rename("/a", "b", err => {
					assert.ifError(err);
					cfs.list("/", (err, list) => {
						assert.ifError(err);
						assert.equal(list.length, 2);
						assert.ok(list.find(x => x.name == "b"), "/ must contain b");
						assert.ok(list.find(x => x.name == "d"), "/ must contain d");
						
						cfs.rename("/b/b", "bb", err => {
							assert.ifError(err);
							cfs.list("/", (err, list) => {
								assert.ifError(err);
								assert.equal(list.length, 2);
								assert.ok(list.find(x => x.name == "b"), "/ must contain b");
								assert.ok(list.find(x => x.name == "d"), "/ must contain d");
								
								cfs.list("/b", (err, list) => {
									assert.ifError(err);
									assert.equal(list.length, 1);
									assert.ok(list.find(x => x.name == "bb"), "/b must contain bb");
									
									done();
								});
							});
						});
					});
				});
			});
		});
	}));
	
	it("must move directories", runOnCleanFS(done => {
		cfs.createDirectory("/a/b/c", err => {
			assert.ifError(err);
			cfs.move("a/b", "/", err => {
				assert.ifError(err);
				cfs.list("/", (err, list) => {
					assert.ifError(err);
					assert.equal(list.length, 2);
					assert.ok(list.find(x => x.name == "a"), "/ must contain a");
					assert.ok(list.find(x => x.name == "b"), "/ must contain b");
					
					cfs.list("/a", (err, list) => {
						assert.ifError(err);
						assert.equal(list.length, 0, "/a must be empty");

						cfs.list("/b", (err, list) => {
							assert.ifError(err);
							assert.equal(list.length, 1);
							assert.ok(list.find(x => x.name == "c"), "/b must contain c");
							
							done();
						});
					});
				});
			});
		});
	}));
	
	it("must remove directories", runOnCleanFS(done => {
		cfs.createDirectory("/a/b/c", err => {
			assert.ifError(err);
			cfs.remove("a/b/c", err => {
				assert.ifError(err);
				cfs.list("/a/b", (err, list) => {
					assert.ifError(err);
					assert.equal(list.length, 0, "/a/b must be empty");
					
					cfs.list("/a", (err, list) => {
						assert.ifError(err);
						assert.equal(list.length, 1);
						assert.ok(list.find(x => x.name == "b"), "/a must contain b");
						
						done();
					});
				});
			});
		});
	}));
	
	it("must create files", runOnCleanFS(done => {	
		cfs.openFile("a.txt", "w", (err, fd) => {
			assert.ifError(err);
			cfs.closeFile(fd, err => {
				assert.ifError(err);
				cfs.list("/", (err, list) => {
					assert.ifError(err);
					assert.equal(list.length, 1);
					assert.ok(list.find(x => x.name == "a.txt" && x.isFile), "/ must contain file a.txt");
					
					done();
				});
			});
		});
	}));
	
	it("must read/write files", runOnCleanFS(done => {	
		cfs.openFile("a.txt", true, (err, fd) => {
			assert.ifError(err);
			cfs.writeFile(fd, 0, new Buffer("Hello"), err => {
				assert.ifError(err);
				cfs.readFile(fd, 0, -1, (err, buf) => {
					assert.ifError(err);
					assert.equal(buf.toString(), "Hello");
					
					cfs.writeFile(fd, 2, new Buffer("Goodbye"), err => {
						assert.ifError(err);
						cfs.readFile(fd, 0, -1, (err, buf) => {
							assert.ifError(err);
							assert.equal(buf.toString(), "HeGoodbye");
							
							cfs.closeFile(fd, err => {
								assert.ifError(err);
								
								cfs.list("/", (err, list) => {
									assert.ifError(err);
									assert.equal(list.length, 1);
									assert.equal(list[0].name, "a.txt", "/ must contain a.txt");
									assert.equal(list[0].isFile, true, "a.txt be a file");
									assert.equal(list[0].length, 9, "a.txt must be 9 bytes long");
									
									done();
								});
							});
						});
					});
				});
			});
		});
	}));
	
	it("must rename files", runOnCleanFS(done => {	
		cfs.openFile("a.txt", "w", (err, fd) => {
			assert.ifError(err);
			cfs.closeFile(fd, err => {
				assert.ifError(err);
				
				cfs.rename("/a.txt", "b.txt", err => {
					assert.ifError(err);
					cfs.list("/", (err, list) => {
						assert.ifError(err);
						assert.equal(list.length, 1);
						assert.ok(list.find(x => x.name == "b.txt" && x.isFile), "/ must contain file b.txt");
						
						done();
					});
				});
			});
		});
	}));
	
	it("must move files", runOnCleanFS(done => {	
		cfs.createDirectory("a", err => {
			cfs.openFile("a.txt", "w", (err, fd) => {
				assert.ifError(err);
				cfs.closeFile(fd, err => {
					assert.ifError(err);
					
					cfs.move("/a.txt", "/a", err => {
						assert.ifError(err);
						cfs.list("/", (err, list) => {
							assert.ifError(err);
							assert.equal(list.length, 1);
							assert.ok(list.find(x => x.name == "a" && x.isDirectory), "/ must contain directory a");
							
							cfs.list("/a", (err, list) => {
								assert.ifError(err);
								assert.equal(list.length, 1);
								assert.ok(list.find(x => x.name == "a.txt" && x.isFile), "/a must contain file a.txt");
								
								done();
							});
						});
					});
				});
			});
		});
	}));
	
	it("must remove files", runOnCleanFS(done => {	
		cfs.openFile("a.txt", "w", (err, fd) => {
			assert.ifError(err);
			cfs.closeFile(fd, err => {
				assert.ifError(err);
				
				cfs.remove("/a.txt", err => {
					assert.ifError(err);
					cfs.list("/", (err, list) => {
						assert.ifError(err);
						assert.equal(list.length, 0, "/ must be empty");
						
						done();
					});
				});
			});
		});
	}));
	
	
	function runOnCleanFS(action) {
		return function(done) {
			this.timeout(60000);
			
			cfs.discardAll(err => {
				assert.ifError(err);
				cfs.list("/", (err, list) => {
					assert.ifError(err);
					assert.equal(list.length, 0, "The filesystem must start empty when testing");
					action(done);
				});
			});
		};
	}
}

