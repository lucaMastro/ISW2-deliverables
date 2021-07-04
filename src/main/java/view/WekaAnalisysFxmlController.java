package view;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import logic.boundary.WekaAnalisysBoundary;

public class WekaAnalisysFxmlController extends BasicPageFxmlController {

    @FXML
    private Label inputFileLabel;

    @FXML
    private Label arffDescriptionLabel;

    @FXML
    private Label outputFileLabel;

    @FXML
    private Label arffFileLabel;

    @FXML
    private Button browseInputFileButton;

    @FXML
    private Button browseOutputFileButton;

    @FXML
    private Button browseArffFileButton;

    @FXML
    private RadioButton saveAsArff;


    @Override
    protected void submitButtonSelected(ActionEvent actionEvent) {
        this.changeEditability();
        var boundary = new WekaAnalisysBoundary(this.inputFileLabel.getText(),
                this.outputFileLabel.getText(),
                this.arffFileLabel.getText());

        this.job = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                boundary.runAnalisys();
                return null;
            }
        };

        this.runTask();
        this.interruptButton.setVisible(Boolean.TRUE);
        this.interruptButton.setDisable(Boolean.FALSE);
    }

    @FXML
    void arffOutputButtonClicked(ActionEvent event) {
        var s = SceneSwitcher.getInstance().fileBrowser("Select output file", Boolean.TRUE);
        this.arffFileLabel.setText(s);
    }


    @FXML
    void csvInputButtonClicked(ActionEvent event) {
        var s = SceneSwitcher.getInstance().fileBrowser("Select input file", Boolean.FALSE);
        this.inputFileLabel.setText(s);
    }

    @FXML
    void csvOutputButtonClicked(ActionEvent event) {
        var s = SceneSwitcher.getInstance().fileBrowser("Select output file", Boolean.TRUE);
        this.outputFileLabel.setText(s);
    }


    @FXML
    void arffRadioButtonSelected(ActionEvent event) {
        var current = this.arffFileLabel.isVisible();

        this.arffFileLabel.setVisible(!current);
        this.browseArffFileButton.setVisible(!current);
        this.arffDescriptionLabel.setVisible(!current);
    }


    @FXML
    @Override
    protected void initialize() {
        super.initialize();
        assert inputFileLabel != null : "fx:id=\"inputFileLabel\" was not injected: check your FXML file 'weka_analisys.fxml'.";
        assert outputFileLabel != null : "fx:id=\"outputFileLabel\" was not injected: check your FXML file 'weka_analisys.fxml'.";
        assert browseInputFileButton != null : "fx:id=\"browseInputFileButton\" was not injected: check your FXML file 'weka_analisys.fxml'.";
        assert browseOutputFileButton != null : "fx:id=\"browseOutputFileButton\" was not injected: check your FXML file 'weka_analisys.fxml'.";
        assert saveAsArff != null : "fx:id=\"saveAsArff\" was not injected: check your FXML file 'weka_analisys.fxml'.";
        assert arffFileLabel != null : "fx:id=\"arffFileLabel\" was not injected: check your FXML file 'weka_analisys.fxml'.";
        assert browseArffFileButton != null : "fx:id=\"browseArffFileButton\" was not injected: check your FXML file 'weka_analisys.fxml'.";

        this.editableItems.add(this.browseInputFileButton);
        this.editableItems.add(this.browseArffFileButton);
        this.editableItems.add(this.browseOutputFileButton);
        this.editableItems.add(this.saveAsArff);

    }
}
