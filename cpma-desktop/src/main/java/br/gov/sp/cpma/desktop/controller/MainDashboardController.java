package br.gov.sp.cpma.desktop.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Tab;

import java.net.URL;
import java.util.ResourceBundle;

public class MainDashboardController implements Initializable {

    @FXML private Tab tabCadastro;
    @FXML private Tab tabTotem;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Parent cadastroView = FXMLLoader.load(getClass().getResource("/br/gov/sp/cpma/desktop/view/cadastroApenadoView.fxml"));
            tabCadastro.setContent(cadastroView);

            Parent totemView = FXMLLoader.load(getClass().getResource("/br/gov/sp/cpma/desktop/view/totemKioskView.fxml"));
            tabTotem.setContent(totemView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
