package co.gov.banrep.iconecta.ssm.firma.entity;

import co.gov.banrep.iconecta.ssm.firma.enums.AtributosCategorias;

public class EntradaCategoriaFirma {
	
	private String etiquetaPF;
	private String etiquetaCS;
	private AtributosCategorias atributoCS;
	
	public EntradaCategoriaFirma(String etiquetaPF, String etiquetaCS, AtributosCategorias atributoCS) {
		super();
		this.etiquetaPF = etiquetaPF;
		this.etiquetaCS = etiquetaCS;
		this.atributoCS = atributoCS;
	}

	public String getEtiquetaPF() {
		return etiquetaPF;
	}

	public void setEtiquetaPF(String etiquetaPF) {
		this.etiquetaPF = etiquetaPF;
	}

	public String getEtiquetaCS() {
		return etiquetaCS;
	}

	public void setEtiquetaCS(String etiquetaCS) {
		this.etiquetaCS = etiquetaCS;
	}

	public AtributosCategorias getAtributoCS() {
		return atributoCS;
	}

	public void setAtributoCS(AtributosCategorias atributoCS) {
		this.atributoCS = atributoCS;
	}
	
		

}
