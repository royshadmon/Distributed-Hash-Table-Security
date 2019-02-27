package Nodes;

import API.ChordNode;
import Nodes.Resource.Partitions.SealedPartition;
import Nodes.Security.Cryptographer;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

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
     * If the key does not exist at this node, returns null and prints not authorized message
     *
     * If the key exists, returns the resource. Returns null if it was not possible to reconstruct the resource.
     *
     * @return Resource or null
     */
    public RESOURCE_TYPE find(String resourceName) {
        String partitionName = this.resourceMap.get(resourceName);

        if (partitionName == null) {
            System.out.println("Not Authorized to find resource");
            return null;
        }

        List<SealedPartition> partitions = retrievePartitions(partitionName);

        if (partitions.size() < Cryptographer.MIN_PARTITIONS) {
            System.out.println("Resource was lost and cannot be recovered");
            return null;
        }

        RESOURCE_TYPE resource = cryptographer.reassembleResource(partitions);

        this.reinsertPartitions(resourceName, partitionName, partitions);

        return resource;
    }

    /**
     *  If present, removes the key from the correct node.
     *
     *  Note:
     *      retrievePartitions inherently removes all partitions of a resource
     */
    public void remove (String resourceName) {
        String partitionName = this.resourceMap.get(resourceName);
        if (partitionName == null)
            return;

        this.retrievePartitions(partitionName);
        this.resourceMap.remove(resourceName);
    }

    public void removeResources() {
        for (Map.Entry<String, String> partition : this.resourceMap.entrySet()) {
            this.retrievePartition(partition.getValue());
        }
        this.resourceMap = new HashMap<>();
    }

    private List<SealedPartition> retrievePartitions(String partitionName) {
        List<SealedPartition> retrievedPartitions = new ArrayList<>();

        for (int i = 1; i <= Cryptographer.MAX_PARTITIONS; i++) {

            int id = this.convertToChordId(partitionName);
            Node <RESOURCE_TYPE> successor = (Node<RESOURCE_TYPE>) this.findSuccessor(id);

            SealedPartition retrieved = successor.retrievePartition(partitionName);
            retrievedPartitions.add(retrieved);

            partitionName = Cryptographer.hashString(partitionName);
        }

        return retrievedPartitions;
    }

    private SealedPartition retrievePartition(String name) {
        for (SealedPartition sp : this.entries) {
            if (sp.getPartitionName().equals(name)) {
                this.entries.remove(sp);
                return sp;
            }
        }

        return null;
    }

    private void reinsertPartitions(String resourceName, String partitionName, List<SealedPartition> partitions) {
        this.renamePartitions(partitions, partitionName);

        this.resourceMap.put(resourceName, partitions.get(0).getPartitionName());

        Collections.shuffle(partitions, new Random());
        this.insertPartitions(partitions);
    }

    private void renamePartitions(List<SealedPartition> partitions, String partitionName) {
        for (SealedPartition sp: partitions) {
            sp.setPartitionName(partitionName);
            partitionName = Cryptographer.hashString(partitionName);
        }
    }

    private int convertToChordId(String partitionName) {
        BigInteger key = new BigInteger(partitionName, 16);
        return key.mod(new BigInteger("" + FingerTable.MAX_NODES, 10)).intValue();
    }

    /**
     * Inserts the key at the Successor of the keyId.
     *
     */
    public void insert(String resourceName, RESOURCE_TYPE resource) {
        if (resource == null || resourceName == null) {
            System.out.println("Invalid Arguments");
        } else {

            List<SealedPartition> partitions = cryptographer.partitionResource(resource);
            String partitionName = partitions.get(0).getPartitionName();

            this.resourceMap.put(resourceName, partitionName);

            this.insertPartitions(partitions);
        }
    }

    private void insertPartitions(List<SealedPartition> partitions) {
        partitions.forEach(sp -> {
            Node<RESOURCE_TYPE> node = (Node<RESOURCE_TYPE>) this.findSuccessor(sp.getChordId());
            node.entries.add(sp);
        });
    }


    /**
     * This function is called when a new node joins, and transfers keys to the node (this node) joining the network.
     *
     */
    protected void migrateEntries() {
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

        for (int i = 0; i < this.entries.size(); i++) {
            SealedPartition entry = this.entries.get(i);

            if (inRightIncludedInterval(this.getId(), entry.getChordId(), id)) {
                System.out.println("Updating keys of Node " + this.getId());
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

        People resource1 = new People("Karthik", "B", "1322");
        People resource2 = new People("Alex", "W", "kcool");
        People resource3 = new People("Roy", "S", "blahblah");

        n1.insert("Karthik", resource1);
        n2.insert("Alex", resource2);
        n1.insert("Roy", resource3);

        People d = n1.find("Karthik");
        System.out.println(d.firstname + " " + d.lastname + " " + d.password);

        d = n1.find("Karthik");
        System.out.println(d.firstname + " " + d.lastname + " " + d.password);

        d = n1.find("Karthik");
        System.out.println(d.firstname + " " + d.lastname + " " + d.password);

        ChordNode<People> n5 = new Node<>(32);
        n5.join(n1);
        n1.leave();

//        n1.prettyPrint();
        n2.prettyPrint();
        n3.prettyPrint();
        n4.prettyPrint();
        n5.prettyPrint();
        n2.find("Karthik");

//        n1.remove("Karthik");

//        n1.prettyPrint();
//        n2.prettyPrint();
//        n3.prettyPrint();
//        n4.prettyPrint();
//        n5.prettyPrint();
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
