package org.guess880.desktop_lucene;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

public class SearchResult {

    public static final String PATH = "path";

    public static final String LINE = "line";

    public static final String CONTENTS = "contents";

    private static final String COMMA = ",";

    private static final String TAB = "\t";

    private final SimpleStringProperty path = new SimpleStringProperty();

    private final SimpleLongProperty line = new SimpleLongProperty();

    private final SimpleStringProperty contents = new SimpleStringProperty();

    public SearchResult() {
        super();
    }

    public SearchResult(final String path, final long line, final String contents) {
        this();
        setPath(path).setLine(line).setContents(contents);
    }

    @Override
    public String toString() {
        return String.format("%1$s: %2$s; %3$s: %4$d; %5$s: %6$s", PATH, getPath(), LINE, getLine(), CONTENTS, getContents());
    }

    public String toCsv() {
        return new StringBuilder()
            .append(getPath())
            .append(COMMA)
            .append(getLine())
            .append(COMMA)
            .append(doubleQuote(getContents()))
            .toString();
    }

    private String doubleQuote(final String arg) {
        if (arg.contains("\"") || arg.contains(",")) {
            return new StringBuilder().append("\"").append(arg).append("\"").toString();
        } else {
            return arg;
        }
    }

    public String toTsv() {
        return new StringBuilder()
            .append(getPath())
            .append(TAB)
            .append(getLine())
            .append(TAB)
            .append(getContents())
            .toString();
    }

    public String getPath() {
        return path.get();
    }

    public SearchResult setPath(final String path) {
        this.path.set(path);
        return this;
    }

    public long getLine() {
        return line.get();
    }

    public SearchResult setLine(final long line) {
        this.line.set(line);
        return this;
    }

    public String getContents() {
        return contents.get();
    }

    public SearchResult setContents(final String contents) {
        this.contents.set(contents);
        return this;
    }

}
