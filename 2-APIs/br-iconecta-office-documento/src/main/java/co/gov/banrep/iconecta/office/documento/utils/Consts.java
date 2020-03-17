package co.gov.banrep.iconecta.office.documento.utils;

public final class Consts {
	
	private Consts() {
		throw new AssertionError();
	}
	/*
	 * Constantes Etiquetas Radicación Documento
	 */
	public static final String NUMERO_RADICADO = "numeroRadicado";
	public static final String FECHA_RADICADO = "fechaRadicado";	
	/*
	 * Constantes Etiquetas Destino Documento
	 */
	public static final String NOMBRE_DESTINO = "nombreDestino";
	public static final String CARGO_DESTINO = "cargoDestino";
	public static final String DEPENDENCIA_DESTINO = "dependenciaDestino";
	public static final String PCR_DESTINO = "pcrDestino";
	public static final String CIUDAD_DESTINO = "ciudadDestino";
	public static final String REFERENCIA_DESTINO = "referenciaDestino";
	/*
	 * Constantes Etiquetas Asunto Documento
	 */
	
	public static final String ASUNTO = "asunto";
	/*
	 * Constantes Etiquetas Firmante Documento
	 */
	public static final String NOMBRE_FIRMANTE = "nombreFirmante";
	public static final String CARGO_FIRMANTE = "cargoFirmante";
	public static final String DEPENDENCIA_FIRMANTE = "dependenciaFirmante";
	public static final String ID_FIRMANTE = "idFirmante";
	public static final String SGR_FIRMANTE = "sgrFirmante";
	public static final String FIRMA_FIRMANTE = "firmaFirmante";
	public static final String NOMBRE_PCR = "nombrePCR";
	public static final String NOMBRE_CDD = "nombreCDD";
	public static final String ID_PCR = "idPCR";
	public static final String ID_CDD = "idCDD";
	/*
	 * Constantes Etiquetas Copias Documento
	 */
	public static final String COPIAS = "copias";
	public static final String PCR_COPIAS = "pcrCopias";
	
	/*
	 * Constantes Etiquetas Custom Documento
	 */
	public static final String TIPOLOGIA_PLANTILLA = "tipologiaPlantilla";
	public static final String TIPO_PLANTILLA = "tipoPlantilla";
	public static final String PERSONALIZADA_PLANTILLA = "personalizadaPlantilla";
	public static final String IDIOMA_PLANTILLA = "idiomaPlantilla";
	public static final String TIPO_ENVIO_PLANTILLA = "tipoEnvioPlantilla";
	public static final String VERSION_PLANTILLA = "versionPlantilla";
	public static final String ES_FONDO_INDEPENDIENTE = "esFondoIndependiente";
	public static final String ES_CORREO_CERTIFICADO = "esCorreoCertificado";
	public static final String ES_IMPRESION_AREA = "esImpresionArea";
	public static final String CORREO_COPIA = "correoCopia";
	public static final String ES_DOBLE_FIRMANTE = 	"SegundoRemitente";
	

	/*
	 * Constantes Etiquetas Anexos Documento
	 */
	public static final String ANEXOS_FISICOS = "anexosFisicos";
	public static final String ANEXOS_ELECTRONICOS = "anexosElectronicos";
	/*
	 * Constantes Tipologia Documento
	 */
	public static final String TIPOLOGIA_PLANTILLA_CA = "tipologiaPlantillaCarta";
	
	/**
	 * Constantes de personalizar Documento
	 */
	
	public static final String TITULO_DESTINATARIOS = "Según relación adjunta";
	
	public static final String TITULO_TABLA = "Relación adjunta destinatarios carta";
	
	public static final String ETIQUETA_DESTINATARIO = "des_00";

	public static final String TIPO_NORMAL = "Normal";
	
	public static final String TIPO_CONFIDENCIAL = "Confidencial";
	
	public static final String SIGLA_FIRMANTE = "00_SGR_FIRMANTE";
	
	public static final String REEMPLAZO_CARACTERES_ESPECIALES = " ";
	
	public static final int TAMANO_MAXIMO_NOMBRE_ARCHIVO = 200;
	
	/*
	 * Constantes ajustes
	 */
	
	public static final String CARACTER_RETORNO_CORREO_COPIA = "_x000d_"; //Constante para eliminar el caracter especial que añade el Add-In de correspondencia
	
	

}
