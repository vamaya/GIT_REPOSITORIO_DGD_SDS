package co.gov.banrep.iconecta.cs.app.service.utils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Permite el manejo de las respuestas hacia el Content Server en formanto XML
 * para que estas puedan ser obtenidas mediante XPATH
 * 
 *  @author <a href="mailto:jjrojassa@banrep.gov.co">John Jairo Rojas S.</a>
 *
 */
@XmlRootElement(name = "respuesta")
public class RespuestaXml {	

	private boolean valor;	
	private String mensaje;
	private long idDocumento;
	
	protected RespuestaXml(){};
	
	public RespuestaXml(boolean valor, String mensaje) {
		super();
		this.valor = valor;
		this.mensaje = mensaje;		
	}
	
	public RespuestaXml(boolean valor, String mensaje, long idDocumento) {
		super();
		this.valor = valor;
		this.mensaje = mensaje;
		this.idDocumento = idDocumento;
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

	public long getIdDocumento() {
		return idDocumento;
	}
	
	@XmlElement
	public void setIdDocumento(long idDocumento) {
		this.idDocumento = idDocumento;
	}
	
	

}
