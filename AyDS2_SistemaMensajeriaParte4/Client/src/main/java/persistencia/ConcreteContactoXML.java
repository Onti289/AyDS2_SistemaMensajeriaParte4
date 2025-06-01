package persistencia;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import modeloNegocio.Usuario;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.PriorityQueue;

public class ConcreteContactoXML extends ConcreteFactoryXML implements IPersistenciaContacto {

	private final XStream xstream;

	public ConcreteContactoXML() {
		this.xstream = new XStream(new DomDriver());
		xstream.allowTypes(new Class[] { Usuario.class, PriorityQueue.class }); // Para evitar errores de seguridad
	}

	@Override
	public void guardarContacto(String nombre, String contacto) {
		String filename = "ContactosDe" + nombre + ".xml";

		PriorityQueue<Usuario> contactos = cargarContacto(nombre); // Cargar los ya existentes
		if (contactos == null) {
			contactos = new PriorityQueue<>();
		}

		contactos.add(new Usuario(contacto));

		try (FileOutputStream fos = new FileOutputStream(new File(filename))) {
			xstream.toXML(contactos, fos);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public PriorityQueue<Usuario> cargarContacto(String nombre) {
		String filename = "ContactosDe" + nombre + ".xml";

		File file = new File(filename);
		if (!file.exists()) {
			return new PriorityQueue<>();
		}

		try (FileInputStream fis = new FileInputStream(file)) {
			Object obj = xstream.fromXML(fis);
			if (obj instanceof PriorityQueue<?>) {
				return (PriorityQueue<Usuario>) obj;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new PriorityQueue<>();
	}

}
