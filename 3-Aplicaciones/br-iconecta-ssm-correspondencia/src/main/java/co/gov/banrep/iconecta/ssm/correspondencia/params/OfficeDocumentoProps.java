package co.gov.banrep.iconecta.ssm.correspondencia.params;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("officedocumento")
@Component
public class OfficeDocumentoProps {
	
	private String numeroRadicado;
	private String fechaRadicado; 
	private String nombreDestino; 
	private String cargoDestino; 
	private String dependenciaDestino;
	private String pcrDestino; 
	private String ciudadDestino;
	private String referenciaDestino;
	private String asunto; 
	private String nombreFirmante; 
	private String cargoFirmante;
	private String dependenciaFirmante;
	private String idFirmante;
	private String sgrFirmante;
	private String firmaFirmante;
	private String copias;
	private String pcrCopias;
	private String tipologiaPlantilla;
	private String tipoPlantilla;
	private String personalizadaPlantilla;
	private String idiomaPlantilla;
	private String tipoEnvioPlantilla;
	private String versionPlantilla;
	private String anexosFisicos;
	private String anexosElectronicos;
	private String tipologiaPlantillaCarta;
	private String esFondoIndependiente;
	private String esCorreoCertificado;
	private String esImpresionArea;
	private String correoCopia;
	private String nombrePCR;
	private String nombreCDD;
	private String idPCR;
	private String idCDD;
	private String SegundoRemitente;
	
	
	public String getSegundoRemitente() {
		return SegundoRemitente;
	}

	public void setSegundoRemitente(String segundoRemitente) {
		SegundoRemitente = segundoRemitente;
	}
	
	
	
	public String getNumeroRadicado() {
		return numeroRadicado;
	}
	
	public void setNumeroRadicado(String numeroRadicado) {
		this.numeroRadicado = numeroRadicado;
	}
	public String getFechaRadicado() {
		return fechaRadicado;
	}
	public void setFechaRadicado(String fechaRadicado) {
		this.fechaRadicado = fechaRadicado;
	}
	public String getNombreDestino() {
		return nombreDestino;
	}
	public void setNombreDestino(String nombreDestino) {
		this.nombreDestino = nombreDestino;
	}
	public String getCargoDestino() {
		return cargoDestino;
	}
	public void setCargoDestino(String cargoDestino) {
		this.cargoDestino = cargoDestino;
	}
	public String getDependenciaDestino() {
		return dependenciaDestino;
	}
	public void setDependenciaDestino(String dependenciaDestino) {
		this.dependenciaDestino = dependenciaDestino;
	}
	public String getPcrDestino() {
		return pcrDestino;
	}
	public void setPcrDestino(String pcrDestino) {
		this.pcrDestino = pcrDestino;
	}
	public String getCiudadDestino() {
		return ciudadDestino;
	}
	public void setCiudadDestino(String ciudadDestino) {
		this.ciudadDestino = ciudadDestino;
	}
	public String getReferenciaDestino() {
		return referenciaDestino;
	}
	public void setReferenciaDestino(String referenciaDestino) {
		this.referenciaDestino = referenciaDestino;
	}
	public String getAsunto() {
		return asunto;
	}
	public void setAsunto(String asunto) {
		this.asunto = asunto;
	}
	public String getNombreFirmante() {
		return nombreFirmante;
	}
	public void setNombreFirmante(String nombreFirmante) {
		this.nombreFirmante = nombreFirmante;
	}
	public String getCargoFirmante() {
		return cargoFirmante;
	}
	public void setCargoFirmante(String cargoFirmante) {
		this.cargoFirmante = cargoFirmante;
	}
	public String getDependenciaFirmante() {
		return dependenciaFirmante;
	}
	public void setDependenciaFirmante(String dependenciaFirmante) {
		this.dependenciaFirmante = dependenciaFirmante;
	}
	public String getIdFirmante() {
		return idFirmante;
	}
	public void setIdFirmante(String idFirmante) {
		this.idFirmante = idFirmante;
	}
	public String getSgrFirmante() {
		return sgrFirmante;
	}
	public void setSgrFirmante(String sgrFirmante) {
		this.sgrFirmante = sgrFirmante;
	}
	public String getFirmaFirmante() {
		return firmaFirmante;
	}
	public void setFirmaFirmante(String firmaFirmante) {
		this.firmaFirmante = firmaFirmante;
	}
	public String getCopias() {
		return copias;
	}
	public void setCopias(String copias) {
		this.copias = copias;
	}
	public String getPcrCopias() {
		return pcrCopias;
	}
	public void setPcrCopias(String pcrCopias) {
		this.pcrCopias = pcrCopias;
	}
	public String getTipologiaPlantilla() {
		return tipologiaPlantilla;
	}
	public void setTipologiaPlantilla(String tipologiaPlantilla) {
		this.tipologiaPlantilla = tipologiaPlantilla;
	}
	public String getTipoPlantilla() {
		return tipoPlantilla;
	}
	public void setTipoPlantilla(String tipoPlantilla) {
		this.tipoPlantilla = tipoPlantilla;
	}
	public String getPersonalizadaPlantilla() {
		return personalizadaPlantilla;
	}
	public void setPersonalizadaPlantilla(String personalizadaPlantilla) {
		this.personalizadaPlantilla = personalizadaPlantilla;
	}
	public String getIdiomaPlantilla() {
		return idiomaPlantilla;
	}
	public void setIdiomaPlantilla(String idiomaPlantilla) {
		this.idiomaPlantilla = idiomaPlantilla;
	}
	public String getTipoEnvioPlantilla() {
		return tipoEnvioPlantilla;
	}
	public void setTipoEnvioPlantilla(String tipoEnvioPlantilla) {
		this.tipoEnvioPlantilla = tipoEnvioPlantilla;
	}
	public String getVersionPlantilla() {
		return versionPlantilla;
	}
	public void setVersionPlantilla(String versionPlantilla) {
		this.versionPlantilla = versionPlantilla;
	}
	public String getAnexosFisicos() {
		return anexosFisicos;
	}
	public void setAnexosFisicos(String anexosFisicos) {
		this.anexosFisicos = anexosFisicos;
	}
	public String getAnexosElectronicos() {
		return anexosElectronicos;
	}
	public void setAnexosElectronicos(String anexosElectronicos) {
		this.anexosElectronicos = anexosElectronicos;
	}
	public String getTipologiaPlantillaCarta() {
		return tipologiaPlantillaCarta;
	}
	public void setTipologiaPlantillaCarta(String tipologiaPlantillaCarta) {
		this.tipologiaPlantillaCarta = tipologiaPlantillaCarta;
	}

	public String getEsFondoIndependiente() {
		return esFondoIndependiente;
	}

	public void setEsFondoIndependiente(String esFondoIndependiente) {
		this.esFondoIndependiente = esFondoIndependiente;
	}

	public String getEsCorreoCertificado() {
		return esCorreoCertificado;
	}

	public void setEsCorreoCertificado(String esCorreoCertificado) {
		this.esCorreoCertificado = esCorreoCertificado;
	}

	public String getEsImpresionArea() {
		return esImpresionArea;
	}

	public void setEsImpresionArea(String esImpresionArea) {
		this.esImpresionArea = esImpresionArea;
	}

	public String getCorreoCopia() {
		return correoCopia;
	}

	public void setCorreoCopia(String correoCopia) {
		this.correoCopia = correoCopia;
	}

	public String getNombrePCR() {
		return nombrePCR;
	}

	public void setNombrePCR(String nombrePCR) {
		this.nombrePCR = nombrePCR;
	}

	public String getNombreCDD() {
		return nombreCDD;
	}

	public void setNombreCDD(String nombreCDD) {
		this.nombreCDD = nombreCDD;
	}

	public String getIdPCR() {
		return idPCR;
	}

	public void setIdPCR(String idPCR) {
		this.idPCR = idPCR;
	}

	public String getIdCDD() {
		return idCDD;
	}

	public void setIdCDD(String idCDD) {
		this.idCDD = idCDD;
	}
	
}
