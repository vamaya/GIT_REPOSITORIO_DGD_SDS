package co.gov.banrep.iconecta.ssm.firma;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.gov.banrep.iconecta.cs.autenticacion.AutenticacionCS;
import co.gov.banrep.iconecta.ssm.firma.params.GlobalProps;
import co.gov.banrep.iconecta.ssm.firma.utils.Encrypt;

/**
 * Maneja la configuracion de autenticacion de la Maquina de Estados 
 * contra Content Server de Opentext
 * 
 *  @author <a href="mailto:jjrojassa@banrep.gov.co">John Jairo Rojas S.</a>
 *
 */
@Configuration
@ConfigurationProperties("content")
public class CSAuthenticationConfig {
	
	@NotNull
	private String username;		
	@NotNull
	private String cuenta;
	@NotNull
	private String endpoint;
	@NotNull
	private String tipo;
	@NotNull
	private String authenticationWsdl;
	@NotNull
	private String paCon;
	
	@Autowired
	private GlobalProps globalProperties;
	
	public void setUsername(String username) {
		this.username = username;
	}

	public void setCuenta(String cuenta) {
		this.cuenta = cuenta;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	public void setAuthenticationWsdl(String authenticationWsdl) {
		this.authenticationWsdl = authenticationWsdl;
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
			auth.setPassword(Encrypt.decrypt(globalProperties.getVector(), paCon));
		} catch (Exception e) {
			System.err.println("Error obteniedo contraseña");
			e.printStackTrace();
			return null;
		}
			
		return auth;
	}

}
