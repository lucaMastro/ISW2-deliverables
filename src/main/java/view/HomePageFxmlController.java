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
import java.lang.annotation.Target;
import java.net.URL;

public class HomePageFxmlController extends Application {

    @FXML
    private Button cloneRepoButton;

    @FXML
    private Button processControlChartButton;

    @FXML
    private Button classesBugginess;

    @FXML
    private void classesBugginessSelected(ActionEvent event) throws IOException {
        SceneSwitcher.getInstance().setBugginessInputScene(event);
    }

    @FXML
    private void cloneRepositorySelected(ActionEvent event) throws IOException {
        SceneSwitcher.getInstance().setCloneRepositoryScene(event);
    }

    @FXML
    private void processControlChartSelected(ActionEvent event) {
        // TODO: 27/05/21
    }

    @FXML
    private void initialize() {
        assert cloneRepoButton != null : "fx:id=\"cloneRepoButton\" was not injected: check your FXML file 'home.fxml'.";
        assert processControlChartButton != null : "fx:id=\"processControlChartButton\" was not injected: check your FXML file 'home.fxml'.";
        assert classesBugginess != null : "fx:id=\"classesBugginess\" was not injected: check your FXML file 'home.fxml'.";
    }

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        URL url = getClass().getResource("/view/home.fxml");
        loader.setLocation(url);

        Parent root = loader.load();
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }
}
