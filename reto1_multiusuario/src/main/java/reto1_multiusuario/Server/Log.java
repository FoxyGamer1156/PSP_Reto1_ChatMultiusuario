package reto1_multiusuario.Server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Log global del servidor.
 *
 * Registra las acciones importantes (usuario que entra, usuario que sale y
 * mensajes enviados) en la consola y en un archivo de texto.
 *
 * Varios hilos (uno por cliente) pueden escribir en el log a la vez, por eso
 * el metodo escribir() es synchronized: solo un hilo puede estar escribiendo
 * en cada momento y las lineas no se mezclan.
 */
public class Log {

    // Nombre del archivo donde se guarda el log
    private static final String ARCHIVO = "servidor.log";

    // Formato de la fecha y hora que aparece al principio de cada linea
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Escribe una linea en el log (consola + archivo).
     *
     * Al ser un metodo static synchronized, el monitor es la propia clase Log,
     * asi que todos los hilos se sincronizan sobre el mismo objeto.
     *
     * @param mensaje texto que se quiere registrar
     */
    public static synchronized void escribir(String mensaje) {
        String linea = "[" + LocalDateTime.now().format(FORMATO) + "] " + mensaje;

        // Mostrar por consola
        System.out.println(linea);

        // Guardar en el archivo. El "true" hace que se anada al final
        // en vez de borrar lo que ya habia.
        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(ARCHIVO, true))) {
            escritor.write(linea);
            escritor.newLine();
        } catch (IOException e) {
            System.out.println("Error al escribir en el log: " + e.getMessage());
        }
    }
}
