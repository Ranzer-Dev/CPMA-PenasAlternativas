package com.mycompany.cpma;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class CPMA extends Application {

    @Override
    public void start(Stage stage) {
        try {
            Font.loadFont(getClass().getResourceAsStream("/fonts/Nunito-Regular.ttf"), 10);
            Font.loadFont(getClass().getResourceAsStream("/fonts/Nunito-Bold.ttf"), 10);
            Font.loadFont(getClass().getResourceAsStream("/fonts/Nunito-Italic.ttf"), 10);
            Font.loadFont(getClass().getResourceAsStream("/fonts/Nunito-BoldItalic.ttf"), 10);
            FXMLLoader fxmlLoader = new FXMLLoader(CPMA.class.getResource("/com/mycompany/cpma/login.fxml"));

            Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            stage.setTitle("Login - CPMA");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("### ERRO CRÍTICO AO CARREGAR A VIEW DE LOGIN ###");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
