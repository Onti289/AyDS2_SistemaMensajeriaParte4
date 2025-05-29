package persistencia;

import java.util.ArrayList;

import modeloNegocio.Mensaje;

public class ConcreteMensajeXML extends ConcreteFactoryXML implements IPersistenciaMensaje {

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
