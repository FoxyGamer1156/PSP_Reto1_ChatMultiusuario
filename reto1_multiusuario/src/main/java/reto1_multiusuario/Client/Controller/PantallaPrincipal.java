package reto1_multiusuario.Client.Controller;

import java.io.IOException;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.Window;


public class PantallaPrincipal {

    private static final String TITULO = "Chat multiusuario";
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML private TextField txtIp;
    @FXML private TextField txtPuerto;
    @FXML private TextField txtUsuario;
    @FXML private TextField txtDestinatario;
    @FXML private TextField txtMensaje;
    @FXML private Button btnConectar;
    @FXML private Button btnDesconectar;
    @FXML private Button btnEnviar;
    @FXML private CheckBox chkPrivado;
    @FXML private Label lblEstado;
    @FXML private Label lblChat;
    @FXML private ScrollPane scrollChat;

    // Socket, streams, conectado y lockSalida estan ahora en Conexion
    private final Conexion conexion = new Conexion();
    private String nombreUsuario = "";
    private final StringBuilder chat = new StringBuilder();
    private boolean uiConectado = false;

    @FXML
    public void initialize() {

        lblChat.setText("");
        lblChat.setWrapText(true);
        lblChat.setAlignment(Pos.TOP_LEFT);
        lblChat.setPrefHeight(Region.USE_COMPUTED_SIZE);
        scrollChat.setFitToWidth(true);
        lblChat.heightProperty().addListener((obs, antes, ahora) ->
                Platform.runLater(() -> scrollChat.setVvalue(1.0)));

        chkPrivado.selectedProperty().addListener((obs, antes, ahora) -> refrescarControles());

        setConectado(false);

        lblChat.sceneProperty().addListener((obs, antigua, scene) -> {
            if (scene != null) {
                scene.windowProperty().addListener((o, antiguaV, ventana) -> engancharCierre(ventana));
                engancharCierre(scene.getWindow());
            }
        });
    }

    private void engancharCierre(Window ventana) {
        if (ventana != null) {
            ventana.setOnHiding(e -> desconectar());
        }
    }
    @FXML
    private void onConectar() {
        String ip = txtIp.getText().trim();
        if (ip.isEmpty()) {
            ip = txtIp.getPromptText();
        }
        String textoPuerto = txtPuerto.getText().trim();
        if (textoPuerto.isEmpty()) {
            textoPuerto = txtPuerto.getPromptText();
        }
        String nombre = txtUsuario.getText().trim();

        int puerto;
        try {
            puerto = Integer.parseInt(textoPuerto);
            if (puerto < 1 || puerto > 65535) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            avisar("El puerto debe ser un numero entre 1 y 65535.");
            return;
        }
        if (nombre.isEmpty()) {
            avisar("Escribe un nombre de usuario.");
            return;
        }
        if (nombre.contains("|")) {
            avisar("El nombre de usuario no puede contener '|'.");
            return;
        }

        nombreUsuario = nombre;
        lblEstado.setText("Conectando...");
        btnConectar.setDisable(true);

        final String ipFinal = ip;
        final int puertoFinal = puerto;

        // conectarYRecibir esta ahora en HiloReceptor
        Thread receptor = new Thread(new HiloReceptor(this, conexion, ipFinal, puertoFinal, nombreUsuario), "Hilo-Receptor");
        receptor.setDaemon(true);   // no impide cerrar la aplicacion
        receptor.start();
    }

    @FXML
    private void onDesconectar() {
        desconectar();
    }

    @FXML
    private void onEnviar() {
        if (!conexion.conectado) {
            return;
        }
        String texto = txtMensaje.getText().trim();
        if (texto.isEmpty()) {
            return;
        }

        try {
            if (chkPrivado.isSelected()) {
                String destino = txtDestinatario.getText().trim();
                if (destino.isEmpty()) {
                    avisar("Escribe el usuario destinatario en el campo 'Para:'.");
                    return;
                }
                if (destino.contains("|")) {
                    avisar("El nombre del destinatario no puede contener '|'.");
                    return;
                }
                if (destino.equals(nombreUsuario)) {
                    avisar("No puedes enviarte un mensaje privado a ti mismo.");
                    return;
                }
                conexion.enviar("PRIVADO|" + destino + "|" + texto);
                // Al privado no hay retransmision hacia nosotros: lo mostramos en local.
                anadirLinea("[PRIVADO] tu -> " + destino + ": " + texto);
            } else {
                conexion.enviar("PUBLICO|" + texto);
                // El mensaje publico lo mostrara el servidor al retransmitirlo a todos.
            }
            txtMensaje.clear();
        } catch (IOException e) {
            anadirLinea("[SISTEMA] Error al enviar el mensaje: " + e.getMessage());
            cerrarConexion(null);
        }
        txtMensaje.requestFocus();
    }

    private void desconectar() {
        if (!conexion.conectado) {
            return;
        }
        try {
            conexion.enviar("SALIR");
        } catch (IOException e) {
        }
        cerrarConexion(null);
        anadirLinea("[SISTEMA] Te has desconectado.");
    }

    /**
     * Cierra la conexion con el servidor y deja la interfaz en "No conectado".
     * La parte de cerrar el socket y los streams esta en Conexion.cerrar().
     */
    void cerrarConexion(Socket propietario) {
        conexion.cerrar(propietario);
        Platform.runLater(() -> {
            if (!conexion.conectado) {
                setConectado(false);
                lblEstado.setText("No conectado");
                ponerTitulo(TITULO);
            }
        });
    }

    /**
     * Lo llama HiloReceptor cuando la conexion se ha establecido.
     * (Es el bloque Platform.runLater que estaba dentro de conectarYRecibir.)
     */
    void mostrarConectado(String ip, int puerto) {
        Platform.runLater(() -> {
            setConectado(true);
            lblEstado.setText("Conectado");
            ponerTitulo(TITULO + " - " + nombreUsuario);
            anadirLinea("[SISTEMA] Conectado a " + ip + ":" + puerto + " como " + nombreUsuario + ".");
        });
    }


    //Util


    private void setConectado(boolean estado) {
        uiConectado = estado;
        refrescarControles();
    }

    private void refrescarControles() {
        btnConectar.setDisable(uiConectado);
        btnDesconectar.setDisable(!uiConectado);
        btnEnviar.setDisable(!uiConectado);
        txtMensaje.setDisable(!uiConectado);
        chkPrivado.setDisable(!uiConectado);
        txtDestinatario.setDisable(!uiConectado || !chkPrivado.isSelected());
        txtIp.setDisable(uiConectado);
        txtPuerto.setDisable(uiConectado);
        txtUsuario.setDisable(uiConectado);
    }

    void mostrarDesdeHilo(String texto) {
        Platform.runLater(() -> anadirLinea(texto));
    }

    private void anadirLinea(String texto) {
        chat.append("[").append(LocalTime.now().format(HORA)).append("] ")
            .append(texto).append("\n");
        lblChat.setText(chat.toString());
    }

    private void ponerTitulo(String titulo) {
        if (lblChat.getScene() != null && lblChat.getScene().getWindow() instanceof Stage) {
            ((Stage) lblChat.getScene().getWindow()).setTitle(titulo);
        }
    }

    private void avisar(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING, mensaje);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
