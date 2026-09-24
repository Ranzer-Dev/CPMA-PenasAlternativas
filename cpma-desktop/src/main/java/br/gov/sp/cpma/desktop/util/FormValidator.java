package br.gov.sp.cpma.desktop.util;

import br.gov.sp.cpma.desktop.client.FieldErrorDetail;
import br.gov.sp.cpma.desktop.client.StandardApiError;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;

import java.util.HashMap;
import java.util.Map;

public class FormValidator {

    private final Map<String, TextInputControl> fieldMap = new HashMap<>();
    private final Map<String, Label> labelMap = new HashMap<>();

    public void registerField(String fieldName, TextInputControl control, Label errorLabel) {
        fieldMap.put(fieldName, control);
        if (errorLabel != null) {
            labelMap.put(fieldName, errorLabel);
            errorLabel.setText("");
            errorLabel.getStyleClass().add("field-error-label");
        }

        control.textProperty().addListener((obs, oldVal, newVal) -> {
            clearFieldError(fieldName);
        });
    }

    public void applyErrors(StandardApiError apiError) {
        clearAllErrors();
        if (apiError == null || apiError.getErrors() == null) return;

        TextInputControl firstInvalidControl = null;

        for (FieldErrorDetail detail : apiError.getErrors()) {
            String field = detail.getField();
            String message = detail.getMessage();

            TextInputControl control = fieldMap.get(field);
            if (control != null) {
                if (!control.getStyleClass().contains("field-error")) {
                    control.getStyleClass().add("field-error");
                }
                control.setTooltip(new Tooltip(message));

                if (firstInvalidControl == null) {
                    firstInvalidControl = control;
                }
            }

            Label label = labelMap.get(field);
            if (label != null) {
                label.setText(message);
                label.setVisible(true);
            }
        }

        if (firstInvalidControl != null) {
            final TextInputControl focusTarget = firstInvalidControl;
            Platform.runLater(focusTarget::requestFocus);
        }
    }

    public void clearFieldError(String fieldName) {
        TextInputControl control = fieldMap.get(fieldName);
        if (control != null) {
            control.getStyleClass().remove("field-error");
            control.setTooltip(null);
        }

        Label label = labelMap.get(fieldName);
        if (label != null) {
            label.setText("");
            label.setVisible(false);
        }
    }

    public void clearAllErrors() {
        for (String field : fieldMap.keySet()) {
            clearFieldError(field);
        }
    }

    public static void limparAoDigitar(TextInputControl control, Label errorLabel) {
        if (control == null) return;
        control.textProperty().addListener((obs, oldVal, newVal) -> {
            control.getStyleClass().remove("field-error");
            if (errorLabel != null) {
                errorLabel.setText("");
                errorLabel.setVisible(false);
            }
        });
    }

    public static boolean validarCampoObrigatorio(TextInputControl control, Label errorLabel, String message) {
        if (control == null || control.getText() == null || control.getText().trim().isEmpty()) {
            marcarErro(control, errorLabel, message);
            return false;
        }
        if (control.getStyleClass().contains("field-error")) {
            control.getStyleClass().remove("field-error");
        }
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
        }
        return true;
    }

    public static void marcarErro(TextInputControl control, Label errorLabel, String message) {
        if (control != null && !control.getStyleClass().contains("field-error")) {
            control.getStyleClass().add("field-error");
        }
        if (errorLabel != null) {
            errorLabel.setText(message != null ? message : "");
            errorLabel.getStyleClass().setAll("field-error-label");
            errorLabel.setVisible(message != null && !message.isEmpty());
        }
    }
}
