package reto1_multiusuario.Client;

import java.io.IOException;
import javafx.fxml.FXML;
import reto1_multiusuario.main;

public class PrimaryController {

    @FXML
    private void switchToSecondary() throws IOException {
        main.setRoot("reto1_multiusuario\\src\\main\\resources\\view\\secondary.fxml");
    }
}
