package com.github.snobutaka.zookeeper.example.sync;

class FunctionPaths {

    final String nodeName;
    final String functionName;

    FunctionPaths(String nodeName, String functionName) {
        this.nodeName = nodeName;
        this.functionName = functionName;
    }

    String nodeRoot() {
        return String.format("/%s", nodeName);
    }

    String functionRoot() {
        StringBuilder funcRoot = new StringBuilder(nodeRoot());
        funcRoot.append("/").append(functionName);
        return funcRoot.toString();
    }

    String inputs() {
        StringBuilder funcIn = new StringBuilder(this.functionRoot());
        funcIn.append("/in");
        return funcIn.toString();
    }

    String outputs() {
        StringBuilder funcOut = new StringBuilder(this.functionRoot());
        funcOut.append("/out");
        return funcOut.toString();
    }

    String input(String requestId) {
        StringBuilder input = new StringBuilder(this.inputs());
        input.append("/").append(requestId);
        return input.toString();
    }

    String output(String requestId) {
        StringBuilder out = new StringBuilder(this.outputs());
        out.append("/").append(requestId);
        return out.toString();
    }
}
