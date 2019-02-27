package Nodes.Security;

/**
 *
 * Definition of a Cache that contains nodes already present in the network.
 *
 */

public class ChordCache {

//    private LinkedList<ChordNode> queue;
//    private static Integer MAX_SIZE = FingerTable.MAX_ENTRIES;
//
//    ChordCache() {
//        queue = new LinkedList<>();
//    }
//
//    ChordNode getNode(ChordNode requester) {
//        if (!isFull()) {
//            System.out.println("Special State");
//            ChordNode top = this.queue.peek();
//            this.add(requester);
//            return top;
//        } else {
//            return getNodeForOperation(requester);
//        }
//    }
//
//    private ChordNode getNodeForOperation(ChordNode requester) {
//        System.out.println("General State");
//        if (!this.queue.contains(requester)) {
//            this.add(requester);
//            return queue.poll();
//        }
//
//        if (this.queue.peek().equals(requester)) {
//
//        }
//
//        ChordNode selected = queue.poll();
//        this.add(requester);
//
//        return selected;
//    }
//
//    /**
//     * Adds the node to the cache.
//     *
//     * If the node is present in the cache, then adds it to the front of the queue.
//     *
//     * @param node that is being added.
//     */
//    void add(ChordNode node) {
//        if (!this.queue.contains(node))
//            this.queue.add(node);
//        else {
//            this.queue.remove(node);
//            this.queue.addFirst(node);
//        }
//    }
//
//    int size() {
//        return this.queue.size();
//    }
//
//    public boolean isFull() {
//        return this.size() == MAX_SIZE;
//    }
//
//    public static void main(String[] args) {
//        ChordCache q = new ChordCache();
//
//        ChordNode node0 = new Node(0);
//        node0.join(null);
//
//        ChordNode node1 = new Node(1);
//        node1.join(node0);
//
//        ChordNode node2 = new Node(2);
//        node2.join(node1);
//
//        ChordNode node3 = new Node(3);
//        node3.join(node2);
//
//        ChordNode node4 = new Node(4);
//        node4.join(node2);
//
//        ChordNode node5 = new Node(5);
//        node5.join(node2);
//
//        ChordNode node6 = new Node(6);
//        node6.join(node2);
//
//        ChordNode node7 = new Node(7);
//        node7.join(node2);
//
//        ChordNode node8 = new Node(8);
//        node8.join(node7);
//
//        ChordNode node9 = new Node(9);
//        node9.join(node7);
//
//
//        q.getNode(node0);
//        q.getNode(node1);
//        q.getNode(node2);
//        q.getNode(node3);
//        q.getNode(node4);
//        q.getNode(node5);
//        q.getNode(node6);
//        q.getNode(node7);
//
//        ChordNode sel;
//        sel = q.getNode(node8);
//
//        sel = q.getNode(node7);
//
//        sel = q.getNode(node9);
//        sel = q.getNode(node9);
//        sel = q.getNode(node9);
//        sel = q.getNode(node9);
//
//        q.queue.forEach(node -> System.out.println("AbstractNode in Queue " + node));
//
//        System.out.println(sel);
//    }

}
