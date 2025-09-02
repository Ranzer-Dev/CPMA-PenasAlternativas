module com.mycompany.cpma {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.graphics;
    requires java.desktop;

    opens view to javafx.graphics, javafx.fxml;
    opens controller to javafx.fxml;
}
