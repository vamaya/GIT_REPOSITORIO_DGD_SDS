package co.gov.banrep.iconecta.ssm.correspondencia.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "respuesta")
public class RespuestaXml {
	
	private boolean valor;	
	private String mensaje;
	private Long codigoRuta;
	private Boolean pasoPorCurrier;
	
	public Boolean getPasoPorCurrier() {
		return pasoPorCurrier;
	}

	public void setPasoPorCurrier(Boolean pasoPorCurrier) {
		this.pasoPorCurrier = pasoPorCurrier;
	}

	protected RespuestaXml(){};
	
	public RespuestaXml(boolean valor, String mensaje, Long codigoRuta, Boolean pasoPorCurrier) {
		super();
		this.valor = valor;
		this.mensaje = mensaje;
		this.codigoRuta = codigoRuta;
		this.pasoPorCurrier = pasoPorCurrier;
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

	public Long getCodigoRuta() {
		return codigoRuta;
	}

	@XmlElement
	public void setCodigoRuta(Long codigoRuta) {
		this.codigoRuta = codigoRuta;
	}
	
	@Override
	public String toString() {
		return "RespuestaXML: valor [" + valor + "] - mensaje [" + mensaje + "] - codigoRuta [" + codigoRuta
				+ "] - pasoPorCurrier [" + pasoPorCurrier + "]";
	}
}
