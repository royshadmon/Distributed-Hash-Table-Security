package Cryptography;

import Nodes.Node;
import org.apache.commons.lang3.SerializationUtils;

/**
 * https://github.com/rkj2096/IDA
 */
public class IDA {

    private int n, m, p;

    public IDA(int n,int m, int p) {
        this.n = n;
        this.m = m;
        this.p = p;
    }

    public double[][] encode(double[] message){
        int l = message.length;

        double[][] a = new double[n][m];
        double[][] c = new double[n][l/m];

        for(int i = 0;i<n;++i)
            for(int j = 0;j < m;++j)
                a[i][j] = Math.pow(1+i,j);


        for(int i=0; i<n; ++i)
            for(int j=0; j<l/m; ++j)
                for(int k=0; k<m; ++k)
                    c[i][j] += a[i][k] * message[j*m+k];

        return c;
    }

    public double[] decode(double[][] message, int[] fid){
        int l = (message.length) * (message[0].length);

        double[][] a = new double[m][m];
        double[] dm = new double[l];
        double[][] ia;

        for(int i = 0; i < m; ++i)
            for(int j = 0; j < m; ++j)
                a[i][j] = Math.pow(fid[i], j);

        Inverse in = new Inverse();

        ia = in.invert(a);

        for(int i = 0;i < l; ++i) {

            for (int k = 0; k < m; ++k) {
                int index = i/m;
                dm[i] += ia[i % m][k] * message[k][index];
            }
        }
        return dm;
    }

    public String sencode(String st){

        String en="";

        String[] mess=st.split(" ");

        int l = mess.length;
        double[] message = new double[l];

        for(int i=0;i<l;++i)
            message[i]=Double.parseDouble(mess[i]);

        double[][] em = encode(message);

        for(int i=0;i<n;++i){
            en += (i+1)+":";
            for(int j=0;j<l/m;++j){
                en += em[i][j]+" ";
            }

            en+="\n";
        }

        return en;
    }

    public String sdecode(String ms){
        String rme = "";

        String[] sms = ms.split("\n");
        int numberOfParts = sms.length;

        String[] id = new String[numberOfParts];

        for(int i=0;i<numberOfParts;++i){
            String[] partitions = sms[i].split(":");

            id[i] = partitions[0];

            sms[i] = partitions[1];
        }

        String fl[] = sms[0].split(" ");

        int partLength = fl.length;

        String[][] SMSs = new String[numberOfParts][partLength];
        int[] fid = new int[numberOfParts];

        double[][] mess = new double[numberOfParts][partLength];

        for(int i=0;i<numberOfParts;++i)
            SMSs[i] = sms[i].split(" ");

        for(int i = 0; i < numberOfParts;++i)
            fid[i] = Integer.parseInt(id[i]);

        for(int i = 0;i < numberOfParts;++i)
            for(int j = 0;j < partLength;++j)
                mess[i][j] = Double.parseDouble(SMSs[i][j]);


        double []rm = decode(mess, fid);

        for (double v : rm) {
            rme += Math.round(v) + " ";
        }

        return rme;
    }

    public static void main(String[] args) {
        Node<String> node = new Node<>(90);
        node.join(null);

        String resource = "Resource";
        int key = 1;

        node.insert(key, resource);
        Node<String> node1 = new Node<>(89);
        node1.join(node);

        /*
            Serializing and saving state
         */
        byte[] originalNodeBytes = SerializationUtils.serialize(node);
        System.out.println("Converted object into " + originalNodeBytes.length + " number of bytes");
        StringBuilder sb = new StringBuilder();

        for (byte b: originalNodeBytes)
            sb.append(b).append(" ");

        String orig = sb.toString();
        System.out.println(orig);

        sb.append("0"); // Padding with a single zero to avoid index out of bounds error

        IDA ida = new IDA(14,10, 131);
        String encoded = ida.sencode(sb.toString());

        StringBuilder ms = new StringBuilder();

        String[] encodedArray = encoded.split("\n");

        for (int i = 0; i < 10; i++) {
            ms.append(encodedArray[i]).append("\n");
        }

        String tenParts = ms.toString();

        String decoded = ida.sdecode(tenParts);
        String[] bytes = decoded.split(" ");

        byte[] reconstructed = new byte[originalNodeBytes.length];
        StringBuilder reconstructedString = new StringBuilder();

        for (int i = 0; i < reconstructed.length; i++) {
            reconstructed[i] = Byte.parseByte(bytes[i]);
            reconstructedString.append(reconstructed[i]).append(" ");
        }

        System.out.println("Reconstructing Node from " + reconstructed.length + " bytes");
        System.out.println(reconstructedString);

        /*
            Reconstructing and checking
         */
        Node<String> node2 = SerializationUtils.deserialize(reconstructed);

        System.out.println(node2.find(key));
        System.out.println(node2.getId());
        System.out.println("node2.getSuccessor() = " + node2.getSuccessor());
    }
}