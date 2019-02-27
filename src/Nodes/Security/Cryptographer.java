package Nodes.Security;

import Nodes.Security.IDA.IDA;
import Nodes.Resource.Partitions.Partition;
import Nodes.Resource.Partitions.SealedPartition;

import org.apache.commons.lang3.SerializationUtils;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.io.Serializable;

import java.math.BigInteger;

import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.List;


public class Cryptographer<RESOURCE_TYPE extends Serializable> {

    private static final byte[] initVector = "RandomInitVector".getBytes(StandardCharsets.UTF_8);
    private static final byte[] key = "RandomInitKeyVec".getBytes(StandardCharsets.UTF_8);
    private static final String ENCRYPTION_ALGORITHM = "AES/CBC/PKCS5PADDING";

    private SecretKeySpec keySpec;
    private IvParameterSpec iv;

    // IDA Constants
    public static final int MAX_PARTITIONS = 5;
    public static final int MIN_PARTITIONS = 3;
    private static final int PADDING = 10;

    private IDA ida;

    public Cryptographer() {
        ida = new IDA(MAX_PARTITIONS, MIN_PARTITIONS, PADDING);

        keySpec = new SecretKeySpec(key, "AES");
        iv = new IvParameterSpec(initVector);
    }

    public Cipher getEncryptCipher(){
        return getCipher(Cipher.ENCRYPT_MODE);
    }

    public Cipher getDecryptCipher(){
        return getCipher(Cipher.DECRYPT_MODE);
    }

    private Cipher getCipher(int mode){
        try {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(mode, keySpec, iv);

            return cipher;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String hashString(String input) {
        return hashBytes(input.getBytes());
    }

    public static String hashBytes(byte[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            byte[] messageDigest = md.digest(input);

            BigInteger no = new BigInteger(1, messageDigest);

            StringBuilder hashtext = new StringBuilder(no.toString(16));

            while (hashtext.length() < 32) {
                hashtext.insert(0, "0");
            }

            return hashtext.toString();
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

    public List<SealedPartition> partitionResource (RESOURCE_TYPE resource) {
        byte[] serialized = this.serialize(resource);
        String hash = hashBytes(serialized);
        return sealPartitions(ida.encodeBytes(serialized), hash);
    }

    public RESOURCE_TYPE reassembleResource(List<SealedPartition> partitionList) {
        byte[] resourceByteStream = ida.getDecodedBytes(unsealPartitions(partitionList));
        return SerializationUtils.deserialize(resourceByteStream);
    }

    private List<SealedPartition> sealPartitions(List<Partition> partitions, String hash) {
        List<SealedPartition> sealedPartitions = new ArrayList<>();

        for (Partition p : partitions) {
            try {
                SealedObject ob = new SealedObject(p, getEncryptCipher());

                SealedPartition sp = new SealedPartition(hash, p.getKey(), ob);
                sealedPartitions.add(sp);
                hash = hashString(hash);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return sealedPartitions;
    }

    private List<Partition> unsealPartitions(List<SealedPartition> sealedPartitions) {
        List<Partition> partitions = new ArrayList<>();

        for (SealedPartition sp: sealedPartitions) {
            try {
                Partition p = (Partition) sp.getObject(getDecryptCipher());
                partitions.add(p);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return partitions;
    }

    private byte[] serialize (RESOURCE_TYPE resource) {
        byte[] resourceBytes = SerializationUtils.serialize(resource);
        byte[] augmentedBytes = new byte[resourceBytes.length + PADDING];
        System.arraycopy(resourceBytes, 0, augmentedBytes, 0, resourceBytes.length);

        return augmentedBytes;
    }

    public static void main(String[] args) {

        People roy = new People("Roy", "Shadmon", "12345");
        Cryptographer<People> cryptographer = new Cryptographer<>();

        List<SealedPartition> sp = cryptographer.partitionResource(roy);
        SealedPartition s = sp.get(4);

        String key = s.getPartitionName();

        sp.forEach(p -> {
            if (p.getPartitionName().equals(key))
                System.out.println(p.getChordId());
        });

        sp.forEach(p -> System.out.println(p.getChordId() + " | " + p.getPartitionName()));

        roy = cryptographer.reassembleResource(sp);

        System.out.println(roy.firstname);
        String hash = Cryptographer.hashBytes(cryptographer.serialize(roy));

        for (int i = 0; i < 5; i++) {
            System.out.println(hash);
            hash = Cryptographer.hashString(hash);
        }
    }
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