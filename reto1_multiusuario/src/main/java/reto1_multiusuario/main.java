package reto1_multiusuario;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import reto1_multiusuario.Client.Controller.PantallaPrincipal;

public class main extends Application {

    private PantallaPrincipal pantalla;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(main.class.getResource("/view/msgView.fxml"));
        Scene scene = new Scene(loader.load());
        pantalla = loader.getController();

        stage.setTitle("Chat multiusuario");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        pantalla.desconectar();
    }

    public static void main(String[] args) {
        launch();
    }
}
