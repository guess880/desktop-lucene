package org.guess880.desktop_lucene;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.FileChooserBuilder;
import javafx.stage.Window;

abstract class SearchResultWriter {

    private static final String RK_PATH = "path";

    private static final String RK_LINE = "line";

    private static final String RK_CONTENTS = "contents";

    private Preferences.SaveSearchResultsPrefs pref;

    private Window owner;

    protected final Preferences.SaveSearchResultsPrefs getPreference() {
        return pref;
    }

    protected final SearchResultWriter setPreferences(final Preferences.SaveSearchResultsPrefs pref) {
        this.pref = pref;
        return this;
    }

    protected final SearchResultWriter setOwner(final Window owner) {
        this.owner = owner;
        return this;
    }

    protected void write(final List<SearchResult> results) throws IOException {
        final FileChooser chooser = FileChooserBuilder.create().extensionFilters(getExtensionFilters()).build();
        final File file = chooser.showSaveDialog(owner);
        if (file != null) {
            try (final PrintWriter writer = new PrintWriter(
                    Files.newBufferedWriter(
                            Paths.get(file.toURI()),
                            Charset.forName(pref.getCharsetProperty().get())))) {
                writeHeader(writer);
                for (final SearchResult result : results) {
                    writeLine(writer, result);
                }
                writeFooter(writer);
            }
        }
    }

    protected abstract Collection<ExtensionFilter> getExtensionFilters();

    protected abstract void writeHeader(PrintWriter writer);

    protected abstract void writeLine(PrintWriter writer, SearchResult result);

    protected abstract void writeFooter(PrintWriter writer);

    protected static final SearchResultWriter create(final WriterType type) {
        switch (type) {
        case CSV:
            return new SearchResultCsvWriter();
        case TSV:
            return new SearchResultTsvWriter();
        default:
            return new SearchResultPreferredFormatWriter();
        }
    }

    static enum WriterType {
        CSV, TSV, PREF;
    }

    private static final class SearchResultCsvWriter extends SearchResultWriter {

        private static final String DELIMITER = ",";

        @Override
        protected Collection<ExtensionFilter> getExtensionFilters() {
            return Arrays.asList(new ExtensionFilter[] { new ExtensionFilter(Resources.get("extCsv"), "*.csv") });
        }

        @Override
        protected void writeHeader(final PrintWriter writer) {
            writer.println(
                    new StringBuilder()
                        .append(Resources.get(RK_PATH))
                        .append(DELIMITER)
                        .append(Resources.get(RK_LINE))
                        .append(DELIMITER)
                        .append(Resources.get(RK_CONTENTS))
                    );
        }

        @Override
        protected void writeLine(final PrintWriter writer, final SearchResult result) {
            writer.println(result.toCsv());
        }

        @Override
        protected void writeFooter(final PrintWriter writer) {
            // do nothing
        }

    }

    private static final class SearchResultTsvWriter extends SearchResultWriter {

        private static final String DELIMITER = "\t";

        @Override
        protected Collection<ExtensionFilter> getExtensionFilters() {
            return Arrays.asList(new ExtensionFilter[] { new ExtensionFilter(Resources.get("extTsv"), "*.tsv") });
        }

        @Override
        protected void writeHeader(final PrintWriter writer) {
            writer.println(
                    new StringBuilder()
                        .append(Resources.get(RK_PATH))
                        .append(DELIMITER)
                        .append(Resources.get(RK_LINE))
                        .append(DELIMITER)
                        .append(Resources.get(RK_CONTENTS))
                    );
        }

        @Override
        protected void writeLine(final PrintWriter writer, final SearchResult result) {
            writer.println(result.toTsv());
        }

        @Override
        protected void writeFooter(final PrintWriter writer) {
            // do nothing
        }

    }

    private static final class SearchResultPreferredFormatWriter extends SearchResultWriter {

        @Override
        protected Collection<ExtensionFilter> getExtensionFilters() {
            return Arrays.asList(new ExtensionFilter[] {
                    new ExtensionFilter(Resources.get("extTxt"), "*.txt"),
                    new ExtensionFilter(Resources.get("extAll"), "*.*") });
        }

        @Override
        protected void writeHeader(final PrintWriter writer) {
            writer.println(getPreference().getHeaderProperty().get()
                    .replace(ReservedWords.CHARSET, getPreference().getCharsetProperty().get())
                    );
        }

        @Override
        protected void writeLine(final PrintWriter writer, final SearchResult result) {
            writer.println(getPreference().getBodyProperty().get()
                    .replace(ReservedWords.CHARSET, getPreference().getCharsetProperty().get())
                    .replace(ReservedWords.PATH, result.getPath())
                    .replace(ReservedWords.LINE, String.valueOf(result.getLine()))
                    .replace(ReservedWords.CONTENTS, result.getContents())
                    );
        }

        @Override
        protected void writeFooter(final PrintWriter writer) {
            writer.println(getPreference().getFooterProperty().get()
                    .replace(ReservedWords.CHARSET, getPreference().getCharsetProperty().get())
                    );
        }

    }

}
