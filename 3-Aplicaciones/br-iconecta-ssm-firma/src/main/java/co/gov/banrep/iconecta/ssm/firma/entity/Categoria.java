package co.gov.banrep.iconecta.ssm.firma.entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import co.gov.banrep.iconecta.ssm.firma.enums.AtributosCategorias;

public class Categoria {
	
	private Map<AtributosCategorias, Metadato> metadatos;
		
	public Map<AtributosCategorias, Metadato> getMetadatos() {
		return metadatos;
	}

	public void setMetadatos(Map<AtributosCategorias, Metadato> metadatos) {
		this.metadatos = metadatos;
	}
	
	public Map<String, Object> getMetadatosCS() {
		Map<String, Object> metadatosMap = new HashMap<String, Object>();

		Iterator<Entry<AtributosCategorias, Metadato>> iter = getMetadatos().entrySet().iterator();
		while (iter.hasNext()) {
			Metadato unMetadato = iter.next().getValue();
			String unaEtiqueta = unMetadato.getEtiquetaCS();
			String unValor = unMetadato.getValor();
			metadatosMap.put(unaEtiqueta, unValor);
		}

		return metadatosMap;
	}

}
