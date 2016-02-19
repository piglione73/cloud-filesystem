"use strict";


/*
This is a store based on AWS S3 and DynamoDB.
*/
class StoreAWS {

	constructor(bucketName, bucketRegion, bucketPrefix, tableName, tableRegion) {
		if(!bucketName || !bucketRegion || !bucketPrefix || !tableName || !tableRegion){
			console.error("Missing parameters in StoreAWS constructor");
			return;
		}
			
		this.bucketName = bucketName;
		this.bucketRegion = bucketRegion;
		this.bucketPrefix = bucketPrefix;
		this.tableName = tableName;
		this.tableRegion = tableRegion;

		this.s3 = new AWS.S3({ region: this.bucketRegion });
		this.db = new AWS.DynamoDB({ region: this.tableRegion });
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
					callback(RetCodes.NotFound);
				else
					callback(err);
			}
			else {
				//Response received
				callback(RetCodes.OK, data.Body);
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
					callback(RetCodes.OK);
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
					callback(RetCodes.OK);
				}
			});
		}
    }
	

    cleanLogInfo(key, callback) {
        /*
        Removes the log info associated to a given key.

        Calls callback(status).
        */
		var params = {
			Key: { "key": { S: key } },
			TableName: this.tableName
		};
		this.db.deleteItem(params, function(err, data) {
			if(err) {
				//Something went wrong
				callback(err);
			}
			else {
				//Response received
				callback(RetCodes.OK);
			}
		});
    }


    getLogInfo(key, callback) {
        /*
        Gets the log info associated to a given key.

        Calls callback(status, index, id).
        */
		var params = {
			Key: { "key": { S: key } },
			TableName: this.tableName,
			ConsistentRead: true
		};
		this.db.getItem(params, function(err, data) {
			if(err) {
				//Something went wrong
				callback(err);
			}
			else {
				//Response received
				if(data.Item)
					callback(RetCodes.OK, parseInt(data.Item.index.N), data.Item.id.S);
				else
					callback(RetCodes.NotFound);
			}
		});
    }

    insertLogInfo(key, newIndex, newID, callback) {
        /*
        Consistently insert a log info when it does not exist for the given key.
        The index is set to newIndex and the id is set to newID.

        Calls callback(status).
        */
		var params = {
			Item: { "key": { S: key }, "index": { N: newIndex.toString() }, "id": { S: newID } },
			TableName: this.tableName,
			ConditionExpression: "attribute_not_exists(#p1)",
			ExpressionAttributeNames: {
				"#p1": "key"
			}
		};
		this.db.putItem(params, function(err, data) {
			if(err) {
				//Something went wrong
				if(err.code == "ConditionalCheckFailedException")
					callback(RetCodes.AlreadyPresent);
				else
					callback(err);
			}
			else {
				//Response received
				callback(RetCodes.OK);
			}
		});
    }

    updateLogInfo(key, existingIndex, newIndex, newID, callback) {
        /*
        Consistently updates the log info associated to a given key. The update is only performed 
        if the index is equal to existingIndex. The index is set to newIndex and the id is set to newID.

        Calls callback(status).
        */
		var params = {
			Item: { "key": { S: key }, "index": { N: newIndex.toString() }, "id": { S: newID } },
			TableName: this.tableName,
			ConditionExpression: "attribute_exists(#p1) and #p2=:ndx",
			ExpressionAttributeNames: {
				"#p1": "key",
				"#p2": "index"
			},
			ExpressionAttributeValues: {
				":ndx": { N: existingIndex.toString() }
			},
			ReturnValues: "ALL_OLD"
		};
		this.db.putItem(params, function(err, data) {
			if(err) {
				//Something went wrong
				if(err.code == "ConditionalCheckFailedException")
					callback(RetCodes.NotFoundOrIndexDoesNotMatch);
				else
					callback(err);
			}
			else {
				//Response received
				callback(RetCodes.OK);
			}
		});
    }

}



module.exports = StoreAWS;
var AWS = require("aws-sdk"); 
var RetCodes = require("./return-codes.js");
