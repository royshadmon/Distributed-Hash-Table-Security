package API;

import Nodes.FingerTable;

import java.io.Serializable;
import java.util.Comparator;

public interface ChordNode<RESOURCE_TYPE> extends Serializable {

    int getId();

    void join(ChordNode helper);
    void leave();

    RESOURCE_TYPE find(int keyId);

    void insert(int keyId, RESOURCE_TYPE resource);
    void remove(int keyId);

    void prettyPrint();

    String toString();


}
