package modeloNegocio;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.net.Socket;

import java.time.LocalDateTime;
import java.util.*;

import dto.RespuestaListaMensajes;
import dto.RespuestaLista;
import dto.MensajeDTO;
import dto.UsuarioDTO;
import encriptacion.EncriptacionConfederados;
import encriptacion.EncriptacionXoR;
import encriptacion.IEncriptacion;
import util.Util;

public class SistemaUsuario extends Observable {
	private Usuario usuario;
	private static SistemaUsuario sistema_instancia = null;
	private int puerto_servidor;
	private static String ip_monitor;
	private static int puerto_Monitor;
	private String claveEncriptacion;
	private String tipoEncriptacion;
	private String tipoPersistencia;
	private IEncriptacion encriptacion;
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
/*
	public int getPuerto() {
		return this.usuario.getPuerto();
	}
*/
	public void pedirListaUsuarios() {
		try {

			Solicitud solicitud = new Solicitud(new UsuarioDTO(this.getnickName()), Util.SOLICITA_LISTA_USUARIO);
			oos.writeObject(solicitud);
			oos.flush();
		} catch (IOException e) {
			estableceConexion(this.usuario.getNickName());
		}
	}

	public void setUsuario(String nickname, String tipoPersistencia, String tipoEncriptacion) {
		this.usuario = new Usuario(nickname, tipoPersistencia, tipoEncriptacion);
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
			System.out.println("Usuario que llego a agregar contacto "+nickName);
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
	public void agregarConversacion(Usuario contacto) {
		this.usuario.agregarConversacion(contacto);
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
		try (Socket socket = new Socket(ip_monitor, puerto_Monitor)) {
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
			System.out.println("Puerto de servidor obtenido" + this.puerto_servidor);
			socketServidor = new Socket(Util.IPLOCAL, this.puerto_servidor);
			oos = new ObjectOutputStream(socketServidor.getOutputStream());
			ois = new ObjectInputStream(socketServidor.getInputStream());
			oos.flush();
			System.out.println("Llego bien nonmbre Usert" + nombreUser);
			Solicitud sol = new Solicitud(new UsuarioDTO(nombreUser), Util.CONEXION_NUEVO_SERVER);
			oos.writeObject(sol);
			oos.flush();
			try {
				Object mensajeOk = ois.readObject();
				System.out.println("Llego msj de OK " + (String) mensajeOk);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Thread escuchaServidor = new Thread(() -> {
				try {
					while (true) {
						Object recibido = ois.readObject();
						if (recibido instanceof Mensaje) {
							Mensaje mensaje = (Mensaje) recibido;
							System.out.println("Llegó mensaje encriptado: " + mensaje.getContenido());
							String mensajeDesencriptado = this.encriptacion.desencriptar(mensaje.getContenido(), this.claveEncriptacion);
							mensaje.setContenido(mensajeDesencriptado);
							System.out.println("Mensaje desencriptado: " + mensaje.getContenido());
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
									if (solicitud.getTipoSolicitud().equalsIgnoreCase(Util.CTELOGIN)) {
										cargaClaveTipoEncriptacionyPersistencia(solicitud.getNombre());
									}
									else{
										guardaEncriptacionyPersistenciaEnArchivo(solicitud.getNombre());
									}
									setUsuario(solicitud.getNombre(), this.tipoPersistencia,
											this.tipoEncriptacion);									
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
										String mensajeDesencriptado = this.encriptacion.desencriptar(m.getContenido(), this.claveEncriptacion);
										this.usuario.recibirMensaje(
												new Mensaje(mensajeDesencriptado, m.getFechayhora(), emisor, receptor));
										if(m.getEmisor().getNombre().equalsIgnoreCase(getnickName())) {			
											agregarConversacion(new Usuario(m.getReceptor().getNombre()));
										}
										else {
											agregarConversacion(new Usuario(m.getEmisor().getNombre()));
										}	
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
					e.printStackTrace(); // conexión caída
					this.puerto_servidor = -1;
					estableceConexion(nombreUser);

				}
			});
			escuchaServidor.start();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void guardaEncriptacionyPersistenciaEnArchivo(String nickname) {
		try (Writer cargaEncriptacion = new FileWriter("configuracionDe" +nickname + ".txt")) {
			cargaEncriptacion.append(this.tipoEncriptacion+"\n");
			cargaEncriptacion.append(this.claveEncriptacion+"\n");
			cargaEncriptacion.append(this.tipoPersistencia);
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
				String mensajeEncriptado = this.encriptacion.encriptar(mensaje, this.claveEncriptacion);
				msg = new Mensaje(mensajeEncriptado, LocalDateTime.now(), this.usuario, ureceptor);
				oos.writeObject(msg);
				oos.flush();
				System.out.println("Envía mensaje encriptado: " + msg.getContenido());
				msg.setContenido(mensaje);
				System.out.println("Mensaje sin encriptar: " + msg.getContenido());
				this.usuario.guardarMensaje(msg);
				setChanged(); // importante
				notifyObservers(msg);
			}
		} catch (IOException e) {
			estableceConexion(this.usuario.getNickName());
		}
	}

	public void enviaSolicitudAServidor(String nickName, String tipoPersistencia, String tipoEncriptacion,
			String claveEncriptacion,String tipoSolicitud) {

		try {
			if (this.puerto_servidor != -1) {
				if(tipoSolicitud.equalsIgnoreCase(Util.CTEREGISTRAR)) {
					this.claveEncriptacion=claveEncriptacion;
					this.tipoEncriptacion=tipoEncriptacion;
					this.guardaTipoEncriptacion(tipoEncriptacion);
					this.tipoPersistencia=tipoPersistencia;	
				}
				Solicitud soli = new Solicitud(new UsuarioDTO(nickName),
						tipoSolicitud);
				oos.writeObject(soli);
				oos.flush();
			}

		} catch (IOException e) {
			System.err.println("error");
			e.printStackTrace();
		}
	}

	public List<Usuario> getListaConversaciones() {
		return this.usuario.getListaConversaciones();
	}
	
	public void cargaClaveTipoEncriptacionyPersistencia(String nickname) {
        try (BufferedReader br = new BufferedReader(new FileReader("configuracionDe" +nickname + ".txt"))) {
            String linea;
            linea = br.readLine();
            this.guardaTipoEncriptacion(linea);
            linea = br.readLine();
            this.claveEncriptacion=linea;
            linea = br.readLine();
            this.tipoPersistencia=linea;
            
        } catch (IOException e) {
        	e.printStackTrace();
        }
	}
	
	private void guardaTipoEncriptacion(String tipo) {
		switch (tipo) {
		case Util.XOR:
			this.encriptacion = new EncriptacionXoR();
			break;
		case Util.CONFEDERADOS:
			this.encriptacion = new EncriptacionConfederados();
			break;
		default:
			break;
		}
	}
/*
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
*/
}
