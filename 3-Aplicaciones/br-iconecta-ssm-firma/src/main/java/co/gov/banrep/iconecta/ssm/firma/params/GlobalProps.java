package co.gov.banrep.iconecta.ssm.firma.params;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties
@Component
public class GlobalProps {
	
	//private static int NUMERO_MAXIMO_REINTENTOS = 10;
	//private static long TIEMPO_MAXIMO_REINTENTO = 3601000;
	
	@Max(3601000)
	@Min(60000)
	private long tiempoReintento;
	private String rutaTemporal = "";
	
	//@NotNull	
	@Max(10000000)
	@Min(1)
	private long idConsultaFirmantes;
	
	@NotNull
	@Max(10)
	@Min(1)
	private int numeroReintentos;
	
	private String vector;

	public long getTiempoReintento() {
		return tiempoReintento;
	}

	public void setTiempoReintento(long tiempoReintento) {
		this.tiempoReintento = tiempoReintento;
	}

	public String getRutaTemporal() {
		return rutaTemporal;
	}

	public void setRutaTemporal(String rutaTemporal) {
		this.rutaTemporal = rutaTemporal;
	}

	public int getNumeroReintentos() {
		return numeroReintentos;
	}

	public void setNumeroReintentos(int numeroReintentos) {
		this.numeroReintentos = numeroReintentos;
	}
	
	public long getIdConsultaFirmantes() {
		return idConsultaFirmantes;
	}

	public void setIdConsultaFirmantes(long idConsultaFirmantes) {
		this.idConsultaFirmantes = idConsultaFirmantes;
	}

	public String getVector() {
		return vector;
	}

	public void setVector(String vector) {
		this.vector = vector;
	}
		
	
}
