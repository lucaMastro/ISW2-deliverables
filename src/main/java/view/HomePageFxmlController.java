package view;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class HomePageFxmlController extends Application {

    @FXML
    private Button cloneRepoButton;


    @FXML
    private Button classesBugginess;

    @FXML
    private void classesBugginessSelected(ActionEvent event) throws IOException {
        SceneSwitcher.getInstance().setBugginessScene(event);
    }

    @FXML
    private void cloneRepositorySelected(ActionEvent event) throws IOException {
        SceneSwitcher.getInstance().setCloneRepositoryScene(event);
    }

    @FXML
    void wekaButtonSelected(ActionEvent event) throws IOException {
        SceneSwitcher.getInstance().setWekaScene(event);
    }

    @FXML
    private void initialize() {
        assert cloneRepoButton != null : "fx:id=\"cloneRepoButton\" was not injected: check your FXML file 'home.fxml'.";
        assert classesBugginess != null : "fx:id=\"classesBugginess\" was not injected: check your FXML file 'home.fxml'.";
    }

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        var loader = new FXMLLoader();
        var url = getClass().getResource("/view/home.fxml");
        loader.setLocation(url);

        Parent root = loader.load();
        var scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }
}
