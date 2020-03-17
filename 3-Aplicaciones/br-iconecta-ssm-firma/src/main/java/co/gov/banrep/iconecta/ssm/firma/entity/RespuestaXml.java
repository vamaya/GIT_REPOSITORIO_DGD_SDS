package co.gov.banrep.iconecta.ssm.firma.entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "respuesta")
public class RespuestaXml {
	
	private boolean valor;	
	private String mensaje;
	private String nombreFirmante1;
	private String correoFirmante1;
	
	protected RespuestaXml(){};
	
	public RespuestaXml(boolean valor, String mensaje) {
		super();
		this.valor = valor;
		this.mensaje = mensaje;
	}
	
	public RespuestaXml(boolean valor, String mensaje, String nombreFirmante1, String correoFirmante1) {
		super();
		this.valor = valor;
		this.mensaje = mensaje;
		this.nombreFirmante1 = nombreFirmante1;
		this.correoFirmante1 = correoFirmante1;
	}

	public boolean isValor() {
		return valor;
	}
	
	@XmlElement
	public void setValor(boolean valor) {
		this.valor = valor;
	}
	public String getMensaje() {
		return mensaje;
	}
	
	@XmlElement
	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	public String getNombreFirmante1() {
		return nombreFirmante1;
	}
	
	@XmlElement
	public void setNombreFirmante1(String nombreFirmante1) {
		this.nombreFirmante1 = nombreFirmante1;
	}

	public String getCorreoFirmante1() {
		return correoFirmante1;
	}

	@XmlElement
	public void setCorreoFirmante1(String correoFirmante1) {
		this.correoFirmante1 = correoFirmante1;
	}
	
	
	
}
