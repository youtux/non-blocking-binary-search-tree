package it.unitn.studenti.alessiobogon.concurrency;

import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.*;

/**
 * Created by Alessio Bogon on 03/08/15.
 */
public class ConcurrentFormatter extends Formatter {
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final MessageFormat messageFormat = new MessageFormat(
            "[{0,date,HH:mm:ss.S}] Thread#{1}:{2}{3}: {4}" + LINE_SEPARATOR
    );

    public ConcurrentFormatter() {
        super();
    }

    @Override
    public String format(LogRecord record) {
        Object[] arguments = {
                new Date(record.getMillis()),
                record.getThreadID(),
                makeTabs(record.getThreadID()),
                record.getSourceMethodName(),
                formatMessage(record),
        };
        return messageFormat.format(arguments);
    }

    private String makeTabs(int howMany) {
        String res = "";
        for (; howMany > 0; howMany--) {
            res += "  ";
        }
        return res;
    }
}
