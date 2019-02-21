package Trackers.Partitions;

import Nodes.Resource.ChordEntry;

import java.util.Arrays;

public class Partition extends ChordEntry<Integer, double[]> {
    public Partition(Integer key, double[] resource) {
        super(key, resource);
    }

    @Override
    public String toString() {
        return this.getKey()+ " : " + Arrays.toString(this.getValue());
    }
}

