package persistencia;

import java.util.ArrayList;

import modeloNegocio.Mensaje;

public interface IPersistenciaMensaje {
	public void guardarMensaje(String nombre,Mensaje mensaje);
	public ArrayList<Mensaje> cargarMensaje(String nombre);
}
