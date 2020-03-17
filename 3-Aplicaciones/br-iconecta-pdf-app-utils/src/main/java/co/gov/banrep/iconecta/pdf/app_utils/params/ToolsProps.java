package co.gov.banrep.iconecta.pdf.app_utils.params;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("tools")
@Component
public class ToolsProps {
	@NotNull
	private String rutaPdf;
	@NotNull
	private String rutaMerge;
	@NotNull
	private String extension;
	@NotNull
	private String vectorCifrado;
	
	private Adlib adlib = new Adlib();
	
	public String getRutaPdf() {
		return rutaPdf;
	}

	public void setRutaPdf(String rutaPdf) {
		this.rutaPdf = rutaPdf;
	}

	public String getRutaMerge() {
		return rutaMerge;
	}

	public void setRutaMerge(String rutaMerge) {
		this.rutaMerge = rutaMerge;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	
	
	public String getVectorCifrado() {
		return vectorCifrado;
	}

	public void setVectorCifrado(String vectorCifrado) {
		this.vectorCifrado = vectorCifrado;
	}

	public Adlib getAdlib() {
		return adlib;
	}

	public void setAdlib(Adlib adlib) {
		this.adlib = adlib;
	}

	public static class Adlib {
		
		private String rutaEntrada;
		private String rutaSalida;
		private long tiempoReintento;
		private long tiempoReintentoDos;
		
		public String getRutaEntrada() {
			return rutaEntrada;
		}
		public void setRutaEntrada(String rutaEntrada) {
			this.rutaEntrada = rutaEntrada;
		}
		public String getRutaSalida() {
			return rutaSalida;
		}
		public void setRutaSalida(String rutaSalida) {
			this.rutaSalida = rutaSalida;
		}
		public long getTiempoReintento() {
			return tiempoReintento;
		}
		public void setTiempoReintento(long tiempoReintento) {
			this.tiempoReintento = tiempoReintento;
		}
		public long getTiempoReintentoDos() {
			return tiempoReintentoDos;
		}
		public void setTiempoReintentoDos(long tiempoReintentoDos) {
			this.tiempoReintentoDos = tiempoReintentoDos;
		}
		
		
	}
	
}
