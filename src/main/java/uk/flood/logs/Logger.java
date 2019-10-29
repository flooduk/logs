package uk.flood.logs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

@SuppressWarnings({"unused", "ResultOfMethodCallIgnored", "Convert2Lambda"})
public final class Logger extends Thread {

    private final long maxFileSize;
    private final int maxFilesCount;
    private final String path;
    private final String fileName;
    private final String fileExt;
    private final AtomicLock fl = new AtomicLock();
    private final ArrayList<LogItem> items1 = new ArrayList<>(1024);
    private final ArrayList<LogItem> items2 = new ArrayList<>(1024);
    private volatile ArrayList<LogItem> write = items1;
    private volatile ArrayList<LogItem> read = items2;
    private volatile boolean ran = true;
    private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    private File currentFile = null;

    private Logger(long maxFileSize, int maxFilesCount, String path, String fileName, String fileExt) {
        super();
        this.maxFileSize = maxFileSize;
        this.maxFilesCount = maxFilesCount;
        this.path = path;
        this.fileName = fileName;
        this.fileExt = fileExt;
        setDaemon(true);
        setName("LogThread");
        setPriority(Thread.MIN_PRIORITY);
        start();
    }

    /**
     * Возвращает новый логгер
     *
     * @param maxFileSizeMB - максимальный размер файла с логами (1..1024)
     * @param maxFilesCount - максимальное количество файлов (2..100)
     * @param file          - объект файла с логами, не может быть null
     * @return uk.flood.logs.Logger или выбрасывает IllegalArgumentException
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
        if (maxFilesCount < 2 || maxFilesCount > 100) {
            throw new IllegalArgumentException("maxFilesCount must be in 2..100");
        }
        if (file == null) {
            throw new IllegalArgumentException("file must not be null");
        }

        String path = file.getAbsolutePath();
        int last = path.lastIndexOf('.');
        if (last == -1) {
            throw new IllegalArgumentException("file must be with extension");
        }
        int lastSeparator = path.lastIndexOf(File.separatorChar);
        String filePath = path.substring(0, lastSeparator);
        String fileName = path.substring(filePath.length()+1, last);
        String fileExt = path.substring(last);
        return new Logger((long) maxFileSizeMB * 1024L * 1024L,
                maxFilesCount, filePath, fileName, fileExt);
    }

    public void log(String tag, String value) {
        fl.lock();
        try {
            write.add(new LogItem(tag, value));
        } finally {
            fl.unlock();
        }
    }

    public void slog(String value) {
        fl.lock();
        try {
            write.add(new ShortLogItem("", value));
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
                File f = ensure(getCurrentFile());
                if (f.length() > maxFileSize) {
                    f = rename(getCurrentFile(), maxFilesCount);
                    Thread.sleep(100);
                }
                try (FileOutputStream fos = new FileOutputStream(ensure(f), true)) {
                    for (LogItem li : read) {
                        fos.write(li.toString().getBytes());
                    }
                }
                read.clear();
                Thread.sleep(100);
            } catch (Throwable e) {
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

    private File getCurrentFile() {
        if (currentFile == null) {
            currentFile = new File(path + File.separator + fileName + "." + df.format(new Date()) + fileExt);
        }
        return currentFile;
    }

    private File rename(File file, int maxFilesCount) throws IOException {
        // rename file
        String filename = file.getAbsolutePath();
        File dir = file.getParentFile();
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file1, String s) {
                return s.startsWith(fileName.substring(fileName.lastIndexOf(File.separator) + 1));
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
        currentFile = null;
        return ensure(getCurrentFile());
    }


}
