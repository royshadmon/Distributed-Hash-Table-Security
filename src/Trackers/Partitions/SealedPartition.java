package Trackers.Partitions;


import Nodes.Resource.ChordEntry;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;

public class SealedPartition extends ChordEntry<Integer, SealedObject> {
    public SealedPartition(Integer key, SealedObject resource) {
        super(key, resource);
    }

    public Object getObject(Cipher cipher) throws Exception {
        SealedObject ob = this.getValue();
        return ob.getObject(cipher);
    }

    @Override
    public String toString() {
        return getKey() + " " + getValue();
    }
}