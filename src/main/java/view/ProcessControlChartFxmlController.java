package view;

import javafx.event.ActionEvent;
import logic.boundary.ProcessControlChartBoundary;
import logic.exception.InvalidRangeException;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;

public class ProcessControlChartFxmlController extends ProcessControlChartAndProportionFxmlController {


    @Override
    protected void submitButtonSelected(ActionEvent actionEvent) {
        try {
            var boundary = new ProcessControlChartBoundary(this.outputFileLabel.getText(),
                    this.repositoryLabel.getText(),
                    this.projectName.getText());
            boundary.runUseCase();
            SceneSwitcher.getInstance().informationAlertShow("Done!!");
        }catch (InvalidRangeException | GitAPIException | IOException e){
            SceneSwitcher.getInstance().errorAlertShow(e.getMessage());
        }
    }
}

