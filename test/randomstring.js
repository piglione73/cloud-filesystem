"use strict";

var randomstring = require("randomstring");
var assert = require("assert");

describe("Random string", function() {
    it("must be random", function() {
        for(let i = 0; i < 1000; i++) {
            let r1 = randomstring.generate(10);
            let r2 = randomstring.generate(10);
            
            assert.equal(r1.length, 10);
            assert.equal(r2.length, 10);
            assert.notEqual(r1, r2);
        }
    });  
});
