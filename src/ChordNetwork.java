import API.ChordNode;
import Test.ChordTester;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.List;

public class ChordNetwork {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException {

        int totalNodes = 11;
        int totalKeys = 13;

        String resource = "192.168.0.0";

        List<ChordNode<String>> nodes = ChordTester.generateRandomNodeList(totalNodes);
        List<Integer> keys = ChordTester.generateRandomKeyList(totalKeys);

        System.out.println("\nNode " + nodes.get(0) + " is joining.\n");
        ChordNode node0 = nodes.get(0);
        node0.join(null);

        System.out.println("\nNode " + nodes.get(1) + " is joining.\n");
        ChordNode node1 = nodes.get(1);
        node1.join(node0);

        System.out.println("\nInserting keys with id " + keys.get(0) + " and " + keys.get(1) + " at Nodes.Node "
                + node0 + " and Nodes.Node " + node1 + "\n");

        node0.insert(keys.get(0), resource);
        node1.insert(keys.get(1), resource);

        System.out.println("\nNode " + nodes.get(2) + " is joining.\n");
        ChordNode node2 = nodes.get(2);
        node2.join(node0);

        System.out.println("\nNode " + nodes.get(3) + " is joining.\n");
        ChordNode node3 = nodes.get(3);
        node3.join(node2);

        System.out.println("\nNode " + nodes.get(4) + " is joining.\n");
        ChordNode node4 = nodes.get(4);
        node4.join(node1);

        System.out.println("\nInserting keys with id " + keys.get(2) + " and " + keys.get(3) + "\n");
        node3.insert(keys.get(2), resource);
        node0.insert(keys.get(3), resource);

        System.out.println("\nNode " + nodes.get(5) + " is joining.\n");
        ChordNode node5 = nodes.get(5);
        node5.join(node2);

        System.out.println("\nInserting key with id " + keys.get(4) + " at Nodes.Node " + node4 + "\n");
        node4.insert(keys.get(4), resource);

        System.out.println("\nFinding keys, should return null\n");
        System.out.println(node0.find(0));
        System.out.println("End Finding Keys\n");

        System.out.println("\nInserting several keys\n");
        node3.insert(keys.get(5), resource);
        node0.insert(keys.get(6), resource);
        node0.insert(keys.get(7), resource);
        node1.insert(keys.get(8), resource);
        node2.insert(keys.get(9), resource);

        System.out.println("\nNode " + nodes.get(6) + " is joining.\n");
        ChordNode node6 = nodes.get(6);
        node6.join(node5);

        System.out.println("\nNode " + nodes.get(7) + " is joining.\n");
        ChordNode node7 = nodes.get(7);
        node7.join(node4);

        System.out.println("\nNode " + nodes.get(8) + " is joining.\n");
        ChordNode node8 = nodes.get(8);
        node8.join(node7);

        System.out.println("\nInserting several keys\n");
        node8.insert(keys.get(10), resource);
        node4.insert(keys.get(11), resource);
        node6.insert(keys.get(12), resource);

        System.out.println("\nNode " + nodes.get(9) + " is joining.\n");
        ChordNode node9 = nodes.get(9);
        node9.join(node0);

        System.out.println("\nNode " + nodes.get(10) + " is joining.\n");
        ChordNode node10 = nodes.get(10);
        node10.join(node7);

        System.out.println("\n------------Printing final node tables after inserting all nodes-----------");
        node0.prettyPrint();
        node1.prettyPrint();
        node2.prettyPrint();
        node3.prettyPrint();
        node4.prettyPrint();
        node5.prettyPrint();
        node6.prettyPrint();
        node7.prettyPrint();
        node8.prettyPrint();
        node9.prettyPrint();
        node10.prettyPrint();

        System.out.println("\nFinding keys, should be found\n");
        System.out.println(node5.find(64));
        System.out.println(node2.find(232));

        node5.remove(64);
        node5.remove(232);

        System.out.println("\nShould return null, and so this should print true\n");
        System.out.println(node5.find(64) == null);

        System.out.println("\nShould return null, and so this should print true\n");
        System.out.println(node2.find(232) == null);

        System.out.println("End Finding Keys\n");


        System.out.println("\n------------Nodes leave now, then reprint---------------");

        System.out.println("\nNode " + node9 + "is leaving\n");
        node9.leave();

        System.out.println("\nNode " + node0 + "is leaving\n");
        node0.leave();

        System.out.println("\nNode " + node3 + "is leaving\n");
        node3.leave();

        System.out.println("\nNodes.Node " + node6 + "is leaving\n");
        node6.leave();

        node0.prettyPrint();
        node1.prettyPrint();
        node2.prettyPrint();
        node3.prettyPrint();
        node4.prettyPrint();
        node5.prettyPrint();
        node6.prettyPrint();
        node7.prettyPrint();
        node8.prettyPrint();
        node9.prettyPrint();
        node10.prettyPrint();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out;

        byte[] yourBytes;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(node10);
            out.flush();
            yourBytes = bos.toByteArray();
        } finally {
            bos.close();
        }

        for (byte b : yourBytes)
            System.out.println((double)b + " size " + yourBytes.length);
    }


}
