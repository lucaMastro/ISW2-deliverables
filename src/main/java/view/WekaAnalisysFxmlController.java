package view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;


public class WekaAnalisysFxmlController extends BasicPageFxmlController {

    @Override
    protected void submitButtonSelected(ActionEvent actionEvent) {

    }

    @FXML
    @Override
    protected void initialize() {
        super.initialize();
        assert submitButton != null : "fx:id=\"submitButton\" was not injected: check your FXML file 'weka_analisys.fxml'.";
        assert backButton != null : "fx:id=\"backButton\" was not injected: check your FXML file 'weka_analisys.fxml'.";
        assert interruptButton != null : "fx:id=\"interruptButton\" was not injected: check your FXML file 'weka_analisys.fxml'.";

    }
}
