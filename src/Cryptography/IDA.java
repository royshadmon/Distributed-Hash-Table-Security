package Cryptography;

import Nodes.AbstractNode;
import Nodes.Node;
import Trackers.Partitions.Partition;
import org.apache.commons.lang3.SerializationUtils;
import java.security.MessageDigest;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;


/**
 * https://github.com/rkj2096/IDA
 */
public class IDA {


    private int totalParts, threshold, padding;

    public IDA(int totalParts, int threshold, int padding) {
        this.totalParts = totalParts;
        this.threshold = threshold;
        this.padding = padding;
    }

    public List<Partition> encodeBytes(byte[] bytes) {
        double[] bytesForEncoding = new double[bytes.length];
        System.out.println("Double array is : ");

        for(int i=0; i<bytes.length; ++i) {
            bytesForEncoding[i] = bytes[i];
        }
        double[][] encoded = encode(bytesForEncoding);
        List<Partition> partitionList = new ArrayList<>();
        for (int i = 0; i < encoded.length; i++) {
            partitionList.add(new Partition(i+1, ArrayUtils.toObject(encoded[i])));
        }
        return partitionList;
    }

    public byte[] getDecodedBytes(List<Partition> partitionsList) {

        int[] fid = new int[partitionsList.size()];
        double[][] message = new double[this.threshold][partitionsList.get(0).getValue().length];
        for (int i = 0; i < this.threshold; i++) {
            fid[i] = (int) ArrayUtils.toPrimitive(partitionsList.get(i).getKey());
            message[i] = ArrayUtils.toPrimitive(partitionsList.get(i).getValue());
        }


        double[] decoded = decode(message, fid);
        int size = decoded.length - this.padding;
        byte[] bytes = new byte[size];

        for (int i = 0; i < size; i++)
            bytes[i] = (byte) Math.round(decoded[i]);

        return bytes;
    }

    private double[][] encode(double[] message){
        int lengthOfMessage = message.length;

        double[][] a = new double[totalParts][threshold];
        double[][] c = new double[totalParts][lengthOfMessage/ threshold];

        for(int i = 0; i < totalParts; ++i)
            for(int j = 0; j < threshold; ++j)
                a[i][j] = Math.pow(1+i, j);


        for(int i = 0; i < totalParts; ++i)
            for(int j = 0; j < lengthOfMessage/ threshold; ++j)
                for(int k = 0; k < threshold; ++k)
                    c[i][j] += a[i][k] * message[j* threshold +k];

        return c;
    }

    private double[] decode(double[][] message, int[] fid){
        int numberOfBytes = (message.length) * (message[0].length);

        double[][] inversionArray = new double[threshold][threshold];
        double[] decodedBytes = new double[numberOfBytes];
        double[][] invertedArray;

        for(int i = 0; i < threshold; ++i)
            for(int j = 0; j < threshold; ++j)
                inversionArray[i][j] = Math.pow(fid[i], j);

        Inverse in = new Inverse();

        invertedArray = in.invert(inversionArray);

        for(int i = 0;i < numberOfBytes; ++i) {
            for (int k = 0; k < threshold; ++k) {
                int index = i / threshold;
                decodedBytes[i] += invertedArray[i % threshold][k] * message[k][index];
            }
        }

        return decodedBytes;
    }

    private double[][] selectParts(double[][] encoded, int[] arr) {
        int i = 0;

        double[][] selected = new double[arr.length][encoded[0].length];
        for (int integer: arr) {
            selected[i] = encoded[integer - 1];
            i++;
        }

        return selected;
    }

    private void printArray(double[] a) {
        for (double b: a) {
            System.out.print(b);
            System.out.print(" ");
        }
        System.out.println();
    }

    private void printArray(byte[] a) {
        for (byte b: a) {
            System.out.print(b);
            System.out.print(" ");
        }
        System.out.println();
    }




//    public static void main(String[] args) {
//
//        IDA ida = new IDA(5,3);
//
//
//        Node node = new Node(5);
//
//
//        People roy = new People("Roy", "Shadmon", "12345");
//
//
//
//
//        byte[] resourceBytes = SerializationUtils.serialize(roy);
//
//        byte[] augmentedBytes = new byte[resourceBytes.length + 10];
//        System.arraycopy(resourceBytes, 0, augmentedBytes, 0, resourceBytes.length);
//
////        double[][] en = ida.encodeBytes(augmentedBytes);
//
//        System.out.println();
//
//        int i = 1;
//        for (double[] db : en) {
//            System.out.print(i + ": ");
//            ida.printArray(db);
//            i++;
//        }
//
//        System.out.println();
//        System.out.println();
//
//        int[] selected = {5, 2, 1};
//
//        en = ida.selectParts(en, selected);
//
//        for (int j = 0; j < selected.length; j++) {
//            System.out.print(selected[j] + ": ");
//            ida.printArray(en[j]);
//        }
//
//        byte[] decodedBytes = ida.getDecodedBytes(en, selected, resourceBytes.length);
//
//        System.out.println();
//        System.out.println();
//
//        ida.printArray(resourceBytes);
//        ida.printArray(decodedBytes);
//
//        try {
//            roy = SerializationUtils.deserialize(decodedBytes);
//            System.out.println(roy.firstname);
//        } catch (Exception e) {
//            System.out.println("Serialization exception = " + e.getLocalizedMessage());
//        } finally {
//            System.out.println("Done");
//        }
//    }
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