package view;

import javafx.event.ActionEvent;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import logic.boundary.FindBugginessBoundary;
import logic.exception.InvalidRangeException;
import org.eclipse.jgit.api.errors.GitAPIException;

public class ClassBugginessFxmlController extends BasicPageFxmlController {

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
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Select output file");
        var f = fileChooser.showSaveDialog(new Stage());
        if (f != null) {
            String path = f.getPath();
            this.outputFileLabel.setText(path);
        }
    }

    @FXML
    @Override
    protected void initialize() {
        super.initialize();
        assert browseRepoPathButton != null : "fx:id=\"browseRepoPathButton\" was not injected: check your FXML file 'process_control_chart.fxml'.";
        assert outputFileLabel != null : "fx:id=\"outputFileLabel\" was not injected: check your FXML file 'process_control_chart.fxml'.";
        assert browseOutputFileButton != null : "fx:id=\"browseOutputFilehButton\" was not injected: check your FXML file 'process_control_chart.fxml'.";
        assert projectName != null : "fx:id=\"projectNameLabel\" was not injected: check your FXML file 'process_control_chart.fxml'.";
    }

    @Override
    public void submitButtonSelected(ActionEvent actionEvent) {
        try {
            var boundary = new FindBugginessBoundary(this.outputFileLabel.getText(),
                    this.repositoryLabel.getText(),
                    this.projectName.getText());
            boundary.runUseCase();
            SceneSwitcher.getInstance().informationAlertShow("Done!!");
        }catch (InvalidRangeException |GitAPIException | IOException e){
            SceneSwitcher.getInstance().errorAlertShow(e.getMessage());
        }
    }
}

