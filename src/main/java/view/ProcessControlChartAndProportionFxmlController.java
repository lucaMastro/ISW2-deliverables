package view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public abstract class ProcessControlChartAndProportionFxmlController extends BasicPageFxmlController {

    @FXML
    protected Label outputFileLabel;

    @FXML
    protected Button browseOutputFileButton;

    @FXML
    protected TextField projectName;

    @FXML
    protected void browseOutputFilePath(ActionEvent event) {
        var s = SceneSwitcher.getInstance().fileBrowser("Select output file", Boolean.TRUE);
        this.outputFileLabel.setText(s);
    }

    @FXML
    @Override
    protected void initialize() {
        super.initialize();
        assert outputFileLabel != null : "fx:id=\"outputFileLabel\" was not injected: check your FXML file 'process_control_chart.fxml'.";
        assert browseOutputFileButton != null : "fx:id=\"browseOutputFilehButton\" was not injected: check your FXML file 'process_control_chart.fxml'.";
        assert projectName != null : "fx:id=\"projectNameLabel\" was not injected: check your FXML file 'process_control_chart.fxml'.";

        this.editableItems.add(this.projectName);
        this.editableItems.add(this.browseOutputFileButton);

    }
}
