module com.mycompany.cpma {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires java.sql;

    requires org.bytedeco.javacv;
    requires org.bytedeco.opencv;

    requires java.desktop;
    requires org.json;

    opens com.mycompany.cpma to javafx.fxml;
    opens controller to javafx.fxml;
    opens model to javafx.base;
    exports com.mycompany.cpma;
}
