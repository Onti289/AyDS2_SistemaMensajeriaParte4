package encriptacion;

public interface IEncriptacion {
	public String encriptar(String mensaje, String clave);
	public String desencriptar(String mensajeCifrado, String clave);
}
