package reto1_multiusuario.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import reto1_multiusuario.Server.Controller.GestorClientes;
import reto1_multiusuario.Server.Controller.HiloCliente;
import reto1_multiusuario.Server.Controller.HiloEstadisticas;

public class MainServer {

    private static final int PUERTO = 5050;
    private static final int MAX_CLIENTES = 10;
    private static final int SEGUNDOS_ESTADISTICAS = 30;

    public static void main(String[] args) {
        GestorClientes gestor = new GestorClientes(MAX_CLIENTES);
        new HiloEstadisticas(gestor, SEGUNDOS_ESTADISTICAS).start();

        int contadorClientes = 0;
        try (ServerSocket server = new ServerSocket(PUERTO)) {
            Log.escribir("Servidor iniciado en el puerto " + PUERTO + " (maximo " + MAX_CLIENTES + " clientes).");
            while (true) {
                Socket socket = server.accept();
                contadorClientes++;
                new Thread(new HiloCliente(socket, contadorClientes, gestor)).start();
            }
        } catch (IOException e) {
            Log.escribir("Error en el servidor: " + e.getMessage());
        }
    }
}
