package task1;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ArraySumTask extends RecursiveTask<Long> {

    private final int[] array;
    private final int start;
    private final int end;
    private static final int THRESHOLD = 100_000;

    public ArraySumTask(int[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Long compute() {
        int length = end - start;
        if (length <= THRESHOLD) {
            long sum = 0;
            for (int i = start; i < end; i++) sum += array[i];
            return sum;
        } else {
            int mid = start + length / 2;
            ArraySumTask leftTask = new ArraySumTask(array, start, mid);
            ArraySumTask rightTask = new ArraySumTask(array, mid, end);
            leftTask.fork();
            long rightResult = rightTask.compute();
            long leftResult = leftTask.join();
            return leftResult + rightResult;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Параллельное суммирование массива ===");

        int size = 10_000_000;
        System.out.println("Размер массива: " + size + " элементов");
        System.out.println("Порог разделения: " + THRESHOLD + " элементов");

        int[] arr = new int[size];
        for (int i = 0; i < size; i++) arr[i] = i + 1;

        // Последовательное суммирование
        long start1 = System.currentTimeMillis();
        long seq = 0;
        for (int v : arr) seq += v;
        long end1 = System.currentTimeMillis();

        System.out.println("\nПоследовательное суммирование:");
        System.out.println("Результат: " + seq);
        System.out.println("Время выполнения: " + (end1 - start1) + " мс");

        // Параллельное суммирование
        ForkJoinPool pool = ForkJoinPool.commonPool();
        long start2 = System.currentTimeMillis();
        long parallel = pool.invoke(new ArraySumTask(arr, 0, arr.length));
        long end2 = System.currentTimeMillis();

        System.out.println("\nПараллельное суммирование (Fork/Join):");
        System.out.println("Результат: " + parallel + (parallel == seq ? " ✓" : " ✗"));
        System.out.println("Время выполнения: " + (end2 - start2) + " мс");

        System.out.printf("\nУскорение: %.2fx\n", (double) (end1 - start1) / (end2 - start2));
    }
}
