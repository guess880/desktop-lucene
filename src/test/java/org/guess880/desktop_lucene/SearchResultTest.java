package org.guess880.desktop_lucene;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class SearchResultTest {

    private SearchResult ret;

    @Before
    public void setUp() {
        ret = new SearchResult("path_value", 123L, "contents_value");
    }

    @Test
    public void testSearchResult() {
        ret = new SearchResult();
        assertThat(ret.getPath(), is(nullValue()));
        assertThat(ret.getLine(), is(equalTo((0L))));
        assertThat(ret.getContents(), is(nullValue()));
    }

    @Test
    public void testSearchResultStringLongString() {
        assertThat(ret.getPath(), is(equalTo("path_value")));
        assertThat(ret.getLine(), is(equalTo((123L))));
        assertThat(ret.getContents(), is(equalTo("contents_value")));
    }

    @Test
    public void testToString() {
        assertThat(ret.toString(), is(equalTo("path: path_value; line: 123; contents: contents_value")));
    }

    @Test
    public void testToCsv() {
        assertThat(ret.toCsv(), is(equalTo("path_value,123,contents_value")));
    }

    @Test
    public void testToCsv_WithCommma() {
        assertThat(ret.setContents("contents,value").toCsv(), is(equalTo("path_value,123,\"contents,value\"")));
    }

    @Test
    public void testToCsv_WithDoubleQuotation() {
        assertThat(ret.setContents("contents\"value").toCsv(), is(equalTo("path_value,123,\"contents\"value\"")));
    }

    @Test
    public void testToTsv() {
        assertThat(ret.toTsv(), is(equalTo("path_value\t123\tcontents_value")));
    }

    @Test
    public void testSetPath() {
        assertThat(ret.setPath("new_path").getPath(), is(equalTo("new_path")));
    }

    @Test
    public void testSetLine() {
        assertThat(ret.setLine(456L).getLine(), is(equalTo((456L))));
    }

    @Test
    public void testSetContents() {
        assertThat(ret.setContents("new_contents").getContents(), is(equalTo("new_contents")));
    }

}
