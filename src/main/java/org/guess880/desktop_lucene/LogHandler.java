package org.guess880.desktop_lucene;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import javafx.beans.property.SimpleStringProperty;

public final class LogHandler extends StreamHandler {

    private final ByteArrayOutputStream stream = new ByteArrayOutputStream();

    private SimpleStringProperty logProperty;

    public LogHandler() {
        super();
        setOutputStream(stream);
    }

    public LogHandler setLogProperty(final SimpleStringProperty logProperty) {
        this.logProperty = logProperty;
        return this;
    }

    @Override
    public synchronized void publish(final LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }
        super.publish(record);
        flush();
        try {
            logProperty.set(stream.toString(Preferences.DEF_CHARSET));
        } catch (final UnsupportedEncodingException e) {
            logProperty.set(stream.toString());
        }
        stream.reset();
    }

}
