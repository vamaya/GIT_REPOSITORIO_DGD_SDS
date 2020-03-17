package co.gov.banrep.iconecta.pf.circuito.entity;

import java.util.ArrayList;
import java.util.List;

public final class Circuito {

	public final String nifSolictante;
	public final String asunto;
	public final List<Documento> documentos;
	public final List<Firmante> firmantes;
	public final int idOrgGroup;
	public final boolean enableNoticeEnd;
	public final String valuesForm;
	public final String nameFolder;

	// Escenario 1-1-1
	public Circuito(String nifSolictante, String asunto, Documento documento, Firmante firmante) {

		if (nifSolictante == null) {
			throw new NullPointerException("El parametro nif del Solicitante no puede ser nulo");
		}

		if (asunto == null) {
			throw new NullPointerException("El parametro asunto no puede ser nulo");
		}

		if (documento == null) {
			throw new NullPointerException("El parametro documentos no puede ser nulo");
		}

		if (firmante == null) {
			throw new NullPointerException("El parametro firmante no puede ser nulo");
		}

		this.nifSolictante = nifSolictante;
		this.asunto = asunto;
		this.documentos = new ArrayList<Documento>();
		this.documentos.add(documento);

		this.firmantes = new ArrayList<Firmante>();
		this.firmantes.add(firmante);

		// Parametros por default
		this.idOrgGroup = 1; // TODO Preguntar a que hace referencia el n�mero 1
		this.enableNoticeEnd = false;
		this.valuesForm = ""; // TODO Preguntar si alguna vez se va a usar este parametro
		this.nameFolder = ""; // TODO Preguntar como se deberia setear este parametro
	}

	// Escenario 1-n-1
	public Circuito(String nifSolictante, String asunto, List<Documento> documentos, Firmante firmante) {

		if (nifSolictante == null) {
			throw new NullPointerException("El parametro nif del Solicitante no puede ser nulo");
		}

		if (asunto == null) {
			throw new NullPointerException("El parametro asunto no puede ser nulo");
		}

		if (documentos == null) {
			throw new NullPointerException("El parametro documentos no puede ser nulo");
		}

		if (firmante == null) {
			throw new NullPointerException("El parametro firmante no puede ser nulo");
		}

		this.nifSolictante = nifSolictante;
		this.asunto = asunto;
		this.documentos = new ArrayList<Documento>(documentos);

		this.firmantes = new ArrayList<Firmante>();
		this.firmantes.add(firmante);

		// Parametros por default
		this.idOrgGroup = 1; // TODO Preguntar a que hace referencia el n�mero 1
		this.enableNoticeEnd = false;
		this.valuesForm = ""; // TODO Preguntar si alguna vez se va a usar este parametro
		this.nameFolder = ""; // TODO Preguntar como se deberia setear este parametro
	}

	// Escenario 1-1-n
	public Circuito(String nifSolictante, String asunto, Documento documento, List<Firmante> firmantes) {

		if (nifSolictante == null) {
			throw new NullPointerException("El parametro nif del Solicitante no puede ser nulo");
		}

		if (asunto == null) {
			throw new NullPointerException("El parametro asunto no puede ser nulo");
		}

		if (documento == null) {
			throw new NullPointerException("El parametro documentos no puede ser nulo");
		}

		if (firmantes == null) {
			throw new NullPointerException("El parametro firmantes no puede ser nulo");
		}

		this.nifSolictante = nifSolictante;
		this.asunto = asunto;
		this.documentos = new ArrayList<Documento>();
		this.documentos.add(documento);

		this.firmantes = new ArrayList<Firmante>(firmantes);

		// Parametros por default
		this.idOrgGroup = 1; // TODO Preguntar a que hace referencia el n�mero 1
		this.enableNoticeEnd = false;
		this.valuesForm = ""; // TODO Preguntar si alguna vez se va a usar este parametro
		this.nameFolder = ""; // TODO Preguntar como se deberia setear este parametro
	}

	// Escenario 1-n-n
	public Circuito(String nifSolictante, String asunto, List<Documento> documentos, List<Firmante> firmantes) {

		if (nifSolictante == null) {
			throw new NullPointerException("El parametro nif del Solicitante no puede ser nulo");
		}

		if (asunto == null) {
			throw new NullPointerException("El parametro asunto no puede ser nulo");
		}

		if (documentos == null) {
			throw new NullPointerException("El parametro documentos no puede ser nulo");
		}

		if (firmantes == null) {
			throw new NullPointerException("El parametro firmantes no puede ser nulo");
		}

		this.nifSolictante = nifSolictante;
		this.asunto = asunto;
		this.documentos = new ArrayList<Documento>(documentos);
		this.firmantes = new ArrayList<Firmante>(firmantes);

		// Parametros por default
		this.idOrgGroup = 1; // TODO Preguntar a que hace referencia el n�mero 1
		this.enableNoticeEnd = false;
		this.valuesForm = ""; // TODO Preguntar si alguna vez se va a usar este parametro
		this.nameFolder = ""; // TODO Preguntar como se deberia setear este parametro
	}
	
	//Constructor con todos los parametros
	private Circuito(String nifSolictante, String asunto, List<Documento> documentos, List<Firmante> firmantes,
			int idOrgGroup, boolean enableNoticeEnd, String valuesForm, String nameFolder) {
		this.nifSolictante = nifSolictante;
		this.asunto = asunto;
		this.documentos = new ArrayList<Documento>(documentos);
		this.firmantes = new ArrayList<Firmante>(firmantes);
		this.idOrgGroup = idOrgGroup;
		this.enableNoticeEnd = enableNoticeEnd;
		this.valuesForm = valuesForm;
		this.nameFolder = nameFolder;
	}

	public Circuito withIdOrgGroup(int idOrgGroup) {
		return new Circuito(nifSolictante, asunto, documentos, firmantes, idOrgGroup, enableNoticeEnd, valuesForm,
				nameFolder);
	}
	
	public Circuito withEnableNoticeEnd(boolean enableNoticeEnd){
		return new Circuito(nifSolictante, asunto, documentos, firmantes, idOrgGroup, enableNoticeEnd, valuesForm,
				nameFolder);
	}
	
	public Circuito withValuesForm(String valuesForm){
		return new Circuito(nifSolictante, asunto, documentos, firmantes, idOrgGroup, enableNoticeEnd, valuesForm,
				nameFolder);
	}
	
	public Circuito withNameFolder(String nameFolder){
		return new Circuito(nifSolictante, asunto, documentos, firmantes, idOrgGroup, enableNoticeEnd, valuesForm,
				nameFolder);
	}
	
	public int getFirmantesSize(){
		return firmantes.size();
	}
	
	public int getDocumentosSize(){
		return documentos.size();
	}

}
