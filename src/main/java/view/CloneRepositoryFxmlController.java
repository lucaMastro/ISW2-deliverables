package view;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import logic.boundary.CloneRepositoryBoundary;
import logic.exception.InvalidInputException;

public class CloneRepositoryFxmlController extends BasicPageFxmlController {

    @FXML
    private TextField urlTextField;

    @FXML
    protected void submitButtonSelected(ActionEvent event) {
        String outputDir = this.repositoryLabel.getText();
        String url = this.urlTextField.getText();

        try {
            var boundary = new CloneRepositoryBoundary(url, outputDir);
            var task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    boundary.cloneRepository();
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
        catch (InvalidInputException e){
            SceneSwitcher.getInstance().errorAlertShow(e.getMessage());
        }

    }

    @FXML
    @Override
    protected void initialize() {
        super.initialize();
    }
}
