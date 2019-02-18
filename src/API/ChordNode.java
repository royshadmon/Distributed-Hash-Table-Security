package API;

import Nodes.FingerTable;

import java.io.Serializable;
import java.util.Comparator;

public interface ChordNode<RESOURCE_TYPE> extends Serializable {

    int getId();

    void join(ChordNode helper);
    void leave();

    ChordNode find(int keyId);

    void insert(int keyId, RESOURCE_TYPE resource);
    void remove(int keyId);

    void prettyPrint();

    String toString();


    /************************************************************************************************
     STATIC HELPER CLASS FOR COMPARISON
     ************************************************************************************************/

    static NodeComparator getComparator() {
        return new NodeComparator();
    }

    /**
     *  Helper Class to allow for Nodes.Node sorting if required. Used only in getActiveNodes
     */
    class NodeComparator implements Comparator<ChordNode> {
        public int compare(ChordNode a, ChordNode b) {
            return a.getId() - b.getId();
        }
    }

    /************************************************************************************************
     STATIC HELPER FUNCTIONS
     ************************************************************************************************

     /**
     * Emulates C++ Unsigned 8 bit Integer.
     * @param number
     * @return
     */
    static int hash(int number) { return number & 0xff; }


    /**
     * Checks if c belongs in the interval [a, b]
     *
     * @return True or False
     */
    static boolean inClosedInterval(int a, int c, int b) {

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
    static boolean inOpenInterval(int a, int c, int b) { return inClosedInterval(a+1, c,b-1); }


    /**
     * Checks if c belongs in the interval [a, b)
     *
     * @return True or False
     */
    static boolean inLeftIncludedInterval(int a, int c, int b) { return inClosedInterval(a, c, b-1); }


    /**
     * Checks if c belongs in the interval (a, b]
     *
     * @return True or False
     */
    static boolean inRightIncludedInterval(int a, int c, int b) { return inClosedInterval(a+1, c, b); }

}
