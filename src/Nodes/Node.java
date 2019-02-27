package Nodes;

import API.ChordNode;
import Nodes.Resource.Partitions.SealedPartition;
import Nodes.Security.Cryptographer;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class Node<RESOURCE_TYPE extends Serializable> extends AbstractNode<RESOURCE_TYPE> {

    private Cryptographer<RESOURCE_TYPE> cryptographer;

    public Node(int nodeId) {
        super(nodeId);
        cryptographer = new Cryptographer<>();
    }

    /************************************************************************************************
     KEY METHODS - Methods used for the addition and removal of keys in the network
     ************************************************************************************************

     /**
     * If the key exists, returns the node containing the key. Else returns null.
     *
     * @return Nodes.AbstractNode or null
     * @throws IndexOutOfBoundsException Keys must be between 0 and 255.
     */
    public RESOURCE_TYPE find(String resourceName) {
        List<SealedPartition> partitions = new ArrayList<>();
        String partitionName = this.resourceMap.get(resourceName);

        for (int i = 1; i <= Cryptographer.MAX_PARTITIONS; i++) {

            int id = this.getChordId(partitionName);

            Node <RESOURCE_TYPE> successor = (Node<RESOURCE_TYPE>) this.findSuccessor(id);

            for (SealedPartition sp : successor.entries )
                if (sp.getPartitionName().equals(partitionName))
                    partitions.add(sp);

            partitionName = Cryptographer.hashString(partitionName);
        }

        if (partitions.size() < Cryptographer.MIN_PARTITIONS) {
            System.out.println("RESOURCE LOST");
            return null;
        }

        RESOURCE_TYPE resource = cryptographer.reassembleResource(partitions);

        // Everything under here just puts it back
        this.resourceMap.put(resourceName, partitionName);

        for (SealedPartition sp: partitions) {

            Node<RESOURCE_TYPE> node = (Node<RESOURCE_TYPE>) this.findSuccessor(sp.getChordId());

            node.entries.remove(sp);

            sp.setPartitionName(partitionName);
            partitionName = Cryptographer.hashString(partitionName);
        }

        this.reinsertPartitions(resourceName, partitionName, partitions);

        return resource;

    }

    private void reinsertPartitions(String resourceName, String partitionName, List<SealedPartition> partitions){
        this.resourceMap.put(resourceName, partitionName);

        for (SealedPartition sp: partitions) {

            Node<RESOURCE_TYPE> node = (Node<RESOURCE_TYPE>) this.findSuccessor(sp.getChordId());

            node.entries.remove(sp);

            sp.setPartitionName(partitionName);
            partitionName = Cryptographer.hashString(partitionName);
        }

        partitions.forEach(sp -> {
            Node<RESOURCE_TYPE> node = (Node<RESOURCE_TYPE>)this.findSuccessor(sp.getChordId());
            node.entries.add(sp);
        });
    }

    public int getChordId(String partitionName) {
        BigInteger key = new BigInteger(partitionName, 16);
        return key.mod(new BigInteger("" + FingerTable.MAX_NODES, 10)).intValue();
    }

    /**
     * Inserts the key at the Successor of the keyId.
     *
     */
    public void insert(String resourceName, RESOURCE_TYPE resource) {
        List<SealedPartition> partitions = cryptographer.partitionResource(resource);
        String first = partitions.get(0).getPartitionName();

        this.resourceMap.put(resourceName, first);

        partitions.forEach(sp -> {
            Node<RESOURCE_TYPE> node = (Node<RESOURCE_TYPE>)this.findSuccessor(sp.getChordId());
            node.entries.add(sp);
        });
    }

    /**
     * This function is called when a new node joins, and transfers keys to the node (this node) joining the network.
     *
     */
    protected void migrateEntries() {
        // 1. This function should find the successor of the node, from the finger table,
        // 2. Update the successor's key set to remove keys it should no longer manage.
        // 3. Add those keys to this node's key set

        // Should work even when there are no keys in the system
        List<SealedPartition> newEntries = this.getSuccessor().updateEntries(this.getId());

        if (newEntries.size() != 0) {
            System.out.println("Adding them to new node " + this.getId());
            this.entries.addAll(newEntries);
            System.out.println("----------------------");
            System.out.println();
        }
    }


    /**
     * Removes entries that no longer belong to this node.
     *
     * @param id of the node joining the network
     * @return entries that have been removed from this node.
     */
    protected List<SealedPartition> updateEntries(int id) {
        List<SealedPartition> removedEntries = new ArrayList<>();

        for (int i=0; i < this.entries.size(); i++) {
            SealedPartition entry = this.entries.get(i);

            if (inRightIncludedInterval(this.getId(), entry.getChordId(), id)) {
                System.out.println("Updating keys of Nodes.AbstractNode " + this.getId());
                System.out.println();
                System.out.println("Removing key with id: " + entry.getChordId());

                removedEntries.add(entry);
                this.entries.remove(i);
                i--;
            }
        }

        return removedEntries;
    }

    public static void main(String[] args) {
        ChordNode<People> n1 = new Node<>(0);
        ChordNode<People> n2 = new Node<>(64);
        ChordNode<People> n3 = new Node<>(125);
        ChordNode<People> n4 = new Node<>(200);

        n1.join(null);
        n2.join(n1);
        n3.join(n2);
        n4.join(n3);

        String karthikResourceKey = "Karthik";

        People karthikResource = new People(karthikResourceKey, "B", "1322");
        People alex = new People("Alex", "W", "kcool");
        People roy = new People("Roy", "S", "blahblah");

        n1.insert(karthikResourceKey, karthikResource);
        n2.insert("Alex", alex);
        n1.insert("Roy", roy);

        People d = (People)((Node) n1).find(karthikResourceKey);
        System.out.println(d.firstname + " " + d.lastname + " " + d.password);

        n1.prettyPrint();
        n2.prettyPrint();
        n3.prettyPrint();
        n4.prettyPrint();

        d = (People)((Node) n1).find(karthikResourceKey);
        System.out.println(d.firstname + " " + d.lastname + " " + d.password);

        n1.prettyPrint();
        n2.prettyPrint();
        n3.prettyPrint();
        n4.prettyPrint();

    }

    /**
     *  If present, removes the key from the correct node.
     *
     * @throws IndexOutOfBoundsException Keys must be between 0 and 255.
     */
//    public void remove(int keyId) {
//        if (!inLeftIncludedInterval(0, keyId, FingerTable.MAX_NODES))
//            throw new IndexOutOfBoundsException("Invalid Key Id");
//
//        int key = hash(keyId);
//
//        AbstractNode<RESOURCE_TYPE> node = this.findSuccessor(key);
//
//        List<ChordEntry<Integer, RESOURCE_TYPE>> entries = node.entries;
//
//        for (int i = 0; i < entries.size(); i++) {
//            ChordEntry<Integer, RESOURCE_TYPE> entry = entries.get(i);
//            if (entry.getKey() == key) {
//                entries.remove(i);
//                return;
//            }
//        }
//
//        System.out.println("Key with id " + keyId + " not found");
//    }

}

class People implements Serializable {

    String firstname;
    String lastname;
    String password;

    People(String firstname, String lastname, String password){
        this.firstname = firstname;
        this.lastname = lastname;
        this.password = password;
    }
}
