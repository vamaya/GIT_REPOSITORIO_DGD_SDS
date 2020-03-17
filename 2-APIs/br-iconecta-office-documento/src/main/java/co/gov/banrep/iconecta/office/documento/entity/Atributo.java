package co.gov.banrep.iconecta.office.documento.entity;

public class Atributo {
	
	String Etiqueta;
	String valor;
	Coordenada coordenada;
	
	public Atributo(String etiqueta, String valor, Coordenada coordenada) {
		super();
		Etiqueta = etiqueta;
		this.valor = valor;
		this.coordenada = coordenada;
	}

	public String getEtiqueta() {
		return Etiqueta;
	}

	public void setEtiqueta(String etiqueta) {
		Etiqueta = etiqueta;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	public Coordenada getCoordenada() {
		return coordenada;
	}

	public void setCoordenada(Coordenada coordenada) {
		this.coordenada = coordenada;
	}
	

}
