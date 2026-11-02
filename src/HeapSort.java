
public class HeapSort {

	private enum SortingType {
		ASCENDING,
		DESCENDING
	}
	
	static void heapSort(int[] arrayToSort, SortingType sort) {
		// TODO: Faire le triage par monceau sur le tableau en entrée
		if (arrayToSort == null || arrayToSort.length <= 1) {
            return;
        }

        if (sort == SortingType.ASCENDING) {
            
            PriorityQueue<Integer> pq = new PriorityQueue<>();
            for (int val : arrayToSort) {
                pq.add(val);
            }
            for (int i = 0; i < arrayToSort.length; i++) {
                arrayToSort[i] = pq.poll();
            }

        } else {
            
            PriorityQueue<Integer> pq = new PriorityQueue<>(Collections.reverseOrder());
            for (int val : arrayToSort) {
                pq.add(val);
            }
            for (int i = 0; i < arrayToSort.length; i++) {
                arrayToSort[i] = pq.poll();
            }
        }
    }
		
	}
	
	public static void main(String[] args) {
		
		// TODO: Faite le triage par monceau d'un tableau d'entier.
		//       Le tableau d'entier peut être généré aléatoirement et
		//       avoir une taille considérable (20 éléments désordonnées).
		//       Trier le même tableau d'entrée en ordre croissant et décroissant
		//       (faite une copie).
		//       Afficher les valeurs avant et après le triage.		
		// 1. Génération d'un tableau de 20 éléments désordonnés aléatoirement

        int size = 20;
        int[] originalArray = new int[size];
        Random random = new Random();
        
        for (int i = 0; i < size; i++) {
            originalArray[i] = random.nextInt(100); 
        }
        

        int[] arrayAsc = Arrays.copyOf(originalArray, originalArray.length);
        System.out.println("=== TRI CROISSANT ===");
        System.out.println("Avant le tri : " + Arrays.toString(arrayAsc));
        heapSort(arrayAsc, SortingType.ASCENDING);
        System.out.println("Après le tri : " + Arrays.toString(arrayAsc));
        
        System.out.println();
        
    
        int[] arrayDesc = Arrays.copyOf(originalArray, originalArray.length);
        System.out.println("=== TRI DÉCROISSANT ===");
        System.out.println("Avant le tri : " + Arrays.toString(arrayDesc));
        heapSort(arrayDesc, SortingType.DESCENDING);
        System.out.println("Après le tri : " + Arrays.toString(arrayDesc));
	
}
