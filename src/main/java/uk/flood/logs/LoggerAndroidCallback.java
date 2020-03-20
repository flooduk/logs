package uk.flood.logs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public interface LoggerAndroidCallback {

    FileOutputStream open(File file, boolean flag) throws FileNotFoundException;

    void create(File file) throws IOException;

    void remove(File file);

}
