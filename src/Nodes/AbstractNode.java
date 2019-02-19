package Nodes;

import API.ChordNode;
import API.ChordTracker;
import Nodes.Resource.ChordEntry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class AbstractNode<RESOURCE_TYPE> implements ChordNode<RESOURCE_TYPE> {

    private ChordTracker tracker;
    private int nodeId;
    AbstractNode predecessor;
    FingerTable table;

    List<ChordEntry<Integer, RESOURCE_TYPE>> entries;

    /* Used for printing node's during lookup */
    static boolean DEBUG;

    public AbstractNode(int nodeId) {
        if (!inLeftIncludedInterval(0, nodeId, FingerTable.MAX_NODES))
            throw new IndexOutOfBoundsException("Invalid Key Id");

        this.nodeId = hash(nodeId);
        this.table = new FingerTable(nodeId);
        this.entries = new ArrayList<>();
        this.predecessor = null;
        DEBUG = false;
    }

    public AbstractNode(ChordTracker tracker) {
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

        AbstractNode helper = (AbstractNode) help;
        this.put(1, (AbstractNode) helper.findSuccessor(this.computeStart(1)));

        this.predecessor = this.getSuccessor().predecessor;
        this.getSuccessor().predecessor = this;

        for (int i = 2; i <= FingerTable.MAX_ENTRIES; i++) {
            if (inLeftIncludedInterval(this.getId(), this.computeStart(i), this.get(i-1).getId()))
                this.put(i, this.get(i-1));
            else {
                AbstractNode successor = (AbstractNode) helper.findSuccessor(this.computeStart(i));

                if (inClosedInterval(this.computeStart(i), this.getId(), successor.getId()))
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

            AbstractNode predecessor = (AbstractNode) this.findPredecessor(updateIndex);

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
    protected void updateFingerTable(AbstractNode node, int entryNumber) {
        if (node.getId() == this.getId()) return;

        if (inLeftIncludedInterval(this.getId(), node.getId(), this.get(entryNumber).getId())) {
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

        List<AbstractNode> activeNodes = this.getActiveNodes();
        activeNodes.remove(this);

        for (AbstractNode node : activeNodes) {
            for (int i = 1; i <= FingerTable.MAX_ENTRIES; i++) {
                AbstractNode succ = (AbstractNode) this.findSuccessor(node.computeStart(i));
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
    protected List<AbstractNode> getActiveNodes(){
        List<AbstractNode> list = new ArrayList<>();

        AbstractNode temp = this;
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
     * @return Nodes.AbstractNode or null
     * @throws IndexOutOfBoundsException Keys must be between 0 and 255.
     */
    public abstract RESOURCE_TYPE find(int keyId);

    /**
     * Inserts the key at the Successor of the keyId.
     *
     * @param keyId
     */
    public abstract void insert(int keyId, RESOURCE_TYPE resource);

    /**
     *  If present, removes the key from the correct node.
     *
     * @param keyId
     * @throws IndexOutOfBoundsException Keys must be between 0 and 255.
     */
    public abstract void remove(int keyId);

    /**
     * This function is called when a new node joins, and transfers keys to the node (this node) joining the network.
     *
     */
    protected abstract void migrateEntries();

    /**
     * Removes entries that no longer belong to this node.
     *
     * @param id of the node joining the network
     * @return entries that have been removed from this node.
     */
    protected abstract List<ChordEntry<Integer, RESOURCE_TYPE>> updateEntries(int id);

    /************************************************************************************************
     BASIC FOUNDATIONAL METHODS - Helper methods to find the successor and predecessor of an id.
     ************************************************************************************************

    /**
     * This function returns the node that is the successor of the specified ID
     *
     * @param id is the id of the Nodes.AbstractNode or the key
     * @return Nodes.AbstractNode that is the Successor of the id
     */
    ChordNode findSuccessor(int id) {
        AbstractNode node = (AbstractNode) this.findPredecessor(id);
        return node.getSuccessor();
    }


    /**
     * This function returns the node that precedes the specified ID on the chord ring.
     * @param id
     * @return Nodes.AbstractNode that is the Predecessor of the id.
     */
    ChordNode findPredecessor(int id) {
        AbstractNode predecessor = this;

        while (!inRightIncludedInterval(predecessor.getId(), id, predecessor.getSuccessor().getId())) {
            predecessor = (AbstractNode) predecessor.findClosestPrecedingFinger(id);

            if (DEBUG) System.out.println(predecessor);
        }
        return predecessor;
    }


    /**
     * This functions looks in this node's finger table to find the node that is closest to the id.
     * @param id
     * @return Nodes.AbstractNode in the finger table that is closest to the specified id.
     */
    ChordNode findClosestPrecedingFinger(int id) {
        for (int i = FingerTable.MAX_ENTRIES; i >= 1 ; i--)
            if (inOpenInterval(this.getId(), this.get(i).getId(), id)) return this.get(i);

        return this;
    }

    /************************************************************************************************
     HELPER METHODS
     ************************************************************************************************

    /**
     * Returns first entry of the finger table.
     */
    public AbstractNode getSuccessor() { return this.get(1); }


    /**
     * Computes: id + 2^(i-1)
     *
     * @param entryNumber
     * @return
     */
    int computeStart(int entryNumber) { return FingerTable.computeStart(this.getId(), entryNumber); }


    /**
     * Computes: id - 2^(i)
     *
     * @param index
     * @return
     */
    int computeUpdateIndex(int index) {
        int result = this.getId() - (int) Math.pow(2, index) + FingerTable.MAX_NODES;
        result = result % FingerTable.MAX_NODES;
        return result;
    }

    public void prettyPrint() {
        this.table.prettyPrint();

        if (this.entries.size() > 0)
            System.out.println("AbstractNode " + this + "'s Resource Keys are: " + this.entries);
        System.out.println("\n___________________________________\n");
    }

    public String toString() { return "" + this.getId(); }

    void put(int entryNumber, AbstractNode node) { this.table.put(entryNumber, node); }

    AbstractNode get(int entryNumber) { return (AbstractNode) this.table.get(entryNumber); }

     /**
     * Emulates C++ Unsigned 8 bit Integer.
     * @param number
     * @return
     */
    int hash(int number) { return number & 0xff; }


    /**
     * Checks if c belongs in the interval [a, b]
     *
     * @return True or False
     */
    boolean inClosedInterval(int a, int c, int b) {

        a = a % FingerTable.MAX_NODES;
        b = b % FingerTable.MAX_NODES;
        c = c % FingerTable.MAX_NODES;

        if (a <= b) return (a <= c && c <= b);
        else return a <= c || c <= b;
    }


    /**
     * Checks if c belongs in the interval (a, b)
     *
     * @return True or False
     */
    boolean inOpenInterval(int a, int c, int b) { return inClosedInterval(a+1, c,b-1); }


    /**
     * Checks if c belongs in the interval [a, b)
     *
     * @return True or False
     */
    boolean inLeftIncludedInterval(int a, int c, int b) { return inClosedInterval(a, c, b-1); }


    /**
     * Checks if c belongs in the interval (a, b]
     *
     * @return True or False
     */
    boolean inRightIncludedInterval(int a, int c, int b) { return inClosedInterval(a+1, c, b); }

    /**
     *  Helper Class to allow for Nodes.AbstractNode sorting if required. Used only in getActiveNodes
     */
    class NodeComparator implements Comparator<ChordNode> {
        public int compare(ChordNode a, ChordNode b) {
            return a.getId() - b.getId();
        }
    }
}
