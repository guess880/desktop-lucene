package org.guess880.desktop_lucene;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Locale;
import java.util.MissingResourceException;

import org.junit.Test;

public class ResourcesTest {

    static {
        Locale.setDefault(Locale.ROOT);
    }

    @Test
    public void testGetResourceBundle() {
        assertThat(Resources.getResourceBundle().getString("file"), is(equalTo("_File")));
    }

    @Test
    public void testGet_Exists() {
        assertThat(Resources.get("indexing", "a data"), is(equalTo("Indexing for a data.")));
    }

    @Test(expected = MissingResourceException.class)
    public void testGet_NotExists() {
        Resources.get("not-exists-key");
    }

}
