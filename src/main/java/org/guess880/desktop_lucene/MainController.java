package org.guess880.desktop_lucene;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooserBuilder;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.guess880.desktop_lucene.SearchResultWriter.WriterType;

public class MainController implements Initializable {

    private static final String FXML_PREFERENCES = "Preferences.fxml";

    private static final String RK_EXECUTE = "execute";

    private static final String RK_CANCEL = "cancel";

    private static final String RK_TITLE_PREFERENCES = "titlePrefer";

    @FXML
    private TextField txtKeyword;

    @FXML
    private TextField txtDirectory;

    @FXML
    private TextField txtIncludes;

    @FXML
    private TextField txtExcludes;

    @FXML
    private Button btnExecute;

    @FXML
    private CheckBox cbRegexp;

    @FXML
    private Tab tabLog;

    @FXML
    private TextArea txtLog;

    @FXML
    private Tab tabResults;

    @FXML
    private MenuButton mbSave;

    @FXML
    private TableView<SearchResult> tblResults;

    @FXML
    private MenuItem miOpenEditor;

    @FXML
    private MenuItem miCopyResult;

    @FXML
    private TableColumn<SearchResult, String> clmPath;

    @FXML
    private TableColumn<SearchResult, Long> clmLine;

    @FXML
    private TableColumn<SearchResult, String> clmContents;

    private final ObservableList<LogRecord> logs = FXCollections.observableArrayList();

    private Task<?> searchTask;

    private Preferences prefs;

    @Override
    public void initialize(final URL url, final ResourceBundle rb) {
        prefs = new Preferences();
        try {
            prefs.load();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        logs.addListener(new ListChangeListener<LogRecord>() {
            @Override
            public void onChanged(Change<? extends LogRecord> change) {
                while (change.next()) {
                    if (change.wasAdded()) {
                        final List<? extends LogRecord> addeds = change.getAddedSubList();
                        for (final LogRecord added : addeds) {
                            txtLog.appendText(added.toString());
                        }
                    }
                }
            }
        });
        tblResults.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<SearchResult>() {
            @Override
            public void changed(final ObservableValue<? extends SearchResult> obsearvable, final SearchResult oldValue,
                    final SearchResult newValue) {
                miOpenEditor.setDisable(newValue == null);
                miCopyResult.setDisable(newValue == null);
            }
        });
        tblResults.getItems().addListener(new ListChangeListener<SearchResult>() {
            @Override
            public void onChanged(javafx.collections.ListChangeListener.Change<? extends SearchResult> change) {
                mbSave.setDisable(tblResults.getItems().size() == 0);
            }
        });
        clmPath.setCellValueFactory(new PropertyValueFactory<SearchResult, String>(SearchResult.PATH));
        clmPath.prefWidthProperty().bind(tblResults.widthProperty().multiply(0.3));
        clmLine.setCellValueFactory(new PropertyValueFactory<SearchResult, Long>(SearchResult.LINE));
        clmLine.prefWidthProperty().bind(tblResults.widthProperty().multiply(0.1));
        clmContents.setCellValueFactory(new PropertyValueFactory<SearchResult, String>(SearchResult.CONTENTS));
        clmContents.prefWidthProperty().bind(tblResults.widthProperty().multiply(0.6));
    }

    @FXML
    private void executeSearch() {
        if (searchTask == null || searchTask.isDone()) {
            btnExecute.setText(Resources.get(RK_CANCEL));
            tblResults.getItems().clear();
            tabLog.getTabPane().getSelectionModel().select(tabLog);
            searchTask = new DesktopLuceneTask()
            .setKeyword(txtKeyword.getText())
            .setDirectory(txtDirectory.getText())
            .setIncludes(txtIncludes.getText())
            .setExcludes(txtExcludes.getText())
            .setUsesRegexp(cbRegexp.isSelected())
            .setLimit(Integer.MAX_VALUE)
            .setLogLevel(prefs.getLogPrefs().getShowDetailProperty().get() ? Level.FINEST : Level.INFO);
            DesktopLucene.SERVICE.submit(searchTask);
        } else {
            searchTask.cancel();
            btnExecute.setText(Resources.get(RK_EXECUTE));
        }
    }

    @FXML
    private void openDialog() {
        final File dir = DirectoryChooserBuilder.create().build().showDialog(null);
        if (dir != null) {
            txtDirectory.setText(dir.getAbsolutePath());
        }
    }

    @FXML
    private void clearLog() {
        logs.clear();
        txtLog.clear();
    }

    @FXML
    private void openPrefsDialog() throws IOException {
        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setResources(Resources.getResourceBundle());
        try (final InputStream is = getClass().getResourceAsStream(FXML_PREFERENCES);) {
            final Pane pane = (Pane) fxmlLoader.load(is);
            final PreferencesController prefsController = fxmlLoader.getController();
            prefsController.setPreferences(prefs);
            final Stage dialog = new Stage();
            dialog.initOwner(getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setScene(new Scene(pane));
//            dialog.setResizable(false);
            dialog.setTitle(Resources.get(RK_TITLE_PREFERENCES));
            dialog.showAndWait();
        }
    }

    private Window getWindow() {
        return txtKeyword.getScene().getWindow();
    }

    @FXML
    private void quit() {
        ((Stage) getWindow()).close();
    }

    @FXML
    private void openEditor() throws IOException {
        ExternalEditorExecutor
                .create()
                .setPreferences(prefs.getExtEditorPrefs())
                .setSearchResult(tblResults.getSelectionModel().getSelectedItem())
                .execute();
    }

    @FXML
    private void copyResult() {
        final ClipboardContent content = new ClipboardContent();
        content.putString(tblResults.getSelectionModel().getSelectedItem().toCsv());
        Clipboard.getSystemClipboard().setContent(content);
    }

    @FXML
    private void saveAsCsv() throws IOException {
        SearchResultWriter
                .create(WriterType.CSV)
                .setOwner(getWindow())
                .setPreferences(prefs.getSaveSearchResultsPrefs())
                .write(tblResults.getItems());
    }

    @FXML
    private void saveAsTsv() throws IOException {
        SearchResultWriter
                .create(WriterType.TSV)
                .setOwner(getWindow())
                .setPreferences(prefs.getSaveSearchResultsPrefs())
                .write(tblResults.getItems());
    }

    @FXML
    private void saveAsPreferredFormat() throws IOException {
        SearchResultWriter
                .create(WriterType.PREF)
                .setOwner(getWindow())
                .setPreferences(prefs.getSaveSearchResultsPrefs())
                .write(tblResults.getItems());
    }

    private class DesktopLuceneTask extends LuceneTask {

        @Override
        protected void succeeded() {
            super.succeeded();
            btnExecute.setText(Resources.get(RK_EXECUTE));
            if (!getResults().isEmpty()) {
                tblResults.getItems().addAll(getResults());
                tabResults.getTabPane().getSelectionModel().select(tabResults);
            }
        }

        @Override
        protected void cancelled() {
            super.cancelled();
            btnExecute.setText(Resources.get(RK_EXECUTE));
        }

        @Override
        protected void failed() {
            super.failed();
            btnExecute.setText(Resources.get(RK_EXECUTE));
        }

        @Override
        protected void messageChanged(final String newMessage) {
            txtLog.appendText(newMessage);
        }

    }

}
