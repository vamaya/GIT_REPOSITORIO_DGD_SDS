package co.gov.banrep.iconecta.ssm.correspondencia.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Periodo")
@XmlAccessorType(XmlAccessType.FIELD)
public class Periodo {

	@XmlElement(name = "Fondo")
	private List<Fondo> fondos = new ArrayList<Fondo>();

	@XmlAttribute(name = "id")
    String id;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<Fondo> getFondos() {
		return fondos;
	}

	public void setFondos(List<Fondo> fondos) {
		this.fondos = fondos;
	}
	
	
}
