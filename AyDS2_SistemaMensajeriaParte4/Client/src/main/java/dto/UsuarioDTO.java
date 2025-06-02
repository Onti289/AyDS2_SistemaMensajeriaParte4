package dto;

import java.io.Serializable;
import java.util.Objects;

public class UsuarioDTO implements Serializable {
    private String nombre;
    private int puerto;
    private String ip;
    private String tipoPersistencia;
    private String tipoEncriptacion;
    public UsuarioDTO(String nombre) {
        this.nombre = nombre;
        this.puerto = 0;
        this.ip = null;
    }
    public UsuarioDTO(String nombre,String tipoPersistencia, String tipoEncriptacion) {
        this.nombre = nombre;
        this.puerto = 0;
        this.ip = null;
        this.tipoPersistencia=tipoPersistencia;
        this.tipoEncriptacion=tipoEncriptacion;
    }
    
	public UsuarioDTO(String nombre, String tipoPersistencia) {
		this.nombre = nombre;
        this.puerto = 0;
        this.ip = null;
        this.tipoPersistencia=tipoPersistencia;
	}
	public void setTipoEncriptacion(String tipoEncriptacion) {
		this.tipoEncriptacion = tipoEncriptacion;
	}
	
	public String getTipoEncriptacion() {
		return tipoEncriptacion;
	}
	public void setTipoPersistencia(String tipoPersistencia) {
		this.tipoPersistencia = tipoPersistencia;
	}
	public String getTipoPersistencia() {
		return tipoPersistencia;
	}

	public String getNombre() {
        return nombre;
    }

    public String getIp() {
        return ip;
    }

    public int getPuerto() {
        return puerto;
    }

    @Override
    public String toString() {
        return nombre;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        UsuarioDTO other = (UsuarioDTO) obj;
        return nombre.equalsIgnoreCase(other.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, puerto);
    }

}