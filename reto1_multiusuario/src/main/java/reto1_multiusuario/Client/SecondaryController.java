package reto1_multiusuario.Client;

import java.io.IOException;
import javafx.fxml.FXML;
import reto1_multiusuario.main;

public class SecondaryController {

    @FXML
    private void switchToPrimary() throws IOException {
        main.setRoot("primary");
    }
}