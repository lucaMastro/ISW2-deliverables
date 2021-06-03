package view;

import com.jfoenix.controls.JFXButton;
import javafx.concurrent.Task;
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
    protected Button browseRepoPathButton;

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
        else if (this.getClass().getName().contains("Chart"))
            s = "Select repository for process control chart analisys";
        else
            s = "Select repository for proportion analisys";

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

    protected void runTask(Task<Void> task){
        task.setOnSucceeded(e ->{
            SceneSwitcher.getInstance().informationAlertShow("Done!!");
            SceneSwitcher.getInstance().setDefautlCursor();
        });

        task.setOnFailed(e ->{
            var exc = task.getException();
            SceneSwitcher.getInstance().errorAlertShow(exc.getMessage());
            SceneSwitcher.getInstance().setDefautlCursor();
        });

        SceneSwitcher.getInstance().setWorkingCursor();
        var t = new Thread(task);
        t.start();
    }

    protected void initialize(){
        assert repositoryLabel != null : "fx:id=\"repositoryLabel\" was not injected: check your FXML file 'process_control_chart.fxml'.";
        assert submitButton != null : "fx:id=\"SubmitButton\" was not injected: check your FXML file 'process_control_chart.fxml'.";
        assert backButton != null : "fx:id=\"backButton\" was not injected: check your FXML file 'process_control_chart.fxml'.";
        assert browseRepoPathButton != null : "fx:id=\"browseRepoPathButton\" was not injected: check your FXML file 'clone_page.fxml'.";
    }

}
