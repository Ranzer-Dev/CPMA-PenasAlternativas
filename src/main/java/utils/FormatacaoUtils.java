package utils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.util.StringConverter;
import java.util.function.UnaryOperator;

/**
 * Classe utilitária para formatação de campos de texto
 */
public class FormatacaoUtils {
    

/**
     * Aplica formatação de hora HH:mm usando TextFormatter com StringConverter.
     * Implementação robusta baseada em LocalTime e DateTimeFormatter.
     */
    public static void aplicarFormatacaoHora(TextField campo) {
        campo.setTooltip(new Tooltip("Digite a hora no formato HH:mm (ex: 08:30 ou 1415)"));
        
        // Define o formatador de tempo
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        // StringConverter para LocalTime
        StringConverter<LocalTime> timeConverter = new StringConverter<LocalTime>() {
            @Override
            public String toString(LocalTime time) {
                return (time == null) ? "" : timeFormatter.format(time);
            }

            @Override
            public LocalTime fromString(String string) {
                if (string == null || string.trim().isEmpty()) {
                    return null;
                }
                
                try {
                    // Tenta formatar entradas como "800" para "08:00"
                    String apenasDigitos = string.replaceAll("\\D", "");
                    String textoFormatado = string;
                    
                    if (apenasDigitos.length() == 3) {
                        textoFormatado = "0" + apenasDigitos.charAt(0) + ":" + apenasDigitos.substring(1);
                    } else if (apenasDigitos.length() == 2) {
                        textoFormatado = apenasDigitos + ":00";
                    } else if (apenasDigitos.length() == 4) {
                        textoFormatado = apenasDigitos.substring(0, 2) + ":" + apenasDigitos.substring(2);
                    }
                    
                    return LocalTime.parse(textoFormatado, timeFormatter);
                } catch (DateTimeParseException e) {
                    return null; // Handle invalid input gracefully
                }
            }
        };
        
        // Filtro para validação de entrada
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            
            // Rejeita se exceder o comprimento máximo
            if (newText.length() > 5) { // HH:mm = 5 caracteres
                return null;
            }
            
            // Permite apenas dígitos e ":"
            if (!newText.matches("[0-9:]*")) {
                return null;
            }
            
            return change;
        };
        
        // Aplica o TextFormatter
        TextFormatter<LocalTime> timeFormatterInput = new TextFormatter<>(timeConverter, null, filter);
        campo.setTextFormatter(timeFormatterInput);
        
        // Validação visual
        campo.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && !campo.getText().isEmpty()) { // Ao perder foco
                LocalTime time = timeFormatterInput.getValue();
                if (time != null) {
                    campo.setStyle("-fx-border-color: #10b981;"); // Verde para sucesso
                    campo.setTooltip(new Tooltip("Hora válida: " + timeFormatter.format(time)));
                } else {
                    campo.setStyle("-fx-border-color: #dc2626; -fx-border-width: 1.5px;"); // Vermelho para erro
                    campo.setTooltip(new Tooltip("Hora inválida! Use o formato HH:mm"));
                }
            } else if (newValue) {
                campo.setStyle(""); // Limpa o estilo quando ganha foco
            }
        });
    }

    /**
     * Aplica formatação de CEP 00000-000 em um campo de texto
     */
    public static void aplicarFormatacaoCEP(TextField campo) {
        campo.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                String formatted = formatarCEP(newValue);
                if (!formatted.equals(newValue)) {
                    campo.setText(formatted);
                    campo.positionCaret(formatted.length());
                }
            }
        });
    }
    
    /**
     * Aplica formatação de telefone (XX) XXXXX-XXXX em um campo de texto
     */
    public static void aplicarFormatacaoTelefone(TextField campo) {
        campo.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                String formatted = formatarTelefone(newValue);
                if (!formatted.equals(newValue)) {
                    campo.setText(formatted);
                    campo.positionCaret(formatted.length());
                }
            }
        });
    }
    
    /**
     * Formata texto como hora HH:mm, interpretando entradas como "800" para "08:00".
     */
    private static String formatarHora(String texto) {
        if (texto == null) {
            return "";
        }
        
        String digits = texto.replaceAll("\\D", "");
        
        if (digits.length() > 4) {
            digits = digits.substring(0, 4);
        }
        
        if (digits.length() <= 2) {
            return digits;
        } else if (digits.length() == 3) {
            return "0" + digits.substring(0, 1) + ":" + digits.substring(1, 3);
        } else {
            return digits.substring(0, 2) + ":" + digits.substring(2, 4);
        }
    }
    
    /**
     * Formata texto como CEP 00000-000
     */
    private static String formatarCEP(String texto) {
        if (texto == null || texto.isEmpty()) {
            return "";
        }
        
        // Remove caracteres não numéricos
        String digits = texto.replaceAll("\\D", "");
        
        // Limita a 8 dígitos
        if (digits.length() > 8) {
            digits = digits.substring(0, 8);
        }
        
        // Formata como 00000-000
        if (digits.length() >= 5) {
            return digits.substring(0, 5) + "-" + digits.substring(5);
        }
        
        return digits;
    }
    
    /**
     * Formata texto como telefone (XX) XXXXX-XXXX
     */
    private static String formatarTelefone(String texto) {
        if (texto == null || texto.isEmpty()) {
            return "";
        }
        
        // Remove caracteres não numéricos
        String digits = texto.replaceAll("\\D", "");
        
        // Limita a 11 dígitos
        if (digits.length() > 11) {
            digits = digits.substring(0, 11);
        }
        
        // Formata como (XX) XXXXX-XXXX
        if (digits.length() >= 7) {
            return "(" + digits.substring(0, 2) + ") " + 
                   digits.substring(2, 7) + "-" + digits.substring(7);
        } else if (digits.length() >= 2) {
            return "(" + digits.substring(0, 2) + ") " + digits.substring(2);
        } else if (digits.length() >= 1) {
            return "(" + digits;
        }
        
        return digits;
    }
    
    /**
     * Valida se a string representa uma hora válida no formato HH:mm
     */
    private static boolean ehHoraValida(String hora) {
        if (hora == null || hora.trim().isEmpty()) {
            return false;
        }
        
        try {
            LocalTime.parse(hora, DateTimeFormatter.ofPattern("HH:mm"));
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    
    /**
     * Valida e converte string para LocalTime
     */
    public static LocalTime parseHora(String hora) {
        if (hora == null || hora.trim().isEmpty()) {
            return null;
        }
        
        try {
            return LocalTime.parse(hora, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    /**
     * Obtém o valor LocalTime de um TextField que usa TextFormatter
     */
    public static LocalTime getHoraValue(TextField campo) {
        if (campo.getTextFormatter() != null && campo.getTextFormatter().getValue() instanceof LocalTime) {
            return (LocalTime) campo.getTextFormatter().getValue();
        }
        return parseHora(campo.getText());
    }
}
