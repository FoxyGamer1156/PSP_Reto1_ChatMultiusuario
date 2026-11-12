package reto1_multiusuario.Server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Log {

    private static final String ARCHIVO = "servidor.log";
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static synchronized void escribir(String mensaje) {
        String linea = "[" + LocalDateTime.now().format(FORMATO) + "] " + mensaje;
        System.out.println(linea);

        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(ARCHIVO, true))) {
            escritor.write(linea);
            escritor.newLine();
        } catch (IOException e) {
            System.out.println("Error al escribir en el log: " + e.getMessage());
        }
    }
}
