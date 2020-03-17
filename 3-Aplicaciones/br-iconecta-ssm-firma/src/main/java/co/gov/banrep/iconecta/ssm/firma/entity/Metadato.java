package co.gov.banrep.iconecta.ssm.firma.entity;

public class Metadato {
	
	private String valor;
	private String etiquetaPF;
	private String etiquetaCS;
		
	
	public Metadato(String etiquetaCS, String etiquetaPF, String valor) {
		super();
		this.valor = valor;
		this.etiquetaCS = etiquetaCS;
		this.etiquetaPF = etiquetaPF;
	}
	
	public Metadato(String etiquetaCS, String valor) {
		super();
		this.valor = valor;
		this.etiquetaCS = etiquetaCS;
		this.etiquetaPF = "-";
	}
	
	public String getValor() {
		return valor;
	}
	public void setValor(String valor) {
		this.valor = valor;
	}
	public String getEtiquetaPF() {
		return etiquetaPF;
	}
	public void setEtiquetaPF(String etiqueta) {
		this.etiquetaPF = etiqueta;
	}
	public String getEtiquetaCS() {
		return etiquetaCS;
	}
	public void setEtiquetaCS(String etiquetaCS) {
		this.etiquetaCS = etiquetaCS;
	}
	


}
