package persistencia;

public class ConcreteFactoryTextoPlano implements IAbstractFactoryPersistencia {

	@Override
	public IPersistenciaContacto crearPersistenciaContacto() {
		return new ConcreteContactoTextoPlano();
	}

	@Override
	public IPersistenciaMensaje crearPersistenciaMensaje() {
		return new ConcreteMensajeTextoPlano();
	}

}
