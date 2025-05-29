package persistencia;

import java.util.PriorityQueue;

import modeloNegocio.Usuario;

public class ConcreteContactoXML extends ConcreteFactoryXML implements IPersistenciaContacto {

	@Override
	public void guardarContacto(String nombre,String contacto) {
		

	}

	@Override
	public PriorityQueue<Usuario>  cargarContacto(String nombre) {
		
		return null;
	}

}
