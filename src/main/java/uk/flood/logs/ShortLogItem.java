package uk.flood.logs;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ShortLogItem extends LogItem {

    private final static SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);

    ShortLogItem(String level, String value) {
        super(level, value);
    }

    @Override
    public String toString() {
        dt.setTime(time);
        return String.format(Locale.getDefault(), "%s %s\n",
                df.format(dt), value);
    }

}
