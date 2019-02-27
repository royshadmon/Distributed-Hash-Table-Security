package Nodes.Resource.Partitions;

import Nodes.FingerTable;
import Nodes.Resource.ChordEntry;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import java.math.BigInteger;

public class SealedPartition extends ChordEntry<Integer, SealedObject> {

    private String partitionName;

    public SealedPartition(String partitionName, Integer partitionId, SealedObject resource) {
        super(partitionId, resource);
        this.partitionName = partitionName;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public int getChordId() {
        BigInteger key = new BigInteger(this.partitionName, 16);
        return key.mod(new BigInteger("" + FingerTable.MAX_NODES, 10)).intValue();
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