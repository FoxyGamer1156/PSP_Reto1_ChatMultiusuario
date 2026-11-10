# Chat Multiusuario

Aplicación de chat multiusuario en Java con arquitectura cliente-servidor.
Proyecto del reto de **Programación de Servicios y Procesos** (2º DAM).

- **Servidor:** aplicación de consola. Crea un hilo por cliente, mantiene la lista de usuarios y el historial de mensajes, y escribe un log global.
- **Cliente:** aplicación con interfaz gráfica en JavaFX. Recibe los mensajes en un hilo independiente.

## Autores

- _Christian Garay Salinas_
- _Alvaro Jauregui Sanchez de isasia_
- _Anartz Cabanelas Rios_

## Estructura del proyecto

```
/PSP_Reto1_ChatMultiusuario
├── reto1_multiusuario/
│   ├── src/main/
│   │   ├── java/
│   │   │   ├── reto1_multiusuario/
│   │   │   │   ├── Modelo/Mensaje.java                 (objeto que viaja por el socket)
│   │   │   │   ├── Server/MainServer.java              (main del servidor)
│   │   │   │   ├── Server/Log.java                     (log global)
│   │   │   │   ├── Server/Controller/GestorClientes.java
│   │   │   │   ├── Server/Controller/HiloCliente.java
│   │   │   │   ├── Server/Controller/HiloEstadisticas.java
│   │   │   │   ├── Client/Controller/PantallaPrincipal.java
│   │   │   │   ├── Client/Controller/Conexion.java
│   │   │   │   ├── Client/Controller/HiloReceptor.java
│   │   │   │   └── main.java                           (main del cliente)
│   │   │   └── module-info.java
│   │   └── resources/view/msgView.fxml
│   └── pom.xml
└── README.MD
```

## Requisitos

- JDK **25** (también seleccionado como JDK de Maven).
- Maven **3.9.x**.
- JavaFX **25**, gestionado por Maven.

## Cómo ejecutar el servidor

Desde la raíz del repositorio:

```powershell
mvn -f reto1_multiusuario/pom.xml clean compile
java -cp reto1_multiusuario/target/classes reto1_multiusuario.Server.MainServer
```

Por defecto, el servidor:

| Parámetro | Valor | Dónde se cambia |
|---|---|---|
| Puerto | 5050 | `MainServer.PUERTO` |
| Máximo de clientes | 10 | `MainServer.MAX_CLIENTES` |
| Intervalo de estadísticas | 30 s | `MainServer.SEGUNDOS_ESTADISTICAS` |

El log se muestra por consola y además se guarda en `servidor.log`, en la carpeta desde donde se lance el servidor.
Cada 30 segundos se imprime una línea `[ESTADO]` con los usuarios conectados, el tiempo activo y el último mensaje.

## Cómo ejecutar el cliente

En otra terminal, desde la raíz del repositorio (se pueden abrir varios clientes):

```powershell
mvn -f reto1_multiusuario/pom.xml javafx:run
```

## Cómo usar la aplicación

### Conectar y desconectar

1. Escribe la IP y el puerto del servidor. Si los dejas vacíos, se usan `127.0.0.1` y `5050`.
2. Escribe tu nombre de usuario y pulsa **Conectar**.
3. El nombre de usuario se muestra en la barra de título y junto a los botones ("Conectado como ...").
4. Pulsa **Desconectar** para salir. Cerrar la ventana también desconecta.

El servidor rechaza la conexión, y el cliente muestra el motivo, si:
- el servidor está lleno, o
- ya hay un usuario conectado con ese nombre.

### Enviar mensajes públicos

Con la casilla **Privado** desmarcada, escribe el mensaje y pulsa **Enviar** (o Enter). Lo reciben todos los usuarios conectados.

### Enviar mensajes privados

Marca **Privado**, escribe el nombre del destinatario en **Para:** y envía el mensaje. Solo lo reciben el destinatario y el emisor. Si el destinatario no está conectado, el servidor avisa.

### Cómo se ven los mensajes

```
[18:20:01] [SISTEMA] Luis se ha unido al chat.
[18:20:05] [PUBLICO] Ana: hola a todos
[18:20:09] [PRIVADO] Ana -> Tu: hola Luis
[18:20:12] [PRIVADO] Tu -> Ana: que tal
```

## Funcionalidades

- [x] Múltiples clientes simultáneos (un hilo por cliente)
- [x] Mensajes públicos y privados
- [x] Aviso al resto de usuarios cuando entra o sale un cliente
- [x] Log global (entradas, salidas y mensajes) en consola y en archivo
- [x] Lista de usuarios conectados e historial de mensajes en el servidor
- [x] Límite máximo de clientes conectados
- [x] Información periódica en consola (usuarios conectados, tiempo activo y último mensaje)
- [x] Recepción de mensajes en hilo independiente y actualización segura de la interfaz (`Platform.runLater`)
- [x] Sincronización de recursos compartidos con `synchronized`
