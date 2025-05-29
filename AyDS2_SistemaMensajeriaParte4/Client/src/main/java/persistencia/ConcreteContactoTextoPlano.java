package persistencia;

import java.util.PriorityQueue;

import modeloNegocio.Usuario;

public class ConcreteContactoTextoPlano extends ConcreteFactoryTextoPlano implements IPersistenciaContacto {

	@Override
	public void guardarContacto(String nombre,String contacto) {
		// TODO Auto-generated method stub

	}

	@Override
	public PriorityQueue<Usuario>  cargarContacto(String nombre) {
		// TODO Auto-generated method stub
		return null;
	}

}
