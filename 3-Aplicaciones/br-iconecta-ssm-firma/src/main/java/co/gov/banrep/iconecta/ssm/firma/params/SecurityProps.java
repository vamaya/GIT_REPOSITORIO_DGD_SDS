package co.gov.banrep.iconecta.ssm.firma.params;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("servletsecurity")
public class SecurityProps {
	
	@NotNull
	private String username;
	@NotNull
	private String cuenta;
	@NotNull
	private String endpoint;
	@NotNull
	private String tipo;	
	@NotNull
	private String paFirma;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getCuenta() {
		return cuenta;
	}
	public void setCuenta(String cuenta) {
		this.cuenta = cuenta;
	}
	public String getEndpoint() {
		return endpoint;
	}
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public String getPaFirma() {
		return paFirma;
	}
	public void setPaFirma(String paFirma) {
		this.paFirma = paFirma;
	}
	
	
	
	
}
