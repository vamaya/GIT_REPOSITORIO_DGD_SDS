package co.gov.banrep.iconecta.ssm.correspondencia;



import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.activation.DataHandler;
import javax.xml.soap.SOAPException;
import javax.xml.ws.WebServiceException;

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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sun.xml.ws.fault.ServerSOAPFaultException;

import co.gov.banrep.iconecta.cs.autenticacion.AutenticacionCS;
import co.gov.banrep.iconecta.cs.cliente.user.Member;
import co.gov.banrep.iconecta.cs.documento.ContenidoDocumento;
import co.gov.banrep.iconecta.cs.documento.Reporte;
import co.gov.banrep.iconecta.cs.usuario.UsuarioCS;
import co.gov.banrep.iconecta.office.documento.entity.MetadatosPlantilla;
import co.gov.banrep.iconecta.ssm.correspondencia.dto.RespuestaEntrega;
import co.gov.banrep.iconecta.ssm.correspondencia.enums.Events;
import co.gov.banrep.iconecta.ssm.correspondencia.enums.States;
import co.gov.banrep.iconecta.ssm.correspondencia.exceptions.ErrorFuncional;
import co.gov.banrep.iconecta.ssm.correspondencia.params.CSCatDocumento;
import co.gov.banrep.iconecta.ssm.correspondencia.params.CategoriaProps;
import co.gov.banrep.iconecta.ssm.correspondencia.params.EstadoCorrespondenciaProps;
import co.gov.banrep.iconecta.ssm.correspondencia.params.GlobalProps;
import co.gov.banrep.iconecta.ssm.correspondencia.params.WsdlProps;
import co.gov.banrep.iconecta.ssm.correspondencia.persist.MachineRepository;
import co.gov.banrep.iconecta.ssm.correspondencia.persist.MaquinaCorrespondencia;
import co.gov.banrep.iconecta.ssm.correspondencia.persist.Mensaje;
import co.gov.banrep.iconecta.ssm.correspondencia.persist.MensajeRepository;
import co.gov.banrep.iconecta.ssm.correspondencia.persist.PruebaEntrega;
import co.gov.banrep.iconecta.ssm.correspondencia.persist.PruebaEntregaRepository;
import co.gov.banrep.iconecta.ssm.correspondencia.ruta.ControladorRutas;
import co.gov.banrep.iconecta.ssm.correspondencia.ruta.Posicion;
import co.gov.banrep.iconecta.ssm.correspondencia.utils.Consts;
import co.gov.banrep.iconecta.ssm.correspondencia.utils.ValidadorDocumento;
import co.gov.banrep.iconecta.ssm.correspondencia.xml.Atributos;
import co.gov.banrep.iconecta.ssm.correspondencia.xml.ErrorCorrespondencia;
import co.gov.banrep.iconecta.ssm.correspondencia.xml.Respuesta;
import co.gov.banrep.iconecta.ssm.correspondencia.xml.RespuestaValidador;
import co.gov.banrep.iconecta.ssm.correspondencia.xml.RespuestaXml;
import co.gov.banrep.iconecta.ssm.correspondencia.xml.RespuestaXmlDer;
import co.gov.banrep.iconecta.ssm.correspondencia.xml.readXML;

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////                          CONTROLLER                                 ///////////////////////
/////////////////////////////                     ----- SSM CORR -----                            ///////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


@Controller
@RequestMapping("/corr")
public class StateMachineController {

	// ----------------------- INICIO DECLARACION DE VARIABLES --------------------------
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private StateMachineFactory<States, Events> stateMachineFactory;
	
	@Autowired
	private WsdlProps wsdlsProps;

	@Autowired
	private MachineRepository repository;
	
	@Autowired
	private PruebaEntregaRepository repositoryPrueba;
	
	@Autowired
	private MensajeRepository repositorymensaje;
	
	@Autowired
	private CategoriaProps categoriaProps;
	
	@Autowired
	private CSCatDocumento categoriaDocProps;
	
	@Autowired
	private StateMachinePersister<States, Events, String> stateMachinePersister;
	
	@Autowired
	private GlobalProps globalProperties;
	
	@Autowired
	private  ContextRefreshedListener appContext;
	
	@Autowired
	private EstadoCorrespondenciaProps estadosProps;
	
	@Autowired
	private AutenticacionCS autenticacionCS;

	private final Map<String, StateMachine<States, Events>> machines = new HashMap<>();
	
	private final  CopyOnWriteArrayList<Long> radicados = new CopyOnWriteArrayList<>();
	
	private final Map<String, String> erroresDuplicados = new TreeMap<>();
	// ----------------------- FIN DECLARACION DE VARIABLES --------------------------
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////                INICIO REQUEST ----- DER -----                       ///////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Request para DER, inicio de radicacion: Este llamado inicia DER-Remoto
	 * 
	 * @param conexionUsuario
	 * @param workId
	 * @param idSolicitante
	 * @param oficina
	 * @param pcrOrigen
	 * @param cddOrigen
	 * @param tramiteDCIN
	 * @param radicadoDCIN
	 * @param pbxOrigen
	 * @param rolCalidadDigitalizar
	 * @return
	 */
	@RequestMapping(value = "/radicarDerAsync", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody Respuesta radicarDerAsync(
			@RequestParam(value = Consts.CONEXION_CS_SOLICITANTE, required = true) String conexionUsuario,
			@RequestParam(value = Consts.ID_WORKFLOW_RADICACION, required = true) String workId,
			@RequestParam(value = "idSolicitante", required = true) Long idSolicitante,
			@RequestParam(value = "oficina", required = true) String oficina,
			@RequestParam(value = "pcrOrigen", required = true) String pcrOrigen,
			@RequestParam(value = "cddOrigen", required = true) String cddOrigen,
			@RequestParam(value = "pbxOrigen", required = false) String pbxOrigen,
			@RequestParam(value = "rolCalidadDigitalizar", required = false) String rolCalidadDigitalizar) {

		log.info("Entro el controller /radicarDerAsync con parametros: " 
		        + Consts.CONEXION_CS_SOLICITANTE + " ["+ conexionUsuario + "]"
		        + " - " + Consts.ID_WORKFLOW_RADICACION + " [" + workId + "]"
		        + " - idSolicitante [" + idSolicitante + "]"
				+ " - oficina [" + oficina + "]"
				+ " - pcrOrigen [" + pcrOrigen + "]"
				+ " - cddOrigen [" + cddOrigen + "]"
				+ " - rolCalidadDigitalizar [" + rolCalidadDigitalizar + "]"
				+ " - pbxOrigen [" + pbxOrigen + "]"
				);

		String raizlog = "Workid [" + workId + "] - Controller radicarDerAsync - ";

		StateMachine<States, Events> machine = null;
		if (StringUtils.hasText(workId)) {
			machine = getMachine(workId);
		}

		String estadoActualInicio = machine.getState().getId().name().toString();
		log.info(raizlog+"INICIO ESTADO ACTUAL MAQUINA:" + estadoActualInicio);

		Map<String, Object> headers = new HashMap<>();
		headers.put(Consts.CONEXION_CS_SOLICITANTE, conexionUsuario);
		headers.put(Consts.OFICINA_DER, oficina);
		headers.put(Consts.ID_SOLICITANTE, idSolicitante);
		headers.put(Consts.ID_WORKFLOW_RADICACION, workId);
		headers.put(Consts.PCR_ORIGEN_DER, pcrOrigen);
		headers.put(Consts.CDD_ORIGEN_DER, cddOrigen);
		headers.put(Consts.ROL_CALIDAD_DIGITALIZAR_DER, rolCalidadDigitalizar);
		headers.put(Consts.PBX_ORIGEN, pbxOrigen);

		machine.sendEvent(MessageBuilder.createMessage(Events.INICIAR_RADICAR_DER, new MessageHeaders(headers)));

		log.info(raizlog + "Se envió el evento: ["+Events.INICIAR_RADICAR_DER+"]  para la maquina con workId: [" + workId +"]");

		Atributos atributos = new Atributos("OK", "", "", "", false, false, "", "");
		ErrorCorrespondencia errorCorrespondencia = new ErrorCorrespondencia(false, 0, "");

		log.info(raizlog + "FIN Controller");
		return new Respuesta(atributos, errorCorrespondencia);
	}
	
	
	
	/**
	 * Request para DER, el formulario DER ya se completo y ahora se extraen los
	 * datos
	 * 
	 * @param conexionUsuario
	 * @param workId
	 * @param idDoc
	 * @param radicadoDCIN
	 * @return
	 */
	@RequestMapping(value = "/obtenerDataFormDer", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody Respuesta obtenerDataFormDer(
			@RequestParam(value = Consts.CONEXION_CS_SOLICITANTE, required = true) String conexionUsuario,
			@RequestParam(value = Consts.ID_WORKFLOW_RADICACION, required = true) String workId,
			@RequestParam(value = Consts.ID_DOC_RADICACION, required = true) Long idDoc
			) {

		log.info("Entro el controller /obtenerDataFormDer con parametros: "
				+ "workId [" + workId + "] - "
				+ Consts.CONEXION_CS_SOLICITANTE + " [" + conexionUsuario + "] - " 
				+ Consts.ID_DOC_RADICACION + " [" + idDoc + "] - "
				);

		String raizlog = "Workid [" + workId + "] - Controller obtenerDataFormDer - ";

		if (idDoc < 1) {
			throw new IllegalArgumentException("El parametro idDoc (" + idDoc + ") no es valido");
		}

		// SE OBTIENE LA MÁQUINA DE LA BD
		StateMachine<States, Events> machine = null;
		if (StringUtils.hasText(workId)) {
			try {
				machine = obtenerMaquina(workId);
			} catch (Exception e) {
				String error = "Error obtenido maquina de base de datos";
				log.error(raizlog + error, e);
				Atributos atributos = new Atributos("respuesta fallo", "", "", "", false, false, "", "");
				ErrorCorrespondencia errorCorrespondencia = new ErrorCorrespondencia(true, 0, error);

				log.info(raizlog + "FIN Controller");
				return new Respuesta(atributos, errorCorrespondencia);
			}
		}

		
		// SE REVISA SI LA MAQUINA ESTA EN EL ESTADO ESPERADO
		String estadoActual = machine.getState().getId().name().toString();

		log.info(raizlog + "Estado Actual [" + estadoActual + "]  -  Estado Esperado [" + States.ESPERAR_OBTENER_DATOS_CATEGORIA_FORM_DER.toString() + "]");
		
		// SE DECLARAN VARIABLES DE RESPUESTA
		Atributos atributos;
		ErrorCorrespondencia errorCorrespondencia;
		
		//SE VALIDA EL ESTADO ACTUAL
		if(estadoActual.equalsIgnoreCase(States.ESPERAR_OBTENER_DATOS_CATEGORIA_FORM_DER.toString())) {
			
			Map<String, Object> headers = new HashMap<>();
			headers.put(Consts.ID_DOC_RADICACION, idDoc);

			machine.sendEvent(
					MessageBuilder.createMessage(Events.IR_OBTENER_DATOS_CATEGORIA_FORM_DER, new MessageHeaders(headers)));
			log.info(raizlog + "Se envió el evento: ["+Events.IR_OBTENER_DATOS_CATEGORIA_FORM_DER+"]  para la maquina con workId: [" + workId +"]");

			atributos = new Atributos("OK", "", "", "", false, false, "", "");
			errorCorrespondencia = new ErrorCorrespondencia(false, 0, "");

			log.info(raizlog + "FIN Controller");
			
			
		}else {// LA MAQUINA NO SE ENCONTRO EN EL ESTADO ESPERADO
			String error = "Estado actual es diferente al estado esperado";
			log.warn(raizlog + error);
			atributos = new Atributos("respuesta fallo", "", "", "", false, false, "", "");
			errorCorrespondencia = new ErrorCorrespondencia(true, 0, error);

			log.info(raizlog + "FIN Controller");
			
		}
		
		return new Respuesta(atributos, errorCorrespondencia);
	}
	
	
	
	/**
	 * Request para DER, se consulta el proceso de extraccion de datos del
	 * formulario
	 * 
	 * @param workId
	 * @return
	 */
	@RequestMapping(value = "/obtenerDataFormDerResult", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody Respuesta obtenerDataFormDerResult(
			@RequestParam(value = Consts.ID_WORKFLOW_RADICACION, required = true) String workId) {

		log.info("Entro el controller /obtenerDataFormDerResult con parametros: workId [" + workId + "]");

		String raizlog = "Workid [" + workId + "] - Controller obtenerDataFormDerResult - ";

		StateMachine<States, Events> machine = null;
		if (StringUtils.hasText(workId)) {
			try {
				machine = obtenerMaquina(workId);
				log.info(raizlog + "Maquina (" + workId + ") obtenida correctamente de la BD");

				String estadoActual = machine.getState().getId().name().toString();

				log.info(raizlog + "Estado Actual [" + estadoActual + "]");
			} catch (Exception e) {
				String error = "Error obtenido maquina de base de datos";
				log.error(raizlog + error, e);

				Atributos atributos = new Atributos("respuesta fallo", null, null, null, false, false, "", "");
				ErrorCorrespondencia errorCorrespondencia = new ErrorCorrespondencia(true, 0, "");

				log.info(raizlog + "FIN Controller");

				return new Respuesta(atributos, errorCorrespondencia);

			}

		}

		String estadoActual = machine.getState().getId().name().toString();

		log.info(raizlog + " estadoActual [" + estadoActual + "]");

		String destinosDER = machine.getExtendedState().get(Consts.DESTINOS_DER, String.class);
		String formaEntregaDER = machine.getExtendedState().get(Consts.FORMA_ENTREGA_DER, String.class);

		log.info(raizlog + " destinosDER [" + destinosDER + "] formaEntregaDER  [" + formaEntregaDER + "]");

		Atributos atributos = new Atributos("OK", destinosDER, formaEntregaDER, "", false, false, "", "");
		ErrorCorrespondencia errorCorrespondencia = new ErrorCorrespondencia(false, 0, "");

		log.info(raizlog + "FIN Controller");

		return new Respuesta(atributos, errorCorrespondencia);

	}
	
	
	
	
	/**
	 * Request para DER, aplica solo para DER electronico, llamado desde flujo de
	 * digitalizacion
	 * 
	 * @param workId
	 * @param idDocsDigitalizados
	 * @param esCalidad
	 * @return
	 */
	@RequestMapping(value = "/respuestaDigitalizar", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody RespuestaXml respuestaDigitalizar(
			@RequestParam(value = Consts.ID_WORKFLOW_RADICACION, required = true) String workId,
			@RequestParam(value = "idDocsDigitalizados", required = false) String idDocsDigitalizados,
			@RequestParam(value = "esCalidad", required = false) Boolean esCalidad) {

		log.info("Entro el controller /respuestaDigitalizar con parametros: workId [" + workId
				+ "] -  idDocsDigitalizados [" + idDocsDigitalizados + "] - esCalidad [" + esCalidad + "]");

		String raizlog = "Workid [" + workId + "] - Controller respuestaDigitalizar - ";

		StateMachine<States, Events> machine = null;
		if (StringUtils.hasText(workId)) {
			try {
				machine = obtenerMaquina(workId);
				log.info(raizlog + "Maquina [" + workId + "] obtenida correctamente de la BD");

				String estadoActual = machine.getState().getId().name().toString();

				log.info(raizlog + "Estado Actual [" + estadoActual + "]");
			} catch (Exception e) {
				String error = "Error obtenido maquina de base de datos";
				log.error(raizlog + error, e);

				log.info(raizlog + "FIN Controller");
				return new RespuestaXml(false, "false", 0L, false);
			}

		}

		Map<String, Object> headers = new HashMap<>();

		// SE OBTIENEN LOS TEXTOS DEL ESTADO ACTUAL Y DEL ESTADO ESPERADO
		String estadoActual = machine.getState().getId().name().toString();		
		String estadoEsperado = ( esCalidad==false? States.ESPERAR_DIGITALIZAR_DER.toString() : States.ESPERAR_CALIDAD_DIGITALIZAR_DER.toString() );		
		log.info(raizlog + "Estado actual ["+estadoActual+"]  - Estado esperado ["+estadoEsperado+"]");
		// SE VALIDA SI ESTADO ACTUAL ES IGUAL A ESTAO ESPERADO
		if(estadoActual.contentEquals(estadoEsperado)) { //ESTADO ACTUAL = ESTADO ESPERADO
			log.info(raizlog + "Estado actual es igual a Estado esperado..." );
			//  SE INICIALIZAN VARIABLES PARA GUARDAR LA RESPUESTA
			Long idGrupo = 0L;
			boolean result = false;
			// SE VALIDA SI EL ESTADO ES DIGITALIZAR
			if(estadoActual.contentEquals(States.ESPERAR_DIGITALIZAR_DER.toString())){
				log.info(raizlog + "Se va a consultar el rol que debe realizar el proceso de calidad...");
				String rolCalidadDigitalizar = machine.getExtendedState().get(Consts.ROL_CALIDAD_DIGITALIZAR_DER,
						String.class);
				log.info(raizlog + "Rol Calidad a consultar ["+rolCalidadDigitalizar+"]");
				Member grupo = null;
				// SE CONSULTA EL ID DEL GRUPO PARA PONER TAREA DE APROBACION
				try {
					grupo = UsuarioCS.getMemberByLoginName(autenticacionCS.getAdminSoapHeader(), rolCalidadDigitalizar,
							wsdlsProps.getUsuario());
					idGrupo = grupo.getID();
					log.info(raizlog + "idGrupo para Rol Calidad consultado ["+idGrupo+"]");
					
					headers.put(Consts.ID_DOCS_DIGITALIZADOS_DER, idDocsDigitalizados);
					machine.sendEvent(MessageBuilder.createMessage(Events.IR_CARGAR_CATEGORIA_DOC_DIGITALIZADO_DER, new MessageHeaders(headers)));
					//machine.sendEvent(Events.IR_ESPERAR_CALIDAD_DIGITALIZAR_DER);
					log.info(raizlog + "Se envió el evento: ["+Events.IR_CARGAR_CATEGORIA_DOC_DIGITALIZADO_DER+"]  para la maquina con workId: [" + workId +"]");
					log.info(raizlog + "DER Siquiente paso CALIDAD");
					result = true;
				} catch (SOAPException | IOException e) {
					log.error(raizlog + "Error :" + e.getMessage(), e);
				}
				
			}
			// SE VALIDA SI EL ESTADO ES CALIDAD
			else if(estadoActual.contentEquals(States.ESPERAR_CALIDAD_DIGITALIZAR_DER.toString())){
				log.info(raizlog + "Se ha realizado proceso de Aprobación del doc ["+idDocsDigitalizados+"]");
				//headers.put(Consts.ID_DOCS_DIGITALIZADOS_DER, idDocsDigitalizados);
				//headers.put(Consts.PASO_CALIDAD, "OK");
				//headers.put(Consts.ID_DOCS_DIGITALIZADOS_DER, idDocsDigitalizados);
				//machine.sendEvent(MessageBuilder.createMessage(Events.IR_DISTRIBUIR_DER, new MessageHeaders(headers)));	
				machine.sendEvent(Events.IR_DISTRIBUIR_DER);
				log.info(raizlog + "Se envió el evento: ["+Events.IR_DISTRIBUIR_DER+"]  para la maquina con workId: [" + workId +"]");
				log.info(raizlog + "DER Siquiente paso DISTRIBUIR_MEMORANDO");
				result = true;
				
			}
			
			log.info(raizlog + "FIN Controller");
			return new RespuestaXml(result, idGrupo.toString(), 0L, false);
			
			
			
		}else {// ESATDO ACTUAL != ESTADO ESPERADO
			log.info(raizlog + "Estado actual es diferente a Estado esperado..." );
			log.info(raizlog + "FIN Controller");
			// SE ENVÍA VALOR EN FALSE PARA LOGICA DE REINTENTOS EN EL WF
			return new RespuestaXml(false, "Error", 0L, false);			
		}
		

	}
	
	
	
	
	/**
	 * Request para DER, consulta de rutas en proceso de distribucion
	 * 
	 * @param rolActual
	 * @param resultado
	 * @param workId
	 * @param workidInstancia
	 * @param reintento
	 * @param codigoRuta
	 * @return
	 * @throws InterruptedException
	 */
	@RequestMapping(value = "/procesoRutaDistribucionDER", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody RespuestaXmlDer procesoRutaDistribucionDER(
			@RequestParam(value = "rolActual", required = true) String rolActual,
			@RequestParam(value = "resultado", required = true) String resultado,
			@RequestParam(value = "workId", required = true) String workId,
			@RequestParam(value = "workIdInstancia", required = false) long workidInstancia,
			@RequestParam(value = "reintento", required = false, defaultValue = "false") boolean reintento
			//@RequestParam(value = "codigoRuta", required = false) Long codigoRuta
			) throws InterruptedException {

		log.info("Entro el controller /procesoRutaDistribucionDER con parametros: "
				+ "rolActual [" + rolActual+ "] - "
				+ "resultado [" + resultado + "] - "
				+ "workId [" + workId + "] - "
				+ "workIdInstancia [" + workidInstancia+ "] "
				+ "- reintento [" + reintento + "]"
				//+ "codigoRuta [" + codigoRuta + "]"
				);

		String raizlog = "Workid [" + workId + "] - Controller procesoRutaDistribucionDER - ";

		String conexionUsuarioCS = globalProperties.getConexionUsuarioCS();

		StateMachine<States, Events> machine = null;

		if (StringUtils.hasText(workId)) {

			log.info(raizlog + "Se inicia proceso de obtención de la maquina de la BD");
			try {
				machine = obtenerMaquina(workId);
				log.info(raizlog + "Maquina [" + workId + "] obtenida correctamente de la BD");

				String estadoActual = machine.getState().getId().name().toString();

				log.info(raizlog + "Estado Actual [" + estadoActual + "]");
			} catch (Exception e) {
				String error = "Error obtenido maquina de base de datos";
				log.error(raizlog + error, e);
				RespuestaXmlDer respuestaXMl = RespuestaXmlDer.getRespuestaXmlDerError(200,
						"Error obtenido maquina de base de datos");
				log.info("RespuestaXML - " + respuestaXMl);
				log.info(raizlog + "FIN Controller");
				return respuestaXMl;
			}

		}

		String estadoActualInicio = machine.getState().getId().name().toString();
		log.info(raizlog + "Estado actual maquina [" + estadoActualInicio + "]");

		log.info(raizlog + "Distribucion Ruta - workidInstancia[" + workidInstancia + "] - resultado [" + resultado + "]");
		ControladorRutas controladorRuta = machine.getExtendedState().get(Consts.CONTROLADOR_RUTAS,
				ControladorRutas.class);

		//SE CONSULTA LA SIGUIENTE POSICIÓN BASADOS EN EL ESTADO (ACEPTADO O RECHAZADO)
		Posicion siguientePos = controladorRuta.getSiguientePaso(workidInstancia, resultado);

		//SE ACTUALIZA CONTROLADOR DE RUTAS EN EL CONTEXTO DE LA SSM
		machine.getExtendedState().getVariables().put(Consts.CONTROLADOR_RUTAS, controladorRuta);

		String siguienteRol = "FINALIZADO";
		if (siguientePos != null) {
			siguienteRol = siguientePos.getRol();

			log.info(raizlog + "Rol [" + siguienteRol + "]");

			Member rol = null;
			try {
				rol = UsuarioCS.getMemberByLoginName(autenticacionCS.getUserSoapHeader(conexionUsuarioCS), siguienteRol,
						wsdlsProps.getUsuario());
			} catch (SOAPException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Long idRol = rol.getID();

			log.info(raizlog + "Rol [" + siguienteRol + "] - id [" + idRol + "]");
			log.info(raizlog + "Distribucion Ruta - Pos [" + siguientePos.getPosicion() + "] - rol [" + siguienteRol + "]");

			// boolean finalizoRadicacion = false;

			log.info(raizlog + "Inicio guardar maquina");
			try {
				persistMaquina(machine);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			log.info(raizlog + "Fin guardar maquina");
			RespuestaXmlDer respuestaXMl = RespuestaXmlDer.getRespuestaXmlDerRolSiguiente(idRol, siguienteRol);
			log.info("RespuestaXML - " + respuestaXMl);
			log.info(raizlog + "FIN Controller");
			// return new RespuestaXml(finalizoRadicacion, idRol.toString(), codigoRuta,
			// true);
			return respuestaXMl;
		} else { //FINALIZÓ RUTA O FUÉ RECHAZADO
			
			Map<String, Object> headers = new HashMap<>();
			headers.put(Consts.ID_WORKFLOW_DIS, workidInstancia);
			machine.sendEvent(MessageBuilder.createMessage(Events.IR_ACTUALIZAR_ESTADO_FINAL_CORR_DER, new MessageHeaders(headers)));
			log.info(raizlog + "Se envió el evento: ["+Events.IR_ACTUALIZAR_ESTADO_FINAL_CORR_DER+"]  para la maquina con workId: [" + workId +"]");
			if(resultado.equalsIgnoreCase(Consts.RUTA_RECHAZADA)) {
				log.info(raizlog + "Distribucion Ruta - Rechazada, se dejará que el Admin se encargue de finalizar la distribución");
			}
			
			log.info(raizlog + "Distribucion Ruta - Pos [final] - rol [" + siguienteRol + "]");
			
			RespuestaXmlDer respuestaXMl = RespuestaXmlDer.getRespuestaXmlDerFinalizo();
			log.info("RespuestaXML - " + respuestaXMl);
			log.info(raizlog + "FIN Controller");
			// return new RespuestaXml(true, "", codigoRuta, true);
			return respuestaXMl;
		}

	}
	

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////                   FIN REQUEST ----- DER -----                       ///////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	
	
	//----------------------------------------------------------------------------------------------------------------------
	
	
	
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////            INICIO REQUEST ----- DER - EMAIL -----                   ///////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	/**
	 * Request para DER-Email, inicio radicacion
	 * 
	 * @param conexionUsuario
	 * @param workId
	 * @param oficina
	 * @param idSolicitante
	 * @param idDoc
	 * @param remitente
	 * @param tipoComunicacion
	 * @param dependenciaDestino
	 * @param pcrDestino
	 * @param destino
	 * @param asunto
	 * @param correoDestino
	 * @param correoRemitente
	 * @return
	 */
	@RequestMapping(value = "/radicarCorreoDERAsync", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody Respuesta radicarCorreoDERAsync(
			@RequestParam(value = Consts.CONEXION_CS_SOLICITANTE, required = true) String conexionUsuario,
			@RequestParam(value = Consts.ID_WORKFLOW_RADICACION, required = true) String workId,
			@RequestParam(value = "oficina", required = false) String oficina,
			@RequestParam(value = "idSolicitante", required = false) Long idSolicitante,
			@RequestParam(value = "idDoc", required = false) Long idDoc,
			@RequestParam(value = "remitente", required = false) String remitente,
			@RequestParam(value = "tipoComunicacion", required = false) String tipoComunicacion,
			@RequestParam(value = "dependenciaDestino", required = false) String dependenciaDestino,
			@RequestParam(value = "pcrDestino", required = false) String pcrDestino,
			@RequestParam(value = "destino", required = false) String destino,
			@RequestParam(value = "asunto", required = false) String asunto,
			@RequestParam(value = "correoDestino", required = false) String correoDestino,
			@RequestParam(value = "correoRemitente", required = false) String correoRemitente,
			//ADD NEW
			@RequestParam(value = "parentIdAdjuntos", required = false) Long parentIdAdjuntos,
			@RequestParam(value = "pcrOrigen", required = true) String pcrOrigen
			) {

		log.info("Entro el controller /radicarCorreoDERAsync con parametros: " 
				+ Consts.CONEXION_CS_SOLICITANTE + " ["+ conexionUsuario + "] - " 
				+ Consts.ID_WORKFLOW_RADICACION + " [" + workId + "] - "
				+ "idSolicitante [" + idSolicitante+ "] - "
				+ "oficina [" + oficina + "] - "
				+ "pcrDestino [" + pcrDestino + "] - "
				+ "idDoc [" + idDoc+ "] - "
				+ "remitente [" + remitente + "] - "
				+ "tipoComunicacion [" + tipoComunicacion+ "] - "
				+ "dependenciaDestino [" + dependenciaDestino + "] - "
				+ "destino [" + destino + "] - "
				+ "asunto[" + asunto+ "] - "
				+ "correoDestino[" + correoDestino + "] - "
				+ "correoRemitente[" + correoRemitente + "] - "
				+ "parentIdAdjuntos[" + parentIdAdjuntos + "] - "
				+ "pcrOrigen[" + pcrOrigen + "]"
				);

		// Vamaya: Se cambia el caracter ":" en el asunto de un correo DER, donde se
		// encuentre
		asunto = asunto.replace(":", " ");
		// Vamaya.

		// Vamaya: Se valida si el destino viene nulo
		if (correoDestino == null || correoDestino == "") {
			correoDestino = "Correo del destino no definido en la comunicación";
		}
		if (destino == null || destino == "") {
			destino = "Destino no definido en la comunicación";
		}
		// Vamaya.

		String raizlog = "Workid [" + workId + "] - Controller radicarCorreoDERAsync - ";

		StateMachine<States, Events> machine = null;
		if (StringUtils.hasText(workId)) {
			machine = getMachine(workId);
		}

		Map<String, Object> headers = new HashMap<>();
		headers.put(Consts.CONEXION_CS_SOLICITANTE, conexionUsuario);
		headers.put(Consts.ID_WORKFLOW_RADICACION, workId);
		headers.put(Consts.OFICINA_DER, oficina);
		headers.put(Consts.REMITENTE, remitente);
		headers.put(Consts.TIPO_COMUNICACION_DER, tipoComunicacion);
		headers.put(Consts.DESTINO_DER_EMAIL, destino);
		headers.put(Consts.ASUNTO, asunto);
		headers.put(Consts.CORREO_REMITENTE_DER_EMAIL, correoRemitente);
		headers.put(Consts.ID_SOLICITANTE, idSolicitante);
		headers.put(Consts.ID_DOC_RADICACION, idDoc);
		headers.put(Consts.CORREO_DESTINO_DER_EMAIL, correoDestino);
		headers.put(Consts.PCR_DESTINO_DER, pcrDestino);//ANTES SE HABÍA GUARDADO EN PCR_ORIGEN
		headers.put(Consts.DEPENDENCIA_DESTINO, dependenciaDestino);
		headers.put(Consts.PCR_ORIGEN_DER, pcrOrigen);
		headers.put(Consts.PARENT_ID_ADJUNTOS_DER_EMAIL, parentIdAdjuntos);

		String estado = machine.getState().getId().name().toString();
		log.info("Estado de la maquina" + estado);

		String estadoActualInicio = machine.getState().getId().name().toString();
		log.info("INICIO ESTADO ACTUAL MAQUINA:" + estadoActualInicio);

		machine.sendEvent(MessageBuilder.createMessage(Events.INICIAR_RADICAR_DER_EMAIL, new MessageHeaders(headers)));		
		log.info(raizlog + "Se envió el evento: ["+Events.INICIAR_RADICAR_DER_EMAIL+"]  para la maquina con workId: [" + workId +"]");

		Atributos atributos = new Atributos("true", "", "", "", false, false, "", "");
		ErrorCorrespondencia errorCorrespondencia = new ErrorCorrespondencia(false, 0, "");

		log.info(raizlog + "Estado de la maquina" + estado);

		log.info(raizlog + "FIN Controller");

		return new Respuesta(atributos, errorCorrespondencia);
	}
	
	
	
	
	/**
	 * Request para DER-Email, consulta proceso de radicacion
	 * 
	 * @param workId
	 * @return
	 */
	@RequestMapping(value = "/radicarCorreoDERResult", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody Respuesta radicarCorreoDERResult(
			@RequestParam(value = Consts.ID_WORKFLOW_RADICACION, required = true) String workId) {

		log.info("Entro el controller /radicarCorreoDERResult con parametros: workId [" + workId + "]");

		String raizlog = "Workid [" + workId + "] - Controller radicarCorreoDERResult - ";

		// Se obtiene la hora actual
		LocalDateTime fecha = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		String formatDateTime = fecha.format(formatter);

		String numeroRadicado = "";
		try {
			numeroRadicado = obtenerNumeroRadicadoBD(workId);
		} catch (Exception e) {
			log.error(raizlog + "Error consultado numero de radicado de la Base de datos", e);

			Atributos atributos = new Atributos(null, "", formatDateTime, "", false, false, "", "");
			ErrorCorrespondencia errorCorrespondencia = new ErrorCorrespondencia(true, 0, "");
			return new Respuesta(atributos, errorCorrespondencia);
		}

		log.info(raizlog + "numero de radicado Obtenido [" + numeroRadicado + "]");

		Atributos atributos = new Atributos(numeroRadicado, "", formatDateTime, "", false, false, "", "");
		ErrorCorrespondencia errorCorrespondencia = new ErrorCorrespondencia(false, 0, "");

		log.info(raizlog + "FIN Controller");

		return new Respuesta(atributos, errorCorrespondencia);

	}
	
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////               FIN REQUEST ----- DER - EMAIL -----                   ///////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	
	
	
	//----------------------------------------------------------------------------------------------------------------------
	
	
	
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////             INICIO REQUEST ----- CARTA/MEMO -----                   ///////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	
	/**
	 * Peticion "validarRadCA_ME", permite validar si un documento cumple con todas las condiciones para iniciar una radicacion
	 * en Content Server (CS) sin iniciar una máquina de estados
	 * @param workId id del flujo de radicacion iniciado desde CS
	 * @param idDoc id del documento en CS
	 * @param conexionUsuario conexión del usuario que inicio radicacion desde CS	 
	 * @return Salida XML *RespuestaValidador*, se especifica si hay anexos y errores
	 */
	@RequestMapping(value = "/validarRadCA_ME", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody RespuestaValidador validarRadCA_ME(
			@RequestParam(value = Consts.ID_DOC_RADICACION, required = true) long idDoc,
			@RequestParam(value = Consts.CONEXION_CS_SOLICITANTE, required = true) String conexionUsuario,
			@RequestParam(value = Consts.ID_WORKFLOW_RADICACION, required = true) String workId)
			{

		//MOSTRAR ATRIBUTOS INICIALES DEL LLAMADO HTTP DESDE CS
		log.info("Entro el controller /validarRadCA_ME con parametros: \n"
				+ Consts.ID_WORKFLOW_RADICACION + "[" + workId + "] - \n"
				+ Consts.CONEXION_CS_SOLICITANTE + " [" + conexionUsuario + "] - \n"
				+ Consts.ID_DOC_RADICACION + " [" + idDoc + "] \n"
				);

		//SE CREA RAIZ LOG, RAIZ QUE SE MOSTRARÁ AL INICIO DEL LOG
		String raizlog = "Workid [" + workId + "] - Controller validarRadCA_ME - ";

		//SE REVISA SI EL ID DEL DOCUMENTO YA SE ENCUENTRA EN PROCESO DE RADICACIÓN (PARA PROCESOS RECIENTES)
		boolean isRadicado = radicados.stream().anyMatch(x -> x.longValue() == idDoc);
		
		if(isRadicado) {			
			log.info(raizlog + "Radicación para ID documento [" + idDoc + "] ya fue iniciada");
			Iterable<Mensaje> mensajes =repositorymensaje.findAll();
			Iterator<Mensaje> men = mensajes.iterator();
			while(men.hasNext()) {
				Mensaje mensaje = men.next();
				if(mensaje.getCodigo().equals("112")) {
					erroresDuplicados.put(workId, mensaje.getMensaje());
				}
			}			
			erroresDuplicados.forEach((k,v) -> System.out.println("llave: "+ k + " valor: " + v));
			log.info(raizlog + "FIN CON ERRORES");
			int codigoError = 1;
			String mensajeError = "Ya se inició un flujo de radicación sobre el documento";
			return new RespuestaValidador(true,false,codigoError,mensajeError);//RESPUESTA CON ERRORES
			
		}else {
			// SE AÑADE EL ID DEL DOC A LOS DOCS YA RADICADOS
			radicados.add(idDoc);
		}
		
		//SE INCIAN VALIDACIONES Y SE RETORNA ANEXOS Y ERRORES
		log.info(raizlog + "Validando contenido del documento...");
		boolean anexos = false;
		try {
			anexos = ValidadorDocumento.validarDatosPlantilla(raizlog, workId, idDoc, conexionUsuario, appContext,autenticacionCS,globalProperties,wsdlsProps);
		} catch (ErrorFuncional e) {
			log.info(raizlog + "FIN CON ERRORES FUNCIONALES");
			int codigoError = 1;
			String mensajeError = e.getMessage();
			return new RespuestaValidador(true,anexos,codigoError,mensajeError);//RESPUESTA CON ERRORES FUNCIONALES
		} catch (Exception e) {
			log.info(raizlog + "FIN CON ERRORES DEL SISTEMA");
			int codigoError = 1;
			String mensajeError = e.getMessage();
			return new RespuestaValidador(true,anexos,codigoError,mensajeError);//RESPUESTA CON ERRORES TECNOLOGICOS
		}
		
		log.info(raizlog + "Validación exitosa");
		
		log.info(raizlog + "FIN Controller");

		return new RespuestaValidador(false, anexos);//RESPUESTA EXITOSA
	}
	
	
	
	
	
	@RequestMapping(value = "/radicarCA_ME", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody Respuesta radicarCA_ME(
			@RequestParam(value = Consts.ID_DOC_RADICACION, required = true) Long idDoc,
			@RequestParam(value = Consts.CONEXION_CS_SOLICITANTE, required = true) String conexionUsuario,
			@RequestParam(value = Consts.ID_SOLICITANTE, required = true) Long idSolicitante,
			@RequestParam(value = Consts.NOMBRE_SOLICITANTE, required = true) String nombreSolicitante,
			@RequestParam(value = Consts.ID_WORKFLOW_RADICACION, required = true) String workId,			
			@RequestParam(value = Consts.NOMBRE_DOCUMENTO_ORIGINAL, required = false) String nombreDocOriginal,
			@RequestParam(value = Consts.PARENT_ID_ORIGINAL_MEMO_CARTA, required = false) Long parentIdOriginal,
			@RequestParam(value = Consts.ID_DOCS_ADJUNTOS, required = false) String idDocsAdjuntos) {

		//MOSTRAR ATRIBUTOS INICIALES DEL LLAMADO HTTP DESDE CS
		log.info("Entro el controller /radicarCA_ME con parametros: \n"
				+ Consts.ID_WORKFLOW_RADICACION + "[" + workId + "] - \n"
				+ Consts.ID_DOC_RADICACION + " [" + idDoc + "] - \n"
				+ Consts.CONEXION_CS_SOLICITANTE + " [" + conexionUsuario + "] - \n" 
				+ Consts.ID_SOLICITANTE+" [" + idSolicitante + "] - \n"
				+ Consts.NOMBRE_SOLICITANTE + "[" + nombreSolicitante + "] - \n"
				+ Consts.PARENT_ID_ORIGINAL_MEMO_CARTA +" ["+ parentIdOriginal + "] - \n"
				+ Consts.NOMBRE_DOCUMENTO_ORIGINAL +" ["+ nombreDocOriginal + "] - \n"
				+ Consts.ID_DOCS_ADJUNTOS +" ["+ idDocsAdjuntos + "] \n"
				);

		//SE CREA RAIZ LOG, RAIZ QUE SE MOSTRARÁ AL INICIO DEL LOG
		String raizlog = "Workid [" + workId + "] - Controller radicarCA_ME - ";

		//SE INICIA MÁQUINA DE CORRESPONDENCIA
		log.info(raizlog + "INICIANDO MAQUINA DE CORRESPONDENCIA [" + workId + "]");
		StateMachine<States, Events> machine = null;
		if (StringUtils.hasText(workId)) {
			machine = getMachine(workId);
		}

		//SE CARGAN VARAIBLES EN EL CONTEXTO DE LA SSM		
		machine.getExtendedState().getVariables().put(Consts.ID_DOC_RADICACION, idDoc);
		machine.getExtendedState().getVariables().put(Consts.ID_WORKFLOW_RADICACION, workId);
		machine.getExtendedState().getVariables().put(Consts.CONEXION_CS_SOLICITANTE, conexionUsuario);
		machine.getExtendedState().getVariables().put(Consts.ID_SOLICITANTE, idSolicitante);
		machine.getExtendedState().getVariables().put(Consts.NOMBRE_SOLICITANTE, nombreSolicitante);
		machine.getExtendedState().getVariables().put(Consts.PARENT_ID_ORIGINAL_MEMO_CARTA, parentIdOriginal);
		machine.getExtendedState().getVariables().put(Consts.NOMBRE_DOCUMENTO_ORIGINAL, nombreDocOriginal);
		String nombreDocServidor = workId+"_"+idDoc+".docx";//Formato con el cual se almacenó la plantilla en el servidor OTBR
		machine.getExtendedState().getVariables().put(Consts.NOMBRE_DOCUMENTO_SERVIDOR, nombreDocServidor);
		if(idDocsAdjuntos != null) {
			machine.getExtendedState().getVariables().put(Consts.ID_DOCS_ADJUNTOS, idDocsAdjuntos);
		}
		
		//SE ENVÍA EVENTO PARA INICAR ESTADOS
		machine.sendEvent(Events.INICIAR_RADICAR_CA_ME);
		log.info(raizlog + "Se envió el evento: ["+Events.INICIAR_RADICAR_CA_ME+"]  para la maquina con workId: [" + workId +"]");

		
		log.info(raizlog + "FIN Controller");
		
		//SE RETORNA LA RESPUESTA AL FLUJO
		Atributos atributos = new Atributos("", "", "", "", false, false, "", "");
		ErrorCorrespondencia errorCorrespondencia = new ErrorCorrespondencia(false, 0, "");
		return new Respuesta(atributos, errorCorrespondencia);

	}
	
	
	
	/**
	 * Request para CA/ME, inicio del proceso de radicacion
	 * 
	 * @param idDoc
	 * @param conexionUsuario
	 * @param workId
	 * @param idSolicitante
	 * @param tipoWfDoc
	 * @param parentIdOriginal
	 * @param parentIdAdjuntos
	 * @param tipoDocumentalCatDocumento
	 * @param serieCatDocumento
	 * @return
	 */
	@RequestMapping(value = "/radicarAsync", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody Respuesta radicarAsync(@RequestParam(value = Consts.ID_DOC_RADICACION, required = true) long idDoc,
			@RequestParam(value = Consts.CONEXION_CS_SOLICITANTE, required = true) String conexionUsuario,
			@RequestParam(value = Consts.ID_WORKFLOW_RADICACION, required = true) String workId,
			@RequestParam(value = "idSolicitante", required = true) Long idSolicitante,
			@RequestParam(value = "tipoWfDoc", required = false) String tipoWfDoc,
			@RequestParam(value = "parentIdOriginal", required = false) Long parentIdOriginal,
			@RequestParam(value = "parentIdAdjuntos", required = false) Long parentIdAdjuntos,
			@RequestParam(value = "tipoDocumentalCatDocumento", required = false) String tipoDocumentalCatDocumento,
			@RequestParam(value = "serieCatDocumento", required = false) String serieCatDocumento) {

		log.info("Entro el controller /radicarAsync con parametros: workId [" + workId + "] - "
				+ Consts.CONEXION_CS_SOLICITANTE + " [" + conexionUsuario + "] - " + Consts.ID_DOC_RADICACION + " [" + idDoc + "] - "
				+ Consts.ID_WORKFLOW_RADICACION + "[" + workId + "] - idSolicitante [" + idSolicitante + "] - parentIdOriginal ["
				+ parentIdOriginal + "] - parentIdAdjuntos [" + parentIdAdjuntos + "] - tipoDocumentalCatDocumento ["
				+ tipoDocumentalCatDocumento + "] - serieCatDocumento [" + serieCatDocumento + "]");

		String raizlog = "Workid [" + workId + "] - Controller radicarAsync - ";

		boolean radicar = radicados.stream().anyMatch(x -> x.longValue() == idDoc);

		if (radicar) {

			log.info(raizlog + "maquina con ID documento [" + idDoc + "] ya fue iniciada");
			Iterable<Mensaje> mensajes = repositorymensaje.findAll();
			Iterator<Mensaje> men = mensajes.iterator();
			while (men.hasNext()) {
				Mensaje mensaje = men.next();
				if (mensaje.getCodigo().equals("112")) {
					erroresDuplicados.put(workId, mensaje.getMensaje());
				}
			}

			erroresDuplicados.forEach((k, v) -> System.out.println("llave: " + k + " valor: " + v));

			Atributos atributos = new Atributos("", "", "", "", false, false, "", "");
			ErrorCorrespondencia errorCorrespondencia = new ErrorCorrespondencia(false, 0, "");
			return new Respuesta(atributos, errorCorrespondencia);

		} else {

			radicados.add(idDoc);
		}

		if (tipoWfDoc == null || tipoWfDoc.isEmpty()) {
			tipoWfDoc = "Normal";
		}
				
		
		StateMachine<States, Events> machine = null;
		if (StringUtils.hasText(workId)) {
			machine = getMachine(workId);	
			log.info(raizlog + "Se generó la máquina ["+machine.getId()+"]");
		}
		

		Atributos atributos = new Atributos("", "", "", "", false, false, "", "");
		ErrorCorrespondencia errorCorrespondencia = new ErrorCorrespondencia(false, 0, "");

		log.info(raizlog + "INICIANDO MAQUINA DE CORRESPONDENCIA [" + workId + "]");
		Map<String, Object> headers = new HashMap<>();
		headers.put(Consts.ID_DOC_RADICACION, idDoc);
		headers.put(Consts.CONEXION_CS_SOLICITANTE, conexionUsuario);
		headers.put(Consts.ID_WORKFLOW_RADICACION, workId);
		headers.put(Consts.ID_SOLICITANTE, idSolicitante);
		headers.put(Consts.TIPO_WF_DOCUMENTO, tipoWfDoc);
		headers.put(Consts.PARENT_ID_ADJUNTOS_MEMO_CARTA, parentIdOriginal);
		headers.put(Consts.PARENT_ID_ADJUNTOS_WF_MEMO_CARTA, parentIdAdjuntos);
		headers.put(Consts.TIPO_DOCUMENTAL_CAT_DOCUMENTO, tipoDocumentalCatDocumento);
		headers.put(Consts.SERIE_CAT_DOCUMENTO, serieCatDocumento);

		log.info(raizlog + "El usuario que llega del CS es: [" + conexionUsuario + "]");

		log.info(raizlog + "ID Documento: [" + idDoc + "]");
				
		EnviarEventoIniciarAsincrono(machine, headers, Events.INICIAR_RADICAR_CA_ME);

		log.info(raizlog + "FIN Controller");

		return new Respuesta(atributos, errorCorrespondencia);

	}
	
	
	
	/**
	 * Request para CA/ME, consulta del proceso de inicio de radicacion
	 * 
	 * @param workId
	 * @return
	 */
	@RequestMapping(value = "/radicarAsyncResult", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody Respuesta radicarAsyncResult(
			@RequestParam(value = Consts.ID_WORKFLOW_RADICACION, required = true) String workId) {

		log.info("Entro el controller /radicarAsyncResult con parametros: workId [" + workId + "]");

		String raizlog = "Workid [" + workId + "] - Controller radicarAsyncResult - ";

		boolean error = erroresDuplicados.keySet().stream().anyMatch(x -> x.equals(workId));

		if (error) {
			Atributos atributos = new Atributos("", "", "", "", false, false, "", "");
			ErrorCorrespondencia errorCorrespondencia = new ErrorCorrespondencia(false, 112,
					erroresDuplicados.get(workId));
			return new Respuesta(atributos, errorCorrespondencia);
		}

		log.info(raizlog + "INICIO validar estado ESPERAR FIRMA ");
		String estadoActualBD = "";
		try {
			estadoActualBD = obtenerEstadoMaquinaBD(workId);
			log.info(raizlog + "Estado Actual [" + estadoActualBD + "]");
		} catch (Exception e1) {
			log.error("Error validando estado");
			log.error(raizlog, e1);
		}

		if (       (!estadoActualBD.equals(States.ESPERAR_FIRMA.toString()))
				&& (!estadoActualBD.equals(States.FINALIZADO_ERRORES.toString()))
				&& (!estadoActualBD.equals(States.FINALIZADO_ERRORES_FUNCIONALES.toString()))
				) {
			log.warn(raizlog + "Id workflow [" + workId + "] - Todavia no se esta en el estado ESPERAR FIRMA");

			String mensajeError = "Todavia no se esta en el estado ESPERAR FIRMA";
			int codigo = Consts.MSJ_108;

			log.warn(raizlog + "Id workflow [" + workId + "] - codigo de error: " + codigo + " mensaje de error: "
					+ mensajeError);

			Atributos atributos = new Atributos("", "", "", "", false, false, "", "");
			ErrorCorrespondencia errorCorrespondencia = new ErrorCorrespondencia(false, codigo, mensajeError);

			log.info(raizlog + "FIN Controller");
			return new Respuesta(atributos, errorCorrespondencia);
		}
		
		log.info(raizlog + "FIN validar estado ESPERAR FIRMA ");

		StateMachine<States, Events> machine = null;
		if (StringUtils.hasText(workId)) {
			try {
				machine = obtenerMaquina(workId);
				log.info(raizlog + "Maquina [" + workId + "] obtenida correctamente de la BD");

				String estadoActual = machine.getState().getId().name().toString();

				log.info(raizlog + "Estado Actual [" + estadoActual + "]");
			} catch (Exception e) {
				String errorBD = "Error obtenido maquina de base de datos";
				log.error(raizlog + errorBD, e);

				Atributos atributos = new Atributos("", "", "", "", false, false, "", "");
				ErrorCorrespondencia errorCorrespondencia = new ErrorCorrespondencia(false, 110,
						"Error usuario no se encuentra registrado en la tabla de empleados");

				log.info(raizlog + "FIN Controller");
				return new Respuesta(atributos, errorCorrespondencia);

			}

		}

		if (machine == null) {
			throw new NullPointerException("No se encontro la maquina");
		}

		String estadoActual = machine.getState().getId().name().toString();
		String tipologia = (machine.getExtendedState().get(Consts.TIPOLOGIA_MEMO_CARTA, String.class) == null) ? ""
				: machine.getExtendedState().get(Consts.TIPOLOGIA_MEMO_CARTA, String.class);
		//String cddOrigen = machine.getExtendedState().get(Consts.CDD_ORIGEN_CARTA, String.class);
		//String pcrOrigen = machine.getExtendedState().get(Consts.PCR_ORIGEN_CARTA, String.class);
		long idDoc = machine.getExtendedState().get(Consts.ID_DOC_RADICACION, Long.class);

		radicados.remove(idDoc);

		radicados.stream().forEach(System.out::println);

		log.info(raizlog + "Id workflow [" + workId + "[ - Estado Actual: [" + estadoActual+"]");

		/*
		if (tipologia.equals("CA") && (cddOrigen == null || pcrOrigen == null)) {

			log.info(raizlog + "Id workflow (" + workId
					+ ") - Error  usuario no se encuentra registrado en la tabla de empleados");

			Atributos atributos = new Atributos("", "", "", "", false, false, "", "");
			ErrorCorrespondencia errorCorrespondencia = new ErrorCorrespondencia(false, 110,
					"Error usuario no se encuentra registrado en la tabla de empleados");

			log.info(raizlog + "FIN Controller");
			return new Respuesta(atributos, errorCorrespondencia);
		}
		*/

		if (estadoActual.equals(States.ESPERAR_FIRMA.toString())) {

			boolean isError = false;
			// Atributos atributos = new Atributos("", "", "", false, false, "", "");
			Atributos atributos = Atributos.getAtributosEsperarFirma();
			ErrorCorrespondencia errorCorrespondencia = new ErrorCorrespondencia(false, 0, "");
			try {

				String mensajeError = "";
				String numeroRadicado = "";
				String idsDocsPersonalizados = "";
				String asunto = "";
				String nitFirmante = "";
				String listaNitFirmantes = "";
				Boolean tieneAdjuntos = false;
				Boolean esPersonalizado = false;
				try {

					isError = machine.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class);
					mensajeError = machine.getExtendedState().get(Consts.MENSAJE_ERROR, String.class);

				} catch (NullPointerException e) {
					isError = false;
				}

				if (isError) {

					int codigoError = machine.getExtendedState().get(Consts.CODIGO_ERROR_RADICAR, Integer.class);
					errorCorrespondencia = new ErrorCorrespondencia(false, codigoError, mensajeError);

					log.info(raizlog + "Id workflow (" + workId + ") - Respuesta Fallida con Codigo: " + codigoError
							+ " mensaje: " + mensajeError);

				} else {
					numeroRadicado = machine.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO, String.class);
					asunto = machine.getExtendedState().get(Consts.ASUNTO, String.class);
					nitFirmante = machine.getExtendedState().get(Consts.NIT_FIRMANTE, String.class);
					listaNitFirmantes = machine.getExtendedState().get(Consts.LISTA_NIT_FIRMANTES, String.class);
					tieneAdjuntos = machine.getExtendedState().get(Consts.TIENE_ANEXOS, Boolean.class);
					idsDocsPersonalizados = machine.getExtendedState()
							.get(Consts.CADENA_LISTA_ID_DOCS_PERSONALIZADOS_CARTA, String.class);
					esPersonalizado = machine.getExtendedState().get(Consts.ES_PERSONALIZADO, Boolean.class);

					log.info(raizlog + "Id workflow (" + workId + ") - Respuesta Exitosa con numero radicado: "
							+ numeroRadicado);

					atributos = new Atributos(numeroRadicado, asunto, nitFirmante, listaNitFirmantes, tieneAdjuntos,
							esPersonalizado, idsDocsPersonalizados, tipologia);
					errorCorrespondencia = new ErrorCorrespondencia(true, 0, "");

				}

				log.info(raizlog + "FIN Controller");
				return new Respuesta(atributos, errorCorrespondencia);

			} catch (RuntimeException e) {

				log.info(raizlog + "ErrorCorrespondencia operacion inicial: " + e);
				atributos = new Atributos("", "", "", "", false, false, "", "");
				errorCorrespondencia = new ErrorCorrespondencia(false, 02,
						"ErrorCorrespondencia en ejecucion de Maquina de Estados Correspondencia:" + e);

				log.info(raizlog + "FIN Controller");
				return new Respuesta(atributos, errorCorrespondencia);
			}

		} else {

			if (estadoActual.equals(States.FIN.toString())
					|| estadoActual.equals(States.FINALIZADO_ERRORES.toString())
					|| estadoActual.equals(States.FINALIZADO_ERRORES_FUNCIONALES.toString())) {

				log.error(raizlog + "Id workflow [" + workId + "] - Fallo radicacion carta Async");

				String mensajeError = (machine.getExtendedState().get(Consts.MENSAJE_ERROR, String.class) == null)
						? "Fallo radicacion carta Async"
						: machine.getExtendedState().get(Consts.MENSAJE_ERROR, String.class);
				int codigo = (machine.getExtendedState().get(Consts.CODIGO_ERROR_RADICAR, Integer.class) == null)
						? Consts.MSJ_109
						: machine.getExtendedState().get(Consts.CODIGO_ERROR_RADICAR, Integer.class);

				log.warn(raizlog + "Id workflow [" + workId + "] - codigo de error: [" + codigo + "] - mensaje de error: ["
						+ mensajeError+"]");

				Atributos atributos = new Atributos("", "", "", "", false, false, "", "");
				ErrorCorrespondencia errorCorrespondencia = new ErrorCorrespondencia(false, codigo, mensajeError);

				log.info(raizlog + "FIN Controller");
				return new Respuesta(atributos, errorCorrespondencia);

			} else {

				log.warn(raizlog + "Id workflow [" + workId + "] - Todavia no se esta en el estado ESPERAR FIRMA");

				String mensajeError = (machine.getExtendedState().get(Consts.MENSAJE_ERROR, String.class) == null)
						? "Todavia no se esta en el estado ESPERAR FIRMA"
						: machine.getExtendedState().get(Consts.MENSAJE_ERROR, String.class);
				int codigo = (machine.getExtendedState().get(Consts.CODIGO_ERROR_RADICAR, Integer.class) == null)
						? Consts.MSJ_108
						: machine.getExtendedState().get(Consts.CODIGO_ERROR_RADICAR, Integer.class);

				log.warn(raizlog + "Id workflow (" + workId + ") - codigo de error: " + codigo + " mensaje de error: "
						+ mensajeError);

				Atributos atributos = new Atributos("", "", "", "", false, false, "", "");
				ErrorCorrespondencia errorCorrespondencia = new ErrorCorrespondencia(false, codigo, mensajeError);

				log.info(raizlog + "FIN Controller");
				return new Respuesta(atributos, errorCorrespondencia);
			}

		}

	}
	
	
	
	
	/**
	 * Metodo que permite actualizar el estado de la categoria Correspondencia de un documento
	 * 
	 * @param idDoc identificador del documento al cual se le va a actualizar el estado de la categoria correspondencia en el CS
	 * @param nuevoEstado cadena de texto que indica el nuevo estado que se va a asignar a la categoria correspondencia del doc en el CS
	 * @return
	 */
	//TODO Revisar este Request por codigo repetido en SSMUtils
	@RequestMapping(value = "/actualizarEstadoCorrespondencia", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody RespuestaXml actualizarEstadoCorrespondencia(
			@RequestParam(value = "idDoc", required = true) long idDoc, 
			@RequestParam(value = "nuevoEstado", required = true) String nuevoEstado) {
		
		log.info("Entro el controller /actualizarEstadoCorrespondencia con parametros: idDoc [" + idDoc
				+ "] - nuevoEstado [" + nuevoEstado + "]");

		String raizlog = "IdDoc [" + idDoc + "] - Controller actualizarEstadoCorrespondencia - ";
		
		int idCategoria=categoriaProps.getCorrespondencia().getId();
		String nombreCategoria = categoriaProps.getCorrespondencia().getNombre();
		String nombreCampo=categoriaProps.getCorrespondencia().getAtributoEstado();
		
		String nuevoEstadoResultado="";
		boolean result = false;
		
		try {
			log.info(raizlog + "INICIO cambiar estado correspndencia");
			nuevoEstadoResultado = ContenidoDocumento.actualizarAtributoCategoria(autenticacionCS.getAdminSoapHeader(),
					idDoc, idCategoria, nombreCategoria, nombreCampo, nuevoEstado, wsdlsProps.getDocumento());
			result = true;
			
			log.info(raizlog + "FIN cambiar estado correspndencia el nuevo estado es:"+nuevoEstadoResultado);
			log.info(raizlog +  "FIN actualizarEstadoCorrespondencia para el doc("+idDoc+") con nuevo estado ("+nuevoEstado+")");
			
		} catch (ServerSOAPFaultException | IOException | SOAPException | InterruptedException e) {
			log.error(raizlog + "No se pudo actualizar el estado del documento (" + idDoc + ")");
			log.error(raizlog + "Error de Content Server", e);			
		}

		log.info(raizlog + "FIN Controller");
		return new RespuestaXml(result, nuevoEstadoResultado, 0L, false);
	}
	
	
	
	
	
	/**
	 * Request para CA - RDI/RDE, cargue de prueba de entrega en proceso de distribucion
	 * 
	 * @param workId
	 * @param conexionUsuario
	 * @param currier
	 * @param numeroGuia
	 * @param numeroRadicado
	 * @param idDocPruebaEntrega
	 * @param idDocOriginal
	 * @param destino
	 * @return
	 */
	@RequestMapping(value = "/pruebaEntrega", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody RespuestaXml pruebaEntrega(@RequestParam(value = "workId", required = true) String workId,
			@RequestParam(value = Consts.CONEXION_CS_SOLICITANTE, required = true) String conexionUsuario,
			@RequestParam(value = "currier", required = false) String currier,
			@RequestParam(value = "numeroGuia", required = false) String numeroGuia,
			@RequestParam(value = "numeroRadicado", required = false) String numeroRadicado,
			@RequestParam(value = "pruebaEntrega", required = false) long idDocPruebaEntrega,
			@RequestParam(value = "idDocOriginal", required = false, defaultValue = "0") long idDocOriginal,
			@RequestParam(value = "destino", required = false) String destino) {
		
		log.info("Entro el controller /pruebaEntrega con parametros: workId [" + workId + "] - "
				+ Consts.CONEXION_CS_SOLICITANTE + " [" + conexionUsuario + "] - numeroRadicado [" + numeroRadicado
				+ "] - currier [" + currier + "] - numeroGuia [" + numeroGuia + "] - numeroRadicado [" + numeroRadicado
				+ "] - pruebaEntrega [" + idDocPruebaEntrega + "] - idDocOriginal [" + idDocOriginal + "] - destino ["
				+ destino + "]");

		String raizlog = "Workid [" + workId + "] - Controller pruebaEntrega - ";
				
				
		StateMachine<States, Events> machine = null;
		if (StringUtils.hasText(workId)) {
			try {
				machine = obtenerMaquina(workId);
				log.info(raizlog + "Maquina (" + workId + ") obtenida correctamente de la BD");
				
				String estadoActual = machine.getState().getId().name().toString();
				
				log.info(raizlog + "Estado Actual [" + estadoActual + "]" );				
			} catch (Exception e) {
				String error = "Error obtenido maquina de base de datos";
				log.error(raizlog + error ,e);
				
				log.info(raizlog + "FIN Controller");
				return new RespuestaXml(false, "respuesta fallo", 0L, false);
			}

		}
		
		String tipologia = (machine.getExtendedState().get(Consts.TIPOLOGIA_RDI_RDE, String.class)) == null ? (machine.getExtendedState().get(Consts.TIPOLOGIA_MEMO_CARTA, String.class)) : (machine.getExtendedState().get(Consts.TIPOLOGIA_RDI_RDE, String.class));
		String siglaOrigen = "";
		
		log.info(raizlog + "Tipologia es: " +  tipologia);
		
		if(!tipologia.equals("CA")) {			
			siglaOrigen = "BR";			
		}else {			
			MetadatosPlantilla cat = machine.getExtendedState().get(Consts.METADATOS_PLANTILLA, MetadatosPlantilla.class);
			siglaOrigen = cat.getFirmantes().get(0).getSiglaremitente();
			
			if(!cat.getEsFondoIndependiente()) {
				siglaOrigen = "BR";
			}
			
		}
		
		/*
		PruebaEntrega pruebaEntrega = null;
		List<PruebaEntrega> pruebaEntregas=repositoryPrueba.findByIdMaquinaAndNumeroRadicadoAndDestino(workId, numeroRadicado, destino);
		
		if(pruebaEntregas != null && !pruebaEntregas.isEmpty()) {
			pruebaEntrega = pruebaEntregas.get(0);
		}
		*/
		
		//Long id=null;
		
		PruebaEntrega pruebaEntrega = null;
		Optional<PruebaEntrega> unaPruebaDeEntregaOpt = repositoryPrueba.findByIdMaquinaAndNumeroRadicadoAndDestino(workId, numeroRadicado, destino).stream().findFirst();
		
		if (unaPruebaDeEntregaOpt.isPresent()) {
			pruebaEntrega = unaPruebaDeEntregaOpt.get();

			if (idDocPruebaEntrega != 0) {

				LocalDate date = LocalDate.now();

				long idCarpeta = readXML.getIdFolderXML(date.getYear(), date.getMonthValue(), siglaOrigen,
						"PruebaEntrega", globalProperties.getRutaXML());

				String nombreDoc = "";

				if (pruebaEntrega.getNombreDocumento() == null) {
					nombreDoc = "nombre Documento es nulo";
				} else {
					nombreDoc = pruebaEntrega.getNombreDocumento();
				}

				log.info(raizlog + "Entro al controller pruebaEntrega con parametros idCarpeta:" + idCarpeta
						+ " idDocPruebaEntrega:" + idDocPruebaEntrega + " pruebaEntrega.getNombreDocumento():"
						+ nombreDoc);

				DataHandler dataHandlerDoc = null;

				try {
					dataHandlerDoc = ContenidoDocumento.obtenerContenidoDocumentoDTHLR(
							autenticacionCS.getAdminSoapHeader(), idDocPruebaEntrega, globalProperties.getRutaTemp(),
							workId, wsdlsProps.getDocumento());
				} catch (SOAPException | IOException | WebServiceException | InterruptedException e) {					
					log.error(raizlog + "Error obtenido documento - " + e.getMessage(),e); 
					
					log.info(raizlog + "FIN Controller");
					return new RespuestaXml(false, "respuesta fallo", 0L, false);
				}

				nombreDoc = pruebaEntrega.getNombreDocumento();

				try {
					String nombreCompelto = ContenidoDocumento.obtenerNombreDocumento(
							autenticacionCS.getUserSoapHeader(conexionUsuario), idDocPruebaEntrega,
							wsdlsProps.getDocumento());

					log.info(raizlog + "Entro al controller pruebaEntrega con parametros nombreCompelto:" + nombreCompelto);
					String[] partesDocumento = nombreCompelto.split("\\.");
					nombreDoc = numeroRadicado + " - " + destino + "." + partesDocumento[partesDocumento.length - 1];

				} catch (SOAPException | IOException | WebServiceException | InterruptedException e) {
					log.error(raizlog + "Error obtenido documento 2 - " + e.getMessage(),e); 
					
					log.info(raizlog + "FIN Controller");
					return new RespuestaXml(false, "respuesta fallo", 0L, false);
					
				}

				long idDocConsecutivo = 0L;

				Map<String, Object> metadatosCatDoc = new HashMap<String, Object>();

				metadatosCatDoc.put("Tipo documental", readXML.getTypeDcocumentXML(date.getYear(), date.getMonthValue(),
						siglaOrigen, "PruebaEntrega", globalProperties.getRutaXML()));
				metadatosCatDoc.put("Serie", readXML.getSerieDocumentXML(date.getYear(), date.getMonthValue(),
						siglaOrigen, "PruebaEntrega", globalProperties.getRutaXML()));

				
				try {
					idDocConsecutivo = ContenidoDocumento.crearDocumentoCatDocumento(
							(autenticacionCS.getAdminSoapHeader()), null, nombreDoc, dataHandlerDoc,
							categoriaDocProps.getId(), categoriaDocProps.getNombreCategoria(), metadatosCatDoc, false,
							idCarpeta, wsdlsProps.getDocumento());
					log.info(raizlog + "Documento consecutivo: " + idDocConsecutivo);
				} catch (SOAPException | IOException | WebServiceException | InterruptedException e) {
					log.error(raizlog + "Error crearDocumentoCatDocumento - " + e.getMessage(),e); 					
					
					log.info(raizlog + "FIN Controller");
					return new RespuestaXml(false, "respuesta fallo", 0L, false);
				}
				

			}
			// }

			// if (pruebaEntrega != null) {

			//id = pruebaEntrega.getId();
			pruebaEntrega.setCurrier(currier);
			pruebaEntrega.setNumeroGuia(numeroGuia);
			pruebaEntrega.setFechaRecibido(new Date());

			repositoryPrueba.save(pruebaEntrega);

			// }
		}else {
		//if(pruebaEntrega == null){
		
			List<PruebaEntrega> pruebasEntregas = repositoryPrueba.findByIdMaquinaAndNumeroRadicado(workId, numeroRadicado);
			
			if (!pruebasEntregas.isEmpty()) {

				long totaldestinos = pruebasEntregas.size();
				long totalConfirmados = pruebasEntregas.stream().filter(p -> p.getFechaRecibido() != null).count();

				if (totalConfirmados == totaldestinos) {

					try {
						actualizarEstadoCorrespondencia(idDocOriginal, estadosProps.getConfirmado());
					} catch (Exception e) {
						log.error(raizlog
								+ "ErrorCorrespondencia obtenerRespuestaDistribuirCarta actualizarEstadoCorrespondencia "
								+ e, e);
						
						log.info(raizlog + "FIN Controller");
						return new RespuestaXml(false, "respuesta fallo", 0L, false);

					}

				}
			}else {
				log.error(raizlog + "No se encontraron pruebas de entrega asociadas al workid [" + workId + "] en la BD"); 
			}
		
		}
		
		log.info(raizlog + "FIN Controller");
		
		return new RespuestaXml(true, "OK", 0L, false);
		
	}
	
	
	
	
	/**
	 * Request para CA - RDI/RDE, actualizacion en repuesta de distribucion
	 * 
	 * @param workId
	 * @param respuesta
	 * @param esCarta
	 * @return
	 */
	@RequestMapping(value = "/respuestaCdd", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody RespuestaXml respuestaCdd(@RequestParam(value = "workId", required = true) String workId,
			@RequestParam(value = "respuesta", required = true) Boolean respuesta,
			@RequestParam(value = "esCarta", required = false) Boolean esCarta) {

		log.info("Entro el controller /respuestaCdd con parametros: workId [" + workId + "] - respuesta [" + respuesta
				+ "] - esCarta [" + esCarta + "]");

		String raizlog = "Workid [" + workId + "] - Controller respuestaCdd - ";

		StateMachine<States, Events> machine = null;
		if (StringUtils.hasText(workId)) {
			try {
				machine = obtenerMaquina(workId);
				log.info(raizlog + "Maquina (" + workId + ") obtenida correctamente de la BD");

				String estadoActual = machine.getState().getId().name().toString();

				log.info(raizlog + "Estado Actual [" + estadoActual + "]");
			} catch (Exception e) {
				String error = "Error obtenido maquina de base de datos";
				log.error(raizlog + error, e);

				log.info(raizlog + "FIN Controller");
				return new RespuestaXml(false, "respuesta fallo", 0L, false);
			}

		}

		Map<String, Object> headers = new HashMap<>();
		headers.put(Consts.RESPUESTA_CDD_DISTRIBUIR_RDI_RDE, respuesta);

		String estadoActual = machine.getState().getId().name().toString();

		if (estadoActual.equals(States.ESPERAR_RESPUESTA_DISTRIBUIR_CDD_RDI_RDE.toString())
				|| (estadoActual.equals(States.ESPERAR_RESPUESTA_DISTRIBUIR_CDD_CARTA.toString()))) {

			if (esCarta == null || !esCarta) {

				machine.sendEvent(MessageBuilder.createMessage(Events.IR_OBTENER_RESPUESTA_DISTRIBUIR_CDD_RDI_RDE,
						new MessageHeaders(headers)));

			} else if (esCarta != null && esCarta) {

				machine.sendEvent(MessageBuilder.createMessage(Events.IR_OBTENER_RESPUESTA_DISTRIBUIR_CDD_CARTA,
						new MessageHeaders(headers)));
			}

			log.info(raizlog + "FIN Controller");
			return new RespuestaXml(true, "OK", 0L, false);

		} else {

			log.error(raizlog + "No se encontraba en el estado esperado:  " + estadoActual + " workId:" + workId
					+ " respuesta:" + respuesta);
			log.info(raizlog + "FIN Controller");
			return new RespuestaXml(false, "false", 0L, false);

		}

	}

	
	
	
	/**
	 * Request para CA, actualizacion en ruta de distribucion
	 * 
	 * @param rolActual
	 * @param resultado
	 * @param workId
	 * @param workidInstancia
	 * @param reintento
	 * @param codigoRuta
	 * @param actualizarDER
	 * @return
	 * @throws InterruptedException
	 */
	@SuppressWarnings("unchecked") // Vamaya: Se añade Suppress Warnings
	@RequestMapping(value = "/procesoRutaDistribucion", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody RespuestaXml procesoRutaDistribucion(
			@RequestParam(value = "rolActual", required = true) String rolActual,
			@RequestParam(value = "resultado", required = true) String resultado,
			@RequestParam(value = "workId", required = true) String workId,
			@RequestParam(value = "workIdInstancia", required = false) long workidInstancia,
			@RequestParam(value = "reintento", required = false, defaultValue = "false") boolean reintento,
			@RequestParam(value = "codigoRuta", required = false) Long codigoRuta,
			@RequestParam(value = "actualizarDER", required = false) Boolean actualizarDER)
			throws InterruptedException {

		// long codigoRuta = codigoRutaOri;
		log.info("Entro el controller /procesoRutaDistribucion con parametros: rolActual [" + rolActual
				+ "] - resultado [" + resultado + "] - workId [" + workId + "] - workIdInstancia [" + workidInstancia
				+ "] - reintento [" + reintento + "] - codigoRuta [" + codigoRuta + "]");

		String raizlog = "Workid [" + workId + "] - Controller procesoRutaDistribucion - ";

		String conexionUsuarioCS = globalProperties.getConexionUsuarioCS();

		StateMachine<States, Events> machine = null;
		if (StringUtils.hasText(workId)) {

			log.info(raizlog + "Se inicia proceso de obtención de la maquina de la BD");
			try {
				machine = obtenerMaquina(workId);
				log.info(raizlog + "Maquina (" + workId + ") obtenida correctamente de la BD");

				String estadoActual = machine.getState().getId().name().toString();

				log.info(raizlog + "Estado Actual [" + estadoActual + "]");
			} catch (Exception e) {
				String error = "Error obtenido maquina de base de datos";
				log.error(raizlog + error, e);

				log.info(raizlog + "FIN Controller");
				return new RespuestaXml(true, "respuesta fallo", 0L, false);
			}

		}

		Map<Long, RespuestaEntrega> distribucionRuta = machine.getExtendedState().get(Consts.DISTRIBUCION_RUTA,
				HashMap.class);

		RespuestaEntrega r = new RespuestaEntrega();
		r.setRolsigueinte(rolActual);
		r.setFinalizacion(false);
		r.setPasoCurrier(false);

		distribucionRuta.put(workidInstancia, r);

		machine.getExtendedState().getVariables().put(Consts.DISTRIBUCION_RUTA, distribucionRuta);

		String estadoActualInicio = machine.getState().getId().name().toString();
		log.info(raizlog + "Estado actual maquina [" + estadoActualInicio + "]");

		String tipologia = machine.getExtendedState().get(Consts.TIPOLOGIA_MEMO_CARTA, String.class);
		long idSolicitante = machine.getExtendedState().get(Consts.ID_SOLICITANTE, Long.class);

		log.info(raizlog + "Tipologia [" + tipologia + "]");

		if (!reintento) {

			Map<String, Object> headers = new HashMap<>();
			headers.put(Consts.RESULTADO_DISTRIBUIR_RDI_RDE, resultado);
			headers.put(Consts.ROL_ACTUAL_DISTRIBUIR_RDI_RDE, rolActual);
			headers.put(Consts.ID_RUTA, workidInstancia);

			if (codigoRuta != null && !codigoRuta.equals((long) 0)) {
				headers.put(Consts.CODIGO_RUTA, codigoRuta);
			} else {

				codigoRuta = new Long(0);
			}

			if (tipologia != null && tipologia.equalsIgnoreCase("CA")) {
				machine.sendEvent(MessageBuilder.createMessage(Events.IR_OBTENER_RESPUESTA_DISTRIBUIR_CARTA,
						new MessageHeaders(headers)));
				log.info(raizlog + "Obtener respuesta distribuir Carta");
			} else {
				machine.sendEvent(MessageBuilder.createMessage(Events.OBTENIENDO_RESULTADO_DISTRIBUIR_RDI_RDE,
						new MessageHeaders(headers)));
				log.info(raizlog + "Obtener respuesta distribuir RDI RDE");
			}

			// Se espera la consulta de la ruta

			log.info(raizlog + "Entro a esperar 10 segundos");
			Thread.sleep(10000);

		}

		log.info(raizlog + "Rol actual [" + rolActual + "] - Resultado [" + resultado + "] -  workidInstancia ["
				+ workidInstancia + "]");

		Map<Long, RespuestaEntrega> distribucionRutaActualizado = machine.getExtendedState()
				.get(Consts.DISTRIBUCION_RUTA, HashMap.class);

		// String rolSiguiente =
		// machine.getExtendedState().get(Consts.ROL_SIGUIENTE_DISTRIBUIR_RDI_RDE,
		// String.class);
		r = new RespuestaEntrega();
		r = distribucionRutaActualizado.get(workidInstancia);

		String rolSiguiente = r.getRolsigueinte();
		Boolean finalizoRadicacion = r.getFinalizacion();
		Boolean pasoPorCurrier = r.getPasoCurrier();

		log.info(raizlog + "rolSiguiente [" + rolSiguiente + "] -  finalizoRadicacion [" + finalizoRadicacion
				+ "] - pasoPorCurrier [" + pasoPorCurrier + "]");

		Member grupo = null;
		Long idGrupo = (long) 0;

		try {
			if (rolSiguiente != null && !rolSiguiente.isEmpty() && !rolSiguiente.equalsIgnoreCase("XXX")) {

				if (tipologia != null && tipologia.equalsIgnoreCase("CA")
						&& rolSiguiente.equalsIgnoreCase(Consts.ID_SOLICITANTE)) {

					idGrupo = idSolicitante;

				} else {

					grupo = UsuarioCS.getMemberByLoginName(autenticacionCS.getUserSoapHeader(conexionUsuarioCS),
							rolSiguiente, wsdlsProps.getUsuario());
					idGrupo = grupo.getID();

				}
			}
		} catch (SOAPException | IOException e) {
			e.printStackTrace();
			log.error(raizlog + "Error metodo getMemberByLoginName - " + e.getMessage(), e);

			log.warn(raizlog + "Respuesta fallida ruta");
			log.info(raizlog + "FIN Controller");
			return new RespuestaXml(false, "-1", codigoRuta, pasoPorCurrier);
		}

		String estadoActualFin = machine.getState().getId().name().toString();

		log.info(raizlog + "FIN Estado actual maquina [" + estadoActualFin + "]");

		log.info(raizlog + "Fin controller procesoRutaDistribucion el rol siguiente es [" + rolSiguiente
				+ "] -  finalizoRadicacion [" + finalizoRadicacion + "]");

		// Esperar que este en el evento de espera de nuevo
		String estadoActual = machine.getState().getId().name().toString();

		log.info(raizlog + "Estado actual: " + estadoActual);

		if (estadoActual.equals("ESPERAR_RESPUESTA_DISTRIBUIR_CARTA")
				|| estadoActual.equals("ESPERAR_RESULTADO_DISTRIBUIR_RDI_RDE") || estadoActual.equals("FIN")
				|| estadoActual.equals("FIN_DER") || estadoActual.equals("ESPERAR_RESULTADO_DISTRIBUIR_DER")) {
			log.info(raizlog + "Respuesta ok ruta");

			log.info(raizlog + "FIN Controller");
			return new RespuestaXml(finalizoRadicacion, idGrupo.toString(), codigoRuta, pasoPorCurrier);
		} else { // Vamaya: Se añade sentencia else para indicar que la máquina no se encontraba
					// en el estado esperado
			log.warn(raizlog + "ERROR:  La máquina no se encontraba en el estado esperado");
			String estadoEncontrado = machine.getState().getId().name().toString();
			log.info(raizlog + "Estado no esperado en el que se encuentra la máquina: " + estadoEncontrado);
		}

		// Si no ingresó a ninguno de los filtros de control se asume error en la ruta y
		// se retorna
		log.warn(raizlog + "Respuesta fallida ruta");
		log.info(raizlog + "FIN Controller");
		return new RespuestaXml(false, "-1", codigoRuta, pasoPorCurrier);

	}

	
	
	
	/**
	 * Request para ME, actualizacion en ruta de distribucion
	 * 
	 * @param workId
	 * @param idDocOriginal
	 * @param resultadoDistribuir
	 * @param mensaje
	 * @return
	 */
	@RequestMapping(value = "/resultadoDistribuir", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody RespuestaXml resultadoDistribuir(
			@RequestParam(value = "workId", required = true) String workId,
			@RequestParam(value = "idDocOriginal", required = false) long idDocOriginal,
			@RequestParam(value = "resultadoDistribuir", required = false) Boolean resultadoDistribuir,
			@RequestParam(value = "mensaje", required = false) String mensaje) {

		log.info("Entro el controller /resultadoDistribuir con parametros: workId [" + workId + "] - idDocOriginal ["
				+ idDocOriginal + "] - resultadoDistribuir [" + resultadoDistribuir + "] - mensaje [" + mensaje + "]");

		String raizlog = "Workid [" + workId + "] - Controller resultadoDistribuir - ";

		log.info("Entro al controller resultado DISTRIBUIR_MEMORANDO");
		StateMachine<States, Events> machine = null;
		if (StringUtils.hasText(workId)) {
			try {
				machine = obtenerMaquina(workId);
				log.info(raizlog + "Maquina (" + workId + ") obtenida correctamente de la BD");

				String estadoActual = machine.getState().getId().name().toString();

				log.info(raizlog + "Estado Actual [" + estadoActual + "]");
			} catch (Exception e) {
				String error = "Error obtenido maquina de base de datos";
				log.error(raizlog + error, e);

				log.info(raizlog + "FIN Controller");
				return new RespuestaXml(false, "respuesta fallo", 0L, false);
			}

		}

		Map<String, Object> headers = new HashMap<>();
		headers.put(Consts.RESULTADO_DISTRIBUIR, resultadoDistribuir);

		machine.sendEvent(MessageBuilder.createMessage(Events.DISTRIBUCION_FINALIZADA, new MessageHeaders(headers)));

		log.info(raizlog + "FIN Controller");

		return new RespuestaXml(true, "true", 0L, false);
	}
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////                FIN REQUEST ----- CARTA/MEMO -----                   ///////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	
	
	
	
	//----------------------------------------------------------------------------------------------------------------------
	
	
	
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////               INICIO REQUEST ----- RDI/RDE -----                    ///////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	
	/**
	 * Request para RDI/RDE, Inicio proceso de radicacion
	 * Asincrono
	 * 
	 * @param idForm
	 * @param conexionUsuario
	 * @param workId
	 * @param sigla
	 * @param tipologia
	 * @param asunto
	 * @param idSolicitante
	 * @param origenPcr
	 * @param origenCdd
	 * @param destino
	 * @param esMensajeria
	 * @param parentIdAdjuntos
	 * @param nombreDependenciaOrigen
	 * @param esSucursal
	 * @param origenEsPcr
	 * @return
	 */
	@RequestMapping(value = "/radicarRdiRdeAsync", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody Respuesta radicarRdiRdeAsync(
			@RequestParam(value = Consts.ID_FORM, required = false) Long idForm,
			@RequestParam(value = Consts.CONEXION_CS_SOLICITANTE, required = false) String conexionUsuario,
			@RequestParam(value = Consts.ID_WORKFLOW_RADICACION, required = false) String workId,
			@RequestParam(value = "sigla", required = false) String sigla,
			@RequestParam(value = "tipologia", required = false) String tipologia,
			@RequestParam(value = "asunto", required = true) String asunto,
			@RequestParam(value = "idSolicitante", required = false) Long idSolicitante,
			@RequestParam(value = "origenPcr", required = false) String origenPcr,
			@RequestParam(value = "origenCdd", required = false) String origenCdd,
			@RequestParam(value = "destino", required = false) String destino,
			@RequestParam(value = "esMensajeria", required = true) Boolean esMensajeria,
			@RequestParam(value = "parentIdAdjuntos", required = false) Long parentIdAdjuntos,
			@RequestParam(value = "nombreDependenciaOrigen", required = false) String nombreDependenciaOrigen,
			@RequestParam(value = "esSucursal", required = false) Boolean esSucursal,
			@RequestParam(value = "origenEsPcr", required = false) Boolean origenEsPcr) {

		log.info("Entro el controller /radicarRdiRdeAsync con parametros: workId [" + workId + "] - "
				+ Consts.CONEXION_CS_SOLICITANTE + " [" + conexionUsuario + "] - sigla [" + sigla + "] - tipologia ["
				+ tipologia + "] - asunto [" + asunto + "] - idSolicitante[" + idSolicitante + "] - origenPcr["
				+ origenPcr + "] - origenCdd [" + origenCdd + "] - destino [" + destino + "] - esMensajeria ["
				+ esMensajeria + "] - parentIdAdjuntos [" + parentIdAdjuntos + "] - nombreDependenciaOrigen ["
				+ nombreDependenciaOrigen + "] - esSucursal [" + esSucursal + "] - origenEsPcr [" + origenEsPcr + "]");

		String raizlog = "Workid [" + workId + "] - Controller radicarRdiRdeAsync - ";

		StateMachine<States, Events> machine = null;
		if (StringUtils.hasText(workId)) {
			machine = getMachine(workId);
		}
		
		String estadoActualInicio = machine.getState().getId().name().toString();
		log.info(raizlog + "INICIO ESTADO ACTUAL MAQUINA: [" + estadoActualInicio +"]");

		Map<String, Object> headers = new HashMap<>();
		headers.put(Consts.ID_FORM, idForm);
		headers.put(Consts.CONEXION_CS_SOLICITANTE, conexionUsuario);
		headers.put(Consts.ID_WORKFLOW_RADICACION, workId);
		headers.put(Consts.SIGLA_RDI_RDE, sigla);
		headers.put(Consts.TIPOLOGIA_RDI_RDE, tipologia);
		headers.put(Consts.ASUNTO_RDI_RDE, asunto);
		headers.put(Consts.ID_SOLICITANTE, idSolicitante);
		headers.put(Consts.ORIGEN_RDI_RDE, origenCdd);
		headers.put(Consts.ORIGEN_RDI_RDE_PCR, origenPcr);
		headers.put(Consts.DESTINO_RDI_RDE, destino);
		headers.put(Consts.ES_MENSAJERIA_RDI_RDE, esMensajeria);
		headers.put(Consts.PARENT_ID_ADJUNTOS_RDI_RDE, parentIdAdjuntos);
		headers.put(Consts.NOMBRE_DEPENDENCIA_ORIGEN, nombreDependenciaOrigen);
		headers.put(Consts.ES_SUCURSAL, esSucursal);
		headers.put(Consts.ORIGEN_ES_PCR, origenEsPcr);

		List<String> origenes = new ArrayList<>();
		origenes.add("DGD");
		List<String> destinos = new ArrayList<>();
		destinos.add("DGD2");

		headers.put(Consts.LISTA_ORIGENES_RDI_RDE, origenes);
		headers.put(Consts.LISTA_DESTINOS_RDI_RDE, destinos);

		machine.sendEvent(MessageBuilder.createMessage(Events.INICIAR_RADICAR_RDI_RDE, new MessageHeaders(headers)));

		String estadoActualFin = machine.getState().getId().name().toString();

		Atributos atributos = new Atributos("", "", "", "", false, false, "", "");
		ErrorCorrespondencia errorCorrespondencia = new ErrorCorrespondencia(false, 0, "");

		log.info(raizlog + "FIN ESTADO ACTUAL MAQUINA:" + estadoActualFin);

		log.info(raizlog + "FIN Controller");
		return new Respuesta(atributos, errorCorrespondencia);
	}
	
	
	
	
	
	/**
	 * Request para RDI/RDE, consulta proceso de radicacion
	 * 
	 * @param workId
	 * @return
	 */
	@RequestMapping(value = "/radicarRdiRdeResultAsync", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody Respuesta radicarRdiRdeResultAsync(
			@RequestParam(value = Consts.ID_WORKFLOW_RADICACION, required = false) String workId) {

		log.info("Entro el controller /radicarRdiRdeResultAsync con parametros: workId [" + workId + "]");

		String raizlog = "Workid [" + workId + "] - Controller radicarRdiRdeResultAsync - ";

		StateMachine<States, Events> machine = null;
		if (StringUtils.hasText(workId)) {
			try {
				machine = obtenerMaquina(workId);
				log.info(raizlog + "Maquina (" + workId + ") obtenida correctamente de la BD");

				String estadoActual = machine.getState().getId().name().toString();

				log.info(raizlog + "Estado Actual [" + estadoActual + "]");
			} catch (Exception e) {
				String error = "Error obtenido maquina de base de datos";
				log.error(raizlog + error, e);

				ErrorCorrespondencia errorCorrespondencia = new ErrorCorrespondencia(false, 0, "");
				errorCorrespondencia = new ErrorCorrespondencia(false, -1, "Error obtenido maquina de base de datos");

				log.info(raizlog + "FIN Controller");
				return new Respuesta(null, errorCorrespondencia);
			}

		}

		Atributos atributos = new Atributos("", "", "", "", false, false, "", "");
		ErrorCorrespondencia errorCorrespondencia = new ErrorCorrespondencia(false, 0, "");

		String estadoActual = machine.getState().getId().name().toString();
		log.info(raizlog + "INICIO ESTADO ACTUAL MAQUINA:" + estadoActual);

		if (estadoActual.equals(States.GENERAR_STICKER_RDI_RDE.toString())) {

			String mensajeError = "";
			boolean isError = false;

			try {

				isError = machine.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class);
				mensajeError = machine.getExtendedState().get(Consts.MENSAJE_ERROR, String.class);

			} catch (NullPointerException e) {
				isError = false;
			}

			if (isError) {
				int codigoError = machine.getExtendedState().get(Consts.CODIGO_ERROR_RADICAR, Integer.class);

				errorCorrespondencia = new ErrorCorrespondencia(false, codigoError, mensajeError);
			} else {
				String numeroRadicado = machine.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO, String.class);
				DateFormat dateFormat = new SimpleDateFormat(globalProperties.getFormatoFecha());
				Date date = new Date();
				String fecha = dateFormat.format(date);
				String asunto = machine.getExtendedState().get(Consts.ASUNTO_RDI_RDE, String.class);

				atributos = new Atributos(numeroRadicado, asunto, fecha, "", false, false, "", "");
				errorCorrespondencia = new ErrorCorrespondencia(true, 0, "");

				log.info(raizlog + "Fin GENERAR_STICKER_RDI_RDE numero radicado enviado " + numeroRadicado);

				log.info(raizlog + "FIN Controller");
				return new Respuesta(atributos, errorCorrespondencia);

			}

		} else {

			if (estadoActual.equals(States.FIN.toString())
					|| estadoActual.equals(States.FINALIZADO_ERRORES.toString())) {

				log.error(raizlog + "Id workflow (" + workId + ") - Fallo radicacion RDI/RDE Async");

				String mensajeError = (machine.getExtendedState().get(Consts.MENSAJE_ERROR, String.class) == null)
						? "Fallo radicacion carta Async"
						: machine.getExtendedState().get(Consts.MENSAJE_ERROR, String.class);
				int codigo = (machine.getExtendedState().get(Consts.CODIGO_ERROR_RADICAR, Integer.class) == null)
						? Consts.MSJ_109
						: machine.getExtendedState().get(Consts.CODIGO_ERROR_RADICAR, Integer.class);

				atributos = new Atributos("", "", "", "", false, false, "", "");
				errorCorrespondencia = new ErrorCorrespondencia(false, codigo, mensajeError);

				log.info(raizlog + "FIN Controller");
				return new Respuesta(atributos, errorCorrespondencia);

			} else {

				log.warn(raizlog + "Id workflow (" + workId
						+ ") - Todavia no se esta en el estado GENERAR_STICKER_RDI_RDE");

				String mensajeError = (machine.getExtendedState().get(Consts.MENSAJE_ERROR, String.class) == null)
						? "Todavia no se esta en el estado GENERAR_STICKER_RDI_RDE"
						: machine.getExtendedState().get(Consts.MENSAJE_ERROR, String.class);
				int codigo = (machine.getExtendedState().get(Consts.CODIGO_ERROR_RADICAR, Integer.class) == null)
						? Consts.MSJ_108
						: machine.getExtendedState().get(Consts.CODIGO_ERROR_RADICAR, Integer.class);

				log.warn(raizlog + "Id workflow (" + workId + ") - codigo:" + codigo + " mensaje: " + mensajeError);

				atributos = new Atributos("", "", "", "", false, false, "", "");
				errorCorrespondencia = new ErrorCorrespondencia(false, codigo, mensajeError);

				log.info(raizlog + "FIN Controller");
				return new Respuesta(atributos, errorCorrespondencia);
			}

		}

		log.info(raizlog + "FIN Controller");
		return new Respuesta(atributos, errorCorrespondencia);

	}
	
	
	
	
	
	/**
	 * Request para RDI/RDE, carga categoria al formulario
	 * 
	 * @param workId
	 * @param idDoc
	 * @return
	 */
	@RequestMapping(value = "/cargarCategoriaRdiRde", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody RespuestaXml cargarCategoriaRdiRde(
			@RequestParam(value = "workId", required = true) String workId,
			@RequestParam(value = "idDoc", required = true) Long idDoc) {

		log.info("Entro el controller /cargarCategoriaRdiRde con parametros: workId [" + workId + "] - " + Consts.ID_DOC_RADICACION
				+ " [" + idDoc + "]");

		String raizlog = "Workid [" + workId + "] - Controller cargarCategoriaRdiRde - ";

		StateMachine<States, Events> machine = null;
		if (StringUtils.hasText(workId)) {
			try {
				machine = obtenerMaquina(workId);
				log.info(raizlog + "Maquina (" + workId + ") obtenida correctamente de la BD");

				String estadoActual = machine.getState().getId().name().toString();

				log.info(raizlog + "Estado Actual [" + estadoActual + "]");
			} catch (Exception e) {
				String error = "Error obtenido maquina de base de datos";
				log.error(raizlog + error, e);

				log.info(raizlog + "FIN Controller");
				return new RespuestaXml(false, "respuesta fallo", 0L, false);
			}

		}

		Map<String, Object> headers = new HashMap<>();
		headers.put(Consts.ID_DOC_RADICACION, idDoc);

		machine.sendEvent(
				MessageBuilder.createMessage(Events.INICIAR_CARGAR_CATEGORIA_RDI_RDE, new MessageHeaders(headers)));

		log.info(raizlog + "FIN Controller");
		return new RespuestaXml(true, "OK", 0L, false);
	}
	
	
	
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////                  FIN REQUEST ----- RDI/RDE -----                    ///////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	
	
	//----------------------------------------------------------------------------------------------------------------------
	
	
	
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////               INICIO REQUEST ----- PLANTA -----                     ///////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	
	/**
	 * Request para Planta, Lllamado al hacer la actualizacion de novedades de
	 * planta
	 * 
	 * @return
	 */
	@RequestMapping(value = "/reporteEmpleados", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody RespuestaXml reporteEmpleados() {

		log.info("Entro el controller /reporteEmpleados");

		String raizlog = "Controller reporteEmpleados - ";

		try {
			Reporte.generarInformeEMpleadosAddin(autenticacionCS.getAdminSoapHeader(),
					globalProperties.getIdReporteAddin(), wsdlsProps.getDocumento(),
					globalProperties.getRutaPlanoAddin());
			log.info(raizlog + "Reporte Genrado correctamante");

			log.info(raizlog + "FIN Controller");
			return new RespuestaXml(true, "OK", 0L, false);
		} catch (Exception e) {
			log.error(raizlog + "Error generando reporte de empleados", e);

			log.info(raizlog + "FIN Controller");
			return new RespuestaXml(false, "false", 0L, false);
		}

	}
	

	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////                  FIN REQUEST ----- PLANTA -----                     ///////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	
	
	
	//----------------------------------------------------------------------------------------------------------------------
	
	
	
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////             INICIO REQUEST ----- UTILITARIOS -----                  ///////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	/**
	 * Request utilitario, Monitoreo por parte del Banco
	 * 
	 * @return
	 */
	@RequestMapping(value = "/monitoreo", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
	public @ResponseBody String monitoreo() {
		
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
	
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////                FIN REQUEST ----- UTILITARIOS -----                  ///////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	
	
	
	//----------------------------------------------------------------------------------------------------------------------
	
	
	
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////             INICIO REQUEST ----- LLAMADO FIRMA -----                ///////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	
	
	/**
	 * Request para CA/ME, llamado desde SSM Firma, proceso de firma finalizado
	 * 
	 * @param workId
	 * @param idDocOriginal
	 * @param resultadoFirma
	 * @param mensaje
	 * @param usuarioCancela
	 * @return
	 */
	@RequestMapping(value = "/resultadoFirma", method = RequestMethod.POST, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody RespuestaXml resultadoFirma(@RequestParam(value = "workId", required = true) String workId,
			@RequestParam(value = "idDocOriginal", required = false) String idDocOriginal,
			@RequestParam(value = "resultadoFirma", required = true) boolean resultadoFirma,
			@RequestParam(value = "mensaje", required = false) String mensaje,
			@RequestParam(value = "usuarioCancela", required = false) String usuarioCancela) {

		log.info("Entro el controller /resultadoFirma con parametros: "
				+ "workId ["+ workId+"] - "
				+ "idDocOriginal [" + idDocOriginal+ "] - "
				+ "resultadoFirma [" + resultadoFirma + "] -  "
				+ "mensaje [" + mensaje + "] - "
				+ "usuarioCancela [" + usuarioCancela + "]");
		
		String raizlog = "Workid [" + workId + "] - Controller resultadoFirma - ";
				


		// SE OBTIENE LA MÁQUINA DE LA BD
		StateMachine<States, Events> machine = null;
		if (StringUtils.hasText(workId)) {
			try {
				machine = obtenerMaquina(workId);
			} catch (Exception e) {
				String error = "Error obtenido maquina de base de datos";
				log.error(raizlog + error, e);

				log.info(raizlog + "FIN ERROR - Controller resultadoFirma");
				return new RespuestaXml(false, "", 0L, false);
			}
		}

		
		// SE REVISA SI LA MAQUINA ESTA EN EL ESTADO ESPERADO
		String estadoActual = machine.getState().getId().name().toString();

		log.info(raizlog + "Estado Actual [" + estadoActual + "]  -  Estado Esperado [" + States.ESPERAR_RESULTADO_FIRMA_CA_ME.toString() + "]");
		
		Map<String, Object> headers = new HashMap<>();
		headers.put(Consts.RESULTADO_FIRMA, resultadoFirma);
		headers.put(Consts.MENSAJE_RESPUESTA_FIRMA, mensaje);
		headers.put(Consts.NOMBRE_USUARIO_CANCELA, usuarioCancela);
		
		//String tipologia = machine.getExtendedState().get(Consts.TIPOLOGIA_MEMO_CARTA, String.class);
		//log.info(raizlog+"La tipología es: ["+tipologia+"]");
		
		machine.sendEvent(MessageBuilder.createMessage(Events.IR_EVALUAR_RESULTADO_FIRMA_CA_ME, new MessageHeaders(headers)));
		
		/*
		if (resultadoFirma) {
			if(tipologia.equalsIgnoreCase("ME"))
				machine.sendEvent(Events.FIRMA_FINALIZADO_OK);
			else
				machine.sendEvent(Events.FIRMA_FINALIZADO_OK_CARTA);
		}else {
			machine.sendEvent(MessageBuilder.createMessage(Events.FIRMA_CANCELADA, new MessageHeaders(headers)));
		}
		*/
		
		/*
		try {			
			resultadoFirmaAsync(workId, idDocOriginal, resultadoFirma, mensaje, usuarioCancela);
		} catch (Exception e) {
			log.error(raizlog + "ERROR: " + e.getMessage(),e);			
		}
		*/
		
		log.info(raizlog + "FIN Controller");
		
		return new RespuestaXml(true, "", 0L, false);
	}	
	
	
	
	
	
	/**
	 * Request para CA/ME, llamado desde SSM Firma, proceso de firma finalizado
	 * 
	 * @param workId
	 * @param idDocOriginal
	 * @param resultadoFirma
	 * @param mensaje
	 * @param usuarioCancela
	 * @return
	 */
	@RequestMapping(value = "/resultadoBlazon", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody RespuestaXml resultadoBlazon(
			@RequestParam(value = "source", required = false) Object source,
			@RequestParam(value = "sourceN", required = false) Object sourceN,
			@RequestParam(value = "source0", required = false) Object source0,
			@RequestParam(value = "target", required = false) Object target,
			@RequestParam(value = "outputformat", required = false) Object outputformat,
			@RequestParam(value = "notificationurl", required = false) Object notificationurl,
			@RequestParam(value = "threadid", required = false) Object threadid,
			@RequestParam(value = "type", required = false) Object type,
			@RequestParam(value = "jobid", required = false) Object jobid,
			@RequestParam(value = "filename", required = false) Object filename,
			@RequestParam(value = "starttime", required = false) Object starttime,
			@RequestParam(value = "tempdir", required = false) Object tempdir,
			@RequestParam(value = "ext", required = false) Object ext,
			@RequestParam(value = "targetfile", required = false) Object targetfile,
			@RequestParam(value = "endtime", required = false) Object endtime,
			@RequestParam(value = "totaltime", required = false) Object totaltime,
			@RequestParam(value = "mainfile", required = false) Object mainfile
			) {

		log.info("Entro el controller /resultadoBlazon con parametros: "+"\n"
				+ "source ["+ source+"] - "+"\n"
				+ "sourceN ["+ sourceN+"] - "+"\n"
				+ "source0 ["+ source0+"] - "+"\n"
				+ "target ["+ target+"] - "+"\n"
				+ "outputformat ["+ outputformat+"] - "+"\n"
				+ "notificationurl ["+ notificationurl+"] - "+"\n"
				+ "threadid ["+ threadid+"] - "+"\n"
				+ "type ["+ type+"] - "+"\n"
				+ "jobid ["+ jobid+"] - "+"\n"
				+ "filename ["+ filename+"] - "+"\n"
				+ "starttime ["+ starttime+"] - "+"\n"
				+ "tempdir ["+ tempdir+"] - "+"\n"
				+ "ext ["+ ext+"] - "
				+ "targetfile ["+ targetfile+"] - "+"\n"
				+ "endtime ["+ endtime+"] - "+"\n"
				+ "totaltime ["+ totaltime+"] - "+"\n"
				+ "mainfile ["+ mainfile+"] - "+"\n"
				);
		
		log.info("FIN Controller");
		
		return new RespuestaXml(true, "", 0L, false);
	}	
	
	
	
	/**
	 * Request para CA/ME, llamado desde SSM Firma, proceso de firma finalizado
	 * 
	 * @param workId
	 * @param idDocOriginal
	 * @param resultadoFirma
	 * @param mensaje
	 * @param usuarioCancela
	 * @return
	 */
	@RequestMapping(value = "/resultadoBlazon2", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody RespuestaXml resultadoBlazon2(
			@RequestParam Map<String, String> allParams
			) {

		log.info("Entro el controller /resultadoBlazon con parametros: "+"\n"
				);
		
		allParams.forEach((k,v)->log.info("LLave ["+k+"] - valor ["+v+"]"));
		
		log.info("FIN Controller");
		
		return new RespuestaXml(true, "", 0L, false);
	}	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////                FIN REQUEST ----- LLAMADO FIRMA -----                ///////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	
	
	
	
	//----------------------------------------------------------------------------------------------------------------------
	
	
	
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////             INICIO METODOS ----- UTILITARIOS -----                  ///////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	/**
	 * Metodo para resultado de firma 
	 * @param workId
	 * @param idDocOriginal
	 * @param resultadoFirma
	 * @param mensaje
	 * @param usuarioCancela
	 * @return
	 */
	/*
	private Future<String> resultadoFirmaAsync(String workId, String idDocOriginal, boolean resultadoFirma, String mensaje, String usuarioCancela) {

		CompletableFuture<String> completableFuture = new CompletableFuture<>();
		Executors.newCachedThreadPool().submit(() -> {

			StateMachine<States, Events> machine = null;
			if (StringUtils.hasText(workId)) {
				machine = obtenerMaquina(workId);

			}
			
			Map<String, Object> headers = new HashMap<>();
			headers.put(Consts.MENSAJE_RESPUESTA_FIRMA, mensaje);
			headers.put(Consts.NOMBRE_USUARIO_CANCELA, usuarioCancela);
			
			String tipologia = machine.getExtendedState().get(Consts.TIPOLOGIA_MEMO_CARTA, String.class);
			
			String estadoActualInicio = machine.getState().getId().name().toString();
			log.info("INICIO ESTADO ACTUAL MAQUINA:" + estadoActualInicio);
			if (resultadoFirma) {
				if(tipologia.equalsIgnoreCase("ME"))
					machine.sendEvent(Events.FIRMA_FINALIZADO_OK);
				else
					machine.sendEvent(Events.FIRMA_FINALIZADO_OK_CARTA);
			}else {
				machine.sendEvent(MessageBuilder.createMessage(Events.FIRMA_CANCELADA, new MessageHeaders(headers)));
			}

			String estadoActualFin = machine.getState().getId().name().toString();

			log.info("FIN ESTADO ACTUAL MAQUINA:" + estadoActualFin);
			return null;
		});

		return completableFuture;

	}
	*/
	
	
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	private synchronized StateMachine<States, Events> getMachine(String id) {
		StateMachine<States, Events> machine = machines.get(id);
		if (machine == null) {
			machine = stateMachineFactory.getStateMachine(id);
			machines.put(id, machine);
		}
		return machine;
	}
	

	
	
	/**
	 * 
	 * @return
	 */
	public Map<String, StateMachine<States, Events>> getMachines() {
		return machines;
	}
	
	
	
	
	/**
	 * Metodo que permite obtener una maquina de la fabrica de maquinas de estado o de la base de datos
	 * 
	 * @param idMaquina identificador de la maquina que se desea buscar
	 * @return machine maquina de estados encontrada
	 * @throws Exception Si no se puede obtener o cargar la maquina de estados
	 */
	private StateMachine<States, Events> obtenerMaquina(String idMaquina) throws Exception {

		StateMachine<States, Events> machine = null;

		// machine=machines.get(idMaquina);

		Optional<MaquinaCorrespondencia> maquinaBdOpt = repository.findByIdMaquina(idMaquina).stream().findFirst();

		if (maquinaBdOpt.isPresent()) {
			MaquinaCorrespondencia maquinaBd = maquinaBdOpt.get();
			String idMaquinaBd = maquinaBd.getIdMaquina();
			machine = stateMachineFactory.getStateMachine(idMaquinaBd);
			StateMachine<States, Events> context = null;

			try {
				context = stateMachinePersister.restore(machine, idMaquinaBd);
			} catch (Exception e) {
				log.error("No se pudo obtener la maquina [" + idMaquina + "] de la Base de datos", e);
				throw new Exception("No se pudo obtener la maquina [" + idMaquina + "] de la Base de datos", e);
			}

			getMachines().put(idMaquinaBd, context);
		} else {
			throw new NoSuchElementException("La maquina con id [" + idMaquina + "] No se encuentra en la Base de datos");
		}

		return machine;
	}
	
	
	
	
	/**
	 * Metodo que permite obtener una maquina de la fabrica de maquinas de estado o de la base de datos
	 * 
	 * @param idMaquina identificador de la maquina que se desea buscar
	 * @return machine maquina de estados encontrada
	 * @throws Exception Si no se puede obtener o cargar la maquina de estados
	 */
	private String obtenerEstadoMaquinaBD(String idMaquina) throws Exception {
		
		String estadoActual = "";		

		Optional<MaquinaCorrespondencia> maquinaBdOpt = repository.findByIdMaquina(idMaquina).stream().findFirst();

		if (maquinaBdOpt.isPresent()) {
			MaquinaCorrespondencia maquinaBd = maquinaBdOpt.get();
			estadoActual = maquinaBd.getEstadoActual();
			
		} else {
			throw new NoSuchElementException("La maquina con id (" + idMaquina + "No se encuentra en la Base de datos");
		}

		return estadoActual;
	}
	
	
	
	
	/**
	 * 
	 * @param idMaquina
	 * @return
	 * @throws Exception
	 */
	private String obtenerNumeroRadicadoBD(String idMaquina) throws Exception {
		
		String numeroRadicado = "";		

		Optional<MaquinaCorrespondencia> maquinaBdOpt = repository.findByIdMaquina(idMaquina).stream().findFirst();

		if (maquinaBdOpt.isPresent()) {
			MaquinaCorrespondencia maquinaBd = maquinaBdOpt.get();
			numeroRadicado = maquinaBd.getNumeroRadicado();
			
		} else {
			throw new NoSuchElementException("La maquina con id (" + idMaquina + "No se encuentra en la Base de datos");
		}

		return numeroRadicado;
	}
	
	
	
	
	/**
	 * 
	 * @param machine
	 * @throws Exception
	 */
	private void persistMaquina(StateMachine<States, Events> machine) throws Exception {		
	
		String machineId = machine.getId();
		
		if(machineId == null){
			throw new NullPointerException("La machineId retornado es nulo");
		}
		
		if(machineId.isEmpty()){
			throw new IllegalArgumentException("La machineId retornado esta vacio");
		}

		stateMachinePersister.persist(machine, machine.getId());
	}
	
	
	
	
	/**
	 * Metodo para poder activar los llamados asincronos a la SSM
	 * @param machine
	 * @param atributos
	 * @param evento
	 */
	@Async
	private void EnviarEventoIniciarAsincrono(StateMachine<States,Events> machine, Map<String, Object> atributos, Events evento) {
		
			machine.sendEvent(MessageBuilder.createMessage(evento, new MessageHeaders(atributos)));
	
		
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////                FIN METODOS ----- UTILITARIOS -----                  ///////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	
	
	
	//----------------------------------------------------------------------------------------------------------------------
	

	

	//TODO PARECE OBSOLETO
	//Controlador para resultado de generar num radicado
	/*
	@RequestMapping(value = "/resultadoGenerarNumRadicado", method = RequestMethod.POST, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody RespuestaXml resultadoGenerarNumRadicado(@RequestParam(value = "workId", required = true) String workId,
			@RequestParam(value = "numeroRadicado", required = true) String numeroRadicado) {
		
		log.info("Entro el controller /resultadoGenerarNumRadicado con parametros: workId ["+ workId+"] - numeroRadicado [" + numeroRadicado
				+ "]");
		
		String raizlog = "Workid [" + workId + "] - Controller resultadoGenerarNumRadicado - ";
							
		StateMachine<States, Events> machine = null;
		if (StringUtils.hasText(workId)) {
			try {
				machine = obtenerMaquina(workId);
				log.info(raizlog + "Maquina (" + workId + ") obtenida correctamente de la BD");
				
				String estadoActual = machine.getState().getId().name().toString();
				
				log.info(raizlog + "Estado Actual [" + estadoActual + "]" );				
			} catch (Exception e) {
				String error = "Error obtenido maquina de base de datos";
				log.error(raizlog + error ,e);
				
				//TODO Revisar error false
				log.info(raizlog + "FIN Controller");
				return new RespuestaXml(false, "respuesta fallo", 0L, false);
			}

		}

		if (numeroRadicado != null && !numeroRadicado.isEmpty()) {

			Map<String, Object> headers = new HashMap<>();
			headers.put(Consts.NUMERO_RADICADO_OBTENIDO, numeroRadicado);

			machine.sendEvent(MessageBuilder.createMessage(Events.NUM_RADICADO_OBTENIDO, new MessageHeaders(headers)));
		}

		log.info(raizlog + "FIN Controller");

		
		
		return new RespuestaXml(true, "", 0L, false);
	}
*/

	
	
	//TODO SE VOLVIÓ OBSOLETO AL UTILIZAR CAMBIO DE RUTAS EN DER
	/*
	@RequestMapping(value = "/reasignarDistribucion", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody RespuestaXml reasignarDistribucion(
			@RequestParam(value = "rolActual", required = false) String rolActual,
			@RequestParam(value = "resultado", required = false) String resultado,
			@RequestParam(value = "workId", required = false) String workId,
			@RequestParam(value = "cddDestino", required = false) String cddDestino,
			@RequestParam(value = "pcrDestino", required = false) String pcrDestino,
			@RequestParam(value = "dependenciaOriginal", required = false) String dependenciaOriginal,
			@RequestParam(value = "dependenciaDestino", required = false) String dependenciaDestino,
			@RequestParam(value = "codigoRuta", required = false) Long codigoRuta) {

		log.info("Entro el controller /reasignarDistribucion con parametros: rolActual [" + rolActual
				+ "] - resultado [" + resultado + "] - workId [" + workId + "] - cddDestino [" + cddDestino
				+ "] - pcrDestino [" + pcrDestino + "] - dependenciaOriginal [" + dependenciaOriginal
				+ "] - dependenciaDestino [" + dependenciaDestino + "] - codigoRuta [" + codigoRuta + "]");

		String raizlog = "Workid [" + workId + "] - Controller reasignarDistribucion - ";

		StateMachine<States, Events> machine = null;
		if (StringUtils.hasText(workId)) {
			try {
				machine = obtenerMaquina(workId);
				log.info(raizlog + "Maquina (" + workId + ") obtenida correctamente de la BD");
				
				String estadoActual = machine.getState().getId().name().toString();
				
				log.info(raizlog + "Estado Actual [" + estadoActual + "]" );				
			} catch (Exception e) {
				String error = "Error obtenido maquina de base de datos";
				log.error(raizlog + error ,e);
				
				log.info(raizlog + "FIN Controller");
				return new RespuestaXml(true, "Error obtenido maquina de base de datos", 0L, false);
			}

		}

		Map<String, Object> headers = new HashMap<>();
		headers.put(Consts.RESULTADO_DISTRIBUIR_RDI_RDE, resultado);
		headers.put(Consts.ROL_ACTUAL_DISTRIBUIR_RDI_RDE, rolActual);
		headers.put(Consts.CODIGO_RUTA, codigoRuta);
		headers.put(Consts.DEPENDENCIA_ORIGINAL, dependenciaOriginal);
		headers.put(Consts.DEPENDENCIA_DESTINO, dependenciaDestino);
		headers.put(Consts.CDD_DESTINO_DER, cddDestino);
		headers.put(Consts.PCR_DESTINO_DER, pcrDestino);

		machine.sendEvent(MessageBuilder.createMessage(Events.IR_REASIGNAR_RUTA_DER, new MessageHeaders(headers)));

		try {
			Thread.sleep(20000);
		} catch (InterruptedException e1) {
			log.error(raizlog + "Error Thread wait - " + e1.getMessage(), e1);
		}

		String rolSiguiente = machine.getExtendedState().get(Consts.ROL_SIGUIENTE_DISTRIBUIR_RDI_RDE, String.class);

		log.info(raizlog + "rolSiguiente: " + rolSiguiente);

		Member grupo = null;
		Long idGrupo = 0L;
		long ruta = codigoRuta;
		try {
			grupo = UsuarioCS.getMemberByLoginName(autenticacionCS.getAdminSoapHeader(), rolSiguiente,
					wsdlsProps.getUsuario());
			idGrupo = grupo.getID();
		} catch (SOAPException | IOException e) {
			log.error(raizlog + "Error metodo getMemberByLoginName - " + e.getMessage(), e);
		}

		log.info(raizlog + "FIN Controller");

		return new RespuestaXml(false, idGrupo.toString(), ruta, false);
	}
	*/
	
	
	
	
	//TODO PARECE OBSOLETO	
	/*
	@RequestMapping(value = "/moverDocumento", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody RespuestaXml moverDocumento(@RequestParam(value = "idDoc", required = false) Long idDoc,
			@RequestParam(value = "parentId", required = false) Long parentId) {

		log.info("Entro el controller /moverDocumento con parametros: idDoc [" + idDoc + "] - parentId [" + parentId
				+ "]");

		String raizlog = "IdDoc [" + idDoc + "] - Controller moverDocumento - ";

		boolean result = false;
		String mensaje = "OK";

		try {

			ContenidoDocumento.MoverDocumento(autenticacionCS.getAdminSoapHeader(), idDoc, parentId,
					wsdlsProps.getDocumento());
			result = true;
		} catch (SOAPException | IOException | InterruptedException e) {
			mensaje = e.getMessage();
			log.error(raizlog + "Error metodo MoverDocumento - " + e.getMessage(), e);
		} 

		log.info(raizlog + "FIN Controller");

		return new RespuestaXml(result, mensaje, 0L, false);
	}
	*/
	
	
	//TODO PARECE OBSOLETO
	/*
	@RequestMapping(value = "/insertRutas", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody void insertRutas() {
		
		CentroDistribucion centroDistribucion = new CentroDistribucion();
		centroDistribucion.setNombre("CDD");
		
		
		DependenciaDistribucion dependenciaOrigen = new DependenciaDistribucion();
		dependenciaOrigen.setId(2);
		DependenciaDistribucion dependenciaDestino = new DependenciaDistribucion();
		dependenciaDestino.setId(1);
		
		RutaDistribucion ruta = new RutaDistribucion();
		
		PasoDistribucion paso = new PasoDistribucion();
		paso.setOrden(1);
		paso.setCentroDistribucion(centroDistribucion);
		paso.setRuta(ruta);	
	}
	*/
	
	
	

}
