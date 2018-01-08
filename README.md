# ZooKeeper Java Example

## About
This is example of ZooKeeper Java client.
The original codes and its description are provided in [ZooKeeper documentation page](http://zookeeper.apache.org/doc/r3.4.11/javaExample.html).

This project includes dependent libraries so that you can run program immediately.

## How to use

### 1\. Start ZooKeeper service 

This program is a ZooKeeper client.
So external ZooKeeper service is needed and you have to prepare it.


### 2\. Run this program

You can start the program by following command:

```
mvn exec:java -Dexec.mainClass="Executor" -Dexec.args="localhost:2181 /example ./znode_data.txt ./command.sh"
```

(You need maven to use this project.)


### 3\. Interact with program

This program watches znode ``/example`` (this path is determined by the second program argument, so you can choose another one), and

- if znode is created or its data is changed, this program  write its data into ``./znode_data.txt`` and runs ``command.sh`` (check standard output).
- if znode is deleted, this program stops.

As you create, set data or delte the znode, this program takes above actions.
