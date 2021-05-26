package view;

import javafx.event.ActionEvent;
import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ClassBugginessFxmlController extends BasicPage{

    @FXML
    private Button browseRepoPathButton;

    @FXML
    private Label outputFileLabel;

    @FXML
    private Button browseOutputFileButton;

    @FXML
    private TextField projectName;

    @FXML
    void browseOutputFilePath(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select output file");
        File f = fileChooser.showSaveDialog(new Stage());
        if (f != null) {
            String path = f.getPath();
            this.outputFileLabel.setText(path);
        }
    }

    @FXML
    protected void initialize() {
        super.initialize();
        assert browseRepoPathButton != null : "fx:id=\"browseRepoPathButton\" was not injected: check your FXML file 'class_bugginess.fxml'.";
        assert outputFileLabel != null : "fx:id=\"outputFileLabel\" was not injected: check your FXML file 'class_bugginess.fxml'.";
        assert browseOutputFileButton != null : "fx:id=\"browseOutputFilehButton\" was not injected: check your FXML file 'class_bugginess.fxml'.";
        assert projectName != null : "fx:id=\"projectNameLabel\" was not injected: check your FXML file 'class_bugginess.fxml'.";
    }

    public void submitButtonSelected(ActionEvent actionEvent) {
    }
}

