package com.mycompany.cpma;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CPMA extends Application {

    @Override
    public void start(Stage stage) {
        try {
            // CORREÇÃO: Utiliza um caminho absoluto para o FXML, começando com "/"
            // para indicar a raiz da pasta 'resources'.
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
