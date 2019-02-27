package Nodes;

import Nodes.Resource.ChordEntry;
import Nodes.Resource.Partitions.SealedPartition;
import Nodes.Security.Cryptographer;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
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
        System.out.println("NULL HERE: "+ partitionName);
        for (int i = 1; i <= Cryptographer.MAX_PARTITIONS; i++) {
            int id = this.getChordId(partitionName);
            Node <RESOURCE_TYPE> successor = (Node<RESOURCE_TYPE>) this.findSuccessor(id);
            for (SealedPartition sp : successor.entries ) {
                if (sp.getPartitionName().equals(partitionName)) {
                    partitions.add(sp);
                }
            }

            if (partitions.size() == Cryptographer.MIN_PARTITIONS) {
                break;
            }
            partitionName = cryptographer.hashString(partitionName);
        }

        if (partitions.size() < Cryptographer.MIN_PARTITIONS) {
            System.out.println("RESOURCE LOST---SORRRY");
            return null;
        }

        return cryptographer.reassembleResource(partitions);

    }

    public int getChordId(String partitionName) {
        System.out.println("PARTITION NAME : " + partitionName);
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
        System.out.println("MY RESOURCE NAME: " + resourceName);
        this.resourceMap.put(resourceName, first);
        System.out.println("GETTING: " + resourceMap.get(resourceName));
        partitions.forEach(sp -> {
            Node<RESOURCE_TYPE> node = (Node<RESOURCE_TYPE>)this.findSuccessor(sp.getChordId());
            node.entries.add(sp);
        });
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
        AbstractNode n1 = new Node(0);
        AbstractNode n2 = new Node(64);
        AbstractNode n3 = new Node(255);

        n1.join(null);
        n2.join(n1);
        n3.join(n2);

        String k = "Karthik";

        People karthik = new People(k, "B", "1322");
        People alex = new People("Alex", "W", "kcool");
        People roy = new People("Roy", "S", "blahblah");

        ((Node) n1).insert(k, karthik);
        ((Node) n2).insert("Alex", alex);
        ((Node) n1).insert("Roy", roy);

        n1.prettyPrint();
        n2.prettyPrint();
        n3.prettyPrint();

        People d = (People)((Node) n1).find(k);
        System.out.println(d.firstname + " " + d.lastname + " " + d.password);



    }
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
