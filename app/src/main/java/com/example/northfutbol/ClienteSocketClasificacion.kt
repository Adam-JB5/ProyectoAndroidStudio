package com.example.northfutbol

import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

class ClienteSocketClasificacion(private val ip: String, private val puerto: Int) {
    fun enviarPeticion(peticion: PeticionClasificacion): RespuestaClasificacion? {
        return try {
            val socket = Socket(ip, puerto)
            val out = ObjectOutputStream(socket.getOutputStream())
            out.writeObject(peticion)
            out.flush()
            val inp = ObjectInputStream(socket.getInputStream())
            val respuesta = inp.readObject() as RespuestaClasificacion
            socket.close()
            respuesta
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}