package co.gov.banrep.iconecta.ssm.firma.params;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("portafirmas")
@Component
public class PortafirmasProps {
	@NotNull
	@Min(1)
	@Max(10)
	private int diaslimite;
	
	@NotNull
	private String ticketadmin;

	@NotNull
	private String nifadmin;

	@NotNull
	private String url;
		
	private Etiqueta etiqueta = new Etiqueta();	

	public static class Etiqueta {	

		@NotNull
		private String nombrefirmante;

		@NotNull
		private String fechafirma;

		@NotNull
		private String entidad;

		@NotNull
		private String serial;

		@NotNull
		private String hash;

		@NotNull
		private String algoritmo;
		
		@NotNull
		private String cantidadfirmantes;
		
		public String getNombrefirmante() {
			return nombrefirmante;
		}

		public void setNombrefirmante(String nombrefirmante) {
			this.nombrefirmante = nombrefirmante;
		}

		public String getFechafirma() {
			return fechafirma;
		}

		public void setFechafirma(String fechafirma) {
			this.fechafirma = fechafirma;
		}

		public String getEntidad() {
			return entidad;
		}

		public void setEntidad(String entidad) {
			this.entidad = entidad;
		}

		public String getSerie() {
			return serial;
		}

		public void setSerial(String serial) {
			this.serial = serial;
		}

		public String getHash() {
			return hash;
		}

		public void setHash(String hash) {
			this.hash = hash;
		}

		public String getAlgoritmo() {
			return algoritmo;
		}

		public void setAlgoritmo(String algoritmo) {
			this.algoritmo = algoritmo;
		}

		public String getCantidadfirmantes() {
			return cantidadfirmantes;
		}

		public void setCantidadfirmantes(String cantidadfirmantes) {
			this.cantidadfirmantes = cantidadfirmantes;
		}

		public String getSerial() {
			return serial;
		}
		
	}
	
	
	
	public int getDiaslimite() {
		return diaslimite;
	}

	public void setDiaslimite(int diaslimite) {
		this.diaslimite = diaslimite;
	}

	public Etiqueta getEtiqueta() {
		return etiqueta;
	}

	public void setEtiqueta(Etiqueta etiqueta) {
		this.etiqueta = etiqueta;
	}

	public String getTicketadmin() {
		return ticketadmin;
	}

	public void setTicketadmin(String ticketadmin) {
		this.ticketadmin = ticketadmin;
	}

	public String getNifadmin() {
		return nifadmin;
	}

	public void setNifadmin(String nifadmin) {
		this.nifadmin = nifadmin;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
		
}
