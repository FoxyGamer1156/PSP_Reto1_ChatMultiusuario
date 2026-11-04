package reto1_multiusuario.Server.Controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import reto1_multiusuario.Server.Log;

public class HiloCliente implements Runnable {
    private final Socket socket;
    // Sin static: cada hilo guarda su propio numero de cliente
    private final int numCliente;
    // Lista compartida: es el MISMO objeto para todos los hilos
    private final GestorClientes gestor;
    // Nombre de usuario. Hasta que el cliente nos lo diga, lo identificamos por su numero
    private String nombre;
    // Stream de salida hacia ESTE cliente. Es atributo porque otros hilos
    // lo usaran (a traves de enviar()) para mandarle mensajes.
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

    /**
     * Envia un mensaje a este cliente.
     *
     * Es synchronized porque varios hilos pueden querer escribir a la vez en
     * el MISMO stream (por ejemplo, Ana y Luis envian un publico a la vez y
     * los dos hilos escriben en el stream de Carlos). Sin synchronized, los
     * datos de los dos mensajes se podrian mezclar y romper el stream.
     */
    public synchronized void enviar(String mensaje) {
        try {
            salida.writeObject(mensaje);
            salida.flush();
        } catch (IOException e) {
            // Si este cliente se ha caido, no queremos que falle el hilo que
            // esta enviando: solo lo registramos. Su propio hilo lo sacara de la lista.
            Log.escribir("Error al enviar a " + nombre + ": " + e.getMessage());
        }
    }

    @Override
    public void run() {

        try(
            ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream())
        ) {
            // Guardamos el stream de salida en el atributo para poder usarlo en enviar()
            this.salida = salida;

            // 1. Lo primero que envia el cliente al conectarse es su nombre de usuario
            nombre = (String) entrada.readObject();
            // Ya sabemos quien es: lo anadimos a la lista compartida
            gestor.agregar(this);
            Log.escribir("Usuario '" + nombre + "' ha entrado (cliente " + numCliente + ")."
                    + " Conectados: " + gestor.getNumConectados());

            // 2. Bucle de lectura: readObject() se queda esperando hasta que llega
            //    un mensaje. Se repite mientras el cliente siga conectado.
            while (true) {
                String mensaje = (String) entrada.readObject();

                // El mensaje tiene la forma TIPO|resto. Separamos solo en 2 partes
                // para que el texto pueda contener '|' sin romperse.
                String[] partes = mensaje.split("\\|", 2);
                String tipo = partes[0];

                if (tipo.equals("SALIR")) {
                    // El cliente pulsa Desconectar: salimos del bucle y el finally hace el resto
                    break;

                } else if (tipo.equals("PUBLICO") && partes.length == 2) {
                    // PUBLICO|texto  ->  se reenvia a todos como PUBLICO|emisor|texto
                    String texto = partes[1];
                    Log.escribir("[PUBLICO] " + nombre + ": " + texto);
                    gestor.enviarATodos("PUBLICO|" + nombre + "|" + texto);

                } else if (tipo.equals("PRIVADO") && partes.length == 2) {
                    // PRIVADO|destino|texto  ->  solo al destino como PRIVADO|emisor|texto
                    String[] datos = partes[1].split("\\|", 2);
                    if (datos.length == 2) {
                        String destino = datos[0];
                        String texto = datos[1];
                        boolean encontrado = gestor.enviarPrivado(destino, "PRIVADO|" + nombre + "|" + texto);
                        if (encontrado) {
                            Log.escribir("[PRIVADO] " + nombre + " -> " + destino + ": " + texto);
                        } else {
                            // Avisamos solo al emisor de que ese usuario no existe
                            enviar("SISTEMA|El usuario '" + destino + "' no esta conectado.");
                        }
                    }

                } else {
                    Log.escribir("Mensaje no reconocido de " + nombre + ": " + mensaje);
                }
            }

        } catch (IOException e) {
            // Cuando el cliente cierra la conexion (o se corta la red),
            // readObject() lanza una IOException y salimos del bucle.
            // No es un error del servidor: es la forma normal de desconectarse.
        } catch (ClassNotFoundException e) {
            Log.escribir("Error: se ha recibido un objeto desconocido de " + nombre);
        } finally {
            // Pase lo que pase, el usuario sale de la lista y se cierra su socket.
            // Si se desconecto antes de enviar su nombre, no estaba en la lista
            // y eliminar() simplemente no hace nada.
            gestor.eliminar(this);
            Log.escribir("Usuario '" + nombre + "' ha salido."
                    + " Conectados: " + gestor.getNumConectados());
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error al cerrar el socket: " + e.getMessage());
            }
        }
    }

}
