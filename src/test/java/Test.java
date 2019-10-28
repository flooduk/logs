import uk.flood.logs.Logger;

import java.io.File;

public class Test {

    @org.junit.Test
    public void test() {
        Logger l = Logger.createLogger(1, 1, new File("/Users/antonbogdanov/Desktop/XXX.txt"));

        for (int k = 0; k < 1000; k++) {
            for (int i = 0; i < 1000; i++) {
                l.log("X", "test");
            }
            try { Thread.sleep(500); } catch (Exception e) {}
        }


    }

}
