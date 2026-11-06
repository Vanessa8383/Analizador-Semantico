module application.analizador {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;


    opens application.analizador to javafx.fxml;
    exports application.analizador;
}