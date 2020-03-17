package co.gov.banrep.iconecta.pf.circuito.entity;

import java.util.HashMap;

import javax.activation.DataHandler;

public final class Documento {

	public final String nombre;
	public final DataHandler contenido;
	public final String guid;
	public final HashMap<String, String> metadatos;
	public final int idDocType;

	public Documento(String nombre, DataHandler contenido) {

		if (contenido == null) {
			throw new NullPointerException("El contenido del documento no puede ser nulo");
		}

		this.nombre = nombre;
		this.contenido = contenido;
		this.guid = "0"; // TODO saber que valores deben ir aqui
		this.metadatos = new HashMap<String, String>(); // TODO conocer en que
														// casos se debe enviar
														// metadatos
		this.idDocType = 0; // TODO conocer cuales son los posibles valores y
							// cuando se deben eviar
	}

	private Documento(String nombre, DataHandler contenido, String guid, HashMap<String, String> metadatos,
			int idDocType) {
		this.nombre = nombre;
		this.contenido = contenido;
		this.guid = guid;
		this.metadatos = metadatos;
		this.idDocType = idDocType;
	}

	public Documento withGuid(String guid) {
		return new Documento(nombre, contenido, guid, metadatos, idDocType);
	}

	public Documento withMetadatos(HashMap<String, String> metadatos) {
		// Copia defensiva - previene la altetacion externa del parametro
		// metadatos
		HashMap<String, String> localMetadatos = new HashMap<String, String>(metadatos);

		return new Documento(nombre, contenido, guid, localMetadatos, idDocType);
	}

	public Documento withIdDocType(int idDocType) {
		return new Documento(nombre, contenido, guid, metadatos, idDocType);
	}

	@Override
	public String toString() {
		return "Documento con nombre: " + this.nombre;
	}

}
