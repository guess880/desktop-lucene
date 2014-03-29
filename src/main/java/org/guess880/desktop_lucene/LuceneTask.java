package org.guess880.desktop_lucene;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.sandbox.queries.regex.RegexQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.mozilla.universalchardet.UniversalDetector;

class LuceneTask extends Task<List<SearchResult>> {

    private static final String RK_CANCELLED = "cancelled";

    private static final String RK_FAILED = "failed";

    private static final String RK_BLANK_KEYWORD = "blankKeyword";

    private static final String RK_BLANK_DIR = "blankDir";

    private static final String RK_DIR_NOT_EXISTS = "dirNotExists";

    private static final String RK_DIR_NOT_DIR = "dirNotDir";

    private static final String RK_DIR_NOT_READ = "dirNotRead";

    private static final String RK_START_INDEXING = "startIndexing";

    private static final String RK_FINISH_INDEXING = "finishIndexing";

    private static final String RK_DETECTED_ENC = "detectedEnc";

    private static final String RK_START_SEARCHING = "startSearching";

    private static final String RK_FINISH_SEARCHING = "finishSearching";

    private static final String FIELD_KEY = "key";

    private static final String FIELD_PATH = "path";

    private static final String FIELD_LINE = "line";

    private static final String FIELD_CONTENTS = "contents";

    private static final Logger LOGGER = Logger.getGlobal();

    private String keyword;

    private String directory;

    private String includes;

    private String excludes;

    private boolean usesRegexp;

    private int limit;

    private Path targetDirectory;

    private Path indexDirectory;

    private final Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);

    private final SimpleStringProperty logProperty = new SimpleStringProperty();

    private final List<SearchResult> results = new ArrayList<>();

    protected LuceneTask() {
        super();
        messageProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> observable, final String oldValue,
                    final String newValue) {
                messageChanged(newValue);
            }
        });
        logProperty.addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> observable, final String oldValue,
                    final String newValue) {
                updateMessage(newValue);
            }
        });
        Logger logger = LOGGER;
        while (logger != null) {
            for (final Handler handler : logger.getHandlers()) {
                if (handler instanceof LogHandler) {
                    ((LogHandler) handler).setLogProperty(logProperty);
                }
            }
            if (!logger.getUseParentHandlers()) {
                break;
            }
            logger = logger.getParent();
        }
    }

    protected LuceneTask setKeyword(final String keyword) {
        this.keyword = keyword;
        return this;
    }

    protected LuceneTask setDirectory(final String directory) {
        this.directory = directory;
        return this;
    }

    protected LuceneTask setIncludes(final String includes) {
        this.includes = includes;
        return this;
    }

    protected LuceneTask setExcludes(final String excludes) {
        this.excludes = excludes;
        return this;
    }

    protected LuceneTask setUsesRegexp(final boolean usesRegexp) {
        this.usesRegexp = usesRegexp;
        return this;
    }

    protected LuceneTask setLimit(final int limit) {
        this.limit = limit;
        return this;
    }

    protected LuceneTask setLogLevel(final Level level) {
        LOGGER.setLevel(level);
        return this;
    }

    protected List<SearchResult> getResults() {
        return results;
    }

    @Override
    protected List<SearchResult> call() {
        if (canExecute()) {
            try (final TemporaryDirectory tempDir = new TemporaryDirectory()) {
                indexDirectory = tempDir.path;
                index();
                search();
            } catch (final IOException | ParseException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            } catch (final CancellationException e) {
                LOGGER.log(Level.WARNING, Resources.get(RK_CANCELLED));
            }
        }
        return results;
    }

    @Override
    protected void failed() {
        LOGGER.log(Level.SEVERE, Resources.get(RK_FAILED));
    }

    protected void messageChanged(final String newMessage) {
        // do nothing
    }

    private boolean canExecute() {
        if (isBlank(keyword)) {
            LOGGER.log(Level.INFO, Resources.get(RK_BLANK_KEYWORD));
            return false;
        }
        if (isBlank(directory)) {
            LOGGER.log(Level.INFO, Resources.get(RK_BLANK_DIR));
            return false;
        }
        targetDirectory = Paths.get(directory).toAbsolutePath();
        if (Files.notExists(targetDirectory)) {
            LOGGER.log(Level.INFO, Resources.get(RK_DIR_NOT_EXISTS));
            return false;
        }
        if (!Files.isReadable(targetDirectory)) {
            LOGGER.log(Level.INFO, Resources.get(RK_DIR_NOT_READ));
            return false;
        }
        if (!Files.isDirectory(targetDirectory)) {
            LOGGER.log(Level.INFO, Resources.get(RK_DIR_NOT_DIR));
            return false;
        }
        return true;
    }

    private void index() throws IOException {
        LOGGER.log(Level.INFO, Resources.get(RK_START_INDEXING));
        try (final IndexWriter writer = new IndexWriter(
                FSDirectory.open(indexDirectory.toFile()),
                new IndexWriterConfig(Version.LUCENE_47, analyzer))) {
            Files.walkFileTree(targetDirectory, new IndexingFileVisitor(writer));
        }
        LOGGER.log(Level.INFO, Resources.get(RK_FINISH_INDEXING));
    }

    private boolean matches(final String pathName) {
        if ((!isBlank(includes) && !pathName.matches(includes)) ||
                (!isBlank(excludes) && pathName.matches(excludes))) {
            return false;
        }
        return  true;
    }

    private Charset detectCharset(final File file) throws IOException {
        try (final FileInputStream fis = new FileInputStream(file)) {
            final UniversalDetector detector = new UniversalDetector(null);
            final byte[] buf = new byte[4096];
            int nread;
            while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
            detector.dataEnd();
            final String charset = detector.getDetectedCharset();
            LOGGER.log(Level.FINEST, Resources.get(RK_DETECTED_ENC, charset));
            return charset == null ? Charset.defaultCharset() : Charset.forName(charset);
        }
    }

    private void search() throws IOException, ParseException {
        ensureContinue();
        try (final DirectoryReader reader = DirectoryReader.open(FSDirectory.open(indexDirectory.toFile()))) {
            final IndexSearcher searcher = new IndexSearcher(reader);
            final Term term = new Term(FIELD_CONTENTS, keyword);
            final Query query = usesRegexp ? new RegexQuery(term) : new TermQuery(term);
            LOGGER.log(Level.INFO, Resources.get(RK_START_SEARCHING));
            final TopDocs topDocs = searcher.search(query, limit);
            if (topDocs.totalHits > 0) {
                final ScoreDoc[] hits = topDocs.scoreDocs;
                for (final ScoreDoc hit : hits) {
                    ensureContinue();
                    final Document doc = searcher.doc(hit.doc);
                    results.add(new SearchResult(doc.getField(FIELD_PATH).stringValue(), doc.getField(FIELD_LINE)
                            .numericValue().longValue(), doc.getField(FIELD_CONTENTS).stringValue()));
                }
            }
            LOGGER.log(Level.INFO, Resources.get(RK_FINISH_SEARCHING, topDocs.totalHits));
        }
    }

    private boolean isBlank(final String value) {
        if (value == null) {
            return true;
        }
        final int length = value.length();
        if (length == 0) {
            return true;
        }
        for (int i = 0; i < length; i++) {
            if (!Character.isWhitespace(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private void ensureContinue() {
        if (isCancelled()) {
            throw new CancellationException();
        }
    }
    private final class IndexingFileVisitor extends SimpleFileVisitor<Path> {

        private static final String RK_INDEXING = "indexing";

        private static final String RK_SKIP_INDEXING = "skipIndexing";

        private final IndexWriter writer;

        private IndexingFileVisitor(final IndexWriter writer) {
            super();
            this.writer = writer;
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
                throws IOException {
            ensureContinue();
            final Path absolutePath = file.toAbsolutePath();
            final String pathName = absolutePath.toString();
            if (matches(pathName)) {
                LOGGER.log(Level.FINEST, Resources.get(RK_INDEXING, absolutePath));
                final File absoluteFile = absolutePath.toFile();
                try (
                        final FileInputStream fis = new FileInputStream(absoluteFile);
                        final InputStreamReader isr = new InputStreamReader(fis, detectCharset(absoluteFile));
                        final BufferedReader reader = new BufferedReader(isr);) {
                    int i = 1;
                    String contents;
                    while ((contents = reader.readLine()) != null) {
                        final Document doc = new Document();
                        doc.add(new TextField(FIELD_KEY, pathName + i, Field.Store.YES));
                        doc.add(new StringField(FIELD_PATH, pathName, Field.Store.YES));
                        doc.add(new LongField(FIELD_LINE, i, Field.Store.YES));
                        doc.add(new TextField(FIELD_CONTENTS, contents, Field.Store.YES));
                        writer.addDocument(doc);
                        i++;
                    }
                } catch (final IOException e) {
                    LOGGER.log(Level.WARNING, Resources.get(RK_SKIP_INDEXING, absolutePath), e);
                }
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
            LOGGER.log(Level.WARNING, Resources.get(RK_SKIP_INDEXING, file.toAbsolutePath().toString()));
            LOGGER.log(Level.WARNING, exc.getMessage(), exc);
            return FileVisitResult.CONTINUE;
        }

    }

    private static class TemporaryDirectory implements Closeable {

        private static final String PREFIX = "desktop-lucene-";

        private final Path path;

        private TemporaryDirectory() throws IOException {
            path = Files.createTempDirectory(PREFIX, new FileAttribute<?>[] {});
        }

        @Override
        public void close() throws IOException {
            Runtime.getRuntime().addShutdownHook(new Thread(new FileDeleter()));
        }

        private final class FileDeleter implements Runnable {

            @Override
            public void run() {
                try {
                    Files.walkFileTree(path, new DeleteFileVisitor());
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }

        private static class DeleteFileVisitor extends SimpleFileVisitor<Path> {

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
                    throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc)
                    throws IOException {
                if (exc == null) {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                } else {
                    throw exc;
                }
            }

        }

    }

}
