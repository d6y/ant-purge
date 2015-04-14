# ant-purge

The purge tasks works with Ant to remove all but the most recent few files from a fileset. 

For example: let's say you're using an automated build tool, such as Cruise Control, 
which generates files (logs?) each time it's run. 
You can use the purge task to keep just the most recent few files --- rather than allowing the directory to fill with old files.
