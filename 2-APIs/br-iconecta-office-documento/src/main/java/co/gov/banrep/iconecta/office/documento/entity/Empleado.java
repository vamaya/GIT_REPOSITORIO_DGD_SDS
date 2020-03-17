package co.gov.banrep.iconecta.office.documento.entity;

import co.gov.banrep.iconecta.office.documento.utils.AppUtils;
import co.gov.banrep.iconecta.office.documento.utils.Consts;

public class Empleado {
	
	private String nombre;
	private String cargo;
	private String dependencia;
	private String rol;
	private String siglaremitente;
	private long idDocumento;
	private String nombreDocumento;
	private String nombrePCR;
	private long idPCR;
	private String nombreCDD;
	private long idCDD;
	
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		nombre = AppUtils.reemplazarCarateresEspeciales(nombre, Consts.REEMPLAZO_CARACTERES_ESPECIALES);
		this.nombre = nombre;
	}
	public String getCargo() {
		return cargo;
	}
	public void setCargo(String cargo) {
		cargo = AppUtils.reemplazarCarateresEspeciales(cargo, Consts.REEMPLAZO_CARACTERES_ESPECIALES);
		this.cargo = cargo;
	}
	public String getDependencia() {
		return dependencia;
	}
	public void setDependencia(String dependencia) {
		dependencia = AppUtils.reemplazarCarateresEspeciales(dependencia, Consts.REEMPLAZO_CARACTERES_ESPECIALES);
		this.dependencia = dependencia;
	}
	public String getRol() {
		return rol;
	}
	public void setRol(String rol) {
		this.rol = rol;
	}
	public String getSiglaremitente() {
		return siglaremitente;
	}
	public void setSiglaremitente(String siglaremitente) {
		this.siglaremitente = siglaremitente;
	}
	public long getIdDocumento() {
		return idDocumento;
	}
	public void setIdDocumento(long idDocumento) {
		this.idDocumento = idDocumento;
	}
	public String getNombreDocumento() {
		return nombreDocumento;
	}
	public void setNombreDocumento(String nombreDocumento) {
		this.nombreDocumento = nombreDocumento;
	}
	public String getNombrePCR() {
		return nombrePCR;
	}
	public void setNombrePCR(String nombrePCR) {
		this.nombrePCR = nombrePCR;
	}
	public long getIdPCR() {
		return idPCR;
	}
	public void setIdPCR(long idPCR) {
		this.idPCR = idPCR;
	}
	public String getNombreCDD() {
		return nombreCDD;
	}
	public void setNombreCDD(String nombreCDD) {
		this.nombreCDD = nombreCDD;
	}
	public long getIdCDD() {
		return idCDD;
	}
	public void setIdCDD(long idCDD) {
		this.idCDD = idCDD;
	}
}
