package co.gov.banrep.iconecta.ssm.correspondencia.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "atributos")
public class Atributos {
		
	private String numeroRadicado;
	private String asunto;
	private String nitFirmante;
	private String listaNitFirmantes;
	private Boolean tieneAnexos;
	private String idsDocsPersonalizados;
	private Boolean esPersonalizado;
	private String tipologia;


	protected Atributos(){};
	
	public Atributos(String numeroRadicado, String asunto, String nitFirmante, String listaNitFirmantes, Boolean tieneAnexos, Boolean esPersonalizado , String idsDocsPersonalizados, String tipologia) {
		super();
		this.numeroRadicado = numeroRadicado;
		this.asunto = asunto;
		this.nitFirmante = nitFirmante;
		this.listaNitFirmantes = listaNitFirmantes;
		this.tieneAnexos = tieneAnexos;
		this.esPersonalizado = esPersonalizado;
		this.idsDocsPersonalizados = idsDocsPersonalizados;
		this.tipologia = tipologia;
		
	}
	
	
	public static Atributos getAtributosEsperarFirma() {
		return new Atributos("", "", "","",false, false, "", "");
	}
	

	public String getNumeroRadicado() {
		return numeroRadicado;
	}

	public String getAsunto() {
		return asunto;
	}
	
	@XmlElement
	public void setAsunto(String asunto) {
		this.asunto = asunto;
	}
	
	@XmlElement
	public void setNumeroRadicado(String numeroRadicado) {
		this.numeroRadicado = numeroRadicado;
	}

	public String getNitFirmante() {
		return nitFirmante;
	}
	
	@XmlElement
	public void setNitFirmante(String nitFirmante) {
		this.nitFirmante = nitFirmante;
	}
	
	public Boolean getTieneAnexos() {
		return tieneAnexos;
	}
	
	@XmlElement
	public void setTieneAnexos(Boolean tieneAnexos) {
		this.tieneAnexos = tieneAnexos;
	}

	public String getIdsDocsPersonalizados() {
		return idsDocsPersonalizados;
	}
	
	@XmlElement
	public void setIdsDocsPersonalizados(String idsDocsPersonalizados) {
		this.idsDocsPersonalizados = idsDocsPersonalizados;
	}

	public Boolean getEsPersonalizado() {
		return esPersonalizado;
	}
	
	@XmlElement
	public void setEsPersonalizado(Boolean esPersonalizado) {
		this.esPersonalizado = esPersonalizado;
	}

	public String getTipologia() {
		return tipologia;
	}

	public void setTipologia(String tipologia) {
		this.tipologia = tipologia;
	}

	public String getListaNitFirmantes() {
		return listaNitFirmantes;
	}
	
	@XmlElement
	public void setListaNitFirmantes(String listaNitFirmantes) {
		this.listaNitFirmantes = listaNitFirmantes;
	}
	
	
	
	

}
