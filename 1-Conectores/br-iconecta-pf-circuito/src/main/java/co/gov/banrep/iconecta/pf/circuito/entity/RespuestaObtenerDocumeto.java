package co.gov.banrep.iconecta.pf.circuito.entity;

import java.util.HashMap;

import javax.activation.DataHandler;

public class RespuestaObtenerDocumeto {
	
	private DataHandler contenido;
	private HashMap<String, String> metadatos;
	
	public RespuestaObtenerDocumeto(DataHandler contenido,HashMap<String, String> metadatos) {
		this.contenido = new DataHandler(contenido.getDataSource());
		this.metadatos = new HashMap<String, String>(metadatos);
	}

	public DataHandler getContenido() {
		return contenido;
	}

	public HashMap<String, String> getMetadatos() {
		return metadatos;
	}
		

}
