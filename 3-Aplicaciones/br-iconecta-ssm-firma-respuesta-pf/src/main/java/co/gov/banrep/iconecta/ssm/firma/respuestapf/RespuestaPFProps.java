package co.gov.banrep.iconecta.ssm.firma.respuestapf;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * Maneja la propiedad de la URL donde se encuentra desplegada la Maquina de
 * Estados
 * 
 *  @author <a href="mailto:jjrojassa@banrep.gov.co">John Jairo Rojas S.</a>
 *
 */
@ConfigurationProperties
@Component
//@PropertySource("file:${ICONECTAFILES_PATH}/respuesta_pf.properties")
@PropertySource("file:/apps/iconectaFiles_16/respuesta_pf.properties")
public class RespuestaPFProps {
	
	@NotNull
	public static String URL;

	public void setUrl(String url) {
		URL = url;
	}

}
