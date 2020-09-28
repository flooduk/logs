package uk.flood.logs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

public interface LoggerAndroidCallback {

    OutputStream open(File file, boolean flag) throws FileNotFoundException;

    void create(File file) throws IOException;

    void remove(File file);

}
