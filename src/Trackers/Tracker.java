package Trackers;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.ArrayList;
import Cryptography.IDA;
import API.ChordNode;
import API.ChordTracker;
import Trackers.Partitions.Partition;
import org.apache.commons.lang3.SerializationUtils;

public class Tracker<RESOURCE_TYPE extends Serializable> implements ChordTracker {


    private static final int MAX_PARTITIONS = 5;
    private static final int MIN_PARTITIONS = 3;
    private static final int PADDING = 10;
    private ChordCache cache;
    private IDA ida;

    Tracker () {
        cache = new ChordCache();
        ida = new IDA(MAX_PARTITIONS, MIN_PARTITIONS, PADDING);
    }

    public Integer assignId() {
        return -1;
    }



    //https://www.geeksforgeeks.org/sha-1-hash-in-java/
    public static int encryptThisString(String input)
    {
        try {

            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }

            BigInteger key = new BigInteger(hashtext, 16);
            int keyId = key.mod(new BigInteger("256", 10)).intValue();
            return keyId;
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
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

//    private RESOURCE_TYPE rebuildResource (List<Partition> partitions) {
//        ida.getDecodedBytes(partitions);
//    }

    private List<Partition> partitionResource (RESOURCE_TYPE resource) {

        List<Partition> partitions = new ArrayList<>();

        byte[] serilaized = this.serialize(resource);

        return ida.encodeBytes(serilaized);
    }

    private RESOURCE_TYPE reassemblePartition (List<Partition> partitionList) {
        byte[] resourceByteStream = ida.getDecodedBytes(partitionList);
        return SerializationUtils.deserialize(resourceByteStream);
    }

    private byte[] serialize (RESOURCE_TYPE resource) {

        byte[] resourceBytes = SerializationUtils.serialize(resource);
        byte[] augmentedBytes = new byte[resourceBytes.length + PADDING];
        System.arraycopy(resourceBytes, 0, augmentedBytes, 0, resourceBytes.length);

        return augmentedBytes;
    }

    public static void main(String[] args) {

        People roy = new People("Roy", "Shadmon", "12345");

        Tracker tracker = new Tracker();
        List<Partition> p = tracker.partitionResource(roy);
        p.forEach(System.out :: println);

        People roy1 = (People) tracker.reassemblePartition(p);
        System.out.println(roy1.firstname);
    }
  
    /* Lookup
    *
    * Gets selected node from the cache cache
    * Takes in a key from the requesting AbstractNode
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

class People implements Serializable {

    String firstname;
    String lastname;
    String password;

    People(String firstname, String lastname, String password){
        this.firstname = firstname;
        this.lastname = lastname;
        this.password = password;
    }

}