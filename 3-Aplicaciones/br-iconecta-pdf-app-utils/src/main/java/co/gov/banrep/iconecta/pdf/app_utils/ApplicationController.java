package co.gov.banrep.iconecta.pdf.app_utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.soap.SOAPException;
import javax.xml.ws.WebServiceException;

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import co.gov.banrep.iconecta.cs.autenticacion.AutenticacionCS;
import co.gov.banrep.iconecta.cs.cliente.document.Attachment;
import co.gov.banrep.iconecta.cs.documento.ContenidoDocumento;
import co.gov.banrep.iconecta.pdf.app_utils.params.Constants;
import co.gov.banrep.iconecta.pdf.app_utils.params.Mensajes;
import co.gov.banrep.iconecta.pdf.app_utils.params.ToolsProps;
import co.gov.banrep.iconecta.pdf.app_utils.params.WsdlProps;
import co.gov.banrep.iconecta.pdf.app_utils.persist.DocumentoPDF;
import co.gov.banrep.iconecta.pdf.app_utils.persist.DocumentoPDFRepository;
import co.gov.banrep.iconecta.pdf.app_utils.xml.RespuestaXml;

@Controller
public class ApplicationController {
	
	private static final String SEPARADOR_NOMBRE_DOC = "_";

	private final Logger log = LoggerFactory.getLogger(ApplicationController.class);
	
	@Autowired
	private AutenticacionCS autenticacionCS;
	
	@Autowired
	private WsdlProps wsdlsProps;
	
	@Autowired
	private ToolsProps toolsProps;
	
	@Autowired
	private DocumentoPDFRepository repository;
	
	
	String nombreCompleto = "";
	
	/**
	 * Request para realizar el proceso de unir documentos
	 * @param idWorkflow
	 * @param idsDocumentos
	 * @param idDocumentoOriginal
	 * @param conexionCS
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/unirDocumentos", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody RespuestaXml unirDocumentos(
			@RequestParam(value = "idWorkflow", required = true) Long idWorkflow,
			@RequestParam(value = "idsDocumentos", required = false) String idsDocumentos,
			@RequestParam(value = "idDocumentoOriginal", required = true) Long idDocumentoOriginal,
			@RequestParam(value = "conexionCS", required = true) String conexionCS)
			throws Exception {
			
		  	log.info("Entro al controller unirDocumentos con los siguientes parametros idWorkflow:" + idWorkflow + "  idsDocumentos= "+ idsDocumentos+ " idDocumentoOriginal: "+idDocumentoOriginal + " conexionCS: "+ conexionCS);
		   
		  	if(idsDocumentos== null) {
		  		
		  		resultJoinDocumentsSinAdjuntos(idWorkflow, idDocumentoOriginal, conexionCS);
		  		
		  	}else {
		  		
		  		resultJoinDocuments(idWorkflow, idDocumentoOriginal, idsDocumentos, conexionCS);
		  	}
		  	
		  	
			
		  	log.info("Finalizo al controller unirDocumentos con los siguientes parametros idWorkflow:" + idWorkflow + "  idsDocumentos= "+ idsDocumentos+ " idDocumentoOriginal: "+idDocumentoOriginal + " conexionCS: "+ conexionCS);
		   		
		return new RespuestaXml(true, "OK", "");
	}
	
	@RequestMapping(value = "/docsPersonalizados", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody RespuestaXml renderizarPersonalizados(
			@RequestParam(value = "idsDocsPersonalizados", required = true) String idsDocsPersonalizados,
			@RequestParam(value = "idDocumentoOriginal", required = true) Long idDocumentoOriginal,
			@RequestParam(value = "idWorkflow", required = true) Long idWorkflow,
			@RequestParam(value = "idsDocsAnexos", required = false) String idsDocsAnexos,
			@RequestParam(value = "conexionCS", required = true) String conexionCS)
	throws Exception{
		
		if(idsDocsAnexos == null) {
			idsDocsAnexos = "";
		}
		
		log.info("Entro al controller docsPersonalizados con los siguientes parametros idWorkflow:" + idWorkflow + "  idsDocsPersonalizados= "+ idsDocsPersonalizados+ " idDocumentoOriginal: "+idDocumentoOriginal + " idsDocsAnexos: "  + idsDocsAnexos + " conexionCS: "+ conexionCS);
		   
	  	resultDocPersonalizado(idWorkflow, idDocumentoOriginal, idsDocsPersonalizados, idsDocsAnexos, conexionCS);
		
	  	log.info("Finalizo al controller docsPersonalizados con los siguientes parametros idWorkflow:" + idWorkflow + "  idsDocsPersonalizados= "+ idsDocsPersonalizados+ " idDocumentoOriginal: "+idDocumentoOriginal + " conexionCS: "+ conexionCS);

		return new RespuestaXml(true, "OK", "");
	}
	
	
	
	/**
	 * Request para realizar el proceso de unir documentos
	 * @param idWorkflow
	 * @param idsDocumentos
	 * @param idDocumentoOriginal
	 * @param conexionCS
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/convertirUnirDocumentos", produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody RespuestaXml convertirUnirDocumentos(
			@RequestParam(value = "idWorkflow", required = true) Long idWorkflow,
			@RequestParam(value = "idDocumentoOriginal", required = true) Long idDocumentoOriginal,
			@RequestParam(value = "esPersonalizado", required = true) Boolean esPersonalizado,
			@RequestParam(value = "idsDocsPersonalizados", required = false) String idsDocsPersonalizados,
			@RequestParam(value = "tieneAnexos", required = true) Boolean tieneAnexos,
			@RequestParam(value = "idsDocsAnexos", required = false) String idsDocsAnexos,
			@RequestParam(value = "conexionCS", required = true) String conexionCS)
			throws Exception {

		log.info("Entro al controller convertirUnirDocumentos con los siguientes parametros: " 
				+"idWorkflow = ["+ idWorkflow + "] - " 
				+ "idDocumentoOriginal = [" + idDocumentoOriginal + "] - "
			    + "esPersonalizado= ["+ esPersonalizado+ "] - "
				+ "idsDocsPersonalizados = [" + idsDocsPersonalizados + "] - " 
			    + "idsDocsAnexos = [" + idsDocsAnexos+ "] - " 
			    + "tieneAnexos= ["+ tieneAnexos+ "] - "
				+ "conexionCS = [" + conexionCS + "] ");
		
		String raizLog = "PDF-UTILS ["+idWorkflow+"] - Controller convertirUnirDocumentos - ";
		

		//Se evalua cada entrada (Personalizado y anexos)
		if ( !esPersonalizado && !tieneAnexos ) {
			// NO ES PERSONALIZADO Y NO TIENE ANEXOS
			resultJoinDocumentsSinAdjuntos(idWorkflow, idDocumentoOriginal, conexionCS);
		} else if ( !esPersonalizado && tieneAnexos ) {
			// NO ES PERSONALIZADO Y TIENE ANEXOS
			resultJoinDocuments(idWorkflow, idDocumentoOriginal, idsDocsAnexos, conexionCS);
		} else if ( esPersonalizado &&  !tieneAnexos ) {
			// ES PERSONALIZADO Y NO TIENE ANEXOS
			resultDocPersonalizado(idWorkflow, idDocumentoOriginal, idsDocsPersonalizados, "", conexionCS);
		} else {
			// ES PERSONALIZADO Y TIENE ANEXOS
			resultDocPersonalizado(idWorkflow, idDocumentoOriginal, idsDocsPersonalizados, idsDocsAnexos, conexionCS);
		}

		log.info(raizLog +"Finalizo el controller convertirUnirDocumentos");

		return new RespuestaXml(true, "OK", "");
	}
	
	
	
	
	/**
	 * Request para consultar el proceso de unir documentos
	 * @param idWorkflow
	 * @param idDocumentoOriginal
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/consultarProceso", produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody RespuestaXml consultarProceso(
			@RequestParam(value = "idWorkflow", required = true) Long idWorkflow,
			@RequestParam(value = "idDocumentoOriginal", required = true) Long idDocumentoOriginal)	throws Exception {
		    
		    log.info("[" + idWorkflow + "] " + "ENTRO A CONSULTAR PROCESO" );
		    		    	
				DocumentoPDF doc;
				try {
					doc = repository.findByIdworkflowAndIdDocumento(idWorkflow, idDocumentoOriginal);
		
					log.info("[" + idWorkflow + "] " + " parametros consulta entidad doc.getId(): " +  doc.getId() + " doc.getIdDocumento(): " + doc.getIdDocumento() + " doc.getIdworkflow(): " + doc.getIdworkflow() );
					
					if(doc!=null) {
						
						log.info("[" + idWorkflow + "] " + " respuesta: " +  "doc.getEstado(): " + doc.getEstado()  +  " doc.getDescipcionEstado( ): " + doc.getDescripcionEstado()  + " doc.getIdDocumento(): " + doc.getIdDocumento() );
					    
					    if(doc.getEstado().equals(Constants.EXITOSO)) {
					    	
					    	return new RespuestaXml(true, doc.getDescripcionEstado(), doc.getEstado() );
					    	
					    }else {
					    	
					    	return new RespuestaXml(false, doc.getDescripcionEstado(), doc.getEstado() );
					    	
					    }
					    
						
					}
					
								
				} catch (Exception e) {
					 log.error("[" + idWorkflow + "] " + "error consultado proceso en BD", e);	
				}
				
				log.error("[" + idWorkflow + "] " + " respuesta: " +  "fail Error no se encuentra registro en BD " );
			    log.error("[" + idWorkflow + "] " + "FINALIZO CONSULTAR PROCESO" );
		   		
		return new RespuestaXml(false, "Error no se encuentra registro en BD", "Error no se encuentra registro en BD");
	}
	
	
	
	/**
	 * Metodo para monitoreo del Banco
	 * @return
	 */
	@RequestMapping(value = "/monitoreo", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
	public @ResponseBody String monitoreo() {

//		log.info("Entro al controller /monitoreo");
		
		return "<html>\r\n" + 
				"  <head>\r\n" + 
				"  <meta charset=\"UTF-8\">\r\n" + 
				"</head>\r\n" + 
				"  <body>\r\n" + 
				"    <p>\r\n" + 
				"      STATUS: OK\r\n" + 
				"    </p>\r\n" + 
				"  </body>\r\n" + 
				"</html>";
		
	}
	
	
	/**
	 * Metodo Asincrono para realizar el proceso completo de unir documentos
	 * @param idWorkflow
	 * @param idDocumentoOriginal
	 * @param idDoc
	 * @param conexionCS
	 * @return
	 */
	private Future<RespuestaXml> resultJoinDocuments(long idWorkflow, long  idDocumentoOriginal,  String idDoc, String conexionCS) {

		CompletableFuture<RespuestaXml> completableFuture = new CompletableFuture<>();
		Executors.newCachedThreadPool().submit(() -> {
				
			    try {
					DocumentoPDF doc = new DocumentoPDF(0,idWorkflow, idDocumentoOriginal, new Date(), Constants.PROGRESO, "Proceso Inicado");
					repository.save(doc);
				} catch (Exception e) {
					 log.error("[" + idWorkflow + "] " + "----- Error al guardar en BD -----", e);	
					 throw e;
				}
			
				String idsDocumentos = idDoc;
				idsDocumentos = idsDocumentos.replace("{", "");
				idsDocumentos = idsDocumentos.replace("}", "");
				
				log.info("[" + idWorkflow + "] " + "----- DOCUMENTO NO PERSONALIZADO  -  ANEXOS -----");
				log.trace("[" + idWorkflow + "] " + "ID's documentos anexos: " + idsDocumentos);
				List<String> idDocsStr = new ArrayList<String>(Arrays.asList(idsDocumentos.split(",")));
				log.info("[" + idWorkflow + "] " +"Cantidad de Documentos Anexos: " + idDocsStr.size());
				List<Long> idDoclong = new ArrayList<Long>();
				if(idDocsStr.size()>0) {
					
					for(String id : idDocsStr)
						idDoclong.add(Long.parseLong(id));
				}
				
				
				log.info("[" + idWorkflow + "] " + "----- Inicio Order adjuntos -----");	
				
				idDocsStr = orderDocumentbyDateCration(idDocsStr, idDocumentoOriginal, idWorkflow);
				
				log.info("[" + idWorkflow + "] " + "----- Fin Order adjuntos -----");
				
				log.info("[" + idWorkflow + "] " + "----- Inicio downloadFile -----");	
				boolean down = downloadFile(idDocumentoOriginal, idWorkflow, idDocumentoOriginal);
				
				for(long adjuntos: idDoclong)
					down = down && downloadFile(adjuntos, idWorkflow, idDocumentoOriginal);
				
				log.info("[" + idWorkflow + "] " + "----- Finalizo downloadFile -----");	
				
				log.info("[" + idWorkflow + "] " + "----- Inicio resultAdlib -----");	
				try {
					
					Thread.sleep(toolsProps.getAdlib().getTiempoReintento());
				
					boolean adlib = resultAdlib(idDocumentoOriginal, idWorkflow, idDocumentoOriginal);
					
					if(!adlib) {
						Thread.sleep(toolsProps.getAdlib().getTiempoReintentoDos());
						adlib = resultAdlib(idDocumentoOriginal, idWorkflow, idDocumentoOriginal);
						
					}
					
					if(!adlib) {
						log.error("[" + idWorkflow + "] " +"Archivo no encontrado " + idDocumentoOriginal);
						updateBD(idWorkflow, idDocumentoOriginal, Constants.FALLO, Mensajes.ERROR_CONVERSION_ORIGINAL + idDocumentoOriginal);
						
					}
					
					for(long adjuntos: idDoclong) {
						 int i = 1;
						adlib =  resultAdlib(adjuntos, idWorkflow, idDocumentoOriginal);
						
						 if(!adlib) {
							 
							Thread.sleep(toolsProps.getAdlib().getTiempoReintentoDos());
							adlib =  resultAdlib(adjuntos, idWorkflow, idDocumentoOriginal);
						 }
						 
						if(!adlib) {
							log.error("[" + idWorkflow + "] " +"Archivo no encontrado " + adjuntos);
							updateBD(idWorkflow, idDocumentoOriginal, Constants.FALLO, Mensajes.ERROR_CONVERSION_ANEXO + adjuntos +  " Numero " + i + " de " +  idDoclong.size());
								
						}
						
						i++;
					}
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}	
			
			log.info("[" + idWorkflow + "] " + "----- Finalizo resultAdlib -----");	
				
		    log.info("[" + idWorkflow + "] " + "----- Inicio Merge -----");	
			String DocOrignal = String.valueOf(idDocumentoOriginal)	;
			log.info("[" + idWorkflow + "] " + "----- Doc Original -----");	
			boolean merge = false;
			
			String nameLoad = getName(idDocumentoOriginal,idWorkflow );
			
			if(down)
				merge = mergePDF(DocOrignal, idDocsStr, idWorkflow, idDocumentoOriginal, nameLoad, idDocumentoOriginal );
			log.info("[" + idWorkflow + "] " + "----- Finalizo Merge -----");	
			
						
			log.info("[" + idWorkflow + "] " + "----- Inicio Nueva Version -----");
			boolean load=false;
			if(merge)
				load = LoadVersion(DocOrignal, nameLoad, idWorkflow, idDocumentoOriginal, toolsProps.getRutaMerge(), idDocumentoOriginal );
			log.info("[" + idWorkflow + "] " + "----- Finalizo Nueva Version -----");
			
			log.info("[" + idWorkflow + "] " + "----- Inicio Delete files -----"); 
			List<String> lista =  new ArrayList<String>();
			deleteFiles(DocOrignal, idDocsStr, lista);
			log.info("[" + idWorkflow + "] " + "----- Finalizo Delete files -----");
			
			log.info("[" + idWorkflow + "] " + "----- Inicio Actualziacion BD -----"); 
			if(down && merge && load) {
				
				log.info("[" + idWorkflow + "] " + "----- Inicio Actualziacion BD -----"); 
				updateBD(idWorkflow, idDocumentoOriginal, Constants.EXITOSO, Mensajes.PROCESO_EXITOSO);	
				
			}else {
				
				log.info("[" + idWorkflow + "] " + "----- Inicio Actualziacion BD -----"); 
				
				updateBD(idWorkflow, idDocumentoOriginal, Constants.FALLO, Mensajes.ERROR_UPLOAD_MERGE);
			}
			
			log.info("[" + idWorkflow + "] " + "----- Finalizo Actualziacion BD -----"); 

		});

		return completableFuture;

	}
	
	
	/**
	 * Metodo Asincrono para realizar el proceso completo de unir documentos
	 * @param idWorkflow
	 * @param idDocumentoOriginal
	 * @param idDoc
	 * @param conexionCS
	 * @return
	 */
	private Future<RespuestaXml> resultJoinDocumentsSinAdjuntos(long idWorkflow, long  idDocumentoOriginal, String conexionCS) {

		CompletableFuture<RespuestaXml> completableFuture = new CompletableFuture<>();
		Executors.newCachedThreadPool().submit(() -> {
				
			    try {
					DocumentoPDF doc = new DocumentoPDF(0,idWorkflow, idDocumentoOriginal, new Date(), Constants.PROGRESO, "Proceso Inicado");
					repository.save(doc);
				} catch (Exception e) {
					 log.error("[" + idWorkflow + "] " + "----- Error al guardar en BD -----", e);	
					 throw e;
				}
			
							
								
				log.info("[" + idWorkflow + "] " + "----- Inicio downloadFile -----");	
				boolean down = downloadFile(idDocumentoOriginal, idWorkflow, idDocumentoOriginal);
				
								
				log.info("[" + idWorkflow + "] " + "----- Inicio resultAdlib -----");	
				try {
					
					Thread.sleep(toolsProps.getAdlib().getTiempoReintento());
				
					boolean adlib = resultAdlib(idDocumentoOriginal, idWorkflow, idDocumentoOriginal);
					
					if(!adlib) {
						Thread.sleep(toolsProps.getAdlib().getTiempoReintentoDos());
						adlib = resultAdlib(idDocumentoOriginal, idWorkflow, idDocumentoOriginal);
						
					}
					
					if(!adlib) {
						log.error("[" + idWorkflow + "] " +"Archivo no encontrado " + idDocumentoOriginal);
						updateBD(idWorkflow, idDocumentoOriginal, Constants.FALLO, Mensajes.ERROR_CONVERSION_ORIGINAL + idDocumentoOriginal);
						
					}
					
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}	
			
			log.info("[" + idWorkflow + "] " + "----- Finalizo resultAdlib -----");	
				
		    log.info("[" + idWorkflow + "] " + "----- Inicio Merge -----");	
			String DocOrignal = String.valueOf(idDocumentoOriginal)	;
			log.info("[" + idWorkflow + "] " + "----- Doc Original -----");	
			
			String nameLoad = getName(idDocumentoOriginal,idWorkflow );
					
			log.info("[" + idWorkflow + "] " + "----- Inicio Nueva Version -----");
			boolean load=false;
			
			String ruta = toolsProps.getRutaPdf() + getRutaID(idDocumentoOriginal);
			if(down)
				load = LoadVersion(DocOrignal, nameLoad, idWorkflow, idDocumentoOriginal, ruta, idDocumentoOriginal );
			log.info("[" + idWorkflow + "] " + "----- Finalizo Nueva Version -----");
			
			log.info("[" + idWorkflow + "] " + "----- Inicio Delete files -----");
			
			List<String> lista =  new ArrayList<String>();
			deleteFiles(DocOrignal,  lista, lista);
			log.info("[" + idWorkflow + "] " + "----- Finalizo Delete files -----");
		
			log.info("[" + idWorkflow + "] " + "----- Inicio Actualziacion BD -----"); 
			if(down && load) {
				
				log.info("[" + idWorkflow + "] " + "----- Inicio Actualziacion BD -----"); 
				updateBD(idWorkflow, idDocumentoOriginal, Constants.EXITOSO, Mensajes.PROCESO_EXITOSO);	
				
			}else {
				
				log.info("[" + idWorkflow + "] " + "----- Inicio Actualziacion BD -----"); 
				
				updateBD(idWorkflow, idDocumentoOriginal, Constants.FALLO, Mensajes.ERROR_UPLOAD);
			}
			
			log.info("[" + idWorkflow + "] " + "----- Finalizo Actualziacion BD -----"); 

		});

		return completableFuture;

	}
	
	/**
	 * Metodo asincrono para realizar el proceso completo de personalizar documentos
	 * @param idWorkflow
	 * @param idDocumentoOriginal
	 * @param idDoc
	 * @param anexos
	 * @param conexionCS
	 * @return
	 */
	private Future<RespuestaXml> resultDocPersonalizado(long idWorkflow, long  idDocumentoOriginal,  String idDoc,  String anexos, String conexionCS) {

		CompletableFuture<RespuestaXml> completableFuture = new CompletableFuture<>();
		Executors.newCachedThreadPool().submit(() -> {
				
			    try {
					DocumentoPDF doc = new DocumentoPDF(0,idWorkflow, idDocumentoOriginal, new Date(), Constants.PROGRESO, "Proceso Inicado");
					repository.save(doc);
				} catch (Exception e) {
					e.printStackTrace();
				}
			
				String idsDocumentos = idDoc;
				idsDocumentos = idsDocumentos.replace("{", "");
				idsDocumentos = idsDocumentos.replace("}", "");
				
				String idsAnexos = anexos;
				
				if(!anexos.equals("")) {
					
					idsAnexos = idsAnexos.replace("{", "");
					idsAnexos = idsAnexos.replace("}", "");
				}
				
				
				log.info("[" + idWorkflow + "] " + "----- DOCUMENTO PERSONALIZADO  -  ANEXOS -----");
				log.trace("[" + idWorkflow + "] " + "ID's documentos anexos: " + idsDocumentos);
				
				List<String> idDocsStr = new ArrayList<String>(Arrays.asList(idsDocumentos.split(",")));
				List<String> idAnexos = new ArrayList<String>();
				List<Long> idAnexosLong = new ArrayList<Long>();
				if(!anexos.equals("")) {
					idAnexos = new ArrayList<String>(Arrays.asList(idsAnexos.split(",")));
					idAnexosLong = new ArrayList<Long>();
					log.info("[" + idWorkflow + "] " +"Cantidad de Anexos: " + idAnexos.size());
					
					
					if(idAnexos.size()>0) {
						
						for(String id : idAnexos)
							idAnexosLong.add(Long.parseLong(id));
					}

				}
				
				log.info("[" + idWorkflow + "] " +"Cantidad de copias: " + idDocsStr.size());
				
				
				List<Long> idDoclong = new ArrayList<Long>();
				if(idDocsStr.size()>0) {
					
					for(String id : idDocsStr)
					 idDoclong.add(Long.parseLong(id));
				}
				
				
				if (!anexos.equals("")) {
					log.info("[" + idWorkflow + "] " + "----- Inicio Order adjuntos -----");
	
					idAnexos = orderDocumentbyDateCration(idAnexos, idDocumentoOriginal, idWorkflow);
	
					log.info("[" + idWorkflow + "] " + "----- Fin Order adjuntos -----");
				}
				
				log.info("[" + idWorkflow + "] " + "----- Inicio downloadFile -----");					
				boolean down = downloadFile(idDocumentoOriginal, idWorkflow, idDocumentoOriginal);
				
				for(long adjuntos: idDoclong)
					down = down &&	downloadFile(adjuntos, idWorkflow, idDocumentoOriginal);
				
				if(!anexos.equals("")) {
					for(long adjuntos: idAnexosLong)
						down = down && downloadFile(adjuntos, idWorkflow, idDocumentoOriginal);
				
				}
				
				log.info("[" + idWorkflow + "] " + "----- Finalizo downloadFile -----");
				
				log.info("[" + idWorkflow + "] " + "----- Inicio resultAdlib -----");

				try {
				
					Thread.sleep(toolsProps.getAdlib().getTiempoReintento());
				
					boolean adlib = resultAdlib(idDocumentoOriginal, idWorkflow, idDocumentoOriginal);
					
					if(!adlib) {
						
						Thread.sleep(toolsProps.getAdlib().getTiempoReintentoDos());
						adlib =  resultAdlib(idDocumentoOriginal, idWorkflow, idDocumentoOriginal);
						
					}
					
					if(!adlib) {
						log.error("[" + idWorkflow + "] " +"Archivo no encontrado " + idDocumentoOriginal);
						updateBD(idWorkflow, idDocumentoOriginal, Constants.FALLO, Mensajes.ERROR_CONVERSION_ORIGINAL + idDocumentoOriginal);
						
					}
					
					for(long adjuntos: idDoclong) {
						 int i = 1;
						 adlib = resultAdlib(adjuntos, idWorkflow, idDocumentoOriginal);
						
						 if(!adlib) {
							Thread.sleep(toolsProps.getAdlib().getTiempoReintentoDos());
							adlib = resultAdlib(adjuntos, idWorkflow, idDocumentoOriginal);
						 }
						 
						 
						if(!adlib) {
							log.error("[" + idWorkflow + "] " +"Archivo no encontrado " + adjuntos);
							updateBD(idWorkflow, idDocumentoOriginal, Constants.FALLO, Mensajes.ERROR_CONVERSION_ANEXO + adjuntos +  " Numero " + i + " de " +  idDoclong.size());
								
						}
						i++;
					}
					
					
					if(!anexos.equals("")) {
						
						for(long adjuntos: idAnexosLong) {
							int i=1;
							adlib =  resultAdlib(adjuntos, idWorkflow, idDocumentoOriginal);
						
								 
							if(!adlib) {
								Thread.sleep(toolsProps.getAdlib().getTiempoReintentoDos());
								adlib = resultAdlib(adjuntos, idWorkflow, idDocumentoOriginal);
							 }
							 					 
							if(!adlib) {
								log.error("[" + idWorkflow + "] " +"Archivo no encontrado " + adjuntos);
								updateBD(idWorkflow, idDocumentoOriginal, Constants.FALLO,  Mensajes.ERROR_CONVERSION_ANEXO + adjuntos +  " Numero " + i + " de " +  idDoclong.size());
									
							}
							
							i++;
						}
					
					}
				
				} catch (InterruptedException e) {
					e.printStackTrace();
				}	
				
				log.info("[" + idWorkflow + "] " + "----- Finalizo resultAdlib -----");
			
				log.info("[" + idWorkflow + "] " + "----- Inicio Merge -----");	
				String DocOrignal = String.valueOf(idDocumentoOriginal)	;
				log.info("[" + idWorkflow + "] " + "----- Doc Original -----");	
				boolean merge = true;
				String nameLoad = getName(idDocumentoOriginal,idWorkflow );
				if(!anexos.equals("")) {
					if(down)
						merge = mergePDF(DocOrignal, idAnexos, idWorkflow, idDocumentoOriginal, nameLoad, idDocumentoOriginal );
					
					
					for(long adjuntos: idDoclong) {
						String DocOrignalopt = String.valueOf(adjuntos)	;
						merge = mergePDF(DocOrignalopt, idAnexos, idWorkflow, adjuntos, nameLoad, idDocumentoOriginal );
					}
				}
				
				
				log.info("[" + idWorkflow + "] " + "----- Finalizo Merge -----");		
				
				log.info("[" + idWorkflow + "] " + "----- Inicio Nueva Version -----");
				
				boolean load=false;
				
					if(merge || anexos.equals("") ) {
				
						String ruta = "";
						
					if(!anexos.equals("")) {
						
						ruta = toolsProps.getRutaMerge();
					}else {
						ruta = toolsProps.getRutaPdf() + getRutaID(idDocumentoOriginal);
					}
					
						
					load = LoadVersion(DocOrignal, nameLoad, idWorkflow, idDocumentoOriginal, ruta, idDocumentoOriginal);
					
					for(long adjuntos: idDoclong) {
						
						if(!anexos.equals("")) {
							
							ruta = toolsProps.getRutaMerge();
						}else {
							ruta = toolsProps.getRutaPdf() + getRutaID(adjuntos);
						}
						
						 String DocOrignalopt = String.valueOf(adjuntos)	;
					     nameLoad = getName(adjuntos,idWorkflow );
						 load = LoadVersion(DocOrignalopt, nameLoad, idWorkflow, idDocumentoOriginal, ruta, idDocumentoOriginal );
						 
					}
					
				}
			
			log.info("[" + idWorkflow + "] " + "----- Finalizo Nueva Version -----");
			
			log.info("[" + idWorkflow + "] " + "----- Inicio Delete files -----"); 
			deleteFiles(DocOrignal, idDocsStr, idAnexos);
			if(!anexos.equals("")) {
				deleteFiles(DocOrignal, idAnexos, idAnexos);
			}
			log.info("[" + idWorkflow + "] " + "----- Finalizo Delete files -----");
			log.info("[" + idWorkflow + "] " + "----- Inicio Actualziacion BD -----"); 
			if(down && load && merge ) {
				
				log.info("[" + idWorkflow + "] " + "----- Inicio Actualziacion BD -----"); 
				updateBD(idWorkflow, idDocumentoOriginal, Constants.EXITOSO, Mensajes.PROCESO_EXITOSO);	
				
			}else {
				
				log.info("[" + idWorkflow + "] " + "----- Inicio Actualziacion BD -----"); 
				
				updateBD(idWorkflow, idDocumentoOriginal, Constants.FALLO, Mensajes.ERROR_UPLOAD_MERGE);
			}
			
			log.info("[" + idWorkflow + "] " + "----- Finalizo Actualziacion BD -----"); 
		});

		return completableFuture;

	}
	
	
	/**
	 * Metodo que actualiza registros en la Base de datos
	 * @param idWorkflow
	 * @param idDocumentoOriginal
	 * @param estado
	 */
	private void updateBD(long idWorkflow, long idDocumentoOriginal, String estado, String descripcionEstado) {
		
		log.info("[" + idWorkflow + "] " + "----- Inicio Actualziacion BD -----"); 
		DocumentoPDF doc;
		try {
			doc = repository.findByIdworkflowAndIdDocumento(idWorkflow, idDocumentoOriginal);

			log.info("[" + idWorkflow + "] " + " parametros consulta entidad doc.getId(): " +  doc.getId() + " doc.getIdDocumento(): " + doc.getIdDocumento() + " doc.getIdworkflow(): " + doc.getIdworkflow() +  " doc.getDescipcionEstado(): " + doc.getDescripcionEstado() );
			
			if(doc!=null) {
				doc.setEstado(estado);
				
				if(doc.getDescripcionEstado().equals("Proceso Inicado")) {
						doc.setDescripcionEstado(descripcionEstado);
				}
				repository.save(doc);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Metodo que obtiene el nombre de un documento del contenet service
	 * @param idDocumento
	 * @param idWorkflow
	 * @return
	 */
	private String getName(long idDocumento, long idWorkflow) {
		
		String result = "";
		
		try {
			result = ContenidoDocumento.obtenerNombreDocumento(autenticacionCS.getAdminSoapHeader(), idDocumento, wsdlsProps.getDocumento());
		} catch (SOAPException e) {
			e.printStackTrace();
			updateBD(idWorkflow, idDocumento, Constants.FALLO, Mensajes.ERROR_OBTENER_NOMBRE + idDocumento + Mensajes.CONTENT_SERVER);
		} catch (IOException e) {
			e.printStackTrace();
			updateBD(idWorkflow, idDocumento, Constants.FALLO, Mensajes.ERROR_OBTENER_NOMBRE+ idDocumento + Mensajes.CONTENT_SERVER);
		} catch (InterruptedException e) {
			e.printStackTrace();
			updateBD(idWorkflow, idDocumento, Constants.FALLO, Mensajes.ERROR_OBTENER_NOMBRE+ idDocumento + Mensajes.CONTENT_SERVER);
		}
		
		return result;
	}
	
	/**
	 * Metodo que descarga un documento del content service
	 * @param idDocumento
	 * @param idWorkflow
	 * @return
	 */
	private boolean downloadFile(long idDocumento, long idWorkflow, long idDocumentoOriginal) {
		
		log.info("Entro al downloadFile  con los siguientes parametros idDocumento:" + idDocumento);
		boolean result = false;
		
		try {
			
		String ruta = toolsProps.getRutaPdf() + getRutaID(idDocumento);
			
		log.info("Entro al obtenerContenidoDocumentoDTHLR  con los siguientes parametros idDocumento:" + idDocumento);
		
		 ContenidoDocumento.obtenerContenidoDocumentoDTHLR(autenticacionCS.getAdminSoapHeader(), idDocumento, ruta , String.valueOf(idWorkflow), wsdlsProps.getDocumento());

		log.info("Finalizo al obtenerContenidoDocumentoDTHLR  con los siguientes parametros idDocumento:" + idDocumento);
		
		log.info("Entro al obtenerNombreDocumento  con los siguientes parametros idDocumento:" + idDocumento);

		nombreCompleto = ContenidoDocumento.obtenerNombreDocumento(autenticacionCS.getAdminSoapHeader(), idDocumento, wsdlsProps.getDocumento());
		
		log.info("Finalizo al obtenerNombreDocumento  con los siguientes parametros nombreCompleto:" + nombreCompleto);

		log.info("Entro al mover  con los siguientes parametros idDocumento:" + idDocumento);
		String nombreEx = "";
		String extension = "";
		
		Files.walk(Paths.get(ruta)).forEach(r-> System.out.println( "ruta: "+ r.getFileName()));
		
		Path file = null;
		List<Path> fileOpt = Files.walk(Paths.get(ruta)).collect(Collectors.toList());
		
		if(!fileOpt.isEmpty() && fileOpt.size()> 1) {
			file = fileOpt.get(1);
			
				nombreCompleto = file.getFileName().toString();
				extension = FilenameUtils.getExtension(nombreCompleto);
				nombreEx = getName(nombreCompleto);
				
				log.info("extension: " + extension + " nombreCompleto: " + nombreCompleto + " nombreEx: " + nombreEx);
								
				
				if (!extension.equals("pdf")) {

						String nombreAlib = ruta + nombreCompleto;
						File f = new File(nombreAlib);
						f.setReadable(true);
						f.setReadable(true, false);
						// f.setWritable(true, false);
						// f.setExecutable(true, false);

						Path path = Paths.get(f.getAbsolutePath());
						Set<PosixFilePermission> perms = Files.readAttributes(path, PosixFileAttributes.class)
								.permissions();
						perms.add(PosixFilePermission.OWNER_WRITE);
						perms.add(PosixFilePermission.OWNER_READ);
						perms.add(PosixFilePermission.OWNER_EXECUTE);
						perms.add(PosixFilePermission.GROUP_WRITE);
						perms.add(PosixFilePermission.GROUP_READ);
						perms.add(PosixFilePermission.GROUP_EXECUTE);
						perms.add(PosixFilePermission.OTHERS_WRITE);
						perms.add(PosixFilePermission.OTHERS_READ);
						perms.add(PosixFilePermission.OTHERS_EXECUTE);
						Files.setPosixFilePermissions(path, perms);

						Path source = Paths.get(nombreAlib);
						String newName = String.valueOf(idWorkflow) + SEPARADOR_NOMBRE_DOC + String.valueOf(idDocumento) + "."
								+ extension;
						Path target = Paths.get(toolsProps.getAdlib().getRutaEntrada() + newName);
												
						Files.move(source, target);
						
						Files.setPosixFilePermissions(target, perms);

					} else {

						log.info("Finalizo al renderizar  con los siguientes parametros idDocumento: " + idDocumento);

						log.info(
								"Entro al renombrarArchivo  con los siguientes parametros idDocumento: " + idDocumento);
						String nombreCompletoload = ruta + nombreEx + toolsProps.getExtension();
						Path source = Paths.get(nombreCompletoload);
						String newName = String.valueOf(idDocumento) + toolsProps.getExtension();
						Files.move(source, source.resolveSibling(newName));

					}

				
				log.info("Finalizo al renombrarArchivo  con los siguientes parametros idDocumento: " + idDocumento);
		
				result = true;

				log.info("Finalizo al downloadFile  con los siguientes parametros idDocumento: " + idDocumento);

			
		}
		
		
		} catch (IOException | SOAPException | WebServiceException e) {
			e.printStackTrace();
			updateBD(idWorkflow, idDocumentoOriginal, Constants.FALLO, Mensajes.ERROR_DESCARGANDO_DOCUMENTO + idDocumento  + Mensajes.CONTENT_SERVER);

		} catch (Exception e) {
			e.printStackTrace();
			updateBD(idWorkflow, idDocumentoOriginal, Constants.FALLO, Mensajes.ERROR_DESCARGANDO_DOCUMENTO + idDocumento  + Mensajes.CONTENT_SERVER);

		}
		
		

		return result;
		
	}
	
	/**
	 * Metodo que envia documentos Adlib para renderizar
	 * @param idDocumento
	 * @param idWorkflow
	 * @return
	 */
	private boolean resultAdlib (long idDocumento, long idWorkflow, long idDocumentoOriginal) {
				
		log.info("Entro al resultAdlib  con los siguientes parametros idDocumento:" + idDocumento);
		boolean result = false;
		
		try {
			
			String ruta = toolsProps.getRutaPdf() + getRutaID(idDocumento);	
			String nombreCompletoload = ruta + String.valueOf(idDocumento) + toolsProps.getExtension();
			File archive = new File(nombreCompletoload);
				
			if(!archive.exists()) {
						
					String filerender = toolsProps.getAdlib().getRutaSalida() + String.valueOf(idWorkflow) + SEPARADOR_NOMBRE_DOC +  String.valueOf(idDocumento) + toolsProps.getExtension(); 
					
					log.info("file renderizado: " + filerender);
					
					String filedestino = ruta +  String.valueOf(idDocumento) + toolsProps.getExtension();
					
					log.info("file ruta movido: " + filedestino);
					
					File render = new File(filerender);
					
					if(!render.exists()) {
						return  false;
					}
					
					log.info("Archivo Encontrado");
					result = true;
					Path source = Paths.get(filerender);
					Path target = Paths.get(filedestino);
					
						Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
						
					System.out.println("Termino de mover");
					
			}else {
					
				return true;
			}
				

		} catch (Exception e) {
			updateBD(idWorkflow, idDocumentoOriginal, Constants.FALLO, Mensajes.ERROR_RENDERIZACION + idDocumento );
			e.printStackTrace();
		}
		
		return result;
		
	}
	
	/**
	 * Metodo que relaiza la union de los documentos
	 * @param pdfPrincipal
	 * @param listaPdfUnir
	 * @param idWorkflow
	 * @param idDocumento
	 * @return
	 */
	private  boolean  mergePDF(String pdfPrincipal, List<String> listaPdfUnir, long idWorkflow, long idDocumento, String nombre, long idDocumentoOriginal ) {
		
		log.info("Entro al mergePDF  con los siguientes parametros pdfPrincipal:" + pdfPrincipal + " listaPdfUnir " + listaPdfUnir.size());
		
		nombre = getName(nombre);
		boolean respuesta = false; 
		
		    PdfReader cover;
		    FileOutputStream file = null; 
		    
				try {
						String ruta = toolsProps.getRutaPdf() + getRutaID(idDocumento);
						String nombrePrincipal = ruta  + pdfPrincipal + toolsProps.getExtension();
						log.info("Entro al mergePDF  con los siguientes parametros nombreprincipal:" + nombrePrincipal);
						cover = new PdfReader(nombrePrincipal);
				        Document document = new Document();
				        
				        String NombreMerge = toolsProps.getRutaMerge() + pdfPrincipal + toolsProps.getExtension();
						log.info("Entro al mergePDF  con los siguientes parametros NombreMerge:" + NombreMerge);
						file = new FileOutputStream(NombreMerge);
				        PdfCopy copy = new PdfCopy(document, file);
				        document.open();
				        copy.addDocument(cover);
				        cover.close();
				        
				        for(String doc: listaPdfUnir) {
				        	
				        	 ruta = toolsProps.getRutaPdf() + doc + "/";	
				        	 String nombreAdjunto = ruta + doc + toolsProps.getExtension();
				        	 String nombreAdjuntoWater = ruta + doc + "w" + toolsProps.getExtension();
							 log.info("Entro al mergePDF  con los siguientes parametros nombreAdjunto:" + nombreAdjunto);
							 
							 if(verifyWrite(nombreAdjunto)) {
								 log.error("Fallo Merge documento anexo " + nombreAdjunto + " archivo se encuentra protegido ");	
								 updateBD(idWorkflow, idDocumentoOriginal, Constants.FALLO, Mensajes.ERROR_UNION);
								 return false;
							 }
							 
							 watermark(nombreAdjunto, nombreAdjuntoWater, nombre);
							 
				        	 PdfReader reader = new PdfReader(nombreAdjuntoWater);
				        	 copy.addDocument(reader);
				        	 reader.close();
				        }
				           
				        document.close();
				        respuesta = true;
				        
				} catch ( Exception e) {
					log.error("Fallo Merge Documentos");	
					e.printStackTrace();
					updateBD(idWorkflow, idDocumentoOriginal, Constants.FALLO, Mensajes.ERROR_UNION_DOCUMENTO  + pdfPrincipal + Mensajes.CONTENT_SERVER);
				} finally {
					safeClose(file);
				}
				
		log.info("Entro al mergePDF  respuesta" + respuesta);			
		return respuesta;
	}

   /**
    * Metodo que carga una nueva version del documento al content service
    * @param idOriginal
    * @param name
    * @param idWorkflow
    * @param idDocumento
    * @return
    */
   private boolean LoadVersion(String idOriginal, String name, long idWorkflow, long idDocumento, String ruta, long idDocumentoOrigi ) {
	   
	   boolean result = false;
	   log.info("Entro al LoadVersion  con los siguientes parametros idOriginal:" + idOriginal + " name " + name);
	   
	   try {
		   
		   long idDocumentoOriginal = Long.parseLong(idOriginal);
		   
		   String nameExt = getName(name) + toolsProps.getExtension();
		   
		   GregorianCalendar calendar = new GregorianCalendar();
		   calendar.setTime(new Date());
		   XMLGregorianCalendar XMLCalendar;
		   XMLCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
	       String NombreMerge = ruta + idOriginal + toolsProps.getExtension();

		   byte[] array = Files.readAllBytes(new File(NombreMerge).toPath());
		   
		   Attachment att = new Attachment();
		   att.setFileName(nameExt);
		   att.setModifiedDate(XMLCalendar);
		   att.setContents(array);
		   att.setFileSize(array.length);
		   att.setCreatedDate(XMLCalendar);
		   
		   ContenidoDocumento.cargarNuevaVersion(autenticacionCS.getAdminSoapHeader(), idDocumentoOriginal, att, wsdlsProps.getDocumento());
		   
		   log.info("Entro al LoadVersion  con los siguientes parametros idOriginal:" + idOriginal);
		   
		   return true;

		
		} catch (Exception  e) {
			log.error("ERROR : Falló LoadVersion");
			e.printStackTrace();
			updateBD(idWorkflow, idDocumentoOrigi, Constants.FALLO, Mensajes.ERROR_LOAD + idOriginal + Mensajes.CONTENT_SERVER);
		} 
	   
	   return result;
	   
   }
   
   /**
    * Metodo que elimina los archivos creados
    * @param pdfPrincipal
    * @param listaPdfUnir
    * @return
    */
   private boolean deleteFiles(String pdfPrincipal, List<String> listaPdfUnir, List<String> anexos ) {
	   
	   boolean result = false;
	   File file = null;
	   String ruta = toolsProps.getRutaPdf() + pdfPrincipal;
	   System.out.println(ruta);
	   try {
		deleteFile(ruta);
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	}
	   
		   
	   file = new File (toolsProps.getRutaMerge() + pdfPrincipal + toolsProps.getExtension());
	   file.delete();
	   
	   for(String name: anexos) {
		   file = new File (toolsProps.getRutaMerge() + name + toolsProps.getExtension());
		   file.delete();
	   }
	   
	   for(String doc : listaPdfUnir ) {
		   ruta = toolsProps.getRutaPdf() + doc;
		   try {
			deleteFile(ruta);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		   
	   }
	   
	   result = true;
	   return result;
   }
   
   
      
   /**
    * Metodo que retorna el nombre del documento sin extension
    * @param nombre
    * @return
    */
   private String getName(String nombre) {
	   
	   String result = "";
	   
	   result  = FilenameUtils.removeExtension(nombre);
	   
	   return result;
	   
   }
   
   /**
    * Metodo que permite ordenar los documentos anexos
    * @param adjuntos
    * @param idDocumentoOrigi
    * @param idWorkflow
    * @return
    */
	private List<String> orderDocumentbyDateCration(List<String> adjuntos, long idDocumentoOrigi, long idWorkflow) {

		List<String> listaFinal = new ArrayList<>();
		TreeMap<LocalDateTime, String> map = new TreeMap<>();

		for (String doc : adjuntos) {

			Long idDocumento = Long.parseLong(doc);

			try {
								
				LocalDateTime fecha = ContenidoDocumento.obtenerFechaCreacionDocumento(
						autenticacionCS.getAdminSoapHeader(), idDocumento, wsdlsProps.getDocumento());
								
				map.put(fecha, doc);

			} catch (Exception e) {
				updateBD(idWorkflow, idDocumentoOrigi, Constants.FALLO, Mensajes.ERROR_FECHA_CREACION + doc  + Mensajes.CONTENT_SERVER);
				e.printStackTrace();

			}

		}

		map.forEach((k, v) -> System.out.println("key: " + k + " value:" + v));
		
		listaFinal =  (ArrayList<String>) map.values().stream().collect(Collectors.toList());
		
		if(listaFinal.size() != adjuntos.size()) {
				return adjuntos;
		}
		
		return listaFinal;

	}
   
   /**
    * Metodo que permite crear un nuevo documento con Marca de agua
    * @param src
    * @param dest
    * @param texto
    * @throws IOException
    * @throws DocumentException
    */
   public void watermark(String src, String dest, String texto) throws IOException, DocumentException {
		PdfReader reader = new PdfReader(src);
		int n = reader.getNumberOfPages();
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
		stamper.setRotateContents(false);
		// text watermark
		Font f = new Font(FontFamily.HELVETICA, 12);
		Phrase p = new Phrase("Anexos de la comunicación: " + texto, f);
		// transparency
		PdfGState gs1 = new PdfGState();
		gs1.setFillOpacity(0.9f);
		// properties
		PdfContentByte over;
		Rectangle pagesize;
		float x, y;
		float percentage = 0.9f;
		// loop over every page
		for (int i = 1; i <= n; i++) {

			float offsetX = (reader.getPageSize(i).getWidth() * (1 - percentage)) / 2;
			float offsetY = (reader.getPageSize(i).getHeight() * (1 - percentage)) / 2;
			stamper.getUnderContent(i)
					.setLiteral(String.format("\nq %s 0 0 %s %s %s cm\nq\n", percentage, percentage, offsetX, offsetY));
			stamper.getOverContent(i).setLiteral("\nQ\nQ\n");

			pagesize = reader.getPageSize(i);
			x = (pagesize.getLeft() + pagesize.getRight() - 10);
			y = (pagesize.getBottom() + pagesize.getTop() * 0.965f);
			over = stamper.getOverContent(i);
			over.saveState();
			over.setGState(gs1);
			ColumnText.showTextAligned(over, Element.ALIGN_RIGHT, p, x, y, 0);
		}
		stamper.close();
		reader.close();
	}
   
   /**
    * Metodo que permite verificar si el documento se encuentra protegido
    * @param file
    * @return
    */
   public static boolean verifyWrite(String file) {
	
		boolean result = false;
		PDDocument doc;
		try {
			doc = PDDocument.load(new File(file));
			if (doc.isEncrypted()) {
				result = true;
			}
		} catch (InvalidPasswordException | NoClassDefFoundError e) {
			result=true;
			e.printStackTrace();
		} catch (IOException e) {
			result = true;
			e.printStackTrace();
		}
		System.out.println("Verificacion proteccion Documento: " + file + " - resultado: " + result);
		return result;
		
	}
   
   /**
    * Metodo que retorna la ruta a partir del ID
    * @param id
    * @return
    */
   private String getRutaID(long id) {
	   String result = "";
	   
	   result = String.valueOf(id) + "/";
	   
	   return result;
	   
   }
   
   /**
	 * Eliminar todos los archivos y carpetas de una carpeta
	 * @param delpath
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static boolean deleteFile(String delpath) throws FileNotFoundException
		{
		File file = new File(delpath);
		if (!file.isDirectory()) {
			file.delete();
		} else if (file.isDirectory()) {
			File[] fileList = file.listFiles();
			for (int i = 0; i <fileList.length; i++) {
				File delfile = fileList[i];
				if (!delfile.isDirectory()) {
					System.out.println("La ruta relativa=" + delfile.getPath());
					System.out.println("Ruta absoluta=" + delfile.getAbsolutePath());
					System.out.println("Nombre de archivo=" + delfile.getName());
					delfile.delete();
					System.out.println("Borrar el archivo de éxito");
				} else if (delfile.isDirectory()) {
					deleteFile(fileList[i].getPath());
				}
			}
			file.delete();
		}
		return true;
	}

	/**
	 * Metodo para cerrar correctamante un FileOutputStream
	 * @param fis
	 */
	private void safeClose(FileOutputStream fis) {
		 if (fis != null) {
			 try {
				 fis.close();
			 } catch (IOException e) {
				 e.printStackTrace();
			 }
			 
		 }
	}
	

	
}
