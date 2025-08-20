package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LoginView extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/view/login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);

        scene.getStylesheets().add(getClass().getResource("/resources/view/style.css").toExternalForm());

        primaryStage.setTitle("Menu Principal");
        primaryStage.setMaximized(true);
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
