package encriptacion;

public class EncriptacionXoR implements IEncriptacion {
	public EncriptacionXoR() {
		
	}
	@Override
	public String encriptar(String mensaje, String clave) {
		int longitudMensaje;
		int longitudClave;
		StringBuilder respuesta = new StringBuilder();
		longitudMensaje=mensaje.length();
		longitudClave=clave.length();
		for(int i=0;i<longitudMensaje;i++) {
			 char c = mensaje.charAt(i);
	         char k = clave.charAt(i % longitudClave);
	         respuesta.append((char)(c ^ k));
		}
		return respuesta.toString();
	}

	@Override
	public String desencriptar(String mensajeCifrado, String clave) {
		int longitudMensaje;
		int longitudClave;
		StringBuilder respuesta = new StringBuilder();
		longitudMensaje=mensajeCifrado.length();
		longitudClave=clave.length();
		for(int i=0;i<longitudMensaje;i++) {
			 char c = mensajeCifrado.charAt(i);
	         char k = clave.charAt(i % longitudClave);
	         respuesta.append((char)(c ^ k));
		}
		return respuesta.toString();
	}

}
