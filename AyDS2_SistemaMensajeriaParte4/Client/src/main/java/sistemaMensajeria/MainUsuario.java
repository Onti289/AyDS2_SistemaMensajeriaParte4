package sistemaMensajeria;

import controlador.ControladorUsuario;
import modeloNegocio.SistemaUsuario;

public class MainUsuario {

    public static void main(String[] args) {
        SistemaUsuario sMensajeria = SistemaUsuario.get_Instancia();
        ControladorUsuario controlador = new ControladorUsuario(sMensajeria);
    }
}