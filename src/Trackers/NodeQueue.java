package Trackers;

import API.ChordNode;
import Nodes.FingerTable;

import java.util.LinkedList;

public class NodeQueue extends LinkedList <ChordNode> {

    public ChordNode pop(ChordNode requester) {
        ChordNode selected = super.pop();

        if (this.contains(requester)) {
            this.offer(selected);
        }
        else {
            this.offer(requester);
        }

        return selected;
    }


}
