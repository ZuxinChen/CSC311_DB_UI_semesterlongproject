module com.example.csc311_db_ui_semesterlongproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires commons.csv;
    requires com.azure.storage.blob;
    requires org.slf4j;
    requires java.desktop;
    requires java.prefs;
    requires org.apache.pdfbox;


    opens viewmodel;
    exports viewmodel;
    opens dao;
    exports dao;
    opens model;
    exports model;
}