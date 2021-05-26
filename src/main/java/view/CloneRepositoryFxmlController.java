package view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import logic.boundary.CloneRepositoryBoundary;
import org.eclipse.jgit.api.errors.GitAPIException;


public class CloneRepositoryFxmlController extends BasicPage{

    private CloneRepositoryBoundary boundary;

    @FXML
    private Button browseRepoPathButton;

    @FXML
    private TextField urlTextField;

    @FXML
    protected void submitButtonSelected(ActionEvent event) throws GitAPIException {
        String outputDir = this.repositoryLabel.getText();
        String url = this.urlTextField.getText();

        this.boundary = new CloneRepositoryBoundary(url, outputDir);
        this.boundary.cloneRepository();
        SceneSwitcher.getInstance().informationAlertShow("Done!!");
    }




    @FXML
    protected void initialize() {
        super.initialize();
        assert browseRepoPathButton != null : "fx:id=\"browseRepoPathButton\" was not injected: check your FXML file 'clone_page.fxml'.";
    }
}
