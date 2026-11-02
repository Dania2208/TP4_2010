### Partie 1 : Triage par file de priorité

#### 1. Choix d'implémentation
Pour trier le tableau, on a utilisé la classe `PriorityQueue` de Java qui fonctionne avec un monceau (heap). 
- Pour l'ordre croissant (`ASCENDING`), on garde le comportement de base (Min-Heap) : on insère tous les éléments puis on utilise `poll()` pour récupérer les valeurs de la plus petite à la plus grande.
- Pour l'ordre décroissant (`DESCENDING`), on utilise un Max-Heap avec `Collections.reverseOrder()` pour que la valeur la plus grande sorte en premier.

*Petite précaution :* On fait une copie du tableau (`Arrays.copyOf`) avant chaque test pour éviter que le premier tri ne modifie le tableau d'origine.

#### 2. Complexité
- **Temps : O(N log N)** 
  Insérer N éléments dans la file de priorité prend O(log k) par élément, soit O(N log N) au total. C'est la même chose pour les extraire un par un avec `poll()`. La complexité globale est donc de O(N log N).
- **Espace : O(N)** 
  La file de priorité stocke temporairement les N éléments du tableau en mémoire.

#### 3. Résultats
Testé avec 20 entiers aléatoires, le programme donne bien les résultats attendus :

=== TRI CROISSANT ===
Avant : [58, 23, 89, 12, 4, 76, 95, 31, 47, 62, 3, 81, 19, 50, 42, 67, 8, 33, 91, 15]
Après : [3, 4, 8, 12, 15, 19, 23, 31, 33, 42, 47, 50, 58, 62, 67, 76, 81, 89, 91, 95]

=== TRI DÉCROISSANT ===
Avant : [58, 23, 89, 12, 4, 76, 95, 31, 47, 62, 3, 81, 19, 50, 42, 67, 8, 33, 91, 15]
Après : [95, 91, 89, 81, 76, 67, 62, 58, 50, 47, 42, 33, 31, 23, 19, 15, 12, 8, 4, 3]




## Partie 2 : Compression de Huffman

### 1. Décodage du fichier secret
- Type de fichier : Image au format Bitmap (`.BMP`), identifiée par la signature magique `BM` présente dans les premiers octets du fichier décodé[cite: 20].
- Contenu : Une illustration en noir et blanc représentant une composition florale dans un vase.
- Méthode : Le fichier `fichierSecret.bin` a été décodé en lisant de manière récursive l'arbre de Huffman sérialisé en début de fichier (`recursiveReadTree`), puis en interprétant le flux de bits (*bitstream*) à l'aide de la taille des données stockée sur 4 octets pour restituer les octets originaux.

### 2. Fréquence des octets (Fichier original décodé)
Voici un extrait des fréquences d'apparition des octets calculées par l'algorithme sur le fichier original :

| Octet (Hex / Décimal) | Caractère / Représentation | Fréquence |
| :--- | :--- | :--- |
| `0x00` (0) | NUL | 3589078 |
| `0x01` (1) | `.` | 186 |
| `0x02` (2) | `.` | 173 |
| `0x03` (3) | `.` | 174 |
| `0x04` (4) | `.` | 153 |
| `0x05` (5) | `.` | 170 |
| `0x06` (6) | `.` | 183 |
| `0x07` (7) | `.` | 213 |
| `0x08` (8) | `.` | 300 |
| `0x09` (9) | `.` | 295 |