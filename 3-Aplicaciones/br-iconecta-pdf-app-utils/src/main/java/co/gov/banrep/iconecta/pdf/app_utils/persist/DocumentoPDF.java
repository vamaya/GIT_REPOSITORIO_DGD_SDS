package co.gov.banrep.iconecta.pdf.app_utils.persist;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
public class DocumentoPDF {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "documentopdf-seq-gen")
	@SequenceGenerator(name = "documentopdf-seq-gen", sequenceName = "SEC_DOCUMENTOPDF")
	private long id;
	
	private long idworkflow;
	
	private long idDocumento;
	
	private Date fecha;
	
	private String estado;
	
	private String descripcionEstado;

	public DocumentoPDF( long idworkflow, long idDocumento, Date fecha, String estado, String descripcionEstado) {
		this.idworkflow = idworkflow;
		this.idDocumento = idDocumento;
		this.fecha = fecha;
		this.estado = estado;
		this.descripcionEstado = descripcionEstado;
	}
	
	public DocumentoPDF( String estado) {
		this.estado = estado;
	}
	
	public DocumentoPDF( int id,long idworkflow, long idDocumento, Date fecha, String estado, String descripcionEstado) {
		this.id = -1;
		this.idworkflow = idworkflow;
		this.idDocumento = idDocumento;
		this.fecha = fecha;
		this.estado = estado;
		this.descripcionEstado = descripcionEstado;
	}

	public DocumentoPDF() {
		this.id = -1;
		this.idworkflow = -1;
		this.idDocumento = -1;
		this.fecha = new Date();
		this.estado = "";
		
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getIdworkflow() {
		return idworkflow;
	}

	public void setIdworkflow(long idworkflow) {
		this.idworkflow = idworkflow;
	}

	public long getIdDocumento() {
		return idDocumento;
	}

	public void setIdDocumento(long idDocumento) {
		this.idDocumento = idDocumento;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public String getDescripcionEstado() {
		return descripcionEstado;
	}

	public void setDescripcionEstado(String descripcionEstado) {
		this.descripcionEstado = descripcionEstado;
	}
	
	
}
