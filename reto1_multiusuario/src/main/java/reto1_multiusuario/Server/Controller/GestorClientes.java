package reto1_multiusuario.Server.Controller;

import java.util.ArrayList;

/**
 * Lista compartida de los clientes conectados al servidor.
 *
 * Solo existe UN objeto GestorClientes, que crea el MainServer y que se pasa
 * a todos los HiloCliente. Como todos los hilos acceden a la misma lista a la
 * vez (uno entra mientras otro sale...), es un recurso compartido.
 *
 * Por eso todos los metodos son synchronized: el monitor es este objeto,
 * asi que solo un hilo puede estar dentro de cualquiera de ellos a la vez.
 * Sin esto, dos hilos podrian modificar el ArrayList al mismo tiempo y
 * perderse un cliente o romperse la lista.
 */
public class GestorClientes {

    private final ArrayList<HiloCliente> clientes = new ArrayList<>();

    /**
     * Anade un cliente a la lista (cuando ya nos ha dicho su nombre).
     */
    public synchronized void agregar(HiloCliente cliente) {
        clientes.add(cliente);
    }

    /**
     * Quita un cliente de la lista (cuando se desconecta).
     */
    public synchronized void eliminar(HiloCliente cliente) {
        clientes.remove(cliente);
    }

    /**
     * Devuelve cuantos clientes hay conectados ahora mismo.
     */
    public synchronized int getNumConectados() {
        return clientes.size();
    }

    /**
     * Envia un mensaje a TODOS los clientes conectados (incluido el que lo
     * ha escrito, porque el cliente espera recibir su propio mensaje publico).
     *
     * Es synchronized para que nadie entre o salga de la lista mientras
     * la estamos recorriendo.
     */
    public synchronized void enviarATodos(String mensaje) {
        for (HiloCliente cliente : clientes) {
            cliente.enviar(mensaje);
        }
    }

    /**
     * Envia un mensaje solo al cliente que se llama "destino".
     *
     * @return true si se ha encontrado al destinatario, false si no esta conectado
     */
    public synchronized boolean enviarPrivado(String destino, String mensaje) {
        for (HiloCliente cliente : clientes) {
            if (cliente.getNombre().equals(destino)) {
                cliente.enviar(mensaje);
                return true;
            }
        }
        return false;
    }
}
