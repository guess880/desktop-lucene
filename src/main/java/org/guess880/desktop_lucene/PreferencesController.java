package org.guess880.desktop_lucene;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooserBuilder;
import javafx.stage.FileChooserBuilder;
import javafx.stage.Stage;

public class PreferencesController implements Initializable {

    @FXML
    private TextField txtExtEditorPath;

    @FXML
    private TextField txtExtEditorArgs;

    @FXML
    private TextField txtExtEditorWkDir;

    @FXML
    private ChoiceBox<String> cbSearchResultsCharset;

    @FXML
    private TextField txtSearchResultHeader;

    @FXML
    private TextField txtSearchResultBody;

    @FXML
    private TextField txtSearchResultFooter;

    @FXML
    private CheckBox cbShowDetailLog;

    private Preferences prefs;

    @Override
    public void initialize(final URL url, final ResourceBundle rb) {
        cbSearchResultsCharset.getItems().addAll(Charset.availableCharsets().keySet());
    }

    protected PreferencesController setPreferences(final Preferences prefs) {
        this.prefs = prefs;
        txtExtEditorPath.textProperty().bindBidirectional(prefs.getExtEditorPrefs().pathProperty());
        txtExtEditorArgs.textProperty().bindBidirectional(prefs.getExtEditorPrefs().argsProperty());
        txtExtEditorWkDir.textProperty().bindBidirectional(prefs.getExtEditorPrefs().wkDirProperty());
        cbSearchResultsCharset.valueProperty().bindBidirectional(prefs.getSaveSearchResultsPrefs().getCharsetProperty());
        txtSearchResultHeader.textProperty().bindBidirectional(prefs.getSaveSearchResultsPrefs().getHeaderProperty());
        txtSearchResultBody.textProperty().bindBidirectional(prefs.getSaveSearchResultsPrefs().getBodyProperty());
        txtSearchResultFooter.textProperty().bindBidirectional(prefs.getSaveSearchResultsPrefs().getFooterProperty());
        cbShowDetailLog.selectedProperty().bindBidirectional(prefs.getLogPrefs().getShowDetailProperty());
        return this;
    }

    @FXML
    private void openExtEditorPathChooser() {
        final File file = FileChooserBuilder.create().build().showOpenDialog(null);
        if (file != null) {
            txtExtEditorPath.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void openExtEditorWkDirChooser() {
        final File dir = DirectoryChooserBuilder.create().build().showDialog(null);
        if (dir != null) {
            txtExtEditorWkDir.setText(dir.getAbsolutePath());
        }
    }

    @FXML
    private void ok() throws IOException {
        prefs.save();
        close();
    }

    @FXML
    private void cancel() {
        close();
    }

    private void close() {
        ((Stage) txtExtEditorArgs.getScene().getWindow()).close();
    }

}
