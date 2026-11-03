package reto1_multiusuario.Server.Controller;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class HiloCliente implements Runnable {
    private final Socket socket;
    private static int numCliente = 0;
    public HiloCliente(Socket socket, int numCliente) {
        this.socket = socket;
        this.numCliente = numCliente;
    }
    @Override
    public void run() {
        try(
            ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream())
        ) {
            System.out.println("Cliente " + numCliente + " conectado.");
            
        } catch (Exception e) {
            System.out.println("Error en el hilo del cliente: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
                System.out.println("Error al cerrar el socket: " + e.getMessage());
            }
        }
    }

}
