package com.mycompany.cpma;

import javafx.application.Application;
import javafx.stage.Stage;
import view.LoginView;

public class CPMA extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        new LoginView().start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}