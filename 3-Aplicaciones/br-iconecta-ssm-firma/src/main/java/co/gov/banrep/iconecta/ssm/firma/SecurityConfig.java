package co.gov.banrep.iconecta.ssm.firma;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import co.gov.banrep.iconecta.ssm.firma.params.GlobalProps;
import co.gov.banrep.iconecta.ssm.firma.params.SecurityProps;
import co.gov.banrep.iconecta.ssm.firma.utils.Encrypt;

/**
 * Maneja la configuracion de seguridad de la Maquina de Estados, 
 * la seguridad esta basada en el esquema de Spring Boot Security
 * 
 *  @author <a href="mailto:jjrojassa@banrep.gov.co">John Jairo Rojas S.</a>
 *
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private SecurityProps props;
	
	@Autowired
	private GlobalProps globalProperties;

	@Override
	protected void configure(HttpSecurity httpSecurity) throws Exception{
		httpSecurity.authorizeRequests().antMatchers("/respuestafirma").permitAll()
		.and().authorizeRequests().antMatchers("/reporteFirmantes").permitAll()
		.and().authorizeRequests().antMatchers("/iniciarFirmaCaMe").permitAll()
		.and().authorizeRequests().antMatchers("/evaluarFirmante").permitAll()
		.and().authorizeRequests().antMatchers("/monitoreo").permitAll()
		.anyRequest().authenticated()
		.and()
		.httpBasic()
		.and()
		.csrf().disable();		
	}
	
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception{
		
		String pa = "";
				
		try {
			pa = Encrypt.decrypt(globalProperties.getVector(), props.getPaFirma());
		} catch (Exception e) {
			System.err.println("Error obteniedo contraseña");
			e.printStackTrace();			
		}
		
		auth.inMemoryAuthentication().withUser(props.getUsername()).password(pa).roles("ADMIN, SUPERUSER");
	}
	/*
	@Override
	public void configure(WebSecurity web){
		web.ignoring().antMatchers("/respuestafirma", "/iconecta-firma/respuestafirma");
	}
	*/

}
