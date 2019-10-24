import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

final class LogItem {
    private final static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
    private final static Date dt = new Date(0);

    private final String value;
    private final String level;
    private final long threadTag = Thread.currentThread().getId();
    private final long time = System.currentTimeMillis();

    LogItem(String level, String value) {
        this.level = level;
        this.value = value;
    }

    @Override
    public String toString() {
        dt.setTime(time);
        return String.format(Locale.getDefault(), "%s\t% 5d %s: %s\n",
                df.format(dt), threadTag, level, value);
    }
}
