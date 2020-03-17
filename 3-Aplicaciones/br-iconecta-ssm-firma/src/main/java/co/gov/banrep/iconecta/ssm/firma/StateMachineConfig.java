package co.gov.banrep.iconecta.ssm.firma;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.MalformedParametersException;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.soap.SOAPException;
import javax.xml.ws.WebServiceException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.config.configurers.StateConfigurer.History;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;

import co.gov.banrep.iconecta.cs.autenticacion.AutenticacionCS;
import co.gov.banrep.iconecta.cs.documento.ContenidoDocumento;
import co.gov.banrep.iconecta.cs.workflow.TipoAtributo;
import co.gov.banrep.iconecta.cs.workflow.ValorAtributo;
import co.gov.banrep.iconecta.cs.workflow.Workflow;
import co.gov.banrep.iconecta.pf.circuito.CircuitoPortafirmas;
import co.gov.banrep.iconecta.pf.circuito.DocumentoPortafirmas;
import co.gov.banrep.iconecta.pf.circuito.entity.Circuito;
import co.gov.banrep.iconecta.pf.circuito.entity.Documento;
import co.gov.banrep.iconecta.pf.circuito.entity.Firmante;
import co.gov.banrep.iconecta.pf.circuito.entity.RespuestaObtenerDocumeto;
import co.gov.banrep.iconecta.ssm.firma.entity.DocumentoSSM;
import co.gov.banrep.iconecta.ssm.firma.entity.FirmanteSSM;
import co.gov.banrep.iconecta.ssm.firma.enums.Events;
import co.gov.banrep.iconecta.ssm.firma.enums.States;
import co.gov.banrep.iconecta.ssm.firma.params.CSCatDocumentoGralProps;
import co.gov.banrep.iconecta.ssm.firma.params.CSCatFirmaProps;
import co.gov.banrep.iconecta.ssm.firma.params.CSWorkflowProps;
import co.gov.banrep.iconecta.ssm.firma.params.GlobalConsts;
import co.gov.banrep.iconecta.ssm.firma.params.GlobalProps;
import co.gov.banrep.iconecta.ssm.firma.params.PortafirmasProps;
import co.gov.banrep.iconecta.ssm.firma.params.WsdlProps;
import co.gov.banrep.iconecta.ssm.firma.persist.ContextoPersist;
import co.gov.banrep.iconecta.ssm.firma.persist.OracleStateMachinePersist;
import co.gov.banrep.iconecta.ssm.firma.plugin.AccionExterna;
import co.gov.banrep.iconecta.ssm.firma.plugin.AccionExternaFactory;
import co.gov.banrep.iconecta.ssm.firma.utils.SSMUtils;

/**
 * Clase principal de la Maquina de Estados que contiene la configuracion de los
 * estados, eventos y acciones
 * 
 *  @author <a href="mailto:jjrojassa@banrep.gov.co">John Jairo Rojas S.</a>
 *
 */
@Configuration
@EnableAsync
//@PropertySource("file:${ICONECTAFILES_PATH}/firma.properties")
@PropertySource("file:/apps/iconectaFiles_16/firma.properties")
public class StateMachineConfig {
	
	@Bean
	public StateMachineLogListener stateMachineLogListener(){
		return new StateMachineLogListener();
	}
	
	@Configuration
	@EnableStateMachineFactory
	public static class Config extends EnumStateMachineConfigurerAdapter<States, Events>{
		
		
		private static final String ELEMENTOS_POR_PROCESAR = "Elementos  por Procesar: ";
		private static final String ELEMENTOS_PROCESADOS = "Elementos Procesados: ";	
		private static final String ERROR_FATAL = "FATAL ";
		private static final int ID_ACCION_FIRMA = 5;
		
		//private String validacionError = "ERROR NO ESPECIFICADO";
		//private String validacionEstado = "ESTADO NO ESPECIFICADO";
		//private boolean validacionEstadoError = false;
		
		//Valor por defecto si no se coloca ningun valor en el archivo properties
		private long tiempoReintento = 300000; //5 minutos de espera
		
		private String tipoAccionExterna = "ninguna";
		
		private final Logger log = LoggerFactory.getLogger(this.getClass());
		
		@Autowired
		private GlobalProps globalProperties;
		
		@Autowired
		private AutenticacionCS autenticacionCS;
		
		@Autowired
		private PortafirmasProps portafirmasProps;
		
		@Autowired
		private CSCatDocumentoGralProps documentoGeneralProps;
		
		@Autowired
		private CSCatFirmaProps firmaProps;
		
		@Autowired
		private CSWorkflowProps workflowProps;
		
		@Autowired
		private WsdlProps WSDLs;
				
		@Autowired
		private StateMachineLogListener stateMachineLogListener;
		
		@Bean
		public ContextoPersist contextoPersist(){
			return new ContextoPersist();
		}
		
		@Autowired
		private ContextoPersist contextoPersist;
		
		@Autowired
		private StateMachinePersister<States, Events, String> stateMachinePersister;
		
		@Autowired
		private ContextRefreshedListener firmantesCompleto;
		
		
		@Bean(name = StateMachineSystemConstants.TASK_EXECUTOR_BEAN_NAME)
		public TaskExecutor taskExecutor(){
			ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
			taskExecutor.setCorePoolSize(10);
			taskExecutor.setMaxPoolSize(30);
			taskExecutor.setQueueCapacity(500);
			//taskExecutor.setAllowCoreThreadTimeOut(true);			
			taskExecutor.setAwaitTerminationSeconds(60);
			//taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
			
			return taskExecutor;
		}
		
				
		@Override
		public void configure(StateMachineConfigurationConfigurer<States, Events> config) throws Exception{
			config
				.withConfiguration()
					.autoStartup(true)
					.taskExecutor(taskExecutor())
					.listener(stateMachineLogListener);
		}
		
		@Override
		public void configure(StateMachineStateConfigurer<States, Events> states) throws Exception{
			
			states
				.withStates()
					.initial(States.FASE_INICIAL)									
					.state(States.FASE_FINAL)
					.state(States.ESPERARNDO_REINENTO, entryVertificarNumeroReintento(),persist())
					.state(States.INICIAR_WF_NOTIFICACION)
					.choice(States.INICIAR_TAREA_CANCELACION)
					.state(States.FINALIZADO_CANCELADO_ALTERNATIVO, entryAccionExternaCancelacion(), null)
					.state(States.FINALIZADO_CANCELADO,entryIniciarWFNotificacionUsuario())
					.choice(States.FINALIZADO_ERRORES)
					.state(States.FINALIZADO_ERRORES_NORMAL,notificarError(),entryIniciarWFNotificacionUsuario())
					.state(States.FINALIZADO_ERRORES_ALTERNATIVO,notificarError(), iniciarWfNotificacionAlt())
					.state(States.FINALIZADO_CORRECTO,persistWithoutContext())
					.end(States.FINALIZADO)
					.and()
						.withStates()
							.parent(States.FASE_INICIAL)
							.initial(States.LISTO)
							.state(States.INICIALIZAR_PARAMETROS,entryInicializarParametros(), null)
							.state(States.OBTENER_DOCUMENTO_CS, entryObtenerDocumentoCS(), null)
							.state(States.INICIAR_CIRCUITO_PF, entryIniciarCircuitoPF(),null)
							.state(States.ESPERANDO_RESPUESTA_PF,persistCircuit(),persistWithoutContext())
							.state(States.FIN_FASE_INICIAL)
							.and()
						.withStates()
							.parent(States.FASE_FINAL)
							.initial(States.LISTO_FASE_FINAL,iniciarlizarListasPF())
							.state(States.OBTENER_DOCUMENTO_PF, entryObtenerDocumentoPF(), persist())
							.state(States.ACTUALIZAR_DOC_CS, entryActualizarDocCS(), persist())
							.state(States.OBTENER_COPIA_COMPULSADA_PF, entryObtenerCopiaCompulsadaPF(), persist())
							.state(States.CARGAR_COPIA_COMPULSADA_CS, entryCargarCopiaCompulsadaCS(), persist())
							.choice(States.INICIAR_TAREA_FINAL)
							.state(States.INICIAR_WF_NOTIFICACION_FINAL, entryIniciarWFNotificacionUsuario(),persistAndDeleteContext())
							.state(States.ACCION_FINAL_ALTERNATIVA, entryAccionExterna(), persistAndDeleteContext())
							.history(States.HISTORY, History.DEEP);	
		}
		
				

	


		@Override
		public void configure(StateMachineTransitionConfigurer<States, Events> transitions) throws Exception{
			transitions
			.withExternal()
				.source(States.LISTO).target(States.INICIALIZAR_PARAMETROS)
				.event(Events.INICIAR)					
				.and()
			.withExternal()
				.source(States.INICIALIZAR_PARAMETROS).target(States.OBTENER_DOCUMENTO_CS)					
				.action(documentosPorObtenerCS())
				.and()
			.withExternal()
				.source(States.OBTENER_DOCUMENTO_CS).target(States.INICIAR_CIRCUITO_PF)
				.event(Events.DOCUMENTO_OBTENIDO_CS)
				.action(iniciarlizarListas())
				.and()
			.withExternal()
				.source(States.INICIAR_CIRCUITO_PF).target(States.ESPERANDO_RESPUESTA_PF)
				//.event(Events.CIRCUITO_INICIADO)
				.action(enviarNotificacionInicial())
				.and()
			.withExternal()
				.source(States.FASE_INICIAL).target(States.INICIAR_WF_NOTIFICACION)
				.event(Events.ERROR_OPERACION)
				.and()
			.withExternal()
				.source(States.ESPERANDO_RESPUESTA_PF).target(States.FIN_FASE_INICIAL)
				//.event(Events.RESPUESTA_PF_EXITOSA)				
				.and()	
			.withExternal()
				.source(States.FASE_INICIAL).target(States.FASE_FINAL)
				.event(Events.RESPUESTA_PF_EXITOSA)				
				.and()				
			.withExternal()
				.source(States.LISTO_FASE_FINAL).target(States.OBTENER_DOCUMENTO_PF)					
				.and()
			/*
			.withExternal()
				.source(States.OBTENER_DOCUMENTO_PF).target(States.ACTUALIZAR_DOC_CS)
				.event(Events.DOCUMENTO_FIRMADO)
				.action(iniciarlizarListasFaseFinal())
				.and()
			.withExternal()
				.source(States.ACTUALIZAR_DOC_CS).target(States.INICIAR_TAREA_FINAL)
				.event(Events.DOCUMENTO_ACTUALIZADO)
				.and()
			.withExternal()
				.source(States.ACTUALIZAR_DOC_CS).target(States.OBTENER_COPIA_COMPULSADA_PF)
				.event(Events.DOCUMENTO_ACTUALIZADO_COPIA_COMPULSADA)
				.action(iniciarlizarListasFaseFinal())
				.and()
			*/
			.withExternal()
				.source(States.OBTENER_DOCUMENTO_PF).target(States.INICIAR_TAREA_FINAL)
				.event(Events.DOCUMENTO_ACTUALIZADO)
				.and()
			.withExternal()
				.source(States.OBTENER_DOCUMENTO_PF).target(States.OBTENER_COPIA_COMPULSADA_PF)
				.event(Events.DOCUMENTO_ACTUALIZADO_COPIA_COMPULSADA)
				.action(iniciarlizarListasFaseFinal())
				.and()
			
			
				
			/*	
			.withExternal()
				.source(States.OBTENER_COPIA_COMPULSADA_PF).target(States.CARGAR_COPIA_COMPULSADA_CS)
				.event(Events.COPIA_COMPULSADA_OBTENIDA)
				.action(iniciarlizarListasFaseFinal())
				.and()
			.withExternal()
				.source(States.CARGAR_COPIA_COMPULSADA_CS).target(States.INICIAR_TAREA_FINAL)
				.event(Events.COPIA_COMPULSADA_ACTUALIZADA)
				.and()
			*/
			.withExternal()
				.source(States.OBTENER_COPIA_COMPULSADA_PF).target(States.INICIAR_TAREA_FINAL)
				.event(Events.COPIA_COMPULSADA_OBTENIDA)
				.action(iniciarlizarListasFaseFinal())
				.and()
				
			.withExternal()
				.source(States.FASE_FINAL).target(States.FINALIZADO_CORRECTO)
				.event(Events.WORKFLOW_INICIADO)				
				.and()
			.withExternal()
				.source(States.FASE_FINAL).target(States.FINALIZADO_CORRECTO)
				.event(Events.ACCION_EXTERNA_FINALIZADA)
				.and()
			.withExternal()
				.source(States.FASE_FINAL).target(States.ESPERARNDO_REINENTO)
				.event(Events.ERROR_OPERACION_FINAL)
				.and()
			.withExternal()
				.source(States.FASE_FINAL).target(States.FINALIZADO_ERRORES)
				.event(Events.ERROR_OPERACION_FINAL_SIN_REINTENTO)
				.and()
			.withExternal()
				.source(States.ESPERARNDO_REINENTO).target(States.HISTORY)
				.timer(tiempoReintento)				
				.and()
			.withExternal()
				.source(States.INICIAR_WF_NOTIFICACION).target(States.FINALIZADO_ERRORES)
				.and()
			.withExternal()
				.source(States.FASE_INICIAL).target(States.INICIAR_TAREA_CANCELACION)
				.event(Events.RESPUESTA_PF_CANCELADO)
				.and()
			.withExternal()
				.source(States.ESPERARNDO_REINENTO).target(States.FINALIZADO_ERRORES)
				.event(Events.NUMERO_MAXIMO_REINTENTOS_ALCANZADO)
				.and()
			.withExternal()
				.source(States.FINALIZADO_CANCELADO).target(States.FINALIZADO)
				.event(Events.FINALIZAR_CANCELACION)
				.action(persistWithoutContext())
				.and()
			.withExternal()
				.source(States.FINALIZADO_CANCELADO_ALTERNATIVO).target(States.FINALIZADO_CORRECTO)
				.event(Events.FINALIZAR_CANCELACION_ALTERNATIVA)				
				.and()
//			.withExternal()
//				.source(States.FINALIZADO_ERRORES).target(States.FINALIZADO)
//				.action(persistWithoutContext())
//				.and()
				
			.withExternal()
				.source(States.FINALIZADO_ERRORES_NORMAL).target(States.FINALIZADO)
				.action(persistWithoutContext())
				.and()
			.withExternal()
				.source(States.FINALIZADO_ERRORES_ALTERNATIVO).target(States.FINALIZADO)
				.action(persistWithoutContext())
				.and()
			.withExternal()
				.source(States.FINALIZADO_CORRECTO).target(States.FINALIZADO)				
				.and()
			.withChoice()
				.source(States.INICIAR_TAREA_FINAL)
				.first(States.INICIAR_WF_NOTIFICACION_FINAL, evaluarTipoAccion1())
				.then(States.ACCION_FINAL_ALTERNATIVA, evaluarTipoAccion2())
				.and()
			.withChoice()
				.source(States.INICIAR_TAREA_CANCELACION)
				.first(States.FINALIZADO_CANCELADO, evaluarTipoAccion1())
				.then(States.FINALIZADO_CANCELADO_ALTERNATIVO, evaluarTipoAccion2())
				.and()
			.withChoice()
				.source(States.FINALIZADO_ERRORES)
				.first(States.FINALIZADO_ERRORES_NORMAL, evaluarTipoAccion1())
				.then(States.FINALIZADO_ERRORES_ALTERNATIVO, evaluarTipoAccion2());
			
		}
		

		@Bean
		public StateMachinePersist<States, Events, String> stateMachinePersist() {
			return new OracleStateMachinePersist();
		}

		@Bean
		public StateMachinePersister<States, Events, String> H2StateMachinePersister(
				StateMachinePersist<States, Events, String> stateMachinePersist) {
			return new DefaultStateMachinePersister<States, Events, String>(stateMachinePersist);
		}
		
		@Bean
		public Action<States, Events> entryInicializarParametros() {
			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " entryInicializarParametros");

				// Se cargan los valores que llegan del controller dentro de
				// variables extended de la SSM
				String idWorkflow = context.getMessageHeaders().get(GlobalConsts.ID_WORKFLOW, String.class);
				String asunto = context.getMessageHeaders().get(GlobalConsts.ASUNTO, String.class);
				boolean copiaCompulsada = context.getMessageHeaders().get(GlobalConsts.COPIA_COMPULSADA, Boolean.class);
				String idUsuarioCS = context.getMessageHeaders().get(GlobalConsts.ID_USUARIO_CS, String.class);
				String conexionUsuarioCS = context.getMessageHeaders().get(GlobalConsts.CONEXION_USUARIO_CS,
						String.class);
				String nombreUsuarioCS = context.getMessageHeaders().get(GlobalConsts.NOMBRE_USUARIO_CS, String.class);
				String strListaFirmantes = context.getMessageHeaders().get(GlobalConsts.IDS_FIRMANTES, String.class);
				String idFirmante = context.getMessageHeaders().get(GlobalConsts.ID_FIRMANTE, String.class);
				String accionExterna = context.getMessageHeaders().get(GlobalConsts.ACCION_EXTERNA, String.class);
				String urlExterna = context.getMessageHeaders().get(GlobalConsts.URL_EXTERNA, String.class);
				Boolean actualizarCatDocumento = context.getMessageHeaders().get(GlobalConsts.ACTUALIZAR_CAT_DOCUMENTO, Boolean.class);
				String comentarios = context.getMessageHeaders().get(GlobalConsts.COMENTARIOS, String.class);
				Boolean notificacionSolicitante = context.getMessageHeaders().get(GlobalConsts.NOTIFICAR_SOLICITANTE, Boolean.class);

				long workIdlong = Long.parseLong(idWorkflow);
				
		
				try {
					
				
					log.info(raizLog + "Asunto: " + asunto);
					log.info(raizLog + "Tiempo para reintento: " + tiempoReintento);
					log.debug(raizLog + "Copia compulsada: " + copiaCompulsada);
					log.debug(raizLog + "Id workflow: " + idWorkflow);
					log.debug(raizLog + "Conexion usuario CS: " + conexionUsuarioCS);
					log.debug(raizLog + "Nombre usuario CS: " + nombreUsuarioCS);
					log.debug(raizLog + "Id usuario CS: " + idUsuarioCS);
					log.debug(raizLog + "Id Firmantes: " + strListaFirmantes);
					
					int reintento = 1;
					
					Pattern patron = Pattern.compile("^\\/.\\$~");
					Matcher encaja = patron.matcher(asunto);
					asunto = encaja.replaceAll(" ");
					
					
					context.getExtendedState().getVariables().put(GlobalConsts.ID_WORKFLOW, idWorkflow);
					context.getExtendedState().getVariables().put(GlobalConsts.REINTENTO, reintento);
					context.getExtendedState().getVariables().put(GlobalConsts.ASUNTO, asunto);
					context.getExtendedState().getVariables().put(GlobalConsts.COPIA_COMPULSADA, copiaCompulsada);
					context.getExtendedState().getVariables().put(GlobalConsts.CONEXION_USUARIO_CS, conexionUsuarioCS);
					context.getExtendedState().getVariables().put(GlobalConsts.ID_USUARIO_CS, idUsuarioCS);
					context.getExtendedState().getVariables().put(GlobalConsts.NOMBRE_USUARIO_CS, nombreUsuarioCS);					
					context.getExtendedState().getVariables().put(GlobalConsts.ERROR_MAQUINA, false);
					context.getExtendedState().getVariables().put(GlobalConsts.MENSAJE_ERROR, "ERROR NO ESPECIFICADO");
					context.getExtendedState().getVariables().put(GlobalConsts.ACTUALIZAR_CAT_DOCUMENTO, actualizarCatDocumento);
					context.getExtendedState().getVariables().put(GlobalConsts.NOTIFICAR_SOLICITANTE, notificacionSolicitante);
					
					if (org.springframework.util.StringUtils.hasText(comentarios)) {
						context.getExtendedState().getVariables().put(GlobalConsts.COMENTARIOS, comentarios);
					}else{
						context.getExtendedState().getVariables().put(GlobalConsts.COMENTARIOS, "Sin comentarios");
					}
					
				
					
					List<FirmanteSSM> firmantesProceso = new ArrayList<>();

					if (org.springframework.util.StringUtils.hasText(idFirmante)) {

						FirmanteSSM firmante = SSMUtils.obternerFirmanteByCedula(idFirmante,
								firmantesCompleto.getFirmantes());
						
						if (firmante != null) {
							firmantesProceso.add(firmante);
						}

					} else if (org.springframework.util.StringUtils.hasText(strListaFirmantes)) {
						firmantesProceso = SSMUtils.obternerFirmantes(strListaFirmantes,
								firmantesCompleto.getFirmantes());
					}
					
					if(firmantesProceso.isEmpty()){
						log.error("No se encontro ningun firmante valido");
						throw new NullPointerException("No se encontro ningun firmante valido");
					}
					

					context.getExtendedState().getVariables().put(GlobalConsts.FIRMANTES, firmantesProceso);
					
					
					if (org.springframework.util.StringUtils.hasText(accionExterna)) {
						context.getExtendedState().getVariables().put(GlobalConsts.ACCION_EXTERNA, accionExterna);
					}

					if (org.springframework.util.StringUtils.hasText(urlExterna)) {
						context.getExtendedState().getVariables().put(GlobalConsts.URL_EXTERNA, urlExterna);
					}

				} catch (Exception e) {
					log.error(raizLog + "No se pudo obtener los firmantes del Workflow (" + workIdlong + ")", e);
					//context.getExtendedState().getVariables().put(GlobalConsts.MENSAJE_ERROR, e.getMessage());
					//String estado = context.getStateMachine().getState().getId().name();
					//context.getExtendedState().getVariables().put(GlobalConsts.ESTADO_ERROR, estado);
					cargarError(context, e);
					context.getStateMachine().sendEvent(Events.ERROR_OPERACION);
				}
			};
		}
		
		@Bean
		public Action<States, Events> documentosPorObtenerCS() {
			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " documentosPorObtenerCS");
				log.info(raizLog + " Un Documento: " + context.getMessageHeaders().get(GlobalConsts.ID_DOC, String.class));
				if (context.getMessageHeaders().containsKey(GlobalConsts.ID_DOCS)) {
					log.info(raizLog + " Documentos adcionales"
							+ context.getMessageHeaders().get(GlobalConsts.ID_DOCS, String.class));
				}
				
				long idDocumentoPrincipal = 0;
				
				Map<Long, DocumentoSSM> documentos = new HashMap<Long, DocumentoSSM>();
				List<Long> listaIn = new ArrayList<Long>(documentos.keySet());
				List<Long> listaOut = new ArrayList<Long>();				
				try {

					if (context.getMessageHeaders().containsKey(GlobalConsts.ID_DOC)) {
						String idDocumentoStr = context.getMessageHeaders().get(GlobalConsts.ID_DOC, String.class);
						idDocumentoPrincipal = Long.parseLong(idDocumentoStr);
						log.info(raizLog + "Un Documento: " + idDocumentoStr);
						
						DocumentoSSM documento = new DocumentoSSM(idDocumentoPrincipal);

						documentos.put(idDocumentoPrincipal, documento);
						listaIn.add(idDocumentoPrincipal);
					} 
					
					
					if (context.getMessageHeaders().containsKey(GlobalConsts.ID_DOCS)) {
						String idDocsStr = context.getMessageHeaders().get(GlobalConsts.ID_DOCS, String.class);
						List<Long> idsDocumentos = new ArrayList<Long>();
						log.info(raizLog + "Documentos adicionales: " + idDocsStr);

						SSMUtils.cargarValoresCadenaCS(idDocsStr, idsDocumentos);

						Iterator<Long> iter = idsDocumentos.iterator();
						while (iter.hasNext()) {
							Long unId = iter.next();

							DocumentoSSM unDocumento = new DocumentoSSM(unId);

							documentos.put(unId, unDocumento);
							listaIn.add(unId);
						}
					}
					
					context.getExtendedState().getVariables().put(GlobalConsts.ID_DOC_PRINCIPAL, idDocumentoPrincipal);
					context.getExtendedState().getVariables().put(GlobalConsts.LISTA_IN, listaIn);
					context.getExtendedState().getVariables().put(GlobalConsts.LISTA_OUT, listaOut);
					context.getExtendedState().getVariables().put(GlobalConsts.DOCUMENTOS_SSM, documentos);
				} catch (Exception e) {
					log.error("No se pudieron cargar todos los ids de los documentos : "
							+ SSMUtils.toStringFromList(listaIn), e);
					cargarError(context, e);
					context.getStateMachine().sendEvent(Events.ERROR_OPERACION);
				}
							
				
				log.info(raizLog + "SALE documentosPorObtenerCS");
			};
		}

		
		@Bean
		public Action<States, Events> entryObtenerDocumentoCS() {
			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " entryObtenerDocumentoCS");
				
				String conexionUsuarioCS =  context.getExtendedState().get(GlobalConsts.CONEXION_USUARIO_CS, String.class);
								
				@SuppressWarnings("unchecked")
				Map<Long, DocumentoSSM> documentos = context.getExtendedState().get(GlobalConsts.DOCUMENTOS_SSM, HashMap.class);			
				@SuppressWarnings("unchecked")
				ArrayList<Long> listaIn = context.getExtendedState().get(GlobalConsts.LISTA_IN, ArrayList.class);								
				@SuppressWarnings("unchecked")
				ArrayList<Long> listaOut = context.getExtendedState().get(GlobalConsts.LISTA_OUT, ArrayList.class);
				
				String idWorkflow = context.getMessageHeaders().get(GlobalConsts.ID_WORKFLOW, String.class);
				
				long workIdlong = Long.parseLong(idWorkflow);
												
				//DataHandler contenido = null;
				long unId = -1;
																
				log.info(raizLog + ELEMENTOS_POR_PROCESAR + SSMUtils.toStringFromList(listaIn));					
								
				int i = 1;				
				try {									
					Iterator<Long> iter = listaIn.iterator();
					while(iter.hasNext()){
						unId = iter.next();
						
						DocumentoSSM unDocumento = documentos.get(unId);
						
						log.info(raizLog + "Obteniendo documento (" + unId +") de Content Server" );
						
						//contenido = ContenidoDocumento.obtenerContenidoDocumento(
						//		autenticacionCS.getUserSoapHeader(conexionUsuarioCS), unDocumento.getIdCs(), globalProperties.getRutaTemporal(), WSDLs.getDocumento());
						
						Path documentoPath = null;
						//String documentoPath =null;
						
						
						try {
							documentoPath = ContenidoDocumento.obtenerContenidoDocumento(
											autenticacionCS.getUserSoapHeader(conexionUsuarioCS), unDocumento.getIdCs(), globalProperties.getRutaTemporal(),workIdlong , WSDLs.getDocumento());
						} catch (Exception e) {
							log.error(raizLog + "Error en el metodo obtenerContenidoDocumento", e);
							cargarError(context, e);
							context.getStateMachine().sendEvent(Events.ERROR_OPERACION);
						}
						
						//String nombreOriginal = contenido.getName();
						String nombreOriginal = ContenidoDocumento.obtenerNombreDocumento(autenticacionCS.getUserSoapHeader(conexionUsuarioCS), unDocumento.getIdCs(), WSDLs.getDocumento());
						String nombreAdjuntoModificado = StringUtils.substringBefore(nombreOriginal, ".")
								+ GlobalConsts.NOMBRE_MODIFICADO_DOC_PREFIJO + i
								+ GlobalConsts.NOMBRE_MODIFICADO_DOC_SUFIJO;						
						i++;
						
						unDocumento.setNombre(nombreOriginal);
						unDocumento.setNombreModificado(nombreAdjuntoModificado);						
						//unDocumento.setContenidoOriginal(contenido);
						unDocumento.setContenidoOriginalPath(documentoPath);
						
						if(log.isDebugEnabled()){					
							log.debug(raizLog + "Documento (" + unId + ") - Nombre Documento Original: " + nombreOriginal);
							log.debug(raizLog + "Documento (" + unId + ") - Nombre Documento Modificado: " + nombreAdjuntoModificado);
							log.debug(raizLog + "Documento (" + unId + ") - Data handler documento: " + unDocumento.toString());
						}
						log.info(raizLog + "Documento (" + unId +") Obtenido de Content Server" );
						
						listaOut.add(unId);						
						iter.remove();						
						
						context.getStateMachine().sendEvent(Events.DOCUMENTO_OBTENIDO_CS);
					}
				} catch (SOAPException | InterruptedException e) {
					log.error(raizLog + "Error al obtener el documento con id: " + unId , e);
					cargarError(context, e);
					context.getStateMachine().sendEvent(Events.ERROR_OPERACION);
				} catch (IOException e) {
					log.error(raizLog + "Error al obtener el documento con id: " + unId, e);
					cargarError(context, e);
					context.getStateMachine().sendEvent(Events.ERROR_OPERACION);
				}
				
				log.info(raizLog + ELEMENTOS_PROCESADOS + SSMUtils.toStringFromList(listaOut));				
			};
		}
		
		@Bean
		public Action<States, Events> iniciarlizarListas(){
			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " Inicializando listaIn y ListaOut");
						
				
				@SuppressWarnings("unchecked")
				Map<Long, DocumentoSSM> documentos = context.getExtendedState().get(GlobalConsts.DOCUMENTOS_SSM, HashMap.class);			
				@SuppressWarnings("unchecked")
				ArrayList<Long> listaIn = context.getExtendedState().get(GlobalConsts.LISTA_IN, ArrayList.class);								
				@SuppressWarnings("unchecked")
				ArrayList<Long> listaOut = context.getExtendedState().get(GlobalConsts.LISTA_OUT, ArrayList.class);
						
				listaIn.clear();
				
				Iterator<Entry<Long, DocumentoSSM>> iter = documentos.entrySet().iterator();
				while(iter.hasNext()){
					listaIn.add(iter.next().getKey());
				}
				
				listaOut.clear();				
								
			};
		}
		
		@Bean
		public Action<States, Events> iniciarlizarListasFaseFinal(){
			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " Inicializando listaIn y ListaOut");
				
				Map<Object, Object> contextVars = context.getExtendedState().getVariables();
				
				log.info(raizLog + "Restaurando contexto previo de la BD");				
				log.info("Obteniendo variables de la BD");				
				
				if (contextVars.isEmpty()) {
					try {
						restaurarvariablesBD(contextVars, context.getStateMachine().getId());
					} catch (Exception e) {
						log.warn(raizLog + "Error obteniendo variables de la BD + " + e.getMessage());
					}
					log.info("FIN Obteniendo variables de la BD");
				}
								
				@SuppressWarnings("unchecked")
				Map<Long, DocumentoSSM> documentos = context.getExtendedState().get(GlobalConsts.DOCUMENTOS_SSM, HashMap.class);			
				@SuppressWarnings("unchecked")
				ArrayList<Long> listaIn = context.getExtendedState().get(GlobalConsts.LISTA_IN, ArrayList.class);								
				@SuppressWarnings("unchecked")
				ArrayList<Long> listaOut = context.getExtendedState().get(GlobalConsts.LISTA_OUT, ArrayList.class);
						
				listaIn.clear();
				
				Iterator<Entry<Long, DocumentoSSM>> iter = documentos.entrySet().iterator();
				while(iter.hasNext()){
					listaIn.add(iter.next().getKey());
				}
				
				listaOut.clear();
				
				try {
					log.debug("GUARDANDO VARIABLES EN LA BD");
					persistContexto(context);
				} catch (Exception e) {
					log.error(
							raizLog + "ERROR GUARDANDO LAS VARIABLES [" + context.getStateMachine().getId() + "] en la base de datos",
							e);
				}
											
			};
		}
		
		@Bean
		public Action<States, Events> entryIniciarCircuitoPF(){
			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " entryIniciarCircuitoPF");
				
				@SuppressWarnings("unchecked")
				Map<Long, DocumentoSSM> documentos = context.getExtendedState().get(GlobalConsts.DOCUMENTOS_SSM, HashMap.class);			
				@SuppressWarnings("unchecked")
				ArrayList<Long> listaIn = context.getExtendedState().get(GlobalConsts.LISTA_IN, ArrayList.class);								
				@SuppressWarnings("unchecked")
				ArrayList<Long> listaOut = context.getExtendedState().get(GlobalConsts.LISTA_OUT, ArrayList.class);
				
				String asunto = context.getExtendedState().get(GlobalConsts.ASUNTO, String.class);
				//@SuppressWarnings("unchecked")
				//ArrayList<String> nifsFirmantes = context.getExtendedState().get(GlobalConsts.NIFS_FIRMANTES, ArrayList.class);
				@SuppressWarnings("unchecked")
				List<FirmanteSSM> firmantesProceso = context.getExtendedState().get(GlobalConsts.FIRMANTES, List.class);
				
				try {
					log.info(raizLog + ELEMENTOS_POR_PROCESAR + SSMUtils.toStringFromList(listaIn));
				} catch (IndexOutOfBoundsException e) {
					listaIn.clear();
					Iterator<Entry<Long, DocumentoSSM>> iter = documentos.entrySet().iterator();
					while (iter.hasNext()) {
						listaIn.add(iter.next().getKey());
					}
					listaOut.clear();
					log.info(raizLog + ELEMENTOS_POR_PROCESAR + SSMUtils.toStringFromList(listaIn));
				}
				
				List<Documento> documentosPF = new ArrayList<Documento>();
				
				Iterator<Long> iterLista = listaIn.iterator();
				while(iterLista.hasNext()){
					long unId = iterLista.next();
					DocumentoSSM unDocumentoSSM = documentos.get(unId);
					
					Path unDocumentoOriginalPath = unDocumentoSSM.getContenidoOriginalPath();
					File unDocContent = unDocumentoOriginalPath.toFile();
					FileDataSource unFds = new FileDataSource(unDocContent);
					DataHandler unDthlr = new DataHandler(unFds);
					
					//Documento unDocumentoPF = new Documento(unDocumentoSSM.getNombreModificado(), unDocumentoSSM.getContenidoOriginal());
					Documento unDocumentoPF = new Documento(unDocumentoSSM.getNombreModificado(), unDthlr);
					documentosPF.add(unDocumentoPF);
										
					
					listaOut.add(unId);					
				}
				
				boolean esPrimero = true;
				
				List<Firmante> firmantes = new ArrayList<Firmante>();
				
				//Iterator<String> iterFirmantes = nifsFirmantes.iterator();
				Iterator<FirmanteSSM> iterFirmantes = firmantesProceso.iterator();
				while(iterFirmantes.hasNext()){
					FirmanteSSM unFirmanteSSM = iterFirmantes.next();
					long unNifLong = unFirmanteSSM.getCedula();
					String unNif = String.valueOf(unNifLong);

					// El primer correo de notifcacion debe ser enviado por el
					// content y no por portafirmas
					if (esPrimero) {
						Firmante firmante = new Firmante(unNif).withDaysLimit(portafirmasProps.getDiaslimite());
						firmantes.add(firmante);
						
						//Se cargan los datos del primer Firmante para el controller
						context.getExtendedState().getVariables().put(GlobalConsts.FIRMANTE1_NOMBRE, unFirmanteSSM.getNombre());
						context.getExtendedState().getVariables().put(GlobalConsts.FIRMANTE1_CORREO, unFirmanteSSM.getCorreo());
						
						esPrimero = false;
					} else {
						Firmante notificacion = new Firmante(unNif).withIdAction(ID_ACCION_FIRMA);						
						Firmante firmante = new Firmante(unNif).withDaysLimit(portafirmasProps.getDiaslimite());
						firmantes.add(notificacion);
						firmantes.add(firmante);
					}
				}
				
				Circuito circuito = new Circuito("1111", asunto, documentosPF, firmantes);			
				
				int idCircuito = 0;

				try {
					idCircuito = CircuitoPortafirmas.iniciarCircuito(portafirmasProps.getTicketadmin(),
							portafirmasProps.getNifadmin(), portafirmasProps.getUrl(), circuito, WSDLs.getCircuito());
					
					log.info(raizLog + "Id Circuito: " + idCircuito);
					log.debug(raizLog + "Circuito (" +idCircuito + ") - Respuesta Servicio Portafirmas: " + CircuitoPortafirmas.getRespuestaServicio());
					log.debug(raizLog + "Circuito (" +idCircuito + ") - Codigo respuesta Servicio Portafirmas: " + CircuitoPortafirmas.getCodigoRespuestaServicio());

					if (idCircuito != -1) {
						log.info(raizLog + "Circuito iniciado correctamente");
						log.info(raizLog + ELEMENTOS_PROCESADOS + SSMUtils.toStringFromList(listaOut));
						
						context.getExtendedState().getVariables().put(GlobalConsts.ID_CIRCUITO,	idCircuito);
						//context.getStateMachine().sendEvent(Events.CIRCUITO_INICIADO);						
					} else {
						throw new Exception("Error iniciando circuito devuelto por Portafirmas");
					}

				} catch (Exception e) {
					log.error(raizLog + "Error al iniciar el Circuito", e);					
					cargarError(context, e);
					context.getStateMachine().sendEvent(Events.ERROR_OPERACION);
				}			
			};
		}
		
		
		@Bean
		public Action<States, Events> enviarNotificacionInicial(){
			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " notificacionInicial");
				
				String asunto = context.getExtendedState().get(GlobalConsts.ASUNTO, String.class);
				String idUsuarioCS = context.getExtendedState().get(GlobalConsts.ID_USUARIO_CS, String.class);
				
				String firmante1Nombre = context.getExtendedState().get(GlobalConsts.FIRMANTE1_NOMBRE, String.class);
				String firmante1Correo = context.getExtendedState().get(GlobalConsts.FIRMANTE1_CORREO, String.class);
				String comentarios = context.getExtendedState().get(GlobalConsts.COMENTARIOS, String.class);
				Boolean notificarSolicitante = context.getExtendedState().get(GlobalConsts.NOTIFICAR_SOLICITANTE, Boolean.class);
				
				Map<String, ValorAtributo> atributos = new HashMap<String, ValorAtributo>();
				
				if(log.isTraceEnabled() || log.isDebugEnabled()){
					log.debug(raizLog + "Parametro workflow");
					log.debug(raizLog + "respuestaFirma: " + true);
					log.debug(raizLog + "asunto: " + asunto);
					log.debug(raizLog + "usuarioSolicitante: " + idUsuarioCS);
					log.debug(raizLog + "correoFirmante: " + firmante1Correo);
					log.debug(raizLog + "nombreFirmante: " + firmante1Nombre);
					log.debug(raizLog + "comentarios: " + comentarios);
					log.debug(raizLog + "notificarSolicitante: " + notificarSolicitante);
				}
				
				if(comentarios.isEmpty()){
					comentarios = "Sin comentarios";
				}
				
				atributos.put("respuestaFirma", new ValorAtributo(true, TipoAtributo.STRING));
				atributos.put("asunto", new ValorAtributo(asunto, TipoAtributo.MULTILINE));
				atributos.put("usuarioSolicitante", new ValorAtributo(Long.parseLong(idUsuarioCS), TipoAtributo.USER));
				atributos.put("​correoFirmante", new ValorAtributo(firmante1Correo, TipoAtributo.STRING));
				atributos.put("​nombreFirmante", new ValorAtributo(firmante1Nombre, TipoAtributo.STRING));
				atributos.put("​comentarios", new ValorAtributo(comentarios, TipoAtributo.MULTILINE));
				atributos.put("​notificarSolicitante", new ValorAtributo(notificarSolicitante, TipoAtributo.STRING));
				
				long longIdMap = workflowProps.getNotificacionInicial().getMapId();

				long idWorkflow;
				try {
					idWorkflow = Workflow.iniciarWorkflowConAtributos(
							autenticacionCS.getAdminSoapHeader(), longIdMap,"FT114 - Notificación inicial firma  - " + context.getStateMachine().getId()  , atributos, WSDLs.getWorkflow());
										
				
					log.info(raizLog + "Se inicia al instancia de workflow: " + idWorkflow);
				} catch (SOAPException | WebServiceException e) {
					log.error(raizLog + "Error al iniciar el Workflow con MapID : " + longIdMap, e);
					cargarError(context, e);
					context.getStateMachine().sendEvent(Events.ERROR_OPERACION_FINAL);
				} catch (NumberFormatException | NoSuchElementException | IOException e) {					
					log.error(raizLog + ERROR_FATAL + "Error al iniciar el Workflow con MapID : " + longIdMap, e);
					cargarError(context, e);
					context.getStateMachine().sendEvent(Events.ERROR_OPERACION_FINAL_SIN_REINTENTO);
				}
				
				
				
			};
		}
	

		@Bean
		public Action<States, Events> iniciarlizarListasPF(){
			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " Inicializando listaIn y ListaOut - PF");
				
				Map<Object, Object> contextVars = context.getExtendedState().getVariables();				
			
				log.info(raizLog + "Restaurando contexto previo de la BD");				
				log.info("Obteniendo variables de la BD");				
				
				if (contextVars.isEmpty()) {
					try {
						restaurarvariablesBD(contextVars, context.getStateMachine().getId());
					} catch (Exception e) {
						log.warn(raizLog + "Error obteniendo variables de la BD + " + e.getMessage());
					}
					log.info("FIN Obteniendo variables de la BD");
				}
								
				@SuppressWarnings("unchecked")
				Map<Long, DocumentoSSM> documentos = context.getExtendedState().get(GlobalConsts.DOCUMENTOS_SSM, HashMap.class);			
				@SuppressWarnings("unchecked")
				ArrayList<Long> listaIn = context.getExtendedState().get(GlobalConsts.LISTA_IN, ArrayList.class);								
				@SuppressWarnings("unchecked")
				ArrayList<Long> listaOut = context.getExtendedState().get(GlobalConsts.LISTA_OUT, ArrayList.class);				
				
				String datosPF = context.getMessageHeaders().get(GlobalConsts.DATOS_PF, String.class);
				
				if(listaIn == null){
					log.error("ListaIn es nula");
					throw new NullPointerException("ListaIn es nula");
				}
				
				
				log.debug("DATOS PF: " + datosPF);
									
				listaIn.clear();
				try {
					SSMUtils.cargarIdsDocumentosPF(documentos,datosPF);
				} catch (NoSuchElementException | MalformedParametersException e) {
					context.getExtendedState().getVariables().put(GlobalConsts.ERROR_DATOS_PF, true);
					log.error(raizLog + ERROR_FATAL + "Error cargado los datos del GUID de portafirmas",e);
				}
						
				Iterator<Entry<Long, DocumentoSSM>> iter = documentos.entrySet().iterator();				
				while(iter.hasNext()){
					Entry<Long, DocumentoSSM> unEntry = iter.next();
					listaIn.add(unEntry.getKey());					
				}
				
				listaOut.clear();
				
				try {
					log.debug("GUARDANDO VARIABLES EN LA BD");
					persistContexto(context);
				} catch (Exception e) {
					log.error(
							raizLog + "ERROR GUARDANDO LAS VARIABLES [" + context.getStateMachine().getId() + "] en la base de datos",
							e);
				}							
							
			};
		}
		
		
		@Bean
		public Action<States, Events> entryObtenerDocumentoPF() {
			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " entryObtenerDocumentoPF");

				//Se obtienen las variables de la base de datos y se cargan a las variables en memoria
				Map<Object, Object> contextVars = context.getExtendedState().getVariables();
				log.info(raizLog + "Restaurando contexto previo de la BD");
				log.info("Obteniendo variables de la BD");
				
				if (contextVars.isEmpty()) {
					try {
						restaurarvariablesBD(contextVars, context.getStateMachine().getId());
					} catch (Exception e) {
						log.warn(raizLog + "Error obteniendo variables de la BD + " + e.getMessage());
					}
					log.info("FIN Obteniendo variables de la BD");
				}
				
				boolean copiaCompusada = context.getExtendedState().get(GlobalConsts.COPIA_COMPULSADA, Boolean.class);
				
				String conexionUsuarioCS = context.getExtendedState().get(GlobalConsts.CONEXION_USUARIO_CS, String.class);				
				boolean actulizarCategoriaDoc = context.getExtendedState().get(GlobalConsts.ACTUALIZAR_CAT_DOCUMENTO, Boolean.class);
				
				log.info(raizLog + "requiere copia compulsada: " + copiaCompusada);
				
				boolean errorDatosPF = context.getExtendedState().getVariables()
						.containsKey(GlobalConsts.ERROR_DATOS_PF);

				if (errorDatosPF) {
					context.getStateMachine().sendEvent(Events.ERROR_OPERACION_FINAL_SIN_REINTENTO);
				} else {
					@SuppressWarnings("unchecked")
					Map<Long, DocumentoSSM> documentos = context.getExtendedState().get(GlobalConsts.DOCUMENTOS_SSM,
							HashMap.class);
					@SuppressWarnings("unchecked")
					ArrayList<Long> listaIn = context.getExtendedState().get(GlobalConsts.LISTA_IN, ArrayList.class);
					@SuppressWarnings("unchecked")
					ArrayList<Long> listaOut = context.getExtendedState().get(GlobalConsts.LISTA_OUT, ArrayList.class);

					long unId = -1;
					String unIdDocumentoPF = "";

					try {
						log.info(raizLog + ELEMENTOS_POR_PROCESAR + SSMUtils.toStringFromList(listaIn));
					} catch (IndexOutOfBoundsException e) {
						listaIn.clear();
						Iterator<Entry<Long, DocumentoSSM>> iter = documentos.entrySet().iterator();
						while (iter.hasNext()) {
							listaIn.add(iter.next().getKey());
						}
						listaOut.clear();
						log.info(raizLog + ELEMENTOS_POR_PROCESAR + SSMUtils.toStringFromList(listaIn));
					}

					try {
						Iterator<Long> iter = listaIn.iterator();
						while (iter.hasNext()) {
							unId = iter.next();
							DocumentoSSM unDocumento = documentos.get(unId);
							unIdDocumentoPF = unDocumento.getIdPf();

							log.info(raizLog + "Obteniendo documento (" + unIdDocumentoPF + ") de portafirmas");

							RespuestaObtenerDocumeto respuesta = DocumentoPortafirmas
									.obtenerDocumentoFirmando(portafirmasProps.getTicketadmin(), unIdDocumentoPF, WSDLs.getCircuito());

							//TODO bloque cambio data handler
							DataHandler unDocContenido = respuesta.getContenido();
							//InputStream is = unDocContenido.getInputStream();
							//OutputStream os = new FileOutputStream(unDocumento.getContenidoOriginalPath().toFile());
							
							//IOUtils.copy(is, os);
							
							//IOUtils.closeQuietly(is);
							//IOUtils.closeQuietly(os);
							
							
							//unDocumento.setContenidoFirmando(respuesta.getContenido());
							SSMUtils.cargarMetadatosCatDocumento(unDocumento, respuesta.getMetadatos(), documentoGeneralProps);
							
							if (log.isTraceEnabled()) {
								log.trace(raizLog + "Metadatos portafirmas");
								HashMap<String, String> metadatosResp = respuesta.getMetadatos();
								Iterator<Entry<String, String>> iterMetadatos = metadatosResp.entrySet().iterator();
								while (iterMetadatos.hasNext()) {
									Entry<String, String> unMetadato = iterMetadatos.next();
									log.trace(raizLog + "Un metadato llave [" + unMetadato.getKey()+ "] - valor [" + unMetadato.getValue() + "]");									
								}
							}
							
														
							SSMUtils.cargarMetadatosCatFirma(unDocumento, respuesta.getMetadatos(), firmaProps,
									portafirmasProps);

							//Map<AtributosCategorias, Metadato> metadatos = unDocumento.getCatFirma().getMetadatosSubGrupo();
							
							//List<Map<AtributosCategorias, Metadato>> metadatosLista = unDocumento.getCatFirma().getMetadatosSubGrupo();
							//Map<AtributosCategorias, Metadato> metadatos = metadatosLista.get(0);
							/*
							if (log.isDebugEnabled()) {
								log.debug(raizLog + "Documento (" + unId + ") - Id documento Portafirmas: "
										+ unIdDocumentoPF);
								log.debug(raizLog + "Documento (" + unId + ") - Nombre Firmante: "
										+ metadatos.get(AtributosCategorias.NOMBRE_FIRMANTE).getValor());
								log.debug(raizLog + "Documento (" + unId + ") - Fecha Firma: "
										+ metadatos.get(AtributosCategorias.FECHA_FIRMA).getValor());
								log.debug(raizLog + "Documento (" + unId + ") - Entidad Certificadora: "
										+ metadatos.get(AtributosCategorias.ENTIDAD).getValor());
								log.debug(raizLog + "Documento (" + unId + ") - Numero de Serie: "
										+ metadatos.get(AtributosCategorias.SERIE).getValor());
								log.debug(raizLog + "Documento (" + unId + ") - Valor Hash: "
										+ metadatos.get(AtributosCategorias.HASH).getValor());
								log.debug(raizLog + "Documento (" + unId + ") - Algoritmo: "
										+ metadatos.get(AtributosCategorias.ALGORITMO).getValor());
							}*/
							log.info(raizLog + "Documento firmado (" + unId
									+ ") de Porfafirmas obtenido de portafirmas");
							
							//Si los documentos no requieren copia compulsada se solicita ser borrados de Portafirmas
							//depues de oobtenidos							
							if (!copiaCompusada) {
								try {
									DocumentoPortafirmas.borrarDocumento(portafirmasProps.getTicketadmin(),
											unIdDocumentoPF, WSDLs.getCircuito());
									log.info(raizLog + "Documento (" + unId + ") - Id documento Portafirmas ("
											+ unIdDocumentoPF + ") fue borrado de portafirmas"); 
								} catch (Exception e) {
									log.warn(raizLog + "Documento (" + unId + ") - Id documento Portafirmas ("
											+ unIdDocumentoPF + ") NO fue borrado de portafirmas"); 
								}
							}
							
							
							//Se inicia proceso de cargue de las copias compulsadas
							
							log.info(raizLog + "Subiendo version y metadatos documento (" + unId + ") de Content Server");

							int idCatDocumentoGeneral = Integer.parseInt(documentoGeneralProps.getId());
							int idCatFirma = Integer.parseInt(firmaProps.getId());
							
							String nombreCatDocumentoGeneral  = documentoGeneralProps.getNombreCategoria();
							String nombreCatFirma  = firmaProps.getNombreCategoria();						
							Map<String, Object> metadatosDocumento = unDocumento.getCatDocumentoGeneral().getMetadatosCS();						
							String nombreSubGrupo = firmaProps.getGrupoAtributos();
							//Map<String, Object> metadatosSubGrupo = unDocumento.getCatFirma().getMetadatosSubGrupoCS();
							List<Map<String, Object>> metadatosSubGrupo = unDocumento.getCatFirma().getMetadatosSubGrupoCS();
							
							//TODO prueba
							/*for (Map<String, Object> unaFila : metadatosSubGrupo) {
								System.out.println("Nueva fila");
								unaFila.forEach((k, v) -> System.out
										.println("Metadatos Subgrupo llave [" + k + "] - valor [" + v + "]"));
							}*/
							
							if(log.isDebugEnabled()){
								log.debug(raizLog + "Documento ("+unId +") - Nombre Categoria: " + nombreCatDocumentoGeneral);
								log.debug(raizLog + "Documento ("+unId +") - Metadatos: " + SSMUtils.toStringFromMap(metadatosDocumento));
								log.debug(raizLog + "Documento ("+unId +") - Nombre Categoria: " + nombreCatFirma);
								//log.debug(raizLog + "Documento ("+unId +") - Metadatos: " + SSMUtils.toStringFromMap(metadatosSubGrupo));							
							}
							
							log.debug(raizLog + "Documento (" + unId
									+ ") - Actualizando Documento y Categoria Documento General");
							
							//TODO Prueba nombre
							String nombre = unDocumento.getNombre();
							nombre = StringUtils.substringBeforeLast(nombre, ".");
							nombre = nombre + ".pdf";
							unDocumento.setNombre(nombre);
							
							if (actulizarCategoriaDoc) {
								log.debug(raizLog + "Documento (" + unId + ") - Actualizando Categoria Documento");
								/*
								ContenidoDocumento.actualizarVersionMetadatosDocumento(
										autenticacionCS.getUserSoapHeader(conexionUsuarioCS), unId, nombre,
										unDocumento.getContenidoFirmando(), idCatDocumentoGeneral,
										nombreCatDocumentoGeneral, metadatos, true, WSDLs.getDocumento());
								*/
								ContenidoDocumento.actualizarVersionMetadatosDocumento(
										autenticacionCS.getUserSoapHeader(conexionUsuarioCS), unId, nombre,
										unDocContenido, idCatDocumentoGeneral,
										nombreCatDocumentoGeneral, metadatosDocumento, true, WSDLs.getDocumento());
								
							} else {
								log.debug(raizLog + "No se actualiza categoria documento - parametro actulizarCategoriaDoc:"
										+ actulizarCategoriaDoc);
								ContenidoDocumento.actualizarVersionDoc(autenticacionCS.getUserSoapHeader(conexionUsuarioCS), unId, nombre, unDocContenido, false, WSDLs.getDocumento());
							}

							log.debug(raizLog + "Documento (" + unId + ") - Actualizando Categoria firma");
							ContenidoDocumento.actualizarMetadatosSubgrupo(
									autenticacionCS.getUserSoapHeader(conexionUsuarioCS), unId, idCatFirma, nombreCatFirma,
									nombreSubGrupo, metadatosSubGrupo, false, WSDLs.getDocumento());						
							
							
							log.info(raizLog + "Actualizados version y metadatos documento (" + unId +") de Content Server" );
							
							

							listaOut.add(unId);
							iter.remove();
							
							context.getExtendedState().getVariables().put(GlobalConsts.LISTA_IN, listaIn);
							context.getExtendedState().getVariables().put(GlobalConsts.LISTA_OUT, listaOut);
							
							//context.getStateMachine().sendEvent(Events.DOCUMENTO_FIRMADO);
							
							log.info(raizLog + ELEMENTOS_PROCESADOS + SSMUtils.toStringFromList(listaOut));
						}
						
						//context.getStateMachine().sendEvent(Events.DOCUMENTO_FIRMADO);
						
						if (copiaCompusada) {
							context.getStateMachine().sendEvent(Events.DOCUMENTO_ACTUALIZADO_COPIA_COMPULSADA);
						} else {
							context.getStateMachine().sendEvent(Events.DOCUMENTO_ACTUALIZADO);
						}
						
					} catch (Exception e) {
						log.error(raizLog + "Error al obtener el documento (" + unId + ") de portafirmas con id: "
								+ unIdDocumentoPF, e);
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.ERROR_OPERACION_FINAL);
					}

					try {
						log.debug("GUARDANDO VARIABLES EN LA BD");
						persistContexto(context);
					} catch (Exception e) {
						log.error(
								raizLog + "ERROR GUARDANDO LAS VARIABLES [" + context.getStateMachine().getId() + "] en la base de datos",
								e);
					}				
				}

			};
		}
		
		@Bean
		public Action<States, Events> entryActualizarDocCS(){
			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " entryActualizarDocCS");
				
				//Se obtienen las variables de la base de datos y se cargan a las variables en memoria
				Map<Object, Object> contextVars = context.getExtendedState().getVariables();
				log.info(raizLog + "Restaurando contexto previo de la BD");
				log.info("Obteniendo variables de la BD");
				
				if (contextVars.isEmpty()) {
					try {
						restaurarvariablesBD(contextVars, context.getStateMachine().getId());
					} catch (Exception e) {
						log.warn(raizLog + "Error obteniendo variables de la BD " + e.getMessage());
					}
					log.info("FIN Obteniendo variables de la BD");
				}
				
				@SuppressWarnings("unchecked")
				Map<Long, DocumentoSSM> documentos = context.getExtendedState().get(GlobalConsts.DOCUMENTOS_SSM, HashMap.class);			
				@SuppressWarnings("unchecked")
				ArrayList<Long> listaIn = context.getExtendedState().get(GlobalConsts.LISTA_IN, ArrayList.class);								
				@SuppressWarnings("unchecked")
				ArrayList<Long> listaOut = context.getExtendedState().get(GlobalConsts.LISTA_OUT, ArrayList.class);
				
				String conexionUsuarioCS = context.getExtendedState().get(GlobalConsts.CONEXION_USUARIO_CS, String.class);
				boolean copiaCompusada = context.getExtendedState().get(GlobalConsts.COPIA_COMPULSADA, Boolean.class);
				boolean actulizarCategoriaDoc = context.getExtendedState().get(GlobalConsts.ACTUALIZAR_CAT_DOCUMENTO, Boolean.class);
				
				long unId = -1;
				
				try {
					log.info(raizLog + ELEMENTOS_POR_PROCESAR + SSMUtils.toStringFromList(listaIn));
				} catch (IndexOutOfBoundsException e) {							
					listaIn.clear();					
					Iterator<Entry<Long, DocumentoSSM>> iterDocumentos = documentos.entrySet().iterator();
					while(iterDocumentos.hasNext()){
						listaIn.add(iterDocumentos.next().getKey());
					}					
					listaOut.clear();
					log.info(raizLog + ELEMENTOS_POR_PROCESAR + SSMUtils.toStringFromList(listaIn));
				}
				
				try {
					Iterator<Long> iterDocumentos = listaIn.iterator();
					while (iterDocumentos.hasNext()) {
						unId = iterDocumentos.next();

						DocumentoSSM unDocumento = documentos.get(unId);

						log.info(raizLog + "Subiendo version y metadatos documento (" + unId + ") de Content Server");

						int idCatDocumentoGeneral = Integer.parseInt(documentoGeneralProps.getId());
						int idCatFirma = Integer.parseInt(firmaProps.getId());
						
						String nombreCatDocumentoGeneral  = documentoGeneralProps.getNombreCategoria();
						String nombreCatFirma  = firmaProps.getNombreCategoria();						
						Map<String, Object> metadatos = unDocumento.getCatDocumentoGeneral().getMetadatosCS();						
						String nombreSubGrupo = firmaProps.getGrupoAtributos();
						//Map<String, Object> metadatosSubGrupo = unDocumento.getCatFirma().getMetadatosSubGrupoCS();
						List<Map<String, Object>> metadatosSubGrupo = unDocumento.getCatFirma().getMetadatosSubGrupoCS();
						
						if(log.isDebugEnabled()){
							log.debug(raizLog + "Documento ("+unId +") - Nombre Categoria: " + nombreCatDocumentoGeneral);
							log.debug(raizLog + "Documento ("+unId +") - Metadatos: " + SSMUtils.toStringFromMap(metadatos));
							log.debug(raizLog + "Documento ("+unId +") - Nombre Categoria: " + nombreCatFirma);
							//log.debug(raizLog + "Documento ("+unId +") - Metadatos: " + SSMUtils.toStringFromMap(metadatosSubGrupo));							
						}
						
						log.debug(raizLog + "Documento (" + unId
								+ ") - Actualizando Documento y Categoria Documento General");
						
						//TODO Prueba nombre
						String nombre = unDocumento.getNombre();
						nombre = StringUtils.substringBeforeLast(nombre, ".");
						nombre = nombre + ".pdf";
						unDocumento.setNombre(nombre);
						
						
						FileDataSource fds = new FileDataSource(unDocumento.getContenidoOriginalPath().toFile());
						DataHandler dthdlr = new DataHandler(fds);
											
						
						if (actulizarCategoriaDoc) {
							log.debug(raizLog + "Documento (" + unId + ") - Actualizando Categoria Documento");
							/*
							ContenidoDocumento.actualizarVersionMetadatosDocumento(
									autenticacionCS.getUserSoapHeader(conexionUsuarioCS), unId, nombre,
									unDocumento.getContenidoFirmando(), idCatDocumentoGeneral,
									nombreCatDocumentoGeneral, metadatos, true, WSDLs.getDocumento());
							*/
							ContenidoDocumento.actualizarVersionMetadatosDocumento(
									autenticacionCS.getUserSoapHeader(conexionUsuarioCS), unId, nombre,
									dthdlr, idCatDocumentoGeneral,
									nombreCatDocumentoGeneral, metadatos, true, WSDLs.getDocumento());
							
						} else {
							log.debug(raizLog + "No se actualiza categoria documento - parametro actulizarCategoriaDoc:"
									+ actulizarCategoriaDoc);
							ContenidoDocumento.actualizarVersionDoc(autenticacionCS.getUserSoapHeader(conexionUsuarioCS), unId, nombre, dthdlr, false, WSDLs.getDocumento());
						}

						log.debug(raizLog + "Documento (" + unId + ") - Actualizando Categoria firma");
						ContenidoDocumento.actualizarMetadatosSubgrupo(
								autenticacionCS.getUserSoapHeader(conexionUsuarioCS), unId, idCatFirma, nombreCatFirma,
								nombreSubGrupo, metadatosSubGrupo, false, WSDLs.getDocumento());						
						
						
						log.info(raizLog + "Actualizados version y metadatos documento (" + unId +") de Content Server" );
						
						//Bloque para eliminar el documento temporal
						log.info(raizLog + "Eliminando documento (" + unId +") del fichero temporal" );
						//FileDataSource temporal = (FileDataSource) unDocumento.getContenidoOriginal().getDataSource();						
						//File temporalFile = temporal.getFile();
						File temporalFile = unDocumento.getContenidoOriginalPath().toFile();
						
						if (temporalFile.delete()) {							
							log.info(raizLog + "El fichero (" + unId +") ha sido borrado satisfactoriamente" );
						} else {							
							log.info(raizLog + "El fichero (" + unId +") no puede ser borrado" );
						}
											
						listaOut.add(unId);						
						iterDocumentos.remove();
						
						if (copiaCompusada) {
							context.getStateMachine().sendEvent(Events.DOCUMENTO_ACTUALIZADO_COPIA_COMPULSADA);
						} else {
							context.getStateMachine().sendEvent(Events.DOCUMENTO_ACTUALIZADO);
						}
						
						log.info(raizLog + ELEMENTOS_PROCESADOS + SSMUtils.toStringFromList(listaOut));
					}
				} catch (SOAPException | WebServiceException | InterruptedException e) {
					log.error(raizLog + "Error al obtener el actualizar el documento (" + unId + ") en Content Server", e);
					cargarError(context, e);
					context.getStateMachine().sendEvent(Events.ERROR_OPERACION_FINAL);					
				} catch ( NoSuchElementException | DateTimeException | IOException e){
					log.error(raizLog +  ERROR_FATAL + "Error al obtener el actualizar el documento (" + unId + ") en Content Server", e);
					cargarError(context, e);
					context.getStateMachine().sendEvent(Events.ERROR_OPERACION_FINAL_SIN_REINTENTO);									
				}
					
				try {
					log.debug("GUARDANDO VARIABLES EN LA BD");
					persistContexto(context);
				} catch (Exception e) {
					log.error(
							raizLog + "ERROR GUARDANDO LAS VARIABLES [" + context.getStateMachine().getId() + "] en la base de datos",
							e);
				}
					
			};
		}
		
		@Bean
		public Action<States, Events> entryIniciarWFNotificacionUsuario(){
			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " entryIniciarWFNotificacionUsuario");
				
				boolean isFirmaCancelada = false;
				boolean isFallida = false;
				
				if (context.getMessageHeaders().containsKey(GlobalConsts.FIRMA_CANCELADA)) {
					isFirmaCancelada = context.getMessageHeaders().get(GlobalConsts.FIRMA_CANCELADA, Boolean.class);
				}
				
				
				
					Map<Object, Object> contextVars = context.getExtendedState().getVariables();
					log.info(raizLog + "Restaurando contexto previo de la BD");
					log.info("Obteniendo variables de la BD");
					
						try {
							restaurarvariablesBD(contextVars, context.getStateMachine().getId());
						} catch (Exception e) {
							log.warn(raizLog + "Error obteniendo variables de la BD + " + e.getMessage());
						}
						log.info("FIN Obteniendo variables de la BD");
					
						
				if (contextVars.containsKey(GlobalConsts.ERROR_MAQUINA)) {
					isFallida =  (Boolean) contextVars.get(GlobalConsts.ERROR_MAQUINA);
				}
					
					
				log.debug(raizLog + "Parametros flujo de notificacion");
				log.debug(raizLog + "isFirmaCancelada: " + isFirmaCancelada);
				log.debug(raizLog + "isFallida: " + isFallida);
				
				//Se obtienen las variables de la base de datos y se cargan a las variables en memoria
				String idSolicitante = (String) contextVars.get(GlobalConsts.ID_USUARIO_CS);
				String asunto = (String) contextVars.get(GlobalConsts.ASUNTO);
				Integer idCircuito = (Integer) contextVars.get(GlobalConsts.ID_CIRCUITO);
				Long idDocumentoPrincipal = (Long) contextVars.get(GlobalConsts.ID_DOC_PRINCIPAL);				
				String nombreUsuarioCS = (String) contextVars.get(GlobalConsts.NOMBRE_USUARIO_CS);
				
				log.debug(raizLog + "Asunto: " + asunto);
				log.debug(raizLog + "id circuito: " + idCircuito);
				log.debug(raizLog + "Solictante id: " + idSolicitante);
				log.debug(raizLog + "nombreSolicitante: " + nombreUsuarioCS);
												
				Map<String, ValorAtributo> atributos = new HashMap<String, ValorAtributo>();
							
				atributos.put("asunto", new ValorAtributo(asunto, TipoAtributo.STRING));
				atributos.put("idCircuito", new ValorAtributo(idCircuito, TipoAtributo.STRING));
				atributos.put("solicitante", new ValorAtributo(Long.parseLong(idSolicitante), TipoAtributo.USER));
				String email = workflowProps.getNotificacion().getEmailRemitente();
				atributos.put("emailRemitente", new ValorAtributo(email, TipoAtributo.STRING));
				atributos.put("nombreSolicitante", new ValorAtributo(nombreUsuarioCS, TipoAtributo.STRING));
				atributos.put("idDocumentoPrincipal", new ValorAtributo(idDocumentoPrincipal, TipoAtributo.INTEGER));
				
				if (!(isFirmaCancelada || isFallida)) {
					atributos.put("firma_exitosa", new ValorAtributo(true, TipoAtributo.STRING));
				} else if (isFirmaCancelada) {
					atributos.put("firma_exitosa", new ValorAtributo(false, TipoAtributo.STRING));
					atributos.put("firma_cancelada", new ValorAtributo(true, TipoAtributo.STRING));
					String motivo = context.getMessageHeaders().get(GlobalConsts.MOTIVO_CANCELACION, String.class);
					String usuarioCancela = context.getMessageHeaders().get(GlobalConsts.USUARIO_CANCELACION,
							String.class);					

					if (org.springframework.util.StringUtils.hasText(motivo)) {
						atributos.put("motivo", new ValorAtributo(motivo, TipoAtributo.MULTILINE));
					} else {
						atributos.put("motivo", new ValorAtributo("Ninguno", TipoAtributo.MULTILINE));
					}

					if (org.springframework.util.StringUtils.hasText(usuarioCancela)) {
						
						@SuppressWarnings("unchecked")
						List<FirmanteSSM> firmantesProceso = context.getExtendedState().get(GlobalConsts.FIRMANTES, List.class);
						
						Optional<FirmanteSSM> firmanteCancelaOpt = firmantesProceso.stream().filter(unUsuario -> unUsuario.getCedula().equals(Long.parseLong(usuarioCancela))).findFirst();
						
						String nombre = "NO ESPECIFICADO";
						
						if(firmanteCancelaOpt.isPresent()){
							FirmanteSSM firmanteCancela = firmanteCancelaOpt.get();
							nombre = firmanteCancela.getNombre();
							
						}
					
						atributos.put("usuario_cancela", new ValorAtributo(nombre, TipoAtributo.STRING));

						if (log.isTraceEnabled()) {
							log.trace(raizLog + "Usuario que cancela: " + usuarioCancela);
							log.trace(raizLog + "Motivo de cancelacion: " + motivo);
						}

					}
				} else {
					atributos.put("firma_exitosa", new ValorAtributo(false, TipoAtributo.STRING));
					atributos.put("firma_cancelada", new ValorAtributo(false, TipoAtributo.STRING));
				}
				
				
				long longIdMap = Long.parseLong(workflowProps.getNotificacion().getMapId());
				
				log.info(raizLog + "Inciando Workflow de notificacion");
				
				
				long idWorkflow;
				try {
					idWorkflow = Workflow.iniciarWorkflowConAtributos(
							autenticacionCS.getAdminSoapHeader(), longIdMap,"Notificacion Firma maquina " + context.getStateMachine().getId()  , atributos, WSDLs.getWorkflow());
										
					if (!isFirmaCancelada) {
						context.getStateMachine().sendEvent(Events.WORKFLOW_INICIADO);
					}else {
						context.getStateMachine().sendEvent(Events.FINALIZAR_CANCELACION);
					}
					
					log.info(raizLog + "Se inicia al instancia de workflow: " + idWorkflow);
				} catch (SOAPException | WebServiceException e) {
					log.error(raizLog + "Error al iniciar el Workflow con MapID : " + longIdMap, e);
					cargarError(context, e);
					context.getStateMachine().sendEvent(Events.ERROR_OPERACION_FINAL);
				} catch (NumberFormatException | NoSuchElementException | IOException e) {					
					log.error(raizLog + ERROR_FATAL + "Error al iniciar el Workflow con MapID : " + longIdMap, e);
					cargarError(context, e);
					context.getStateMachine().sendEvent(Events.ERROR_OPERACION_FINAL_SIN_REINTENTO);
				}
				
				if (!isFirmaCancelada) {
					try {
						log.debug("GUARDANDO VARIABLES EN LA BD");
						persistContexto(context);
					} catch (Exception e) {
						log.error(raizLog + "ERROR GUARDANDO LAS VARIABLES [" + context.getStateMachine().getId()
								+ "] en la base de datos", e);
					}
				}
					
			};
		}
		
		
		
		@Bean
		public Action<States, Events> iniciarWfNotificacionAlt(){
			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " iniciarWfNotificacionAlt");
				
								
				log.debug(raizLog + "Accion externa: " + tipoAccionExterna);
				
				AccionExterna accionExterna = AccionExternaFactory.crearAccionExterna(tipoAccionExterna);
				
				if(accionExterna == null){
					log.debug(raizLog + "Accion Externa es nula");
				}
				
				String idSolicitante = context.getExtendedState().get(GlobalConsts.ID_USUARIO_CS, String.class);
				String asunto = context.getExtendedState().get(GlobalConsts.ASUNTO, String.class);
				asunto = StringUtils.substringAfterLast(asunto, " - ");
				long longIdMap = workflowProps.getNotificacionAlternativa().getMapId();
				
											
				Map<String, ValorAtributo> atributos = new HashMap<String, ValorAtributo>();
							
				atributos.put("_idSolicitante", new ValorAtributo(Long.parseLong(idSolicitante), TipoAtributo.USER));
				atributos.put("_asunto", new ValorAtributo(asunto, TipoAtributo.STRING));
				atributos.put("_numeroRadicado", new ValorAtributo(asunto, TipoAtributo.STRING));
				atributos.put("_errorDistribucion", new ValorAtributo(true, TipoAtributo.STRING));
								
				String respuestaAccion = "";
				
				//TODO bloque de prueba
				
				log .info("variables vacias?");
				if(atributos.isEmpty()){
					throw new NullPointerException("Variables vacias");
				}
				
				log .info("entry set vacio?");
				if(atributos.entrySet().isEmpty()){
					throw new NullPointerException("Variables vacias");
				}
				
						
				log.info(raizLog + "Llamado al metodo ejecutar de la Accion Externa del plugin");
				try {
					
					log.trace(raizLog + "Atributos enviados al WF");
					atributos.forEach((k,v) -> log.trace(raizLog + "Llave: " + k + " - Valor: " + v));
					
					respuestaAccion = accionExterna.notificarError(atributos, longIdMap, WSDLs.getWorkflow(), autenticacionCS.getAdminSoapHeader());
					
				} catch (IOException | SOAPException e1) {
					e1.printStackTrace();
				
				}
				
				log.info(raizLog + "Mensaje accion Externa del plugin: " + respuestaAccion); 
									
				
					try {
						log.debug("GUARDANDO VARIABLES EN LA BD");
						persistContexto(context);
					} catch (Exception e) {
						log.error(raizLog + "ERROR GUARDANDO LAS VARIABLES [" + context.getStateMachine().getId()
								+ "] en la base de datos", e);
					}
				
					
			};
		}
		
		
		@Bean
		public Action<States, Events> entryObtenerCopiaCompulsadaPF(){
			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " entryObtenerDocumentoPF");
				
				//Se obtienen las variables de la base de datos y se cargan a las variables en memoria
				Map<Object, Object> contextVars = context.getExtendedState().getVariables();
				log.info(raizLog + "Restaurando contexto previo de la BD");
				
				if (contextVars.isEmpty()) {
					log.info("Obteniendo variables de la BD");
					try {
						restaurarvariablesBD(contextVars, context.getStateMachine().getId());
					} catch (Exception e) {
						log.warn(raizLog + "Error obteniendo variables de la BD + " + e.getMessage());

					}
					log.info("FIN Obteniendo variables de la BD");
				}
				
				
				@SuppressWarnings("unchecked")
				Map<Long, DocumentoSSM> documentos = context.getExtendedState().get(GlobalConsts.DOCUMENTOS_SSM, HashMap.class);			
				@SuppressWarnings("unchecked")
				ArrayList<Long> listaIn = context.getExtendedState().get(GlobalConsts.LISTA_IN, ArrayList.class);								
				@SuppressWarnings("unchecked")
				ArrayList<Long> listaOut = context.getExtendedState().get(GlobalConsts.LISTA_OUT, ArrayList.class);
				
				String conexionUsuarioCS = context.getExtendedState().get(GlobalConsts.CONEXION_USUARIO_CS, String.class);
				
				List<Long> idsCopiasCompulsadas  = new ArrayList<Long>();
				
				long unId = -1;
				String unIdDocumentoPF = "";
				
				try {
					log.info(raizLog + ELEMENTOS_POR_PROCESAR + SSMUtils.toStringFromList(listaIn));
				} catch (IndexOutOfBoundsException e) {
					listaIn.clear();
					Iterator<Entry<Long, DocumentoSSM>> iter = documentos.entrySet().iterator();
					while (iter.hasNext()) {
						listaIn.add(iter.next().getKey());
					}
					listaOut.clear();
					log.info(raizLog + ELEMENTOS_POR_PROCESAR + SSMUtils.toStringFromList(listaIn));
				}
								
				try {
					Iterator<Long> iter = listaIn.iterator();
					while (iter.hasNext()) {

						unId = iter.next();
						DocumentoSSM unDocumento = documentos.get(unId);
						unIdDocumentoPF = unDocumento.getIdPf();

						log.info(raizLog + "Obteniendo Copia Compulsada (" + unIdDocumentoPF + ") de portafirmas");

						RespuestaObtenerDocumeto respuesta = DocumentoPortafirmas
								.obtenerCopiaCompulsada(portafirmasProps.getTicketadmin(), unIdDocumentoPF, WSDLs.getCircuito());

						//unDocumento.setContenidoCopiaCompulsada(respuesta.getContenido());
						
						DataHandler unDocContenido = respuesta.getContenido();
						//InputStream is = unDocContenido.getInputStream();
						//OutputStream os = new FileOutputStream(unDocumento.getContenidoOriginalPath().toFile());
						
						log.info(raizLog + "Documento Copia Compulsada [" + unId
								+ "] de Porfafirmas obtenido de portafirmas");
						
						log.info(raizLog + "Se inicio el proceso de carge de copia compulsada para el documento [" + unId
								+ "]");
						
						String nombreCategoria  = documentoGeneralProps.getNombreCategoria();
						
						// Metadatos de la Copia Compulsada
						Map<String, Object> metadatosCC = new HashMap<String, Object>();
						metadatosCC.put(documentoGeneralProps.getMetadato().getFechaDocumento(), new Date());
						metadatosCC.put(documentoGeneralProps.getMetadato().getEstado(), "Oficial");
						metadatosCC.put(documentoGeneralProps.getMetadato().getTipoDocumental(), "Copia compulsada");
						metadatosCC.put(documentoGeneralProps.getMetadato().getSerie(), "SR0900000");
						
						Long idCC = ContenidoDocumento.crearDocumentoCatDocumento(autenticacionCS.getUserSoapHeader(conexionUsuarioCS),
								unId, unDocumento.getNombre(), unDocContenido,
								Long.parseLong(documentoGeneralProps.getId()), nombreCategoria, metadatosCC, true, null , WSDLs.getDocumento());
						
						if(idCC != null) {
							log.info(raizLog + "Copia compulsada creada con id [" + idCC + "]");
							idsCopiasCompulsadas.add(idCC);
						}

						log.info(raizLog + "creado documento y metadatos de la Copia Compulsada del documento [" + unId
								+ "] de Content Server");
						
						//IOUtils.copy(is, os);
						
						//IOUtils.closeQuietly(is);
						//IOUtils.closeQuietly(os);

						listaOut.add(unId);
						iter.remove();

					}
					
					context.getExtendedState().getVariables().put(GlobalConsts.IDS_COPIAS_COMPULSADAS, idsCopiasCompulsadas);
					
					context.getStateMachine().sendEvent(Events.COPIA_COMPULSADA_OBTENIDA);
					
					log.info(raizLog + ELEMENTOS_PROCESADOS + SSMUtils.toStringFromList(listaOut));

				} catch (Exception e) {
					log.error(raizLog + "Error al obtener la copia compulsada (" + unId + ") de portafirmas con id: "
							+ unIdDocumentoPF, e);
					cargarError(context, e);
					context.getStateMachine().sendEvent(Events.ERROR_OPERACION_FINAL);
				}

								
				try {
					log.debug("GUARDANDO VARIABLES EN LA BD");
					persistContexto(context);
					log.debug("FIN GUARDANDO VARIABLES EN LA BD");
				} catch (Exception e) {
					log.error(
							raizLog + "ERROR GUARDANDO LAS VARIABLES [" + context.getStateMachine().getId() + "] en la base de datos",
							e);
				}
			
			};
		}
		
		@Bean
		public Action<States, Events> entryCargarCopiaCompulsadaCS(){
			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " entryActualizarDocCS");
				
				//Se obtienen las variables de la base de datos y se cargan a las variables en memoria
				Map<Object, Object> contextVars = context.getExtendedState().getVariables();
				log.info(raizLog + "Restaurando contexto previo de la BD");
				
				if (contextVars.isEmpty()) {
					log.info("Obteniendo variables de la BD");
					try {
						restaurarvariablesBD(contextVars, context.getStateMachine().getId());
					} catch (Exception e) {
						log.warn(raizLog + "Error obteniendo variables de la BD " + e.getMessage());

					}
					log.info("FIN Obteniendo variables de la BD");
				}
				
				@SuppressWarnings("unchecked")
				Map<Long, DocumentoSSM> documentos = context.getExtendedState().get(GlobalConsts.DOCUMENTOS_SSM, HashMap.class);			
				@SuppressWarnings("unchecked")
				ArrayList<Long> listaIn = context.getExtendedState().get(GlobalConsts.LISTA_IN, ArrayList.class);								
				@SuppressWarnings("unchecked")
				ArrayList<Long> listaOut = context.getExtendedState().get(GlobalConsts.LISTA_OUT, ArrayList.class);
				
				long unId = -1;
				
				try {
					log.info(raizLog + ELEMENTOS_POR_PROCESAR + SSMUtils.toStringFromList(listaIn));
				} catch (IndexOutOfBoundsException e) {
					listaIn.clear();
					Iterator<Entry<Long, DocumentoSSM>> iter = documentos.entrySet().iterator();
					while (iter.hasNext()) {
						listaIn.add(iter.next().getKey());
					}
					listaOut.clear();
					log.info(raizLog + ELEMENTOS_POR_PROCESAR + SSMUtils.toStringFromList(listaIn));
				}
				
				try {
					Iterator<Long> iter = listaIn.iterator();
					while (iter.hasNext()) {
						unId = iter.next();

						DocumentoSSM unDocumento = documentos.get(unId);

						log.info(raizLog + "Subiendo version y metadatos Copia Compulsada del documento (" + unId + ") de Content Server");

						//int idCategoriaInt = Integer.parseInt(categoryProps.getId());						
						String nombreCategoria  = documentoGeneralProps.getNombreCategoria();					
						
						//ContenidoDocumento.crearCopiaCompulsada(autenticacionCS.getUserSoapHeader(usuario), unId, unDocumento.getNombre(), unDocumento.getContenidoCopiaCompulsada(), idCategoriaInt, nombreCategoria);
						
						// Metadatos de la Copia Compulsada
						Map<String, Object> metadatosCC = new HashMap<String, Object>();
						metadatosCC.put(documentoGeneralProps.getMetadato().getFechaDocumento(), new Date());
						metadatosCC.put(documentoGeneralProps.getMetadato().getEstado(), "Oficial");
						metadatosCC.put(documentoGeneralProps.getMetadato().getTipoDocumental(), "Copia compulsada");
						metadatosCC.put(documentoGeneralProps.getMetadato().getSerie(), "SR0900000");
						
						//TODO prueba datahandler
						FileDataSource fds = new FileDataSource(unDocumento.getContenidoOriginalPath().toFile());
						DataHandler dthdlr = new DataHandler(fds);
						
						/*
						ContenidoDocumento.crearDocumentoCatDocumento(autenticacionCS.getUserSoapHeader(conexionUsuarioCS),
								unId, unDocumento.getNombre(), unDocumento.getContenidoCopiaCompulsada(),
								Long.parseLong(documentoGeneralProps.getId()), nombreCategoria, metadatosCC, true, null , WSDLs.getDocumento());
						*/
						
						ContenidoDocumento.crearDocumentoCatDocumento(autenticacionCS.getAdminSoapHeader(),
								unId, unDocumento.getNombre(), dthdlr,
								Long.parseLong(documentoGeneralProps.getId()), nombreCategoria, metadatosCC, true, null , WSDLs.getDocumento());

						log.info(raizLog + "creado documento y metadatos de la Copia Compulsada del documento (" + unId
								+ ") de Content Server");

						// Se elimina la referencia al documento de la copia
						// compulsada
						//unDocumento.setContenidoCopiaCompulsada(null);

						listaOut.add(unId);
						iter.remove();

						log.info(raizLog + ELEMENTOS_PROCESADOS + SSMUtils.toStringFromList(listaOut));		
						
					}
					
					context.getStateMachine().sendEvent(Events.COPIA_COMPULSADA_ACTUALIZADA);
				} catch (SOAPException | WebServiceException | InterruptedException e) {
					log.error(raizLog + "Error al crear copia compulsada de (" + unId + ") en Content Server", e);
					cargarError(context, e);
					context.getStateMachine().sendEvent(Events.ERROR_OPERACION_FINAL);					
				} catch (NullPointerException | NoSuchElementException | DateTimeException | IOException e) {
					log.error(raizLog + ERROR_FATAL + "Error al crear copia compulsada de (" + unId + ") en Content Server", e);
					cargarError(context, e);
					context.getStateMachine().sendEvent(Events.ERROR_OPERACION_FINAL_SIN_REINTENTO);					
				}
				
				try {
					log.debug("GUARDANDO VARIABLES EN LA BD");
					persistContexto(context);
				} catch (Exception e) {
					log.error(
							raizLog + "ERROR GUARDANDO LAS VARIABLES [" + context.getStateMachine().getId() + "] en la base de datos",
							e);
				}
				
			};
		}
				
		@Bean
		public Action<States, Events> entryVertificarNumeroReintento() {
			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " entryVertificarNumeroReintento");

				int reintentoActual = context.getExtendedState().get(GlobalConsts.REINTENTO, Integer.class);

				int proximoReintento = reintentoActual + 1;

				context.getExtendedState().getVariables().put(GlobalConsts.REINTENTO, proximoReintento);

				long tiempoParam = globalProperties.getTiempoReintento();
				long tiempoMinutos = tiempoParam / 60000;

				if ((reintentoActual + 1) < globalProperties.getNumeroReintentos()) {

					log.info(raizLog + "Reintento numero: " + reintentoActual);
					log.info(raizLog + "Proxima ejecucion en : " + tiempoMinutos + " minutos aproximadamente");
					
				//Penultima ejecucion reintento	
				} else if ((reintentoActual + 1) == globalProperties.getNumeroReintentos()) {
					
					log.warn(raizLog + "Penultima ejecucion");
					log.info(raizLog + "Reintento numero: " + reintentoActual);
					log.info(raizLog + "Proxima ejecucion en : " + tiempoMinutos + " minutos aproximadamente");

					String error = "";

					try {
						error = context.getExtendedState().get(GlobalConsts.MENSAJE_ERROR, String.class);

						if (!org.springframework.util.StringUtils.hasText(error)) {
							error = "NO ESPECIFICADO";
						}

					} catch (Exception e) {
						error = "NO ESPECIFICADO";
					}

					String asuntoMaquina = context.getExtendedState().get(GlobalConsts.ASUNTO, String.class);
					String idMquina = context.getStateMachine().getId();

					log.debug(raizLog + "Id Maquina: " + idMquina);
					log.debug(raizLog + "Asunto: " + asuntoMaquina);

					Map<String, ValorAtributo> atributos = new HashMap<String, ValorAtributo>();

					atributos.put("asunto",
							new ValorAtributo("Ultima ejecucion de reintentos - maquina No: " + idMquina, TipoAtributo.STRING));

					atributos.put("error", new ValorAtributo(
							"ADVERTENCIA ultima ejecucion Maquina No " + idMquina + " en "
									+ tiempoMinutos + "  minutos aproximandamente  " + " - MENSAJE DE ERROR: " + error,
							TipoAtributo.MULTILINE));

					String email = workflowProps.getNotificacionAdmon().getEmailAdmon();
					atributos.put("correoDestinatario", new ValorAtributo(email, TipoAtributo.STRING));
					atributos.put("idMaquina", new ValorAtributo(Long.parseLong(idMquina), TipoAtributo.INTEGER));
					atributos.put("asuntoMaquina", new ValorAtributo(asuntoMaquina, TipoAtributo.STRING));

					long longIdMap = Long.parseLong(workflowProps.getNotificacionAdmon().getMapId());

					log.info(raizLog + "Inciando Workflow de notificacion Administrador");

					long idWorkflow;
					try {
						idWorkflow = Workflow.iniciarWorkflowConAtributos(autenticacionCS.getAdminSoapHeader(),
								longIdMap, "Error Reintento maquina " + idMquina, atributos, WSDLs.getWorkflow());
						// context.getStateMachine().sendEvent(Events.WORKFLOW_INICIADO);
						log.info(raizLog + "Se inicia la instancia de workflow: " + idWorkflow);
					} catch (SOAPException e) {
						log.error(raizLog + "Error al iniciar el Workflow con MapID : " + longIdMap, e);
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.ERROR_OPERACION_FINAL_SIN_REINTENTO);
					} catch (NumberFormatException | NoSuchElementException | IOException e) {
						log.error(raizLog + ERROR_FATAL + "Error al iniciar el Workflow con MapID : " + longIdMap, e);
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.ERROR_OPERACION_FINAL_SIN_REINTENTO);
					}
					
					
				} else if (reintentoActual == globalProperties.getNumeroReintentos()) {
					log.error(raizLog + "Numero maximo de reintentos alcanzado: "
							+ globalProperties.getNumeroReintentos());
					context.getStateMachine().sendEvent(Events.NUMERO_MAXIMO_REINTENTOS_ALCANZADO);
				}

			};
		}
		
		@Bean
		public Action<States, Events> entryAccionExterna(){
			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " entryAccionExterna");
				
				Map<Object, Object> variablesMap = context.getExtendedState().getVariables();
				log.info(raizLog + "Restaurando contexto previo de la BD");
				if (variablesMap.isEmpty()) {
					log.info("Obteniendo variables de la BD");
					try {
						restaurarvariablesBD(variablesMap, context.getStateMachine().getId());
					} catch (Exception e) {
						log.warn(raizLog + "Error obteniendo variables de la BD + " + e.getMessage());
					}
					log.info("FIN Obteniendo variables de la BD");
				}
				
				String urlExterna = context.getExtendedState().get(GlobalConsts.URL_EXTERNA, String.class);
				
				AccionExterna accionExterna = AccionExternaFactory.crearAccionExterna(tipoAccionExterna);
				
				//log.debug(raizLog + "Entra a eliminar referencias a objetos pesados en memoria");
				//SSMUtils.limpiarReferenciasPesadas(context);
				
				//Map<Object, Object> variablesMap = context.getExtendedState().getVariables();
				
				String respuestaAccion = "";
				
				//TODO bloque de prueba
				
				log .info("variables vacias?");
				if(variablesMap.isEmpty()){
					throw new NullPointerException("Variables vacias");
				}
				
				log .info("entry set vacio?");
				if(variablesMap.entrySet().isEmpty()){
					throw new NullPointerException("Variables vacias");
				}
				
				if (log.isTraceEnabled()) {
					log.trace("Imprimir variables");
					Iterator<Entry<Object, Object>> iterVarsMap = variablesMap.entrySet().iterator();
					while (iterVarsMap.hasNext()) {
						Entry<Object, Object> unVar = iterVarsMap.next();
						log.trace("Un nombre: " + unVar.getKey());
						log.trace("Un valor: " + unVar.getValue());
					}
				}
				
				log.info(raizLog + "Llamado al metodo ejecutar de la Accion Externa del plugin");
				try {					
					respuestaAccion = accionExterna.ejecutar(variablesMap, urlExterna);
					
					log.info(raizLog + "Mensaje accion Externa del plugin: " + respuestaAccion); 
					
					context.getStateMachine().sendEvent(Events.ACCION_EXTERNA_FINALIZADA);					
				} catch (IOException e) {					
					log.error(raizLog + ERROR_FATAL + "Error ejecutanto accion externa  : " + tipoAccionExterna, e);
					cargarError(context, e);
					context.getStateMachine().sendEvent(Events.ERROR_OPERACION_FINAL);
					//context.getStateMachine().sendEvent(Events.ERROR_OPERACION_FINAL_SIN_REINTENTO);
				}								
			};
		}
		
		@Bean
		public Action<States, Events> entryAccionExternaCancelacion(){
			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " entryAccionExternaCancelacion");
				
				String urlExterna = context.getExtendedState().get(GlobalConsts.URL_EXTERNA, String.class);
				String motivo = context.getMessageHeaders().get(GlobalConsts.MOTIVO_CANCELACION, String.class);
				
				if(org.springframework.util.StringUtils.hasText(motivo)){
					context.getExtendedState().getVariables().put(GlobalConsts.MOTIVO_CANCELACION, motivo);
				}else{
					context.getExtendedState().getVariables().put(GlobalConsts.MOTIVO_CANCELACION, "NO ESPECIFICADO");
				}
				
				String usuarioCancela = context.getMessageHeaders().get(GlobalConsts.USUARIO_CANCELACION,
						String.class);
				
				String nombreUsuarioCancela = "NO ESPECIFICADO";

				if (org.springframework.util.StringUtils.hasText(usuarioCancela)) {

					@SuppressWarnings("unchecked")
					List<FirmanteSSM> firmantesProceso = context.getExtendedState().get(GlobalConsts.FIRMANTES,
							List.class);

					Optional<FirmanteSSM> firmanteCancelaOpt = firmantesProceso.stream()
							.filter(unUsuario -> unUsuario.getCedula().equals(Long.parseLong(usuarioCancela)))
							.findFirst();

					if (firmanteCancelaOpt.isPresent()) {
						FirmanteSSM firmanteCancela = firmanteCancelaOpt.get();
						nombreUsuarioCancela = firmanteCancela.getNombre();

					}

				}
				
				context.getExtendedState().getVariables().put(GlobalConsts.NOMBRE_USUARIO_CANCELACION, nombreUsuarioCancela);
				
				
				AccionExterna accionExterna = AccionExternaFactory.crearAccionExterna(tipoAccionExterna);
							
				Map<Object, Object> variablesMap = context.getExtendedState().getVariables();
				
				String respuestaAccion = "";
				
				log.info(raizLog + "Llamado al metodo ejecutar de la Accion Externa de cancelación del plugin");
				try {
					respuestaAccion = accionExterna.cancelar(variablesMap , urlExterna);
					
					log.info(raizLog + "Mensaje accion Externa de cancelacion del plugin: " + respuestaAccion);
					
					context.getStateMachine().sendEvent(Events.FINALIZAR_CANCELACION_ALTERNATIVA);
				} catch (IOException e) {
					log.error(raizLog + ERROR_FATAL + "Error ejecutanto accion externa de cancelacion  : " + tipoAccionExterna, e);
					cargarError(context, e);
					context.getStateMachine().sendEvent(Events.ERROR_OPERACION_FINAL);
					//context.getStateMachine().sendEvent(Events.ERROR_OPERACION_FINAL_SIN_REINTENTO);
				}			
				
			};
		}
		
		@Bean
		public Action<States, Events> notificarError() {
			return (context) -> {
				context.getExtendedState().getVariables().put(GlobalConsts.ERROR_MAQUINA, true);
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " notificarErrorAdmin");
				

				//Se obtienen las variables de la base de datos y se cargan a las variables en memoria
				Map<Object, Object> contextVars = context.getExtendedState().getVariables();
				log.info(raizLog + "Restaurando contexto previo de la BD");
				log.info("Obteniendo variables de la BD");
				
				
				try {
					restaurarvariablesBD(contextVars, context.getStateMachine().getId());
				} catch (Exception e) {
					log.warn(raizLog + "Error obteniendo variables de la BD + " + e.getMessage());
				}
				log.info("FIN Obteniendo variables de la BD");
				
				
				
				
				//String asuntoMaquina = context.getExtendedState().get(GlobalConsts.ASUNTO, String.class);
				String asuntoMaquina = (String) contextVars.get(GlobalConsts.ASUNTO);
				String idMquina = context.getStateMachine().getId();
				
				log.debug(raizLog + "Id Maquina: " + idMquina);
				log.debug(raizLog + "Asunto: " + asuntoMaquina);				
				
				Map<String, ValorAtributo> atributos = new HashMap<String, ValorAtributo>();				
				
				atributos.put("asunto", new ValorAtributo("Error en la maquina No: " + idMquina, TipoAtributo.STRING));
				
				//String error = context.getExtendedState().get(GlobalConsts.MENSAJE_ERROR, String.class);
				String error = (String) contextVars.get(GlobalConsts.MENSAJE_ERROR);
				
				//String estado = context.getExtendedState().get(GlobalConsts.ESTADO_ERROR, String.class);
				String estado = (String) contextVars.get(GlobalConsts.ESTADO_ERROR);
				
				String mensaje = "Estado: " + estado + "\n";
				mensaje = mensaje + " - Error: " + error;
				
				atributos.put("error", new ValorAtributo(mensaje, TipoAtributo.MULTILINE));
				String email = workflowProps.getNotificacionAdmon().getEmailAdmon();
				atributos.put("correoDestinatario", new ValorAtributo(email, TipoAtributo.STRING));
				atributos.put("idMaquina", new ValorAtributo(Long.parseLong(idMquina), TipoAtributo.INTEGER));
				atributos.put("asuntoMaquina", new ValorAtributo(asuntoMaquina, TipoAtributo.STRING));
				
				long longIdMap = Long.parseLong(workflowProps.getNotificacionAdmon().getMapId());
				
				log.info(raizLog + "Inciando Workflow de notificacion Administrador");
				
				long idWorkflow;
				try {
					idWorkflow = Workflow.iniciarWorkflowConAtributos(autenticacionCS.getAdminSoapHeader(), longIdMap,
							"Error maquina " + idMquina, atributos, WSDLs.getWorkflow());
					context.getStateMachine().sendEvent(Events.WORKFLOW_INICIADO);
					log.info(raizLog + "Se inicia al instancia de workflow: " + idWorkflow);
				} catch (SOAPException e) {
					log.error(raizLog + "Error al iniciar el Workflow con MapID : " + longIdMap, e);
				} catch (NumberFormatException | NoSuchElementException | IOException e) {
					log.error(raizLog + ERROR_FATAL + "Error al iniciar el Workflow con MapID : " + longIdMap, e);
				}
								
			
			};
		}
		
		@Bean
		public Action<States, Events> persist() {
			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + "GUARDANDO MAQUINA [" + context.getStateMachine().getId() + "]");
				StateMachine<States, Events> machine = context.getStateMachine();

				try {
					log.info("Guardando en la tabla MAQUINA");
					persistMaquina(context);

				} catch (Exception e) {
					log.error(raizLog + "ERROR GUARDANDO LA MAQUINA [" + machine.getId() + "] en la base de datos", e);
				}

				try {
					log.info("Guardando en la tabla CONTEXTO");
					persistContexto(context);
				} catch (Exception e) {
					log.error(raizLog + "ERROR GUARDANDO LAS VARIABLES [" + machine.getId() + "] en la base de datos",
							e);
				}
			};
		}
		
		@Bean
		public Action<States, Events> persistWithoutContext() {
			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + "GUARDANDO MAQUINA [" + context.getStateMachine().getId() + "]");
				StateMachine<States, Events> machine = context.getStateMachine();

				try {
					log.info("Guardando en la tabla MAQUINA");
					persistMaquina(context);

				} catch (Exception e) {
					log.error(raizLog + "ERROR GUARDANDO LA MAQUINA [" + machine.getId() + "] en la base de datos", e);
				}

			};
		}
		
		@Bean
		public Action<States, Events> persistAndDeleteContext() {
			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				raizLog = raizLog + "PERSIST - ";
				log.info(raizLog + "GUARDANDO MAQUINA [" + context.getStateMachine().getId() + "]");
				StateMachine<States, Events> machine = context.getStateMachine();
				
				try {
					log.info(raizLog + "Guardando maquina");
					persistMaquina(context);

				} catch (Exception e) {
					log.error(raizLog + "ERROR GUARDANDO LA MAQUINA [" + machine.getId() + "] en la base de datos", e);
				}


				try {
					log.info(raizLog + "Guardando variables");
					persistMaquina(context);

				} catch (Exception e) {

					log.error(raizLog + "ERROR GUARDANDO LAS VARIABLES [" + machine.getId() + "] en la base de datos",
							e);
				}

				try {
					log.info(raizLog + "Borrando resgistro de la tabla CONTEXTO");
					deleteContexto(context);
				} catch (Exception e) {
					log.error(raizLog + "ERROR BORRANDO CONTEXTO [" + machine.getId() + "] en la base de datos", e);


				}

			};
		}
		
		@Bean
		public Action<States, Events> persistCircuit(){
			return(context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + "GUARDANDO MAQUINA [" + context.getStateMachine().getId() + "]");
				StateMachine<States, Events> machine = context.getStateMachine();
				//@SuppressWarnings("unchecked")
				//Map<Long, DocumentoSSM> documentos = context.getExtendedState().get(GlobalConsts.DOCUMENTOS_SSM, HashMap.class);
							
				try {
					persistMaquina(context);
					
//					Iterator<Entry<Long, DocumentoSSM>> iterDocumentos = documentos.entrySet().iterator();
//					while(iterDocumentos.hasNext()){
//						DocumentoSSM unDocumento = iterDocumentos.next().getValue();
//						//Bloque para eliminar						
//						DataHandler contenido = unDocumento.getContenidoOriginal();
//						if(contenido != null){
//							contenido = null;
//						}
//						
//					}
					
				} catch (Exception e) {
					log.error(raizLog + "ERROR GUARDANDO LA MAQUINA [" + machine.getId() + "] en la base de datos",e);
				}								
			};
		}
		
		
		@Bean
		public Guard<States, Events> evaluarTipoAccion1() {			
			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " GUARD evaluarTipoAccion1");
				
				String accion  = (String) context.getExtendedState().getVariables().get(GlobalConsts.ACCION_EXTERNA);
				log.info(raizLog + "Accion externa: " + accion);
				
				if(org.springframework.util.StringUtils.hasText(accion)){
					tipoAccionExterna = accion;
					return false;
				}else{
					return true;
				}
			};
		}
		
		@Bean
		public Guard<States, Events> evaluarTipoAccion2() {
			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " GUARD evaluarTipoAccion2");
				
				String accion  = (String) context.getExtendedState().getVariables().get(GlobalConsts.ACCION_EXTERNA);
				log.info(raizLog + "Accion externa: " + accion);
				
				if(org.springframework.util.StringUtils.hasText(accion)){
					tipoAccionExterna = accion;
					return true;
				}else{
					return false;
				}
			};
		}
		
		private void restaurarvariablesBD(Map<Object, Object> variables, String idMaquina){
			
			Map<Object, Object> tempVars = contextoPersist.read(idMaquina);

			// contextVars.putAll(tempVars);
			if (tempVars == null) {
				log.info("lista vacia");
			} else if (tempVars.isEmpty()) {
				log.info("lista nula");
			} else {
				log.info("Cargando variables");

				Iterator<Entry<Object, Object>> iterTempVars = tempVars.entrySet().iterator();
				while (iterTempVars.hasNext()) {
					Entry<Object, Object> unEntry = iterTempVars.next();					
					variables.put(unEntry.getKey(), unEntry.getValue());
				}

			}
			
		}
		
		
		private void persistMaquina(StateContext<States, Events> context) throws Exception {

			StateMachine<States, Events> machine = context.getStateMachine();

			stateMachinePersister.persist(machine, machine.getId());
		}
		
		private void persistContexto(StateContext<States, Events> context) throws DataAccessException {
			
			/*
			if(!context.getExtendedState().getVariables().get(GlobalConsts.MENSAJE_ERROR).equals(validacionError)){
				context.getExtendedState().getVariables().put(GlobalConsts.MENSAJE_ERROR, validacionError);
				context.getExtendedState().getVariables().put(GlobalConsts.ERROR_MAQUINA, validacionEstadoError);
				context.getExtendedState().getVariables().put(GlobalConsts.ESTADO_ERROR, validacionEstado);
			}
			*/
			
			Map<Object, Object> variablesMap = context.getExtendedState().getVariables();
			
			//String estado = (context.getStateMachine().getState().getId()== null) ? "Estado vacio" : (context.getStateMachine().getState().getId().toString());
			String estado;
			try {
				estado = context.getStateMachine().getState().getId().toString();
			} catch (Exception e) {
				estado = "NO DEFINIDO";
			}
			
			log.trace("Estado: " + estado);
			log.trace("Variables PersistContexto: ");
			variablesMap.forEach((k,v) -> log.trace("Llave: "+ k + " - Valor: " + v));
			
			contextoPersist.write(context.getStateMachine().getId(), variablesMap);
		}
		
		private void deleteContexto(StateContext<States, Events> context) throws DataAccessException {
			contextoPersist.delete(context.getStateMachine().getId());
		}
		
		private void cargarError(StateContext<States, Events> context, Exception e ){
			try {
				context.getExtendedState().getVariables().put(GlobalConsts.MENSAJE_ERROR, e.getMessage());
				//validacionError = e.getMessage();
				String estado = context.getStateMachine().getState().getId().name();
				context.getExtendedState().getVariables().put(GlobalConsts.ESTADO_ERROR, estado);
				//validacionEstado = estado;
				context.getExtendedState().getVariables().put(GlobalConsts.ERROR_MAQUINA, true);
				//validacionEstadoError = true;
				
			} catch (Exception e1) {
				log.error("Error cargando mensaje de error",e1);
			}
		}
		
		/*******/
	
	}
	


}
