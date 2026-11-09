package reto1_multiusuario.Server.Controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import reto1_multiusuario.Modelo.Mensaje;
import reto1_multiusuario.Modelo.Mensaje.Tipo;
import reto1_multiusuario.Server.Log;

public class HiloCliente implements Runnable {

    private static final String SERVIDOR = "Servidor";

    private final Socket socket;
    private final int numCliente;
    private final GestorClientes gestor;
    private String nombre;
    private ObjectOutputStream salida;

    public HiloCliente(Socket socket, int numCliente, GestorClientes gestor) {
        this.socket = socket;
        this.numCliente = numCliente;
        this.gestor = gestor;
        this.nombre = "Cliente " + numCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public synchronized void enviar(Mensaje mensaje) {
        try {
            salida.writeObject(mensaje);
            salida.flush();
            salida.reset();
        } catch (IOException e) {
            Log.escribir("Error al enviar a " + nombre + ": " + e.getMessage());
        }
    }

    private void avisar(String texto) {
        enviar(new Mensaje(Tipo.SISTEMA, SERVIDOR, nombre, texto));
    }

    @Override
    public void run() {
        boolean haEntrado = false;

        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream())) {
            salida = out;
            nombre = (String) entrada.readObject();

            String error = gestor.agregar(this);
            if (error != null) {
                avisar(error);
                Log.escribir("Conexion rechazada (cliente " + numCliente + ", '" + nombre + "'): " + error);
                return;
            }
            haEntrado = true;

            Log.escribir("Usuario '" + nombre + "' ha entrado. Conectados: " + gestor.getNumConectados());
            avisar("Bienvenido, " + nombre + ". Conectados: " + gestor.getNombres());
            gestor.enviarATodos(new Mensaje(Tipo.SISTEMA, SERVIDOR, null, nombre + " se ha unido al chat."), this);

            boolean conectado = true;
            while (conectado) {
                Mensaje recibido = (Mensaje) entrada.readObject();

                switch (recibido.getTipo()) {
                    case PUBLICO -> {
                        Mensaje publico = new Mensaje(Tipo.PUBLICO, nombre, null, recibido.getContenido());
                        gestor.guardarMensaje(publico);
                        Log.escribir("[PUBLICO] " + publico);
                        gestor.enviarATodos(publico, null);
                    }
                    case PRIVADO -> {
                        Mensaje privado = new Mensaje(Tipo.PRIVADO, nombre, recibido.getDestinatario(), recibido.getContenido());
                        if (gestor.enviarPrivado(privado)) {
                            enviar(privado);
                            gestor.guardarMensaje(privado);
                            Log.escribir("[PRIVADO] " + privado);
                        } else {
                            avisar("El usuario '" + recibido.getDestinatario() + "' no esta conectado.");
                        }
                    }
                    case SALIR -> conectado = false;
                    default -> Log.escribir("Mensaje no valido de " + nombre + ": " + recibido.getTipo());
                }
            }
        } catch (IOException e) {
            if (haEntrado) {
                Log.escribir("Conexion perdida con " + nombre + ".");
            }
        } catch (ClassNotFoundException | ClassCastException e) {
            Log.escribir("Se ha recibido un objeto no valido de " + nombre + ".");
        } finally {
            if (haEntrado) {
                gestor.eliminar(this);
                Log.escribir("Usuario '" + nombre + "' ha salido. Conectados: " + gestor.getNumConectados());
                gestor.enviarATodos(new Mensaje(Tipo.SISTEMA, SERVIDOR, null, nombre + " ha salido del chat."), null);
            }
            try {
                socket.close();
            } catch (IOException e) {
                Log.escribir("Error al cerrar el socket de " + nombre + ": " + e.getMessage());
            }
        }
    }
}
