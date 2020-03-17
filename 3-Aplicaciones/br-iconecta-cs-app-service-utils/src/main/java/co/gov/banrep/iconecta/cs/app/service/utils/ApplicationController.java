package co.gov.banrep.iconecta.cs.app.service.utils;

import java.io.IOException;

import javax.xml.soap.SOAPException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import co.gov.banrep.iconecta.cs.autenticacion.AutenticacionCS;
import co.gov.banrep.iconecta.cs.documento.ContenidoDocumento;
import co.gov.banrep.iconecta.cs.documento.Nodo;

/**
 * Controlador de las peticiones web de la apliacion Service Utils
 * 
 *  @author <a href="mailto:jjrojassa@banrep.gov.co">John Jairo Rojas S.</a>
 *
 */
@Controller
public class ApplicationController {
	
	private static final String MENSAJE_ERROR_COPIAR = "ERROR copiando adjunto";
	private static final String MENSAJE_ERROR_MOVER = "ERROR moviendo adjunto";
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private ApplicationConfig props;
	
	@Autowired
	private AutenticacionCS autenticacionCS;
	
	@Autowired
	private Nodo nodo;
	
	/*
	@RequestMapping(value = "/copiarAdjunto",   method = RequestMethod.GET,   produces = MediaType.APPLICATION_XML_VALUE)	
	public @ResponseBody RespuestaXml copiarAdjunto(@RequestParam(value="idParent", required=true)long idParent,
			@RequestParam(value="idDocumento", required=true)long idDocumento){
		
		Long idDocumentoCopiado = new Long(-1);
		String nombreDocumento = "";
		String nombreExtensionPDF = "";
		
		log.info("INICIO Copiar adjunto - Documento id Content: " + idDocumento );			
		
		try {
			nombreDocumento = ContenidoDocumento.obtenerNombreDocumento(autenticacionCS.getUserSoapHeader(props.getUsername()), idDocumento,props.getDocumentoWsdl());
			nombreExtensionPDF= StringUtils.substringBefore(nombreDocumento, ".") + ".pdf";
		} catch (IOException | SOAPException | InterruptedException e) {
			log.error("No se pudo obtener el nombre del documento [" + idDocumento + "]");
			log.error("Error de Content Server", e);			
			return new RespuestaXml(false, MENSAJE_ERROR_COPIAR);
		} 
		
		try {
			idDocumentoCopiado = copiarNodo(idParent, idDocumento, nombreExtensionPDF,props.getDocumentoWsdl());
			//log.info("El documento (" + idDocumento + ") fue copiado exitosamente en la carpeta (" + idParent + ")");
		}  catch (Exception e) {
			log.error("No se pudo copiar el documento [" + idDocumento + "]");
			log.error("Error de Content Server", e);			
			return new RespuestaXml(false, MENSAJE_ERROR_COPIAR);
		} 							
		
		log.info("FIN Copiar adjunto");	
		
		return new RespuestaXml(true, "OPERACION EXITOSA", idDocumentoCopiado);
	}
	*/
		
	@RequestMapping(value = "/nuevaVersion",   method = RequestMethod.GET,   produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody RespuestaXml nuevaVersion(@RequestParam(value="idOriginal", required=true)long idOriginal,
			@RequestParam(value="idNuevo", required=true)long idNuevo/*, Model model*/){
		
		log.info("INICIO subir nueva version");
		log.info("Agregando al documento original [" + idOriginal + "] el nuevo documento [" + idNuevo + "] como nueva version" );
		try {
			ContenidoDocumento.cargarNuevaVersionDocumentoExistente(autenticacionCS.getUserSoapHeader(props.getUsername()), idOriginal, idNuevo, props.getDocumentoWsdl());
		} catch (Exception e) {
			log.error("no se pudo agregar la nueva version al documento [" + idOriginal + "]");
			log.error("Error de Content Server", e);
			return new RespuestaXml(false, MENSAJE_ERROR_MOVER);
		}
		
		log.info("FIN Subir nueva version");		
		return new RespuestaXml(true, "OPERACION EXITOSA", idNuevo);
	}
	
	
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
	
	/*
	private Long copiarNodo(long idParent, long idDocumento, String nombreDocumento, String wsdl) throws SOAPException, IOException {

		Long idDocumentoCopiado = new Long(-1);
		log.info("Iniciando copiado documento [" + idDocumento + "] en la carpeta [" + idParent + "]");

		idDocumentoCopiado = Nodo.copiarNodo(autenticacionCS.getUserSoapHeader(props.getUsername()), idDocumento, idParent,
				nombreDocumento, wsdl);
		log.info("Documento [" + idDocumento + "] guardado en la carpeta [" + idParent + "] exitosamente");
		log.info("Id documento copiado: [" + idDocumentoCopiado + "]");

		return idDocumentoCopiado;

	}
	*/
	
	
	//Se añade la función copiarAdjunto2 en ApplicationController de la app-service-utils

	@RequestMapping(value = "/copiarAdjunto",   method = RequestMethod.GET,   produces = MediaType.APPLICATION_XML_VALUE)	
		public @ResponseBody RespuestaXml copiarAdjunto(@RequestParam(value="idParent", required=true)long idParent,
				@RequestParam(value="idDocumento", required=true)long idDocumento,
				@RequestParam(value="conexionCS", required=false) String conexionCS){
			
			Long idDocumentoCopiado = new Long(-1);
			String nombreDocumento = "";
			String nombreExtensionPDF = "";
			
			log.info("INICIO Copiar adjunto - Documento id Content: " + idDocumento );			
			
			try {
				nombreDocumento = ContenidoDocumento.obtenerNombreDocumento(autenticacionCS.getAdminSoapHeader(), idDocumento,props.getDocumentoWsdl());
				nombreExtensionPDF= StringUtils.substringBefore(nombreDocumento, ".") + ".pdf";
			} catch (IOException | SOAPException | InterruptedException e) {
				log.error("No se pudo obtener el nombre del documento [" + idDocumento + "]");
				log.error("Error de Content Server", e);			
				return new RespuestaXml(false, MENSAJE_ERROR_COPIAR);
			} 
			
			try {				
				idDocumentoCopiado = copiarNodo(idParent, idDocumento, nombreExtensionPDF, conexionCS, props.getDocumentoWsdl());
				//log.info("El documento [" + idDocumento + "] fue copiado exitosamente en la carpeta [" + idParent + "]");
			}  catch (Exception e) {
				log.error("No se pudo copiar el documento [" + idDocumento + "]");
				log.error("Error de Content Server", e);			
				return new RespuestaXml(false, MENSAJE_ERROR_COPIAR);
			} 							
			
			log.info("FIN Copiar adjunto");	
			
			return new RespuestaXml(true, "OPERACION EXITOSA", idDocumentoCopiado);
		}

	//Se añade la función copiarNodo2 en ApplicationController de la app-service-utils

		private Long copiarNodo(long idParent, long idDocumento, String nombreDocumento, String conexionCS, String wsdl) throws SOAPException, IOException {

			Long idDocumentoCopiado = new Long(-1);
			log.info("Iniciando copiado documento [" + idDocumento + "] en la carpeta [" + idParent + "]");

			//idDocumentoCopiado = nodo2.copiarNodo(autenticacionCS.getUserSoapHeader(props.getUsername()), idDocumento, idParent,
				//	nombreDocumento);
			//idDocumentoCopiado = nodo2.copiarNodo(autenticacionCS.getUserSoapHeader("desa_jrojassa"), idDocumento, idParent,
				//	nombreDocumento);
			if(org.springframework.util.StringUtils.hasText(conexionCS)) {
				log.info("Se copiará el documento [" + idDocumento + "] en la carpeta [" + idParent + "] por el usuario [" + conexionCS + "]");
				idDocumentoCopiado = nodo.copiarNodo(autenticacionCS.getUserSoapHeader(conexionCS), idDocumento, idParent,
						nombreDocumento);
			}else {
				idDocumentoCopiado = nodo.copiarNodo(autenticacionCS.getAdminSoapHeader(), idDocumento, idParent,
						nombreDocumento);
			}
			
			log.info("Documento [" + idDocumento + "] guardado en la carpeta [" + idParent + "] exitosamente");
			log.info("Id documento copiado: [" + idDocumentoCopiado + "]");

			return idDocumentoCopiado;

		}
	
	
	
	

}
