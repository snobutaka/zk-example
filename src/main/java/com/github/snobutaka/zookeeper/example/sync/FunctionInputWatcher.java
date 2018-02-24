package com.github.snobutaka.zookeeper.example.sync;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;

class FunctionInputWatcher implements Watcher {

    private ZkSyncFunction func;
    private FunctionPaths paths;

    FunctionInputWatcher(ZkSyncFunction func) {
        this.func = func;
        this.paths = new FunctionPaths(func.myNodeName, func.functionName);
    }

    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
        case NodeDataChanged:
            execute();
            break;

        default:
            System.out.println("[ERROR] Unexpected event: " + event);
        }
    }

    void execute() {
        try {
            this.func.getZk().getData(paths.inputs(), this, null);
            List<String> inputs = this.func.getZk().getChildren(paths.inputs(), false);
            for (String input : inputs) {
                call(input);
                this.func.getZk().delete(paths.input(input), -1);
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    void call(String input) throws KeeperException, InterruptedException {
        byte[] data = this.func.getZk().getData(paths.input(input), false, null);
        int arg = Integer.parseInt(new String(data, StandardCharsets.UTF_8));
        int result = this.func.apply(arg);
        this.func.getZk().create(paths.output(input), Integer.toString(result).getBytes(StandardCharsets.UTF_8),
                Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }
}
