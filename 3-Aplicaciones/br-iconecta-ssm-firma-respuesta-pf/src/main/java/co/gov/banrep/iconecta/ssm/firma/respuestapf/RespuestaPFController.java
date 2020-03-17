package co.gov.banrep.iconecta.ssm.firma.respuestapf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controlador de las peticiones web de la apliacion Respuesta Porta Firmas
 * 
 * @author <a href="mailto:jjrojassa@banrep.gov.co">John Jairo Rojas S.</a>
 *
 */
@Controller
public class RespuestaPFController {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * Metodo para monitoreo del Banco
	 * 
	 * @return
	 */
	@RequestMapping(value = "/monitoreo", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
	public @ResponseBody String monitoreo() {

		log.info("Entro al controller /monitoreo");
		return "<html>\r\n" + "  <head>\r\n" + "  <meta charset=\"UTF-8\">\r\n" + "</head>\r\n" + "  <body>\r\n"
				+ "    <p>\r\n" + "      STATUS: OK\r\n" + "    </p>\r\n" + "  </body>\r\n" + "</html>";
	}

}
