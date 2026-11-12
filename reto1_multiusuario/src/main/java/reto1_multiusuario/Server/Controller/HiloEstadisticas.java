package reto1_multiusuario.Server.Controller;

import reto1_multiusuario.Modelo.Mensaje;

public class HiloEstadisticas extends Thread {

    private final GestorClientes gestor;
    private final int segundos;
    private final long inicio = System.currentTimeMillis();

    public HiloEstadisticas(GestorClientes gestor, int segundos) {
        this.gestor = gestor;
        this.segundos = segundos;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(segundos * 1000L);
            } catch (InterruptedException e) {
                return;
            }

            long activo = (System.currentTimeMillis() - inicio) / 1000;
            String tiempo = String.format("%02d:%02d:%02d", activo / 3600, (activo % 3600) / 60, activo % 60);
            Mensaje ultimo = gestor.getUltimoMensaje();
            String textoUltimo = "ninguno";
            if (ultimo != null) {
                textoUltimo = ultimo.toString();
            }

            System.out.println("[ESTADO] Usuarios conectados: " + gestor.getNumConectados()
                    + " | Tiempo activo: " + tiempo
                    + " | Ultimo mensaje: " + textoUltimo);
        }
    }
}
