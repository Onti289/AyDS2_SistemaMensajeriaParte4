package persistencia;
import java.io.Writer;
import java.io.FileWriter;
import java.io.Reader;
import java.io.FileReader;

import java.util.List;
import java.lang.reflect.Type;
import java.io.File;
import java.io.IOException;
import java.util.PriorityQueue;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import modeloNegocio.Usuario;
import java.util.ArrayList;

public class ConcreteContactoJSON extends ConcreteFactoryJSON implements IPersistenciaContacto {
    private Gson gson = new Gson();
	@Override
	public void guardarContacto(String nombre,String contacto) {
		String archivo="contactosDe"+nombre+".json";
		PriorityQueue<Usuario> contactos = cargarContacto(nombre);
		  boolean existe = contactos.stream().anyMatch(u -> u.getNickName().equals(contacto));
	        if (!existe) {
	            contactos.add(new Usuario(contacto));

	            // Guardar de nuevo todo el listado
	            try (Writer writer = new FileWriter(archivo)) {
	                gson.toJson(new ArrayList<>(contactos), writer);  // Gson no serializa bien PriorityQueue directamente
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }

	}

	@Override
	public PriorityQueue<Usuario>  cargarContacto(String nombre) {
		String nombreArchivo="contactosDe"+nombre+".json";
		File archivo = new File(nombreArchivo);
        if (!archivo.exists()) return new PriorityQueue<Usuario>();

        try (Reader reader = new FileReader(archivo)) {
        	 Type tipoLista = new TypeToken<List<Usuario>>() {}.getType();
             List<Usuario> lista = gson.fromJson(reader, tipoLista);
             return new PriorityQueue<>(lista);
        } catch (IOException e) {
            e.printStackTrace();
            return new PriorityQueue<>();
        }
	}

}
