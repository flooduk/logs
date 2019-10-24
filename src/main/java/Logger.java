import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

@SuppressWarnings({"unused", "ResultOfMethodCallIgnored", "Convert2Lambda"})
public final class Logger extends Thread {

    private final long maxFileSize;
    private final int maxFilesCount;
    private final File file;
    private final AtomicLock fl = new AtomicLock();
    private final ArrayList<LogItem> items1 = new ArrayList<>(1024);
    private final ArrayList<LogItem> items2 = new ArrayList<>(1024);
    private ArrayList<LogItem> write = items1;
    private ArrayList<LogItem> read = items2;
    private boolean ran = true;

    private Logger(long maxFileSize, int maxFilesCount, File file) {
        super();
        this.maxFileSize = maxFileSize;
        this.maxFilesCount = maxFilesCount;
        this.file = file;
        setDaemon(true);
        setName("LogThread");
        setPriority(Thread.MIN_PRIORITY);
        start();
    }

    /**
     * Возвращает новый логгер
     *
     * @param maxFileSizeMB - максимальный размер файла с логами (1..1024)
     * @param maxFilesCount - максимальное количество файлов (1..10)
     * @param file          - объект файла с логами, не может быть null
     * @return Logger или выбрасывает IllegalArgumentException
     * @throws IllegalArgumentException - если входные параметры не удовлетворяют условиям
     */
    public static Logger createLogger(
            int maxFileSizeMB,
            int maxFilesCount,
            File file
    ) {
        if (maxFileSizeMB < 1 || maxFileSizeMB > 1024) {
            throw new IllegalArgumentException("maxFileSizeMB must be in 1..1024");
        }
        if (maxFilesCount < 1 || maxFilesCount > 10) {
            throw new IllegalArgumentException("maxFilesCount must be in 1..10");
        }
        if (file == null) {
            throw new IllegalArgumentException("file must not be null");
        }
        return new Logger((long) maxFileSizeMB * 1024L * 1024L, maxFilesCount, file);
    }

    public void log(String tag, String value) {
        fl.lock();
        try {
            write.add(new LogItem(tag, value));
        } finally {
            fl.unlock();
        }
    }

    public void finish() {
        ran = false;
    }

    @Override
    public void run() {
        while (ran) {
            swap();
            try {
                File f = ensure(file);
                if (f.length() > maxFileSize) {
                    f = rename(file, maxFilesCount);
                }
                try (FileOutputStream fos = new FileOutputStream(f, true)) {
                    for (LogItem li : read) {
                        fos.write(li.toString().getBytes());
                    }
                }
                read.clear();
                Thread.sleep(100);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void swap() {
        fl.lock();
        try {
            if (write == items1) {
                write = items2;
                read = items1;
            } else {
                write = items1;
                read = items2;
            }
        } finally {
            fl.unlock();
        }
    }

    private File ensure(File file) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    private File rename(File file, int maxFilesCount) throws IOException {
        // rename file
        String filename = file.getAbsolutePath();
        File dir = file.getParentFile();
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file1, String s) {
                return s.startsWith(file.getName());
            }
        });
        if (files != null && files.length > maxFilesCount) {
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File file, File t1) {
                    return file.getName().compareTo(t1.getName());
                }
            });
            files[0].delete();
        }
        file.renameTo(new File(file.getAbsolutePath() + "." + System.currentTimeMillis()));
        return ensure(new File(filename));
    }


}
