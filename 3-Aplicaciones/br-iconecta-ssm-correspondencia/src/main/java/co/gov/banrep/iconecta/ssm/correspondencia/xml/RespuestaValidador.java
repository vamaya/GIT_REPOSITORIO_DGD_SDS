package co.gov.banrep.iconecta.ssm.correspondencia.xml;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "validador")
public class RespuestaValidador {
	private boolean errorValidacion;
	private boolean anexosElectronicos;
	private int codigoError;
	private String mensajeError;
	
	protected RespuestaValidador() {
		
	}
	

	// PARA RTA EXITOSA
	public RespuestaValidador(boolean errorValidacion, boolean anexosElectronicos) {
		super();
		this.errorValidacion = errorValidacion;
		this.anexosElectronicos = anexosElectronicos;
		this.codigoError = 0;
		this.mensajeError = "";
	}

	
	// PARA RTA CON ERROR
	public RespuestaValidador(boolean errorValidacion, boolean anexosElectronicos, int codigoError,
			String mensajeError) {
		super();
		this.errorValidacion = errorValidacion;
		this.anexosElectronicos = anexosElectronicos;
		this.codigoError = codigoError;
		this.mensajeError = mensajeError;
	}



	public boolean isErrorValidacion() {
		return errorValidacion;
	}
	public void setErrorValidacion(boolean errorValidacion) {
		this.errorValidacion = errorValidacion;
	}
	public boolean isAnexosElectronicos() {
		return anexosElectronicos;
	}
	public void setAnexosElectronicos(boolean anexosElectronicos) {
		this.anexosElectronicos = anexosElectronicos;
	}
	public int getCodigoError() {
		return codigoError;
	}
	public void setCodigoError(int codigoError) {
		this.codigoError = codigoError;
	}
	public String getMensajeError() {
		return mensajeError;
	}
	public void setMensajeError(String mensajeError) {
		this.mensajeError = mensajeError;
	}
	
	

}
