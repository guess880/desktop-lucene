package org.guess880.desktop_lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ExternalEditorExecutor {

    private Preferences.ExternalEditorPrefs prefs;

    private SearchResult result;

    protected ExternalEditorExecutor setPreferences(final Preferences.ExternalEditorPrefs prefs) {
        this.prefs = prefs;
        return this;
    }

    protected ExternalEditorExecutor setSearchResult(final SearchResult result) {
        this.result = result;
        return this;
    }

    protected ExternalEditorExecutor execute() throws IOException {
        final List<String> commands = new ArrayList<>();
        commands.add(prefs.pathProperty().get());
        final String[] args = prefs.argsProperty().get().split(" ");
        for (int i = 0; i < args.length; i++) {
            args[i] = args[i]
                    .replace(ReservedWords.LINE, String.valueOf(result.getLine()))
                    .replace(ReservedWords.PATH, result.getPath());
        }
        commands.addAll(Arrays.asList(args));
        final ProcessBuilder pb = new ProcessBuilder(commands);
        pb.directory(new File(prefs.wkDirProperty().get()));
        pb.start();
        return this;
    }

    protected static final ExternalEditorExecutor create() {
        return new ExternalEditorExecutor();
    }

}
