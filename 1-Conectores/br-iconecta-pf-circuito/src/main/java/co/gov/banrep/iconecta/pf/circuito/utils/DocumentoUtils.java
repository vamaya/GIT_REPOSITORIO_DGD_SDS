package co.gov.banrep.iconecta.pf.circuito.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import co.gov.banrep.iconecta.pf.cliente.SignDocObject.METADATOS;
import co.gov.banrep.iconecta.pf.cliente.SignDocObject.METADATOS.Entry;

public final class DocumentoUtils {
	
	private DocumentoUtils(){
		throw new AssertionError();
	}
	
	public static HashMap<String, String> cargarMapMetadatos(METADATOS metadatos) {

		HashMap<String, String> metadatosMap = new HashMap<String, String>();

		List<Entry> entry = metadatos.getEntry();

		Iterator<Entry> entryIter = entry.iterator();

		while (entryIter.hasNext()) {
			Entry unEntry = entryIter.next();
			String key = unEntry.getKey();
			String value = unEntry.getValue();
			metadatosMap.put(key, value);
		}

		return metadatosMap;
	}

}
