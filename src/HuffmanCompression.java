import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.PriorityQueue;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/*
 * Variables utilisées pour les complexités :
 *   n : nombre d'octets du fichier original
 *   k : nombre d'octets différents dans le fichier (k <= 256)
 *   h : profondeur de l'arbre de Huffman (h <= k)
 */

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
    // Complexité : O(k), on visite chacun des 2k-1 nœuds une fois
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
    // Complexité : O(k * h)
    //   - On visite les 2k-1 nœuds, et chaque code construit a au plus h bits
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
    // Complexité : O(k), on visite chacun des 2k-1 nœuds une fois
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
    // Complexité : O(k), on lit chacun des 2k-1 nœuds une fois
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
    // Complexité : O(k * h + n * h)
    //   - Écriture de l'arbre O(k) et génération des codes O(k * h)
    //   - Pour chacun des n octets, on ajoute son code d'au plus h bits : O(n * h)
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
    // Complexité : O(k + n * h)
    //   - Lecture de l'arbre : O(k)
    //   - On lit chaque bit des données une fois, au plus h bits par octet : O(n * h)
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
    // Complexité : O(n), on lit chaque octet du fichier une fois
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
    // Complexité : O(k log k)
    //   - k insertions dans la file : O(k log k)
    //   - k-1 fusions, chacune avec 2 poll + 1 add en O(log k) : O(k log k)
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
    // Complexité : O(k² log k)
    //   - k-1 fusions, et à chaque fusion on trie toute la liste en O(k log k)
    //   - Total : (k-1) * O(k log k) = O(k² log k)
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
    // Complexité totale de la compression :
    //   - Avec file : O(n) + O(k log k) + O(k * h) + O(n * h) = O(n * h + k log k)
    //   - Sans file : O(n) + O(k² log k) + O(k * h) + O(n * h) = O(n * h + k² log k)
    // Comme k <= 256, la construction de l'arbre reste petite comparée à l'encodage des n octets :
    // pour un gros fichier, c'est O(n * h) qui domine.
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
    // Complexité : O(k + n * h)
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

    // Partie 3

    // Complexité : O(n)
    private int[] getFrequencies(byte[] data) {
        int[] frequencies = new int[256];
        for (byte b : data) {
            frequencies[b & 0xFF]++;
        }
        return frequencies;
    }

    private int countLeaves(HuffmanNode node) {
        if (node == null) return 0;
        if (node.isLeaf()) return 1;
        return countLeaves(node.left) + countLeaves(node.right);
    }

    private void generateTestFile(String path, int size, int distinct) throws IOException {
        java.util.Random random = new java.util.Random(42);
        byte[] data = new byte[size];
        for (int i = 0; i < size; i++) {
            data[i] = (byte) random.nextInt(distinct);
        }
        java.nio.file.Files.write(java.nio.file.Path.of(path), data);
    }

    private void printStatistics(String inputFilePath) throws IOException {
        byte[] data = java.nio.file.Files.readAllBytes(java.nio.file.Path.of(inputFilePath));
        final int RUNS = 20;

        for (int i = 0; i < 5; i++) {
            generateHuffmanTreeWithPriorityQueue(getFrequencies(data));
            generateHuffmanTreeWithoutPriorityQueue(getFrequencies(data));
        }

        long treeWith = 0, treeWithout = 0, total = 0;
        HuffmanNode tree = null;
        long dataBits = 0;
        for (int r = 0; r < RUNS; r++) {
            long start = System.nanoTime();
            int[] frequencies = getFrequencies(data);

            long t0 = System.nanoTime();
            tree = generateHuffmanTreeWithPriorityQueue(frequencies);
            long t1 = System.nanoTime();
            generateHuffmanTreeWithoutPriorityQueue(frequencies);
            long t2 = System.nanoTime();

            huffmanCodes.clear();
            generateHuffmanCodes(tree, "");
            dataBits = 0;
            for (byte b : data) {
                dataBits += huffmanCodes.get(b).length();
            }
            long end = System.nanoTime();

            treeWith += t1 - t0;
            treeWithout += t2 - t1;
            total += (end - start) - (t2 - t1);
        }

        int leaves = countLeaves(tree);
        long treeBits = (leaves - 1) + 9L * leaves;
        long compressedBytes = (treeBits + 32 + dataBits + 7) / 8;

        System.out.printf("%-28s | %10d | %10d | %4d | %5d | %7d | %10d | %11.1f | %12.1f | %10.1f%n",
                inputFilePath, data.length, compressedBytes, leaves, computeMaxDepth(tree),
                (treeBits + 7) / 8, (dataBits + 7) / 8,
                treeWith / 1000.0 / RUNS, treeWithout / 1000.0 / RUNS, total / 1000.0 / RUNS);
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

        // ===== Partie 3 : création des fichiers de test =====
        String text = java.nio.file.Files.readString(java.nio.file.Path.of("monFichier.txt"));
        java.nio.file.Files.writeString(java.nio.file.Path.of("texteGros.txt"), text.repeat(20000));

        int[] distinctCounts = {2, 16, 64, 256};
        for (int k : distinctCounts) {
            hc.generateTestFile("bin_1Ko_" + k + ".bin", 1000, k);
            hc.generateTestFile("bin_1Mo_" + k + ".bin", 1000000, k);
        }

        // ===== Partie 3 : tableau des statistiques =====
        System.out.println();
        System.out.println("Fichier | Taille (o) | Compr. (o) | Diff | Prof. | Arbre(o) | Données(o) | Avec file(µs) | Sans file(µs) | Total(µs)");
        String[] testFiles = {
                "monFichier.txt", "texteGros.txt", "fichierSecretDecode.bmp",
                "bin_1Ko_2.bin", "bin_1Ko_16.bin", "bin_1Ko_64.bin", "bin_1Ko_256.bin",
                "bin_1Mo_2.bin", "bin_1Mo_16.bin", "bin_1Mo_64.bin", "bin_1Mo_256.bin"
        };
        for (String file : testFiles) {
            hc.printStatistics(file);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    }
}