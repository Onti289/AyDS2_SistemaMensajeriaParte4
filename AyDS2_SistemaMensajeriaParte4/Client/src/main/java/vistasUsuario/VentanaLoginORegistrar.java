package vistasUsuario;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import controlador.*;
import util.Util;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;

public class VentanaLoginORegistrar extends JFrame implements IVistaUsuario, ActionListener, KeyListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private ControladorUsuario controlador;
	private JTextField textFieldUsuario;
	private JButton boton;
	private JComboBox<String> comboPersistencia;
	private JLabel labelPersistencia;
	private JComboBox<String> comboEncriptacion;
	private JLabel labelEncriptacion;
	private JTextField textFieldClaveEncriptacion;
	private JLabel labelClaveEncriptacion;

	/**
	 * Launch the application.
	 */

	/**
	 * Create the frame.
	 */
	public VentanaLoginORegistrar(ControladorUsuario controlador, String titulo, String nombreBoton, String nombreAccion) {
	    this.controlador = controlador;
	    setTitle(titulo);
	    setResizable(false);
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    setBounds(100, 100, 300, 300);  // Más espacio
	    Image icono = Toolkit.getDefaultToolkit().getImage("💌.png");
        setIconImage(icono);
	    contentPane = new JPanel();
	    contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
	    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
	    setContentPane(contentPane);

	    // Panel para el nickname
	    JPanel panelNick = new JPanel(new FlowLayout(FlowLayout.LEFT));
	    JLabel labelNick = new JLabel("NickName:");
	    textFieldUsuario = new JTextField(15);
	    textFieldUsuario.addKeyListener(this);
	    panelNick.add(labelNick);
	    panelNick.add(textFieldUsuario);
	    contentPane.add(panelNick);

	    // Panel persistencia (solo en registro)
	    if (nombreAccion.equalsIgnoreCase(Util.CTEREGISTRAR)) {
	        JPanel panelPersistencia = new JPanel();
	        panelPersistencia.setLayout(new BoxLayout(panelPersistencia, BoxLayout.Y_AXIS));

	        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
	        labelPersistencia = new JLabel("Persistencia:");
	        comboPersistencia = new JComboBox<>(new String[]{Util.XML, Util.JSON, Util.TEXTO_PLANO});
	        fila1.add(labelPersistencia);
	        fila1.add(comboPersistencia);
	        panelPersistencia.add(fila1);

	        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
	        labelEncriptacion = new JLabel("Encriptación:");
	        comboEncriptacion = new JComboBox<>(new String[]{"Confederados", "XOR"});
	        fila2.add(labelEncriptacion);
	        fila2.add(comboEncriptacion);
	        panelPersistencia.add(fila2);

	        JPanel fila3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
	        labelClaveEncriptacion = new JLabel("Clave:");
	        textFieldClaveEncriptacion = new JTextField(15);
	        textFieldClaveEncriptacion.addKeyListener(this);
	        fila3.add(labelClaveEncriptacion);
	        fila3.add(textFieldClaveEncriptacion);
	        panelPersistencia.add(fila3);

	        contentPane.add(panelPersistencia);
	    }

	    // Botón
	    JPanel panelBoton = new JPanel();
	    this.boton = new JButton(nombreBoton);
	    this.boton.setToolTipText(nombreBoton);
	    this.boton.setEnabled(false);
	    this.boton.setActionCommand(nombreAccion);
	    panelBoton.add(this.boton);
	    contentPane.add(panelBoton);
	}

	public String getClaveEncriptacion() {
	    return textFieldClaveEncriptacion != null ? textFieldClaveEncriptacion.getText() : null;
	}

	public String getTipoEncriptacionSeleccionada() {
	    return comboEncriptacion != null ? (String) comboEncriptacion.getSelectedItem() : null;
	}

	public String getTipoPersistenciaSeleccionada() {
		return comboPersistencia != null ? (String) comboPersistencia.getSelectedItem() : null;
	}
	// Lo de abajo posiblemente se borra
	public String getUsuario() {
		return textFieldUsuario.getText();
	}

	
	public void vaciarTextFieldNickName() {
		this.textFieldUsuario.setText("");
	}
	public void deshabilitarBoton() {
		this.boton.setEnabled(false);	
	}
	public void refrescaPantalla() {
		deshabilitarBoton();
	    vaciarTextFieldNickName();
	    vaciarTextFieldClave();
	}
	
	private void vaciarTextFieldClave() {
		if (textFieldClaveEncriptacion != null)
			this.textFieldClaveEncriptacion.setText("");	
	}

	//metodo para pantalla login o registro
	public void mostrarErrorUsuarioYaLogueado() {
	    JOptionPane.showMessageDialog(
	        this,
	        "El usuario ya se encuentra logueado. Intente con otro usuario.",
	        "Error de Login",
	        JOptionPane.ERROR_MESSAGE
	    );
	    refrescaPantalla(); 
	}

	//metodo para pantallaLogin
	public void mostrarErrorUsuarioInexistente() {
	    JOptionPane.showMessageDialog(
	        this,
	        "El usuario es Inexistente. Intente con otro usuario.",
	        "Error de Login",
	        JOptionPane.ERROR_MESSAGE
	    );
	    this.boton.setEnabled(false);
	    refrescaPantalla();
	}
	public void mostrarErrorServidoresCaidos(String error) {
		JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
	}
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent e) {
	    String nick = textFieldUsuario.getText().trim();

	    // Si el campo clave no existe (modo login), solo validás el nick
	    if (textFieldClaveEncriptacion == null) {
	        boton.setEnabled(!nick.isEmpty());
	    } else {
	        String clave = textFieldClaveEncriptacion.getText().trim();
	        boton.setEnabled(!nick.isEmpty() && !clave.isEmpty());
	    }
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setActionListener(ActionListener controlador) {
		// TODO Auto-generated method stub
		this.boton.addActionListener(controlador);
	}

}
