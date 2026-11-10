package reto1_multiusuario.Client.Controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import reto1_multiusuario.Modelo.Mensaje;

public class Conexion {

    private final Socket socket = new Socket();
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;

    public synchronized void conectar(String ip, int puerto, String nombre) throws IOException {
        socket.connect(new InetSocketAddress(ip, puerto), 5000);
        salida = new ObjectOutputStream(socket.getOutputStream());
        salida.flush();
        entrada = new ObjectInputStream(socket.getInputStream());
        salida.writeObject(nombre);
        salida.flush();
    }

    public synchronized void enviar(Mensaje mensaje) throws IOException {
        salida.writeObject(mensaje);
        salida.flush();
        salida.reset();
    }

    public Mensaje recibir() throws IOException, ClassNotFoundException {
        return (Mensaje) entrada.readObject();
    }

    public void cerrar() {
        try {
            socket.close();
        } catch (IOException ignorada) {
        }
    }
}
