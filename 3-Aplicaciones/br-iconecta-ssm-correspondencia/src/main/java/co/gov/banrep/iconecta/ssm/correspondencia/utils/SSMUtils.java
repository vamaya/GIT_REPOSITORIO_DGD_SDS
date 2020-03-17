package co.gov.banrep.iconecta.ssm.correspondencia.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.xml.soap.SOAPException;

import org.slf4j.Logger;
import org.springframework.statemachine.StateContext;
import org.springframework.util.StringUtils;

import co.gov.banrep.iconecta.cs.autenticacion.AutenticacionCS;
import co.gov.banrep.iconecta.cs.cliente.document.DataValue;
import co.gov.banrep.iconecta.cs.cliente.document.IntegerValue;
import co.gov.banrep.iconecta.cs.cliente.document.ReportResult;
import co.gov.banrep.iconecta.cs.cliente.document.RowValue;
import co.gov.banrep.iconecta.cs.cliente.document.StringValue;
import co.gov.banrep.iconecta.cs.documento.BusquedaDocumentos;
import co.gov.banrep.iconecta.cs.documento.ContenidoDocumento;
import co.gov.banrep.iconecta.cs.documento.Reporte;
import co.gov.banrep.iconecta.cs.workflow.TipoAtributo;
import co.gov.banrep.iconecta.cs.workflow.ValorAtributo;
import co.gov.banrep.iconecta.cs.workflow.Workflow;
import co.gov.banrep.iconecta.ssm.correspondencia.entity.FirmanteSSM;
import co.gov.banrep.iconecta.ssm.correspondencia.enums.Events;
import co.gov.banrep.iconecta.ssm.correspondencia.enums.States;
import co.gov.banrep.iconecta.ssm.correspondencia.exceptions.ErrorFuncional;
import co.gov.banrep.iconecta.ssm.correspondencia.params.CategoriaProps;
import co.gov.banrep.iconecta.ssm.correspondencia.params.GlobalProps;
import co.gov.banrep.iconecta.ssm.correspondencia.params.WsdlProps;
import co.gov.banrep.iconecta.ssm.correspondencia.persist.RutaDistribucion;

public final class SSMUtils {
	
	private SSMUtils() {
		throw new AssertionError();
	}
	
	public static String getRaizLog(StateContext<States, Events> context) {
		String machineId = context.getStateMachine().getId();
		States estadoActual = context.getTarget().getId();
		
		return Consts.PREFIJO_LOG + machineId + Consts.SUFIJO_LOG + estadoActual + " - ";
	}
	
	public static String truncarString(String cadena, int tamanoMaximo) {

		if (StringUtils.hasText(cadena)) {
			int tamanoActual = cadena.length();

			if (tamanoActual > tamanoMaximo) {
				cadena = cadena.substring(0, tamanoMaximo);
			}

		}

		return cadena;
	}
	
	public static String truncarNombreDocumento(String nombreDoc, int tamanoMaximo) {

		int tamanoActual = nombreDoc.length();

		if (tamanoActual > tamanoMaximo) {

			String nombreSinExtension = org.apache.commons.lang3.StringUtils.substringBeforeLast(nombreDoc, ".");
			String extension = org.apache.commons.lang3.StringUtils.substringAfterLast(nombreDoc, ".");

			String nombreSinExtensionTruncado = truncarString(nombreSinExtension, tamanoMaximo);

			String nombreTruncado = nombreSinExtensionTruncado + "." + extension;

			return nombreTruncado;

		}

		return nombreDoc;
	}
	
	/**
	 * Metodo para Obtener una ruta de una lista
	 * @param rutas
	 * @param tipologia
	 * @return
	 */
	public static RutaDistribucion getRuta(List<RutaDistribucion> rutas, String tipologia , GlobalProps properties) {

		
		RutaDistribucion ruta = null;

		
		if (rutas.size() == 1) {
			ruta = rutas.get(0);
		} else if (rutas.size() > 1) {

			// Se verifica si existe una ruta propia de la tipologia o si se toma la ruta
			// default
			Optional<RutaDistribucion> rutaOpt = rutas.stream().filter((r) -> r.getTipologia().equals(tipologia))
					.findFirst();
			if (rutaOpt.isPresent()) {
				ruta = rutaOpt.get();
			} else {
				Optional<RutaDistribucion> rutaOptDefault = rutas.stream()
						.filter((r) -> r.getTipologia().equals(properties.getRutaDefault())).findFirst();
				if (rutaOptDefault.isPresent()) {
					ruta = rutaOptDefault.get();
				} else {
					//throw new NoSuchElementException("No se encontro ruta");
					return ruta;
				}
			}
		}

		return ruta;
	}
	
	
	
	/**
	 * Actualiza el estado de la categoría de un documento
	 * 
	 * @param workidInstancia
	 * @param log
	 * @param raizlog
	 * @throws InterruptedException
	 */
	//TODO Unificar con request del controller *actualizarEstadoCorrespondencia*
	public static void actualizarEstadoDocumentoCorr(Long idDocumento, Boolean isEnvioFisico, Logger log, String raizlog, String nuevoEstado, WsdlProps wsdlsProps, AutenticacionCS autenticacionCS, CategoriaProps categoriaProps) {
		log.info(raizlog + "INICIO Actualizar estado Documento");
			if(!isEnvioFisico) {

					if (!idDocumento.equals(null) || !idDocumento.equals(0L)) {

						try {
							ContenidoDocumento.actualizarAtributoCategoria(autenticacionCS.getAdminSoapHeader(),
									idDocumento, categoriaProps.getCorrespondencia().getId(),
									categoriaProps.getCorrespondencia().getNombre(),
									categoriaProps.getCorrespondencia().getAtributoEstado(), nuevoEstado, wsdlsProps.getDocumento());
						} catch (IOException | SOAPException | InterruptedException e) {
							log.error(raizlog + "Se ha presentado un error actualizando la categoria", e);
							//TODO si  no existe la categoria, crearla
						}

					} else {
						log.warn(raizlog + "No se realiza la actualización dado que el id del documento no es válido o está vacío, idDocumento ["+idDocumento+"]");
					}

				}else {
					log.info(raizlog + "No se actualiza categoría dado que la forma de entrega es fisica");
				}
	}
	
	
	/**
	 * Retorna el id de los docs adjuntos en un workflow
	 * @return
	 */
	public static List<Long> obtenerListaIdDocAdjuntoWorkflow(long workidInstancia, Boolean isEnvioFisico, Logger log, String raizlog, WsdlProps wsdlsProps, AutenticacionCS autenticacionCS) {
		
		Long idContenedor = 0L;
		List<Long> documentos = new ArrayList<>();
		
		if(!isEnvioFisico) {
		
			try {
				idContenedor = Workflow.obtenerIdContenedorAdjuntos(autenticacionCS.getAdminSoapHeader(),
						workidInstancia, wsdlsProps.getWorkflow());
				} catch (IOException | SOAPException e) {
					log.error(raizlog + "Se ha presentado un error obteniendo contenedor de adjuntos del WF", e);
				}
					
				log.info(raizlog + "idContenedor de adjuntos [" + idContenedor + "]");			
				
				if (idContenedor == null) {
					throw new NullPointerException("idContenedor nulo");
				}
	
				if (!idContenedor.equals(new Long(0))) {
					try {
						documentos = BusquedaDocumentos.obtenerIdsDocsContainer(autenticacionCS.getAdminSoapHeader(),
								idContenedor, wsdlsProps.getDocumento());
						
						if (documentos == null) {
							throw new NullPointerException("documentos en idContenedor nulo");
						}
						
					} catch (IOException | SOAPException e) {
						log.error(raizlog + "Se ha presentado un error obteniendo docs en el contenedor de adjuntos del WF", e);
					}
				}	
				
		}
		
		return documentos;
	}
	
	
	
	
	/**
	 * Metodo que permite ejecutar el reporte de firmantes desde CS
	 * 
	 * @param raizLog
	 * @param autenticacionCS
	 * @param wsdlsProps
	 * @param globalProperties
	 * @return
	 * @throws IOException
	 * @throws SOAPException
	 */
	public static List<FirmanteSSM> obtenerFirmantesReporte(String raizLog, AutenticacionCS autenticacionCS,
			WsdlProps wsdlsProps, GlobalProps globalProperties) throws IOException, SOAPException {

		System.out.println(raizLog + "Se va a ejecutar el reporte de firmantes de CS");

		ReportResult resultado = Reporte.ejecutarReporteSinParametros(autenticacionCS.getAdminSoapHeader(),
				globalProperties.getIdConsultaFirmantes(), wsdlsProps.getDocumento());

		List<FirmanteSSM> firmantes = new ArrayList<>();

		List<RowValue> contents = resultado.getContents();

		Iterator<RowValue> iterContents = contents.iterator();
		while (iterContents.hasNext()) {
			RowValue unRow = iterContents.next();
			FirmanteSSM unFirmante = new FirmanteSSM();
			List<DataValue> values = unRow.getValues();

			// SEQ
			Optional<DataValue> seqOpt = values.stream().filter(unValue -> unValue.getKey().equals("SEQ")).findFirst();

			IntegerValue seq = null;
			if (seqOpt.isPresent()) {
				seq = (IntegerValue) seqOpt.get();
				List<Long> valores = seq.getValues();
				if (!valores.isEmpty()) {
					unFirmante.setSeq(valores.get(0));
				}
			}

			// NOMBRES
			Optional<DataValue> nombreOpt = values.stream().filter(unValue -> unValue.getKey().equals("NOMBRES"))
					.findFirst();

			StringValue nombre = null;
			if (nombreOpt.isPresent()) {
				nombre = (StringValue) nombreOpt.get();
				List<String> valores = nombre.getValues();
				if (!valores.isEmpty()) {
					unFirmante.setNombre(valores.get(0));
				}
			}

			// APELLIDOS
			Optional<DataValue> apellidoOpt = values.stream().filter(unValue -> unValue.getKey().equals("APELLIDOS"))
					.findFirst();

			StringValue apellido = null;
			if (apellidoOpt.isPresent()) {
				apellido = (StringValue) apellidoOpt.get();
				List<String> valores = apellido.getValues();
				if (!valores.isEmpty()) {
					// Se debe concatenar nombre y apellido
					String tempNombre = unFirmante.getNombre() + " " + valores.get(0);
					unFirmante.setNombre(tempNombre);
				}
			}

			// SIGLA = DEPENDENCIA
			Optional<DataValue> siglaOpt = values.stream().filter(unValue -> unValue.getKey().equals("SIGLA"))
					.findFirst();

			StringValue sigla = null;
			if (siglaOpt.isPresent()) {
				sigla = (StringValue) siglaOpt.get();
				List<String> valores = sigla.getValues();
				if (!valores.isEmpty()) {
					unFirmante.setDependencia(valores.get(0));
				}
			}

			// CARGO
			Optional<DataValue> cargoOpt = values.stream().filter(unValue -> unValue.getKey().equals("CARGO"))
					.findFirst();

			StringValue cargo = null;
			if (cargoOpt.isPresent()) {
				cargo = (StringValue) cargoOpt.get();
				List<String> valores = cargo.getValues();
				if (!valores.isEmpty()) {
					unFirmante.setCargo(valores.get(0));
				}
			}

			// CEDULA
			Optional<DataValue> cedulaOpt = values.stream().filter(unValue -> unValue.getKey().equals("CEDULA"))
					.findFirst();

			IntegerValue cedula = null;
			if (cedulaOpt.isPresent()) {
				cedula = (IntegerValue) cedulaOpt.get();
				List<Long> valores = cedula.getValues();
				if (!valores.isEmpty()) {
					unFirmante.setCedula(valores.get(0));
				}
			}

			// CORREO_ELECTRONICO
			Optional<DataValue> correoOpt = values.stream()
					.filter(unValue -> unValue.getKey().equals("CORREO_ELECTRONICO")).findFirst();

			StringValue correo = null;
			if (correoOpt.isPresent()) {
				correo = (StringValue) correoOpt.get();
				List<String> valores = correo.getValues();
				if (!valores.isEmpty()) {
					unFirmante.setCorreo(valores.get(0));
				}
			}

			firmantes.add(unFirmante);

		}

		System.out.println(raizLog + "Reporte ejecutado correctamente");

		System.out.println("\n" + raizLog + "MOSTRANDO DATOS DEL REPORTE DE FIRMANTES..." + "\n");
		for (FirmanteSSM firmante : firmantes) {
			System.out.println("Firmante Reporte - {cedula, nombre, cargo, sigla} - {" + firmante.getCedula() + ","
					+ firmante.getNombre() + "," + firmante.getCargo() + "," + firmante.getDependencia() + "}");
		}
		System.out.println("\n" + raizLog + "FIN DATOS DEL REPORTE DE FIRMANTES MOSTRADOS" + "\n");

		return firmantes;
	}
	
	
	
	public static void validarTipoWfIniciado(String raizLog, Logger log, String tipoWf, String  tipoDocPlantilla)  throws ErrorFuncional{
		if (tipoWf.equalsIgnoreCase(tipoDocPlantilla)) {
			log.info(raizLog + "Se ha iniciado el WF correspondiente al nivel de seguridad especificado en la plantilla");
		} else {// aqui se selecciono un WF diferente al tipo que esta en la plantilla ERROR											
				log.error("\n \n \n"+raizLog + "ERROR - se selecciono un WF diferente al tipo que esta en la plantilla \n");
				throw new ErrorFuncional("Se inicio proceso de radicación con un WorkFlow ["+tipoWf+"], pero en la plantilla se especifico de tipo ["+tipoDocPlantilla+"]");
		}
	}
	
	
	
	
	/**
	 * Metodo pra enviar correos de notificacion de errores
	 * @return
	 */
	public static void notificarErrorAdmin(String raizLog, String idMquina, String numeroRadicado, String mensajeError, String estadoActualError, GlobalProps globalProperties, AutenticacionCS autenticacionCS, WsdlProps wsdlsProps) {
			System.out.println(raizLog + "Entró al método notificarErrorAdmin");
			String asuntoMaquina = "Fallo Maquina Correspondencia " + numeroRadicado;						
			
			//SE CARGAN ATRIBUTOS PARA INICIAR_RADICAR_CA_ME FLUJO DE NOTIFICACIÓN
			Map<String, ValorAtributo> atributos = new HashMap<String, ValorAtributo>();				
			atributos.put("asunto", new ValorAtributo("Error en la maquina No: " + idMquina, TipoAtributo.STRING));
			String mensaje = "Estado: " + estadoActualError + "\n";
			mensaje = mensaje + " - Error: " + mensajeError;			
			atributos.put("error", new ValorAtributo(mensaje, TipoAtributo.MULTILINE));
			String email = globalProperties.getMailAdmin();
			atributos.put("correoDestinatario", new ValorAtributo(email, TipoAtributo.STRING));
			atributos.put("idMaquina", new ValorAtributo(Long.parseLong(idMquina), TipoAtributo.INTEGER));
			atributos.put("asuntoMaquina", new ValorAtributo(asuntoMaquina, TipoAtributo.STRING));
			
			long longIdMap = Long.parseLong(globalProperties.getMapIdNotificacionAdmin());
			
			System.out.println(raizLog + "atributos de notificacion Administrador mensaje [" + mensaje + "]  -  email ["+ email + "]  -  idMquina [" + idMquina + "]  -  asuntoMaquina [" + asuntoMaquina+"]");
			
			
			System.out.println(raizLog + "Iniciando IdWorkflow de notificacion Administrador [" + longIdMap+"]");
			
			long idWorkflow;
			try {
				idWorkflow = Workflow.iniciarWorkflowConAtributos(autenticacionCS.getAdminSoapHeader(), longIdMap,
						"Error maquina " + idMquina, atributos, wsdlsProps.getWorkflow());
				System.out.println(raizLog + "Se inicia La instancia de workflow: [" + idWorkflow+"]");
			} catch (SOAPException e) {
				System.out.println(raizLog + "Error al iniciar el IdWorkflow con MapID : [" + longIdMap + "] \n"+e);
			} catch (NumberFormatException | NoSuchElementException | IOException e) {
				System.out.println(raizLog +  "Error al iniciar el IdWorkflow con MapID : [" + longIdMap+ "] \n"+e);
			}

	}
	
	
	
	
	

}
