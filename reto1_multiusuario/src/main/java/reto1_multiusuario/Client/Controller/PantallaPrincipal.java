package reto1_multiusuario.Client.Controller;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
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

    private Socket socket;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private volatile boolean conectado = false;
    private String nombreUsuario = "";
    private final Object lockSalida = new Object();
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

        Thread receptor = new Thread(() -> conectarYRecibir(ipFinal, puertoFinal), "Hilo-Receptor");
        receptor.setDaemon(true);   // no impide cerrar la aplicacion
        receptor.start();
    }

    @FXML
    private void onDesconectar() {
        desconectar();
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
                enviar("PRIVADO|" + destino + "|" + texto);
                // Al privado no hay retransmision hacia nosotros: lo mostramos en local.
                anadirLinea("[PRIVADO] tu -> " + destino + ": " + texto);
            } else {
                enviar("PUBLICO|" + texto);
                // El mensaje publico lo mostrara el servidor al retransmitirlo a todos.
            }
            txtMensaje.clear();
        } catch (IOException e) {
            anadirLinea("[SISTEMA] Error al enviar el mensaje: " + e.getMessage());
            cerrarConexion(null);
        }
        txtMensaje.requestFocus();
    }


    private void conectarYRecibir(String ip, int puerto) {
        Socket nuevo = new Socket();
        boolean llegoAConectar = false;
        try {
            nuevo.connect(new InetSocketAddress(ip, puerto), 5000);

            // Mismo orden que el servidor: primero la salida y luego la entrada.
            ObjectOutputStream out = new ObjectOutputStream(nuevo.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(nuevo.getInputStream());

            synchronized (lockSalida) {
                socket = nuevo;
                salida = out;
                entrada = in;
                conectado = true;
                salida.writeObject(nombreUsuario); 
                salida.flush();
            }
            llegoAConectar = true;

            Platform.runLater(() -> {
                setConectado(true);
                lblEstado.setText("Conectado");
                ponerTitulo(TITULO + " - " + nombreUsuario);
                anadirLinea("[SISTEMA] Conectado a " + ip + ":" + puerto + " como " + nombreUsuario + ".");
            });

            // Bucle de recepcion
            while (conectado) {
                Object recibido = in.readObject();
                if (recibido instanceof String) {
                    procesarMensaje((String) recibido);
                }
            }

        } catch (EOFException e) {
            if (conectado) {
                mostrarDesdeHilo("[SISTEMA] El servidor ha cerrado la conexion.");
            }
        } catch (IOException | ClassNotFoundException e) {
            if (!llegoAConectar) {
                mostrarDesdeHilo("[SISTEMA] No se pudo conectar con " + ip + ":" + puerto
                        + " (" + e.getMessage() + ")");
            } else if (conectado) {
                mostrarDesdeHilo("[SISTEMA] Conexion perdida: " + e.getMessage());
            }
        } finally {
            cerrarConexion(nuevo);
        }
    }

    private void procesarMensaje(String linea) {
        String[] partes = linea.split("\\|", 3);

        switch (partes[0]) {
            case "PUBLICO":
                if (partes.length == 3) {
                    mostrarDesdeHilo("[PUBLICO] " + partes[1] + ": " + partes[2]);
                }
                break;
            case "PRIVADO":
                if (partes.length == 3) {
                    mostrarDesdeHilo("[PRIVADO] " + partes[1] + " -> tu: " + partes[2]);
                }
                break;
            case "SISTEMA":
                if (partes.length >= 2) {
                    String texto = partes.length == 3 ? partes[1] + "|" + partes[2] : partes[1];
                    mostrarDesdeHilo("[SISTEMA] " + texto);
                }
                break;
            default:
                mostrarDesdeHilo(linea);
        }
    }
    private void enviar(String mensaje) throws IOException {
        synchronized (lockSalida) {
            if (salida == null) {
                throw new IOException("no hay conexion");
            }
            salida.writeObject(mensaje);
            salida.flush();
        }
    }
    private void desconectar() {
        if (!conectado) {
            return;
        }
        try {
            enviar("SALIR");
        } catch (IOException e) {
        }
        cerrarConexion(null);
        anadirLinea("[SISTEMA] Te has desconectado.");
    }

   
     
    
    @param propietario
     
    private void cerrarConexion(Socket propietario) {
        synchronized (lockSalida) {
            if (propietario != null && propietario != socket) {
                cerrarSilencioso(propietario);
            } else {
                conectado = false;
                cerrarSilencioso(entrada);
                cerrarSilencioso(salida);
                cerrarSilencioso(socket);
                entrada = null;
                salida = null;
                socket = null;
            }
        }
        Platform.runLater(() -> {
            if (!conectado) {
                setConectado(false);
                lblEstado.setText("No conectado");
                ponerTitulo(TITULO);
            }
        });
    }

    private void cerrarSilencioso(java.io.Closeable c) {
        try {
            if (c != null) c.close();
        } catch (IOException e) {
            // ignorado
        }
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

    private void mostrarDesdeHilo(String texto) {
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
