package co.gov.banrep.iconecta.ssm.correspondencia.params;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("estadoCorrespondencia")
@Component
public class EstadoCorrespondenciaProps {
	
	@NotNull
	private String rechazo;
	@NotNull
	private String anulado;
	@NotNull
	private String confirmado;
	@NotNull
	private String distribuido;
	@NotNull
	private String rechazadoAlgunos;
	@NotNull
	private String radicado;
	
	public String getRechazo() {
		return rechazo;
	}
	public void setRechazo(String rechazo) {
		this.rechazo = rechazo;
	}
	public String getAnulado() {
		return anulado;
	}
	public void setAnulado(String anulado) {
		this.anulado = anulado;
	}
	public String getConfirmado() {
		return confirmado;
	}
	public void setConfirmado(String confirmado) {
		this.confirmado = confirmado;
	}
	public String getDistribuido() {
		return distribuido;
	}
	public void setDistribuido(String distribuido) {
		this.distribuido = distribuido;
	}
	public String getRechazadoAlgunos() {
		return rechazadoAlgunos;
	}
	public void setRechazadoAlgunos(String rechazadoAlgunos) {
		this.rechazadoAlgunos = rechazadoAlgunos;
	}
	public String getRadicado() {
		return radicado;
	}
	public void setRadicado(String radicado) {
		this.radicado = radicado;
	}	
	
}
