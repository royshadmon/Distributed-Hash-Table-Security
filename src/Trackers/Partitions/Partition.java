package Trackers.Partitions;

import Nodes.Resource.ChordEntry;

public class Partition extends ChordEntry<Integer, Double[]> {
    public Partition(Integer key, Double[] resource) {
        super(key, resource);
    }

    @Override
    public String toString() {
        return this.getKey()+ " " + this.getValue();
    }
}

