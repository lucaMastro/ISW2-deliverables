package view;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import logic.boundary.CloneRepositoryBoundary;

public class CloneRepositoryFxmlController extends BasicPageFxmlController {

    @FXML
    private TextField urlTextField;

    @FXML
    protected void submitButtonSelected(ActionEvent event) {
        this.changeEditability();

        String outputDir = this.repositoryLabel.getText();
        String url = this.urlTextField.getText();
        var boundary = new CloneRepositoryBoundary(url, outputDir);
        this.job = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                boundary.cloneRepository();
                return null;
            }
        };
        this.runTask();

        this.interruptButton.setVisible(Boolean.TRUE);
    }

    @FXML
    @Override
    protected void initialize() {
        super.initialize();
        this.editableItems.add(this.urlTextField);
    }
}
