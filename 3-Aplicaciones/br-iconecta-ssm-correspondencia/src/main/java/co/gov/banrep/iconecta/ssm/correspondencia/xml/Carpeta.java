package co.gov.banrep.iconecta.ssm.correspondencia.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Carpeta")
@XmlAccessorType(XmlAccessType.FIELD)
public class Carpeta {
	
	@XmlAttribute(name = "id")
    String id;
	@XmlElement
	private String ene;
	@XmlElement
	private String feb;
	@XmlElement
	private String mar;
	@XmlElement
	private String abr;
	@XmlElement
	private String may;
	@XmlElement
	private String jun;
	@XmlElement
	private String jul;
	@XmlElement
	private String ago;
	@XmlElement
	private String sep;
	@XmlElement
	private String oct;
	@XmlElement
	private String nov;
	@XmlElement
	private String dic;
	@XmlElement
	private String tipoDocumental;
	@XmlElement
	private String serie;
	
	public String getEne() {
		return ene;
	}
	public void setEne(String ene) {
		this.ene = ene;
	}
	public String getFeb() {
		return feb;
	}
	public void setFeb(String feb) {
		this.feb = feb;
	}
	public String getMar() {
		return mar;
	}
	public void setMar(String mar) {
		this.mar = mar;
	}
	public String getAbr() {
		return abr;
	}
	public void setAbr(String abr) {
		this.abr = abr;
	}
	public String getMay() {
		return may;
	}
	public void setMay(String may) {
		this.may = may;
	}
	public String getJun() {
		return jun;
	}
	public void setJun(String jun) {
		this.jun = jun;
	}
	public String getJul() {
		return jul;
	}
	public void setJul(String jul) {
		this.jul = jul;
	}
	public String getAgo() {
		return ago;
	}
	public void setAgo(String ago) {
		this.ago = ago;
	}
	public String getSep() {
		return sep;
	}
	public void setSep(String sep) {
		this.sep = sep;
	}
	public String getOct() {
		return oct;
	}
	public void setOct(String oct) {
		this.oct = oct;
	}
	public String getNov() {
		return nov;
	}
	public void setNov(String nov) {
		this.nov = nov;
	}
	public String getDic() {
		return dic;
	}
	public void setDic(String dic) {
		this.dic = dic;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTipoDocumental() {
		return tipoDocumental;
	}
	public String getSerie() {
		return serie;
	}
	public void setTipoDocumental(String tipoDocumental) {
		this.tipoDocumental = tipoDocumental;
	}
	public void setSerie(String serie) {
		this.serie = serie;
	}


	
}
