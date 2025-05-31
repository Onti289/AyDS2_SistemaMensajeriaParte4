package persistencia;

import util.Util;

public class SelectorDePersistencia {
    public static IAbstractFactoryPersistencia getFabrica(String tipo) {
        switch (tipo) {
            case Util.XML:
                return new ConcreteFactoryXML();
            case Util.JSON:
                return new ConcreteFactoryJSON();
            case Util.TEXTO_PLANO:
                return new ConcreteFactoryTextoPlano();
            default:
                throw new IllegalArgumentException("Tipo de persistencia no soportado: " + tipo);
        }
    }
}
