package co.gov.banrep.iconecta.ssm.firma;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import co.gov.banrep.iconecta.cs.autenticacion.AutenticacionCS;
import co.gov.banrep.iconecta.ssm.firma.entity.RespuestaXml;
import co.gov.banrep.iconecta.ssm.firma.enums.Events;
import co.gov.banrep.iconecta.ssm.firma.enums.States;
import co.gov.banrep.iconecta.ssm.firma.params.GlobalConsts;
import co.gov.banrep.iconecta.ssm.firma.params.WsdlProps;
import co.gov.banrep.iconecta.ssm.firma.persist.ContextoPersist;
import co.gov.banrep.iconecta.ssm.firma.persist.MachineRepository;
import co.gov.banrep.iconecta.ssm.firma.persist.Maquina;

/**
 * Controlador de la aplicacion que maneja las peticiones web hacia la Maquina
 * de Estados
 * 
 *  @author <a href="mailto:jjrojassa@banrep.gov.co">John Jairo Rojas S.</a>
 *
 */
@Controller
public class StateMachineController {

	private static final String ESTADO_PF_FINALIZADO = "FINALIZAR";
	private static final String ESTADO_PF_CANCELADO = "CANCELAR";
	private final static String[] EVENTOS_CONSOLA = new String[] { Events.ERROR_OPERACION.toString(),
			Events.ERROR_OPERACION_FINAL.toString(), Events.ERROR_OPERACION_FINAL_SIN_REINTENTO.toString(),
			Events.RESPUESTA_PF_EXITOSA.toString(), Events.RESPUESTA_PF_CANCELADO.toString() };

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private StateMachinePersister<States, Events, String> stateMachinePersister;

	@Autowired
	private ContextoPersist contextoPersist;

	@Autowired
	private MachineRepository repository;

	@Autowired
	private StateMachineFactory<States, Events> stateMachineFactory;
	
	@Autowired
	private ContextRefreshedListener firmantesCompleto;
	
	@Autowired
	private AutenticacionCS autenticacionCS;
	
	@Autowired
	private WsdlProps wsdlProps;
	
	private final Map<String, StateMachine<States, Events>> machines = new HashMap<>();

	@RequestMapping("/")
	public String home() {
		return "redirect:/firma";
	}
	
	
	/**
	 * 
	 * @param idFirmante
	 * @return
	 */
		@RequestMapping(value = "/evaluarFirmante", method = RequestMethod.POST)
		public @ResponseBody Boolean evaluarFirmante(
				@RequestParam(value = "idFirmante", required = true) Long idFirmante
				) {
			return firmantesCompleto.getFirmantes().stream().anyMatch(firmante -> firmante.getCedula().equals(idFirmante));
		}
		
		

	// Este metodo responde un XML por lo tanto no tiene una plantilla Thymeleaf
	// asociada
	@RequestMapping(value = "/firma", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody RespuestaXml iniciarFirma(
			@RequestParam(value = GlobalConsts.ID_WORKFLOW, required = true) String idWorkflow,
			@RequestParam(value = GlobalConsts.ID_DOC, required = false) String idDoc,
			@RequestParam(value = GlobalConsts.ID_DOCS, required = false) String idDocs,
			@RequestParam(value = GlobalConsts.ASUNTO, required = true) String asunto,
			@RequestParam(value = GlobalConsts.COPIA_COMPULSADA, required = true) boolean copiaCompulsada,
			@RequestParam(value = GlobalConsts.CONEXION_USUARIO_CS, required = true) String conexionUsuario,
			@RequestParam(value = GlobalConsts.NOMBRE_USUARIO_CS, required = true) String nombreUsuario,
			@RequestParam(value = GlobalConsts.ID_USUARIO_CS, required = true) String idUsuario,
			@RequestParam(value = GlobalConsts.IDS_FIRMANTES, required = false) String listaFirmantes,
			@RequestParam(value = GlobalConsts.ID_FIRMANTE, required = false) String idFirmante,
			@RequestParam(value = GlobalConsts.ACCION_EXTERNA, required = false) String accionExterna,
			@RequestParam(value = GlobalConsts.URL_EXTERNA, required = false) String urlExterna,
			@RequestParam(value = GlobalConsts.ACTUALIZAR_CAT_DOCUMENTO, required = false) Boolean actualizarCatDocumento,
			@RequestParam(value = GlobalConsts.COMENTARIOS, required = false) String comentarios,
			@RequestParam(value = GlobalConsts.NOTIFICAR_SOLICITANTE, required = false) Boolean notificarSolicitante) {

		log.info("INICIANDO MAQUINA [" + idWorkflow + "]");

		StateMachine<States, Events> machine = null;
		if (StringUtils.hasText(idWorkflow)) {
			machine = getMachine(idWorkflow);
		}

		Map<String, Object> headers = new HashMap<>();
		headers.put(GlobalConsts.ID_WORKFLOW, idWorkflow);
		headers.put(GlobalConsts.CONEXION_USUARIO_CS, conexionUsuario);
		headers.put(GlobalConsts.NOMBRE_USUARIO_CS, nombreUsuario);
		headers.put(GlobalConsts.ASUNTO, asunto);
		headers.put(GlobalConsts.COPIA_COMPULSADA, copiaCompulsada);
		headers.put(GlobalConsts.ID_USUARIO_CS, idUsuario);
		headers.put(GlobalConsts.ID_DOC, idDoc);
		log.info("CONTROLLER - IdDocs: " + idDocs);
		if (StringUtils.hasText(idDocs)) {
			headers.put(GlobalConsts.ID_DOCS, idDocs);
		}

		if (StringUtils.hasText(idFirmante)) {
			headers.put(GlobalConsts.ID_FIRMANTE, idFirmante);
		} else if (StringUtils.hasText(listaFirmantes)) {
			headers.put(GlobalConsts.IDS_FIRMANTES, listaFirmantes);
		} else {
			throw new RuntimeException("Los parametro de fimantes son nulos");
		}

		if (StringUtils.hasText(accionExterna)) {
			headers.put(GlobalConsts.ACCION_EXTERNA, accionExterna);
		}

		if (StringUtils.hasText(urlExterna)) {
			headers.put(GlobalConsts.URL_EXTERNA, urlExterna);
		}

		if (actualizarCatDocumento != null) {
			headers.put(GlobalConsts.ACTUALIZAR_CAT_DOCUMENTO, actualizarCatDocumento);
		} else {
			actualizarCatDocumento = true;
			headers.put(GlobalConsts.ACTUALIZAR_CAT_DOCUMENTO, actualizarCatDocumento);
		}
		
		if (notificarSolicitante != null) {
			headers.put(GlobalConsts.NOTIFICAR_SOLICITANTE, notificarSolicitante);
		} else {
			notificarSolicitante = true;
			headers.put(GlobalConsts.NOTIFICAR_SOLICITANTE, notificarSolicitante);
		}
		
		if(StringUtils.hasText(comentarios)){
			headers.put(GlobalConsts.COMENTARIOS, comentarios);
		}
			

		if (log.isTraceEnabled()) {
			log.trace("Parametros recibidos por el servlet");
			log.trace(GlobalConsts.ID_WORKFLOW + ": " + idWorkflow);
			log.trace(GlobalConsts.ID_DOC + ": " + idDoc);
			log.trace(GlobalConsts.ID_DOCS + ": " + idDocs);
			log.trace(GlobalConsts.ASUNTO + ": " + asunto);
			log.trace(GlobalConsts.COPIA_COMPULSADA + ": " + copiaCompulsada);
			log.trace(GlobalConsts.CONEXION_USUARIO_CS + ": " + conexionUsuario);
			log.trace(GlobalConsts.NOMBRE_USUARIO_CS + ": " + nombreUsuario);
			log.trace(GlobalConsts.ID_USUARIO_CS + ": " + idUsuario);
			log.trace(GlobalConsts.IDS_FIRMANTES + ": " + listaFirmantes);
			log.trace(GlobalConsts.ID_FIRMANTE + ": " + idFirmante);
			log.trace(GlobalConsts.ACCION_EXTERNA + ": " + accionExterna);
			log.trace(GlobalConsts.URL_EXTERNA + ": " + urlExterna);
			log.trace(GlobalConsts.ACTUALIZAR_CAT_DOCUMENTO + ": " + actualizarCatDocumento);
			log.trace(GlobalConsts.COMENTARIOS + ": " + comentarios);
			log.trace(GlobalConsts.NOTIFICAR_SOLICITANTE + ": " + notificarSolicitante);
			log.trace("FIN Parametros recibidos por el servlet");
		}

		String firmante1Nombre = "";
		String firmante1Correo = "";

		try {
			machine.sendEvent(MessageBuilder.createMessage(Events.INICIAR, new MessageHeaders(headers)));

			firmante1Nombre = machine.getExtendedState().get(GlobalConsts.FIRMANTE1_NOMBRE, String.class);
			firmante1Correo = machine.getExtendedState().get(GlobalConsts.FIRMANTE1_CORREO, String.class);

			boolean error = false;
			try {
				error = machine.getExtendedState().get(GlobalConsts.ERROR_MAQUINA, Boolean.class);
			} catch (NullPointerException e) {
				error = false;
			}

			if (error) {
				throw new RuntimeException("Error en la operacion");
			}

		} catch (RuntimeException e) {
			log.error("ERROR OPERACION INICIAL", e);
			return new RespuestaXml(false, "ERROR OPERACION INICIAL");
		}

		if (!StringUtils.hasText(firmante1Nombre)) {
			firmante1Nombre = "NO ESPECIFICADO";
		}

		if (!StringUtils.hasText(firmante1Correo)) {
			firmante1Correo = "NO ESPECIFICADO";
		}

		return new RespuestaXml(true, "OPERACION EXITOSA", firmante1Nombre, firmante1Correo);
	}
	
	
	
	@RequestMapping(value = "/firmaAsync", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody RespuestaXml iniciarFirmaAsync(
			@RequestParam(value = GlobalConsts.ID_WORKFLOW, required = true) String idWorkflow,
			@RequestParam(value = GlobalConsts.ID_DOC, required = false) String idDoc,
			@RequestParam(value = GlobalConsts.ID_DOCS, required = false) String idDocs,
			@RequestParam(value = GlobalConsts.ASUNTO, required = true) String asunto,
			@RequestParam(value = GlobalConsts.COPIA_COMPULSADA, required = true) boolean copiaCompulsada,
			@RequestParam(value = GlobalConsts.CONEXION_USUARIO_CS, required = true) String conexionUsuario,
			@RequestParam(value = GlobalConsts.NOMBRE_USUARIO_CS, required = true) String nombreUsuario,
			@RequestParam(value = GlobalConsts.ID_USUARIO_CS, required = true) String idUsuario,
			@RequestParam(value = GlobalConsts.IDS_FIRMANTES, required = false) String listaFirmantes,
			@RequestParam(value = GlobalConsts.ID_FIRMANTE, required = false) String idFirmante,
			@RequestParam(value = GlobalConsts.ACCION_EXTERNA, required = false) String accionExterna,
			@RequestParam(value = GlobalConsts.URL_EXTERNA, required = false) String urlExterna,
			@RequestParam(value = GlobalConsts.ACTUALIZAR_CAT_DOCUMENTO, required = false) Boolean actualizarCatDocumento,
			@RequestParam(value = GlobalConsts.COMENTARIOS, required = false) String comentarios,
			@RequestParam(value = GlobalConsts.NOTIFICAR_SOLICITANTE, required = false) Boolean notificarSolicitante) {

		log.info("INICIANDO MAQUINA [" + idWorkflow + "]");

		StateMachine<States, Events> machine = null;
		if (StringUtils.hasText(idWorkflow)) {
			machine = getMachine(idWorkflow);
		}

		Map<String, Object> headers = new HashMap<>();
		headers.put(GlobalConsts.ID_WORKFLOW, idWorkflow);
		headers.put(GlobalConsts.CONEXION_USUARIO_CS, conexionUsuario);
		headers.put(GlobalConsts.NOMBRE_USUARIO_CS, nombreUsuario);
		headers.put(GlobalConsts.ASUNTO, asunto);
		headers.put(GlobalConsts.COPIA_COMPULSADA, copiaCompulsada);
		headers.put(GlobalConsts.ID_USUARIO_CS, idUsuario);
		headers.put(GlobalConsts.ID_DOC, idDoc);
		log.info("CONTROLLER - IdDocs: " + idDocs);
		if (StringUtils.hasText(idDocs)) {
			headers.put(GlobalConsts.ID_DOCS, idDocs);
		}

		if (StringUtils.hasText(idFirmante)) {
			headers.put(GlobalConsts.ID_FIRMANTE, idFirmante);
		} else if (StringUtils.hasText(listaFirmantes)) {
			headers.put(GlobalConsts.IDS_FIRMANTES, listaFirmantes);
		} else {
			throw new RuntimeException("Los parametro de fimantes son nulos");
		}

		if (StringUtils.hasText(accionExterna)) {
			headers.put(GlobalConsts.ACCION_EXTERNA, accionExterna);
		}

		if (StringUtils.hasText(urlExterna)) {
			headers.put(GlobalConsts.URL_EXTERNA, urlExterna);
		}

		if (actualizarCatDocumento != null) {
			headers.put(GlobalConsts.ACTUALIZAR_CAT_DOCUMENTO, actualizarCatDocumento);
		} else {
			actualizarCatDocumento = true;
			headers.put(GlobalConsts.ACTUALIZAR_CAT_DOCUMENTO, actualizarCatDocumento);
		}
		
		if (notificarSolicitante != null) {
			headers.put(GlobalConsts.NOTIFICAR_SOLICITANTE, notificarSolicitante);
		} else {
			notificarSolicitante = true;
			headers.put(GlobalConsts.NOTIFICAR_SOLICITANTE, notificarSolicitante);
		}
		
		if(StringUtils.hasText(comentarios)){
			headers.put(GlobalConsts.COMENTARIOS, comentarios);
		}

		if (log.isTraceEnabled()) {
			log.trace("Parametros recibidos por el servlet");
			log.trace(GlobalConsts.ID_WORKFLOW + ": " + idWorkflow);
			log.trace(GlobalConsts.ID_DOC + ": " + idDoc);
			log.trace(GlobalConsts.ID_DOCS + ": " + idDocs);
			log.trace(GlobalConsts.ASUNTO + ": " + asunto);
			log.trace(GlobalConsts.COPIA_COMPULSADA + ": " + copiaCompulsada);
			log.trace(GlobalConsts.CONEXION_USUARIO_CS + ": " + conexionUsuario);
			log.trace(GlobalConsts.NOMBRE_USUARIO_CS + ": " + nombreUsuario);
			log.trace(GlobalConsts.ID_USUARIO_CS + ": " + idUsuario);
			log.trace(GlobalConsts.IDS_FIRMANTES + ": " + listaFirmantes);
			log.trace(GlobalConsts.ID_FIRMANTE + ": " + idFirmante);
			log.trace(GlobalConsts.ACCION_EXTERNA + ": " + accionExterna);
			log.trace(GlobalConsts.URL_EXTERNA + ": " + urlExterna);
			log.trace(GlobalConsts.ACTUALIZAR_CAT_DOCUMENTO + ": " + actualizarCatDocumento);
			log.trace(GlobalConsts.COMENTARIOS + ": " + comentarios);
			log.trace(GlobalConsts.NOTIFICAR_SOLICITANTE + ": " + notificarSolicitante);
			log.trace("FIN Parametros recibidos por el servlet");
		}

		
		//final StateMachine<States, Events> innerMachine = machine;
		//Callable<RespuestaXml> callable = new Callable<RespuestaXml>() {

			//@Override
			//public RespuestaXml call() throws Exception {
				
				//innerMachine.sendEvent(MessageBuilder.createMessage(Events.INICIAR, new MessageHeaders(headers)));
				
				//return new RespuestaXml(true, "OPERACION EXITOSA");
			//}
			
		//};
		enviarEventoAsincrono(machine, headers, Events.INICIAR);

		return new RespuestaXml(true, "OPERACION EXITOSA");
	}

	
	
	@PostMapping(value = "/iniciarFirmaCaMe", produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody RespuestaXml iniciarFirmaCaMe(
			@RequestParam(value = GlobalConsts.ID_WORKFLOW, required = true) String idWorkflow,
			@RequestParam(value = GlobalConsts.ID_DOC, required = false) String idDoc,
			@RequestParam(value = GlobalConsts.ID_DOCS, required = false) String idDocs,
			@RequestParam(value = GlobalConsts.ASUNTO, required = true) String asunto,
			@RequestParam(value = GlobalConsts.COPIA_COMPULSADA, required = true) boolean copiaCompulsada,
			@RequestParam(value = GlobalConsts.CONEXION_USUARIO_CS, required = true) String conexionUsuario,
			@RequestParam(value = GlobalConsts.NOMBRE_USUARIO_CS, required = true) String nombreUsuario,
			@RequestParam(value = GlobalConsts.ID_USUARIO_CS, required = true) String idUsuario,
			@RequestParam(value = GlobalConsts.IDS_FIRMANTES, required = false) String listaFirmantes,
			@RequestParam(value = GlobalConsts.ID_FIRMANTE, required = false) String idFirmante,
			@RequestParam(value = GlobalConsts.ACCION_EXTERNA, required = false) String accionExterna,
			@RequestParam(value = GlobalConsts.URL_EXTERNA, required = false) String urlExterna,
			@RequestParam(value = GlobalConsts.ACTUALIZAR_CAT_DOCUMENTO, required = false) Boolean actualizarCatDocumento,
			@RequestParam(value = GlobalConsts.COMENTARIOS, required = false) String comentarios,
			@RequestParam(value = GlobalConsts.NOTIFICAR_SOLICITANTE, required = false) Boolean notificarSolicitante) {

		
		log.info("Entro al controller iniciarFirmaCaMe con los siguientes parametros: " 
				+ GlobalConsts.ID_WORKFLOW +" = ["+ idWorkflow + "] - " 
				+ GlobalConsts.ID_DOC +" = [" + idDoc + "] - "
			    + GlobalConsts.ID_DOCS +" = ["+ idDocs + "] - "
				+ GlobalConsts.ASUNTO +" = [" + asunto + "] - " 
			    + GlobalConsts.COPIA_COMPULSADA +" = [" + copiaCompulsada + "] - " 
			    + GlobalConsts.CONEXION_USUARIO_CS +" = [" + conexionUsuario + "] - " 
			    + GlobalConsts.NOMBRE_USUARIO_CS +" = [" + nombreUsuario + "] - " 
			    + GlobalConsts.ID_USUARIO_CS +" = [" + idUsuario + "] - " 
			    + GlobalConsts.IDS_FIRMANTES +" = [" + listaFirmantes + "] - " 
			    + GlobalConsts.ID_FIRMANTE +" = [" + idFirmante + "] - " 
			    + GlobalConsts.ACCION_EXTERNA +" = [" + accionExterna + "] - " 
			    + GlobalConsts.URL_EXTERNA +" = [" + urlExterna + "] - " 
			    + GlobalConsts.ACTUALIZAR_CAT_DOCUMENTO +" = [" + actualizarCatDocumento + "] - " 
			    + GlobalConsts.COMENTARIOS +" = [" + comentarios + "] - " 
			    + GlobalConsts.NOTIFICAR_SOLICITANTE +" = [" + notificarSolicitante + "] - " 
				);
		
		log.info("INICIANDO MAQUINA [" + idWorkflow + "]");

		StateMachine<States, Events> machine = null;
		if (StringUtils.hasText(idWorkflow)) {
			machine = getMachine(idWorkflow);
		}

		Map<String, Object> headers = new HashMap<>();
		headers.put(GlobalConsts.ID_WORKFLOW, idWorkflow);
		headers.put(GlobalConsts.CONEXION_USUARIO_CS, conexionUsuario);
		headers.put(GlobalConsts.NOMBRE_USUARIO_CS, nombreUsuario);
		headers.put(GlobalConsts.ASUNTO, asunto);
		headers.put(GlobalConsts.COPIA_COMPULSADA, copiaCompulsada);
		headers.put(GlobalConsts.ID_USUARIO_CS, idUsuario);
		headers.put(GlobalConsts.ID_DOC, idDoc);
		
		log.info("CONTROLLER - IdDocs: " + idDocs);
		log.info("¿IDocs tiene texto?"+StringUtils.hasText(idDocs));
		if (StringUtils.hasText(idDocs)) {
			headers.put(GlobalConsts.ID_DOCS, idDocs);
		}

		if (StringUtils.hasText(idFirmante)) {
			headers.put(GlobalConsts.ID_FIRMANTE, idFirmante);
		} else if (StringUtils.hasText(listaFirmantes)) {
			headers.put(GlobalConsts.IDS_FIRMANTES, listaFirmantes);
		} else {
			throw new RuntimeException("Los parametro de fimantes son nulos");
		}

		if (StringUtils.hasText(accionExterna)) {
			headers.put(GlobalConsts.ACCION_EXTERNA, accionExterna);
		}

		if (StringUtils.hasText(urlExterna)) {
			headers.put(GlobalConsts.URL_EXTERNA, urlExterna);
		}

		if (actualizarCatDocumento != null) {
			headers.put(GlobalConsts.ACTUALIZAR_CAT_DOCUMENTO, actualizarCatDocumento);
		} else {
			actualizarCatDocumento = true;
			headers.put(GlobalConsts.ACTUALIZAR_CAT_DOCUMENTO, actualizarCatDocumento);
		}
		
		if (notificarSolicitante != null) {
			headers.put(GlobalConsts.NOTIFICAR_SOLICITANTE, notificarSolicitante);
		} else {
			notificarSolicitante = true;
			headers.put(GlobalConsts.NOTIFICAR_SOLICITANTE, notificarSolicitante);
		}
		
		if(StringUtils.hasText(comentarios)){
			headers.put(GlobalConsts.COMENTARIOS, comentarios);
		}

		/*
		if (log.isTraceEnabled()) {
			log.trace("Parametros recibidos por el servlet");
			log.trace(GlobalConsts.ID_WORKFLOW + ": " + idWorkflow);
			log.trace(GlobalConsts.ID_DOC + ": " + idDoc);
			log.trace(GlobalConsts.ID_DOCS + ": " + idDocs);
			log.trace(GlobalConsts.ASUNTO + ": " + asunto);
			log.trace(GlobalConsts.COPIA_COMPULSADA + ": " + copiaCompulsada);
			log.trace(GlobalConsts.CONEXION_USUARIO_CS + ": " + conexionUsuario);
			log.trace(GlobalConsts.NOMBRE_USUARIO_CS + ": " + nombreUsuario);
			log.trace(GlobalConsts.ID_USUARIO_CS + ": " + idUsuario);
			log.trace(GlobalConsts.IDS_FIRMANTES + ": " + listaFirmantes);
			log.trace(GlobalConsts.ID_FIRMANTE + ": " + idFirmante);
			log.trace(GlobalConsts.ACCION_EXTERNA + ": " + accionExterna);
			log.trace(GlobalConsts.URL_EXTERNA + ": " + urlExterna);
			log.trace(GlobalConsts.ACTUALIZAR_CAT_DOCUMENTO + ": " + actualizarCatDocumento);
			log.trace(GlobalConsts.COMENTARIOS + ": " + comentarios);
			log.trace(GlobalConsts.NOTIFICAR_SOLICITANTE + ": " + notificarSolicitante);
			log.trace("FIN Parametros recibidos por el servlet");
		}
		*/
		
		//final StateMachine<States, Events> innerMachine = machine;
		//Callable<RespuestaXml> callable = new Callable<RespuestaXml>() {

			//@Override
			//public RespuestaXml call() throws Exception {
				
				//innerMachine.sendEvent(MessageBuilder.createMessage(Events.INICIAR, new MessageHeaders(headers)));
				
				//return new RespuestaXml(true, "OPERACION EXITOSA");
			//}
			
		//};
		enviarEventoAsincrono(machine, headers, Events.INICIAR);

		return new RespuestaXml(true, "OPERACION EXITOSA");
	}
	
	
	@RequestMapping(value = "/respuestafirmaOLD", method = RequestMethod.POST, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody RespuestaXml respuestaFirma(
			@RequestParam(value = "idCircuito", required = true) String idCircuito,
			@RequestParam(value = "estadoCircuito", required = true) String estadoCircuito,
			@RequestParam(value = "datosDocumentos", required = false) String datosDocumentos,
			@RequestParam(value = "motivo", required = false) String motivo,
			@RequestParam(value = "usuarioCancela", required = false) String usuarioCancela) {		
		
		log.info("Respuesta de Portafirmas recibida");
		log.info("Id del circuito: " + idCircuito);

		StateMachine<States, Events> machine = null;

		int idCircuitoRemoto = Integer.parseInt(idCircuito);
		log.debug("idCircuitoRemoto: " + idCircuitoRemoto);

		try {
			machine = findMachineByIdCircuto(idCircuitoRemoto);
		} catch (NullPointerException | NoSuchElementException e) {
			log.error("EL ID del circuito [" + idCircuito + "] no corresponde a ninguna maquina");
		} catch (NumberFormatException e) {
			log.error("EL ID del circuito [" + idCircuito + "] no corresponde al formato esperado", e);
		}

		if (machine != null) {
			log.info("Maquina encontrada");
			log.info("Maquina: " + machine.getId() + " corresponde al id de circuito: " + idCircuito);
			log.info("Estado del circuito: " + estadoCircuito);

			if (org.apache.commons.lang3.StringUtils.contains(estadoCircuito, ESTADO_PF_FINALIZADO)) {
				Map<String, Object> headers = new HashMap<String, Object>();
				headers.put(GlobalConsts.DATOS_PF, datosDocumentos);
				
				//Se envia el evento a la maquina de estados de manera asincrona para evitar que la app
				// de respuesta de portafirmas tenga que esperaer a que se complete todo el proceso
				enviarEventoAsincrono2(machine, headers, Events.RESPUESTA_PF_EXITOSA);
				//machine.sendEvent(
				//		MessageBuilder.createMessage(Events.RESPUESTA_PF_EXITOSA, new MessageHeaders(headers)));
								
				log.info("EVENTO ASINCRONO ENVIADO: " + Events.RESPUESTA_PF_EXITOSA);
				//log.info("EVENTO ENVIADO: " + Events.RESPUESTA_PF_EXITOSA);
				
			} else if (org.apache.commons.lang3.StringUtils.contains(estadoCircuito, ESTADO_PF_CANCELADO)) {
				log.info("ESTADO CIRCUITO CANCELADO");

				Map<String, Object> headers = new HashMap<>();
				headers.put(GlobalConsts.FIRMA_CANCELADA, true);
				if (StringUtils.hasText(motivo)) {
					headers.put(GlobalConsts.MOTIVO_CANCELACION, motivo);
				}
				if (StringUtils.hasText(usuarioCancela)) {
					headers.put(GlobalConsts.USUARIO_CANCELACION, usuarioCancela);
				}

				//machine.sendEvent(MessageBuilder.createMessage(Events.RESPUESTA_PF_CANCELADO, new MessageHeaders(headers)));
				enviarEventoAsincrono(machine, headers, Events.RESPUESTA_PF_CANCELADO);
				log.info("EVENTO ASINCRONO ENVIADO: " + Events.RESPUESTA_PF_CANCELADO);
				
				
				return new RespuestaXml(false, "ERROR OPERACION INICIAL");
			}
		} else {
			try {
				log.info("Inicio Restauracion BD");
				machine = restoreMachine(idCircuitoRemoto);
				log.info("Fin Restauracion BD");

				log.info("Maquina encontrada");
				log.info("Maquina: " + machine.getId() + " corresponde al id de circuito: " + idCircuito);
				log.info("Estado del circuito: " + estadoCircuito);

				Map<String, Object> headers = new HashMap<String, Object>();
				headers.put(GlobalConsts.DATOS_PF, datosDocumentos);

				//machine.sendEvent(MessageBuilder.createMessage(Events.RESPUESTA_PF_EXITOSA, new MessageHeaders(headers)));
				enviarEventoAsincrono(machine, headers, Events.RESPUESTA_PF_EXITOSA);
				log.info("EVENTO ASINCRONO ENVIADO: " + Events.RESPUESTA_PF_EXITOSA);

			} catch (NullPointerException | NoSuchElementException e) {
				log.error("EL ID del circuito [" + idCircuito + "] no corresponde a ninguna maquina");
				return new RespuestaXml(false, "ERROR OPERACION INICIAL");
			} catch (Exception e) {
				log.error("La maquina no pudo ser restaurada de la BD", e);
				return new RespuestaXml(false, "ERROR OPERACION INICIAL");
			}

		}
		return new RespuestaXml(true, "OPERACION EXITOSA");
	}
	
	/*
	@RequestMapping(value = "/respuestafirma", method = RequestMethod.POST, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody Callable<RespuestaXml> respuestaFirmaAsync(
			@RequestParam(value = "idCircuito", required = true) String idCircuito,
			@RequestParam(value = "estadoCircuito", required = true) String estadoCircuito,
			@RequestParam(value = "datosDocumentos", required = false) String datosDocumentos,
			@RequestParam(value = "motivo", required = false) String motivo,
			@RequestParam(value = "usuarioCancela", required = false) String usuarioCancela) {		
		
		log.info("Respuesta de Portafirmas recibida");
		log.info("Id del circuito: " + idCircuito);

		StateMachine<States, Events> machine = null;

		int idCircuitoRemoto = Integer.parseInt(idCircuito);
		log.debug("idCircuitoRemoto: " + idCircuitoRemoto);

		try {
			machine = findMachineByIdCircuto(idCircuitoRemoto);
		} catch (NullPointerException | NoSuchElementException e) {
			log.error("EL ID del circuito [" + idCircuito + "] no corresponde a ninguna maquina");
		} catch (NumberFormatException e) {
			log.error("EL ID del circuito [" + idCircuito + "] no corresponde al formato esperado", e);
		}
		
		StateMachine<States, Events> innerMachine = machine;
		Callable<RespuestaXml> callable = new Callable<RespuestaXml>() {

			@Override
			public RespuestaXml call() throws Exception {
				if (innerMachine != null) {
					log.info("Maquina encontrada");
					log.info("Maquina: " + innerMachine.getId() + " corresponde al id de circuito: " + idCircuito);
					log.info("Estado del circuito: " + estadoCircuito);

					if (org.apache.commons.lang3.StringUtils.contains(estadoCircuito, ESTADO_PF_FINALIZADO)) {
						Map<String, Object> headers = new HashMap<String, Object>();
						headers.put(GlobalConsts.DATOS_PF, datosDocumentos);
						
						//Se envia el evento a la maquina de estados de manera asincrona para evitar que la app
						// de respuesta de portafirmas tenga que esperaer a que se complete todo el proceso
						//enviarEventoAsincrono2(innerMachine, headers, Events.RESPUESTA_PF_EXITOSA);
						innerMachine.sendEvent(
								MessageBuilder.createMessage(Events.RESPUESTA_PF_EXITOSA, new MessageHeaders(headers)));
										
						//log.info("EVENTO ASINCRONO ENVIADO: " + Events.RESPUESTA_PF_EXITOSA);
						log.info("EVENTO ENVIADO: " + Events.RESPUESTA_PF_EXITOSA);
						
					} else if (org.apache.commons.lang3.StringUtils.contains(estadoCircuito, ESTADO_PF_CANCELADO)) {
						log.info("ESTADO CIRCUITO CANCELADO");

						Map<String, Object> headers = new HashMap<>();
						headers.put(GlobalConsts.FIRMA_CANCELADA, true);
						if (StringUtils.hasText(motivo)) {
							headers.put(GlobalConsts.MOTIVO_CANCELACION, motivo);
						}
						if (StringUtils.hasText(usuarioCancela)) {
							headers.put(GlobalConsts.USUARIO_CANCELACION, usuarioCancela);
						}

						innerMachine.sendEvent(MessageBuilder.createMessage(Events.RESPUESTA_PF_CANCELADO, new MessageHeaders(headers)));
						//enviarEventoAsincrono(innerMachine, headers, Events.RESPUESTA_PF_CANCELADO);
						log.info("EVENTO ASINCRONO ENVIADO: " + Events.RESPUESTA_PF_CANCELADO);
						
						
						
						return new RespuestaXml(false, "ERROR OPERACION INICIAL");
					}
				} else {
					try {
						log.info("Inicio Restauracion BD");
						StateMachine<States, Events > machine = restoreMachine(idCircuitoRemoto);
						log.info("Fin Restauracion BD");

						log.info("Maquina encontrada");
						log.info("Maquina: " + machine.getId() + " corresponde al id de circuito: " + idCircuito);
						log.info("Estado del circuito: " + estadoCircuito);

						Map<String, Object> headers = new HashMap<String, Object>();
						headers.put(GlobalConsts.DATOS_PF, datosDocumentos);

						machine.sendEvent(MessageBuilder.createMessage(Events.RESPUESTA_PF_EXITOSA, new MessageHeaders(headers)));
						//enviarEventoAsincrono(machine, headers, Events.RESPUESTA_PF_EXITOSA);
						log.info("EVENTO ASINCRONO ENVIADO: " + Events.RESPUESTA_PF_EXITOSA);

					} catch (NullPointerException | NoSuchElementException e) {
						log.error("EL ID del circuito [" + idCircuito + "] no corresponde a ninguna maquina");
						return new RespuestaXml(false, "ERROR OPERACION INICIAL");
					} catch (Exception e) {
						log.error("La maquina no pudo ser restaurada de la BD", e);
						return new RespuestaXml(false, "ERROR OPERACION INICIAL");
					}

				}
				return new RespuestaXml(true, "OPERACION EXITOSA");
			}
			
			
			
		};

		return callable;
		//return new RespuestaXml(true, "OPERACION EXITOSA");
	}
	*/
	
	@RequestMapping(value = "/respuestafirma", method = RequestMethod.POST, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody RespuestaXml respuestaFirmaAsync(
			@RequestParam(value = "idCircuito", required = true) String idCircuito,
			@RequestParam(value = "estadoCircuito", required = true) String estadoCircuito,
			@RequestParam(value = "datosDocumentos", required = false) String datosDocumentos,
			@RequestParam(value = "motivo", required = false) String motivo,
			@RequestParam(value = "usuarioCancela", required = false) String usuarioCancela) throws Exception {

		log.info("Respuesta de Portafirmas recibida");
		log.info("Id del circuito: " + idCircuito);

		StateMachine<States, Events> machine = null;

		int idCircuitoRemoto = Integer.parseInt(idCircuito);
		log.debug("idCircuitoRemoto: " + idCircuitoRemoto);
		
		/*
		try {
			machine = findMachineByIdCircuto(idCircuitoRemoto);
		} catch (NullPointerException | NoSuchElementException e) {
			log.error("EL ID del circuito [" + idCircuito + "] no corresponde a ninguna maquina");
		} catch (NumberFormatException e) {
			log.error("EL ID del circuito [" + idCircuito + "] no corresponde al formato esperado", e);
		}
		*/
		
		
		try {
		machine = restoreMachine(idCircuitoRemoto);
		}catch (Exception e) {
			log.error("EL ID del circuito [" + idCircuito + "] no corresponde a ninguna maquina");
			//throw e;
		}
		

		if (machine != null) {
			log.info("Maquina encontrada");
			log.info("Maquina: " + machine.getId() + " corresponde al id de circuito: " + idCircuito);
			log.info("Estado del circuito: " + estadoCircuito);

			if (org.apache.commons.lang3.StringUtils.contains(estadoCircuito, ESTADO_PF_FINALIZADO)) {
				Map<String, Object> headers = new HashMap<String, Object>();
				headers.put(GlobalConsts.DATOS_PF, datosDocumentos);

				// Se envia el evento a la maquina de estados de manera
				// asincrona para evitar que la app
				// de respuesta de portafirmas tenga que esperaer a que se
				// complete todo el proceso
				 enviarEventoAsincrono(machine, headers,Events.RESPUESTA_PF_EXITOSA);
				//machine.sendEvent(
						//MessageBuilder.createMessage(Events.RESPUESTA_PF_EXITOSA, new MessageHeaders(headers)));

				 log.info("EVENTO ASINCRONO ENVIADO: " +
				 Events.RESPUESTA_PF_EXITOSA);
				//log.info("EVENTO ENVIADO: " + Events.RESPUESTA_PF_EXITOSA);

			} else if (org.apache.commons.lang3.StringUtils.contains(estadoCircuito, ESTADO_PF_CANCELADO)) {
				log.info("ESTADO CIRCUITO CANCELADO");

				Map<String, Object> headers = new HashMap<>();
				headers.put(GlobalConsts.FIRMA_CANCELADA, true);
				if (StringUtils.hasText(motivo)) {
					headers.put(GlobalConsts.MOTIVO_CANCELACION, motivo);
				}
				if (StringUtils.hasText(usuarioCancela)) {
					headers.put(GlobalConsts.USUARIO_CANCELACION, usuarioCancela);
				}

				//machine.sendEvent(
				//		MessageBuilder.createMessage(Events.RESPUESTA_PF_CANCELADO, new MessageHeaders(headers)));
				enviarEventoAsincrono(machine, headers, Events.RESPUESTA_PF_CANCELADO);
				log.info("EVENTO ASINCRONO ENVIADO: " + Events.RESPUESTA_PF_CANCELADO);

				return new RespuestaXml(false, "ERROR OPERACION INICIAL");
			}
		} else {
			try {
				//log.info("Inicio Restauracion BD");
				//machine = restoreMachine(idCircuitoRemoto);
				//log.info("Fin Restauracion BD");
				
				
				try {
					machine = findMachineByIdCircuto(idCircuitoRemoto);
				} catch (NullPointerException | NoSuchElementException e) {
					log.error("EL ID del circuito [" + idCircuito + "] no corresponde a ninguna maquina");
					throw e;
				} catch (NumberFormatException e) {
					log.error("EL ID del circuito [" + idCircuito + "] no corresponde al formato esperado", e);
					throw e;
				}
				
				/*
				try {
					machine = restoreMachine(idCircuitoRemoto);
					}catch (Exception e) {
						log.error("EL ID del circuito [" + idCircuito + "] no corresponde a ninguna maquina");
						throw e;
					}
				*/	

				log.info("Maquina encontrada");
				log.info("Maquina: " + machine.getId() + " corresponde al id de circuito: " + idCircuito);
				log.info("Estado del circuito: " + estadoCircuito);

				if (org.apache.commons.lang3.StringUtils.contains(estadoCircuito, ESTADO_PF_FINALIZADO)) {
					Map<String, Object> headers = new HashMap<String, Object>();
					headers.put(GlobalConsts.DATOS_PF, datosDocumentos);

					// Se envia el evento a la maquina de estados de manera
					// asincrona para evitar que la app
					// de respuesta de portafirmas tenga que esperaer a que se
					// complete todo el proceso
					 enviarEventoAsincrono(machine, headers,Events.RESPUESTA_PF_EXITOSA);
					//machine.sendEvent(
							//MessageBuilder.createMessage(Events.RESPUESTA_PF_EXITOSA, new MessageHeaders(headers)));

					 log.info("EVENTO ASINCRONO ENVIADO: " +
					 Events.RESPUESTA_PF_EXITOSA);
					//log.info("EVENTO ENVIADO: " + Events.RESPUESTA_PF_EXITOSA);

				} else if (org.apache.commons.lang3.StringUtils.contains(estadoCircuito, ESTADO_PF_CANCELADO)) {
					log.info("ESTADO CIRCUITO CANCELADO");

					Map<String, Object> headers = new HashMap<>();
					headers.put(GlobalConsts.FIRMA_CANCELADA, true);
					if (StringUtils.hasText(motivo)) {
						headers.put(GlobalConsts.MOTIVO_CANCELACION, motivo);
					}
					if (StringUtils.hasText(usuarioCancela)) {
						headers.put(GlobalConsts.USUARIO_CANCELACION, usuarioCancela);
					}

					//machine.sendEvent(
					//		MessageBuilder.createMessage(Events.RESPUESTA_PF_CANCELADO, new MessageHeaders(headers)));
					enviarEventoAsincrono(machine, headers, Events.RESPUESTA_PF_CANCELADO);
					log.info("EVENTO ASINCRONO ENVIADO: " + Events.RESPUESTA_PF_CANCELADO);

					return new RespuestaXml(false, "ERROR OPERACION INICIAL");
				}

			} catch (NullPointerException | NoSuchElementException e) {
				log.error("EL ID del circuito [" + idCircuito + "] no corresponde a ninguna maquina");
				return new RespuestaXml(false, "ERROR OPERACION INICIAL");
			} catch (Exception e) {
				log.error("La maquina no pudo ser restaurada de la BD", e);
				return new RespuestaXml(false, "ERROR OPERACION INICIAL");
			}

		}
		return new RespuestaXml(true, "OPERACION EXITOSA");

		// return new RespuestaXml(true, "OPERACION EXITOSA");
	}
	
	
	@RequestMapping(value = "/reporteFirmantes", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody RespuestaXml reporteEmpleados() {

		log.info("Entro al controller /reporteFirmantes");
		try {
			firmantesCompleto.loadFirmantes(autenticacionCS.getAdminSoapHeader(), wsdlProps.getDocumento());
			log.info("Reporte Genrado correctamante");
			return new RespuestaXml(true, "OK");
		} catch (Exception e) {
			log.error("Error generando reporte de firmantes", e);
			return new RespuestaXml(false, "false");
		}
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
	private Future<Void> enviarEventoAsincrono(StateMachine<States, Events> machine, Map<String, Object> headers, Events event){
		
		CompletableFuture<Void> completableFuture = new CompletableFuture<Void>();
		
		Executors.newCachedThreadPool().submit(() -> {
			machine.sendEvent(
					MessageBuilder.createMessage(event, new MessageHeaders(headers)));			
		});		
		return completableFuture;		
	}
	*/
	
	@Async
	private void enviarEventoAsincrono(StateMachine<States, Events> machine, Map<String, Object> headers, Events event){		
		machine.sendEvent(	MessageBuilder.createMessage(event, new MessageHeaders(headers)));			
	}
	
	private Future<Void> enviarEventoAsincrono2(StateMachine<States, Events> machine, Map<String, Object> headers, Events event){
		
		CompletableFuture<Void> completableFuture = new CompletableFuture<Void>();
		
		Executors.newSingleThreadExecutor().submit(() -> {
			machine.sendEvent(
					MessageBuilder.createMessage(event, new MessageHeaders(headers)));			
		});		
		return completableFuture;		
	}
	
	
	

	@RequestMapping("/restaurar")
	public String restaurarMaquinas(@RequestParam(value = "action", required = false) String action,
			@RequestParam(value = "workid", required = false) String workId, Model model) {

		if (StringUtils.hasText(workId) && ObjectUtils.nullSafeEquals(action, "restaurarMaquina")) {

			log.info("ENTRANDO A RESTAURAR");
			log.info("Restaurando maquina: " + workId);

			if (machines.containsKey(workId)) {
				log.info("La maquina ya esta en memoria");
			} else {

				StateMachine<States, Events> machine = stateMachineFactory.getStateMachine(workId);
				StateMachine<States, Events> context = null;
				try {
					context = stateMachinePersister.restore(machine, workId);

					log.debug("Guardando maquina en memoria");
					machines.put(workId, context);

					log.info("Estado actual: " + context.getState().getId().name());
					model.addAttribute("respuesta", workId);

					log.info("Copiar variables a la BD");

					Map<Object, Object> variables = context.getExtendedState().getVariables();

					Map<Object, Object> variablesMap = new HashMap<>();

					log.info("Imprimir varibles");
					Iterator<Entry<Object, Object>> iterVariables = variables.entrySet().iterator();
					while (iterVariables.hasNext()) {
						Entry<Object, Object> unEntry = iterVariables.next();
						log.info("Un elemento - llave: " + unEntry.getKey() + " - valor: " + unEntry.getValue());
						variablesMap.put(unEntry.getKey(), unEntry.getValue());
					}

					contextoPersist.write(context.getId(), variablesMap);

					log.info("FIN RESTAURAR");

				} catch (Exception e) {
					log.error("Error restaurando maquina: " + workId, e);
					model.addAttribute("workId", workId);
					model.addAttribute("respuesta", "0");
				}
			}
		}

		return "restaurar";
	}

	@RequestMapping("/consola")
	public String consola(@RequestParam(value = "action", required = false) String action,
			@RequestParam(value = "evento", required = false) String evento,
			@RequestParam(value = "idMaquina", required = false) String idMaquina, Model model) {

		if (StringUtils.hasText(idMaquina) && StringUtils.hasText(evento)
				&& ObjectUtils.nullSafeEquals(action, "enviarEvento")) {
			StateMachine<States, Events> maquina = machines.get(idMaquina);

			if (evento.equals(Events.ERROR_OPERACION.toString())) {
				maquina.sendEvent(Events.ERROR_OPERACION);
			} else if (evento.equals(Events.ERROR_OPERACION_FINAL.toString())) {
				maquina.sendEvent(Events.ERROR_OPERACION_FINAL);
			} else if (evento.equals(Events.ERROR_OPERACION_FINAL_SIN_REINTENTO.toString())) {
				maquina.sendEvent(Events.ERROR_OPERACION_FINAL_SIN_REINTENTO);
			} else if (evento.equals(Events.RESPUESTA_PF_EXITOSA.toString())) {
				maquina.sendEvent(Events.RESPUESTA_PF_EXITOSA);
			} else if (evento.equals(Events.RESPUESTA_PF_CANCELADO.toString())) {
				maquina.sendEvent(Events.RESPUESTA_PF_CANCELADO);
			}

		}

		model.addAttribute("eventosConsola", EVENTOS_CONSOLA);
		model.addAttribute("idMaquina", idMaquina);
		model.addAttribute("maquinas", machines.values());

		return "consola";
	}

	private synchronized StateMachine<States, Events> getMachine(String id) {
		StateMachine<States, Events> machine = machines.get(id);
		if (machine == null) {
			machine = stateMachineFactory.getStateMachine(id);
			machines.put(id, machine);
		}
		return machine;
	}

	public Map<String, StateMachine<States, Events>> getMachines() {
		return machines;
	}

	private StateMachine<States, Events> restoreMachine(int idCircuto) throws NoSuchElementException, Exception {

		Maquina maquina = null;
		StateMachine<States, Events> machine = null;

		Optional<Maquina> maquinaOpt = repository.findByIdCircuito(idCircuto).stream().findFirst();

		if (maquinaOpt.isPresent()) {
			maquina = maquinaOpt.get();
		} else {
			throw new NoSuchElementException("No se encuentra la maquina en la BD");
		}

		if (maquina.getEstadoActual() != States.FINALIZADO.toString()) {

			String workId = maquina.getIdMaquina();

			machine = stateMachineFactory.getStateMachine(workId);
			StateMachine<States, Events> context = null;

			try {
				context = stateMachinePersister.restore(machine, workId);
				getMachines().put(workId, context);
			} catch (Exception e) {
				log.error("La maquina " + workId + "presento un fallo en el metodo restore.", e);
				throw new Exception("La maquina " + workId + "presento un fallo en el metodo restore.", e);
			}

			try {
				Map<Object, Object> variables = context.getExtendedState().getVariables();

				Map<Object, Object> variablesMap = new HashMap<>();

				Iterator<Entry<Object, Object>> iterVariables = variables.entrySet().iterator();
				while (iterVariables.hasNext()) {
					Entry<Object, Object> unEntry = iterVariables.next();
					log.info("Un elemento - llave: " + unEntry.getKey() + " - valor: " + unEntry.getValue());
					variablesMap.put(unEntry.getKey(), unEntry.getValue());
				}

				contextoPersist.write(context.getId(), variablesMap);
			} catch (Exception e) {
				log.error("Para la maquina " + workId + " no se pudo guardar el contexto.", e);
				throw new Exception("Para la maquina " + workId + "no se pudo guardar el contexto.", e);
			}
		}

		return machine;

	}

	private StateMachine<States, Events> findMachineByIdCircuto(int idCircuito) {

		StateMachine<States, Events> machine = null;

		Iterator<Entry<String, StateMachine<States, Events>>> iterMachines = machines.entrySet().iterator();
		while (iterMachines.hasNext()) {
			Entry<String, StateMachine<States, Events>> unMachineEntry = iterMachines.next();
			int idCircuitoLocal = unMachineEntry.getValue().getExtendedState().get(GlobalConsts.ID_CIRCUITO,
					Integer.class);
			
			if (idCircuitoLocal == idCircuito) {
				return machine = unMachineEntry.getValue();
			}
		}

		return machine;
	}

}
