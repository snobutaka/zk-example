package com.github.snobutaka.zookeeper.example.sync;

import java.nio.charset.StandardCharsets;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

class FunctionOutputWatcher implements Watcher {

    ZkSyncFunction func;
    String outputZnodePath;

    int result;
    boolean waiting = true;

    FunctionOutputWatcher(ZkSyncFunction func, String outputZnodePath) {
        this.func = func;
        this.outputZnodePath = outputZnodePath;
    }

    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case NodeCreated:
                readResult();
                break;

            default:
                System.out.println("[ERROR] Unexpected event: " + event);
                this.waiting = false;
                this.notifyFunction();
        }
    }

    void readResult() {
        try {
            byte[] data = func.getZk().getData(outputZnodePath, false, null);
            this.result = Integer.parseInt(new String(data, StandardCharsets.UTF_8));
            this.waiting = false;
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            notifyFunction();
        }
    }

    private void notifyFunction() {
        synchronized (func) {
            func.notify();
        }
    }

    int get() {
        this.waiting = true;
        return this.result;
    }
}
