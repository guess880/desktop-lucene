package org.guess880.desktop_lucene;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;
import mockit.Mock;
import mockit.MockUp;

import org.apache.commons.io.FileUtils;
import org.guess880.desktop_lucene.Preferences.SaveSearchResultsPrefs;
import org.guess880.desktop_lucene.SearchResultWriter.WriterType;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class SearchResultWriterTest {

    static {
        Locale.setDefault(Locale.ROOT);
    }

    private static final File BASE_DIR = new File("target/test-classes/org/guess880/desktop_lucene/SearchResultWriter");

    @Test
    public void testCreate() {
        assertThat(SearchResultWriter.create(WriterType.CSV).getClass().getSimpleName(), is(equalTo("SearchResultCsvWriter")));
        assertThat(SearchResultWriter.create(WriterType.TSV).getClass().getSimpleName(), is(equalTo("SearchResultTsvWriter")));
        assertThat(SearchResultWriter.create(WriterType.PREF).getClass().getSimpleName(), is(equalTo("SearchResultPreferredFormatWriter")));
    }

    @Test
    public void testPreference() {
        final SearchResultWriter writer = SearchResultWriter.create(WriterType.CSV);
        assertThat(writer.getPreference(), is(nullValue()));
        final Preferences.SaveSearchResultsPrefs prefs = new Preferences().getSaveSearchResultsPrefs();
        assertThat(writer.setPreferences(prefs).getPreference(), is(equalTo(prefs)));
    }

    @DataPoints
    public static final WriteFixture[] WRITE_FIXTURES = {
        new WriteFixture(
                SearchResultWriter.create(WriterType.CSV),
                new File(BASE_DIR, "actual_csv.txt"),
                new File(BASE_DIR, "expected_csv.txt")),
        new WriteFixture(
                SearchResultWriter.create(WriterType.TSV),
                new File(BASE_DIR, "actual_tsv.txt"),
                new File(BASE_DIR, "expected_tsv.txt")),
        new WriteFixture(
                SearchResultWriter.create(WriterType.PREF),
                new File(BASE_DIR, "actual_pref.txt"),
                new File(BASE_DIR, "expected_pref.txt")),
    };

    @Theory
    public void testWrite(final WriteFixture fixture) throws Exception {
        new MockUp<FileChooser>() {
            @Mock
            public File showSaveDialog(final Window paramWindow) {
                return fixture.actual;
            }
        };
        final SaveSearchResultsPrefs prefs = new Preferences().getSaveSearchResultsPrefs();
        fixture.writer.setPreferences(prefs);
        fixture.writer.write(Arrays.asList(new SearchResult[] { new SearchResult("path", 1L, "contents") }));
        final Charset utf8 = Charset.forName("UTF-8");
        assertThat(FileUtils.readLines(fixture.actual, utf8), is(equalTo(FileUtils.readLines(fixture.expected, utf8))));
    }

    @DataPoints
    public static final ExtensionFilterFixture[] EXT_FILT_FIXTURES = {
        new ExtensionFilterFixture(
                SearchResultWriter.create(WriterType.CSV),
                new ExtensionFilter("CSV files (*.csv)", "*.csv")),
        new ExtensionFilterFixture(
                SearchResultWriter.create(WriterType.TSV),
                new ExtensionFilter("TSV files (*.tsv)", "*.tsv")),
        new ExtensionFilterFixture(
                SearchResultWriter.create(WriterType.PREF),
                new ExtensionFilter("Text files (*.txt)", "*.txt"),
                new ExtensionFilter("All files (*.*)", "*.*")),
    };

    @Theory
    public void testGetExtensionFilters(final ExtensionFilterFixture fixture) {
        final Collection<ExtensionFilter> filters = fixture.writer.getExtensionFilters();
        final ExtensionFilter[] expFilters = fixture.expFilters;
        assertThat(filters.size(), is(equalTo(expFilters.length)));
        int i = 0;
        for (final ExtensionFilter filter : filters) {
            final ExtensionFilter expFilter = expFilters[i];
            assertThat(filter.getDescription(), is(equalTo(expFilter.getDescription())));
            final List<String> exts = filter.getExtensions();
            final List<String> expExts = expFilter.getExtensions();
            for (int j = 0; j < exts.size(); j++) {
                assertThat(exts.get(j), is(equalTo(expExts.get(j))));
            }
            i++;
        }
    }

    private static class WriteFixture {
        private final SearchResultWriter writer;
        private final File actual;
        private final File expected;
        public WriteFixture(final SearchResultWriter writer, final File actual, final File expected) {
            this.writer = writer;
            this.actual = actual;
            this.expected = expected;
        }
    }

    private static class ExtensionFilterFixture {
        private final SearchResultWriter writer;
        private final ExtensionFilter[] expFilters;
        public ExtensionFilterFixture(final SearchResultWriter writer, final ExtensionFilter... expFilters) {
            this.writer = writer;
            this.expFilters = expFilters;
        }
    }

}
