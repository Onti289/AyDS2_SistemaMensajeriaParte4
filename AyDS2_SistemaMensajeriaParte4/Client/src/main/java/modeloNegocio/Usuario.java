package modeloNegocio;

import java.io.Serializable;
import java.util.*;
import dto.*;
import persistencia.IAbstractFactoryPersistencia;
import persistencia.IPersistenciaContacto;
import persistencia.IPersistenciaMensaje;
import persistencia.SelectorDePersistencia;
import util.*;

@SuppressWarnings("serial")
public class Usuario implements Serializable {
	private String nickName;
	private String ip;
	private int puerto;
	private transient PriorityQueue<Usuario> agenda = new PriorityQueue<>(Comparator.comparing(Usuario::getNickName));
	private String tipoPersistencia;
	private transient List<Usuario> listaConversaciones = new LinkedList<>();

	private transient ArrayList<Mensaje> mensajes = new ArrayList<>();
	private transient IPersistenciaContacto contactoPersistencia;
	private transient IPersistenciaMensaje mensajePersistencia;

	// constructor para usuario main
	public Usuario(String nickName, int puerto) {
		super();
		this.nickName = nickName;
		this.ip = Util.IPLOCAL;
		this.puerto = puerto;
	}

	public String getTipoPersistencia() {
		return tipoPersistencia;
	}

	// constructor para agregar contacto
	public Usuario(String nickName, int puerto, String ip) {
		super();
		this.nickName = nickName;
		this.ip = ip;
		this.puerto = puerto;
	}

	public Usuario(String nickName) {
		super();
		this.nickName = nickName;
		this.puerto = 0;
		this.ip = null;
	}

	public Usuario(String nickName, String tipoPersistencia) {
		super();
		this.nickName = nickName;
		this.puerto = 0;
		this.ip = null;
		this.tipoPersistencia = tipoPersistencia;
		IAbstractFactoryPersistencia fabricaPersistencia = SelectorDePersistencia.getFabrica(tipoPersistencia);
		this.contactoPersistencia = fabricaPersistencia.crearPersistenciaContacto();
		this.mensajePersistencia = fabricaPersistencia.crearPersistenciaMensaje();
		System.out.println(this.mensajePersistencia + " Constructor mensaje");
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPuerto() {
		return puerto;
	}

	public void setPuerto(int puerto) {
		this.puerto = puerto;
	}

	@Override
	public String toString() {
		return String.format("%s", nickName);
	}

	public void guardarMensaje(Mensaje msg) {
		this.mensajes.add(msg);
		this.mensajePersistencia.guardarMensaje(this.nickName, msg);
	}

	public void recibirMensaje(Mensaje m) {
		this.agregarConversacion(m.getEmisor());
		this.guardarMensaje(m);
	}

	// equals y hashCode ayudan a que el metodo contains en recibir y enviar msg
	// en la lista de conver compare solo por puerto e id y no el puntero del objeto
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Usuario other = (Usuario) obj;
		return this.nickName.equalsIgnoreCase(other.nickName);
	}

	@Override
	public int hashCode() {
		return Integer.hashCode(puerto);
	}

	public ArrayList<Mensaje> getMensajes() {
		return this.mensajes;
	}

	public ArrayList<MensajeDTO> getChat(String nickname) {
		ArrayList<MensajeDTO> chat = new ArrayList<>();
		for (Mensaje m : mensajes) {
			// aca ves si sos emisor o receptor
			boolean yoSoyEmisor = m.getEmisor().equals(this);
			boolean yoSoyReceptor = m.getReceptor().equals(this);
			// aca se fija si CONTACTO que llega es el emisor o receptor
			boolean otroEsEmisor = m.getEmisor().getNickName().equalsIgnoreCase(nickname);
			boolean otroEsReceptor = m.getReceptor().getNickName().equalsIgnoreCase(nickname);
			// Ya que no pueden ambos ser emisores o receptores
			if ((yoSoyEmisor && otroEsReceptor) || (yoSoyReceptor && otroEsEmisor)) {
				String nombre = m.getEmisor().getNickName();
				UsuarioDTO emisor = new UsuarioDTO(nombre);
				nombre = m.getReceptor().getNickName();
				UsuarioDTO receptor = new UsuarioDTO(nombre);
				chat.add(new MensajeDTO(m.getContenido(), m.getFechayhora(), emisor, receptor));
			}
		}
		return chat;
	}

	public PriorityQueue<Usuario> getAgenda() {
		return new PriorityQueue<>(agenda);
	}

	public List<Usuario> getListaConversaciones() {
		return new ArrayList<>(listaConversaciones);
	}

	public void agregarConversacion(Usuario contacto) {
		if (!this.agenda.contains(contacto)) {
			agregaContacto(contacto);
		}
		if (this.listaConversaciones.isEmpty() || !this.listaConversaciones.contains(contacto)) {
			this.listaConversaciones.add(contacto);
		}
	}

	public void setAgenda(PriorityQueue<Usuario> agenda) {
		this.agenda = agenda;
	}

	public void agregaContacto(Usuario contacto) {
		System.out.println("Contacto persistencia " + this.contactoPersistencia);
		agenda.add(contacto);

		this.contactoPersistencia.guardarContacto(this.nickName, contacto.getNickName());
	}

	public void muestraContactos() {
		PriorityQueue<Usuario> agendaCopia = new PriorityQueue<>(agenda);

		while (!agendaCopia.isEmpty()) {
			Usuario contacto = agendaCopia.poll();
			System.out.println(contacto.nickName);
		}
	}

	public Usuario getBuscaContacto(String nickname) {
		PriorityQueue<Usuario> agendaCopia = new PriorityQueue<>(agenda);
		while (!agendaCopia.isEmpty()) {
			Usuario contacto = agendaCopia.poll();
			if (contacto.getNickName().equalsIgnoreCase(nickname)) {
				return contacto;
			}
		}
		return null; // No se encontro
	}

	public boolean estaContacto(int puerto) {
		PriorityQueue<Usuario> agendaCopia = new PriorityQueue<>(agenda);
		boolean esta = false;
		while (!agendaCopia.isEmpty()) {
			Usuario contacto = agendaCopia.poll();
			if (contacto.getPuerto() == puerto) {
				esta = true;
			}
		}
		return esta;
	}

	public void CargaMensajesContactos() {
		this.agenda = this.contactoPersistencia.cargarContacto(this.nickName);
		this.mensajes = this.mensajePersistencia.cargarMensaje(this.nickName);
	}
}
