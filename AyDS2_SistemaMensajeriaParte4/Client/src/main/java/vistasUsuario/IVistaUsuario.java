package vistasUsuario;

import java.awt.event.ActionListener;

public interface IVistaUsuario {

    void setVisible(boolean b);

    void setActionListener(ActionListener controlador);

    void dispose();

    void mostrarErrorServidoresCaidos(String error);
}