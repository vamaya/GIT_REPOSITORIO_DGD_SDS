package co.gov.banrep.iconecta.ssm.correspondencia.persist;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;

@Entity
public class MaquinaCorrespondencia {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "maquina-correspondencia-seq-gen")
	@SequenceGenerator(name = "maquina-correspondencia-seq-gen", sequenceName = "SEC_MAQUINA_CORRESPONDENCIA" )
	private long id;
	private String idMaquina;

	private String numeroRadicado;
	private String nombreDocOriginal;
	private Long idDocumento;

	private String solicitante;
	private String asunto;
	private boolean errorMaquina;
	private int reintento;
	private Date ultimaActualizacion;

	// Se inicia la propiedad con una z porque jpa crea las columnas en orden
	// alfabetico
	// y por facilidad de visualizacion de datos de la tabla es mejor que se vea al
	// final
	@Lob
	byte[] zcontexto;
	private String estadoActual;

	protected MaquinaCorrespondencia() {
	}

	public MaquinaCorrespondencia(String contextOjb, byte[] context, String estadoActual, Date fechaActualizacion) {
		this.idMaquina = contextOjb;
		this.zcontexto = context;
		this.estadoActual = estadoActual;
		this.ultimaActualizacion = fechaActualizacion;
	}

	public MaquinaCorrespondencia(long id, String contextOjb, byte[] context, String estadoActual,
			Date fechaActualizacion) {
		this.id = id;
		this.idMaquina = contextOjb;
		this.zcontexto = context;
		this.estadoActual = estadoActual;
		this.ultimaActualizacion = fechaActualizacion;
	}

	public MaquinaCorrespondencia(long id, String idMaquina, Date ultimaActualizacion, String solicitante,
			String asunto, boolean errorMaquina, int reintento, byte[] zcontexto, String estadoActual, Long idDocumento,
			String numeroRadicado, String nombreDocOriginal) {
		this.id = id;
		this.idMaquina = idMaquina;
		this.ultimaActualizacion = ultimaActualizacion;
		this.solicitante = solicitante;
		this.asunto = asunto;
		this.errorMaquina = errorMaquina;
		this.reintento = reintento;
		this.zcontexto = zcontexto;
		this.estadoActual = estadoActual;
		this.idDocumento = idDocumento;
		this.numeroRadicado = numeroRadicado;
		this.nombreDocOriginal = nombreDocOriginal;
	}

	public MaquinaCorrespondencia(String idMaquina, Date ultimaActualizacion, String solicitante, String asunto,
			boolean errorMaquina, int reintento, byte[] zcontexto, String estadoActual, Long idDocumento,
			String numeroRadicado, String nombreDocOriginal) {
		this.idMaquina = idMaquina;
		this.ultimaActualizacion = ultimaActualizacion;
		this.solicitante = solicitante;
		this.asunto = asunto;
		this.errorMaquina = errorMaquina;
		this.reintento = reintento;
		this.zcontexto = zcontexto;
		this.estadoActual = estadoActual;
		this.idDocumento = idDocumento;
		this.numeroRadicado = numeroRadicado;
		this.nombreDocOriginal = nombreDocOriginal;
	}

	public String getIdMaquina() {
		return idMaquina;
	}

	public void setIdMaquina(String idMaquina) {
		this.idMaquina = idMaquina;
	}

	public byte[] getContexto() {
		return zcontexto;
	}

	public void setContexto(byte[] contexto) {
		this.zcontexto = contexto;
	}

	public String getEstadoActual() {
		return estadoActual;
	}

	public void setEstadoActual(String estadoActual) {
		this.estadoActual = estadoActual;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getUltimaActualizacion() {
		return ultimaActualizacion;
	}

	public void setUltimaActualizacion(Date ultimaActualizacion) {
		this.ultimaActualizacion = ultimaActualizacion;
	}

	public String getSolicitante() {
		return solicitante;
	}

	public void setSolicitante(String solicitante) {
		this.solicitante = solicitante;
	}

	public String getAsunto() {
		return asunto;
	}

	public void setAsunto(String asunto) {
		this.asunto = asunto;
	}

	public boolean isErrorMaquina() {
		return errorMaquina;
	}

	public void setErrorMaquina(boolean errorMaquina) {
		this.errorMaquina = errorMaquina;
	}

	public int getReintento() {
		return reintento;
	}

	public void setReintento(int reintento) {
		this.reintento = reintento;
	}

	public String getNumeroRadicado() {
		return numeroRadicado;
	}

	public void setNumeroRadicado(String numeroRadicado) {
		this.numeroRadicado = numeroRadicado;
	}

	public Long getIdDocumento() {
		return idDocumento;
	}

	public void setIdDocumento(Long idDocumento) {
		this.idDocumento = idDocumento;
	}

	public String getNombreDocOriginal() {
		return nombreDocOriginal;
	}

	public void setNombreDocOriginal(String nombreDocOriginal) {
		this.nombreDocOriginal = nombreDocOriginal;
	}

}
