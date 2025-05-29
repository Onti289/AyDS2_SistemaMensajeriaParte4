package modeloNegocio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDateTime;

import dto.*;

import util.Util;

public class SistemaServidor {

	private ArrayList<Usuario> listaUsuarios = new ArrayList<Usuario>();

	private ArrayList<Usuario> listaConectados = new ArrayList<Usuario>();
	private static SistemaServidor servidor_instancia = null;
	private ArrayList<Mensaje> mensajesPendientes = new ArrayList<Mensaje>();
	private HashMap<String, ConexionUsuario> conexionesUsuarios = new HashMap<>();
	private String ip;
	private int puerto;
	private boolean principal = false;
	private ArrayList<ServidorDTO> listaServidores = new ArrayList<ServidorDTO>();

	private Thread heartbeatThread;
	private volatile boolean heartbeatActivo = true;
	private Socket socketMonitor;
	private ObjectOutputStream oosMonitor;
	private ObjectInputStream oisMonitor;
	private volatile boolean servidorActivo = true;
	private ServerSocket serverSocket; // <-- lo haremos accesible
	private Thread serverThread;
	private HashMap<String, ConexionUsuario> conexiones = new HashMap<>();
	private HashMap<String, ConexionServidor> conexionesServidores = new HashMap<>();
	private static String ip_monitor;
	private static int puerto_Monitor;

	private SistemaServidor() {
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

	public static SistemaServidor get_Instancia() {
		if (servidor_instancia == null)
			servidor_instancia = new SistemaServidor();
		return servidor_instancia;
	}

	private void enviaRespuestaUsuario(Solicitud usuario, ObjectOutputStream oos) {
		try {
			oos.writeObject(usuario);
			oos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void iniciaServidor(int puerto) {
		servidorActivo = true;

		serverThread = new Thread(() -> {
			try {
				this.serverSocket = new ServerSocket(puerto);
				while (servidorActivo) {
					Socket clienteSocket = serverSocket.accept();
					Thread clienteHandler = new Thread(() -> {
						try {
							ObjectOutputStream oos = new ObjectOutputStream(clienteSocket.getOutputStream());
							oos.flush(); // importante para evitar bloqueos
							ObjectInputStream ois = new ObjectInputStream(clienteSocket.getInputStream());

							while (true) {
								Object recibido = ois.readObject();

								if (recibido instanceof Solicitud) {
									Solicitud solicitud = (Solicitud) recibido;

									switch (solicitud.getTipoSolicitud()) {
									case Util.SOLICITA_LISTA_USUARIO:
										retornaLista(oos, this.listaUsuarios, false);
										break;

									case Util.CTEREGISTRAR:
										UsuarioDTO usuarioReg = solicitud.getUsuarioDTO();
										if (registrarUsuario(usuarioReg)) {
											solicitud.setTipoSolicitud(Util.CTEREGISTRO);
											/*String clave = usuarioReg.getNombre();
											ConexionUsuario conexion = new ConexionUsuario(usuarioReg, oos, ois,
													clienteSocket);
											conexionesUsuarios.put(clave, conexion);*/
											principalSincronizaSecundario(solicitud);
										} else {
											solicitud.setTipoSolicitud(Util.CTEUSUARIOLOGUEADO);
										}
										enviaRespuestaUsuario(solicitud, oos);

										break;

									case Util.CTELOGIN:
										UsuarioDTO usuarioLogin = solicitud.getUsuarioDTO();
										int tipo = loginUsuario(usuarioLogin);

										if (tipo == 1) {
											solicitud.setTipoSolicitud(Util.CTELOGIN);
											/*String clave = usuarioLogin.getNombre();
											ConexionUsuario conexion = new ConexionUsuario(usuarioLogin, oos, ois,
													clienteSocket);
											conexionesUsuarios.put(clave, conexion);*/
											this.listaConectados.add(new Usuario(usuarioLogin.getNombre()));

											principalSincronizaSecundario(solicitud);
										} else if (tipo == 2) {
											solicitud.setTipoSolicitud(Util.CTEUSUARIOLOGUEADO);
										} else {
											solicitud.setTipoSolicitud(Util.CTEUSUERINEXISTENTE);
										}
										enviaRespuestaUsuario(solicitud, oos);
										break;

									case Util.CTEDESCONEXION:
										quitarUsuarioDesconectado(solicitud.getNombre());
										principalSincronizaSecundario(solicitud);
										break;

									case Util.CTESOLICITARMENSAJES:

										UsuarioDTO usuario = solicitud.getUsuarioDTO();
										retornaListaMensajesPendientes(entregarMensajesPendientes(usuario.getNombre()),
												oos);
										break;
									case Util.SOS_PRINCIPAL:
										this.principal = true;
										break;
									case Util.CONEXION_NUEVO_SERVER:
										String clave = solicitud.getUsuarioDTO().getNombre();
										ConexionUsuario conexion = new ConexionUsuario(solicitud.getUsuarioDTO(), oos,
												ois, clienteSocket);
										conexionesUsuarios.put(clave, conexion);
										oos.writeObject(Util.CONEXION_NUEVO_SERVER);
										break;
									default:
										break;
									}
								} else if (recibido instanceof Mensaje) {
									Mensaje mensaje = (Mensaje) recibido;

									enviarMensaje(mensaje);
								} else if (recibido instanceof ServidorDTO) {// RESINCRONIZA el serverdto que le llega
																				// es del secundario
									ServidorDTO servidorDTO = (ServidorDTO) recibido;
									ConexionServidor conexion = new ConexionServidor(servidorDTO, oos, ois,
											clienteSocket);
									this.conexionesServidores
											.put(generarClave(servidorDTO.getIp(), servidorDTO.getPuerto()), conexion);
									this.retornaLista(oos, this.listaUsuarios, false); // lista de usuarios
									this.retornaLista(oos, this.listaConectados, true); // lista de usuarios conectados
									this.retornaListaMensajesAServer(oos);
									oos.writeObject(Util.FIN_RESINCRONIZACION);
									oos.flush();
								}
							}

						} catch (Exception e) {
							try {

								quitarUsuarioDesconectado(eliminaConexion(clienteSocket));
								clienteSocket.close();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								// e1.printStackTrace();
							}
							// e.printStackTrace(); // o cerrar recursos si se desea
						}
					});
					clienteHandler.start();
				}
			} catch (IOException e) {
				System.out.println("-555");
				detenerServidor();
				System.err.println("Error al iniciar el servidor: " + e.getMessage());
			}
		});
		serverThread.start();
	}

	private String generarClave(String ip, int puerto) {
		return ip + ":" + puerto;
	}

	private void principalSincronizaSecundario(Solicitud solicitud) { // ESTE METODO LO USA SERVER PRINCIPAL PARA MANDAR
																		// LOS OBJETOS NUEVOS

		for (ConexionServidor conexion : this.conexionesServidores.values()) {
			try {
				conexion.getOos().writeObject(solicitud);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				conexion.getOos().flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void principalSincronizaSecundarioMensaje(Mensaje mensaje) { // ESTE METODO LO USA SERVER PRINCIPAL PARA
																			// MANDAR LOS OBJETOS NUEVOS

		for (ConexionServidor conexion : this.conexionesServidores.values()) {
			try {
				conexion.getOos().writeObject(mensaje);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				conexion.getOos().flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void retornaListaMensajesAServer(ObjectOutputStream oos) {

		try {
			oos.writeObject(mensajesPendientesDTO());
			oos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private RespuestaListaMensajes mensajesPendientesDTO() {

		List<MensajeDTO> mensajesPendientesDTO = new ArrayList<MensajeDTO>();
		for (Mensaje m : this.mensajesPendientes) {
			UsuarioDTO emisor = new UsuarioDTO(m.getEmisor().getNickName());
			UsuarioDTO receptor = new UsuarioDTO(m.getReceptor().getNickName());
			mensajesPendientesDTO.add(new MensajeDTO(m.getContenido(), m.getFechayhora(), emisor, receptor));
		}

		return new RespuestaListaMensajes(mensajesPendientesDTO);
	}

	private void resincronizar(ServidorDTO servidor) { // ESTE METODO SOLO DEBE SER LLAMADO POR SERVIDOR SECUNDARIO
		try {
			System.out.println("Llego A resincronizar");
			System.out.println(servidor.toString());
			Socket socketPrincipal = new Socket(servidor.getIp(), servidor.getPuerto());
			ObjectOutputStream oos = new ObjectOutputStream(socketPrincipal.getOutputStream());
			oos.flush();
			ObjectInputStream ois = new ObjectInputStream(socketPrincipal.getInputStream());
			oos.writeObject(new ServidorDTO(this.puerto, this.ip));
			oos.flush(); // importante para evitar bloqueos
			try {
				boolean condicion = true;
				while (condicion) {
					Object recibido = ois.readObject();
					if (recibido instanceof RespuestaLista) {
						RespuestaLista respuesta = (RespuestaLista) recibido;
						ArrayList<UsuarioDTO> listaDTO = (ArrayList<UsuarioDTO>) respuesta.getLista();
						if (respuesta.isConectado()) {
							for (UsuarioDTO usuarioDTO : listaDTO) {
								this.listaConectados.add(new Usuario(usuarioDTO.getNombre()));
							}
						} else {
							for (UsuarioDTO usuarioDTO : listaDTO) {
								this.listaUsuarios.add(new Usuario(usuarioDTO.getNombre()));
							}
						}
					} else {
						if (recibido instanceof RespuestaListaMensajes) {
							System.out.println("103");
							ArrayList<MensajeDTO> listaDTO = (ArrayList<MensajeDTO>) ((RespuestaListaMensajes) recibido)
									.getLista();
							for (MensajeDTO msjDTO : listaDTO) {
								Usuario emisor = new Usuario(msjDTO.getEmisor().getNombre());
								Usuario receptor = new Usuario(msjDTO.getReceptor().getNombre());
								this.mensajesPendientes.add(
										new Mensaje(msjDTO.getContenido(), msjDTO.getFechayhora(), emisor, receptor));
							}
						} else {
							if (recibido instanceof String) {
								String respuesta = (String) recibido;
								if (respuesta.equalsIgnoreCase(Util.FIN_RESINCRONIZACION)) {
									condicion = false;
								}
							}

						}
					}
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				System.out.println("104");
				e.printStackTrace();
			}
			sincronizacionServerSecundario(oos, ois, socketPrincipal);
			System.out.println("10011");
		} catch (IOException e) {
			System.out.println("10020");
			e.printStackTrace();
		}

	}

	// Este metodo se establece luego de resincronizar y va recibiendo los
	// msj/usuario una vez que este como secundario
	private void sincronizacionServerSecundario(ObjectOutputStream oos, ObjectInputStream ois, Socket socketPrincipal) {
		Thread serverThread = new Thread(() -> {
			System.out.println("Puerto secu en sinc secu" + this.puerto);
			System.out.println("10011");
			while (!socketPrincipal.isClosed() && !this.principal) {

				try {

					Object recibido = ois.readObject();
					System.out.println("1004");
					if (recibido instanceof Solicitud) {
						System.out.println("1005");
						Solicitud respuesta = (Solicitud) recibido;
						if (respuesta.getTipoSolicitud().equalsIgnoreCase(Util.CTEREGISTRO)) {
							System.out.println("1006");
							UsuarioDTO usuarioReg = respuesta.getUsuarioDTO();
							registrarUsuario(usuarioReg);
						} else {
							if (respuesta.getTipoSolicitud().equalsIgnoreCase(Util.CTELOGIN)) {
								System.out.println("1007");
								this.listaConectados.add(new Usuario(respuesta.getNombre()));
							} else {
								if (respuesta.getTipoSolicitud().equalsIgnoreCase(Util.CTEDESCONEXION)) {
									this.quitarUsuarioDesconectadoSecundario(respuesta.getNombre());
								}
							}

						}
					} else {
						if (recibido instanceof Mensaje) {
							Mensaje mensaje = (Mensaje) recibido;
							mensajesPendientes.add(mensaje);
						} else {
							if (recibido instanceof MensajeDTO) {
								MensajeDTO mensajeDTO = (MensajeDTO) recibido;

								int i = 0;
								while (i < this.mensajesPendientes.size()) {
									Mensaje m = this.mensajesPendientes.get(i);
									boolean condicionReceptor = mensajeDTO.getReceptor().getNombre()
											.equalsIgnoreCase(m.getReceptor().getNickName());
									boolean condicionEmisor = mensajeDTO.getEmisor().getNombre()
											.equalsIgnoreCase(m.getEmisor().getNickName());
									if (condicionReceptor && condicionEmisor) {
										this.mensajesPendientes.remove(i);
									} else {
										i++;
									}
								}
							}
						}

					}
				} catch (ClassNotFoundException e) {
					System.out.println("1001");
					e.printStackTrace();
				} catch (IOException e) {
					System.out.println("1002");
					try {
						ois.close();
						oos.close();

					} catch (IOException error) {
						// TODO Auto-generated catch block
						error.printStackTrace();
					}
					// e.printStackTrace();
				}

			}

		});
		serverThread.start();

	}

	private String eliminaConexion(Socket socketDesconectado) {
		for (Map.Entry<String, ConexionUsuario> entry : conexionesUsuarios.entrySet()) {
			Socket socketGuardado = entry.getValue().getSocket();

			// Comparamos por instancia o por equals (ambos deberían funcionar bien con
			// Socket)
			if (socketGuardado == socketDesconectado) {
				String nombre = entry.getKey();

				// Cerramos conexión de manera segura
				entry.getValue().cerrar();

				// Removemos del mapa
				conexionesUsuarios.remove(nombre);

				return nombre;
			}
		}
		return null; // No se encontró el socket
	}

	public boolean puertoDisponible(int puerto) {
		try (ServerSocket socket = new ServerSocket(puerto)) {
			return true; // El puerto esta disponible
		} catch (IOException e) {
			return false; // El puerto ya esta en uso
		}
	}

	private List<MensajeDTO> entregarMensajesPendientes(String nombre) {
		int i = 0;
		Mensaje m;
		MensajeDTO mdto;
		UsuarioDTO ureceptor;
		UsuarioDTO uemisor;
		List<MensajeDTO> listamsj = new ArrayList<MensajeDTO>();
		while (i < this.mensajesPendientes.size()) {
			m = this.mensajesPendientes.get(i);
			String nombreuser = m.getEmisor().getNickName();
			uemisor = new UsuarioDTO(nombreuser);
			nombreuser = m.getReceptor().getNickName();
			ureceptor = new UsuarioDTO(nombreuser);
			if (ureceptor.getNombre().equalsIgnoreCase(nombre)) {
				mdto = new MensajeDTO(m.getContenido(), m.getFechayhora(), uemisor, ureceptor);
				listamsj.add(mdto);
				this.mensajesPendientes.remove(i);
				this.borraMensajeEnSecundario(mdto);
			} else {
				i++;
			}
		}
		return listamsj;
	}

	private void borraMensajeEnSecundario(MensajeDTO mensajeDTO) {
		for (ConexionServidor conexion : this.conexionesServidores.values()) {
			try {
				conexion.getOos().writeObject(mensajeDTO);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				conexion.getOos().flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void enviarMensaje(Mensaje mensaje) {
		ObjectOutputStream oosReceptor;
		System.out.println("DAtos de conexion");
		ConexionUsuario conexionUsuario = null;

		for (Map.Entry<String, ConexionUsuario> entry : conexionesUsuarios.entrySet()) {
			String clave = entry.getKey();
			System.out.println("clavee " + clave);
			ConexionUsuario conexion = entry.getValue();
			if (clave.equalsIgnoreCase(mensaje.getReceptor().getNickName())) {
				conexionUsuario = conexion;
			}
		}
		if (conexionUsuario != null) {
			oosReceptor = conexionUsuario.getOos();
			try {
				oosReceptor.writeObject(mensaje);
				oosReceptor.flush();
			} catch (IOException e) {

				principalSincronizaSecundarioMensaje(mensaje);
				mensajesPendientes.add(mensaje);
			}
		} else {

			principalSincronizaSecundarioMensaje(mensaje);
			mensajesPendientes.add(mensaje);
		}
	}

	private void quitarUsuarioDesconectado(String nombre) {
		int nro = 0;
		while (nro < this.listaConectados.size()) {
			Usuario u = this.listaConectados.get(nro);
			if (u.getNickName().equalsIgnoreCase(nombre)) {
				this.listaConectados.remove(nro);
				conexionesUsuarios.remove(nombre);
				break; // Usuario encontrado y eliminado, salimos del bucle
			}
			nro++;
		}
	}

	private void quitarUsuarioDesconectadoSecundario(String nombre) { // MOMENTANEAMENTE USAMOS ESTO
		int nro = 0;
		while (nro < this.listaConectados.size()) {
			Usuario u = this.listaConectados.get(nro);
			if (u.getNickName().equalsIgnoreCase(nombre)) {
				this.listaConectados.remove(nro);

				break; // Usuario encontrado y eliminado, salimos del bucle
			}
			nro++;
		}
	}

	private void retornaListaMensajesPendientes(List<MensajeDTO> msjPendientes, ObjectOutputStream oos) {
		try {
			RespuestaListaMensajes listaRespuesta = new RespuestaListaMensajes(msjPendientes);
			oos.writeObject(listaRespuesta);
			oos.flush();
			// oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void retornaLista(ObjectOutputStream oos, ArrayList<Usuario> lista, boolean conectado) {
		try {
			RespuestaLista listarespuesta = new RespuestaLista(obtenerListaUsuariosDTO(lista), conectado);
			oos.writeObject(listarespuesta);
			oos.flush();
			// oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private List<UsuarioDTO> obtenerListaUsuariosDTO(ArrayList<Usuario> lista) {
		List<UsuarioDTO> listaDTO = new ArrayList<>();
		for (Usuario u : lista) {
			listaDTO.add(new UsuarioDTO(u.getNickName()));
		}
		return listaDTO;
	}

	public boolean existeUsuarioPorNombre(String nombreBuscado) {
		for (Usuario u : listaUsuarios) {
			if (u.getNickName().equalsIgnoreCase(nombreBuscado)) {
				return true;
			}
		}
		return false;
	}

	public boolean registrarUsuario(UsuarioDTO usuariodto) {
		boolean registro = true;
		if (!existeUsuarioPorNombre(usuariodto.getNombre())) {
			Usuario usuario = new Usuario(usuariodto.getNombre());
			this.listaUsuarios.add(usuario);
			this.listaConectados.add(usuario);
		} else {
			registro = false;
		}
		return registro;
	}

	public boolean estaConectado(String nombre) {
		for (Usuario u : this.listaConectados) {
			if (u.getNickName().equalsIgnoreCase(nombre)) {
				return true;
			}
		}
		return false;
	}

	public int loginUsuario(UsuarioDTO usuario) {
		int tipo = 1;// usuario existente pero no conectado
		String nombre = usuario.getNombre();
		if (this.existeUsuarioPorNombre(nombre)) {
			if (this.estaConectado(nombre)) {
				tipo = 2;
				// usuario existe pero esta logueado
			}
		} else {// usuarioInexistente
			tipo = 3;
		}
		return tipo;
	}

	public void registraServidor(String ip, int puerto) {
		// obtener ip y puerto de monitor desde archivo

		try {
			this.socketMonitor = new Socket(this.ip_monitor, this.puerto_Monitor);
			this.oosMonitor = new ObjectOutputStream(this.socketMonitor.getOutputStream());
			this.oosMonitor.flush();
			this.oisMonitor = new ObjectInputStream(this.socketMonitor.getInputStream());
			this.puerto = puerto;
			this.ip = ip;
			ServidorDTO servidor = new ServidorDTO(puerto, ip);

			oosMonitor.writeObject(servidor);
			oosMonitor.flush();
			try {
				Object recibido = oisMonitor.readObject();
				if (recibido instanceof String) {
					this.principal = true; // Si llega aca es principal
				} else if (recibido instanceof ServidorDTO) {

					this.principal = false;
					resincronizar(((ServidorDTO) recibido));
				}
			} catch (ClassNotFoundException e) {

				e.printStackTrace();
			}
			this.iniciaServidor(puerto);
			Heartbeat();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void Heartbeat() {
		heartbeatThread = new Thread(() -> {
			while (heartbeatActivo) {
				try {
					ServidorDTO servidor = new ServidorDTO(this.puerto, this.ip);
					oosMonitor.writeObject(servidor);
					oosMonitor.flush();
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println("Error en Heartbeat: " + e.getMessage());
				}

				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		});
		heartbeatThread.start();
	}

	public void detenerHeartbeat() {
		heartbeatActivo = false;
		try {

			if (oisMonitor != null)
				oisMonitor.close();
			if (oosMonitor != null)
				oosMonitor.close();
			if (socketMonitor != null)
				socketMonitor.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void detenerServidor() {
		servidorActivo = false;

		try {
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close(); // Esto hará que serverSocket.accept() lance una excepción y termine el bucle
			}
			if (serverThread != null && serverThread.isAlive()) {
				serverThread.join(); // Esperamos a que el hilo termine
			}
			detenerHeartbeat();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

	}

}
