package co.gov.banrep.iconecta.ssm.correspondencia.persist;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

@Entity
public class PasoDistribucion {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "paso-distribucion-seq-gen")
	@SequenceGenerator(name = "paso-distribucion-seq-gen", sequenceName = "SEC_PASO_DISTRIBUCION")
	private long id;

	// private long idRuta;
	private int orden;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "RUTA_ID")
	private RutaDistribucion ruta;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CENTRO_DISTRIBUCION_ID")
	private CentroDistribucion centroDistribucion;

	public long getId() {
		return id;
	}

	public CentroDistribucion getCentroDistribucion() {
		return centroDistribucion;
	}

	public void setCentroDistribucion(CentroDistribucion centroDistribucion) {
		this.centroDistribucion = centroDistribucion;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getOrden() {
		return orden;
	}

	public void setOrden(int orden) {
		this.orden = orden;
	}



	public RutaDistribucion getRuta() {
		return ruta;
	}

	public void setRuta(RutaDistribucion ruta) {
		this.ruta = ruta;
	}

}
