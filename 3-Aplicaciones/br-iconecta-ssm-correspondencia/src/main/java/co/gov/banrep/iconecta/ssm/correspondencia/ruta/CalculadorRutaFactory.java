package co.gov.banrep.iconecta.ssm.correspondencia.ruta;

public class CalculadorRutaFactory {
	
	private static final String DER = "DER";
	
	public static CaculadorRuta crearCalculadorRuta(String tipo) {
		
		CaculadorRuta calculador = null;
		
		if(tipo.equals(DER)) {
			calculador = new CalculadorRutaDer();
		}
		
		return calculador;
	}

}
