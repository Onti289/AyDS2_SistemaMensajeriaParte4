package persistencia;

import java.awt.List;
import java.io.File;
import java.util.PriorityQueue;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import modeloNegocio.Usuario;

public class ConcreteContactoJSON extends ConcreteFactoryJSON implements IPersistenciaContacto {
	private static final String ARCHIVO = "contactos.json";
    private Gson gson = new Gson();
	@Override
	public void guardarContacto(String nombre,String contacto) {
	    List<String> contactos = cargarContactos();
        if (!contactos.contains(nuevoContacto)) {
            contactos.add(nuevoContacto);
            try (Writer writer = new FileWriter(ARCHIVO)) {
                gson.toJson(contactos, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

	}

	@Override
	public PriorityQueue<Usuario>  cargarContacto(String nombre) {
		File archivo = new File(ARCHIVO);
        if (!archivo.exists()) return new PriorityQueue<Usuario>();

        try (Reader reader = new FileReader(ARCHIVO)) {
            Type tipoLista = new TypeToken<List<String>>() {}.getType();
            return gson.fromJson(reader, tipoLista);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
	}

}
