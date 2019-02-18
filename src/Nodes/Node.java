package Nodes;

import API.ChordNode;
import API.ChordTracker;
import Nodes.Resource.ChordEntry;

import java.util.ArrayList;
import java.util.List;

public class Node<RESOURCE_TYPE> implements ChordNode<RESOURCE_TYPE> {

    private ChordTracker tracker;
    private int nodeId;
    private Node predecessor;
    private FingerTable table;
    private List<ChordEntry<Integer, RESOURCE_TYPE>> entries;

    /* Used for printing node's during lookup */
    private static boolean DEBUG = false;


    public Node (int nodeId) {
        if (!ChordNode.inLeftIncludedInterval(0, nodeId, FingerTable.MAX_NODES))
            throw new IndexOutOfBoundsException("Invalid Key Id");

        this.nodeId = ChordNode.hash(nodeId);
        this.table = new FingerTable(nodeId);
        this.entries = new ArrayList<>();
        this.predecessor = null;
    }

    public Node (ChordTracker tracker) {
        this(tracker.assignId());
        this.tracker = tracker;
    }

    public int getId() { return this.nodeId; }



    /************************************************************************************************
     NODE JOIN METHODS - Methods involved in the addition of a new node to the network.
     ************************************************************************************************

    /*
     * Join allows new nodes to join the network with the help of an arbitrary node already in the network.
     *
     * @param helper is the Bootstrapper node provided to the node that is joining. If node is null then
     *             it is assumed that the node that is joining is the first node.
     *
     * @throws RuntimeException Cannot join from the same node.
     */
    public void join(ChordNode helper) {
        if (helper == null) this.initNetwork();
        else {
            if (helper.equals(this)) throw new RuntimeException("Cannot join using same node");

            this.initFingerTable(helper);
            this.updateOthers();
            this.migrateEntries();
        }
        this.prettyPrint();
    }


    /**
     * Initializes the finger table of the joining node.
     *
     * @param help is the bootstrapper node. The node that is joining uses network state information
     *               provided by the bootstrapper node to populate its finger tables.
     */
    private void initFingerTable(ChordNode help) {

        Node helper = (Node) help;
        this.put(1, (Node) helper.findSuccessor(this.computeStart(1)));

        this.predecessor = this.getSuccessor().predecessor;
        this.getSuccessor().predecessor = this;

        for (int i = 2; i <= FingerTable.MAX_ENTRIES; i++) {
            if (ChordNode.inLeftIncludedInterval(this.getId(), this.computeStart(i), this.get(i-1).getId()))
                this.put(i, this.get(i-1));
            else {
                Node successor = (Node) helper.findSuccessor(this.computeStart(i));

                if (ChordNode.inClosedInterval(this.computeStart(i), this.getId(), successor.getId()))
                    this.put(i, this);
                else
                    this.put(i, successor);
            }
        }
    }


    /**
     * Following the node join, the node then proceeds to update other nodes in the network based on an update index.
     */
    protected void updateOthers() {
        for (int i = 1; i <= FingerTable.MAX_ENTRIES; i++) {
            int updateIndex = computeUpdateIndex(i-1);

            Node predecessor = (Node) this.findPredecessor(updateIndex);

            if (predecessor.getSuccessor().getId() == updateIndex)
                predecessor = predecessor.getSuccessor();

            predecessor.updateFingerTable(this, i);
        }
    }


    /**
     * Updates a specific entry of the finger table of this node if required,
     * with the node that has just joined the network.
     *
     * @param node This is the node that has just joined the network, and needs to be added
     *             to the finger table of this node.
     * @param entryNumber This is the entry in the finger table that needs to be updated.
     */
    protected void updateFingerTable(Node node, int entryNumber) {
        if (node.getId() == this.getId()) return;

        if (ChordNode.inLeftIncludedInterval(this.getId(), node.getId(), this.get(entryNumber).getId())) {
            this.put(entryNumber, node);
            this.predecessor.updateFingerTable(node, entryNumber);
        }
        else if (this.computeStart(entryNumber) == node.nodeId) {
            this.put(entryNumber, node);
            this.predecessor.updateFingerTable(node, entryNumber);
        }
    }


    /**
     * Initializes the network when the first node joins
     */
    protected void initNetwork() {
        for (int i = 1; i <= FingerTable.MAX_ENTRIES; i++)
            this.put(i, this);

        this.predecessor = this;
    }


    /************************************************************************************************
     NODE LEAVE - Methods used exclusively for the node leave operation
     ************************************************************************************************

    /**
     * This function is invoked when the node wants to leave the network.
     * It triggers a rebuilding of Fingertables of the remaining nodes in the fingertable.
     */
    @SuppressWarnings("unchecked")
    public void leave() {

        if (this.getSuccessor().equals(this)) {
            System.out.println("No nodes left in the network!");
            return;
        }

        this.getSuccessor().entries.addAll(this.entries);

        List<Node> activeNodes = this.getActiveNodes();
        activeNodes.remove(this);

        for (Node node : activeNodes) {
            for (int i = 1; i <= FingerTable.MAX_ENTRIES; i++) {
                Node succ = (Node) this.findSuccessor(node.computeStart(i));
                if (succ.getId() == this.getId()) {
                    succ = this.getSuccessor();
                }
                node.put(i, succ);
            }
        }

        this.getSuccessor().predecessor = this.predecessor;
        this.table = new FingerTable(this.getId());
        this.entries = new ArrayList<>();
        this.predecessor = null;
    }

    /**
     * Creates an iterable list of all nodes in the network. Time Complexity is O(N)
     * @return
     */
    protected List<Node> getActiveNodes(){
        List<Node> list = new ArrayList<>();

        Node temp = this;
        while (temp.getSuccessor() != this) {
            list.add(temp);
            temp = temp.getSuccessor();
        }
        list.add(temp);

        list.sort(new NodeComparator());

        return list;
    }


    /************************************************************************************************
     KEY METHODS - Methods used for the addition and removal of keys in the network
     ************************************************************************************************

    /**
     * If the key exists, returns the node containing the key. Else returns null.
     *
     * @param keyId
     * @return Nodes.Node or null
     * @throws IndexOutOfBoundsException Keys must be between 0 and 255.
     */
    public ChordNode find(int keyId) {
        if (!ChordNode.inLeftIncludedInterval(0, keyId, FingerTable.MAX_NODES))
            throw new IndexOutOfBoundsException("Invalid Key Id");

        DEBUG = true;

        System.out.println("--------------------------------------------------");
        System.out.println("Nodes.Node's involved in Find operation for key " + keyId + " are: ");

        int key = ChordNode.hash(keyId);
        Node node = (Node) this.findSuccessor(key);

        System.out.println("--------------------------------------------------");

        DEBUG = false;


        for (int i=0; i < node.entries.size(); i++) {
            @SuppressWarnings("unchecked")
            ChordEntry<Integer, RESOURCE_TYPE> entry = (ChordEntry<Integer, RESOURCE_TYPE>) (node.entries.get(0));
            if (entry.getKey() == key) return node;
        }

        return null;
    }


    /**
     * Inserts the key at the Successor of the keyId.
     *
     * @param keyId
     */
    public void insert(int keyId, RESOURCE_TYPE resource) {
        if (!ChordNode.inLeftIncludedInterval(0, keyId, FingerTable.MAX_NODES))
            throw new IndexOutOfBoundsException("Invalid Key Id");

        int key = ChordNode.hash(keyId);
        ChordEntry<Integer, RESOURCE_TYPE> entry = new ChordEntry<>(keyId, resource);

        Node<RESOURCE_TYPE> node = (Node<RESOURCE_TYPE>) this.findSuccessor(key);

        node.entries.add(entry);
    }

    /**
     *  If present, removes the key from the correct node.
     *
     * @param keyId
     * @throws IndexOutOfBoundsException Keys must be between 0 and 255.
     */
    public void remove(int keyId) {
        if (!ChordNode.inLeftIncludedInterval(0, keyId, FingerTable.MAX_NODES))
            throw new IndexOutOfBoundsException("Invalid Key Id");

        int key = ChordNode.hash(keyId);

        Node<RESOURCE_TYPE> node = (Node<RESOURCE_TYPE> ) this.findSuccessor(key);

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
        @SuppressWarnings("unchecked")
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

            if (ChordNode.inRightIncludedInterval(this.getId(), entry.getKey(), id)) {
                System.out.println("Updating keys of Nodes.Node " + this.getId());
                System.out.println();
                System.out.println("Removing key with id: " + entry.getKey());

                removedEntries.add(entry);
                this.entries.remove(i);
                i--;
            }
        }

        return removedEntries;
    }


    /************************************************************************************************
     BASIC FOUNDATIONAL METHODS - Helper methods to find the successor and predecessor of an id.
     ************************************************************************************************

    /**
     * This function returns the node that is the successor of the specified ID
     *
     * @param id is the id of the Nodes.Node or the key
     * @return Nodes.Node that is the Successor of the id
     */
    protected ChordNode findSuccessor(int id) {
        Node node = (Node) this.findPredecessor(id);
        return node.getSuccessor();
    }


    /**
     * This function returns the node that precedes the specified ID on the chord ring.
     * @param id
     * @return Nodes.Node that is the Predecessor of the id.
     */
    protected ChordNode findPredecessor(int id) {
        Node predecessor = this;

        while (!ChordNode.inRightIncludedInterval(predecessor.getId(), id, predecessor.getSuccessor().getId())) {
            predecessor = (Node) predecessor.findClosestPrecedingFinger(id);

            if (DEBUG) System.out.println(predecessor);
        }
        return predecessor;
    }


    /**
     * This functions looks in this node's finger table to find the node that is closest to the id.
     * @param id
     * @return Nodes.Node in the finger table that is closest to the specified id.
     */
    protected ChordNode findClosestPrecedingFinger(int id) {
        for (int i = FingerTable.MAX_ENTRIES; i >= 1 ; i--)
            if (ChordNode.inOpenInterval(this.getId(), this.get(i).getId(), id)) return this.get(i);

        return this;
    }

    /************************************************************************************************
     HELPER METHODS
     ************************************************************************************************

    /**
     * Returns first entry of the finger table.
     */
    public Node getSuccessor() { return this.get(1); }


    /**
     * Computes: id + 2^(i-1)
     *
     * @param entryNumber
     * @return
     */
    private int computeStart(int entryNumber) { return FingerTable.computeStart(this.getId(), entryNumber); }


    /**
     * Computes: id - 2^(i)
     *
     * @param index
     * @return
     */
    private int computeUpdateIndex(int index) {
        int result = this.getId() - (int) Math.pow(2, index) + FingerTable.MAX_NODES;
        result = result % FingerTable.MAX_NODES;
        return result;
    }

    public void prettyPrint() {
        this.table.prettyPrint();

        if (this.entries.size() > 0)
            System.out.println("Node " + this + "'s Resource Keys are: " + this.entries);
        System.out.println("\n___________________________________\n");
    }

    public String toString() { return "" + this.getId(); }

    private void put(int entryNumber, Node node) { this.table.put(entryNumber, node); }

    private Node get(int entryNumber) { return (Node) this.table.get(entryNumber); }

}
