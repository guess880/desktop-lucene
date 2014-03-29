package org.guess880.desktop_lucene;

import java.text.MessageFormat;
import java.util.ResourceBundle;

final class Resources {

    private static final ResourceBundle RB = ResourceBundle.getBundle("org.guess880.desktop_lucene.resources");

    private Resources() {
        // prevent instantiation
        super();
    }

    protected static ResourceBundle getResourceBundle() {
        return RB;
    }

    protected static String get(final String key, final Object... args) {
        return MessageFormat.format(RB.getString(key), args);
    }

}
