package co.gov.banrep.iconecta.ssm.correspondencia;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.activation.DataHandler;
import javax.xml.soap.SOAPException;
import javax.xml.ws.WebServiceException;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.task.TaskExecutor;
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
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;

import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.fault.ServerSOAPFaultException;

import co.gov.banrep.iconecta.cs.autenticacion.AutenticacionCS;
import co.gov.banrep.iconecta.cs.cliente.document.AttributeGroup;
import co.gov.banrep.iconecta.cs.cliente.document.Metadata;
import co.gov.banrep.iconecta.cs.cliente.document.Node;
import co.gov.banrep.iconecta.cs.cliente.user.Member;
import co.gov.banrep.iconecta.cs.cliente.user.User;
import co.gov.banrep.iconecta.cs.cliente.workflow.DataValue;
import co.gov.banrep.iconecta.cs.cliente.workflow.IntegerValue;
import co.gov.banrep.iconecta.cs.cliente.workflow.RowValue;
import co.gov.banrep.iconecta.cs.cliente.workflow.StringValue;
import co.gov.banrep.iconecta.cs.documento.ContenidoDocumento;
import co.gov.banrep.iconecta.cs.usuario.UsuarioCS;
import co.gov.banrep.iconecta.cs.workflow.TipoAtributo;
import co.gov.banrep.iconecta.cs.workflow.ValorAtributo;
import co.gov.banrep.iconecta.cs.workflow.Workflow;
import co.gov.banrep.iconecta.office.documento.DocumentoUtils;
import co.gov.banrep.iconecta.office.documento.entity.Atributo;
import co.gov.banrep.iconecta.office.documento.entity.Coordenada;
import co.gov.banrep.iconecta.office.documento.entity.Empleado;
import co.gov.banrep.iconecta.office.documento.entity.MetadatosPlantilla;
import co.gov.banrep.iconecta.office.documento.entity.Sticker;
import co.gov.banrep.iconecta.ssm.correspondencia.dto.Destino;
import co.gov.banrep.iconecta.ssm.correspondencia.dto.RespuestaEntrega;
import co.gov.banrep.iconecta.ssm.correspondencia.enums.Events;
import co.gov.banrep.iconecta.ssm.correspondencia.enums.States;
import co.gov.banrep.iconecta.ssm.correspondencia.enums.TipoCopiadoDocumento;
import co.gov.banrep.iconecta.ssm.correspondencia.exceptions.ErrorFuncional;
import co.gov.banrep.iconecta.ssm.correspondencia.params.CSCatDocumento;
import co.gov.banrep.iconecta.ssm.correspondencia.params.CategoriaProps;
import co.gov.banrep.iconecta.ssm.correspondencia.params.EstadoCorrespondenciaProps;
import co.gov.banrep.iconecta.ssm.correspondencia.params.GlobalProps;
import co.gov.banrep.iconecta.ssm.correspondencia.params.WorkflowProps;
import co.gov.banrep.iconecta.ssm.correspondencia.params.WsdlProps;
import co.gov.banrep.iconecta.ssm.correspondencia.persist.CentroDistribucion;
import co.gov.banrep.iconecta.ssm.correspondencia.persist.CentroDistribucionRepository;
import co.gov.banrep.iconecta.ssm.correspondencia.persist.Mensaje;
import co.gov.banrep.iconecta.ssm.correspondencia.persist.PasoDistribucion;
import co.gov.banrep.iconecta.ssm.correspondencia.persist.PruebaEntrega;
import co.gov.banrep.iconecta.ssm.correspondencia.persist.PruebaEntregaRepository;
import co.gov.banrep.iconecta.ssm.correspondencia.persist.RutaDistribucion;
import co.gov.banrep.iconecta.ssm.correspondencia.persist.RutaDistribucionRepository;
import co.gov.banrep.iconecta.ssm.correspondencia.persist.StateMachineDataBase;
import co.gov.banrep.iconecta.ssm.correspondencia.ruta.CaculadorRuta;
import co.gov.banrep.iconecta.ssm.correspondencia.ruta.CalculadorRutaFactory;
import co.gov.banrep.iconecta.ssm.correspondencia.ruta.ControladorRutas;
import co.gov.banrep.iconecta.ssm.correspondencia.ruta.Posicion;
import co.gov.banrep.iconecta.ssm.correspondencia.ruta.RutaHandler;
import co.gov.banrep.iconecta.ssm.correspondencia.utils.Consts;
import co.gov.banrep.iconecta.ssm.correspondencia.utils.ControllerCaller;
import co.gov.banrep.iconecta.ssm.correspondencia.utils.SSMUtils;
import co.gov.banrep.iconecta.ssm.correspondencia.xml.RespuestaXmlPdfUtils;
import co.gov.banrep.iconecta.ssm.correspondencia.xml.readXML;


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////                         CONFIGURATION                               ///////////////////////
/////////////////////////////                     ----- SSM CORR -----                            ///////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


/**
 * Clase que contiene toda la configuracion y caminos de la Maquina de Estados de Correspondencia
 *
 */
@Configuration
@EnableAsync
@PropertySource("file:/apps/iconectaFiles_16/correspondencia.properties")//RUTA EN LA QUE SE ALMACENA EL ARCHIVO DE PROPERTIES
public class StateMachineConfig {
	
	//CONFIGURACIÓN DE LA SSM DE CORR
	@Configuration
	@EnableStateMachineFactory
	public static class Config extends EnumStateMachineConfigurerAdapter<States, Events>{
		
		// ----------------------- INICIO DECLARACION DE VARIABLES --------------------------
		private final Logger log = LoggerFactory.getLogger(this.getClass());
		
		private List<Mensaje> mensajes;
		
		private Map<String,String> mapPropsOfficeDoc;
		
		//TODO VARIABLES DE REINTENTO, SE DEBEN AÑADIR DESDE ARCHIVO PROPERTIES
		private static final long tiempoReintentoMilisecs = 1*60*1000; //(#minutos)*(60seg/1min)*(1000miliSeg/1seg)
		private static final long numMaximoReintentos = 5;
		
		@Autowired
		private WsdlProps wsdlsProps;
		
		@Autowired
		private AutenticacionCS autenticacionCS;
		
		@Autowired
		private StateMachinePersister<States, Events, String> stateMachinePersister;
		
		@Autowired
		private GlobalProps globalProperties;
		
		@Autowired
		private WorkflowProps workflowProps;
		
		@Autowired
		private EstadoCorrespondenciaProps estadosProps;
		
		@Autowired
		private CategoriaProps categoriaProps;
		
		@Autowired
		private CSCatDocumento categoriaDocProps;
		
		@Autowired
		private RutaDistribucionRepository rutaDistribucionRepository;
		
		@Autowired
		private CentroDistribucionRepository centroDistribucionRepository;
		
		@Autowired
		private PruebaEntregaRepository repositoryPrueba;
		
		@Autowired
		private  ContextRefreshedListener appContext;
		

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////                   INICIO CONFIGURACION SSM                          ///////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		/**
		 * 
		 */
		@Override
		public void configure(StateMachineConfigurationConfigurer<States, Events> config) throws Exception{
			config
				.withConfiguration()
					.autoStartup(Boolean.TRUE)
					.taskExecutor(taskExecutor())
//					.listener(stateMachineLogListener)
					;
		}
		
		
		/**
		 * 
		 * @return
		 */
		@Bean(name = StateMachineSystemConstants.TASK_EXECUTOR_BEAN_NAME)
		public TaskExecutor taskExecutor(){
			ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
			taskExecutor.setCorePoolSize(10);
			taskExecutor.setMaxPoolSize(30);
			taskExecutor.setQueueCapacity(500);
			taskExecutor.setThreadPriority(Thread.MAX_PRIORITY);
			taskExecutor.initialize();
			//taskExecutor.setAllowCoreThreadTimeOut(true);
//			taskExecutor.setAwaitTerminationSeconds(1200);
//			taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
			
			return taskExecutor;
		}
		
		
		/**
		 * Metodo que permite la configuracion de la definicion de los estados de la SSM
		 */
		@Override
		public void configure(StateMachineStateConfigurer<States, Events> states)
				throws Exception {
			states
				.withStates()
					.initial(States.INICIO)//ACA INICIAN TODAS
					
					
					
					//--------------------------------------------------------------------------------------
					//-----------------                     DER                    -------------------------
					.state(States.INICIO_RADICAR_DER, iniciarRadicarDer(),null)
					.state(States.INICIAR_WF_GENERAR_NUM_RADICADO_DER, iniciarWfGenerarNumRadicadoDer(),null)
					.state(States.INICIAR_WF_FORMULARIO_DER_REMOTO, iniciarWfFormularioDer(),null)
					.state(States.ESPERAR_OBTENER_DATOS_CATEGORIA_FORM_DER, esperarObtenerDatosCatFormDer(),null)
					.state(States.OBTENER_DATOS_CATEGORIA_FORM_DER, obtenerDatosCategoriaFormDer(),null)					
					.state(States.CARGAR_CATEGORIA_FORMULARIO_DER, cargarCategoriaFormularioDer(),null)
					.state(States.VALIDAR_FORMA_ENTREGA_DER, validarFormaEntregaDer(),null)
					.state(States.ESPERAR_DIGITALIZAR_DER, esperarDigitalizarDer(),null)	
					.state(States.CARGAR_CATEGORIA_DOC_DIGITALIZADO_DER, cargarCategoriaDocDigitalizadoDer(),null)
					.state(States.ESPERAR_CALIDAD_DIGITALIZAR_DER, esperarCalidadDigitalizarDer(),null)
					.state(States.SUBIR_DOC_CONSECUTIVO_DER, subirDocConsecutivoDer(),null)
					.state(States.DISTRIBUIR_DER, distribuirDer(),null)
					.state(States.ESPERAR_RESULTADO_DISTRIBUIR_DER, esperarDistribucionDer(),null)
					.state(States.ACTUALIZAR_ESTADO_FINAL_CORR_DER, actualizarEstadoFinalCorrDer(),null)					
					//---------------------------------------------------------------------------------------------
					
					
					
					//---------------------------------------------------------------------------------------------
					//-----------------                      DER EMIAL                    -------------------------	
					.state(States.INICIO_RADICAR_DER_EMAIL, iniciarRadicarDerEmail() ,null)
					//.state(States.INICIAR_WF_GENERAR_NUM_RADICADO_DER, iniciarWfGenerarNumRadicadoDer(),null) // SE HACE EL MISMO LLAMADO AL ESTADO DE DER
					.state(States.CARGAR_CATEGORIA_CORREO_DER, cargarCategoriaCorreoDer() ,null)
					//.state(States.SUBIR_DOC_CONSECUTIVO_DER, subirDocConsecutivoDer(),null) // SE HACE EL MISMO LLAMADO AL ESTADO DE DER
					//.state(States.DISTRIBUIR_DER, distribuirDer(),null) // SE HACE EL MISMO LLAMADO AL ESTADO DE DER
					//.state(States.ESPERAR_RESULTADO_DISTRIBUIR_DER, esperarDistribucionDer(),null) // SE HACE EL MISMO LLAMADO AL ESTADO DE DER
					//.state(States.ACTUALIZAR_ESTADO_FINAL_CORR_DER, actualizarEstadoFinalCorrDer(),null) // SE HACE EL MISMO LLAMADO AL ESTADO DE DER
					//---------------------------------------------------------------------------------------------
					
					
					
					//---------------------------------------------------------------------------------------------
					//-----------------                     CARTA/MEMO                    -------------------------				
					.state(States.CARGAR_PROPS_INICIALES_CA_ME,cargarPropsInicialesCaMe(),persist())
					.state(States.OBTENER_METADATOS_PLANTILLA_CA_ME, obtenerMetadatosPlantillaCaMe(),persist())
					.state(States.CARGAR_METADATOS_PLANTILLA_CA_ME, cargarMetadatosPlantillaCaMe(),persist())
					.state(States.INICIAR_WF_GENERAR_NUM_RADICADO_CA_ME, iniciarWfGenerarNumRadicadoCaMe(),persist())
					.state(States.ACTUALIZAR_RADICADO_DOC_CA_ME, actualizarRadicadoDocCaMe(), persist())
					.state(States.RENOMBRAR_DOC_CS_CA_ME, renombrarDocumentoCSCaMe(),persist())
					.state(States.GENERAR_DOCS_PERSONALIZADOS_CARTA, generarDocsPersonalizadosCarta(),persist())
					.state(States.SUBIR_DOCS_PERSONALIZADOS_CS_CARTA, subirDocsPersonalizadosCSCarta(),persist())
					.state(States.CONVERTIR_UNIR_PDF_CA_ME, convertirUnirPdfCaMe(), persist())
					.state(States.ESPERAR_PROCESO_CA_ME, esperarProcesoCaMe(), persist())
					.state(States.CONSULTAR_CONVERTIR_UNIR_PDF_CA_ME, consultarConvertirUnirPdfCaMe(), persist())
					.state(States.CARGAR_CAT_CORR_CS_CA_ME, cargarCatCorrCSCaMe(), persist())
					.state(States.ENVIAR_FIRMA_CA_ME, enviarFirmaCaMe(), persist())
					.state(States.ESPERAR_RESULTADO_FIRMA_CA_ME, esperarResultadoFirmaCaMe(), null)
					.state(States.EVALUAR_RESULTADO_FIRMA_CA_ME, evaluarResultadoFirmaCaMe(), persist())
					.state(States.DISTRIBUIR_MEMORANDO, distribuirMemorando(),null)
					.state(States.DISTRIBUIR_CARTA, distribuirCarta(),null)
					
					.state(States.FIRMA_CANCELADA_ANULAR_DOC, firmaCanceladaAnularDoc(),null)
					
					///////////////////////////////////////////////////////////////////////////////////////
					//.state(States.OBTENER_DOCUMENTO_CS,obtenerDocumentoCS(),null)
					//.state(States.VERIFICAR_WF_CONFIDENCIAL,verificarWfConfidencial(),null)
					//.state(States.VERIFICAR_VERSION_PLANTILLA_DOC,verificarVersionPlantillaDoc(),null)
					//.state(States.VERIFICAR_ANEXOS,verificarAnexos(),null)
					
					
					//.state(States.CARGAR_CATEGORIA_CS, actualizarMetadatosDocumento(),null)
					
					.state(States.CARGAR_NUEVA_VERSION_DOC_CS, cargarNuevaVersionDocCS(),null)
					
					.state(States.ESPERAR_FIRMA,esperarFirma(),null)
					.state(States.SUBIR_DOC_CONSECUTIVO, subirDocConsecutivo(),null)
					
					//--------------------------------------------------------------------------------------
					
					
					
					
					//---------------------------------------------------------------------------------------------
					//-----------------                       RDI/RDE                     -------------------------				
					.state(States.INICIO_RADICAR_RDI_RDE, iniciarRadicarRdiRde(),null)	
					//--------------------------------------------------------------------------------------
					
					
					
					
					
									
					
					
					
					
					
					
					.state(States.OBTENER_DATOS_CATEGORIA_FORM_RDI_RDE, obtenerDatosCategoriaFormRdiRde(),null)
					
					
					
					
					
					.state(States.GENERAR_DOCUMENTO_FORMULARIO_RDI_RDE, generarDocumentoFormulario(),null)
					
					
					
					
					.state(States.INICIAR_WF_GENERAR_NUM_RADICADO_RDI_RDE, iniciarWfGenerarNumRadicadoRdiRde(),null)
					.state(States.GENERAR_STICKER_RDI_RDE, generarSticker(),null)
					
					.state(States.GENERAR_STICKER_CARTA, generarStickerCarta(),null)
					
					.state(States.CARGAR_CATEGORIA_CS_RDI_RDE, actualizarMetadatosDocumentoRdiRde(),null)
					
					
					
					
					
					
					
					
					//.state(States.DISTRIBUIR_MEMORANDO, distribuir(),null)
					//.state(States.DISTRIBUIR_CARTA, distribuirCarta(),null)
					.state(States.VALIDAR_INICIAR_FORMA_DISTRIBUIR_RDI_RDE, validarIniciarFormaDistribucionRdiRde(),null)
					.state(States.ESPERAR_RESPUESTA_DISTRIBUIR_CDD_RDI_RDE, esperarRespuestaDistribuirCddRdiRde(),null)
					.state(States.OBTENER_RESPUESTA_DISTRIBUIR_CDD_RDI_RDE, obtenerRespuestaDistribuirCddRdiRde(),null)
					.state(States.OBTENER_RESPUESTA_DISTRIBUIR_CDD_CARTA, obtenerRespuestaDistribuirCddCarta(),null)
					.state(States.ESPERAR_RESPUESTA_DISTRIBUIR_CDD_CARTA, esperarRespuestaDistribuirCddCarta(),null)
					.state(States.ESPERAR_RESPUESTA_DISTRIBUIR_CARTA, esperarRespuestaDistribuirCarta(),null)
					.state(States.OBTENER_RESPUESTA_DISTRIBUIR_CARTA, obtenerRespuestaDistribuirCarta(),null)
					.state(States.DISTRIBUIR_RDI_RDE, distribuirRdiRde(),null)
					.state(States.ESPERAR_RESULTADO_DISTRIBUIR,esperarDistribucion(),null)
					.state(States.ESPERAR_RESULTADO_DISTRIBUIR_RDI_RDE,esperarDistribucionRdiRde(),null)
					
					//.state(States.REASIGNAR_RUTA_DER,reasignarRutaDER(),null)
					.state(States.OBTENER_RESULTADO_DISTRIBUIR, obtenerResultadoDistribuir(),null)
					.state(States.OBTENER_RESULTADO_DISTRIBUIR_RDI_RDE, obtenerResultadoDistribuirRdiRde(),null)
					
					//.state(States.OBTENER_RESULTADO_DISTRIBUIR_DER, obtenerResultadoDistribuirDer(),null)
					
					
					//.state(States.DISTRIBUIR_CORREO_DER, distribuirCorreoDER() ,null)
					
					
					
					.state(States.FINALIZADO_ERRORES,notificarError(),null)
					.state(States.FINALIZADO_ERRORES_FUNCIONALES, finErroresFuncionales(),null)
					.state(States.FINALIZADO,registroFinalizado(),null)
//					.history(States.HISTORY, History.SHALLOW)
					
					
					// PARA REINTENTOS (USADO POR TODOS)
					.state(States.ESPERANDO_REINTENTO, verificarReintento(), null)
					
					.end(States.FIN)
					.end(States.FIN_RDI_RDE)	
				    .end(States.FIN_DER);	
			
			
		}
		
		/**
		 * Metodo que permite la configuracion de las transiciones entre los estados de la SSM
		 */
		@Override
		public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
				throws Exception {
			transitions
			//--------------------------------------------------------------------------------------
			//-----------------                     DER                    -------------------------
				//INICIO DER
				.withExternal()
				.source(States.INICIO)
				.target(States.INICIO_RADICAR_DER)
				.event(Events.INICIAR_RADICAR_DER)
				.and()	
				
				//GENERAR NUM RADICADO
				.withExternal()
				.source(States.INICIO_RADICAR_DER)
				.target(States.INICIAR_WF_GENERAR_NUM_RADICADO_DER)
				.and()
				
				//NOTIFICAR NUM RADICADO
				.withExternal()
				.source(States.INICIAR_WF_GENERAR_NUM_RADICADO_DER)
				.target(States.INICIAR_WF_FORMULARIO_DER_REMOTO)
				.event(Events.IR_INICIAR_WF_FORMULARIO_DER_REMOTO)
				.and()				
				
				//ERROR AL GENERAR NUM RADICADO
				.withExternal()
				.source(States.INICIAR_WF_GENERAR_NUM_RADICADO_DER)
				.target(States.FINALIZADO_ERRORES)
				.event(Events.IR_FINALIZADO_ERRORES)					
				.and()
				
				//ESPERAR OBTENER DATOS CATEGORIA FORM DER
				.withExternal()
				.source(States.INICIAR_WF_FORMULARIO_DER_REMOTO)
				.target(States.ESPERAR_OBTENER_DATOS_CATEGORIA_FORM_DER)
				.event(Events.IR_ESPERAR_OBTENER_DATOS_CATEGORIA_FORM_DER)
				.and()
				
				//ERROR AL NOTIFICAR NUM RADICADO
				.withExternal()
				.source(States.INICIAR_WF_FORMULARIO_DER_REMOTO)
				.target(States.FINALIZADO_ERRORES)
				.event(Events.IR_FINALIZADO_ERRORES)					
				.and()
				
				//OBTENER DATOS CAT FORM DER
				.withExternal()
				.source(States.ESPERAR_OBTENER_DATOS_CATEGORIA_FORM_DER)
				.target(States.OBTENER_DATOS_CATEGORIA_FORM_DER)
				.event(Events.IR_OBTENER_DATOS_CATEGORIA_FORM_DER)
				.and()									
				
				//CARGAR CATEGORICA CS DER
				.withExternal()
				.source(States.OBTENER_DATOS_CATEGORIA_FORM_DER)
				.target(States.CARGAR_CATEGORIA_FORMULARIO_DER)
				.event(Events.IR_CARGAR_CATEGORIA_FORMULARIO_DER)
				.and()
				
				//ERROR AL OBTENER DATOS CAT FORM DER
				.withExternal()
				.source(States.OBTENER_DATOS_CATEGORIA_FORM_DER)
				.target(States.FINALIZADO_ERRORES)
				.event(Events.IR_FINALIZADO_ERRORES)					
				.and()
				
				//VALIDAR DESTINATARIOS DER
				.withExternal()
				.source(States.CARGAR_CATEGORIA_FORMULARIO_DER)
				.target(States.VALIDAR_FORMA_ENTREGA_DER)
				.event(Events.IR_VALIDAR_FORMA_ENTREGA_DER)
				.and()
				
				//CARGAR CATEGORICA CS DER
				.withExternal()
				.source(States.CARGAR_CATEGORIA_FORMULARIO_DER)
				.target(States.ESPERANDO_REINTENTO)
				.event(Events.ERROR_REINTENTO)
				.and()		
				
				//CARGAR CATEGORICA CS DER
				.withExternal()
				.source(States.ESPERANDO_REINTENTO)
				.target(States.CARGAR_CATEGORIA_FORMULARIO_DER)
				.event(Events.VOLVER_CARGAR_CATEGORIA_FORMULARIO_DER)
				.and()
				
				//ERROR AL CARGAR CAT CS DER
				.withExternal()
				.source(States.CARGAR_CATEGORIA_FORMULARIO_DER)
				.target(States.FINALIZADO_ERRORES)
				.event(Events.IR_FINALIZADO_ERRORES)					
				.and()
				
				//SUBIR DOC CONSECUTIVO DER
				.withExternal()
				.source(States.VALIDAR_FORMA_ENTREGA_DER)
				.target(States.SUBIR_DOC_CONSECUTIVO_DER)
				.event(Events.IR_DISTRIBUIR_DER)
				.and()	
								
				//ESPERAR DIGITALIZAR DER
				.withExternal()
				.source(States.VALIDAR_FORMA_ENTREGA_DER)
				.target(States.ESPERAR_DIGITALIZAR_DER)
				.event(Events.IR_ESPERAR_DIGITALIZAR_DER)
				.and()
				
				//CARGAR_CATEGORIA_DOC_DIGITALIZADO_DER
				.withExternal()
				.source(States.ESPERAR_DIGITALIZAR_DER)
				.target(States.CARGAR_CATEGORIA_DOC_DIGITALIZADO_DER)
				.event(Events.IR_CARGAR_CATEGORIA_DOC_DIGITALIZADO_DER)
				.and()
				
				//ESPERAR_CALIDAD_DIGITALIZAR_DER
				.withExternal()
				.source(States.CARGAR_CATEGORIA_DOC_DIGITALIZADO_DER)
				.target(States.ESPERAR_CALIDAD_DIGITALIZAR_DER)
				.event(Events.IR_ESPERAR_CALIDAD_DIGITALIZAR_DER)
				.and()
				
				//ESPERANDO_REINTENTO
				.withExternal()
				.source(States.CARGAR_CATEGORIA_DOC_DIGITALIZADO_DER)
				.target(States.ESPERANDO_REINTENTO)
				.event(Events.ERROR_REINTENTO)
				.and()		
				
				//CARGAR_CATEGORIA_DOC_DIGITALIZADO_DER
				.withExternal()
				.source(States.ESPERANDO_REINTENTO)
				.target(States.CARGAR_CATEGORIA_DOC_DIGITALIZADO_DER)
				.event(Events.VOLVER_CARGAR_CATEGORIA_DOC_DIGITALIZADO_DER)
				.and()
				
				//ERROR AL CARGAR_CATEGORIA_DOC_DIGITALIZADO_DER
				.withExternal()
				.source(States.CARGAR_CATEGORIA_DOC_DIGITALIZADO_DER)
				.target(States.FINALIZADO_ERRORES)
				.event(Events.IR_FINALIZADO_ERRORES)					
				.and()
				

				//SUBIR_DOC_CONSECUTIVO_DER
				.withExternal()
				.source(States.ESPERAR_CALIDAD_DIGITALIZAR_DER)
				.target(States.SUBIR_DOC_CONSECUTIVO_DER)
				.event(Events.IR_DISTRIBUIR_DER)
				.and()	
				
				//DISTRIBUIR_DER
				.withExternal()
				.source(States.SUBIR_DOC_CONSECUTIVO_DER)
				.target(States.DISTRIBUIR_DER)
				.and()
				
				//ESPERAR_RESULTADO_DISTRIBUIR_DER
				.withExternal()
				.source(States.DISTRIBUIR_DER)
				.target(States.ESPERAR_RESULTADO_DISTRIBUIR_DER)
				.event(Events.IR_ESPERAR_RESULTADO_DISTRIBUIR_DER)
				.and()
				
				//ESPERANDO_REINTENTO
				.withExternal()
				.source(States.DISTRIBUIR_DER)
				.target(States.ESPERANDO_REINTENTO)
				.event(Events.ERROR_REINTENTO)
				.and()	
				
				//DISTRIBUIR_DER
				.withExternal()
				.source(States.ESPERANDO_REINTENTO)
				.target(States.DISTRIBUIR_DER)
				.event(Events.VOLVER_DISTRIBUIR_DER)
				.and()	
				
				//ERROR AL DISTRIBUIR_DER
				.withExternal()
				.source(States.DISTRIBUIR_DER)
				.target(States.FINALIZADO_ERRORES)
				.event(Events.IR_FINALIZADO_ERRORES)					
				.and()
				
				//ERROR AL ESPERAR_RESULTADO_DISTRIBUIR_DER
				.withExternal()
				.source(States.ESPERAR_RESULTADO_DISTRIBUIR_DER)
				.target(States.FINALIZADO_ERRORES)
				.event(Events.IR_FINALIZADO_ERRORES)
				.and()
				
				//ACTUALIZAR_ESTADO_FINAL_CORR_DER
				.withExternal()
				.source(States.ESPERAR_RESULTADO_DISTRIBUIR_DER)
				.target(States.ACTUALIZAR_ESTADO_FINAL_CORR_DER)
				.event(Events.IR_ACTUALIZAR_ESTADO_FINAL_CORR_DER)
				.and()
				
				//ACTUALIZAR_ESTADO_FINAL_CORR_DER
				.withExternal()
				.source(States.ACTUALIZAR_ESTADO_FINAL_CORR_DER)
				.target(States.ACTUALIZAR_ESTADO_FINAL_CORR_DER)
				.event(Events.IR_ACTUALIZAR_ESTADO_FINAL_CORR_DER)
				.and()
				
				//ACTUALIZAR_ESTADO_FINAL_CORR_DER
				.withExternal()
				.source(States.ACTUALIZAR_ESTADO_FINAL_CORR_DER)
				.target(States.FIN_DER)
				.event(Events.FINALIZAR_DER)
				.and()
			//---------------------------------------------------------------------------------------------
				
				
				
			//---------------------------------------------------------------------------------------------
			//-----------------                     DER - EMAIL                    -------------------------
				//INICIO DER-EMAIL
				.withExternal()
				.source(States.INICIO)
				.target(States.INICIO_RADICAR_DER_EMAIL)
				.event(Events.INICIAR_RADICAR_DER_EMAIL)
				.and()
				
				//GENERAR NUM RADICADO
				.withExternal()
				.source(States.INICIO_RADICAR_DER_EMAIL)
				.target(States.INICIAR_WF_GENERAR_NUM_RADICADO_DER)
				.and()
				
				//CARGAR CATEGORICA CS DER
				.withExternal()
				.source(States.INICIAR_WF_GENERAR_NUM_RADICADO_DER)
				.target(States.CARGAR_CATEGORIA_CORREO_DER)
				.event(Events.IR_CARGAR_CATEGORIA_CORREO_DER)
				.and()
				
				//ESPERANDO REINTENTO
				.withExternal()
				.source(States.CARGAR_CATEGORIA_CORREO_DER)
				.target(States.ESPERANDO_REINTENTO)
				.event(Events.ERROR_REINTENTO)
				.and()		
				
				//CARGAR CATEGORICA CS DER
				.withExternal()
				.source(States.ESPERANDO_REINTENTO)
				.target(States.CARGAR_CATEGORIA_CORREO_DER)
				.event(Events.VOLVER_CARGAR_CATEGORIA_CORREO_DER)
				.and()
				
				//ERROR AL CARGAR CAT CS DER
				.withExternal()
				.source(States.CARGAR_CATEGORIA_CORREO_DER)
				.target(States.FINALIZADO_ERRORES)
				.event(Events.IR_FINALIZADO_ERRORES)					
				.and()
				
				//SUBIR DOC CONSECUTIVO DER
				.withExternal()
				.source(States.CARGAR_CATEGORIA_CORREO_DER)
				.target(States.SUBIR_DOC_CONSECUTIVO_DER)
				.event(Events.IR_DISTRIBUIR_DER)					
				.and()					
			 //---------------------------------------------------------------------------------------------
					
				
				
				
				
				
		    //---------------------------------------------------------------------------------------------
			//-----------------                     CARTA/MEMO                    -------------------------
				//CARGAR PROPERTIES
				.withExternal()
				.source(States.INICIO)
				.target(States.CARGAR_PROPS_INICIALES_CA_ME)
				.event(Events.INICIAR_RADICAR_CA_ME)
				.and()
				
				//OBTENER_METADATOS_PLANTILLA_CA_ME
				.withExternal()
				.source(States.CARGAR_PROPS_INICIALES_CA_ME)
				.target(States.OBTENER_METADATOS_PLANTILLA_CA_ME)
				.and()
				
				//ERROR AL OBTENER_METADATOS_PLANTILLA_CA_ME
				.withExternal()
				.source(States.OBTENER_METADATOS_PLANTILLA_CA_ME)
				.target(States.FINALIZADO_ERRORES)
				.event(Events.IR_FINALIZADO_ERRORES)					
				.and()
				
				//INICIAR_RADICAR_CA_ME WF GENERAR NUM RADICADO
				.withExternal()
				.source(States.OBTENER_METADATOS_PLANTILLA_CA_ME)
				.target(States.CARGAR_METADATOS_PLANTILLA_CA_ME)
				.event(Events.IR_CARGAR_METADATOS_PLANTILLA_CA_ME)
				.and()
				
				//INICIAR_RADICAR_CA_ME WF GENERAR NUM RADICADO
				.withExternal()
				.source(States.CARGAR_METADATOS_PLANTILLA_CA_ME)
				.target(States.INICIAR_WF_GENERAR_NUM_RADICADO_CA_ME)
				.and()
				
				//ERROR AL INICIAR_WF_GENERAR_NUM_RADICADO_CA_ME
				.withExternal()
				.source(States.INICIAR_WF_GENERAR_NUM_RADICADO_CA_ME)
				.target(States.FINALIZADO_ERRORES)
				.event(Events.IR_FINALIZADO_ERRORES)					
				.and()
				
				//ACTUALIZAR RADICADO DOC
				.withExternal()
				.source(States.INICIAR_WF_GENERAR_NUM_RADICADO_CA_ME)
				.target(States.ACTUALIZAR_RADICADO_DOC_CA_ME)
				.event(Events.IR_ACTUALIZAR_RADICADO_DOC_CA_ME)
				.and()
				
				//ERROR AL ACTUALIZAR RADICADO DOC
				.withExternal()
				.source(States.ACTUALIZAR_RADICADO_DOC_CA_ME)
				.target(States.FINALIZADO_ERRORES)
				.event(Events.IR_FINALIZADO_ERRORES)					
				.and()
				
				//RENOMBRAR DOC CS
				.withExternal()
				.source(States.ACTUALIZAR_RADICADO_DOC_CA_ME)
				.target(States.RENOMBRAR_DOC_CS_CA_ME)
				.event(Events.IR_RENOMBRAR_DOC_CS_CA_ME)
				.and()
				
				//ERROR AL RENOMBRAR DOC CS
				.withExternal()
				.source(States.RENOMBRAR_DOC_CS_CA_ME)
				.target(States.FINALIZADO_ERRORES)
				.event(Events.IR_FINALIZADO_ERRORES)					
				.and()
				
				//GENERAR_DOCS_PERSONALIZADOS_CARTA
				.withExternal()
				.source(States.RENOMBRAR_DOC_CS_CA_ME)
				.target(States.GENERAR_DOCS_PERSONALIZADOS_CARTA)
				.event(Events.IR_GENERAR_DOCS_PERSONALIZADOS_CARTA)
				.and()
				
				//ERROR AL GENERAR_DOCS_PERSONALIZADOS_CARTA
				.withExternal()
				.source(States.GENERAR_DOCS_PERSONALIZADOS_CARTA)
				.target(States.FINALIZADO_ERRORES)
				.event(Events.IR_FINALIZADO_ERRORES)
				.and()
				
				//ERROR AL GENERAR_DOCS_PERSONALIZADOS_CARTA
				.withExternal()
				.source(States.GENERAR_DOCS_PERSONALIZADOS_CARTA)
				.target(States.SUBIR_DOCS_PERSONALIZADOS_CS_CARTA)
				.event(Events.IR_SUBIR_DOCS_PERSONALIZADOS_CS_CARTA)
				.and()
				
				//ERROR AL SUBIR_DOCS_PERSONALIZADOS_CS_CARTA
				.withExternal()
				.source(States.SUBIR_DOCS_PERSONALIZADOS_CS_CARTA)
				.target(States.FINALIZADO_ERRORES)
				.event(Events.IR_FINALIZADO_ERRORES)
				.and()				
				
				//CONVERTIR_UNIR_PDF_CA_ME
				.withExternal()
				.source(States.SUBIR_DOCS_PERSONALIZADOS_CS_CARTA)
				.target(States.CONVERTIR_UNIR_PDF_CA_ME)
				.event(Events.IR_CONVERTIR_UNIR_PDF_CA_ME)
				.and()
				
				//CONVERTIR_UNIR_PDF_CA_ME
				.withExternal()
				.source(States.RENOMBRAR_DOC_CS_CA_ME)
				.target(States.CONVERTIR_UNIR_PDF_CA_ME)
				.event(Events.IR_CONVERTIR_UNIR_PDF_CA_ME)
				.and()
				
				//ERROR AL CONVERTIR_UNIR_PDF_CA_ME
				.withExternal()
				.source(States.CONVERTIR_UNIR_PDF_CA_ME)
				.target(States.FINALIZADO_ERRORES)
				.event(Events.IR_FINALIZADO_ERRORES)
				.and()
				
				//ESPERAR_PROCESO_CA_ME
				.withExternal()
				.source(States.CONVERTIR_UNIR_PDF_CA_ME)
				.target(States.ESPERAR_PROCESO_CA_ME)
				.event(Events.IR_ESPERAR_PROCESO_CA_ME)
				.and()
				
				//CONSULTAR_CONVERTIR_UNIR_PDF_CA_ME
				.withExternal()
				.source(States.ESPERAR_PROCESO_CA_ME)
				.target(States.CONSULTAR_CONVERTIR_UNIR_PDF_CA_ME)
				.timer(tiempoReintentoMilisecs)
				.and()
				
				//ESPERAR_PROCESO_CA_ME
				.withExternal()
				.source(States.CONSULTAR_CONVERTIR_UNIR_PDF_CA_ME)
				.target(States.ESPERAR_PROCESO_CA_ME)
				.event(Events.IR_ESPERAR_PROCESO_CA_ME)
				.and()
				
				//CARGAR_CAT_CORR_CS_CA_ME
				.withExternal()
				.source(States.CONSULTAR_CONVERTIR_UNIR_PDF_CA_ME)
				.target(States.CARGAR_CAT_CORR_CS_CA_ME)
				.event(Events.IR_CARGAR_CAT_CORR_CS_CA_ME)
				.and()
				
				//ERROR AL CARGAR_CAT_CORR_CS_CA_ME
				.withExternal()
				.source(States.CARGAR_CAT_CORR_CS_CA_ME)
				.target(States.FINALIZADO_ERRORES)
				.event(Events.IR_FINALIZADO_ERRORES)
				.and()
				
				//ENVIAR_FIRMA_CA_ME
				.withExternal()
				.source(States.CARGAR_CAT_CORR_CS_CA_ME)
				.target(States.ENVIAR_FIRMA_CA_ME)
				.event(Events.IR_ENVIAR_FIRMA_CA_ME)
				.and()
				
				//ERROR AL ENVIAR_FIRMA_CA_ME
				.withExternal()
				.source(States.ENVIAR_FIRMA_CA_ME)
				.target(States.FINALIZADO_ERRORES)
				.event(Events.IR_FINALIZADO_ERRORES)
				.and()
				
				//ESPERAR_RESULTADO_FIRMA_CA_ME
				.withExternal()
				.source(States.ENVIAR_FIRMA_CA_ME)
				.target(States.ESPERAR_RESULTADO_FIRMA_CA_ME)
				.event(Events.IR_ESPERAR_RESULTADO_FIRMA_CA_ME)
				.and()				
				
				//EVALUAR_RESULTADO_FIRMA_CA_ME
				.withExternal()
				.source(States.ESPERAR_RESULTADO_FIRMA_CA_ME)
				.target(States.EVALUAR_RESULTADO_FIRMA_CA_ME)
				.event(Events.IR_EVALUAR_RESULTADO_FIRMA_CA_ME)
				.and()
				
				//FIRMA CANCELADA ANULAR DOC
				.withExternal()
				.source(States.EVALUAR_RESULTADO_FIRMA_CA_ME)
				.target(States.FIRMA_CANCELADA_ANULAR_DOC)
				.event(Events.FIRMA_CANCELADA)
				.and()
				
				//DISTRIBUIR_MEMORANDO_E
				.withExternal()
				.source(States.EVALUAR_RESULTADO_FIRMA_CA_ME)
				.target(States.DISTRIBUIR_MEMORANDO)
				.event(Events.IR_DISTRIBUIR_MEMORANDO)
				.and()
				
				.withExternal()
				.source(States.DISTRIBUIR_MEMORANDO)
				.target(States.ESPERAR_RESULTADO_DISTRIBUIR)
				.and()
				
			.withExternal()
				.source(States.DISTRIBUIR_MEMORANDO)
				.target(States.ESPERANDO_REINTENTO)
				.event(Events.ERROR_REINTENTO)
				.and()
				
				.withExternal()
				.source(States.ESPERANDO_REINTENTO)
				.target(States.DISTRIBUIR_MEMORANDO)
				.event(Events.IR_INTENTAR_DISTRIBUIR)
				.and()
				
			.withExternal()
				.source(States.DISTRIBUIR_MEMORANDO)
				.target(States.FINALIZADO_ERRORES)
				.event(Events.IR_FINALIZADO_ERRORES)					
				.and()
				
				
				//GENERAR_STICKER_CARTA
				.withExternal()
				.source(States.EVALUAR_RESULTADO_FIRMA_CA_ME)
				.target(States.GENERAR_STICKER_CARTA)
				.event(Events.IR_GENERAR_STICKER_CARTA)
				.and()
				
				
				//ERROR AL GENERAR_STICKER_CARTA
				.withExternal()
				.source(States.GENERAR_STICKER_CARTA)
				.target(States.FINALIZADO_ERRORES)
				.event(Events.IR_FINALIZADO_ERRORES)					
				.and()
				
				//SUBIR DOC CONSECUTIVO
				.withExternal()
				.source(States.GENERAR_STICKER_CARTA)
				.target(States.SUBIR_DOC_CONSECUTIVO)
				.event(Events.IR_SUBIR_DOC_CONSECUTIVO)
				.and()
				
				//--HASTA ACÁ VA EL REDISEÑO
				
				
				
				
				////////////////////////////////////////////////////////////
				/*
				//OBTENER DOC CS
				.withExternal()
				.source(States.CARGAR_PROPS_INICIALES_CA_ME)
				.target(States.OBTENER_DOCUMENTO_CS)
				.and()
				
				//VERIFICAR WF CONFIDENCIAL
				.withExternal()
				.source(States.OBTENER_DOCUMENTO_CS)
				.target(States.VERIFICAR_WF_CONFIDENCIAL)
				.event(Events.DOCUMENTO_OBTENIDO_CS)
				.and()
				
				//ERROR AL OBTENER DOC CS
				.withExternal()
				.source(States.OBTENER_DOCUMENTO_CS)
				.target(States.FINALIZADO_ERRORES)
				.event(Events.IR_FINALIZADO_ERRORES)					
				.and()
				
				//ERROR FUNCIONAL AL OBTENER DOC CS
				.withExternal()
				.source(States.OBTENER_DOCUMENTO_CS)
				.target(States.FINALIZADO_ERRORES_FUNCIONALES)
				.event(Events.IR_FINALIZADO_ERRORES_FUNCIONALES)					
				.and()
				
				//VERIFICAR VERSION PLANTILLA DOC
				.withExternal()
				.source(States.VERIFICAR_WF_CONFIDENCIAL)
				.target(States.VERIFICAR_VERSION_PLANTILLA_DOC)
				.event(Events.IR_VERIFICAR_VERSION_PLANTILLA)
				.and()
				
				//ERROR AL VERIFICAR WF CONFIDENCIAL
				.withExternal()
				.source(States.VERIFICAR_WF_CONFIDENCIAL)
				.target(States.FINALIZADO_ERRORES)
				.event(Events.IR_FINALIZADO_ERRORES)					
				.and()
				
				//ERROR AL VERIFICAR WF CONFIDENCIAL
				.withExternal()
				.source(States.VERIFICAR_WF_CONFIDENCIAL)
				.target(States.FINALIZADO_ERRORES_FUNCIONALES)
				.event(Events.IR_FINALIZADO_ERRORES_FUNCIONALES)					
				.and()
				
				//VERIFICAR ANEXOS
				.withExternal()
				.source(States.VERIFICAR_VERSION_PLANTILLA_DOC)
				.target(States.VERIFICAR_ANEXOS)
				.and()
				
				//ERROR AL VERIFICAR VERSION PLANTILLA DOC
				.withExternal()
				.source(States.VERIFICAR_VERSION_PLANTILLA_DOC)
				.target(States.FINALIZADO_ERRORES)
				.event(Events.IR_FINALIZADO_ERRORES)					
				.and()
				
				
				//OBTENER DATOS CATEGORIA DOC
				.withExternal()
				.source(States.VERIFICAR_ANEXOS)
				.target(States.OBTENER_DATOS_CATEGORIA_DOC_CA_ME)
				.and()
				
				
				//ERROR AL VERIFICAR ANEXOS
				.withExternal()
				.source(States.VERIFICAR_ANEXOS)
				.target(States.FINALIZADO_ERRORES)
				.event(Events.IR_FINALIZADO_ERRORES)
				.and()
				
				//CARGAR CATEGORIA CS
				.withExternal()
				.source(States.INICIAR_WF_GENERAR_NUM_RADICADO_CA_ME)
				.target(States.CARGAR_CATEGORIA_CS)
				.and()
				
				//ERROR AL CARGAR CATEGORIA CA
				.withExternal()
				.source(States.CARGAR_CATEGORIA_CS)
				.target(States.FINALIZADO_ERRORES)
				.event(Events.IR_FINALIZADO_ERRORES)					
				.and()
				
				//CARGAR NUEVA VERSION DOC CS
				.withExternal()
				.source(States.PERSONALIZAR_DOCUMENTO_CARTA)
				.target(States.CARGAR_NUEVA_VERSION_DOC_CS)
				.and()
				
				*/
				
				
				
				
				
				//ERROR AL CARGAR NUEVA VERSION DOC CS
				.withExternal()
				.source(States.CARGAR_NUEVA_VERSION_DOC_CS)
				.target(States.FINALIZADO_ERRORES)
				.event(Events.IR_FINALIZADO_ERRORES)					
				.and()
				
				/*
				//ESPERAR FIRMA
				.withExternal()
				.source(States.RENOMBRAR_DOC_CS_CA_ME)
				.target(States.ESPERAR_FIRMA)
				.event(Events.IR_ESPERAR_FIRMA)
				.and()
				
				//SUBIR DOC CONSECUTIVO
				.withExternal()
				.source(States.RENOMBRAR_DOC_CS_CA_ME)
				.target(States.SUBIR_DOC_CONSECUTIVO)
				.event(Events.IR_SUBIR_DOC_CONSECUTIVO)
				.and()
				*/
				
				
				/*
				//DISTRIBUIR_MEMORANDO
				.withExternal()
				.source(States.ESPERAR_FIRMA)
				.target(States.DISTRIBUIR_MEMORANDO)
				.event(Events.FIRMA_FINALIZADO_OK)
				.and()
				
				//GENERAR STICKER CARTA
				.withExternal()
				.source(States.ESPERAR_FIRMA)
				.target(States.GENERAR_STICKER_CARTA)
				.event(Events.FIRMA_FINALIZADO_OK_CARTA)
				.and()
				
				//FIRMA CANCELADA ANULAR DOC
				.withExternal()
				.source(States.ESPERAR_FIRMA)
				.target(States.FIRMA_CANCELADA_ANULAR_DOC)
				.event(Events.FIRMA_CANCELADA)
				.and()

				//RENOMBRAR DOC CS
				.withExternal()
				.source(States.GENERAR_STICKER_CARTA)
				.target(States.RENOMBRAR_DOC_CS_CA_ME)
				.and()
				*/
			
			.withExternal()
				.source(States.SUBIR_DOC_CONSECUTIVO)
				.target(States.DISTRIBUIR_CARTA)
				.and()
			.withExternal()
				.source(States.DISTRIBUIR_CARTA)
				.target(States.ESPERAR_RESPUESTA_DISTRIBUIR_CDD_CARTA)
				.event(Events.IR_ESPERAR_RESPUESTA_DISTRIBUIR_CDD_CARTA)
				.and()
			.withExternal()
				.source(States.DISTRIBUIR_CARTA)
				.target(States.ESPERAR_RESPUESTA_DISTRIBUIR_CARTA)
				.event(Events.IR_ESPERAR_RESPUESTA_DISTRIBUIR_CARTA)
				.and()
			.withExternal()
				.source(States.ESPERAR_RESPUESTA_DISTRIBUIR_CDD_CARTA)
				.target(States.OBTENER_RESPUESTA_DISTRIBUIR_CDD_CARTA)
				.event(Events.IR_OBTENER_RESPUESTA_DISTRIBUIR_CDD_CARTA)
				.and()
			.withExternal()
				.source(States.OBTENER_RESPUESTA_DISTRIBUIR_CDD_CARTA)
				.target(States.ESPERAR_RESPUESTA_DISTRIBUIR_CARTA)
				.and()
				
			.withExternal()
				.source(States.ESPERAR_RESPUESTA_DISTRIBUIR_CARTA)
				.target(States.OBTENER_RESPUESTA_DISTRIBUIR_CARTA)
				.event(Events.IR_OBTENER_RESPUESTA_DISTRIBUIR_CARTA)
				.and()
				
				
			.withExternal()
				.source(States.FIRMA_CANCELADA_ANULAR_DOC)
				.target(States.FIN)
				.and()
				
		
			

			.withExternal()
				.source(States.ESPERAR_RESULTADO_DISTRIBUIR)
				.target(States.OBTENER_RESULTADO_DISTRIBUIR)
				.event(Events.DISTRIBUCION_FINALIZADA)
				.and()
			.withExternal()
				.source(States.OBTENER_RESULTADO_DISTRIBUIR)
				.target(States.ESPERAR_RESULTADO_DISTRIBUIR)
				.event(Events.VOLVER_ESPERAR_DISTRIBUIR)
				.and()
								
			.withExternal()
				.source(States.OBTENER_RESPUESTA_DISTRIBUIR_CARTA)
				.target(States.ESPERAR_RESPUESTA_DISTRIBUIR_CARTA)
				.and()
				
			.withExternal()
				.source(States.ESPERAR_RESPUESTA_DISTRIBUIR_CARTA)
				.target(States.FINALIZADO)
				.event(Events.FINALIZAR)
				.and()
				
			.withExternal()
				.source(States.DISTRIBUIR_CARTA)
				.target(States.FINALIZADO)
				.event(Events.FINALIZAR)
				.and()
				
			//Inicio Estados reintento	
			
			//Distribuir	
				
		
				
			.withExternal()
				.source(States.ESPERAR_RESULTADO_DISTRIBUIR)
				.target(States.ESPERANDO_REINTENTO)
				.event(Events.ERROR_REINTENTO)					
				.and()
			.withExternal()
				.source(States.ESPERAR_RESULTADO_DISTRIBUIR)
				.target(States.ESPERANDO_REINTENTO)
				.event(Events.IR_INTENTAR_DISTRIBUIR)
				.and()			

			.withExternal()
				.source(States.ESPERAR_RESULTADO_DISTRIBUIR)
				.target(States.FINALIZADO_ERRORES)
				.event(Events.NUMERO_MAXIMO_REINTENTOS_ALCANZADO)
				.and()
				
			
			
			//Fin Distribuir	
				
			//Obtener resultado distribuir
				
			.withExternal()
				.source(States.OBTENER_RESULTADO_DISTRIBUIR)
				.target(States.ESPERANDO_REINTENTO)
				.event(Events.ERROR_REINTENTO)
				.and()
			.withExternal()
				.source(States.ESPERANDO_REINTENTO)
				.target(States.OBTENER_RESULTADO_DISTRIBUIR)
				.event(Events.IR_INTENTAR_OBTENER_RESULTADO_DISTRIBUIR)
				.and()	
			.withExternal()
				.source(States.ESPERAR_RESULTADO_DISTRIBUIR_RDI_RDE)
				.target(States.FINALIZADO_ERRORES)
				.event(Events.NUMERO_MAXIMO_REINTENTOS_ALCANZADO)
				.and()
				
			//---------------------------------------------------------------------------------------------
				
				
				
				
				
				
			//---------------------------------------------------------------------------------------------
		    //-----------------                     RDI/RDE                    -------------------------			
				.withExternal()
					.source(States.INICIO)
					.target(States.INICIO_RADICAR_RDI_RDE)
					.event(Events.INICIAR_RADICAR_RDI_RDE)
					.and()	
					
					
					
			//---------------------------------------------------------------------------------------------
				
					
				
					
				
					
				.withExternal()
					.source(States.FINALIZADO)
					.target(States.FIN)
					.and()					
		
			
				
					
					
				
					
				
					

				
				
					
				
					
				
								
				
					
				
					
				
					
				
					
				
				
					
					
					
				
				
				
					
				.withExternal()
					.source(States.INICIO_RADICAR_RDI_RDE)
					.target(States.OBTENER_DATOS_CATEGORIA_FORM_RDI_RDE)
					.and()
				.withExternal()
					.source(States.OBTENER_DATOS_CATEGORIA_FORM_RDI_RDE)
					.target(States.GENERAR_DOCUMENTO_FORMULARIO_RDI_RDE)
					.and()
				.withExternal()
					.source(States.GENERAR_DOCUMENTO_FORMULARIO_RDI_RDE)
					.target(States.INICIAR_WF_GENERAR_NUM_RADICADO_RDI_RDE)
					.and()
				.withExternal()
					.source(States.INICIAR_WF_GENERAR_NUM_RADICADO_RDI_RDE)
					.target(States.GENERAR_STICKER_RDI_RDE)
					.and()
				.withExternal()
					.source(States.GENERAR_STICKER_RDI_RDE)
					.target(States.CARGAR_CATEGORIA_CS_RDI_RDE)
					.event(Events.INICIAR_CARGAR_CATEGORIA_RDI_RDE)
					.and()
				.withExternal()
					.source(States.CARGAR_CATEGORIA_CS_RDI_RDE)
					.target(States.VALIDAR_INICIAR_FORMA_DISTRIBUIR_RDI_RDE)
					.event(Events.IR_VALIDAR_INICIAR_FORMA_DISTRIBUIR_RDI_RDE)
					.and()	
				.withExternal()
					.source(States.VALIDAR_INICIAR_FORMA_DISTRIBUIR_RDI_RDE)
					.target(States.ESPERAR_RESPUESTA_DISTRIBUIR_CDD_RDI_RDE)
					.event(Events.IR_ESPERAR_RESPUESTA_DISTRIBUIR_CDD_RDI_RDE)
					.and()
				.withExternal()
					.source(States.VALIDAR_INICIAR_FORMA_DISTRIBUIR_RDI_RDE)
					.target(States.ESPERAR_RESULTADO_DISTRIBUIR_RDI_RDE)
					.event(Events.IR_ESPERAR_RESPUESTA_DISTRIBUIR_RDI_RDE)
					.and()
				.withExternal()
					.source(States.ESPERAR_RESPUESTA_DISTRIBUIR_CDD_RDI_RDE)
					.target(States.OBTENER_RESPUESTA_DISTRIBUIR_CDD_RDI_RDE)
					.event(Events.IR_OBTENER_RESPUESTA_DISTRIBUIR_CDD_RDI_RDE)
					.and()
				.withExternal()
					.source(States.OBTENER_RESPUESTA_DISTRIBUIR_CDD_RDI_RDE)
					.target(States.ESPERAR_RESULTADO_DISTRIBUIR_RDI_RDE)
					.and()
				.withExternal()
					.source(States.OBTENER_RESULTADO_DISTRIBUIR_RDI_RDE)
					.target(States.FINALIZADO)
					.event(Events.FINALIZAR_RDI_RDE)
					.and()
				.withExternal()
					.source(States.ESPERAR_RESULTADO_DISTRIBUIR_RDI_RDE)
					.target(States.OBTENER_RESULTADO_DISTRIBUIR_RDI_RDE)
					.event(Events.OBTENIENDO_RESULTADO_DISTRIBUIR_RDI_RDE)
					.and()
				.withExternal()
					.source(States.ESPERAR_RESULTADO_DISTRIBUIR_RDI_RDE)
					.target(States.FINALIZADO_ERRORES)
					.event(Events.IR_FINALIZADO_ERRORES)
					.and()
				.withExternal()
					.source(States.OBTENER_RESULTADO_DISTRIBUIR_RDI_RDE)
					.target(States.ESPERAR_RESULTADO_DISTRIBUIR_RDI_RDE)
					.event(Events.VOLVER_ESPERAR_DISTRIBUIR_RDI_RDE)
					.and()
				
				
	
				
				//Fin Obtener resultado distribuir
					
				//validarIniciarFormaDistribucionRdiRde
				
				.withExternal()
					.source(States.ESPERAR_RESPUESTA_DISTRIBUIR_CDD_RDI_RDE)
					.target(States.ESPERANDO_REINTENTO)
					.event(Events.ERROR_REINTENTO)
					.and()
				.withExternal()
					.source(States.ESPERANDO_REINTENTO)
					.target(States.VALIDAR_INICIAR_FORMA_DISTRIBUIR_RDI_RDE)
					.event(Events.IR_VALIDAR_INICIAR_FORMA_DISTRIBUIR_RDI_RDE_REINTENTO)
					.and()	
					
				.withExternal()
					.source(States.ESPERAR_RESPUESTA_DISTRIBUIR_CDD_RDI_RDE)
					.target(States.FINALIZADO_ERRORES)
					.event(Events.NUMERO_MAXIMO_REINTENTOS_ALCANZADO)
					.and()	
					
				.withExternal()
					.source(States.ESPERANDO_REINTENTO)
					.target(States.VALIDAR_INICIAR_FORMA_DISTRIBUIR_RDI_RDE)
					.event(Events.IR_VALIDAR_INICIAR_FORMA_DISTRIBUIR_RDI_RDE)
					.and()
				.withExternal()
					.source(States.VALIDAR_INICIAR_FORMA_DISTRIBUIR_RDI_RDE)
					.target(States.ESPERANDO_REINTENTO)
					.event(Events.ERROR_REINTENTO)
					.and()
					
				//Fin validarIniciarFormaDistribucionRdiRde	
					
				//Inicio obtenerRespuestaDistribuirCddRdiRde
					
				.withExternal()
					.source(States.ESPERAR_RESULTADO_DISTRIBUIR_RDI_RDE)
					.target(States.ESPERANDO_REINTENTO)
					.event(Events.ERROR_REINTENTO)
					.and()			
				.withExternal()
					.source(States.ESPERANDO_REINTENTO)
					.target(States.OBTENER_RESPUESTA_DISTRIBUIR_CDD_RDI_RDE)
					.event(Events.VOLVER_OBTENER_RESPUESTA_DISTRIBUIR_CDD_RDI_RDE)
					.and()
				.withExternal()
					.source(States.ESPERAR_RESULTADO_DISTRIBUIR_RDI_RDE)
					.target(States.FINALIZADO_ERRORES)
					.event(Events.NUMERO_MAXIMO_REINTENTOS_ALCANZADO)
					.and()	
				.withExternal()
					.source(States.OBTENER_RESPUESTA_DISTRIBUIR_CDD_RDI_RDE)
					.target(States.ESPERANDO_REINTENTO)
					.event(Events.ERROR_REINTENTO)
					.and()
					
				//Fin obtenerRespuestaDistribuirCddRdiRde	
					
				//obtenerResultadoDistribuirRdiRde
				
				.withExternal()
					.source(States.OBTENER_RESULTADO_DISTRIBUIR_RDI_RDE)
					.target(States.ESPERANDO_REINTENTO)
					.event(Events.ERROR_REINTENTO)
					.and()
				.withExternal()
					.source(States.ESPERAR_RESULTADO_DISTRIBUIR_RDI_RDE)
					.target(States.ESPERANDO_REINTENTO)
					.event(Events.ERROR_REINTENTO)
					.and()
				.withExternal()
					.source(States.ESPERANDO_REINTENTO)
					.target(States.OBTENER_RESULTADO_DISTRIBUIR_RDI_RDE)
					.event(Events.VOLVER_OBTENIENDO_RESULTADO_DISTRIBUIR_RDI_RDE)
					.and()	
				.withExternal()
					.source(States.ESPERAR_RESULTADO_DISTRIBUIR_RDI_RDE)
					.target(States.OBTENER_RESULTADO_DISTRIBUIR_RDI_RDE)
					.event(Events.VOLVER_OBTENIENDO_RESULTADO_DISTRIBUIR_RDI_RDE)
					.and()	
					
				.withExternal()
					.source(States.ESPERAR_RESULTADO_DISTRIBUIR_RDI_RDE)
					.target(States.FINALIZADO_ERRORES)
					.event(Events.NUMERO_MAXIMO_REINTENTOS_ALCANZADO)
					.and()
					
				//Fin obtenerResultadoDistribuirRdiRde	
					
					
				//Inicio actualizarMetadatosDocumentoDer
				
				
					
				//Fin actualizarMetadatosDocumentoDer	
				
				//Inicio distribuirDer
					
				

					
				// Fin reasignarRutaDER		
					
				//Inicio distribuirCarta
				
				.withExternal()
					.source(States.DISTRIBUIR_CARTA)
					.target(States.ESPERANDO_REINTENTO)
					.event(Events.ERROR_REINTENTO)
					.and()
						
				.withExternal()
					.source(States.ESPERANDO_REINTENTO)
					.target(States.DISTRIBUIR_CARTA)
					.event(Events.VOLVER_ESPERAR_DISTRIBUIR_CARTA)  
					.and()	
					
						
				.withExternal()
					.source(States.DISTRIBUIR_CARTA)
					.target(States.FINALIZADO_ERRORES)
					.event(Events.NUMERO_MAXIMO_REINTENTOS_ALCANZADO)
					.and()
							
				//Fin distribuirCarta		
			

				//inicio obtenerRespuestaDistribuirCddCarta	
		
				.withExternal()
					.source(States.OBTENER_RESPUESTA_DISTRIBUIR_CDD_CARTA)
					.target(States.ESPERANDO_REINTENTO)
					.event(Events.ERROR_REINTENTO)
					.and()	
	
					
				.withExternal()
					.source(States.ESPERAR_RESPUESTA_DISTRIBUIR_CDD_CARTA)
					.target(States.ESPERANDO_REINTENTO)
					.event(Events.ERROR_REINTENTO)
					.and()
					
				.withExternal()
					.source(States.CARGAR_CATEGORIA_CS_RDI_RDE)
					.target(States.ESPERANDO_REINTENTO)
					.event(Events.ERROR_REINTENTO)
					.and()	

				
				.withExternal()
					.source(States.ESPERANDO_REINTENTO)
					.target(States.OBTENER_RESPUESTA_DISTRIBUIR_CDD_CARTA)
					.event(Events.VOLVER_OBTENER_RESPUESTA_DISTRIBUIR_CDD_CARTA)
					.and()	
					
				.withExternal()
					.source(States.ESPERAR_RESPUESTA_DISTRIBUIR_CDD_CARTA)
					.target(States.OBTENER_RESPUESTA_DISTRIBUIR_CDD_CARTA)
					.event(Events.VOLVER_OBTENER_RESPUESTA_DISTRIBUIR_CDD_CARTA)
					.and()		
				
				.withExternal()
					.source(States.ESPERAR_RESPUESTA_DISTRIBUIR_CDD_CARTA)
					.target(States.FINALIZADO_ERRORES)
					.event(Events.NUMERO_MAXIMO_REINTENTOS_ALCANZADO)
					.and()
					
				//Fin obtenerRespuestaDistribuirCddCarta
					
					
					
				//inicio obtenerRespuestaDistribuirCarta	
					
				.withExternal()
					.source(States.OBTENER_RESPUESTA_DISTRIBUIR_CARTA)
					.target(States.ESPERANDO_REINTENTO)
					.event(Events.ERROR_REINTENTO)
					.and()	
		
						
				.withExternal()
					.source(States.ESPERAR_RESPUESTA_DISTRIBUIR_CARTA)
					.target(States.ESPERANDO_REINTENTO)
					.event(Events.ERROR_REINTENTO)
					.and()
					
				.withExternal()
					.source(States.ESPERANDO_REINTENTO)
					.target(States.OBTENER_RESPUESTA_DISTRIBUIR_CARTA)
					.event(Events.VOLVER_OBTENER_RESPUESTA_DISTRIBUIR_CARTA)
					.and()	
						
				.withExternal()
					.source(States.ESPERAR_RESPUESTA_DISTRIBUIR_CARTA)
					.target(States.OBTENER_RESPUESTA_DISTRIBUIR_CARTA)
					.event(Events.VOLVER_OBTENER_RESPUESTA_DISTRIBUIR_CARTA)
					.and()		
				
				.withExternal()
					.source(States.ESPERAR_RESPUESTA_DISTRIBUIR_CARTA)
					.target(States.FINALIZADO_ERRORES)
					.event(Events.NUMERO_MAXIMO_REINTENTOS_ALCANZADO)
					.and()
						
					//Fin obtenerRespuestaDistribuirCarta
			
					
//				.withExternal()
//					.source(States.ESPERANDO_REINTENTO)
//					.target(States.HISTORY)
//					.timer(globalProperties.getTiempoReintento())				
//					.and()
				.withExternal()
					.source(States.ESPERANDO_REINTENTO)
					.target(States.FINALIZADO_ERRORES)
					.event(Events.NUMERO_MAXIMO_REINTENTOS_ALCANZADO)
					.and()
			
					
					
				//Fin Estados reintentos
					
				//INICIO FLUJO CANCELADO POR ERRORES
					
					
				
				
				
				.withExternal()
					.source(States.GENERAR_DOCUMENTO_FORMULARIO_RDI_RDE)
					.target(States.FINALIZADO_ERRORES)
					.event(Events.IR_FINALIZADO_ERRORES)					
					.and()
					

				
				.withExternal()
					.source(States.OBTENER_DATOS_CATEGORIA_FORM_RDI_RDE)
					.target(States.FINALIZADO_ERRORES)
					.event(Events.IR_FINALIZADO_ERRORES)					
					.and()
				
					
				
				
				
				.withExternal()
					.source(States.INICIAR_WF_GENERAR_NUM_RADICADO_RDI_RDE)
					.target(States.FINALIZADO_ERRORES)
					.event(Events.IR_FINALIZADO_ERRORES)					
					.and()
				.withExternal()
					.source(States.GENERAR_STICKER_RDI_RDE)
					.target(States.FINALIZADO_ERRORES)
					.event(Events.IR_FINALIZADO_ERRORES)					
					.and()
				
				
				.withExternal()
					.source(States.CARGAR_CATEGORIA_CS_RDI_RDE)
					.target(States.FINALIZADO_ERRORES)
					.event(Events.IR_FINALIZADO_ERRORES)					
					.and()
				
				
				
				
				
				.withExternal()
					.source(States.SUBIR_DOC_CONSECUTIVO)
					.target(States.FINALIZADO_ERRORES)
					.event(Events.IR_FINALIZADO_ERRORES)					
					.and()
				
				.withExternal()
					.source(States.OBTENER_RESPUESTA_DISTRIBUIR_CDD_CARTA)
					.target(States.FINALIZADO_ERRORES)
					.event(Events.IR_FINALIZADO_ERRORES)					
					.and()
				.withExternal()
					.source(States.OBTENER_RESPUESTA_DISTRIBUIR_CDD_RDI_RDE)
					.target(States.FINALIZADO_ERRORES)
					.event(Events.IR_FINALIZADO_ERRORES)					
					.and()
				.withExternal()
					.source(States.VALIDAR_INICIAR_FORMA_DISTRIBUIR_RDI_RDE)
					.target(States.FINALIZADO_ERRORES)
					.event(Events.IR_FINALIZADO_ERRORES)					
					.and()
				.withExternal()
					.source(States.DISTRIBUIR_RDI_RDE)
					.target(States.FINALIZADO_ERRORES)
					.event(Events.IR_FINALIZADO_ERRORES)					
					.and()
					

					
				.withExternal()
					.source(States.DISTRIBUIR_CARTA)
					.target(States.FINALIZADO_ERRORES)
					.event(Events.IR_FINALIZADO_ERRORES)					
					.and()
				.withExternal()
					.source(States.ESPERAR_RESULTADO_DISTRIBUIR)
					.target(States.FINALIZADO_ERRORES)
					.event(Events.IR_FINALIZADO_ERRORES)					
					.and()
				.withExternal()
					.source(States.OBTENER_RESULTADO_DISTRIBUIR_RDI_RDE)
					.target(States.FINALIZADO_ERRORES)
					.event(Events.IR_FINALIZADO_ERRORES)					
					.and()
					/*
				.withExternal()
					.source(States.OBTENER_RESULTADO_DISTRIBUIR_DER)
					.target(States.FINALIZADO_ERRORES)
					.event(Events.IR_FINALIZADO_ERRORES)					
					.and()
					*/
				.withExternal()
					.source(States.OBTENER_RESULTADO_DISTRIBUIR)
					.target(States.FINALIZADO_ERRORES)
					.event(Events.IR_FINALIZADO_ERRORES)					
					.and()
				.withExternal()
					.source(States.FIRMA_CANCELADA_ANULAR_DOC)
					.target(States.FINALIZADO_ERRORES)
					.event(Events.IR_FINALIZADO_ERRORES)					
					.and()
					/*
				.withExternal()
					.source(States.DISTRIBUIR_CORREO_DER)
					.target(States.FINALIZADO_ERRORES)
					.event(Events.IR_FINALIZADO_ERRORES)					
					.and()
					*/
				

					
			  //FIN FLUJO CANCELADO POR ERRORES
					
					
				.withExternal()
					.source(States.FINALIZADO_ERRORES)
					.target(States.FIN)
					.and()
					
				.withExternal()
					.source(States.FINALIZADO_ERRORES_FUNCIONALES)
					.target(States.FIN)
					.and()
					
				.withExternal()
					.source(States.FINALIZADO)
					.target(States.FIN)
					.and()
				.withExternal()
					.source(States.OBTENER_RESULTADO_DISTRIBUIR)
					.target(States.FINALIZADO)
					.event(Events.FINALIZAR);
						
		}
	
		@Bean
		public StateMachinePersist<States, Events, String> stateMachinePersist() {
			return new StateMachineDataBase();
		}

		@Bean
		public StateMachinePersister<States, Events, String> H2StateMachinePersister(
				StateMachinePersist<States, Events, String> stateMachinePersist) {
			return new DefaultStateMachinePersister<States, Events, String>(stateMachinePersist);
		}

		@Bean
		public Action<States, Events> siguienteEstado() {

			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + "Test cambio de estado");	

			};
		}

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////                       FIN CONFIGURACION SSM                         ///////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		
		
		
		
		//----------------------------------------------------------------------------------------------------------------------
		
		
		
		
		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////                 INICIO ESTADOS ----- DER -----                      ///////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				
		
		
		/**
		 * Metodo que inicia la radicacion DER
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> iniciarRadicarDer() {
			//SE RETORNA EL CONTEXTO DE LA MÁQUINA
			return (context) -> {
				//SE CARGA LA RAIZ LOG
				String raizLog = SSMUtils.getRaizLog(context);
				//SE MARCA EL INICIO DEL ESTADO
				log.info(raizLog + "----------------------INICIO ESTADO iniciarRadicarDer  -----------------------------");
				//SE PASAN LOS HEADERS INICIALES ENVIADOS DESDE EL CONTROLLER "radicarDerAsync" A LAS VARIABLES DE LA MÁQUINA				
				String conexionUsuario = context.getMessageHeaders().get(Consts.CONEXION_CS_SOLICITANTE, String.class);
				context.getExtendedState().getVariables().put(Consts.CONEXION_CS_SOLICITANTE, conexionUsuario);
				
				String oficinaDer = context.getMessageHeaders().get(Consts.OFICINA_DER, String.class);
				context.getExtendedState().getVariables().put(Consts.OFICINA_DER, oficinaDer);
				
				Long idSolicitante = context.getMessageHeaders().get(Consts.ID_SOLICITANTE, Long.class);
				context.getExtendedState().getVariables().put(Consts.ID_SOLICITANTE, idSolicitante);
				
				String pcrOrigen = context.getMessageHeaders().get(Consts.PCR_ORIGEN_DER, String.class);
				context.getExtendedState().getVariables().put(Consts.PCR_ORIGEN_DER, pcrOrigen);
				
				String cddOrigen = context.getMessageHeaders().get(Consts.CDD_ORIGEN_DER, String.class);
				context.getExtendedState().getVariables().put(Consts.CDD_ORIGEN_DER, cddOrigen);
				
				String workId = context.getMessageHeaders().get(Consts.ID_WORKFLOW_RADICACION, String.class);
				context.getExtendedState().getVariables().put(Consts.ID_WORKFLOW_RADICACION, workId);
						
				String rolCalidadDigitalizar = context.getMessageHeaders().get(Consts.ROL_CALIDAD_DIGITALIZAR_DER, String.class);
				context.getExtendedState().getVariables().put(Consts.ROL_CALIDAD_DIGITALIZAR_DER, rolCalidadDigitalizar);
				
				String pbxOrigen = context.getMessageHeaders().get(Consts.PBX_ORIGEN, String.class);
				context.getExtendedState().getVariables().put(Consts.PBX_ORIGEN, pbxOrigen);
								
				log.info(raizLog + "Se cargaron correctamente las variables de los Headers del controller en las varibles del contexto de la SSM");
				
				//SE INICIALIZAN ALGUNAS VARIABLES ADICIONALES
				Boolean errorMaquina = Boolean.FALSE;
				context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, errorMaquina );//SE INICIA VARIABLE DE ERROR EN FALSE
				
				Boolean finRadicacionDER = Boolean.FALSE;
				context.getExtendedState().getVariables().put(Consts.FIN_RADICACION_DER, finRadicacionDER);//SE INICIA VARIABLE DE RADICACION FINALIZADA EN FALSO
				
				Boolean esCorreoDer = Boolean.FALSE;
				context.getExtendedState().getVariables().put(Consts.DER_CORREO, esCorreoDer);//SE INICIA VARIABLE DE DER EMAIL EN FALSO
				
				log.info(raizLog + "Se inician variables: "
				+ Consts.ERROR_MAQUINA +"["+errorMaquina+"] - "
				+ Consts.FIN_RADICACION_DER+" ["+finRadicacionDER+"] - "
				+ Consts.DER_CORREO+"["+esCorreoDer+"]");	
				
				//SE MARCA EL FIN DEL ESTADO
				log.info(raizLog + "----------------------FIN ESTADO iniciarRadicarDer  -----------------------------");
							
				};
		}
		
		
		
		/**
		 * Metodo que inicia WorkFlow para generar numero de radicado de un DER
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> iniciarWfGenerarNumRadicadoDer() {
			// SE RETORNA EL CONTEXTO DE LA MÁQUINA
			return (context) -> {
				// SE CARGA LA RAIZ LOG
				String raizLog = SSMUtils.getRaizLog(context);
				// SE MARCA EL INICIO DEL ESTADO
				log.info(raizLog + "-----------------------INICIO ESTADO iniciarWfGenerarNumRadicadoDer---------------------------------");
				
				// SE CARGAN LAS VARIABLES ALMACENADAS EN EL CONTEXTO DE LA SSM
				String conexionUsuario = context.getExtendedState().get(Consts.CONEXION_CS_SOLICITANTE, String.class);
				String oficinaDer = context.getExtendedState().get(Consts.OFICINA_DER, String.class);
				Long idSolicitante = context.getExtendedState().get(Consts.ID_SOLICITANTE, Long.class);
				String workId = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);
				Boolean esCorreoDer = context.getExtendedState().get(Consts.DER_CORREO, Boolean.class);

				// SE INICIA WF GENERAR NUM RADICADO
				String titulo = "Generar_NroRadicado_DER"; //TITULO DEL WF QUE SERÁ INICIADO
				String atributoNumRadicado = "_radicadoGenerado"; //NOMBRE DEL ATRIBUTO QUE SERÁ REGRESADSO POR EL WF
				
				// ATRIBUTOS QUE SERÁN ENVIADOS PARA INICIAR_RADICAR_CA_ME EL WF
				Map<String, ValorAtributo> atributos = new HashMap<String, ValorAtributo>();
				atributos.put("_oficina", new ValorAtributo(oficinaDer, TipoAtributo.STRING));
				atributos.put("Fecha Radicación", new ValorAtributo(new Date(), TipoAtributo.DATE));
				atributos.put(workflowProps.getAtributo().getIdSolicitante(),
						new ValorAtributo(idSolicitante, TipoAtributo.USER));

				log.info(raizLog + "INICIO iniciar WF Generar NumRadicado con usuario: [" + conexionUsuario + "]");
				
				long idProcess;
				try {
					// SE INCIA WF CON LOS ATRIBUTOS
					idProcess = Workflow.iniciarWorkflowConAtributos(autenticacionCS.getAdminSoapHeader(),
							workflowProps.getGenerarNumeroRadicadoDer(), titulo, atributos, wsdlsProps.getWorkflow());

					log.info(raizLog + "FIN iniciar WF Generar NumRadicado");

					log.info(raizLog + "INICIO obtener NumRadicado");
					
					//SE OBTIENE EL NUM DE RADICADO OBTENIDO DEL WF
					String numeroRadicadoObtenido = Workflow
							.obtenerValoresAtributo(autenticacionCS.getAdminSoapHeader(), idProcess,
									atributoNumRadicado, wsdlsProps.getWorkflow())
							.get(0).toString();

					//SE ALMACENA EL VALOR OBTENIDO EN UNA VARIABLE DEL CONTEXTO DE LA SSM
					context.getExtendedState().getVariables().put(Consts.NUMERO_RADICADO_OBTENIDO,
							numeroRadicadoObtenido);

					log.info(raizLog + "El numero de radicado obtenido es: [" + numeroRadicadoObtenido + "]");
					
					log.info(raizLog + "FIN obtener NumRadicado");
					
					//SE VALIDA SI ES CORREO DE DER EMAIL
					if(esCorreoDer) {
						context.getStateMachine().sendEvent(Events.IR_CARGAR_CATEGORIA_CORREO_DER);
						log.info(raizLog + "Se envió el evento: ["+Events.IR_CARGAR_CATEGORIA_CORREO_DER+"]  para la maquina con workId: [" + workId +"]");
					}else {
						context.getStateMachine().sendEvent(Events.IR_INICIAR_WF_FORMULARIO_DER_REMOTO);
						log.info(raizLog + "Se envió el evento: ["+Events.IR_INICIAR_WF_FORMULARIO_DER_REMOTO+"]  para la maquina con workId: [" + workId +"]");
					}
					

				} catch (Exception e) {
					cargarError(context, e);
					context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, true);
					context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
					log.info(raizLog + "Se envió el evento: ["+Events.IR_FINALIZADO_ERRORES+"]  para la maquina con workId: [" + workId +"]");
					log.info(raizLog
							+ "ErrorCorrespondencia iniciarWfGenerarNumRadicadoDer iniciarWorkflowConAtributos " + e,
							e);
				}

				// SE MARCA EL FIN DEL ESTADO
				log.info(raizLog
						+ "-----------------------FIN ESTADO iniciarWfGenerarNumRadicadoDer---------------------------------");

				// SE ALMACENA CONTEXTO DE LA SSM EN BD
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out
							.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}
			};

		}
		
		
		
		/**
		 * Metodo que inicia WorkFlow Formulario DER-Remoto
		 * @return
		 */
		@Bean
		public Action<States, Events> iniciarWfFormularioDer() {
			// SE RETORNA EL CONTEXTO DE LA MÁQUINA
			return (context) -> {
				// SE CARGA LA RAIZ LOG
				String raizLog = SSMUtils.getRaizLog(context);
				// SE MARCA EL INICIO DEL ESTADO
				log.info(raizLog
						+ "----------------------- INICIO ESTADO iniciarWfFormularioDER ---------------------------------");
				// SE VALIDA QUE NO EXISTAN ERRORES CARGADOS EN LA MÁQUINA
				Boolean errorMaquina = context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class);
				if (!errorMaquina) {					
					
					// SE CARGAN LAS VARIABLES ALMACENADAS EN EL CONTEXTO DE LA SSM
					String conexionUsuario = context.getExtendedState().get(Consts.CONEXION_CS_SOLICITANTE, String.class);
					Long idSolicitante = context.getExtendedState().get(Consts.ID_SOLICITANTE, Long.class);
					String numeroRadicadoObtenido = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO, String.class);
					String workId = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);
					String pbxOrigen = context.getExtendedState().get(Consts.PBX_ORIGEN, String.class);
					
					// SE INICIALIZAN ALGUNAS VARIABLES ADICIONALES PARA LA FECHA
					LocalDateTime date = LocalDateTime.now();
					// SE GUARDA LA VARIABLE FECHA EN EL CONTEXTO DE LA SSM
					context.getExtendedState().getVariables().put(Consts.FECHA_RADICACION, date);
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
					String formatDateTime = date.format(formatter);
					
					log.info(raizLog + " Fecha de radicacion ["+formatDateTime+"]");
					
					// SE VA A INICIAR_RADICAR_CA_ME WF DE DER-REMOTO
					String titulo = "FT115 - Radicacion DER " + numeroRadicadoObtenido;//TITULO DEL WF QUE SERÁ INICIADO

					// ATRIBUTOS QUE SERÁN ENVIADOS PARA INICIAR_RADICAR_CA_ME EL WF
					Map<String, ValorAtributo> atributos = new HashMap<String, ValorAtributo>();
					atributos.put(workflowProps.getDistribucionDer().getAtributoWF().getUsuarioSolicitante(), new ValorAtributo(idSolicitante, TipoAtributo.USER));
					atributos.put(workflowProps.getDistribucionDer().getAtributoWF().getWorkId(), new ValorAtributo(workId, TipoAtributo.STRING));
					atributos.put(workflowProps.getDistribucionDer().getAtributoWF().getNumeroRadicado(), new ValorAtributo(numeroRadicadoObtenido, TipoAtributo.STRING));
					atributos.put(workflowProps.getDistribucionDer().getAtributoWF().getFechaRadicacion(), new ValorAtributo(formatDateTime, TipoAtributo.STRING));
					atributos.put(workflowProps.getDistribucionDer().getAtributoWF().getPbxOrigen(), new ValorAtributo(pbxOrigen, TipoAtributo.STRING));
					
					log.info(raizLog + "INICIO WF Formulario DER - Remoto con usuario: [" + conexionUsuario + "]");
					long idProcess;
					try {
						// SE OBTIENE EL ID AL GENERAR INSTANCIA
						idProcess = Workflow.iniciarWorkflowConAtributos(
									autenticacionCS.getUserSoapHeader(conexionUsuario),
									workflowProps.getIdNotificacionRadicadoDer(), titulo, atributos,
									wsdlsProps.getWorkflow());
						
						log.info(raizLog + "Instancia de WF Formulario DER - Remoto iniciada: [" + idProcess + "]");
						
						// SE ALAMACENA ID DE INSTANCIA INICIADA EN EL CONTEXTO DE LA MAQUINA
						context.getExtendedState().getVariables().put(Consts.ID_WORKFLOW_REMOTO_DER, idProcess);
						
						log.info(raizLog + "FIN WF Formulario DER - Remoto");
						
						context.getStateMachine().sendEvent(Events.IR_ESPERAR_OBTENER_DATOS_CATEGORIA_FORM_DER);
						log.info(raizLog + "Se envió el evento: ["+Events.IR_ESPERAR_OBTENER_DATOS_CATEGORIA_FORM_DER+"]  para la maquina con workId: [" + workId +"]");

					} catch (Exception e) {
						cargarError(context, e);
						context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, true);
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						log.info(raizLog + "Se envió el evento: ["+Events.IR_FINALIZADO_ERRORES+"]  para la maquina con workId: [" + workId +"]");
						log.info(raizLog + "ErrorCorrespondencia iniciarWfFormularioDER iniciarWorkflowConAtributos " + e , e);
					}

					
				}else {
					log.info(raizLog + "La maquina se encontro con al variable ["+ Consts.ERROR_MAQUINA+"] en valor ["+errorMaquina+"]");
				}
				
				// SE MARCA EL FIN DEL ESTADO
				log.info(raizLog
						+ "----------------------- FIN ESTADO iniciarWfFormularioDER ---------------------------------");
				
				// SE ALMACENA CONTEXTO DE LA SSM EN BD
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}
				
				
			};
			
		}
		
		
		
		
		/**
		 * Metodo que permite esperar para obtener Datos del Formulario DER
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> esperarObtenerDatosCatFormDer() {
			// SE RETORNA EL CONTEXTO DE LA MÁQUINA
			return (context) -> {
				// SE CARGA LA RAIZ LOG
				String raizLog = SSMUtils.getRaizLog(context);
				// SE MARCA EL INICIO DEL ESTADO
				log.info(raizLog
						+ "-----------------------INICIO ESTADO esperarObtenerDatosCatFormDer---------------------------------");
				// SE VALIDA QUE NO EXISTAN ERRORES CARGADOS EN LA MÁQUINA
				Boolean errorMaquina = context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class);
				if (!errorMaquina) {		
					
					log.info(raizLog + "Maquina esperando siguiente llamado de un controller .....");
					
				}else {
					log.info(raizLog + "La maquina se encontro con la variable ["+ Consts.ERROR_MAQUINA+"] en valor ["+errorMaquina+"]");
				}
				
				// SE MARCA EL FIN DEL ESTADO
				log.info(raizLog
						+ "-----------------------FIN ESTADO esperarObtenerDatosCatFormDer---------------------------------");

				// SE ALMACENA CONTEXTO DE LA SSM EN BD
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out
							.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}				
				
			};
		}
		
		
		
		/**
		 * Metodo que permite obtener la informacion del formulario DER
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> obtenerDatosCategoriaFormDer() {
			// SE RETORNA EL CONTEXTO DE LA MÁQUINA
			return (context) -> {
				// SE CARGA LA RAIZ LOG
				String raizLog = SSMUtils.getRaizLog(context);
				// SE MARCA EL INICIO DEL ESTADO
				log.info(raizLog + "------------------------------INICIO ESTADO obtenerDatosCategoriaFormDer-------------------------");
				// SE VALIDA QUE NO EXITAN ERRORES CARGADOS EN LA MÁQUINA
				Boolean errorMaquina = context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class);
				if (!errorMaquina) {
					//SE PASAN LOS HEADERS ENVIADOS DESDE EL CONTROLLER "obtenerDataFormDer" A LAS VARIABLES DE LA MÁQUINA
					Long idDoc = context.getMessageHeaders().get(Consts.ID_DOC_RADICACION, Long.class);
					context.getExtendedState().getVariables().put(Consts.ID_DOC_RADICACION, idDoc);
					
					// SE CARGAN VARIABLES YA ALMACENADAS EN EL CONTEXTO DE LA SSM
					long workIdRemoto = context.getExtendedState().get(Consts.ID_WORKFLOW_REMOTO_DER, Long.class);
					String conexionUsuario = context.getExtendedState().get(Consts.CONEXION_CS_SOLICITANTE, String.class);
					String workId = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);

					// SE GUARDA LA TIPOLOGIA
					context.getExtendedState().getVariables().put(Consts.TIPOLOGIA_DER, "DER");
					
					// SE INICIALIZAN VARIABLES PARA GUARDAR DATOS DEL FORMULARIO DER
					String dependeciasDestinosConcat = "";
					String formaEntregaDepConcat="";	
					String cadenaAnexosCantidadDescripcion = "";
					List<Destino> listDestinos = new ArrayList<>();
					
					// SE DEFINE EL NOMBRE DE LAS SECCIONES DEL FOMULARIO PARA REALIZAR LA BUSQUEDA
					String seccion = "INFORMACIÓN DEL DOCUMENTO";
					String seccionDos = "Distribución";
					String seccionTres = "ANEXOS";
					
					// SE CARGAN LOS DATOS DEL FORMULARIO
					try {						
						// SE CARGAN LOS VALORES DE LA SECCION "INFORMACIÓN DEL DOCUMENTO"
						List<RowValue> listValores = Workflow.obtenerValoresFormularioPorSeccion(
								autenticacionCS.getUserSoapHeader(conexionUsuario), workIdRemoto, seccion,
								wsdlsProps.getWorkflow());
						// SE CARGAN LOS VALORES DE LA SECCION "Distribución"
						List<RowValue> listValoresDestinos = Workflow.obtenerValoresFormularioPorSeccion(
								autenticacionCS.getUserSoapHeader(conexionUsuario), workIdRemoto, seccionDos,
								wsdlsProps.getWorkflow());
						// SE CARGAN LOS VALORES DE LA SECCION "ANEXOS"
						List<RowValue> listValoresAnexos = Workflow.obtenerValoresFormularioPorSeccion(
								autenticacionCS.getUserSoapHeader(conexionUsuario), workIdRemoto, seccionTres,
								wsdlsProps.getWorkflow());
						
						// SE GUARDAN LOS VALORES ENCONTRADOS EN LA SECCION "INFORMACIÓN DEL DOCUMENTO"
						for (RowValue rowValue : listValores) {							
							
							DataValue valorTipoCom = rowValue.getValues().stream().filter(
									unCampo -> unCampo.getDescription().equals("Tipo de comunicación"))
									.findFirst().get();													
							
							DataValue valorRemitente = rowValue.getValues().stream().filter(
									unCampo -> unCampo.getDescription().equals("Remitente (entidad o persona natural)"))
									.findFirst().get();
							
							
							if (valorTipoCom instanceof StringValue) {
								StringValue unStringValue = (StringValue) valorTipoCom;
								String valorTipoComTemp = "";
								if (!unStringValue.getValues().isEmpty())
									valorTipoComTemp = unStringValue.getValues().get(0);
								// SE ALAMACENAN LAS VARIABLES EN EL CONTEXTO DE LA SSM
								context.getExtendedState().getVariables().put(Consts.TIPO_COMUNICACION_DER,valorTipoComTemp);
								
							}
							
							if (valorRemitente instanceof StringValue) {
								StringValue unStringValue = (StringValue) valorRemitente;
								String valorRemitenteTemp = "";
								if (!unStringValue.getValues().isEmpty())
									valorRemitenteTemp = unStringValue.getValues().get(0);
								// SE ALAMACENAN LAS VARIABLES EN EL CONTEXTO DE LA SSM
								context.getExtendedState().getVariables().put(Consts.DEPENDENCIA_ORIGEN_DER,valorRemitenteTemp);							
							}
							
						
						}
							
						// SE GUARDAN LOS VALORES ENCONTRADOS EN LA SECCION "Distribución"
						for (RowValue rowValue : listValoresDestinos) {

							Destino destino = new Destino();

							DataValue itemDependenciaDesti = rowValue.getValues().stream()
									.filter(unCampo -> unCampo.getDescription().equals("Dependencia Destino"))
									.findFirst().get();

							DataValue itemPcr = rowValue.getValues().stream()
									.filter(unCampo -> unCampo.getDescription().equals("PCR Destino")).findFirst()
									.get();

							DataValue itemCdd = rowValue.getValues().stream()
									.filter(unCampo -> unCampo.getDescription().equals("CDD Destino")).findFirst()
									.get();

							DataValue itemFormaEntrega = rowValue.getValues().stream().filter(
									unCampo -> unCampo.getDescription().equals("Forma de entrega a la dependencia"))
									.findFirst().get();

							DataValue itemTipoEntrega = rowValue.getValues().stream()
									.filter(unCampo -> unCampo.getDescription().equals("Original / Copia")).findFirst()
									.get();

							if (itemDependenciaDesti instanceof StringValue) {
								StringValue unStringValue = (StringValue) itemDependenciaDesti;
								String depenDestino = "";
								if (!unStringValue.getValues().isEmpty()) {
									depenDestino = unStringValue.getValues().get(0);
								}

								log.info(raizLog + "Dependencia destino: [" + depenDestino + "]");
								destino.setDependenciaDestino(depenDestino);

								dependeciasDestinosConcat += depenDestino + ";";

							}

							if (itemPcr instanceof StringValue) {
								StringValue unStringValue = (StringValue) itemPcr;
								String pcrDestino = "";
								if (!unStringValue.getValues().isEmpty()) {
									pcrDestino = unStringValue.getValues().get(0);
								}

								log.info(raizLog + "PCR destino: [" + pcrDestino + "]");
								destino.setPcrDestino(pcrDestino);

							}

							if (itemCdd instanceof StringValue) {
								StringValue unStringValue = (StringValue) itemCdd;
								String cddDestino = "";
								if (!unStringValue.getValues().isEmpty()) {
									cddDestino = unStringValue.getValues().get(0);
								}
								log.info(raizLog + "CDD destino: [" + cddDestino + "]");
								destino.setCddDestino(cddDestino);

							}

							if (itemFormaEntrega instanceof StringValue) {
								StringValue unStringValue = (StringValue) itemFormaEntrega;
								String formaEntrega = "";
								if (!unStringValue.getValues().isEmpty()) {
									formaEntrega = unStringValue.getValues().get(0);
								}

								log.info(raizLog + "Forma de entrega: [" + formaEntrega + "]");
								destino.setFormaEntregaDer(formaEntrega);
								formaEntregaDepConcat += formaEntrega + ";";

							}

							if (itemTipoEntrega instanceof StringValue) {
								StringValue unStringValue = (StringValue) itemTipoEntrega;
								String strTipoEntrega = "";
								if (!unStringValue.getValues().isEmpty()) {
									strTipoEntrega = unStringValue.getValues().get(0);
								}

								log.info(raizLog + "Tipo de entrega: [" + strTipoEntrega + "]");
								destino.setDescripcionObjeto(strTipoEntrega);

							}
							// SE AÑADE CADA DESTINO A LA LISTA
							listDestinos.add(destino);

							// SE ALAMACENAN LAS VARIABLES EN EL CONTEXTO DE LA SSM
							context.getExtendedState().getVariables().put(Consts.LISTA_DESTINOS_DER, listDestinos);
							context.getExtendedState().getVariables().put(Consts.DESTINOS_DER,
									dependeciasDestinosConcat);
							context.getExtendedState().getVariables().put(Consts.FORMA_ENTREGA_DER,
									formaEntregaDepConcat);

						}
							
						
						// SE GUARDAN LOS VALORES ENCONTRADOS EN LA SECCION "ANEXOS"
						for (RowValue rowValue : listValoresAnexos) {
							
							
					
							DataValue itemAnexos = rowValue.getValues().stream().filter(
									unCampo -> unCampo.getDescription().equals("Anexos"))
									.findFirst().get();
							
							DataValue itemCantidad = rowValue.getValues().stream().filter(
									unCampo -> unCampo.getDescription().equals("Cantidad"))
									.findFirst().get();
							
							DataValue itemDescripcion = rowValue.getValues().stream().filter(
									unCampo -> unCampo.getDescription().equals("Descripción"))
									.findFirst().get();
							
							String flatAnexos = "";
							
							if (itemAnexos instanceof StringValue) {
								StringValue unStringValue = (StringValue) itemAnexos;
								String itemAnexosTemp = "";
								if (!unStringValue.getValues().isEmpty())
									itemAnexosTemp = unStringValue.getValues().get(0);
								cadenaAnexosCantidadDescripcion += "Anexos: "+itemAnexosTemp;
								flatAnexos = itemAnexosTemp;
								
							}
							
							if(flatAnexos.equalsIgnoreCase("si")) {
							
								if (itemCantidad instanceof IntegerValue) {
									IntegerValue unIntegerValue = (IntegerValue) itemCantidad;
									Long temp=new Long(0);
									if (unIntegerValue!=null)
										temp = unIntegerValue.getValues().get(0);
									cadenaAnexosCantidadDescripcion += "\nCantidad: "+temp.toString();		 						
								}
								
								if (itemDescripcion instanceof StringValue) {
									StringValue unStringValue = (StringValue) itemDescripcion;
									String temp = "";
									if (!unStringValue.getValues().isEmpty())
										temp = unStringValue.getValues().get(0);
									cadenaAnexosCantidadDescripcion += "\nDescripción: "+temp;		 						
								}
							
							}
							
							// SE ALAMACENAN LAS VARIABLES EN EL CONTEXTO DE LA SSM
							context.getExtendedState().getVariables().put(Consts.CADENA_ANEXOS_CANTIDAD_DESCRIPCION_DER, cadenaAnexosCantidadDescripcion);
						}
						
						
						context.getStateMachine().sendEvent(Events.IR_CARGAR_CATEGORIA_FORMULARIO_DER);
						log.info(raizLog + "Se envió el evento: ["+Events.IR_CARGAR_CATEGORIA_FORMULARIO_DER+"]  para la maquina con workId: [" + workId +"]");
						
						
					} catch (NoSuchElementException | SOAPException | IOException | WebServiceException e) {
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						log.info(raizLog + "Se envió el evento: ["+Events.IR_FINALIZADO_ERRORES+"]  para la maquina con workId: [" + workId +"]");
						log.info(raizLog + "ErrorCorrespondencia obtenerDatosCategoriaFormDer obtenerValoresFormularioPorSeccion" + e , e);
					}
					
				}else {
					log.info(raizLog + "La maquina se encontro con la variable ["+ Consts.ERROR_MAQUINA+"] en valor ["+errorMaquina+"]");
				}			
				
				
				// SE MARCA EL FIN DEL ESTADO
				log.info(raizLog + "------------------------------FIN ESTADO obtenerDatosCategoriaFormDer-------------------------");
				
				// SE ALMACENA CONTEXTO DE LA SSM EN BD
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}
			};
			
		}
		
		
		
		
		/**
		 * Metodo que permite cargar la categoria Correspondencia con los metadatos
		 * obtenidos del formulario
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> cargarCategoriaFormularioDer() {
			// SE RETORNA EL CONTEXTO DE LA MÁQUINA
			return (context) -> {
				// SE CARGA LA RAIZ LOG
				String raizLog = SSMUtils.getRaizLog(context);
				// SE MARCA EL INICIO DEL ESTADO
				log.info(raizLog
						+ "----------------------- INICIO ESTADO cargarCategoriaFormularioDer ---------------------------------");
				// SE VALIDA QUE NO EXISTAN ERRORES CARGADOS EN LA MÁQUINA
				Boolean errorMaquina = context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class);
				if (!errorMaquina) {					
					// SE CARGAN VARIABLES ALMACENADAS EN EL CONTEXTO DE LA SSM
					@SuppressWarnings("unchecked") // Se anhade porque se está seguro que la lista es del tipo indicado
					List<Destino> listaDestinatarios = (List<Destino>) context.getExtendedState()
							.get(Consts.LISTA_DESTINOS_DER, Object.class);
					
					String workId = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);
					
					String nombreRemitenteDER = context.getExtendedState().get(Consts.DEPENDENCIA_ORIGEN_DER,
							String.class);
					nombreRemitenteDER = SSMUtils.truncarString(nombreRemitenteDER, Consts.TAMANO_MAXIMO_CS_REMITENTE_DER);
					
					String tipologia = context.getExtendedState().get(Consts.TIPOLOGIA_DER, String.class);

					String numeroRadicadoObtenido = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO,
							String.class);
					
					log.info(raizLog + "EL radicado DER generado es: [" + numeroRadicadoObtenido+"]");
					
					Long idDocFormulario = context.getExtendedState().get(Consts.ID_DOC_RADICACION, Long.class);

					log.info(raizLog + "El idDoc (formulario pdf) es: [" + idDocFormulario+"]");

					// SE CREAN VARIABLES PARA LLENAR LA CATEGORIA
					DateFormat dateFormatHora = new SimpleDateFormat(globalProperties.getFormatoHora());
					//Date date = new Date();
					// SE EXTRAE LA FECHA ALMACENADA AL RADICAR
					LocalDateTime localDateTime = context.getExtendedState().get(Consts.FECHA_RADICACION, LocalDateTime.class);
					Date date = Date.from( localDateTime.atZone( ZoneId.systemDefault()).toInstant()); //CONVERTIR FROM LOCALDATETIME TO DATE
					String hora = dateFormatHora.format(date);//Hora de radicacion
					
					log.info(raizLog + "La hora de radicacion a almacenar en la categoria : ["+hora+"]");

					String asunto = "Radicacion DER";
					
					String tipologiaFinal = "DER - Documento Externo Recibido";

					log.info(raizLog +  " Tipologia: [" + tipologia + "] - Tipologia Final: [" + tipologiaFinal + "] - Asunto: [" + asunto + "]");
					
					List<Map<String, Object>> listSubGrupoDestino = new ArrayList<>();

					List<Map<String, Object>> listSubGrupoOrigen = new ArrayList<>();
					
					// SE CREAN LOS METADATOS A ALMACENAR EN LA CATEGORIA
					Map<String, Object> metadatos = new HashMap<>();

					metadatos.put(categoriaProps.getCorrespondencia().getAtributoTipologia(), tipologiaFinal);
					metadatos.put(categoriaProps.getCorrespondencia().getAtributoEstado(),
							estadosProps.getRadicado());
					metadatos.put(categoriaProps.getCorrespondencia().getAtributoAsunto(), asunto);
					metadatos.put(categoriaProps.getCorrespondencia().getAtributoFechaRadicacion(), date);
					metadatos.put(categoriaProps.getCorrespondencia().getAtributoHoraRadicacion(), hora);
					metadatos.put(categoriaProps.getCorrespondencia().getAtributoNumeroRadicacion(), numeroRadicadoObtenido);

					// SE CREAN LOS METADATOS PARA CADA DESTINO
					for (Destino destino : listaDestinatarios) {
						
						log.info(raizLog + "Metadatos DER");

						log.info(raizLog + "Destinatario: [" + destino.getDependenciaDestino()+"]");

						
						// METADATOS ORIGEN*DESTINO, IGUAL PARA TODOS
						Map<String, Object> metadatosSubGrupoOrigen = new HashMap<>();
						
							
						metadatosSubGrupoOrigen.put("Nombre", nombreRemitenteDER);
						metadatosSubGrupoOrigen.put("Cargo", "");
						metadatosSubGrupoOrigen.put("Dependencia", "");
						listSubGrupoOrigen.add(metadatosSubGrupoOrigen);
						
						trucarValoresCategoriaCorrespondencia(listSubGrupoOrigen);

						// METADATOS PARA CADA DESTINO
						Map<String, Object> metadatosSubGrupoDestino = new HashMap<>();

						metadatosSubGrupoDestino.put("Nombre", "");
						metadatosSubGrupoDestino.put("Cargo", "");

						String destinoStr = destino.getDependenciaDestino().trim();
						destinoStr = SSMUtils.truncarString(destinoStr, Consts.TAMANO_MAXIMO_CS_DESTINO_DER);
						metadatosSubGrupoDestino.put("Dependencia / Entidad", destinoStr);

						String direccionStr = destino.getFormaEntregaDer();
						direccionStr = SSMUtils.truncarString(direccionStr, Consts.TAMANO_MAXIMO_CS);
						metadatosSubGrupoDestino.put("Dirección", direccionStr);

						listSubGrupoDestino.add(metadatosSubGrupoDestino);

						trucarValoresCategoriaCorrespondencia(listSubGrupoDestino);
					
					}
					
					//SE ALMACENAN LOS DATOS DE LA CATEGORIA EN EL CONTEXTO DE LA SSM
					context.getExtendedState().getVariables().put(Consts.METADATOS_CATEGORIA_CORR_DER, metadatos);
					context.getExtendedState().getVariables().put(Consts.METADATOS_SUBGRUPO_ORIGEN_CAT_CORR_DER, listSubGrupoOrigen);
					context.getExtendedState().getVariables().put(Consts.METADATOS_SUBGRUPO_DESTINO_CAT_CORR_DER, listSubGrupoDestino);

					// SE ADICIONA CATEGORIA DE CORR
					try {
						//SOLO HAY UN ORIGEN, POR ESO NO HAY ITERACIONES
						log.info(raizLog + "INICIO Crear categoria");
						ContenidoDocumento.adicionarCategoriaMetadata(
								autenticacionCS.getAdminSoapHeader(), idDocFormulario,
								categoriaProps.getCorrespondencia().getId(),
								categoriaProps.getCorrespondencia().getNombre(), metadatos,
								categoriaProps.getCorrespondencia().getSubgrupoOrigen(), listSubGrupoOrigen.get(0),
								Boolean.TRUE, wsdlsProps.getDocumento());

						// SE DEBEN CARGAR LOS VALORES DE TODOS LOS DESTINOS
						for (Map<String, Object> mapDestino : listSubGrupoDestino) {
							ContenidoDocumento.adicionarCategoriaMetadata(
									autenticacionCS.getAdminSoapHeader(), idDocFormulario,
									categoriaProps.getCorrespondencia().getId(),
									categoriaProps.getCorrespondencia().getNombre(), metadatos,
									categoriaProps.getCorrespondencia().getSubgrupoDestino(), mapDestino, Boolean.FALSE,
									wsdlsProps.getDocumento());
						}
						log.info(raizLog + "FIN Crear categoria");
						
						context.getStateMachine().sendEvent(Events.IR_VALIDAR_FORMA_ENTREGA_DER);						
						log.info(raizLog + "Se envió el evento: ["+Events.IR_VALIDAR_FORMA_ENTREGA_DER+"]  para la maquina con workId: [" + workId +"]");
						

					} catch (ServerSOAPFaultException | IOException |  SOAPException e) {
						log.error(raizLog + "ErrorCorrespondencia actualizarMetadatosDocumentoDer adicionarCategoriaMetadata", e);
						cargarError(context, e);						
						context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.CARGAR_CATEGORIA_FORMULARIO_DER);
						context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);						
						log.info(raizLog + "Se envió el evento: ["+Events.ERROR_REINTENTO+"]  para la maquina con workId: [" + workId +"]");
					
					} catch (Exception e) {
						cargarError(context, e);
						context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, true);
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						log.info(raizLog + "Se envió el evento: ["+Events.IR_FINALIZADO_ERRORES+"]  para la maquina con workId: [" + workId +"]");
						log.error(raizLog + "ErrorCorrespondencia actualizarMetadatosDocumentoDer adicionarCategoriaMetadata " + e , e);
					}

				}else {
					log.info(raizLog + "La maquina se encontro con la variable ["+ Consts.ERROR_MAQUINA+"] en valor ["+errorMaquina+"]");
				}
				
				
				// SE MARCA EL FIN DEL ESTADO
				log.info(raizLog
						+ "-----------------------FIN ESTADO cargarCategoriaFormularioDer---------------------------------");

				// SE ALMACENA CONTEXTO DE LA SSM EN BD
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out
							.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}

			};
		}
		
		
		
		
		/**
		 * Metodo que permite validar los destinatarios DER para determinar si espera
		 * digitalizacion o continua al paso de distribucion
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> validarFormaEntregaDer() {
			// SE RETORNA EL CONTEXTO DE LA MÁQUINA
			return (context) -> {
				// SE CARGA LA RAIZ LOG
				String raizLog = SSMUtils.getRaizLog(context);
				// SE MARCA EL INICIO DEL ESTADO
				log.info(raizLog
						+ "----------------------- INICIO ESTADO validarFormaEntregaDER ---------------------------------");
				// SE VALIDA QUE NO EXISTAN ERRORES CARGADOS EN LA MÁQUINA
				Boolean errorMaquina = context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class);
				if (!errorMaquina) {
					// SE CARGAN VARIABLES ALMACENADAS EN EL CONTEXTO DE LA SSM
					@SuppressWarnings("unchecked") // Se anhade porque se está seguro que la lista es del tipo indicado
					List<Destino> listDestinos = (List<Destino>) context.getExtendedState()
							.get(Consts.LISTA_DESTINOS_DER, Object.class);
					
					String workId = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);
					
					// SE VALIDA SI TODAS LAS FORMAS DE ENTREGA DEL DER SON "FISICAS"
					Boolean todoFisico = Boolean.TRUE;
					log.info(raizLog + "Se va a validar la forma de entrega de cada dependencia destino ...");
					for (Destino destino : listDestinos) {
						log.info(raizLog + "Dependencia ["+destino.getDependenciaDestino()+"] - Forma entrega: [" + destino.getFormaEntregaDer()+"]");
						// VALIDACION SI FORMA DE ENTREGA NO ES SOLO FISICA
						if (!destino.getFormaEntregaDer().equals(globalProperties.getDerObjetoFisico())) {
							todoFisico = Boolean.FALSE;
							break;// FINALIZA VALIDACION
						}

					}

					// SI SE ENCUENTRAN TODAS FISICAS
					if (todoFisico) {
						// SE ENVIA EVENTO PARA PASAR AL ESTADO "SUBIR_DOC_CONSECUTIVO_DER"
						log.info(raizLog + "Todas las formas de entrega son físicas, se van a iniciar flujos de distribución ....");
						context.getStateMachine().sendEvent(Events.IR_DISTRIBUIR_DER);						
						log.info(raizLog + "Se envió el evento: ["+Events.IR_DISTRIBUIR_DER+"]  para la maquina con workId: [" + workId +"]");

					} else {
						// SE REQUIERE DIGITALIZACION
						log.info(raizLog + "Se requiere digitalización ....");
						context.getStateMachine().sendEvent(Events.IR_ESPERAR_DIGITALIZAR_DER);						
						log.info(raizLog + "Se envió el evento: ["+Events.IR_ESPERAR_DIGITALIZAR_DER+"]  para la maquina con workId: [" + workId +"]");

					}
					//SE ALMACENA VARIABLE DE TIPO DE DISTRIBUCIÓN EN EL CONTEXTO DE LA SSM
					context.getExtendedState().getVariables().put(Consts.ES_DISTRIBUCION_FISICA_DER, todoFisico);

				}else {
					log.info(raizLog + "La maquina se encontro con la variable ["+ Consts.ERROR_MAQUINA+"] en valor ["+errorMaquina+"]");
				}
				
				
				// SE MARCA EL FIN DEL ESTADO
				log.info(raizLog
						+ "-----------------------FIN ESTADO validarFormaEntregaDER---------------------------------");

				// SE ALMACENA CONTEXTO DE LA SSM EN BD
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out
							.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}

			};
		}
		
		
		
		
		
		
		/**
		 * Metodo que permite esperar la respuesta http de la digitalizacion en
		 * Enterprise Scan
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> esperarDigitalizarDer() {
			// SE RETORNA EL CONTEXTO DE LA MÁQUINA
			return (context) -> {
				// SE CARGA LA RAIZ LOG
				String raizLog = SSMUtils.getRaizLog(context);
				// SE MARCA EL INICIO DEL ESTADO
				log.info(raizLog
						+ "----------------------- INICIO ESTADO esperarDigitalizarDer ---------------------------------");
				// SE VALIDA QUE NO EXISTAN ERRORES CARGADOS EN LA MÁQUINA
				Boolean errorMaquina = context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class);
				if (!errorMaquina) {
					log.info(raizLog + "Maquina esperando siguiente llamado del controller de digitalización .....");
				}else {
					log.info(raizLog + "La maquina se encontro con la variable ["+ Consts.ERROR_MAQUINA+"] en valor ["+errorMaquina+"]");
				}
				
				
				// SE MARCA EL FIN DEL ESTADO
				log.info(raizLog
						+ "-----------------------FIN ESTADO esperarDigitalizarDer---------------------------------");

				// SE ALMACENA CONTEXTO DE LA SSM EN BD
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out
							.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}

			};

		}
		
		
		
		/**
		 * Metodo que permite cargar la categoria Correspondencia con los metadatos
		 * obtenidos del formulario
		 * 
		 * @return
		 */
		@SuppressWarnings("unchecked")
		@Bean
		public Action<States, Events> cargarCategoriaDocDigitalizadoDer() {
			// SE RETORNA EL CONTEXTO DE LA MÁQUINA
			return (context) -> {
				// SE CARGA LA RAIZ LOG
				String raizLog = SSMUtils.getRaizLog(context);
				// SE MARCA EL INICIO DEL ESTADO
				log.info(raizLog
						+ "----------------------- INICIO ESTADO cargarCategoriaFormularioDer ---------------------------------");
				// SE VALIDA QUE NO EXISTAN ERRORES CARGADOS EN LA MÁQUINA
				Boolean errorMaquina = context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class);
				if (!errorMaquina) {
					// SE CARGAN LOS HEADERS ENVIADOS DESDE EL CONTROLLER AL APROBAR DOC
					String idDocsDigitalizados = context.getMessageHeaders().get(Consts.ID_DOCS_DIGITALIZADOS_DER,
							String.class);
					// SE ALMACENA VARIABLE EN EL CONTEXTO DE LS SSM
					context.getExtendedState().getVariables().put(Consts.ID_DOCS_DIGITALIZADOS_DER,
							idDocsDigitalizados);

					// SE CARGAN VARIABLES ALAMACENADAS EN EL CONTEXTO DE LA SSM
					String workId = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);

					// SE CONVIERTEN LOS IDS DE LOS DOC DIGITALIZADOS EN UN ARRAY
					log.info(raizLog + "INICIO idDocsDigitalizados:  [" + idDocsDigitalizados + "]");
					List<Long> listIdDocsDigitalizados = new ArrayList<>();

					if (idDocsDigitalizados != null) {
						idDocsDigitalizados = idDocsDigitalizados.replace("{", "");
						idDocsDigitalizados = idDocsDigitalizados.replace("}", "");
						listIdDocsDigitalizados = (Arrays.asList(idDocsDigitalizados.split("\\s*,\\s*"))).stream()
								.map(Long::parseLong).collect(Collectors.toList());
					}

					log.info(raizLog + "Cantidad de Documentos: [" + listIdDocsDigitalizados.size() + "]");

					// POR LOGICA DE NEGOCIO, SOLO DEBE EXISTIR UN UNICO DOC DIGITALIZADO Y ESTE ES
					// EL QUE SE DEBE SUBIR AL CONSECUTIVO
					if (listIdDocsDigitalizados.stream().findAny().isPresent()) {// HAY AL MENOS UN DOCUMENTO DIGITALIZADO
						Optional<Long> idDocDigitalizadoOpt = listIdDocsDigitalizados.stream().findAny();
						Long idDocDigitalizado = idDocDigitalizadoOpt.get();
						log.info(raizLog + "Se va a utilizar el documento con id [" + idDocDigitalizado  + "]");

						// SE ALMACENA VARIABLE EN EL CONTEXTO DE LS SSM
						context.getExtendedState().getVariables().put(Consts.ID_DOC_DIGITALIZADO_DER,
								idDocDigitalizado);

						// CARGAR CAT DE CORRESPONDENCIA AL DOC DIGITALIZADO
						Map<String, Object> metadatos = (Map<String, Object>) context.getExtendedState()
								.get(Consts.METADATOS_CATEGORIA_CORR_DER, Object.class);
						List<Map<String, Object>> listSubGrupoDestino = (List<Map<String, Object>>) context
								.getExtendedState().get(Consts.METADATOS_SUBGRUPO_DESTINO_CAT_CORR_DER, Object.class);
						List<Map<String, Object>> listSubGrupoOrigen = (List<Map<String, Object>>) context
								.getExtendedState().get(Consts.METADATOS_SUBGRUPO_ORIGEN_CAT_CORR_DER, Object.class);
						// SE ADICIONA CATEGORIA DE CORR
						try {
							// SOLO HAY UN ORIGEN, POR ESO NO HAY ITERACIONES
							log.info(raizLog + "INICIO Crear categoria");
							ContenidoDocumento.adicionarCategoriaMetadata(autenticacionCS.getAdminSoapHeader(),
									idDocDigitalizado, categoriaProps.getCorrespondencia().getId(),
									categoriaProps.getCorrespondencia().getNombre(), metadatos,
									categoriaProps.getCorrespondencia().getSubgrupoOrigen(), listSubGrupoOrigen.get(0),
									Boolean.TRUE, wsdlsProps.getDocumento());
							// SE DEBEN CARGAR LOS VALORES DE TODOS LOS DESTINOS
							for (Map<String, Object> mapDestino : listSubGrupoDestino) {
								ContenidoDocumento.adicionarCategoriaMetadata(autenticacionCS.getAdminSoapHeader(),
										idDocDigitalizado, categoriaProps.getCorrespondencia().getId(),
										categoriaProps.getCorrespondencia().getNombre(), metadatos,
										categoriaProps.getCorrespondencia().getSubgrupoDestino(), mapDestino,
										Boolean.FALSE, wsdlsProps.getDocumento());
							}
							
							log.info(raizLog + "FIN Crear categoria");
							
							
							context.getStateMachine().sendEvent(Events.IR_ESPERAR_CALIDAD_DIGITALIZAR_DER);
							log.info(raizLog + "Se envió el evento: [" + Events.IR_ESPERAR_CALIDAD_DIGITALIZAR_DER
									+ "]  para la maquina con workId: [" + workId + "]");
							

						} catch (ServerSOAPFaultException | IOException | SOAPException e) {
							log.error(raizLog
									+ "ErrorCorrespondencia CARGAR_CATEGORIA_DOC_DIGITALIZADO_DER adicionarCategoriaMetadata",
									e);
							cargarError(context, e);
							context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO,
									States.CARGAR_CATEGORIA_DOC_DIGITALIZADO_DER);
							context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
							log.info(raizLog + "Se envió el evento: [" + Events.ERROR_REINTENTO
									+ "]  para la maquina con workId: [" + workId + "]");

						} catch (Exception e) {
							cargarError(context, e);
							context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, true);
							context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
							log.info(raizLog + "Se envió el evento: [" + Events.IR_FINALIZADO_ERRORES
									+ "]  para la maquina con workId: [" + workId + "]");
							log.error(raizLog
									+ "ErrorCorrespondencia CARGAR_CATEGORIA_DOC_DIGITALIZADO_DER adicionarCategoriaMetadata "
									+ e, e);
						}
					}

				}else {
					log.info(raizLog + "La maquina se encontro con la variable ["+ Consts.ERROR_MAQUINA+"] en valor ["+errorMaquina+"]");
				}
				
				// SE MARCA EL FIN DEL ESTADO
				log.info(raizLog
						+ "-----------------------FIN ESTADO cargarCategoriaFormularioDer---------------------------------");

				// SE ALMACENA CONTEXTO DE LA SSM EN BD
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out
							.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}

			};
		}
		
		
		
		
		/**
		 * Metodo que permite esperar la respuesta http de la revision de calidad de la
		 * digitalizacion
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> esperarCalidadDigitalizarDer() {
			// SE RETORNA EL CONTEXTO DE LA MÁQUINA
			return (context) -> {
				// SE CARGA LA RAIZ LOG
				String raizLog = SSMUtils.getRaizLog(context);
				// SE MARCA EL INICIO DEL ESTADO
				log.info(raizLog
						+ "----------------------- INICIO ESTADO esperarCalidadDigitalizarDer ---------------------------------");
				// SE VALIDA QUE NO EXISTAN ERRORES CARGADOS EN LA MÁQUINA
				Boolean errorMaquina = context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class);
				if (!errorMaquina) {
					log.info(raizLog + "Maquina esperando siguiente llamado del controller de calidad .....");					
				} else {
					log.info(raizLog + "La maquina se encontro con la variable [" + Consts.ERROR_MAQUINA
							+ "] en valor [" + errorMaquina + "]");
				}

				// SE MARCA EL FIN DEL ESTADO
				log.info(raizLog
						+ "-----------------------FIN ESTADO esperarCalidadDigitalizarDer---------------------------------");

				// SE ALMACENA CONTEXTO DE LA SSM EN BD
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out
							.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}
			};

		}
		
		
		
		
		
		/**
		 * Metodo que permite subir el documento escaneado/Formulario de DER a la carpeta consecutivo
		 * @return
		 */
		@Bean
		public Action<States, Events> subirDocConsecutivoDer() {
			return (context) -> {
				// SE CARGA LA RAIZ LOG
				String raizLog = SSMUtils.getRaizLog(context);
				// SE MARCA EL INICIO DEL ESTADO
				log.info(raizLog
						+ "----------------------- INICIO ESTADO subirDocConsecutivoDER ---------------------------------");
				// SE VALIDA QUE NO EXISTAN ERRORES CARGADOS EN LA MÁQUINA
				Boolean errorMaquina = context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class);
				if (!errorMaquina) {					
					// SE CARGAN VARIABLES ALMACENADAS EN EL CONTEXTO DE LA SSM
					Boolean isDistribucionFisica = context.getExtendedState().get(Consts.ES_DISTRIBUCION_FISICA_DER, Boolean.class);
					Boolean esCorreoDer = context.getExtendedState().get(Consts.DER_CORREO, Boolean.class);
					String numeroRadicadoObtenido = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO, String.class);
					String workId = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);
					Long idDocFormulario = context.getExtendedState().get(Consts.ID_DOC_RADICACION, Long.class);
					Long idDocCorreo = context.getExtendedState().get(Consts.ID_DOC_RADICACION, Long.class);
					Long idDocDigitalizado = context.getExtendedState().get(Consts.ID_DOC_DIGITALIZADO_DER, Long.class);
					//String idDocsDigitalizados = context.getExtendedState().get(Consts.ID_DOCS_DIGITALIZADOS_DER, String.class);
					
					//SE BUSCA ID DE LA CATEGORIA, BASADO EN LA FECHA ACTUAL
					LocalDate date = LocalDate.now();
					int anho = date.getYear();
					int mes = date.getMonthValue();
					log.info(raizLog + "Se va a buscar la carpeta correspondiente al año ["+anho+"] y el mes ["+mes+"] en el archivo XML del servidor OTBR");
					Long parentIdDestino = readXML.getIdFolderXML(anho, mes,  "BR", "ConsecutivoDER", globalProperties.getRutaXML());
					log.info(raizLog + "Id consecutivo encontrado [" + parentIdDestino + "]");
					//TODO SE DEBE VALIDAR QUE EL ID EXISTA,  SI NO EXISTE INFORMAR AL ADMIN QUE NO SE PUDO CARGAR Y SEGUIR PROCESO DE DIST			
					
					
					//SE PRE-CARGAN LOS METADATOS DE LA CATEGORIA BR01-DOCUMENTO
					String nombreCategoriaDocumento = categoriaDocProps.getNombreCategoria();
					int idCategoriaDocumento = Integer.parseInt(categoriaDocProps.getId().toString());
					log.info(raizLog + "Se van a precargar los metadatos de la categoria ["+nombreCategoriaDocumento+"]");
					Map<String, Object> metadatosCatDoc = new HashMap<String, Object>();
					String tipoDocumental = readXML.getTypeDcocumentXML(date.getYear(), date.getMonthValue(), "BR", "ConsecutivoDER", globalProperties.getRutaXML());
					String serie = readXML.getSerieDocumentXML(date.getYear(), date.getMonthValue(), "BR", "ConsecutivoDER", globalProperties.getRutaXML());
					metadatosCatDoc.put("Tipo documental", tipoDocumental);
					metadatosCatDoc.put("Serie", serie);
					log.info(raizLog + "Se han precargado los metadatos: 'Tipo documental' ["+tipoDocumental+"]  -  'Serie' [" +serie+"]" );
					
					//SE INICIALIZAN VARIABLES
					Long idDocConsecutivo = null;			
					
					String estadoDistribuido = estadosProps.getDistribuido();
					
					//SE VALIDA EL TIPO DE DISTRIBUCIÓN
					if(isDistribucionFisica) { // DISTRIBUCIÓN COMPLETAMENTE FISICA
						log.info(raizLog + "Todas las distribuciones son Fisicas, se va a copiar el formulario DER en el consecutivo DER");	
						String nuevoNombreDocConsecutivoDer = numeroRadicadoObtenido+".pdf";//SE CARGA NUEVO NOMBRE DEL DOC EN EL CONSECUTIVO
						//SE AGREGA LA CATEGORIA BR01-DOCUMENTO AL FORMULARIO EN LOS ADJUNTOS DEL WF
						try {
							String nombreSubGrupo = null;
							Map<String, Object> subGrupoMetadatos = null;
							ContenidoDocumento.adicionarCategoriaMetadata(autenticacionCS.getAdminSoapHeader(), idDocFormulario, idCategoriaDocumento, nombreCategoriaDocumento, metadatosCatDoc, nombreSubGrupo, subGrupoMetadatos, Boolean.TRUE, wsdlsProps.getDocumento());
							log.info(raizLog + "Se cargó correctamente la categoria ["+nombreCategoriaDocumento+"] al formulario DER con idDoc ["+idDocFormulario+"]"); 
						} catch (Exception e) {
							//SE ENVIA MENSAJE AL ADMINISTRADOR CON EL ERROR, NO SE DETIENE EL PROCESO
							log.error(raizLog + "Se presentó error al intentar agregar categoria [" + nombreCategoriaDocumento+"] al formulario de radicacion DER con idDoc ["+idDocFormulario+"]");
							String mensajeError = "No se ha podido cargar la categoria '"+nombreCategoriaDocumento+"' al formulario de radicacion DER con idDoc '"+idDocFormulario+"' \n"
									+ "Debido a esta falla no se pudo cargar el documento en el consecutivo DER. Deberá cargarlo manualmente";
							String estadoActualError = context.getStateMachine().getState().getId().name().toString();
							SSMUtils.notificarErrorAdmin(raizLog, workId, numeroRadicadoObtenido, mensajeError, estadoActualError, globalProperties, autenticacionCS, wsdlsProps);
							//TO AVOID EXECUTION OF FURTHER CODE
							//return;							
						}
						
						//SE ACTUALIZA EL ESTADO DE LA CATEGORIA 'CORRESPONDENCIA' A 'DISTRIBUIDO'
						try {
							actualizarEstadoCorrespondencia(raizLog, idDocFormulario, estadoDistribuido);
							log.info(raizLog + "Se actualizó el metadato 'Estado' de la cat Correspondencia con el valor ["+estadoDistribuido+"]");
						} catch (SOAPException e1) {
							//SE ENVIA MENSAJE AL ADMINISTRADOR CON EL ERROR, NO SE DETIENE EL PROCESO
							log.error(raizLog + "Se presentó error al intentar actualizar el estado de la categoria 'CORRESPONDENCIA' en el formulario de radicacion DER con idDoc ["+idDocFormulario+"]");
							String mensajeError = "No se ha podido actualizar el estado de la categoria de CORRESPONDENCIA a '"+estadoDistribuido+"' en el formulario de radicacion DER con idDoc '"+idDocFormulario+"' \n"
									+ "Debido a esta falla no se pudo cargar el documento en el consecutivo DER. Deberá cargarlo manualmente";
							String estadoActualError = context.getStateMachine().getState().getId().name().toString();
							SSMUtils.notificarErrorAdmin(raizLog, workId, numeroRadicadoObtenido, mensajeError, estadoActualError, globalProperties, autenticacionCS, wsdlsProps);
						}
						
						//SE COPIA EL FORMULARIO EN EL CONSECUTIVO
						try {
							idDocConsecutivo = ContenidoDocumento.copiarDocCatDocumento(autenticacionCS.getAdminSoapHeader(), idDocFormulario, parentIdDestino, nuevoNombreDocConsecutivoDer, TipoCopiadoDocumento.ORIGINAL.toString(), wsdlsProps.getDocumento());
							log.info(raizLog + "Se ha copiado exitosamente en formulario en el consecutivo con idDocConsecutivo ["+idDocConsecutivo+"]");
						} catch (SOAPException | IOException | InterruptedException e) {
							//SE ENVIA MENSAJE AL ADMINISTRADOR CON EL ERROR, NO SE DETIENE EL PROCESO
							log.error(raizLog + "Se presentó error al intentar copiar el formulario de radicacion DER con idDoc ["+idDocFormulario+"] en la carpeta de consecutivo DER con idParent ["+parentIdDestino+"]");
							String mensajeError = "Se presentó error al intentar copiar el formulario de radicacion DER con idDoc '"+idDocFormulario+"' en la carpeta de consecutivo DER con idParent '"+parentIdDestino+"' \n"
									+ "Debido a esta falla no se pudo cargar el documento en el consecutivo DER. Deberá cargarlo manualmente";
							String estadoActualError = context.getStateMachine().getState().getId().name().toString();
							SSMUtils.notificarErrorAdmin(raizLog, workId, numeroRadicadoObtenido, mensajeError, estadoActualError, globalProperties, autenticacionCS, wsdlsProps);
						}
						
					} else if(!esCorreoDer) { // PASO POR DIGITALIZACIÓN (DIST ELECTRÓNICA COMPLETA O PARCIALMENTE) Y NO ES CORREO DER
						log.info(raizLog
								+ "Todas o algunas de las distribuciones son Electrónicas, se va a copiar el documento digitalizado y aprobado en el consecutivo DER");
						String nuevoNombreDocConsecutivoDer = numeroRadicadoObtenido+".pdf";//SE CARGA NUEVO NOMBRE DEL DOC EN EL CONSECUTIVO
						// SE ACTUALIZA EL ESTADO DE LA CATEGORIA 'CORRESPONDENCIA' A 'DISTRIBUIDO'
						try {
							actualizarEstadoCorrespondencia(raizLog, idDocDigitalizado, estadoDistribuido);
							log.info(raizLog
									+ "Se actualizó el metadato 'Estado' de la cat Correspondencia con el valor ["
									+ estadoDistribuido + "]");
						} catch (SOAPException e1) {
							//SE ENVIA MENSAJE AL ADMINISTRADOR CON EL ERROR, NO SE DETIENE EL PROCESO
							log.error(raizLog + "Se presentó error al intentar actualizar el estado de la categoria 'CORRESPONDENCIA' en el doc digitalizado DER con idDoc ["+idDocDigitalizado+"]");
							String mensajeError = "No se ha podido actualizar el estado de la categoria de CORRESPONDENCIA a '"+estadoDistribuido+"' en el documento digitalizado DER con idDoc '"+idDocDigitalizado+"' \n"
									+ "Debido a esta falla no se pudo cargar el documento en el consecutivo DER. Deberá cargarlo manualmente";
							String estadoActualError = context.getStateMachine().getState().getId().name().toString();
							SSMUtils.notificarErrorAdmin(raizLog, workId, numeroRadicadoObtenido, mensajeError, estadoActualError, globalProperties, autenticacionCS, wsdlsProps);
						}

						// SE AGREGA LA CATEGORIA BR01-DOCUMENTO AL DOC DIGITALIZADO EN LOS ADJUNTOS DEL
						// WF
						try {
							String nombreSubGrupo = null;
							Map<String, Object> subGrupoMetadatos = null;
							ContenidoDocumento.adicionarCategoriaMetadata(autenticacionCS.getAdminSoapHeader(),
									idDocDigitalizado, idCategoriaDocumento, nombreCategoriaDocumento, metadatosCatDoc,
									nombreSubGrupo, subGrupoMetadatos, Boolean.TRUE, wsdlsProps.getDocumento());
							log.info(raizLog + "Se cargó correctamente la categoria [" + nombreCategoriaDocumento
									+ "] al formulario con idDoc [" + idDocDigitalizado + "]");
						} catch (Exception e) {
							//SE ENVIA MENSAJE AL ADMINISTRADOR CON EL ERROR, NO SE DETIENE EL PROCESO
							log.error(raizLog + "Se presentó error al intentar agregar categoria [" + nombreCategoriaDocumento+"] al doc digitalizado DER con idDoc ["+idDocDigitalizado+"]");
							String mensajeError = "No se ha podido cargar la categoria '"+nombreCategoriaDocumento+"' al documento digitalizado DER con idDoc '"+idDocDigitalizado+"' \n"
									+ "Debido a esta falla no se pudo cargar el documento en el consecutivo DER. Deberá cargarlo manualmente";
							String estadoActualError = context.getStateMachine().getState().getId().name().toString();
							SSMUtils.notificarErrorAdmin(raizLog, workId, numeroRadicadoObtenido, mensajeError, estadoActualError, globalProperties, autenticacionCS, wsdlsProps);
						}

						// SE COPIA EL DOC DIGITALIZADO EN EL CONSECUTIVO
						try {
							idDocConsecutivo = ContenidoDocumento.copiarDocCatDocumento(
									autenticacionCS.getAdminSoapHeader(), idDocDigitalizado, parentIdDestino,
									nuevoNombreDocConsecutivoDer, TipoCopiadoDocumento.ORIGINAL.toString(),
									wsdlsProps.getDocumento());
							log.info(raizLog
									+ "Se ha copiado exitosamente en documento digitalizado en el consecutivo con idDocConsecutivo ["
									+ idDocConsecutivo + "]");
						} catch (SOAPException | IOException | InterruptedException e) {
							//SE ENVIA MENSAJE AL ADMINISTRADOR CON EL ERROR, NO SE DETIENE EL PROCESO
							log.error(raizLog + "Se presentó error al intentar copiar el doc digitalizado DER con idDoc ["+idDocDigitalizado+"] en la carpeta de consecutivo DER con idParent ["+parentIdDestino+"]");
							String mensajeError = "Se presentó error al intentar copiar el documento digitalizado DER con idDoc '"+idDocDigitalizado+"' en la carpeta de consecutivo DER con idParent '"+parentIdDestino+"' \n"
									+ "Debido a esta falla no se pudo cargar el documento en el consecutivo DER. Deberá cargarlo manualmente";
							String estadoActualError = context.getStateMachine().getState().getId().name().toString();
							SSMUtils.notificarErrorAdmin(raizLog, workId, numeroRadicadoObtenido, mensajeError, estadoActualError, globalProperties, autenticacionCS, wsdlsProps);
						}

					}else{  // CORREO DE UN DER-EMAIL
						log.info(raizLog + "Se va a copiar el correo DER en el consecutivo DER");
						String nuevoNombreDocConsecutivoDer = numeroRadicadoObtenido+".msg";//SE CARGA NUEVO NOMBRE DEL DOC EN EL CONSECUTIVO
						//SE AGREGA LA CATEGORIA BR01-DOCUMENTO AL FORMULARIO EN LOS ADJUNTOS DEL WF
						try {
							String nombreSubGrupo = null;
							Map<String, Object> subGrupoMetadatos = null;
							ContenidoDocumento.adicionarCategoriaMetadata(autenticacionCS.getAdminSoapHeader(), idDocCorreo, idCategoriaDocumento, nombreCategoriaDocumento, metadatosCatDoc, nombreSubGrupo, subGrupoMetadatos, Boolean.TRUE, wsdlsProps.getDocumento());
							log.info(raizLog + "Se cargó correctamente la categoria ["+nombreCategoriaDocumento+"] al correo DER con idDoc ["+idDocFormulario+"]"); 
						} catch (Exception e) {
							//SE ENVIA MENSAJE AL ADMINISTRADOR CON EL ERROR, NO SE DETIENE EL PROCESO
							log.error(raizLog + "Se presentó error al intentar agregar categoria [" + nombreCategoriaDocumento+"] al correo de radicacion DER-EMAIL con idDoc ["+idDocCorreo+"]");
							String mensajeError = "No se ha podido cargar la categoria '"+nombreCategoriaDocumento+"' al correo de radicacion DER-EMAIL con idDoc '"+idDocCorreo+"' \n"
									+ "Debido a esta falla no se pudo cargar el documento en el consecutivo DER. Deberá cargarlo manualmente";
							String estadoActualError = context.getStateMachine().getState().getId().name().toString();
							SSMUtils.notificarErrorAdmin(raizLog, workId, numeroRadicadoObtenido, mensajeError, estadoActualError, globalProperties, autenticacionCS, wsdlsProps);
							//TO AVOID EXECUTION OF FURTHER CODE
							//return;							
						}
						
						//SE ACTUALIZA EL ESTADO DE LA CATEGORIA 'CORRESPONDENCIA' A 'DISTRIBUIDO'
						try {
							actualizarEstadoCorrespondencia(raizLog, idDocCorreo, estadoDistribuido);
							log.info(raizLog + "Se actualizó el metadato 'Estado' de la cat Correspondencia con el valor ["+estadoDistribuido+"]");
						} catch (SOAPException e1) {
							//SE ENVIA MENSAJE AL ADMINISTRADOR CON EL ERROR, NO SE DETIENE EL PROCESO
							log.error(raizLog + "Se presentó error al intentar actualizar el estado de la categoria 'CORRESPONDENCIA' en el formulario de radicacion DER con idDoc ["+idDocFormulario+"]");
							String mensajeError = "No se ha podido actualizar el estado de la categoria de CORRESPONDENCIA a '"+estadoDistribuido+"' en el formulario de radicacion DER con idDoc '"+idDocFormulario+"' \n"
									+ "Debido a esta falla no se pudo cargar el documento en el consecutivo DER. Deberá cargarlo manualmente";
							String estadoActualError = context.getStateMachine().getState().getId().name().toString();
							SSMUtils.notificarErrorAdmin(raizLog, workId, numeroRadicadoObtenido, mensajeError, estadoActualError, globalProperties, autenticacionCS, wsdlsProps);
						}
						
						//SE COPIA EL FORMULARIO EN EL CONSECUTIVO
						try {
							idDocConsecutivo = ContenidoDocumento.copiarDocCatDocumento(autenticacionCS.getAdminSoapHeader(), idDocCorreo, parentIdDestino, nuevoNombreDocConsecutivoDer, TipoCopiadoDocumento.ORIGINAL.toString(), wsdlsProps.getDocumento());
							log.info(raizLog + "Se ha copiado exitosamente en correo DER en el consecutivo con idDocConsecutivo ["+idDocConsecutivo+"]");
						} catch (SOAPException | IOException | InterruptedException e) {
							//SE ENVIA MENSAJE AL ADMINISTRADOR CON EL ERROR, NO SE DETIENE EL PROCESO
							log.error(raizLog + "Se presentó error al intentar copiar el correo de radicacion DER-EMAIL con idDoc ["+idDocCorreo+"] en la carpeta de consecutivo DER con idParent ["+parentIdDestino+"]");
							String mensajeError = "Se presentó error al intentar copiar el correo de radicacion DER-EMAIL con idDoc '"+idDocCorreo+"' en la carpeta de consecutivo DER con idParent '"+parentIdDestino+"' \n"
									+ "Debido a esta falla no se pudo cargar el documento en el consecutivo DER. Deberá cargarlo manualmente";
							String estadoActualError = context.getStateMachine().getState().getId().name().toString();
							SSMUtils.notificarErrorAdmin(raizLog, workId, numeroRadicadoObtenido, mensajeError, estadoActualError, globalProperties, autenticacionCS, wsdlsProps);
						}
						
					}
					
					
					//SE CARGA EN EL CONTEXTO DE LA SSM EL ID DEL DOC CONSECUTIVO
					context.getExtendedState().getVariables().put(Consts.ID_DOC_CONSECUTIVO, idDocConsecutivo);
					
				}else {
					log.info(raizLog + "La maquina se encontro con la variable [" + Consts.ERROR_MAQUINA
							+ "] en valor [" + errorMaquina + "]");
				}

				// SE MARCA EL FIN DEL ESTADO
				log.info(raizLog
						+ "-----------------------FIN ESTADO subirDocConsecutivoDER---------------------------------");

				// SE ALMACENA CONTEXTO DE LA SSM EN BD
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}
			};

		}
		
		
		
		
		
		/**
		 * Metodo que permite realizar la distribucion DER
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> distribuirDer() {
			return (context) -> {
				// SE CARGA LA RAIZ LOG
				String raizLog = SSMUtils.getRaizLog(context);
				// SE MARCA EL INICIO DEL ESTADO
				log.info(raizLog
						+ "----------------------- INICIO ESTADO distribuirDer ---------------------------------");
				// SE VALIDA QUE NO EXISTAN ERRORES CARGADOS EN LA MÁQUINA
				Boolean errorMaquina = context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class);
				if (!errorMaquina) {
					// SE CARGAN VARIABLES ALMACENADAS EN EL CONTEXTO DE LA SSM
					@SuppressWarnings("unchecked")
					List<Destino> listDestinos = (List<Destino>) context.getExtendedState().get(Consts.LISTA_DESTINOS_DER, Object.class);
					String cddOrigen = context.getExtendedState().get(Consts.CDD_ORIGEN_DER, String.class);
					String pcrOrigen = context.getExtendedState().get(Consts.PCR_ORIGEN_DER, String.class);
					String numeroRadicadoObtenido = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO,String.class);
					Long idSolicitante = context.getExtendedState().get(Consts.ID_SOLICITANTE, Long.class);
					String workIdText = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);
					String tipoComunicacion = context.getExtendedState().get(Consts.TIPO_COMUNICACION_DER, String.class);					
					String cadenaAnexos = context.getExtendedState().get(Consts.CADENA_ANEXOS_CANTIDAD_DESCRIPCION_DER, String.class);
					String idDocsDigitalizados = context.getExtendedState().get(Consts.ID_DOCS_DIGITALIZADOS_DER, String.class);
					Boolean esCorreoDer = context.getExtendedState().get(Consts.DER_CORREO, Boolean.class);
					Long idDocCorreo = context.getExtendedState().get(Consts.ID_DOC_RADICACION, Long.class);
					
					log.info(raizLog+ "idocCorreo ["+idDocCorreo+"]");
					
					List<Long> listIdDocsDigitalizados = new ArrayList<>();
					
					if (idDocsDigitalizados != null) {
						idDocsDigitalizados = idDocsDigitalizados.replace("{", "");
						idDocsDigitalizados = idDocsDigitalizados.replace("}", "");
						listIdDocsDigitalizados = (Arrays.asList(idDocsDigitalizados.split("\\s*,\\s*"))).stream()
								.map(Long::parseLong).collect(Collectors.toList());

						context.getExtendedState().getVariables().put(Consts.ID_DOCS_DIGITALIZADOS_DER,
								idDocsDigitalizados);
					} else {
						idDocsDigitalizados = "";
					}					
									
					// SE CARGA EL ID DEL ESQQUEMA DE DISTRIBUCIÓN DER
					long mapIdDistribucion = workflowProps.getDistribucionDer().getId();
					log.info(raizLog + "Id del esquema de dsitribución DER ["+mapIdDistribucion+"]");
					
					//SE INICIALIZAN VARIABLES							
					ControladorRutas controladorRutas = new ControladorRutas(); //CONTROLADOR DE TODAS LAS RUTAS
	
					//SE BUSCAN Y SE CARGAN LAS RUTAS PARA CADA DESTINO INDIVIDUAL
					for (Destino destino : listDestinos) {					
						
						//SE EXTRAEN LOS DATOS DE CADA DESTINATARIO
						String dependenciaDestino=destino.getDependenciaDestino();
						String formaEntrega=destino.getFormaEntregaDer();
						String cddDestino=destino.getCddDestino();
						String pcrDestino=destino.getPcrDestino();
						String tipoEnvio = destino.getDescripcionObjeto();
						
						//SE MUESTRAN LOS DATOS DE CADA DESTINO
						log.info(raizLog + "Se muestran los datos de cada destino...");						
						log.info("\n \n \n"+raizLog + " INICIO dependenciaDestino [" + dependenciaDestino + "] \n");
																		
						log.info(raizLog + "cddDestino [" + cddDestino + "]");
						log.info(raizLog + "pcrDestino [" + pcrDestino + "]");
						log.info(raizLog + "formaEntrega [" + formaEntrega + "]");
						log.info(raizLog + "tipoEnvio [" + tipoEnvio + "]");	
						
						log.info("\n"+raizLog + " FIN dependenciaDestino [" + dependenciaDestino + "] \n \n \n");	
						
						//SE CARGA UNA NUEVA RAIZ LOG POR DESTINO
						final String raizLogDestino = raizLog + " ["+dependenciaDestino +"] - ";
						
						//LOGICA DE CONTROLADOR DE RUTAS
						List<Posicion> pasosRuta = new ArrayList<Posicion>();
						CaculadorRuta calculador = CalculadorRutaFactory.crearCalculadorRuta("DER");
					
						pasosRuta = calculador.getRutaRoles(pcrOrigen, cddOrigen, pcrDestino,
								cddDestino, formaEntrega, tipoEnvio, rutaDistribucionRepository,
								centroDistribucionRepository, globalProperties);
						
						log.info(raizLog + "INICIO calulador de ruta DER... \n");
						log.info(raizLogDestino + "Mostrando la ruta completa:");
						pasosRuta.forEach(unaPosicion -> log.info(raizLogDestino + "Indice [" + unaPosicion.getPosicion()
						+ "] - rol [" + unaPosicion.getRol() + "]"));
						log.info("\n"+raizLog + "FIN calulador de ruta DER \n");
						
						//SE INICIARÁ DISTRIBUCIÓN DESDE EL SEGUNDO ROL YA QUE EL PRIMERO RADICÓ
						Posicion posicionInicial = pasosRuta.stream().filter(unaPosicion -> unaPosicion.getPosicion() == 2).findFirst().get();
						String rolInicial = posicionInicial.getRol();
						
						
						//SE INICIALIZAN VARIABLES PARA DESPUES CREAR INSTANCIA DEL FLUJO DE DISTRIBUCIÓN
						long idWokflowDistribucion = 0;						
						String titulo = "FT112 - Distribución DER - " + numeroRadicadoObtenido + " - " + dependenciaDestino;
						Member grupo = null;
						Long idGrupo = null;			
						List<Long> listIdDocsAdjuntos = new ArrayList<>();
						
						//SE OBTIENE EL ID DEL GRUPO ENCARGADO DE LA DIST, A PARTIR DEL NOMBRE DEL ROL INICIAL
						try {								
							grupo = UsuarioCS.getGrupoByNombre(autenticacionCS.getAdminSoapHeader(), rolInicial,
									wsdlsProps.getUsuario());
							idGrupo = grupo.getID();
							log.info(raizLogDestino + "rol inicial [" + rolInicial + "] - id [" + idGrupo + "]");

						} catch (SOAPException | IOException e) {
							log.error(raizLog + "ErrorCorrespondencia distribuirDer getGrupoByNombre", e);
							cargarError(context, e);
							context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_DER);
							context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
						}
						
						//SE CARGAN LOS ATRIBUTOS EN COMUN PARA EL PROCESO
						Map<String, ValorAtributo> atributos = new HashMap<String, ValorAtributo>();

						atributos.put(workflowProps.getDistribucionDer().getAtributoWF().getTipoComunicacion(),	new ValorAtributo(tipoComunicacion, TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionDer().getAtributoWF().getRolSiguienteId(),new ValorAtributo(idGrupo, TipoAtributo.USER));
						atributos.put(workflowProps.getDistribucionDer().getAtributoWF().getRolSiguienteNombre(),new ValorAtributo(rolInicial, TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionDer().getAtributoWF().getRolOrigen(),new ValorAtributo(idSolicitante, TipoAtributo.USER));
						atributos.put(workflowProps.getDistribucionDer().getAtributoWF().getFormaEntrega(),new ValorAtributo(formaEntrega, TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionDer().getAtributoWF().getDespendenciaDestino(),new ValorAtributo(dependenciaDestino, TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionDer().getAtributoWF().getNumeroRadicado(),new ValorAtributo(numeroRadicadoObtenido, TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionDer().getAtributoWF().getWorkId(),new ValorAtributo(workIdText, TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionDer().getAtributoWF().getTipoEnvio(),new ValorAtributo(tipoEnvio, TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionDer().getAtributoWF().getEsEmail(), new ValorAtributo(esCorreoDer, TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionDer().getAtributoWF().getAnexos(),new ValorAtributo(cadenaAnexos, TipoAtributo.MULTILINE));						
						//SE VALIDA EL TIPO DE DISTRIBUCIÓN POR DESTINO
						boolean isFisico = false;						
						if (formaEntrega.equalsIgnoreCase(globalProperties.getDerObjetoFisico())) { // FISICA							
							isFisico = true;
							log.info(raizLogDestino + "Distribucion DER - Físico");							
						}else if (formaEntrega.equalsIgnoreCase(globalProperties.getDerObjetoElectronicoyFisico())) { //ELECTRÓNICO Y FISICO}
							listIdDocsAdjuntos.clear();
							listIdDocsAdjuntos.addAll(listIdDocsDigitalizados);
							atributos.put(workflowProps.getDistribucionDer().getAtributoWF().getEsElectronico(),new ValorAtributo("true", TipoAtributo.STRING));
							log.info(raizLogDestino + "Distribucion DER - Electrónico y Físico");
						}else if (formaEntrega.equalsIgnoreCase(globalProperties.getDerObjetoElectronico()) && !esCorreoDer) { // ELECTRONICO
							listIdDocsAdjuntos.clear();
							listIdDocsAdjuntos.addAll(listIdDocsDigitalizados);
							atributos.put(workflowProps.getDistribucionDer().getAtributoWF().getEsElectronico(),new ValorAtributo("true", TipoAtributo.STRING));
							log.info(raizLog + "Distribucion DER - Electrónico");
						} else if (formaEntrega.equalsIgnoreCase(globalProperties.getDerObjetoElectronico()) && esCorreoDer) { //CORREO DER EMAIL
							listIdDocsAdjuntos.clear();
							listIdDocsAdjuntos.add(idDocCorreo);
							atributos.put(workflowProps.getDistribucionDer().getAtributoWF().getEsElectronico(),new ValorAtributo("true", TipoAtributo.STRING));
						}
						
						//SE INICIA CADA HILO DE DIST DER
						log.info(raizLog + "INICIO WF Distribucion DER hilos");
						try {
								idWokflowDistribucion = Workflow.iniciarWorkflowAtributosAdjuntos(
										autenticacionCS.getAdminSoapHeader(), mapIdDistribucion, titulo, atributos,
										listIdDocsAdjuntos, wsdlsProps.getWorkflow());

								log.info(raizLog + "Iniciado WorkFLow de distribución con con ID ["
										+ idWokflowDistribucion + "]");

						} catch (NumberFormatException | NoSuchElementException | IOException e) {
							cargarError(context, e);
							context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
							log.info(raizLog
									+ "ErrorCorrespondencia distribuirDer iniciarWorkflowAtributosAdjuntos " + e,
									e);
						} catch (SOAPException | WebServiceException e) {
							log.error(raizLog
									+ "ErrorCorrespondencia distribuirDer iniciarWorkflowAtributosAdjuntos " + e,
									e);
							cargarError(context, e);
							context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_DER);
							context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
						}

						log.info(raizLog + "FIN WF Distribucion DER hilos");
						
						
						//SE AGREGA LA RUTA AL CONTROLADOR
						controladorRutas.addRuta(idWokflowDistribucion, pasosRuta, posicionInicial, isFisico);						
					}
					
					log.info("\n" + raizLog + "Finalizó obtener rutas");
					log.info(raizLog + "Numero total de rutas [" + controladorRutas.getCantidadRutas() + "]");
					log.info(raizLog+"Información completa de las rutas:");
					List<RutaHandler> rutasLista = controladorRutas.getRutas();
					for(RutaHandler unaRuta: rutasLista) {
						log.info(raizLog + unaRuta.toString());
					}					
					
					//SE ALMACENA CONTROLADOR DE RUTAS EN EL CONTEXTO DE LA SSM
					context.getExtendedState().getVariables().put(Consts.CONTROLADOR_RUTAS, controladorRutas);
					
					//SE ENVIA EVENTO PARA PASAR AL SIGUIENTE ESTADO
					context.getStateMachine().sendEvent(Events.IR_ESPERAR_RESULTADO_DISTRIBUIR_DER);
					log.info(raizLog + "Se envió el evento: [" + Events.IR_ESPERAR_RESULTADO_DISTRIBUIR_DER
							+ "]  para la maquina con workId: [" + workIdText + "]");
					
				}else {
					log.info(raizLog + "La maquina se encontro con la variable [" + Consts.ERROR_MAQUINA
							+ "] en valor [" + errorMaquina + "]");
				}				
				

				// SE MARCA EL FIN DEL ESTADO
				log.info(raizLog
						+ "-----------------------FIN ESTADO distribuirDer---------------------------------");

				// SE ALMACENA CONTEXTO DE LA SSM EN BD				
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}
			};
			
		}
		
		
		
		
		/**
		 * Metodo que permite esperar el resultado de la distribucion DER
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> esperarDistribucionDer() {
			// SE RETORNA EL CONTEXTO DE LA MÁQUINA
			return (context) -> {
				// SE CARGA LA RAIZ LOG
				String raizLog = SSMUtils.getRaizLog(context);
				// SE MARCA EL INICIO DEL ESTADO
				log.info(raizLog
						+ "----------------------- INICIO ESTADO esperarDistribucionDer ---------------------------------");
				// SE VALIDA QUE NO EXISTAN ERRORES CARGADOS EN LA MÁQUINA
				Boolean errorMaquina = context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class);
				if (!errorMaquina) {
					log.info(raizLog + "Maquina esperando siguiente llamado del controller de distribución .....");
				} else {
					log.info(raizLog + "La maquina se encontro con la variable [" + Consts.ERROR_MAQUINA
							+ "] en valor [" + errorMaquina + "]");
				}

				// SE MARCA EL FIN DEL ESTADO
				log.info(raizLog
						+ "-----------------------FIN ESTADO esperarDistribucionDer---------------------------------");

				// SE ALMACENA CONTEXTO DE LA SSM EN BD
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out
							.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}

			};

		}
		
		
		
		/**
		 * Metodo que permite esperar el resultado de la distribucion DER
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> actualizarEstadoFinalCorrDer() {
			// SE RETORNA EL CONTEXTO DE LA MÁQUINA
			return (context) -> {
				// SE CARGA LA RAIZ LOG
				String raizLog = SSMUtils.getRaizLog(context);
				// SE MARCA EL INICIO DEL ESTADO
				log.info(raizLog
						+ "----------------------- INICIO ESTADO actualizarEstadoFinalCorrDer ---------------------------------");
				// SE VALIDA QUE NO EXISTAN ERRORES CARGADOS EN LA MÁQUINA
				Boolean errorMaquina = context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class);
				if (!errorMaquina) {
					//SE CARGAN LOS HEADERS DEL CONTROLADOR EN EL CONTEXTO DE LA SSM
					long workidInstancia = (long) context.getMessageHeaders().get(Consts.ID_WORKFLOW_DIS);					

					//SE CARGAN LAS VARIABLES ALMACENAS EN EL CONTEXTO DE LA SSM
					ControladorRutas controladorRuta = context.getExtendedState().get(Consts.CONTROLADOR_RUTAS, ControladorRutas.class);
					String workId = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);
					Long IdDocConsecutivoDer = context.getExtendedState().get(Consts.ID_DOC_CONSECUTIVO, Long.class);

					//SE INICIALIZAN OTRAS VARIABLES DEL PROCESO
					Boolean isEnvioFisico = controladorRuta.isEnvioFisicoRuta(workidInstancia);
					String nuevoEstado = estadosProps.getConfirmado();

					
					//SE EXTRAE LA LISTA DE DOCS ADJUNTOS DEL WF
					List<Long> listaDocsAdjuntos = SSMUtils.obtenerListaIdDocAdjuntoWorkflow(workidInstancia, isEnvioFisico,
							log, raizLog, wsdlsProps, autenticacionCS);
					Long idDocumento = 0L;
					if (listaDocsAdjuntos.stream().findFirst().isPresent()) {
						idDocumento = listaDocsAdjuntos.get(0);
					}

					//SE ACTUALIZA LA CATEGORIA DEL DOCUMENTO EN EL WF DE DIST
					SSMUtils.actualizarEstadoDocumentoCorr(idDocumento, isEnvioFisico, log, raizLog, nuevoEstado, wsdlsProps,
							autenticacionCS, categoriaProps);

					//SE REVISA SI LAS RUTAS YA TERMINARON TODAS
					if (!controladorRuta.isAlgunaDistribucionPendiente()) {
						//SE ACTUALIZA EL ESTADO DE LA CATEGORIA 'CORRESPONDENCIA' A 'CONFIRMADO' EN EL CONSECUTIVO
						try {
							actualizarEstadoCorrespondencia(raizLog, IdDocConsecutivoDer, nuevoEstado);
							log.info(raizLog + "Se actualizó el metadato 'Estado' de la cat Correspondencia con el valor ["+nuevoEstado+"] en el doc consecutivo DER con idDoc ["+IdDocConsecutivoDer+"]");
						} catch (SOAPException e1) {
							// TODO INFORMAR AL ADMIN QUE NO SE PUDO ACTUALIZAR ESTADO EN AL CAT DEL EL FORMULARIO, AHORA DEBE CARGARLO/ACTUALIZARLO ÉL MISMO EN CONSECUTIVO. EL PROCESO DE DISTRIBUCION DEBE SEGUIR
							e1.printStackTrace();
						}						
						context.getStateMachine().sendEvent(Events.FINALIZAR_DER);
						log.info(raizLog + "Se envió el evento: ["+Events.FINALIZAR_DER+"]  para la maquina con workId: [" + workId +"]");
						
						log.info(raizLog + "Todas la rutas del proceso [" + workId + "] han finalizado...");
					}

				
				} else {
					log.info(raizLog + "La maquina se encontro con la variable [" + Consts.ERROR_MAQUINA
							+ "] en valor [" + errorMaquina + "]");
				}

				// SE MARCA EL FIN DEL ESTADO
				log.info(raizLog
						+ "-----------------------FIN ESTADO actualizarEstadoFinalCorrDer---------------------------------");

				// SE ALMACENA CONTEXTO DE LA SSM EN BD
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out
							.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}

			};

		}
		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////                   FIN ESTADOS ----- DER -----                       ///////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
		
		

		
		
		
		//----------------------------------------------------------------------------------------------------------------------
		
		
	
		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////                INICIO ESTADOS ----- DER EMAIL -----                 ///////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		
		
		/**
		 * Metodo que permite cargar las propiedades iniciales de un DER EMAIL
		 * @return
		 */
		@Bean
		public Action<States, Events> iniciarRadicarDerEmail() {
			// SE RETORNA EL CONTEXTO DE LA MÁQUINA
			return (context) -> {
				// SE CARGA LA RAIZ LOG
				String raizLog = SSMUtils.getRaizLog(context);
				// SE MARCA EL INICIO DEL ESTADO
				log.info("\n"+raizLog + " ------------------------- INICIO iniciarRadicarDerEmail ---------------------------------- \n");
				//SE CARGAN LAS VARIABLES DE LOS HEADERS DEL CONTROLLER DE RADICACIÓN EN EL CONTEXTO DE LA SSM
				String workId = context.getMessageHeaders().get(Consts.ID_WORKFLOW_RADICACION, String.class);	
				context.getExtendedState().getVariables().put(Consts.ID_WORKFLOW_RADICACION,workId);
				
				Long idDoc = context.getMessageHeaders().get(Consts.ID_DOC_RADICACION, Long.class);
				context.getExtendedState().getVariables().put(Consts.ID_DOC_RADICACION,idDoc);	
				
				String conexionUsuario = context.getMessageHeaders().get(Consts.CONEXION_CS_SOLICITANTE, String.class);
				context.getExtendedState().getVariables().put(Consts.CONEXION_CS_SOLICITANTE,conexionUsuario);
				
				Long idSolicitante = context.getMessageHeaders().get(Consts.ID_SOLICITANTE, Long.class);			
				context.getExtendedState().getVariables().put(Consts.ID_SOLICITANTE,idSolicitante);							
				
				String oficina = context.getMessageHeaders().get(Consts.OFICINA_DER, String.class);	
				context.getExtendedState().getVariables().put(Consts.OFICINA_DER,oficina);
				
				String pcrDestino = context.getMessageHeaders().get(Consts.PCR_DESTINO_DER, String.class);	
				context.getExtendedState().getVariables().put(Consts.PCR_DESTINO_DER,pcrDestino);
				
				String pcrOrigen = context.getMessageHeaders().get(Consts.PCR_ORIGEN_DER, String.class);	
				context.getExtendedState().getVariables().put(Consts.PCR_ORIGEN_DER,pcrOrigen);
				
				String dependenciaDestino = context.getMessageHeaders().get(Consts.DEPENDENCIA_DESTINO, String.class);	
				context.getExtendedState().getVariables().put(Consts.DEPENDENCIA_DESTINO,dependenciaDestino);
				
				String remitente = context.getMessageHeaders().get(Consts.REMITENTE, String.class);	
				context.getExtendedState().getVariables().put(Consts.REMITENTE,	remitente);				

				String tipoComunicacion  = context.getMessageHeaders().get(Consts.TIPO_COMUNICACION_DER, String.class);	
				context.getExtendedState().getVariables().put(Consts.TIPO_COMUNICACION_DER,	tipoComunicacion);
				
				String asunto = context.getMessageHeaders().get(Consts.ASUNTO, String.class);	
				context.getExtendedState().getVariables().put(Consts.ASUNTO,asunto);
				
				String destino = context.getMessageHeaders().get(Consts.DESTINO_DER_EMAIL, String.class);
				context.getExtendedState().getVariables().put(Consts.DESTINO_DER_EMAIL,destino);
				
				String correoRemitente = context.getMessageHeaders().get(Consts.CORREO_REMITENTE_DER_EMAIL, String.class);	
				context.getExtendedState().getVariables().put(Consts.CORREO_REMITENTE_DER_EMAIL,correoRemitente);
				
				String correoDestino = context.getMessageHeaders().get(Consts.CORREO_DESTINO_DER_EMAIL, String.class);	
				context.getExtendedState().getVariables().put(Consts.CORREO_DESTINO_DER_EMAIL,correoDestino);
				
				Long parentIdAdjuntos = context.getMessageHeaders().get(Consts.PARENT_ID_ADJUNTOS_DER_EMAIL, Long.class);			
				context.getExtendedState().getVariables().put(Consts.PARENT_ID_ADJUNTOS_DER_EMAIL,parentIdAdjuntos);	
				
				List<Destino> listaDestinos = new ArrayList<Destino>();
				Destino destinoDerEmail = new Destino();
				destinoDerEmail.setDependenciaDestino(destino);
				destinoDerEmail.setFormaEntregaDer(globalProperties.getDerObjetoElectronico());//Se marca la forma de entrega como electrónica
				destinoDerEmail.setPcrDestino(pcrDestino);
				destinoDerEmail.setDescripcionObjeto("Correo Electrónico");//TIPO ENVÍO
				
				listaDestinos.add(destinoDerEmail);
				context.getExtendedState().getVariables().put(Consts.LISTA_DESTINOS_DER, listaDestinos);
				
				//SE INICIALIZAN ALGUNAS VRIABLES
				Boolean errorMaquina = Boolean.FALSE;
				context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA,errorMaquina);
				Boolean esCorreoDER = Boolean.TRUE;
				context.getExtendedState().getVariables().put(Consts.DER_CORREO,esCorreoDER);
				Boolean isDistribucionFisica = Boolean.FALSE;
				context.getExtendedState().getVariables().put(Consts.ES_DISTRIBUCION_FISICA_DER,isDistribucionFisica);
				LocalDateTime date = LocalDateTime.now();
				// SE GUARDA LA VARIABLE FECHA EN EL CONTEXTO DE LA SSM
				context.getExtendedState().getVariables().put(Consts.FECHA_RADICACION, date);
				
				String cadenaAnexos = " "; //SE DEJA LA CADENA DE ANEXOS VACIA PARA QUE EL INICIO DEL WORKFLOW DE DIST NO FALLE
				context.getExtendedState().getVariables().put(Consts.CADENA_ANEXOS_CANTIDAD_DESCRIPCION_DER,cadenaAnexos);
						
				log.info(raizLog + "Se cargaron los siguientes valores iniciales en las propiedades: "
						+ Consts.ERROR_MAQUINA+": ["+errorMaquina+"] - "
						+ Consts.DER_CORREO+": ["+esCorreoDER+"] - "
						+ Consts.ES_DISTRIBUCION_FISICA_DER+": ["+isDistribucionFisica+"] - "
						+ Consts.CADENA_ANEXOS_CANTIDAD_DESCRIPCION_DER+": ["+cadenaAnexos+"] - "
						+ Consts.FECHA_RADICACION+": ["+date+"]");
				
				log.info(raizLog + "Se cargaron correctamente las propiedades iniciales de la SSM");				
				 
				// SE MARCA EL FIN DEL ESTADO
				log.info("\n"+raizLog + " ------------------------- FIN iniciarRadicarDerEmail ---------------------------------- \n");
			};

		}
		
		
		
		
		/**
		 * Metodo que permite cargar la categoria Correspondencia con los metadatos
		 * obtenidos del formulario DER EMAIL
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> cargarCategoriaCorreoDer() {
			// SE RETORNA EL CONTEXTO DE LA MÁQUINA
			return (context) -> {
				// SE CARGA LA RAIZ LOG
				String raizLog = SSMUtils.getRaizLog(context);
				// SE MARCA EL INICIO DEL ESTADO
				log.info(raizLog
						+ "----------------------- INICIO ESTADO cargarCategoriaCorreoDer ---------------------------------");
				// SE VALIDA QUE NO EXISTAN ERRORES CARGADOS EN LA MÁQUINA
				Boolean errorMaquina = context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class);
				if (!errorMaquina) {					
					// SE CARGAN VARIABLES ALMACENADAS EN EL CONTEXTO DE LA SSM
					@SuppressWarnings("unchecked") // Se anhade porque se está seguro que la lista es del tipo indicado
					List<Destino> listaDestinatarios = (List<Destino>) context.getExtendedState().get(Consts.LISTA_DESTINOS_DER, Object.class);
					String workId = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);					
					String remitente = context.getExtendedState().get(Consts.REMITENTE,String.class);
					remitente = SSMUtils.truncarString(remitente, Consts.TAMANO_MAXIMO_CS_REMITENTE_DER);
					String correoRemitente = context.getExtendedState().get(Consts.CORREO_REMITENTE_DER_EMAIL,String.class);
					correoRemitente = SSMUtils.truncarString(correoRemitente, Consts.TAMANO_MAXIMO_CS_REMITENTE_DER);
					String numeroRadicadoObtenido = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO,String.class);
					Long parentIdAdjuntos = context.getExtendedState().get(Consts.PARENT_ID_ADJUNTOS_DER_EMAIL,Long.class);
					
					log.info(raizLog + "EL radicado DER generado es: [" + numeroRadicadoObtenido+"]");
					
					String nuevoNombreDocCorreoDer = numeroRadicadoObtenido+".msg";
					
					Long idDocCorreoOriginal = context.getExtendedState().get(Consts.ID_DOC_RADICACION, Long.class);

					log.info(raizLog + "El id Correo Orignal es: [" + idDocCorreoOriginal+"]");
					
					//SE DEBE COPIAR ARCHIVO EN LOS ADJUNTOS DEL WF DE RADICACIÓN
					Long idDocCorreoWf = null;
					try {						
						idDocCorreoWf = ContenidoDocumento.copiarDocCatDocumento(autenticacionCS.getAdminSoapHeader(), idDocCorreoOriginal, parentIdAdjuntos, nuevoNombreDocCorreoDer, TipoCopiadoDocumento.ORIGINAL.toString(), wsdlsProps.getDocumento());
						log.info(raizLog + "Se ha copiado exitosamente el correo en los adjuntos el WF con idCorreoWf ["+idDocCorreoWf+"]");
						//SE ALMACENA EL ID DEL CORREO COPIADO EN EL CONTEXTO DE LA SSM
						context.getExtendedState().getVariables().put(Consts.ID_DOC_RADICACION,idDocCorreoWf);	
					} catch (SOAPException | IOException | InterruptedException e) {
						//SE ENVIA MENSAJE AL ADMINISTRADOR CON EL ERROR, NO SE DETIENE EL PROCESO
						log.error(raizLog + "Se presentó error al intentar copiar el correo con id ["+idDocCorreoOriginal+"] en la carpeta de adjuntos del WF idParent ["+parentIdAdjuntos+"]");
						String mensajeError = "Se presentó error al intentar copiar el correo con id '"+idDocCorreoOriginal+"' en la carpeta de adjuntos del WF con idParent '"+parentIdAdjuntos+"' \n"
								+ "Debido a esta falla no se pudo cargar el documento en los adjuntos del WF. Deberá cargarlo manualmente";
						String estadoActualError = context.getStateMachine().getState().getId().name().toString();
						SSMUtils.notificarErrorAdmin(raizLog, workId, numeroRadicadoObtenido, mensajeError, estadoActualError, globalProperties, autenticacionCS, wsdlsProps);
					}
					
					//SE DEBE ELIMINAR EL ARCHIVO ORIGINAL
					try {
						ContenidoDocumento.eliminarDocumento(autenticacionCS.getAdminSoapHeader(), idDocCorreoOriginal, wsdlsProps.getDocumento());
						log.info(raizLog + "Se ha eliminado correctamente el documento correo con id ["+idDocCorreoOriginal+"]");
					} catch (SOAPException | IOException | InterruptedException e) {
						//SE ENVIA MENSAJE AL ADMINISTRADOR CON EL ERROR, NO SE DETIENE EL PROCESO
						log.error(raizLog + "Se presentó error al intentar eliminar el correo con id ["+idDocCorreoOriginal+"]");
						String mensajeError = "Se presentó error al intentar eliminar el correo con id '"+idDocCorreoOriginal+"' \n"
								+ "Debido a esta falla no se pudo eliminar el correo de la carpeta original. Deberá eliminarlo manualmente";
						String estadoActualError = context.getStateMachine().getState().getId().name().toString();
						SSMUtils.notificarErrorAdmin(raizLog, workId, numeroRadicadoObtenido, mensajeError, estadoActualError, globalProperties, autenticacionCS, wsdlsProps);
					}					

					// SE CREAN VARIABLES PARA LLENAR LA CATEGORIA
					DateFormat dateFormatHora = new SimpleDateFormat(globalProperties.getFormatoHora());
					// SE EXTRAE LA FECHA ALMACENADA AL RADICAR
					LocalDateTime localDateTime = context.getExtendedState().get(Consts.FECHA_RADICACION, LocalDateTime.class);
					Date date = Date.from( localDateTime.atZone( ZoneId.systemDefault()).toInstant()); //CONVERTIR FROM LOCALDATETIME TO DATE
					String hora = dateFormatHora.format(date);//Hora de radicacion
					
					log.info(raizLog + "La hora de radicacion a almacenar en la categoria : ["+hora+"]");

					String asunto = context.getExtendedState().get(Consts.ASUNTO, String.class);
					
					String tipologiaFinal = "DER - Documento Externo Recibido";

					log.info(raizLog +  "Tipologia Final: [" + tipologiaFinal + "] - Asunto: [" + asunto + "]");
					
					List<Map<String, Object>> listSubGrupoDestino = new ArrayList<>();

					List<Map<String, Object>> listSubGrupoOrigen = new ArrayList<>();
					
					// SE CREAN LOS METADATOS A ALMACENAR EN LA CATEGORIA
					Map<String, Object> metadatos = new HashMap<>();

					metadatos.put(categoriaProps.getCorrespondencia().getAtributoTipologia(), tipologiaFinal);
					metadatos.put(categoriaProps.getCorrespondencia().getAtributoEstado(),estadosProps.getRadicado());
					metadatos.put(categoriaProps.getCorrespondencia().getAtributoAsunto(), asunto);
					metadatos.put(categoriaProps.getCorrespondencia().getAtributoFechaRadicacion(), date);
					metadatos.put(categoriaProps.getCorrespondencia().getAtributoHoraRadicacion(), hora);
					metadatos.put(categoriaProps.getCorrespondencia().getAtributoNumeroRadicacion(), numeroRadicadoObtenido);

					// SE CREAN LOS METADATOS PARA CADA DESTINO (POR LÓGICA DE NEGOCIO SÓLO EXISTE UN DESTINO, PERO IGUAL ITERAMOS)
					for (Destino destino : listaDestinatarios) {
						
						log.info(raizLog + "Metadatos DER");

						log.info(raizLog + "Destinatario: [" + destino.getDependenciaDestino()+"]");

						
						// METADATOS ORIGEN*DESTINO, IGUAL PARA TODOS
						Map<String, Object> metadatosSubGrupoOrigen = new HashMap<>();
						
							
						metadatosSubGrupoOrigen.put("Nombre", remitente);
						metadatosSubGrupoOrigen.put("Cargo", "");
						metadatosSubGrupoOrigen.put("Dependencia", correoRemitente);
						listSubGrupoOrigen.add(metadatosSubGrupoOrigen);
						
						trucarValoresCategoriaCorrespondencia(listSubGrupoOrigen);

						// METADATOS PARA CADA DESTINO
						Map<String, Object> metadatosSubGrupoDestino = new HashMap<>();

						metadatosSubGrupoDestino.put("Nombre", "");
						metadatosSubGrupoDestino.put("Cargo", "");

						String destinoStr = destino.getDependenciaDestino().trim();
						destinoStr = SSMUtils.truncarString(destinoStr, Consts.TAMANO_MAXIMO_CS_DESTINO_DER);
						metadatosSubGrupoDestino.put("Dependencia / Entidad", destinoStr);

						metadatosSubGrupoDestino.put("Dirección", "");

						listSubGrupoDestino.add(metadatosSubGrupoDestino);

						trucarValoresCategoriaCorrespondencia(listSubGrupoDestino);
					
					}
					
					//SE ALMACENAN LOS DATOS DE LA CATEGORIA EN EL CONTEXTO DE LA SSM
					context.getExtendedState().getVariables().put(Consts.METADATOS_CATEGORIA_CORR_DER, metadatos);
					context.getExtendedState().getVariables().put(Consts.METADATOS_SUBGRUPO_ORIGEN_CAT_CORR_DER, listSubGrupoOrigen);
					context.getExtendedState().getVariables().put(Consts.METADATOS_SUBGRUPO_DESTINO_CAT_CORR_DER, listSubGrupoDestino);

					// SE ADICIONA CATEGORIA DE CORR
					try {
						//SOLO HAY UN ORIGEN, POR ESO NO HAY ITERACIONES
						log.info(raizLog + "INICIO Crear categoria");
						ContenidoDocumento.adicionarCategoriaMetadata(
								autenticacionCS.getAdminSoapHeader(), idDocCorreoWf,
								categoriaProps.getCorrespondencia().getId(),
								categoriaProps.getCorrespondencia().getNombre(), metadatos,
								categoriaProps.getCorrespondencia().getSubgrupoOrigen(), listSubGrupoOrigen.get(0),
								Boolean.TRUE, wsdlsProps.getDocumento());

						// SE DEBEN CARGAR LOS VALORES DE TODOS LOS DESTINOS
						for (Map<String, Object> mapDestino : listSubGrupoDestino) {
							ContenidoDocumento.adicionarCategoriaMetadata(
									autenticacionCS.getAdminSoapHeader(), idDocCorreoWf,
									categoriaProps.getCorrespondencia().getId(),
									categoriaProps.getCorrespondencia().getNombre(), metadatos,
									categoriaProps.getCorrespondencia().getSubgrupoDestino(), mapDestino, Boolean.FALSE,
									wsdlsProps.getDocumento());
						}
						log.info(raizLog + "FIN Crear categoria");
						
						context.getStateMachine().sendEvent(Events.IR_DISTRIBUIR_DER);						
						log.info(raizLog + "Se envió el evento: ["+Events.IR_DISTRIBUIR_DER+"]  para la maquina con workId: [" + workId +"]");
						

					} catch (ServerSOAPFaultException | IOException |  SOAPException e) {
						log.error(raizLog + "ErrorCorrespondencia CARGAR_CATEGORIA_CORREO_DER adicionarCategoriaMetadata", e);
						cargarError(context, e);						
						context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.CARGAR_CATEGORIA_CORREO_DER);
						context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);						
						log.info(raizLog + "Se envió el evento: ["+Events.ERROR_REINTENTO+"]  para la maquina con workId: [" + workId +"]");
					
					} catch (Exception e) {
						cargarError(context, e);
						context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, true);
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						log.info(raizLog + "Se envió el evento: ["+Events.IR_FINALIZADO_ERRORES+"]  para la maquina con workId: [" + workId +"]");
						log.error(raizLog + "ErrorCorrespondencia CARGAR_CATEGORIA_CORREO_DER adicionarCategoriaMetadata " + e , e);
					}

				}else {
					log.info(raizLog + "La maquina se encontro con la variable ["+ Consts.ERROR_MAQUINA+"] en valor ["+errorMaquina+"]");
				}
				
				
				// SE MARCA EL FIN DEL ESTADO
				log.info(raizLog
						+ "-----------------------FIN ESTADO cargarCategoriaCorreoDer---------------------------------");

				// SE ALMACENA CONTEXTO DE LA SSM EN BD
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out
							.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}

			};
		}
		
		
		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////                FIN ESTADOS ----- DER EMAIL -----                    ///////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		
		
		
		
		//----------------------------------------------------------------------------------------------------------------------
		
		
	
		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////               INICIO ESTADOS ----- CARTA/MEMO -----                 ///////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				
		
		private void imprimirLogMensajeInicioEstado(String raizLog, String nombreEstado) {
			log.info("\n"+raizLog + "\n"+
					  "################################################################################################################################\n"+
			          "################################################################################################################################\n"+
			          "                                              INICIO - ESTADO: " + nombreEstado + "                        \n"+
			          "################################################################################################################################\n"+
			          "################################################################################################################################"
					);
		}
		
		
		private void imprimirLogMensajeFinEstado(String raizLog, String nombreEstado) {
			log.info(raizLog + "\n"+
					  "################################################################################################################################\n"+
			          "################################################################################################################################\n"+
			          "                                              FIN - ESTADO: " + nombreEstado + "                        \n"+
			          "################################################################################################################################\n"+
			          "################################################################################################################################\n"
					);
		}
		
		private void imprimirLogMensajeError(String raizLog, String nombreEstado, String mensajeError) {
			log.info(raizLog + "\n"+
					  "################################################################################################################################\n"+
			          "################################################################################################################################\n"+
			          "                                              ERROR - ESTADO: " + nombreEstado + " - ERROR: " + mensajeError + "                         \n"+
			          "################################################################################################################################\n"+
			          "################################################################################################################################\n"
					);
		}
		
		
		private void enviarEventoFinErrores(String raizLog, StateContext<States, Events> context, String workId, String nombreEstado, String mensajeError,Exception e) {
			//SE CARGA EL ERROR EN LA MÁQUINA			
			cargarError(context, e);
			//SE CAMBIAN LOS VALORES DE LAS VARIABLES DE ERROR EN EL CONTEXTO DE LA SSM
			context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, true);					 
			context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR, mensajeError);	
			e.printStackTrace();
			//SE ENVIA EVENTO DE FIN CON ERRORES
			context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
			log.info(raizLog + "Se envió el evento: ["+Events.IR_FINALIZADO_ERRORES+"]  para la maquina con workId: [" + workId +"]");
		}
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				
		/**
		 * Metodo que permite cargar propiedades iniciales en el contexto de la SSM
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> cargarPropsInicialesCaMe() {
			String nombreEstadoActual = States.CARGAR_PROPS_INICIALES_CA_ME.toString();	
			//SE RETORNA EL CONTEXTO DE LA MÁQUINA DE ESTADOS
			return (context) -> {//Se retorna contexto de la máquina de estados (Contexto)
				//SE EXTRAE LA RAIZ LOG DEL CONTEXTO
				String raizLog = SSMUtils.getRaizLog(context);//Variable para inicio de impresión del log
				
				imprimirLogMensajeInicioEstado(raizLog, nombreEstadoActual);//Se marca el incio del estado en el log
				
				log.info(raizLog + "Se van a inicializar algunas propiedades de la máquina de estados");
				
				//SE INICIALIZAN LAS VARIABLES
				mapPropsOfficeDoc = appContext.getMapPropsOfficeDoc();//Propiedades del Documento Office	 (tags y valores de las propieades		
				Integer reintento = 1;//Inicialización de la varaible de reintentos	
				Boolean errorMaquina = false;//Inicialización de la varaible de error máquina en false
				String cadenaIdsDocsPersonalizados = "";//inicialización la cadena de id's de los docs personalizados
				
				//SE OBTIENE LA FECHA DE LA RADICACIÓN Y SE ALMACAENA
				DateFormat dateFormat = new SimpleDateFormat(globalProperties.getFormatoFecha());
				DateFormat dateFormatHora = new SimpleDateFormat(globalProperties.getFormatoHora());
				Date date = new Date();
				String fecha = dateFormat.format(date);
				String hora = dateFormatHora.format(date);
				
				//SE ALMACENAN LAS VARIABLES EN EL CONTEXTO DE LA SSM			
				context.getExtendedState().getVariables().put(Consts.REINTENTO, reintento);
				context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, errorMaquina);
				context.getExtendedState().getVariables().put(Consts.CADENA_LISTA_ID_DOCS_PERSONALIZADOS_CARTA, cadenaIdsDocsPersonalizados);
				context.getExtendedState().getVariables().put(Consts.FECHA_RADICACION, fecha);
				context.getExtendedState().getVariables().put(Consts.HORA_RADICACION, hora);
				log.info(raizLog + "Se almacenaron los siguientes valores en el contexto de la SSM: "
						+ Consts.REINTENTO + " ["+reintento+"] - "
						+ Consts.ERROR_MAQUINA + " ["+reintento+"] - "
						+ Consts.FECHA_RADICACION + " ["+fecha+"] - "
						+ Consts.HORA_RADICACION + " ["+hora+"] - "
						+ Consts.CADENA_LISTA_ID_DOCS_PERSONALIZADOS_CARTA + " ["+reintento+"] ");
				//TODO REVISAR Y BORRAR TRAS RE-DISEÑO ESTAS VARIABLES
				Map<Long, RespuestaEntrega> distribucionRuta = new HashMap<>();	
				context.getExtendedState().getVariables().put(Consts.DISTRIBUCION_RUTA, distribucionRuta);
				context.getExtendedState().getVariables().put(Consts.DISTRIBUIDO, 0);
				context.getExtendedState().getVariables().put(Consts.OPERACION, 0);
				//--------------------------------------------------
				
				imprimirLogMensajeFinEstado(raizLog, nombreEstadoActual);//Se marca el fin del estado en el log
				
			};
		
		}
		
		
		/**
		* Metodo que permite obtener el objeto "MetadatosPlantilla", leyendo el documento (plantilla) y guardar este objeto en el contexto de la SSM
		* 
		* @return
		*/
		@Bean
		public Action<States, Events> obtenerMetadatosPlantillaCaMe() {
			String nombreEstadoActual = States.OBTENER_METADATOS_PLANTILLA_CA_ME.toString();
			//SE RETORNA EL CONTEXTO DE LA MÁQUINA DE ESTADOS
			return (context) -> {//Se retorna contexto de la máquina de estados (Contexto)
				//SE EXTRAE LA RAIZ LOG DEL CONTEXTO
				String raizLog = SSMUtils.getRaizLog(context);//Variable para inicio de impresión del log
				
				imprimirLogMensajeInicioEstado(raizLog, nombreEstadoActual);//Se marca el incio del estado en el log
				
				//SE EXTRAEN VARIABLES DEL CONTEXTO DE LA SSM
				String nombreDocServidor = context.getExtendedState().get(Consts.NOMBRE_DOCUMENTO_SERVIDOR, String.class);//Nombre con el cual se almacenó la plantilla en una carpeta del servidor OTBR
				String workId = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);
				//SE EXTRAEN TODOS LOS METADATOS DEL DOCUMENTO (PLANTILLA)
				MetadatosPlantilla metadatosPlantilla = null;
				try {
					log.info(raizLog + "Extrayendo metadatos del documento con nombreDoc: ["+ nombreDocServidor+"]");
					metadatosPlantilla = DocumentoUtils.leerDocumento(raizLog, globalProperties.getRutaTemp() + nombreDocServidor, mapPropsOfficeDoc);
					log.info(raizLog + "Metadatos de la plantilla obtenidos correctamente");
					//SE ALMACENAN METADATOS DE LA PLANTILLA EN EL CONTEXTO DE LA MÁQUINA
					context.getExtendedState().getVariables().put(Consts.METADATOS_PLANTILLA, metadatosPlantilla);
					log.info(raizLog + "Objeto MetadatosPlantilla almacenado en el contexto de la SSM");
					
					//SE ENVIA EVENTO PARA CAMBIO DE ESTADO
					context.getStateMachine().sendEvent(Events.IR_CARGAR_METADATOS_PLANTILLA_CA_ME);
					log.info(raizLog + "Se envió el evento: ["+Events.IR_CARGAR_METADATOS_PLANTILLA_CA_ME+"]  para la maquina con workId: [" + workId +"]");
				} catch (NumberFormatException | IOException e) {
					//SE ENVIA EVENTO DE FIN CON ERRORES Y SE CARGAN VARIABLES DE ERROR EN EL CONTEXTO DE LA SSM
					String mensajeError = "Error obteniendo datos del documento "+e.getMessage();
					enviarEventoFinErrores(raizLog, context, workId, nombreEstadoActual, mensajeError, e);
					
					imprimirLogMensajeError(raizLog, nombreEstadoActual, mensajeError);
					
				}
				
				imprimirLogMensajeFinEstado(raizLog, nombreEstadoActual);//Se marca el fin del estado en el log
				
			};
		}
		
		
		
		/**
		* Metodo que permite cargar los valores de los "MetadatosPlantilla" en el contexto de la SSM
		* 
		* @return
		*/
		@Bean
		public Action<States, Events> cargarMetadatosPlantillaCaMe() {
			String nombreEstadoActual = States.CARGAR_METADATOS_PLANTILLA_CA_ME.toString();
			//SE RETORNA EL CONTEXTO DE LA MÁQUINA DE ESTADOS
			return (context) -> {//Se retorna contexto de la máquina de estados (Contexto)
				//SE EXTRAE LA RAIZ LOG DEL CONTEXTO
				String raizLog = SSMUtils.getRaizLog(context);//Variable para inicio de impresión del log
				
				imprimirLogMensajeInicioEstado(raizLog, nombreEstadoActual);//Se marca el incio del estado en el log
				
				//SE EXTRAEN VARIABLES DEL CONTEXTO DE LA SSM
				MetadatosPlantilla metadatosPlantilla = context.getExtendedState().get(Consts.METADATOS_PLANTILLA, MetadatosPlantilla.class);
				log.info(raizLog + "Se van a almacenar los metadatos de la plantilla en el contexto de la SSM....");
				//SE ALMACENAN ALGUNOS METADATOS DE LA PLANTILLA EN EL CONTEXTO DE LA MÁQUINA
				context.getExtendedState().getVariables().put(Consts.ASUNTO, metadatosPlantilla.getAsunto());//Si es confidencial se debe cambiar por el # de radicado
				context.getExtendedState().getVariables().put(Consts.TIENE_ANEXOS, Boolean.valueOf(metadatosPlantilla.getAnexosElectronicos()));
				context.getExtendedState().getVariables().put(Consts.ES_PERSONALIZADO, Boolean.valueOf(metadatosPlantilla.getPersonalizarDocumento()));
				context.getExtendedState().getVariables().put(Consts.TIPOLOGIA_MEMO_CARTA, metadatosPlantilla.getTipologia());
				context.getExtendedState().getVariables().put(Consts.TIPO_WF_DOCUMENTO, metadatosPlantilla.getTipoDocumento());//Normal o confidencial
				context.getExtendedState().getVariables().put(Consts.DESTINATARIOS, metadatosPlantilla.getDestinatarios());
				context.getExtendedState().getVariables().put(Consts.COPIAS, metadatosPlantilla.getCopias());
				//SE OBTIENE LA CADENA DE FIRMANTES DE LA FORMA ([{Nombre firmante1, sigla dpto firmante 1, cargo firmante 1},{Nombre firmante2, sigla dpto firmante 2, cargo firmante 2},])
				String listaNitFirmantes = retornarCadenaFrimantes( metadatosPlantilla.getFirmantes());
				log.info(raizLog + "La lista de Firmantes es: ["+listaNitFirmantes+"]"); 
				context.getExtendedState().getVariables().put(Consts.LISTA_NIT_FIRMANTES, listaNitFirmantes);
				//SE OBTIENE Y ALAMACENA LA SIGLA DEL DPTO DEL PRIMER FIRMANTE (USADA PARA NUMERO DE RADICADO)
				String siglaRemitente = metadatosPlantilla.getFirmantes().get(0).getSiglaremitente();
				log.info(raizLog + "La sigla del remitente es: [" + siglaRemitente + "]");
				context.getExtendedState().getVariables().put(Consts.SIGLA_FIRMANTE, siglaRemitente);
				log.info(raizLog + "Metadatos de la plantilla almacenados correctamente en el contexto de la SSM");
				//SE OBTIENEN LOS DATOS DEL PCR ORIGEN DE LA CARTA
				if(metadatosPlantilla.getTipologia().equalsIgnoreCase("CA")) {
					context.getExtendedState().getVariables().put(Consts.CDD_ORIGEN_CARTA, metadatosPlantilla.getFirmantes().get(0).getNombreCDD());
					context.getExtendedState().getVariables().put(Consts.PCR_ORIGEN_CARTA, metadatosPlantilla.getFirmantes().get(0).getNombrePCR());
					context.getExtendedState().getVariables().put(Consts.ID_PCR_ORIGEN_CARTA, metadatosPlantilla.getFirmantes().get(0).getIdPCR());
					context.getExtendedState().getVariables().put(Consts.ID_CDD_ORIGEN_CARTA, metadatosPlantilla.getFirmantes().get(0).getIdCDD());
					log.info(raizLog + "cddOrigen: [" + metadatosPlantilla.getFirmantes().get(0).getNombreCDD()  + "] pcrOrigen: [" + metadatosPlantilla.getFirmantes().get(0).getNombrePCR() + "] idPcrOrigen: [" + metadatosPlantilla.getFirmantes().get(0).getIdPCR() + "] idCddOrigen: ["  + metadatosPlantilla.getFirmantes().get(0).getIdCDD() + "]");
				}
				
				imprimirLogMensajeFinEstado(raizLog, nombreEstadoActual);//Se marca el fin del estado en el log
				
			};
		}
		
		
		
		/**
		* Metodo que permite generar el numero de radicado para Memorando Y Carta
		* 
		* @return
		*/
		@Bean
		public Action<States, Events> iniciarWfGenerarNumRadicadoCaMe() {
			String nombreEstadoActual = States.INICIAR_WF_GENERAR_NUM_RADICADO_CA_ME.toString();
			//SE RETORNA EL CONTEXTO DE LA MÁQUINA DE ESTADOS
			return (context) -> {//Se retorna contexto de la máquina de estados (Contexto)
				//SE EXTRAE LA RAIZ LOG DEL CONTEXTO
				String raizLog = SSMUtils.getRaizLog(context);//Variable para inicio de impresión del log
				
				imprimirLogMensajeInicioEstado(raizLog, nombreEstadoActual);// Se marca el incio del estado en el log

				// SE CARGAN LAS VARIABLES ALMACENADAS EN EL CONTEXTO DE LA SSM
				MetadatosPlantilla metadatosPlantilla = context.getExtendedState().get(Consts.METADATOS_PLANTILLA,
						MetadatosPlantilla.class);
				String workId = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);
				String siglaOrigen = context.getExtendedState().get(Consts.SIGLA_FIRMANTE, String.class);
				String tipologia = context.getExtendedState().get(Consts.TIPOLOGIA_MEMO_CARTA, String.class);
				Long idSolicitante = context.getExtendedState().get(Consts.ID_SOLICITANTE, Long.class);

				// SE CARGAN ATRIBUTOS PARA INICIO DEL FLUJO
				String titulo = Consts.TITULO_WF_GENERAR_NUM_RADICADO;
				String atributoNumRadicado = Consts.ATRIBUTO_NUM_RADICADO;
				Map<String, ValorAtributo> atributos = new HashMap<String, ValorAtributo>();
				atributos.put(Consts.ATRIBUTO_SIGLA, new ValorAtributo(siglaOrigen, TipoAtributo.STRING));
				atributos.put(Consts.ATRIBUTO_TIPOLOGIA, new ValorAtributo(tipologia, TipoAtributo.STRING));
				atributos.put(Consts.ATRIBUTO_ID_SOLICITANTE, new ValorAtributo(idSolicitante, TipoAtributo.USER));

				// SE REVISA SI EL DOC TIENE FONDO INDEPENDIENTE (POR EJEMPLO AU-G)
				long mapId = 0L; // SE INICIALIZA LA VARIABLE EN DONDE SE ALMACENARÁ EL ID DEL ESQUEMA DE GENERACIÓN DE NUM DE RADICADO QUE SE INICIARÁ
				if (metadatosPlantilla.getEsFondoIndependiente()) {
					// ESQUEMA CON FONDO INDEPENDIENTE
					log.info(raizLog + "El documento tiene fondo independiente");
					mapId = workflowProps.getGenerarNumeroRadicadoFondoIndependiente();
				} else {
					// ESQUEMA SIN FONDO INDEPENDIENTE
					mapId = workflowProps.getGenerarNumeroRadicado();
				}

				log.info(raizLog + "El id del mapa para el flujo de generacion de num radicado es: [" + mapId + "]");

				// PARA INICIAR LA INSTANCIA CON LOS DATOS YA CARGADOS
				try {
					log.info(raizLog + "Se va a crear instancia del flujo de generación de número de radicado....");
					
					// SE INICIA INSTANCIA DE WF
					long idProcess = Workflow.iniciarWorkflowConAtributos(autenticacionCS.getAdminSoapHeader(), mapId,
							titulo, atributos, wsdlsProps.getWorkflow());

					log.info(raizLog + "Instacia instancia del flujo de generación de número de radicado creada correctamente con id ["+idProcess+"]");

					// SE OBTIENE EL NÚMERO DE RADICADO GENERADO
					log.info(raizLog + "Se va a obtener el número de radicado generado....");
					String numeroRadicadoObtenido = Workflow
							.obtenerValoresAtributo(autenticacionCS.getAdminSoapHeader(), idProcess,
									atributoNumRadicado, wsdlsProps.getWorkflow())
							.get(0).toString();
					
					// SE ALMACENA EL NÚMERO DE RADICADO EL CONTEXTO DE LA SSM
					context.getExtendedState().getVariables().put(Consts.NUMERO_RADICADO_OBTENIDO, numeroRadicadoObtenido);
					log.info(raizLog + "El numero de radicado obtenido es: [" + numeroRadicadoObtenido + "]");
					
					//SE ENVIA EVENTO PARA CAMBIO DE ESTADO
					context.getStateMachine().sendEvent(Events.IR_ACTUALIZAR_RADICADO_DOC_CA_ME);
					log.info(raizLog + "Se envió el evento: ["+Events.IR_ACTUALIZAR_RADICADO_DOC_CA_ME+"]  para la maquina con workId: [" + workId +"]");

				} catch (NumberFormatException | NoSuchElementException | SOAPException | IOException
						| WebServiceException e) {
					// SE ENVIA EVENTO DE FIN CON ERRORES Y SE CARGAN VARIABLES DE ERROR EN EL CONTEXTO DE LA SSM
					String mensajeError = "Error al iniciar workflow de generar numero de radicado  " + e.getMessage();
					
					enviarEventoFinErrores(raizLog, context, workId, nombreEstadoActual, mensajeError, e);
					
				}
				
				imprimirLogMensajeFinEstado(raizLog, nombreEstadoActual);//Se marca el fin del estado en el log
				
			};
		}

		
		
		/**
		* Metodo que permite escribir el numeroRadicado y la fecha en el documento
		* 
		* @return
		*/
		@Bean
		public Action<States, Events> actualizarRadicadoDocCaMe() {
			String nombreEstadoActual = States.ACTUALIZAR_RADICADO_DOC_CA_ME.toString();
			//SE RETORNA EL CONTEXTO DE LA MÁQUINA DE ESTADOS
			return (context) -> {//Se retorna contexto de la máquina de estados (Contexto)
				//SE EXTRAE LA RAIZ LOG DEL CONTEXTO
				String raizLog = SSMUtils.getRaizLog(context);//Variable para inicio de impresión del log
				
				imprimirLogMensajeInicioEstado(raizLog, nombreEstadoActual);// Se marca el incio del estado en el log

				// SE CARGAN LAS VARIABLES ALMACENADAS EN EL CONTEXTO DE LA SSM
				String workId = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);
				String nombreDocServidor = context.getExtendedState().get(Consts.NOMBRE_DOCUMENTO_SERVIDOR, String.class);
				String numeroRadicado = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO,String.class);					
				//SE EXTRAEN METADATOS DE LA CATEGORIA ALAMACENADA EN EL CONTEXTO DE LA SSM
				MetadatosPlantilla metadatosPlantilla = context.getExtendedState().get(Consts.METADATOS_PLANTILLA, MetadatosPlantilla.class);
				//SE REVISA SI EL DOCUMENTO VIENE EN INGLÉS (APLICA SOLO PARA CARTA)
				String esIdiomaInglesStr = (metadatosPlantilla.getIdiomaIngles() != null) ? metadatosPlantilla.getIdiomaIngles() : "No especificado";
				Boolean esIdiomaIngles = esIdiomaInglesStr.equalsIgnoreCase("True");
				log.info(raizLog + "¿El idioma es ingles?: [" + esIdiomaIngles+"]");
				
				//SE ESCRIBE EL NUMERO DE RADICADO EN EL ENCABEZADO DEL DOCUMENTO
				try {
					log.info(raizLog + "Se va a escribir número de radicado ["+numeroRadicado+"] en el documento ["+nombreDocServidor+"]....");
					DocumentoUtils.escribirDocumento(raizLog, globalProperties.getRutaTemp() + nombreDocServidor, numeroRadicado, esIdiomaIngles, mapPropsOfficeDoc);
					log.info(raizLog + "Documento modificado con éxito");
					
					//SE ENVIA EVENTO PARA CAMBIO DE ESTADO
					context.getStateMachine().sendEvent(Events.IR_RENOMBRAR_DOC_CS_CA_ME);
					log.info(raizLog + "Se envió el evento: ["+Events.IR_RENOMBRAR_DOC_CS_CA_ME+"]  para la maquina con workId: [" + workId +"]");
					
				} catch (IOException e) {
					// SE ENVIA EVENTO DE FIN CON ERRORES Y SE CARGAN VARIABLES DE ERROR EN EL CONTEXTO DE LA SSM
					String mensajeError = "Error al escribir número de radicado en el documento  " + e.getMessage();
					
					enviarEventoFinErrores(raizLog, context, workId, nombreEstadoActual, mensajeError, e);
				}
				
				imprimirLogMensajeFinEstado(raizLog, nombreEstadoActual);//Se marca el fin del estado en el log
				
			};
		}
		
		
		
		/**
		* Metodo que permite renombrar el documento en el Content Server 
		* 
		* @return
		*/
		@Bean
		public Action<States, Events> renombrarDocumentoCSCaMe() {
			String nombreEstadoActual = States.RENOMBRAR_DOC_CS_CA_ME.toString();
			//SE RETORNA EL CONTEXTO DE LA MÁQUINA DE ESTADOS
			return (context) -> {//Se retorna contexto de la máquina de estados (Contexto)
				//SE EXTRAE LA RAIZ LOG DEL CONTEXTO
				String raizLog = SSMUtils.getRaizLog(context);//Variable para inicio de impresión del log
				
				imprimirLogMensajeInicioEstado(raizLog, nombreEstadoActual);// Se marca el incio del estado en el log

				// SE CARGAN LAS VARIABLES ALMACENADAS EN EL CONTEXTO DE LA SSM
				String workId = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);
				String numeroRadicado = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO, String.class);
				String conexionUsuario = context.getExtendedState().get(Consts.CONEXION_CS_SOLICITANTE, String.class);
				Long idDoc = context.getExtendedState().get(Consts.ID_DOC_RADICACION, Long.class);
				Boolean esPersonalizado = context.getExtendedState().get(Consts.ES_PERSONALIZADO, Boolean.class);
				String nombreOriginalDoc = context.getExtendedState().get(Consts.NOMBRE_DOCUMENTO_ORIGINAL, String.class);
				log.info(raizLog + "El nombre orginal del doc es: [" + nombreOriginalDoc+"]");
				//SE CREA EL NUEVO NOMBRE DEL DOCUMENTO
				String nuevoNombreDocCS = numeroRadicado+ ".docx";					
				log.info(raizLog + "El nuevo nombre es: [" + nuevoNombreDocCS+"]");
				//SE VALIDA QUE EL NUEVO NOMBRE NO SUPERE EL MÁXIMO DE CARACTERES
				nuevoNombreDocCS = (nuevoNombreDocCS.length() > globalProperties.getLongitudNuevoNombreArchivo())
				? nuevoNombreDocCS.substring(0, globalProperties.getLongitudNuevoNombreArchivo())
				: nuevoNombreDocCS;
				//SE INICIA CAMBIO DEL NOMBRE DEL ARCHIVO EN CS
				try {
					log.info(raizLog + "Se va a renombrar del documento con id ["+idDoc+"]");
					ContenidoDocumento.cambiarNombreDocumento(autenticacionCS.getUserSoapHeader(conexionUsuario),
					idDoc, nuevoNombreDocCS, wsdlsProps.getDocumento());
					log.info(raizLog + "Se renombró exitosamente el documento");
					
					//SE REVISA SI ES PERSONALIZADO
					if(esPersonalizado) {
						log.info(raizLog + "Se debe personalizar el documento....");
						//SE ENVIA EVENTO PARA CAMBIO DE ESTADO
						context.getStateMachine().sendEvent(Events.IR_GENERAR_DOCS_PERSONALIZADOS_CARTA);
						log.info(raizLog + "Se envió el evento: ["+Events.IR_GENERAR_DOCS_PERSONALIZADOS_CARTA+"]  para la maquina con workId: [" + workId +"]");
					}else {
						log.info(raizLog + "Se va a convertir el documento a PDF....");
						//SE ENVIA EVENTO PARA CAMBIO DE ESTADO
						context.getStateMachine().sendEvent(Events.IR_CONVERTIR_UNIR_PDF_CA_ME);
						log.info(raizLog + "Se envió el evento: ["+Events.IR_CONVERTIR_UNIR_PDF_CA_ME+"]  para la maquina con workId: [" + workId +"]");
					}
					
					
				} catch (SOAPException | IOException | InterruptedException e) {
					// SE ENVIA EVENTO DE FIN CON ERRORES Y SE CARGAN VARIABLES DE ERROR EN EL CONTEXTO DE LA SSM
					String mensajeError = "Error al renombrar el documento en content server  " + e.getMessage();
					
					enviarEventoFinErrores(raizLog, context, workId, nombreEstadoActual, mensajeError, e);
				}
				
				imprimirLogMensajeFinEstado(raizLog, nombreEstadoActual);//Se marca el fin del estado en el log
				
			};
		}
		
		
		/**
		* Metodo que permite personalizar Documentos tipo Carta
		* 
		* El metodo genera una copia del documento por cada una de los destinatarios de
		* tal forma que solo se muestre un destinatario
		* 
		* @return
		*/
		@Bean
		public Action<States, Events> generarDocsPersonalizadosCarta() {
			String nombreEstadoActual = States.GENERAR_DOCS_PERSONALIZADOS_CARTA.toString();
			//SE RETORNA EL CONTEXTO DE LA MÁQUINA DE ESTADOS
			return (context) -> {//Se retorna contexto de la máquina de estados (Contexto)
				//SE EXTRAE LA RAIZ LOG DEL CONTEXTO
				String raizLog = SSMUtils.getRaizLog(context);//Variable para inicio de impresión del log
				
				imprimirLogMensajeInicioEstado(raizLog, nombreEstadoActual);// Se marca el incio del estado en el log

				// SE CARGAN LAS VARIABLES ALMACENADAS EN EL CONTEXTO DE LA SSM
				String workId = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);
				String nombreDocServidor = context.getExtendedState().get(Consts.NOMBRE_DOCUMENTO_SERVIDOR, String.class);
				MetadatosPlantilla metadatosPlantilla = context.getExtendedState().get(Consts.METADATOS_PLANTILLA, MetadatosPlantilla.class);

				List<DataHandler> listDataHandler = new ArrayList<>();
				
				try {
					listDataHandler = DocumentoUtils.personalizarDocumentoCartaMasivo(raizLog,
							globalProperties.getRutaTemp() + nombreDocServidor, metadatosPlantilla);
					//SE ALMACENA LISTA DE DOCUEMNTOS GENERADOS EN EL CONTEXTO DE LA SSM
					context.getExtendedState().getVariables().put(Consts.LISTA_DTHL_DOCS_PERSONALIZADOS_CARTA, listDataHandler);
					
					//SE ENVIA EVENTO PARA CAMBIO DE ESTADO
					context.getStateMachine().sendEvent(Events.IR_SUBIR_DOCS_PERSONALIZADOS_CS_CARTA);
					log.info(raizLog + "Se envió el evento: ["+Events.IR_SUBIR_DOCS_PERSONALIZADOS_CS_CARTA+"]  para la maquina con workId: [" + workId +"]");
					
				} catch (IOException e) {
					// SE ENVIA EVENTO DE FIN CON ERRORES Y SE CARGAN VARIABLES DE ERROR EN EL CONTEXTO DE LA SSM
					String mensajeError = "Error al personalizar el documento ["+nombreDocServidor+"] - " + e.getMessage();
					
					enviarEventoFinErrores(raizLog, context, workId, nombreEstadoActual, mensajeError, e);
					
				}
				
				imprimirLogMensajeFinEstado(raizLog, nombreEstadoActual);//Se marca el fin del estado en el log
				
			};
		}
		
		
		
		//TODO ESTA FUNCIÓN SE DEBE RE-EVALUAR EN EL RE-DISEÑO (NO SE DEBEN SUBIR DOCS SINO HASTA EL FINAL)
		/**
		* Metodo que permite subir a CS los documentos personalizados
		* 
		* @return
		*/
		@SuppressWarnings("unchecked")
		@Bean
		public Action<States, Events> subirDocsPersonalizadosCSCarta() {
			String nombreEstadoActual = States.SUBIR_DOCS_PERSONALIZADOS_CS_CARTA.toString();
			// SE RETORNA EL CONTEXTO DE LA MÁQUINA DE ESTADOS
			return (context) -> {// Se retorna contexto de la máquina de estados (Contexto)
				// SE EXTRAE LA RAIZ LOG DEL CONTEXTO
				String raizLog = SSMUtils.getRaizLog(context);// Variable para inicio de impresión del log

				imprimirLogMensajeInicioEstado(raizLog, nombreEstadoActual);// Se marca el incio del estado en el log

				// SE CARGAN LAS VARIABLES ALMACENADAS EN EL CONTEXTO DE LA SSM
				String workId = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);
				MetadatosPlantilla metadatosPlantilla = context.getExtendedState().get(Consts.METADATOS_PLANTILLA, MetadatosPlantilla.class);
				List<DataHandler> listDataHandler = (List<DataHandler>) context.getExtendedState().get(Consts.LISTA_DTHL_DOCS_PERSONALIZADOS_CARTA, Object.class);
				String conexionUsuario = context.getExtendedState().get(Consts.CONEXION_CS_SOLICITANTE, String.class);

				try {
					// Se actualiza el parent id de la carpeta original en caso de que el usuario
					// haya movido el documento

					actualizarIdParentOriginal(context, autenticacionCS.getAdminSoapHeader(),
							wsdlsProps.getDocumento());

					Long parentIdOriginal = context.getExtendedState().get(Consts.PARENT_ID_ORIGINAL_MEMO_CARTA,
							Long.class);
					String numeroRadicado = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO,
							String.class);
					String tipoDocumentalCatDocumento = context.getExtendedState()
							.get(Consts.TIPO_DOCUMENTAL_CAT_DOCUMENTO, String.class);
					String serieCatDocumento = context.getExtendedState().get(Consts.SERIE_CAT_DOCUMENTO, String.class);

					long idDocOriginal = context.getExtendedState().get(Consts.ID_DOC_RADICACION, Long.class);
					List<Empleado> destinos = metadatosPlantilla.getDestinatarios();

					int count = 0;
					List<Long> listIdDocs = new ArrayList<>();

					List<String> listNombresDocsPersonalizados = new ArrayList<>();
					for (DataHandler dataHandler : listDataHandler) {

						log.info(raizLog + "INICIO crear doc personalizado en Content");
						Map<String, Object> metadatosCatDoc = new HashMap<String, Object>();
						Long idDocPersonalizado = null;

						String nombreDocumento = numeroRadicado + "_"
								+ ((destinos.get(count).getNombre() != null
										&& !destinos.get(count).getNombre().isEmpty()) ? destinos.get(count).getNombre()
												: destinos.get(count).getDependencia())
								+ "_" + count;

						nombreDocumento = SSMUtils.truncarString(nombreDocumento,
								Consts.TAMANO_MAXIMO_NOMBRE_DOCUMENTO_ORIGINAL);

						if (tipoDocumentalCatDocumento != null && !tipoDocumentalCatDocumento.isEmpty()
								&& serieCatDocumento != null && !serieCatDocumento.isEmpty()) {

							log.info(raizLog + "Crear documento con categoria documento");

							metadatosCatDoc.put("Tipo documental", tipoDocumentalCatDocumento);
							metadatosCatDoc.put("Serie", serieCatDocumento);

							idDocPersonalizado = ContenidoDocumento.crearDocumentoCatDocumento(
									autenticacionCS.getUserSoapHeader(conexionUsuario), idDocOriginal,
									nombreDocumento + ".docx", dataHandler, categoriaDocProps.getId(),
									categoriaDocProps.getNombreCategoria(), metadatosCatDoc, false, null,
									wsdlsProps.getDocumento());
						} else {

							log.info(raizLog + "Crear documento sin categoria documento");

							idDocPersonalizado = ContenidoDocumento.crearDocumento(
									autenticacionCS.getUserSoapHeader(conexionUsuario), nombreDocumento + ".docx",
									dataHandler, parentIdOriginal, wsdlsProps.getDocumento());

						}

						log.info(raizLog + "FIN crear doc personalizado en Content");

						listIdDocs.add(idDocPersonalizado);

						destinos.get(count).setIdDocumento(idDocPersonalizado);

						log.info(raizLog + "FIN agregar cat correspondencia a doc personalizado");

						log.info(raizLog + "INICIO cambiar nombre documento personalizado");

						String nombreDoc = null;
						nombreDoc = ContenidoDocumento.obtenerNombreDocumento(
								autenticacionCS.getUserSoapHeader(conexionUsuario), idDocPersonalizado,
								wsdlsProps.getDocumento());

						log.info(raizLog + "El nombre del doc personalizado ANTES: [" + nombreDoc + "]");

						ContenidoDocumento.cambiarNombreDocumento(autenticacionCS.getUserSoapHeader(conexionUsuario),
								idDocPersonalizado, nombreDocumento + ".pdf", wsdlsProps.getDocumento());

						String nombreDocDespues = null;

						nombreDocDespues = ContenidoDocumento.obtenerNombreDocumento(
								autenticacionCS.getUserSoapHeader(conexionUsuario), idDocPersonalizado,
								wsdlsProps.getDocumento());

						destinos.get(count).setNombreDocumento(nombreDocDespues);

						log.info(raizLog + "El nombre del doc personalizado DESPUES: [" + nombreDocDespues + "]");

						log.info(raizLog + "FIN cambiar nombre documento personalizado");

						listNombresDocsPersonalizados.add(nombreDoc);

						count++;

						// ELIMINACIÓN DE LOS ARCHIVOS COMBINADOS
						String nombreDocPersonalizado = globalProperties.getRutaTemp() + dataHandler.getName();
						log.info(raizLog + "archivo a eliminar: [" + nombreDocPersonalizado + "]");
						File doc = new File(nombreDocPersonalizado);
						log.info(raizLog + "Archivo eliminado: [" + doc.delete() + "]");

					}
					String idList = listIdDocs.toString();
					String cadenaListIdDocs = String.join(",", idList);

					cadenaListIdDocs = cadenaListIdDocs.replace("[", "{");
					cadenaListIdDocs = cadenaListIdDocs.replace("]", "}");

					cadenaListIdDocs = StringUtils.deleteWhitespace(cadenaListIdDocs);
					log.info(raizLog + "LA CADENA DE IDS PERSONALIZADOS: [" + cadenaListIdDocs + "]");

					// ACTUALIZA DESTINOS DE LA CATEGORIA CON SUS IDS
					metadatosPlantilla.setDestinatarios(destinos);
					context.getExtendedState().getVariables().put(Consts.METADATOS_PLANTILLA, metadatosPlantilla);

					context.getExtendedState().getVariables().put(Consts.LISTA_NOMBRES_DOCS_PERSONALIZADOS_CARTA,
							listNombresDocsPersonalizados);
					context.getExtendedState().getVariables().put(Consts.LISTA_ID_DOCS_PERSONALIZADOS_CARTA,
							listIdDocs);
					context.getExtendedState().getVariables().put(Consts.CADENA_LISTA_ID_DOCS_PERSONALIZADOS_CARTA,
							cadenaListIdDocs);
					// context.getExtendedState().getVariables().put(Consts.ES_PERSONALIZADO,
					// esPersonalizado);
					
					log.info(raizLog + "Se van a convertir los documentos personalizados a PDF....");
					//SE ENVIA EVENTO PARA CAMBIO DE ESTADO
					context.getStateMachine().sendEvent(Events.IR_CONVERTIR_UNIR_PDF_CA_ME);
					log.info(raizLog + "Se envió el evento: ["+Events.IR_CONVERTIR_UNIR_PDF_CA_ME+"]  para la maquina con workId: [" + workId +"]");
					

				} catch (Exception e) {
					// SE ENVIA EVENTO DE FIN CON ERRORES Y SE CARGAN VARIABLES DE ERROR EN EL CONTEXTO DE LA SSM
					String mensajeError = "Error al cargar documentos personalizados a Content Server - " + e.getMessage();
					
					enviarEventoFinErrores(raizLog, context, workId, nombreEstadoActual, mensajeError, e);
					
				}

				imprimirLogMensajeFinEstado(raizLog, nombreEstadoActual);// Se marca el fin del estado en el log

			};
		}
		
		
		
		
		/**
		* Metodo que permite hacer un llamado a un servicio web de la app "pdfUtils"
		* 
		* @return
		*/
		@Bean
		public Action<States, Events> convertirUnirPdfCaMe() {
			String nombreEstadoActual = States.CONVERTIR_UNIR_PDF_CA_ME.toString();
			//SE RETORNA EL CONTEXTO DE LA MÁQUINA DE ESTADOS
			return (context) -> {//Se retorna contexto de la máquina de estados (Contexto)
				//SE EXTRAE LA RAIZ LOG DEL CONTEXTO
				String raizLog = SSMUtils.getRaizLog(context);//Variable para inicio de impresión del log
				
				imprimirLogMensajeInicioEstado(raizLog, nombreEstadoActual);// Se marca el incio del estado en el log

				// SE CARGAN LAS VARIABLES ALMACENADAS EN EL CONTEXTO DE LA SSM
				String workId = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);
				Long idDocOriginal = context.getExtendedState().get(Consts.ID_DOC_RADICACION, Long.class);
				String conexionCS = context.getExtendedState().get(Consts.CONEXION_CS_SOLICITANTE, String.class);
				Boolean esPersonalizado = context.getExtendedState().get(Consts.ES_PERSONALIZADO, Boolean.class);
				String idDocsPersonalizados = context.getExtendedState().get(Consts.CADENA_LISTA_ID_DOCS_PERSONALIZADOS_CARTA, String.class);
				Boolean tieneAnexos = context.getExtendedState().get(Consts.TIENE_ANEXOS, Boolean.class);
				String idDocsAdjuntos = context.getExtendedState().get(Consts.ID_DOCS_ADJUNTOS, String.class);
				
				// SE LLENA EL HASHMAP CON LOS PARAMETROS DE LA URL DESTINO
				HashMap<String, Object> params;
				params = new LinkedHashMap<>();
				params.put("idWorkflow", workId);
				params.put("idDocumentoOriginal", idDocOriginal);
				params.put("conexionCS", conexionCS);
				params.put("esPersonalizado", esPersonalizado);
				params.put("idsDocsPersonalizados", idDocsPersonalizados);
				params.put("tieneAnexos", tieneAnexos);
				params.put("idsDocsAnexos", idDocsAdjuntos);

				// TODO SE DEBE GENERAR LA RUTA EN EL ARCHIVO DE PROPERTIES DE CORR
				String strURLDestino = "http://172.23.30.67:8183/iconecta-pdf-utils/convertirUnirDocumentos";// PDFAppUtils
				

				//SE REALIZA LLAMADO AL SERVICIO WEB
				try {
					log.info(raizLog + "Se va a llamar al controlador de PDFUtils - convertirUnirDocumentos....");
					ControllerCaller.llamarAplicacionPdfUtils(raizLog, params, strURLDestino, "POST");
					log.info(raizLog + "Llamado al servicio web realizado con éxito");
					
					//SE ENVIA EVENTO PARA CAMBIO DE ESTADO
					context.getStateMachine().sendEvent(Events.IR_ESPERAR_PROCESO_CA_ME);
					log.info(raizLog + "Se envió el evento: ["+Events.IR_ESPERAR_PROCESO_CA_ME+"]  para la maquina con workId: [" + workId +"]");
					
				} catch (Exception e) {
					// SE ENVIA EVENTO DE FIN CON ERRORES Y SE CARGAN VARIABLES DE ERROR EN EL CONTEXTO DE LA SSM
					String mensajeError = "Error hacer el llamado a la aplicación PDFUTILS - " + e.getMessage();
					
					enviarEventoFinErrores(raizLog, context, workId, nombreEstadoActual, mensajeError, e);
					
				}
				
				imprimirLogMensajeFinEstado(raizLog, nombreEstadoActual);//Se marca el fin del estado en el log
				
			};
		}

		
		
		/**
		* Metodo para esperar y volver a consultar un proceso
		* 
		* @return
		*/
		@Bean
		public Action<States, Events> esperarProcesoCaMe() {
			String nombreEstadoActual = States.ESPERAR_PROCESO_CA_ME.toString();
			//SE RETORNA EL CONTEXTO DE LA MÁQUINA DE ESTADOS
			return (context) -> {//Se retorna contexto de la máquina de estados (Contexto)
				//SE EXTRAE LA RAIZ LOG DEL CONTEXTO
				String raizLog = SSMUtils.getRaizLog(context);//Variable para inicio de impresión del log
				
				imprimirLogMensajeInicioEstado(raizLog, nombreEstadoActual);// Se marca el incio del estado en el log

				//SE MUESTRA EL TIEMPO DE ESPERA ANTES DEL SIGUIENTE INTENTO
				long tiempoReintentoMins = tiempoReintentoMilisecs/(1000*60);
				log.info(raizLog + "Se va a esperar ["+tiempoReintentoMins+"] minutos antes de volver a hacer el llamado");
				
				imprimirLogMensajeFinEstado(raizLog, nombreEstadoActual);//Se marca el fin del estado en el log
				
			};
		}
		
	
		
		/**
		* Metodo que permite hacer un llamado a un servicio web de la app "pdfUtils" para consultar el proceso
		* 
		* @return
		*/
		@Bean
		public Action<States, Events> consultarConvertirUnirPdfCaMe() {
			String nombreEstadoActual = States.CONSULTAR_CONVERTIR_UNIR_PDF_CA_ME.toString();
			//SE RETORNA EL CONTEXTO DE LA MÁQUINA DE ESTADOS
			return (context) -> {//Se retorna contexto de la máquina de estados (Contexto)
				//SE EXTRAE LA RAIZ LOG DEL CONTEXTO
				String raizLog = SSMUtils.getRaizLog(context);//Variable para inicio de impresión del log
				
				imprimirLogMensajeInicioEstado(raizLog, nombreEstadoActual);// Se marca el incio del estado en el log

				// SE CARGAN LAS VARIABLES ALMACENADAS EN EL CONTEXTO DE LA SSM
				Integer numActualReintento = context.getExtendedState().get(Consts.REINTENTO, Integer.class);
				String workId = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);
				
				// SE LLENA EL HASHMAP CON LOS PARAMETROS DE LA URL DESTINO
				HashMap<String, Object> params;
				params = new LinkedHashMap<>();
				params.put("idWorkflow", context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class));
				params.put("idDocumentoOriginal", context.getExtendedState().get(Consts.ID_DOC_RADICACION, Long.class));

				// TODO SE DEBE GENERAR LA RUTA EN EL ARCHIVO DE PROPERTIES DE CORR
				String strURLDestino = "http://172.23.30.67:8183/iconecta-pdf-utils/consultarProceso";// PDFAppUtils
				

				//SE REALIZA LLAMADO AL SERVICIO WEB
				try {
					log.info(raizLog + "Se va a llamar al controlador de PDFUtils - consultarProceso....");
					RespuestaXmlPdfUtils respuestaXML = ControllerCaller.llamarAplicacionPdfUtils(raizLog, params, strURLDestino, "POST");
					log.info(raizLog + "Llamado al servicio web realizado con éxito");
					
					// SE REVISA LA RESPUESTA
					boolean procesoFinalizado = respuestaXML.isValor();
					if (procesoFinalizado && numActualReintento <= numMaximoReintentos) {
						log.info(raizLog + "Proceso de conversión a PDF ha concluido con éxito");
						
						//SE ENVIA EVENTO PARA CAMBIO DE ESTADO
						context.getStateMachine().sendEvent(Events.IR_CARGAR_CAT_CORR_CS_CA_ME);
						log.info(raizLog + "Se envió el evento: ["+Events.IR_CARGAR_CAT_CORR_CS_CA_ME+"]  para la maquina con workId: [" + workId +"]");

					} else {//reintentos
						log.info(raizLog + "Proceso de conversión a PDF NO ha concluido");
						numActualReintento++;
						context.getExtendedState().getVariables().put(Consts.REINTENTO, numActualReintento);
						
						//SE ENVIA EVENTO PARA CAMBIO DE ESTADO
						context.getStateMachine().sendEvent(Events.IR_ESPERAR_PROCESO_CA_ME);
						log.info(raizLog + "Se envió el evento: ["+Events.IR_ESPERAR_PROCESO_CA_ME+"]  para la maquina con workId: [" + workId +"]");
					}
					
				} catch (Exception e) {
					// SE ENVIA EVENTO DE FIN CON ERRORES Y SE CARGAN VARIABLES DE ERROR EN EL CONTEXTO DE LA SSM
					String mensajeError = "Error hacer el llamado a la aplicación PDFUTILS - " + e.getMessage();
					
					enviarEventoFinErrores(raizLog, context, workId, nombreEstadoActual, mensajeError, e);
					
				}
				
				imprimirLogMensajeFinEstado(raizLog, nombreEstadoActual);//Se marca el fin del estado en el log
				
			};
		}
		
		
		
		/**
		* Metodo que permite hacer un llamado a un servicio web de la app "Firma"
		* 
		* @return
		*/
		@SuppressWarnings("unchecked")
		@Bean
		public Action<States, Events> cargarCatCorrCSCaMe() {
			String nombreEstadoActual = States.CARGAR_CAT_CORR_CS_CA_ME.toString();
			//SE RETORNA EL CONTEXTO DE LA MÁQUINA DE ESTADOS
			return (context) -> {//Se retorna contexto de la máquina de estados (Contexto)
				//SE EXTRAE LA RAIZ LOG DEL CONTEXTO
				String raizLog = SSMUtils.getRaizLog(context);//Variable para inicio de impresión del log
				
				imprimirLogMensajeInicioEstado(raizLog, nombreEstadoActual);// Se marca el incio del estado en el log

				// SE CARGAN LAS VARIABLES ALMACENADAS EN EL CONTEXTO DE LA SSM
				String workId = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);
				Long idDoc = context.getExtendedState().get(Consts.ID_DOC_RADICACION, Long.class);
				String tipologia = context.getExtendedState().get(Consts.TIPOLOGIA_MEMO_CARTA, String.class);
				String asunto = context.getExtendedState().get(Consts.ASUNTO, String.class);
				String numeroRadicadoObtenido = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO, String.class);
				String tipoWfDoc = context.getExtendedState().get(Consts.TIPO_WF_DOCUMENTO, String.class);
				Date fecha = new Date();
				String hora = context.getExtendedState().get(Consts.HORA_RADICACION, String.class);
				List<Empleado> destinatarios = (List<Empleado>) context.getExtendedState().get(Consts.DESTINATARIOS, Object.class);
				List<Empleado> copias = (List<Empleado>) context.getExtendedState().get(Consts.COPIAS, Object.class);
				MetadatosPlantilla metadatosPlantilla = context.getExtendedState().get(Consts.METADATOS_PLANTILLA, MetadatosPlantilla.class);
				List<Empleado> firmantes = metadatosPlantilla.getFirmantes();
				List<Long> listaIdDocsPersonalizados = (List<Long>) context.getExtendedState().get(Consts.LISTA_ID_DOCS_PERSONALIZADOS_CARTA, Object.class);
				Boolean esPersonalizado = context.getExtendedState().get(Consts.ES_PERSONALIZADO, Boolean.class);
				
				//SE VA A CARGAR LA CATEGORIA DE CORRESPONDENCIA AL DOC
				try {						
					//SE REVISA CONFIDENCIALIDAD Y SE MODIFICA ASUNTO
					Boolean esConfidencial = false;

					if (tipoWfDoc.equalsIgnoreCase("Confidencial")) {
						log.info(raizLog + "Es confidencial, se modifica el asunto por: [" + numeroRadicadoObtenido+ "]");
						metadatosPlantilla.setAsunto(numeroRadicadoObtenido);
						asunto = numeroRadicadoObtenido;
						esConfidencial = true;
						context.getExtendedState().getVariables().put(Consts.ASUNTO, numeroRadicadoObtenido);
					}

					//SE CARGAN METADATOS GENERALES
					log.info(raizLog + "Inicio carga de metadatos generales....");
					Map<String, Object> metadatos = new HashMap<>();

					metadatos.put(categoriaProps.getCorrespondencia().getAtributoTipologia(), ((tipologia.equalsIgnoreCase("ME")) ? globalProperties.getTipologiaMemorando(): globalProperties.getTipologiaCarta()));
					metadatos.put(categoriaProps.getCorrespondencia().getAtributoEstado(), estadosProps.getRadicado());
					metadatos.put(categoriaProps.getCorrespondencia().getAtributoAsunto(), asunto);
					metadatos.put(categoriaProps.getCorrespondencia().getAtributoFechaRadicacion(), fecha);//antes estaba "date"
					metadatos.put(categoriaProps.getCorrespondencia().getAtributoHoraRadicacion(), hora);
					metadatos.put(categoriaProps.getCorrespondencia().getAtributoNumeroRadicacion(), numeroRadicadoObtenido);
					metadatos.put("Confidencial", esConfidencial);
					
					//SE CARGAN LOS METADATOS DEL ORIGEN (FIRMANTE)
					log.info(raizLog + "Inicio carga de metadatos origen (Firmante)....");
					List<Map<String, Object>> listSubGrupoOrigen = new ArrayList<>(); //Lista en al que se almacenan metadatos del firmante

					firmantes.forEach(firmante -> {
						Map<String, Object> metadatosSubGrupoOrigen = new HashMap<>();
						metadatosSubGrupoOrigen.put("Nombre", firmante.getNombre().trim());
						metadatosSubGrupoOrigen.put("Cargo", firmante.getCargo().trim());
						metadatosSubGrupoOrigen.put("Dependencia", firmante.getDependencia().trim());						
						listSubGrupoOrigen.add(metadatosSubGrupoOrigen);						
						metadatosSubGrupoOrigen.forEach( (k,v) -> log.info(raizLog + "Firmante ["+listSubGrupoOrigen.size()+"] - "+k+": ["+v+"]"));
					});

					//SE CARGAN LOS METADATOS DEL DESTINO
					log.info(raizLog + "Inicio carga de metadatos destinatario....");
					List<Map<String, Object>> listSubGrupoDestino = new ArrayList<>();
					
					destinatarios.forEach(destinatario -> {
						Map<String, Object> metadatosSubGrupoDestino = new HashMap<>();
						metadatosSubGrupoDestino.put("Nombre", (destinatario.getNombre() != null) ? destinatario.getNombre().trim() : "");
						metadatosSubGrupoDestino.put("Cargo", (destinatario.getCargo() != null) ? destinatario.getCargo().trim(): "");
						metadatosSubGrupoDestino.put("Dependencia / Entidad", (destinatario.getDependencia() != null) ? destinatario.getDependencia().trim() : "");
						metadatosSubGrupoDestino.put("Dirección", (destinatario.getRol() != null && !destinatario.getRol().contains("FT_CORR")) ? destinatario.getRol().trim() : "");						
						listSubGrupoDestino.add(metadatosSubGrupoDestino);
						metadatosSubGrupoDestino.forEach( (k,v) -> log.info(raizLog + "Destino ["+listSubGrupoDestino.size()+"] - "+k+": ["+v+"]"));
					});
					
					//SE CARGAN LOS METADATOS DE LAS COPIAS
					log.info(raizLog + "Inicio carga de metadatos copias....");
					List<Map<String, Object>> listSubGrupoCopias = new ArrayList<>();
					
					copias.forEach(copia -> {
						Map<String, Object> metadatosSubGrupoCopias = new HashMap<>();
						metadatosSubGrupoCopias.put("Nombre", (copia.getNombre() != null) ? copia.getNombre().trim() : "");
						metadatosSubGrupoCopias.put("Cargo", (copia.getCargo() != null) ? copia.getCargo().trim() : "");
						metadatosSubGrupoCopias.put("Dependencia / Entidad", (copia.getDependencia() != null) ? copia.getDependencia().trim() : "");
						metadatosSubGrupoCopias.put("Dirección", (copia.getRol() != null && !copia.getRol().contains("FT_CORR") && !copia.getRol().contains("PCR_COPIAS")) ? copia.getRol().trim() : "");
						listSubGrupoCopias.add(metadatosSubGrupoCopias);
						metadatosSubGrupoCopias.forEach( (k,v) -> log.info(raizLog + "Destino ["+listSubGrupoCopias.size()+"] - "+k+": ["+v+"]"));
					});
					
					//SE TRUNCAN LOS VALORES DE LOS METADATOS
					log.info(raizLog + "Se van a truncar los valores de todos los metadatos obtenidos....");
					trucarValoresCategoriaCorrespondencia(listSubGrupoOrigen);
					trucarValoresCategoriaCorrespondencia(listSubGrupoDestino);
					trucarValoresCategoriaCorrespondencia(listSubGrupoCopias);
					
					//SE CREA CATEGORIA Y SE ADICIONAN METADATOS
					log.info(raizLog+"Se va a crear categoria de correspondencia para el documento con id: ["+idDoc+"]");
					Map<String, List<Map<String, Object>>> mapSubgrupos = new HashMap<>();

					mapSubgrupos.put(categoriaProps.getCorrespondencia().getSubgrupoOrigen(), listSubGrupoOrigen);
					mapSubgrupos.put(categoriaProps.getCorrespondencia().getSubgrupoDestino(), listSubGrupoDestino);
					mapSubgrupos.put(categoriaProps.getCorrespondencia().getSubgrupoCopia(), listSubGrupoCopias);
					
					log.info(raizLog + "Id categoria correspondencia [" + categoriaProps.getCorrespondencia().getId() + "]");

					//SE ADICIONAN LOS METADATOS DEL ORIGEN A LA CATEGORIA DEL DOCUMENTO
					Boolean esPrimerFirmante = true;
					log.info(raizLog + "Se van a a agregar los metadatos del origen a la categoría del documento...");
					
					for (Map<String, Object> mapOrigen : listSubGrupoOrigen) {
						ContenidoDocumento.adicionarCategoriaMetadata(autenticacionCS.getAdminSoapHeader(), idDoc,
								categoriaProps.getCorrespondencia().getId(),
								categoriaProps.getCorrespondencia().getNombre(), metadatos,
								categoriaProps.getCorrespondencia().getSubgrupoOrigen(), mapOrigen, esPrimerFirmante,
								wsdlsProps.getDocumento());
						esPrimerFirmante=false;

					}
					
					//SE ADICIONAN LOS METADATOS DEL DESTINO A LA CATEGORIA DEL DOCUMENTO
					Boolean esPrimerDestino = true;
					log.info(raizLog + "Se van a a agregar los metadatos del destino a la categoría del documento...");
					for (Map<String, Object> mapDestino : listSubGrupoDestino) {
						ContenidoDocumento.adicionarCategoriaMetadata(autenticacionCS.getAdminSoapHeader(), idDoc,
								categoriaProps.getCorrespondencia().getId(),
								categoriaProps.getCorrespondencia().getNombre(), metadatos,
								categoriaProps.getCorrespondencia().getSubgrupoDestino(), mapDestino, esPrimerDestino,
								wsdlsProps.getDocumento());
						esPrimerDestino = false;

					}
					
					//SE ADICIONAN LOS METADATOS DE LAS COPIAS A LA CATEGORIA DEL DOCUMENTO
					Boolean esPrimeraCopia = true;
					log.info(raizLog + "Se van a a agregar los metadatos de las copias a la categoría del documento...");
					if (listSubGrupoCopias != null && !listSubGrupoCopias.isEmpty() && listSubGrupoCopias.size() > 0) {

						for (Map<String, Object> mapCopias : listSubGrupoCopias) {
							ContenidoDocumento.adicionarCategoriaMetadata(autenticacionCS.getAdminSoapHeader(), idDoc,
									categoriaProps.getCorrespondencia().getId(),
									categoriaProps.getCorrespondencia().getNombre(), metadatos,
									categoriaProps.getCorrespondencia().getSubgrupoCopia(), mapCopias, esPrimeraCopia,
									wsdlsProps.getDocumento());
							esPrimeraCopia = false;
						}

					}

					log.info(raizLog+"Se creó correctamente la categoria de correspondencia para el documento con id: ["+idDoc+"]");
					
					
					//SI HAY COPIAS COMPULSADAS...
					if(esPersonalizado) {						
						log.info(raizLog+"Se va a agregar metadata a cada doc Personalizado");
						for(Long idDocPersonalizado : listaIdDocsPersonalizados) {

							//SE ADICIONAN LOS METADATOS DEL ORIGEN A LA CATEGORIA DEL DOCUMENTO
							esPrimerFirmante = true;
							log.info(raizLog + "Se van a a agregar los metadatos del origen a la categoría del documento...");
							
							for (Map<String, Object> mapOrigen : listSubGrupoOrigen) {
								ContenidoDocumento.adicionarCategoriaMetadata(autenticacionCS.getAdminSoapHeader(), idDocPersonalizado,
										categoriaProps.getCorrespondencia().getId(),
										categoriaProps.getCorrespondencia().getNombre(), metadatos,
										categoriaProps.getCorrespondencia().getSubgrupoOrigen(), mapOrigen, esPrimerFirmante,
										wsdlsProps.getDocumento());
								esPrimerFirmante=false;

							}
							
							//SE ADICIONAN LOS METADATOS DEL DESTINO A LA CATEGORIA DEL DOCUMENTO
							esPrimerDestino = true;
							log.info(raizLog + "Se van a a agregar los metadatos del destino a la categoría del documento...");
							for (Map<String, Object> mapDestino : listSubGrupoDestino) {
								ContenidoDocumento.adicionarCategoriaMetadata(autenticacionCS.getAdminSoapHeader(), idDocPersonalizado,
										categoriaProps.getCorrespondencia().getId(),
										categoriaProps.getCorrespondencia().getNombre(), metadatos,
										categoriaProps.getCorrespondencia().getSubgrupoDestino(), mapDestino, esPrimerDestino,
										wsdlsProps.getDocumento());
								esPrimerDestino = false;

							}
							
							//SE ADICIONAN LOS METADATOS DE LAS COPIAS A LA CATEGORIA DEL DOCUMENTO
							esPrimeraCopia = true;
							log.info(raizLog + "Se van a a agregar los metadatos de las copias a la categoría del documento...");
							if (listSubGrupoCopias != null && !listSubGrupoCopias.isEmpty() && listSubGrupoCopias.size() > 0) {

								for (Map<String, Object> mapCopias : listSubGrupoCopias) {
									ContenidoDocumento.adicionarCategoriaMetadata(autenticacionCS.getAdminSoapHeader(), idDocPersonalizado,
											categoriaProps.getCorrespondencia().getId(),
											categoriaProps.getCorrespondencia().getNombre(), metadatos,
											categoriaProps.getCorrespondencia().getSubgrupoCopia(), mapCopias, esPrimeraCopia,
											wsdlsProps.getDocumento());
									esPrimeraCopia = false;
								}

							}

							log.info(raizLog+"Se creó correctamente la categoria de correspondencia para el documento con id: ["+idDocPersonalizado+"]");
						}
					}

					// SE ENVIA EVENTO PARA CAMBIO DE ESTADO
					context.getStateMachine().sendEvent(Events.IR_ENVIAR_FIRMA_CA_ME);
					log.info(raizLog + "Se envió el evento: [" + Events.IR_ENVIAR_FIRMA_CA_ME+ "]  para la maquina con workId: [" + workId + "]");

					
				} catch (Exception e) {
					// SE ENVIA EVENTO DE FIN CON ERRORES Y SE CARGAN VARIABLES DE ERROR EN EL CONTEXTO DE LA SSM
					String mensajeError = "Error no se pudo cargar la categoria de correspondencia al doc con id: ["+idDoc+"] - " + e.getMessage();
					
					enviarEventoFinErrores(raizLog, context, workId, nombreEstadoActual, mensajeError, e);

				}
				
				imprimirLogMensajeFinEstado(raizLog, nombreEstadoActual);//Se marca el fin del estado en el log
				
			};
		}
		
		
		/**
		* Metodo que permite hacer un llamado a un servicio web de la app "Firma"
		* 
		* @return
		*/
		@Bean
		public Action<States, Events> enviarFirmaCaMe() {
			String nombreEstadoActual = States.ENVIAR_FIRMA_CA_ME.toString();
			//SE RETORNA EL CONTEXTO DE LA MÁQUINA DE ESTADOS
			return (context) -> {//Se retorna contexto de la máquina de estados (Contexto)
				//SE EXTRAE LA RAIZ LOG DEL CONTEXTO
				String raizLog = SSMUtils.getRaizLog(context);//Variable para inicio de impresión del log
				
				imprimirLogMensajeInicioEstado(raizLog, nombreEstadoActual);// Se marca el incio del estado en el log

				// SE CARGAN LAS VARIABLES ALMACENADAS EN EL CONTEXTO DE LA SSM
				String workId = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);
				String numRadicado = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO, String.class);
				String asuntoFirma = workId+" - "+numRadicado;
				Long idDocOriginal = context.getExtendedState().get(Consts.ID_DOC_RADICACION, Long.class);
				String conexionCS = context.getExtendedState().get(Consts.CONEXION_CS_SOLICITANTE, String.class);
				String nombreUsuarioCS = context.getExtendedState().get(Consts.NOMBRE_SOLICITANTE, String.class);;
				Long idUsuarioCS = context.getExtendedState().get(Consts.ID_SOLICITANTE, Long.class);
				String listaFirmantes = context.getExtendedState().get(Consts.LISTA_NIT_FIRMANTES, String.class);;
				String accionExtaerna = "memorando";
				Boolean actualizarCatDoc = false; //TODO Esto revisar en el rediseño
				Boolean notificarSolicitante = false;
				String idDocsPersonalizados = context.getExtendedState().get(Consts.CADENA_LISTA_ID_DOCS_PERSONALIZADOS_CARTA, String.class);
				//se define si se necesita copia compulsada
				String tipologia = context.getExtendedState().get(Consts.TIPOLOGIA_MEMO_CARTA, String.class);
				Boolean copiaCompulsada = false;
				if(tipologia.equalsIgnoreCase("CA")) {
					copiaCompulsada=true;
				}
				
				// TODO SE DEBE GENERAR LA RUTA EN EL ARCHIVO DE PROPERTIES DE CORR
				String strURLDestino = "http://172.23.30.67:8183/iconecta-firma/iniciarFirmaCaMe";//Url de firma a la cual se va a llamar
				String urlRetorno = "http://172.23.30.67:8183/iconecta-correspondencia/corr/resultadoFirma";//url de retorno para CORR
				
				// SE LLENA EL HASHMAP CON LOS PARAMETROS DE LA URL DESTINO
				HashMap<String, Object> params;
				params = new LinkedHashMap<>();
				params.put("idWorkflow", workId);
				params.put("idDoc", idDocOriginal);
				params.put("idDocs", idDocsPersonalizados);
				params.put("asunto", asuntoFirma);
				params.put("copiaCompulsada", copiaCompulsada);
				params.put("conexionUsuario", conexionCS);
				params.put("nombreUsuario", nombreUsuarioCS);
				params.put("idUsuario", idUsuarioCS);					
				params.put("accionExterna", accionExtaerna);
				params.put("urlExterna", urlRetorno);
				params.put("actualizarCatDocumento", actualizarCatDoc);//TODO revisar esto en el rediseño
				params.put("notificarSolicitante", notificarSolicitante);
				params.put("listaFirmantes", listaFirmantes);

				//SE REALIZA LLAMADO AL SERVICIO WEB
				try {
					log.info(raizLog + "Se va a llamar al controlador de FIRMA - iniciarFirmaCaMe....");
					ControllerCaller.llamarAplicacionFirma(raizLog, params, strURLDestino, "POST");
					log.info(raizLog + "Llamado al servicio web realizado con éxito");
					
					//SE ENVIA EVENTO PARA CAMBIO DE ESTADO
					context.getStateMachine().sendEvent(Events.IR_ESPERAR_RESULTADO_FIRMA_CA_ME);
					log.info(raizLog + "Se envió el evento: ["+Events.IR_ESPERAR_RESULTADO_FIRMA_CA_ME+"]  para la maquina con workId: [" + workId +"]");
					
				} catch (Exception e) {
					// SE ENVIA EVENTO DE FIN CON ERRORES Y SE CARGAN VARIABLES DE ERROR EN EL CONTEXTO DE LA SSM
					String mensajeError = "Error hacer el llamado a la aplicación PDFUTILS - " + e.getMessage();
					
					enviarEventoFinErrores(raizLog, context, workId, nombreEstadoActual, mensajeError, e);
					
				}
				
				imprimirLogMensajeFinEstado(raizLog, nombreEstadoActual);//Se marca el fin del estado en el log
				
			};
		}
		
		
		
		/**
		* Metodo para esperar respuesta de Firma
		* 
		* @return
		*/
		@Bean
		public Action<States, Events> esperarResultadoFirmaCaMe() {
			String nombreEstadoActual = States.ESPERAR_RESULTADO_FIRMA_CA_ME.toString();
			//SE RETORNA EL CONTEXTO DE LA MÁQUINA DE ESTADOS
			return (context) -> {//Se retorna contexto de la máquina de estados (Contexto)
				//SE EXTRAE LA RAIZ LOG DEL CONTEXTO
				String raizLog = SSMUtils.getRaizLog(context);//Variable para inicio de impresión del log
				
				imprimirLogMensajeInicioEstado(raizLog, nombreEstadoActual);// Se marca el incio del estado en el log
				
				log.info(raizLog + "esperando respuesta de portafirmas....");
				
				imprimirLogMensajeFinEstado(raizLog, nombreEstadoActual);//Se marca el fin del estado en el log
				
				//SE ALMACENA EN BD EL CONTEXTO DE LA SSM
				try {
					log.info("Guardando en la tabla MAQUINA");
					persistContexto(context);

				} catch (Exception e) {
					log.error(raizLog + "ERROR GUARDANDO LA MAQUINA [" + context.getStateMachine().getId() + "] en la base de datos", e);
				}
				
			};
		}
		
		
		
		/**
		* Metodo para EVALUAR respuesta de Firma
		* 
		* @return
		*/
		@Bean
		public Action<States, Events> evaluarResultadoFirmaCaMe() {
			String nombreEstadoActual = States.EVALUAR_RESULTADO_FIRMA_CA_ME.toString();
			//SE RETORNA EL CONTEXTO DE LA MÁQUINA DE ESTADOS
			return (context) -> {//Se retorna contexto de la máquina de estados (Contexto)
				//SE EXTRAE LA RAIZ LOG DEL CONTEXTO
				String raizLog = SSMUtils.getRaizLog(context);//Variable para inicio de impresión del log
				
				imprimirLogMensajeInicioEstado(raizLog, nombreEstadoActual);// Se marca el incio del estado en el log
				
				//SE CARGAN LAS VARIABLES GUARDADAS EN EL HEADER DEL CONTROLADOR
				Boolean resultadoFirma = context.getMessageHeaders().get(Consts.RESULTADO_FIRMA, Boolean.class);
				String mensajeFirma = context.getMessageHeaders().get(Consts.MENSAJE_RESPUESTA_FIRMA, String.class);
				if(mensajeFirma!=null && !mensajeFirma.isEmpty()){
					context.getExtendedState().getVariables().put(Consts.MENSAJE_RESPUESTA_FIRMA, mensajeFirma);
				}				
				String nombreUsuarioCancelaFirma = context.getMessageHeaders().get(Consts.NOMBRE_USUARIO_CANCELA, String.class);
				if(nombreUsuarioCancelaFirma!=null && !nombreUsuarioCancelaFirma.isEmpty()){
					context.getExtendedState().getVariables().put(Consts.NOMBRE_USUARIO_CANCELA, nombreUsuarioCancelaFirma);
				}	
				
				// SE CARGAN LAS VARIABLES ALMACENADAS EN EL CONTEXTO DE LA SSM
				String workId = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);
				String tipologia = context.getExtendedState().get(Consts.TIPOLOGIA_MEMO_CARTA, String.class);
				
				if(resultadoFirma && tipologia.equalsIgnoreCase("ME")) {
					// SE ENVIA EVENTO PARA CAMBIO DE ESTADO
					context.getStateMachine().sendEvent(Events.IR_DISTRIBUIR_MEMORANDO);
					log.info(raizLog + "Se envió el evento: [" + Events.IR_DISTRIBUIR_MEMORANDO+ "]  para la maquina con workId: [" + workId + "]");
					
				}else if(resultadoFirma && tipologia.equalsIgnoreCase("CA")){
					// SE ENVIA EVENTO PARA CAMBIO DE ESTADO
					context.getStateMachine().sendEvent(Events.IR_GENERAR_STICKER_CARTA);
					log.info(raizLog + "Se envió el evento: [" + Events.IR_GENERAR_STICKER_CARTA+ "]  para la maquina con workId: [" + workId + "]");
					
				}else {
					// SE ENVIA EVENTO PARA CAMBIO DE ESTADO
					context.getStateMachine().sendEvent(Events.FIRMA_CANCELADA);
					log.info(raizLog + "Se envió el evento: [" + Events.FIRMA_CANCELADA+ "]  para la maquina con workId: [" + workId + "]");
					
				}

				log.info(raizLog + "esperando respuesta de portafirmas....");
				
				imprimirLogMensajeFinEstado(raizLog, nombreEstadoActual);//Se marca el fin del estado en el log
				
				
			};
		}
		
		
		/**
		* Metodo para esperar respuesta de Firma
		* 
		* @return
		*/
		@Bean
		@SuppressWarnings("unchecked")
		public Action<States, Events> distribuirMemorando() {
			String nombreEstadoActual = States.DISTRIBUIR_MEMORANDO.toString();
			//SE RETORNA EL CONTEXTO DE LA MÁQUINA DE ESTADOS
			return (context) -> {//Se retorna contexto de la máquina de estados (Contexto)
				//SE EXTRAE LA RAIZ LOG DEL CONTEXTO
				String raizLog = SSMUtils.getRaizLog(context);//Variable para inicio de impresión del log
				
				imprimirLogMensajeInicioEstado(raizLog, nombreEstadoActual);// Se marca el incio del estado en el log
				
				
				////////////////////////////////////////////////////////////////////////////

				// SE CARGAN LAS VARIABLES ALMACENADAS EN EL CONTEXTO DE LA SSM				
				List<Empleado> destinatarios = (List<Empleado>) context.getExtendedState().get(Consts.DESTINATARIOS, Object.class);
				List<Empleado> copias = (List<Empleado>) context.getExtendedState().get(Consts.COPIAS, Object.class);
				long idDocOriginal = context.getExtendedState().get(Consts.ID_DOC_RADICACION, Long.class);
				String tipoWfDoc = context.getExtendedState().get(Consts.TIPO_WF_DOCUMENTO, String.class);
				String numeroRadicado = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO, String.class);
				String workIdText = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);
				long workId = Long.valueOf(workIdText).longValue();//Se convierte a Long
				long idSolicitante = context.getExtendedState().get(Consts.ID_SOLICITANTE, Long.class);

				// SE EXTRAEN Y SE MUESTRAN LOS DATOS DE NOMBRES Y ROLES DE LOS DESTINATARIOS (DESTINOS Y COPIAS)		
				List<String> listaRolesDestinatarios = new ArrayList<>();
				List<String> listaNombresDestinatarios = new ArrayList<>();
				
				log.info(raizLog + "A continuación de muestran los destinos y las copias del memorando:");
				//DATOS PARA DESTINOS
				destinatarios.forEach(destino -> {
					listaRolesDestinatarios.add(destino.getRol());
					listaNombresDestinatarios.add(destino.getNombre());
					log.info(raizLog + "Destino ["+destinatarios.indexOf(destino)+"] - Nombre ["+destino.getNombre()+"] - Rol ["+destino.getRol()+"]");
				});
				//DATOS PARA COPIAS
				copias.forEach(copia -> {
					listaRolesDestinatarios.add(copia.getRol());
					listaNombresDestinatarios.add(copia.getNombre());
					log.info(raizLog + "Copia ["+copias.indexOf(copia)+"] - Nombre ["+copia.getNombre()+"] - Rol ["+copia.getRol()+"]");
				});
				
				
				// SE ELIMINA EL ROL QUE TENGA EL NOMBRE "00_PCR_COPIAS"
				listaRolesDestinatarios.removeIf(rolDestino -> rolDestino.equalsIgnoreCase(Consts.ROL_SIN_COPIAS));
				

				int totalDestinatarios = listaRolesDestinatarios.size();
				int totalConfirmadosDistribuir = 0;
				int totalRechazadosDistribuir = 0;

				context.getExtendedState().getVariables().put(Consts.LISTA_ROLES_DESTINATARIOS, listaRolesDestinatarios);

				context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS,
						totalDestinatarios);
				context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS_CONFIRMADOS,
						totalConfirmadosDistribuir);
				context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS_RECHAZADOS,
						totalRechazadosDistribuir);

				long mapId = 0L;
				if (tipoWfDoc.equalsIgnoreCase("Confidencial")) {
					log.info(raizLog + "Es confidencial, WF distribucion");
					mapId = workflowProps.getDistribucionMemorandoConfidencial();
				}else {
					mapId = workflowProps.getDistribucionMemorando();
				}

				

				log.info(raizLog + "INICIO actualizar estado correspondencia: DISTRIBUIR_MEMORANDO");
				
				try {
					
				actualizarEstadoCorrespondencia(raizLog, idDocOriginal,
						estadosProps.getDistribuido());

				} catch (SOAPException  e) {
					
					log.error(raizLog + "ErrorCorrespondencia distribuir obtenerIdGrupo" + e);							
					cargarError(context, e);
					//context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_MEMORANDO);
					context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_MEMORANDO);
					context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
				}
				
				log.info(raizLog + "FIN actualizar estado correspondencia: DISTRIBUIR_MEMORANDO");

				int contador = 0;
				int numberToKeep = 1;
				log.info(raizLog + "INICIO PROCESO DISTRIBUIR_MEMORANDO");
				Long idGrupo=new Long(0);
				
				

				for (String nombreGrupo : listaRolesDestinatarios) {

					String nombreDestinatario = listaNombresDestinatarios.get(contador);
					String titulo = numeroRadicado + " - " + nombreDestinatario;

					if (tipoWfDoc.equalsIgnoreCase("Confidencial")) {
						titulo += " - Confidencial";
					}
					
					log.info(raizLog + "Nombre grupo: " + nombreGrupo);
					
					try {
						idGrupo = obtenerIdGrupo(nombreGrupo, autenticacionCS.getAdminSoapHeader());
					} catch (IOException | SOAPException   e) {
						
						log.error(raizLog + "ErrorCorrespondencia distribuir obtenerIdGrupo" + e);							
						cargarError(context, e);
						context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_MEMORANDO);
						context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
					}
					
					Map<String, ValorAtributo> atributos = new HashMap<String, ValorAtributo>();
					atributos.put(workflowProps.getAtributo().getRolDestino(),
							new ValorAtributo(idGrupo, TipoAtributo.USER));
					atributos.put(workflowProps.getAtributo().getNumeroRadicado(),
							new ValorAtributo(numeroRadicado, TipoAtributo.STRING));
					atributos.put(workflowProps.getAtributo().getIdDocumentoOriginal(),
							new ValorAtributo(idDocOriginal, TipoAtributo.INTEGER));
					atributos.put(workflowProps.getAtributo().getNombreDestinatario(),
							new ValorAtributo(nombreDestinatario, TipoAtributo.STRING));
					atributos.put(workflowProps.getAtributo().getIdWorkflow(),
							new ValorAtributo(workId, TipoAtributo.INTEGER));
					atributos.put(workflowProps.getAtributo().getIdSolicitante(),
							new ValorAtributo(idSolicitante, TipoAtributo.USER));

					List<Long> listIdAdjuntos = new ArrayList<>();
					listIdAdjuntos.add(idDocOriginal);
					int distribuido = 0;
					int operacion=0;
										
					try {
						
						long process = 0L;
						
						distribuido = context.getExtendedState().get(Consts.DISTRIBUIDO, Integer.class)== null ? 0 : context.getExtendedState().get(Consts.DISTRIBUIDO, Integer.class);
						operacion =  context.getExtendedState().get(Consts.OPERACION, Integer.class) == null ? 0 : context.getExtendedState().get(Consts.OPERACION, Integer.class);
						
						log.info(raizLog + "Operacion: " + operacion + " distribuido: " + distribuido);
						
						if(distribuido == contador && operacion == 0){
						
							log.info(raizLog + "INICIO WF de Distribucion para el ROL:" + nombreGrupo
									+ " y el destinatario:" + nombreDestinatario);
							process = Workflow.iniciarWorkflowAtributosAdjuntos(
									autenticacionCS.getAdminSoapHeader(), mapId, titulo, atributos,
									listIdAdjuntos, wsdlsProps.getWorkflow());
							
							log.info(raizLog + "Flujo de Distribucion Iniciado: " + process);
							
							context.getExtendedState().getVariables().put(Consts.OPERACION, 1);
							context.getExtendedState().getVariables().put(Consts.ID_WORKFLOW_DIS, process);
							operacion++;
						
						}
																				
						long idDocAdjunto = 0L;
						
						if(distribuido == contador && operacion == 1){
							
							process = (context.getExtendedState().get(Consts.ID_WORKFLOW_DIS, Long.class)==null) ? 0 : context.getExtendedState().get(Consts.ID_WORKFLOW_DIS, Long.class) ;
							
							log.info(raizLog + "INICIO nueva forma de quitar categoria a adjunto");
															
							idDocAdjunto = (long) Workflow
									.obtenerValoresAtributo(autenticacionCS.getAdminSoapHeader(),
											process, "_idDocumentoCopia", wsdlsProps.getWorkflow())
									.get(0);
							
							context.getExtendedState().getVariables().put(Consts.OPERACION, 2);
							context.getExtendedState().getVariables().put(Consts.ID_DOCUMENTO_ADJUNTO_DIS, idDocAdjunto);
							operacion++;
												
						
						}
													
						Metadata metadataAdjunto = null;
						
						if(distribuido == contador && operacion == 2){
							
							idDocAdjunto = (context.getExtendedState().get(Consts.ID_DOCUMENTO_ADJUNTO_DIS, Long.class)== null) ?0: context.getExtendedState().get(Consts.ID_DOCUMENTO_ADJUNTO_DIS, Long.class);
							
							Node documentoAdjunto = ContenidoDocumento.getDocumentoById(
									autenticacionCS.getAdminSoapHeader(), idDocAdjunto,
									wsdlsProps.getDocumento());

							metadataAdjunto = documentoAdjunto.getMetadata();

							AttributeGroup categoriaCorrespondencia = null;
							AttributeGroup categoriaFirma = null;
							categoriaCorrespondencia = metadataAdjunto.getAttributeGroups().stream()
									.filter(unaCategoria -> unaCategoria.getDisplayName()
											.equals(categoriaProps.getCorrespondencia().getNombre()))
									.findFirst().get();

							categoriaFirma = metadataAdjunto.getAttributeGroups().stream()
									.filter(unaCategoria -> unaCategoria.getDisplayName()
											.equals(categoriaProps.getNombreCategoriaFirma()))
									.findFirst().get();

							metadataAdjunto.getAttributeGroups().clear();

							metadataAdjunto.getAttributeGroups().add(categoriaCorrespondencia);
							metadataAdjunto.getAttributeGroups().add(categoriaFirma);

							documentoAdjunto.setMetadata(metadataAdjunto);
							
							context.getExtendedState().getVariables().put(Consts.OPERACION, 3);
							context.getExtendedState().getVariables().put(Consts.METADATOS_ADJUNTOS_DIS, metadataAdjunto);
							operacion++;
						
						}
													
						if(distribuido == contador && operacion == 3){
						
							idDocAdjunto = (context.getExtendedState().get(Consts.ID_DOCUMENTO_ADJUNTO_DIS, Long.class)== null) ? 0 : context.getExtendedState().get(Consts.ID_DOCUMENTO_ADJUNTO_DIS, Long.class);
							metadataAdjunto =  (Metadata) context.getExtendedState().get(Consts.METADATOS_ADJUNTOS_DIS, Object.class);
							
							ContenidoDocumento.actualizarMetadataDocumento(
								autenticacionCS.getAdminSoapHeader(), idDocAdjunto, metadataAdjunto,
								wsdlsProps.getDocumento());

							log.info(raizLog + "FIN nueva forma de quitar categoria a adjunto");
							
							context.getExtendedState().getVariables().put(Consts.OPERACION, 4);
							operacion++;
						}
													
						if(distribuido == contador && operacion== 4){
						
							log.info(raizLog + "INICIO cambiar nombre Doc para cada copia" + nombreGrupo);
							idDocAdjunto = (context.getExtendedState().get(Consts.ID_DOCUMENTO_ADJUNTO_DIS, Long.class)== null) ? 0: context.getExtendedState().get(Consts.ID_DOCUMENTO_ADJUNTO_DIS, Long.class);

							ContenidoDocumento.cambiarNombreDocumento(
									autenticacionCS.getAdminSoapHeader(), idDocAdjunto,
									numeroRadicado + " - " + nombreDestinatario, wsdlsProps.getDocumento());

							log.info(raizLog + "FIN cambiar nombre Doc para cada copia" + nombreGrupo);
							
							context.getExtendedState().getVariables().put(Consts.OPERACION, 5);
							operacion++;
						}
													
						if(distribuido == contador && operacion== 5){
						
							log.info(raizLog + "INICIO eliminar versiones doc distribuido");
							idDocAdjunto = (context.getExtendedState().get(Consts.ID_DOCUMENTO_ADJUNTO_DIS, Long.class)== null) ? 0: context.getExtendedState().get(Consts.ID_DOCUMENTO_ADJUNTO_DIS, Long.class);

							ContenidoDocumento.eliminarVersionesDocumento(
									autenticacionCS.getAdminSoapHeader(), idDocAdjunto, numberToKeep,
									wsdlsProps.getDocumento());
							log.info(raizLog + "FIN eliminar versiones doc distribuido");

							log.info(raizLog + "FIN WF de Distribucion");
							
							int suma = contador;
							context.getExtendedState().getVariables().put(Consts.OPERACION, 0);
							context.getExtendedState().getVariables().put(Consts.DISTRIBUIDO, ++suma);
						
						}
						
					} catch (NumberFormatException e) {
						cargarError(context, new Exception(e.getMessage() + " Destinatarios distribuidos: " + distribuido + " de " + destinatarios.size() + " operacion: " + operacion + " de 5"));
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						log.error(raizLog + "ErrorCorrespondencia distribuir getDocumentoById " + e , e);							
					} catch (NoSuchElementException | NullPointerException e) {
						cargarError(context, new Exception(e.getMessage() + " Destinatarios distribuidos: " + distribuido + " de " + destinatarios.size() + " operacion: " + operacion + " de 5"));
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						log.error(raizLog + "ErrorCorrespondencia distribuir getDocumentoById " + e , e);	
					} catch (SOAPException | WebServiceException  | FileNotFoundException e) {
						log.error(raizLog + "ErrorCorrespondencia distribuir getDocumentoById " + e, e);
						cargarError(context, new Exception(e.getMessage() + " Destinatarios distribuidos: " + distribuido + " de " + destinatarios.size() + " operacion: " + operacion + " de 5"));
						context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_MEMORANDO);
						context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
					} catch (IOException e) {
						log.error(raizLog + "ErrorCorrespondencia distribuir getDocumentoById " + e);							
						cargarError(context, new Exception(e.getMessage() + " Destinatarios distribuidos: " + distribuido + " de " + destinatarios.size() + " operacion: " + operacion + " de 5"));
						context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_MEMORANDO);
						context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
					} catch (InterruptedException e) {
						cargarError(context, new Exception(e.getMessage() + " Destinatarios distribuidos: " + distribuido + " de " + destinatarios.size() + " operacion: " + operacion + " de 5"));
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						log.error(raizLog + "ErrorCorrespondencia distribuir getDocumentoById " + e , e);							
					}
					contador++;
				}
				
				///////////////////////////////////////////////////////////////////////////
				
				imprimirLogMensajeFinEstado(raizLog, nombreEstadoActual);//Se marca el fin del estado en el log
				
				
			};
		}
		
		
		
		/**
		 * Metodo que permite generarl el sticker para Carta
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> generarStickerCarta() {
			String nombreEstadoActual = States.GENERAR_STICKER_CARTA.toString();
			//SE RETORNA EL CONTEXTO DE LA MÁQUINA DE ESTADOS
			return (context) -> {//Se retorna contexto de la máquina de estados (Contexto)
				//SE EXTRAE LA RAIZ LOG DEL CONTEXTO
				String raizLog = SSMUtils.getRaizLog(context);//Variable para inicio de impresión del log
				
				imprimirLogMensajeInicioEstado(raizLog, nombreEstadoActual);// Se marca el incio del estado en el log
				
				// SE CARGAN LAS VARIABLES ALMACENADAS EN EL CONTEXTO DE LA SSM
				String workId = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);
				
				log.info(raizLog + "SE DECIDE NO GENERAR STICKER");
				// SE ENVIA EVENTO PARA CAMBIO DE ESTADO
				context.getStateMachine().sendEvent(Events.IR_SUBIR_DOC_CONSECUTIVO);
				log.info(raizLog + "Se envió el evento: [" + Events.IR_SUBIR_DOC_CONSECUTIVO+ "]  para la maquina con workId: [" + workId + "]");
				
				imprimirLogMensajeFinEstado(raizLog, nombreEstadoActual);//Se marca el fin del estado en el log
				
				//SE ALMACENA EN BD EL CONTEXTO DE LA SSM
				try {
					log.info("Guardando en la tabla MAQUINA");
					persistContexto(context);

				} catch (Exception e) {
					log.error(raizLog + "ERROR GUARDANDO LA MAQUINA [" + context.getStateMachine().getId() + "] en la base de datos", e);
				}
				
			};
		}
		
		
		
		
		/**
		 * Metodo que permite subir el documento Carta a la carpeta consecutivo
		 * @return
		 */
		@Bean
		public Action<States, Events> subirDocConsecutivo() {
			String nombreEstadoActual = States.SUBIR_DOC_CONSECUTIVO.toString();
			return (context) -> {
				//SE EXTRAE LA RAIZ LOG DEL CONTEXTO
				String raizLog = SSMUtils.getRaizLog(context);//Variable para inicio de impresión del log
				
				imprimirLogMensajeInicioEstado(raizLog, nombreEstadoActual);// Se marca el incio del estado en el log
				
				Boolean errorMaquina = context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class);
				if (!errorMaquina) {					
					// SE CARGAN VARIABLES ALMACENADAS EN EL CONTEXTO DE LA SSM
					String workId = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);
					MetadatosPlantilla metadatosPlantilla = context.getExtendedState().get(Consts.METADATOS_PLANTILLA, MetadatosPlantilla.class);
					String siglaOrigen = context.getExtendedState().get(Consts.SIGLA_FIRMANTE, String.class);
					String numeroRadicadoObtenido = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO, String.class);
					Long idDocRadicado = context.getExtendedState().get(Consts.ID_DOC_RADICACION, Long.class);					
					
					if(!metadatosPlantilla.getEsFondoIndependiente()) {
						siglaOrigen = "BR";
					}

					//SE BUSCA ID DE LA CATEGORIA, BASADO EN LA FECHA ACTUAL
					LocalDate date = LocalDate.now();
					int anho = date.getYear();
					int mes = date.getMonthValue();
					log.info(raizLog + "Se va a buscar la carpeta correspondiente al año ["+anho+"] y el mes ["+mes+"] en el archivo XML del servidor OTBR");
					Long parentIdDestino = readXML.getIdFolderXML(anho, mes,  siglaOrigen, "ConsecutivoCarta", globalProperties.getRutaXML());
					log.info(raizLog + "Id consecutivo encontrado [" + parentIdDestino + "]");
					//TODO SE DEBE VALIDAR QUE EL ID EXISTA,  SI NO EXISTE INFORMAR AL ADMIN QUE NO SE PUDO CARGAR Y SEGUIR PROCESO DE DIST			
					
					
					//SE PRE-CARGAN LOS METADATOS DE LA CATEGORIA BR01-DOCUMENTO
					String nombreCategoriaDocumento = categoriaDocProps.getNombreCategoria();
					int idCategoriaDocumento = Integer.parseInt(categoriaDocProps.getId().toString());
					log.info(raizLog + "Se van a precargar los metadatos de la categoria ["+nombreCategoriaDocumento+"]");
					Map<String, Object> metadatosCatDoc = new HashMap<String, Object>();
					String tipoDocumental = readXML.getTypeDcocumentXML(date.getYear(), date.getMonthValue(), siglaOrigen, "ConsecutivoCarta", globalProperties.getRutaXML());
					String serie = readXML.getSerieDocumentXML(date.getYear(), date.getMonthValue(), siglaOrigen, "ConsecutivoCarta", globalProperties.getRutaXML());
					metadatosCatDoc.put("Tipo documental", tipoDocumental);
					metadatosCatDoc.put("Serie", serie);
					log.info(raizLog + "Se han precargado los metadatos: 'Tipo documental' ["+tipoDocumental+"]  -  'Serie' [" +serie+"]" );
					
					//SE INICIALIZAN VARIABLES
					Long idDocConsecutivo = null;			
					
					String estadoDistribuido = estadosProps.getDistribuido();
					
						log.info(raizLog + "Se va a copiar el Documento en el consecutivo CARTA");	
						String nuevoNombreDocConsecutivoCarta = numeroRadicadoObtenido+".pdf";//SE CARGA NUEVO NOMBRE DEL DOC EN EL CONSECUTIVO
						//SE AGREGA LA CATEGORIA BR01-DOCUMENTO AL DOCUMENTO - CARTA ORIGINAL
						try {
							String nombreSubGrupo = null;
							Map<String, Object> subGrupoMetadatos = null;
							ContenidoDocumento.adicionarCategoriaMetadata(autenticacionCS.getAdminSoapHeader(), idDocRadicado, idCategoriaDocumento, nombreCategoriaDocumento, metadatosCatDoc, nombreSubGrupo, subGrupoMetadatos, Boolean.TRUE, wsdlsProps.getDocumento());
							log.info(raizLog + "Se cargó correctamente la categoria ["+nombreCategoriaDocumento+"] al documento con idDoc ["+idDocRadicado+"]"); 
						} catch (Exception e) {
							//SE ENVIA MENSAJE AL ADMINISTRADOR CON EL ERROR, NO SE DETIENE EL PROCESO
							log.error(raizLog + "Se presentó error al intentar agregar categoria [" + nombreCategoriaDocumento+"] al documento con idDoc ["+idDocRadicado+"]");
							String mensajeError = "No se ha podido cargar la categoria '"+nombreCategoriaDocumento+"' al documento con idDoc '"+idDocRadicado+"' \n"
									+ "Debido a esta falla no se pudo cargar el documento en el consecutivo CARTA. Deberá cargarlo manualmente";
							String estadoActualError = context.getStateMachine().getState().getId().name().toString();
							SSMUtils.notificarErrorAdmin(raizLog, workId, numeroRadicadoObtenido, mensajeError, estadoActualError, globalProperties, autenticacionCS, wsdlsProps);
							//TO AVOID EXECUTION OF FURTHER CODE
							//return;							
						}
						
						//SE ACTUALIZA EL ESTADO DE LA CATEGORIA 'CORRESPONDENCIA' A 'DISTRIBUIDO'
						try {
							actualizarEstadoCorrespondencia(raizLog, idDocRadicado, estadoDistribuido);
							log.info(raizLog + "Se actualizó el metadato 'Estado' de la cat Correspondencia con el valor ["+estadoDistribuido+"]");
						} catch (SOAPException e1) {
							//SE ENVIA MENSAJE AL ADMINISTRADOR CON EL ERROR, NO SE DETIENE EL PROCESO
							log.error(raizLog + "Se presentó error al intentar actualizar el estado de la categoria 'CORRESPONDENCIA' en el documento con idDoc ["+idDocRadicado+"]");
							String mensajeError = "No se ha podido actualizar el estado de la categoria de CORRESPONDENCIA a '"+estadoDistribuido+"' en el documento con idDoc '"+idDocRadicado+"' \n"
									+ "Debido a esta falla no se pudo cargar el documento en el consecutivo CARTA. Deberá cargarlo manualmente";
							String estadoActualError = context.getStateMachine().getState().getId().name().toString();
							SSMUtils.notificarErrorAdmin(raizLog, workId, numeroRadicadoObtenido, mensajeError, estadoActualError, globalProperties, autenticacionCS, wsdlsProps);
						}
						
						//SE COPIA EL DOCUMENTO EN EL CONSECUTIVO
						try {
							idDocConsecutivo = ContenidoDocumento.copiarDocCatDocumento(autenticacionCS.getAdminSoapHeader(), idDocRadicado, parentIdDestino, nuevoNombreDocConsecutivoCarta, TipoCopiadoDocumento.ORIGINAL.toString(), wsdlsProps.getDocumento());
							log.info(raizLog + "Se ha copiado exitosamente el documento el consecutivo con idDocConsecutivo ["+idDocConsecutivo+"]");
						} catch (SOAPException | IOException | InterruptedException e) {
							//SE ENVIA MENSAJE AL ADMINISTRADOR CON EL ERROR, NO SE DETIENE EL PROCESO
							log.error(raizLog + "Se presentó error al intentar copiar el documento con idDoc ["+idDocRadicado+"] en la carpeta de consecutivo CARTA con idParent ["+parentIdDestino+"]");
							String mensajeError = "Se presentó error al intentar copiar el documento con idDoc '"+idDocRadicado+"' en la carpeta de consecutivo CARTA con idParent '"+parentIdDestino+"' \n"
									+ "Debido a esta falla no se pudo cargar el documento en el consecutivo CARTA. Deberá cargarlo manualmente";
							String estadoActualError = context.getStateMachine().getState().getId().name().toString();
							SSMUtils.notificarErrorAdmin(raizLog, workId, numeroRadicadoObtenido, mensajeError, estadoActualError, globalProperties, autenticacionCS, wsdlsProps);
						}
					
					//SE CARGA EN EL CONTEXTO DE LA SSM EL ID DEL DOC CONSECUTIVO
					context.getExtendedState().getVariables().put(Consts.ID_DOC_CONSECUTIVO, idDocConsecutivo);
					
				}else {
					log.info(raizLog + "La maquina se encontro con la variable [" + Consts.ERROR_MAQUINA
							+ "] en valor [" + errorMaquina + "]");
				}

				imprimirLogMensajeFinEstado(raizLog, nombreEstadoActual);//Se marca el fin del estado en el log

				// SE ALMACENA CONTEXTO DE LA SSM EN BD
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}
			};

		}
		
		
		
		
		
		/**
		* Metodo para iniciar flujos de distribución de carta
		* 
		* @return
		*/
		@SuppressWarnings("unchecked")
		@Bean
		public Action<States, Events> distribuirCarta() {
			String nombreEstadoActual = States.DISTRIBUIR_CARTA.toString();
			//SE RETORNA EL CONTEXTO DE LA MÁQUINA DE ESTADOS
			return (context) -> {//Se retorna contexto de la máquina de estados (Contexto)
				//SE EXTRAE LA RAIZ LOG DEL CONTEXTO
				String raizLog = SSMUtils.getRaizLog(context);//Variable para inicio de impresión del log
				
				imprimirLogMensajeInicioEstado(raizLog, nombreEstadoActual);// Se marca el incio del estado en el log

				// SE CARGAN VARIABLES ALMACENADAS EN EL CONTEXTO DE LA SSM
				long idDocOriginal = context.getExtendedState().get(Consts.ID_DOC_RADICACION, Long.class);
				String numeroRadicado = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO, String.class);
				String tipoWfDoc = context.getExtendedState().get(Consts.TIPO_WF_DOCUMENTO, String.class);
				String workIdText = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);
				String asunto = context.getExtendedState().get(Consts.ASUNTO, String.class);
				String conexionUsuarioCS = context.getExtendedState().get(Consts.CONEXION_CS_SOLICITANTE, String.class);
				MetadatosPlantilla metadatosPlantilla = (MetadatosPlantilla) context.getExtendedState().get(Consts.METADATOS_PLANTILLA, Object.class);
				String fechaRadicacion = context.getExtendedState().get(Consts.FECHA_RADICACION, String.class);
				long idSolicitante = context.getExtendedState().get(Consts.ID_SOLICITANTE, Long.class);				
				String tipologia = context.getExtendedState().get(Consts.TIPOLOGIA_MEMO_CARTA, String.class);
				boolean esPersonalizado = context.getExtendedState().get(Consts.ES_PERSONALIZADO, Boolean.class);
				//String nombreDocOriginal = context.getExtendedState().get(Consts.NOMBRE_DOCUMENTO_ORIGINAL, String.class);

				Long idCopiaCC = 0L;
				
				Long idCopiaCC_D = 0L;
				
				//Se actualiza el parent id de la carpeta oroginal en caso de que el usuario haya movido el documento
				try {
					actualizarIdParentOriginal(context, autenticacionCS.getAdminSoapHeader(),
							wsdlsProps.getDocumento());
				} catch (SOAPException | IOException e) {
					log.warn("No se pudo actualizar el id de la carpeta" + e.getMessage());
				}
				
				Long parentIdCarpetaOriginal = context.getExtendedState().get(Consts.PARENT_ID_ORIGINAL_MEMO_CARTA, Long.class);

				List<Long> listIdDocsAdjuntos;


				//List<String> listNombresDocsPersonalizados = (List<String>) context.getExtendedState().get(Consts.LISTA_NOMBRES_DOCS_PERSONALIZADOS_CARTA, Object.class);
				List<Long> listIdDocsPersonalizados = (List<Long>) context.getExtendedState().get(Consts.LISTA_ID_DOCS_PERSONALIZADOS_CARTA, Object.class);

				List<Empleado> destinatarios = (List<Empleado>) context.getExtendedState().get(Consts.DESTINATARIOS, Object.class);
				List<Empleado> copias = (List<Empleado>) context.getExtendedState().get(Consts.COPIAS, Object.class);

				//MOSTRAR DATOS DESTINOS
				log.info(raizLog +"Se muestran los destinatarios de la comunicación:");
				destinatarios.forEach(destino -> log.info(raizLog +"Destinatario ["+destinatarios.indexOf(destino)+"] - nombre: [" + destino.getNombre()+"] - Dependencia/Entidad: ["+destino.getDependencia()+"]"));
				copias.forEach(copia -> log.info(raizLog +"Copia ["+copias.indexOf(copia)+"] - nombre: [" + copia.getNombre()+"] - Dependencia/Entidad: ["+copia.getDependencia()+"]"));


				if (listIdDocsPersonalizados == null) {

					listIdDocsPersonalizados = new ArrayList<>();
				}

				log.info(raizLog + "DESTINATARIOS EN DISTRIBUIR:");

				List<String> listaRoles = new ArrayList<>();

				List<String> listaNombresDestinatarios = new ArrayList<>();

				for (Empleado empleado : destinatarios) {
					listaRoles.add(empleado.getRol());
					listaNombresDestinatarios.add(empleado.getNombre());
				}

				for (Empleado empleado : copias) {
					listaRoles.add(empleado.getRol());
					listaNombresDestinatarios.add(empleado.getNombre());
				}

				int totalDestinatarios = listaRoles.size();
				int totalConfirmadosDistribuir = 0;
				int totalRechazadosDistribuir = 0;

				context.getExtendedState().getVariables().put(Consts.LISTA_ROLES_DESTINATARIOS, listaRoles);

				context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS,
						totalDestinatarios);
				context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS_CONFIRMADOS,
						totalConfirmadosDistribuir);
				context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS_RECHAZADOS,
						totalRechazadosDistribuir);

				log.info(raizLog + "INICIO PROCESO DISTRIBUIR");

				log.info(raizLog + "Se obtienen las copias compulsadas");

				List<Long> idDocsCopiasCompulsada = new ArrayList<>();

				String nombreCopiaCompulsadaOriginal = "(CC) " + numeroRadicado+".pdf";

				context.getExtendedState().getVariables().put(Consts.LISTA_ID_DOCS_COPIAS_COMPULSADAS,
						idDocsCopiasCompulsada);
				
				log.info(raizLog +"ID copia nombre documento original: " + nombreCopiaCompulsadaOriginal + " iDcarpeta: " + parentIdCarpetaOriginal);
				
				try {
					idCopiaCC = ContenidoDocumento.obtenerDocumentoPorNombre(
							autenticacionCS.getAdminSoapHeader(), parentIdCarpetaOriginal, nombreCopiaCompulsadaOriginal, wsdlsProps.getDocumento());
					
					log.info(raizLog +"ID copia complusada: " + idCopiaCC);
					
				} catch (SOAPException | IOException | InterruptedException | NullPointerException e) {
					log.error(raizLog + "ErrorCorrespondencia distribuirCarta obtenerDocumentoPorNombre" + e);	
					cargarError(context, e);
					//context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
					context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
					context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
				} 					
				
				 boolean totalUserCorreo = true;
				 boolean totalDest = true;
				 boolean totalCopias = true;
				 
					for (Empleado empleado : destinatarios) {

						if(!empleado.getRol().contains("@")) {
							totalDest = false;
							break;
						}
					}
					
					
					for (Empleado empleado : copias) {

						if(!empleado.getRol().contains("@") && !empleado.getRol().contains("00_PCR_COPIAS")) {
							totalCopias = false;
							break;
						}
					}
					
					
					totalUserCorreo = (totalDest && totalCopias);
					
					/*						
					log.info(raizLog + "INICIO actualizar estado correspondencia: DISTRIBUIR");

					try {
						actualizarEstadoCorrespondencia(raizLog, idDocOriginal,
								estadosProps.getDistribuido());
					} catch (SOAPException  e) {
						log.error(raizLog + "ErrorCorrespondencia Distribucioncarta actualizarEstadoCorrespondencia" + e);										
						cargarError(context, e);
						context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
						context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
					}

					log.info(raizLog + "FIN actualizar estado correspondencia: DISTRIBUIR_MEMORANDO");
					*/
					
					
					
					if(esPersonalizado){
						
						for(Empleado em : metadatosPlantilla.getDestinatarios()){
							//Vamaya: Se añaden logs para saber la data alamacenada en la lista de destinatarios
							log.info(raizLog + "Nombre destinatario: ["+em.getNombre()+"]");
							log.info(raizLog + "Cargo destinatario: ["+em.getCargo()+"]");
							log.info(raizLog + "Rol destinatario: ["+em.getRol()+"]");
							log.info(raizLog + "Dependencia destinatario: ["+em.getDependencia()+"]");
							log.info(raizLog + "Id doc asociado: ["+em.getIdDocumento()+"]");
							log.info(raizLog + "Nombre doc asociado: ["+em.getNombreDocumento()+"]");
							//Fin bloque
							if(em.getIdDocumento()!= 0 && em.getNombreDocumento() != null){
								
								log.info(raizLog + "INICIO actualizar estado correspondencia: DISTRIBUIR_MEMORANDO");

								try {
									actualizarEstadoCorrespondencia(raizLog, em.getIdDocumento(),
											estadosProps.getDistribuido());
								} catch (SOAPException  e) {
									log.error(raizLog + "ErrorCorrespondencia Distribucioncarta actualizarEstadoCorrespondencia" + e);										
									cargarError(context, e);
									context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
									context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
								}

								log.info(raizLog + "FIN actualizar estado correspondencia: DISTRIBUIR_MEMORANDO");
								
							}
							
						}
						
					}
				
				if (metadatosPlantilla.getTipoEnvio() != null && !metadatosPlantilla.getTipoEnvio().equalsIgnoreCase("Mensajeria")) {

					log.info(raizLog +"Entro a NO es mensajeria");

					//@SuppressWarnings("unused")
					//String pcrOrigen = context.getExtendedState().get(Consts.PCR_ORIGEN_CARTA, String.class);
					//@SuppressWarnings("unused")
					//String cddOrigen = context.getExtendedState().get(Consts.CDD_ORIGEN_CARTA, String.class);
//					String nombreDependenciaFirmante = cat.getFirmantes().get(0).getDependencia();

					long mapId = workflowProps.getDistribucionCarta().getId();
					if (tipoWfDoc.equalsIgnoreCase("Confidencial")) {
						mapId = workflowProps.getDistribucionCarta().getIdConfidencial();
						log.info(raizLog + "Es confidencial CARTA, WF distribucion con MapId: " + mapId);

					}

					String titulo = "Distribución Carta - " + numeroRadicado;

					if (tipoWfDoc.equalsIgnoreCase("Confidencial")) {
						titulo += " - Confidencial";
					}

					if (copias != null && !copias.isEmpty()) {
						// destinatarios.addAll(copias);
					}
					
					int posicion=0;
					for (Empleado destino : destinatarios) {

						listIdDocsAdjuntos = new ArrayList<>();
						titulo = "Distribución Carta - " + numeroRadicado;
						
						if (!destino.getRol().contains("@")) {

							//listIdDocsAdjuntos.add(idSticker);
							
							String[] listaDestinos = destino.getRol().split(",");
							String direccionFinal = "";
							for(int i = 1;i< listaDestinos.length;i++) {
								direccionFinal += listaDestinos[i];
							}
							
							titulo =  "Distribución Carta - " + numeroRadicado + " - " + direccionFinal;
						}

						Long idDocumentoAdjunto = new Long(0);
						idDocumentoAdjunto = idCopiaCC;
						if (Boolean.valueOf(metadatosPlantilla.getPersonalizarDocumento())) {

							if (destino.getNombreDocumento() != null) {

								nombreCopiaCompulsadaOriginal = "(CC) " + converUTF8(destino.getNombreDocumento());

								log.info(raizLog + "nombre copia CC [" + nombreCopiaCompulsadaOriginal
										+ "] - capeta [" + parentIdCarpetaOriginal + "]");

								try {
									idCopiaCC_D = ContenidoDocumento.obtenerDocumentoPorNombre(
											autenticacionCS.getAdminSoapHeader(), parentIdCarpetaOriginal,
											nombreCopiaCompulsadaOriginal, wsdlsProps.getDocumento());

									if (idCopiaCC_D == null) {

										log.info(raizLog + "Entro a ubicacion null");

										Long carpetaOriginal = context.getExtendedState()
												.get(Consts.PARENT_ID_ADJUNTOS_MEMO_CARTA_ORIGINAL, Long.class);

										log.info(raizLog + "Id original [" + carpetaOriginal + "]");

										idCopiaCC_D = ContenidoDocumento.obtenerDocumentoPorNombre(
												autenticacionCS.getAdminSoapHeader(), carpetaOriginal,
												nombreCopiaCompulsadaOriginal, wsdlsProps.getDocumento());

										if (idCopiaCC_D == null) {
											log.info(raizLog + "No se encontro en ubicación alternativa");
											throw new NullPointerException(
													"El servicio web getNodeByName no se encontró para los parametros:idParent ["
															+ parentIdCarpetaOriginal + "] - nombreDoc ["
															+ nombreCopiaCompulsadaOriginal + "]");
										}

									}

									idDocumentoAdjunto = idCopiaCC_D;

									if (destino.getRol().contains("@")) {
										listIdDocsAdjuntos.add(idDocumentoAdjunto);
									}

									log.info(raizLog + "ID copia complusada: " + idCopiaCC_D);

								} catch (SOAPException | IOException | InterruptedException e) {
									log.error(raizLog
											+ "ErrorCorrespondencia distribuirCarta obtenerDocumentoPorNombre" + e);
									cargarError(context, e);
									context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
									context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
								}

							} else if (destino.getRol().contains("@")) {

								// if (destino.getRol().contains("@")) {
								listIdDocsAdjuntos.add(idDocumentoAdjunto);
								// }
							}

						} else if (destino.getRol().contains("@")) {

							// if (destino.getRol().contains("@")) {
							listIdDocsAdjuntos.add(idDocumentoAdjunto);
							// }
						}

						
						Map<String, ValorAtributo> atributos = new HashMap<String, ValorAtributo>();
						String[] listaDestinos = destino.getRol().split(",");
						String rol = (destino.getRol().contains("@") ? listaDestinos[0] : destino.getRol());
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getNumeroRadicado(), new ValorAtributo(numeroRadicado, TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getTipologia(), new ValorAtributo("CA", TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getAsunto(), new ValorAtributo(SSMUtils.truncarString(asunto, Consts.TAMANO_MAXIMO_CS), TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getConexionCs(), new ValorAtributo(conexionUsuarioCS, TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getEsHilos(), new ValorAtributo("true", TipoAtributo.STRING));
//						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getNombreDependencia(),
//								new ValorAtributo(nombreDependenciaFirmante, TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getSolicitante(), new ValorAtributo(idSolicitante, TipoAtributo.USER));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getFechaRadicacion(), new ValorAtributo(fechaRadicacion, TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getWorkid(), new ValorAtributo(workIdText, TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getDestino(), new ValorAtributo(rol, TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getEsFisico(), new ValorAtributo(
								(destino.getRol().contains("@")) ? false : true, TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getRolDestino(), new ValorAtributo(idSolicitante, TipoAtributo.USER));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getEsCorreoCertificado(),
								new ValorAtributo(metadatosPlantilla.getEsCorreoCertificado(), TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getIdDocumentoAdjunto(),
								new ValorAtributo(idDocumentoAdjunto.toString(), TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getNombreDestino(),
								new ValorAtributo((destino.getNombre() != null && !destino.getNombre().isEmpty())
										? destino.getNombre()
										: "  ", TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getEntidadDestino(),
								new ValorAtributo(
										(destino.getDependencia() != null && !destino.getDependencia().isEmpty())
												? destino.getDependencia()
												: "  ",
										TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getEsMensajeria(), new ValorAtributo(false, TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getCorreoCopia(),
								new ValorAtributo((!metadatosPlantilla.getCorreoCopia().equalsIgnoreCase("CorreoCopia")
										? metadatosPlantilla.getCorreoCopia()
										: " "), TipoAtributo.STRING));

						atributos.put("_idDocumentoOriginal", new ValorAtributo(idDocOriginal, TipoAtributo.INTEGER));
						
						context.getExtendedState().getVariables().put(Consts.DESTINO_INDIVIDUAL_CARTA,
								((destino.getNombre() != null && !destino.getNombre().isEmpty())
										? destino.getNombre()
										: destino.getDependencia()));

						log.info(raizLog + "INICIO WF Distribucion carta Hilos:");
						
						int distribuido=0;
						
						//BLoque de prueba
						log.info(raizLog + "Atributos:");
						
						atributos.forEach((k,v)->{log.info("Llave: " + k + " - tipo: " + v.getTipo() +" - Valor: "+ v.getValor() + " - Tamano: " + v.getValor().toString().length());});
						
						log.info(raizLog + "FIN Atributos:");
						
						//Fin bloque de prueba
						
						truncarValoresAtributosWFCarta(atributos);
						
						try {
							
							distribuido = context.getExtendedState().get(Consts.DISTRIBUIDO, Integer.class) == null ? 0 : context.getExtendedState().get(Consts.DISTRIBUIDO, Integer.class);
							

							log.info(raizLog + "distribuido: " + distribuido + " posicion: " + posicion);
							
							log.info(raizLog + "Tamano listIdDocsAdjuntos [" + listIdDocsAdjuntos.size() + "]");
							log.info(raizLog + "Ids adjuntos");
							listIdDocsAdjuntos.forEach(id -> {
								log.info(raizLog + "Id [" + id + "]");
							});

							if(distribuido==posicion) {
								
								long idFlujo = Workflow.iniciarWorkflowAtributosAdjuntos(
										autenticacionCS.getAdminSoapHeader(), mapId, titulo, atributos,
										listIdDocsAdjuntos, wsdlsProps.getWorkflow());

								log.info(raizLog + "FIN WF Distribucion carta Hilos - Flujo Iniciado: " + idFlujo);
								
								int suma = posicion;
								context.getExtendedState().getVariables().put(Consts.DISTRIBUIDO, ++suma);
								
							}
						

						} catch (NumberFormatException | NullPointerException e) {
							cargarError(context, e);
							context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
							log.error(raizLog + "ErrorCorrespondencia distribuirCarta iniciarWorkflowAtributosAdjuntos " + e, e);	
						} catch (NoSuchElementException e) {
							log.error(raizLog + "ErrorCorrespondencia distribuirCarta iniciarWorkflowAtributosAdjuntos " + e, e);	
							cargarError(context, e);
							context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
							context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
						} catch (SOAPException e) {
							log.error(raizLog + "ErrorCorrespondencia distribuirCarta iniciarWorkflowAtributosAdjuntos " + e, e);	
							cargarError(context, e);
							context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
							context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
						} catch (IOException e) {
							log.error(raizLog + "ErrorCorrespondencia distribuirCarta iniciarWorkflowAtributosAdjuntos " + e, e);	
							cargarError(context, e);
							context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
							context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
						}
						
						posicion++;
						
						try {
							persistContexto(context);
						} catch (Exception e) {
							log.error("Error operacion", e);
						}
					}
					
					context.getStateMachine().sendEvent(Events.FINALIZAR);


				} else {

					if ((metadatosPlantilla.getEsImpresionArea() || tipoWfDoc.equalsIgnoreCase("Confidencial")) && !totalUserCorreo) {
						
						
						log.info(raizLog +"Entro a es confidencial o es impresion area");
						
						long idPcr = context.getExtendedState().get(Consts.ID_PCR_ORIGEN_CARTA, Long.class);
						long idCdd = context.getExtendedState().get(Consts.ID_CDD_ORIGEN_CARTA, Long.class);
						
						log.info(raizLog +"idPcr: " + idPcr + " idCdd: " + idCdd);
						
						listIdDocsAdjuntos = new ArrayList<>();

						long mapId = workflowProps.getDistribucionCarta().getId();
						if (tipoWfDoc.equalsIgnoreCase("Confidencial")) {
							mapId = workflowProps.getDistribucionCarta().getIdConfidencial();
							log.info(raizLog + "Es confidencial CARTA, WF distribucion ---" + workflowProps.getDistribucionCarta().getIdConfidencial() );

						}

						
						String titulo = "Distribución Carta - " + numeroRadicado;

						if (tipoWfDoc.equalsIgnoreCase("Confidencial")) {
							titulo += " - Confidencial";
							
						}

						//listIdDocsAdjuntos.add(idSticker);
						
						Long idDocumentoAdjunto = idCopiaCC;
//						String nombreDependenciaFirmante = cat.getFirmantes().get(0).getDependencia();
						List<Long> documentos = new ArrayList<Long>();
						documentos.add(idDocumentoAdjunto);
						
						esPersonalizado = Boolean.parseBoolean(metadatosPlantilla.getPersonalizarDocumento());
						
						if(esPersonalizado){
							
							for(Empleado em: destinatarios){
								
								if(em.getNombreDocumento() != null){
									
									nombreCopiaCompulsadaOriginal = "(CC) " + converUTF8(em.getNombreDocumento());
									
									try {
										idCopiaCC_D = ContenidoDocumento.obtenerDocumentoPorNombre(
												autenticacionCS.getAdminSoapHeader(), parentIdCarpetaOriginal, nombreCopiaCompulsadaOriginal, wsdlsProps.getDocumento());
										
										documentos.add(idCopiaCC_D);
																					
									} catch (SOAPException | IOException | InterruptedException e) {
										log.error(raizLog + "ErrorCorrespondencia distribuirCarta obtenerDocumentoPorNombre" + e);	
										cargarError(context, e);
										context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
										context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
									} 			
									
								}
								
							}
							
						}
						
						Map<String, ValorAtributo> atributos = new HashMap<String, ValorAtributo>();

						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getNumeroRadicado(), new ValorAtributo(numeroRadicado, TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getTipologia(), new ValorAtributo("CA", TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getAsunto(), new ValorAtributo(SSMUtils.truncarString(asunto, Consts.TAMANO_MAXIMO_CS), TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getConexionCs(), new ValorAtributo(conexionUsuarioCS, TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getEsHilos(), new ValorAtributo("false", TipoAtributo.STRING));
//						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getNombreDependencia(),
//								new ValorAtributo(nombreDependenciaFirmante, TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getSolicitante(), new ValorAtributo(idSolicitante, TipoAtributo.USER));
						
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getIdPCR(), new ValorAtributo(idPcr, TipoAtributo.USER));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getIdCDD(), new ValorAtributo(idCdd, TipoAtributo.USER));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getSolicitante(), new ValorAtributo(idSolicitante, TipoAtributo.USER));
						
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getFechaRadicacion(), new ValorAtributo(fechaRadicacion, TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getWorkid(), new ValorAtributo(workIdText, TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getEsMensajeria(), new ValorAtributo(true, TipoAtributo.STRING));
						atributos.put("_documentoOriginal", new ValorAtributo(documentos, TipoAtributo.ITEM_REFERENCE,true));							
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getIdDocumentoAdjunto(),
								new ValorAtributo(idDocumentoAdjunto.toString(), TipoAtributo.STRING));
						
						truncarValoresAtributosWFCarta(atributos);

						log.info(raizLog + "INICIO WF Distribucion carta:");
						
						try {
							long idFlujo = Workflow.iniciarWorkflowAtributosAdjuntos(
									autenticacionCS.getAdminSoapHeader(), mapId, titulo, atributos,
									listIdDocsAdjuntos, wsdlsProps.getWorkflow());

							log.info(raizLog + "FIN WF Distribucion carta - Flujo Inicado: " + idFlujo);


						} catch (NumberFormatException e) {
							cargarError(context, e);
							context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
							log.error(raizLog + "ErrorCorrespondencia distribuirCarta iniciarWorkflowAtributosAdjuntos " + e, e);	
						} catch (NoSuchElementException e) {
							cargarError(context, e);
							context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
							log.error(raizLog + "ErrorCorrespondencia distribuirCarta iniciarWorkflowAtributosAdjuntos " + e, e);	
						} catch (SOAPException e) {
							log.error(raizLog + "ErrorCorrespondencia distribuirCarta iniciarWorkflowAtributosAdjuntos " + e, e);	
							cargarError(context, e);
							context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
							context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
						} catch (IOException e) {
							log.error(raizLog + "ErrorCorrespondencia distribuirCarta iniciarWorkflowAtributosAdjuntos " + e, e);	
							cargarError(context, e);
							context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
							context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
						}
						
						

						context.getStateMachine().sendEvent(Events.IR_ESPERAR_RESPUESTA_DISTRIBUIR_CDD_CARTA);

					} else {

						
						for (Empleado empleado : destinatarios) {

							log.info(raizLog +"Destinatario:" + empleado.getNombre());

						}

						for (Empleado empleado : copias) {

							log.info(raizLog +"Copia:" + empleado.getNombre());

						}

						String pcrOrigen = context.getExtendedState().get(Consts.PCR_ORIGEN_CARTA, String.class);
						String cddOrigen = context.getExtendedState().get(Consts.CDD_ORIGEN_CARTA, String.class);
												
//						String nombreDependenciaFirmante = cat.getFirmantes().get(0).getDependencia();

						long mapId = workflowProps.getDistribucionCarta().getId();
						if (tipoWfDoc.equalsIgnoreCase("Confidencial")) {
							mapId = workflowProps.getDistribucionCarta().getIdConfidencial();
							log.info(raizLog + "Es confidencial CARTA, WF distribucion");

						}

						String titulo = "Distribución Carta - " + numeroRadicado;

						if (tipoWfDoc.equalsIgnoreCase("Confidencial")) {
							titulo += " - Confidencial";
						}

						Member grupo = null;
						Long idGrupo = null;

						log.info(raizLog + "El origen que se esta consultando es:" + cddOrigen);
						CentroDistribucion centroDistribucionOrigen = centroDistribucionRepository
								.findByNombre(cddOrigen);
						log.info(raizLog + "El destinatario que se esta consultando es: EXTERNO");
						CentroDistribucion centroDistribucionDestino = centroDistribucionRepository
								.findByNombre("EXTERNO");
						
						List<PasoDistribucion> pasosRuta = new ArrayList<PasoDistribucion>();
//						RutaDistribucion rutaDistribucion = rutaDistribucionRepository
//								.findByCentroDistribucionOrigenAndCentroDistribucionDestino(
//										centroDistribucionOrigen, centroDistribucionDestino);
						
						List<RutaDistribucion> rutasDB = rutaDistribucionRepository
								.findByCentroDistribucionOrigenAndCentroDistribucionDestino(centroDistribucionOrigen, centroDistribucionDestino);
									
						RutaDistribucion rutaDistribucion = null;
										
						if(rutasDB != null && !rutasDB.isEmpty()) {
							rutaDistribucion = SSMUtils.getRuta(rutasDB, tipologia , globalProperties);
						}
						
						if(rutaDistribucion != null && !rutaDistribucion.getPasos().isEmpty()){
							
						log.info(raizLog + "Si existe ruta en BD para origen:" + cddOrigen + " y destino: EXTERNO");

						pasosRuta = rutaDistribucion.getPasos();
						
						}
						
						String rutaCompletaTexto = pcrOrigen + ";" + cddOrigen + ";";
						String pasoSiguiente = "";

						for (PasoDistribucion pasoDistribucion : pasosRuta) {
							String nombrePaso = pasoDistribucion.getCentroDistribucion().getNombre();
							rutaCompletaTexto += nombrePaso + ";";

							if (nombrePaso.contains(workflowProps.getAtributo().getMensajeria())){
								pasoSiguiente = nombrePaso;
							}
						}

						log.info(raizLog + "La ruta completa Carta es:" + rutaCompletaTexto);
						
						if(pasoSiguiente.equals("")){
							pasoSiguiente = cddOrigen;
						}
						
						context.getExtendedState().getVariables().put(Consts.RUTA_COMPLETA, rutaCompletaTexto);

						try {
							grupo = UsuarioCS.getGrupoByNombre(autenticacionCS.getUserSoapHeader(conexionUsuarioCS),
									pasoSiguiente, wsdlsProps.getUsuario());

							idGrupo = grupo.getID();
						} catch (SOAPException e) {
							log.error(raizLog + "ErrorCorrespondencia distribuirCarta getGrupoByNombre " + e, e);	
							cargarError(context, e);
							context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
							context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
						} catch (IOException e) {
							log.error(raizLog + "ErrorCorrespondencia distribuirCarta getGrupoByNombre" + e, e);	
							cargarError(context, e);
							context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
							context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
						}

						if (copias != null && !copias.isEmpty()) {
							// destinatarios.addAll(copias);
						}

						int posicion=0;
						for (Empleado destino : destinatarios) {
							listIdDocsAdjuntos = new ArrayList<>();
							Long idDocumentoAdjunto = new Long(0);
							
							titulo = "Distribución Carta - " + numeroRadicado;
							idDocumentoAdjunto = idCopiaCC;
							
							if (Boolean.valueOf(metadatosPlantilla.getPersonalizarDocumento())) {
							
									if(destino.getNombreDocumento() != null){
									
										nombreCopiaCompulsadaOriginal = "(CC) " + converUTF8(destino.getNombreDocumento());
										
										log.info(raizLog +"nombre copia CC: " + nombreCopiaCompulsadaOriginal + " capeta: " + parentIdCarpetaOriginal);
										
										try {
											idCopiaCC_D = ContenidoDocumento.obtenerDocumentoPorNombre(
													autenticacionCS.getAdminSoapHeader(), parentIdCarpetaOriginal, nombreCopiaCompulsadaOriginal, wsdlsProps.getDocumento());
											
											idDocumentoAdjunto = idCopiaCC_D;
											
											if(destino.getRol().contains("@")){
												listIdDocsAdjuntos.add(idDocumentoAdjunto);
											}
											
											log.info(raizLog +"ID copia complusada: " + idCopiaCC_D);
											
										} catch (SOAPException | IOException | InterruptedException e) {
											log.error(raizLog + "ErrorCorrespondencia distribuirCarta obtenerDocumentoPorNombre" + e);	
											cargarError(context, e);
											context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
											context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
										} 
									
									} else {
										
										if(destino.getRol().contains("@")){
											listIdDocsAdjuntos.add(idDocumentoAdjunto);
										}
										
									}
																

							} else {
								
								if(destino.getRol().contains("@")){
									listIdDocsAdjuntos.add(idDocumentoAdjunto);
			
								}

							}
							
							if (!destino.getRol().contains("@")) {

								//listIdDocsAdjuntos.add(idSticker);
								
								String[] listaDestinos = destino.getRol().split(",");
								String direccionFinal = "";
								for(int i = 1;i< listaDestinos.length;i++) {
									direccionFinal += listaDestinos[i];
								}
								
								titulo =  "Distribución Carta - " + numeroRadicado + " - " + direccionFinal;

							}
							
							List<Long> documentos = new ArrayList<Long>();
							documentos.add(idDocumentoAdjunto);
						
							Map<String, ValorAtributo> atributos = new HashMap<String, ValorAtributo>();
							
							String[] listaDestinos = destino.getRol().split(",");
							String rol = (destino.getRol().contains("@") ? listaDestinos[0] : destino.getRol());
							
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getNumeroRadicado(),
									new ValorAtributo(numeroRadicado, TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getTipologia(), new ValorAtributo("CA", TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getAsunto(), new ValorAtributo(SSMUtils.truncarString(asunto, Consts.TAMANO_MAXIMO_CS), TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getConexionCs(), new ValorAtributo(conexionUsuarioCS, TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getEsHilos(), new ValorAtributo("true", TipoAtributo.STRING));
//							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getNombreDependencia(),
//									new ValorAtributo(nombreDependenciaFirmante, TipoAtributo.STRING));

							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getSolicitante(), new ValorAtributo(idSolicitante, TipoAtributo.USER));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getFechaRadicacion(),
									new ValorAtributo(fechaRadicacion, TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getWorkid(), new ValorAtributo(workIdText, TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getDestino(), new ValorAtributo(rol, TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getEsFisico(), new ValorAtributo(
									(destino.getRol().contains("@")) ? false : true, TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getRolDestino(), new ValorAtributo(idGrupo, TipoAtributo.USER));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getEsCorreoCertificado(),
									new ValorAtributo(metadatosPlantilla.getEsCorreoCertificado(), TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getIdDocumentoAdjunto(),
									new ValorAtributo(idDocumentoAdjunto.toString(), TipoAtributo.STRING));
							atributos.put("_documentoOriginal", new ValorAtributo(documentos, TipoAtributo.ITEM_REFERENCE,true));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getNombreDestino(),
									new ValorAtributo(
											(destino.getNombre() != null && !destino.getNombre().isEmpty())
													? destino.getNombre()
													: "  ",
											TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getEntidadDestino(),
									new ValorAtributo((destino.getDependencia() != null
											&& !destino.getDependencia().isEmpty()) ? destino.getDependencia()
													: "  ",
											TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getEsMensajeria(), new ValorAtributo(true, TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getCorreoCopia(),
									new ValorAtributo((!metadatosPlantilla.getCorreoCopia().equalsIgnoreCase("CorreoCopia")
											? metadatosPlantilla.getCorreoCopia()
											: " "), TipoAtributo.STRING));
							
							truncarValoresAtributosWFCarta(atributos);

							log.info(raizLog + "INICIO WF Distribucion carta Hilos:");
							int distribuido=0;
							try {
								
								distribuido = context.getExtendedState().get(Consts.DISTRIBUIDO, Integer.class) == null ? 0 : context.getExtendedState().get(Consts.DISTRIBUIDO, Integer.class);
								
								log.info(raizLog + "distribuido: " + distribuido + " posicion: " + posicion);
								
								if(distribuido==posicion) {
									
									long idFlujo = Workflow.iniciarWorkflowAtributosAdjuntos(
											autenticacionCS.getAdminSoapHeader(), mapId, titulo,
											atributos, listIdDocsAdjuntos, wsdlsProps.getWorkflow());
									
									int suma = posicion;
									context.getExtendedState().getVariables().put(Consts.DISTRIBUIDO, ++suma);
									log.info(raizLog + "FIN WF Distribucion carta Hilos - Flujo Inicado: " + idFlujo);
								
								}

							} catch (NumberFormatException e) {
								cargarError(context, e);
								context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
								log.error(raizLog + "ErrorCorrespondencia distribuirCarta iniciarWorkflowAtributosAdjuntos " + e, e);	
							} catch (NoSuchElementException e) {
								cargarError(context, e);
								log.error(raizLog + "ErrorCorrespondencia distribuirCarta iniciarWorkflowAtributosAdjuntos " + e, e);	
								cargarError(context, e);
								context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
								context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
							} catch (SOAPException | WebServiceException e) {
								log.error(raizLog + "ErrorCorrespondencia distribuirCarta iniciarWorkflowAtributosAdjuntos " + e, e);	
								cargarError(context, e);
								context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
								context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
							} catch (IOException e) {
								log.error(raizLog + "ErrorCorrespondencia distribuirCarta iniciarWorkflowAtributosAdjuntos " + e, e);	
								cargarError(context, e);
								context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
								context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
							}

							context.getExtendedState().getVariables().put(Consts.DESTINO_INDIVIDUAL_CARTA,
									((destino.getNombre() != null && !destino.getNombre().isEmpty())
											? destino.getNombre()
											: destino.getDependencia()));
							posicion++;
							try {
								persistContexto(context);
							} catch (Exception e) {
								log.info(raizLog + "ERROR: Ocurrio un error guardando la maquina de estados en la BD");
								log.error("Error operacion", e);
							}

						}

						context.getStateMachine().sendEvent(Events.IR_ESPERAR_RESPUESTA_DISTRIBUIR_CARTA);

					}
					
					

				}

			

				
				
				
				imprimirLogMensajeFinEstado(raizLog, nombreEstadoActual);//Se marca el fin del estado en el log
				
			};
		}

		
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		
		
		
		/**
		 * Metodo que permite cargar las propiedades iniciales
		 * @return
		 */
		/*
		@Bean
		public Action<States, Events> cargarProperties() {
			// SE RETORNA EL CONTEXTO DE LA MÁQUINA
			return (context) -> {
				// SE CARGA LA RAIZ LOG
				String raizLog = SSMUtils.getRaizLog(context);
				// SE MARCA EL INICIO DEL ESTADO
				log.info("\n"+raizLog + " ------------------------- INICIO cargarProperties ---------------------------------- \n");

				//SE CARGAN LAS PROPIEDADES DE OFFICE PARA LA PLANTILLA
				mapPropsOfficeDoc = appContext.getMapPropsOfficeDoc();
				
				//SE INICIALIZAN LAS PROPIEDADES DE LA MAQUINA
				int reintento = 1;
				boolean errorMaquina = false;
				Map<Long, RespuestaEntrega> distribucionRuta = new HashMap<>();	
				int distribuido = 0;
				int operacion = 0;
				int codigoErrorRadicar = 108;
				String mensajeError = "ErrorCorrespondencia no identificado en proceso de radicacion de memorando, consulte con el adminitrador del sistema ";
				//SE INICIALIZAN LAS VARIABLES EN EL CONTEXTO DE LA SSM
				context.getExtendedState().getVariables().put(Consts.REINTENTO, reintento);
				context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, errorMaquina);
				context.getExtendedState().getVariables().put(Consts.DISTRIBUCION_RUTA, distribucionRuta);
				context.getExtendedState().getVariables().put(Consts.DISTRIBUIDO, distribuido);
				context.getExtendedState().getVariables().put(Consts.OPERACION, operacion);
				context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, codigoErrorRadicar);
				context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR, mensajeError);
				
				log.info(raizLog + "Se cargaron los siguientes valores iniciales en las propiedades: "
				        + Consts.REINTENTO+": ["+reintento+"] - "
						+ Consts.ERROR_MAQUINA+": ["+errorMaquina+"] - "
						+ Consts.CODIGO_ERROR_RADICAR+": ["+codigoErrorRadicar+"] - "
						+ Consts.MENSAJE_ERROR+": ["+mensajeError+"] - "
						+ Consts.DISTRIBUCION_RUTA+": ["+distribucionRuta+"] - "
						+ Consts.DISTRIBUIDO+": ["+distribuido+"] - "
						+ Consts.OPERACION+": ["+operacion+"] ");
				
				log.info(raizLog + "Se cargaron correctamente las propiedades iniciales de la SSM");
				
				// SE MARCA EL FIN DEL ESTADO
				log.info("\n"+raizLog + " ------------------------- FIN cargarProperties ---------------------------------- \n");
			};

		}
		*/
		
		
		/**
		 * Metodo que permite obtener documento del content server
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> obtenerDocumentoCS() {
			// SE RETORNA EL CONTEXTO DE LA MÁQUINA
			return (context) -> {
				// SE CARGA LA RAIZ LOG
				String raizLog = SSMUtils.getRaizLog(context);
				// SE MARCA EL INICIO DEL ESTADO
				log.info("\n"+raizLog + " ------------------------- INICIO obtenerDocumentoCS ---------------------------------- \n");
				// SE CARGAN EN MEMORIA LOS MENSAJES DE ERROR ALMACENADOS EN LA BD
				log.info(raizLog + "INICIO Obtener mensajes ");
				mensajes = (List<Mensaje>) appContext.getMensajes();
				context.getExtendedState().getVariables().put(Consts.LISTA_MENSAJES, mensajes);
				log.info(raizLog + "FIN Obtener mensajes ");
				//SE INICIALIZAN VARIABLES PARA GUARDAR CONTENIDO DEL DOC
				DataHandler contenido = null;
				String nombreDocumentoOriginal;
				// SE CARGAN LAS VARIABLES DE LA SSM DESDE LOS HEADERS INICIALES DEL CONTROLLER
				long idDoc = context.getMessageHeaders().get(Consts.ID_DOC_RADICACION, Long.class);
				context.getExtendedState().getVariables().put(Consts.ID_DOC_RADICACION, idDoc);

				String conexionUsuario = context.getMessageHeaders().get(Consts.CONEXION_CS_SOLICITANTE, String.class);
				context.getExtendedState().getVariables().put(Consts.CONEXION_CS_SOLICITANTE, conexionUsuario);

				String workId = context.getMessageHeaders().get(Consts.ID_WORKFLOW_RADICACION, String.class);
				context.getExtendedState().getVariables().put(Consts.ID_WORKFLOW_RADICACION, workId);

				String tipoWfDoc = context.getMessageHeaders().get(Consts.TIPO_WF_DOCUMENTO, String.class);
				context.getExtendedState().getVariables().put(Consts.TIPO_WF_DOCUMENTO, tipoWfDoc);

				long idSolicitante = context.getMessageHeaders().get(Consts.ID_SOLICITANTE, Long.class);
				context.getExtendedState().getVariables().put(Consts.ID_SOLICITANTE, idSolicitante);

				Long parentIdOriginal = context.getMessageHeaders().get(Consts.PARENT_ID_ADJUNTOS_MEMO_CARTA,
						Long.class);
				context.getExtendedState().getVariables().put(Consts.PARENT_ID_ADJUNTOS_MEMO_CARTA, parentIdOriginal);
				context.getExtendedState().getVariables().put(Consts.PARENT_ID_ADJUNTOS_MEMO_CARTA_ORIGINAL, parentIdOriginal);

				Long parentIdAdjuntosWf = context.getMessageHeaders().get(Consts.PARENT_ID_ADJUNTOS_WF_MEMO_CARTA,
						Long.class);
				context.getExtendedState().getVariables().put(Consts.PARENT_ID_ADJUNTOS_WF_MEMO_CARTA,
						parentIdAdjuntosWf);

				String tipoDocumentalCatDocumento = context.getMessageHeaders()
						.get(Consts.TIPO_DOCUMENTAL_CAT_DOCUMENTO, String.class);
				String serieCatDocumento = context.getMessageHeaders().get(Consts.SERIE_CAT_DOCUMENTO, String.class);

				context.getExtendedState().getVariables().put(Consts.CADENA_LISTA_ID_DOCS_PERSONALIZADOS_CARTA, "");
				context.getExtendedState().getVariables().put(Consts.ES_PERSONALIZADO, false);


				if (tipoDocumentalCatDocumento != null && serieCatDocumento != null) {

					context.getExtendedState().getVariables().put(Consts.TIPO_DOCUMENTAL_CAT_DOCUMENTO,
							tipoDocumentalCatDocumento);
					context.getExtendedState().getVariables().put(Consts.SERIE_CAT_DOCUMENTO, serieCatDocumento);

				}

				log.info(raizLog + "Documento [" + idDoc + "] por obtener de Content Server...");

				//SE OBTIENE EL DOC DESDE CS
				try {
					contenido = ContenidoDocumento.obtenerContenidoDocumentoDTHLR(
							autenticacionCS.getUserSoapHeader(conexionUsuario), idDoc, globalProperties.getRutaTemp(), workId, wsdlsProps.getDocumento());
					context.getExtendedState().getVariables().put(Consts.CONTENIDO_DOC, contenido);
					/*
					// SE VALIDA EL CONTENIDO DESCARGADO (LANZA ERROR FUNCIONAL SI HAY ERRORES)
					ValidadorDocumento.validarContenidoDocumento(raizLog, contenido);
					*/
					log.info(raizLog + "Contenido del doc con id [" + idDoc + "] obtenido correctamente desde CS");					
					// SE ALMACENA EN EL CONTEXTO DE LA SSM ALGUNAS VARIABLES DEL DOC DESCARGADO
					nombreDocumentoOriginal = contenido.getName();
					log.info(raizLog + "Nombre del documento original: [" + nombreDocumentoOriginal+"]");
					context.getExtendedState().getVariables().put(Consts.NOMBRE_DOCUMENTO_ORIGINAL,
							nombreDocumentoOriginal);					
					//SE TRUNCA EL NOMBRE SI SUPERA EL MAXIMO PERMITIDO POR OFFICE
					nombreDocumentoOriginal = SSMUtils.truncarNombreDocumento(nombreDocumentoOriginal, Consts.TAMANO_MAXIMO_NOMBRE_DOCUMENTO_ORIGINAL);
					context.getExtendedState().getVariables().put(Consts.NOMBRE_DOCUMENTO_ORIGINAL_DELETE, nombreDocumentoOriginal);
					//SE ENVIA EVENTO PARA PASAR AL ESTADO "verificarWfConfidencial"
					context.getStateMachine().sendEvent(Events.DOCUMENTO_OBTENIDO_CS);
					log.info(raizLog + "Se envió el evento: ["+Events.DOCUMENTO_OBTENIDO_CS+"]  para la maquina con workId: [" + workId +"]");					
				} catch (SOAPException  | WebServiceException | IOException | InterruptedException e) {
					cargarError(context, e);
					context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
					log.info(raizLog + "Se envió el evento: ["+Events.IR_FINALIZADO_ERRORES+"]  para la maquina con workId: [" + workId +"]");
					context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, true);
					log.error("\n \n \n"+raizLog + "ErrorCorrespondencia obtenerDocumentoCS obtenerContenidoDocumentoDTHLR " + e , e);
				}  /*catch (ErrorFuncional e) {
					log.error(raizLog + "Se presentó error funcional validando datos de en la plantilla");						
					context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES_FUNCIONALES); // NO SE LE NOTIFICA AL ADMIN
					log.info(raizLog + "Se envió el evento: ["+Events.IR_FINALIZADO_ERRORES_FUNCIONALES+"]  para la maquina con workId: [" + workId +"]");
					context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, true);
					context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, 120);// SE PONE CUALQUIER NÚMERO (MENSAJES VARIOS)
					String mensajeError = e.getMessage();
					cargarError(context, new Exception(mensajeError));
					context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,
							mensajeError);
				}*/
				
				// SE MARCA EL FIN DEL ESTADO
				log.info("\n"+raizLog + " ------------------------- FIN obtenerDocumentoCS ---------------------------------- \n");
				
				//SE ALMACENA EL CONTEXTO DE LA SSM EN BD
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}
			};
		}
		
		
		
		/**
		 * Metodo que verifica si el documento enviado es confidencial
		 * @return
		 */
		@Bean
		public Action<States, Events> verificarWfConfidencial() {
			// SE RETORNA EL CONTEXTO DE LA MÁQUINA
			return (context) -> {
				// SE CARGA LA RAIZ LOG
				String raizLog = SSMUtils.getRaizLog(context);
				// SE MARCA EL INICIO DEL ESTADO
				log.info("\n"+raizLog
						+ " ----------------------- INICIO ESTADO verificarWfConfidencial --------------------------------- \n");
				// SE VALIDA QUE NO EXISTAN ERRORES CARGADOS EN LA MÁQUINA
				Boolean errorMaquina = context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class);
				if (!errorMaquina) {
					// SE CARGAN VARIABLES DEL CONTEXTO DE LA SSM
					String nombreDocOriginal = context.getExtendedState().get(Consts.NOMBRE_DOCUMENTO_ORIGINAL, String.class);
					String tipoWfIniciado = context.getExtendedState().get(Consts.TIPO_WF_DOCUMENTO, String.class);
					String workId = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);
					// SE INICIALIZAN VARIABLES PARA GUARDADO DE DATOS
					MetadatosPlantilla cat = null;
					String tipoDocPlantilla = "";
					// SE EXTRAEN LOS DATOS DEL DOCUMENTO
					try {
						//EXTRAYENDO DATOS
						log.info(raizLog + "Se van a extraer los datos del documento...");
						cat = DocumentoUtils.leerDocumento(globalProperties.getRutaTemp() + nombreDocOriginal,
								mapPropsOfficeDoc); // SE ALAMACENAN LOS DATOS EN LA CLASE "CATEGORIA"
						log.info(raizLog + "Datos extraidos correctamente");
						/*
						//SE INCIAN VALIDACIONES Y SE RETORNAN ERRORES
						log.info(raizLog + "Validando contenido del documento...");						
						ValidadorDocumento.validarDatosPlantilla(raizLog, cat, autenticacionCS, globalProperties, wsdlsProps); // Retorna errores funcionales
						log.info(raizLog + "Validaciones exitosas");
						*/
						// SE CARGAN DATOS DEL DOC EN EL CONTEXTO DE LA SSM
						tipoDocPlantilla = cat.getTipoDocumento();
						context.getExtendedState().getVariables().put(Consts.METADATOS_PLANTILLA, cat);
						context.getExtendedState().getVariables().put(Consts.TIPOLOGIA_MEMO_CARTA, cat.getTipologia());						
						String cddOrigen = null;
						String pcrOrigen = null;
						long idPcrOrigen = 0L;
						long idCddOrigen = 0L;
						
						if(!cat.getFirmantes().isEmpty() && cat.getFirmantes().size()>0 && cat.getTipologia().equalsIgnoreCase("CA")) {
						
							cddOrigen = cat.getFirmantes().get(0).getNombreCDD();
							pcrOrigen = cat.getFirmantes().get(0).getNombrePCR();
							idPcrOrigen = cat.getFirmantes().get(0).getIdPCR();
							idCddOrigen = cat.getFirmantes().get(0).getIdCDD();
							
							log.info(raizLog + "cddOrigen: [" + cddOrigen  + "] pcrOrigen: [" + pcrOrigen + "] idPcrOrigen: [" + idPcrOrigen + "] idCddOrigen: ["  +idCddOrigen + "]");
						
						}
						if (cddOrigen != null && pcrOrigen != null && idCddOrigen != 0L && idPcrOrigen != 0L) {
							
							context.getExtendedState().getVariables().put(Consts.CDD_ORIGEN_CARTA, cddOrigen);
							context.getExtendedState().getVariables().put(Consts.PCR_ORIGEN_CARTA, pcrOrigen);
							context.getExtendedState().getVariables().put(Consts.ID_PCR_ORIGEN_CARTA, idPcrOrigen);
							context.getExtendedState().getVariables().put(Consts.ID_CDD_ORIGEN_CARTA, idCddOrigen);
							
						}
						
						//SE VALIDA EL TIPO DE WF INICIADO VS EL TIPO DE WF DEL DOC Y SE RETORNA ERROR
						log.info(raizLog + "Tipo de seguridad especificada en el doc: [" + tipoDocPlantilla + "]  - tipo de WF iniciado: [" + tipoWfIniciado + "]");
						
						SSMUtils.validarTipoWfIniciado(raizLog, log, tipoWfIniciado, tipoDocPlantilla);
						
						
						//SE ENVIA EVENTO PARA PASAR EXITOSAMENTE AL SIGUIENTE ESTADO
						if(!context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class)) {							
							context.getStateMachine().sendEvent(Events.IR_VERIFICAR_VERSION_PLANTILLA); 
							log.info(raizLog + "Se envió el evento: ["+Events.IR_VERIFICAR_VERSION_PLANTILLA+"]  para la maquina con workId: [" + workId +"]");	
						}
							
						
													
					} catch (WebServiceException   e) {
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						log.info(raizLog + "Se envió el evento: ["+Events.IR_FINALIZADO_ERRORES+"]  para la maquina con workId: [" + workId +"]");
						context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, true);
						log.error("\n \n \n"+raizLog + "ErrorCorrespondencia verificarWfConfidencial leerDocumento " + e , e);
						
					} catch ( NotOfficeXmlFileException | NullPointerException | NoSuchElementException | IOException e) {
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						log.info(raizLog + "Se envió el evento: ["+Events.IR_FINALIZADO_ERRORES+"]  para la maquina con workId: [" + workId +"]");
						context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, true);
						context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, Consts.MSJ_111);
						String mensajeError = obtenerMensaje(Consts.MSJ_111.toString());
						context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,
								mensajeError);
						log.warn(raizLog + "Fallo debido a error: ["+mensajeError+"]");
						log.error("\n \n \n"+raizLog + "ErrorCorrespondencia verificarWfConfidencial leerDocumento " + e , e);

					} catch ( NumberFormatException e) {
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						log.info(raizLog + "Se envió el evento: ["+Events.IR_FINALIZADO_ERRORES+"]  para la maquina con workId: [" + workId +"]");
						context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, true);
						context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, Consts.MSJ_110);
						String mensajeError = obtenerMensaje(Consts.MSJ_110.toString());
						context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,
								mensajeError);
						log.warn(raizLog + "Fallo debido a error: ["+mensajeError+"]");
						log.error("\n \n \n"+raizLog + "ErrorCorrespondencia verificarWfConfidencial leerDocumento " + e , e);

					}catch (ErrorFuncional e) { // SI HAY ERRORES FUNCIONALES
						log.error(raizLog + "Se presentó error funcional validando datos de en la plantilla");						
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES_FUNCIONALES); // NO SE LE NOTIFICA AL ADMIN
						log.info(raizLog + "Se envió el evento: ["+Events.IR_FINALIZADO_ERRORES_FUNCIONALES+"]  para la maquina con workId: [" + workId +"]");
						context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, true);
						context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, 120);// SE PONE CUALQUIER NÚMERO (MENSAJES VARIOS)
						String mensajeError = e.getMessage();
						cargarError(context, new Exception(mensajeError));
						context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,
								mensajeError);
						
						log.error(raizLog + "error retornado al usuario: [" + mensajeError +"]" );
						
					}catch (Exception e) { //CUALQUIER OTRO ERROR SERÁ TECNOLOGICO
						log.error(raizLog + "Se presentó error validando datos de en la plantilla");						
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES); // SI SE LE NOTIFICA AL ADMIN
						log.info(raizLog + "Se envió el evento: ["+Events.IR_FINALIZADO_ERRORES+"]  para la maquina con workId: [" + workId +"]");
						context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, true);
						context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, 120);// SE PONE CUALQUIER NÚMERO (MENSAJES VARIOS)
						String mensajeError = "No se pudo validar datos de la plantilla "+e.getMessage();
						cargarError(context, new Exception(mensajeError));
						context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,
								mensajeError);
						
						log.error(raizLog + "error retornado al usuario: [" + mensajeError +"]" );
						
					}
					

				}else {
					log.info(raizLog + "La maquina se encontro con la variable [" + Consts.ERROR_MAQUINA
							+ "] en valor [" + errorMaquina + "]");
				}

				// SE MARCA EL FIN DEL ESTADO
				log.info("\n"+raizLog
						+ " ----------------------- FIN ESTADO verificarWfConfidencial --------------------------------- \n");

				//SE ALMACENA EL CONTEXTO DE LA SSM EN BD
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}

			};

		}
		
		
		
		/**
		 * Metodo que permite verificar la version de la plantilla
		 * 
		 * Verifica la version de la plantilla que se encuentra en las propiedades del
		 * documento comparandola con la version requerida
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> verificarVersionPlantillaDoc() {
			// SE RETORNA EL CONTEXTO DE LA MÁQUINA
			return (context) -> {
				// SE CARGA LA RAIZ LOG
				String raizLog = SSMUtils.getRaizLog(context);
				// SE MARCA EL INICIO DEL ESTADO
				log.info("\n" + raizLog
						+ " ----------------------- INICIO ESTADO verificarVersionPlantillaDoc --------------------------------- \n");
				// SE VALIDA QUE NO EXISTAN ERRORES CARGADOS EN LA MÁQUINA
				Boolean errorMaquina = context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class);
				if (!errorMaquina) {

					String nombreOriginal = "";
					try {

						MetadatosPlantilla cat = (MetadatosPlantilla) context.getExtendedState().get(Consts.METADATOS_PLANTILLA,
								Object.class);
						String versionPlantillaOficial = (cat.getTipologia().equalsIgnoreCase("ME")
								? globalProperties.getVersionPlantillaOficialMemorando()
								: globalProperties.getVersionPlantillaOficialCarta());
						// Vamaya: Se añade la extraccion de la version oficial de las nuevas plantillas
						String versionPlantillaOficial2 = (cat.getTipologia().equalsIgnoreCase("ME")
								? globalProperties.getVersionPlantillaOficialMemorando2()
								: globalProperties.getVersionPlantillaOficialCarta2());
						// Fin bloque

						nombreOriginal = context.getExtendedState().get(Consts.NOMBRE_DOCUMENTO_ORIGINAL, String.class);
						log.info(raizLog + "INICIO verificar version plantilla documento nombreOriginal: "
								+ nombreOriginal);
						if (DocumentoUtils.verificarVersion(globalProperties.getRutaTemp() + nombreOriginal,
								versionPlantillaOficial, versionPlantillaOficial2, mapPropsOfficeDoc)) {
							log.info(raizLog + "La version de la plantilla es CORRECTA");
						} else {
							log.info(raizLog + "La version de la plantilla es INCORRECTA");
							context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
							context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, true);
							context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, Consts.MSJ_100);
							context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,
									obtenerMensaje(Consts.MSJ_100.toString()));
							cargarError(context, new Exception(obtenerMensaje(Consts.MSJ_100.toString())));
							log.info(raizLog + "La Version de Plantilla es Incorrecta");
							return;
						}

						if (DocumentoUtils.validarNumeroRadicado(globalProperties.getRutaTemp() + nombreOriginal,
								mapPropsOfficeDoc)) {
							log.info(raizLog + "La version de la plantilla es CORRECTA NUmero Radicado");

						} else {

							log.info(raizLog
									+ "La version de la plantilla es INCORRECTA la plantailla tiene numero de radicado asignado");
							context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
							context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, true);
							context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, Consts.MSJ_100);
							context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,
									obtenerMensaje(Consts.MSJ_100.toString()));
							cargarError(context, new Exception(obtenerMensaje(Consts.MSJ_100.toString())));
							log.info(raizLog
									+ "La Version de Plantilla es Incorrecta la plantailla tiene numero de radicado asignado");
							return;

						}

						log.info(raizLog + "FIN verificar version plantilla documento nombreOriginal: "
								+ nombreOriginal);

					} catch (IOException | WebServiceException e) {
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, true);
						context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,
								"ErrorCorrespondencia: " + e.getMessage());
						log.error("Error operacion IOException", e);

					} catch (NullPointerException e) {
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, true);
						context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,
								obtenerMensaje(Consts.MSJ_100.toString()));
						log.error("Error operacion NullPointerException", e);
					}

					catch (Exception e) {
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, true);
						context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,
								"ErrorCorrespondencia: " + e.getMessage());
						log.error("Error operacion Exception", e);
					}

				} else {
					log.info(raizLog + "La maquina se encontro con la variable [" + Consts.ERROR_MAQUINA
							+ "] en valor [" + errorMaquina + "]");
				}

				// SE MARCA EL FIN DEL ESTADO
				log.info("\n" + raizLog
						+ " ----------------------- FIN ESTADO verificarVersionPlantillaDoc --------------------------------- \n");

				// SE ALMACENA EL CONTEXTO DE LA SSM EN BD
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out
							.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}
			};
		}
		
		
		
		
		
		
		/**
		 * Metodo de verificacion si el documento enviado contiene anexos
		 * @return
		 */
		@Bean
		public Action<States, Events> verificarAnexos() {
			// SE RETORNA EL CONTEXTO DE LA MÁQUINA
			return (context) -> {
				// SE CARGA LA RAIZ LOG
				String raizLog = SSMUtils.getRaizLog(context);
				// SE MARCA EL INICIO DEL ESTADO
				log.info("\n" + raizLog
						+ " ----------------------- INICIO ESTADO verificarAnexos --------------------------------- \n");
				// SE VALIDA QUE NO EXISTAN ERRORES CARGADOS EN LA MÁQUINA
				Boolean errorMaquina = context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class);
				if (!errorMaquina) {
					// SE CARGAN DATOS DESDE EL CONTEXTO DE LA SSM
					MetadatosPlantilla cat = (MetadatosPlantilla) context.getExtendedState().get(Consts.METADATOS_PLANTILLA,
							Object.class);

					String anexos = "";
					anexos = cat.getAnexosElectronicos();

					log.info(raizLog + "anexos: [" + anexos + "]");
					Boolean tieneAnexos = false;
					if (anexos.equalsIgnoreCase("True")) {

						log.info(raizLog + "es electronico, tiene anexos");
						tieneAnexos = true;

					}

					context.getExtendedState().getVariables().put(Consts.TIENE_ANEXOS, tieneAnexos);

				}else {
					log.info(raizLog + "La maquina se encontro con la variable [" + Consts.ERROR_MAQUINA
							+ "] en valor [" + errorMaquina + "]");
				}

				// SE MARCA EL FIN DEL ESTADO
				log.info("\n" + raizLog
						+ " ----------------------- FIN ESTADO verificarAnexos --------------------------------- \n");

				// SE ALMACENA EL CONTEXTO DE LA SSM EN BD
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}

			};

		}
		
		
		
		
		
		
		
		/**
		 * Metodo que permite obtener los datos de la MetadatosPlantilla leyendo el documento
		 * 
		 * @return
		 */
		/*
		@Bean
		public Action<States, Events> obtenerDatosCategoriaDoc() {
			// SE RETORNA EL CONTEXTO DE LA MÁQUINA
			return (context) -> {
				// SE CARGA LA RAIZ LOG
				String raizLog = SSMUtils.getRaizLog(context);
				// SE MARCA EL INICIO DEL ESTADO
				log.info("\n" + raizLog
						+ " ----------------------- INICIO ESTADO obtenerDatosCategoriaDoc --------------------------------- \n");
				// SE VALIDA QUE NO EXISTAN ERRORES CARGADOS EN LA MÁQUINA
				Boolean errorMaquina = context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class);
				if (!errorMaquina) {
					// SE CARGAN DATOS DESDE EL CONTEXTO DE LA SSM
					String nombreOriginal;
					MetadatosPlantilla cat = new MetadatosPlantilla();
					try {
						nombreOriginal = context.getExtendedState().get(Consts.NOMBRE_DOCUMENTO_ORIGINAL, String.class);
						log.info(raizLog + "INICIO obtener datos categoria leyendo documento nombreOriginal: "
								+ nombreOriginal);
						cat = DocumentoUtils.leerDocumento(globalProperties.getRutaTemp() + nombreOriginal,
								mapPropsOfficeDoc);
						
						if(cat.getCopias()!= null) {
							
							List<Empleado> copias = cat.getCopias();
							Iterator<Empleado> copiasIter = copias.iterator();
							
							while(copiasIter.hasNext()) {
								Empleado unaCopia = copiasIter.next();
								
								if(unaCopia.getRol().contains("00_PCR_COPIAS")) {
									copiasIter.remove();
								}
							}
							
						}
						
						List<Empleado> destinatarios = cat.getDestinatarios();
						List<Empleado> copias = cat.getCopias();
						
						if (destinatarios.isEmpty() || (destinatarios.get(0).getRol().equals("00_PCR_DESTINO")) ) {
							Integer codigo = Consts.MSJ_102;
							String mensajeErr = obtenerMensaje(codigo.toString());							
							
							context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, Boolean.TRUE);
							context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, codigo);
							context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,	mensajeErr);
							
							context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);							
							log.warn(raizLog + "ErrorCorrespondencia - codigo [" + codigo + "] - mensaje [" + mensajeErr + "]" );
							
							return;
						}
												
						destinatarios.forEach(e-> System.out.println("destinatarios rol:" +  e.getRol()));
						copias.forEach(e-> System.out.println("copias rol:" +  e.getRol()));
						
						//Vamaya: Se añade validación de asunto antes de generar número de radicado
						String asunto = cat.getAsunto();
						if(asunto.isEmpty() || asunto.equals("Incluir asunto")) {
							Integer codigo = Consts.MSJ_107;
							String mensajeErr = obtenerMensaje(codigo.toString());							
							
							context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, Boolean.TRUE);
							context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, codigo);
							context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,	mensajeErr);
							
							context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);							
							log.warn(raizLog + "ErrorCorrespondencia - codigo [" + codigo + "] - mensaje [" + mensajeErr + "]" );
							
							return;
						}
						//Fin bloque
						
						context.getExtendedState().getVariables().put(Consts.METADATOS_PLANTILLA, cat);
						context.getExtendedState().getVariables().put(Consts.ASUNTO, asunto);
						
						log.info(raizLog + "FIN obtener datos categoria leyendo documento nombreOriginal: "
								+ nombreOriginal);
					} catch (IOException | WebServiceException | NumberFormatException | NullPointerException e) {
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, true);
						context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,
								"Error obteniendo datos del documento");
						log.error(raizLog + "ErrorCorrespondencia obtenerDatosCategoriaDoc leerDocumento " + e , e);
					}

				}else {
					log.info(raizLog + "La maquina se encontro con la variable [" + Consts.ERROR_MAQUINA
							+ "] en valor [" + errorMaquina + "]");
				}

				// SE MARCA EL FIN DEL ESTADO
				log.info("\n" + raizLog
						+ " ----------------------- FIN ESTADO obtenerDatosCategoriaDoc --------------------------------- \n");

				// SE ALMACENA EL CONTEXTO DE LA SSM EN BD
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}
				
			};
		}
		*/
		
		
		
		
		/**
		 * Metodo que permite genrar el numero de radicado para Memorando
		 * 
		 * @return
		 */
		/*
		@SuppressWarnings("unused")
		@Bean
		public Action<States, Events> iniciarWfGenerarNumRadicado() {
			// SE RETORNA EL CONTEXTO DE LA MÁQUINA
			return (context) -> {
				// SE CARGA LA RAIZ LOG
				String raizLog = SSMUtils.getRaizLog(context);
				// SE MARCA EL INICIO DEL ESTADO
				log.info("\n" + raizLog
						+ " ----------------------- INICIO ESTADO iniciarWfGenerarNumRadicado --------------------------------- \n");
				// SE VALIDA QUE NO EXISTAN ERRORES CARGADOS EN LA MÁQUINA
				Boolean errorMaquina = context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class);
				if (!errorMaquina) {
					// SE CARGAN DATOS DESDE EL CONTEXTO DE LA SSM
					String conexionUsuario = context.getExtendedState().get(Consts.CONEXION_CS_SOLICITANTE, String.class);
					long idSolicitante = context.getExtendedState().get(Consts.ID_SOLICITANTE, Long.class);
					MetadatosPlantilla cat = context.getExtendedState().get(Consts.METADATOS_PLANTILLA, MetadatosPlantilla.class);
					String siglaOrigen = cat.getFirmantes().get(0).getSiglaremitente();
					String tipologia = cat.getTipologia();					
					

					String titulo = "FT104_Generar_NroRadicado";
					String atributoNumRadicado = "_radicadoGenerado";
					if (siglaOrigen == null || siglaOrigen.isEmpty()) {
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,
								obtenerMensaje(Consts.MSJ_101.toString()));
						cargarError(context, new Exception((Consts.MSJ_101.toString())));

						return;
					} else if (tipologia == null || tipologia.isEmpty()) {
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,
								obtenerMensaje(Consts.MSJ_101.toString()));
						cargarError(context, new Exception((Consts.MSJ_101.toString())));
						return;

					}

					Map<String, ValorAtributo> atributos = new HashMap<String, ValorAtributo>();
					atributos.put("_sigla", new ValorAtributo(siglaOrigen, TipoAtributo.STRING));
					atributos.put("_tipologia", new ValorAtributo(tipologia, TipoAtributo.STRING));
					atributos.put("_idSolicitante", new ValorAtributo(idSolicitante, TipoAtributo.USER));

					log.info(raizLog + "El documento tiene el parametro de Fondo Independiente en: [" + cat.getEsFondoIndependiente() + "]");

					long mapId = (!cat.getEsFondoIndependiente()) ? workflowProps.getGenerarNumeroRadicado()
							: workflowProps.getGenerarNumeroRadicadoFondoIndependiente();

					log.info(raizLog + "El flujo de generacion de num radicado es: [" + mapId + "]");

					try {
						if (!context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class)) {
							log.info(raizLog + "INICIO iniciar WF Generar NumRadicado");
							long idProcess = Workflow.iniciarWorkflowConAtributos(
									autenticacionCS.getAdminSoapHeader(), mapId, titulo, atributos,
									wsdlsProps.getWorkflow());

							log.info(raizLog + "FIN iniciar WF Genrar NumRadicado");

							log.info(raizLog + "INICIO obtener NumRadicado");

							String numeroRadicadoObtenido = Workflow
									.obtenerValoresAtributo(autenticacionCS.getAdminSoapHeader(),
											idProcess, atributoNumRadicado, wsdlsProps.getWorkflow())
									.get(0).toString();

							context.getExtendedState().getVariables().put(Consts.NUMERO_RADICADO_OBTENIDO,
									numeroRadicadoObtenido);

							log.info(raizLog + "El numero de radicado es: [" + numeroRadicadoObtenido + "]");
							log.info(raizLog + "FIN obtener NumRadicado");
						}
					} catch (NumberFormatException | NoSuchElementException |  SOAPException | IOException | WebServiceException e) {
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						log.error(raizLog + "ErrorCorrespondencia iniciarWfGenerarNumRadicado iniciarWorkflowConAtributos " + e , e);
					}

				}else {
					log.info(raizLog + "La maquina se encontro con la variable [" + Consts.ERROR_MAQUINA
							+ "] en valor [" + errorMaquina + "]");
				}

				// SE MARCA EL FIN DEL ESTADO
				log.info("\n" + raizLog
						+ " ----------------------- FIN ESTADO iniciarWfGenerarNumRadicado --------------------------------- \n");

				// SE ALMACENA EL CONTEXTO DE LA SSM EN BD
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}
			};

		}
		*/
		
		
		/**
		 * Metodo que permite actualizar los metadatos del documento en el Content
		 * Server
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> actualizarMetadatosDocumento() {

			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);

				try {
					if (!context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class)) {

						MetadatosPlantilla cat = context.getExtendedState().get(Consts.METADATOS_PLANTILLA, MetadatosPlantilla.class);
						String tipologia = cat.getTipologia();

						long idDoc = context.getExtendedState().get(Consts.ID_DOC_RADICACION, Long.class);

						String numeroRadicadoObtenido = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO,
								String.class);
						String tipoWfDoc = context.getExtendedState().get(Consts.TIPO_WF_DOCUMENTO, String.class);
						log.info(raizLog + "EL radicado Generado es: [" + numeroRadicadoObtenido + "]");

						log.info(raizLog + " actualizarMetadatosDocumento");

						log.info(raizLog + "Actualizando metadatos documento");
						Boolean esConfidencial = false;

						if (cat.getAsunto() == null || cat.getAsunto().equalsIgnoreCase("")
								|| cat.getAsunto().equalsIgnoreCase("Incluir asunto")) {
							
							Integer codigo = Consts.MSJ_107;
							String mensajeErr = obtenerMensaje(codigo.toString());							
							
							context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, Boolean.TRUE);
							context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, codigo);
							context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,	mensajeErr);
							
							context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);							
							log.warn(raizLog + "ErrorCorrespondencia - codigo [" + codigo + "] - mensaje [" + mensajeErr + "]" );
							
							return;

						}
						
						log.info(raizLog + "Actualizando metadatos documento asunto");
						
						if (tipoWfDoc.equalsIgnoreCase("Confidencial")) {
							log.info(raizLog + "Es confidencial, se modifica el asunto por: [" + numeroRadicadoObtenido + "]");
							cat.setAsunto(numeroRadicadoObtenido);
							esConfidencial = true;
							context.getExtendedState().getVariables().put(Consts.ASUNTO, numeroRadicadoObtenido);
						}

						log.info(raizLog + "INICIO actualizar Metadatos del DOC idDoc: " + idDoc);

						DateFormat dateFormat = new SimpleDateFormat(globalProperties.getFormatoFecha());
						DateFormat dateFormatHora = new SimpleDateFormat(globalProperties.getFormatoHora());
						Date date = new Date();
						String fecha = dateFormat.format(date);
						String hora = dateFormatHora.format(date);
						log.info(raizLog + fecha);
						log.info(raizLog + hora);

						context.getExtendedState().getVariables().put(Consts.FECHA_RADICACION, fecha);

						List<Empleado> firmantes = cat.getFirmantes();
						List<Empleado> destinatarios = cat.getDestinatarios();
						List<Empleado> copias = cat.getCopias();

						if (firmantes == null || firmantes.isEmpty()) {							
							Integer codigo = Consts.MSJ_101;
							String mensajeErr = obtenerMensaje(codigo.toString());							
							
							context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, Boolean.TRUE);
							context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, codigo);
							context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,	mensajeErr);
							
							context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);							
							log.warn(raizLog + "ErrorCorrespondencia - codigo [" + codigo + "] - mensaje [" + mensajeErr + "]" );
							
							
							return;
							
						} else if (destinatarios == null || destinatarios.isEmpty()) {							
							
							Integer codigo = Consts.MSJ_102;
							String mensajeErr = obtenerMensaje(codigo.toString());							
							
							context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, Boolean.TRUE);
							context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, codigo);
							context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,	mensajeErr);
							
							context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);							
							log.warn(raizLog + "ErrorCorrespondencia - codigo [" + codigo + "] - mensaje [" + mensajeErr + "]" );
							
							return;
						}

						context.getExtendedState().getVariables().put(Consts.DESTINATARIOS, destinatarios);
						context.getExtendedState().getVariables().put(Consts.COPIAS, copias);

						List<Map<String, Object>> listSubGrupoOrigen = new ArrayList<>();
						String nitFirmante = "";
						String listaNitFirmantes ="";
						String siglaRemitente = "";
						String nombreFirmante = "";
						String cargoFirmante = "";

						boolean isPrimerFirmante = true;
						
						for (Empleado empleadoFirmante : firmantes) {

							if (empleadoFirmante.getNombre()
									.equalsIgnoreCase("NOMBRE DEL REMITENTE (FIRMA AUTORIZADA).")
									|| empleadoFirmante.getCargo().equalsIgnoreCase("Cargo Remitente.")
									|| empleadoFirmante.getDependencia().equalsIgnoreCase("Dependencia Remitente")) {
								
								/*
								context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, Boolean.TRUE);
								context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, 101);
								context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,
										obtenerMensaje(Consts.MSJ_101.toString()));
								*/
								
								Integer codigo = Consts.MSJ_101;
								String mensajeErr = obtenerMensaje(codigo.toString());							
								
								context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, Boolean.TRUE);
								context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, codigo);
								context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,	mensajeErr);
								
								context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);							
								log.warn(raizLog + "ErrorCorrespondencia - codigo [" + codigo + "] - mensaje [" + mensajeErr + "] - firmante no modificado en la plantilla" );

							} else if (isPrimerFirmante) {

								Map<String, Object> metadatosSubGrupoOrigen = new HashMap<>();

								nitFirmante = empleadoFirmante.getRol();								
								siglaRemitente = empleadoFirmante.getSiglaremitente();
								nombreFirmante = empleadoFirmante.getNombre();
								cargoFirmante = empleadoFirmante.getCargo();
								
								listaNitFirmantes = formarCadenaFirmantes(nombreFirmante.trim(), siglaRemitente.trim(), cargoFirmante.trim()) +",";

								metadatosSubGrupoOrigen.put("Nombre", nombreFirmante.trim());
								metadatosSubGrupoOrigen.put("Cargo", cargoFirmante.trim());
								metadatosSubGrupoOrigen.put("Dependencia", empleadoFirmante.getDependencia().trim());
								listSubGrupoOrigen.add(metadatosSubGrupoOrigen);
								
								isPrimerFirmante = false;

								//break;

							}else {
								if(org.springframework.util.StringUtils.hasText(empleadoFirmante.getNombre())) {
									listaNitFirmantes =  listaNitFirmantes + formarCadenaFirmantes(empleadoFirmante.getNombre().trim(), empleadoFirmante.getSiglaremitente().trim(), empleadoFirmante.getCargo().trim());
								}
								
								break; //Se sale de las iteraciones en el segundo ciclo
							}

						}

						log.info(raizLog + "La lista de Firmantes es: "+listaNitFirmantes); 
						
						context.getExtendedState().getVariables().put(Consts.NIT_FIRMANTE, nitFirmante);						
						context.getExtendedState().getVariables().put(Consts.NOMBRE_FIRMANTE, nombreFirmante);						
						context.getExtendedState().getVariables().put(Consts.LISTA_NIT_FIRMANTES, listaNitFirmantes);

						log.info(raizLog + "La sigla del remitente es: [" + siglaRemitente + "]");

						List<Map<String, Object>> listSubGrupoDestino = new ArrayList<>();
						List<Map<String, Object>> listSubGrupoCopias = new ArrayList<>();

						if (tipologia.equals("ME")) {

							for (Empleado empleadoDestinatario : destinatarios) {

								Map<String, Object> metadatosSubGrupoDestino = new HashMap<>();

								if (empleadoDestinatario.getNombre().equalsIgnoreCase("Nombre Destinatario.")
										|| empleadoDestinatario.getCargo().equalsIgnoreCase("Cargo Destinatario.")
										|| empleadoDestinatario.getDependencia()
												.equalsIgnoreCase("Dependencia Destinatario.")) {
									/*
									context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, Boolean.TRUE);
									context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, 102);
									context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,
											obtenerMensaje(Consts.MSJ_102.toString()));

									log.info(raizLog
											+ "ErrorCorrespondencia Destinatario incorrecto, no se puede radicar el documento");
									*/
									
									Integer codigo = Consts.MSJ_102;
									String mensajeErr = obtenerMensaje(codigo.toString());							
									
									context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, Boolean.TRUE);
									context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, codigo);
									context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,	mensajeErr);
									
									context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);							
									log.warn(raizLog + "ErrorCorrespondencia - codigo [" + codigo + "] - mensaje [" + mensajeErr + "] - destinatario no modificado en la plantilla" );
									return;

								} else {

									metadatosSubGrupoDestino.put("Nombre", empleadoDestinatario.getNombre().trim());
									metadatosSubGrupoDestino.put("Cargo", empleadoDestinatario.getCargo().trim());
									metadatosSubGrupoDestino.put("Dependencia / Entidad",
											empleadoDestinatario.getDependencia().trim());
									listSubGrupoDestino.add(metadatosSubGrupoDestino);
								}

							}

							for (Empleado empleadoCopias : copias) {

								Map<String, Object> metadatosSubGrupoCopias = new HashMap<>();

								if (empleadoCopias.getNombre().equalsIgnoreCase("Nombre")
										|| empleadoCopias.getCargo().equalsIgnoreCase("Cargo")
										|| empleadoCopias.getDependencia().equalsIgnoreCase("Dependencia (Copia).")) {

									log.info(raizLog
											+ "ErrorCorrespondencia: Una de las copias no es valida, no se agregará esta copia a la categoria Correspondencia");
								} else {

									metadatosSubGrupoCopias.put("Nombre", empleadoCopias.getNombre().trim());
									metadatosSubGrupoCopias.put("Cargo", empleadoCopias.getCargo().trim());
									metadatosSubGrupoCopias.put("Dependencia / Entidad",
											empleadoCopias.getDependencia().trim());
									listSubGrupoCopias.add(metadatosSubGrupoCopias);
								}
							}

						} else if (tipologia.equalsIgnoreCase("CA")) {
							for (Empleado empleadoDestinatario : destinatarios) {

								Map<String, Object> metadatosSubGrupoDestino = new HashMap<>();

								if (empleadoDestinatario.getNombre() == null
										&& empleadoDestinatario.getDependencia() == null) {
									
									/*
									context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, Boolean.TRUE);
									context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, 102);
									context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,
											obtenerMensaje(Consts.MSJ_102.toString()));

									log.info(raizLog
											+ "ErrorCorrespondencia Destinatario incorrecto, no se puede radicar el documento");
									*/
									
									Integer codigo = Consts.MSJ_102;
									String mensajeErr = obtenerMensaje(codigo.toString());							
									
									context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, Boolean.TRUE);
									context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, codigo);
									context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,	mensajeErr);
									
									context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);							
									log.warn(raizLog + "ErrorCorrespondencia - codigo [" + codigo + "] - mensaje [" + mensajeErr + "]" );
									
									return;

								} else {

									metadatosSubGrupoDestino.put("Nombre",
											(empleadoDestinatario.getNombre() != null)
													? empleadoDestinatario.getNombre().trim()
													: "");
									metadatosSubGrupoDestino.put("Cargo",
											(empleadoDestinatario.getCargo() != null)
													? empleadoDestinatario.getCargo().trim()
													: "");
									metadatosSubGrupoDestino.put("Dependencia / Entidad",
											(empleadoDestinatario.getDependencia() != null)
													? empleadoDestinatario.getDependencia().trim()
													: "");
									metadatosSubGrupoDestino.put("Dirección",
											(empleadoDestinatario.getRol() != null
													&& !empleadoDestinatario.getRol().contains("FT_CORR"))
															? empleadoDestinatario.getRol().trim()
															: "");
									listSubGrupoDestino.add(metadatosSubGrupoDestino);
								}

							}

							for (Empleado empleadoCopias : copias) {

								Map<String, Object> metadatosSubGrupoCopias = new HashMap<>();

								if (empleadoCopias.getNombre() == null && empleadoCopias.getDependencia() == null) {

									log.info(raizLog
											+ "ErrorCorrespondencia: Una de las copias no es valida, no se agregará esta copia a la categoria Correspondencia");
								} else {

									metadatosSubGrupoCopias.put("Nombre",
											(empleadoCopias.getNombre() != null) ? empleadoCopias.getNombre().trim()
													: "");
									metadatosSubGrupoCopias.put("Cargo",
											(empleadoCopias.getCargo() != null) ? empleadoCopias.getCargo().trim()
													: "");
									metadatosSubGrupoCopias.put("Dependencia / Entidad",
											(empleadoCopias.getDependencia() != null)
													? empleadoCopias.getDependencia().trim()
													: "");
									metadatosSubGrupoCopias.put("Dirección",
											(empleadoCopias.getRol() != null
													&& !empleadoCopias.getRol().contains("FT_CORR") && !empleadoCopias.getRol().contains("PCR_COPIAS"))
															? empleadoCopias.getRol().trim()
															: "");
									listSubGrupoCopias.add(metadatosSubGrupoCopias);
								}
							}

						}

						Map<String, List<Map<String, Object>>> mapSubgrupos = new HashMap<>();

						Map<String, Object> metadatos = new HashMap<>();

						metadatos.put(categoriaProps.getCorrespondencia().getAtributoTipologia(),
								((tipologia.equalsIgnoreCase("ME")) ? globalProperties.getTipologiaMemorando()
										: globalProperties.getTipologiaCarta()));
						metadatos.put(categoriaProps.getCorrespondencia().getAtributoEstado(),
								estadosProps.getRadicado());
						metadatos.put(categoriaProps.getCorrespondencia().getAtributoAsunto(), cat.getAsunto());
						metadatos.put(categoriaProps.getCorrespondencia().getAtributoFechaRadicacion(), date);
						metadatos.put(categoriaProps.getCorrespondencia().getAtributoHoraRadicacion(), hora);
						metadatos.put(categoriaProps.getCorrespondencia().getAtributoNumeroRadicacion(),
								numeroRadicadoObtenido);
						metadatos.put("Confidencial", esConfidencial);
						
						trucarValoresCategoriaCorrespondencia(listSubGrupoOrigen);
						trucarValoresCategoriaCorrespondencia(listSubGrupoDestino);
						trucarValoresCategoriaCorrespondencia(listSubGrupoCopias);

						mapSubgrupos.put(categoriaProps.getCorrespondencia().getSubgrupoOrigen(), listSubGrupoOrigen);
						mapSubgrupos.put(categoriaProps.getCorrespondencia().getSubgrupoDestino(), listSubGrupoDestino);
						mapSubgrupos.put(categoriaProps.getCorrespondencia().getSubgrupoCopia(), listSubGrupoCopias);

						try {

							Boolean existeError = context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class);

							log.info(raizLog + "INICIO CREAR CATEGORIA");
							log.info(raizLog + "INICIO crear/actualizar categoria del DOC idDoc: " + idDoc);
							
							log.info(raizLog + "Id categoria correspondencia [" + categoriaProps.getCorrespondencia().getId() + "]");;

							if (!existeError) {
								for (Map<String, Object> mapOrigen : listSubGrupoOrigen) {

									ContenidoDocumento.adicionarCategoriaMetadata(
											autenticacionCS.getAdminSoapHeader(), idDoc,
											categoriaProps.getCorrespondencia().getId(),
											categoriaProps.getCorrespondencia().getNombre(), metadatos,
											categoriaProps.getCorrespondencia().getSubgrupoOrigen(), mapOrigen, Boolean.TRUE,
											wsdlsProps.getDocumento());

								}
							}

							if (!existeError) {
								for (Map<String, Object> mapDestino : listSubGrupoDestino) {
									ContenidoDocumento.adicionarCategoriaMetadata(
											autenticacionCS.getAdminSoapHeader(), idDoc,
											categoriaProps.getCorrespondencia().getId(),
											categoriaProps.getCorrespondencia().getNombre(), metadatos,
											categoriaProps.getCorrespondencia().getSubgrupoDestino(), mapDestino, false,
											wsdlsProps.getDocumento());

								}
							}

							if (listSubGrupoCopias != null && !listSubGrupoCopias.isEmpty()
									&& listSubGrupoCopias.size() > 0 && !existeError) {

								for (Map<String, Object> mapCopias : listSubGrupoCopias) {

									ContenidoDocumento.adicionarCategoriaMetadata(
											autenticacionCS.getAdminSoapHeader(), idDoc,
											categoriaProps.getCorrespondencia().getId(),
											categoriaProps.getCorrespondencia().getNombre(), metadatos,
											categoriaProps.getCorrespondencia().getSubgrupoCopia(), mapCopias, false,
											wsdlsProps.getDocumento());
								}

							}

							log.info(raizLog + "FIN crear/actualizar categoria del DOC idDoc: " + idDoc);
							log.info(raizLog + "FIN CREAR CATEGORIA");

						} catch (DateTimeException e) {
							cargarError(context, e);
							context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
							context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, 200);
							log.error(raizLog + "ErrorCorrespondencia actualizarMetadatosDocumento adicionarCategoriaMetadata " + e, e);
							
						} catch (IOException | SOAPException | WebServiceException  e) {
							context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, 200);
							log.error(raizLog + "ErrorCorrespondencia actualizarMetadatosDocumento adicionarCategoriaMetadata " + e, e);
							cargarError(context, e);
							context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
						}

					}
					
				} catch (Exception e) {
					cargarError(context, e);
					context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
					context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, 200);
					log.error(raizLog + "ErrorCorrespondencia actualizarMetadatosDocumento adicionarCategoriaMetadata " + e , e);

				}
				
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out
							.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}
			};

		}
		
		
		
		
		/**
		 * Metodo que permite escribir el numeroRadicado y la fecha en el documento
		 * 
		 * @return
		 */
		/*
		@Bean
		public Action<States, Events> actualizarRadicadoDoc() {

			return (context) -> {

				if (!context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class)) {

					String raizLog = SSMUtils.getRaizLog(context);
					log.info(raizLog + " actualizarRadicadoDoc");

					log.info(raizLog + "INICIO escribir radicado en el DOC");

					String nombreOriginal = context.getExtendedState().get(Consts.NOMBRE_DOCUMENTO_ORIGINAL,
							String.class);
					String numeroRadicado = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO,
							String.class);

					MetadatosPlantilla cat = context.getExtendedState().get(Consts.METADATOS_PLANTILLA, MetadatosPlantilla.class);
					
					//JLG: Se modifica el mensaje de idioma inglés para que fuera interpretable
					//String esIdiomaIngles = (cat.getIdiomaIngles() != null) ? cat.getIdiomaIngles() : "";
					String esIdiomaIngles = (cat.getIdiomaIngles() != null) ? cat.getIdiomaIngles() : "No especificado";
					//log.info(raizLog + "El idioma es ingles" + esIdiomaIngles);
					log.info(raizLog + "¿El idioma es ingles?: "+"[" + esIdiomaIngles+"]");
					//---------

					Map<String, Integer> cordenadas = new HashMap<>();

					DateFormat dateFormat = (!esIdiomaIngles.equalsIgnoreCase("True"))
							? new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("es","ES"))
							: new SimpleDateFormat("MMMM, d yyyy", Locale.ENGLISH);

					String fecha = dateFormat.format(new Date());

					cordenadas.put("header", 1);
					cordenadas.put("table", 0);
					cordenadas.put("row", 1);
					cordenadas.put("cell", 2);
					cordenadas.put("paragraph", 0);
					cordenadas.put("text", 0);

					try {
						log.info(raizLog + "INICIO escribir radicado en el DOC - numeroRadicado: "
								+ numeroRadicado);
						DocumentoUtils.escribirDocumento(globalProperties.getRutaTemp() + nombreOriginal,
								numeroRadicado, fecha, cordenadas, mapPropsOfficeDoc);
						//JLG: se comentó log repetido
						//log.info(raizLog + "FIN escribir radicado en el DOC - numeroRadicado: " + numeroRadicado);
						log.info(raizLog + "FIN escribir radicado en el DOC - numeroRadicado: " + numeroRadicado);
						
					} catch (IOException | WebServiceException e) {
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, 200);
						log.error(raizLog + "ErrorCorrespondencia actualizarRadicadoDoc escribirDocumento " + e, e);
					} catch (NullPointerException e) {
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, 2);
						context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,
								obtenerMensaje(Consts.MSJ_103.toString()));
						log.error(raizLog + "ErrorCorrespondencia actualizarRadicadoDoc escribirDocumento " + e, e);
					}

				}
				
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}
			};
		}
		
		*/
		
		
		
		
		
		/**
		 * Metodo que permite renombrar el documento en el Content Server para posterior
		 * renderizacion
		 * 
		 * @return
		 */
		/*
		@Bean
		public Action<States, Events> renombrarDocumentoCS() {

			return (context) -> {

				if (!context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class)) {

					String raizLog = SSMUtils.getRaizLog(context);
					log.info(raizLog + " renombrarDocumentoCS");

					log.info(raizLog + "INICIO cambiar nombre documento");

					String numeroRadicado = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO,
							String.class);
					MetadatosPlantilla cat = context.getExtendedState().get(Consts.METADATOS_PLANTILLA, MetadatosPlantilla.class);
					String conexionUsuario = context.getExtendedState().get(Consts.CONEXION_CS_SOLICITANTE, String.class);
					String tipoWfDoc = context.getExtendedState().get(Consts.TIPO_WF_DOCUMENTO, String.class);

					long idDoc = context.getExtendedState().get(Consts.ID_DOC_RADICACION, Long.class);

					String estadoOrigen = context.getSource().getId().toString();

					log.info(raizLog + "El estado origen es:" + estadoOrigen);

					String nuevoNombreDoc = "";

					if (estadoOrigen.equals("CARGAR_NUEVA_VERSION_DOC_CS") && cat.getTipologia().equalsIgnoreCase("CA")
							&& Boolean.valueOf(cat.getPersonalizarDocumento()))
						nuevoNombreDoc = "";

					if (!tipoWfDoc.equalsIgnoreCase("Confidencial")) {
						nuevoNombreDoc += numeroRadicado;
					} else {
						nuevoNombreDoc += numeroRadicado + " Confidencial";

					}
					nuevoNombreDoc = (nuevoNombreDoc.length() > globalProperties.getLongitudNuevoNombreArchivo())
							? nuevoNombreDoc.substring(0, globalProperties.getLongitudNuevoNombreArchivo())
							: nuevoNombreDoc;

					try {
						ContenidoDocumento.cambiarNombreDocumento(autenticacionCS.getUserSoapHeader(conexionUsuario),
								idDoc, nuevoNombreDoc + ".pdf", wsdlsProps.getDocumento());

						log.info(raizLog + "FIN cambiar nombre documento");
						log.info(raizLog + " FIN cambiar nombre documento idDoc " + idDoc);
					} catch (ServerSOAPFaultException e) {
						log.error(raizLog + "ErrorCorrespondencia renombrarDocumentoCS cambiarNombreDocumento " + e, e);
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, 200);
					} catch (IOException e) {
						log.error(raizLog + "ErrorCorrespondencia renombrarDocumentoCS cambiarNombreDocumento " + e, e);
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, 200);
					} catch (SOAPException e) {
						log.error(raizLog + "ErrorCorrespondencia renombrarDocumentoCS cambiarNombreDocumento " + e, e);
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, 200);
					} catch (InterruptedException e) {
						log.error(raizLog + "ErrorCorrespondencia renombrarDocumentoCS cambiarNombreDocumento " + e, e);
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, 200);
					}

					// Aqui se realiza la persistencia de la maquina de estados en BD

					try {
						persistContexto(context);
					} catch (Exception e) {
						log.info(raizLog + "ERROR: Ocurrio un error guardando la maquina de estados en la BD");
						log.error("Error operacion", e);
					}

					context.getExtendedState().getVariables().put(Consts.NOMBRE_DOCUMENTO_ORIGINAL,
							nuevoNombreDoc + ".pdf");

					if (!estadoOrigen.equals("CARGAR_NUEVA_VERSION_DOC_CS")) {
						context.getStateMachine().sendEvent(Events.IR_SUBIR_DOC_CONSECUTIVO);

					} else {
						context.getStateMachine().sendEvent(Events.IR_ESPERAR_FIRMA);
					}

					log.info(raizLog + " Persistir Maquina Estados");

				}
				
				try {
					persistContexto(context);
				} catch (Exception e) {
					log.error("ERROR: Ocurrio un error guardando la maquina de estados en la BD", e);
					
				}

			};
		}
		*/
		
		
		/**
		 * Metodo intermedio en la maquina de estados que permite esperar el resultado
		 * de la firma del Memorando
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> esperarFirma() {

			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + "----------------------ESTADO esperar firma ------------------------");

				if (!context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class)) {

					@SuppressWarnings("unchecked")
					List<Empleado> destinatarios = (List<Empleado>) context.getExtendedState().get(Consts.DESTINATARIOS,
							Object.class);

					log.info(raizLog + "DESTINATARIOS: ");

					for (Empleado empleado : destinatarios) {
						String nombre = empleado.getNombre();
						String rol = empleado.getRol();
						log.info(raizLog + "Nombre empleado:" + nombre);
						log.info(raizLog + "ROL:" + rol);
					
					}
					
					//Eliminar Archivo
					String nombreDoc =  context.getExtendedState().get(Consts.NOMBRE_DOCUMENTO_ORIGINAL_DELETE, String.class);
					nombreDoc = globalProperties.getRutaTemp() +  nombreDoc;
					log.info(raizLog + "archivo a eliminar: " +  nombreDoc);
					File doc = new File(nombreDoc);
					log.info(raizLog +"Archivo eliminado:" + doc.delete() );
					
					log.info(raizLog + " Persistir Maquina Estados");

					try {
						persistContexto(context);
					} catch (Exception e) {
						log.info(raizLog + "ERROR: Ocurrio un error guardando la maquina de estados en la BD");
						log.error("Error operacion", e);
					}
				}
			};
		}
		
		
		
		
		
		
		
		
		/**
		 * Metodo que permite subir documento de carta a la carpeta consecutivo
		 * @return
		 */
		@Bean
		public Action<States, Events> subirDocConsecutivo2() {

			return (context) -> {

				if (!context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class)) {

					String raizLog = SSMUtils.getRaizLog(context);
					log.info(raizLog + " subirDocConsecutivo");

					log.info(raizLog + "INICIO subirDocConsecutivo");
					Long idDocOriginal = context.getExtendedState().get(Consts.ID_DOC_RADICACION, Long.class);
					String nombreDocumentoOriginal = context.getExtendedState().get(Consts.NOMBRE_DOCUMENTO_ORIGINAL,
							String.class);
					
					String workId = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);
					
					MetadatosPlantilla cat = context.getExtendedState().get(Consts.METADATOS_PLANTILLA, MetadatosPlantilla.class);
					String siglaOrigen = cat.getFirmantes().get(0).getSiglaremitente();
					
					if(!cat.getEsFondoIndependiente()) {
						siglaOrigen = "BR";
					}
					
					DataHandler dataHandlerDoc = null;


					try {
						dataHandlerDoc = ContenidoDocumento.obtenerContenidoDocumentoDTHLR(
								autenticacionCS.getAdminSoapHeader(), idDocOriginal, globalProperties.getRutaTemp(), workId,
								wsdlsProps.getDocumento());
					} catch (SOAPException | IOException | WebServiceException | InterruptedException e) {
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						log.error(raizLog + "ErrorCorrespondencia subirDocConsecutivo obtenerContenidoDocumentoDTHLR " + e, e);
					}

					LocalDate date = LocalDate.now();
					Long parentIdDestino = readXML.getIdFolderXML(date.getYear(), date.getMonthValue(),  siglaOrigen, "ConsecutivoCarta", globalProperties.getRutaXML());
										
					Map<String, Object> metadatosCatDoc = new HashMap<String, Object>();
					
					metadatosCatDoc.put("Tipo documental", readXML.getTypeDcocumentXML(date.getYear(), date.getMonthValue(), siglaOrigen, "ConsecutivoCarta", globalProperties.getRutaXML()));
					metadatosCatDoc.put("Serie", readXML.getSerieDocumentXML(date.getYear(), date.getMonthValue(), siglaOrigen, "ConsecutivoCarta", globalProperties.getRutaXML()));
					
					Long idDocConsecutivo = null;

					try {
						idDocConsecutivo = ContenidoDocumento.crearDocumentoCatDocumento(
								(autenticacionCS.getAdminSoapHeader()), null,
								nombreDocumentoOriginal, dataHandlerDoc, categoriaDocProps.getId(), categoriaDocProps.getNombreCategoria(),
								metadatosCatDoc, false, parentIdDestino, wsdlsProps.getDocumento());
						
						String nombreDoc = globalProperties.getRutaTemp() +  dataHandlerDoc.getName();
						log.info(raizLog + "archivo a eliminar: " +  nombreDoc);
						File doc = new File(nombreDoc);
						log.info(raizLog +"Archivo eliminado:" + doc.delete() );
						
					} catch (SOAPException | IOException | WebServiceException | InterruptedException e) {
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						log.error(raizLog + "ErrorCorrespondencia subirDocConsecutivo crearDocumentoCatDocumento " + e, e);
					}

					log.info(raizLog + "INICIO agregar cat correspondencia a doc personalizado");
					
					Node documentoAdjunto;
					Node documentoOriginal;
					try {
						documentoAdjunto = ContenidoDocumento.getDocumentoById(
								autenticacionCS.getAdminSoapHeader(), idDocConsecutivo,
								wsdlsProps.getDocumento());
						
						documentoOriginal = ContenidoDocumento.getDocumentoById(
								autenticacionCS.getAdminSoapHeader(), idDocOriginal,
								wsdlsProps.getDocumento());
						
						Metadata metadataAdjunto = documentoAdjunto.getMetadata();
						Metadata metadataOriginal = documentoOriginal.getMetadata();

						AttributeGroup categoriaCorrespondencia = null;
					
						categoriaCorrespondencia = metadataOriginal.getAttributeGroups().stream()
								.filter(unaCategoria -> unaCategoria.getDisplayName()
										.equals(categoriaProps.getCorrespondencia().getNombre()))
								.findFirst().get();

						metadataAdjunto.getAttributeGroups().add(categoriaCorrespondencia);

						documentoAdjunto.setMetadata(metadataAdjunto);

						ContenidoDocumento.actualizarMetadataDocumento(
								autenticacionCS.getAdminSoapHeader(), idDocConsecutivo, metadataAdjunto,
								wsdlsProps.getDocumento());
					} catch (ServerSOAPFaultException | IOException | SOAPException | InterruptedException  e) {
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						log.error(raizLog + "ErrorCorrespondencia subirDocConsecutivo getDocumentoById " + e, e);
					
					}

					log.info(raizLog + "FIN agregar cat correspondencia a doc personalizado");

					log.info(raizLog + "FIN subirDocConsecutivo");
				}
				
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}
			};

		}
		
		
		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////                 FIN ESTADOS ----- CARTA/MEMO -----                  ///////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		
		
		
		//----------------------------------------------------------------------------------------------------------------------
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		//----------------------------------------------------------------------------------------------------------------------
		
		
		
		
		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////                 INICIO ESTADOS ----- DER -----                      ///////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				

		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////                   FIN ESTADOS ----- DER -----                       ///////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		
		
		
		//----------------------------------------------------------------------------------------------------------------------
		
		
		
		
		
		
		

		
		
		
		
		
		
		
		
		
		
		
		
		
		
		/**
		 * Metodo que inicia la radicacion RDI/RDE
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> iniciarRadicarRdiRde() {

			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);

				log.info(raizLog
						+ "---------------------------------------------------------------------------------------");
				log.info(raizLog
						+ "---------------------------------------------------------------------------------------");
				log.info(raizLog
						+ "---------------------------------------------------------------------------------------");
				log.info(raizLog
						+ "---------------------------------------------------------------------------------------");
				log.info(raizLog
						+ "----------------------INICIO ESTADO iniciarRadicarRdiRde  -----------------------------");

				log.info(raizLog + " iniciarRadicarRdiRde");

				try {

					mensajes = (List<Mensaje>) appContext.getMensajes();
					context.getExtendedState().getVariables().put(Consts.LISTA_MENSAJES, mensajes);

					context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, false);
					context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, 108);
					context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,
							"ErrorCorrespondencia no identificado en proceso de radicacion de RDI/RDE, consulte con el adminitrador del sistema ");

					String conexionUsuario = context.getMessageHeaders().get(Consts.CONEXION_CS_SOLICITANTE, String.class);
					context.getExtendedState().getVariables().put(Consts.CONEXION_CS_SOLICITANTE, conexionUsuario);

					String origenCdd = context.getMessageHeaders().get(Consts.ORIGEN_RDI_RDE, String.class);
					context.getExtendedState().getVariables().put(Consts.ORIGEN_RDI_RDE, origenCdd);

					String origenPcr = context.getMessageHeaders().get(Consts.ORIGEN_RDI_RDE_PCR, String.class);
					context.getExtendedState().getVariables().put(Consts.ORIGEN_RDI_RDE_PCR, origenPcr);

					String workId = context.getMessageHeaders().get(Consts.ID_WORKFLOW_RADICACION, String.class);
					context.getExtendedState().getVariables().put(Consts.ID_WORKFLOW_RADICACION, workId);

					String sigla = context.getMessageHeaders().get(Consts.SIGLA_RDI_RDE, String.class);
					context.getExtendedState().getVariables().put(Consts.SIGLA_RDI_RDE, sigla);

					String tipologia = context.getMessageHeaders().get(Consts.TIPOLOGIA_RDI_RDE, String.class);
					context.getExtendedState().getVariables().put(Consts.TIPOLOGIA_RDI_RDE, tipologia);

					Boolean esMensajeria = context.getMessageHeaders().get(Consts.ES_MENSAJERIA_RDI_RDE, Boolean.class);
					context.getExtendedState().getVariables().put(Consts.ES_MENSAJERIA_RDI_RDE, esMensajeria);

					Boolean esSucursal = context.getMessageHeaders().get(Consts.ES_SUCURSAL, Boolean.class);
					context.getExtendedState().getVariables().put(Consts.ES_SUCURSAL, esSucursal);

					Boolean origenEsPcr = context.getMessageHeaders().get(Consts.ORIGEN_ES_PCR, Boolean.class);
					context.getExtendedState().getVariables().put(Consts.ORIGEN_ES_PCR, origenEsPcr);

					Long parentIdAdjuntos = context.getMessageHeaders().get(Consts.PARENT_ID_ADJUNTOS_RDI_RDE,
							Long.class);
					context.getExtendedState().getVariables().put(Consts.PARENT_ID_ADJUNTOS_RDI_RDE, parentIdAdjuntos);

					String asunto = context.getMessageHeaders().get(Consts.ASUNTO_RDI_RDE, String.class);
					context.getExtendedState().getVariables().put(Consts.ASUNTO_RDI_RDE, asunto);

					Long idSolicitante = context.getMessageHeaders().get(Consts.ID_SOLICITANTE, Long.class);
					context.getExtendedState().getVariables().put(Consts.ID_SOLICITANTE, idSolicitante);
					
					Map<Long, RespuestaEntrega> destribucionRuta = new HashMap<>();
					context.getExtendedState().getVariables().put(Consts.DISTRIBUCION_RUTA, destribucionRuta);
					
					@SuppressWarnings("unchecked")
					List<String> origenesRdiRde = (List<String>) context.getMessageHeaders()
							.get(Consts.LISTA_ORIGENES_RDI_RDE, Object.class);
					context.getExtendedState().getVariables().put(Consts.LISTA_ORIGENES_RDI_RDE, origenesRdiRde);

					@SuppressWarnings("unchecked")
					List<String> destinosRdiRde = (List<String>) context.getMessageHeaders()
							.get(Consts.LISTA_DESTINOS_RDI_RDE, Object.class);
					context.getExtendedState().getVariables().put(Consts.LISTA_DESTINOS_RDI_RDE, destinosRdiRde);

					String nombreDependenciaOrigen = context.getMessageHeaders().get(Consts.NOMBRE_DEPENDENCIA_ORIGEN,
							String.class);
					context.getExtendedState().getVariables().put(Consts.NOMBRE_DEPENDENCIA_ORIGEN,
							nombreDependenciaOrigen);

					context.getExtendedState().getVariables().put(Consts.DISTRIBUIDO, 0);
					
					MetadatosPlantilla cat = new MetadatosPlantilla();

					cat.setAsunto(asunto);
					cat.setTipologia(tipologia);

					context.getExtendedState().getVariables().put(Consts.METADATOS_PLANTILLA, cat);

				} catch (Exception e) {
					log.info(raizLog + "Ocurrio un error iniciando radicacion RDI/RDE:" + e, e);
					context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, Boolean.TRUE);
				}


				log.info(raizLog + "FIN ESTADO iniciarRadicarRdiRde");

			};
		}

		/**
		 * Metodo que permite obtener el documento del Content Server
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> obtenerDocumentoCS2() {

			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + "INICIO Obtener mensajes ");
				mensajes = (List<Mensaje>) appContext.getMensajes();
				context.getExtendedState().getVariables().put(Consts.LISTA_MENSAJES, mensajes);

				log.info(raizLog + "FIN Obtener mensajes ");

				log.info(raizLog + " obtenerDocumentoCS");

				context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, false);
				context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, 108);
				context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,
						"ErrorCorrespondencia no identificado en proceso de radicacion de memorando, consulte con el adminitrador del sistema ");

				DataHandler contenido = null;
				String nombreDocumentoOriginal;
				long idDoc = context.getMessageHeaders().get(Consts.ID_DOC_RADICACION, Long.class);

				String conexionUsuario = context.getExtendedState().get(Consts.CONEXION_CS_SOLICITANTE, String.class);
				context.getExtendedState().getVariables().put(Consts.CONEXION_CS_SOLICITANTE, conexionUsuario);

				String workId = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);
				context.getExtendedState().getVariables().put(Consts.ID_WORKFLOW_RADICACION, workId);

				long idSolicitante = context.getExtendedState().get(Consts.ID_SOLICITANTE, Long.class);
				context.getExtendedState().getVariables().put(Consts.ID_SOLICITANTE, idSolicitante);

				log.info(raizLog + "Documento (" + idDoc + ") por obtener de Content Server");
				context.getExtendedState().getVariables().put(Consts.ID_DOC_RADICACION, idDoc);

				try {
					contenido = ContenidoDocumento.obtenerContenidoDocumentoDTHLR(
							autenticacionCS.getUserSoapHeader(conexionUsuario), idDoc, "", workId, wsdlsProps.getDocumento());
					if (contenido != null)
						log.info(raizLog + "OK Se obtuvo el contenido del DOC from CS ");
					else
						log.info(raizLog + "ERROR no se obtuvo el contenido del DOC from CS");

					nombreDocumentoOriginal = contenido.getName();
					context.getExtendedState().getVariables().put(Consts.NOMBRE_DOCUMENTO_ORIGINAL,	nombreDocumentoOriginal);
					
					context.getExtendedState().getVariables().put(Consts.NOMBRE_DOCUMENTO_ORIGINAL_DELETE, nombreDocumentoOriginal);
					
					context.getExtendedState().getVariables().put(Consts.CONTENIDO_DOC, contenido);

					log.info(raizLog + "Documento (" + idDoc + ") Obtenido de Content Server");
					context.getStateMachine().sendEvent(Events.DOCUMENTO_OBTENIDO_CS);
				} catch (SOAPException e) {
					context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, Boolean.TRUE);
					log.error("Error operacion", e);					
				} catch (IOException e) {
					context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, Boolean.TRUE);
					log.error("Error operacion", e);
				} catch (InterruptedException e) {
					context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, Boolean.TRUE);
					log.error("Error operacion", e);
				}
				
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}
			};
		}
		

		

		



		
		/**
		 * Metodo que genera documentos formulario
		 * @return
		 */
		@Bean
		public Action<States, Events> generarDocumentoFormulario() {

			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);

				if (!context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class)) {
					log.info(raizLog
							+ "------------------------------ESTADO generarDocumentoFormulario-------------------------");

					@SuppressWarnings("unchecked")
					List<Destino> listaDestinos = (List<Destino>) context.getExtendedState()
							.get(Consts.LISTA_DESTINOS_RDI_RDE, Object.class);

					String siglaOrigen = context.getExtendedState().get(Consts.SIGLA_RDI_RDE, String.class);
					String tipologia = context.getExtendedState().get(Consts.TIPOLOGIA_RDI_RDE, String.class);
					String asunto = context.getExtendedState().get(Consts.ASUNTO_RDI_RDE, String.class);

					String nombreDependenciaOrigen = context.getExtendedState().get(Consts.NOMBRE_DEPENDENCIA_ORIGEN,
							String.class);

					log.info(raizLog + "siglaOrigen:" + siglaOrigen + " tipologia:" + tipologia + " asunto:" + asunto);
					String tipologiaFinal = "";
					if (tipologia.equalsIgnoreCase("RDI")) {
						tipologiaFinal = "RDI - Registro de Distribución Interna";
					} else if (tipologia.equalsIgnoreCase("RDE")) {
						tipologiaFinal = "RDE - Registro de Distribución Externa";

					}

					try {
						String contenidoDoc = "Tipologia:" + tipologiaFinal + " siglaOrigen:" + siglaOrigen + " asunto:"
								+ asunto + " nombreDependenciaOrigen:" + nombreDependenciaOrigen;

						for (Destino destino : listaDestinos) {

							if (tipologia.equalsIgnoreCase("RDI")) {

								contenidoDoc += "Nombre:" + destino.getDependenciaDestino();
								contenidoDoc += "Dependencia / Entidad:" + destino.getDependenciaDestino();
							} else if (tipologia.equalsIgnoreCase("RDE")) {

								contenidoDoc += "Nombre:" + destino.getNombre();
								contenidoDoc += "Cargo:" + destino.getCargo();
								contenidoDoc += "Dependencia / Entidad:" + destino.getEntidad();
								contenidoDoc += "Dirección:" + destino.getDireccion() + ", " + destino.getCiudad()
										+ ", " + destino.getPais();
							}
						}

						DataHandler dataHandlerDoc = DocumentoUtils.generarDocumento(contenidoDoc, globalProperties.getRutaTemp());
						
						String nombreDoc = globalProperties.getRutaTemp() +  dataHandlerDoc.getName();
						log.info(raizLog + "archivo a eliminar: " +  nombreDoc);
						File doc = new File(nombreDoc);
						log.info(raizLog +"Archivo eliminado:" + doc.delete() );

					} catch (Exception e) {
						cargarError(context, e);
						context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, true);
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						log.info(raizLog + "ErrorCorrespondencia generarDocumentoFormulario " + e , e);
					}
				}
				
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}
			};

		}
		

	
		
		/**
		 * Metodo que permite obtener la informacion del formulario RDI/RDE
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> obtenerDatosCategoriaFormRdiRde() {

			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);

				log.info(raizLog
						+ "------------------------------ESTADO obtenerDatosCategoriaFormRdiRde-------------------------");

				if (!context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class)) {

					log.info(raizLog + " obtenerDatosCategoriaFormRdiRde");

					String workId = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);
					long workIdLong = Long.parseLong(workId);
					// borrar esta variable que no se necesita y eliminar el parametro del metodo

					String conexionUsuario = context.getExtendedState().get(Consts.CONEXION_CS_SOLICITANTE, String.class);
					String tipologia = context.getExtendedState().get(Consts.TIPOLOGIA_RDI_RDE, String.class);

					String atributo = "";

					if (tipologia.equalsIgnoreCase("RDI")) {
						atributo = "Información RDI";
					} else if (tipologia.equalsIgnoreCase("RDE")) {

						atributo = "Información RDE";

					}

					try {

						List<RowValue> listValores = Workflow.obtenerValoresFormularioPorSeccion(
								autenticacionCS.getUserSoapHeader(conexionUsuario), workIdLong, atributo,
								wsdlsProps.getWorkflow());
						
						log.info(raizLog + "Tamano lista de valores ["+ listValores.size() + "]");
						
						List<Destino> destinos = new ArrayList<>();
						
						List<String> listPcrDestino = new ArrayList<>();
						List<String> listCddDestino = new ArrayList<>();
						List<String> listDependenciaDestino = new ArrayList<>();

						for (RowValue rowValue : listValores) {

							Destino destino = new Destino();

							try {

								if (tipologia.equalsIgnoreCase("RDI")) {

									log.info(raizLog + "Obteniendo datos form RDI");

									//try {
									/*	
									List<String> listPcrDestino = new ArrayList<>();
									try {
										listPcrDestino = Workflow.obtenerValoresFormularioPorSeccionAtributo(
												autenticacionCS.getUserSoapHeader(conexionUsuario), workIdLong,
												atributo, "PCR Destino", wsdlsProps.getWorkflow());
									} catch (SOAPException e) {
										cargarError(context, e);
										context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
										log.error(raizLog
												+ "Ocurrio un error Obteniendo los datos del formulario RDI desde Content Server - PCR Destino",
												e);
										context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA,
												Boolean.TRUE);
										context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR,
												Consts.MSJ_106);
										context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,
												obtenerMensaje(Consts.MSJ_106.toString()));
										context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
										throw new Exception(e);

									}

									List<String> listCddDestino = new ArrayList<>();
									try {
										listCddDestino = Workflow.obtenerValoresFormularioPorSeccionAtributo(
												autenticacionCS.getUserSoapHeader(conexionUsuario), workIdLong,
												atributo, "CDD Destino", wsdlsProps.getWorkflow());
									} catch (SOAPException e) {
										cargarError(context, e);
										context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
										log.error(raizLog
												+ "Ocurrio un error Obteniendo los datos del formulario RDI desde Content Server - CDD Destino",
												e);
										context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA,
												Boolean.TRUE);
										context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR,
												Consts.MSJ_106);
										context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,
												obtenerMensaje(Consts.MSJ_106.toString()));
										context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
										throw new Exception(e);

									}
									
								

									List<String> listDependenciaDestino = new ArrayList<>();
									try {
										listDependenciaDestino = Workflow.obtenerValoresFormularioPorSeccionAtributo(
												autenticacionCS.getUserSoapHeader(conexionUsuario), workIdLong,
												atributo, "Dependencia Destino", wsdlsProps.getWorkflow());
									} catch (SOAPException e) {
										cargarError(context, e);
										context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
										log.error(raizLog
												+ "Ocurrio un error Obteniendo los datos del formulario RDI desde Content Server - Dependencia Destino",
												e);
										context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA,
												Boolean.TRUE);
										context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR,
												Consts.MSJ_106);
										context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,
												obtenerMensaje(Consts.MSJ_106.toString()));
										context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
										throw new Exception(e);

									}
									
									if(listPcrDestino.isEmpty()) {
										throw new IndexOutOfBoundsException("La lista listPcrDestino esta vacia");
									}
									
									if(listCddDestino.isEmpty()) {
										throw new IndexOutOfBoundsException("La lista listCddDestino esta vacia");
									}
									
									if(listDependenciaDestino.isEmpty()) {
										throw new IndexOutOfBoundsException("La lista listDependenciaDestino esta vacia");
									}
									
									log.info(raizLog + "Lista PCR Destino");
									for(String pcrDestino : listPcrDestino) {
										log.info(raizLog + "PCR Destino [" + pcrDestino + "]");
									}
									
									
									log.info(raizLog + "Lista Cdd Destino");
									for(String ccdDestino : listCddDestino) {
										log.info(raizLog + "Cdd Destino [" + ccdDestino + "]");
									}
									
									log.info(raizLog + "Lista dependencia Destino");
									for(String dependenciaDestino : listDependenciaDestino) {
										log.info(raizLog + "dependencia Destino [" + dependenciaDestino + "]");
									}
									*/

									DataValue valorDependencia = rowValue.getValues().stream()
											.filter(unCampo -> unCampo.getDescription().equals("Dependencia Destino"))
											.findFirst().get();
									DataValue valorPcr = rowValue.getValues().stream()
											.filter(unCampo -> unCampo.getDescription().equals("PCR Destino"))
											.findFirst().get();
									DataValue valorCdd = rowValue.getValues().stream()
											.filter(unCampo -> unCampo.getDescription().equals("CDD Destino"))
											.findFirst().get();
									DataValue valorDescripcion = rowValue.getValues().stream()
											.filter(unCampo -> unCampo.getDescription().equals("Descripción Objeto"))
											.findFirst().get();
									DataValue valorCantidad = rowValue.getValues().stream()
											.filter(unCampo -> unCampo.getDescription().equals("Cantidad")).findFirst()
											.get();

									if (valorDependencia instanceof StringValue) {
										StringValue unStringValue = (StringValue) valorDependencia;
										String dependenciaDestino = "";
										if (!unStringValue.getValues().isEmpty()) {
											dependenciaDestino = unStringValue.getValues().get(0);
											log.info(raizLog + "dependenciaDestino [" +dependenciaDestino + "]" );
											listDependenciaDestino.add(dependenciaDestino);
										destino.setDependenciaDestino(dependenciaDestino);
										} else {
											throw new NullPointerException(
													"El atributo dependencia destino no esta diligenciado en el formulario");
										}
									}

									if (valorPcr instanceof StringValue) {
										StringValue unStringValue = (StringValue) valorPcr;
										String pcrDestino = "";
										if (!unStringValue.getValues().isEmpty()) {
											pcrDestino = unStringValue.getValues().get(0);

										log.info(raizLog + "pcrDestino [" + pcrDestino + "]");
										listPcrDestino.add(pcrDestino);

										destino.setPcrDestino(pcrDestino);
										} else {
											throw new NullPointerException(
													"El atributo pcrDestino no esta diligenciado en el formulario");
										}
									}

									if (valorCdd instanceof StringValue) {
										StringValue unStringValue = (StringValue) valorCdd;
										String cddDestino = "";
										if (!unStringValue.getValues().isEmpty()) {
											cddDestino = unStringValue.getValues().get(0);
											
										log.info(raizLog + "cddDestino [" + cddDestino + "]");
										listCddDestino.add(cddDestino);
										destino.setCddDestino(cddDestino);
										}else {
											throw new NullPointerException(
													"El atributo cddDestino no esta diligenciado en el formulario");
										}
									}

									if (valorDescripcion instanceof StringValue) {
										StringValue unStringValue = (StringValue) valorDescripcion;
										String descripcion = unStringValue.getValues().get(0);
										
										log.info(raizLog + "descripcion [" + descripcion + "]");
										destino.setDescripcionObjeto(descripcion);
										;
									}

									if (valorCantidad instanceof IntegerValue) {
										IntegerValue unStringValue = (IntegerValue) valorCantidad;
										Long cantidad = unStringValue.getValues().get(0);
										
										log.info(raizLog + "cantidad [" + cantidad + "]");
										destino.setCantidad(cantidad);
									}

									if (destino.getDescripcionObjeto().isEmpty()
											|| destino.getDependenciaDestino().isEmpty()
											|| destino.getCantidad() == null) {

										context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA,
												Boolean.TRUE);
										context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR,
												Consts.MSJ_106);
										context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,
												obtenerMensaje(Consts.MSJ_106.toString()));
										log.info(raizLog + "Uno de los datos del formulario RDI esta vacio");
										return;
									}

										destinos.add(destino);
										
										
										/*
										context.getExtendedState().getVariables().put(Consts.LIST_PCR_DESTINO_RDI_RDE,
												listPcrDestino);
										context.getExtendedState().getVariables().put(Consts.LIST_CDD_DESTINO_RDI_RDE,
												listCddDestino);
										context.getExtendedState().getVariables()
												.put(Consts.LIST_DEPENDENCIA_DESTINO_RDI_RDE, listDependenciaDestino);
										*/

									/*} catch (NoSuchElementException e) {
										cargarError(context, e);
										context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
										log.error(raizLog + "Ocurrio un error leyendo los datos del formulario RDI",e);
										context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA,
												Boolean.TRUE);
										context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR,
												Consts.MSJ_106);
										context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,
												obtenerMensaje(Consts.MSJ_106.toString()));
									}catch (SOAPException e) {
										cargarError(context, e);
										context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
										log.error(raizLog + "Ocurrio un error Obteniendo los datos del formulario RDI desde Content Server",e);
										context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA,
												Boolean.TRUE);										
										context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR,
												Consts.MSJ_106);
										context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,
												obtenerMensaje(Consts.MSJ_106.toString()));
									
									}*/
									

								} else if (tipologia.equalsIgnoreCase("RDE")) {

									log.info(raizLog + "Obteniendo datos form RDE");

									try {

										DataValue valorNombre = rowValue.getValues().stream()
												.filter(unCampo -> unCampo.getDescription().equals("Nombre"))
												.findFirst().get();
										DataValue valorCargo = rowValue.getValues().stream()
												.filter(unCampo -> unCampo.getDescription().equals("Cargo")).findFirst()
												.get();
										DataValue valorDireccion = rowValue.getValues().stream()
												.filter(unCampo -> unCampo.getDescription().equals("Dirección"))
												.findFirst().get();
										DataValue valorPais = rowValue.getValues().stream()
												.filter(unCampo -> unCampo.getDescription().equals("País")).findFirst()
												.get();
										DataValue valorCiudad = rowValue.getValues().stream()
												.filter(unCampo -> unCampo.getDescription().equals("Ciudad"))
												.findFirst().get();
										DataValue valorEntidad = rowValue.getValues().stream()
												.filter(unCampo -> unCampo.getDescription().equals("Entidad"))
												.findFirst().get();
										DataValue valorTelefono = rowValue.getValues().stream()
												.filter(unCampo -> unCampo.getDescription().equals("Teléfono"))
												.findFirst().get();
										DataValue valorDescripcion = rowValue.getValues().stream().filter(
												unCampo -> unCampo.getDescription().equals("Descripción Objeto"))
												.findFirst().get();

										if (valorNombre instanceof StringValue) {
											StringValue unStringValue = (StringValue) valorNombre;
											String valor = "";
											if (!unStringValue.getValues().isEmpty())
												valor = unStringValue.getValues().get(0);
											log.info(raizLog + "Nombre:" + valor);
											destino.setNombre(valor);
										}

										if (valorCargo instanceof StringValue) {
											StringValue unStringValue = (StringValue) valorCargo;
											String valor = "";
											if (!unStringValue.getValues().isEmpty())
												valor = unStringValue.getValues().get(0);
											destino.setCargo(valor);
										}

										if (valorDireccion instanceof StringValue) {
											StringValue unStringValue = (StringValue) valorDireccion;
											String valor = "";
											if (!unStringValue.getValues().isEmpty())
												valor = unStringValue.getValues().get(0);
											destino.setDireccion(valor);
										}

										if (valorPais instanceof StringValue) {
											StringValue unStringValue = (StringValue) valorPais;
											String valor = "";
											if (!unStringValue.getValues().isEmpty())
												valor = unStringValue.getValues().get(0);
											destino.setPais(valor);
										}

										if (valorCiudad instanceof StringValue) {
											StringValue unStringValue = (StringValue) valorCiudad;
											String valor = "";
											if (!unStringValue.getValues().isEmpty())
												valor = unStringValue.getValues().get(0);
											destino.setCiudad(valor);
										}

										if (valorEntidad instanceof StringValue) {
											StringValue unStringValue = (StringValue) valorEntidad;
											String valor = "";
											if (!unStringValue.getValues().isEmpty())
												valor = unStringValue.getValues().get(0);
											destino.setEntidad(valor);
										}

										if (valorTelefono instanceof StringValue) {
											StringValue unStringValue = (StringValue) valorTelefono;
											String valor = "";
											if (!unStringValue.getValues().isEmpty())
												valor = unStringValue.getValues().get(0);
											destino.setTelefono(valor);
										}

										if (valorDescripcion instanceof StringValue) {
											StringValue unStringValue = (StringValue) valorDescripcion;
											String valor = "";
											if (!unStringValue.getValues().isEmpty())
												valor = unStringValue.getValues().get(0);
											destino.setDescripcionObjeto(valor);
										}

										if (destino.getDescripcionObjeto().isEmpty() || destino.getCiudad().isEmpty()
												|| destino.getPais().isEmpty() || destino.getDireccion().isEmpty()
												|| (destino.getNombre().isEmpty() && destino.getEntidad().isEmpty())) {

											context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA,
													Boolean.TRUE);
											context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR,
													Consts.MSJ_106);
											context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,
													obtenerMensaje(Consts.MSJ_106.toString()));
											log.info(raizLog + "Uno de los datos del formulario RDE esta vacio");
											return;
										}

										destinos.add(destino);

									} catch (Exception e) {
										cargarError(context, e);
										context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
										log.error(raizLog + "Ocurrio un error leyendo los datos del formulario RDE",e);
										context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA,
												Boolean.TRUE);
										context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR,
												Consts.MSJ_106);
										context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,
												obtenerMensaje(Consts.MSJ_106.toString()));
									}

								}

							} catch (Exception e) {
								cargarError(context, e);
								context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
								context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR,
										Consts.MSJ_106);
								context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,
										obtenerMensaje(Consts.MSJ_106.toString()));
								log.info(raizLog + "Ocurrio un error leyendo los datos del formulario RDI/RDE:" + e,e);

							}

						}
						
						context.getExtendedState().getVariables().put(Consts.LIST_PCR_DESTINO_RDI_RDE,
								listPcrDestino);
						context.getExtendedState().getVariables().put(Consts.LIST_CDD_DESTINO_RDI_RDE,
								listCddDestino);
						context.getExtendedState().getVariables()
								.put(Consts.LIST_DEPENDENCIA_DESTINO_RDI_RDE, listDependenciaDestino);

						context.getExtendedState().getVariables().put(Consts.LISTA_DESTINOS_RDI_RDE, destinos);

					} catch (NoSuchElementException e) {
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						log.info(raizLog + "ErrorCorrespondencia obtenerDatosCategoriaFormRdiRde obtenerValoresFormularioPorSeccion" + e, e);
					} catch (SOAPException | WebServiceException e) {
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						log.info(raizLog + "ErrorCorrespondencia obtenerDatosCategoriaFormRdiRde obtenerValoresFormularioPorSeccion" + e,e);
					} catch (IOException e) {
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						log.info(raizLog + "ErrorCorrespondencia obtenerDatosCategoriaFormRdiRde obtenerValoresFormularioPorSeccion" + e, e);
					}
				}
				
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}
			};
		}


		
		

		/**
		 * Metodo que permite generar el numero de radicado RDI/RDE
		 * 
		 * @return
		 */
		@SuppressWarnings("unused")
		@Bean
		public Action<States, Events> iniciarWfGenerarNumRadicadoRdiRde() {

			return (context) -> {

				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " obtenerDatosCategoriaFormRdiRde");

				log.info(raizLog
						+ "-----------------------INICIO ESTADO iniciar WF Genrar NumRadicado RDI/RDE---------------------------------");

				if (!context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class)) {

					mensajes = (List<Mensaje>) appContext.getMensajes();

					String siglaOrigen = context.getExtendedState().get(Consts.SIGLA_RDI_RDE, String.class);
					String tipologia = context.getExtendedState().get(Consts.TIPOLOGIA_RDI_RDE, String.class);
					Long idSolicitante = context.getExtendedState().get(Consts.ID_SOLICITANTE, Long.class);
					String conexionUsuario = context.getExtendedState().get(Consts.CONEXION_CS_SOLICITANTE, String.class);

					String titulo = "FT107_Generar_NroRadicado_RDI_RDE";
					String atributoNumRadicado = "_radicadoGenerado";
					if (siglaOrigen == null || siglaOrigen.isEmpty()) {
						context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, Boolean.TRUE);
						context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, 101);
						context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,
								obtenerMensaje(Consts.MSJ_101.toString()));
						return;
					} else if (tipologia == null || tipologia.isEmpty()) {
						context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, Boolean.TRUE);
						context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, 101);
						context.getExtendedState().getVariables().put(Consts.MENSAJE_ERROR,
								obtenerMensaje(Consts.MSJ_101.toString()));
						return;

					}

					Map<String, ValorAtributo> atributos = new HashMap<String, ValorAtributo>();
					atributos.put("_sigla", new ValorAtributo(siglaOrigen, TipoAtributo.STRING));
					atributos.put("_tipologia", new ValorAtributo(tipologia, TipoAtributo.STRING));
					atributos.put(workflowProps.getAtributo().getIdSolicitante(), new ValorAtributo(idSolicitante, TipoAtributo.USER));
					atributos.put("Fecha Radicación", new ValorAtributo(new Date(), TipoAtributo.DATE));

					try {
						if (!context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class)) {
							log.info(raizLog + "INICIO iniciar WF Genrar NumRadicado");
							long idProcess = Workflow.iniciarWorkflowConAtributos(
									autenticacionCS.getAdminSoapHeader(),
									workflowProps.getGenerarNumeroRadicado(), titulo, atributos,
									wsdlsProps.getWorkflow());

							log.info(raizLog + "FIN iniciar WF Genrar NumRadicado");

							log.info(raizLog + "INICIO obtener NumRadicado");

							String numeroRadicadoObtenido = Workflow
									.obtenerValoresAtributo(autenticacionCS.getAdminSoapHeader(),
											idProcess, atributoNumRadicado, wsdlsProps.getWorkflow())
									.get(0).toString();

							context.getExtendedState().getVariables().put(Consts.NUMERO_RADICADO_OBTENIDO,
									numeroRadicadoObtenido);

							log.info(raizLog + "El numero de radicado es:" + numeroRadicadoObtenido);
							log.info(raizLog + "FIN obtener NumRadicado");
						}
					} catch (NumberFormatException | NoSuchElementException |SOAPException | IOException | WebServiceException   e) {
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						log.info(raizLog + "ErrorCorrespondencia iniciarWfGenerarNumRadicadoRdiRde iniciarWorkflowConAtributos" + e , e);
					
					}

					log.info(raizLog + "FIN ESTADO iniciar WF Genrar NumRadicado RDI/RDE");

				}
				
				try {
					persistContexto(context);
				} catch (Exception e) {
					log.info(raizLog +
							"Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}
				
				
			};

		}
		
		
		
		/**
		 * Metodo que permite cargar una nueva version del documento en el Content
		 * Server
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> cargarNuevaVersionDocCS() {

			return (context) -> {

				if (!context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class)) {

					String raizLog = SSMUtils.getRaizLog(context);
					log.info(raizLog + " cargarNuevaVersionDocCS");

					String nombreOriginal = context.getExtendedState().get(Consts.NOMBRE_DOCUMENTO_ORIGINAL,
							String.class);
					String numeroRadicado = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO,
							String.class);
					//MetadatosPlantilla cat = context.getExtendedState().get(Consts.METADATOS_PLANTILLA, MetadatosPlantilla.class);
					//String conexionUsuario = context.getExtendedState().get(Consts.CONEXION_CS_SOLICITANTE, String.class);
					long idDoc = context.getExtendedState().get(Consts.ID_DOC_RADICACION, Long.class);

					//String nuevoNombreDoc = numeroRadicado + " " + cat.getAsunto().replace(":", "");
					
					String nuevoNombreDoc = numeroRadicado;

					nuevoNombreDoc = (nuevoNombreDoc.length() > globalProperties.getLongitudNuevoNombreArchivo())
							? nuevoNombreDoc.substring(0, globalProperties.getLongitudNuevoNombreArchivo())
							: nuevoNombreDoc;

					String extensionDocumento = "docx";

					try {
						log.info(raizLog + "INICIO cargar documento CS");
						log.info(raizLog + " INICIO cargar documento CS idDoc: " + idDoc + "  nuevoNombreDoc: " + nuevoNombreDoc + " nombreOriginal: " + nombreOriginal);
						ContenidoDocumento.cargarDocumentoCS(autenticacionCS.getAdminSoapHeader(), idDoc,
								nombreOriginal, nuevoNombreDoc + "." + extensionDocumento,globalProperties.getRutaTemp(), wsdlsProps.getDocumento());
						log.info(raizLog + "FIN cargar documento CS");
						log.info(raizLog + " FIN cargar documento CS idDoc " + idDoc);
					} catch (SOAPException | WebServiceException | IOException | InterruptedException e) {
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						context.getExtendedState().getVariables().put(Consts.CODIGO_ERROR_RADICAR, 200);
						log.error(raizLog + "ErrorCorrespondencia cargarNuevaVersionDocCS cargarDocumentoCS " + e, e);
					} 
				}
				
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}
			};
		}
		
		
		

		/**
		 * Metodo que permite generar el sticker para RDE
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> generarSticker() {

			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " generarSticker");

				log.info(raizLog
						+ "----------------------INICIO ESTADO generarSticker------------------------------------");

				if (!context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class)) {

					String numeroRadicadoObtenido = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO,
							String.class);
					
					String workId = context.getMessageHeaders().get(Consts.ID_WORKFLOW_RADICACION, String.class);
					
					String tipologia = context.getExtendedState().get(Consts.TIPOLOGIA_RDI_RDE, String.class);
					Long parentIdAdjuntos = context.getExtendedState().get(Consts.PARENT_ID_ADJUNTOS_RDI_RDE,
							Long.class);
					@SuppressWarnings("unchecked")
					List<Destino> destinos = (List<Destino>) context.getExtendedState().getVariables()
							.get(Consts.LISTA_DESTINOS_RDI_RDE);
					String conexionUsuario = context.getExtendedState().get(Consts.CONEXION_CS_SOLICITANTE, String.class);

					List<Sticker> stickers = new ArrayList<>();
					DataHandler documento = null;

					if (tipologia.equalsIgnoreCase("RDE")) {

						for (Destino destino : destinos) {

							log.info(raizLog + "Sticker Destino:" + destino.getNombre());

							List<Atributo> atributos = new ArrayList<>();

							int coordenada = 0;

							if (numeroRadicadoObtenido != null && !numeroRadicadoObtenido.isEmpty()) {

								atributos.add(new Atributo(estadosProps.getRadicado(), numeroRadicadoObtenido,
										new Coordenada(coordenada, 0)));
								coordenada++;
							}

							if (destino.getNombre() != null && !destino.getNombre().isEmpty()) {

								atributos.add(
										new Atributo("Nombre", destino.getNombre(), new Coordenada(coordenada, 0)));
								coordenada++;
							}

							if (destino.getCargo() != null && !destino.getCargo().isEmpty()) {

								atributos.add(new Atributo("Cargo", destino.getCargo(), new Coordenada(coordenada, 0)));
								coordenada++;

							}

							if (destino.getEntidad() != null && !destino.getEntidad().isEmpty()) {

								atributos.add(
										new Atributo("Entidad", destino.getEntidad(), new Coordenada(coordenada, 0)));
								coordenada++;
							}

							if (destino.getDireccion() != null && !destino.getDireccion().isEmpty()) {

								atributos.add(new Atributo("Dirección", destino.getDireccion(),
										new Coordenada(coordenada, 0)));
								coordenada++;
							}

							if (destino.getCiudad() != null && !destino.getCiudad().isEmpty()
									&& destino.getPais() != null && !destino.getPais().isEmpty()) {

								atributos.add(new Atributo("Ciudad", destino.getCiudad() + ", " + destino.getPais(),
										new Coordenada(coordenada, 0)));
								coordenada++;
							}

							Sticker sticker = new Sticker(atributos);

							stickers.add(sticker);

						}

						try {
							documento = DocumentoUtils.generarDocumentoSticker(stickers, workId, globalProperties.getRutaTemp());
						} catch (IOException | WebServiceException e) {
							cargarError(context, e);
							context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
							log.info(raizLog + "ErrorCorrespondencia generarSticker generarDocumentoSticker " + e , e);
						}

						String nombreDoc = "Sobre - " + numeroRadicadoObtenido + ".docx";
						try {
							ContenidoDocumento.crearDocumento(autenticacionCS.getUserSoapHeader(conexionUsuario),
									nombreDoc, documento, parentIdAdjuntos, wsdlsProps.getDocumento());
							
							nombreDoc = globalProperties.getRutaTemp() +  documento.getName();
							log.info(raizLog + "archivo a eliminar: " +  nombreDoc);
							File doc = new File(nombreDoc);
							log.info(raizLog +"Archivo eliminado:" + doc.delete() );
							
						} catch (SOAPException | WebServiceException | IOException | InterruptedException e) {
							cargarError(context, e);
							context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
							log.info(raizLog + "ErrorCorrespondencia generarSticker crearDocumento " + e , e);
						} 

					}

				}
				log.info(raizLog
						+ "----------------------FIN ESTADO generarSticker------------------------------------");
				
				try {
					persistContexto(context);
				} catch (Exception e) {
					log.info(raizLog +
							"Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}
			};
		}
		
		
		

		/**
		 * Metodo que permite generarl el sticker para Carta
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> generarStickerCarta2() {

			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " generarSticker");

				log.info(raizLog
						+ "----------------------INICIO ESTADO generarSticker Carta------------------------------------");

				if (!context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class)) {

					String numeroRadicadoObtenido = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO,
							String.class);
					
					String workId = context.getMessageHeaders().get(Consts.ID_WORKFLOW_RADICACION, String.class);
					
					String tipologia = context.getExtendedState().get(Consts.TIPOLOGIA_MEMO_CARTA, String.class);

					String conexionUsuario = context.getExtendedState().get(Consts.CONEXION_CS_SOLICITANTE, String.class);

					MetadatosPlantilla cat = (MetadatosPlantilla) context.getExtendedState().get(Consts.METADATOS_PLANTILLA,
							Object.class);

					
					Long parentId = null;
					try {
						parentId = Workflow.obtenerIdContenedorAdjuntos(autenticacionCS.getAdminSoapHeader(), Integer.parseInt(workId), wsdlsProps.getWorkflow());
					} catch (NumberFormatException | IOException | SOAPException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					


					List<Sticker> stickers = new ArrayList<>();
					DataHandler documento = null;

					List<Empleado> destinos = cat.getDestinatarios();

					List<Empleado> copias = cat.getCopias();

					if (copias != null && !copias.isEmpty())
						destinos.addAll(copias);

					if (tipologia.equalsIgnoreCase("CA")) {

						for (Empleado destino : destinos) {

							log.info(raizLog + "Sticker Destino:" + destino.getNombre());

							if (!destino.getRol().contains("@")) {

								List<Atributo> atributos = new ArrayList<>();

								int coordenada = 0;

								if (numeroRadicadoObtenido != null && !numeroRadicadoObtenido.isEmpty()) {

									atributos.add(new Atributo("Radicado", numeroRadicadoObtenido,
											new Coordenada(coordenada, 0)));
									coordenada++;
								}

								if (destino.getNombre() != null && !destino.getNombre().isEmpty()) {

									atributos.add(
											new Atributo("Nombre", destino.getNombre(), new Coordenada(coordenada, 0)));
									coordenada++;
								}

								if (destino.getCargo() != null && !destino.getCargo().isEmpty()) {

									atributos.add(
											new Atributo("Cargo", destino.getCargo(), new Coordenada(coordenada, 0)));
									coordenada++;

								}

								if (destino.getDependencia() != null && !destino.getDependencia().isEmpty()) {

									atributos.add(new Atributo("Entidad", destino.getDependencia(),
											new Coordenada(coordenada, 0)));
									coordenada++;
								}

								if (destino.getRol() != null && !destino.getRol().isEmpty()) {

									atributos.add(
											new Atributo("Dirección", destino.getRol(), new Coordenada(coordenada, 0)));
									coordenada++;
								}

								Sticker sticker = new Sticker(atributos);

								stickers.add(sticker);

							}

						}

						try {
							documento = DocumentoUtils.generarDocumentoSticker(stickers, workId, globalProperties.getRutaTemp());
							
							context.getStateMachine().sendEvent(Events.IR_SUBIR_DOC_CONSECUTIVO);
							
						} catch (IOException | WebServiceException e) {
							cargarError(context, e);
							context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
							log.error(raizLog + "ErrorCorrespondencia generarStickerCarta generarDocumentoSticker " + e, e);
						}

						String nombreDoc = "Sobre - " + numeroRadicadoObtenido + ".docx";
						Long idDocSticker;
						try {
							idDocSticker = ContenidoDocumento.crearDocumento(
									autenticacionCS.getUserSoapHeader(conexionUsuario), nombreDoc, documento, parentId,
									wsdlsProps.getDocumento());
							context.getExtendedState().getVariables().put(Consts.ID_DOC_STICKER_CARTA, idDocSticker);
						} catch (SOAPException | WebServiceException e) {
							cargarError(context, e);
							context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
							log.error(raizLog + "ErrorCorrespondencia generarStickerCarta crearDocumento " + e, e);
						} catch (IOException | InterruptedException e) {
							context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
							log.error(raizLog + "ErrorCorrespondencia generarStickerCarta crearDocumento " + e, e);
						} 

					}

				}
				log.info(raizLog
						+ "----------------------FIN ESTADO generarSticker Carta------------------------------------");
				
				try {
					persistContexto(context);
				} catch (Exception e) {
					log.info(raizLog +
							"Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}
			};
		}



		/**
		 * Metodo que permite cargar la categoria Correspondencia con los metadatos
		 * obtenidos del formulario
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> actualizarMetadatosDocumentoRdiRde() {

			return (context) -> {

				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " actualizarMetadatosDocumentoRdiRde");

				if (!context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class)) {

					log.info(raizLog
							+ "-----------------------------------ESTADO actualizarMetadatosDocumentoRdiRde-----------------------------------");

					@SuppressWarnings("unchecked")
					List<Destino> listaDestinatarios = (List<Destino>) context.getExtendedState()
							.get(Consts.LISTA_DESTINOS_RDI_RDE, Object.class);

	
					String nombreDependenciaOrigen = context.getExtendedState().get(Consts.NOMBRE_DEPENDENCIA_ORIGEN,
							String.class);

					Long idDoc = context.getMessageHeaders().get(Consts.ID_DOC_RADICACION, Long.class);
					context.getExtendedState().getVariables().put(Consts.ID_DOC_RADICACION, idDoc);

					log.info(raizLog + "El idDoc (forulario pdf) es:" + idDoc);

					String numeroRadicadoObtenido = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO,
							String.class);

					log.info(raizLog + "EL radicado RDI/RDE generado es:" + numeroRadicadoObtenido);
					
					DateFormat dateFormatHora = new SimpleDateFormat(globalProperties.getFormatoHora());
					Date date = new Date();
					
					String hora = dateFormatHora.format(date);

					String siglaOrigen = context.getExtendedState().get(Consts.SIGLA_RDI_RDE, String.class);
					String tipologia = context.getExtendedState().get(Consts.TIPOLOGIA_RDI_RDE, String.class);
					String asunto = context.getExtendedState().get(Consts.ASUNTO_RDI_RDE, String.class);
				

					log.info(raizLog + "siglaOrigen:" + siglaOrigen + " tipologia:" + tipologia + " asunto:" + asunto);
					String tipologiaFinal = "";
					if (tipologia.equalsIgnoreCase("RDI")) {
						tipologiaFinal = "RDI - Registro de Distribución Interna";
					} else if (tipologia.equalsIgnoreCase("RDE")) {
						tipologiaFinal = "RDE - Registro de Distribución Externa";

					}

					Map<String, Object> metadatos = new HashMap<>();
		

					metadatos.put(categoriaProps.getCorrespondencia().getAtributoTipologia(), tipologiaFinal);
					metadatos.put(categoriaProps.getCorrespondencia().getAtributoEstado(),
							estadosProps.getRadicado());
					metadatos.put(categoriaProps.getCorrespondencia().getAtributoAsunto(), asunto);
					metadatos.put(categoriaProps.getCorrespondencia().getAtributoFechaRadicacion(), date);
					metadatos.put(categoriaProps.getCorrespondencia().getAtributoHoraRadicacion(), hora);
					metadatos.put(categoriaProps.getCorrespondencia().getAtributoNumeroRadicacion(), numeroRadicadoObtenido);

					List<Map<String, Object>> listSubGrupoDestino = new ArrayList<>();

					List<Map<String, Object>> listSubGrupoOrigen = new ArrayList<>();

					for (Destino destino : listaDestinatarios) {

						log.info(raizLog + "Destinatario:" + destino.getDependenciaDestino());

						Map<String, Object> metadatosSubGrupoDestino = new HashMap<>();

						Map<String, Object> metadatosSubGrupoOrigen = new HashMap<>();

						metadatosSubGrupoOrigen.put("Nombre", nombreDependenciaOrigen);
						metadatosSubGrupoOrigen.put("Cargo", "");
						metadatosSubGrupoOrigen.put("Dependencia", nombreDependenciaOrigen);
						listSubGrupoOrigen.add(metadatosSubGrupoOrigen);
						
						trucarValoresCategoriaCorrespondencia(listSubGrupoOrigen);
						
						
						if (tipologia.equalsIgnoreCase("RDI")) {

							log.info(raizLog + "Metadatos RDI");

							metadatosSubGrupoDestino.put("Nombre", "");
							metadatosSubGrupoDestino.put("Cargo", "");
							metadatosSubGrupoDestino.put("Dependencia / Entidad", destino.getDependenciaDestino());
							listSubGrupoDestino.add(metadatosSubGrupoDestino);
							
							trucarValoresCategoriaCorrespondencia(listSubGrupoDestino);
							
						} else if (tipologia.equalsIgnoreCase("RDE")) {

							log.info(raizLog + "Metadatos RDE");

							metadatosSubGrupoDestino.put("Nombre", destino.getNombre());
							metadatosSubGrupoDestino.put("Cargo", destino.getCargo());
							metadatosSubGrupoDestino.put("Dependencia / Entidad", destino.getEntidad());
							metadatosSubGrupoDestino.put("Dirección",
									destino.getDireccion() + ", " + destino.getCiudad() + ", " + destino.getPais());
							listSubGrupoDestino.add(metadatosSubGrupoDestino);
							
							trucarValoresCategoriaCorrespondencia(listSubGrupoDestino);

						}

					}

					try {

						log.info(raizLog + "INICIO Crear categoria");
						ContenidoDocumento.adicionarCategoriaMetadata(
								autenticacionCS.getAdminSoapHeader(), idDoc,
								categoriaProps.getCorrespondencia().getId(),
								categoriaProps.getCorrespondencia().getNombre(), metadatos,
								categoriaProps.getCorrespondencia().getSubgrupoOrigen(), listSubGrupoOrigen.get(0),
								Boolean.TRUE, wsdlsProps.getDocumento());

						for (Map<String, Object> mapDestino : listSubGrupoDestino) {

							ContenidoDocumento.adicionarCategoriaMetadata(
									autenticacionCS.getAdminSoapHeader(), idDoc,
									categoriaProps.getCorrespondencia().getId(),
									categoriaProps.getCorrespondencia().getNombre(), metadatos,
									categoriaProps.getCorrespondencia().getSubgrupoDestino(), mapDestino, Boolean.FALSE,
									wsdlsProps.getDocumento());
						}
						log.info(raizLog + "FIN Crear categoria");

					} catch (ServerSOAPFaultException | IOException | SOAPException  e) {
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
						log.error(raizLog + "ErrorCorrespondencia actualizarMetadatosDocumentoRdiRde adicionarCategoriaMetadata " + e , e);
					
					} catch (Exception e) {
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						log.error(raizLog + "ErrorCorrespondencia actualizarMetadatosDocumentoRdiRde adicionarCategoriaMetadata " + e , e);
					}

				}
				try {
					persistContexto(context);
					context.getStateMachine().sendEvent(Events.IR_VALIDAR_INICIAR_FORMA_DISTRIBUIR_RDI_RDE);
				} catch (Exception e) {					
					log.error("Error operacion", e);
					cargarError(context, e);
					context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
					log.error(raizLog + "Ocurrio un error almacenando la maquina de estados en la BD: " + e , e);
				}
				
			};
		}
		


		/**
		 * Función que permite retornar una cadena con el nombre la sigla y el cargo de los firmantes
		 * @param firmantes
		 * @param listaNitFirmantes
		 * @return
		 */
		private String retornarCadenaFrimantes(List<Empleado> firmantes) {
			String listaNitFirmantes = null;
			String siglaRemitente = "";
			String nombreFirmante = "";
			String cargoFirmante = "";

			boolean isPrimerFirmante = true;
			
			for (Empleado empleadoFirmante : firmantes) {
				if (isPrimerFirmante) {								
					siglaRemitente = empleadoFirmante.getSiglaremitente();
					nombreFirmante = empleadoFirmante.getNombre();
					cargoFirmante = empleadoFirmante.getCargo();
					
					listaNitFirmantes = formarCadenaFirmantes(nombreFirmante.trim(), siglaRemitente.trim(), cargoFirmante.trim()) +",";
					
					isPrimerFirmante = false;

				}else {
					if(org.springframework.util.StringUtils.hasText(empleadoFirmante.getNombre())) {
						listaNitFirmantes =  listaNitFirmantes + formarCadenaFirmantes(empleadoFirmante.getNombre().trim(), empleadoFirmante.getSiglaremitente().trim(), empleadoFirmante.getCargo().trim());
					}
					
					break; //Se sale de las iteraciones en el segundo ciclo
				}

			}
			
			return listaNitFirmantes;
		}

		
		/**
		 * Función que permite formar una cadena con el nombre la sigla y el cargo para cada firmante
		 * útil para enviar a firma
		 * @param nombre
		 * @param Sigla
		 * @param Cargo
		 * @return
		 */
		private String formarCadenaFirmantes(String nombre, String Sigla, String Cargo) {
			String separador = " - ";
			String listaFirmantes = "{" + nombre + separador + Sigla + separador + Cargo + "}";
			return listaFirmantes;
		}



		/**
		 * Metodo que permite personalizar Documentos tipo Carta
		 * 
		 * El metodo genera una copia del documento por cada una de los destinatarios de
		 * tal forma que solo se muestre un destinatario
		 * 
		 * @return
		 */
		/*
		@Bean
		public Action<States, Events> personalizarDocumento() {

			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);

				log.info(raizLog + "-------------INICIO estado personalizarDocumento-------------");
				if (!context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class)) {

					try {

						String nombreOriginal = context.getExtendedState().get(Consts.NOMBRE_DOCUMENTO_ORIGINAL,
								String.class);
												
						MetadatosPlantilla cat = context.getExtendedState().get(Consts.METADATOS_PLANTILLA, MetadatosPlantilla.class);
						Boolean esPersonalizado = false;
						
						if (cat.getTipologia().equalsIgnoreCase("CA")) {
							
							String conexionUsuario = context.getExtendedState().get(Consts.CONEXION_CS_SOLICITANTE,
									String.class);
							
							//Se actualiza el parent id de la carpeta original en caso de que el usuario haya movido el documento
							actualizarIdParentOriginal(context, autenticacionCS.getAdminSoapHeader(), wsdlsProps.getDocumento());
							
							Long parentIdAdjuntos = context.getExtendedState().get(Consts.PARENT_ID_ADJUNTOS_MEMO_CARTA,
									Long.class);
							String numeroRadicado = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO,
									String.class);
							String tipoDocumentalCatDocumento = context.getExtendedState()
									.get(Consts.TIPO_DOCUMENTAL_CAT_DOCUMENTO, String.class);
							String serieCatDocumento = context.getExtendedState().get(Consts.SERIE_CAT_DOCUMENTO,
									String.class);

							long idDocOriginal = context.getExtendedState().get(Consts.ID_DOC_RADICACION, Long.class);
							List<DataHandler> listDataHandler = new ArrayList<>();
							List<Empleado> destinos = cat.getDestinatarios();

							esPersonalizado = Boolean.valueOf(cat.getPersonalizarDocumento());

							if (esPersonalizado) {
								
								log.info(raizLog + "El documento [" + nombreOriginal + "] SI se va a personalizar");
								log.info(raizLog + "parent:" + parentIdAdjuntos);
								try {
									//listDataHandler = DocumentoUtils.personalizarDocumentoCartaMasivo(
										//	globalProperties.getRutaTemp() + nombreOriginal, cat,
											//globalProperties.getRutaTemp() + nombreOriginal, mapPropsOfficeDoc);
									listDataHandler = DocumentoUtils.personalizarDocumentoCartaMasivo(
											globalProperties.getRutaTemp() + nombreOriginal, cat,
											globalProperties.getRutaTemp() + nombreOriginal);
								} catch (IOException e) {
									log.info(raizLog + "Ocurrio un error personalizando el documento:" + nombreOriginal
											+ ", error:" + e);
									e.printStackTrace();
								}
								int count = 0;
								List<Long> listIdDocs = new ArrayList<>();

								Node nodoDocOriginal = ContenidoDocumento.getDocumentoById(
										autenticacionCS.getUserSoapHeader(conexionUsuario), idDocOriginal,
										wsdlsProps.getDocumento());

								Metadata metadataDocOriginal = nodoDocOriginal.getMetadata();

								AttributeGroup categoriaCorrespondencia = null;
								categoriaCorrespondencia = metadataDocOriginal.getAttributeGroups().stream()
										.filter(unaCategoria -> unaCategoria.getDisplayName()
												.equals(categoriaProps.getCorrespondencia().getNombre()))
										.findFirst().get();

								List<String> listNombresDocsPersonalizados = new ArrayList<>();
								for (DataHandler dataHandler : listDataHandler) {
									try {

										log.info(raizLog + "INICIO crear doc personalizado en Content");
										Map<String, Object> metadatosCatDoc = new HashMap<String, Object>();
										Long idDoc;

										String nombreDocumento = numeroRadicado + "_"
												+ ((destinos.get(count).getNombre() != null
														&& !destinos.get(count).getNombre().isEmpty())
																? destinos.get(count).getNombre()
																: destinos.get(count).getDependencia())
												+ "_" + count;
																				
										nombreDocumento = SSMUtils.truncarString(nombreDocumento, Consts.TAMANO_MAXIMO_NOMBRE_DOCUMENTO_ORIGINAL);
																			
										if (tipoDocumentalCatDocumento != null && !tipoDocumentalCatDocumento.isEmpty()
												&& serieCatDocumento != null && !serieCatDocumento.isEmpty()) {


											metadatosCatDoc.put("Tipo documental", tipoDocumentalCatDocumento);
											metadatosCatDoc.put("Serie", serieCatDocumento);

											log.info(raizLog + "Crear documento con categoria documento");

											idDoc = ContenidoDocumento.crearDocumentoCatDocumento(
													autenticacionCS.getUserSoapHeader(conexionUsuario), idDocOriginal,
													nombreDocumento + ".docx", dataHandler, categoriaDocProps.getId(),
													categoriaDocProps.getNombreCategoria(), metadatosCatDoc, false, null,
													wsdlsProps.getDocumento());
										} else {

											log.info(raizLog + "Crear documento sin categoria documento");

											idDoc = ContenidoDocumento.crearDocumento(
													autenticacionCS.getUserSoapHeader(conexionUsuario),
													nombreDocumento + ".docx", dataHandler, parentIdAdjuntos,
													wsdlsProps.getDocumento());

										}

										log.info(raizLog + "FIN crear doc personalizado en Content");

										log.info(raizLog + "INICIO agregar cat correspondencia a doc personalizado");

										Node nodoDocPerso = ContenidoDocumento.getDocumentoById(
												autenticacionCS.getUserSoapHeader(conexionUsuario), idDoc,
												wsdlsProps.getDocumento());

										Metadata metadataDocPerso = nodoDocPerso.getMetadata();

										metadataDocPerso.getAttributeGroups().add(categoriaCorrespondencia);

										nodoDocPerso.setMetadata(metadataDocPerso);

										ContenidoDocumento.actualizarMetadataDocumento(
												autenticacionCS.getUserSoapHeader(conexionUsuario), idDoc,
												metadataDocPerso, wsdlsProps.getDocumento());

										listIdDocs.add(idDoc);
										
										destinos.get(count).setIdDocumento(idDoc);

										log.info(raizLog + "FIN agregar cat correspondencia a doc personalizado");

										log.info(raizLog + "INICIO cambiar nombre documento personalizado");

										String nombreDoc = ContenidoDocumento.obtenerNombreDocumento(
												autenticacionCS.getUserSoapHeader(conexionUsuario), idDoc,
												wsdlsProps.getDocumento());										
										
										log.info(raizLog + "El nombre del doc personalizado ANTES:" + nombreDoc);

										ContenidoDocumento.cambiarNombreDocumento(
												autenticacionCS.getUserSoapHeader(conexionUsuario), idDoc,
												nombreDocumento + ".pdf", wsdlsProps.getDocumento());

										String nombreDocDespues = ContenidoDocumento.obtenerNombreDocumento(
												autenticacionCS.getUserSoapHeader(conexionUsuario), idDoc,
												wsdlsProps.getDocumento());										
										
										destinos.get(count).setNombreDocumento(nombreDocDespues);
										
										log.info(raizLog + "El nombre del doc personalizado DESPUES:"
												+ nombreDocDespues);

										log.info(raizLog + "FIN cambiar nombre documento personalizado");

										listNombresDocsPersonalizados.add(nombreDoc);

									} catch (SOAPException e) {
										e.printStackTrace();
									} catch (IOException e) {
										e.printStackTrace();
									}
									count++;
									
									//Vamaya: Se modifica para que borre el archivo correctamente -- Esta implementación añadida se comentó, pero se debe revisar el borrado de los documentos en el servidor para futuras correciones
									//String workId = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION,String.class);
									//String nombreDoc = globalProperties.getRutaTemp() + workId + "-" + idDocOriginal + "-" + dataHandler.getName();
									String nombreDoc = globalProperties.getRutaTemp() + dataHandler.getName();									
									log.info(raizLog + "archivo a eliminar: " +  nombreDoc);
									File doc = new File(nombreDoc);
									log.info(raizLog +"Archivo eliminado:" + doc.delete() );
																		
								}
								String idList = listIdDocs.toString();
								String cadenaListIdDocs = String.join(",", idList);

								cadenaListIdDocs = cadenaListIdDocs.replace("[", "{");
								cadenaListIdDocs = cadenaListIdDocs.replace("]", "}");

								cadenaListIdDocs = StringUtils.deleteWhitespace(cadenaListIdDocs);
								log.info(raizLog + "LA CADENAAAA DE IDS PERSONALIZADOS:" + cadenaListIdDocs);

								cat.setDestinatarios(destinos);
								
								context.getExtendedState().getVariables().put(Consts.METADATOS_PLANTILLA, cat);
								
								context.getExtendedState().getVariables().put(
										Consts.LISTA_NOMBRES_DOCS_PERSONALIZADOS_CARTA, listNombresDocsPersonalizados);
								context.getExtendedState().getVariables().put(Consts.LISTA_ID_DOCS_PERSONALIZADOS_CARTA,
										listIdDocs);
								context.getExtendedState().getVariables()
										.put(Consts.CADENA_LISTA_ID_DOCS_PERSONALIZADOS_CARTA, cadenaListIdDocs);
								context.getExtendedState().getVariables().put(Consts.ES_PERSONALIZADO, esPersonalizado);
							} else {
								log.info(raizLog + "El documento " + nombreOriginal + " NO se va a personalizar");
							}

						} else {

							log.info(raizLog + "El documento " + nombreOriginal
									+ " NO se va a personalizar porque la tipologia no es Carta");
						}

					} catch (Exception e) {
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						log.error(raizLog + "ErrorCorrespondencia personalizarDocumento generarDocumentoSticker " + e , e);
					}

				}
				log.info(raizLog + "-------------FIN estado personalizarDocumento-------------");
				
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}
			};

		}

		*/




		/**
		 * Metodo que permite obtener la respuesta del primer Centro de Distribucion CDD
		 * por donde empieza la el envio antes de distribuir la Carta
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> obtenerRespuestaDistribuirCarta() {

			return (context) -> {

				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " obtenerRespuestaDistribuirCarta");

				log.info(raizLog
						+ "-------------------------ESTADO obtenerRespuestaDistribuirCarta -------------------------------");
				
				if (!context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class)) {

									
					String resultado = context.getMessageHeaders().get(Consts.RESULTADO_DISTRIBUIR_RDI_RDE, String.class);
					context.getExtendedState().getVariables().put(Consts.RESULTADO_DISTRIBUIR_RDI_RDE, resultado);
					long idDocOriginal = context.getExtendedState().get(Consts.ID_DOC_RADICACION, Long.class);
					String tipoWfDoc = context.getExtendedState().get(Consts.TIPO_WF_DOCUMENTO, String.class);
					
					Long idRuta = context.getMessageHeaders().get(Consts.ID_RUTA, Long.class);
					@SuppressWarnings("unchecked") //Vamaya: Se aña el supress warnings
					Map<Long, RespuestaEntrega> distribucionRuta =  context.getExtendedState().get(Consts.DISTRIBUCION_RUTA, HashMap.class);
					
					MetadatosPlantilla cat = (MetadatosPlantilla) context.getExtendedState().get(Consts.METADATOS_PLANTILLA,
							Object.class);
						
					long idSolicitante = context.getExtendedState().get(Consts.ID_SOLICITANTE, Long.class);

					
					@SuppressWarnings("unchecked")
					List<Empleado> destinatarios = (List<Empleado>) context.getExtendedState().get(Consts.DESTINATARIOS,
							Object.class);
	
					String rolActualDistribucion = context.getMessageHeaders().get(Consts.ROL_ACTUAL_DISTRIBUIR_RDI_RDE,
							String.class);
					
					if(rolActualDistribucion.indexOf('(') > -1 ){
						
						String[] cadena = rolActualDistribucion.split(" ");
						rolActualDistribucion = cadena[cadena.length-1];
						rolActualDistribucion = rolActualDistribucion.replaceAll("\\(", "");
						rolActualDistribucion = rolActualDistribucion.replaceAll("\\)", "");

					}
					
					log.info(raizLog + "ROl Distribucuion trabajado " + rolActualDistribucion );
					
					context.getExtendedState().getVariables().put(Consts.ROL_ACTUAL_DISTRIBUIR_RDI_RDE,
							rolActualDistribucion);
					User user = null;
					try {
						 user =  UsuarioCS.getUsuario(autenticacionCS.getAdminSoapHeader(), idSolicitante, wsdlsProps.getUsuario());
					} catch (SOAPException e) {
						log.error(raizLog + "ErrorCorrespondencia obtenerRespuestaDistribuirCarta getUsuario " + e);							
						cargarError(context, e);
						//context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.OBTENER_RESPUESTA_DISTRIBUIR_CARTA);
						context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.OBTENER_RESPUESTA_DISTRIBUIR_CARTA);
						context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
					} catch (IOException e) {
						log.error(raizLog + "ErrorCorrespondencia obtenerRespuestaDistribuirCarta getUsuario " + e);							
						cargarError(context, e);
						context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.OBTENER_RESPUESTA_DISTRIBUIR_CARTA);
						context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
					}
					
					String rutaCompleta = context.getExtendedState().get(Consts.RUTA_COMPLETA, String.class);
					
					if(user != null)
						rutaCompleta = user.getName() + ";"+ rutaCompleta;
					
					int totalConfirmados = (context.getExtendedState().get(Consts.NUMERO_TOTAL_DESTINATARIOS_CONFIRMADOS, Integer.class)!= null ? context.getExtendedState().get(Consts.NUMERO_TOTAL_DESTINATARIOS_CONFIRMADOS, Integer.class) : 0 );
					int totalRechazados = (context.getExtendedState().get(Consts.NUMERO_TOTAL_DESTINATARIOS_RECHAZADOS, Integer.class)!= null ? context.getExtendedState().get(Consts.NUMERO_TOTAL_DESTINATARIOS_RECHAZADOS, Integer.class) : 0 );
					int totalDestinatarios = destinatarios.size();
					
					
					log.info(raizLog + "Cantidad de destinatarios son " + totalDestinatarios + " total rechazados: " + totalRechazados + " total confirmados " + totalConfirmados);
					
					List<String> rutaCompletaSplit = Arrays.asList(rutaCompleta.split(";"));
					String rolSiguiente = rutaCompletaSplit.get(1);
					int sizeRuta = rutaCompletaSplit.size();
					Boolean finalizoDistribucion = false;
					
					
					log.info(raizLog + "RUTA COMPLETA:" + rutaCompleta);
					
					if (cat.getTipoEnvio() != null && !cat.getTipoEnvio().equalsIgnoreCase("Mensajeria") || cat.getEsImpresionArea() || tipoWfDoc.equalsIgnoreCase("Confidencial") ) {
						
					
						for (String rol : rutaCompletaSplit) {
							
							log.info(raizLog + "rol:" + rol);
							
							if (rol.equalsIgnoreCase(rolActualDistribucion)) {
								
								int poscicion = rutaCompletaSplit.indexOf(rolActualDistribucion);
		
							if (resultado.equalsIgnoreCase(estadosProps.getConfirmado()) && (poscicion != sizeRuta - 1)) {
									rolSiguiente = rutaCompletaSplit.get(poscicion + 1);
									
								}
		
							else if (resultado.equalsIgnoreCase(estadosProps.getConfirmado()) && (poscicion == sizeRuta - 1)) {
		
									finalizoDistribucion = true;
									rolSiguiente = "XXX";
									totalConfirmados++;
									context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS_CONFIRMADOS, totalConfirmados);
									if(totalConfirmados== totalDestinatarios) {
										
										try {
											actualizarEstadoCorrespondencia(raizLog, idDocOriginal,estadosProps.getConfirmado());
										} catch (Exception e) {
											log.error(raizLog + "ErrorCorrespondencia obtenerRespuestaDistribuirCarta actualizarEstadoCorrespondencia " + e);							
											cargarError(context, e);
											context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.OBTENER_RESPUESTA_DISTRIBUIR_CARTA);
											context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
										}
										
										context.getStateMachine().sendEvent(Events.FINALIZAR);
										
									}
		
								}
		
							else if (!resultado.equalsIgnoreCase(estadosProps.getConfirmado())) {
									if (poscicion != 0) {
										rolSiguiente = rutaCompletaSplit.get(poscicion - 1);
										finalizoDistribucion = false;
										
									} else {
										rolSiguiente = "";
										finalizoDistribucion = true;
										
										totalRechazados++;
										context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS_RECHAZADOS, totalRechazados);
										
										if(totalRechazados == totalDestinatarios) {
											
											try {
												actualizarEstadoCorrespondencia(raizLog, idDocOriginal,estadosProps.getRechazo());
											} catch (Exception e) {
												log.error(raizLog + "ErrorCorrespondencia obtenerRespuestaDistribuirCarta actualizarEstadoCorrespondencia " + e);							
												cargarError(context, e);
												context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.OBTENER_RESPUESTA_DISTRIBUIR_CARTA);
												context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
											}
									
											context.getStateMachine().sendEvent(Events.FINALIZAR);
											
										}else {
											
											try {
												actualizarEstadoCorrespondencia(raizLog, idDocOriginal,estadosProps.getRechazadoAlgunos());
											} catch (Exception e) {
												log.error(raizLog + "ErrorCorrespondencia obtenerRespuestaDistribuirCarta actualizarEstadoCorrespondencia " + e);							
												cargarError(context, e);
												context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.OBTENER_RESPUESTA_DISTRIBUIR_CARTA);
												context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
											}
									
											context.getStateMachine().sendEvent(Events.FINALIZAR);
										}
										
											
		
									}
		
								} else {
		
								}
								log.info(raizLog + "La posicion del rol que encontro es:" + poscicion);
		
							}
						}
					}else {
						
						if(rolActualDistribucion.contains(globalProperties.getNombreCourrier())){
							
							if(resultado.equalsIgnoreCase(estadosProps.getConfirmado())) {
								rolSiguiente = "";
								finalizoDistribucion = true;
							}else {
								rolSiguiente = Consts.ID_SOLICITANTE;
							}
							

						}else {
							if(resultado.equalsIgnoreCase(estadosProps.getConfirmado())) {
								
								rolSiguiente = rutaCompletaSplit.get(sizeRuta-1);
								
							}else {
								rolSiguiente = "";
								finalizoDistribucion = true;
							}
							
						}
						
					}
						
					// ROL_SIGUIENTE_DISTRIBUIR_RDI_RDE
					
					RespuestaEntrega r = new RespuestaEntrega();
					r.setRolsigueinte(rolSiguiente);
					r.setFinalizacion(finalizoDistribucion);
					r.setPasoCurrier(false);
					
					distribucionRuta.put(idRuta, r);
					
					context.getExtendedState().getVariables().put(Consts.DISTRIBUCION_RUTA, distribucionRuta);
					
//					context.getExtendedState().getVariables().put(Consts.ROL_SIGUIENTE_DISTRIBUIR_RDI_RDE, rolSiguiente);
	
//					context.getExtendedState().getVariables().put(Consts.FINALIZO_DISTRIBUCION_RDI_RDE,
//							finalizoDistribucion);
//					context.getExtendedState().getVariables().put(Consts.PASO_POR_COURRIER, false);
//	
					if (!finalizoDistribucion)
						context.getStateMachine().sendEvent(Events.VOLVER_ESPERAR_DISTRIBUIR_CARTA);
	
					log.info(raizLog + "La respuesta respuesta:" + resultado + " codigoRuta:" + " rolActualDistribucion:"
							+ rolActualDistribucion + " rolSiguiente:" + rolSiguiente);
				}
				
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}
			};

		}

		/**
		 * Metodo que permite obtener la respuesta del primer Centro de Distribucion CDD
		 * por donde empieza la el envio antes de distribuir la Carta
		 * 
		 * @return
		 */
		@SuppressWarnings("unused")
		@Bean
		public Action<States, Events> obtenerRespuestaDistribuirCddCarta() {

			return (context) -> {

				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " obtenerRespuestaDistribuirCddCarta");

				log.info(raizLog
						+ "-------------------------ESTADO obtenerRespuestaDistribuirCddCarta -------------------------------");
				
				if (!context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class)) {

				Boolean respuestaCdd =  (context.getExtendedState().get(Consts.RESPUESTA_CDD_DISTRIBUIR_RDI_RDE, Boolean.class)==null) 
				? context.getMessageHeaders().get(Consts.RESPUESTA_CDD_DISTRIBUIR_RDI_RDE,	Boolean.class)
				: context.getExtendedState().get(Consts.RESPUESTA_CDD_DISTRIBUIR_RDI_RDE, Boolean.class);

				String cddOrigen = context.getExtendedState().get(Consts.CDD_ORIGEN_CARTA, String.class);
				String pcrOrigen = context.getExtendedState().get(Consts.PCR_ORIGEN_CARTA, String.class);
				context.getExtendedState().getVariables().put(Consts.RESPUESTA_CDD_DISTRIBUIR_RDI_RDE, respuestaCdd);

				if (respuestaCdd) {

					@SuppressWarnings("unchecked")
					List<Empleado> destinatarios = (List<Empleado>) context.getExtendedState().get(Consts.DESTINATARIOS,
							Object.class);

					@SuppressWarnings("unchecked")
					List<Empleado> copias = (List<Empleado>) context.getExtendedState().get(Consts.COPIAS,
							Object.class);

					long idDocOriginal = context.getExtendedState().get(Consts.ID_DOC_RADICACION, Long.class);

					String tipoWfDoc = context.getExtendedState().get(Consts.TIPO_WF_DOCUMENTO, String.class);

					String numeroRadicado = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO,
							String.class);
					

					String workIdText = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);

					String asunto = context.getExtendedState().get(Consts.ASUNTO, String.class);

					String conexionUsuarioCS = context.getExtendedState().get(Consts.CONEXION_CS_SOLICITANTE, String.class);

					MetadatosPlantilla cat = (MetadatosPlantilla) context.getExtendedState().get(Consts.METADATOS_PLANTILLA,
							Object.class);

					String fechaRadicacion = context.getExtendedState().get(Consts.FECHA_RADICACION, String.class);

					long idSolicitante = context.getExtendedState().get(Consts.ID_SOLICITANTE, Long.class);
					
//					long idSticker = context.getExtendedState().get(Consts.ID_DOC_STICKER_CARTA, Long.class);
					
					String tipologia = (context.getExtendedState().get(Consts.TIPOLOGIA_MEMO_CARTA, String.class) == null ? context.getMessageHeaders().get(Consts.TIPOLOGIA_RDI_RDE, String.class) : context.getExtendedState().get(Consts.TIPOLOGIA_MEMO_CARTA, String.class));

//					String nombreDependenciaFirmante = cat.getFirmantes().get(0).getDependencia();

					@SuppressWarnings("unchecked")
					List<Long> listIdDocsPersonalizados = (List<Long>) context.getExtendedState()
							.get(Consts.LISTA_ID_DOCS_PERSONALIZADOS_CARTA, Object.class);

					@SuppressWarnings("unchecked")
					List<Long> idDocsCopiasCompulsada = (List<Long>) context.getExtendedState()
							.get(Consts.LISTA_ID_DOCS_COPIAS_COMPULSADAS, Object.class);
					
					//Se actualiza el parent id de la carpeta oroginal en caso de que el usuario haya movido el documento
					try {
						actualizarIdParentOriginal(context, autenticacionCS.getAdminSoapHeader(), wsdlsProps.getDocumento());
					} catch (SOAPException | IOException e) {
						log.warn("No se pudo actualizar el id de la carpeta" + e.getMessage());
					}
					
					Long parentIdCarpetaOriginal = context.getExtendedState().get(Consts.PARENT_ID_ADJUNTOS_MEMO_CARTA,
							Long.class);
					
					String nombreDocOriginal = context.getExtendedState().get(Consts.NOMBRE_DOCUMENTO_ORIGINAL,
							String.class);
					

					List<Long> listIdDocsAdjuntos = new ArrayList<>();

					long mapId = workflowProps.getDistribucionCarta().getId();
					if (tipoWfDoc.equalsIgnoreCase("Confidencial")) {
						mapId = workflowProps.getDistribucionCarta().getIdConfidencial();
						log.info(raizLog + "Es confidencial CARTA, WF distribucion "+ mapId);

					}

					Member grupo = null;
					Long idGrupo = null;
					
					
					String rutaCompletaTexto = pcrOrigen + ";" + cddOrigen + ";";
					log.info(raizLog + "El origen que se esta consultando es:" + cddOrigen);
					CentroDistribucion centroDistribucionOrigen = centroDistribucionRepository.findByNombre(cddOrigen);
					log.info(raizLog + "El destinatario que se esta consultando es: EXTERNO");
					CentroDistribucion centroDistribucionDestino = centroDistribucionRepository.findByNombre("EXTERNO");

//					RutaDistribucion rutaDistribucion = rutaDistribucionRepository
//							.findByCentroDistribucionOrigenAndCentroDistribucionDestino(centroDistribucionOrigen,
//									centroDistribucionDestino);
					
					List<RutaDistribucion> rutasDB = rutaDistribucionRepository
							.findByCentroDistribucionOrigenAndCentroDistribucionDestino(centroDistribucionOrigen, centroDistribucionDestino);
								
					RutaDistribucion rutaDistribucion = null;
									
					if(rutasDB != null && !rutasDB.isEmpty()) {
						
						rutaDistribucion = SSMUtils.getRuta(rutasDB, tipologia , globalProperties);
						
						log.info(raizLog + "Si existe ruta en BD para origen:" + cddOrigen + " y destino: EXTERNO");
					}

					
					List<PasoDistribucion> pasosRuta = rutaDistribucion.getPasos();
					
					
					String pasoSiguiente = "";
					boolean paso = true;
					for (PasoDistribucion pasoDistribucion : pasosRuta) {
						String nombrePaso = pasoDistribucion.getCentroDistribucion().getNombre();
						rutaCompletaTexto = rutaCompletaTexto +  nombrePaso + ";";

						if (paso)
							pasoSiguiente = nombrePaso;
							paso = false;
					}

					log.info(raizLog + "La ruta completa Carta es:" + rutaCompletaTexto);
					
					if(pasoSiguiente.equals("")) {
						pasoSiguiente = cddOrigen;
					}
					
					context.getExtendedState().getVariables().put(Consts.RUTA_COMPLETA,
							rutaCompletaTexto);

					try {
						grupo = UsuarioCS.getMemberByLoginName(autenticacionCS.getAdminSoapHeader(),
								pasoSiguiente, wsdlsProps.getUsuario());

						idGrupo = grupo.getID();
						
						context.getExtendedState().getVariables().put(Consts.ROL_SIGUIENTE_DISTRIBUIR_RDI_RDE, pasoSiguiente);
						context.getExtendedState().getVariables().put(Consts.FINALIZO_DISTRIBUCION_RDI_RDE, false);
						
					} catch (SOAPException | IOException  e) {
						log.error(raizLog + "ErrorCorrespondencia obtenerRespuestaDistribuirCddCarta getGrupoByNombre " + e);
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
						//context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.OBTENER_RESPUESTA_DISTRIBUIR_CDD_CARTA);
						context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.OBTENER_RESPUESTA_DISTRIBUIR_CDD_CARTA);
					}

					long idCopiaCC = 0L;
					long idCopiaCCD = 0L;
					
					String nombreCopiaCompulsadaOriginal = "(CC) " + nombreDocOriginal;

					try {
						
						idCopiaCC = ContenidoDocumento.obtenerDocumentoPorNombre(autenticacionCS.getAdminSoapHeader(), parentIdCarpetaOriginal, nombreCopiaCompulsadaOriginal, wsdlsProps.getDocumento());
						
						log.info(raizLog +"ID copia complusada: " + idCopiaCC);
											
					} catch (SOAPException | IOException | InterruptedException  e) {
						log.error(raizLog + "ErrorCorrespondencia obtenerRespuestaDistribuirCddCarta obtenerDocumentoPorNombre" + e);	
						cargarError(context, e);
						context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.OBTENER_RESPUESTA_DISTRIBUIR_CDD_CARTA);
						context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
					} 
					
					int posicion=0;
					for (Empleado destino : destinatarios) {
						
							String titulo = "Distribución Carta - " + numeroRadicado;					 
							String[] listaDestinos = destino.getRol().split(",");
							String direccionFinal = "";
							
							for(int i = 1;i< listaDestinos.length;i++) {
								direccionFinal += listaDestinos[i];
							}
							
							titulo  += direccionFinal;

						
						Long idDocumentoAdjunto = new Long(0);
//						listIdDocsAdjuntos.add(idDocOriginal);
						
						idDocumentoAdjunto = idCopiaCC;
						String destinoPer = destino.getRol();
						if (destino.getRol().contains("@")) {
							if (Boolean.valueOf(cat.getPersonalizarDocumento())) {
								
								if(destino.getIdDocumento() != 0 && destino.getNombreDocumento() != null){
									
									String[] listadestino = destino.getRol().split(",");
									destinoPer = listadestino[0];
									
									nombreCopiaCompulsadaOriginal = "(CC) " + converUTF8(destino.getNombreDocumento());
									
									log.info(raizLog +"nombre copia CC: " + nombreCopiaCompulsadaOriginal + " capeta: " + parentIdCarpetaOriginal);
									
									try {
										idCopiaCCD = ContenidoDocumento.obtenerDocumentoPorNombre(
												autenticacionCS.getAdminSoapHeader(), parentIdCarpetaOriginal, nombreCopiaCompulsadaOriginal, wsdlsProps.getDocumento());
										
										idDocumentoAdjunto = idCopiaCCD;
										log.info(raizLog +"ID copia complusada: " + idCopiaCCD);
										
									} catch (SOAPException | IOException | InterruptedException e) {
										log.error(raizLog + "ErrorCorrespondencia obtenerRespuestaDistribuirCddCarta obtenerDocumentoPorNombre" + e);	
										cargarError(context, e);
										context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.OBTENER_RESPUESTA_DISTRIBUIR_CDD_CARTA);
										context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
									} 									
								}
								
							}						
							
						}
						
						log.info(raizLog + "destino enviar: " + destinoPer);
						
						Map<String, ValorAtributo> atributos = new HashMap<String, ValorAtributo>();
						
						asunto = SSMUtils.truncarString(asunto, Consts.TAMANO_MAXIMO_CS_ASUNTO);
						String nombreDestino = SSMUtils.truncarString(destino.getNombre(), Consts.TAMANO_MAXIMO_ATRIBUTOS_DESTINO);
						//String cargoDestino = SSMUtils.truncarString(destino.getCargo(), Consts.TAMANO_MAXIMO_ATRIBUTOS_DESTINO);
						String entidadDestino = SSMUtils.truncarString(destino.getDependencia(), Consts.TAMANO_MAXIMO_ATRIBUTOS_DESTINO);

						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getNumeroRadicado(), new ValorAtributo(numeroRadicado, TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getTipologia(), new ValorAtributo("CA", TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getAsunto(), new ValorAtributo(asunto, TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getConexionCs(), new ValorAtributo(conexionUsuarioCS, TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getEsHilos(), new ValorAtributo("true", TipoAtributo.STRING));
//						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getNombreDependencia(),
//								new ValorAtributo(nombreDependenciaFirmante, TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getSolicitante(), new ValorAtributo(idSolicitante, TipoAtributo.USER));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getFechaRadicacion(), new ValorAtributo(fechaRadicacion, TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getWorkid(), new ValorAtributo(workIdText, TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getDestino(), new ValorAtributo(destinoPer, TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getEsFisico(), new ValorAtributo((destino.getRol().contains("@")) ? false : true,
								TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getRolDestino(), new ValorAtributo(idGrupo, TipoAtributo.USER));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getEsCorreoCertificado(),
								new ValorAtributo(cat.getEsCorreoCertificado(), TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getIdDocumentoAdjunto(),
								new ValorAtributo(idDocumentoAdjunto.toString(), TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getNombreDestino(),
								new ValorAtributo((destino.getNombre() != null && !destino.getNombre().isEmpty())
										? nombreDestino
										: "  ", TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getEntidadDestino(),
								new ValorAtributo(
										(destino.getDependencia() != null && !destino.getDependencia().isEmpty())
												? entidadDestino
												: "  ",
										TipoAtributo.STRING));
						atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getEsMensajeria(), new ValorAtributo(true, TipoAtributo.STRING));
						atributos.put("_pasoCDD", new ValorAtributo(true, TipoAtributo.STRING));
						

						log.info(raizLog + "INICIO WF Distribucion carta Hilos:");
						
						int distribuido=0;
						try {
							
							distribuido = context.getExtendedState().get(Consts.DISTRIBUIDO, Integer.class) == null ? 0 : context.getExtendedState().get(Consts.DISTRIBUIDO, Integer.class);
							
							log.info(raizLog + "distribuido: " + distribuido + " posicion: " + posicion);
							
							
							if(distribuido==posicion) {
																							
								long idFlujo = Workflow.iniciarWorkflowAtributosAdjuntos(
										autenticacionCS.getAdminSoapHeader(), mapId, titulo, atributos,
										listIdDocsAdjuntos, wsdlsProps.getWorkflow());
								
								int suma = posicion;
								context.getExtendedState().getVariables().put(Consts.DISTRIBUIDO, ++suma);
								
								log.info(raizLog + "FIN WF Distribucion carta Hilos - Flujo Iniciado: " + idFlujo);
								
							}

						} catch (NumberFormatException e) {
							cargarError(context, e);
							context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
							log.error(raizLog + "ErrorCorrespondencia obtenerRespuestaDistribuirCddCarta iniciarWorkflowAtributosAdjuntos " + e, e);
						} catch (NoSuchElementException | SOAPException | IOException | WebServiceException  e) {
							log.error(raizLog + "ErrorCorrespondencia obtenerRespuestaDistribuirCddCarta iniciarWorkflowAtributosAdjuntos " + e , e);
							cargarError(context, e);
							context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
							context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.OBTENER_RESPUESTA_DISTRIBUIR_CDD_CARTA);

						}

							context.getExtendedState().getVariables().put(Consts.DESTINO_INDIVIDUAL_CARTA,
						
								(!destino.getNombre().isEmpty() ? destino.getNombre() : destino.getDependencia()));
						
							posicion++;
							try {
								persistContexto(context);
							} catch (Exception e) {
								log.error("Error operacion", e);
							}
						
						}
						
					}

				}
				

			};

		}

		/**
		 * Metodo que permite obtener la respuesta del primer Centro de Distribucion CDD
		 * por donde empieza la el envio antes de distribuir
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> obtenerRespuestaDistribuirCddRdiRde() {

			return (context) -> {

				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " obtenerRespuestaDistribuirCddRdiRde");

				log.info(raizLog
						+ "-------------------------ESTADO obtenerRespuestaDistribuirCddRdiRde -------------------------------");

				if (!context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class)) {
					
					Boolean respuestaCdd;
					if(context.getExtendedState().get(Consts.RESPUESTA_CDD_DISTRIBUIR_RDI_RDE, Boolean.class)== null) {

						 respuestaCdd = context.getMessageHeaders().get(Consts.RESPUESTA_CDD_DISTRIBUIR_RDI_RDE,
								Boolean.class);
						
						 context.getExtendedState().getVariables().put(Consts.RESPUESTA_CDD_DISTRIBUIR_RDI_RDE,
									respuestaCdd);
						
					}else {
						
						 respuestaCdd = context.getExtendedState().get(Consts.RESPUESTA_CDD_DISTRIBUIR_RDI_RDE, Boolean.class);
					}
					
					
					String origenRdiRde = context.getExtendedState().get(Consts.ORIGEN_RDI_RDE, String.class);
					String origenPcr = context.getExtendedState().get(Consts.ORIGEN_RDI_RDE_PCR, String.class);
					Boolean esSucursal = context.getExtendedState().get(Consts.ES_SUCURSAL, Boolean.class);
					String tipologia = context.getExtendedState().get(Consts.TIPOLOGIA_RDI_RDE, String.class);
					String workId = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);
					String asunto = context.getExtendedState().get(Consts.ASUNTO_RDI_RDE, String.class);
					String numeroRadicado = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO,
							String.class);

					@SuppressWarnings("unchecked")
					List<Destino> listaDestinos = (List<Destino>) context.getExtendedState()
							.get(Consts.LISTA_DESTINOS_RDI_RDE, Object.class);
					Long idSolicitante = context.getExtendedState().get(Consts.ID_SOLICITANTE, Long.class);
					long idDocOriginal = context.getExtendedState().get(Consts.ID_DOC_RADICACION, Long.class);

					@SuppressWarnings("unchecked")
					List<String> listaDestinatarios = (List<String>) context.getExtendedState()
							.get(Consts.LIST_CDD_DESTINO_RDI_RDE, Object.class);
					@SuppressWarnings("unchecked")
					List<String> listaPcrDestino = (List<String>) context.getExtendedState()
							.get(Consts.LIST_PCR_DESTINO_RDI_RDE, Object.class);

					log.info(raizLog + "Numero Radicado:" + numeroRadicado);

					Member grupo = null;
					Long idGrupo = null;

					String conexionUsuarioCS = context.getExtendedState().get(Consts.CONEXION_CS_SOLICITANTE, String.class);
					long mapId = workflowProps.getDistribucionRdiRde().getId();


					log.info(raizLog + "La respuesta del CDD:" + respuestaCdd + " origenRdiRde:" + origenRdiRde + " ");

					if (respuestaCdd) {

						log.info(raizLog + "Aca se inician todos hilos ");
						log.info(raizLog + "Distribucion MENSAJERIA");

						if (tipologia.equalsIgnoreCase("RDI")) {

							log.info(raizLog + "Resultado CDD Distribucion RDI");

							Long llave = (long) 1;
							int posicion = 0;
							for (String destinatario : listaDestinatarios) {

								String titulo = "Distribución RDI/RDE - " + numeroRadicado + " - "
										+ listaDestinos.get(posicion).getDependenciaDestino();

								log.info(raizLog + "Destinatario CDD:" + destinatario);
								if (!origenRdiRde.equalsIgnoreCase(destinatario)) {

									log.info(raizLog + "El origen que se esta consultando es:" + origenRdiRde);
									CentroDistribucion centroDistribucionOrigen = centroDistribucionRepository
											.findByNombre(origenRdiRde);
									log.info(raizLog + "El destinatario que se esta consultando es:" + destinatario);
									CentroDistribucion centroDistribucionDestino = centroDistribucionRepository
											.findByNombre(destinatario);

//									RutaDistribucion rutaDistribucion = rutaDistribucionRepository
//											.findByCentroDistribucionOrigenAndCentroDistribucionDestino(
//													centroDistribucionOrigen, centroDistribucionDestino);
									
									List<RutaDistribucion> rutasDB = rutaDistribucionRepository
											.findByCentroDistribucionOrigenAndCentroDistribucionDestino(centroDistribucionOrigen, centroDistribucionDestino);
												
									RutaDistribucion rutaDistribucion = null;
													
									if(rutasDB != null && !rutasDB.isEmpty()) {
										rutaDistribucion = SSMUtils.getRuta(rutasDB, tipologia , globalProperties);
									}

									List<PasoDistribucion> pasosRuta = rutaDistribucion.getPasos();

									String pasoSiguiente = "";

									if (esSucursal) {

										pasoSiguiente = origenRdiRde;
									} else {

										if (pasosRuta != null && !pasosRuta.isEmpty()) {
											pasoSiguiente = pasosRuta.get(0).getCentroDistribucion().getNombre();
										} else {

											pasoSiguiente = destinatario;
										}

									}

									try {
										grupo = UsuarioCS.getGrupoByNombre(
												autenticacionCS.getUserSoapHeader(conexionUsuarioCS), pasoSiguiente,
												wsdlsProps.getUsuario());
										idGrupo = grupo.getID();
										log.info(raizLog + "El grupo " + pasoSiguiente + " tiene ID= " + idGrupo);

									} catch (SOAPException |  IOException e) {
										log.error(raizLog + "ErrorCorrespondencia obtenerRespuestaDistribuirCddRdiRde getGrupoByNombre" + e);										cargarError(context, e);
										context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
									}
									Map<String, ValorAtributo> atributos = new HashMap<String, ValorAtributo>();
									atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getRolDestino(), new ValorAtributo(idGrupo, TipoAtributo.USER));
									atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getEstado(), new ValorAtributo("CONTINUAR", TipoAtributo.STRING));
									atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getNumeroRadicado(),
											new ValorAtributo(numeroRadicado, TipoAtributo.STRING));
									atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getTipologia(), new ValorAtributo(tipologia, TipoAtributo.STRING));
									atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getWorkid(), new ValorAtributo(workId, TipoAtributo.STRING));
									atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getCodigoRuta(), new ValorAtributo(llave, TipoAtributo.INTEGER));
									atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getAsunto(), new ValorAtributo(asunto, TipoAtributo.STRING));
									atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getCantidad(), new ValorAtributo(
											listaDestinos.get(posicion).getCantidad(), TipoAtributo.INTEGER));
									atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getDescripcion(), new ValorAtributo(
											listaDestinos.get(posicion).getDescripcionObjeto(), TipoAtributo.MULTILINE));
									atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getDestino(), new ValorAtributo(
											listaDestinos.get(posicion).getDependenciaDestino(), TipoAtributo.STRING));
									atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getRolOrigen(), new ValorAtributo(idSolicitante, TipoAtributo.USER));
									
									int distribuido = 0;
									
									try {
										
										distribuido = context.getExtendedState().get(Consts.DISTRIBUIDO, Integer.class) == null ? 0 : context.getExtendedState().get(Consts.DISTRIBUIDO, Integer.class);
										log.info(raizLog + "distribuido: " + distribuido + " posicion: "  + posicion + " llave: " + llave);
										
										if(distribuido==posicion) {
											
											log.info(raizLog + "INICIO iniciar WF distribucion RDI/RDE con rol siguiente:"
												+ pasoSiguiente);									
							
											Workflow.iniciarWorkflowConAtributos(
													autenticacionCS.getAdminSoapHeader(), mapId, titulo,
													atributos, wsdlsProps.getWorkflow());
											log.info(raizLog + "FIN iniciar WF distribucion RDI/RDE con rol siguiente:"
													+ pasoSiguiente);
											
											int suma = posicion;
											context.getExtendedState().getVariables().put(Consts.DISTRIBUIDO, ++suma);
										
										}

									} catch (NumberFormatException | NoSuchElementException e) {
										cargarError(context, e);
										context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
										log.error(raizLog + "ErrorCorrespondencia obtenerRespuestaDistribuirCddRdiRde iniciarWorkflowConAtributos " + e, e);										
									} catch ( SOAPException | IOException | WebServiceException  e) {
										log.error(raizLog + "ErrorCorrespondencia obtenerRespuestaDistribuirCddRdiRde iniciarWorkflowConAtributos " + e , e);										
										cargarError(context, e);
										//context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.OBTENER_RESPUESTA_DISTRIBUIR_CDD_RDI_RDE);
										context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.OBTENER_RESPUESTA_DISTRIBUIR_CDD_RDI_RDE);
										context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
									}
									log.info(raizLog + "El siguiente paso es:" + pasoSiguiente);

								} else {

									String pasoSiguiente = listaPcrDestino.get(posicion);
									log.info(raizLog + "El origen:" + origenPcr + " y el destino:" + pasoSiguiente
											+ " tienen el mismo CDD:" + origenRdiRde);
									log.info(raizLog + "El siguiente paso es:" + pasoSiguiente);

									try {
										grupo = UsuarioCS.getGrupoByNombre(
												autenticacionCS.getUserSoapHeader(conexionUsuarioCS), pasoSiguiente,
												wsdlsProps.getUsuario());
										idGrupo = grupo.getID();
										log.info(raizLog + "El grupo " + pasoSiguiente + " tiene ID= " + idGrupo);

									} catch (SOAPException | IOException  e) {
										log.error(raizLog + "ErrorCorrespondencia obtenerRespuestaDistribuirCddRdiRde getGrupoByNombre " + e , e);										
										cargarError(context, e);
										context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.OBTENER_RESPUESTA_DISTRIBUIR_CDD_RDI_RDE);
										context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);

									}
									Map<String, ValorAtributo> atributos = new HashMap<String, ValorAtributo>();
									atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getRolDestino(), new ValorAtributo(idGrupo, TipoAtributo.USER));
									atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getEstado(), new ValorAtributo("CONTINUAR", TipoAtributo.STRING));
									atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getNumeroRadicado(),
											new ValorAtributo(numeroRadicado, TipoAtributo.STRING));
									atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getTipologia(), new ValorAtributo(tipologia, TipoAtributo.STRING));
									atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getWorkid(), new ValorAtributo(workId, TipoAtributo.STRING));
									atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getCodigoRuta(), new ValorAtributo(llave, TipoAtributo.INTEGER));
									atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getAsunto(), new ValorAtributo(asunto, TipoAtributo.STRING));
									atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getCantidad(), new ValorAtributo(
											listaDestinos.get(posicion).getCantidad(), TipoAtributo.INTEGER));
									atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getDescripcion(), new ValorAtributo(
											listaDestinos.get(posicion).getDescripcionObjeto(), TipoAtributo.MULTILINE));
									atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getDestino(), new ValorAtributo(
											listaDestinos.get(posicion).getDependenciaDestino(), TipoAtributo.STRING));
									atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getRolOrigen(), new ValorAtributo(idSolicitante, TipoAtributo.USER));

									int distribuido = 0;
									
									try {
										
										distribuido = context.getExtendedState().get(Consts.DISTRIBUIDO, Integer.class) == null ? 0 : context.getExtendedState().get(Consts.DISTRIBUIDO, Integer.class);
										log.info(raizLog + "distribuido: " + distribuido + " posicion: "  + posicion + " llave: " + llave);
										
										if (distribuido == posicion) {
											
											log.info(raizLog
													+ "INICIO iniciar WF distribucion RDI/RDE con rol siguiente:"
													+ pasoSiguiente);

											Workflow.iniciarWorkflowConAtributos(autenticacionCS.getAdminSoapHeader(),
													mapId, titulo, atributos, wsdlsProps.getWorkflow());
											log.info(raizLog + "FIN iniciar WF distribucion RDI/RDE con rol siguiente:"
													+ pasoSiguiente);
											
											int suma = posicion;
											context.getExtendedState().getVariables().put(Consts.DISTRIBUIDO, ++suma);

										}
										
									} catch (NumberFormatException e) {
										cargarError(context, e);
										context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
										log.error(raizLog + "ErrorCorrespondencia obtenerRespuestaDistribuirCddRdiRde iniciarWorkflowConAtributos " + e , e);	
									} catch (NoSuchElementException | SOAPException | IOException | WebServiceException e) {
										log.error(raizLog + "ErrorCorrespondencia obtenerRespuestaDistribuirCddRdiRde iniciarWorkflowConAtributos " + e , e);										
										cargarError(context, e);
										context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.OBTENER_RESPUESTA_DISTRIBUIR_CDD_RDI_RDE);
										context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
									}

								}

								posicion++;
								llave++;

							}

							log.info(raizLog + "Se actualiza estado a:"
									+ estadosProps.getDistribuido());
							try {
								actualizarEstadoCorrespondencia(raizLog, idDocOriginal,
										estadosProps.getDistribuido());
							} catch (SOAPException  e) {
								
								log.error(raizLog + "ErrorCorrespondencia obtenerRespuestaDistribuirCddRdiRde iniciarWorkflowConAtributos " + e , e);										
								cargarError(context, e);
								context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.OBTENER_RESPUESTA_DISTRIBUIR_CDD_RDI_RDE);
								context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
							}

						} else {
							log.info(raizLog + "Distribucion RDE");

							Long llave = (long) 1;
							int posicion=0;
							for (Destino destino : listaDestinos) {


								if (origenRdiRde.equalsIgnoreCase(destino.getCddDestino())) {

								} else {

									String titulo = "Distribución RDI/RDE - " + numeroRadicado + " - "
											+ destino.getPais() + " - " + destino.getCiudad();

									log.info(raizLog + "El origen que se esta consultando es:" + origenRdiRde);
									CentroDistribucion centroDistribucionOrigen = centroDistribucionRepository
											.findByNombre(origenRdiRde);
									log.info(raizLog + "El destinatario que se esta consultando es:" + "EXTERNO");
									CentroDistribucion centroDistribucionDestino = centroDistribucionRepository
											.findByNombre("EXTERNO");

//									RutaDistribucion rutaDistribucion = rutaDistribucionRepository
//											.findByCentroDistribucionOrigenAndCentroDistribucionDestino(
//													centroDistribucionOrigen, centroDistribucionDestino);
									
									List<RutaDistribucion> rutasDB = rutaDistribucionRepository
											.findByCentroDistribucionOrigenAndCentroDistribucionDestino(centroDistribucionOrigen, centroDistribucionDestino);
												
									RutaDistribucion rutaDistribucion = null;
													
									if(rutasDB != null && !rutasDB.isEmpty()) {
										rutaDistribucion = SSMUtils.getRuta(rutasDB, tipologia , globalProperties);
									}

									String pasoSiguiente = "";
									if (rutaDistribucion != null) {

										List<PasoDistribucion> pasosRuta = rutaDistribucion.getPasos();

										if (esSucursal) {

											pasoSiguiente = origenRdiRde;
										} else {

											if (pasosRuta != null && !pasosRuta.isEmpty()) {
												pasoSiguiente = pasosRuta.get(0).getCentroDistribucion().getNombre();
											}

										}

										try {
											grupo = UsuarioCS.getGrupoByNombre(
													autenticacionCS.getUserSoapHeader(conexionUsuarioCS), pasoSiguiente,
													wsdlsProps.getUsuario());
											idGrupo = grupo.getID();
											log.info(raizLog + "El grupo " + pasoSiguiente + " tiene ID= " + idGrupo);

										} catch (SOAPException | IOException e) {
											log.error(raizLog + "ErrorCorrespondencia obtenerRespuestaDistribuirCddRdiRde getGrupoByNombre " + e , e);			
											cargarError(context, e);
											context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.OBTENER_RESPUESTA_DISTRIBUIR_CDD_RDI_RDE);
											context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
										}
										

										Map<String, ValorAtributo> atributos = new HashMap<String, ValorAtributo>();
										atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getRolDestino(), new ValorAtributo(idGrupo, TipoAtributo.USER));
										atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getEstado(), new ValorAtributo("CONTINUAR", TipoAtributo.STRING));
										atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getNumeroRadicado(),
												new ValorAtributo(numeroRadicado, TipoAtributo.STRING));
										atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getTipologia(), new ValorAtributo(tipologia, TipoAtributo.STRING));
										atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getWorkid(), new ValorAtributo(workId, TipoAtributo.STRING));
										atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getCodigoRuta(), new ValorAtributo(llave, TipoAtributo.INTEGER));
										atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getAsunto(), new ValorAtributo(asunto, TipoAtributo.STRING));
										//atributos.put("Cantidad",
										//		new ValorAtributo(destino.getCantidad(), TipoAtributo.INTEGER));
										atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getDescripcion(),
												new ValorAtributo(destino.getDescripcionObjeto(), TipoAtributo.MULTILINE));
										atributos
												.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getDestino(),
														new ValorAtributo(
																destino.getDireccion() + ", " + destino.getCiudad()
																		+ ", " + destino.getPais(),
																TipoAtributo.STRING));
										atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getRolOrigen(),
												new ValorAtributo(idSolicitante, TipoAtributo.USER));
										
										//Se imprime el log de los atributos
										if (log.isDebugEnabled() || log.isTraceEnabled()) {
											log.debug(raizLog + "Atributos que se van a enviar el workflow");
											Iterator<Entry<String, ValorAtributo>> atributosIter = atributos.entrySet()
													.iterator();
											while (atributosIter.hasNext()) {
												Entry<String, ValorAtributo> unAtributo = atributosIter.next();
												String unKey = unAtributo.getKey();
												ValorAtributo unValorAtributo = unAtributo.getValue();
												Object unValor = unValorAtributo.getValor();
												String unValorStr = "";
												if (unValor != null) {
													unValorStr = unValor.toString();
												} else {
													log.warn(raizLog + "Para la llave (" + unKey
															+ ") el valor del atributo es nulo");
												}
												log.debug("Un atributo llave (" + unKey + ") - valor (" + unValorStr
														+ ")");
											}
										}
										
										int distribuido=0;

										try {
											
											distribuido = context.getExtendedState().get(Consts.DISTRIBUIDO, Integer.class) == null ? 0 : context.getExtendedState().get(Consts.DISTRIBUIDO, Integer.class);
											log.info(raizLog + "distribuido: " + distribuido + " posicion: "  + posicion + " llave: " + llave);
											log.info(raizLog
													+ "INICIO iniciar WF distribucion RDI/RDE con rol siguiente:"
													+ pasoSiguiente);
											
											
											if (distribuido == posicion) {
												
												Workflow.iniciarWorkflowConAtributos(
														autenticacionCS.getAdminSoapHeader(), mapId, titulo, atributos,
														wsdlsProps.getWorkflow());
												log.info(raizLog
														+ "FIN iniciar WF distribucion RDI/RDE con rol siguiente:"
														+ pasoSiguiente);

												int suma = posicion;
												context.getExtendedState().getVariables().put(Consts.DISTRIBUIDO,
														++suma);
												
												String destinoSave = destino.getDireccion() + ", " + destino.getCiudad()+ ", " + destino.getPais();
												PruebaEntrega p = new PruebaEntrega(context.getStateMachine().getId(), numeroRadicado, "RDE - "+numeroRadicado, null, null, conexionUsuarioCS, destinoSave, new Date(), null);
												repositoryPrueba.save(p);

											}
											

										} catch (NumberFormatException e) {
											cargarError(context, e);
											context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
											log.error(raizLog + "ErrorCorrespondencia obtenerRespuestaDistribuirCddRdiRde iniciarWorkflowConAtributos " + e , e);										
										} catch (NoSuchElementException | SOAPException | IOException | WebServiceException  e) {
											log.error(raizLog + "ErrorCorrespondencia obtenerRespuestaDistribuirCddRdiRde iniciarWorkflowConAtributos " + e , e );										
											cargarError(context, e);
											context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.OBTENER_RESPUESTA_DISTRIBUIR_CDD_RDI_RDE);
											context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
										}
										log.info(raizLog + "El siguiente paso es:" + pasoSiguiente);

										log.info(raizLog + "Se actualiza estado a:"
												+ estadosProps.getDistribuido());

									} else {

										log.info(raizLog + "No se encontro ruta RDE en BD para origen:" + origenRdiRde
												+ " y destino:EXTERNO");

									}

								}

								llave++;
								posicion++;
							}

							try {
								actualizarEstadoCorrespondencia(raizLog, idDocOriginal,
										estadosProps.getDistribuido());
							} catch (SOAPException  e) {
								log.error(raizLog + "ErrorCorrespondencia obtenerRespuestaDistribuirCddRdiRde actualizarEstadoCorrespondencia " + e , e);										
								cargarError(context, e);
								context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.OBTENER_RESPUESTA_DISTRIBUIR_CDD_RDI_RDE);
								context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
							}

						}

					} else {

						log.info(raizLog + "Se realizo rechazo antes del CDD, se actualiza el estado a Rechazado");
						try {
							actualizarEstadoCorrespondencia(raizLog, idDocOriginal,
									estadosProps.getRechazo());
						} catch (SOAPException  e) {
							log.error(raizLog + "ErrorCorrespondencia obtenerRespuestaDistribuirCddRdiRde actualizarEstadoCorrespondencia " + e , e);										
							cargarError(context, e);
							context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
						}

					}

				}
				
				try {
					persistContexto(context);
				} catch (Exception e) {
					log.info(raizLog +
							"Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}
			};

		}

		/**
		 * Metodo que deja la maquina en espera de la respuesta del primer Centro de
		 * Distribucion CDD
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> esperarRespuestaDistribuirCddRdiRde() {

			return (context) -> {

				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " esperarRespuestaDistribuirCddRdiRde");

				log.info(raizLog
						+ "--------------------------ESTADO esperarRespuestaDistribuirCddRdiRde ------------------------");

				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out
							.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}

			};

		}

		/**
		 * Metodo que deja la maquina en espera de la respuesta del primer Centro de
		 * Distribucion CDD
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> esperarRespuestaDistribuirCddCarta() {

			return (context) -> {

				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " esperarRespuestaDistribuirCddCarta");

				log.info(raizLog
						+ "--------------------------ESTADO esperarRespuestaDistribuirCddCarta ------------------------");
				
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}

			};

		}
		
		
		
		//TODO BORRAR - OBSOLETA
		/**
		* Metodo que permite personalizar Documentos tipo Carta
		* 
		* El metodo genera una copia del documento por cada una de los destinatarios de
		* tal forma que solo se muestre un destinatario
		* 
		* @return
		*/
		@Bean
		public Action<States, Events> personalizarDocumentoCarta() {
			// SE RETORNA EL CONTEXTO DE LA MÁQUINA DE ESTADOS (ATRIBUTOS INICIALES)
			return (context) -> {
				// SE EXTRAE LA RAIZ LOG DEL CONTEXTO
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog
						+ "---------------------------INICIO ESTADO personalizarDocumento--------------------------------");
				// SI NO HAY ERROR MÁQUINA CARGADO
				if (!context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class)) {

					try {

						String nombreDocServidor = context.getExtendedState().get(Consts.NOMBRE_DOCUMENTO_SERVIDOR,
								String.class);

						MetadatosPlantilla categoria = context.getExtendedState().get(Consts.METADATOS_PLANTILLA,
								MetadatosPlantilla.class);
						Boolean esCarta = categoria.getTipologia().equalsIgnoreCase("CA");

						if (esCarta) {

							String conexionUsuario = context.getExtendedState().get(Consts.CONEXION_CS_SOLICITANTE,
									String.class);

							// Se actualiza el parent id de la carpeta original en caso de que el usuario
							// haya movido el documento
							actualizarIdParentOriginal(context, autenticacionCS.getAdminSoapHeader(),
									wsdlsProps.getDocumento());

							Long parentIdAdjuntos = context.getExtendedState().get(Consts.PARENT_ID_ADJUNTOS_MEMO_CARTA,
									Long.class);
							String numeroRadicado = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO,
									String.class);
							String tipoDocumentalCatDocumento = context.getExtendedState()
									.get(Consts.TIPO_DOCUMENTAL_CAT_DOCUMENTO, String.class);
							String serieCatDocumento = context.getExtendedState().get(Consts.SERIE_CAT_DOCUMENTO,
									String.class);

							long idDocOriginal = context.getExtendedState().get(Consts.ID_DOC_RADICACION, Long.class);
							List<DataHandler> listDataHandler = new ArrayList<>();
							List<Empleado> destinos = categoria.getDestinatarios();

							// Boolean esPersonalizado =
							// Boolean.valueOf(categoria.getPersonalizarDocumento());

							Boolean esPersonalizado = context.getExtendedState().get(Consts.ES_PERSONALIZADO,
									Boolean.class);

							if (esPersonalizado) {

								log.info(raizLog + "El documento [" + nombreDocServidor + "] SI se va a personalizar");
								log.info(raizLog + "parent:" + parentIdAdjuntos);
								try {
									/*
									 * listDataHandler = DocumentoUtils.personalizarDocumentoCartaMasivo(
									 * globalProperties.getRutaTemp() + nombreOriginal, cat,
									 * globalProperties.getRutaTemp() + nombreOriginal, mapPropsOfficeDoc);
									 */
									listDataHandler = DocumentoUtils.personalizarDocumentoCartaMasivo(raizLog,
											globalProperties.getRutaTemp() + nombreDocServidor, categoria);
								} catch (IOException e) {
									log.info(raizLog + "Ocurrio un error personalizando el documento:"
											+ nombreDocServidor + ", error:" + e);
									e.printStackTrace();
								}
								
			
			
			
								int count = 0;
								List<Long> listIdDocs = new ArrayList<>();
								//TODO COMENTE LO RELACIONADO CON CATEGORIAS
/*								Node nodoDocOriginal = ContenidoDocumento.getDocumentoById(
										autenticacionCS.getUserSoapHeader(conexionUsuario), idDocOriginal,
										wsdlsProps.getDocumento());

								Metadata metadataDocOriginal = nodoDocOriginal.getMetadata();

								AttributeGroup categoriaCorrespondencia = null;
								categoriaCorrespondencia = metadataDocOriginal.getAttributeGroups().stream()
										.filter(unaCategoria -> unaCategoria.getDisplayName()
												.equals(categoriaProps.getCorrespondencia().getNombre()))
										.findFirst().get();
										*/

								List<String> listNombresDocsPersonalizados = new ArrayList<>();
								for (DataHandler dataHandler : listDataHandler) {
									try {

										log.info(raizLog + "INICIO crear doc personalizado en Content");
										Map<String, Object> metadatosCatDoc = new HashMap<String, Object>();
										Long idDocPersonalizado;

										String nombreDocumento = numeroRadicado + "_"
												+ ((destinos.get(count).getNombre() != null
														&& !destinos.get(count).getNombre().isEmpty())
																? destinos.get(count).getNombre()
																: destinos.get(count).getDependencia())
												+ "_" + count;

										nombreDocumento = SSMUtils.truncarString(nombreDocumento,
												Consts.TAMANO_MAXIMO_NOMBRE_DOCUMENTO_ORIGINAL);

										if (tipoDocumentalCatDocumento != null && !tipoDocumentalCatDocumento.isEmpty()
												&& serieCatDocumento != null && !serieCatDocumento.isEmpty()) {

											log.info(raizLog + "Crear documento con categoria documento");
											
											metadatosCatDoc.put("Tipo documental", tipoDocumentalCatDocumento);
											metadatosCatDoc.put("Serie", serieCatDocumento);

											idDocPersonalizado = ContenidoDocumento.crearDocumentoCatDocumento(
													autenticacionCS.getUserSoapHeader(conexionUsuario), idDocOriginal,
													nombreDocumento + ".docx", dataHandler, categoriaDocProps.getId(),
													categoriaDocProps.getNombreCategoria(), metadatosCatDoc, false,
													null, wsdlsProps.getDocumento());
										} else {

											log.info(raizLog + "Crear documento sin categoria documento");

											idDocPersonalizado = ContenidoDocumento.crearDocumento(
													autenticacionCS.getUserSoapHeader(conexionUsuario),
													nombreDocumento + ".docx", dataHandler, parentIdAdjuntos,
													wsdlsProps.getDocumento());

										}

										log.info(raizLog + "FIN crear doc personalizado en Content");

										//TODO COMENTE LO RELACIONADO CON CATEGORIAS
										/*
										log.info(raizLog + "INICIO agregar cat correspondencia a doc personalizado");

										Node nodoDocPerso = ContenidoDocumento.getDocumentoById(
												autenticacionCS.getUserSoapHeader(conexionUsuario), idDoc,
												wsdlsProps.getDocumento());

										Metadata metadataDocPerso = nodoDocPerso.getMetadata();

										metadataDocPerso.getAttributeGroups().add(categoriaCorrespondencia);

										nodoDocPerso.setMetadata(metadataDocPerso);

										ContenidoDocumento.actualizarMetadataDocumento(
												autenticacionCS.getUserSoapHeader(conexionUsuario), idDoc,
												metadataDocPerso, wsdlsProps.getDocumento());
										*/

										listIdDocs.add(idDocPersonalizado);

										destinos.get(count).setIdDocumento(idDocPersonalizado);

										log.info(raizLog + "FIN agregar cat correspondencia a doc personalizado");

										log.info(raizLog + "INICIO cambiar nombre documento personalizado");

										String nombreDoc = ContenidoDocumento.obtenerNombreDocumento(
												autenticacionCS.getUserSoapHeader(conexionUsuario), idDocPersonalizado,
												wsdlsProps.getDocumento());

										log.info(raizLog + "El nombre del doc personalizado ANTES: [" + nombreDoc + "]");

										ContenidoDocumento.cambiarNombreDocumento(
												autenticacionCS.getUserSoapHeader(conexionUsuario), idDocPersonalizado,
												nombreDocumento + ".pdf", wsdlsProps.getDocumento());

										String nombreDocDespues = ContenidoDocumento.obtenerNombreDocumento(
												autenticacionCS.getUserSoapHeader(conexionUsuario), idDocPersonalizado,
												wsdlsProps.getDocumento());

										destinos.get(count).setNombreDocumento(nombreDocDespues);

										log.info(raizLog + "El nombre del doc personalizado DESPUES: ["
												+ nombreDocDespues +"]");

										log.info(raizLog + "FIN cambiar nombre documento personalizado");

										listNombresDocsPersonalizados.add(nombreDoc);

									} catch (SOAPException e) {
										e.printStackTrace();
									} catch (IOException e) {
										e.printStackTrace();
									}
									count++;

									// Vamaya: Se modifica para que borre el archivo correctamente -- Esta
									// implementación añadida se comentó, pero se debe revisar el borrado de los
									// documentos en el servidor para futuras correciones
									// String workId =
									// context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION,String.class);
									// String nombreDoc = globalProperties.getRutaTemp() + workId + "-" +
									// idDocOriginal + "-" + dataHandler.getName();
									
									//ELIMINACIÓN DE LOS ARCHIVOS COMBINADOS
									String nombreDocPersonalizado = globalProperties.getRutaTemp() + dataHandler.getName();
									log.info(raizLog + "archivo a eliminar: [" + nombreDocPersonalizado+"]");
									File doc = new File(nombreDocPersonalizado);
									log.info(raizLog + "Archivo eliminado: [" + doc.delete() + "]");

								}
								String idList = listIdDocs.toString();
								String cadenaListIdDocs = String.join(",", idList);

								cadenaListIdDocs = cadenaListIdDocs.replace("[", "{");
								cadenaListIdDocs = cadenaListIdDocs.replace("]", "}");

								cadenaListIdDocs = StringUtils.deleteWhitespace(cadenaListIdDocs);
								log.info(raizLog + "LA CADENA DE IDS PERSONALIZADOS: [" + cadenaListIdDocs + "]");

								//ACTUALIZA DESTINOS DE LA CATEGORIA CON SUS IDS
								categoria.setDestinatarios(destinos);
								context.getExtendedState().getVariables().put(Consts.METADATOS_PLANTILLA, categoria);

								context.getExtendedState().getVariables().put(
										Consts.LISTA_NOMBRES_DOCS_PERSONALIZADOS_CARTA, listNombresDocsPersonalizados);
								context.getExtendedState().getVariables().put(Consts.LISTA_ID_DOCS_PERSONALIZADOS_CARTA,
										listIdDocs);
								context.getExtendedState().getVariables()
										.put(Consts.CADENA_LISTA_ID_DOCS_PERSONALIZADOS_CARTA, cadenaListIdDocs);
								// context.getExtendedState().getVariables().put(Consts.ES_PERSONALIZADO,
								// esPersonalizado);
							} else {// NO ES PERSONALIZADO
								log.info(raizLog + "El documento [" + nombreDocServidor + "] NO se va a personalizar");
							}

						} else {// NO ES TIPOLOGIA CA
							log.info(raizLog + "El documento [" + nombreDocServidor
									+ "] NO se va a personalizar porque la tipologia no es Carta");
						}

					} catch (Exception e) {
						// CARGAR ERROR
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
						log.error(raizLog + "ErrorCorrespondencia personalizarDocumento generarDocumentoSticker " + e,
								e);
					}

				}

				log.info(raizLog
						+ "---------------------------FIN ESTADO personalizarDocumento--------------------------------");

				try {
					// SE ALMACENA EL CONTEXTO DE LA MÁQUINA EN BD
					persistContexto(context);
				} catch (Exception e) {
					System.out
							.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}
			};

		}

		/**
		 * Metodo que deja la maquina en espera de la respuesta del primer Centro de
		 * Distribucion CDD
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> esperarRespuestaDistribuirCarta() {

			return (context) -> {

				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " esperarRespuestaDistribuirCarta");

				log.info(raizLog
						+ "--------------------------ESTADO esperarRespuestaDistribuirCarta ------------------------");
				
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}

			};

		}

		/**
		 * Metodo que permite continuar el flujo de distribucion rdi/rde teniendo en
		 * cuenta el tipo de envio seleccionado
		 * 
		 * En este metodo tambien se calculan y se almacenan en el contexto las rutas
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> validarIniciarFormaDistribucionRdiRde() {

			return (context) -> {

				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " validarIniciarFormaDistribucionRdiRde");

				log.info(raizLog
						+ "---------------------------ESTADO validarIniciarFormaDistribucionRdiRde -------------------------------------------");

				if (!context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class)) {

					Boolean esMensajeria = context.getExtendedState().get(Consts.ES_MENSAJERIA_RDI_RDE, Boolean.class);

					String numeroRadicado = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO,
							String.class);
					String origenRdiRde = context.getExtendedState().get(Consts.ORIGEN_RDI_RDE, String.class);
					String origenRdiRdePcr = context.getExtendedState().get(Consts.ORIGEN_RDI_RDE_PCR, String.class);
					Long idSolicitante = context.getExtendedState().get(Consts.ID_SOLICITANTE, Long.class);
					String tipologia = context.getExtendedState().get(Consts.TIPOLOGIA_RDI_RDE, String.class);
					String workId = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);
					String asunto = context.getExtendedState().get(Consts.ASUNTO_RDI_RDE, String.class);
					long idDocOriginal = context.getExtendedState().get(Consts.ID_DOC_RADICACION, Long.class);
					@SuppressWarnings("unchecked")
					List<String> listaDestinatarios = (List<String>) context.getExtendedState()
							.get(Consts.LIST_CDD_DESTINO_RDI_RDE, Object.class);
					@SuppressWarnings("unchecked")
					List<String> listaDestinatariosPcr = (List<String>) context.getExtendedState()
							.get(Consts.LIST_PCR_DESTINO_RDI_RDE, Object.class);
					@SuppressWarnings("unchecked")
					List<Destino> listaDestinos = (List<Destino>) context.getExtendedState()
							.get(Consts.LISTA_DESTINOS_RDI_RDE, Object.class);

					String conexionUsuarioCS = context.getExtendedState().get(Consts.CONEXION_CS_SOLICITANTE, String.class);

					int totalConfirmadosDistribuir = 0;
					int totalRechazadosDistribuir = 0;

					context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS_CONFIRMADOS,
							totalConfirmadosDistribuir);
					context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS_RECHAZADOS,
							totalRechazadosDistribuir);

					log.info(raizLog + "Numero Radicado:" + numeroRadicado);

					Map<Long, String> rutas = new HashMap<>();

					long mapId = workflowProps.getDistribucionRdiRde().getId();

					Member grupo = null;
					Long idGrupo = null;

					if (!esMensajeria) {

						log.info(raizLog + "Distribucion DIRECTO");

						if (tipologia.equalsIgnoreCase("RDI")) {

							log.info(raizLog + "Distribucion DIRECTO RDI");

							Long llave = (long) 1;
							int posicion = 0;
							for (String destinatario : listaDestinatariosPcr) {
								String titulo = "Distribución RDI/RDE - " + numeroRadicado + " - "
										+ listaDestinos.get(posicion).getDependenciaDestino();

								String rutaCompletaTexto = origenRdiRdePcr + ";" + destinatario;
								rutas.put(llave, rutaCompletaTexto);

								try {
									grupo = UsuarioCS.getGrupoByNombre(
											autenticacionCS.getUserSoapHeader(conexionUsuarioCS), destinatario,
											wsdlsProps.getUsuario());
									idGrupo = grupo.getID();
								} catch (SOAPException | WebServiceException e) {
									cargarError(context, e);
									context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
									log.error(raizLog + "ErrorCorrespondencia validarIniciarFormaDistribucionRdiRde getGrupoByNombre " + e, e);
								} catch (IOException e) {
									cargarError(context, e);
									context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
									log.error(raizLog + "ErrorCorrespondencia validarIniciarFormaDistribucionRdiRde getGrupoByNombre " + e , e);
								}

								Map<String, ValorAtributo> atributos = new HashMap<String, ValorAtributo>();
								atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getRolDestino(), new ValorAtributo(idGrupo, TipoAtributo.USER));
								atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getEstado(), new ValorAtributo("FINALIZAR", TipoAtributo.STRING));
								atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getNumeroRadicado(),
										new ValorAtributo(numeroRadicado, TipoAtributo.STRING));
								atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getTipologia(), new ValorAtributo(tipologia, TipoAtributo.STRING));
								atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getWorkid(), new ValorAtributo(workId, TipoAtributo.STRING));
								atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getCodigoRuta(), new ValorAtributo(llave, TipoAtributo.INTEGER));
								atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getAsunto(), new ValorAtributo(asunto, TipoAtributo.STRING));
								atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getCantidad(), new ValorAtributo(listaDestinos.get(posicion).getCantidad(),
										TipoAtributo.INTEGER));
								atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getDescripcion(), new ValorAtributo(
										listaDestinos.get(posicion).getDescripcionObjeto(), TipoAtributo.MULTILINE));
								atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getDestino(), new ValorAtributo(
										listaDestinos.get(posicion).getDependenciaDestino(), TipoAtributo.STRING));
								atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getRolOrigen(), new ValorAtributo(idSolicitante, TipoAtributo.USER));
								
								int distribuido=0;
																
								try {
									
									distribuido = context.getExtendedState().get(Consts.DISTRIBUIDO, Integer.class) == null ? 0 : context.getExtendedState().get(Consts.DISTRIBUIDO, Integer.class);
									
									log.info(raizLog + "INICIO iniciar WF distribucion RDI/RDE DIRECTO destino:"
											+ destinatario);
									
									log.info(raizLog + "distribuido: " + distribuido + " posicion: " + posicion);

									
									if (distribuido == posicion) {

										Long id = Workflow.iniciarWorkflowConAtributos(
												autenticacionCS.getAdminSoapHeader(), mapId, titulo, atributos,
												wsdlsProps.getWorkflow());
										log.info(raizLog + "FIN iniciar WF distribucion RDI/RDE DIRECTO destino:"
												+ destinatario);
										log.info(raizLog + "ID Workflow iniciado" + id);
										
										int suma = posicion;
										context.getExtendedState().getVariables().put(Consts.DISTRIBUIDO, ++suma);

									}
									
								} catch (NumberFormatException | NoSuchElementException | IOException  e) {
									cargarError(context, e);
									context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
									log.error(raizLog + "ErrorCorrespondencia validarIniciarFormaDistribucionRdiRde iniciarWorkflowConAtributos " + e, e);
							
								}catch( SOAPException | WebServiceException e) {
									log.error(raizLog + "ErrorCorrespondencia distribuirRdiRde iniciarWorkflowConAtributos " + e, e);
									cargarError(context, e);
									//context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.VALIDAR_INICIAR_FORMA_DISTRIBUIR_RDI_RDE);
									context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.VALIDAR_INICIAR_FORMA_DISTRIBUIR_RDI_RDE);
									context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
								}

								posicion++;
								llave++;
							}

							for (Map.Entry<Long, String> entry : rutas.entrySet()) {
								log.info(raizLog + entry.getKey() + ":" + entry.getValue().toString());
							}
						} else {

							log.info(raizLog + "Distribucion Directo RDE");

							Long llave = (long) 1;
							int posicion = 0;
							for (Destino destino : listaDestinos) {

								String titulo = "Distribución RDI/RDE - " + numeroRadicado + " - "
										+ ((!destino.getNombre().isEmpty()) ? destino.getNombre()
												: destino.getEntidad());

								String rutaCompletaTexto = origenRdiRdePcr;
								rutas.put(llave, rutaCompletaTexto);

								try {
									grupo = UsuarioCS.getGrupoByNombre(
											autenticacionCS.getUserSoapHeader(conexionUsuarioCS), origenRdiRdePcr,
											wsdlsProps.getUsuario());
									idGrupo = grupo.getID();
								} catch (SOAPException | WebServiceException e) {
									cargarError(context, e);
									context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
									log.error(raizLog + "ErrorCorrespondencia validarIniciarFormaDistribucionRdiRde getGrupoByNombre " + e, e);;
								} catch (IOException e) {
									cargarError(context, e);
									context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
									log.error(raizLog + "ErrorCorrespondencia validarIniciarFormaDistribucionRdiRde getGrupoByNombre " + e, e);
								}

								Map<String, ValorAtributo> atributos = new HashMap<String, ValorAtributo>();
								atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getRolDestino(), new ValorAtributo(idGrupo, TipoAtributo.USER));
								atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getEstado(), new ValorAtributo("FINALIZAR", TipoAtributo.STRING));
								atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getNumeroRadicado(),
										new ValorAtributo(numeroRadicado, TipoAtributo.STRING));
								atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getTipologia(), new ValorAtributo(tipologia, TipoAtributo.STRING));
								atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getWorkid(), new ValorAtributo(workId, TipoAtributo.STRING));
								atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getCodigoRuta(), new ValorAtributo(llave, TipoAtributo.INTEGER));
								atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getAsunto(), new ValorAtributo(asunto, TipoAtributo.STRING));
								atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getDescripcion(),
										new ValorAtributo(destino.getDescripcionObjeto(), TipoAtributo.MULTILINE));
								atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getEsDirecto(), new ValorAtributo(Boolean.TRUE, TipoAtributo.STRING));
								atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getDestino(), new ValorAtributo(
										destino.getDireccion() + ", " + destino.getCiudad() + ", " + destino.getPais(),
										TipoAtributo.STRING));
								atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getRolOrigen(), new ValorAtributo(idSolicitante, TipoAtributo.USER));

								int distribuido=0;
								
								try {
									
									distribuido = context.getExtendedState().get(Consts.DISTRIBUIDO, Integer.class) == null ? 0 : context.getExtendedState().get(Consts.DISTRIBUIDO, Integer.class);
									
									log.info(raizLog + "INICIO iniciar WF distribucion RDI/RDE DIRECTO destino:"
											+ origenRdiRdePcr);
									

									log.info(raizLog + "distribuido: " + distribuido + " posicion: " + posicion);

									
									if (distribuido == posicion) {
										
										long id = Workflow.iniciarWorkflowConAtributos(
												autenticacionCS.getAdminSoapHeader(), mapId, titulo, atributos,
												wsdlsProps.getWorkflow());

										log.info(raizLog + "ID Workflow iniciado" + id);
										
										int suma = posicion;
										context.getExtendedState().getVariables().put(Consts.DISTRIBUIDO, ++suma);

									}
									
									String destinoSave = destino.getDireccion() + ", " + destino.getCiudad()+ ", " + destino.getPais();
									PruebaEntrega p = new PruebaEntrega(context.getStateMachine().getId(), numeroRadicado, "RDE - "+numeroRadicado, null, null, conexionUsuarioCS, destinoSave, new Date(), null);
									repositoryPrueba.save(p);
									
									log.info(raizLog + "FIN iniciar WF distribucion RDI/RDE DIRECTO destino:"
											+ origenRdiRdePcr);
								} catch (NumberFormatException | NoSuchElementException | IOException e) {
									cargarError(context, e);
									context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
									log.error(raizLog + "ErrorCorrespondencia validarIniciarFormaDistribucionRdiRde iniciarWorkflowConAtributos " + e, e);
						
								}catch ( SOAPException | WebServiceException e) {
									log.error(raizLog + "ErrorCorrespondencia distribuirRdiRde iniciarWorkflowConAtributos " + e, e);
									cargarError(context, e);
									context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.VALIDAR_INICIAR_FORMA_DISTRIBUIR_RDI_RDE);
									context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
								}

								llave++;
								posicion++;

							}
						}

						try {
							actualizarEstadoCorrespondencia(raizLog, idDocOriginal,
									estadosProps.getDistribuido());
						} catch (SOAPException e) {
							log.error(raizLog + "ErrorCorrespondencia distribuirRdiRde actualizarEstadoCorrespondencia " + e, e);
							cargarError(context, e);
							context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.VALIDAR_INICIAR_FORMA_DISTRIBUIR_RDI_RDE);
							context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
						}

						context.getExtendedState().getVariables().put(Consts.MAP_RUTAS__RDI_RDE, rutas);

						context.getExtendedState().getVariables().put(Consts.ROL_SIGUIENTE_DISTRIBUIR_RDI_RDE, "");
						context.getExtendedState().getVariables().put(Consts.FINALIZO_DISTRIBUCION_RDI_RDE, true);
						context.getStateMachine().sendEvent(Events.IR_ESPERAR_RESPUESTA_DISTRIBUIR_RDI_RDE);

					} else {

						log.info(raizLog + "Distribucion MENSAJERIA");

						if (tipologia.equalsIgnoreCase("RDI")) {

							log.info(raizLog + "Distribucion RDI");

							Long llave = (long) 1;
							int posicion = 0;
							for (String destinatario : listaDestinatarios) {

								String rutaCompletaTexto = "";

								if (origenRdiRde.equalsIgnoreCase(destinatario)) {

									log.info(raizLog + "El origen:" + origenRdiRdePcr + " y el destino:"
											+ listaDestinatariosPcr.get(posicion) + " tienen el mismo CDD:"
											+ origenRdiRde);

									rutaCompletaTexto += origenRdiRdePcr + ";" + origenRdiRde + ";"
											+ listaDestinatariosPcr.get(posicion);
									rutas.put(llave, rutaCompletaTexto);
									log.info(raizLog + "La ruta completa en texto es:" + rutaCompletaTexto);
								} else {

									log.info(raizLog + "El origen que se esta consultando es:" + origenRdiRde);
									CentroDistribucion centroDistribucionOrigen = centroDistribucionRepository
											.findByNombre(origenRdiRde);
									log.info(raizLog + "El destinatario que se esta consultando es:" + destinatario);
									CentroDistribucion centroDistribucionDestino = centroDistribucionRepository
											.findByNombre(destinatario);

//									RutaDistribucion rutaDistribucion = rutaDistribucionRepository
//											.findByCentroDistribucionOrigenAndCentroDistribucionDestino(
//													centroDistribucionOrigen, centroDistribucionDestino);
									
									List<RutaDistribucion> rutasDB = rutaDistribucionRepository
											.findByCentroDistribucionOrigenAndCentroDistribucionDestino(centroDistribucionOrigen, centroDistribucionDestino);
												
									RutaDistribucion rutaDistribucion = null;
													
									if(rutasDB != null && !rutasDB.isEmpty()) {
										rutaDistribucion = SSMUtils.getRuta(rutasDB, tipologia , globalProperties);
									}

									if (rutaDistribucion != null) {

										log.info(raizLog + "Si existe ruta en BD para origen:" + origenRdiRde
												+ " y destino:" + destinatario);

										List<PasoDistribucion> pasosRuta = rutaDistribucion.getPasos();

										rutaCompletaTexto += origenRdiRdePcr + ";" + origenRdiRde + ";";

										for (PasoDistribucion pasoDistribucion : pasosRuta) {
											String nombrePaso = pasoDistribucion.getCentroDistribucion().getNombre();
											rutaCompletaTexto += nombrePaso + ";";
										}


										rutaCompletaTexto += destinatario + ";" + listaDestinatariosPcr.get(posicion);

										rutas.put(llave, rutaCompletaTexto);

										log.info(raizLog + "La ruta completa en texto es:" + rutaCompletaTexto);

									} else {
										log.info(raizLog + "No se encontro ruta en BD para origen:" + origenRdiRde
												+ " y destino:" + destinatario);

									}
								}

								posicion++;
								llave++;
							}

							context.getExtendedState().getVariables().put(Consts.MAP_RUTAS__RDI_RDE, rutas);
							context.getStateMachine().sendEvent(Events.IR_ESPERAR_RESPUESTA_DISTRIBUIR_CDD_RDI_RDE);

						} else {

							log.info(raizLog + "Distribucion RDE");

							Long llave = (long) 1;
							for (Destino destino : listaDestinos) {

								String rutaCompletaTexto = "";

								if (origenRdiRde.equalsIgnoreCase(destino.getCddDestino())) {

									log.info(raizLog + "El origen:" + origenRdiRdePcr + " y el destino:"
											+ destino.getPcrDestino() + " tienen el mismo CDD:" + origenRdiRde);

									rutaCompletaTexto += origenRdiRdePcr + ";" + origenRdiRde;
									rutas.put(llave, rutaCompletaTexto);
									log.info(raizLog + "La ruta completa en texto es:" + rutaCompletaTexto);
								} else {

									log.info(raizLog + "El origen que se esta consultando es:" + origenRdiRde);
									CentroDistribucion centroDistribucionOrigen = centroDistribucionRepository
											.findByNombre(origenRdiRde);
									log.info(raizLog + "El destinatario que se esta consultando es:" + "EXTERNO");
									CentroDistribucion centroDistribucionDestino = centroDistribucionRepository
											.findByNombre("EXTERNO");

//									RutaDistribucion rutaDistribucion = rutaDistribucionRepository
//											.findByCentroDistribucionOrigenAndCentroDistribucionDestino(
//													centroDistribucionOrigen, centroDistribucionDestino);
									
									List<RutaDistribucion> rutasDB = rutaDistribucionRepository
											.findByCentroDistribucionOrigenAndCentroDistribucionDestino(centroDistribucionOrigen, centroDistribucionDestino);
												
									RutaDistribucion rutaDistribucion = null;
													
									if(rutasDB != null && !rutasDB.isEmpty()) {
										rutaDistribucion = SSMUtils.getRuta(rutasDB, tipologia , globalProperties);
									}

									if (rutaDistribucion != null) {

										List<PasoDistribucion> pasosRuta = rutaDistribucion.getPasos();

										rutaCompletaTexto += origenRdiRdePcr + ";" + origenRdiRde + ";";

										for (PasoDistribucion pasoDistribucion : pasosRuta) {
											String nombrePaso = pasoDistribucion.getCentroDistribucion().getNombre();
											rutaCompletaTexto += nombrePaso + ";";
										}

										rutas.put(llave, rutaCompletaTexto);

										log.info(raizLog + "La ruta completa en texto es:" + rutaCompletaTexto);

									} else {

										log.info(raizLog + "No se encontro ruta RDE en BD para origen:" + origenRdiRde
												+ " y destino:EXTERNO");

									}

								}

								llave++;

							}

							context.getExtendedState().getVariables().put(Consts.MAP_RUTAS__RDI_RDE, rutas);
							context.getStateMachine().sendEvent(Events.IR_ESPERAR_RESPUESTA_DISTRIBUIR_CDD_RDI_RDE);

						}

					}
					log.info(raizLog + "Se actualiza el estado de la CAT a DISTRIBUIDO");
					try {
						actualizarEstadoCorrespondencia(raizLog, idDocOriginal,
								estadosProps.getDistribuido());
					} catch (SOAPException  e) {
						log.error(raizLog + "ErrorCorrespondencia distribuirRdiRde iniciarWorkflowConAtributos " + e);
						cargarError(context, e);
						context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.VALIDAR_INICIAR_FORMA_DISTRIBUIR_RDI_RDE);
						context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
					}

					try {
						persistContexto(context);
					} catch (Exception e) {
						log.info(raizLog +
								"Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
						log.error("Error operacion", e);
					}
				}
			};

		}

		// este metodo no se esta usando en este momento
		@Bean
		public Action<States, Events> iniciarPrimeraTareaDistribuirRdiRde() {

			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog
						+ "---------------------------------ESTADO iniciarPrimeraTareaDistribuirRdiRde --------------------------");

				log.info(raizLog + " iniciarPrimeraTareaDistribuirRdiRde");
				if (!context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class)) {

					String numeroRadicado = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO,
							String.class);

					String origenRdiRde = context.getExtendedState().get(Consts.ORIGEN_RDI_RDE, String.class);
					String tipologia = context.getExtendedState().get(Consts.TIPOLOGIA_RDI_RDE, String.class);
					String workId = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);
					String asunto = context.getExtendedState().get(Consts.ASUNTO_RDI_RDE, String.class);

					@SuppressWarnings("unchecked")
					List<String> listaDestinatarios = (List<String>) context.getExtendedState()
							.get(Consts.LIST_CDD_DESTINO_RDI_RDE, Object.class);

					long mapId = workflowProps.getDistribucionRdiRde().getId();

					Member grupo = null;
					Long idGrupo = null;

					String titulo = "Distribución RDI/RDE - " + numeroRadicado + " - " + new Date();

					String destino = listaDestinatarios.get(0);

					log.info(raizLog + "El origen que se esta consultando es:" + origenRdiRde);
					CentroDistribucion centroDistribucionOrigen = centroDistribucionRepository
							.findByNombre(origenRdiRde);
					log.info(raizLog + "El destinatario que se esta consultando es:" + destino);
					CentroDistribucion centroDistribucionDestino = centroDistribucionRepository.findByNombre(destino);

//					RutaDistribucion rutaDistribucion = rutaDistribucionRepository
//							.findByCentroDistribucionOrigenAndCentroDistribucionDestino(centroDistribucionOrigen,
//									centroDistribucionDestino);
					
					List<RutaDistribucion> rutasDB = rutaDistribucionRepository
							.findByCentroDistribucionOrigenAndCentroDistribucionDestino(centroDistribucionOrigen, centroDistribucionDestino);
								
					RutaDistribucion rutaDistribucion = null;
									
					if(rutasDB != null && !rutasDB.isEmpty()) {
						rutaDistribucion = SSMUtils.getRuta(rutasDB, tipologia , globalProperties);
					}

					List<PasoDistribucion> pasosRuta = rutaDistribucion.getPasos();

					String pasoSiguiente = pasosRuta.get(0).getCentroDistribucion().getNombre();

					try {
						grupo = UsuarioCS.getGrupoByNombre(autenticacionCS.getAdminSoapHeader(),
								pasoSiguiente, wsdlsProps.getUsuario());
						idGrupo = grupo.getID();
					} catch (SOAPException |IOException e) {
						log.error(raizLog + "ErrorCorrespondencia iniciarPrimeraTareaDistribuirRdiRde getGrupoByNombre" + e);
						cargarError(context, e);
						context.getExtendedState().getVariables().put("ESTADOREINTENTO", "iniciarPrimeraTareaDistribuirRdiRde");
						context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
					}

					Map<String, ValorAtributo> atributos = new HashMap<String, ValorAtributo>();
					atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getRolDestino(), new ValorAtributo(idGrupo, TipoAtributo.USER));
					atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getEstado(), new ValorAtributo("CONTINUAR", TipoAtributo.STRING));
					atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getNumeroRadicado(), new ValorAtributo(numeroRadicado, TipoAtributo.STRING));
					atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getTipologia(), new ValorAtributo(tipologia, TipoAtributo.STRING));
					atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getWorkid(), new ValorAtributo(workId, TipoAtributo.STRING));
					atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getAsunto(), new ValorAtributo(asunto, TipoAtributo.STRING));

					try {
						Workflow.iniciarWorkflowConAtributos(autenticacionCS.getAdminSoapHeader(),
								mapId, titulo, atributos, wsdlsProps.getWorkflow());
					} catch (NumberFormatException e) {
						context.getExtendedState().getVariables().put(Consts.ERROR_MAQUINA, Boolean.TRUE);
						log.error(raizLog + "ErrorCorrespondencia iniciarPrimeraTareaDistribuirRdiRde iniciarWorkflowConAtributos" + e);
					} catch (NoSuchElementException | SOAPException | IOException  e) {
						log.error(raizLog + "ErrorCorrespondencia iniciarPrimeraTareaDistribuirRdiRde iniciarWorkflowConAtributos" + e);
						cargarError(context, e);
						context.getExtendedState().getVariables().put("ESTADOREINTENTO", "iniciarPrimeraTareaDistribuirRdiRde");
						context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
					}
				}
				
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}
			};
		}

		// este metodo no se esta usando en este momento
		@Bean
		public Action<States, Events> distribuirRdiRde() {
			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);

				log.info(raizLog
						+ "---------------------------INICIO estado distribuirRdiRde------------------------------------");

				if (!context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class)) {
					log.info(raizLog + " distribuirRdiRde");

					String origenRdiRde = context.getExtendedState().get(Consts.ORIGEN_RDI_RDE, String.class);
					String tipologia = context.getExtendedState().get(Consts.TIPOLOGIA_RDI_RDE, String.class);
					String workId = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);
					String asunto = context.getExtendedState().get(Consts.ASUNTO_RDI_RDE, String.class);
					Boolean esMensajeria = context.getExtendedState().get(Consts.ES_MENSAJERIA_RDI_RDE, Boolean.class);
					String numeroRadicado = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO,
							String.class);

					@SuppressWarnings("unchecked")
					List<String> listaDestinatarios = (List<String>) context.getExtendedState()
							.get(Consts.LIST_CDD_DESTINO_RDI_RDE, Object.class);

					String rutaCompletaTexto = "";
					Member grupo = null;
					Long idGrupo = null;

					long mapId = workflowProps.getDistribucionRdiRde().getId();
					String titulo = "Distribución RDI/RDE - " + numeroRadicado + " - " + new Date();

					Map<Long, String> rutas = new HashMap<>();

					Long llave = (long) 1;
					if (listaDestinatarios != null && !listaDestinatarios.isEmpty()) {
						for (String destinatario : listaDestinatarios) {

							if (!esMensajeria) {

								log.info(raizLog + "Distribucion DIRECTO");

								try {
									grupo = UsuarioCS.getGrupoByNombre(
											autenticacionCS.getAdminSoapHeader(), destinatario,
											wsdlsProps.getUsuario());
									idGrupo = grupo.getID();
								} catch (SOAPException | IOException  e) {
									log.error(raizLog + "Error IOException ", e);
									e.printStackTrace();
									cargarError(context, e);
									context.getExtendedState().getVariables().put("ESTADOREINTENTO", "distribuirRdiRde");
									context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
								}

								Map<String, ValorAtributo> atributos = new HashMap<String, ValorAtributo>();
								atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getRolDestino(), new ValorAtributo(idGrupo, TipoAtributo.USER));
								atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getEstado(), new ValorAtributo("CONTINUAR", TipoAtributo.STRING));
								atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getNumeroRadicado(),
										new ValorAtributo(numeroRadicado, TipoAtributo.STRING));
								atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getTipologia(), new ValorAtributo(tipologia, TipoAtributo.STRING));
								atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getWorkid(), new ValorAtributo(workId, TipoAtributo.STRING));
								atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getCodigoRuta(), new ValorAtributo(llave, TipoAtributo.INTEGER));
								atributos.put(workflowProps.getDistribucionRdiRde().getAtributoWF().getAsunto(), new ValorAtributo(asunto, TipoAtributo.STRING));
								
								try {
									log.info(raizLog + "INICIO iniciar WF distribucion RDI/RDE DIRECTO destino:"
											+ destinatario);
									Workflow.iniciarWorkflowConAtributos(
											autenticacionCS.getAdminSoapHeader(), mapId, titulo,
											atributos, wsdlsProps.getWorkflow());
									log.info(raizLog +
											"FIN iniciar WF distribucion RDI/RDE DIRECTO destino:" + destinatario);
								} catch (NumberFormatException e) {
									cargarError(context, e);
									context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
									log.error(raizLog + "ErrorCorrespondencia distribuirRdiRde iniciarWorkflowConAtributos " + e, e);
								} catch (NoSuchElementException |SOAPException | IOException | WebServiceException  e) {
									log.error(raizLog + "ErrorCorrespondencia distribuirRdiRde iniciarWorkflowConAtributos " + e, e);
									cargarError(context, e);
									context.getExtendedState().getVariables().put("ESTADOREINTENTO", "distribuirRdiRde");
									context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
								}

							} else {

								log.info(raizLog + "Distribucion MENSAJERIA");

								rutaCompletaTexto = "";

								log.info(raizLog + "El origen que se esta consultando es:" + origenRdiRde);
								CentroDistribucion centroDistribucionOrigen = centroDistribucionRepository
										.findByNombre(origenRdiRde);
								log.info(raizLog + "El destinatario que se esta consultando es:" + destinatario);
								CentroDistribucion centroDistribucionDestino = centroDistribucionRepository
										.findByNombre(destinatario);

//								RutaDistribucion rutaDistribucion = rutaDistribucionRepository
//										.findByCentroDistribucionOrigenAndCentroDistribucionDestino(
//												centroDistribucionOrigen, centroDistribucionDestino);
								
								List<RutaDistribucion> rutasDB = rutaDistribucionRepository
										.findByCentroDistribucionOrigenAndCentroDistribucionDestino(centroDistribucionOrigen, centroDistribucionDestino);
											
								RutaDistribucion rutaDistribucion = null;
												
								if(rutasDB != null && !rutasDB.isEmpty()) {
									rutaDistribucion = SSMUtils.getRuta(rutasDB, tipologia , globalProperties);
								}

								List<PasoDistribucion> pasosRuta = rutaDistribucion.getPasos();

								String pasoSiguiente = pasosRuta.get(0).getCentroDistribucion().getNombre();

								List<String> rutaCompleta = new ArrayList<>();
								rutaCompletaTexto += origenRdiRde + ";";

								rutaCompleta.add(origenRdiRde);

								for (PasoDistribucion pasoDistribucion : pasosRuta) {
									String nombrePaso = pasoDistribucion.getCentroDistribucion().getNombre();
									rutaCompleta.add(nombrePaso);
									rutaCompletaTexto += nombrePaso + ";";
								}

								rutas.put(llave, rutaCompletaTexto);

								log.info(raizLog + "La ruta completa en texto es:" + rutaCompletaTexto);

								context.getExtendedState().getVariables().put(Consts.RUTA_TOTAL_RDI_RDE, rutaCompleta);
								context.getExtendedState().getVariables().put(Consts.MAP_RUTAS__RDI_RDE, rutas);

								Map<String, ValorAtributo> atributos = new HashMap<String, ValorAtributo>();

								try {
									grupo = UsuarioCS.getGrupoByNombre(
											autenticacionCS.getAdminSoapHeader(), pasoSiguiente,
											wsdlsProps.getUsuario());
									idGrupo = grupo.getID();
									log.info(raizLog + "El grupo " + pasoSiguiente + " tiene ID= " + idGrupo);

								} catch (SOAPException  | IOException e) {
									log.error(raizLog + "ErrorCorrespondencia distribuirRdiRde getGrupoByNombre " + e, e);
									cargarError(context, e);
									context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
								
								}

								atributos.put(workflowProps.getAtributo().getRolDestino(), new ValorAtributo(idGrupo, TipoAtributo.USER));
								atributos.put("Estado", new ValorAtributo("CONTINUAR", TipoAtributo.STRING));
								atributos.put(workflowProps.getAtributo().getNumeroRadicado(),
										new ValorAtributo(numeroRadicado, TipoAtributo.STRING));
								atributos.put("Tipología", new ValorAtributo(tipologia, TipoAtributo.STRING));
								atributos.put("_workId", new ValorAtributo(workId, TipoAtributo.STRING));
								atributos.put("codigoRuta", new ValorAtributo(llave, TipoAtributo.INTEGER));
								atributos.put("Asunto", new ValorAtributo(asunto, TipoAtributo.STRING));

								try {
									log.info(raizLog + "INICIO iniciar WF distribucion RDI/RDE con rol siguiente:"
											+ pasoSiguiente);
									Workflow.iniciarWorkflowConAtributos(
											autenticacionCS.getAdminSoapHeader(), mapId, titulo,
											atributos, wsdlsProps.getWorkflow());
									log.info(raizLog + "FIN iniciar WF distribucion RDI/RDE con rol siguiente:"
											+ pasoSiguiente);

								} catch (NumberFormatException e) {
									cargarError(context, e);
									context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
									log.error(raizLog + "ErrorCorrespondencia distribuirRdiRde iniciarWorkflowConAtributos " + e , e);
								} catch (NoSuchElementException | SOAPException | IOException | WebServiceException e) {
									log.error(raizLog + "ErrorCorrespondencia distribuirRdiRde iniciarWorkflowConAtributos" + e , e);
									cargarError(context, e);
									context.getExtendedState().getVariables().put("ESTADOREINTENTO", "distribuirRdiRde");
									context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
								}
								log.info(raizLog + "El siguiente paso es:" + pasoSiguiente);
							}

							llave++;
						}
					}
				}

				log.info(raizLog + " Persistir Maquina Estados");

				try {
					persistContexto(context);
				} catch (Exception e) {
					log.info(raizLog + "ERROR: Ocurrio un error guardando la maquina de estados en la BD");
					log.error("Error operacion", e);
				}

				log.info(raizLog + "FIN estado distribuirRdiRde");

			};

		}

		/**
		 * Metodo que permite realizar la distribucion del memorando a todos los
		 * destinatarios y copias
		 * 
		 * Permite iniciar un workflow en el Content Server por cada uno de los
		 * destinatarios o copias
		 * 
		 * @return
		 */
		//TODO BORRAR OBSOLETO
		@Bean
		public Action<States, Events> distribuir() throws IOException {

			return (context) -> {
				
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog+"--------------------------------ENNTRO A ESTADO DISTRIBUIR_MEMORANDO -----------------------");
				if (!context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class)) {
					
					log.info(raizLog + " distribuir");

					@SuppressWarnings("unchecked")
					List<Empleado> destinatarios = (List<Empleado>) context.getExtendedState().get(Consts.DESTINATARIOS,
							Object.class);

					@SuppressWarnings("unchecked")
					List<Empleado> copias = (List<Empleado>) context.getExtendedState().get(Consts.COPIAS,
							Object.class);

					long idDocOriginal = context.getExtendedState().get(Consts.ID_DOC_RADICACION, Long.class);

					String tipoWfDoc = context.getExtendedState().get(Consts.TIPO_WF_DOCUMENTO, String.class);

					String numeroRadicado = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO,
							String.class);

					String workIdText = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);

					long workId = Long.valueOf(workIdText).longValue();

					long idSolicitante = context.getExtendedState().get(Consts.ID_SOLICITANTE, Long.class);

					log.info(raizLog + "DESTINATARIOS EN DISTRIBUIR_MEMORANDO: ");

					List<String> listaRoles = new ArrayList<>();

					List<String> listaNombresDestinatarios = new ArrayList<>();

					destinatarios.get(0).getNombre();

					for (Empleado empleado : destinatarios) {
						listaRoles.add(empleado.getRol());
						listaNombresDestinatarios.add(empleado.getNombre());
					}

					for (Empleado empleado : copias) {
						listaRoles.add(empleado.getRol());
						listaNombresDestinatarios.add(empleado.getNombre());
					}
					
					
					//listaRoles.stream().filter(unElemento -> unElemento.contains(Consts.ROL_SIN_COPIAS)).findFirst().
					//Se elimina el rol 00_PCR_COPIAS
					Iterator<String> iterRoles = listaRoles.iterator();
					while(iterRoles.hasNext()){
						String unRol = iterRoles.next();
						log.info(raizLog + "UnRol: " + unRol);
					}
					
					
					for(int i = 0; i < listaRoles.size(); i++){
						if(listaRoles.get(i).contains(Consts.ROL_SIN_COPIAS)){
							listaRoles.remove(i);
							log.info("Se eliminio el rol : " + Consts.ROL_SIN_COPIAS + "de la lista de roles");
						}
					}
					

					int totalDestinatarios = listaRoles.size();
					int totalConfirmadosDistribuir = 0;
					int totalRechazadosDistribuir = 0;

					context.getExtendedState().getVariables().put(Consts.LISTA_ROLES_DESTINATARIOS, listaRoles);

					context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS,
							totalDestinatarios);
					context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS_CONFIRMADOS,
							totalConfirmadosDistribuir);
					context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS_RECHAZADOS,
							totalRechazadosDistribuir);

					long mapId = 0L;
					if (tipoWfDoc.equalsIgnoreCase("Confidencial")) {
						log.info(raizLog + "Es confidencial, WF distribucion");
						mapId = workflowProps.getDistribucionMemorandoConfidencial();
					}else {
						mapId = workflowProps.getDistribucionMemorando();
					}

					

					log.info(raizLog + "INICIO actualizar estado correspondencia: DISTRIBUIR_MEMORANDO");
					
					try {
						
					actualizarEstadoCorrespondencia(raizLog, idDocOriginal,
							estadosProps.getDistribuido());

					} catch (SOAPException  e) {
						
						log.error(raizLog + "ErrorCorrespondencia distribuir obtenerIdGrupo" + e);							
						cargarError(context, e);
						//context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_MEMORANDO);
						context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_MEMORANDO);
						context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
					}
					
					log.info(raizLog + "FIN actualizar estado correspondencia: DISTRIBUIR_MEMORANDO");

					int contador = 0;
					int numberToKeep = 1;
					log.info(raizLog + "INICIO PROCESO DISTRIBUIR_MEMORANDO");
					Long idGrupo=new Long(0);
					
					

					for (String nombreGrupo : listaRoles) {

						String nombreDestinatario = listaNombresDestinatarios.get(contador);
						String titulo = numeroRadicado + " - " + nombreDestinatario;

						if (tipoWfDoc.equalsIgnoreCase("Confidencial")) {
							titulo += " - Confidencial";
						}
						
						log.info(raizLog + "Nombre grupo: " + nombreGrupo);
						
						try {
							idGrupo = obtenerIdGrupo(nombreGrupo, autenticacionCS.getAdminSoapHeader());
						} catch (IOException | SOAPException   e) {
							
							log.error(raizLog + "ErrorCorrespondencia distribuir obtenerIdGrupo" + e);							
							cargarError(context, e);
							context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_MEMORANDO);
							context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
						}
						
						Map<String, ValorAtributo> atributos = new HashMap<String, ValorAtributo>();
						atributos.put(workflowProps.getAtributo().getRolDestino(),
								new ValorAtributo(idGrupo, TipoAtributo.USER));
						atributos.put(workflowProps.getAtributo().getNumeroRadicado(),
								new ValorAtributo(numeroRadicado, TipoAtributo.STRING));
						atributos.put(workflowProps.getAtributo().getIdDocumentoOriginal(),
								new ValorAtributo(idDocOriginal, TipoAtributo.INTEGER));
						atributos.put(workflowProps.getAtributo().getNombreDestinatario(),
								new ValorAtributo(nombreDestinatario, TipoAtributo.STRING));
						atributos.put(workflowProps.getAtributo().getIdWorkflow(),
								new ValorAtributo(workId, TipoAtributo.INTEGER));
						atributos.put(workflowProps.getAtributo().getIdSolicitante(),
								new ValorAtributo(idSolicitante, TipoAtributo.USER));

						List<Long> listIdAdjuntos = new ArrayList<>();
						listIdAdjuntos.add(idDocOriginal);
						int distribuido = 0;
						int operacion=0;
											
						try {
							
							long process = 0L;
							
							distribuido = context.getExtendedState().get(Consts.DISTRIBUIDO, Integer.class)== null ? 0 : context.getExtendedState().get(Consts.DISTRIBUIDO, Integer.class);
							operacion =  context.getExtendedState().get(Consts.OPERACION, Integer.class) == null ? 0 : context.getExtendedState().get(Consts.OPERACION, Integer.class);
							
							log.info(raizLog + "Operacion: " + operacion + " distribuido: " + distribuido);
							
							if(distribuido == contador && operacion == 0){
							
								log.info(raizLog + "INICIO WF de Distribucion para el ROL:" + nombreGrupo
										+ " y el destinatario:" + nombreDestinatario);
								process = Workflow.iniciarWorkflowAtributosAdjuntos(
										autenticacionCS.getAdminSoapHeader(), mapId, titulo, atributos,
										listIdAdjuntos, wsdlsProps.getWorkflow());
								
								log.info(raizLog + "Flujo de Distribucion Iniciado: " + process);
								
								context.getExtendedState().getVariables().put(Consts.OPERACION, 1);
								context.getExtendedState().getVariables().put(Consts.ID_WORKFLOW_DIS, process);
								operacion++;
							
							}
																					
							long idDocAdjunto = 0L;
							
							if(distribuido == contador && operacion == 1){
								
								process = (context.getExtendedState().get(Consts.ID_WORKFLOW_DIS, Long.class)==null) ? 0 : context.getExtendedState().get(Consts.ID_WORKFLOW_DIS, Long.class) ;
								
								log.info(raizLog + "INICIO nueva forma de quitar categoria a adjunto");
																
								idDocAdjunto = (long) Workflow
										.obtenerValoresAtributo(autenticacionCS.getAdminSoapHeader(),
												process, "_idDocumentoCopia", wsdlsProps.getWorkflow())
										.get(0);
								
								context.getExtendedState().getVariables().put(Consts.OPERACION, 2);
								context.getExtendedState().getVariables().put(Consts.ID_DOCUMENTO_ADJUNTO_DIS, idDocAdjunto);
								operacion++;
													
							
							}
														
							Metadata metadataAdjunto = null;
							
							if(distribuido == contador && operacion == 2){
								
								idDocAdjunto = (context.getExtendedState().get(Consts.ID_DOCUMENTO_ADJUNTO_DIS, Long.class)== null) ?0: context.getExtendedState().get(Consts.ID_DOCUMENTO_ADJUNTO_DIS, Long.class);
								
								Node documentoAdjunto = ContenidoDocumento.getDocumentoById(
										autenticacionCS.getAdminSoapHeader(), idDocAdjunto,
										wsdlsProps.getDocumento());
	
								metadataAdjunto = documentoAdjunto.getMetadata();
	
								AttributeGroup categoriaCorrespondencia = null;
								AttributeGroup categoriaFirma = null;
								categoriaCorrespondencia = metadataAdjunto.getAttributeGroups().stream()
										.filter(unaCategoria -> unaCategoria.getDisplayName()
												.equals(categoriaProps.getCorrespondencia().getNombre()))
										.findFirst().get();
	
								categoriaFirma = metadataAdjunto.getAttributeGroups().stream()
										.filter(unaCategoria -> unaCategoria.getDisplayName()
												.equals(categoriaProps.getNombreCategoriaFirma()))
										.findFirst().get();
	
								metadataAdjunto.getAttributeGroups().clear();
	
								metadataAdjunto.getAttributeGroups().add(categoriaCorrespondencia);
								metadataAdjunto.getAttributeGroups().add(categoriaFirma);
	
								documentoAdjunto.setMetadata(metadataAdjunto);
								
								context.getExtendedState().getVariables().put(Consts.OPERACION, 3);
								context.getExtendedState().getVariables().put(Consts.METADATOS_ADJUNTOS_DIS, metadataAdjunto);
								operacion++;
							
							}
														
							if(distribuido == contador && operacion == 3){
							
								idDocAdjunto = (context.getExtendedState().get(Consts.ID_DOCUMENTO_ADJUNTO_DIS, Long.class)== null) ? 0 : context.getExtendedState().get(Consts.ID_DOCUMENTO_ADJUNTO_DIS, Long.class);
								metadataAdjunto =  (Metadata) context.getExtendedState().get(Consts.METADATOS_ADJUNTOS_DIS, Object.class);
								
								ContenidoDocumento.actualizarMetadataDocumento(
									autenticacionCS.getAdminSoapHeader(), idDocAdjunto, metadataAdjunto,
									wsdlsProps.getDocumento());

								log.info(raizLog + "FIN nueva forma de quitar categoria a adjunto");
								
								context.getExtendedState().getVariables().put(Consts.OPERACION, 4);
								operacion++;
							}
														
							if(distribuido == contador && operacion== 4){
							
								log.info(raizLog + "INICIO cambiar nombre Doc para cada copia" + nombreGrupo);
								idDocAdjunto = (context.getExtendedState().get(Consts.ID_DOCUMENTO_ADJUNTO_DIS, Long.class)== null) ? 0: context.getExtendedState().get(Consts.ID_DOCUMENTO_ADJUNTO_DIS, Long.class);

								ContenidoDocumento.cambiarNombreDocumento(
										autenticacionCS.getAdminSoapHeader(), idDocAdjunto,
										numeroRadicado + " - " + nombreDestinatario, wsdlsProps.getDocumento());
	
								log.info(raizLog + "FIN cambiar nombre Doc para cada copia" + nombreGrupo);
								
								context.getExtendedState().getVariables().put(Consts.OPERACION, 5);
								operacion++;
							}
														
							if(distribuido == contador && operacion== 5){
							
								log.info(raizLog + "INICIO eliminar versiones doc distribuido");
								idDocAdjunto = (context.getExtendedState().get(Consts.ID_DOCUMENTO_ADJUNTO_DIS, Long.class)== null) ? 0: context.getExtendedState().get(Consts.ID_DOCUMENTO_ADJUNTO_DIS, Long.class);

								ContenidoDocumento.eliminarVersionesDocumento(
										autenticacionCS.getAdminSoapHeader(), idDocAdjunto, numberToKeep,
										wsdlsProps.getDocumento());
								log.info(raizLog + "FIN eliminar versiones doc distribuido");
	
								log.info(raizLog + "FIN WF de Distribucion");
								
								int suma = contador;
								context.getExtendedState().getVariables().put(Consts.OPERACION, 0);
								context.getExtendedState().getVariables().put(Consts.DISTRIBUIDO, ++suma);
							
							}
							
						} catch (NumberFormatException e) {
							cargarError(context, new Exception(e.getMessage() + " Destinatarios distribuidos: " + distribuido + " de " + destinatarios.size() + " operacion: " + operacion + " de 5"));
							context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
							log.error(raizLog + "ErrorCorrespondencia distribuir getDocumentoById " + e , e);							
						} catch (NoSuchElementException | NullPointerException e) {
							cargarError(context, new Exception(e.getMessage() + " Destinatarios distribuidos: " + distribuido + " de " + destinatarios.size() + " operacion: " + operacion + " de 5"));
							context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
							log.error(raizLog + "ErrorCorrespondencia distribuir getDocumentoById " + e , e);	
						} catch (SOAPException | WebServiceException  | FileNotFoundException e) {
							log.error(raizLog + "ErrorCorrespondencia distribuir getDocumentoById " + e, e);
							cargarError(context, new Exception(e.getMessage() + " Destinatarios distribuidos: " + distribuido + " de " + destinatarios.size() + " operacion: " + operacion + " de 5"));
							context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_MEMORANDO);
							context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
						} catch (IOException e) {
							log.error(raizLog + "ErrorCorrespondencia distribuir getDocumentoById " + e);							
							cargarError(context, new Exception(e.getMessage() + " Destinatarios distribuidos: " + distribuido + " de " + destinatarios.size() + " operacion: " + operacion + " de 5"));
							context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_MEMORANDO);
							context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
						} catch (InterruptedException e) {
							cargarError(context, new Exception(e.getMessage() + " Destinatarios distribuidos: " + distribuido + " de " + destinatarios.size() + " operacion: " + operacion + " de 5"));
							context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
							log.error(raizLog + "ErrorCorrespondencia distribuir getDocumentoById " + e , e);							
						}
						contador++;
					}

					
				}
				log.info(raizLog + "FIN PROCESO DISTRIBUIR_MEMORANDO");
				
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}
			};
		}

		/**
		 * Metodo que permite realizar la distribucion de la carta a todos los
		 * destinatarios y copias
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> distribuirCarta2() {

			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog +
						"--------------------------------ENNTRO A ESTADO DISTRIBUIR_MEMORANDO CARTA-----------------------");
				
				if (!context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class)) {

					log.info(raizLog + " distribuir");

					long idDocOriginal = context.getExtendedState().get(Consts.ID_DOC_RADICACION, Long.class);

					String nombreDocOriginal = context.getExtendedState().get(Consts.NOMBRE_DOCUMENTO_ORIGINAL,
							String.class);

					String tipoWfDoc = context.getExtendedState().get(Consts.TIPO_WF_DOCUMENTO, String.class);

					String numeroRadicado = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO,
							String.class);

					String workIdText = context.getExtendedState().get(Consts.ID_WORKFLOW_RADICACION, String.class);

					String asunto = context.getExtendedState().get(Consts.ASUNTO, String.class);

					String conexionUsuarioCS = context.getExtendedState().get(Consts.CONEXION_CS_SOLICITANTE, String.class);

					MetadatosPlantilla cat = (MetadatosPlantilla) context.getExtendedState().get(Consts.METADATOS_PLANTILLA,
							Object.class);

					String fechaRadicacion = context.getExtendedState().get(Consts.FECHA_RADICACION, String.class);

					//@SuppressWarnings("unused")
					//long workId = Long.valueOf(workIdText).longValue();

					long idSolicitante = context.getExtendedState().get(Consts.ID_SOLICITANTE, Long.class);

					//Long idSticker = context.getExtendedState().get(Consts.ID_DOC_STICKER_CARTA, Long.class);
					
					String tipologia = context.getExtendedState().get(Consts.TIPOLOGIA_MEMO_CARTA, String.class);

					Long idCopiaCC = 0L;
					
					Long idCopiaCC_D = 0L;
					
					//Se actualiza el parent id de la carpeta oroginal en caso de que el usuario haya movido el documento
					try {
						actualizarIdParentOriginal(context, autenticacionCS.getAdminSoapHeader(),
								wsdlsProps.getDocumento());
					} catch (SOAPException | IOException e) {
						log.warn("No se pudo actualizar el id de la carpeta" + e.getMessage());
					}
					
					Long parentIdCarpetaOriginal = context.getExtendedState().get(Consts.PARENT_ID_ORIGINAL_MEMO_CARTA,
							Long.class);

					List<Long> listIdDocsAdjuntos;

					@SuppressWarnings({ "unchecked", "unused" })
					List<String> listNombresDocsPersonalizados = (List<String>) context.getExtendedState()
							.get(Consts.LISTA_NOMBRES_DOCS_PERSONALIZADOS_CARTA, Object.class);
					
					@SuppressWarnings("unchecked")
					List<Long> listIdDocsPersonalizados = (List<Long>) context.getExtendedState()
							.get(Consts.LISTA_ID_DOCS_PERSONALIZADOS_CARTA, Object.class);

					List<Empleado> destinatarios = cat.getDestinatarios();
					List<Empleado> copias = cat.getCopias();

					log.info(raizLog +"Destinatarios:");
					for (Empleado empleado : destinatarios) {

						log.info(raizLog +"Destinatario:" + empleado.getNombre());

					}

					for (Empleado empleado : copias) {

						log.info(raizLog +"Copia:" + empleado.getNombre());

					}

					if (listIdDocsPersonalizados == null) {

						listIdDocsPersonalizados = new ArrayList<>();
					}

					log.info(raizLog + "DESTINATARIOS EN DISTRIBUIR_MEMORANDO:");

					List<String> listaRoles = new ArrayList<>();

					List<String> listaNombresDestinatarios = new ArrayList<>();

					for (Empleado empleado : destinatarios) {
						listaRoles.add(empleado.getRol());
						listaNombresDestinatarios.add(empleado.getNombre());
					}

					for (Empleado empleado : copias) {
						listaRoles.add(empleado.getRol());
						listaNombresDestinatarios.add(empleado.getNombre());
					}

					int totalDestinatarios = listaRoles.size();
					int totalConfirmadosDistribuir = 0;
					int totalRechazadosDistribuir = 0;

					context.getExtendedState().getVariables().put(Consts.LISTA_ROLES_DESTINATARIOS, listaRoles);

					context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS,
							totalDestinatarios);
					context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS_CONFIRMADOS,
							totalConfirmadosDistribuir);
					context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS_RECHAZADOS,
							totalRechazadosDistribuir);

					log.info(raizLog + "INICIO PROCESO DISTRIBUIR_MEMORANDO");

					log.info(raizLog + "Se obtienen las copias compulsadas");

					List<Long> idDocsCopiasCompulsada = new ArrayList<>();

					String nombreCopiaCompulsadaOriginal = "(CC) " + nombreDocOriginal;

					context.getExtendedState().getVariables().put(Consts.LISTA_ID_DOCS_COPIAS_COMPULSADAS,
							idDocsCopiasCompulsada);
					
					log.info(raizLog +"ID copia nombre documento original: " + nombreCopiaCompulsadaOriginal + " iDcarpeta: " + parentIdCarpetaOriginal);
					
					try {
						idCopiaCC = ContenidoDocumento.obtenerDocumentoPorNombre(
								autenticacionCS.getAdminSoapHeader(), parentIdCarpetaOriginal, nombreCopiaCompulsadaOriginal, wsdlsProps.getDocumento());
						
						log.info(raizLog +"ID copia complusada: " + idCopiaCC);
						
					} catch (SOAPException | IOException | InterruptedException | NullPointerException e) {
						log.error(raizLog + "ErrorCorrespondencia distribuirCarta obtenerDocumentoPorNombre" + e);	
						cargarError(context, e);
						//context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
						context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
						context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
					} 					
					
					 boolean totalUserCorreo = true;
					 boolean totalDest = true;
					 boolean totalCopias = true;
					 
						for (Empleado empleado : destinatarios) {

							if(!empleado.getRol().contains("@")) {
								totalDest = false;
								break;
							}
						}
						
						
						for (Empleado empleado : copias) {

							if(!empleado.getRol().contains("@") && !empleado.getRol().contains("00_PCR_COPIAS")) {
								totalCopias = false;
								break;
							}
						}
						
						
						totalUserCorreo = (totalDest && totalCopias);
												
						log.info(raizLog + "INICIO actualizar estado correspondencia: DISTRIBUIR_MEMORANDO");

						try {
							actualizarEstadoCorrespondencia(raizLog, idDocOriginal,
									estadosProps.getDistribuido());
						} catch (SOAPException  e) {
							log.error(raizLog + "ErrorCorrespondencia Distribucioncarta actualizarEstadoCorrespondencia" + e);										
							cargarError(context, e);
							context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
							context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
						}

						log.info(raizLog + "FIN actualizar estado correspondencia: DISTRIBUIR_MEMORANDO");
						
						
						boolean personalizado = Boolean.parseBoolean(cat.getPersonalizarDocumento());
						
						if(personalizado){
							
							for(Empleado em : cat.getDestinatarios()){
								//Vamaya: Se añaden logs para saber la data alamacenada en la lista de destinatarios
								log.info(raizLog + "Nombre destinatario: ["+em.getNombre()+"]");
								log.info(raizLog + "Cargo destinatario: ["+em.getCargo()+"]");
								log.info(raizLog + "Rol destinatario: ["+em.getRol()+"]");
								log.info(raizLog + "Dependencia destinatario: ["+em.getDependencia()+"]");
								log.info(raizLog + "Id doc asociado: ["+em.getIdDocumento()+"]");
								log.info(raizLog + "Nombre doc asociado: ["+em.getNombreDocumento()+"]");
								//Fin bloque
								if(em.getIdDocumento()!= 0 && em.getNombreDocumento() != null){
									
									log.info(raizLog + "INICIO actualizar estado correspondencia: DISTRIBUIR_MEMORANDO");

									try {
										actualizarEstadoCorrespondencia(raizLog, em.getIdDocumento(),
												estadosProps.getDistribuido());
									} catch (SOAPException  e) {
										log.error(raizLog + "ErrorCorrespondencia Distribucioncarta actualizarEstadoCorrespondencia" + e);										
										cargarError(context, e);
										context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
										context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
									}

									log.info(raizLog + "FIN actualizar estado correspondencia: DISTRIBUIR_MEMORANDO");
									
								}
								
							}
							
						}
					
					if (cat.getTipoEnvio() != null && !cat.getTipoEnvio().equalsIgnoreCase("Mensajeria")) {

						log.info(raizLog +"Entro a NO es mensajeria");

						@SuppressWarnings("unused")
						String pcrOrigen = context.getExtendedState().get(Consts.PCR_ORIGEN_CARTA, String.class);
						@SuppressWarnings("unused")
						String cddOrigen = context.getExtendedState().get(Consts.CDD_ORIGEN_CARTA, String.class);
//						String nombreDependenciaFirmante = cat.getFirmantes().get(0).getDependencia();

						long mapId = workflowProps.getDistribucionCarta().getId();
						if (tipoWfDoc.equalsIgnoreCase("Confidencial")) {
							mapId = workflowProps.getDistribucionCarta().getIdConfidencial();
							log.info(raizLog + "Es confidencial CARTA, WF distribucion con MapId: " + mapId);

						}

						String titulo = "Distribución Carta - " + numeroRadicado;

						if (tipoWfDoc.equalsIgnoreCase("Confidencial")) {
							titulo += " - Confidencial";
						}

						if (copias != null && !copias.isEmpty()) {
							// destinatarios.addAll(copias);
						}
						
						int posicion=0;
						for (Empleado destino : destinatarios) {

							listIdDocsAdjuntos = new ArrayList<>();
							titulo = "Distribución Carta - " + numeroRadicado;
							
							if (!destino.getRol().contains("@")) {

								//listIdDocsAdjuntos.add(idSticker);
								
								String[] listaDestinos = destino.getRol().split(",");
								String direccionFinal = "";
								for(int i = 1;i< listaDestinos.length;i++) {
									direccionFinal += listaDestinos[i];
								}
								
								titulo =  "Distribución Carta - " + numeroRadicado + " - " + direccionFinal;
							}

							Long idDocumentoAdjunto = new Long(0);
							idDocumentoAdjunto = idCopiaCC;
							if (Boolean.valueOf(cat.getPersonalizarDocumento())) {

								if (destino.getNombreDocumento() != null) {

									nombreCopiaCompulsadaOriginal = "(CC) " + converUTF8(destino.getNombreDocumento());

									log.info(raizLog + "nombre copia CC [" + nombreCopiaCompulsadaOriginal
											+ "] - capeta [" + parentIdCarpetaOriginal + "]");

									try {
										idCopiaCC_D = ContenidoDocumento.obtenerDocumentoPorNombre(
												autenticacionCS.getAdminSoapHeader(), parentIdCarpetaOriginal,
												nombreCopiaCompulsadaOriginal, wsdlsProps.getDocumento());

										if (idCopiaCC_D == null) {

											log.info(raizLog + "Entro a ubicacion null");

											Long carpetaOriginal = context.getExtendedState()
													.get(Consts.PARENT_ID_ADJUNTOS_MEMO_CARTA_ORIGINAL, Long.class);

											log.info(raizLog + "Id original [" + carpetaOriginal + "]");

											idCopiaCC_D = ContenidoDocumento.obtenerDocumentoPorNombre(
													autenticacionCS.getAdminSoapHeader(), carpetaOriginal,
													nombreCopiaCompulsadaOriginal, wsdlsProps.getDocumento());

											if (idCopiaCC_D == null) {
												log.info(raizLog + "No se encontro en ubicación alternativa");
												throw new NullPointerException(
														"El servicio web getNodeByName no se encontró para los parametros:idParent ["
																+ parentIdCarpetaOriginal + "] - nombreDoc ["
																+ nombreCopiaCompulsadaOriginal + "]");
											}

										}

										idDocumentoAdjunto = idCopiaCC_D;

										if (destino.getRol().contains("@")) {
											listIdDocsAdjuntos.add(idDocumentoAdjunto);
										}

										log.info(raizLog + "ID copia complusada: " + idCopiaCC_D);

									} catch (SOAPException | IOException | InterruptedException e) {
										log.error(raizLog
												+ "ErrorCorrespondencia distribuirCarta obtenerDocumentoPorNombre" + e);
										cargarError(context, e);
										context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
										context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
									}

								} else if (destino.getRol().contains("@")) {

									// if (destino.getRol().contains("@")) {
									listIdDocsAdjuntos.add(idDocumentoAdjunto);
									// }
								}

							} else if (destino.getRol().contains("@")) {

								// if (destino.getRol().contains("@")) {
								listIdDocsAdjuntos.add(idDocumentoAdjunto);
								// }
							}

							
							Map<String, ValorAtributo> atributos = new HashMap<String, ValorAtributo>();
							String[] listaDestinos = destino.getRol().split(",");
							String rol = (destino.getRol().contains("@") ? listaDestinos[0] : destino.getRol());
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getNumeroRadicado(), new ValorAtributo(numeroRadicado, TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getTipologia(), new ValorAtributo("CA", TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getAsunto(), new ValorAtributo(SSMUtils.truncarString(asunto, Consts.TAMANO_MAXIMO_CS), TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getConexionCs(), new ValorAtributo(conexionUsuarioCS, TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getEsHilos(), new ValorAtributo("true", TipoAtributo.STRING));
//							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getNombreDependencia(),
//									new ValorAtributo(nombreDependenciaFirmante, TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getSolicitante(), new ValorAtributo(idSolicitante, TipoAtributo.USER));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getFechaRadicacion(), new ValorAtributo(fechaRadicacion, TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getWorkid(), new ValorAtributo(workIdText, TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getDestino(), new ValorAtributo(rol, TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getEsFisico(), new ValorAtributo(
									(destino.getRol().contains("@")) ? false : true, TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getRolDestino(), new ValorAtributo(idSolicitante, TipoAtributo.USER));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getEsCorreoCertificado(),
									new ValorAtributo(cat.getEsCorreoCertificado(), TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getIdDocumentoAdjunto(),
									new ValorAtributo(idDocumentoAdjunto.toString(), TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getNombreDestino(),
									new ValorAtributo((destino.getNombre() != null && !destino.getNombre().isEmpty())
											? destino.getNombre()
											: "  ", TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getEntidadDestino(),
									new ValorAtributo(
											(destino.getDependencia() != null && !destino.getDependencia().isEmpty())
													? destino.getDependencia()
													: "  ",
											TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getEsMensajeria(), new ValorAtributo(false, TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getCorreoCopia(),
									new ValorAtributo((!cat.getCorreoCopia().equalsIgnoreCase("CorreoCopia")
											? cat.getCorreoCopia()
											: " "), TipoAtributo.STRING));

							atributos.put("_idDocumentoOriginal", new ValorAtributo(idDocOriginal, TipoAtributo.INTEGER));
							
							context.getExtendedState().getVariables().put(Consts.DESTINO_INDIVIDUAL_CARTA,
									((destino.getNombre() != null && !destino.getNombre().isEmpty())
											? destino.getNombre()
											: destino.getDependencia()));

							log.info(raizLog + "INICIO WF Distribucion carta Hilos:");
							
							int distribuido=0;
							
							//BLoque de prueba
							log.info(raizLog + "Atributos:");
							
							atributos.forEach((k,v)->{log.info("Llave: " + k + " - tipo: " + v.getTipo() +" - Valor: "+ v.getValor() + " - Tamano: " + v.getValor().toString().length());});
							
							log.info(raizLog + "FIN Atributos:");
							
							//Fin bloque de prueba
							
							truncarValoresAtributosWFCarta(atributos);
							
							try {
								
								distribuido = context.getExtendedState().get(Consts.DISTRIBUIDO, Integer.class) == null ? 0 : context.getExtendedState().get(Consts.DISTRIBUIDO, Integer.class);
								

								log.info(raizLog + "distribuido: " + distribuido + " posicion: " + posicion);
								
								log.info(raizLog + "Tamano listIdDocsAdjuntos [" + listIdDocsAdjuntos.size() + "]");
								log.info(raizLog + "Ids adjuntos");
								listIdDocsAdjuntos.forEach(id -> {
									log.info(raizLog + "Id [" + id + "]");
								});

								if(distribuido==posicion) {
									
									long idFlujo = Workflow.iniciarWorkflowAtributosAdjuntos(
											autenticacionCS.getAdminSoapHeader(), mapId, titulo, atributos,
											listIdDocsAdjuntos, wsdlsProps.getWorkflow());

									log.info(raizLog + "FIN WF Distribucion carta Hilos - Flujo Iniciado: " + idFlujo);
									
									int suma = posicion;
									context.getExtendedState().getVariables().put(Consts.DISTRIBUIDO, ++suma);
									
								}
							

							} catch (NumberFormatException | NullPointerException e) {
								cargarError(context, e);
								context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
								log.error(raizLog + "ErrorCorrespondencia distribuirCarta iniciarWorkflowAtributosAdjuntos " + e, e);	
							} catch (NoSuchElementException e) {
								log.error(raizLog + "ErrorCorrespondencia distribuirCarta iniciarWorkflowAtributosAdjuntos " + e, e);	
								cargarError(context, e);
								context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
								context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
							} catch (SOAPException e) {
								log.error(raizLog + "ErrorCorrespondencia distribuirCarta iniciarWorkflowAtributosAdjuntos " + e, e);	
								cargarError(context, e);
								context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
								context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
							} catch (IOException e) {
								log.error(raizLog + "ErrorCorrespondencia distribuirCarta iniciarWorkflowAtributosAdjuntos " + e, e);	
								cargarError(context, e);
								context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
								context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
							}
							
							posicion++;
							
							try {
								persistContexto(context);
							} catch (Exception e) {
								log.error("Error operacion", e);
							}
						}
						
						context.getStateMachine().sendEvent(Events.FINALIZAR);


					} else {

						if ((cat.getEsImpresionArea() || tipoWfDoc.equalsIgnoreCase("Confidencial")) && !totalUserCorreo) {
							
							
							log.info(raizLog +"Entro a es confidencial o es impresion area");
							
							long idPcr = context.getExtendedState().get(Consts.ID_PCR_ORIGEN_CARTA, Long.class);
							long idCdd = context.getExtendedState().get(Consts.ID_CDD_ORIGEN_CARTA, Long.class);
							
							log.info(raizLog +"idPcr: " + idPcr + " idCdd: " + idCdd);
							
							listIdDocsAdjuntos = new ArrayList<>();

							long mapId = workflowProps.getDistribucionCarta().getId();
							if (tipoWfDoc.equalsIgnoreCase("Confidencial")) {
								mapId = workflowProps.getDistribucionCarta().getIdConfidencial();
								log.info(raizLog + "Es confidencial CARTA, WF distribucion ---" + workflowProps.getDistribucionCarta().getIdConfidencial() );

							}

							
							String titulo = "Distribución Carta - " + numeroRadicado;

							if (tipoWfDoc.equalsIgnoreCase("Confidencial")) {
								titulo += " - Confidencial";
								
							}

							//listIdDocsAdjuntos.add(idSticker);
							
							Long idDocumentoAdjunto = idCopiaCC;
//							String nombreDependenciaFirmante = cat.getFirmantes().get(0).getDependencia();
							List<Long> documentos = new ArrayList<Long>();
							documentos.add(idDocumentoAdjunto);
							
							personalizado = Boolean.parseBoolean(cat.getPersonalizarDocumento());
							
							if(personalizado){
								
								for(Empleado em: destinatarios){
									
									if(em.getNombreDocumento() != null){
										
										nombreCopiaCompulsadaOriginal = "(CC) " + converUTF8(em.getNombreDocumento());
										
										try {
											idCopiaCC_D = ContenidoDocumento.obtenerDocumentoPorNombre(
													autenticacionCS.getAdminSoapHeader(), parentIdCarpetaOriginal, nombreCopiaCompulsadaOriginal, wsdlsProps.getDocumento());
											
											documentos.add(idCopiaCC_D);
																						
										} catch (SOAPException | IOException | InterruptedException e) {
											log.error(raizLog + "ErrorCorrespondencia distribuirCarta obtenerDocumentoPorNombre" + e);	
											cargarError(context, e);
											context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
											context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
										} 			
										
									}
									
								}
								
							}
							
							Map<String, ValorAtributo> atributos = new HashMap<String, ValorAtributo>();

							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getNumeroRadicado(), new ValorAtributo(numeroRadicado, TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getTipologia(), new ValorAtributo("CA", TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getAsunto(), new ValorAtributo(SSMUtils.truncarString(asunto, Consts.TAMANO_MAXIMO_CS), TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getConexionCs(), new ValorAtributo(conexionUsuarioCS, TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getEsHilos(), new ValorAtributo("false", TipoAtributo.STRING));
//							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getNombreDependencia(),
//									new ValorAtributo(nombreDependenciaFirmante, TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getSolicitante(), new ValorAtributo(idSolicitante, TipoAtributo.USER));
							
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getIdPCR(), new ValorAtributo(idPcr, TipoAtributo.USER));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getIdCDD(), new ValorAtributo(idCdd, TipoAtributo.USER));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getSolicitante(), new ValorAtributo(idSolicitante, TipoAtributo.USER));
							
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getFechaRadicacion(), new ValorAtributo(fechaRadicacion, TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getWorkid(), new ValorAtributo(workIdText, TipoAtributo.STRING));
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getEsMensajeria(), new ValorAtributo(true, TipoAtributo.STRING));
							atributos.put("_documentoOriginal", new ValorAtributo(documentos, TipoAtributo.ITEM_REFERENCE,true));							
							atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getIdDocumentoAdjunto(),
									new ValorAtributo(idDocumentoAdjunto.toString(), TipoAtributo.STRING));
							
							truncarValoresAtributosWFCarta(atributos);

							log.info(raizLog + "INICIO WF Distribucion carta:");
							
							try {
								long idFlujo = Workflow.iniciarWorkflowAtributosAdjuntos(
										autenticacionCS.getAdminSoapHeader(), mapId, titulo, atributos,
										listIdDocsAdjuntos, wsdlsProps.getWorkflow());

								log.info(raizLog + "FIN WF Distribucion carta - Flujo Inicado: " + idFlujo);


							} catch (NumberFormatException e) {
								cargarError(context, e);
								context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
								log.error(raizLog + "ErrorCorrespondencia distribuirCarta iniciarWorkflowAtributosAdjuntos " + e, e);	
							} catch (NoSuchElementException e) {
								cargarError(context, e);
								context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
								log.error(raizLog + "ErrorCorrespondencia distribuirCarta iniciarWorkflowAtributosAdjuntos " + e, e);	
							} catch (SOAPException e) {
								log.error(raizLog + "ErrorCorrespondencia distribuirCarta iniciarWorkflowAtributosAdjuntos " + e, e);	
								cargarError(context, e);
								context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
								context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
							} catch (IOException e) {
								log.error(raizLog + "ErrorCorrespondencia distribuirCarta iniciarWorkflowAtributosAdjuntos " + e, e);	
								cargarError(context, e);
								context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
								context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
							}
							
							

							context.getStateMachine().sendEvent(Events.IR_ESPERAR_RESPUESTA_DISTRIBUIR_CDD_CARTA);

						} else {

							
							for (Empleado empleado : destinatarios) {

								log.info(raizLog +"Destinatario:" + empleado.getNombre());

							}

							for (Empleado empleado : copias) {

								log.info(raizLog +"Copia:" + empleado.getNombre());

							}

							String pcrOrigen = context.getExtendedState().get(Consts.PCR_ORIGEN_CARTA, String.class);
							String cddOrigen = context.getExtendedState().get(Consts.CDD_ORIGEN_CARTA, String.class);
													
//							String nombreDependenciaFirmante = cat.getFirmantes().get(0).getDependencia();

							long mapId = workflowProps.getDistribucionCarta().getId();
							if (tipoWfDoc.equalsIgnoreCase("Confidencial")) {
								mapId = workflowProps.getDistribucionCarta().getIdConfidencial();
								log.info(raizLog + "Es confidencial CARTA, WF distribucion 4594692");

							}

							String titulo = "Distribución Carta - " + numeroRadicado;

							if (tipoWfDoc.equalsIgnoreCase("Confidencial")) {
								titulo += " - Confidencial";
							}

							Member grupo = null;
							Long idGrupo = null;

							log.info(raizLog + "El origen que se esta consultando es:" + cddOrigen);
							CentroDistribucion centroDistribucionOrigen = centroDistribucionRepository
									.findByNombre(cddOrigen);
							log.info(raizLog + "El destinatario que se esta consultando es: EXTERNO");
							CentroDistribucion centroDistribucionDestino = centroDistribucionRepository
									.findByNombre("EXTERNO");
							
							List<PasoDistribucion> pasosRuta = new ArrayList<PasoDistribucion>();
//							RutaDistribucion rutaDistribucion = rutaDistribucionRepository
//									.findByCentroDistribucionOrigenAndCentroDistribucionDestino(
//											centroDistribucionOrigen, centroDistribucionDestino);
							
							List<RutaDistribucion> rutasDB = rutaDistribucionRepository
									.findByCentroDistribucionOrigenAndCentroDistribucionDestino(centroDistribucionOrigen, centroDistribucionDestino);
										
							RutaDistribucion rutaDistribucion = null;
											
							if(rutasDB != null && !rutasDB.isEmpty()) {
								rutaDistribucion = SSMUtils.getRuta(rutasDB, tipologia , globalProperties);
							}
							
							if(rutaDistribucion != null && !rutaDistribucion.getPasos().isEmpty()){
								
							log.info(raizLog + "Si existe ruta en BD para origen:" + cddOrigen + " y destino: EXTERNO");

							pasosRuta = rutaDistribucion.getPasos();
							
							}
							
							String rutaCompletaTexto = pcrOrigen + ";" + cddOrigen + ";";
							String pasoSiguiente = "";

							for (PasoDistribucion pasoDistribucion : pasosRuta) {
								String nombrePaso = pasoDistribucion.getCentroDistribucion().getNombre();
								rutaCompletaTexto += nombrePaso + ";";

								if (nombrePaso.contains(workflowProps.getAtributo().getMensajeria())){
									pasoSiguiente = nombrePaso;
								}
							}

							log.info(raizLog + "La ruta completa Carta es:" + rutaCompletaTexto);
							
							if(pasoSiguiente.equals("")){
								pasoSiguiente = cddOrigen;
							}
							
							context.getExtendedState().getVariables().put(Consts.RUTA_COMPLETA, rutaCompletaTexto);

							try {
								grupo = UsuarioCS.getGrupoByNombre(autenticacionCS.getUserSoapHeader(conexionUsuarioCS),
										pasoSiguiente, wsdlsProps.getUsuario());

								idGrupo = grupo.getID();
							} catch (SOAPException e) {
								log.error(raizLog + "ErrorCorrespondencia distribuirCarta getGrupoByNombre " + e, e);	
								cargarError(context, e);
								context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
								context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
							} catch (IOException e) {
								log.error(raizLog + "ErrorCorrespondencia distribuirCarta getGrupoByNombre" + e, e);	
								cargarError(context, e);
								context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
								context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
							}

							if (copias != null && !copias.isEmpty()) {
								// destinatarios.addAll(copias);
							}

							int posicion=0;
							for (Empleado destino : destinatarios) {
								listIdDocsAdjuntos = new ArrayList<>();
								Long idDocumentoAdjunto = new Long(0);
								
								titulo = "Distribución Carta - " + numeroRadicado;
								idDocumentoAdjunto = idCopiaCC;
								
								if (Boolean.valueOf(cat.getPersonalizarDocumento())) {
								
										if(destino.getNombreDocumento() != null){
										
											nombreCopiaCompulsadaOriginal = "(CC) " + converUTF8(destino.getNombreDocumento());
											
											log.info(raizLog +"nombre copia CC: " + nombreCopiaCompulsadaOriginal + " capeta: " + parentIdCarpetaOriginal);
											
											try {
												idCopiaCC_D = ContenidoDocumento.obtenerDocumentoPorNombre(
														autenticacionCS.getAdminSoapHeader(), parentIdCarpetaOriginal, nombreCopiaCompulsadaOriginal, wsdlsProps.getDocumento());
												
												idDocumentoAdjunto = idCopiaCC_D;
												
												if(destino.getRol().contains("@")){
													listIdDocsAdjuntos.add(idDocumentoAdjunto);
												}
												
												log.info(raizLog +"ID copia complusada: " + idCopiaCC_D);
												
											} catch (SOAPException | IOException | InterruptedException e) {
												log.error(raizLog + "ErrorCorrespondencia distribuirCarta obtenerDocumentoPorNombre" + e);	
												cargarError(context, e);
												context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
												context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
											} 
										
										} else {
											
											if(destino.getRol().contains("@")){
												listIdDocsAdjuntos.add(idDocumentoAdjunto);
											}
											
										}
																	

								} else {
									
									if(destino.getRol().contains("@")){
										listIdDocsAdjuntos.add(idDocumentoAdjunto);
				
									}

								}
								
								if (!destino.getRol().contains("@")) {

									//listIdDocsAdjuntos.add(idSticker);
									
									String[] listaDestinos = destino.getRol().split(",");
									String direccionFinal = "";
									for(int i = 1;i< listaDestinos.length;i++) {
										direccionFinal += listaDestinos[i];
									}
									
									titulo =  "Distribución Carta - " + numeroRadicado + " - " + direccionFinal;

								}
								
								List<Long> documentos = new ArrayList<Long>();
								documentos.add(idDocumentoAdjunto);
							
								Map<String, ValorAtributo> atributos = new HashMap<String, ValorAtributo>();
								
								String[] listaDestinos = destino.getRol().split(",");
								String rol = (destino.getRol().contains("@") ? listaDestinos[0] : destino.getRol());
								
								atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getNumeroRadicado(),
										new ValorAtributo(numeroRadicado, TipoAtributo.STRING));
								atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getTipologia(), new ValorAtributo("CA", TipoAtributo.STRING));
								atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getAsunto(), new ValorAtributo(SSMUtils.truncarString(asunto, Consts.TAMANO_MAXIMO_CS), TipoAtributo.STRING));
								atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getConexionCs(), new ValorAtributo(conexionUsuarioCS, TipoAtributo.STRING));
								atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getEsHilos(), new ValorAtributo("true", TipoAtributo.STRING));
//								atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getNombreDependencia(),
//										new ValorAtributo(nombreDependenciaFirmante, TipoAtributo.STRING));

								atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getSolicitante(), new ValorAtributo(idSolicitante, TipoAtributo.USER));
								atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getFechaRadicacion(),
										new ValorAtributo(fechaRadicacion, TipoAtributo.STRING));
								atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getWorkid(), new ValorAtributo(workIdText, TipoAtributo.STRING));
								atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getDestino(), new ValorAtributo(rol, TipoAtributo.STRING));
								atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getEsFisico(), new ValorAtributo(
										(destino.getRol().contains("@")) ? false : true, TipoAtributo.STRING));
								atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getRolDestino(), new ValorAtributo(idGrupo, TipoAtributo.USER));
								atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getEsCorreoCertificado(),
										new ValorAtributo(cat.getEsCorreoCertificado(), TipoAtributo.STRING));
								atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getIdDocumentoAdjunto(),
										new ValorAtributo(idDocumentoAdjunto.toString(), TipoAtributo.STRING));
								atributos.put("_documentoOriginal", new ValorAtributo(documentos, TipoAtributo.ITEM_REFERENCE,true));
								atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getNombreDestino(),
										new ValorAtributo(
												(destino.getNombre() != null && !destino.getNombre().isEmpty())
														? destino.getNombre()
														: "  ",
												TipoAtributo.STRING));
								atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getEntidadDestino(),
										new ValorAtributo((destino.getDependencia() != null
												&& !destino.getDependencia().isEmpty()) ? destino.getDependencia()
														: "  ",
												TipoAtributo.STRING));
								atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getEsMensajeria(), new ValorAtributo(true, TipoAtributo.STRING));
								atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getCorreoCopia(),
										new ValorAtributo((!cat.getCorreoCopia().equalsIgnoreCase("CorreoCopia")
												? cat.getCorreoCopia()
												: " "), TipoAtributo.STRING));
								
								truncarValoresAtributosWFCarta(atributos);

								log.info(raizLog + "INICIO WF Distribucion carta Hilos:");
								int distribuido=0;
								try {
									
									distribuido = context.getExtendedState().get(Consts.DISTRIBUIDO, Integer.class) == null ? 0 : context.getExtendedState().get(Consts.DISTRIBUIDO, Integer.class);
									
									log.info(raizLog + "distribuido: " + distribuido + " posicion: " + posicion);
									
									if(distribuido==posicion) {
										
										long idFlujo = Workflow.iniciarWorkflowAtributosAdjuntos(
												autenticacionCS.getAdminSoapHeader(), mapId, titulo,
												atributos, listIdDocsAdjuntos, wsdlsProps.getWorkflow());
										
										int suma = posicion;
										context.getExtendedState().getVariables().put(Consts.DISTRIBUIDO, ++suma);
										log.info(raizLog + "FIN WF Distribucion carta Hilos - Flujo Inicado: " + idFlujo);
									
									}

								} catch (NumberFormatException e) {
									cargarError(context, e);
									context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
									log.error(raizLog + "ErrorCorrespondencia distribuirCarta iniciarWorkflowAtributosAdjuntos " + e, e);	
								} catch (NoSuchElementException e) {
									cargarError(context, e);
									log.error(raizLog + "ErrorCorrespondencia distribuirCarta iniciarWorkflowAtributosAdjuntos " + e, e);	
									cargarError(context, e);
									context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
									context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
								} catch (SOAPException | WebServiceException e) {
									log.error(raizLog + "ErrorCorrespondencia distribuirCarta iniciarWorkflowAtributosAdjuntos " + e, e);	
									cargarError(context, e);
									context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
									context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
								} catch (IOException e) {
									log.error(raizLog + "ErrorCorrespondencia distribuirCarta iniciarWorkflowAtributosAdjuntos " + e, e);	
									cargarError(context, e);
									context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.DISTRIBUIR_CARTA);
									context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
								}

								context.getExtendedState().getVariables().put(Consts.DESTINO_INDIVIDUAL_CARTA,
										((destino.getNombre() != null && !destino.getNombre().isEmpty())
												? destino.getNombre()
												: destino.getDependencia()));
								posicion++;
								try {
									persistContexto(context);
								} catch (Exception e) {
									log.info(raizLog + "ERROR: Ocurrio un error guardando la maquina de estados en la BD");
									log.error("Error operacion", e);
								}

							}

							context.getStateMachine().sendEvent(Events.IR_ESPERAR_RESPUESTA_DISTRIBUIR_CARTA);

						}
						
						

					}

				}
				log.info(raizLog + "FIN PROCESO DISTRIBUIR_MEMORANDO CARTA");
				
				
			};
		}

	


		/**
		 * Metodo de espera del resultado de la distribucion
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> esperarDistribucion() {

			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + "-------------------ESTADO esperar Distribuir --------------------------");

				log.info(raizLog + " esperarDistribucion");

				log.info(raizLog + " Persistir Maquina Estados");

				//SE ALMACENA EN BD EL CONTEXTO DE LA SSM
				try {
					log.info("Guardando en la tabla MAQUINA");
					persistContexto(context);

				} catch (Exception e) {
					log.error(raizLog + "ERROR GUARDANDO LA MAQUINA [" + context.getStateMachine().getId() + "] en la base de datos", e);
				}

			};
		}

		/**
		 * Metodo que permite esperar el resultado de la distribucion RDI/RDE
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> esperarDistribucionRdiRde() {

			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog
						+ "------------------------------ESTADO esperar Distribuir RDI/RDE -----------------------------------");

				log.info(raizLog + " esperarDistribucionRdiRde");

				log.info(raizLog + " Persistir Maquina Estados");

				try {
					persistContexto(context);
				} catch (Exception e) {
					log.info(raizLog + "ERROR: Ocurrio un error guardando la maquina de estados en la BD");
					log.error("Error operacion", e);
				}

			};
		}
		

		

		/**
		 * Metodo que permite obtener el resultado de la distribucion RDI/RDE
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> obtenerResultadoDistribuirRdiRde() {

			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog
						+ "---------------------------------ESTADO Obteniendo resultado distribuir RDI/RDE----------------------------------");
				
				if (!context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class)) {

					try {
						
						//Vamaya: Se añade mensaje para saber estado actual de la máquina antes de lanzar los eventos de transición
						String estadoInicial = context.getStateMachine().getState().getId().toString();
						log.info(raizLog + "Estado actual: " + estadoInicial);
						//-----------
						
						//log.info(raizLog + " obtenerResultadoDistribuirRdiRde");

						String numeroRadicado = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO,
								String.class);

						log.info(raizLog + "Numero Radicado:" + numeroRadicado);

						String resultadoDistribucion = context.getMessageHeaders().get(Consts.RESULTADO_DISTRIBUIR_RDI_RDE,
								String.class);						
						context.getExtendedState().getVariables().put(Consts.RESULTADO_DISTRIBUIR_RDI_RDE,
								resultadoDistribucion);
						log.info(raizLog + "Resultado Distribucion:" + resultadoDistribucion);

						String rolActualDistribucion = context.getMessageHeaders().get(Consts.ROL_ACTUAL_DISTRIBUIR_RDI_RDE,
								String.class);
						context.getExtendedState().getVariables().put(Consts.ROL_ACTUAL_DISTRIBUIR_RDI_RDE,
								rolActualDistribucion);
						log.info(raizLog + "Rol Actual Distribucion:" + rolActualDistribucion);
						
						Long codigoRuta = context.getMessageHeaders().get(Consts.CODIGO_RUTA, Long.class);
						context.getExtendedState().getVariables().put(Consts.CODIGO_RUTA, codigoRuta);
						log.info(raizLog + "Codigo Ruta:" + codigoRuta);

						long idDocOriginal = context.getExtendedState().get(Consts.ID_DOC_RADICACION, Long.class);
						log.info(raizLog + "Id Documento Original:" + idDocOriginal);

						String origenRdiRde = context.getExtendedState().get(Consts.ORIGEN_RDI_RDE, String.class);
						log.info(raizLog + "Origen RdiRde:" + origenRdiRde);
						
						String origenRdiRdePcr = context.getExtendedState().get(Consts.ORIGEN_RDI_RDE_PCR, String.class);
						log.info(raizLog + "Origen RdiRdePcr:" + origenRdiRdePcr);

						String tipologia = context.getExtendedState().get(Consts.TIPOLOGIA_RDI_RDE, String.class);
						log.info(raizLog + "Tipologia:" + tipologia);

						Boolean esMensajeria = context.getExtendedState().get(Consts.ES_MENSAJERIA_RDI_RDE, Boolean.class);
						log.info(raizLog + "EsMensajeria:" + esMensajeria);
						
						Long idRuta = context.getMessageHeaders().get(Consts.ID_RUTA, Long.class);
						@SuppressWarnings("unchecked") //Vamaya: Se añade el suppress Warnings
						Map<Long, RespuestaEntrega> distribucionRuta =  context.getExtendedState().get(Consts.DISTRIBUCION_RUTA, HashMap.class);
						log.info(raizLog + "IdRuta:" + idRuta);


						@SuppressWarnings("unchecked")
						List<String> destinatariosPcr = (List<String>) context.getExtendedState()
								.get(Consts.LIST_PCR_DESTINO_RDI_RDE, Object.class);
						log.info(raizLog + "Destinatarios Pcr:" + destinatariosPcr);

						@SuppressWarnings("unchecked")
						Map<Integer, String> mapRutas = (Map<Integer, String>) context.getExtendedState()
								.get(Consts.MAP_RUTAS__RDI_RDE, Object.class);

						int totalDestinatariosConfirmados = context.getExtendedState()
								.get(Consts.NUMERO_TOTAL_DESTINATARIOS_CONFIRMADOS, Integer.class);
						log.info(raizLog + "Total Destinatarios Confirmados:" + totalDestinatariosConfirmados);
						int totalDestinatariosRechazados = context.getExtendedState()
								.get(Consts.NUMERO_TOTAL_DESTINATARIOS_RECHAZADOS, Integer.class);
						log.info(raizLog + "Total Destinatarios Rechazados:" + totalDestinatariosRechazados);
						

						log.info(raizLog + "resultadoDistribuicion:" + resultadoDistribucion + " rolActualDistribucion:"
								+ rolActualDistribucion + " esMensajeria:" + esMensajeria + " codigoRuta:" + codigoRuta);

						Boolean finalizoDistribucion = false;
						int totalDestinatarios = mapRutas.size();

						@SuppressWarnings("unlikely-arg-type") //Vamaya: Se añade Suppress Warnings
						String rutaCadenaTexto = mapRutas.get(codigoRuta);
						List<String> rutaCompletaSplit = Arrays.asList(rutaCadenaTexto.split("\\s*;\\s*"));

						if (!esMensajeria) {
							if (resultadoDistribucion.equalsIgnoreCase(estadosProps.getConfirmado())) {

								log.info(raizLog + "Distribucion directa y confirmada ");

								if (rolActualDistribucion.equalsIgnoreCase(origenRdiRdePcr)) {

									finalizoDistribucion = false;

									String rolSiguiente = rutaCompletaSplit.get(1);
									
									RespuestaEntrega r = new RespuestaEntrega();
									r.setRolsigueinte(rolSiguiente);
									r.setFinalizacion(finalizoDistribucion);
									r.setPasoCurrier(false);
									
									distribucionRuta.put(idRuta, r);
																		
									context.getExtendedState().getVariables().put(Consts.DISTRIBUCION_RUTA, distribucionRuta);
									
//									context.getExtendedState().getVariables().put(Consts.ROL_SIGUIENTE_DISTRIBUIR_RDI_RDE,
//											rolSiguiente);
//									context.getExtendedState().getVariables().put(Consts.FINALIZO_DISTRIBUCION_RDI_RDE,
//											finalizoDistribucion);
								} else {

									finalizoDistribucion = true;

									totalDestinatariosConfirmados++;
									
									RespuestaEntrega r = new RespuestaEntrega();
									r.setRolsigueinte("");
									r.setFinalizacion(finalizoDistribucion);
									r.setPasoCurrier(false);
									
									distribucionRuta.put(idRuta, r);
																		
									context.getExtendedState().getVariables().put(Consts.DISTRIBUCION_RUTA, distribucionRuta);
									
//									context.getExtendedState().getVariables().put(Consts.ROL_SIGUIENTE_DISTRIBUIR_RDI_RDE,
//											"");
//									context.getExtendedState().getVariables().put(Consts.FINALIZO_DISTRIBUCION_RDI_RDE,
//											finalizoDistribucion);
								}

								log.info(raizLog + "totalDestinatariosConfirmados:" + totalDestinatariosConfirmados);
								log.info(raizLog + "totalDestinatariosRechazados:" + totalDestinatariosRechazados);

								context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS_CONFIRMADOS,
										totalDestinatariosConfirmados);
								context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS_RECHAZADOS,
										totalDestinatariosRechazados);

							} else if (!resultadoDistribucion.equalsIgnoreCase(estadosProps.getConfirmado())) {

								log.info(raizLog + "Distribucion directa y rechazada ");

								if (rolActualDistribucion.equalsIgnoreCase(origenRdiRdePcr)) {

									totalDestinatariosRechazados++;

									finalizoDistribucion = true;
									
									RespuestaEntrega r = new RespuestaEntrega();
									r.setRolsigueinte("");
									r.setFinalizacion(finalizoDistribucion);
									r.setPasoCurrier(false);
									
									distribucionRuta.put(idRuta, r);
									
									context.getExtendedState().getVariables().put(Consts.DISTRIBUCION_RUTA, distribucionRuta);
									
//									context.getExtendedState().getVariables().put(Consts.ROL_SIGUIENTE_DISTRIBUIR_RDI_RDE,
//											"");
//									context.getExtendedState().getVariables().put(Consts.FINALIZO_DISTRIBUCION_RDI_RDE,
//											finalizoDistribucion);
									
								} else {

									finalizoDistribucion = false;
									
									RespuestaEntrega r = new RespuestaEntrega();
									r.setRolsigueinte(origenRdiRdePcr);
									r.setFinalizacion(finalizoDistribucion);
									r.setPasoCurrier(false);
											
									distribucionRuta.put(idRuta, r);
									
									context.getExtendedState().getVariables().put(Consts.DISTRIBUCION_RUTA, distribucionRuta);

//									context.getExtendedState().getVariables().put(Consts.ROL_SIGUIENTE_DISTRIBUIR_RDI_RDE,
//											origenRdiRdePcr);
//									context.getExtendedState().getVariables().put(Consts.FINALIZO_DISTRIBUCION_RDI_RDE,
//											finalizoDistribucion);

									
								}

							} else if (resultadoDistribucion.equalsIgnoreCase("aceptaRechazo")) {

								log.info(raizLog + "Distribucion directa y aceptaRechazo ");

								finalizoDistribucion = true;
																
								RespuestaEntrega r = new RespuestaEntrega();
								r.setRolsigueinte("");
								r.setFinalizacion(finalizoDistribucion);
								r.setPasoCurrier(false);
								
								distribucionRuta.put(idRuta, r);
								
								context.getExtendedState().getVariables().put(Consts.DISTRIBUCION_RUTA, distribucionRuta);
								
//								context.getExtendedState().getVariables().put(Consts.ROL_SIGUIENTE_DISTRIBUIR_RDI_RDE, "");
//								context.getExtendedState().getVariables().put(Consts.FINALIZO_DISTRIBUCION_RDI_RDE,
//										finalizoDistribucion);

							} else if (resultadoDistribucion.equalsIgnoreCase("Reenviar")) {

								log.info(raizLog + "Distribucion directa y Reenviar ");
								
								
								RespuestaEntrega r = new RespuestaEntrega();
								r.setRolsigueinte(destinatariosPcr.get(0));
								r.setFinalizacion(finalizoDistribucion);
								r.setPasoCurrier(false);
												
								distribucionRuta.put(idRuta, r);
								
								context.getExtendedState().getVariables().put(Consts.DISTRIBUCION_RUTA, distribucionRuta);
								
//								context.getExtendedState().getVariables().put(Consts.ROL_SIGUIENTE_DISTRIBUIR_RDI_RDE,
//										destinatariosPcr.get(0));
//								context.getExtendedState().getVariables().put(Consts.FINALIZO_DISTRIBUCION_RDI_RDE,
//										finalizoDistribucion);

							}

							if (totalDestinatariosConfirmados == totalDestinatarios && totalDestinatariosRechazados == 0
									&& !tipologia.equalsIgnoreCase("RDE")) {

								log.info(raizLog + "Todos los destinatarios CONFIRMARON");
								log.info(raizLog + "Total Destinatarios:" + totalDestinatarios);
								log.info(raizLog + "Total Confirmados:" + totalDestinatariosConfirmados);
								log.info(raizLog + "Total Rechazados:" + totalDestinatariosRechazados);

								log.info(raizLog + "INICIO actualizar estado correspondencia ORIGEN: CONFIRMADO");

								try {
									actualizarEstadoCorrespondencia(raizLog, idDocOriginal,
											estadosProps.getConfirmado());
								} catch (Exception e) {
									log.error(raizLog + "ErrorCorrespondencia obtenerResultadoDistribuirRdiRde actualizarEstadoCorrespondencia " + e);							
									cargarError(context, e);
									//context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.OBTENER_RESULTADO_DISTRIBUIR_RDI_RDE);
									context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.OBTENER_RESULTADO_DISTRIBUIR_RDI_RDE);
									context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
								}

								log.info(raizLog + "FIN actualizar estado correspondencia ORIGEN: CONFIRMADO");

								context.getStateMachine().sendEvent(Events.FINALIZAR_RDI_RDE);

							} else if (totalDestinatariosRechazados == totalDestinatarios
									&& totalDestinatariosConfirmados == 0) {

								log.info(raizLog + "Todos los destinatarios RECHAZARON");
								log.info(raizLog + "Total Destinatarios:" + totalDestinatarios);
								log.info(raizLog + "Total Confirmados:" + totalDestinatariosConfirmados);
								log.info(raizLog + "Total Rechazados:" + totalDestinatariosRechazados);

								log.info(raizLog + "INICIO actualizar estado correspondencia ORIGEN: RECHAZADO");

								try {
									actualizarEstadoCorrespondencia(raizLog, idDocOriginal,
											estadosProps.getRechazo());
								} catch (Exception e) {
									log.error(raizLog + "ErrorCorrespondencia obtenerResultadoDistribuirRdiRde actualizarEstadoCorrespondencia " + e);							
									cargarError(context, e);
									context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.OBTENER_RESULTADO_DISTRIBUIR_RDI_RDE);
									context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
								}

								log.info(raizLog + "FIN actualizar estado correspondencia ORIGEN: RECHAZADO");

								context.getStateMachine().sendEvent(Events.FINALIZAR_RDI_RDE);

							} else if (totalDestinatariosConfirmados + totalDestinatariosRechazados == totalDestinatarios
									&& totalDestinatariosRechazados != 0 && !tipologia.equalsIgnoreCase("RDE")) {

								log.info(raizLog + "Rechazado por algunos");
								log.info(raizLog + "Total Destinatarios:" + totalDestinatarios);
								log.info(raizLog + "Total Confirmados:" + totalDestinatariosConfirmados);
								log.info(raizLog + "Total Rechazados:" + totalDestinatariosRechazados);

								log.info(
										raizLog + "INICIO actualizar estado correspondencia ORIGEN: RECHAZADO POR ALGUNOS");

								try {
									actualizarEstadoCorrespondencia(raizLog, idDocOriginal,
											estadosProps.getRechazadoAlgunos());
								} catch (Exception e) {
									log.error(raizLog + "ErrorCorrespondencia obtenerResultadoDistribuirRdiRde actualizarEstadoCorrespondencia " + e);							
									cargarError(context, e);
									context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.OBTENER_RESULTADO_DISTRIBUIR_RDI_RDE);
									context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
								}

								log.info(raizLog + "FIN actualizar estado correspondencia ORIGEN: RECHAZADO POR ALGUNOS");

								context.getStateMachine().sendEvent(Events.FINALIZAR_RDI_RDE);

							} else {

								log.info(raizLog
										+ "No se han recibido todas las respuestas de Distribucion ... continuar estado ESPERA DISTRIBUIR_MEMORANDO");
								log.info(raizLog + "Total Destinatarios:" + totalDestinatarios);
								log.info(raizLog + "Total Confirmados:" + totalDestinatariosConfirmados);
								log.info(raizLog + "Total Rechazados:" + totalDestinatariosRechazados);

								context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS_CONFIRMADOS,
										totalDestinatariosConfirmados);
								context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS_RECHAZADOS,
										totalDestinatariosRechazados);

								context.getStateMachine().sendEvent(Events.VOLVER_ESPERAR_DISTRIBUIR);
							}
						} else {

							int sizeRuta = rutaCompletaSplit.size();

							log.info(raizLog + "el tamaño de la lista de rutas es:" + sizeRuta);

							String rolSiguiente = "";
							finalizoDistribucion = false;
							
							RespuestaEntrega r = new RespuestaEntrega();
							r.setRolsigueinte(rolSiguiente);
							r.setFinalizacion(finalizoDistribucion);
							r.setPasoCurrier(false);
							
							distribucionRuta.put(idRuta, r);
							
							context.getExtendedState().getVariables().put(Consts.DISTRIBUCION_RUTA, distribucionRuta);
//							
//							context.getExtendedState().getVariables().put(Consts.ROL_SIGUIENTE_DISTRIBUIR_RDI_RDE,
//									rolSiguiente);
							Boolean pasoPorCourrier = false;
//							context.getExtendedState().getVariables().put(Consts.PASO_POR_COURRIER, pasoPorCourrier);

							log.info(raizLog + "La ruta completa es:");

							if(rutaCompletaSplit.get(0).equals(rolActualDistribucion) && rutaCompletaSplit.get(0).equals(rutaCompletaSplit.get(rutaCompletaSplit.size()-1))){
									finalizoDistribucion = true;
									totalDestinatariosConfirmados++;
									rolSiguiente = "XXX";
									
							} else {

								for (String rol : rutaCompletaSplit) {
									log.info(raizLog + "rol:" + rol);
									if (rol.equalsIgnoreCase(rolActualDistribucion)) {
										int poscicion = rutaCompletaSplit.indexOf(rolActualDistribucion);

										if (resultadoDistribucion.equalsIgnoreCase(estadosProps.getConfirmado())
												&& poscicion != sizeRuta - 1) {
											rolSiguiente = rutaCompletaSplit.get(poscicion + 1);
										}

										else if (resultadoDistribucion.equalsIgnoreCase(estadosProps.getConfirmado())
												&& poscicion == sizeRuta - 1) {

											finalizoDistribucion = true;
											totalDestinatariosConfirmados++;
											rolSiguiente = "XXX";

										}

										else if (resultadoDistribucion.equalsIgnoreCase(estadosProps.getRechazo())) {

											// aqui se verifica si ya paso por CURRIER en la ruta
											for (int i = poscicion - 1; i >= 0; i--) {

												if (rutaCompletaSplit.get(i)
														.contains(workflowProps.getAtributo().getMensajeria())) {

													pasoPorCourrier = true;
												}

											}

											if (pasoPorCourrier) {

												rolSiguiente = "";
												finalizoDistribucion = true;
												totalDestinatariosRechazados++;

											} else {

												if (poscicion != 0) {
													rolSiguiente = rutaCompletaSplit.get(poscicion - 1);
													finalizoDistribucion = false;
												} else {
													rolSiguiente = "";
													finalizoDistribucion = true;
													totalDestinatariosRechazados++;

												}
											}

										}
										log.info(raizLog + "La posicion del rol que encontro es:" + poscicion);

									}
								}

							}
							
							log.info(raizLog + "El rol siguiente es:" + rolSiguiente);
							log.info(raizLog + "Paso por Courrier:" + pasoPorCourrier);
							
							r = new RespuestaEntrega();
							r.setRolsigueinte(rolSiguiente);
							r.setFinalizacion(finalizoDistribucion);
							r.setPasoCurrier(false);
												
							distribucionRuta.put(idRuta, r);
							context.getExtendedState().getVariables().put(Consts.DISTRIBUCION_RUTA, distribucionRuta);
							
//							context.getExtendedState().getVariables().put(Consts.PASO_POR_COURRIER, pasoPorCourrier);
//							context.getExtendedState().getVariables().put(Consts.ROL_SIGUIENTE_DISTRIBUIR_RDI_RDE,
//									rolSiguiente);
//							context.getExtendedState().getVariables().put(Consts.FINALIZO_DISTRIBUCION_RDI_RDE,
//									finalizoDistribucion);
							context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS_CONFIRMADOS,
									totalDestinatariosConfirmados);
							context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS_RECHAZADOS,
									totalDestinatariosRechazados);

							if (totalDestinatariosConfirmados == totalDestinatarios && totalDestinatariosRechazados == 0
									&& !tipologia.equalsIgnoreCase("RDE")) {

								log.info(raizLog + "Todos los destinatarios CONFIRMARON");
								log.info(raizLog + "Total Destinatarios:" + totalDestinatarios);
								log.info(raizLog + "Total Confirmados:" + totalDestinatariosConfirmados);
								log.info(raizLog + "Total Rechazados:" + totalDestinatariosRechazados);

								log.info(raizLog + "INICIO actualizar estado correspondencia ORIGEN: CONFIRMADO");

								try {
									actualizarEstadoCorrespondencia(raizLog, idDocOriginal,
											estadosProps.getConfirmado());
								} catch (Exception e) {
									log.error(raizLog + "Error Correspondencia obtenerResultadoDistribuirRdiRde actualizarEstadoCorrespondencia " + e);							
									cargarError(context, e);
									context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.OBTENER_RESULTADO_DISTRIBUIR_RDI_RDE);
									context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
								}

								log.info(raizLog + "FIN actualizar estado correspondencia ORIGEN: CONFIRMADO");
								
								//Vamaya: Se añaden mensajes para saber eventos de transición
								
								log.info(raizLog + "Se lanza evento FINALIZAR_RDI_RDE");
								
								context.getStateMachine().sendEvent(Events.FINALIZAR_RDI_RDE); //pasa a estado FINALIZADO
								//-----------
								

							} else if (totalDestinatariosRechazados == totalDestinatarios
									&& totalDestinatariosConfirmados == 0) {

								log.info(raizLog + "Todos los destinatarios RECHAZARON");
								log.info(raizLog + "Total Destinatarios:" + totalDestinatarios);
								log.info(raizLog + "Total Confirmados:" + totalDestinatariosConfirmados);
								log.info(raizLog + "Total Rechazados:" + totalDestinatariosRechazados);

								log.info(raizLog + "INICIO actualizar estado correspondencia ORIGEN: RECHAZADO");

								try {
									actualizarEstadoCorrespondencia(raizLog, idDocOriginal,
											estadosProps.getRechazo());
								} catch (Exception e) {
									log.error(raizLog + "ErrorCorrespondencia obtenerResultadoDistribuirRdiRde actualizarEstadoCorrespondencia " + e);							
									cargarError(context, e);
									context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.OBTENER_RESULTADO_DISTRIBUIR_RDI_RDE);
									context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
								}

								log.info(raizLog + "FIN actualizar estado correspondencia ORIGEN: RECHAZADO");
								
								//Vamaya: Se añaden mensajes para saber eventos de transición
								
								log.info(raizLog + "Se lanza evento FINALIZAR_RDI_RDE");
								
								context.getStateMachine().sendEvent(Events.FINALIZAR_RDI_RDE);
								//-----------


							} else if (totalDestinatariosConfirmados + totalDestinatariosRechazados == totalDestinatarios
									&& totalDestinatariosRechazados != 0 && !tipologia.equalsIgnoreCase("RDE")) {

								log.info(raizLog + "Rechazado por algunos");
								log.info(raizLog + "Total Destinatarios:" + totalDestinatarios);
								log.info(raizLog + "Total Confirmados:" + totalDestinatariosConfirmados);
								log.info(raizLog + "Total Rechazados:" + totalDestinatariosRechazados);

								log.info(
										raizLog + "INICIO actualizar estado correspondencia ORIGEN: RECHAZADO POR ALGUNOS");

								try {
									actualizarEstadoCorrespondencia(raizLog, idDocOriginal,
											estadosProps.getRechazadoAlgunos());
								} catch (Exception e) {
									log.error(raizLog + "ErrorCorrespondencia obtenerResultadoDistribuirRdiRde actualizarEstadoCorrespondencia " + e);							
									cargarError(context, e);
									context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.OBTENER_RESULTADO_DISTRIBUIR_RDI_RDE);
									
									//Vamaya: Se añaden mensajes para saber eventos de transición
									log.info(raizLog + "Se lanza evento ERROR_REINTENTO");
									
									context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
									//-----------
									
								}

								log.info(raizLog + "FIN actualizar estado correspondencia ORIGEN: RECHAZADO POR ALGUNOS");
								
								//Vamaya: Se añaden mensajes para saber eventos de transición
								log.info(raizLog + "Se lanza evento FINALIZAR_RDI_RDE");
								
								context.getStateMachine().sendEvent(Events.FINALIZAR_RDI_RDE);
								//-----------


							} else {//para carta

								log.info(raizLog
										+ "No se han recibido todas las respuestas de Distribucion ... continuar estado ESPERA DISTRIBUIR_MEMORANDO");
								log.info(raizLog + "Total Destinatarios:" + totalDestinatarios);
								log.info(raizLog + "Total Confirmados:" + totalDestinatariosConfirmados);
								log.info(raizLog + "Total Rechazados:" + totalDestinatariosRechazados);

								context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS_CONFIRMADOS,
										totalDestinatariosConfirmados);
								context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS_RECHAZADOS,
										totalDestinatariosRechazados);
								
								//Vamaya: Se añaden mensajes para saber eventos de transición
								log.info(raizLog + "Se lanza evento VOLVER_ESPERAR_DISTRIBUIR");
								
								context.getStateMachine().sendEvent(Events.VOLVER_ESPERAR_DISTRIBUIR);
								//---------
								
							}
								
								
							}


						context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS_CONFIRMADOS,
								totalDestinatariosConfirmados);
						context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS_RECHAZADOS,
								totalDestinatariosRechazados);
						
						//Evento que se lanza para que los RDE (No manejados por la  lógica anterior) cambien de estado
						
						//Vamaya: Se añaden mensajes para saber eventos de transición
						log.info(raizLog + "Se lanza evento VOLVER_ESPERAR_DISTRIBUIR_RDI_RDE");
						
						context.getStateMachine().sendEvent(Events.VOLVER_ESPERAR_DISTRIBUIR_RDI_RDE);
						//---------
						
					} catch (Exception e) {
						cargarError(context, e);
						//Vamaya: Se añaden mensajes para saber eventos de transición
						log.info(raizLog + "Se lanza evento IR_FINALIZADO_ERRORES");
						
						context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES); 
						//-----------
						
						log.error(raizLog + "ErrorCorrespondencia obtenerResultadoDistribuirRdiRde " + e, e);
					}

					}
				
				try {
					persistContexto(context);
				} catch (Exception e) {
					log.info(raizLog +
							"ERROR: Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}
				
			};
		}
		
		
		
		
		/**
		 * Metodo que permite ir obteniendo los resultados de la distribucion
		 * 
		 * Permite asignar un estado global a la categoria del documento original una
		 * vez se reciban todos los resultados de la distribucion
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> obtenerResultadoDistribuir() {

			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog
						+ "------------------ESTADO Obteniendo resultado distribuir------------------------------------");
				
				if (!context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class)) {
				
				try {
					log.info(raizLog + " obtenerResultadoDistribuir");

					boolean resultadoDistribuir = context.getMessageHeaders().get(Consts.RESULTADO_DISTRIBUIR,
							Boolean.class);

					int totalDestinatarios = context.getExtendedState().get(Consts.NUMERO_TOTAL_DESTINATARIOS,
							Integer.class);
					int totalDestinatariosConfirmados = context.getExtendedState()
							.get(Consts.NUMERO_TOTAL_DESTINATARIOS_CONFIRMADOS, Integer.class);
					long idDocOriginal = context.getExtendedState().get(Consts.ID_DOC_RADICACION, Long.class);
					int totalDestinatariosRechazados = context.getExtendedState()
							.get(Consts.NUMERO_TOTAL_DESTINATARIOS_RECHAZADOS, Integer.class);

					if (resultadoDistribuir)
						totalDestinatariosConfirmados++;
					else
						totalDestinatariosRechazados++;

					if (totalDestinatariosConfirmados == totalDestinatarios && totalDestinatariosRechazados == 0) {

						log.info(raizLog + "Todos los destinatarios CONFIRMARON");
						log.info(raizLog + "Total Destinatarios:" + totalDestinatarios);
						log.info(raizLog + "Total Confirmados:" + totalDestinatariosConfirmados);
						log.info(raizLog + "Total Rechazados:" + totalDestinatariosRechazados);

						context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS_CONFIRMADOS,
								totalDestinatariosConfirmados);
						context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS_RECHAZADOS,
								totalDestinatariosRechazados);

						log.info(raizLog + "INICIO actualizar estado correspondencia ORIGEN: CONFIRMADO");

						try {
							actualizarEstadoCorrespondencia(raizLog, idDocOriginal,
									estadosProps.getConfirmado());
							
							log.info(raizLog + "FIN actualizar estado correspondencia ORIGEN: CONFIRMADO");

							context.getStateMachine().sendEvent(Events.FINALIZAR);
							
						} catch (Exception e) {
							log.error(raizLog + "ErrorCorrespondencia obtenerResultadoDistribuir actualizarEstadoCorrespondencia " + e);							
							cargarError(context, e);
							context.getExtendedState().getVariables().put(Consts.ESTADO_REINTENTO, States.OBTENER_RESULTADO_DISTRIBUIR);
							context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
						}

						

					} else if (totalDestinatariosRechazados == totalDestinatarios && totalDestinatariosConfirmados == 0) {

						log.info(raizLog + "Todos los destinatarios RECHAZARON");
						log.info(raizLog + "Total Destinatarios:" + totalDestinatarios);
						log.info(raizLog + "Total Confirmados:" + totalDestinatariosConfirmados);
						log.info(raizLog + "Total Rechazados:" + totalDestinatariosRechazados);

						context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS_CONFIRMADOS,
								totalDestinatariosConfirmados);
						context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS_RECHAZADOS,
								totalDestinatariosRechazados);

						log.info(raizLog + "INICIO actualizar estado correspondencia ORIGEN: RECHAZADO");

						actualizarEstadoCorrespondencia(raizLog, idDocOriginal, estadosProps.getRechazo());

						log.info(raizLog + "FIN actualizar estado correspondencia ORIGEN: RECHAZADO");

						context.getStateMachine().sendEvent(Events.FINALIZAR);

					} else if (totalDestinatariosConfirmados + totalDestinatariosRechazados == totalDestinatarios
							&& totalDestinatariosRechazados != 0) {

						log.info(raizLog + "Rechazado por algunos");
						log.info(raizLog + "Total Destinatarios:" + totalDestinatarios);
						log.info(raizLog + "Total Confirmados:" + totalDestinatariosConfirmados);
						log.info(raizLog + "Total Rechazados:" + totalDestinatariosRechazados);

						context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS_CONFIRMADOS,
								totalDestinatariosConfirmados);
						context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS_RECHAZADOS,
								totalDestinatariosRechazados);

						log.info(raizLog + "INICIO actualizar estado correspondencia ORIGEN: RECHAZADO POR ALGUNOS");

						actualizarEstadoCorrespondencia(raizLog, idDocOriginal,
								estadosProps.getRechazadoAlgunos());

						log.info(raizLog + "FIN actualizar estado correspondencia ORIGEN: RECHAZADO POR ALGUNOS");

						context.getStateMachine().sendEvent(Events.FINALIZAR);

					} else {

						log.info(raizLog
								+ "No se han recibido todas las respuestas de Distribucion ... continuar estado ESPERA DISTRIBUIR_MEMORANDO");
						log.info(raizLog + "Total Destinatarios:" + totalDestinatarios);
						log.info(raizLog + "Total Confirmados:" + totalDestinatariosConfirmados);
						log.info(raizLog + "Total Rechazados:" + totalDestinatariosRechazados);

						context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS_CONFIRMADOS,
								totalDestinatariosConfirmados);
						context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS_RECHAZADOS,
								totalDestinatariosRechazados);

						context.getStateMachine().sendEvent(Events.VOLVER_ESPERAR_DISTRIBUIR);
					}

					context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS, totalDestinatarios);
					context.getExtendedState().getVariables().put(Consts.NUMERO_TOTAL_DESTINATARIOS_CONFIRMADOS,
							totalDestinatariosConfirmados);

					log.info(raizLog + " Persistir Maquina Estados");
					
				} catch (Exception e) {
					cargarError(context, e);
					context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
					log.error(raizLog + "ErrorCorrespondencia obtenerResultadoDistribuir " + e , e);
				}
				
				}
				
				try {
					persistContexto(context);
				} catch (Exception e) {
					log.info(raizLog + "ERROR: Ocurrio un error guardando la maquina de estados en la BD");
					log.error("Error operacion", e);
				}

			};
		}

		/**
		 * Metodo que permite asignar el estado Anulado a la categoria correspondencia
		 * de un documento al cual se le cancelo la firma
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> firmaCanceladaAnularDoc() {

			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);

				log.info(raizLog + "Entro Estado Firma Cancelada Anular Doc");

				log.info(raizLog + " firmaCanceladaAnularDoc");

				//String respuestaFirma = context.getMessageHeaders().get(Consts.MENSAJE_RESPUESTA_FIRMA, String.class);
				//String usuarioCancela = context.getMessageHeaders().get(Consts.NOMBRE_USUARIO_CANCELA, String.class);
				
				String respuestaFirma = context.getExtendedState().get(Consts.MENSAJE_RESPUESTA_FIRMA, String.class);
				String usuarioCancela = context.getExtendedState().get(Consts.NOMBRE_USUARIO_CANCELA, String.class);
				
				//String conexionUsuarioCS = globalProperties.getConexionUsuarioCS();

				String comentarios = "La firma fue cancelada por el usuario [" + usuarioCancela + "]";
				if (!respuestaFirma.isEmpty())
					comentarios = respuestaFirma;

				String numeroRadicado = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO, String.class);
				long idSolicitante = context.getExtendedState().get(Consts.ID_SOLICITANTE, Long.class);
				long idDocOriginal = context.getExtendedState().get(Consts.ID_DOC_RADICACION, Long.class);
				String asunto = context.getExtendedState().get(Consts.ASUNTO, String.class);
				//String nombreFirmante = context.getExtendedState().get(Consts.NOMBRE_FIRMANTE, String.class);

				String titulo = "Cancelación Firma" + "-" + numeroRadicado;
				
				
				log.info(raizLog + " impresion variables numeroRadicado "+ numeroRadicado + " idSolicitante " +  idSolicitante +  " idDocOriginal " + idDocOriginal + " nombre usuario cancela " + usuarioCancela );  
				
				
				Map<String, ValorAtributo> atributos = new HashMap<String, ValorAtributo>();
				atributos.put(workflowProps.getAtributo().getIdSolicitante(),
						new ValorAtributo(idSolicitante, TipoAtributo.USER));
				atributos.put(workflowProps.getAtributo().getNumeroRadicadoCancelacion(),
						new ValorAtributo(numeroRadicado, TipoAtributo.STRING));
				atributos.put(workflowProps.getAtributo().getAsunto(), new ValorAtributo(SSMUtils.truncarString(asunto, Consts.TAMANO_MAXIMO_CS), TipoAtributo.STRING));
				atributos.put(workflowProps.getAtributo().getComentarios(),
						new ValorAtributo(comentarios, TipoAtributo.STRING));
				atributos.put("_nombreRemitente", new ValorAtributo(usuarioCancela, TipoAtributo.STRING));
				
				if (log.isDebugEnabled() || log.isTraceEnabled()) {
					log.debug(raizLog + "Parametros enviados al workflow");
					Iterator<Entry<String, ValorAtributo>> iterAtributos = atributos.entrySet().iterator();
					while (iterAtributos.hasNext()) {
						Entry<String, ValorAtributo> unAtributo = iterAtributos.next();
						log.debug(raizLog + "Atributo etiqueta: " + unAtributo.getKey());
						log.debug(raizLog + "Atributo valor: " + unAtributo.getValue().getValor());
					}
				}

				log.info(raizLog + "INICIO actualizar estado correspondencia firma cancelada: ANULADO");

				try {
					actualizarEstadoCorrespondencia(raizLog, idDocOriginal, estadosProps.getAnulado());
				} catch (SOAPException  e) {
					log.error(raizLog + "ErrorCorrespondencia obtenerRespuestaDistribuirCddRdiRde iniciarWorkflowConAtributos" + e);										
					cargarError(context, e);
					context.getStateMachine().sendEvent(Events.ERROR_REINTENTO);
				}

				log.info(raizLog + "FIN actualizar estado correspondencia firma cancelada: ANULADO");

				try {
					log.info(raizLog + "INICIO iniciar WF de cancelacion firma");
					Workflow.iniciarWorkflowConAtributos(autenticacionCS.getAdminSoapHeader(),
							workflowProps.getNotificacionCorrespondencia(), titulo, atributos, wsdlsProps.getWorkflow());
					log.info(raizLog + "FIN iniciar WF de cancelacion firma");
				} catch (NumberFormatException | NoSuchElementException | SOAPException | IOException | WebServiceException e) {
					cargarError(context, e);
					context.getStateMachine().sendEvent(Events.IR_FINALIZADO_ERRORES);
					log.error(raizLog + "ErrorCorrespondencia firmaCanceladaAnularDoc " + e , e);				
				}
				
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}

			};

		}
		
		/**
		 * Metodo que permite administrar los reintentos
		 * @return
		 */
		@Bean
		public Action<States, Events> verificarReintento() {
			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " entro VerificarReintento");

				Integer reintentoActual = context.getExtendedState().get(Consts.REINTENTO, Integer.class);
				String numeroRadicado = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO,
						String.class);
				
				if (reintentoActual == null) {
					reintentoActual = 1;
				} else if (reintentoActual <= 0) {
					reintentoActual = 1;
				}
				
				int proximoReintento = reintentoActual + 1;

				context.getExtendedState().getVariables().put(Consts.REINTENTO, proximoReintento);

				//String estado = context.getExtendedState().get("ESTADOREINTENTO", String.class);
				//String estado = context.getExtendedState().get(Consts.ESTADO_REINTENTO, String.class);
				States estado = context.getExtendedState().get(Consts.ESTADO_REINTENTO, States.class);
				
				long tiempoParam = globalProperties.getTiempoReintento();
				long tiempoMinutos = tiempoParam / 60000;
				
				try {
					Thread.sleep(globalProperties.getTiempoReintento());
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
				if ((reintentoActual + 1) < globalProperties.getCantidadReintentos()) {

					log.info(raizLog + "Reintento numero: " + reintentoActual);
					log.info(raizLog + "Proxima ejecucion en : " + tiempoMinutos + " minutos aproximadamente");
					
					switch(estado) {
					
					case DISTRIBUIR_MEMORANDO:
						context.getStateMachine().sendEvent(Events.IR_INTENTAR_DISTRIBUIR);
						break;
						
					case OBTENER_RESULTADO_DISTRIBUIR:
						context.getStateMachine().sendEvent(Events.IR_INTENTAR_OBTENER_RESULTADO_DISTRIBUIR);
						break;
						
					case VALIDAR_INICIAR_FORMA_DISTRIBUIR_RDI_RDE:
						context.getStateMachine().sendEvent(Events.IR_VALIDAR_INICIAR_FORMA_DISTRIBUIR_RDI_RDE);
						break;
					
					case OBTENER_RESPUESTA_DISTRIBUIR_CDD_RDI_RDE:
						context.getStateMachine().sendEvent(Events.VOLVER_OBTENER_RESPUESTA_DISTRIBUIR_CDD_RDI_RDE);
						break;
						
					case OBTENER_RESULTADO_DISTRIBUIR_RDI_RDE:
						context.getStateMachine().sendEvent(Events.VOLVER_OBTENIENDO_RESULTADO_DISTRIBUIR_RDI_RDE);
						break;
						
					case CARGAR_CATEGORIA_FORMULARIO_DER:
						context.getStateMachine().sendEvent(Events.VOLVER_CARGAR_CATEGORIA_FORMULARIO_DER);
						break;
						
					case CARGAR_CATEGORIA_DOC_DIGITALIZADO_DER:
						context.getStateMachine().sendEvent(Events.VOLVER_CARGAR_CATEGORIA_DOC_DIGITALIZADO_DER);
						break;
						
					case CARGAR_CATEGORIA_CORREO_DER:
						context.getStateMachine().sendEvent(Events.VOLVER_CARGAR_CATEGORIA_CORREO_DER);
						break;
						
					case DISTRIBUIR_DER:
						context.getStateMachine().sendEvent(Events.VOLVER_DISTRIBUIR_DER);
						break;

					case DISTRIBUIR_CARTA:
						context.getStateMachine().sendEvent(Events.VOLVER_ESPERAR_DISTRIBUIR_CARTA);
						break;
						
					case OBTENER_RESPUESTA_DISTRIBUIR_CDD_CARTA:
						context.getStateMachine().sendEvent(Events.VOLVER_OBTENER_RESPUESTA_DISTRIBUIR_CDD_CARTA);
						break;
						
					case OBTENER_RESPUESTA_DISTRIBUIR_CARTA:
						context.getStateMachine().sendEvent(Events.VOLVER_OBTENER_RESPUESTA_DISTRIBUIR_CARTA);
						break;
						
					default:
						//TODO DEFAULT IMPLEMENTATION
						break;
						
					}
					
				//Penultima ejecucion reintento	
				} else if ((reintentoActual + 1) == globalProperties.getCantidadReintentos()) {
					
					log.warn(raizLog + "Penultima ejecucion");
					log.info(raizLog + "Reintento numero: " + reintentoActual);
					log.info(raizLog + "Proxima ejecucion en : " + tiempoMinutos + " minutos aproximadamente");

					String error = "";

					try {
						error = context.getExtendedState().get(globalProperties.getMensajeError(), String.class);

						if (!org.springframework.util.StringUtils.hasText(error)) {
							error = "NO ESPECIFICADO";
						}

					} catch (Exception e) {
						error = "NO ESPECIFICADO";
					}

					String asuntoMaquina = "Fallo maquina Correspondencia " + numeroRadicado;
					String idMquina = context.getStateMachine().getId();

					log.debug(raizLog + "Id Maquina: " + idMquina);
					log.debug(raizLog + "Asunto: " + asuntoMaquina);

					Map<String, ValorAtributo> atributos = new HashMap<String, ValorAtributo>();

					atributos.put("asunto",	new ValorAtributo("Ultima ejecucion de reintentos - maquina No: " + idMquina, TipoAtributo.STRING));
					atributos.put("error", new ValorAtributo("ULTIMA EJECUCION DE REINTENTO ALCANZADA - Maquina No: " + idMquina + " - Proxima ejecucion en: "
									+ tiempoMinutos + "  minutos aproximadamente" + " - MENSAJE DE ERROR: " + error,
							TipoAtributo.MULTILINE));

					String email = globalProperties.getMailAdmin();
					atributos.put("correoDestinatario", new ValorAtributo(email, TipoAtributo.STRING));
					atributos.put("idMaquina", new ValorAtributo(Long.parseLong(idMquina), TipoAtributo.INTEGER));
					atributos.put("asuntoMaquina", new ValorAtributo(asuntoMaquina, TipoAtributo.STRING));

					long longIdMap = Long.parseLong(globalProperties.getMapIdNotificacionAdmin());

					log.info(raizLog + "Inciando IdWorkflow de notificacion Administrador");

					long idWorkflow;
					try {
						idWorkflow = Workflow.iniciarWorkflowConAtributos(autenticacionCS.getAdminSoapHeader(),
								longIdMap, "Error Reintento maquina " + idMquina, atributos,  wsdlsProps.getWorkflow());
						log.info(raizLog + "Se inicia la instancia de workflow: " + idWorkflow);
					} catch (SOAPException e) {
						log.error(raizLog + "Error al iniciar el IdWorkflow con MapID : " + longIdMap, e);
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.ERROR_OPERACION_FINAL_SIN_REINTENTO);
					} catch (NumberFormatException | NoSuchElementException | IOException e) {
						log.error(raizLog +  "Error al iniciar el IdWorkflow con MapID : " + longIdMap, e);
						cargarError(context, e);
						context.getStateMachine().sendEvent(Events.ERROR_OPERACION_FINAL_SIN_REINTENTO);
					}
					
					switch(estado) {
					
					case DISTRIBUIR_MEMORANDO:
						context.getStateMachine().sendEvent(Events.IR_INTENTAR_DISTRIBUIR);
						break;
						
					case OBTENER_RESULTADO_DISTRIBUIR:
						context.getStateMachine().sendEvent(Events.IR_INTENTAR_OBTENER_RESULTADO_DISTRIBUIR);
						break;
						
					case VALIDAR_INICIAR_FORMA_DISTRIBUIR_RDI_RDE:
						context.getStateMachine().sendEvent(Events.IR_VALIDAR_INICIAR_FORMA_DISTRIBUIR_RDI_RDE);
						break;
					
					case OBTENER_RESPUESTA_DISTRIBUIR_CDD_RDI_RDE:
						context.getStateMachine().sendEvent(Events.VOLVER_OBTENER_RESPUESTA_DISTRIBUIR_CDD_RDI_RDE);
						break;
						
					case OBTENER_RESULTADO_DISTRIBUIR_RDI_RDE:
						context.getStateMachine().sendEvent(Events.VOLVER_OBTENIENDO_RESULTADO_DISTRIBUIR_RDI_RDE);
						break;
						
					case CARGAR_CATEGORIA_FORMULARIO_DER:
						context.getStateMachine().sendEvent(Events.VOLVER_CARGAR_CATEGORIA_FORMULARIO_DER);
						break;
						
					case CARGAR_CATEGORIA_DOC_DIGITALIZADO_DER:
						context.getStateMachine().sendEvent(Events.VOLVER_CARGAR_CATEGORIA_DOC_DIGITALIZADO_DER);
						break;
						
					case CARGAR_CATEGORIA_CORREO_DER:
						context.getStateMachine().sendEvent(Events.VOLVER_CARGAR_CATEGORIA_CORREO_DER);
						break;
						
					case DISTRIBUIR_DER:
						context.getStateMachine().sendEvent(Events.VOLVER_DISTRIBUIR_DER);
						break;

					case DISTRIBUIR_CARTA:
						context.getStateMachine().sendEvent(Events.VOLVER_ESPERAR_DISTRIBUIR_CARTA);
						break;
						
					case OBTENER_RESPUESTA_DISTRIBUIR_CDD_CARTA:
						context.getStateMachine().sendEvent(Events.VOLVER_OBTENER_RESPUESTA_DISTRIBUIR_CDD_CARTA);
						break;
						
					case OBTENER_RESPUESTA_DISTRIBUIR_CARTA:
						context.getStateMachine().sendEvent(Events.VOLVER_OBTENER_RESPUESTA_DISTRIBUIR_CARTA);
						break;
						
					default:
						//TODO DEFAULT IMPLEMENTATION
						break;
						
					}
					
				} else if (reintentoActual == globalProperties.getCantidadReintentos()) {//SE ALCANZÓ EL MÁXIMO NÚMERO DE REINTENTOS
					log.error(raizLog + "Numero maximo de reintentos alcanzado, total reintentos: ["
							+ globalProperties.getCantidadReintentos()+"]");
					context.getStateMachine().sendEvent(Events.NUMERO_MAXIMO_REINTENTOS_ALCANZADO);
				}

			};
		}
		
		/**
		 * Metodo pra enviar correos de notificacion de errores
		 * @return
		 */
		@Bean
		public Action<States, Events> notificarError() {
			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + " notificarErrorAdmin");
				
				String numeroRadicado =  (context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO,	String.class)) == null ? Consts.SIN_NUMERO_RADICADO :  (context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO,String.class));
				String asuntoMaquina = "Fallo Maquina Correspondencia " + numeroRadicado;			
				String idMquina = context.getStateMachine().getId();
				
				log.debug(raizLog + "Id Maquina: " + idMquina);
				log.debug(raizLog + "Asunto: " + idMquina);				
				
				Map<String, ValorAtributo> atributos = new HashMap<String, ValorAtributo>();				
				
				atributos.put("asunto", new ValorAtributo("Error en la maquina No: " + idMquina, TipoAtributo.STRING));
				
				String error = context.getExtendedState().get(globalProperties.getMensajeError(), String.class);
				
				String estado = context.getExtendedState().get(globalProperties.getEstadoError(), String.class);
				String mensaje = "Estado: " + estado + "\n";
				mensaje = mensaje + " - Error: " + error;
				
				atributos.put("error", new ValorAtributo(mensaje, TipoAtributo.MULTILINE));
				String email = globalProperties.getMailAdmin();
				atributos.put("correoDestinatario", new ValorAtributo(email, TipoAtributo.STRING));
				atributos.put("idMaquina", new ValorAtributo(Long.parseLong(idMquina), TipoAtributo.INTEGER));
				atributos.put("asuntoMaquina", new ValorAtributo(asuntoMaquina, TipoAtributo.STRING));
				
				long longIdMap = Long.parseLong(globalProperties.getMapIdNotificacionAdmin());
				
				log.info(raizLog + "atributos de notificacion Administrador mensaje [" + mensaje + "]  -  email ["+ email + "]  -  idMquina [" + idMquina + "]  -  asuntoMaquina [" + asuntoMaquina+"]");
				
				
				log.info(raizLog + "Iniciando IdWorkflow de notificacion Administrador [" + longIdMap+"]");
				
				long idWorkflow;
				try {
					idWorkflow = Workflow.iniciarWorkflowConAtributos(autenticacionCS.getAdminSoapHeader(), longIdMap,
							"Error maquina " + idMquina, atributos, wsdlsProps.getWorkflow());
					context.getStateMachine().sendEvent(Events.WORKFLOW_INICIADO);
					log.info(raizLog + "Se inicia La instancia de workflow: [" + idWorkflow+"]");
				} catch (SOAPException e) {
					log.error(raizLog + "Error al iniciar el IdWorkflow con MapID : " + longIdMap, e);
				} catch (NumberFormatException | NoSuchElementException | IOException e) {
					log.error(raizLog +  "Error al iniciar el IdWorkflow con MapID : " + longIdMap, e);
				}
				
				
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}
			
			};
		}
		
		
		
		
		/**
		 * Metodo que permite recibir peticiones de digitliazar y calidad por adelantado
		 * @return
		 */
		//ESTADO OBSOLETO
		/*
		@Bean
		public Action<States, Events> obtenerResultadoDER() {
			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);

				log.info(raizLog + "------------------------------INICIO ESTADO obtenerResultadoDER-------------------------");

				if (!context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class)) {
						
					String paso_digitalizar = context.getMessageHeaders().get(Consts.PASO_DIGITALIZAR, String.class);
					if(paso_digitalizar != null && !paso_digitalizar.isEmpty()) {	
						log.info(raizLog + "paso a digitalizar: " + paso_digitalizar);
						context.getExtendedState().getVariables().put(Consts.PASO_DIGITALIZAR, paso_digitalizar);
					}
					
					String paso_calidad = context.getMessageHeaders().get(Consts.PASO_CALIDAD, String.class);
					if(paso_calidad != null && !paso_calidad.isEmpty())	{
						log.info(raizLog + "paso a paso_calidad: " + paso_calidad);
						context.getExtendedState().getVariables().put(Consts.PASO_CALIDAD, paso_calidad);
						
						String idDocsDigitalizados = context.getMessageHeaders().get(Consts.ID_DOCS_DIGITALIZADOS_DER, String.class);
												
						if(idDocsDigitalizados!=null) {

							context.getExtendedState().getVariables().put(Consts.ID_DOCS_DIGITALIZADOS_DER, idDocsDigitalizados);
						} 

					}
				}
				
				log.info(raizLog + "------------------------------FIN ESTADO obtenerResultadoDER-------------------------");
				
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}
			};
		}
		*/
		
		
		

		
		
		/**
		 * Metodo que permite registasr en BD que el proceso termino exitosamente
		 * 
		 * @return 
		 */
		@Bean
		public Action<States, Events> registroFinalizado() {

			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);

				log.info(raizLog + "------------------------------INICIO ESTADO registroFinalizado-------------------------");
				
				log.info(raizLog + "------------------------------FIN ESTADO registroFinalizado-------------------------");
				
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}
				
			};
			
		}
		
		
		
		
		/**
		 * 
		 * @return
		 */
		@Bean
		public Action<States, Events> persist() {
			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);
				log.info(raizLog + "GUARDANDO MAQUINA [" + context.getStateMachine().getId() + "] - en el estado ["+context.getStateMachine().getState().getId().name()+"]");
				StateMachine<States, Events> machine = context.getStateMachine();

				try {
					log.info("Guardando en la tabla MAQUINA");
					persistContexto(context);

				} catch (Exception e) {
					log.error(raizLog + "ERROR GUARDANDO LA MAQUINA [" + machine.getId() + "] en la base de datos", e);
				}
				
			};
		}
		
		
		/**
		 * Metodo que permite manejar los errores funcionales
		 * 
		 * @return 
		 */
		@Bean
		public Action<States, Events> finErroresFuncionales() {

			return (context) -> {
				String raizLog = SSMUtils.getRaizLog(context);

				log.info(raizLog + "------------------------------INICIO ESTADO finErroresFuncionales()-------------------------");
				
				log.info(raizLog + "------------------------------FIN ESTADO finErroresFuncionales()-------------------------");
				
				try {
					persistContexto(context);
				} catch (Exception e) {
					System.out.println("Ocurrio un error almacenando la maquina de estados en la BD: " + e.getMessage());
					log.error("Error operacion", e);
				}
				
			};
			
		}
		
		
		/**
		 * Metodo para capturar los errores
		 * @param context
		 * @param e
		 */
		private void cargarError(StateContext<States, Events> context, Exception e ){
			try {
				log.info("Ingresó a cargar mensaje de error");
				String mensaje = (e.getMessage()== null) ? "Atributo nulo o vacio" : e.getMessage();
				context.getExtendedState().getVariables().put(globalProperties.getMensajeError(), mensaje);
				String estado = context.getStateMachine().getState().getId().name();
				estado = (estado == null) ? "Estado Inicial" : estado;
				context.getExtendedState().getVariables().put(globalProperties.getEstadoError(), estado);
			} catch (Exception e1) {
				log.error("Error cargando mensaje de error",e1);
			}
		}

		/**
		 * Metodo que permite obtener el id de un grupo en el Content Server a partir de
		 * su nombre
		 * 
		 * @param nombreGrupo
		 *            nombre del grupo/rol en el ContenteServer
		 * @param header
		 *            Header de usuario de autenticacion contra el ContentServer
		 * @return idGrupo el id del grupo/rol en el ContentServer
		 */
		private Long obtenerIdGrupo(String nombreGrupo, Header headerConexionUsuario)     {

			Member grupo = null;
			Long idGrupo = null;

			try {
				grupo = UsuarioCS.getGrupoByNombre(autenticacionCS.getAdminSoapHeader(), nombreGrupo,
						wsdlsProps.getUsuario());
				idGrupo = grupo.getID();
				
			} catch (SOAPException e) {
				log.error("Error operacion", e);
			} catch (IOException e) {
				log.error("Error operacion", e);
			}
			catch (NoSuchElementException e) {
				log.error("Error operacion", e);
			}

			return idGrupo;

		}

		/**
		 * Metodo que permite generar una copia del Doc en el Content Server
		 * 
		 * @param idDoc
		 *            identificador del docuemnto en el CS
		 * @param nombreDocCopia
		 *            nombre a asignar a la copia generada
		 * @param conexionUsuario
		 *            usuario de conexion con el CS
		 * @return documentoCopia nuevo nodo copia generado
		 */
		@SuppressWarnings("unused")
		private Node obtenerCopiaDocumento(long idDoc, String nombreDocCopia, String conexionUsuario) {

			Node documentoCopia = null;
			try {
				documentoCopia = ContenidoDocumento.copiarDocumento(autenticacionCS.getUserSoapHeader(conexionUsuario),
						idDoc, nombreDocCopia, wsdlsProps.getDocumento());
			} catch (ServerSOAPFaultException | IOException | SOAPException | InterruptedException e) {
				log.error("Error operacion", e);
			} 
			
			return documentoCopia;

		}

		/**
		 * Metodo que permite actualizar el estado de la categoria Correspondencia de un
		 * documento en el CS
		 * 
		 * @param idDoc
		 *            documento en el CS al cual se le va actualizar el estado de la
		 *            categoria
		 * @param nuevoEstado
		 *            texto con el nuevo estado que se va asignar a la categoria
		 *            Correspondencia
		 * @return nuevoEstadoResultado texto del nuevo estado que quedo asignado a la
		 *         categoria Correspondencia
		 */
		private String actualizarEstadoCorrespondencia(String raizLog, long idDoc, String nuevoEstado) throws SOAPException {

			log.info(raizLog + "En maquina de estados: Metodo cambiar estado correspondencia para el doc: [" + idDoc
					+ "] con nuevoEstado= [" + nuevoEstado +"]");

			String nombreCampo = categoriaProps.getCorrespondencia().getAtributoEstado();
			String nuevoEstadoResultado = "";

			try {
				log.info(raizLog + "INICIO cambiar estado correspondencia");
				nuevoEstadoResultado = ContenidoDocumento.actualizarAtributoCategoria(
						autenticacionCS.getAdminSoapHeader(), idDoc,
						categoriaProps.getCorrespondencia().getId(),
						categoriaProps.getCorrespondencia().getNombre(), nombreCampo, nuevoEstado,
						wsdlsProps.getDocumento());
				log.info("FIN cambiar estado correspondencia el nuevo estado es: [" + nuevoEstadoResultado +"]");
			} catch (ServerSOAPFaultException e) {
				log.error("Error operacion", e);
			} catch (IOException e) {
				log.error("Error operacion", e);
			} catch (SOAPException e) {
				log.error("Error operacion", e);
			} catch (InterruptedException e) {
				log.error("Error operacion", e);
			}

			return nuevoEstadoResultado;
		}
		
		/**
		 * Metodo para Obtener una ruta de una lista
		 * @param rutas
		 * @param tipologia
		 * @return
		 */
		/*
		private RutaDistribucion getRuta(List<RutaDistribucion> rutas, String tipologia) {
			
			RutaDistribucion ruta = null;
			
			if(rutas.size() == 1) {
				ruta = rutas.get(0);
			}else if(rutas.size()>1) {
				
				Optional<RutaDistribucion> rutaOpt = rutas.stream().filter((r)-> r.getTipologia().equals(tipologia)).findFirst();
				if(rutaOpt.isPresent()) {
					ruta = rutaOpt.get();
				}else {					
					ruta = rutas.stream().filter((r)-> r.getTipologia().equals(globalProperties.getRutaDefault())).findFirst().get();
				}
			}
			
			
			return ruta;
		}
		*/
		
		/**
		 * Metodo que permite persistir la maquina de estados en la BD
		 * 
		 * @param context
		 *            contexto de la maquina de estados
		 * @throws Exception
		 */
		private void persistContexto(StateContext<States, Events> context) throws Exception {
			
			if(context == null){
				throw new NullPointerException("El parametro contexto no puede ser nulo");
			}

			StateMachine<States, Events> machine = context.getStateMachine();
			
			if(machine== null){
				throw new NullPointerException("La maquina retornada es nula");
			}
			
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
		 * Metodo que permite obtener un mensaje a partir de su codigo, de la lista de
		 * mensajes previamente cargada desde base de datos
		 * 
		 * @param codigoMensaje
		 *            codigo del mensaje que se desea obtener
		 * @return mensaje cadena de texto con el mensaje obtenido
		 */
		private String obtenerMensaje(String codigoMensaje) {

			String mensaje = "NO ESPECIFICADO";

			Optional<Mensaje> mensajeOpt = mensajes.stream().filter(msj -> msj.getCodigo().equals(codigoMensaje))
					.findFirst();

			if (mensajeOpt.isPresent()) {
				mensaje = mensajeOpt.get().getMensaje();
			}

			return mensaje;

		}
		
		/**
		 * Permite convertir texto a UTF-8
		 * @param texto
		 * @return
		 */
		private static String converUTF8 (String texto) {
			try {
			
			byte[] utf8 = texto.getBytes();
		    
			texto = new String(utf8, "UTF-8");
			
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return texto;
		}
		
		private void actualizarIdParentOriginal(StateContext<States, Events> context, Header soapAuthHeader,String wsdl) throws SOAPException, IOException {
			//context.getExtendedState().getVariables().put(Consts.ID_DOC_RADICACION, idDoc);
			long idDoc = context.getExtendedState().get(Consts.ID_DOC_RADICACION, Long.class);
						
			long idCarpeta = ContenidoDocumento.ObtenerParentIdDoc(soapAuthHeader, idDoc, wsdl);
						
			context.getExtendedState().getVariables().put(Consts.PARENT_ID_ORIGINAL_MEMO_CARTA, idCarpeta);
			
		}
		
		private void trucarValoresCategoriaCorrespondencia(List<Map<String, Object>> metadatos) {
			
			metadatos.forEach((participante)->{
			
				Optional<Entry<String, Object>> OptNombre = participante.entrySet().stream().filter(v -> v.getKey().equals(Consts.CATEGORIA_NOMBRE)).findFirst();
				if(OptNombre.isPresent()) {
					String nombre = OptNombre.get().getValue().toString();
					nombre = SSMUtils.truncarString(nombre, Consts.TAMANO_MAXIMO_NOMBRE_CATEGORIA);
					participante.put(Consts.CATEGORIA_NOMBRE, nombre);
				}
				
				Optional<Entry<String, Object>> OptCargo = participante.entrySet().stream().filter(v -> v.getKey().equals(Consts.CATEGORIA_CARGO)).findFirst();
				if(OptCargo.isPresent()) {
					String cargo = OptCargo.get().getValue().toString();
					cargo = SSMUtils.truncarString(cargo, Consts.TAMANO_MAXIMO_CARGO_CATEGORIA);
					participante.put(Consts.CATEGORIA_CARGO, cargo);
				}
				
				Optional<Entry<String, Object>> OptDependenciaRemitente = participante.entrySet().stream().filter(v -> v.getKey().equals(Consts.CATEGORIA_DEPENDENCIA_REMITENTE)).findFirst();
				if(OptDependenciaRemitente.isPresent()) {
					String dependenciaRemitente = OptDependenciaRemitente.get().getValue().toString();
					dependenciaRemitente = SSMUtils.truncarString(dependenciaRemitente, Consts.TAMANO_MAXIMO_REMIENTENTE_DEPENDECIA_CATEGORIA);
					participante.put(Consts.CATEGORIA_DEPENDENCIA_REMITENTE, dependenciaRemitente);
				}
				
				Optional<Entry<String, Object>> OptDependencia = participante.entrySet().stream().filter(v -> v.getKey().equals(Consts.CATEGORIA_DEPENDENCIA)).findFirst();
				if(OptDependencia.isPresent()) {
					String dependencia = OptDependencia.get().getValue().toString();
					dependencia = SSMUtils.truncarString(dependencia, Consts.TAMANO_MAXIMO_DEPENDECIA_CATEGORIA);
					participante.put(Consts.CATEGORIA_DEPENDENCIA, dependencia);
				}
				
			});
			
		}
		
		private void truncarValoresAtributosWFCarta(Map<String, ValorAtributo> atributos) {

			ValorAtributo tipologia = atributos
					.get(workflowProps.getDistribucionCarta().getAtributoWF().getTipologia());
			if (tipologia != null) {
				String valorTipologia = (String) tipologia.getValor();
				if (org.springframework.util.StringUtils.hasText(valorTipologia)) {
					valorTipologia = SSMUtils.truncarString(valorTipologia, Consts.TAMANO_MAXIMO_CARTA_TIPOLOGIA);
					atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getTipologia(),
							new ValorAtributo(valorTipologia, TipoAtributo.STRING));
				}
			}

			ValorAtributo asunto = atributos.get(workflowProps.getDistribucionCarta().getAtributoWF().getAsunto());
			if (asunto != null) {
				String valorAsunto = (String) asunto.getValor();
				if (org.springframework.util.StringUtils.hasText(valorAsunto)) {
					valorAsunto = SSMUtils.truncarString(valorAsunto, Consts.TAMANO_MAXIMO_CARTA_ASUNTO);
					atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getAsunto(),
							new ValorAtributo(valorAsunto, TipoAtributo.STRING));
				}
			}

			ValorAtributo numeroRadicado = atributos
					.get(workflowProps.getDistribucionCarta().getAtributoWF().getNumeroRadicado());
			if (numeroRadicado != null) {
				String valorNumeroRadicado = (String) numeroRadicado.getValor();
				if (org.springframework.util.StringUtils.hasText(valorNumeroRadicado)) {
					valorNumeroRadicado = SSMUtils.truncarString(valorNumeroRadicado,
							Consts.TAMANO_MAXIMO_CARTA_NUMERO_RADICADO);
					atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getNumeroRadicado(),
							new ValorAtributo(valorNumeroRadicado, TipoAtributo.STRING));
				}
			}

			ValorAtributo destino = atributos.get(workflowProps.getDistribucionCarta().getAtributoWF().getDestino());
			if (destino != null) {
				String valorDestino = (String) destino.getValor();
				if (org.springframework.util.StringUtils.hasText(valorDestino)) {
					valorDestino = SSMUtils.truncarString(valorDestino, Consts.TAMANO_MAXIMO_CARTA_DESTINO);
					atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getDestino(),
							new ValorAtributo(valorDestino, TipoAtributo.STRING));
				}
			}

			ValorAtributo conexionCS = atributos
					.get(workflowProps.getDistribucionCarta().getAtributoWF().getConexionCs());
			if (conexionCS != null) {
				String valorConexionCS = (String) conexionCS.getValor();
				if (org.springframework.util.StringUtils.hasText(valorConexionCS)) {
					valorConexionCS = SSMUtils.truncarString(valorConexionCS, Consts.TAMANO_MAXIMO_CARTA_CONEXIONCS);
					atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getConexionCs(),
							new ValorAtributo(valorConexionCS, TipoAtributo.STRING));
				}
			}		

			ValorAtributo fechaRadicacion = atributos
					.get(workflowProps.getDistribucionCarta().getAtributoWF().getFechaRadicacion());
			if (fechaRadicacion != null) {
				String valorFechaRadicacion = (String) fechaRadicacion.getValor();
				if (org.springframework.util.StringUtils.hasText(valorFechaRadicacion)) {
					valorFechaRadicacion = SSMUtils.truncarString(valorFechaRadicacion,
							Consts.TAMANO_MAXIMO_CARTA_FECHA_RADICACION);
					atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getFechaRadicacion(),
							new ValorAtributo(valorFechaRadicacion, TipoAtributo.STRING));
				}
			}

		
			ValorAtributo workId = atributos.get(workflowProps.getDistribucionCarta().getAtributoWF().getWorkid());
			if (workId != null) {
				String valorWorkId = (String) workId.getValor();
				if (org.springframework.util.StringUtils.hasText(valorWorkId)) {
					valorWorkId = SSMUtils.truncarString(valorWorkId, Consts.TAMANO_MAXIMO_CARTA_WORKID);
					atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getWorkid(),
							new ValorAtributo(valorWorkId, TipoAtributo.STRING));
				}
			}

			ValorAtributo nombreDestino = atributos
					.get(workflowProps.getDistribucionCarta().getAtributoWF().getNombreDestino());
			if (nombreDestino != null) {
				String valorNombreDestino = (String) nombreDestino.getValor();
				if (org.springframework.util.StringUtils.hasText(valorNombreDestino)) {
					valorNombreDestino = SSMUtils.truncarString(valorNombreDestino,
							Consts.TAMANO_MAXIMO_NOMBRE_DESTINO);
					atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getNombreDestino(),
							new ValorAtributo(valorNombreDestino, TipoAtributo.STRING));
				}
			}
					
			ValorAtributo entidadDestino = atributos
					.get(workflowProps.getDistribucionCarta().getAtributoWF().getEntidadDestino());
			if (entidadDestino != null) {
				String valorEntidadDestino = (String) entidadDestino.getValor();
				if (org.springframework.util.StringUtils.hasText(valorEntidadDestino)) {
					valorEntidadDestino = SSMUtils.truncarString(valorEntidadDestino,
							Consts.TAMANO_MAXIMO_ENTIDAD_DESTINO);
					atributos.put(workflowProps.getDistribucionCarta().getAtributoWF().getEntidadDestino(),
							new ValorAtributo(valorEntidadDestino, TipoAtributo.STRING));
				}
			}

		}
		
	}

}
