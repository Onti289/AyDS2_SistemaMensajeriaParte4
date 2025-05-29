package persistencia;

import modeloNegocio.Mensaje;

public interface IPersistenciaMensaje {
	public void guardarMensaje(Mensaje mensaje);
	public Mensaje cargarMensaje();
}
