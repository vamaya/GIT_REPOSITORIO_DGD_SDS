package co.gov.banrep.iconecta.ssm.firma.plugin;

public class AccionExternaFactory {
	
	private static final String TIPO_MEMORANDO = "memorando";
	
	public static AccionExterna crearAccionExterna(String tipo){
		AccionExterna accionExterna = null;
		
		if(tipo.equals(TIPO_MEMORANDO)){
			accionExterna = new AccionExternaMemorando();
		}
		
		return accionExterna;
	}

}
