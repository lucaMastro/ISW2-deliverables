package view;

import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class WekaAnalisysFxmlController extends BasicPageFxmlController {

    @FXML
    private Button submitButton;

    @FXML
    private JFXButton backButton;

    @FXML
    private Button interruptButton;

    @Override
    protected void submitButtonSelected(ActionEvent actionEvent) {

    }

    @FXML
    protected void initialize() {
        super.initialize();
        assert submitButton != null : "fx:id=\"submitButton\" was not injected: check your FXML file 'weka_analisys.fxml'.";
        assert backButton != null : "fx:id=\"backButton\" was not injected: check your FXML file 'weka_analisys.fxml'.";
        assert interruptButton != null : "fx:id=\"interruptButton\" was not injected: check your FXML file 'weka_analisys.fxml'.";

    }
}
