package Test;

import API.ChordNode;
import Nodes.AbstractNode;
import Nodes.FingerTable;
import Nodes.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChordTester {

    private static Random generator = new Random(0);

    @SuppressWarnings("unchecked")
    public static List<AbstractNode<String>> generateRandomNodeList(int numberOfNodes) {
        List<Integer> ids = generateListOfIds();

        List<AbstractNode<String>> nodeList = new ArrayList<>();

        for (int i = 0; i < numberOfNodes; i++) {
            int randomIndex = generator.nextInt(FingerTable.MAX_NODES - i);

            int nodeId = ids.get(randomIndex);
            ids.remove(randomIndex);

            nodeList.add(new Node(nodeId));
        }

        System.out.println("Nodes that have been generated: " + nodeList);
        return nodeList;
    }

    public static List<Integer> generateRandomKeyList(int numberOfKeys) {
        List<Integer> ids = generateListOfIds();
        List<Integer> keyList = new ArrayList<>();

        for (int i = 0; i < numberOfKeys; i++) {
            int randomIndex = generator.nextInt(FingerTable.MAX_NODES - i);
            int keyId = ids.get(randomIndex);
            ids.remove(randomIndex);

            keyList.add(keyId);
        }
        System.out.println("Keys that have been generated: " + keyList);
        return keyList;
    }

    private static List<Integer> generateListOfIds() {
        List<Integer> idList = new ArrayList<>();
        for (int i = 0; i < FingerTable.MAX_NODES; i++) {
            idList.add(i);
        }

        return idList;
    }
}