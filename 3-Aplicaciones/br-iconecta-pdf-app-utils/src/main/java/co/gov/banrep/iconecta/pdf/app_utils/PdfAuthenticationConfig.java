package co.gov.banrep.iconecta.pdf.app_utils;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import co.gov.banrep.iconecta.cs.autenticacion.AutenticacionCS;
import co.gov.banrep.iconecta.pdf.app_utils.params.ToolsProps;
import co.gov.banrep.iconecta.pdf.app_utils.utils.Encrypt;

@Configuration
//@PropertySource("file:${ICONECTAFILES_PATH}/pdf-utils.properties")
@PropertySource("file:/apps/iconectaFiles_16/pdf-utils.properties")
@ConfigurationProperties("content")
public class PdfAuthenticationConfig {
	

	@NotNull
	private String username;
	@NotNull
	private String authenticationWsdl;
	@NotNull
	private String cuenta;
	@NotNull
	private String endpoint;
	@NotNull
	private String tipo;
	@NotNull
	private String paCon;

	@Autowired
	private ToolsProps toolsProps;

	public String getAuthenticationWsdl() {
		return authenticationWsdl;
	}

	public void setAuthenticationWsdl(String authenticationWsdl) {
		this.authenticationWsdl = authenticationWsdl;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setCuenta(String cuenta) {
		this.cuenta = cuenta;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	
	public void setPaCon(String paCon) {
		this.paCon = paCon;
	}

	@Bean
	public AutenticacionCS autenticacionCS() throws Exception{
		AutenticacionCS auth = new AutenticacionCS();
		
		auth.setUsuarioAdmin(username);
		auth.setUrlWSDL(authenticationWsdl);	

		try {
			auth.setPassword(Encrypt.decrypt(toolsProps.getVectorCifrado(), paCon));
		} catch (Exception e) {
			System.err.println("Error obteniedo contraseña");
			e.printStackTrace();
			return null;
		}

		return auth;
	}

}
