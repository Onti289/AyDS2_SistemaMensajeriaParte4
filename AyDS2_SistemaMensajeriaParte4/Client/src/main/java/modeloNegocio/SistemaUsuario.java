package modeloNegocio;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.Socket;

import java.time.LocalDateTime;
import java.util.*;

import dto.RespuestaListaMensajes;
import dto.RespuestaLista;
import dto.MensajeDTO;
import dto.UsuarioDTO;

import util.Util;

public class SistemaUsuario extends Observable {
	private Usuario usuario;
	private static SistemaUsuario sistema_instancia = null;
	private int puerto_servidor;
	private static String ip_monitor;
	private static int puerto_Monitor;
	// Socket y flujos para comunicarse con el servidor
	private Socket socketServidor;
	private ObjectOutputStream oos = null;
	private ObjectInputStream ois = null;

	private SistemaUsuario() {
		ObtienePuertoEIpMonitor();
	}

	private static void ObtienePuertoEIpMonitor() {
		String ruta = "Monitor.txt";

		try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
			String linea = br.readLine(); // Leer solo una línea

			if (linea != null) {
				String[] partes = linea.split(",");
				try {
					ip_monitor = partes[0].trim();

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					puerto_Monitor = Integer.parseInt(partes[1].trim());

				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static SistemaUsuario get_Instancia() {
		if (sistema_instancia == null)
			sistema_instancia = new SistemaUsuario();
		return sistema_instancia;
	}

	public String getnickName() {
		return this.usuario.getNickName();
	}

	public int getPuerto() {
		return this.usuario.getPuerto();
	}

	public void pedirListaUsuarios() {
		try {

			Solicitud solicitud = new Solicitud(new UsuarioDTO(this.getnickName(),this.usuario.getTipoPersistencia()), Util.SOLICITA_LISTA_USUARIO);
			oos.writeObject(solicitud);
			oos.flush();
		} catch (IOException e) {
			estableceConexion(this.usuario.getNickName());
		}
	}

	public void setUsuario(String nickname,String tipoPersistencia) {
		this.usuario = new Usuario(nickname,tipoPersistencia);
	}

	public boolean existeContactoPorNombre(PriorityQueue<Usuario> lista, String nombreBuscado) {
		for (Usuario u : lista) {
			if (u.getNickName().equalsIgnoreCase(nombreBuscado)) {
				return true;
			}
		}
		return false;
	}

	public int agregarContacto(String nickName) {
		int condicion = 1; // contacto ya agendado
		if (!existeContactoPorNombre(this.usuario.getAgenda(), nickName)) {
			this.usuario.agregaContacto(new Usuario(nickName));
			condicion = 2;
		}
		return condicion;
	}

	public PriorityQueue<Usuario> getAgenda() {
		return this.usuario.getAgenda();
	}

	public ArrayList<MensajeDTO> getChat(String nickname) {
		return usuario.getChat(nickname);
	}

	public ArrayList<Mensaje> getMensajes() {
		return usuario.getMensajes();
	}

	public void setContactoActual(String nickname) {
		Usuario contacto = usuario.getBuscaContacto(nickname);
		if (contacto != null) {
			this.usuario.agregarConversacion(contacto);
		}
	}

	public void estableceConexion(String usuario) {
		cerrarConexionAnterior();
		obtienePuertoServidor();
		if (this.puerto_servidor != -1) {

			comunicacionServidor(usuario);
		} else {
			setChanged(); // importante
			notifyObservers(Util.SIN_SERVER_DISPONIBLE);
		}

	}

	public void obtienePuertoServidor() {
		try (Socket socket = new Socket(this.ip_monitor, this.puerto_Monitor)) {
			ObjectOutputStream oosMonitor = null;
			oosMonitor = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream oisMonitor = new ObjectInputStream(socket.getInputStream());
			Solicitud soli = new Solicitud(Util.SOLICITA_PUERTO_SERVIDOR);
			oosMonitor.writeObject(soli);
			oosMonitor.flush();
			try {

				this.puerto_servidor = (int) oisMonitor.readObject();

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			oosMonitor.close();
		} catch (IOException e) {
			System.out.println("error");
		}
	}

	public void comunicacionServidor(String nombreUser) {
		try {
			socketServidor = new Socket(Util.IPLOCAL, this.puerto_servidor);
			oos = new ObjectOutputStream(socketServidor.getOutputStream());
			oos.flush();
			Solicitud sol = new Solicitud(new UsuarioDTO(nombreUser), Util.CONEXION_NUEVO_SERVER);
			oos.writeObject(sol);
			oos.flush();
			ois = new ObjectInputStream(socketServidor.getInputStream());
		
			Thread escuchaServidor = new Thread(() -> {
				try {
					while (true) {
						Object recibido = ois.readObject();
						if (recibido instanceof Mensaje) {
							Mensaje mensaje = (Mensaje) recibido;

							this.usuario.recibirMensaje(mensaje);
							setChanged(); // importante
							notifyObservers(mensaje);

						} else {// si llega aca es por que el server lo pudo registrar o loguear

							if (recibido instanceof Solicitud) {
								Solicitud solicitud = (Solicitud) recibido;

								// Si registra o loguea lo tiene que crear igual por que inicio de 0 el sistema
								// usuario
								if (solicitud.getTipoSolicitud().equalsIgnoreCase(Util.CTEREGISTRO)
										|| solicitud.getTipoSolicitud().equalsIgnoreCase(Util.CTELOGIN)) {
									setUsuario(solicitud.getNombre(),solicitud.getUsuarioDTO().getTipoPersistencia());
								}
								setChanged(); // importante
								notifyObservers(solicitud);
							} else {
								if (recibido instanceof RespuestaListaMensajes) {
									RespuestaListaMensajes respuesta = (RespuestaListaMensajes) recibido;
									List<MensajeDTO> mensajes = respuesta.getLista();

									for (MensajeDTO m : mensajes) {
										String nick = m.getEmisor().getNombre();
										int puertoaux = m.getEmisor().getPuerto();
										String ip = m.getEmisor().getIp();
										Usuario emisor = new Usuario(nick, puertoaux, ip);
										nick = m.getReceptor().getNombre();
										puertoaux = m.getReceptor().getPuerto();
										ip = m.getReceptor().getIp();
										Usuario receptor = new Usuario(nick, puertoaux, ip);
										this.usuario.recibirMensaje(
												new Mensaje(m.getContenido(), m.getFechayhora(), emisor, receptor));
									}
									setChanged(); // importante
									notifyObservers(respuesta);
								} else {
									if (recibido instanceof RespuestaLista) {
										RespuestaLista respuesta = (RespuestaLista) recibido;

										setChanged(); // importante
										notifyObservers(respuesta);
									}
								}

							}

						}
					}
				} catch (Exception e) {

					this.puerto_servidor = -1;
					estableceConexion(this.usuario.getNickName());
					// e.printStackTrace(); // conexión caída
				}
			});
			escuchaServidor.start();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void cerrarConexionAnterior() {
		try {
			if (ois != null)
				ois.close();
		} catch (IOException e) {

		}
		try {
			if (oos != null)
				oos.close();
		} catch (IOException e) {

		}
		try {
			if (socketServidor != null && !socketServidor.isClosed())
				socketServidor.close();
		} catch (IOException e) {

		}
	}

	private String getIp() {
		return this.usuario.getIp();
	}

	public Usuario buscarUsuarioPorDTO(UsuarioDTO dto) {
		for (Usuario u : usuario.getAgenda()) {
			if (u.getNickName().equalsIgnoreCase(dto.getNombre())) {
				return u;
			}
		}
		return null; // o lanzar excepcion si queres asegurarte que este
	}

	public Usuario getUsuario() {
		return this.usuario;
	}

	public void enviarMensajeServidor(UsuarioDTO contacto, String mensaje) {
		try {
			Usuario ureceptor = this.buscarUsuarioPorDTO(contacto);
			Mensaje msg;
			if (ureceptor != null) {
				msg = new Mensaje(mensaje, LocalDateTime.now(), this.usuario, ureceptor);
				oos.writeObject(msg);
				oos.flush();
				this.usuario.guardarMensaje(msg);
				setChanged(); // importante
				notifyObservers(msg);

			}

		} catch (IOException e) {
			estableceConexion(this.usuario.getNickName());
		}
	}

	public void enviaSolicitudAServidor(String nickName,String tipoPersistencia, String tipoSolicitud) {

		try {
			if (this.puerto_servidor != -1) {
				Solicitud soli = new Solicitud(new UsuarioDTO(nickName,tipoPersistencia),tipoSolicitud);
				oos.writeObject(soli);
				oos.flush();
			}

		} catch (IOException e) {
			System.out.println("error");
		}
	}

	public List<Usuario> getListaConversaciones() {
		return this.usuario.getListaConversaciones();
	}

	public String getAlias(int puerto) {
		PriorityQueue<Usuario> lista = this.usuario.getAgenda();
		while (!lista.isEmpty()) {
			Usuario contacto = lista.poll();
			if (contacto.getPuerto() == puerto) {
				return contacto.getNickName();
			}
		}
		return null;
	}

}
