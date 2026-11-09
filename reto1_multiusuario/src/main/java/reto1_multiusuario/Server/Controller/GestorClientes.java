package reto1_multiusuario.Server.Controller;

import java.util.ArrayList;

import reto1_multiusuario.Modelo.Mensaje;

public class GestorClientes {

    private final int maxClientes;
    private final ArrayList<HiloCliente> clientes = new ArrayList<>();
    private final ArrayList<Mensaje> historial = new ArrayList<>();

    public GestorClientes(int maxClientes) {
        this.maxClientes = maxClientes;
    }

    public synchronized String agregar(HiloCliente cliente) {
        String nombre = cliente.getNombre();
        if (nombre == null || nombre.isBlank()) {
            return "El nombre de usuario no es valido.";
        }
        if (clientes.size() >= maxClientes) {
            return "Servidor lleno (maximo " + maxClientes + " usuarios). Prueba mas tarde.";
        }
        for (HiloCliente c : clientes) {
            if (c.getNombre().equalsIgnoreCase(nombre)) {
                return "El nombre '" + nombre + "' ya esta en uso.";
            }
        }
        clientes.add(cliente);
        return null;
    }

    public synchronized void eliminar(HiloCliente cliente) {
        clientes.remove(cliente);
    }

    public synchronized int getNumConectados() {
        return clientes.size();
    }

    public synchronized String getNombres() {
        ArrayList<String> nombres = new ArrayList<>();
        for (HiloCliente c : clientes) {
            nombres.add(c.getNombre());
        }
        return String.join(", ", nombres);
    }

    private synchronized ArrayList<HiloCliente> copiaClientes() {
        return new ArrayList<>(clientes);
    }

    public void enviarATodos(Mensaje mensaje, HiloCliente excepto) {
        for (HiloCliente c : copiaClientes()) {
            if (c != excepto) {
                c.enviar(mensaje);
            }
        }
    }

    public boolean enviarPrivado(Mensaje mensaje) {
        for (HiloCliente c : copiaClientes()) {
            if (c.getNombre().equalsIgnoreCase(mensaje.getDestinatario())) {
                c.enviar(mensaje);
                return true;
            }
        }
        return false;
    }

    public synchronized void guardarMensaje(Mensaje mensaje) {
        historial.add(mensaje);
    }

    public synchronized Mensaje getUltimoMensaje() {
        if (historial.isEmpty()) {
            return null;
        }
        return historial.get(historial.size() - 1);
    }
}
