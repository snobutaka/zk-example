package com.github.snobutaka.zookeeper.example.sync;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ZkSyncFunctionTest {

    TestingServer zookeeperServer;

    @Before
    public void setUp() throws Exception {
        zookeeperServer = new TestingServer();
        zookeeperServer.start();
    }

    @After
    public void tearDown() throws Exception {
        if (zookeeperServer != null) {
            zookeeperServer.close();
        }
    }

    @Test
    public void testCallFunction() throws Exception {
        final int numLoops = 1_000; // 5.0 sec 程度要する
        final String calleeName = "callee";
        final String callerName = "caller";
        final String functionName = "square";

        ZooKeeper zk1 = new ZooKeeper(zookeeperServer.getConnectString(), 1000, null);
        ZkSyncFunction callee = new ZkSyncFunction(zk1, calleeName, functionName);
        callee.prepareForCall();

        ZooKeeper zk2 = new ZooKeeper(zookeeperServer.getConnectString(), 1000, null);
        ZkSyncFunction caller = new ZkSyncFunction(zk2, callerName, functionName);

        long start = System.currentTimeMillis();
        for (int i = 0; i < numLoops; i++) {
            assertThat(caller.call(calleeName, i), is(i * i));
        }
        System.out.println(String.format("Elapsed time=%d", (System.currentTimeMillis() - start)));

        zk1.close();
        zk2.close();
    }
}
