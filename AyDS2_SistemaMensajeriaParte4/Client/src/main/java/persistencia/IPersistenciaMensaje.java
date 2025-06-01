package persistencia;

import java.util.ArrayList;
import java.util.List;

import modeloNegocio.Mensaje;
import modeloNegocio.Usuario;

public interface IPersistenciaMensaje {
	public void guardarMensaje(String nombre,Mensaje mensaje);
	public ArrayList<Mensaje> cargarMensaje(String nombre);
}
