package view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import logic.boundary.FindBugginessBoundary;
import logic.enums.ProportionAlgoOptions;

public class ClassBugginessFxmlController extends ProcessControlChartAndProportionFxmlController {


    @FXML
    private ComboBox<String> proportionPossibilities;

    @FXML
    private Label windowsPeriodLabel;

    @FXML
    private Spinner<Integer> windowPeriodValue;


    @Override
    protected void submitButtonSelected(ActionEvent actionEvent) {
        this.changeEditability();

        var boundary = new FindBugginessBoundary(this.outputFileLabel.getText(),
                this.repositoryLabel.getText(),
                this.projectName.getText(),
                this.proportionPossibilities.getValue());
        this.job = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                boundary.runUseCase();
                return null;
            }
        };

        this.runTask();
        this.interruptButton.setVisible(Boolean.TRUE);
        this.interruptButton.setDisable(Boolean.FALSE);
    }

    @Override
    protected void initialize(){
        super.initialize();
        this.repositoryLabel.setText("/home/luca/Scrivania/ISW2/deliverables/deliverable2/bookkeeper");
        this.outputFileLabel.setText("/home/luca/Scrivania/bookkeeperGUI.csv");
        this.projectName.setText("bookkeeper");

        this.editableItems.add(this.windowPeriodValue);
        this.editableItems.add(this.proportionPossibilities);

        ObservableList<String> options = FXCollections.observableArrayList();
        for (ProportionAlgoOptions obj : ProportionAlgoOptions.class.getEnumConstants()){
            options.add(obj.getAlgo());
        }
        this.proportionPossibilities.getItems().addAll(options);
        //showing other elements if window mode is selected:
        this.proportionPossibilities.valueProperty().addListener((obs, oldItem, newItem) ->{
            if (newItem.equals(ProportionAlgoOptions.PROPORTION_MOVING_WINDOW.getAlgo())){
                this.windowPeriodValue.setVisible(Boolean.TRUE);
                this.windowsPeriodLabel.setVisible(Boolean.TRUE);
            }
            else{
                this.windowPeriodValue.setVisible(Boolean.FALSE);
                this.windowsPeriodLabel.setVisible(Boolean.FALSE);
            }
        });

        SceneSwitcher.getInstance().initializeIntegerSpinner(this.windowPeriodValue);
    }
}

