package co.gov.banrep.iconecta.ssm.firma.plugin;

import java.io.IOException;
import java.util.Map;


import com.sun.xml.ws.api.message.Header;

import co.gov.banrep.iconecta.cs.workflow.ValorAtributo;

public interface AccionExterna {
	
	public String ejecutar(Map<Object, Object> parametros, String url) throws IOException;
	
	public String cancelar(Map<Object, Object> parametros, String url) throws IOException;
	
	public String notificarError(Map<String,ValorAtributo> parametros, long mapId, String url, Header aut );
	
	
}
