package co.gov.banrep.iconecta.office.documento.entity;

import java.util.Date;
import java.util.List;

public class MetadatosPlantilla {

	private String Asunto;
	private String numeroRadicado;
	private Date fechaRadicado;
	private String tipologia;
	private List<Empleado> destinatarios;
	private List<Empleado> firmantes;
	private List<Empleado> copias;
	private String anexosElectronicos;
	private String anexosFisicos;
	private String tipoDocumento;
	private String personalizarDocumento;
	private String idiomaIngles;
	private String tipoEnvio;
	private Boolean esFondoIndependiente;
	private Boolean esDobleFirmante;
	private Boolean esGeneradoDesdePlantilla;
	private String versionPlantilla;
	
	private Boolean esCorreoCertificado;
	private Boolean esImpresionArea;
	private String correoCopia;



	public String getAsunto() {
		return Asunto;
	}

	public void setAsunto(String asunto) {
		Asunto = asunto;
	}

	public String getNumeroRadicado() {
		return numeroRadicado;
	}

	public void setNumeroRadicado(String numeroRadicado) {
		this.numeroRadicado = numeroRadicado;
	}

	public Date getFechaRadicado() {
		return fechaRadicado;
	}

	public void setFechaRadicado(Date fechaRadicado) {
		this.fechaRadicado = fechaRadicado;
	}

	public List<Empleado> getDestinatarios() {
		return destinatarios;
	}

	public void setDestinatarios(List<Empleado> destinatarios) {
		this.destinatarios = destinatarios;
	}

	public List<Empleado> getFirmantes() {
		return firmantes;
	}

	public void setFirmantes(List<Empleado> firmantes) {
		this.firmantes = firmantes;
	}

	public List<Empleado> getCopias() {
		return copias;
	}

	public void setCopias(List<Empleado> copias) {
		this.copias = copias;
	}

	public String getTipologia() {
		return tipologia;
	}

	public void setTipologia(String tipologia) {
		this.tipologia = tipologia;
	}

	public String getAnexosElectronicos() {
		return anexosElectronicos;
	}

	public void setAnexosElectronicos(String anexosElectronicos) {
		this.anexosElectronicos = anexosElectronicos;
	}

	public String getAnexosFisicos() {
		return anexosFisicos;
	}

	public void setAnexosFisicos(String anexosFisicos) {
		this.anexosFisicos = anexosFisicos;
	}

	public String getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(String tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	public String getPersonalizarDocumento() {
		return personalizarDocumento;
	}

	public void setPersonalizarDocumento(String personalizarDocumento) {
		this.personalizarDocumento = personalizarDocumento;
	}

	public String getIdiomaIngles() {
		return idiomaIngles;
	}

	public void setIdiomaIngles(String idiomaIngles) {
		this.idiomaIngles = idiomaIngles;
	}

	public String getTipoEnvio() {
		return tipoEnvio;
	}

	public void setTipoEnvio(String tipoEnvio) {
		this.tipoEnvio = tipoEnvio;
	}

	public Boolean getEsFondoIndependiente() {
		return esFondoIndependiente;
	}

	public void setEsFondoIndependiente(Boolean esFondoIndependiente) {
		this.esFondoIndependiente = esFondoIndependiente;
	}



	public String getCorreoCopia() {
		return correoCopia;
	}

	public void setCorreoCopia(String correoCopia) {
		this.correoCopia = correoCopia;
	}
	
	public Boolean getEsCorreoCertificado() {
		return esCorreoCertificado;
	}

	public void setEsCorreoCertificado(Boolean esCorreoCertificado) {
		this.esCorreoCertificado = esCorreoCertificado;
	}

	public Boolean getEsImpresionArea() {
		return esImpresionArea;
	}

	public void setEsImpresionArea(Boolean esImpresionArea) {
		this.esImpresionArea = esImpresionArea;
	}

	public Boolean getEsDobleFirmante() {
		return esDobleFirmante;
	}

	public void setEsDobleFirmante(Boolean esDobleFirmante) {
		this.esDobleFirmante = esDobleFirmante;
	}

	public Boolean getEsGeneradoDesdePlantilla() {
		return esGeneradoDesdePlantilla;
	}

	public void setEsGeneradoDesdePlantilla(Boolean esGeneradoDesdePlantilla) {
		this.esGeneradoDesdePlantilla = esGeneradoDesdePlantilla;
	}

	public String getVersionPlantilla() {
		return versionPlantilla;
	}

	public void setVersionPlantilla(String versionPlantilla) {
		this.versionPlantilla = versionPlantilla;
	}
	
}
