"use strict";

var AWS = require("aws-sdk"); 
var StoreBase = require("./store-base.js");

/*
This is a store based on AWS S3 and DynamoDB.
*/
class StoreAWS {

	constructor(bucketName, bucketPrefix, tableName) {
		if(!bucketName){
			console.error("Missing bucket name in StoreAWS constructor");
			return;
		}
		else if(!bucketPrefix){
			console.error("Missing bucket prefix in StoreAWS constructor");
			return;
		}
		else if(!tableName){
			console.error("Missing table name in StoreAWS constructor");
			return;
		}
			
		this.bucketName = bucketName;
		this.bucketPrefix = bucketPrefix;
		this.tableName = tableName;

		this.s3 = new AWS.S3();
	}
	
    getBytes(key, callback) {
        /*
        Reads bytes associated to a given key and calls the callback.
    
        The callback is called as callback(status, bytes).
        */
		var params = {Bucket: this.bucketName, Key: this.bucketPrefix + key};
		this.s3.getObject(params, function(err, data) {
			if(err) {
				//Something went wrong
				if(err.code == "NoSuchKey")
					callback(StoreBase.NotFound);
				else
					callback(err);
			}
			else {
				//Response received
				callback(StoreBase.OK, data.Body);
			}
		});
    }

    setBytes(key, bytes, callback) {
        /*
        Write bytes associated to a given key.

        The callback is called as callback(status).
        */
		if(bytes) {
			var params = {Bucket: this.bucketName, Key: this.bucketPrefix + key, Body: bytes};
			this.s3.upload(params, function(err, data) {
				if(err) {
					//Something went wrong
					callback(err);
				}
				else {
					//Response received
					callback(StoreBase.OK);
				}
			});
		}
		else {
			var params = {Bucket: this.bucketName, Key: this.bucketPrefix + key};
			this.s3.deleteObject(params, function(err, data) {
				if(err) {
					//Something went wrong
					callback(err);
				}
				else {
					//Response received
					callback(StoreBase.OK);
				}
			});
		}
    }
	
}



module.exports = StoreAWS;
