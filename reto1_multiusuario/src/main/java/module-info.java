module reto1_multiusuario {
    requires javafx.controls;
    requires javafx.fxml;

    opens reto1_multiusuario to javafx.graphics, javafx.fxml;
    exports reto1_multiusuario;

    opens reto1_multiusuario.Client to javafx.graphics, javafx.fxml;
    exports reto1_multiusuario.Client;
}
