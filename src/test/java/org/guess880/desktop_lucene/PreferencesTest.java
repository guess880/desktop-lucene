package org.guess880.desktop_lucene;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class PreferencesTest {

    private static final String USER_HOME = "user.home";

    private static final String EMPTY_STR = "";

    private static String oldUserHome;

    @BeforeClass
    public static void setUpBeforeClass() {
        oldUserHome = System.getProperty(USER_HOME);
    }

    @AfterClass
    public static void tearDownAfterClass() {
        System.setProperty(USER_HOME, oldUserHome);
    }

    @Test
    public void testLoad_FileNotExists() throws IOException {
        final File dir = new File("target/test-classes/org/guess880/desktop_lucene/Preferences/load_not_exists");
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
        System.setProperty(USER_HOME, dir.getAbsolutePath());
        final Preferences prefs = new Preferences().load();
        assertThat(prefs.getExtEditorPrefs().pathProperty().get(), is(equalTo(EMPTY_STR)));
        assertThat(prefs.getExtEditorPrefs().argsProperty().get(), is(equalTo(EMPTY_STR)));
        assertThat(prefs.getExtEditorPrefs().wkDirProperty().get(), is(equalTo(EMPTY_STR)));
        assertThat(prefs.getSaveSearchResultsPrefs().getCharsetProperty().get(), is(equalTo("UTF-8")));
        assertThat(prefs.getSaveSearchResultsPrefs().getHeaderProperty().get(), is(equalTo("<?xml version=\"1.0\" encoding=\"${charset}\"?><results>")));
        assertThat(prefs.getSaveSearchResultsPrefs().getBodyProperty().get(), is(equalTo("<result><path>${path}</path><line>${line}</line><contents>${contents}</contents></result>")));
        assertThat(prefs.getSaveSearchResultsPrefs().getFooterProperty().get(), is(equalTo("</results>")));
        assertThat(prefs.getLogPrefs().getShowDetailProperty().get(), is(equalTo(Boolean.FALSE)));
    }

    @Test
    public void testLoad() throws IOException {
        final File dir = new File("target/test-classes/org/guess880/desktop_lucene/Preferences/load");
        System.setProperty(USER_HOME, dir.getAbsolutePath());
        final Preferences prefs = new Preferences().load();
        assertThat(prefs.getExtEditorPrefs().pathProperty().get(), is(equalTo("test_path")));
        assertThat(prefs.getExtEditorPrefs().argsProperty().get(), is(equalTo("test_arguments")));
        assertThat(prefs.getExtEditorPrefs().wkDirProperty().get(), is(equalTo("test_working_directory")));
        assertThat(prefs.getSaveSearchResultsPrefs().getCharsetProperty().get(), is(equalTo("test_charset")));
        assertThat(prefs.getSaveSearchResultsPrefs().getHeaderProperty().get(), is(equalTo("test_header_format")));
        assertThat(prefs.getSaveSearchResultsPrefs().getBodyProperty().get(), is(equalTo("test_body_format")));
        assertThat(prefs.getSaveSearchResultsPrefs().getFooterProperty().get(), is(equalTo("test_footer_format")));
        assertThat(prefs.getLogPrefs().getShowDetailProperty().get(), is(equalTo(Boolean.FALSE)));
    }

    @Test
    public void testSave() throws IOException {
        final File loadDir = new File("target/test-classes/org/guess880/desktop_lucene/Preferences/load");
        System.setProperty(USER_HOME, loadDir.getAbsolutePath());
        final Preferences prefs = setNewValues(new Preferences().load());
        final File saveDir = new File("target/test-classes/org/guess880/desktop_lucene/Preferences/save");
        System.setProperty(USER_HOME, saveDir.getAbsolutePath());
        prefs.save();
        assertNewValues(new Preferences().load());
    }

    @Test
    public void testSave_FileNotExists() throws IOException {
        final Preferences prefs = setNewValues(new Preferences());
        final File dir = new File("target/test-classes/org/guess880/desktop_lucene/Preferences/save_filenotexists");
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
        System.setProperty(USER_HOME, dir.getAbsolutePath());
        prefs.save();
        assertNewValues(new Preferences().load());
    }

    private Preferences setNewValues(final Preferences prefs) {
        prefs.getExtEditorPrefs().pathProperty().set("new_path");
        prefs.getExtEditorPrefs().argsProperty().set("new_arguments");
        prefs.getExtEditorPrefs().wkDirProperty().set("new_working_directory");
        prefs.getSaveSearchResultsPrefs().getCharsetProperty().set("new_charset");
        prefs.getSaveSearchResultsPrefs().getHeaderProperty().set("new_header_format");
        prefs.getSaveSearchResultsPrefs().getBodyProperty().set("new_body_format");
        prefs.getSaveSearchResultsPrefs().getFooterProperty().set("new_footer_format");
        prefs.getLogPrefs().getShowDetailProperty().set(Boolean.TRUE);
        return prefs;
    }

    private void assertNewValues(final Preferences prefs) {
        assertThat(prefs.getExtEditorPrefs().pathProperty().get(), is(equalTo("new_path")));
        assertThat(prefs.getExtEditorPrefs().argsProperty().get(), is(equalTo("new_arguments")));
        assertThat(prefs.getExtEditorPrefs().wkDirProperty().get(), is(equalTo("new_working_directory")));
        assertThat(prefs.getSaveSearchResultsPrefs().getCharsetProperty().get(), is(equalTo("new_charset")));
        assertThat(prefs.getSaveSearchResultsPrefs().getHeaderProperty().get(), is(equalTo("new_header_format")));
        assertThat(prefs.getSaveSearchResultsPrefs().getBodyProperty().get(), is(equalTo("new_body_format")));
        assertThat(prefs.getSaveSearchResultsPrefs().getFooterProperty().get(), is(equalTo("new_footer_format")));
        assertThat(prefs.getLogPrefs().getShowDetailProperty().get(), is(equalTo(Boolean.TRUE)));
    }

}
