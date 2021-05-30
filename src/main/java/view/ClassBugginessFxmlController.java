package view;

import javafx.event.ActionEvent;
import java.io.IOException;
import logic.boundary.FindBugginessBoundary;
import logic.exception.InvalidRangeException;
import org.eclipse.jgit.api.errors.GitAPIException;

public class ClassBugginessFxmlController extends ProcessControlChartAndProportionFxmlController {



    @Override
    protected void submitButtonSelected(ActionEvent actionEvent) {
        try {
            this.progressBar.setVisible(Boolean.TRUE);
            var boundary = new FindBugginessBoundary(this.outputFileLabel.getText(),
                    this.repositoryLabel.getText(),
                    this.projectName.getText());
            boundary.runUseCase();
            SceneSwitcher.getInstance().informationAlertShow("Done!!");
        }catch (InvalidRangeException |GitAPIException | IOException e){
            SceneSwitcher.getInstance().errorAlertShow(e.getMessage());
        }
        finally {
            this.progressBar.setVisible(Boolean.FALSE);
        }
    }
}

