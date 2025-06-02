package persistencia;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import modeloNegocio.Mensaje;

public class ConcreteMensajeTextoPlano extends ConcreteFactoryTextoPlano implements IPersistenciaMensaje {

	@Override
	public void guardarMensaje(String nombre,Mensaje mensaje) {
		ArrayList<Mensaje> mensajes = cargarMensaje(nombre); // Lee los anteriores
	    mensajes.add(mensaje);
	    try (Writer writer = new FileWriter("ConversacionesDe" + nombre + ".txt")) {
	            
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	}

	@Override
	public ArrayList<Mensaje> cargarMensaje(String nombre) {
		// TODO Auto-generated method stub
		return null;
	}

}
