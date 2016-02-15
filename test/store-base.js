"use strict";

var assert = require("assert");
var StoreBase = require("../src/store-base");


describe("StoreBase", function() {

    it("must store bytes", function() {
        var store = new StoreBase();

        store.getBytes("AAA", function(status, bytes) {
            assert.equal(status, StoreBase.NotFound);
            assert.equal(bytes, undefined);
        });

        store.setBytes("AAA", new Buffer("aaa"), function(status) {
            assert.equal(status, StoreBase.OK);
        });

        store.getBytes("AAA", function(status, bytes) {
            assert.equal(status, StoreBase.OK);
            assert.equal(bytes.equals(new Buffer("aaa")), true);
        });

        store.setBytes("AAA", new Buffer("aaa2"), function(status) {
            assert.equal(status, StoreBase.OK);
        });

        store.getBytes("AAA", function(status, bytes) {
            assert.equal(status, StoreBase.OK);
            assert.equal(bytes.equals(new Buffer("aaa2")), true);
        });

        store.getBytes("BBB", function(status, bytes) {
            assert.equal(status, StoreBase.NotFound);
            assert.equal(bytes, undefined);
        });
    });

    it("must handle log info consistently", function() {
        var store = new StoreBase();

        store.getLogInfo("AAA", function(status, index, id) {
            assert.equal(status, StoreBase.NotFound);
            assert.equal(index, undefined);
            assert.equal(id, undefined);
        });

        store.updateLogInfo("AAA", 0, 1, "ABC", function(status) {
            assert.equal(status.NotFound);
        });

        store.insertLogInfo("AAA", 1, "ABC", function(status) {
            assert.equal(status, StoreBase.OK);
        });

        store.insertLogInfo("AAA", 1, "ABC", function(status) {
            assert.equal(status, StoreBase.AlreadyPresent);
        });

        store.updateLogInfo("AAA", 0, 1, "ABC", function(status) {
            assert.equal(status, StoreBase.IndexDoesNotMatch);
        });

        store.updateLogInfo("AAA", 1, 2, "CDE", function(status) {
            assert.equal(status, StoreBase.OK);
        });

        store.getLogInfo("AAA", function(status, index, id) {
            assert.equal(status, StoreBase.OK);
            assert.equal(index, 2);
            assert.equal(id, "CDE");
        });
    });
});
