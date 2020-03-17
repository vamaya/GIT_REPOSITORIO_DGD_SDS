package co.gov.banrep.iconecta.ssm.correspondencia.xml;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "respuesta")
public class Respuesta {
	
	
	private Atributos atributos;
	
	private ErrorCorrespondencia errorCorrespondencia;
	
	protected Respuesta(){};
	
	public Respuesta(Atributos atributos, ErrorCorrespondencia errorCorrespondencia) {
		super();
		this.atributos = atributos;
		this.errorCorrespondencia = errorCorrespondencia;
	}

	public Atributos getAtributos() {
		return atributos;
	}

	public void setAtributos(Atributos atributos) {
		this.atributos = atributos;
	}

	public ErrorCorrespondencia getError() {
		return errorCorrespondencia;
	}

	public void setError(ErrorCorrespondencia errorCorrespondencia) {
		this.errorCorrespondencia = errorCorrespondencia;
	}

	
	
}
