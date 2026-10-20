module reto1_multiusuario {
    requires javafx.controls;
    requires javafx.fxml;

    opens reto1_multiusuario to javafx.fxml;
    exports reto1_multiusuario;
}
