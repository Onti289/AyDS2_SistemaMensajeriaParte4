package persistencia;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.PriorityQueue;

import modeloNegocio.Usuario;

public class ConcreteContactoTextoPlano extends ConcreteFactoryTextoPlano implements IPersistenciaContacto {

	@Override
	public void guardarContacto(String nombre,String contacto) {
		String archivo = "contactosDe" + nombre + ".txt";
		PriorityQueue<Usuario> contactos = cargarContacto(nombre);
		boolean existe = contactos.stream().anyMatch(u -> u.getNickName().equals(contacto));
		if (!existe) {
			contactos.add(new Usuario(contacto));
			// Guardar de nuevo todo el listado
			try (Writer writer = new FileWriter(archivo)) {
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public PriorityQueue<Usuario>  cargarContacto(String nombre) {
		// TODO Auto-generated method stub
		return null;
	}

}
