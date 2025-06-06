package persistencia;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;


import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;

import modeloNegocio.Mensaje;
import modeloNegocio.Usuario;

public class ConcreteMensajeTextoPlano extends ConcreteFactoryTextoPlano implements IPersistenciaMensaje {
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
	
	@Override
	public void guardarMensaje(String nombre,Mensaje mensaje) {
	    try (Writer writer = new FileWriter("ConversacionesDe" + nombre + ".txt", true)) {
	      if (mensaje.getEmisor().getNickName().equalsIgnoreCase(nombre))
              {
                writer.append("Envio\n");
                writer.append(mensaje.getReceptor().getNickName() + "\n");
              }
              else
              {
                writer.append("Recepcion\n");
                writer.append(mensaje.getEmisor().getNickName() + "\n");
              }
	        writer.append(mensaje.getFechayhora().format(this.formatter) + "\n");
            writer.append(mensaje.getContenido() + "\n");
	              
            }
            catch (IOException e) {
	      e.printStackTrace();
            }
	}

	@Override
	public ArrayList<Mensaje> cargarMensaje(String nombre){
	    ArrayList<Mensaje> mensajes = new ArrayList<Mensaje>();

	            try (BufferedReader br = new BufferedReader(new FileReader("ConversacionesDe" + nombre + ".txt"))) {
	                String linea;
	                while ((linea = br.readLine()) != null) {
                            Usuario emisor, receptor;
                            if (linea.equalsIgnoreCase("Envio"))
	                    {
	                        linea = br.readLine();
                              emisor = new Usuario(nombre);
                              receptor = new Usuario(linea);
	                    }
	                    else
	                    {
	                        linea = br.readLine();
	                      emisor = new Usuario(linea);
	                      receptor = new Usuario(nombre);
	                    }
	                    linea = br.readLine();
	                    LocalDateTime fechaHora = LocalDateTime.parse(linea, this.formatter);
	                    linea = br.readLine();
                        String contenido = linea;
                        mensajes.add(new Mensaje(contenido, fechaHora, emisor, receptor));
                        }
	                } catch (IOException e) {
                        }
                    return mensajes;
        
	}

}
