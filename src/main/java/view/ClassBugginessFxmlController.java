package view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import logic.boundary.FindBugginessBoundary;
import logic.enums.ProportionAlgoOptions;

public class ClassBugginessFxmlController extends ProcessControlChartAndProportionFxmlController {


    @FXML
    private ComboBox<String> proportionPossibilities;

    @Override
    protected void submitButtonSelected(ActionEvent actionEvent) {
        var boundary = new FindBugginessBoundary(this.outputFileLabel.getText(),
                this.repositoryLabel.getText(),
                this.projectName.getText(),
                this.proportionPossibilities.getValue());
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
        this.repositoryLabel.setText("/home/luca/Scrivania/ISW2/deliverables/deliverable2/bookkeeper");
        this.outputFileLabel.setText("/home/luca/Scrivania/bookkeeperGUI.csv");
        this.projectName.setText("bookkeeper");

        ObservableList<String> options = FXCollections.observableArrayList();
        for (ProportionAlgoOptions obj : ProportionAlgoOptions.class.getEnumConstants()){
            options.add(obj.getAlgo());
        }
        this.proportionPossibilities.getItems().addAll(options);
    }
}

