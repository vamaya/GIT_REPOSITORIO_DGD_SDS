package co.gov.banrep.iconecta.ssm.firma.persist;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;

@Entity
public class Contexto {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "maquina-contexto-seq-gen")
	@SequenceGenerator(name = "maquina-contexto-seq-gen", sequenceName = "SEQ_MAQUINA_CONTEXTO")
	private long id;
	private String idMaquina;
	private Date fecha;
	
	@Lob
	private byte[] variables;
	
	protected Contexto() {};
	
	public Contexto(String idMaquina, Date fecha, byte[] variables) {
		super();
		this.idMaquina = idMaquina;
		this.fecha = fecha;
		this.variables = variables;
	}
	
	public Contexto(long id, String idMaquina, Date fecha, byte[] variables) {
		super();
		this.id = id;
		this.idMaquina = idMaquina;
		this.fecha = fecha;
		this.variables = variables;
	}

	public String getIdMaquina() {
		return idMaquina;
	}

	public void setIdMaquina(String idMaquina) {
		this.idMaquina = idMaquina;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public byte[] getVariables() {
		return variables;
	}

	public void setVariables(byte[] variables) {
		this.variables = variables;
	}

	public long getId() {
		return id;
	}
	
	

}
