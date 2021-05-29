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

public class ClassBugginessFxmlController extends ProcessControlChartAndProportionFxmlController {



    @Override
    protected void submitButtonSelected(ActionEvent actionEvent) {
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

