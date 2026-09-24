package br.gov.sp.cpma.desktop.controller;

import br.gov.sp.cpma.desktop.client.ApiResponse;
import br.gov.sp.cpma.desktop.client.CpmaApiClient;
import br.gov.sp.cpma.desktop.client.InstituicaoDTO;
import br.gov.sp.cpma.desktop.client.UsuarioDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.List;

public class DashboardContentController {

    @FXML
    private Label lblTotalApenados;

    @FXML
    private Label lblTotalInstituicoes;

    @FXML
    private TextField txtBuscaRapida;

    private final CpmaApiClient apiClient = new CpmaApiClient();

    @FXML
    public void initialize() {
        carregarContadores();
    }

    private void carregarContadores() {
        new Thread(() -> {
            ApiResponse<List<UsuarioDTO>> respUsers = apiClient.listarUsuarios();
            ApiResponse<List<InstituicaoDTO>> respInst = apiClient.listarInstituicoes();

            Platform.runLater(() -> {
                if (respUsers.isSuccess() && respUsers.getData() != null) {
                    lblTotalApenados.setText(String.valueOf(respUsers.getData().size()));
                } else {
                    lblTotalApenados.setText("0");
                }

                if (respInst.isSuccess() && respInst.getData() != null) {
                    lblTotalInstituicoes.setText(String.valueOf(respInst.getData().size()));
                } else {
                    lblTotalInstituicoes.setText("0");
                }
            });
        }).start();
    }

    @FXML
    public void handleNovoApenado() {
        if (MainShellController.getInstance() != null) {
            MainShellController.getInstance().navegarParaApenados();
        }
    }

    @FXML
    public void handleNovaInstituicao() {
        if (MainShellController.getInstance() != null) {
            MainShellController.getInstance().navegarParaInstituicoes();
        }
    }

    @FXML
    public void handleNovaPena() {
        if (MainShellController.getInstance() != null) {
            MainShellController.getInstance().navegarParaPenas();
        }
    }

    @FXML
    public void handleRegistrarFrequencia() {
        if (MainShellController.getInstance() != null) {
            MainShellController.getInstance().navegarParaFrequencia();
        }
    }

    @FXML
    public void handleAbrirTotem() {
        if (MainShellController.getInstance() != null) {
            MainShellController.getInstance().navegarParaTotem();
        }
    }

    @FXML
    public void handleBuscaRapida() {
        if (MainShellController.getInstance() != null) {
            MainShellController.getInstance().navegarParaApenados();
        }
    }
}
