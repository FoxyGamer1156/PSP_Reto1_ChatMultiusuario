package reto1_multiusuario.Server;

import java.net.ServerSocket;
import java.net.Socket;

import reto1_multiusuario.Server.Controller.HiloCliente;

public class mainServer {
    private static final int PORT = 5050;
    private static int contadorClientes = 0;
    public static void main(String[] args) {    
        try(ServerSocket server = new ServerSocket(PORT);){
            System.out.println("Servidor iniciado en el puerto " + PORT);
            while(true){
                Socket clientSocket = server.accept();
                contadorClientes++;
                HiloCliente hiloCliente = new HiloCliente(clientSocket, contadorClientes);
                hiloCliente.run();
            }
        } catch (Exception e) {
            System.out.println("Error en el servidor: " + e.getMessage());
        }
        
    }
}
