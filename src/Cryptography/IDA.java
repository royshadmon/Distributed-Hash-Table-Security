package Cryptography;

import Nodes.Node;
import org.apache.commons.lang3.SerializationUtils;

/**
 * https://github.com/rkj2096/IDA
 */
public class IDA {

    private int totalParts, threshold;

    public IDA(int totalParts, int threshold) {
        this.totalParts = totalParts;
        this.threshold = threshold;

    }

    private double[][] encode(double[] message){
        int lengthOfMessage = message.length;

        double[][] a = new double[totalParts][threshold];
        double[][] c = new double[totalParts][lengthOfMessage/ threshold];

        for(int i = 0; i< totalParts; ++i)
            for(int j = 0; j < threshold; ++j)
                a[i][j] = Math.pow(1+i, j);


        for(int i = 0; i< totalParts; ++i)
            for(int j = 0; j<lengthOfMessage/ threshold; ++j)
                for(int k = 0; k< threshold; ++k)
                    c[i][j] += a[i][k] * message[j* threshold +k];

        return c;
    }

    public byte[] getDecodedBytes(double[][] message, int[] fid, int size) {

        double[] decoded = decode(message, fid);

        byte[] bytes = new byte[size];

        for (int i = 0; i < size; i++)
            bytes[i] = (byte) Math.round(decoded[i]);

        return bytes;
    }

    private double[] decode(double[][] message, int[] fid){
        int l = (message.length) * (message[0].length);

        double[][] a = new double[threshold][threshold];
        double[] dm = new double[l];
        double[][] ia;

        for(int i = 0; i < threshold; ++i)
            for(int j = 0; j < threshold; ++j)
                a[i][j] = Math.pow(fid[i], j);

        Inverse in = new Inverse();

        ia = in.invert(a);

        for(int i = 0;i < l; ++i) {

            for (int k = 0; k < threshold; ++k) {
                int index = i/ threshold;
                dm[i] += ia[i % threshold][k] * message[k][index];
            }
        }
        return dm;
    }

    public double[][] encodeBytes(byte[] bytes) {
        double[] message = new double[bytes.length];
        System.out.println("Double array is : ");

        for(int i=0;i< bytes.length;++i) {
            message[i] = bytes[i];
        }

        return encode(message);
    }

    public static void main(String[] args) {

        IDA ida = new IDA(14,10);

        Node node = new Node(5);

        byte[] resourceBytes = SerializationUtils.serialize(node);

        byte[] augmentedBytes = new byte[resourceBytes.length + 10];

        for (int i = 0; i < resourceBytes.length; i++) {
            augmentedBytes[i] = resourceBytes[i];
        }

        double[][] en = ida.encodeBytes(augmentedBytes);

        System.out.println();

        int i = 1;
        for (double[] db : en) {
            System.out.print(i + ": ");
            ida.printArray(db);
            i++;
        }

        System.out.println();
        System.out.println();

        int[] selected = {11,1,2,4,5,10,6,7,8,9};

        en = ida.selectParts(en, selected);

        for (int j = 0; j < selected.length; j++) {
            System.out.print(selected[j] + ": ");
            ida.printArray(en[j]);
        }

        byte[] decodedBytes = ida.getDecodedBytes(en, selected, resourceBytes.length);

        System.out.println();
        System.out.println();

        ida.printArray(resourceBytes);
        ida.printArray(decodedBytes);

        node = SerializationUtils.deserialize(decodedBytes);

        System.out.println(node.getId());
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

    private void printArray(int[] a) {
        for (int b: a) {
            System.out.print(b);
            System.out.print(" ");
        }
        System.out.println();
    }
}