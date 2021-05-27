package view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import logic.boundary.CloneRepositoryBoundary;
import logic.exception.InvalidInputException;
import org.eclipse.jgit.api.errors.GitAPIException;


public class CloneRepositoryFxmlController extends BasicPageFxmlController {

    private CloneRepositoryBoundary boundary;

    @FXML
    private Button browseRepoPathButton;

    @FXML
    private TextField urlTextField;

    @FXML
    protected void submitButtonSelected(ActionEvent event) {
        String outputDir = this.repositoryLabel.getText();
        String url = this.urlTextField.getText();

        try {
            this.boundary = new CloneRepositoryBoundary(url, outputDir);
            this.boundary.cloneRepository();
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
        assert browseRepoPathButton != null : "fx:id=\"browseRepoPathButton\" was not injected: check your FXML file 'clone_page.fxml'.";
    }
}
