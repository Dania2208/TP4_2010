import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.PriorityQueue;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class HuffmanCompression {   
    
    // TODO: À compléter
    private class HuffmanNode implements Comparable<HuffmanNode> {
        int frequency;
        byte character;
        HuffmanNode left;
        HuffmanNode right;

        public HuffmanNode(byte character, int frequency) {
            this.character = character;
            this.frequency = frequency;
            this.left = null;
            this.right = null;
        }

        public HuffmanNode(int frequency, HuffmanNode left, HuffmanNode right) {
            this.frequency = frequency;
            this.character = 0;
            this.left = left;
            this.right = right;
        }
        
        public boolean isLeaf() {
            return this.left == null && this.right == null;
        }

        @Override
        public int compareTo(HuffmanNode other) {
            return Integer.compare(this.frequency, other.frequency);
        }
    }
    
    // TODO: Donne la hauteur d'un arbre, utiliser pour les statistiques
    private int computeMaxDepth(HuffmanNode node) {
        if (node == null) {
            return 0;
        }
        if (node.isLeaf()) {
            return 1;
        }
        return 1 + Math.max(computeMaxDepth(node.left), computeMaxDepth(node.right));       
    }
    
    private Map<Byte, String> huffmanCodes = new HashMap<>();

    // TODO: Génère les codes pour chaque octets
    //       Changer les entrées au besoin
    private void generateHuffmanCodes(HuffmanNode node, String code) {
        if (node == null) {
            return;
        }
        if (node.isLeaf()) {
            huffmanCodes.put(node.character, code.isEmpty() ? "0" : code);
            return;
        }
        generateHuffmanCodes(node.left, code + "0");
        generateHuffmanCodes(node.right, code + "1");
    }
    
    private BitStreamWriter huffmanWriter;
    private BitStreamReader huffmanReader;
    
    // TODO: Fait l'écriture de l'arbre dans le fichier de sortie (huffmanWriter)
    //       Un noeud intermédiare écrit un bit à 0 et une feuille un bit à 1
    private void recursiveWriteTree(HuffmanNode node) throws IOException {
        if (node == null) {
            return;
        }
        if (node.isLeaf()) {
        huffmanWriter.writeBit((byte) 1); // Ajout du cast (byte)
        huffmanWriter.writeByte(node.character);
    } else {
        huffmanWriter.writeBit((byte) 0); // Ajout du cast (byte)
        recursiveWriteTree(node.left);
        recursiveWriteTree(node.right);
    }
    }
    
    // TODO: Fait la lecture de l'arbre dans le fichier d'entrée (huffmanReader)
    private HuffmanNode recursiveReadTree() throws IOException {
        int bit = huffmanReader.readBit();
        if (bit == 1) {
            byte val = huffmanReader.readByte();
            return new HuffmanNode(val, 0);
        } else if (bit == 0) {
            HuffmanNode left = recursiveReadTree();
            HuffmanNode right = recursiveReadTree();
            return new HuffmanNode(0, left, right);
        }
        return null;
    }
    
    
    // TODO: Écrit le message compresser dans le fichier de sortie
    private void writeHuffmanCompressedFile(HuffmanNode tree, byte[] data, String outputFilePath) {
        try {
            huffmanWriter = new BitStreamWriter(outputFilePath);            
            
            
            recursiveWriteTree(tree);
            
            
            huffmanCodes.clear();
            generateHuffmanCodes(tree, "");
            
            
            StringBuilder sb = new StringBuilder();
            for (byte b : data) {
                sb.append(huffmanCodes.get(b));
            }
            String bitString = sb.toString();
            
            
            int dataLength = bitString.length();
            for (int i = 3; i >= 0; i--) {
                huffmanWriter.writeByte((byte) ((dataLength >> (i * 8)) & 0xFF));
            }
            
            
            for (int i = 0; i < bitString.length(); i++) {
                huffmanWriter.writeBit(bitString.charAt(i) == '1' ? (byte) 1 : (byte) 0);
            }
                        
            huffmanWriter.close();            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // TODO: Fait la lecture d'un fichier compresser par l'algorithm de Huffman
    private byte[] readHuffmanCompressedFile(String inputFilePath) {
        try {
            huffmanReader = new BitStreamReader(inputFilePath);
            
            
            HuffmanNode tree = recursiveReadTree();
            
            
            int sizeInBits = 0;
            for (int i = 0; i < 4; i++) {
                int b = huffmanReader.readByte() & 0xFF;
                sizeInBits = (sizeInBits << 8) | b;
            }
            
            
            List<Byte> decodedBytes = new ArrayList<>();
            HuffmanNode currentNode = tree;
            
            for (int i = 0; i < sizeInBits; i++) {
                int bit = huffmanReader.readBit();
                if (bit == 0) {
                    currentNode = currentNode.left;
                } else {
                    currentNode = currentNode.right;
                }
                
                if (currentNode.isLeaf()) {
                    decodedBytes.add(currentNode.character);
                    currentNode = tree;
                }
            }
            
            huffmanReader.close();
            
            byte[] result = new byte[decodedBytes.size()];
            for (int i = 0; i < decodedBytes.size(); i++) {
                result[i] = decodedBytes.get(i);
            }
            return result; 
            
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // TODO: Calcul la fréquence de chaque occurence d'un octet dans le fichier
    private int[] getFrequencies(String inputFilePath) throws IOException {
        int[] frequencies = new int[256];
        FileInputStream fis = new FileInputStream(inputFilePath);
        int byteValue;
        while ((byteValue = fis.read()) != -1) {
            frequencies[byteValue & 0xFF]++;
        }
        fis.close();
        return frequencies;
    }
    
    // TODO: Faite l'arbre de Huffman à l'aide d'une file de priorité
    private HuffmanNode generateHuffmanTreeWithPriorityQueue(int[] frequencies) {
        PriorityQueue<HuffmanNode> pq = new PriorityQueue<>();
        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i] > 0) {
                pq.add(new HuffmanNode((byte) i, frequencies[i]));
            }
        }
        
        if (pq.isEmpty()) {
            return null;
        }
        
        if (pq.size() == 1) {
            HuffmanNode single = pq.poll();
            HuffmanNode emptyLeaf = new HuffmanNode((byte) (single.character + 1), 0);
            return new HuffmanNode(single.frequency, single, emptyLeaf);
        }
        
        while (pq.size() > 1) {
            HuffmanNode left = pq.poll();
            HuffmanNode right = pq.poll();
            HuffmanNode parent = new HuffmanNode(left.frequency + right.frequency, left, right);
            pq.add(parent);
        }
        
        return pq.poll();
    }
    
    // TODO: Faite l'arbre de Huffman sans l'aide d'une file de priorité
    private HuffmanNode generateHuffmanTreeWithoutPriorityQueue(int[] frequencies) {
        List<HuffmanNode> nodes = new ArrayList<>();
        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i] > 0) {
                nodes.add(new HuffmanNode((byte) i, frequencies[i]));
            }
        }
        
        if (nodes.isEmpty()) {
            return null;
        }
        
        if (nodes.size() == 1) {
            HuffmanNode single = nodes.get(0);
            HuffmanNode emptyLeaf = new HuffmanNode((byte) (single.character + 1), 0);
            return new HuffmanNode(single.frequency, single, emptyLeaf);
        }
        
        while (nodes.size() > 1) {
            nodes.sort((n1, n2) -> Integer.compare(n1.frequency, n2.frequency));
            HuffmanNode left = nodes.remove(0);
            HuffmanNode right = nodes.remove(0);
            
            HuffmanNode parent = new HuffmanNode(left.frequency + right.frequency, left, right);
            nodes.add(parent);
        }
        
        return nodes.get(0);
    }
    
    // TODO: Compresse le fichier avec l'algorithme de Huffman, puis l'écrit sur le disque
    //       Faite une version avec et sans file de priorité
    public void compress(String inputFilePath, String outputFilePath, boolean avecFile) {
    try {
        int[] frequencies = getFrequencies(inputFilePath);

        HuffmanNode tree = avecFile
                ? generateHuffmanTreeWithPriorityQueue(frequencies)
                : generateHuffmanTreeWithoutPriorityQueue(frequencies);

        FileInputStream fis = new FileInputStream(inputFilePath);
        byte[] data = fis.readAllBytes();
        fis.close();

        writeHuffmanCompressedFile(tree, data, outputFilePath);
    } catch (IOException e) {
        e.printStackTrace();
    }
}
    
    // TODO: Décompresse le fichier avec l'algorithme de Huffman, puis l'écrit sur le disque.
    public void decompress(String inputFilePath, String outputFilePath) {       
        byte[] decodedData = readHuffmanCompressedFile(inputFilePath);
        if (decodedData != null) {
            try {
                FileOutputStream fos = new FileOutputStream(outputFilePath);
                fos.write(decodedData);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void main(String[] args) {
    HuffmanCompression hc = new HuffmanCompression();

    try {
        // ===== Partie 2 : fréquences du fichier secret décodé =====
        int[] freqs = hc.getFrequencies("fichierSecretDecode.bmp");
        System.out.println("| Octet (Code ASCII / Hex) | Caractère / Description | Fréquence |");
        System.out.println("| :--- | :--- | :--- |");
        for (int i = 0; i < freqs.length; i++) {
            if (freqs[i] > 0) {
                char c = (i >= 32 && i <= 126) ? (char) i : '.';
                System.out.printf("| 0x%02X (%3d) | '%c' | %d |\n", i, i, c, freqs[i]);
            }
        }

        // ===== Partie 2 : compression de mon fichier =====
        hc.compress("monFichier.txt", "monFichier.bin", true);
        hc.decompress("monFichier.bin", "monFichier_verif.txt");

        byte[] original = java.nio.file.Files.readAllBytes(java.nio.file.Path.of("monFichier.txt"));
        byte[] verif    = java.nio.file.Files.readAllBytes(java.nio.file.Path.of("monFichier_verif.txt"));
        System.out.println(java.util.Arrays.equals(original, verif)
                ? "OK : fichier identique a l'original"
                : "ERREUR : fichiers differents");
        System.out.println("Taille originale  : " + original.length + " octets");
        System.out.println("Taille compressee : "
                + java.nio.file.Files.size(java.nio.file.Path.of("monFichier.bin")) + " octets");
    } catch (Exception e) {
        e.printStackTrace();
    }
}
    
}