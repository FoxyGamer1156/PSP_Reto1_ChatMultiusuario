# Chat Multiusuario

Aplicación de chat multiusuario en Java con arquitectura cliente-servidor.
Proyecto del reto de **Programación de Servicios y Procesos** (2º DAM).

- **Servidor:** aplicación de consola que gestiona las conexiones, un hilo por cliente y el log global.
- **Cliente:** aplicación con interfaz gráfica en JavaFX.

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
│   │   │   │   ├── Client/Controller/PantallaPrincipal.java
│   │   │   │   ├── Server/Controller/HiloCliente.java
│   │   │   │   ├── Server/mainServer.java
│   │   │   │   └── main.java
│   │   │   └── module-info.java
│   │   └── resources/
│   │       ├── styles/
│   │       └── view/msgView.fxml
│   ├── target/
│   └── pom.xml
├── GUIA_SERVIDOR.md
├── gitignore
└── README.MD
```

## Requisitos

- JDK **25**, tambien seleccionado como JDK de Maven.
- Maven **3.9.x** actualizado.
- JavaFX **25**, gestionado por Maven; no hace falta instalar un SDK separado.

## Guia del servidor

La [guia del servidor](GUIA_SERVIDOR.md) analiza esta version del proyecto y
explica arquitectura, sockets, hilos, `synchronized`, protocolo, implementacion
de referencia y pruebas locales con JDK 25.

El servidor actual es inicial: llama a `run()` directamente y todavia no procesa
mensajes. La referencia de la guia no se ha incorporado a las clases del proyecto.

## Cómo ejecutar el servidor

Desde la raiz del repositorio, con JDK 25 y Maven disponibles:

```powershell
mvn -f reto1_multiusuario/pom.xml clean compile
java -cp reto1_multiusuario/target/classes reto1_multiusuario.Server.mainServer
```

El servidor inicial escucha en el puerto **5050**. Todavia no ofrece el chat
completo ni restringe la escucha a la interfaz local. La guia explica como
desarrollar esas funciones y escuchar exclusivamente en `127.0.0.1`.

## Cómo ejecutar el cliente

En otro terminal, desde la raiz del repositorio:

```powershell
mvn -f reto1_multiusuario/pom.xml javafx:run
```

Se abre la vista JavaFX actual. El controlador y las acciones de conexion y envio
estan pendientes de implementar. La IP local prevista es **127.0.0.1** y el
puerto sugerido es **5050**. `promptText` es solo una sugerencia, no un valor.

Para comprobar las herramientas: `java -version`, `javac -version` y
`mvn -version`. Esta revision no pudo compilarse ni ejecutarse en el terminal
de trabajo porque no encuentra esas herramientas en el `PATH`.

## Cómo usar la aplicación

### Conectar y desconectar

_El usuario introducira la IP y el puerto para conectarse al servidor._

### Enviar mensajes públicos

_Los mensajes en publico se enviaran cuando la checkbox de "Privado" este desactivado._

### Enviar mensajes privados

_Los mensajes en privado se enviaran cuando la checkbox de "Privado" este activa, y solo hablara con el cliente puesto._

## Funcionalidades

- [ ] Múltiples clientes simultáneos (un hilo por cliente)
- [ ] Mensajes públicos y privados
- [ ] Aviso al resto de usuarios cuando entra un nuevo cliente
- [ ] Log global (entradas, salidas y mensajes)
- [ ] Límite máximo de clientes conectados
- [ ] Información periódica en consola (usuarios conectados, tiempo activo y último mensaje)
- [ ] Recepción de mensajes en hilo independiente y actualización segura de la interfaz
