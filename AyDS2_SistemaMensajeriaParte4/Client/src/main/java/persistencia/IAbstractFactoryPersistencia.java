package persistencia;

public interface IAbstractFactoryPersistencia {
	 IPersistenciaContacto crearPersistenciaContacto();
	 IPersistenciaMensaje crearPersistenciaMensaje(); // opcional
}
