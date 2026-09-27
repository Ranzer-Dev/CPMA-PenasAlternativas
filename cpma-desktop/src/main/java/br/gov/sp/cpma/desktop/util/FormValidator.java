package br.gov.sp.cpma.desktop.util;

import br.gov.sp.cpma.desktop.client.FieldErrorDetail;
import br.gov.sp.cpma.desktop.client.StandardApiError;
import javafx.application.Platform;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FormValidator {

    private static final Pattern CPF_PATTERN = Pattern.compile("^\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}$");
    private final Map<String, Control> fieldMap = new HashMap<>();
    private final Map<String, Label> labelMap = new HashMap<>();

    public void registerField(String fieldName, Control control, Label errorLabel) {
        fieldMap.put(fieldName, control);
        if (errorLabel != null) {
            labelMap.put(fieldName, errorLabel);
            errorLabel.setText("");
            errorLabel.getStyleClass().add("field-error-label");
            errorLabel.setVisible(false);
        }

        vincularLimpezaAoInteragir(control, errorLabel);
    }

    public void applyErrors(StandardApiError apiError) {
        clearAllErrors();
        if (apiError == null || apiError.getErrors() == null) {
            return;
        }

        Control firstInvalidControl = null;

        for (FieldErrorDetail detail : apiError.getErrors()) {
            String field = detail.getField();
            String message = detail.getMessage();

            Control control = fieldMap.get(field);
            if (control != null) {
                marcarErro(control, labelMap.get(field), message);
                if (firstInvalidControl == null) {
                    firstInvalidControl = control;
                }
            } else {
                Label label = labelMap.get(field);
                if (label != null) {
                    label.setText(message);
                    label.setVisible(true);
                }
            }
        }

        if (firstInvalidControl != null) {
            final Control focusTarget = firstInvalidControl;
            Platform.runLater(focusTarget::requestFocus);
        }
    }

    public void clearFieldError(String fieldName) {
        Control control = fieldMap.get(fieldName);
        Label label = labelMap.get(fieldName);
        limparErro(control, label);
    }

    public void clearAllErrors() {
        for (String field : fieldMap.keySet()) {
            clearFieldError(field);
        }
    }

    public static void vincularLimpezaAoInteragir(Control control, Label errorLabel) {
        if (control == null) {
            return;
        }
        if (control instanceof TextInputControl textInput) {
            textInput.textProperty().addListener((obs, oldVal, newVal) -> limparErro(control, errorLabel));
        } else if (control instanceof ComboBoxBase<?> combo) {
            combo.valueProperty().addListener((obs, oldVal, newVal) -> limparErro(control, errorLabel));
        }
    }

    public static void limparAoDigitar(Control control, Label errorLabel) {
        vincularLimpezaAoInteragir(control, errorLabel);
    }

    public static boolean validarCampoObrigatorio(Control control, Label errorLabel, String message) {
        if (control == null) {
            return false;
        }

        if (control instanceof TextInputControl textInput) {
            String text = textInput.getText();
            if (text == null || text.trim().isEmpty()) {
                marcarErro(control, errorLabel, message);
                return false;
            }
        } else if (control instanceof ComboBoxBase<?> combo) {
            if (combo.getValue() == null) {
                marcarErro(control, errorLabel, message);
                return false;
            }
        }

        limparErro(control, errorLabel);
        return true;
    }

    public static boolean validarCpf(TextInputControl control, Label errorLabel, String message) {
        if (control == null) {
            return false;
        }
        String text = control.getText();
        if (text == null || text.trim().isEmpty()) {
            marcarErro(control, errorLabel, "CPF e obrigatorio");
            return false;
        }

        String digitsOnly = text.replaceAll("\\D", "");
        if (digitsOnly.length() != 11 || digitsOnly.chars().distinct().count() == 1) {
            marcarErro(control, errorLabel, message != null ? message : "CPF invalido (deve conter 11 digitos validos)");
            return false;
        }

        limparErro(control, errorLabel);
        return true;
    }

    public static boolean validarNumeroInteiroPositivo(TextInputControl control, Label errorLabel, String message) {
        if (control == null) {
            return false;
        }
        String text = control.getText();
        if (text == null || text.trim().isEmpty()) {
            marcarErro(control, errorLabel, message);
            return false;
        }
        try {
            int valor = Integer.parseInt(text.trim());
            if (valor <= 0) {
                marcarErro(control, errorLabel, "O valor deve ser maior que zero");
                return false;
            }
        } catch (NumberFormatException e) {
            marcarErro(control, errorLabel, "Informe um numero inteiro valido");
            return false;
        }

        limparErro(control, errorLabel);
        return true;
    }

    public static void marcarErro(Control control, Label errorLabel, String message) {
        if (control != null) {
            if (!control.getStyleClass().contains("field-error")) {
                control.getStyleClass().add("field-error");
            }
            if (message != null && !message.isEmpty()) {
                control.setTooltip(new Tooltip(message));
            }
        }
        if (errorLabel != null) {
            errorLabel.setText(message != null ? message : "");
            if (!errorLabel.getStyleClass().contains("field-error-label")) {
                errorLabel.getStyleClass().add("field-error-label");
            }
            errorLabel.setVisible(message != null && !message.isEmpty());
        }
    }

    public static void limparErro(Control control, Label errorLabel) {
        if (control != null) {
            control.getStyleClass().remove("field-error");
            control.setTooltip(null);
        }
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
        }
    }

    public static String formatarMensagemErro(StandardApiError apiError, int statusCode) {
        if (apiError == null) {
            return "Erro inesperado de comunicacao com o servidor (Status HTTP: " + statusCode + ").";
        }

        String mensagemBase = apiError.getMessage() != null && !apiError.getMessage().isBlank()
                ? apiError.getMessage()
                : "Falha na operacao";

        if (apiError.getErrors() != null && !apiError.getErrors().isEmpty()) {
            String detalhesCampos = apiError.getErrors().stream()
                    .map(err -> err.getField() + ": " + err.getMessage())
                    .collect(Collectors.joining(" | "));
            return mensagemBase + " [" + detalhesCampos + "]";
        }

        return switch (statusCode) {
            case 400 -> "Requisicao Invalida (400): " + mensagemBase;
            case 404 -> "Registro Nao Encontrado (404): " + mensagemBase;
            case 409 -> "Conflito de Dados (409): " + mensagemBase;
            case 422 -> "Validacao Recusada (422): " + mensagemBase;
            case 500 -> "Erro Interno no Servidor (500): " + mensagemBase;
            case 503 -> "Servico Indisponivel (503): " + mensagemBase;
            default -> mensagemBase + " (Codigo HTTP: " + statusCode + ")";
        };
    }
}
