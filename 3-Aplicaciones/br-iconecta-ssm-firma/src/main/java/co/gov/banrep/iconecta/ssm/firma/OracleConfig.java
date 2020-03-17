package co.gov.banrep.iconecta.ssm.firma;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.gov.banrep.iconecta.ssm.firma.params.GlobalProps;
import co.gov.banrep.iconecta.ssm.firma.utils.Encrypt;

//import com.ca.ppm.clients.utils.CAPUPMException;

//import co.gov.banrep.aipec.Extractor;
//import oracle.jdbc.pool.OracleDataSource;
//import com.oracle.*;
import oracle.jdbc.pool.OracleDataSource;

/**
 * Maneja la configuracion de la base de datos
 * 
 * @author <a href="mailto:jjrojassa@banrep.gov.co">John Jairo Rojas S.</a>
 *
 */
@Configuration
@ConfigurationProperties("oracle")
public class OracleConfig {

	@NotNull
	private String username;
	@NotNull
	private String cuenta;
	@NotNull
	private String endpoint;
	@NotNull
	private String tipo;
	@NotNull
	private String url;
	@NotNull
	private String paBD;

	@Autowired
	private GlobalProps globalProperties;
	
	public void setCuenta(String cuenta) {
		this.cuenta = cuenta;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setPaBD(String paBD) {
		this.paBD = paBD;
	}

	@Bean
	public DataSource dataSource() throws Exception {
		OracleDataSource dataSource = new OracleDataSource();
		dataSource.setUser(username);

		try {
			dataSource.setPassword(Encrypt.decrypt(globalProperties.getVector(), paBD));
		} catch (Exception e) {
			System.err.println("Error obteniedo contraseña");
			e.printStackTrace();
			return null;
		}		

		dataSource.setURL(url);
		dataSource.setImplicitCachingEnabled(true);
		dataSource.setFastConnectionFailoverEnabled(true);
		return dataSource;
	}

}
