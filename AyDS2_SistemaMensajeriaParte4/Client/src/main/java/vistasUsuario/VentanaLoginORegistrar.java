package vistasUsuario;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import controlador.*;
import util.Util;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.awt.FlowLayout;
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
	/**
	 * Launch the application.
	 */

	/**
	 * Create the frame.
	 */
	public VentanaLoginORegistrar(ControladorUsuario controlador,String titulo,String nombreBoton,String nombreAccion) {
		this.controlador = controlador;
		setTitle(titulo);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 250, 250);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(3,1));

		JPanel panel_nickName = new JPanel(null);
		contentPane.add(panel_nickName);
		

		JLabel label_NickName = new JLabel("NickName:");
		
		label_NickName.setBounds(10, 20, 80, 20);
		panel_nickName.add(label_NickName);
		
		textFieldUsuario = new JTextField();
		textFieldUsuario.addKeyListener(this);
		textFieldUsuario.setBounds(100, 20, 100, 20);
		panel_nickName.add(textFieldUsuario);
		
		// NUEVO: Panel para la persistencia (visible solo si es registro)
				JPanel panelPersistencia = new JPanel(null);
				if (nombreAccion.equalsIgnoreCase(Util.CTEREGISTRAR)) {
					labelPersistencia = new JLabel("Persistencia:");
					labelPersistencia.setBounds(10, 10, 100, 20);
					panelPersistencia.add(labelPersistencia);

					comboPersistencia = new JComboBox<>(new String[] { Util.XML,Util.JSON,Util.TEXTO_PLANO });
					comboPersistencia.setBounds(100, 10, 100, 20);
					panelPersistencia.add(comboPersistencia);

					contentPane.add(panelPersistencia);
				}
		
		JPanel panel_Registrarse = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 20));
		contentPane.add(panel_Registrarse);
		
		this.boton = new JButton(nombreBoton);
		this.boton.setToolTipText(nombreBoton);
		this.boton.setEnabled(false);
		this.boton.setActionCommand(nombreAccion);
		panel_Registrarse.add(this.boton);
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
		// TODO Auto-generated method stub
		this.boton.setEnabled(
				!(textFieldUsuario.getText().isEmpty()));
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
