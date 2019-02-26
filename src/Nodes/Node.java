package Nodes;

import Nodes.Resource.ChordEntry;
import Nodes.Security.Cryptographer;

import java.io.Serializable;
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
    public RESOURCE_TYPE find(int keyId) {
        if (!inLeftIncludedInterval(0, keyId, FingerTable.MAX_NODES))
            throw new IndexOutOfBoundsException("Invalid Key Id");

        int key = hash(keyId);
        AbstractNode<RESOURCE_TYPE> node = this.findSuccessor(key);

        for (int i=0; i < node.entries.size(); i++) {
            ChordEntry<Integer, RESOURCE_TYPE> entry = (node.entries.get(0));
            if (entry.getKey() == key) return entry.getValue();
        }

        return null;
    }


    /**
     * Inserts the key at the Successor of the keyId.
     *
     */
    public void insert(int keyId, RESOURCE_TYPE resource) {
        if (!inLeftIncludedInterval(0, keyId, FingerTable.MAX_NODES))
            throw new IndexOutOfBoundsException("Invalid Key Id");

        int key = hash(keyId);
        ChordEntry<Integer, RESOURCE_TYPE> entry = new ChordEntry<>(keyId, resource);

        AbstractNode<RESOURCE_TYPE> node = this.findSuccessor(key);

        node.entries.add(entry);
    }

    /**
     *  If present, removes the key from the correct node.
     *
     * @throws IndexOutOfBoundsException Keys must be between 0 and 255.
     */
    public void remove(int keyId) {
        if (!inLeftIncludedInterval(0, keyId, FingerTable.MAX_NODES))
            throw new IndexOutOfBoundsException("Invalid Key Id");

        int key = hash(keyId);

        AbstractNode<RESOURCE_TYPE> node = this.findSuccessor(key);

        List<ChordEntry<Integer, RESOURCE_TYPE>> entries = node.entries;

        for (int i = 0; i < entries.size(); i++) {
            ChordEntry<Integer, RESOURCE_TYPE> entry = entries.get(i);
            if (entry.getKey() == key) {
                entries.remove(i);
                return;
            }
        }

        System.out.println("Key with id " + keyId + " not found");
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
        List<ChordEntry<Integer, RESOURCE_TYPE>> newEntries = this.getSuccessor().updateEntries(this.getId());

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
    protected List<ChordEntry<Integer, RESOURCE_TYPE>> updateEntries(int id) {
        List<ChordEntry<Integer, RESOURCE_TYPE>> removedEntries = new ArrayList<>();

        for (int i=0; i < this.entries.size(); i++) {
            ChordEntry<Integer, RESOURCE_TYPE> entry = this.entries.get(i);

            if (inRightIncludedInterval(this.getId(), entry.getKey(), id)) {
                System.out.println("Updating keys of Nodes.AbstractNode " + this.getId());
                System.out.println();
                System.out.println("Removing key with id: " + entry.getKey());

                removedEntries.add(entry);
                this.entries.remove(i);
                i--;
            }
        }

        return removedEntries;
    }
}
