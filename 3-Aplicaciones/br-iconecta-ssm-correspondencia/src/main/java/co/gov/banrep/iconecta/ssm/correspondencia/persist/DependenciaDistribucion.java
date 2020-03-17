package co.gov.banrep.iconecta.ssm.correspondencia.persist;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
public class DependenciaDistribucion {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dependencia-distribucion-seq-gen")
	@SequenceGenerator(name = "dependencia-distribucion-seq-gen", sequenceName = "SEC_DEPENDENCIA_DISTRIBUCION")
	private long id;
	
	private String nombre;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	
	
}
