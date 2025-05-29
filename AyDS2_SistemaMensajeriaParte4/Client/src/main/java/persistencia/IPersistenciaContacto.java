package persistencia;

import java.util.PriorityQueue;

import modeloNegocio.Usuario;

public interface IPersistenciaContacto {
	public void guardarContacto(String nombre,String contacto);
	public PriorityQueue<Usuario> cargarContacto(String nombre);
}
