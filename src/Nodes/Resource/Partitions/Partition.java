package Nodes.Resource.Partitions;

import Nodes.Resource.ChordEntry;

import java.util.Arrays;

public class Partition extends ChordEntry<Integer, double[]> {
    public Partition(Integer partitionId, double[] resource) {
        super(partitionId, resource);
    }

    @Override
    public String toString() {
        return this.getKey()+ " : " + Arrays.toString(this.getValue());
    }
}

