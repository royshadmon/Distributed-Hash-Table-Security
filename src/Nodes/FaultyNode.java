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

    }

    @Override
    protected ChordNode<RESOURCE_TYPE> findSuccessor(int keyId) {
        return null;
    }

    @Override
    protected ChordNode<RESOURCE_TYPE> findPredecessor(int keyId) {
        return null;
    }

    @Override
    protected void initFingerTable(ChordNode<RESOURCE_TYPE> helper) {

    }
}
