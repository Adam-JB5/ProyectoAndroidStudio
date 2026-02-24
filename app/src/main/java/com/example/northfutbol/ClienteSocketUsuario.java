package com.example.northfutbol;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * CLIENTE SOCKET
 * ==============
 * Esta clase se encarga de llamar al servidor
 *
 * FLujo de trabajo:
 * 1. Abre una conexion (socket) con la IP y el puerto del servidor.
 * 2. Envía un objeto "Peticion".
 * 3. Espera a recibir un objeto "Respuesta".
 * 4. Cierra todas las conexiones y devuelve la respuesta
 */
public class ClienteSocketUsuario {
    // 1. Conexion
    private String host;
    private int puerto;

    // Constructor
    public ClienteSocketUsuario(String host, int puerto) {
        this.puerto = puerto;
        this.host = host;
    }

    // Metodo principal
    // Retorna un NULL si hubo un error de conexión
    // o un objeto Respuesta si no
    public RespuestaUsuario enviarPeticion(PeticionUsuario peticion) {
        // Declarar las variables de conexion
        Socket socket = null;
        ObjectInputStream ois = null; // Recibimos
        ObjectOutputStream oos = null; // Enviamos
        RespuestaUsuario respuesta = null;

        // Conectamos
        try {
            // Conectamos con el servidor y esto
            // puede darnos un error de conexion
            // si la IP está mal o el server apagado
            socket = new Socket(host, puerto);

            // Creamos los canales
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            // Enviamos la peticion (Request)
            // para ello la escribimos en el canal
            oos.writeObject(peticion);
            oos.flush();

            // Esperamos a recibir un objeto
            // Respuesta
            respuesta = (RespuestaUsuario) ois.readObject();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }  catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            // Cerramos
            try {
                if (oos != null) oos.close();
                if (ois != null) ois.close();
                if (socket != null) socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Devolvemos la respuesta
        return respuesta;
    }

}
