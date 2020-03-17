package co.gov.banrep.iconecta.ssm.correspondencia.utils;

public final class Consts {
	
	private Consts() {
		throw new AssertionError();
	}
	
	/**Tamano maximo permitido por CS para los atributos de tipo texto*/
	public static final int TAMANO_MAXIMO_CS = 149;
	
	/**Tamano maximo permitido por CS para los atributo remitente del DER que esta relacionado con la categoria de correspondencia*/
	public static final int TAMANO_MAXIMO_CS_REMITENTE_DER = 100;
	
	/**Tamano maximo permitido por CS para los atributo destino del DER que esta relacionado con la categoria de correspondencia*/
	public static final int TAMANO_MAXIMO_CS_DESTINO_DER = 120;
	
	/**Tamano maximo permitido por CS para el atributo asunto en la tipología Carta*/
	public static final int TAMANO_MAXIMO_CS_ASUNTO = 249;
	
	/**Tamano maximo permitido por CS para los atributos : Nombre, cargo y dependencia. En la tipología Carta*/
	public static final int TAMANO_MAXIMO_ATRIBUTOS_DESTINO = 99;
	
	/**Tamano maximo permitido por CS para los atributos : Nombre, cargo y dependencia. En la tipología Carta*/
	public static final int TAMANO_MAXIMO_NOMBRE_DOCUMENTO_ORIGINAL = 200;
	
	/**Parametro para indicar si hubo o no un error en la maquina de estados*/
	public static final String ERROR_MAQUINA = "errorMaquina";
	public static final String MENSAJE_ERROR = "mensajeError";
	public static final String NUMERO_RADICADO = "numeroRadicado";
	
	/**Parametro para recibir el id del documento*/
	public static final String ID_DOC_RADICACION = "idDoc";
	public static final String ID_FORM = "idForm";
	
	public static final String CONEXION_CS_SOLICITANTE = "conexionCS";
	
	
	//Constantes para logback	
	public static final String PREFIJO_LOG = "Maquina CORR [";		
	public static final String SUFIJO_LOG = "] - ";
	
	//Constantes para el contexto de la Maquina de estados
	public static final String CONTENIDO_DOC = "contenidoDoc";
	public static final String NOMBRE_DOCUMENTO_ORIGINAL = "nombreDocOriginal";
	public static final String ASUNTO = "asuntoDocumento";
	public static final String CODIGO_ERROR_RADICAR = "codigoErrorRadicar";
	public static final String NIT_FIRMANTE = "nitFirmante";
	
	public static final String LISTA_NIT_FIRMANTES = "listaNitFirmantes";
	
	public static final String METADATOS_PLANTILLA = "metadatosPlantilla";
	public static final Object DESTINATARIOS = "destinatarios";
	public static final Object COPIAS = "copias";
	public static final String ID_SOLICITANTE = "idSolicitante";
	public static final Object LISTA_ROLES_DESTINATARIOS = "listaRolesDestinatarios";
	public static final String RESULTADO_DISTRIBUIR = "resultadoDistribuir";
	public static final Object NUMERO_TOTAL_DESTINATARIOS = "numeroTotalDestinatarios";
	public static final Object NUMERO_TOTAL_DESTINATARIOS_CONFIRMADOS = "numeroTotalDestinatariosConfirmados";
	public static final Object NUMERO_TOTAL_DESTINATARIOS_RECHAZADOS = "numeroTotalDestinatariosRechazados";
	public static final String NOMBRE_FIRMANTE = "nombreFirmante";
	public static final Object LISTA_MENSAJES = "listaMensajes";
	public static final String NUMERO_RADICADO_OBTENIDO = "numeroRadicadoObtenido";
	
	public static final String NOMBRE_DOCUMENTO_SERVIDOR = "nombreDocumentoServidor"; //Vamaya añadido
	public static final String ID_DOCS_ADJUNTOS = "idDocsAdjuntos"; //Vamaya añadido
	//public static final String ID_DOCUMENTO = "idDoc"; //Vamaya añadido
	
	//Variables para los mensajes de error en base de datos
	public final static Integer MSJ_100 = 100;
	public final static Integer MSJ_101 = 101;
	public final static Integer MSJ_102 = 102;
	public final static Integer MSJ_103 = 103;
	public final static Integer MSJ_104 = 104;
	public final static Integer MSJ_105 = 105;
	public final static Integer MSJ_106 = 106;
	public static final Integer MSJ_107 = 107;
	public static final Integer MSJ_108 = 108;
	public static final Integer MSJ_109 = 109;
	public static final Integer MSJ_110 = 110;
	public static final Integer MSJ_111 = 111;
	
	public static final String SIGLA_RDI_RDE = "siglaRdiRde";
	public static final String TIPOLOGIA_RDI_RDE = "tiplogiaRdiRde";
	public static final String ASUNTO_RDI_RDE = "asuntoRdiRde";
	public static final String LISTA_ORIGENES_RDI_RDE = "listaOrigenesRdiRde";
	public static final String LISTA_DESTINOS_RDI_RDE = "listaDestinosRdiRde";
	public static final String ORIGEN_RDI_RDE = "origenRdiRde";
	public static final String DESTINO_RDI_RDE = "destinoRdiRde";
	public static final String RESULTADO_DISTRIBUIR_RDI_RDE = "resultadoDistribuirRdiRde";
	public static final String ROL_ACTUAL_DISTRIBUIR_RDI_RDE = "rolActualDistribuirRdiRde";
	public static final Object RUTA_TOTAL_RDI_RDE = "rutaTotalRdiRde";
	public static final Object ROL_SIGUIENTE_DISTRIBUIR_RDI_RDE = "rolSiguienteDistribuirRdiRde";
	public static final Object FINALIZO_DISTRIBUCION_RDI_RDE = "finalizoDistribucionRdiRde";
	public static final Object MAP_RUTAS__RDI_RDE = "mapRutasRdiRde";
	public static final String CODIGO_RUTA = "codigoRuta";
	public static final String ES_MENSAJERIA_RDI_RDE = "esMensajeriaRdiRde";
	public static final Object LIST_DEPENDENCIA_DESTINO_RDI_RDE = "listDependenciaDestinoRdiRde";
	public static final Object LIST_PCR_DESTINO_RDI_RDE = "listPcrDestinoRdiRde";
	public static final Object LIST_CDD_DESTINO_RDI_RDE = "listCddDestinoRdiRde";
	public static final String RESPUESTA_CDD_DISTRIBUIR_RDI_RDE = "respuestaCddDistribuirRdiRde";
	public static final String ORIGEN_RDI_RDE_PCR = "origenRdiRdePcr";
	public static final String PARENT_ID_ADJUNTOS_RDI_RDE = "parentIdAdjuntosRdiRde";
	public static final Object DEPENDENCIA_ORIGEN_RDI = "dependenciaOrigenRdiRde";
	public static final String NOMBRE_DEPENDENCIA_ORIGEN = "nombreDependenciaOrigen";
	public static final String PASO_POR_COURRIER = "pasoPorCurrier";
	public static final String ES_SUCURSAL = "esSucursal";
	public static final String ORIGEN_ES_PCR = "origenEsPcr";
	//Permite verificar si el tipo WF es confidencial o normal
	public static final String TIPO_WF_DOCUMENTO = "tipoWfDocumento";
	public static final Object TIENE_ANEXOS = "tieneAnexos";
	public static final String MENSAJE_RESPUESTA_FIRMA = "mensajeRespuestaFirma";
	public static final String PARENT_ID_ADJUNTOS_MEMO_CARTA = "parentIdAdjuntosMemoCarta";
	public static final String PARENT_ID_ADJUNTOS_WF_MEMO_CARTA = "parentIdAdjuntosWfMemoCarta";
	public static final Object LISTA_ID_DOCS_PERSONALIZADOS_CARTA = "listaIdDocsPersonalizadosCarta";
	public static final Object CADENA_LISTA_ID_DOCS_PERSONALIZADOS_CARTA = "cadenaListaIdDocsPersonalizadosCarta";
	public static final String ES_PERSONALIZADO = "esPersonalizado";
	public static final String TIPO_DOCUMENTAL_CAT_DOCUMENTO = "tipoDocumentalCatDocumento";
	public static final String SERIE_CAT_DOCUMENTO = "serieCatDocumento";
	public static final Object TIPOLOGIA_MEMO_CARTA = "tipologiaMemoCarta";
	public static final String FECHA_RADICACION = "fechaRadicacion";
	public static final String PCR_ORIGEN_CARTA = "pcrOrigenCarta";
	public static final String CDD_ORIGEN_CARTA = "cddOrigenCarta";
	public static final String ID_DOC_STICKER_CARTA = "idDocStickerCarta";
	public static final Object LISTA_NOMBRES_DOCS_PERSONALIZADOS_CARTA = "listaNombresDocsPersonalizadosCarta";
	public static final Object LISTA_ID_DOCS_COPIAS_COMPULSADAS = "listaIdDocsCopiasCompulsadas";
	public static final Object DESTINO_INDIVIDUAL_CARTA = "destinoIndividualCarta";
	public static final Object RUTA_COMPLETA = "rutaCompleta";
	public static final String OFICINA_DER = "oficinaDer";
	public static final String CDD_ORIGEN_DER = "cddOrigenDer";
	public static final String PCR_ORIGEN_DER = "pcrOrigenDer";
	public static final Object LISTA_DESTINOS_DER = "listaDestinosDer";
	public static final String ROL_CALIDAD_DIGITALIZAR_DER = "rolCalidadDigitalizarDer";
	public static final String TIPOLOGIA_DER = "tipologiaDer";
	public static final String SIGLA_DER = "siglaDer";
	public static final String DEPENDENCIA_ORIGEN_DER = "dependenciaOrigenDer";
	public static final String ID_OBJETO_FISICO = "idObjetoFisico";
	public static final String ID_DOCS_DIGITALIZADOS_DER = "idDocsDigitalizadosDer";
	public static final String TIPO_COMUNICACION_DER = "tipoComunicacionDer";
	public static final Object CADENA_ANEXOS_CANTIDAD_DESCRIPCION_DER = "cadenaAnexosCantidadDescripcionDer";
	public static final String ROL_SIN_COPIAS = "00_PCR_COPIAS";
	public static final String RADICADO_DCIN = "radicadoDCIN";
	public static final String PASO_CALIDAD = "PASO_CALIDAD";
	public static final String PASO_DIGITALIZAR = "PASO_DIGITALIZAR";
	public static final String CDD_DESTINO_DER = "CDD_DESTINO_DER";
	public static final String PCR_DESTINO_DER = "PCR_DESTINO_DER";
	public static final String ASIGNAR_RUTAS = "ASIGNAR_RUTAS";
	public static final String DESTINOS_DER = "DESTINOS_DER";
	public static final String FORMA_ENTREGA_DER = "FORMA_ENTREGA_DER";
	public static final String DEPENDENCIA_ORIGINAL = "DEPENDENCIA_ORIGINAL";
	public static final String DER_CORREO = "DER_CORREO";
	public static final String DEPENDENCIA_DESTINO = "DEPENDENCIA_DESTINO";
	public static final String REMITENTE = "REMITENTE";
	public static final String REINTENTO="reintento";
	public static final String DISTRIBUCION_RUTA = "destribucionRuta";
	public static final String ID_RUTA = "idRuta";
	public static final String NOMBRE_DOCUMENTO_ORIGINAL_DELETE = "nombre_documento_original_eliminar";
	public static final String ID_PCR_ORIGEN_CARTA = "id_pcr_origen_carta";
	public static final String ID_CDD_ORIGEN_CARTA = "id_cdd_origen_carta";
	public static final String TRAMITE_DCIN = "tramite_DCIN";
	public static final String PBX_ORIGEN = "pbx_origen";
	public static final String GENERAR_DCIN = "generar_DCIN";
	public static final String NUMERO_RADICADO_DCIN = "numero_radicado_DCIN";
	//public static final String IDWORKFLOW_NOTI_DER = "id_notoficacion_workflow"; //Obsoleto
	public static final String SIN_NUMERO_RADICADO = "Sin numero de Radicado";
	public static final String DISTRIBUIDO = "Estado_Distribuido";
	public static final String OPERACION = "Operacion_Distribucion";
	public static final String ID_WORKFLOW_DIS = "idWorkFlowDist";
	public static final String ID_DOCUMENTO_ADJUNTO_DIS = "idDocumentoAdjuntoDis";
	public static final String METADATOS_ADJUNTOS_DIS = "metadatosAdjuntosDis";
	public static final String PARENT_ID_ADJUNTOS_MEMO_CARTA_ORIGINAL = "parentIdOriginal";
	
	//Tamanos maximo de los metadatos de particiapentes de la categoria de correspondencia	
	public static final int TAMANO_MAXIMO_NOMBRE_CATEGORIA = 100;
	public static final int TAMANO_MAXIMO_REMIENTENTE_DEPENDECIA_CATEGORIA = 120;
	public static final int TAMANO_MAXIMO_DEPENDECIA_CATEGORIA = 120;
	public static final int TAMANO_MAXIMO_CARGO_CATEGORIA = 100;

	public static final String CATEGORIA_CARGO = "Cargo";
	public static final String CATEGORIA_NOMBRE = "Nombre";
	public static final String CATEGORIA_DEPENDENCIA_REMITENTE = "Dependencia";
	public static final String CATEGORIA_DEPENDENCIA = "Dependencia / Entidad";
	
	//Tamanos maximos de los atributos del flujo de distribucion de carta
	public static final int TAMANO_MAXIMO_CARTA_TIPOLOGIA = 10;
	public static final int TAMANO_MAXIMO_CARTA_ASUNTO = 250;
	public static final int TAMANO_MAXIMO_CARTA_NUMERO_RADICADO = 50;
	public static final int TAMANO_MAXIMO_CARTA_DESTINO = 250;
	public static final int TAMANO_MAXIMO_CARTA_CONEXIONCS = 32;	
	public static final int TAMANO_MAXIMO_CARTA_FECHA_RADICACION = 32;
	public static final int TAMANO_MAXIMO_CARTA_WORKID = 50;
	public static final int TAMANO_MAXIMO_NOMBRE_DESTINO = 100;
	public static final int TAMANO_MAXIMO_ENTIDAD_DESTINO = 100;

	public static final String NOMBRE_USUARIO_CANCELA = "usuarioCancela";	
	
	public static final String CONTROLADOR_RUTAS = "controladorRutas";

	/**Constante para indicar rechazo de un DER, enviado desde Workflow de distribucion*/
	public static final String RUTA_RECHAZADA = "Rechazado";

	/**Constante para indicar esatdo confirmado, categoria de CORR*/
	public static final String ESTADO_CONFIRMADO = "Confirmado";

	public static final String FIN_RADICACION_DER = "isFinRadicacionDER";
	
	/** Parametro para almacenar el id del WF remoto DER */
	public static final String ID_WORKFLOW_REMOTO_DER = "idWorkFlowRemotoDER";

	/** Parametro para almacenar el estado desde el cual se hara la logica de reintentos */
	public static final String ESTADO_REINTENTO = "ESTADOREINTENTO";
	
	public static final String SIGLA_FIRMANTE = "00_SGR_FIRMANTE";

	public static final String METADATOS_CATEGORIA_CORR_DER = "metadatosCategoriaCorrDer";

	public static final String METADATOS_SUBGRUPO_ORIGEN_CAT_CORR_DER = "metadatosSubgrupoOrigenCatCorrDer";

	public static final String METADATOS_SUBGRUPO_DESTINO_CAT_CORR_DER = "metadatosSubgrupoDestinoCatCorrDer";

	public static final String ES_DISTRIBUCION_FISICA_DER = "esDistribucionFisicaDer";

	public static final String ID_DOC_CONSECUTIVO = "idDocConsecutivo";

	public static final String ID_DOC_DIGITALIZADO_DER = "idDocDigitalizadoDer";

	public static final String CORREO_REMITENTE_DER_EMAIL = "correoRemitenteDerEmail";

	public static final String CORREO_DESTINO_DER_EMAIL = "correoDestinoDerEmail";

	public static final String DESTINO_DER_EMAIL = "destinoDerEmail";

	public static final String PARENT_ID_ADJUNTOS_DER_EMAIL = "parentIdAdjuntosDerEmail";
	
	
	
	//Constantes para iniciar WF de generar numero de radicado
	public static final String TITULO_WF_GENERAR_NUM_RADICADO = "FT104_Generar_NroRadicado";
	public static final String ATRIBUTO_NUM_RADICADO = "_radicadoGenerado";
	public static final String ATRIBUTO_SIGLA = "_sigla";	
	public static final String ATRIBUTO_TIPOLOGIA = "_tipologia";	
	public static final String ATRIBUTO_ID_SOLICITANTE = "_idSolicitante";

	public static final String NOMBRE_SOLICITANTE = "nombreSolicitante";

	public static final String HORA_RADICACION = "horaRadicacion";

	
	/*
	 * CONSTANTES PARA VARIABLES DE LA SSM - CORR
	 * CARTA Y MEMORANDO
	 */
	public static final String PARENT_ID_ORIGINAL_MEMO_CARTA = "parentIdOriginal";	
	public static final String ID_WORKFLOW_RADICACION = "workId";

	
	
	
	
	public static final String LISTA_DTHL_DOCS_PERSONALIZADOS_CARTA = "listaDTHLDocsPersonalizadosCarta";

	public static final String RESULTADO_FIRMA = "resultadoFirma";
	
	
}

