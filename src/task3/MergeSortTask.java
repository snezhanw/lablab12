package task3;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class MergeSortTask extends RecursiveTask<int[]> {

    private final int[] array;
    private static final int THRESHOLD = 10_000;

    public MergeSortTask(int[] array) {
        this.array = array;
    }

    @Override
    protected int[] compute() {
        if (array.length <= THRESHOLD) {
            int[] sorted = array.clone();
            Arrays.sort(sorted);
            return sorted;
        }

        int mid = array.length / 2;
        int[] left = Arrays.copyOfRange(array, 0, mid);
        int[] right = Arrays.copyOfRange(array, mid, array.length);

        MergeSortTask leftTask = new MergeSortTask(left);
        MergeSortTask rightTask = new MergeSortTask(right);

        leftTask.fork();
        int[] rightResult = rightTask.compute();
        int[] leftResult = leftTask.join();

        return merge(leftResult, rightResult);
    }

    private int[] merge(int[] left, int[] right) {
        int[] result = new int[left.length + right.length];
        int i = 0, j = 0, k = 0;

        while (i < left.length && j < right.length) {
            if (left[i] <= right[j]) result[k++] = left[i++];
            else result[k++] = right[j++];
        }

        while (i < left.length) result[k++] = left[i++];
        while (j < right.length) result[k++] = right[j++];

        return result;
    }

    public static void main(String[] args) {
        System.out.println("=== Параллельная сортировка слиянием ===");

        int size = 1_000_000;
        int threshold = THRESHOLD;
        Random rnd = new Random();

        System.out.println("Размер массива: " + size + " элементов");
        System.out.println("Порог разделения: " + threshold + " элементов");
        System.out.println("Генерация случайного массива...");

        int[] arr = new int[size];
        for (int i = 0; i < size; i++) arr[i] = rnd.nextInt();

        System.out.println("Первые 10 элементов до сортировки: " + Arrays.toString(Arrays.copyOf(arr, 10)));

        int[] arrCopy = arr.clone();

        //стандартная сортировка
        long s1 = System.currentTimeMillis();
        Arrays.sort(arrCopy);
        long e1 = System.currentTimeMillis();
        System.out.println("\nСтандартная сортировка (Arrays.sort):");
        System.out.println("Время выполнения: " + (e1 - s1) + " мс");
        System.out.println("Первые 10 элементов: " + Arrays.toString(Arrays.copyOf(arrCopy, 10)));

        //параллельная сортировка
        ForkJoinPool pool = ForkJoinPool.commonPool();
        long s2 = System.currentTimeMillis();
        int[] result = pool.invoke(new MergeSortTask(arr));
        long e2 = System.currentTimeMillis();
        System.out.println("\nПараллельная сортировка (Fork/Join):");
        System.out.println("Время выполнения: " + (e2 - s2) + " мс");
        System.out.println("Первые 10 элементов: " + Arrays.toString(Arrays.copyOf(result, 10)));

        boolean correct = Arrays.equals(result, arrCopy);
        System.out.println("\nПроверка корректности: " + (correct ? "✓ Массив отсортирован правильно" : "✗ Ошибка сортировки"));

        double speedup = (double) (e1 - s1) / (e2 - s2);
        System.out.printf("Ускорение: %.2fx\n", speedup);
    }
}
