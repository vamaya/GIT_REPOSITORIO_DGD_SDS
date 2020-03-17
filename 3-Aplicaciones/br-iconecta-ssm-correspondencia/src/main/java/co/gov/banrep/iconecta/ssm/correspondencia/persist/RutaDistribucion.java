package co.gov.banrep.iconecta.ssm.correspondencia.persist;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;

@Entity
public class RutaDistribucion {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ruta-distribucion-seq-gen")
	@SequenceGenerator(name = "ruta-distribucion-seq-gen", sequenceName = "SEC_RUTA_DISTRIBUCION")
	private long id;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy="ruta", cascade = CascadeType.ALL)
	private List <PasoDistribucion> pasos;
	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CENTRO_DISTRIBUCION_ORIGEN_ID")
	private CentroDistribucion centroDistribucionOrigen;
	

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CENTRO_DISTRIBUCION_DESTINO_ID")
	private CentroDistribucion centroDistribucionDestino;
	
	private String tipologia;
	
	public List<PasoDistribucion> getPasos() {
		return pasos;
	}
	
	
	public void setPasos(List<PasoDistribucion> pasos) {
		this.pasos = pasos;
	}
	

	public CentroDistribucion getCentroDistribucionOrigen() {
		return centroDistribucionOrigen;
	}


	public void setCentroDistribucionOrigen(CentroDistribucion centroDistribucionOrigen) {
		this.centroDistribucionOrigen = centroDistribucionOrigen;
	}


	public CentroDistribucion getCentroDistribucionDestino() {
		return centroDistribucionDestino;
	}


	public void setCentroDistribucionDestino(CentroDistribucion centroDistribucionDestino) {
		this.centroDistribucionDestino = centroDistribucionDestino;
	}


	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}


	public String getTipologia() {
		return tipologia;
	}


	public void setTipologia(String tipologia) {
		this.tipologia = tipologia;
	}
	
	

}
