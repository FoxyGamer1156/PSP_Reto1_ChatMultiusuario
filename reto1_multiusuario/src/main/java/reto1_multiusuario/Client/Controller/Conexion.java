package reto1_multiusuario.Client.Controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Conexion con el servidor: socket, streams y los metodos para enviar y cerrar.
 *
 * Codigo movido tal cual desde PantallaPrincipal.
 */
public class Conexion {

    Socket socket;
    ObjectOutputStream salida;
    ObjectInputStream entrada;
    volatile boolean conectado = false;
    final Object lockSalida = new Object();

    void enviar(String mensaje) throws IOException {
        synchronized (lockSalida) {
            if (salida == null) {
                throw new IOException("no hay conexion");
            }
            salida.writeObject(mensaje);
            salida.flush();
        }
    }

    /**
     * Cierra la conexion con el servidor.
     *
     * @param propietario socket del hilo receptor que pide cerrar. Si ya no es
     *                    el socket actual (porque el usuario se ha vuelto a
     *                    conectar), solo se cierra ese socket antiguo.
     *                    Con null se cierra la conexion actual.
     */
    void cerrar(Socket propietario) {
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
    }

    private void cerrarSilencioso(java.io.Closeable c) {
        try {
            if (c != null) c.close();
        } catch (IOException e) {
            // ignorado
        }
    }
}
