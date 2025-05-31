package persistencia;

import modeloNegocio.Mensaje;
import modeloNegocio.Usuario;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.io.*;
import java.lang.reflect.Type;

public class ConcreteMensajeJSON extends ConcreteFactoryJSON implements IPersistenciaMensaje {

	@Override
	public void guardarMensaje(String nombre,Mensaje mensaje) {
		   ArrayList<Mensaje> mensajes = cargarMensaje(nombre); // Leé los anteriores
	       mensajes.add(mensaje);
	        try (Writer writer = new FileWriter(RUTA_BASE + nombre + ".json")) {
	            gson.toJson(mensajes, writer); // Guarda todo el array
	        } catch (IOException e) {
	            e.printStackTrace();
	        }

	}

	@Override
	public List<Usuario> cargarMensaje(String nombre) {
		// TODO Auto-generated method stub
		return null;
	}


}
