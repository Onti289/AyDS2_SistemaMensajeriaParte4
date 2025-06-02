package persistencia;

public class ConcreteFactoryXML implements IAbstractFactoryPersistencia{

	@Override
	public IPersistenciaContacto crearPersistenciaContacto() {
		return new ConcreteContactoXML();
	}

	@Override
	public IPersistenciaMensaje crearPersistenciaMensaje() {
		return new ConcreteMensajeXML();
	}

}
