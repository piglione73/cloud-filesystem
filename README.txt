cloud-filesystem
================

A cloud filesystem. Supports locking and transactions. 
Can be backed by an S3 bucket and is not affected by eventual consistency issues. 
Can be mounted in Linux and Windows.
Uses fuse and JNI to allow mounting a generic Java filesystem interface.
