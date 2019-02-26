package Nodes.Security.IDA;

import Nodes.Resource.Partitions.Partition;

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

        for(int i=0; i<bytes.length; ++i) {
            bytesForEncoding[i] = bytes[i];
        }

        double[][] encoded = encode(bytesForEncoding);

        List<Partition> partitionList = new ArrayList<>();

        for (int i = 0; i < encoded.length; i++) {
            partitionList.add(new Partition(i+1, encoded[i]));
        }
        return partitionList;
    }

    public byte[] getDecodedBytes(List<Partition> partitionsList) {

        int[] fid = new int[partitionsList.size()];
        double[][] message = new double[this.threshold][partitionsList.get(0).getValue().length];
        for (int i = 0; i < this.threshold; i++) {
            fid[i] = (int) ArrayUtils.toPrimitive(partitionsList.get(i).getKey());
            message[i] = partitionsList.get(i).getValue();
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
}