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

public abstract class BasicPageFxmlController {

    @FXML
    protected Button submitButton;

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
        String s;
        /* Different titles for each use case, based on the object type instantiated    */
        if (this.getClass().getName().contains("Clone"))
            s = "Select output directory";
        else
            s = "Select repository for bugginess analisys";

        var repository = new DirectoryChooser();
        repository.setTitle(s);
        File dir = repository.showDialog(new Stage());
        if (dir != null) {
            String path = dir.getPath();
            this.repositoryLabel.setText(path);
        }
    }

    @FXML
    protected abstract void submitButtonSelected(ActionEvent actionEvent);

    protected void initialize(){
        assert repositoryLabel != null : "fx:id=\"repositoryLabel\" was not injected: check your FXML file 'class_bugginess_and_control_chart.fxml'.";
        assert submitButton != null : "fx:id=\"SubmitButton\" was not injected: check your FXML file 'class_bugginess_and_control_chart.fxml'.";
        assert backButton != null : "fx:id=\"backButton\" was not injected: check your FXML file 'class_bugginess_and_control_chart.fxml'.";
    }

}
