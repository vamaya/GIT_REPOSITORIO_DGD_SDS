package co.gov.banrep.iconecta.ssm.firma.params;

public class GlobalConsts {
	
	//Parametros para el controller	
	
	/**Parametro para el id del objeto documento del Content Server*/
	public static final String ID_WORKFLOW = "idWorkflow";
	
	/**Parametro para el id del objeto documento del Content Server*/
	public static final String ID_DOC = "idDoc";
	
	/**Parametro para el id de más de un documento de Content Server*/
	public static final String ID_DOCS = "idDocs";
	
	public static final String ID_DOC_PRINCIPAL = "documentoPrincipal";
	
	/**Parametro para el id del objeto documento del Content Server*/
	public static final String CONEXION_USUARIO_CS = "conexionUsuario";
	
	/**Parametro para el id del objeto documento del Content Server*/
	public static final String NOMBRE_USUARIO_CS = "nombreUsuario";
	
	/** parametro que indica el asunto con el cual se crear el circuito en portafirmas*/
	public final static String 	ASUNTO = "asunto";
	
	/**Parametro que indica el nif que corresponde al firmante*/
	//public static final String NIFS_FIRMANTES = "nifFirmantes";
	
	/**Parametro para almacenar un map con la relacion entre el nif y el usuario en Content Server*/
	//public static final String NIFS_VS_USUARIO_CS = "nifUsuarioCS";	
	
	/**Parametro que indica los ids de CS que corresponde al firmante*/
	public static final String IDS_FIRMANTES = "listaFirmantes";
	
	/**Parametro que indica los ids CS de un solo firmante que corresponde al firmante*/
	public static final String ID_FIRMANTE = "idFirmante";
	
	
	//Parametros para el Map de ExtendedVariables de la SSM
	
	/**Parametro para la lista de elementos de entrada para procesar*/
	public static final String LISTA_IN = "listaIn";
	
	/**Parametro para la lista de elementos ya procesados*/
	public static final String LISTA_OUT = "listaOut";
	
	/**Parametro para el Map de documentos de la SSM*/
	public static final String DOCUMENTOS_SSM = "documentosSSM";
	
	/**Parametro con el id del circuito en POrtafirmas*/
	public final static String ID_CIRCUITO = "idCircuito";
	
	/**Parametro para el String con el listado de datos de respuesta de portafirmas*/
	public static final String DATOS_PF = "datosPF";
	
	/**Parametro para el String el dato que indica que la firma fue cancelada*/
	public static final String FIRMA_CANCELADA = "firmaCancelada";
	
	/**Parametro para almecenar el motivo de cancelacion del circuito de firma*/
	public static final String MOTIVO_CANCELACION = "motivo";
	
	/**Parametro que almacena el dato de quien cancelo el circuito*/
	public static final String USUARIO_CANCELACION = "usuarioCancela";
	
	/**Parametro que almacena el nombre de quien cancelo el circuito*/
	public static final String NOMBRE_USUARIO_CANCELACION = "usuarioCancelaNombre";
	
	/**Parametro para establecer si se requiere o no copia compulsada*/
	public static final String COPIA_COMPULSADA = "copiaCompulsada";
	
	/**Parametro para establecer si se actualiza o no la categoria documento*/
	public static final String ACTUALIZAR_CAT_DOCUMENTO = "actualizarCatDocumento";
	
	/**Parametro que indica que el circuito fue cancelado*/
	public static final String ID_USUARIO_CS = "idUsuario";
	
	/**Parametro que contiene la etiqueta del atributo Firmantes del workflow*/
	public static final String ATRIBUTO_FIRMANTES = "Firmantes";
	
	/**Parametro para indicar si hubo o no un error al cargar los datos de Portafirmas*/
	public static final String ERROR_DATOS_PF = "errorPortafirmas";
	
	/**Parametro para indicar si hubo o no un error en la maquina de estados*/
	public static final String ERROR_MAQUINA = "errorMaquina";
	
	/**Parametro para almacenar mensajes de error*/
	public static final Object MENSAJE_ERROR = "mensajeError";
	
	/**Parametro para almacenar el estado donde se produjo un error*/
	public static final Object ESTADO_ERROR = "estadoError";
	
	/**paramentro para guardar el numero de reintento actual*/
	public static final String REINTENTO = "reintento";
	
	/**Parametro para guardar el tipo de accion externa*/
	public static final String ACCION_EXTERNA = "accionExterna";
	
	/**Parametro para guardar la url del servicio asociado a la accion externa*/
	public static final String URL_EXTERNA = "urlExterna";
	
	/**Parametro para guardar los firmantes del proceso*/
	public static final String FIRMANTES = "firmantes";
	
	/**Parametro para guardar el nombre del primer firmante*/
	public static final String FIRMANTE1_NOMBRE = "firmante1Nombre";
	
	/**Parametro para guardar el correo del primer firmante*/
	public static final String FIRMANTE1_CORREO = "firmante1Correo";
	
	/**Parametro para alamcenar los comentarios enviados por el solicitante*/
	public static final String COMENTARIOS = "comentarios";
	
	/**Parametro para indicar si se enviar notificacion al solicitante del inicio del proceso de firma*/
	public static final String NOTIFICAR_SOLICITANTE = "notificarSolicitante";
			
	//Constantes para modificar el nombre del adjunto
	
	public static final String NOMBRE_MODIFICADO_DOC_PREFIJO = "_(adjunto ";	
	public static final String NOMBRE_MODIFICADO_DOC_SUFIJO = ").pdf";
	
	
	//Constantes para logback	
	public static final String PREFIJO_LOG = "Maquina [";		
	public static final String SUFIJO_LOG = "] - ";
	
	//Constantes con los valores fijos de categorias
	//private static final String ESTADO_CS_VALOR = "Oficial";
	public static final String ESTADO_CS_VALOR = "Firmado Digitalmente";
	public static final String TIPO_FIRMA_CS_VALOR = "Firma con Certificado Digital";

	public static final String USUARIO_CANCELACION_CEDULA = "usuarioCancelaCedula";

	public static final String IDS_COPIAS_COMPULSADAS = "idsCopiasCompulsadas";
	
	 //Este método previene la construción de objetos de esta clase	
	private GlobalConsts(){
		throw new AssertionError();
	}

}
