package persistencia;

import java.util.ArrayList;

import modeloNegocio.Mensaje;

public class ConcreteMensajeJSON extends ConcreteFactoryJSON implements IPersistenciaMensaje {

	@Override
	public void guardarMensaje(String nombre,Mensaje mensaje) {
		// TODO Auto-generated method stub

	}

	@Override
	public ArrayList<Mensaje> cargarMensaje(String nombre) {
		// TODO Auto-generated method stub
		return null;
	}

}
