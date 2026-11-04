package reto1_multiusuario.Server;

import java.net.ServerSocket;
import java.net.Socket;

import reto1_multiusuario.Server.Controller.GestorClientes;
import reto1_multiusuario.Server.Controller.HiloCliente;

public class MainServer {
    private static final int PORT = 5050;
    private static int contadorClientes = 0;
    public static void main(String[] args) {
        // Una sola lista para todo el servidor, que compartiran todos los hilos
        GestorClientes gestor = new GestorClientes();

        try(ServerSocket server = new ServerSocket(PORT);){
            System.out.println("Servidor iniciado en el puerto " + PORT);
            while(true){
                Socket clientSocket = server.accept();
                contadorClientes++;
                HiloCliente hiloCliente = new HiloCliente(clientSocket, contadorClientes, gestor);
                // start() crea un hilo nuevo que ejecutara run() en paralelo.
                // Asi el main vuelve enseguida a accept() para atender al siguiente cliente.
                new Thread(hiloCliente).start();
            }
        } catch (Exception e) {
            System.out.println("Error en el servidor: " + e.getMessage());
        }
        
    }
}
