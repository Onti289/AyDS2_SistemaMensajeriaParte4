package persistencia;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import modeloNegocio.Mensaje;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ConcreteMensajeXML extends ConcreteFactoryXML implements IPersistenciaMensaje {
	//private final String carpeta = "Mensajes De";

	public ConcreteMensajeXML() {
		// Crea la carpeta si no existe
		/*File dir = new File(carpeta);
		if (!dir.exists()) {
			dir.mkdir();
		}*/
	}

	@Override
	public void guardarMensaje(String nombre, Mensaje mensaje) {
		 ArrayList<Mensaje> mensajes = cargarMensaje(nombre); // Carga los existentes
	        mensajes.add(mensaje); // Agrega el nuevo

	        XStream xstream = new XStream(new StaxDriver());
	        xstream.allowTypes(new Class[] { Mensaje.class, ArrayList.class });

	        try (FileWriter writer = new FileWriter("ConversacionesDe"+ nombre + ".xml")) {
	            xstream.toXML(mensajes, writer);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }

	}

	@Override
	public ArrayList<Mensaje> cargarMensaje(String nombre) {
		File archivo = new File("ConversacionesDe"+ nombre + ".xml");
        if (!archivo.exists()) {
            return new ArrayList<>(); // Si no hay archivo, retorna lista vacía
        }

        XStream xstream = new XStream(new StaxDriver());
        xstream.allowTypes(new Class[] { Mensaje.class, ArrayList.class });

        try (FileReader reader = new FileReader(archivo)) {
            return (ArrayList<Mensaje>) xstream.fromXML(reader);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
	}

}
