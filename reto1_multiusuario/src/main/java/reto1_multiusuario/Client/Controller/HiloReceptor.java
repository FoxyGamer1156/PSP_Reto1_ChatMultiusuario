package reto1_multiusuario.Client.Controller;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Hilo que conecta con el servidor y recibe los mensajes.
 *
 * Codigo movido tal cual desde PantallaPrincipal (conectarYRecibir y
 * procesarMensaje). Lo que toca la interfaz se pide a PantallaPrincipal.
 */
public class HiloReceptor implements Runnable {

    private final PantallaPrincipal pantalla;
    private final Conexion conexion;
    private final String ip;
    private final int puerto;
    private final String nombreUsuario;

    public HiloReceptor(PantallaPrincipal pantalla, Conexion conexion, String ip, int puerto, String nombreUsuario) {
        this.pantalla = pantalla;
        this.conexion = conexion;
        this.ip = ip;
        this.puerto = puerto;
        this.nombreUsuario = nombreUsuario;
    }

    @Override
    public void run() {
        conectarYRecibir(ip, puerto);
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

            synchronized (conexion.lockSalida) {
                conexion.socket = nuevo;
                conexion.salida = out;
                conexion.entrada = in;
                conexion.conectado = true;
                conexion.salida.writeObject(nombreUsuario);
                conexion.salida.flush();
            }
            llegoAConectar = true;

            pantalla.mostrarConectado(ip, puerto);

            // Bucle de recepcion
            while (conexion.conectado) {
                Object recibido = in.readObject();
                if (recibido instanceof String) {
                    procesarMensaje((String) recibido);
                }
            }

        } catch (EOFException e) {
            if (conexion.conectado) {
                pantalla.mostrarDesdeHilo("[SISTEMA] El servidor ha cerrado la conexion.");
            }
        } catch (IOException | ClassNotFoundException e) {
            if (!llegoAConectar) {
                pantalla.mostrarDesdeHilo("[SISTEMA] No se pudo conectar con " + ip + ":" + puerto
                        + " (" + e.getMessage() + ")");
            } else if (conexion.conectado) {
                pantalla.mostrarDesdeHilo("[SISTEMA] Conexion perdida: " + e.getMessage());
            }
        } finally {
            pantalla.cerrarConexion(nuevo);
        }
    }

    private void procesarMensaje(String linea) {
        String[] partes = linea.split("\\|", 3);

        switch (partes[0]) {
            case "PUBLICO":
                if (partes.length == 3) {
                    pantalla.mostrarDesdeHilo("[PUBLICO] " + partes[1] + ": " + partes[2]);
                }
                break;
            case "PRIVADO":
                if (partes.length == 3) {
                    pantalla.mostrarDesdeHilo("[PRIVADO] " + partes[1] + " -> tu: " + partes[2]);
                }
                break;
            case "SISTEMA":
                if (partes.length >= 2) {
                    String texto = partes.length == 3 ? partes[1] + "|" + partes[2] : partes[1];
                    pantalla.mostrarDesdeHilo("[SISTEMA] " + texto);
                }
                break;
            default:
                pantalla.mostrarDesdeHilo(linea);
        }
    }
}
