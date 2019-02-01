package Nodes.Resource;

import java.util.Map.Entry;

public class ChordEntry<K, V> implements Entry<K, V> {
    private final K key;
    private V resource;

    public ChordEntry(final K key, final V resource) {
        this.key = key;
        this.resource = resource;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return resource;
    }

    public V setValue(final V resource) {
        final V oldValue = this.resource;
        this.resource = resource;
        return oldValue;
    }

    public String toString() {
        return "" + this.key;
    }
}