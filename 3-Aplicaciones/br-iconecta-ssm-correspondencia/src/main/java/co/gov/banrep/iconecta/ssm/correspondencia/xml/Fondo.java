package co.gov.banrep.iconecta.ssm.correspondencia.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Fondo")
@XmlAccessorType(XmlAccessType.FIELD)
public class Fondo {
	
	@XmlElement(name = "Carpeta")
	private List<Carpeta> carpetas = new ArrayList<Carpeta>();

	@XmlAttribute(name = "id")
    String id;
	
	public List<Carpeta> getCarpetas() {
		return carpetas;
	}

	public void setCarpetas(List<Carpeta> carpetas) {
		this.carpetas = carpetas;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
