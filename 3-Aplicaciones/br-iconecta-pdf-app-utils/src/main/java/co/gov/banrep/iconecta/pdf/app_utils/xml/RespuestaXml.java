package co.gov.banrep.iconecta.pdf.app_utils.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "respuesta")
public class RespuestaXml {	

	private boolean valor;	
	private String mensaje;
	private String descripcion;
	
	protected RespuestaXml(){};
	
	public RespuestaXml(boolean valor, String mensaje) {
		super();
		this.valor = valor;
		this.mensaje = mensaje;		
	}
	
	public RespuestaXml(boolean valor, String mensaje, String idDocumento) {
		super();
		this.valor = valor;
		this.mensaje = mensaje;
		this.descripcion = idDocumento;
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

	public String getDescripcion() {
		return descripcion;
	}

	@XmlElement
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	
	
	

}
