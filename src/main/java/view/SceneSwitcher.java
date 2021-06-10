package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import javafx.event.ActionEvent;
import javafx.util.Duration;
import javafx.util.converter.IntegerStringConverter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.function.UnaryOperator;

public class SceneSwitcher extends Application {
	private Scene scene;
	private Stage primaryStage;
	private static SceneSwitcher instance = null;

	public static SceneSwitcher getInstance(){
		if (SceneSwitcher.instance == null)
			SceneSwitcher.instance = new SceneSwitcher();
		return SceneSwitcher.instance;
	}

	@Override
	public void start(Stage stage) throws IOException {
		this.setHomeScene(stage);
	}

	public void setHomeScene(Stage stage) throws IOException {
		this.primaryStage = stage;
		Parent root = FXMLLoader.load(getClass().getResource("home.fxml"));
		this.scene = new Scene(root);
		primaryStage.setScene(scene);
		this.primaryStage.setTitle("Home page");
		primaryStage.show();
	}

	public void setHomeScene(ActionEvent event) throws IOException {
		this.primaryStage = (Stage)((Node)event.getSource()).getScene().getWindow();
		this.primaryStage.setTitle("Home page");
		this.setHomeScene(this.primaryStage);
	}

	public void setControlChartScene(ActionEvent event) throws IOException {
		Parent controlChartScene = FXMLLoader.load(getClass().getResource("process_control_chart.fxml"));
		this.scene = new Scene(controlChartScene);
		this.primaryStage = (Stage)((Node)event.getSource()).getScene().getWindow();
		this.primaryStage.setTitle("Process control chart");
		this.primaryStage.setScene(scene);
		this.primaryStage.show();
	}


	public void setWekaScene(ActionEvent event) throws IOException {
		Parent wekaScene = FXMLLoader.load(getClass().getResource("weka_analisys.fxml"));
		this.scene = new Scene(wekaScene);
		this.primaryStage = (Stage)((Node)event.getSource()).getScene().getWindow();
		this.primaryStage.setTitle("Weka analisys");
		this.primaryStage.setScene(scene);
		this.primaryStage.show();
	}

	public void setBugginessScene(ActionEvent event) throws IOException {
		Parent bugginessScene = FXMLLoader.load(getClass().getResource("class_bugginess.fxml"));
		this.scene = new Scene(bugginessScene);
		this.primaryStage = (Stage)((Node)event.getSource()).getScene().getWindow();
		this.primaryStage.setTitle("Proportion analisys");
		this.primaryStage.setScene(scene);
		this.primaryStage.show();
	}

	public void setCloneRepositoryScene(ActionEvent event) throws IOException {
		Parent cloneScene = FXMLLoader.load(getClass().getResource("clone_page.fxml"));
		this.scene = new Scene(cloneScene);
		this.primaryStage = (Stage)((Node)event.getSource()).getScene().getWindow();
		this.primaryStage.setTitle("Clone a repository");
		this.primaryStage.setScene(scene);
		this.primaryStage.show();
	}

	public void informationAlertShow(String informationString) {
		var loginAlert = new Alert(Alert.AlertType.INFORMATION);
		loginAlert.setTitle("Confirmation Alert");
		loginAlert.setHeaderText(null);
		loginAlert.setContentText(informationString);

		loginAlert.showAndWait();
	}

	public void errorAlertShow(String alertString) {
		var loginAlert = new Alert(Alert.AlertType.ERROR);
		loginAlert.setTitle("Alert");
		loginAlert.setHeaderText(null);
		loginAlert.setContentText(alertString);

		loginAlert.show();
	}

	public void showTooltip(Label node, Tooltip tooltipOpened, String classStyle){
		tooltipOpened.setShowDelay(Duration.ZERO);

		tooltipOpened.setText(node.getText());
		tooltipOpened.getStyleClass().add(classStyle);

		var bounds = node.localToScreen(node.getBoundsInLocal());
		tooltipOpened.setX(bounds.getMinX());
		tooltipOpened.setY(bounds.getMinY() - node.getHeight());

		tooltipOpened.show(node, bounds.getMinX(), bounds.getMinY() - node.getHeight());
	}

	public void setWorkingCursor(){
		this.scene.setCursor(Cursor.WAIT);
	}

	public void setDefautlCursor() {
		this.scene.setCursor(Cursor.DEFAULT);
	}

	public void initializeIntegerSpinner(Spinner<Integer> spinner){
		var format = NumberFormat.getIntegerInstance();
		UnaryOperator<TextFormatter.Change> filter = c -> {
			if (c.isContentChange()) {
				var parsePosition = new ParsePosition(0);
				// NumberFormat evaluates the beginning of the text
				format.parse(c.getControlNewText(), parsePosition);
				if (parsePosition.getIndex() == 0 ||
						parsePosition.getIndex() < c.getControlNewText().length()) {
					// reject parsing the complete text failed
					return null;
				}
			}
			return c;
		};
		TextFormatter<Integer> priceFormatter = new TextFormatter<>(
				new IntegerStringConverter(), 0, filter);
		var fact = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE);
		spinner.setValueFactory(fact);
		spinner.getEditor().setTextFormatter(priceFormatter);
	}

	public static void main(String[] args) { launch(args); }

}