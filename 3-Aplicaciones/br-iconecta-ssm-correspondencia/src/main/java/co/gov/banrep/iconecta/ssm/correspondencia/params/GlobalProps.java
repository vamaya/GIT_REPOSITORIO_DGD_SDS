package co.gov.banrep.iconecta.ssm.correspondencia.params;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties
@Component
public class GlobalProps {

	@NotNull
	private String conexionUsuarioCS;
	@NotNull
	private int longitudNuevoNombreArchivo;
	@NotNull
	private String rutaTemp;
	// TODO NOT NULL CARTA- DER
	private String rutaXML;
	@NotNull
	private String formatoFecha;
	//@NotNull
	//private String formatoFechaAmd;
	@NotNull
	private String formatoHora;
	@NotNull
	private String reintento;
	@NotNull
	private int cantidadReintentos;
	@NotNull
	private int tiempoReintento;
	@NotNull
	private String mensajeError;
	@NotNull
	private String estadoError;
	@NotNull
	private String mailAdmin;
	@NotNull
	private String mapIdNotificacionAdmin;

	// TODO NOT NULL CARTA- DER
	private String derObjetoFisico;
	// TODO NOT NULL CARTA- DER
	private String derObjetoElectronico;
	// TODO NOT NULL CARTA- DER
	private String derObjetoElectronicoyFisico;

	//@NotNull
	//private String nombreCategoriaFirma;
	@NotNull
	private String versionPlantillaOficialMemorando;
	// TODO NOT NULL CARTA- DER
	private String versionPlantillaOficialCarta;
	
	// @NotNull
	@Max(10000000)
	@Min(1)
	private long idConsultaFirmantes;

	public long getIdConsultaFirmantes() {
		return idConsultaFirmantes;
	}

	public void setIdConsultaFirmantes(long idConsultaFirmantes) {
		this.idConsultaFirmantes = idConsultaFirmantes;
	}
	
	
	
	//Vamaya: Se añade para las nuevas versiones de ME y CA
	@NotNull
	private String versionPlantillaOficialMemorando2;
	// TODO NOT NULL CARTA- DER
	private String versionPlantillaOficialCarta2;
	//Fin bloque
	@NotNull
	private String nombreCourrier;
	@NotNull
	private String rutaDefault;
	@NotNull
	@Min(1000)
	private long idReporteAddin;
	@NotNull
	private String rutaPlanoAddin;
	@NotNull
	private String vectorCifrado;

	public String getVersionPlantillaOficialCarta() {
		return versionPlantillaOficialCarta;
	}

	public void setVersionPlantillaOficialCarta(String versionPlantillaOficialCarta) {
		this.versionPlantillaOficialCarta = versionPlantillaOficialCarta;
	}	

	public String getVersionPlantillaOficialMemorando() {
		return versionPlantillaOficialMemorando;
	}

	public void setVersionPlantillaOficialMemorando(String versionPlantillaOficialMemorando) {
		this.versionPlantillaOficialMemorando = versionPlantillaOficialMemorando;
	}
	
	//Vamaya: Getters y Setters para nuevas versiones de CA y ME
	public String getVersionPlantillaOficialCarta2() {
		return versionPlantillaOficialCarta2;
	}

	public void setVersionPlantillaOficialCarta2(String versionPlantillaOficialCarta2) {
		this.versionPlantillaOficialCarta2 = versionPlantillaOficialCarta2;
	}	

	public String getVersionPlantillaOficialMemorando2() {
		return versionPlantillaOficialMemorando2;
	}

	public void setVersionPlantillaOficialMemorando2(String versionPlantillaOficialMemorando2) {
		this.versionPlantillaOficialMemorando2 = versionPlantillaOficialMemorando2;
	}	
	//Fin bloque 
	
	private String tipologiaMemorando;
	
	private String tipologiaCarta;

	public String getConexionUsuarioCS() {
		return conexionUsuarioCS;
	}

	public void setConexionUsuarioCS(String conexionUsuarioCS) {
		this.conexionUsuarioCS = conexionUsuarioCS;
	}

	public int getLongitudNuevoNombreArchivo() {
		return longitudNuevoNombreArchivo;
	}

	public void setLongitudNuevoNombreArchivo(int longitudNuevoNombreArchivo) {
		this.longitudNuevoNombreArchivo = longitudNuevoNombreArchivo;
	}

	public String getRutaTemp() {
		return rutaTemp;
	}

	public void setRutaTemp(String rutaTemp) {
		this.rutaTemp = rutaTemp;
	}

	public String getFormatoFecha() {
		return formatoFecha;
	}

	public void setFormatoFecha(String formatoFecha) {
		this.formatoFecha = formatoFecha;
	}

	public String getFormatoHora() {
		return formatoHora;
	}

	public void setFormatoHora(String formatoHora) {
		this.formatoHora = formatoHora;
	}

	public String getReintento() {
		return reintento;
	}

	public void setReintento(String reintento) {
		this.reintento = reintento;
	}

	public int getCantidadReintentos() {
		return cantidadReintentos;
	}

	public void setCantidadReintentos(int cantidadReintentos) {
		this.cantidadReintentos = cantidadReintentos;
	}

	public int getTiempoReintento() {
		return tiempoReintento;
	}

	public void setTiempoReintento(int tiempoReintento) {
		this.tiempoReintento = tiempoReintento;
	}

	public String getMensajeError() {
		return mensajeError;
	}

	public void setMensajeError(String mensajeError) {
		this.mensajeError = mensajeError;
	}

	public String getEstadoError() {
		return estadoError;
	}

	public void setEstadoError(String estadoError) {
		this.estadoError = estadoError;
	}

	public String getMailAdmin() {
		return mailAdmin;
	}

	public void setMailAdmin(String mailAdmin) {
		this.mailAdmin = mailAdmin;
	}

	public String getMapIdNotificacionAdmin() {
		return mapIdNotificacionAdmin;
	}

	public void setMapIdNotificacionAdmin(String mapId) {
		this.mapIdNotificacionAdmin = mapId;
	}
	
	public String getTipologiaMemorando() {
		return tipologiaMemorando;
	}

	public void setTipologiaMemorando(String tipologiaMemorando) {
		this.tipologiaMemorando = tipologiaMemorando;
	}

	public String getTipologiaCarta() {
		return tipologiaCarta;
	}

	public void setTipologiaCarta(String tipologiaCarta) {
		this.tipologiaCarta = tipologiaCarta;
	}

	public String getDerObjetoFisico() {
		return derObjetoFisico;
	}

	public void setDerObjetoFisico(String derObjetoFisico) {
		this.derObjetoFisico = derObjetoFisico;
	}

	public String getDerObjetoElectronico() {
		return derObjetoElectronico;
	}

	public void setDerObjetoElectronico(String derObjetoElectronico) {
		this.derObjetoElectronico = derObjetoElectronico;
	}

	public String getDerObjetoElectronicoyFisico() {
		return derObjetoElectronicoyFisico;
	}

	public void setDerObjetoElectronicoyFisico(String derObjetoElectronicoyFisico) {
		this.derObjetoElectronicoyFisico = derObjetoElectronicoyFisico;
	}
	
	/*
	public long getIdCarpetaConsecutivosCarta() {
		return idCarpetaConsecutivosCarta;
	}

	public void setIdCarpetaConsecutivosCarta(long idCarpetaConsecutivosCarta) {
		this.idCarpetaConsecutivosCarta = idCarpetaConsecutivosCarta;
	}
	*/
	
	/*
	public String getValorTipoDocumentalConsecutivo() {
		return valorTipoDocumentalConsecutivo;
	}

	public void setValorTipoDocumentalConsecutivo(String valorTipoDocumentalConsecutivo) {
		this.valorTipoDocumentalConsecutivo = valorTipoDocumentalConsecutivo;
	}
	*/
	
	/*
	public String getValorSerieCartaConsecutivo() {
		return valorSerieCartaConsecutivo;
	}

	public void setValorSerieCartaConsecutivo(String valorSerieCartaConsecutivo) {
		this.valorSerieCartaConsecutivo = valorSerieCartaConsecutivo;
	}
	*/
	public String getRutaXML() {
		return rutaXML;
	}

	public void setRutaXML(String rutaXML) {
		this.rutaXML = rutaXML;
	}

	public String getNombreCourrier() {
		return nombreCourrier;
	}

	public void setNombreCourrier(String nombreCourrier) {
		this.nombreCourrier = nombreCourrier;
	}

	public String getRutaDefault() {
		return rutaDefault;
	}

	public void setRutaDefault(String rutaDefault) {
		this.rutaDefault = rutaDefault;
	}

	public long getIdReporteAddin() {
		return idReporteAddin;
	}

	public void setIdReporteAddin(long idReporteAddin) {
		this.idReporteAddin = idReporteAddin;
	}

	public String getRutaPlanoAddin() {
		return rutaPlanoAddin;
	}

	public void setRutaPlanoAddin(String rutaPlanoAddin) {
		this.rutaPlanoAddin = rutaPlanoAddin;
	}

	public String getVectorCifrado() {
		return vectorCifrado;
	}

	public void setVectorCifrado(String vectorCifrado) {
		this.vectorCifrado = vectorCifrado;
	}
	
	

}
