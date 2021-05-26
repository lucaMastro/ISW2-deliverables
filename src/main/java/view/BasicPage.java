package view;

import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public abstract class BasicPage {

    @FXML
    protected Button SubmitButton;

    @FXML
    protected JFXButton backButton;

    @FXML
    protected Label repositoryLabel;

    @FXML
    protected void backButtonSelected(ActionEvent actionEvent) throws IOException {
        SceneSwitcher.getInstance().setHomeScene(actionEvent);
    }

    @FXML
    protected void browseRepositoryPath(ActionEvent event) {
        DirectoryChooser repository = new DirectoryChooser();
        repository.setTitle("Select repository for bugginess analisys");
        File dir = repository.showDialog(new Stage());
        if (dir != null) {
            String path = dir.getPath();
            this.repositoryLabel.setText(path);
        }
    }

    @FXML
    protected abstract void submitButtonSelected(ActionEvent actionEvent) throws Exception;

    protected void initialize(){
        assert repositoryLabel != null : "fx:id=\"repositoryLabel\" was not injected: check your FXML file 'class_bugginess.fxml'.";
        assert SubmitButton != null : "fx:id=\"SubmitButton\" was not injected: check your FXML file 'class_bugginess.fxml'.";
        assert backButton != null : "fx:id=\"backButton\" was not injected: check your FXML file 'class_bugginess.fxml'.";
    }

}
