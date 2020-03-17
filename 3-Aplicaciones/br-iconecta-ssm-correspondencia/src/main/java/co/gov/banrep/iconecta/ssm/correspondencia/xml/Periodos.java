package co.gov.banrep.iconecta.ssm.correspondencia.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Periodos")
@XmlAccessorType(XmlAccessType.FIELD)
public class Periodos {

	@XmlElement(name = "Periodo")
	private List<Periodo> periodos = new ArrayList<Periodo>();

	public List<Periodo> getPeriodos() {
		return periodos;
	}

	public void setPeriodos(List<Periodo> periodos) {
		this.periodos = periodos;
	}

	
}
