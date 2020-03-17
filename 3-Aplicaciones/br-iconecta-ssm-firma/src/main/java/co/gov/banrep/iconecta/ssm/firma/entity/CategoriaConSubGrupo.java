package co.gov.banrep.iconecta.ssm.firma.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import co.gov.banrep.iconecta.ssm.firma.enums.AtributosCategorias;

public class CategoriaConSubGrupo extends Categoria {

	//private Map<AtributosCategorias, Metadato> metadatosSubGrupo;
	private List<Map<AtributosCategorias, Metadato>> metadatosSubGrupo;

	
	public List<Map<AtributosCategorias, Metadato>> getMetadatosSubGrupo() {
		return metadatosSubGrupo;
	}


	public void setMetadatosSubGrupo(List<Map<AtributosCategorias, Metadato>> metadatosSubGrupo) {
		this.metadatosSubGrupo = metadatosSubGrupo;
	}


	/*
	public Map<String, Object> getMetadatosSubGrupoCS() {

		Map<String, Object> metadatosMap = new HashMap<String, Object>();

		Iterator<Entry<AtributosCategorias, Metadato>> iter = metadatosSubGrupo.entrySet().iterator();
		while (iter.hasNext()) {
			Metadato unMetadato = iter.next().getValue();
			String unaEtiqueta = unMetadato.getEtiquetaCS();
			String unValor = unMetadato.getValor();
			metadatosMap.put(unaEtiqueta, unValor);
		}

		return metadatosMap;

	}
	*/
	
	
	public List<Map<String, Object>> getMetadatosSubGrupoCS() {

		List<Map<String, Object>> listaMetadatos = new ArrayList<Map<String, Object>>();

		for (Map<AtributosCategorias, Metadato> fila : metadatosSubGrupo) {

			Map<String, Object> metadatosMap = new HashMap<String, Object>();

			// Map<AtributosCategorias, Metadato> unaFilaMetadatos =
			// metadatosSubGrupo.get(0);

			Set<Entry<AtributosCategorias, Metadato>> metadatosFila = fila.entrySet();

			for (Entry<AtributosCategorias, Metadato> entry : metadatosFila) {

				// Iterator<Entry<AtributosCategorias, Metadato>> iter =
				// unaFilaMetadatos.entrySet().iterator();
				// while (iter.hasNext()) {
				Metadato unMetadato = entry.getValue();
				String unaEtiqueta = unMetadato.getEtiquetaCS();
				String unValor = unMetadato.getValor();
				metadatosMap.put(unaEtiqueta, unValor);
			}

			listaMetadatos.add(metadatosMap);

		}

		return listaMetadatos;
	}
	
}
