package view;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import logic.boundary.ProcessControlChartBoundary;

public class ProcessControlChartFxmlController extends ProcessControlChartAndProportionFxmlController {

    @FXML
    private Spinner<Integer> thresholdSpinner;


    @Override
    protected void submitButtonSelected(ActionEvent actionEvent) {

        var boundary = new ProcessControlChartBoundary(this.outputFileLabel.getText(),
                this.repositoryLabel.getText(),
                this.projectName.getText(),
                this.thresholdSpinner.getValue());
        var task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                boundary.runUseCase();
                return null;
            }
        };
        this.runTask(task);
    }

    @Override
    protected void initialize(){
        super.initialize();

        //prevent chars value on spinner
        SceneSwitcher.getInstance().initializeIntegerSpinner(this.thresholdSpinner);
    }
}

