package co.gov.banrep.iconecta.ssm.correspondencia.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.util.StringUtils;

@XmlRootElement(name = "distribucionResponse")
public class RespuestaXmlDer {
	
	//private long idRolSiguiente;
	private long rolSiguienteId;
	private String rolSiguiente;
	private boolean isFinalizo;
	private boolean isError;
	private int codigoError;
	private String mensajeError;
	
	protected RespuestaXmlDer() {};
	
		
	private RespuestaXmlDer(long rolSiguienteId, String rolSiguiente, boolean isFinalizo, boolean isError) {
		super();
		this.rolSiguienteId = rolSiguienteId;
		this.rolSiguiente = rolSiguiente;
		this.isFinalizo = isFinalizo;
		this.isError = isError;
	}
	
	
	private RespuestaXmlDer(boolean isError, int codigoError, String mensajeError) {
		super();
		this.isError = isError;
		this.codigoError = codigoError;
		this.mensajeError = mensajeError;
	}
	
	public static RespuestaXmlDer getRespuestaXmlDerRolSiguiente(long rolSiguienteId, String rolSiguiente) {
		return new RespuestaXmlDer(rolSiguienteId, rolSiguiente, false, false);
	}
	
	public static RespuestaXmlDer getRespuestaXmlDerFinalizo() {
		return new RespuestaXmlDer(0L,"", true, false);
	}

	public static RespuestaXmlDer getRespuestaXmlDerError(int codigoError, String mensajeError) {

		if (codigoError == 0) {
			mensajeError = "NO ESPECIFICADO";
		}

		if (!StringUtils.hasText(mensajeError)) {
			mensajeError = "NO ESPECIFICADO";
		}
		return new RespuestaXmlDer(true, codigoError, mensajeError);
	}
	
	



	
	public long getRolSiguienteId() {
		return rolSiguienteId;
	}

	@XmlElement
	public void setRolSiguienteId(long rolSiguienteId) {
		this.rolSiguienteId = rolSiguienteId;
	}


	public String getRolSiguiente() {
		return rolSiguiente;
	}
	
	@XmlElement
	public void setRolSiguiente(String rolSiguiente) {
		this.rolSiguiente = rolSiguiente;
	}
	public boolean isFinalizo() {
		return isFinalizo;
	}
	
	@XmlElement
	public void setFinalizo(boolean isFinalizo) {
		this.isFinalizo = isFinalizo;
	}
	public boolean isError() {
		return isError;
	}
	
	@XmlElement
	public void setError(boolean isError) {
		this.isError = isError;
	}
	public int getCodigoError() {
		return codigoError;
	}
	
	@XmlElement
	public void setCodigoError(int codigoError) {
		this.codigoError = codigoError;
	}
	public String getMensajeError() {
		return mensajeError;
	}
	
	@XmlElement
	public void setMensajeError(String mensajeError) {
		this.mensajeError = mensajeError;
	}
	
	@Override
	public String toString() {
		return "RespuestaXMLDer rolSiguienteId ["+ rolSiguienteId + "] - rolSiguiente[" + rolSiguiente + "] - isFinalizo [" + isFinalizo + "] - isError ["
				+ isError + "] - codigoError [" + codigoError + "] - mensajeError [" + mensajeError + "]";
	}

}
