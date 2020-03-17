package co.gov.banrep.iconecta.ssm.correspondencia.persist;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
public class PruebaEntrega {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "prueba-entrega-seq-gen")
	@SequenceGenerator(name = "prueba-entrega-seq-gen", sequenceName = "SEC_PRUEBA_ENTREGA" )
	private long id;
	private String idMaquina;
	private String numeroRadicado;
	private String nombreDocumento;
	private String numeroGuia;
	private String currier;
	private String solicitante;
	private String destino;
	private Date fechaDistribuido;
	private Date fechaRecibido;
	
	public long getId() {
		return id;
	}
	
	protected PruebaEntrega() {
		
	}
	
	public PruebaEntrega(String idMaquina, String numeroRadicado, String nombreDocumento, String numeroGuia, String currier, String solicitante, String destino, Date fechaDistribuido, Date fechaRecibido) {
		
		this.idMaquina = idMaquina;
		this.numeroRadicado = numeroRadicado;
		this.nombreDocumento = nombreDocumento;
		this.numeroGuia = numeroGuia;
		this.currier = currier;
		this.solicitante = solicitante;
		this.destino = destino;
		this.fechaDistribuido = fechaDistribuido;
		this.fechaRecibido = fechaRecibido;
	}
	
	public PruebaEntrega(Long id, String idMaquina, String numeroRadicado, String nombreDocumento, String numeroGuia, String currier, String solicitante, String destino, Date fechaDistribuido, Date fechaRecibido) {
		
		this.id = id;
		this.idMaquina = idMaquina;
		this.numeroRadicado = numeroRadicado;
		this.nombreDocumento = nombreDocumento;
		this.numeroGuia = numeroGuia;
		this.currier = currier;
		this.solicitante = solicitante;
		this.destino = destino;
		this.fechaDistribuido = fechaDistribuido;
		this.fechaRecibido = fechaRecibido;
	}
	
	public PruebaEntrega(Long id, String numeroGuia, String currier, Date fechaRecibido) {
		
		this.id = id;
		this.numeroGuia = numeroGuia;
		this.currier = currier;
		
		this.fechaRecibido = fechaRecibido;
	}
	
	
	public void setId(long id) {
		this.id = id;
	}
	public String getIdMaquina() {
		return idMaquina;
	}
	public void setIdMaquina(String idMaquina) {
		this.idMaquina = idMaquina;
	}
	public String getNumeroRadicado() {
		return numeroRadicado;
	}
	public void setNumeroRadicado(String numeroRadicado) {
		this.numeroRadicado = numeroRadicado;
	}
	public String getNombreDocumento() {
		return nombreDocumento;
	}
	public void setNombreDocumento(String nombreDocumento) {
		this.nombreDocumento = nombreDocumento;
	}
	public String getNumeroGuia() {
		return numeroGuia;
	}
	public void setNumeroGuia(String numeroGuia) {
		this.numeroGuia = numeroGuia;
	}
	public String getCurrier() {
		return currier;
	}
	public void setCurrier(String currier) {
		this.currier = currier;
	}
	public String getSolicitante() {
		return solicitante;
	}
	public void setSolicitante(String solicitante) {
		this.solicitante = solicitante;
	}
	public String getDestino() {
		return destino;
	}
	public void setDestino(String destino) {
		this.destino = destino;
	}
	public Date getFechaDistribuido() {
		return fechaDistribuido;
	}
	public void setFechaDistribuido(Date fechaDistribuido) {
		this.fechaDistribuido = fechaDistribuido;
	}
	public Date getFechaRecibido() {
		return fechaRecibido;
	}
	public void setFechaRecibido(Date fechaRecibido) {
		this.fechaRecibido = fechaRecibido;
	}

}
