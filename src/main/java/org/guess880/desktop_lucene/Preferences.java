package org.guess880.desktop_lucene;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

class Preferences {

    public static final String DEF_CHARSET = "UTF-8";

    private static final Charset CHARSET = Charset.forName(DEF_CHARSET);

    private final Properties props = new Properties();

    private final ExternalEditorPrefs extEditorPrefs = new ExternalEditorPrefs();

    private final SaveSearchResultsPrefs saveSearchResultPrefs = new SaveSearchResultsPrefs();

    private final LogPrefs logPrefs = new LogPrefs();

    private final List<PrefsGroup> groups = Collections.unmodifiableList(Arrays.asList(extEditorPrefs, saveSearchResultPrefs, logPrefs));

    protected final ExternalEditorPrefs getExtEditorPrefs() {
        return extEditorPrefs;
    }

    protected final SaveSearchResultsPrefs getSaveSearchResultsPrefs() {
        return saveSearchResultPrefs;
    }

    protected final LogPrefs getLogPrefs() {
        return logPrefs;
    }

    protected Preferences load() throws IOException {
        props.clear();
        final Path path = getPropertiesPath();
        if (Files.exists(path)) {
            props.load(Files.newBufferedReader(path, CHARSET));
        }
        for (final PrefsGroup group : groups) {
            group.read(props);
        }
        return this;
    }

    protected Preferences save() throws IOException {
        final Path filePath = getPropertiesPath();
        final Path dirPath = filePath.getParent();
        if (Files.notExists(dirPath)) {
            Files.createDirectories(dirPath);
        }
        for (final PrefsGroup group : groups) {
            group.write(props);
        }
        props.store(Files.newBufferedWriter(filePath, CHARSET), "");
        return this;
    }

    // not member because of test
    private Path getPropertiesPath() {
        return Paths.get(System.getProperty("user.home"), ".desktop-lucene", "preferences.properties");
    }

    private abstract class PrefsGroup {

        private static final String SEPARATOR = ".";

        protected final Map<String, Property<? extends Object>> propMap = new ConcurrentHashMap<>();

        protected final Map<String, String> defaultMap = new ConcurrentHashMap<>();

        private final String name;

        private PrefsGroup(final String name) {
            super();
            this.name = name;
        }

        protected abstract void setUpPropMap();

        protected abstract void setUpDefaultMap();

        protected void read(final Properties props) {
            for (final Entry<String, Property<? extends Object>> entry : propMap.entrySet()) {
                final String key = entry.getKey();
                final String value = props.getProperty(name + SEPARATOR + key, getDefault(key));
                final Property<? extends Object> prop = entry.getValue();
                if (prop instanceof BooleanProperty) {
                    ((BooleanProperty) prop).set(Boolean.valueOf(value));
                } else {
                    ((StringProperty) prop).set(value);
                }
            }
        }

        private String getDefault(final String key) {
            final String def = defaultMap.get(key);
            return def == null ? "" : def;
        }

        protected void write(final Properties props) {
            for (final Entry<String, Property<? extends Object>> entry : propMap.entrySet()) {
                props.setProperty(name + SEPARATOR + entry.getKey(), entry.getValue().getValue().toString());
            }
        }

    }

    final class ExternalEditorPrefs extends PrefsGroup {

        private static final String NAME = "externalEditor";

        private static final String KEY_PATH = "path";

        private static final String KEY_ARGS = "arguments";

        private static final String KEY_WK_DIR = "workingDirectory";

        private final SimpleStringProperty path = new SimpleStringProperty();

        private final SimpleStringProperty args = new SimpleStringProperty();

        private final SimpleStringProperty wkDir = new SimpleStringProperty();

        private ExternalEditorPrefs() {
            super(NAME);
            setUpPropMap();
        }

        @Override
        protected void setUpPropMap() {
            propMap.put(KEY_PATH, path);
            propMap.put(KEY_ARGS, args);
            propMap.put(KEY_WK_DIR, wkDir);
        }

        @Override
        protected void setUpDefaultMap() {
            // do nothing
        }

        protected SimpleStringProperty pathProperty() {
            return path;
        }

        protected SimpleStringProperty argsProperty() {
            return args;
        }

        protected SimpleStringProperty wkDirProperty() {
            return wkDir;
        }

    }

    final class SaveSearchResultsPrefs extends PrefsGroup {

        private static final String NAME = "saveSearchResults";

        private static final String KEY_CHARSET = "charset";

        private static final String KEY_HEADER_FORMAT = "headerFormat";

        private static final String KEY_BODY_FORMAT = "bodyFormat";

        private static final String KEY_FOOTER_FORMAT = "footerFormat";

        private static final String DEF_HEADER_FORMAT = "<?xml version=\"1.0\" encoding=\"" + ReservedWords.CHARSET + "\"?><results>";

        private static final String DEF_BODY_FORMAT = "<result><path>" + ReservedWords.PATH + "</path><line>"
                + ReservedWords.LINE + "</line><contents>" + ReservedWords.CONTENTS + "</contents></result>";

        private static final String DEF_FOOTER_FORMAT = "</results>";

        private final SimpleStringProperty charset = new SimpleStringProperty(DEF_CHARSET);

        private final SimpleStringProperty header = new SimpleStringProperty(DEF_HEADER_FORMAT);

        private final SimpleStringProperty body = new SimpleStringProperty(DEF_BODY_FORMAT);

        private final SimpleStringProperty footer = new SimpleStringProperty(DEF_FOOTER_FORMAT);

        private SaveSearchResultsPrefs() {
            super(NAME);
            setUpPropMap();
            setUpDefaultMap();
        }

        @Override
        protected void setUpPropMap() {
            propMap.put(KEY_CHARSET, charset);
            propMap.put(KEY_HEADER_FORMAT, header);
            propMap.put(KEY_BODY_FORMAT, body);
            propMap.put(KEY_FOOTER_FORMAT, footer);
        }

        @Override
        protected void setUpDefaultMap() {
            defaultMap.put(KEY_CHARSET, DEF_CHARSET);
            defaultMap.put(KEY_HEADER_FORMAT, DEF_HEADER_FORMAT);
            defaultMap.put(KEY_BODY_FORMAT, DEF_BODY_FORMAT);
            defaultMap.put(KEY_FOOTER_FORMAT, DEF_FOOTER_FORMAT);
        }

        protected SimpleStringProperty getCharsetProperty() {
            return charset;
        }

        protected SimpleStringProperty getHeaderProperty() {
            return header;
        }

        protected SimpleStringProperty getBodyProperty() {
            return body;
        }

        protected SimpleStringProperty getFooterProperty() {
            return footer;
        }

    }

    final class LogPrefs extends PrefsGroup {

        private static final String NAME = "log";

        private static final String KEY_SHOW_DETAIL = "showDetail";

        private final SimpleBooleanProperty showDetail = new SimpleBooleanProperty();

        private LogPrefs() {
            super(NAME);
            setUpPropMap();
            setUpDefaultMap();
        }

        @Override
        protected void setUpPropMap() {
            propMap.put(KEY_SHOW_DETAIL, showDetail);
        }

        @Override
        protected void setUpDefaultMap() {
            defaultMap.put(KEY_SHOW_DETAIL, Boolean.FALSE.toString());
        }

        protected SimpleBooleanProperty getShowDetailProperty() {
            return showDetail;
        }

    }

}
