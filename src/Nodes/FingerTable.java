package Nodes;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class FingerTable<RESOURCE_TYPE extends Serializable> implements Serializable {

    private int hostNodeId;

    public static final int MAX_ENTRIES = 17;

    public static final int MAX_NODES = (int) (Math.pow(2, MAX_ENTRIES));
    private LinkedHashMap<Integer, AbstractNode<RESOURCE_TYPE>> fingerTable = new LinkedHashMap<>();

    FingerTable(Integer hostNodeId) {
        this.hostNodeId = hostNodeId;
    }

    AbstractNode<RESOURCE_TYPE> get(Integer entryNumber) {
        if (entryNumber == 0) throw new RuntimeException("Entries start from 1");
        if (entryNumber > MAX_ENTRIES) throw new RuntimeException("Exceeded number of entries. " +
                "Only " + MAX_ENTRIES + " entries are allowed.");

        Integer key = computeStart(entryNumber);
        return this.fingerTable.get(key);
    }

    void put(Integer entryNumber, AbstractNode<RESOURCE_TYPE> node) {
        if (entryNumber == 0) throw new RuntimeException("Entries start from 1");
        if (entryNumber > MAX_ENTRIES) throw new RuntimeException("Exceeded number of entries. " +
                "Only " + MAX_ENTRIES + " entries are allowed.");

        Integer key = computeStart(entryNumber);
        this.fingerTable.put(key, node);
    }

    void prettyPrint() {
        if (this.fingerTable.size() > 0) {
            System.out.println("-------------------------");
            System.out.println("Finger Table - AbstractNode " + this.hostNodeId);
            System.out.println("-------------------------");
            this.fingerTable.forEach((key, value) -> {
                System.out.print(" Start: " + key);
                System.out.println("      Id: " + value.getId());
            });
            System.out.println("-------------------------");
        }
    }

    private Integer computeStart(Integer entryNumber) {
        int index = this.hostNodeId + (int) ((Math.pow(2, entryNumber - 1)));
        index = index % MAX_NODES;
        return index;

    }

    static Integer computeStart(int hostNodeId, int entryNumber) {
        int index = hostNodeId + (int) ((Math.pow(2, entryNumber - 1)));
        index = index % MAX_NODES;
        return index;
    }
}