package view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import logic.boundary.CloneRepositoryBoundary;
import logic.exception.InvalidInputException;
import org.eclipse.jgit.api.errors.GitAPIException;


public class CloneRepositoryFxmlController extends BasicPageFxmlController {

    @FXML
    private TextField urlTextField;

    @FXML
    protected void submitButtonSelected(ActionEvent event) {
        String outputDir = this.repositoryLabel.getText();
        String url = this.urlTextField.getText();

        try {
            var boundary = new CloneRepositoryBoundary(url, outputDir);
            boundary.cloneRepository();
            SceneSwitcher.getInstance().informationAlertShow("Done!!");
        }
        catch (InvalidInputException | GitAPIException e){
            SceneSwitcher.getInstance().errorAlertShow(e.getMessage());
        }
    }

    @FXML
    @Override
    protected void initialize() {
        super.initialize();
    }
}
