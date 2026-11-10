package reto1_multiusuario.Client.Controller;

import java.io.EOFException;
import java.io.IOException;

import javafx.application.Platform;
import reto1_multiusuario.Modelo.Mensaje;

public class HiloReceptor extends Thread {

    private final PantallaPrincipal pantalla;
    private final Conexion conexion;
    private final String ip;
    private final int puerto;
    private final String nombre;

    public HiloReceptor(PantallaPrincipal pantalla, Conexion conexion, String ip, int puerto, String nombre) {
        this.pantalla = pantalla;
        this.conexion = conexion;
        this.ip = ip;
        this.puerto = puerto;
        this.nombre = nombre;
        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            conexion.conectar(ip, puerto, nombre);
        } catch (IOException e) {
            Platform.runLater(() -> pantalla.conexionTerminada(conexion,
                    "No se pudo conectar con " + ip + ":" + puerto + " (" + e.getMessage() + ")"));
            return;
        }

        Platform.runLater(() -> pantalla.conexionEstablecida(conexion, ip, puerto));

        try {
            while (true) {
                Mensaje mensaje = conexion.recibir();
                Platform.runLater(() -> pantalla.mostrarMensaje(mensaje));
            }
        } catch (EOFException e) {
            Platform.runLater(() -> pantalla.conexionTerminada(conexion, "El servidor ha cerrado la conexion."));
        } catch (IOException | ClassNotFoundException e) {
            Platform.runLater(() -> pantalla.conexionTerminada(conexion, "Conexion perdida: " + e.getMessage()));
        }
    }
}
