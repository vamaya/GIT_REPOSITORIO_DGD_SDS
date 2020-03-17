package co.gov.banrep.iconecta.ssm.correspondencia.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.springframework.util.StringUtils;

import co.gov.banrep.iconecta.cs.autenticacion.AutenticacionCS;
import co.gov.banrep.iconecta.cs.cliente.document.Attachment;
import co.gov.banrep.iconecta.cs.documento.ContenidoDocumento;
import co.gov.banrep.iconecta.office.documento.DocumentoUtils;
import co.gov.banrep.iconecta.office.documento.entity.MetadatosPlantilla;
import co.gov.banrep.iconecta.office.documento.entity.Empleado;
import co.gov.banrep.iconecta.ssm.correspondencia.ContextRefreshedListener;
import co.gov.banrep.iconecta.ssm.correspondencia.exceptions.ErrorFuncional;
import co.gov.banrep.iconecta.ssm.correspondencia.exceptions.ErrorTecnologico;
import co.gov.banrep.iconecta.ssm.correspondencia.params.GlobalProps;
import co.gov.banrep.iconecta.ssm.correspondencia.params.WsdlProps;

/**
 * Clase final que posee funciones para realizar validaciones sobre un documento
 * radicado en Content Server, antes de iniciar una máquina de estados
 * 
 * @author vamayafe91008
 *
 */
public final class ValidadorDocumento {

	// NO SE PERMITEN INSTANCIAS DE ESTA CLASE
	private ValidadorDocumento() {
		throw new AssertionError();
	}

	/**
	 * Clase principal que permite realizar diversas validaciones sobre el contenido
	 * de un documento
	 * 
	 * @param raizLog          raiz de impresión de logs con un formato definido
	 * @param workId           id del flujo de radicacion
	 * @param idDoc            id del documento a radicar
	 * @param conexionUsuario  conexión en CS del usuario solicitante
	 * @param appContext
	 * @param autenticacionCS
	 * @param globalProperties
	 * @param wsdlsProps
	 * @return anexos Boolean para saber si se debe seguir con o sin anexos
	 */
	public static boolean validarDatosPlantilla(String raizLog, String workId, long idDoc, String conexionUsuario,
			ContextRefreshedListener appContext, AutenticacionCS autenticacionCS, GlobalProps globalProperties,
			WsdlProps wsdlsProps) throws Exception {

		
		// SE OBTIENE EL DOCUMENTO DESDE CS
		Attachment documentoObtenido = obtenerDocCS(raizLog + "OBTENER_DOC_CS - ", idDoc, conexionUsuario,
				autenticacionCS, wsdlsProps);

		// SE VALIDA INICIALMENTE NOMBRE, EXTENSIÓN Y TAMAÑO DEL DOC OBTENIDO
		validarNombreDoc(raizLog + "VALIDAR_NOMBRE_DOC - ", documentoObtenido);
		validarExtensionDoc(raizLog + "VALIDAR_EXTENSION_DOC - ", documentoObtenido);
		validarTamano(raizLog + "VALIDAR_TAMANO_DOC - ", documentoObtenido);

		// SE DESCARGA EL DOCMUENTO OBTENIDO EN LA RUTA DEL SERVIDOR
		DataHandler documentoDescargado = descargarDocCS(raizLog + "DESCARGAR_CONTENIDO_DOC - ", workId, idDoc,
				globalProperties, documentoObtenido);

		// SE LLENAN LOS ATRIBUTOS DE LA PLANTILLA CON EL DOCUMENTO ALMACENADO EN EL
		// SERVIDOR
		MetadatosPlantilla categoria = obtenerDatosDoc(raizLog + "OBTENER_METADATOS_DOC - ", globalProperties, appContext,
				documentoDescargado);

		// SE VALIDAN LOS DEMÁS CAMPOS
		validarGeneradoDesdePlantilla(raizLog + "VALIDAR_GENERADO_DESDE_PLANTILLA - ", categoria);
		validarVersionPlantilla(raizLog + "VALIDAR_VERSION_PLANTILLA - ", categoria, globalProperties);
		validarAsunto(raizLog + "VALIDAR_ASUNTO - ", categoria);

		
		// SE EJECUTA EL REPORTE DE FIRMANTES Y SE ALMACENA EN UNA LISTA
		//List<FirmanteSSM> firmantesReporte = appContext.getListaFirmantes();
		/*
		List<FirmanteSSM> firmantesReporte = SSMUtils.obtenerFirmantesReporte(raizLog + "EJECUTAR_REPORTE_FIRMANTE - ",
				autenticacionCS, wsdlsProps, globalProperties);
				*/
		
		//validarFirmante(raizLog + "VALIDAR_FIRMANTES - ", categoria, firmantesReporte);
		validarFirmante(raizLog + "VALIDAR_FIRMANTES - ", categoria);
		validarDestinos(raizLog + "VALIDAR_DESTINOS - ", categoria);
		validarCopias(raizLog + "VALIDAR_COPIAS - ", categoria);
		boolean isAnexos = validarAnexos(raizLog+"VALIDAR_ANEXOS - ", categoria);

		return isAnexos;
	}
	
	
	/**
	 * 
	 * @param idDoc
	 * @param conexionUsuario
	 * @param raizLog
	 * @param autenticacionCS
	 * @param wsdlsProps
	 * @return
	 * @throws ErrorTecnologico
					 
	 */
	private static Attachment obtenerDocCS(String raizLog, long idDoc, String conexionUsuario,
			AutenticacionCS autenticacionCS, WsdlProps wsdlsProps) throws ErrorTecnologico {

		System.out.println(raizLog + "Documento [" + idDoc + "] por obtener de Content Server");
		Attachment attachment = null;
		try {
			attachment = ContenidoDocumento.obtenerDocumentoCS(raizLog,
					autenticacionCS.getUserSoapHeader(conexionUsuario), idDoc, wsdlsProps.getDocumento());

			System.out.println(raizLog + "Documento [" + idDoc + "] Obtenido de Content Server");

		} catch (Exception e) {
			System.out.println(raizLog + "ERROR - No se pudo obtener el docmuento de CS");
			throw new ErrorTecnologico("No se pudo obtener el documento desde iConecta" + e.getMessage());
		}

		return attachment;
	}

	/**
	 * 
	 * @param workId
	 * @param idDoc
	 * @param raizLog
	 * @param globalProperties
	 * @param attachment
	 * @return
	 * @throws ErrorTecnologico
	 */
	private static DataHandler descargarDocCS(String raizLog, String workId, long idDoc, GlobalProps globalProperties,
			Attachment attachment) throws ErrorTecnologico {

		System.out.println(raizLog + "Descargando contenido del documento en el servidor");
		DataHandler contenidoDoc = null;
		try {
			contenidoDoc = ContenidoDocumento.descargarDocumento(raizLog, attachment, globalProperties.getRutaTemp(),
					workId, idDoc);

			if (contenidoDoc != null) {
				System.out.println(raizLog
						+ "Contenido del documento obtenido correctamente desde CS y descargado en el servidor");
			} else {
				System.out.println(
						raizLog + "ERROR - Falla al descargar contenido del documento en el servidor, archivo vacío");
				throw new ErrorTecnologico("Falla al descargar contenido del documento en el servidor, archivo vacío");
			}

		} catch (Exception e) {
			System.out.println(raizLog + "ERROR - No se pudo descargar archivo en el servidor");
			throw new ErrorTecnologico("No se pudo descargar archivo en el servidor " + e.getMessage());
		}

		return contenidoDoc;
	}

	/**
	 * 
	 * @param raizLog
	 * @param globalProperties
	 * @param appContext
	 * @param contenidoDoc
	 * @return
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	private static MetadatosPlantilla obtenerDatosDoc(String raizLog, GlobalProps globalProperties,
			ContextRefreshedListener appContext, DataHandler contenidoDoc) throws NumberFormatException, IOException {

		System.out.println(raizLog + "Se van a extraer los metadatos del documento...");
		MetadatosPlantilla metadatosPlantilla = null;
		String nombreDocServidor = contenidoDoc.getName();
		Map<String, String> mapPropsOfficeDoc = appContext.getMapPropsOfficeDoc();

		metadatosPlantilla = DocumentoUtils.leerDocumento(raizLog, globalProperties.getRutaTemp() + nombreDocServidor,
				mapPropsOfficeDoc);

		System.out.println(raizLog + "Metadatos extraidos exitosamente del documento");

		return metadatosPlantilla;
	}

	/**
	 * Valida que el documento tenga la extensión esperada (docx)
	 * 
	 * @param raizLog   cadena de caracteres que contiene información del id del
	 *                  flujo para identificar a que maquina pertenece el llamado
	 * @param documento Attachment donde se almaceno el documento descargado de CS
	 * @throws ErrorFuncional
	 * @exception NoSuchElementException lanza excepción si la extensión no es la
	 *                                   esperada
	 * 
	 */
	private static void validarExtensionDoc(String raizLog, Attachment documento) throws ErrorFuncional {

		System.out.println(raizLog + "INICIO VALIDACION");
		String extension = org.apache.commons.lang3.StringUtils.substringAfterLast(documento.getFileName(), ".");
		System.out.println(raizLog + "Extension Doc original + [" + extension + "]");
		if (!(extension.equals("docx"))) {
			System.out
					.println(raizLog + "ERROR - El documento radicado no se encontraba en formato docx, se radico un [."
							+ extension + "]");
			throw new ErrorFuncional("El documento radicado no se encuentra en formato 'docx'.  "
					+ "Se radicó un documento con la extensión '" + extension + "'. "
					+ "Comuníquese con la línea 2000 para asesoría en el formato de Word");
		}
		System.out.println(raizLog + "FIN VALIDACION");
	}

	/**
	 * Valida que el nombre del documento no esté vacio ni supere el maximo de
	 * caracteres permitidos
	 * 
	 * @param raizLog   cadena de caracteres que contiene información del id del
	 *                  flujo para identificar a que maquina pertenece el llamado
	 * @param documento Attachment donde se almaceno el documento descargado de CS
	 * @throws ErrorFuncional
	 * @exception NoSuchElementException lanza excepción si hay inconsistencias en
	 *                                   el nombre del documento
	 * 
	 */
	private static void validarNombreDoc(String raizLog, Attachment documento) throws ErrorFuncional {
		
		System.out.println(raizLog + "INICIO VALIDACION");
		String nombreDocOriginal = documento.getFileName();
																			
		if (!(StringUtils.hasText(nombreDocOriginal))
				&& (nombreDocOriginal.length() > Consts.TAMANO_MAXIMO_NOMBRE_DOCUMENTO_ORIGINAL)) {
			System.out.println(raizLog + "ERROR - Nombre del documento inconsistente o demasiado largo");
			throw new ErrorFuncional("Hay caracteres no compatibles en el nombre del documento radicado. "
					+ "Se radicó un documento con el nombre " + nombreDocOriginal);
		}
		System.out.println(raizLog + "FIN VALIDACION");
	}

 
	/**
	 * 
	 * @param raizLog
	 * @param documento
	 * @throws ErrorFuncional
	 */
	private static void validarTamano(String raizLog, Attachment documento) throws ErrorFuncional {
		System.out.println(raizLog + "INICIO VALIDACION");
		long tamano = documento.getFileSize();
		System.out.println(raizLog + "El documento tiene un tamaño de: ["+tamano+"]");
		if (tamano <= 0) {
			System.out.println(raizLog + "ERROR - Tamaño del documento inconsistente o documento vacío");
			throw new ErrorFuncional("Hay inconsistencias en el documento. " + "Se radicó un documento vacío");
																					 
		}
		System.out.println(raizLog + "FIN VALIDACION");

	}

 
	/**
	 * Valida que el asunto del documento no esté vacio
	 * 
	 * @param raizLog   cadena de caracteres que contiene información del id del
	 *                  flujo para identificar a que maquina pertenece el llamado
	 * @param categoria CAtegoria donde se almacenaron los datos leidos de la
	 *                  plantilla
	 * @throws ErrorFuncional
	 * @exception NoSuchElementException lanza excepción si el asunto está vacío
	 * 
	 */
	private static void validarVersionPlantilla(String raizLog, MetadatosPlantilla categoria, GlobalProps globalProperties)
			throws ErrorFuncional {

		System.out.println(raizLog + "INICIO VALIDACION");
		String versionOficialPlantilla = categoria.getTipologia().equalsIgnoreCase("ME")
				? globalProperties.getVersionPlantillaOficialMemorando()
				: globalProperties.getVersionPlantillaOficialCarta();
		String versionPlantillaDoc = categoria.getVersionPlantilla();

		System.out.println(raizLog + "la version de la plantilla addin es [" + versionPlantillaDoc
				+ "] la version de la propiedad de correspondencia es [" + versionOficialPlantilla + "]");

		if (!versionPlantillaDoc.equals(versionOficialPlantilla)) {
			System.out.println(raizLog + "ERROR - Plantilla desactualizada");
			throw new ErrorFuncional("Está trabajando sobre una plantilla desactualizada, "
					+ "descargue la plantilla desde iConecta y radique nuevamente");
		}
		System.out.println(raizLog + "FIN VALIDACION");
	}

	/**
	 * Valida que el asunto del documento no esté vacio
	 * 
	 * @param raizLog   cadena de caracteres que contiene información del id del
	 *                  flujo para identificar a que maquina pertenece el llamado
	 * @param categoria CAtegoria donde se almacenaron los datos leidos de la
	 *                  plantilla
	 * @throws ErrorFuncional
	 * @exception NoSuchElementException lanza excepción si el asunto está vacío
	 * 
	 */
	private static void validarGeneradoDesdePlantilla(String raizLog, MetadatosPlantilla categoria) throws ErrorFuncional {

		System.out.println(raizLog + "INICIO VALIDACION");
		Boolean esGeneradoDesdePlantilla = categoria.getEsGeneradoDesdePlantilla();
		if (!esGeneradoDesdePlantilla) {
			System.out.println(raizLog + "ERROR - El documento no fue generado desde una plantilla de iConecta");
			throw new ErrorFuncional(
					"El documento no fue generado a partir de la plantilla, descargue la plantilla desde iConecta "
							+ "y radique nuevamente");
		}else {
			System.out.println(raizLog + "El documento fue correctamente generado a partir de una plantilla de CS");
		}
		System.out.println(raizLog + "FIN VALIDACION");
	}

	/**
	 * Valida que el asunto del documento no esté vacio
	 * 
	 * @param raizLog   cadena de caracteres que contiene información del id del
	 *                  flujo para identificar a que maquina pertenece el llamado
	 * @param categoria CAtegoria donde se almacenaron los datos leidos de la
	 *                  plantilla
	 * @throws ErrorFuncional
	 * @exception NoSuchElementException lanza excepción si el asunto está vacío
	 * 
	 */
	private static void validarAsunto(String raizLog, MetadatosPlantilla categoria) throws ErrorFuncional {

		System.out.println(raizLog + "INICIO VALIDACION");
		String asunto = categoria.getAsunto();
		if (!(StringUtils.hasText(asunto)) || (asunto.equals("Incluir asunto"))) {
			System.out.println(raizLog + "ERROR - Asunto vacío");
			throw new ErrorFuncional("No se diligenció correctamente el asunto del documento. "
					+ "Corrija en la plantilla y vuelva a radicar");
		}else {
			System.out.println(raizLog + "Asunto del documento diligenciado correctamente");
		}
		System.out.println(raizLog + "FIN VALIDACION");
	}

	/**
	 * Valida que el firmante del documento no esté vacio
	 * 
	 * @param raizLog   cadena de caracteres que contiene información del id del
	 *                  flujo para identificar a que maquina pertenece el llamado
	 * @param categoria MetadatosPlantilla donde se almacenaron los datos leidos de la
	 *                  plantilla
	 * @throws ErrorFuncional
	 * @throws ErrorTecnologico 
	 * @exception NoSuchElementException lanza excepción si el firmante está vacío
	 * 
	 */
	private static void validarFirmante(String raizLog, MetadatosPlantilla categoria)
			throws ErrorFuncional, ErrorTecnologico {

		System.out.println(raizLog + "INICIO VALIDACION");
		List<Empleado> firmantes = categoria.getFirmantes();
											  
		
		System.out.println("\n" + raizLog + "MOSTRANDO DATOS DE LOS FIRMANTES DEL DOCUMENTO..." + "\n");

		for (Empleado firmante : firmantes) {
			System.out.println(raizLog+"Firmante Documento - {cedula, nombre, cargo, sigla} - {" + firmante.getRol() + ","
					+ firmante.getNombre() + "," + firmante.getCargo() + "," + firmante.getSiglaremitente() + "}");
		}
		
		System.out.println("\n" + raizLog + "FIN DATOS FIRMANTES DEL DOCUMENTO MOSTRADOS" + "\n");
		
		System.out.println(raizLog + "Se va a validar el primer firmante...");

		Empleado firmante1 = firmantes.get(0);
		if (firmante1.getDependencia().equals("Dependencia Remitente")) {
			System.out.println(raizLog + "ERROR - No se diligenció correctamente el firmante principal del documento");
			throw new ErrorFuncional("No se diligenció correctamente el firmante principal del documento. "
					+ "Corrija en la plantilla y vuelva a radicar");																																
													 
		} else {
			
			// TODO SE DEBE GENERAR LA RUTA EN EL ARCHIVO DE PROPERTIES DE CORR
			String strURLDestino = "http://172.23.30.67:8183/iconecta-firma/evaluarFirmante";//Url de firma a la cual se va a llamar
			
			// SE LLENA EL HASHMAP CON LOS PARAMETROS DE LA URL DESTINO
			HashMap<String, Object> params;
			params = new LinkedHashMap<>();
			params.put("idFirmante", firmante1.getRol());

			//SE REALIZA LLAMADO AL SERVICIO WEB
			try {
				System.out.println(raizLog + "Se va a llamar al controlador de FIRMA - evaluarFirmante....");
				String[] respuestaLlamadoHttpStrings = ControllerCaller.llamarAplicacionFirmaEvaluarFirmante(raizLog, params, strURLDestino, "POST");
				
				//Integer codigoRespuesta = Integer.parseInt(respuestaLlamadoHttpStrings[0]);
				//String mensaje = respuestaLlamadoHttpStrings[1];
				String strXmlResponse = respuestaLlamadoHttpStrings[2];
				System.out.println(raizLog + "Respuesta del controlador HTTP de FIRMA ["+strXmlResponse+"]");
				Boolean firmantePresente = Boolean.parseBoolean(strXmlResponse);
				
				System.out.println(raizLog + "Llamado al servicio web realizado con éxito");
				
				if(!firmantePresente) {
					System.out.println(raizLog
							+ "ERROR - El firmante principal del documento no se encuentra activo en la lista de firmantes de iConecta");
					throw new ErrorFuncional(
							"El firmante principal del documento no se encuentra activo en la lista de firmantes de iConecta. "
									+ "Corrija en la plantilla y vuelva a radicar");
				
				}
				
			} catch (Exception e) {
				//TODO ARREGLAR MENSAJE PARA USUARIO FINAL,
				/*
				 * El error puede iocurrir por:
				 * Falla al conectar con la app Firma
				 * Fallo extraño y se envió la cédula vacío o 
				 * cedula con un formato incorrecto, cedula que no es un número
				 */
				System.out.println(
						raizLog + "ERROR - No se pudo establecer conexión con la aplicación FIRMA - " + e.getMessage());
				throw new ErrorTecnologico("No se pudo establecer conexión con la aplicación FIRMA");
				
			}
			
		}
		
		System.out.println(raizLog + "Primer firmante diligenciado correctamente");

		Boolean esDobleFirmante = categoria.getEsDobleFirmante();

		if (esDobleFirmante) {
			System.out.println(raizLog + "Se va a validar el segundo firmante...");
			Empleado firmante2 = firmantes.get(1);
			if (firmante2.getDependencia().equals("Dependencia Remitente")) {
				System.out.println(
						raizLog + "ERROR - No se diligenció correctamente el firmante secundario del documento");
				throw new ErrorFuncional("No se diligenció correctamente el firmante secundario del documento. "
						+ "Corrija en la plantilla y vuelva a radicar");
																				   
																																					 
																																				 
													  
			} else {
				// TODO SE DEBE GENERAR LA RUTA EN EL ARCHIVO DE PROPERTIES DE CORR
				String strURLDestino = "http://172.23.30.67:8183/iconecta-firma/evaluarFirmante";//Url de firma a la cual se va a llamar
				
				// SE LLENA EL HASHMAP CON LOS PARAMETROS DE LA URL DESTINO
				HashMap<String, Object> params;
				params = new LinkedHashMap<>();
				params.put("idFirmante", firmante2.getRol());

				//SE REALIZA LLAMADO AL SERVICIO WEB
				try {
					System.out.println(raizLog + "Se va a llamar al controlador de FIRMA - evaluarFirmante....");
					String[] respuestaLlamadoHttpStrings = ControllerCaller.llamarAplicacionFirmaEvaluarFirmante(raizLog, params, strURLDestino, "POST");
					
					//Integer codigoRespuesta = Integer.parseInt(respuestaLlamadoHttpStrings[0]);
					//String mensaje = respuestaLlamadoHttpStrings[1];
					String strXmlResponse = respuestaLlamadoHttpStrings[2];
					System.out.println("Respuesta del controlador HTTP ["+strXmlResponse+"]");
					Boolean firmantePresente = Boolean.parseBoolean(strXmlResponse);
					
					System.out.println(raizLog + "Llamado al servicio web realizado con éxito");
					
					if(!firmantePresente) {
						System.out.println(raizLog
								+ "ERROR - El firmante principal del documento no se encuentra activo en la lista de firmantes de iConecta");
						throw new ErrorFuncional(
								"El firmante principal del documento no se encuentra activo en la lista de firmantes de iConecta. "
										+ "Corrija en la plantilla y vuelva a radicar");
					
					}
					
				} catch (Exception e) {
					//TODO ARREGLAR MENSAJE PARA USUARIO FINAL,
					/*
					 * El error puede iocurrir por:
					 * Falla al conectar con la app Firma
					 * Fallo extraño y se envió la cédula vacío o 
					 * cedula con un formato incorrecto, cedula que no es un número
					 */
					System.out.println(
							raizLog + "ERROR - No se pudo establecer conexión con la aplicación FIRMA - " + e.getMessage());
					throw new ErrorTecnologico("No se pudo establecer conexión con la aplicación FIRMA");
					
				}
				
			}
			System.out.println(raizLog + "Segundo firmante diligenciado correctamente");
		}
		
		System.out.println(raizLog + "FIN VALIDACION");

	}

	/**
	 * 
	 * @param raizLog
	 * @param categoria
	 * @throws ErrorFuncional
	 */
	private static void validarDestinos(String raizLog, MetadatosPlantilla categoria) throws ErrorFuncional {

		System.out.println(raizLog + "INICIO VALIDACION");
		List<Empleado> destinos = categoria.getDestinatarios();
		int cantidadDestinos = destinos.size();
		
		System.out.println(raizLog + "Cantidad de destinos: ["+cantidadDestinos+"]");

		if (cantidadDestinos == 0) {
			System.out.println(raizLog + "ERROR - No se diligenció correctamente el destinatario del documento");
			throw new ErrorFuncional("No se diligenció correctamente el destinatario del documento. "
					+ "Corrija en la plantilla y vuelva a radicar");
		} else {
			for (Empleado destino : destinos) {
				System.out.println(raizLog+"Destino Documento - {Nombre, Cargo, Dependencia} ==> {" + destino.getNombre() + "," + destino.getCargo() + ","+ destino.getDependencia() + "}");
				if (destino.getNombre().equals("Nombre Destinatario ")) {
					System.out
							.println(raizLog + "ERROR - No se diligenció correctamente el destinatario del documento");
					throw new ErrorFuncional("No se diligenció correctamente el destinatario del documento. "
							+ "Corrija en la plantilla y vuelva a radicar");
				}
			}

		}
		System.out.println(raizLog + "Destinos diligenciados correctamente");
		
		System.out.println(raizLog + "FIN VALIDACION");

	}

	/**
	 * 
	 * @param raizLog
	 * @param categoria
	 * @throws ErrorFuncional
	 */
	private static void validarCopias(String raizLog, MetadatosPlantilla categoria) throws ErrorFuncional {

		System.out.println(raizLog + "INICIO VALIDACION");
		List<Empleado> copias = categoria.getCopias();
		int cantidadCopias = copias.size();		
		System.out.println(raizLog + "Cantidad de copias: ["+cantidadCopias+"]");

		for (Empleado copia : copias) {
			System.out.println(raizLog+"Copia Documento - {Nombre, Cargo, Dependencia} ==> {" + copia.getNombre() + ","+ copia.getCargo() + ","+ copia.getDependencia() + "}");
			if (copia.getNombre().equals("Nombre") && copia.getCargo().equals("Cargo")) {
				System.out.println(
						raizLog + "ERROR - No se diligenció correctamente el destinatario-copia del documento");
				throw new ErrorFuncional("En la pestaña de destinatarios no se diligenció correctamente la información de la copia. "
						+ "Corrija en la plantilla y vuelva a radicar");
			}
		}
		System.out.println(raizLog + "Copias diligenciadas correctamente");
		
		System.out.println(raizLog + "FIN VALIDACION");

	}

	/**
	 * 
	 * @param categoria
	 * @param raizLog
	 * @return
	 * @throws ErrorFuncional
	 */
   
	private static boolean validarAnexos(String raizLog, MetadatosPlantilla categoria) throws ErrorFuncional {
		
		System.out.println(raizLog + "INICIO VALIDACION");
		String anexosStr = categoria.getAnexosElectronicos();
		Boolean anexos;
		if (StringUtils.hasText(anexosStr)) {
			anexos = Boolean.parseBoolean(anexosStr);
		} else {
			System.out.println(
					raizLog + "ERROR - EL campo de anexos esta vacio o no corresponde a un valor esperado. Se encontró el valor: [" + anexosStr + "]");																																
			throw new ErrorFuncional(
					"EL campo de anexos esta vacio o no corresponde a un valor esperado. Se encontró el valor: '" + anexosStr + "'");
		}

		System.out.println(raizLog + "Anexos [" + anexos + "]");
		
		System.out.println(raizLog + "FIN VALIDACION");
		return anexos;
	}

}
