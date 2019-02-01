package Nodes;

import API.ChordNode;

public class FaultyNode<RESOURCE_TYPE> extends Node <RESOURCE_TYPE> {

    public FaultyNode(int id) {
        super(id);
    }

    @Override
    public ChordNode find(int keyId) {
        return null;
    }

    @Override
    public void leave() {
        this.entries = null;
        this.predecessor = null;
        this.table = null;
    }

    @Override
    protected ChordNode findSuccessor(int keyId) {
        return null;
    }

    @Override
    protected ChordNode findPredecessor(int keyId) {
        return null;
    }
}
