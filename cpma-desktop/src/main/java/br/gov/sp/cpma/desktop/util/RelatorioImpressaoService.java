package br.gov.sp.cpma.desktop.util;

import br.gov.sp.cpma.desktop.client.*;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Window;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RelatorioImpressaoService {

    public static void imprimirFichaCompleta(Window owner, CpmaApiClient apiClient, UsuarioDTO usuario, Long penaId) {
        new Thread(() -> {
            UsuarioDTO u = usuario;
            PenaDTO p = null;
            ResumoCumprimentoDTO resumo = null;
            List<RegistroTrabalhoDTO> registros = null;

            ApiResponse<List<PenaDTO>> respPenas = apiClient.listarPenasPorUsuario(u.getIdUsuario());
            if (respPenas.isSuccess() && respPenas.getData() != null && !respPenas.getData().isEmpty()) {
                if (penaId != null) {
                    for (PenaDTO item : respPenas.getData()) {
                        if (item.getIdPena().equals(penaId)) {
                            p = item;
                            break;
                        }
                    }
                }
                if (p == null) {
                    p = respPenas.getData().get(0);
                }
            }

            if (p != null) {
                ApiResponse<ResumoCumprimentoDTO> respResumo = apiClient.obterResumoCumprimento(p.getIdPena());
                if (respResumo.isSuccess()) {
                    resumo = respResumo.getData();
                }

                ApiResponse<List<RegistroTrabalhoDTO>> respRegs = apiClient.listarRegistrosPorPena(p.getIdPena());
                if (respRegs.isSuccess()) {
                    registros = respRegs.getData();
                }
            }

            final UsuarioDTO finalUser = u;
            final PenaDTO finalPena = p;
            final ResumoCumprimentoDTO finalResumo = resumo;
            final List<RegistroTrabalhoDTO> finalRegistros = registros;

            Platform.runLater(() -> {
                Node documento = criarDocumento(finalUser, finalPena, finalResumo, finalRegistros);
                imprimir(owner, documento);
            });
        }).start();
    }

    public static Node criarDocumento(UsuarioDTO usuario, PenaDTO pena, ResumoCumprimentoDTO resumo, List<RegistroTrabalhoDTO> registros) {
        VBox doc = new VBox(12);
        doc.setPrefWidth(540);
        doc.setMaxWidth(540);
        doc.setStyle("-fx-background-color: #ffffff; -fx-padding: 20px;");

        HBox cabecalho = new HBox(12);
        cabecalho.setAlignment(Pos.CENTER_LEFT);

        try {
            Image imgLogo = new Image(RelatorioImpressaoService.class.getResourceAsStream("/br/gov/sp/cpma/desktop/images/Governo-de-SP-Estado.png"));
            ImageView imgView = new ImageView(imgLogo);
            imgView.setFitHeight(45);
            imgView.setPreserveRatio(true);
            cabecalho.getChildren().add(imgView);
        } catch (Exception ignored) {}

        VBox titulos = new VBox(2);
        titulos.setAlignment(Pos.CENTER_LEFT);

        Text t1 = new Text("GOVERNO DO ESTADO DE SÃO PAULO");
        t1.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        t1.setFill(Color.web("#1e293b"));

        Text t2 = new Text("SECRETARIA DA ADMINISTRAÇÃO PENITENCIÁRIA - CPMA");
        t2.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        t2.setFill(Color.web("#475569"));

        Text t3 = new Text("COMPROVANTE DE COMPARECIMENTO E REGISTROS DE TRABALHO");
        t3.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        t3.setFill(Color.web("#2563eb"));

        titulos.getChildren().addAll(t1, t2, t3);
        cabecalho.getChildren().add(titulos);
        doc.getChildren().add(cabecalho);

        Line sep1 = new Line(0, 0, 500, 0);
        sep1.setStroke(Color.web("#cbd5e1"));
        sep1.setStrokeWidth(1.2);
        doc.getChildren().add(sep1);

        VBox secUsuario = new VBox(4);
        Text titUser = new Text("1. DADOS DO APENADO");
        titUser.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        titUser.setFill(Color.web("#0f172a"));
        secUsuario.getChildren().add(titUser);

        GridPane gridUser = new GridPane();
        gridUser.setHgap(16);
        gridUser.setVgap(4);
        adicionarCampoGrid(gridUser, 0, 0, "Nome:", usuario != null && usuario.getNome() != null ? usuario.getNome() : "-");
        adicionarCampoGrid(gridUser, 1, 0, "Matrícula/Código:", usuario != null && usuario.getCodigo() != null ? usuario.getCodigo() : "-");
        adicionarCampoGrid(gridUser, 0, 1, "CPF:", usuario != null && usuario.getCpf() != null ? usuario.getCpf() : "-");
        adicionarCampoGrid(gridUser, 1, 1, "Telefone:", usuario != null && usuario.getTelefone() != null ? usuario.getTelefone() : "-");
        adicionarCampoGrid(gridUser, 0, 2, "Endereço:", usuario != null && usuario.getEndereco() != null ? usuario.getEndereco() + (usuario.getBairro() != null ? " - " + usuario.getBairro() : "") : "-");
        secUsuario.getChildren().add(gridUser);
        doc.getChildren().add(secUsuario);

        VBox secPena = new VBox(4);
        Text titPena = new Text("2. MEDIDA ALTERNATIVA E INSTITUIÇÃO PARCEIRA");
        titPena.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        titPena.setFill(Color.web("#0f172a"));
        secPena.getChildren().add(titPena);

        GridPane gridPena = new GridPane();
        gridPena.setHgap(16);
        gridPena.setVgap(4);
        adicionarCampoGrid(gridPena, 0, 0, "Tipo de Pena:", pena != null && pena.getTipoPena() != null ? pena.getTipoPena() : "Prestação de Serviços à Comunidade (PSC)");
        adicionarCampoGrid(gridPena, 1, 0, "Instituição:", pena != null && pena.getInstituicaoPrincipalNome() != null ? pena.getInstituicaoPrincipalNome() : "-");
        adicionarCampoGrid(gridPena, 0, 1, "Total de Horas:", pena != null ? pena.getHorasTotais() + " horas" : "-");
        adicionarCampoGrid(gridPena, 1, 1, "Data de Início:", pena != null && pena.getDataInicio() != null ? pena.getDataInicio().toString() : "-");
        secPena.getChildren().add(gridPena);
        doc.getChildren().add(secPena);

        VBox secResumo = new VBox(4);
        Text titResumo = new Text("3. RESUMO DE CUMPRIMENTO DAS HORAS");
        titResumo.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        titResumo.setFill(Color.web("#0f172a"));
        secResumo.getChildren().add(titResumo);

        HBox cardTotais = new HBox(12);
        cardTotais.setStyle("-fx-background-color: #f8fafc; -fx-padding: 8px 12px; -fx-border-color: #e2e8f0; -fx-border-radius: 4px;");

        double cumpridas = resumo != null && resumo.getHorasCumpridas() != null ? resumo.getHorasCumpridas() : 0.0;
        double totais = resumo != null && resumo.getHorasTotais() != null ? resumo.getHorasTotais() : (pena != null ? pena.getHorasTotais() : 0.0);
        double restantes = resumo != null && resumo.getHorasRestantes() != null ? resumo.getHorasRestantes() : Math.max(0.0, totais - cumpridas);
        double perc = totais > 0 ? (cumpridas / totais) * 100.0 : 0.0;

        cardTotais.getChildren().addAll(
                criarItemResumo("Total Previsto", String.format("%.0fh", totais), "#475569"),
                criarItemResumo("Horas Cumpridas", String.format("%.1fh", cumpridas), "#059669"),
                criarItemResumo("Horas Restantes", String.format("%.1fh", restantes), "#dc2626"),
                criarItemResumo("Conclusão", String.format("%.1f%%", perc), "#2563eb")
        );
        secResumo.getChildren().add(cardTotais);
        doc.getChildren().add(secResumo);

        VBox secRegistros = new VBox(4);
        Text titRegs = new Text("4. REGISTROS DE COMPARECIMENTO E TRABALHO");
        titRegs.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        titRegs.setFill(Color.web("#0f172a"));
        secRegistros.getChildren().add(titRegs);

        GridPane gridRegs = new GridPane();
        gridRegs.setHgap(8);
        gridRegs.setVgap(3);
        gridRegs.setStyle("-fx-padding: 4px 0px;");

        Text h1 = new Text("Data");
        h1.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        Text h2 = new Text("Entrada");
        h2.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        Text h3 = new Text("Saída");
        h3.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        Text h4 = new Text("Horas");
        h4.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        Text h5 = new Text("Atividades");
        h5.setFont(Font.font("Arial", FontWeight.BOLD, 10));

        gridRegs.add(h1, 0, 0);
        gridRegs.add(h2, 1, 0);
        gridRegs.add(h3, 2, 0);
        gridRegs.add(h4, 3, 0);
        gridRegs.add(h5, 4, 0);

        int linha = 1;
        if (registros != null && !registros.isEmpty()) {
            for (RegistroTrabalhoDTO r : registros) {
                if (linha > 12) break;
                Text tData = new Text(r.getDataTrabalho() != null ? r.getDataTrabalho().toString() : "-");
                tData.setFont(Font.font("Arial", 9));
                Text tEnt = new Text(r.getHorarioInicio() != null ? r.getHorarioInicio() : "-");
                tEnt.setFont(Font.font("Arial", 9));
                Text tSai = new Text(r.getHorarioSaida() != null ? r.getHorarioSaida() : "-");
                tSai.setFont(Font.font("Arial", 9));
                Text tH = new Text(r.getHorasCumpridas() != null ? String.format("%.1fh", r.getHorasCumpridas()) : "-");
                tH.setFont(Font.font("Arial", FontWeight.BOLD, 9));
                Text tAtiv = new Text(r.getAtividades() != null ? r.getAtividades() : "-");
                tAtiv.setFont(Font.font("Arial", 9));
                tAtiv.setWrappingWidth(180);

                gridRegs.add(tData, 0, linha);
                gridRegs.add(tEnt, 1, linha);
                gridRegs.add(tSai, 2, linha);
                gridRegs.add(tH, 3, linha);
                gridRegs.add(tAtiv, 4, linha);
                linha++;
            }
        } else {
            Text tVazio = new Text("Nenhum registro de trabalho lançado para esta pena.");
            tVazio.setFont(Font.font("Arial", 9));
            tVazio.setFill(Color.web("#94a3b8"));
            gridRegs.add(tVazio, 0, 1, 5, 1);
        }

        secRegistros.getChildren().add(gridRegs);
        doc.getChildren().add(secRegistros);

        Line sep2 = new Line(0, 0, 500, 0);
        sep2.setStroke(Color.web("#cbd5e1"));
        sep2.setStrokeWidth(1);
        doc.getChildren().add(sep2);

        HBox assinaturas = new HBox(60);
        assinaturas.setAlignment(Pos.CENTER);
        assinaturas.setStyle("-fx-padding: 24px 0px 8px 0px;");

        VBox ass1 = new VBox(4);
        ass1.setAlignment(Pos.CENTER);
        Line lAss1 = new Line(0, 0, 180, 0);
        lAss1.setStroke(Color.web("#64748b"));
        Text tAss1 = new Text("Assinatura do Apenado");
        tAss1.setFont(Font.font("Arial", 9));
        tAss1.setFill(Color.web("#64748b"));
        ass1.getChildren().addAll(lAss1, tAss1);

        VBox ass2 = new VBox(4);
        ass2.setAlignment(Pos.CENTER);
        Line lAss2 = new Line(0, 0, 180, 0);
        lAss2.setStroke(Color.web("#64748b"));
        Text tAss2 = new Text("Responsável Técnico CPMA");
        tAss2.setFont(Font.font("Arial", 9));
        tAss2.setFill(Color.web("#64748b"));
        ass2.getChildren().addAll(lAss2, tAss2);

        assinaturas.getChildren().addAll(ass1, ass2);
        doc.getChildren().add(assinaturas);

        HBox rodape = new HBox();
        rodape.setAlignment(Pos.CENTER);
        String dataHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        Text txtRodape = new Text("Documento emitido eletronicamente em " + dataHora + " via CPMA Desktop");
        txtRodape.setFont(Font.font("Arial", 8));
        txtRodape.setFill(Color.web("#94a3b8"));
        rodape.getChildren().add(txtRodape);
        doc.getChildren().add(rodape);

        return doc;
    }

    private static void adicionarCampoGrid(GridPane grid, int col, int row, String label, String valor) {
        HBox box = new HBox(4);
        Text l = new Text(label);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        l.setFill(Color.web("#475569"));
        Text v = new Text(valor);
        v.setFont(Font.font("Arial", 10));
        v.setFill(Color.web("#1e293b"));
        box.getChildren().addAll(l, v);
        grid.add(box, col, row);
    }

    private static VBox criarItemResumo(String titulo, String valor, String corHex) {
        VBox v = new VBox(2);
        v.setAlignment(Pos.CENTER);
        HBox.setHgrow(v, Priority.ALWAYS);
        Text t = new Text(titulo);
        t.setFont(Font.font("Arial", 9));
        t.setFill(Color.web("#64748b"));
        Text val = new Text(valor);
        val.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        val.setFill(Color.web(corHex));
        v.getChildren().addAll(t, val);
        return v;
    }

    public static boolean imprimir(Window owner, Node documento) {
        try {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Impressora Não Disponível", "Nenhuma impressora ou serviço de impressão PDF detectado no sistema.");
                return false;
            }

            boolean proceder = job.showPrintDialog(owner);
            if (!proceder) {
                return false;
            }

            Printer printer = job.getPrinter();
            PageLayout pageLayout = printer.createPageLayout(
                    Paper.A4,
                    PageOrientation.PORTRAIT,
                    Printer.MarginType.DEFAULT
            );

            double printableWidth = pageLayout.getPrintableWidth();
            double nodeWidth = 540;
            double scale = printableWidth / nodeWidth;
            if (scale < 1.0) {
                documento.getTransforms().add(new javafx.scene.transform.Scale(scale, scale));
            }

            boolean sucesso = job.printPage(pageLayout, documento);
            if (sucesso) {
                job.endJob();
                mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Documento enviado para a impressora com sucesso!");
                return true;
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao enviar documento para a impressora.");
                return false;
            }
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Falha na Impressão", "Erro ao executar a impressão: " + e.getMessage());
            return false;
        }
    }

    private static void mostrarAlerta(Alert.AlertType tipo, String titulo, String msg) {
        Alert a = new Alert(tipo);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
