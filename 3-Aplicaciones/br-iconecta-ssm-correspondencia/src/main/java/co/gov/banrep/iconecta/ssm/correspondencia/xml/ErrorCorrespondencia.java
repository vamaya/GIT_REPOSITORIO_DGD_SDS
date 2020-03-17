package co.gov.banrep.iconecta.ssm.correspondencia.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "error")
public class ErrorCorrespondencia {
	
	private boolean valor;
	private int codigo;	
	private String mensaje;
	
	protected ErrorCorrespondencia(){};
	
	public ErrorCorrespondencia(boolean valor, int codigo, String mensaje) {
		super();
		this.valor = valor;
		this.mensaje = mensaje;
		//this.mensaje = mensaje;
		this.codigo = codigo;
		
	}

	public boolean isValor() {
		return valor;
	}
	
	@XmlElement
	public void setValor(boolean valor) {
		this.valor = valor;
	}
	public int getCodigo() {
		return codigo;
	}
	
	@XmlElement
	public void setCodigo(int codigo) {
		this.codigo = codigo;
	}

	public String getMensaje() {
		return mensaje;
	}
	
	@XmlElement
	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}
	
}
