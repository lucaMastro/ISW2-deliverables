package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import javafx.event.ActionEvent;
import java.io.IOException;

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
		primaryStage.show();
	}

	public void setHomeScene(ActionEvent event) throws IOException {
		Stage stage = this.primaryStage = (Stage)((Node)event.getSource()).getScene().getWindow();
		this.setHomeScene(stage);
	}

	public void setBugginessInputScene(ActionEvent event) throws IOException {
		Parent bugginessScene = FXMLLoader.load(getClass().getResource("class_bugginess.fxml"));
		this.scene = new Scene(bugginessScene);
		this.primaryStage = (Stage)((Node)event.getSource()).getScene().getWindow();
		this.primaryStage.setScene(scene);
		this.primaryStage.show();
	}

	public void setCloneRepositoryScene(ActionEvent event) throws IOException {
		Parent cloneScene = FXMLLoader.load(getClass().getResource("clone_page.fxml"));
		this.scene = new Scene(cloneScene);
		this.primaryStage = (Stage)((Node)event.getSource()).getScene().getWindow();
		this.primaryStage.setScene(scene);
		this.primaryStage.show();
	}

	public void informationAlertShow(String informationString) {
		Alert loginAlert = new Alert(Alert.AlertType.INFORMATION);
		loginAlert.setTitle("Confirmation Alert");
		loginAlert.setHeaderText(null);
		loginAlert.setContentText(informationString);

		loginAlert.showAndWait();
	}

	public static void main(String[] args) { launch(args); }

}