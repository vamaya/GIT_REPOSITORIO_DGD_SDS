package co.gov.banrep.iconecta.ssm.correspondencia.distribucion;

public class DistribuidorFactory {
	
	private static final String DER = "DER";
	
	public static Distribuidor crearDistribuidor (String tipo) {
		Distribuidor distribuidor = null;
		
		if (tipo.equals(DER)) {
			distribuidor = new DistribuidorDer();
		}
		
		return distribuidor;
	}

}
