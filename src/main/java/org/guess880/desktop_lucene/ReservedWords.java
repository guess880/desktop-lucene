package org.guess880.desktop_lucene;

final class ReservedWords {

    static final String CHARSET = "${charset}";

    static final String PATH = "${path}";

    static final String LINE = "${line}";

    static final String CONTENTS = "${contents}";

    private ReservedWords() {
        // prevent instantiation
        super();
    }

}
