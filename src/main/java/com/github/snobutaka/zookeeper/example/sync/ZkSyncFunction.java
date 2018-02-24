package com.github.snobutaka.zookeeper.example.sync;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

/**
 * ZooKeeper を利用した同期呼び出し関数．
 * ZooKeeper の思想的に NG な使い方だが，試しに実装してみたもの．
 *
 * @author Nobutaka
 */
public class ZkSyncFunction {

    final String myNodeName;
    final String functionName;
    private ZooKeeper zk;

    public ZkSyncFunction(ZooKeeper zk, String myNodeName, String functionName) {
        this.myNodeName = myNodeName;
        this.functionName = functionName;
        this.zk = zk;
    }

    /**
     * 関数の被呼び出し側の事前準備
     *
     * @throws KeeperException
     * @throws InterruptedException
     */
    public void prepareForCall() throws KeeperException, InterruptedException {
        // やりとりに必要な　znode　の作成
        FunctionPaths paths = new FunctionPaths(this.myNodeName, this.functionName);
        for (String path : Arrays.asList(paths.nodeRoot(), paths.functionRoot(), paths.inputs(), paths.outputs())) {
            this.getZk().create(path, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
        // 入力の通知に用いる監視
        this.getZk().getData(paths.inputs(), new FunctionInputWatcher(this), null);
    }

    /**
     * 実際の関数の計算内容．今は int 値の二乗．
     *
     * @param n　二乗したい値
     * @return n の二乗
     */
    int apply(int n) {
        return n * n;
    }

    /**
     * 関数の同期呼び出しを行う．
     *
     * @param nodeToCall 関数を呼び出す相手の名前
     * @param n　関数の引数
     * @return　呼び出し相手からが返してきて計算結果
     * @throws KeeperException
     * @throws InterruptedException
     */
    public synchronized int call(String nodeToCall, int n) throws KeeperException, InterruptedException {
        String requestId = UUID.randomUUID().toString(); // 関数呼び出し毎に割り振る ID
        FunctionPaths paths = new FunctionPaths(nodeToCall, this.functionName);
        FunctionOutputWatcher watcher = new FunctionOutputWatcher(this, paths.output(requestId));

        this.getZk().exists(paths.output(requestId), watcher); // 結果が出力される znode を監視
        byte[] param = Integer.toString(n).getBytes(StandardCharsets.UTF_8);
        this.getZk().create(paths.input(requestId), param, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT); // 入力データを書き込む
        this.getZk().setData(paths.inputs(), new byte[0], -1); // callee が監視している znode に書き込みを行い通知する

        synchronized (this) {
            while(watcher.waiting) {
                this.wait(1000); // 待ちきれなければ InterruptedException
            }
        }

        return watcher.get();
    }

    ZooKeeper getZk() {
        return this.zk;
    }
}
