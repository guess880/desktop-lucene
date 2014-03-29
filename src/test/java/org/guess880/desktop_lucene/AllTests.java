package org.guess880.desktop_lucene;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ PreferencesTest.class, ResourcesTest.class,
        SearchResultTest.class, SearchResultWriterTest.class })
public class AllTests {

}
