package view;

import javafx.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.jfoenix.controls.JFXButton;

public class ClassBugginessFxmlController {

    @FXML
    private Label repositoryLabel;

    @FXML
    private Button browseRepoPathButton;

    @FXML
    private Label outputFileLabel;

    @FXML
    private Button browseOutputFilehButton;

    @FXML
    private TextField projectName;

    @FXML
    private Button SubmitButton;

    @FXML
    private JFXButton backButton;

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
    void browseRepositoryPath(ActionEvent event) {
        DirectoryChooser repository = new DirectoryChooser();
        repository.setTitle("Select repository for bugginess analisys");
        File dir = repository.showDialog(new Stage());
        if (dir != null) {
            String path = dir.getPath();
            this.repositoryLabel.setText(path);
        }
    }

    @FXML
    public void backButtonSelected(ActionEvent actionEvent) throws IOException {
        SceneSwitcher.getInstance().setHomeScene(actionEvent);
    }

    @FXML
    void initialize() {
        assert repositoryLabel != null : "fx:id=\"repositoryLabel\" was not injected: check your FXML file 'class_bugginess.fxml'.";
        assert browseRepoPathButton != null : "fx:id=\"browseRepoPathButton\" was not injected: check your FXML file 'class_bugginess.fxml'.";
        assert outputFileLabel != null : "fx:id=\"outputFileLabel\" was not injected: check your FXML file 'class_bugginess.fxml'.";
        assert browseOutputFilehButton != null : "fx:id=\"browseOutputFilehButton\" was not injected: check your FXML file 'class_bugginess.fxml'.";
        assert projectName != null : "fx:id=\"projectNameLabel\" was not injected: check your FXML file 'class_bugginess.fxml'.";
        assert SubmitButton != null : "fx:id=\"SubmitButton\" was not injected: check your FXML file 'class_bugginess.fxml'.";
        assert backButton != null : "fx:id=\"backButton\" was not injected: check your FXML file 'class_bugginess.fxml'.";
    }

    public void submitButtonSelected(ActionEvent actionEvent) {
    }
}

