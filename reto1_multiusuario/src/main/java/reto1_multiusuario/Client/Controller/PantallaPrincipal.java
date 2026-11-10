package reto1_multiusuario.Client.Controller;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import reto1_multiusuario.Modelo.Mensaje;
import reto1_multiusuario.Modelo.Mensaje.Tipo;

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
    @FXML private TextArea txtChat;

    private Conexion conexion;
    private boolean conectado = false;
    private String nombreUsuario = "";

    @FXML
    public void initialize() {
        refrescarControles();
    }

    @FXML
    private void onPrivado() {
        refrescarControles();
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
        } catch (NumberFormatException e) {
            puerto = -1;
        }
        if (puerto < 1 || puerto > 65535) {
            avisar("El puerto debe ser un numero entre 1 y 65535.");
            return;
        }
        if (nombre.isEmpty()) {
            avisar("Escribe un nombre de usuario.");
            return;
        }

        nombreUsuario = nombre;
        conexion = new Conexion();
        btnConectar.setDisable(true);
        lblEstado.setText("Conectando...");
        new HiloReceptor(this, conexion, ip, puerto, nombre).start();
    }

    @FXML
    public void desconectar() {
        if (conexion == null) {
            return;
        }
        Conexion actual = conexion;
        conexion = null;

        if (conectado) {
            try {
                actual.enviar(new Mensaje(Tipo.SALIR, nombreUsuario, null, null));
            } catch (IOException ignorada) {
            }
            anadirLinea("[SISTEMA] Te has desconectado.");
        }
        actual.cerrar();
        marcarDesconectado();
    }

    @FXML
    private void onEnviar() {
        if (!conectado) {
            return;
        }
        String texto = txtMensaje.getText().trim();
        if (texto.isEmpty()) {
            return;
        }

        Mensaje mensaje;
        if (chkPrivado.isSelected()) {
            String destino = txtDestinatario.getText().trim();
            if (destino.isEmpty()) {
                avisar("Escribe el usuario destinatario en el campo 'Para:'.");
                return;
            }
            if (destino.equalsIgnoreCase(nombreUsuario)) {
                avisar("No puedes enviarte un mensaje privado a ti mismo.");
                return;
            }
            mensaje = new Mensaje(Tipo.PRIVADO, nombreUsuario, destino, texto);
        } else {
            mensaje = new Mensaje(Tipo.PUBLICO, nombreUsuario, null, texto);
        }

        try {
            conexion.enviar(mensaje);
            txtMensaje.clear();
        } catch (IOException e) {
            conexionTerminada(conexion, "Error al enviar el mensaje: " + e.getMessage());
        }
        txtMensaje.requestFocus();
    }

    void conexionEstablecida(Conexion origen, String ip, int puerto) {
        if (origen != conexion) {
            return;
        }
        conectado = true;
        refrescarControles();
        lblEstado.setText("Conectado como " + nombreUsuario);
        ponerTitulo(TITULO + " - " + nombreUsuario);
        anadirLinea("[SISTEMA] Conectado a " + ip + ":" + puerto + " como " + nombreUsuario + ".");
    }

    void conexionTerminada(Conexion origen, String motivo) {
        if (origen != conexion) {
            return;
        }
        conexion = null;
        origen.cerrar();
        anadirLinea("[SISTEMA] " + motivo);
        marcarDesconectado();
    }

    void mostrarMensaje(Mensaje mensaje) {
        if (mensaje.getTipo() == Tipo.PUBLICO) {
            anadirLinea("[PUBLICO] " + mensaje.getEmisor() + ": " + mensaje.getContenido());
        } else if (mensaje.getTipo() == Tipo.PRIVADO) {
            if (mensaje.getEmisor().equalsIgnoreCase(nombreUsuario)) {
                anadirLinea("[PRIVADO] Tu -> " + mensaje.getDestinatario() + ": " + mensaje.getContenido());
            } else {
                anadirLinea("[PRIVADO] " + mensaje.getEmisor() + " -> Tu: " + mensaje.getContenido());
            }
        } else if (mensaje.getTipo() == Tipo.SISTEMA) {
            anadirLinea("[SISTEMA] " + mensaje.getContenido());
        }
    }

    private void marcarDesconectado() {
        conectado = false;
        refrescarControles();
        lblEstado.setText("No conectado");
        ponerTitulo(TITULO);
    }

    private void refrescarControles() {
        boolean conectando = conexion != null && !conectado;
        btnConectar.setDisable(conectado || conectando);
        btnDesconectar.setDisable(!conectado);
        btnEnviar.setDisable(!conectado);
        txtMensaje.setDisable(!conectado);
        chkPrivado.setDisable(!conectado);
        txtDestinatario.setDisable(!conectado || !chkPrivado.isSelected());
        txtIp.setDisable(conectado || conectando);
        txtPuerto.setDisable(conectado || conectando);
        txtUsuario.setDisable(conectado || conectando);
    }

    private void anadirLinea(String texto) {
        txtChat.appendText("[" + LocalTime.now().format(HORA) + "] " + texto + "\n");
    }

    private void ponerTitulo(String titulo) {
        if (txtChat.getScene() != null && txtChat.getScene().getWindow() instanceof Stage) {
            ((Stage) txtChat.getScene().getWindow()).setTitle(titulo);
        }
    }

    private void avisar(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING, mensaje);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
