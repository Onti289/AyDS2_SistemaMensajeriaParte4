package vistaServidor;

import java.awt.event.ActionListener;

public interface IVistaServidor {

    void setVisible(boolean b);

    void setActionListener(ActionListener controlador);

    void dispose();

    void mostrarErrorServidoresCaidos(String error);
}
