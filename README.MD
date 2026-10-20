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
/
├── Servidor/    # Código fuente del servidor (consola)
├── Cliente/     # Código fuente del cliente (JavaFX)
├── .gitignore
└── README.MD
```

## Requisitos

- JDK _(25)_
- JavaFX SDK _()_
- _(Maven si se utiliza)_

## Cómo ejecutar el servidor

_Pendiente de completar._

```bash
# Ejemplo (por definir)
cd Servidor
```

## Cómo ejecutar el cliente

_Pendiente de completar._

```bash
# Ejemplo (por definir)
cd Cliente
```

## Cómo usar la aplicación

### Conectar y desconectar

_Pendiente de completar: configurar IP y puerto, introducir el nombre de usuario, botones de conectar y desconectar._

### Enviar mensajes públicos

_Pendiente de completar._

### Enviar mensajes privados

_Pendiente de completar._

## Funcionalidades

- [ ] Múltiples clientes simultáneos (un hilo por cliente)
- [ ] Mensajes públicos y privados
- [ ] Aviso al resto de usuarios cuando entra un nuevo cliente
- [ ] Log global (entradas, salidas y mensajes)
- [ ] Límite máximo de clientes conectados
- [ ] Información periódica en consola (usuarios conectados, tiempo activo y último mensaje)
- [ ] Recepción de mensajes en hilo independiente y actualización segura de la interfaz
