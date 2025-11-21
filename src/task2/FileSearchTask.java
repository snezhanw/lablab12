package task2;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class FileSearchTask extends RecursiveAction {

    private final File directory;
    private final String extension;
    private final ConcurrentLinkedQueue<String> results;

    public FileSearchTask(File directory, String extension, ConcurrentLinkedQueue<String> results) {
        this.directory = directory;
        this.extension = extension;
        this.results = results;
    }

    @Override
    protected void compute() {
        File[] files = directory.listFiles();
        if (files == null) return;

        List<FileSearchTask> subTasks = new ArrayList<>();

        for (File file : files) {
            if (file.isDirectory()) {
                FileSearchTask task = new FileSearchTask(file, extension, results);
                subTasks.add(task);
                task.fork();

            } else if (file.getName().endsWith(extension)) {
                results.add(file.getAbsolutePath());
            }
        }

        for (FileSearchTask task : subTasks) {
            task.join();
        }
    }

    // Метод создания тестовой директории
    private static void createTestDirectory(String rootPath) throws IOException {
        File root = new File(rootPath);
        root.mkdirs();

        new File(root, "snezhka.txt").createNewFile();
        new File(root, "newCode.doc").createNewFile();

        File sub1 = new File(root, "sub1");
        sub1.mkdirs();
        new File(sub1, "snehanw.txt").createNewFile();

        File sub2 = new File(sub1, "sub2");
        sub2.mkdirs();
        new File(sub2, "dota2.txt").createNewFile();
    }

    public static void main(String[] args) throws Exception {

        String path = "test_directory";
        createTestDirectory(path);

        System.out.println("=== Параллельный поиск файлов ===");
        System.out.println("Корневая директория: " + new File(path).getAbsolutePath());
        System.out.println("Искомое расширение: .txt\n");

        ConcurrentLinkedQueue<String> results = new ConcurrentLinkedQueue<>();

        ForkJoinPool pool = ForkJoinPool.commonPool();

        long start = System.currentTimeMillis();
        pool.invoke(new FileSearchTask(new File(path), ".txt", results));
        long end = System.currentTimeMillis();

        System.out.println("Найденные файлы:");
        int index = 1;
        for (String s : results) {
            System.out.println(index++ + ". " + s);
        }
        System.out.println("\nВсего найдено: " + results.size());
        System.out.println("Время выполнения: " + (end - start) + " мс");
    }
}
