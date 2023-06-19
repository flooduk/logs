import uk.flood.logs.Logger;
import uk.flood.logs.LoggerAndroidCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Test {

    @org.junit.Test
    public void test() {
        Logger l = Logger.createLogger(1, 1, new File("/Users/antonbogdanov/Desktop/XXX.txt"), new LoggerAndroidCallback() {
            @Override
            public FileOutputStream open(File file, boolean flag) throws FileNotFoundException {
                return new FileOutputStream(file, flag);
            }

            @Override
            public void create(File file) throws IOException {
                file.createNewFile();
            }

            @Override
            public void remove(File file) {
                file.delete();
            }

            @Override
            public File[] listFiles(File file) {
                return file.listFiles();
            }
        });

        for (int k = 0; k < 1000; k++) {
            for (int i = 0; i < 1000; i++) {
                l.log("X", "test");
            }
            try { Thread.sleep(500); } catch (Exception e) {}
        }


    }

}
