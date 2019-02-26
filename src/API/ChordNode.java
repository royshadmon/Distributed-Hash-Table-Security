package API;

import java.io.Serializable;

public interface ChordNode<RESOURCE_TYPE extends Serializable> {

    int getId();

    void join(ChordNode<RESOURCE_TYPE> helper);
    void leave();

    RESOURCE_TYPE find(int keyId);

    void insert(int keyId, RESOURCE_TYPE resource);
    void remove(int keyId);

    void prettyPrint();

    String toString();
}
