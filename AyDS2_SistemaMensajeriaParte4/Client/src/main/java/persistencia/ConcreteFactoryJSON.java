package persistencia;

public class ConcreteFactoryJSON implements IAbstractFactoryPersistencia {

	@Override
	public IPersistenciaContacto crearPersistenciaContacto() {
		return new ConcreteContactoJSON();
	}

	@Override
	public IPersistenciaMensaje crearPersistenciaMensaje() {
		// TODO Auto-generated method stub
		return new ConcreteMensajeJSON();
	}

}
