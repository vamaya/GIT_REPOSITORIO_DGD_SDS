package co.gov.banrep.iconecta.ssm.firma.persist;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;

@Entity
public class Maquina {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "maquina-firma-seq-gen")
	@SequenceGenerator(name = "maquina-firma-seq-gen", sequenceName = "SEQ_MAQUINA_FIRMA")
	private long id;
	private String idMaquina;
	private Date ultimaActualizacion;
	private String solicitante;
	private String asunto;	
	private int idCircuito;
	private boolean copiaCompulsada;
	private boolean errorMaquina;
	private int reintento;
	
	//Se inicia la propiedad con una z porque jpa crea las columnas en orden alfabetico
	//y por facilidad de visualizacion de datos de la tabla es mejor que se vea al final
	@Lob
	byte[] zcontexto;
	private String estadoActual;
	
	protected Maquina() {}
	
	public Maquina(String contextOjb, byte[] context, String estadoActual, Date fechaActualizacion){
		this.idMaquina = contextOjb;
		this.zcontexto = context;
		this.estadoActual = estadoActual;
		this.ultimaActualizacion = fechaActualizacion;
	}
	
	public Maquina(long id, String contextOjb, byte[] context, String estadoActual, Date fechaActualizacion){
		this.id = id;
		this.idMaquina = contextOjb;
		this.zcontexto = context;
		this.estadoActual = estadoActual;
		this.ultimaActualizacion = fechaActualizacion;
	}

	
	public Maquina(long id, String idMaquina, Date ultimaActualizacion, String solicitante, String asunto,
			int idCircuito, boolean copiaCompulsada, boolean errorMaquina, int reintento,
			byte[] zcontexto, String estadoActual) {		
		this.id = id;
		this.idMaquina = idMaquina;
		this.ultimaActualizacion = ultimaActualizacion;
		this.solicitante = solicitante;
		this.asunto = asunto;		
		this.idCircuito = idCircuito;
		this.copiaCompulsada = copiaCompulsada;
		this.errorMaquina = errorMaquina;
		this.reintento = reintento;
		this.zcontexto = zcontexto;
		this.estadoActual = estadoActual;
	}
	
	public Maquina(String idMaquina, Date ultimaActualizacion, String solicitante, String asunto,
			int idCircuito, boolean copiaCompulsada, boolean errorMaquina, int reintento,
			byte[] zcontexto, String estadoActual) {		
		this.idMaquina = idMaquina;
		this.ultimaActualizacion = ultimaActualizacion;
		this.solicitante = solicitante;
		this.asunto = asunto;		
		this.idCircuito = idCircuito;
		this.copiaCompulsada = copiaCompulsada;
		this.errorMaquina = errorMaquina;
		this.reintento = reintento;
		this.zcontexto = zcontexto;
		this.estadoActual = estadoActual;
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

	public int getIdCircuito() {
		return idCircuito;
	}

	public void setIdCircuito(int idCircuito) {
		this.idCircuito = idCircuito;
	}

	public boolean isCopiaCompulsada() {
		return copiaCompulsada;
	}

	public void setCopiaCompulsada(boolean copiaCompulsada) {
		this.copiaCompulsada = copiaCompulsada;
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

	
	
}
