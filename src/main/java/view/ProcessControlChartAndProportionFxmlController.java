package view;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import logic.abstracts.AbstractBoundary;

public abstract class ProcessControlChartAndProportionFxmlController extends BasicPageFxmlController {

    @FXML
    protected Label outputFileLabel;

    @FXML
    protected Button browseOutputFileButton;

    @FXML
    protected TextField projectName;

    @FXML
    protected void browseOutputFilePath(ActionEvent event) {
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Select output file");
        var f = fileChooser.showSaveDialog(new Stage());
        if (f != null) {
            String path = f.getPath();
            this.outputFileLabel.setText(path);
        }
    }

    protected void runTask(AbstractBoundary boundary){
        var task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                boundary.runUseCase();
                return null;
            }
        };
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

    @FXML
    @Override
    protected void initialize() {
        super.initialize();
        assert outputFileLabel != null : "fx:id=\"outputFileLabel\" was not injected: check your FXML file 'process_control_chart.fxml'.";
        assert browseOutputFileButton != null : "fx:id=\"browseOutputFilehButton\" was not injected: check your FXML file 'process_control_chart.fxml'.";
        assert projectName != null : "fx:id=\"projectNameLabel\" was not injected: check your FXML file 'process_control_chart.fxml'.";
    }
}
