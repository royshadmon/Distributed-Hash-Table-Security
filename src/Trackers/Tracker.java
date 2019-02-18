package Trackers;

import API.ChordTracker;

public class Tracker implements ChordTracker {

    private ChordCache cache;

    Tracker () {
        cache = new ChordCache();
    }

    public Integer assignId() {
        return -1;
    }





    /* Insert
    *
    * Gets selected node from cache cache
    *
    * Takes a resource and the assigned resource key from the node as input
    *
    * Partition is the IDA, assignID portions
    *
    * List will have partitionID, partition
    *
    * - selected = ChordCache.pop()
    * - list = this.partition(resource)
    * - keymap.put for each element in list
    * - selected.insert-> (for all elements in list)    *
    *
    *
    *
    * */

    /* Partition
    *
    * Takes in the resource
    *
    * Calls IDA on resource (which also does all the encoding work)
    *
    * - listOfPartitions = IDA(resource)
    * - return listOfPartitions
    *
    *
    *
    * */



    /* Lookup
    *
    * Gets selected node from the cache cache
    * Takes in a key from the requesting Node
    * Tracker finds key in hash table (keymap)
    * For each key, query -> gets the partitions -> adds each partition and its key to a list
    * Call inverse on list
    * Returns resource to node (encryted? PKC?)
    *
    * - selected = ChordCache.pop()
    * - listOfPartitionKeys = keymap.get(resourceKey)
    * - listOfPartitions = query (for each partition key)
    * - resource = inverse(listOfPartitons)
    * - return resource
    *
    * */

    /* */

    /* */

    /* */

    /* */

    /* */

    /* */

}


