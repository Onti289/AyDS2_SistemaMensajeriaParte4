package persistencia;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import modeloNegocio.Mensaje;

import java.io.Writer;
import java.io.FileWriter;
import java.io.Reader;
import java.io.FileReader;
import java.util.ArrayList;
import java.io.IOException;
import java.lang.reflect.Type;


public class ConcreteMensajeJSON extends ConcreteFactoryJSON implements IPersistenciaMensaje {
	private final Gson gson = new Gson();

	@Override
	public void guardarMensaje(String nombre,Mensaje mensaje) {
		   ArrayList<Mensaje> mensajes = cargarMensaje(nombre); // Leé los anteriores
	       mensajes.add(mensaje);
	        try (Writer writer = new FileWriter("ConversacionesDe" + nombre + ".json")) {
	            gson.toJson(mensajes, writer); // Guarda todo el array
	        } catch (IOException e) {
	            e.printStackTrace();
	        }

	}

	@Override
	public ArrayList<Mensaje> cargarMensaje(String nombre) {
		   try (Reader reader = new FileReader("ConversacionesDe" + nombre + ".json")) {
	            Type listType = new TypeToken<ArrayList<Mensaje>>(){}.getType();
	            return gson.fromJson(reader, listType);
	        } catch (IOException e) {
	            // Si no existe el archivo, devolvé lista vacía
	            return new ArrayList<>();
	        }
	}



}
