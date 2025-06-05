package persistencia;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.PriorityQueue;

import modeloNegocio.Mensaje;
import modeloNegocio.Usuario;

public class ConcreteContactoTextoPlano extends ConcreteFactoryTextoPlano implements IPersistenciaContacto {

	@Override
	public void guardarContacto(String nombre,String contacto) {
		String archivo = "contactosDe" + nombre + ".txt";
			try (Writer writer = new FileWriter(archivo, true)) {
			    writer.append(contacto + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	@Override
	public PriorityQueue<Usuario>  cargarContacto(String nombre) {
		PriorityQueue<Usuario> contactos = new PriorityQueue<Usuario>();

		        try (BufferedReader br = new BufferedReader(new FileReader("contactosDe" + nombre + ".txt"))) {
		            String linea;
		            while ((linea = br.readLine()) != null) {
		                contactos.add(new Usuario(linea));
		            }
		            } catch (IOException e) {
		            }
		        return contactos;
	}

}
