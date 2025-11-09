module com.mycompany.cpma {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires java.sql;

    requires org.bytedeco.javacv;
    requires org.bytedeco.opencv;

    requires java.desktop;
    requires org.json;
    requires org.apache.pdfbox;

    opens com.mycompany.cpma to javafx.fxml;
    opens controller to javafx.fxml;
    opens model to javafx.base, javafx.fxml;
    opens dao to javafx.fxml;
    
    exports com.mycompany.cpma;
    exports controller;
    exports model;
    exports dao;
}
