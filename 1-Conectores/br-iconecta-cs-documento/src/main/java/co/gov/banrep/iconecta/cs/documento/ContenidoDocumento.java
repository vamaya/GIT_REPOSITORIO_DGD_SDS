package co.gov.banrep.iconecta.cs.documento;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.sun.xml.ws.api.message.Header;
//import com.sun.xml.ws.client.ClientTransportException;
import com.sun.xml.ws.developer.WSBindingProvider;
import com.sun.xml.ws.fault.ServerSOAPFaultException;

import co.gov.banrep.iconecta.cs.cliente.document.Attachment;
import co.gov.banrep.iconecta.cs.cliente.document.AttributeGroup;
import co.gov.banrep.iconecta.cs.cliente.document.AttributeSourceType;
import co.gov.banrep.iconecta.cs.cliente.document.BooleanValue;
import co.gov.banrep.iconecta.cs.cliente.document.CopyOptions;
import co.gov.banrep.iconecta.cs.cliente.document.DataValue;
import co.gov.banrep.iconecta.cs.cliente.document.DateValue;
import co.gov.banrep.iconecta.cs.cliente.document.DocumentManagement;
import co.gov.banrep.iconecta.cs.cliente.document.DocumentManagement_Service;
import co.gov.banrep.iconecta.cs.cliente.document.IntegerValue;
import co.gov.banrep.iconecta.cs.cliente.document.Metadata;
import co.gov.banrep.iconecta.cs.cliente.document.MoveOptions;
import co.gov.banrep.iconecta.cs.cliente.document.Node;
import co.gov.banrep.iconecta.cs.cliente.document.NodePermissions;
import co.gov.banrep.iconecta.cs.cliente.document.NodeRight;
import co.gov.banrep.iconecta.cs.cliente.document.NodeVersionInfo;
import co.gov.banrep.iconecta.cs.cliente.document.RealValue;
import co.gov.banrep.iconecta.cs.cliente.document.RowValue;
import co.gov.banrep.iconecta.cs.cliente.document.StringValue;
import co.gov.banrep.iconecta.cs.cliente.document.TableValue;
import co.gov.banrep.iconecta.cs.cliente.document.Version;
import co.gov.banrep.iconecta.cs.documento.utils.LlamadosWS;
import co.gov.banrep.iconecta.cs.documento.utils.NombreServicio;
import co.gov.banrep.iconecta.cs.documento.utils.ParametrosServicios;
/**
 * 
 * Utilidades para la manipulaci�n de documentos a trav�s del servicio
 * DocumentManagement de Content Server
 * 
 * @author <a href="mailto:jjrojassa@banrep.gov.co">John Jairo Rojas S.</a>
 *
 */
public final class ContenidoDocumento {

	private static final String PREFIJO_COPIA_COMPULSADA = "(CC) ";

	/**
	 * No se permite la creaci�n de instacias de esta clase
	 */
	private ContenidoDocumento() {
		throw new AssertionError();
	}

	/**
	 * Obtiene el contenido de un documento alojado en el Content Server
	 * 
	 * @param soapAuthHeader
	 *            que contiene el token de autenticaci�n del servicio
	 * @param usuario
	 *            conexi�n de usuario de Content Server
	 * @param idDoc
	 *            id del documento en el Content Server
	 * @param directoryPath,
	 *            Opcional, directorio donde se crean los documentos temporales,
	 *            Default temp/
	 * @return un objeto DataHandler de javax.activation.DataHandler con el
	 *         contenido del documento
	 * @throws Exception
	 */
	public static Path obtenerContenidoDocumento(Header soapAuthHeader, long idDoc, String directoryPath, Long idWork ,String wsdl)
			throws Exception {

		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no puede ser nulo");
		}

		if (directoryPath == null) {
			throw new NullPointerException("El parametro directoryPath no puede ser nulo");
		}

		if (directoryPath.isEmpty()) {
			throw new IllegalArgumentException("El parametro directoryPath no puede estar vacio");
		}

		if (idDoc < 1) {
			throw new IllegalArgumentException("El parametro idDoc (" + idDoc + ") no es valido");
		}

		byte[] documentoBytes = null;
		Attachment attachment = new Attachment();

		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		DocumentManagement_Service documentService = new DocumentManagement_Service(newURLWSDL, qName);
		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();

		// Lamado al servicio de Obtener Contenido del Content Server
		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);


		try {
			//attachment = client.getVersionContents(idDoc, 0);
			//llamarServicio(DocumentManagement client, ParametrosServicios servicioParams, NombreServicio llave)
			//attachment = LlamadosWS.llamarGetVersionContents(client, idDoc, 0L);
			ParametrosServicios servicioParams = new ParametrosServicios().withIdDoc(idDoc).withVersion(0L);
			attachment = (Attachment) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_VERSIONS_CONTENTS);
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException(
					"Error al llamar al servicio getVersionContents de Content Server- ORIGINAL: " + e.getMessage(), e);
		}

		documentoBytes = attachment.getContents();
		
		//Bloque para adicionar el parametro opcional de idWork que se le adicionara el documento para hacerlo unico
		if (idWork == null) {
			SecureRandom sr = null;

			try {
				sr = SecureRandom.getInstance("SHA1PRNG", "SUN");
			} catch (Exception e) {
				e.printStackTrace();
				idWork = new Long(0);
			}
			
			if(sr != null) {
				idWork = sr.nextLong();
			}else {
				idWork = new Long(0);
			}
		}

		// Se crea un fileDataSource con el nombre del documento
		// Si la ruta del directorio temporal no se recibe como parametro se
		// envia establece la ruta por defecto/
		// Se concatena el id de documento y del workflow para evitar que existan documentos repetidos
		FileDataSource fileDataSource = null;
		fileDataSource = new FileDataSource(directoryPath + "/" + idWork + "-" + idDoc + "-"  + attachment.getFileName());

		// Se copia el documento en Bytes detro del File de FileDataSource
		try {
			FileUtils.writeByteArrayToFile(fileDataSource.getFile(), documentoBytes);
		} catch (IOException e) {
			throw new IOException("Error al pasar del byte a File", e);
		}

		URI uri;
		try {
			uri = fileDataSource.getFile().toURI();
		} catch (Exception e) {
			throw new Exception("Error obteniedo URI del archivo", e);
		}

		Path path;
		try {
			path = Paths.get(uri);
		} catch (Exception e) {
			throw new Exception("Error obtendiento path a partir del URI", e);
		}

		return path;

	}
	
	
	
	
	//TODO BORRAR SI SE DEFINE COMO OBSOLETA
	/**
	 * 
	 * Retorna el contenido de un documento del Content Server
	 * 
	 * @param soapAuthHeader contiene el token de autenticaci�n del servicio
	 * @param idDoc Corresponde al id del documento en CS
	 * @param directoryPath directorio donde se crean los documentos temporales,
	 *            Default temp/
	 * @param workflow Corresponden al id del workflow donde esta el documento
	 * @param wsdl url de la wsdl del servicio web de CS
	 * @return un objento DataHandler con el contenido del documento solicitado
	 * @throws SOAPException Si el CS arroja un error en un servicio web
	 * @throws IOException Si se presenta un error de escritura o lectura en la ruta temporal
	 */
	public static DataHandler obtenerContenidoDocumentoDTHLR(Header soapAuthHeader, long idDoc, String directoryPath, String workflow,
			String wsdl) throws SOAPException, IOException, InterruptedException {

		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no puede ser nulo");
		}

		if (directoryPath == null) {
			throw new NullPointerException("El parametro directoryPath no puede ser nulo");
		}

		if (directoryPath.isEmpty()) {
			throw new IllegalArgumentException("El parametro directoryPath no puede estar vacio");
		}

		if (idDoc < 1) {
			throw new IllegalArgumentException("El parametro idDoc (" + idDoc + ") no es valido");
		}

		byte[] documentoBytes = null;
		Attachment attachment = new Attachment();

		DataHandler docDataHandler = null;

		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

	//	DocumentManagement_Service documentService = new DocumentManagement_Service(newURLWSDL, qName);
		
		DocumentManagement_Service documentService = null;
		
		try{
			documentService = new DocumentManagement_Service(newURLWSDL, qName);
		}
		catch(WebServiceException e) {
			throw new SOAPException(
					"Se presento un error en el DocumentManagement_Service en la URL: "+newURLWSDL + " - " + e.getMessage(), e);			
			
		}
		
		
		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();

		// Lamado al servicio de Obtener Contenido del Content Server
		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);

		try {
			//attachment = client.getVersionContents(idDoc, 0);
			//attachment = LlamadosWS.llamarGetVersionContents(client, idDoc, 0L);
			ParametrosServicios servicioParams = new ParametrosServicios().withIdDoc(idDoc).withVersion(0L);
			attachment = (Attachment) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_VERSIONS_CONTENTS);
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException(
					"Error al llamar al servicio getVersionContents de Content Server- ORIGINAL: " + e.getMessage(), e);
		}

		documentoBytes = attachment.getContents();

		// Se crea un fileDataSource con el nombre del documento
		// Si la ruta del directorio temporal no se recibe como parametro se
		// envia establece la ruta por defecto
		FileDataSource fileDataSource = null;
		fileDataSource = new FileDataSource(directoryPath + "/" + workflow+ "-" +attachment.getFileName());

		// Se copia el documento en Bytes detro del File de FileDataSource
		try {
			FileUtils.writeByteArrayToFile(fileDataSource.getFile(), documentoBytes);
		} catch (IOException e) {
			throw new IOException("Error al pasar del byte a File", e);
		}
		docDataHandler = new DataHandler(fileDataSource);

		return docDataHandler;
		// return Paths.get(fileDataSource.getFile().getPath());

	}
	
	
	
	
	
	/**
	 * 
	 * Retorna el contenido de un documento del Content Server y lo ubica en una
	 * ruta temporal del servidor
	 * 
	 * @param raizLog        cadena de caracteres que contiene información del id
	 *                       del flujo para identificar a que maquina pertenece el
	 *                       llamado
	 * @param soapAuthHeader contiene el token de autenticaci�n del servicio
	 * @param idDoc          Corresponde al id del documento en CS
	 * @param directoryPath  directorio donde se crean los documentos temporales,
	 *                       Default temp/
	 * @param workflow       Corresponden al id del workflow donde esta el documento
	 * @param wsdl           url de la wsdl del servicio web de CS
	 * @return docDataHandler, DataHandler con el contenido del documento solicitado
	 * @throws SOAPException Si el CS arroja un error en un servicio web
	 * @throws MalformedURLException 
	 * @throws IOException   Si se presenta un error de escritura o lectura en la
	 *                       ruta temporal
	 */
	public static Attachment obtenerDocumentoCS(String raizLog, Header soapAuthHeader, long idDoc, String wsdl) throws SOAPException, MalformedURLException {

		// VALIDACIONES INICIALES
		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no puede ser nulo");
		}

		if (idDoc < 1) {
			throw new IllegalArgumentException("El parametro idDoc [" + idDoc + "] no es valido");
		}

		// DECLARACIÓN DE VARIABLES PARA DESCARGAR EL DOCUMENTO 
		Attachment attachment = new Attachment();

		// DECLARACIÓN DE VARIABLES PARA REALIZAR LLAMADO AL SERVICIO WEB
		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);
		DocumentManagement_Service documentService = null;

		// CREACIÓN DEL CLIENTE DEL SERVICIO WEB
		try {
			documentService = new DocumentManagement_Service(newURLWSDL, qName);
		} catch (WebServiceException e) {
			throw new SOAPException("Se presento un error en el DocumentManagement_Service en la URL: " + newURLWSDL+ " - " + e.getMessage(), e);
		}

		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();
		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);

		// LLAMADO AL SERVICIO WEB PARA OBTENER EL DOC DESDE CS
		try {
			ParametrosServicios servicioParams = new ParametrosServicios().withIdDoc(idDoc).withVersion(0L);
			attachment = (Attachment) LlamadosWS.llamarServicio(client, servicioParams,
					NombreServicio.GET_VERSIONS_CONTENTS);			

		} catch (ServerSOAPFaultException e) {
			throw new SOAPException("Error al llamar al servicio getVersionContents de Content Server- ORIGINAL: " + e.getMessage(), e);
		}

		return attachment;

	}
	
	
	/**
	 * 
	 * Ubica un documento previamente descargado de CS en el servidor
	 * 
	 * @param raizLog        cadena de caracteres que contiene información del id
	 *                       del flujo para identificar a que maquina pertenece el
	 *                       llamado
	 * @param attachment documento previamente descargado de CS
	 * @param directoryPath  directorio donde se crean los documentos temporales,
	 *                       Default temp/
	 * @param workflow       Corresponden al id del workflow donde esta el documento
	 * @param idDoc          Corresponde al id del documento en CS
	 * @throws IOException   Si se presenta un error de escritura o lectura en la
	 *                       ruta temporal
	 */
	public static DataHandler descargarDocumento(String raizLog, Attachment attachment,String directoryPath,String workflow, long idDoc) throws IOException  {
		// VALIDACIONES INICIALES
		if (directoryPath == null) {
			throw new NullPointerException("El parametro directoryPath no puede ser nulo");
		}

		if (directoryPath.isEmpty()) {
			throw new IllegalArgumentException("El parametro directoryPath no puede estar vacio");
		}
				
		// DECLARACIÓN DE VARIABLES PARA CREAR EL DOCUMENTO EN EL SERVIDOR
		byte[] documentoBytes = attachment.getContents();;
		DataHandler docDataHandler = null;
		
		// SE RENOMBRA EL ATTACHMENT
		System.out.println(raizLog+"Se va a renombrar el documento con nombreOriginal: ["+attachment.getFileName()+"]");		
		String extensionOriginal = org.apache.commons.lang3.StringUtils.substringAfterLast(attachment.getFileName(), ".");
		String nuevoNombreDoc = workflow+"_"+idDoc+"."+extensionOriginal;
		attachment.setFileName(nuevoNombreDoc);		
		System.out.println(raizLog+"Se renombró el documento con nuevoNombre: ["+attachment.getFileName()+"]");
		


		// SE CREA EL DOCMUENTO EN EL SERVIDOR
		System.out.println(raizLog+"Se va a descargar el contenido del documento en el servidor");
		FileDataSource fileDataSource = null;
		fileDataSource = new FileDataSource(directoryPath + "/" + attachment.getFileName());
		System.out.println(raizLog+"Se creó correctamente el archivo en la ruta: ["+directoryPath + "/" + attachment.getFileName()+"]");

		// SE COPIA EL CONTENIDO DEL DOC DESCARGADO DE CS AL DOC CREADO EN EL SERVIDOR
		try {
			FileUtils.writeByteArrayToFile(fileDataSource.getFile(), documentoBytes);
			System.out.println(raizLog+"Se copió correctamente el contenido del archivo de CS en el archivo del servidor");
		} catch (IOException e) {
			throw new IOException("Error al pasar del byte a File", e);
		}
		
		docDataHandler = new DataHandler(fileDataSource);

		return docDataHandler;

	}
	
	
	
	
	
	
	
	
	
	

	/**
	 * Permite actualizar el contenido y/o los metadatos de un documento de
	 * Content Server
	 * 
	 * @param soapAuthHeader
	 *            que contiene el token de autenticaci�n del servicio
	 * @param idDoc
	 *            id del documento de Content Server
	 * @param documento
	 *            contenido del documento
	 * @param [idCategoria]
	 *            id de la categoria de Content Server
	 * @param [metadatos]
	 *            map de metatados
	 * @param [nombreSubGrupo]
	 *            nombre del subgrupo de metadatos
	 * @param [subGrupoMetadatos]
	 *            map de metadatos del subgrupo
	 * @throws NoSuchElementException
	 *             cuando no se encuentra algun elemento de la categoria
	 * @throws IOException
	 *             cuando hay errores cuando se construye el attachment
	 * @throws DateTimeException
	 *             cuando hay errores al setear la fecha del attachment
	 * @throws SOAPException
	 *             cuando hay alg�n error en el llamado de los Servicios de
	 *             Content Server
	 */
	public static void actualizarVersionMetadatosDocumento(Header soapAuthHeader, long idDoc, String nombreDoc,
			DataHandler documento, long idCategoria, String nombreCategoria, Map<String, Object> metadatos,
			String nombreSubGrupo, List<Map<String, Object>> subGrupoMetadatos, boolean validarCategoria, String wsdl)
			throws NoSuchElementException, IOException, DateTimeException, SOAPException, InterruptedException{

		if (idDoc <= 0) {
			throw new IllegalArgumentException("Id Documento (" + idDoc + ") no validos");
		}

		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no debe ser nulo");
		}

		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		DocumentManagement_Service documentService = new DocumentManagement_Service(newURLWSDL, qName);
		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();

		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);

		Metadata metadata = null;

		if (nombreCategoria != null) {
			if (!nombreCategoria.isEmpty()) {

				if (!validarCategoria) {
					/*if (!(client.getNode(idDoc).getMetadata().getAttributeGroups().stream()
							.filter(unAttributeGroup -> unAttributeGroup.getDisplayName().equals(nombreCategoria))
							.findFirst().isPresent())) {*/
					ParametrosServicios servicioParams = new ParametrosServicios().withIdNode(idDoc);
					Node nodo = (Node) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_NODE);
					//LlamadosWS.llamarGetNode(client, idDoc);
					if (!(nodo.getMetadata().getAttributeGroups().stream()
								.filter(unAttributeGroup -> unAttributeGroup.getDisplayName().equals(nombreCategoria))
								.findFirst().isPresent())) {	
						try {
							adicionarCategoria(idDoc, idCategoria, nombreCategoria, client);
						} catch (ServerSOAPFaultException e) {
							throw new SOAPException(
									"Error al llamar al servicio updateNode de Content Server - ORIGINAL: "
											+ e.getMessage(),
									e);
						}
					}
				}

				metadata = cargarMetadatosCategoria(idDoc, idCategoria, nombreCategoria, metadatos, nombreSubGrupo,
						subGrupoMetadatos, client, validarCategoria);
			}
		}

		if (nombreDoc == null) {
			nombreDoc = "";
		}

		Attachment attachment = new Attachment();

		if (!nombreDoc.isEmpty()) {
			attachment = cargarDocumento(nombreDoc, documento);
			try {
				//client.addVersion(idDoc, metadata, attachment);
				//LlamadosWS.llamarAddVersion(client, idDoc, metadata, attachment);
				ParametrosServicios servicioParams = new ParametrosServicios().withIdDoc(idDoc).withMetadata(metadata).withAttachment(attachment);
				LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.ADD_VERSION);
				
			} catch (ServerSOAPFaultException e) {
				throw new SOAPException(
						"Error al llamar al servicio addVersion de Content Server - ORIGINAL: " + e.getMessage(), e);
			}
			
			try {
				//client.renameNode(idDoc, nombreDoc);
				//LlamadosWS.llamarRenameNode(client, idDoc, nombreDoc);
				ParametrosServicios servicioParams = new ParametrosServicios().withIdDoc(idDoc).withNombreDoc(nombreDoc);
				LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.RENAME_NODE);
			}catch (ServerSOAPFaultException e) {
				throw new SOAPException(
						"Error al llamar al servicio renameNode de Content Server - ORIGINAL: " + e.getMessage(), e);
			}	

		} else {
			try {
				//Node nodo = client.getNode(idDoc);
				//Node nodo = LlamadosWS.llamarGetNode(client, idDoc);
				ParametrosServicios servicioParams = new ParametrosServicios().withIdNode(idDoc);
				Node nodo = (Node) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_NODE);
				nodo.setMetadata(metadata);
				//client.updateNode(nodo);
				//LlamadosWS.llamarUpdateNode(client, nodo);
				ParametrosServicios servicioParams2 = new ParametrosServicios().withNode(nodo);
				LlamadosWS.llamarServicio(client, servicioParams2, NombreServicio.UPDATE_NODE);
			} catch (ServerSOAPFaultException e) {
				throw new SOAPException(
						"Error al llamar al servicio updateNode de Content Server - ORIGINAL: " + e.getMessage(), e);
			}
		}

	}



	public static void actualizarVersionMetadatosDocumento(Header soapAuthHeader, long idDoc, String nombreDoc,
			DataHandler documento, int idCategoria, String nombreCategoria, Map<String, Object> metadatos,
			boolean validarCategoria, String wsdl) throws DateTimeException, IOException, SOAPException, InterruptedException {

		actualizarVersionMetadatosDocumento(soapAuthHeader, idDoc, nombreDoc, documento, idCategoria, nombreCategoria,
				metadatos, "", null, validarCategoria, wsdl);
	}

	public static void actualizarVersionDoc(Header soapAuthHeader, long idDoc, String nombreDoc, DataHandler documento,
			boolean validarCategoria, String wsdl) throws DateTimeException, IOException, SOAPException, InterruptedException {

		actualizarVersionMetadatosDocumento(soapAuthHeader, idDoc, nombreDoc, documento, -1, null, null, null, null,
				validarCategoria, wsdl);
	}

	public static void actualizarMetadatosSubgrupo(Header soapAuthHeader, long idDoc, int idCategoria,
			String nombreCategoria, String nombreSubGrupo, List<Map<String, Object>> subGrupoMetadatos,
			boolean validarCategoria, String wsdl)
			throws NoSuchElementException, DateTimeException, IOException, SOAPException, InterruptedException {

		actualizarVersionMetadatosDocumento(soapAuthHeader, idDoc, "", null, idCategoria, nombreCategoria, null,
				nombreSubGrupo, subGrupoMetadatos, validarCategoria, wsdl);
	}

	/**
	 * Valida si hay algun dato en alguno de los campos de la primera fila
	 * 
	 * @param tabla
	 *            contiene los metadatos en forma de tabla
	 * @return true si ninguno de los campos de la primera fila contiene
	 *         informaci�n
	 */
	private static boolean isPrimeraFilaVacia(TableValue tabla) {
		RowValue primeraFila = tabla.getValues().get(0);

		// Se valida si los valores de la primera fila, que corresponden a
		// diferentes tipos de Content Server
		// estan vacios o contienen alguna informaci�n
		Iterator<DataValue> iter = primeraFila.getValues().iterator();
		while (iter.hasNext()) {
			DataValue unElemento = iter.next();

			if (unElemento instanceof StringValue) {
				StringValue unStringValue = (StringValue) unElemento;
				if (!unStringValue.getValues().isEmpty()) {
					return false;
				}

			}

			if (unElemento instanceof IntegerValue) {
				IntegerValue unIntegerValue = (IntegerValue) unElemento;
				if (!unIntegerValue.getValues().isEmpty()) {
					return false;
				}
			}

			if (unElemento instanceof DateValue) {
				DateValue unDateValue = (DateValue) unElemento;
				if (!unDateValue.getValues().isEmpty()) {
					return false;
				}
			}

			if (unElemento instanceof BooleanValue) {
				BooleanValue unBooleanValue = (BooleanValue) unElemento;
				if (!unBooleanValue.getValues().isEmpty()) {
					return false;
				}
			}

			if (unElemento instanceof RealValue) {
				RealValue unRealValue = (RealValue) unElemento;
				if (!unRealValue.getValues().isEmpty()) {
					return false;
				}
			}

		}

		return true;
	}
	
	/**
	 * permite crear un documento
	 * @param soapAuthHeader
	 * @param nombreDoc
	 * @param documento
	 * @param parentId
	 * @throws SOAPException
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public static void crearDocumento(Header soapAuthHeader, String nombreDoc, DataHandler documento, Long parentId)
			throws SOAPException, IOException, InterruptedException {
		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no debe ser nulo");
		}

		if (documento == null) {
			throw new NullPointerException("El parametro documento no debe ser nulo");
		}

		DocumentManagement_Service documentService = new DocumentManagement_Service();
		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();

		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);

		byte[] docByte;
		InputStream is = null;

		try {
			is = documento.getInputStream();
			docByte = IOUtils.toByteArray(is);
		} catch (IOException e) {
			throw new IOException("EL documento temporal no pudo ser leido", e);
		} finally {
			if (is != null) {
				safeClose(is);
			}
		}

		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		XMLGregorianCalendar XMLCalendar;

		try {
			XMLCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
		} catch (DatatypeConfigurationException e) {
			throw new DateTimeException("Error al inicializar un objeto de tipo XMLGregorianCalendar", e);
		}

		Attachment attachment = new Attachment();

		attachment.setContents(docByte);
		attachment.setFileName(nombreDoc);
		attachment.setFileSize(docByte.length);
		attachment.setCreatedDate(XMLCalendar);
		attachment.setModifiedDate(XMLCalendar);

		try {
			//client.createDocument(parentId, nombreDoc, null, false, null, attachment);
			//LlamadosWS.llamarCreateDocument(client, parentId, nombreDoc, null, false, null, attachment);
			ParametrosServicios servicioParams = new ParametrosServicios().withIdParent(parentId).withNombreDoc(nombreDoc).withComment(null).withVersionControl(false).withMetadata(null).withAttachment(attachment);
			LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.CREATE_DOCUMENT);
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException("Error al llamar al servicio createDocument de Content Server", e);
		}

	}

	public static Long crearDocumento(Header soapAuthHeader, String nombreDoc, DataHandler documento, Long parentId,
			String wsdl) throws SOAPException, IOException, InterruptedException {
		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no debe ser nulo");
		}

		if (documento == null) {
			throw new NullPointerException("El parametro documento no debe ser nulo");
		}

		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		DocumentManagement_Service documentService = new DocumentManagement_Service(newURLWSDL, qName);
		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();

		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);

		byte[] docByte;
		InputStream is = null;
		Long idNodo = new Long(0);

		try {
			is = documento.getInputStream();
			docByte = IOUtils.toByteArray(is);
		} catch (IOException e) {
			throw new IOException("EL documento temporal no pudo ser leido", e);
		} finally {
			if (is != null) {
				safeClose(is);
			}
		}

		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		XMLGregorianCalendar XMLCalendar;

		try {
			XMLCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
		} catch (DatatypeConfigurationException e) {
			throw new DateTimeException("Error al inicializar un objeto de tipo XMLGregorianCalendar", e);
		}

		Attachment attachment = new Attachment();

		attachment.setContents(docByte);
		attachment.setFileName(nombreDoc);
		attachment.setFileSize(docByte.length);
		attachment.setCreatedDate(XMLCalendar);
		attachment.setModifiedDate(XMLCalendar);

		try {
			//Node nodo = client.createDocument(parentId, nombreDoc, null, false, null, attachment);
						
			//Node nodo = LlamadosWS.llamarCreateDocument(client, parentId, nombreDoc, null, false, null, attachment);
			ParametrosServicios servicioParams = new ParametrosServicios().withIdParent(parentId).withNombreDoc(nombreDoc).withComment(null).withVersionControl(false).withMetadata(null).withAttachment(attachment);
			Node nodo = (Node) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.CREATE_DOCUMENT);
			
			idNodo = nodo.getID();
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException("Error al llamar al servicio createDocument de Content Server", e);
		}

		return idNodo;

	}

	public static Long crearDocumentoCatDocumento(Header soapAuthHeader, Long idDoc, String nombreDoc,
			DataHandler documento, long idCategoria, String nombreCategoria, Map<String, Object> metadatos,
			boolean isCopiaCompulsada, Long parentIdDestino, String wsdl) throws SOAPException, IOException, InterruptedException {

		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no debe ser nulo");
		}

		if (documento == null) {
			throw new NullPointerException("El parametro documento no debe ser nulo");
		}

		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		DocumentManagement_Service documentService = new DocumentManagement_Service(newURLWSDL, qName);
		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();

		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);

		Long idDocCreado = null;

		AttributeGroup categoryTemplate = new AttributeGroup();

		Metadata metadata = new Metadata();

		try {
			//categoryTemplate = client.getCategoryTemplate(idCategoria);
			//categoryTemplate = LlamadosWS.llamarGetCategoryTemplate(client, idCategoria);
			ParametrosServicios servicioParams = new ParametrosServicios().withIdCategoria(idCategoria);
			categoryTemplate = (AttributeGroup) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_CATEGORY_TEMPLATE);
			
		} catch (ServerSOAPFaultException e) {
			e.printStackTrace();
			throw new SOAPException(
					"Error al llamar al servicio getCategoryTemplate de Content Server- ORIGINAL: " + e.getMessage(),
					e);
		}

		Iterator<Entry<String, Object>> iterMetadatos = metadatos.entrySet().iterator();
		while (iterMetadatos.hasNext()) {

			Entry<String, Object> unEntry = iterMetadatos.next();
			String unaEtiqueta = unEntry.getKey();

			try {
				DataValue unTAtributo = categoryTemplate.getValues().stream()
						.filter(unCampo -> unCampo.getDescription().equals(unaEtiqueta)).findFirst().get();

				if (unTAtributo instanceof StringValue) {
					StringValue unStringValue = (StringValue) unTAtributo;
					String unValor = (String) unEntry.getValue();
					unStringValue.getValues().clear();
					unStringValue.getValues().add(unValor);

				} else if (unTAtributo instanceof IntegerValue) {
					IntegerValue unIntegerValue = (IntegerValue) unTAtributo;
					Long unValor = (Long) unEntry.getValue();
					unIntegerValue.getValues().clear();
					unIntegerValue.getValues().add(unValor);

				} else if (unTAtributo instanceof DateValue) {
					DateValue unDateValue = (DateValue) unTAtributo;
					Date unFecha = (Date) unEntry.getValue();
					GregorianCalendar unCalendar = new GregorianCalendar();
					unCalendar.setTime(unFecha);
					XMLGregorianCalendar xmlCalendar;
					try {
						xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(unCalendar);
					} catch (DatatypeConfigurationException e) {
						throw new DateTimeException("Error al inicializar un objeto de tipo XMLGregorianCalendar", e);
					}
					unDateValue.getValues().clear();
					unDateValue.getValues().add(xmlCalendar);
				}
			} catch (DateTimeException | NoSuchElementException e) {
				throw new NoSuchElementException("No se pudo cargar le metadato (" + unaEtiqueta + ") en la categoria");
			}
		}

		metadata.getAttributeGroups().add(categoryTemplate);

		if (isCopiaCompulsada) {
			nombreDoc = PREFIJO_COPIA_COMPULSADA + nombreDoc;
		}

		byte[] docByte;
		InputStream is = null;

		try {
			is = documento.getInputStream();
			docByte = IOUtils.toByteArray(is);
		} catch (IOException e) {
			throw new IOException("EL documento temporal no pudo ser leido", e);
		} finally {
			if (is != null) {
				safeClose(is);
			}
		}

		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		XMLGregorianCalendar XMLCalendar;

		try {
			XMLCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
		} catch (DatatypeConfigurationException e) {
			throw new DateTimeException("Error al inicializar un objeto de tipo XMLGregorianCalendar", e);
		}

		Attachment attachment = new Attachment();

		attachment.setContents(docByte);
		attachment.setFileName(nombreDoc);
		attachment.setFileSize(docByte.length);
		attachment.setCreatedDate(XMLCalendar);
		attachment.setModifiedDate(XMLCalendar);

		// Se obtiene el dato parent id
		long parentId = -1;
		
		try {
			if (parentIdDestino == null) {
				//parentId = client.getNode(idDoc).getParentID();
				//parentId = LlamadosWS.llamarGetNode(client, idDoc).getParentID();
				ParametrosServicios servicioParams = new ParametrosServicios().withIdNode(idDoc);
				Node nodo = (Node) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_NODE);
				parentId = nodo.getParentID();
			} else {
				parentId = parentIdDestino;
			}

		} catch (ServerSOAPFaultException e) {
			throw new SOAPException("Error al llamar al servicio getNode de Content Server", e);
		}

		try {
			//Node nodo = client.createDocument(parentId, nombreDoc, null, false, metadata, attachment);
			//Node nodo = LlamadosWS.llamarCreateDocument(client, parentId, nombreDoc, null, false, metadata, attachment);
			ParametrosServicios servicioParams = new ParametrosServicios().withIdParent(parentId).withNombreDoc(nombreDoc).withComment(null).withVersionControl(false).withMetadata(metadata).withAttachment(attachment);
			Node nodo = (Node) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.CREATE_DOCUMENT);
			idDocCreado = nodo.getID();
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException(
					"Error al llamar al servicio createDocument de Content Server- ORIGINAL: " + e.getMessage(), e);
		}

		return idDocCreado;
	}

	/**
	 * Metodo para obteer el nombre del documento del contenet
	 * @param soapAuthHeader
	 * @param idDocumento
	 * @param wsdl
	 * @return
	 * @throws SOAPException
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public static String obtenerNombreDocumento(Header soapAuthHeader, long idDocumento, String wsdl)
			throws SOAPException, IOException, InterruptedException {

		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no puede ser nulo");
		}

		if (idDocumento < 1) {
			throw new IllegalArgumentException("El parametro idDoc (" + idDocumento + ") no es valido");
		}

		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		DocumentManagement_Service documentService = new DocumentManagement_Service(newURLWSDL, qName);
		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();

		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);

		String nombre = "";

		try {
			//nombre = client.getNode(idDocumento).getName();
			//nombre = LlamadosWS.llamarGetNode(client, idDocumento).getName();
			ParametrosServicios servicioParams = new ParametrosServicios().withIdNode(idDocumento);
			Node nodo = (Node) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_NODE);
			nombre = nodo.getName();

		} catch (ServerSOAPFaultException e) {
			throw new SOAPException(
					"Error al llamar al servicio getVersionContents de Content Server- ORIGINAL: " + e.getMessage(), e);
		}

		return nombre;
	}
	

	/**
	 * Metodo para obtener el nombre de la ultima version del documento del contenet
	 * @param soapAuthHeader
	 * @param idDocumentoOriginal
	 * @param idDocumentoNuevo
	 * @param wsdl
	 * @throws SOAPException
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public static String obtenerNombreDocumentoVersion(Header soapAuthHeader, long idDocumento, String wsdl)
			throws SOAPException, IOException, InterruptedException {

		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no puede ser nulo");
		}

		if (idDocumento < 1) {
			throw new IllegalArgumentException("El parametro idDoc (" + idDocumento + ") no es valido");
		}

		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		DocumentManagement_Service documentService = new DocumentManagement_Service(newURLWSDL, qName);
		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();

		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);

		String nombre = "";

		try {
		    //NodeVersionInfo versionInfo = client.getNode(idDocumento).getVersionInfo();
			//NodeVersionInfo versionInfo = LlamadosWS.llamarGetNode(client, idDocumento).getVersionInfo();
			ParametrosServicios servicioParams = new ParametrosServicios().withIdNode(idDocumento);
			Node nodo = (Node) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_NODE);
			NodeVersionInfo versionInfo = nodo.getVersionInfo();
			
			List<Version> versiones = versionInfo.getVersions();
			nombre = versiones.get(versiones.size()-1).getFilename();
		  
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException(
					"Error al llamar al servicio getVersionContents de Content Server- ORIGINAL: " + e.getMessage(), e);
		}

		return nombre;
	}

	/**
	 * Metodo que permite obtener la fecha de creacion de un documento del content server
	 * @param soapAuthHeader
	 * @param idDocumento
	 * @param wsdl
	 * @return
	 * @throws SOAPException
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public static LocalDateTime obtenerFechaCreacionDocumento(Header soapAuthHeader, long idDocumento, String wsdl)
			throws SOAPException, IOException, InterruptedException {

		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no puede ser nulo");
		}

		if (idDocumento < 1) {
			throw new IllegalArgumentException("El parametro idDoc (" + idDocumento + ") no es valido");
		}

		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		DocumentManagement_Service documentService = new DocumentManagement_Service(newURLWSDL, qName);
		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();

		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);

		LocalDateTime fecha = LocalDateTime.now();

		try {
			//XMLGregorianCalendar date = client.getNode(idDocumento).getCreateDate();
			//XMLGregorianCalendar date = LlamadosWS.llamarGetNode(client, idDocumento).getCreateDate();
			ParametrosServicios servicioParams = new ParametrosServicios().withIdNode(idDocumento);
			Node nodo = (Node) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_NODE);
			XMLGregorianCalendar date = nodo.getCreateDate();
			fecha = LocalDateTime.of(date.getYear(), date.getMonth(), date.getDay(), date.getHour(), date.getMinute(), date.getSecond());
			
			
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException(
					"Error al llamar al servicio getVersionContents de Content Server- ORIGINAL: " + e.getMessage(), e);
		}

		return fecha;
	}
	
	

	public static void cargarNuevaVersionDocumentoExistente(Header soapAuthHeader, long idDocumentoOriginal,
			long idDocumentoNuevo, String wsdl) throws SOAPException, IOException, InterruptedException {

		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no puede ser nulo");
		}

		if (idDocumentoOriginal < 1) {
			throw new IllegalArgumentException(
					"El parametro idDocumentoOriginal (" + idDocumentoOriginal + ") no es valido");
		}

		if (idDocumentoNuevo < 1) {
			throw new IllegalArgumentException("El parametro idDocumentoNuevo (" + idDocumentoNuevo + ") no es valido");
		}

		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		DocumentManagement_Service documentService = new DocumentManagement_Service(newURLWSDL, qName);
		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();

		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);

		Attachment attachment = new Attachment();
		try {
			//attachment = client.getVersionContents(idDocumentoNuevo, 0);
			//attachment = LlamadosWS.llamarGetVersionContents(client, idDocumentoNuevo, 0L);
			ParametrosServicios servicioParams = new ParametrosServicios().withIdDoc(idDocumentoNuevo).withVersion(0L);
			attachment = (Attachment) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_VERSIONS_CONTENTS);
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException(
					"Error al llamar al servicio getVersionContents de Content Server- ORIGINAL: " + e.getMessage(), e);
		}

		cargarNuevaVersion(soapAuthHeader, idDocumentoOriginal, attachment, wsdl);
	}	
	
	
	public static void cargarNuevaVersionDataHandler(Header soapAuthHeader, long idDocumentoOriginal,
			DataHandler docHandler, String wsdl) throws SOAPException, IOException, InterruptedException {

		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no puede ser nulo");
		}

		if (idDocumentoOriginal < 1) {
			throw new IllegalArgumentException(
					"El parametro idDocumentoOriginal (" + idDocumentoOriginal + ") no es valido");
		}

		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		DocumentManagement_Service documentService = new DocumentManagement_Service(newURLWSDL, qName);
		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();

		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);

		Attachment attachment = null;

		byte[] docByte;
		InputStream is = null;

		try {
			is = docHandler.getInputStream();
			docByte = IOUtils.toByteArray(is);
		} catch (IOException e) {
			throw new IOException("EL documento temporal no pudo ser leido", e);
		} finally {
			if (is != null) {
				safeClose(is);
			}
		}

		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		XMLGregorianCalendar XMLCalendar;

		try {
			XMLCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
		} catch (DatatypeConfigurationException e) {
			throw new DateTimeException("Error al inicializar un objeto de tipo XMLGregorianCalendar", e);
		}

		attachment = new Attachment();

		attachment.setContents(docByte);
		attachment.setFileName(docHandler.getName());
		attachment.setFileSize(docByte.length);
		attachment.setCreatedDate(XMLCalendar);
		attachment.setModifiedDate(XMLCalendar);

		cargarNuevaVersion(soapAuthHeader, idDocumentoOriginal, attachment, wsdl);
	}

	public static void cargarNuevaVersion(Header soapAuthHeader, long idDocumentoOriginal, Attachment attachment,
			String wsdl) throws SOAPException, IOException, InterruptedException {

		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no puede ser nulo");
		}

		if (idDocumentoOriginal < 1) {
			throw new IllegalArgumentException(
					"El parametro idDocumentoOriginal (" + idDocumentoOriginal + ") no es valido");
		}

		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		DocumentManagement_Service documentService = new DocumentManagement_Service(newURLWSDL, qName);
		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();

		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);

		try {
			//client.addVersion(idDocumentoOriginal, null, attachment);
			//LlamadosWS.llamarAddVersion(client, idDocumentoOriginal, null, attachment);
			ParametrosServicios servicioParams = new ParametrosServicios().withIdDoc(idDocumentoOriginal).withMetadata(null).withAttachment(attachment);
			LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.ADD_VERSION);
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException(
					"Error al llamar al servicio addVersion de Content Server- ORIGINAL: " + e.getMessage(), e);
		}

	}

	private static Metadata cargarMetadatosCategoria(long idDoc, long idCategoria, String nombreCategoria,
			Map<String, Object> metadatos, String nombreSubGrupo, List<Map<String, Object>> subGrupoMetadatos,
			DocumentManagement client, boolean validarCategoria) throws SOAPException, InterruptedException {

		Metadata metadata = new Metadata();

		AttributeGroup categoryTemplate = new AttributeGroup();
	//	Node documentoNode = null;
		Node documentoNode = new Node();

		try {
			//documentoNode = client.getNode(idDoc);
			//documentoNode = LlamadosWS.llamarGetNode(client, idDoc);
			ParametrosServicios servicioParams = new ParametrosServicios().withIdNode(idDoc);
			documentoNode = (Node) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_NODE);
			
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException(
					"Error al llamar el servicio getNode de Content Server- ORIGINAL: " + e.getMessage(), e);
		}
		
		/*
		try {
			//categoryTemplate = client.getCategoryTemplate(idCategoria);
			//categoryTemplate = LlamadosWS.llamarGetCategoryTemplate(client, idCategoria);
			ParametrosServicios servicioParams = new ParametrosServicios().withIdCategoria(idCategoria);
			categoryTemplate = (AttributeGroup) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_CATEGORY_TEMPLATE);
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException(
					"Error al llamar al servicio getCategoryTemplate de Content Server- ORIGINAL: " + e.getMessage(),
					e);
		}
		*/

		try {
			metadata = documentoNode.getMetadata();
		} catch (NullPointerException e) {
			throw new NullPointerException("El documento [" + idDoc + "] no tiene metadata");
		}

		// Busca el AttributeGroup que corresponde a la categoria
		// DocumentoGeneral
		AttributeGroup categoria = null;

		try {
			categoria = metadata.getAttributeGroups().stream()
					.filter(unaCategoria -> unaCategoria.getDisplayName().equals(nombreCategoria)).findFirst().get();
		} catch (NoSuchElementException e) {
			throw new NoSuchElementException(
					"El documento id doc = (" + idDoc + ") no tiene la categoria (" + nombreCategoria + ")");
		}

		// Bloque actualizazion Metadatos
		// Se verifica que hallan sido enviados como parametro una lista de
		// metadatos para ingresar al bloque de actualización
		if (metadatos != null) {
			if (!metadatos.isEmpty()) {
				Iterator<Entry<String, Object>> iter = metadatos.entrySet().iterator();
				while (iter.hasNext()) {
					Entry<String, Object> unEntry = iter.next();
					String nombreCampoCs = unEntry.getKey();
					Object valorCampoCs = unEntry.getValue();
					DataValue unAtributo = null;
					try {

						unAtributo = categoria.getValues().stream()
								.filter(unCampo -> unCampo.getDescription().equals(nombreCampoCs)).findFirst().get();

						if (unAtributo instanceof StringValue) {
							StringValue unStringValue = (StringValue) unAtributo;
							String unValor = (String) valorCampoCs;
							unStringValue.getValues().clear();
							unStringValue.getValues().add(unValor);

						} else if (unAtributo instanceof IntegerValue) {
							IntegerValue unIntegerValue = (IntegerValue) unAtributo;
							Long unValor = (Long) valorCampoCs;
							unIntegerValue.getValues().clear();
							unIntegerValue.getValues().add(unValor);

						} else if (unAtributo instanceof DateValue) {
							DateValue unDateValue = (DateValue) unAtributo;
							Date unFecha = (Date) valorCampoCs;
							GregorianCalendar unCalendar = new GregorianCalendar();
							unCalendar.setTime(unFecha);
							XMLGregorianCalendar xmlCalendar;
							try {
								xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(unCalendar);
							} catch (DatatypeConfigurationException e) {
								throw new DateTimeException(
										"Error al inicializar un objeto de tipo XMLGregorianCalendar", e);
							}
							unDateValue.getValues().clear();
							unDateValue.getValues().add(xmlCalendar);
						}

					} catch (Exception e) {
						throw new NoSuchElementException(
								"No se puedo agregar el valor al campo: " + nombreCampoCs.toString());
					}

				}
			}
		}

		// Bloque para procesar metadatos de un subgrupo
		if (!StringUtils.isBlank(nombreSubGrupo)) {
			//Variable booleana para determinar si se esta insertando el firmante en la primer iteracion
			boolean isPrimeraIteracion = true;
			
			// Busca la tabla de datos que corresponde a los metadatos Firma
			// Integridad
			TableValue tablaMetadatos = null;
			
			try {
				tablaMetadatos = (TableValue) categoria.getValues().stream()
						.filter(unCampo -> unCampo.getDescription().equals(nombreSubGrupo)).findFirst().get();
			} catch (NoSuchElementException e) {
				throw new NoSuchElementException("La categoria (" + nombreCategoria
						+ ") no contiene el grupo de atributos (" + nombreSubGrupo + ")");
			}		
			
		

			for (Map<String, Object> fila : subGrupoMetadatos) {

				//System.out.println("Paso metadatos");	
				
				try {
					//categoryTemplate = client.getCategoryTemplate(idCategoria);
					//categoryTemplate = LlamadosWS.llamarGetCategoryTemplate(client, idCategoria);
					ParametrosServicios servicioParams = new ParametrosServicios().withIdCategoria(idCategoria);
					categoryTemplate = (AttributeGroup) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_CATEGORY_TEMPLATE);
				} catch (ServerSOAPFaultException e) {
					throw new SOAPException(
							"Error al llamar al servicio getCategoryTemplate de Content Server- ORIGINAL: " + e.getMessage(),
							e);
				}
				
				// Se obtiene del template de la categoria los datos iniciales
				// de la
				// tabla de datos Firma Integridad
				TableValue templateTablaMetadatos = (TableValue) categoryTemplate.getValues().stream()
						.filter(unCampo -> unCampo.getDescription().equals(nombreSubGrupo)).findFirst().get();

				RowValue templateUnaFila = templateTablaMetadatos.getValues().get(0);

				// Map<String, Object> unaFilaMetadatos = subGrupoMetadatos.get(0);

				// Iterator<Entry<String, Object>> iterMetadatos =
				// filaMetadatos.entrySet().iterator();
				DataValue tUnCampo = null;

				// while (iterMetadatos.hasNext()) {
				Set<Entry<String, Object>> metadatosFila = fila.entrySet();

				for (Entry<String, Object> unEntry : metadatosFila) {

					// Entry<String, Object> unEntry = iterMetadatos.next();

					String etiqueta = unEntry.getKey();
					String valor = unEntry.getValue().toString();	

					//System.out.println("paso fila - etiqueta[" + etiqueta + "] - valor [" + valor + "]");

					Optional<DataValue> tUnCampoOpt = templateUnaFila.getValues().stream()
							.filter(unCampo -> unCampo.getDescription().equals(etiqueta)).findFirst();

					if (tUnCampoOpt.isPresent()) {
						tUnCampo = tUnCampoOpt.get();

						if (tUnCampo instanceof StringValue) {
							StringValue unStringValue = (StringValue) tUnCampo;
							unStringValue.getValues().add(valor);
							templateUnaFila.getValues().add(unStringValue);
						}
					} else {
						throw new NoSuchElementException("Error elemento");
					}
				}
				// }

				// En el siguiente bloque se agrega una nueva fila dentro de la
				// tabla real que corresponde a la categoria asociada al
				// documento

				/* Se comenta Bloque ya que porta-Firmas no estaba retornando la data esperada al momento de realizar las pruebas en Desarrollo
				// Dado que la categoria siempre se crea con una fila vacia se
				// procede a borrar la primera fila y luego a agregar el dato
				if (tablaMetadatos.getValues().size() == 1) {
					if (isPrimeraFilaVacia(tablaMetadatos)) {
						System.out.println("Fila Vacia");
						tablaMetadatos.getValues().clear();
						tablaMetadatos.getValues().add(0, templateUnaFila);
					} else {
						System.out.println("La primera fila no esta vacia");
						tablaMetadatos.getValues().add(templateUnaFila);
					}
				} else {
					System.out.println("De la primera fila en adelante");
					tablaMetadatos.getValues().add(templateUnaFila);
				}
				*/
				
				//Vamaya Bloque: añadido para manejar la datas obtenida de todos los formantes en PF
				//Siempre se limpia la data en la primera iteración
				if(isPrimeraIteracion) {
					//System.out.println("Se borran los datos de la tabla");
					tablaMetadatos.getValues().clear();
					tablaMetadatos.getValues().add(templateUnaFila);
					isPrimeraIteracion = false;
				}else {
					//System.out.println("No se borran los datos, se añade fila");
					tablaMetadatos.getValues().add(templateUnaFila);
				}
				
				
			}
		}
		// FIN bloque metadatos Subgrupo

		return metadata;

	}

	private static Attachment cargarDocumento(String nombreDoc, DataHandler documento) throws IOException {

		Attachment attachment = new Attachment();
		// Bloque documento
		byte[] docByte;
		InputStream is = null;
		try {
			is = documento.getInputStream();
			docByte = IOUtils.toByteArray(is);
		} catch (IOException e) {
			throw new IOException("EL documento temporal no pudo ser leido", e);
		} finally {
			if (is != null) {
				safeClose(is);
			}
		}

		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		XMLGregorianCalendar XMLCalendar;

		try {
			XMLCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
		} catch (DatatypeConfigurationException e) {
			throw new DateTimeException("Error al inicializar un objeto de tipo XMLGregorianCalendar", e);
		}

		attachment.setContents(docByte);
		attachment.setFileName(nombreDoc);
		attachment.setFileSize(docByte.length);
		attachment.setCreatedDate(XMLCalendar);
		attachment.setModifiedDate(XMLCalendar);

		return attachment;
	}

	private static void adicionarCategoria(long idDoc, long idCategoria, String nombreCategoria,
			DocumentManagement client) throws ServerSOAPFaultException, InterruptedException {
		//Node documento = client.getNode(idDoc);
		//Node documento = LlamadosWS.llamarGetNode(client, idDoc);
		ParametrosServicios servicioParams = new ParametrosServicios().withIdNode(idDoc);
		Node documento = (Node) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_NODE);

		//AttributeGroup templateCategoria = client.getCategoryTemplate(idCategoria);
		//AttributeGroup templateCategoria = LlamadosWS.llamarGetCategoryTemplate(client, idCategoria);
		ParametrosServicios servicioParams2 = new ParametrosServicios().withIdCategoria(idCategoria);
		AttributeGroup templateCategoria = (AttributeGroup) LlamadosWS.llamarServicio(client, servicioParams2, NombreServicio.GET_CATEGORY_TEMPLATE);
		Metadata metadata = documento.getMetadata();
		metadata.getAttributeGroups().add(templateCategoria);
		documento.setMetadata(metadata);
		//client.updateNode(documento);
		//LlamadosWS.llamarUpdateNode(client, documento);
		ParametrosServicios servicioParams3 = new ParametrosServicios().withNode(documento);
		LlamadosWS.llamarServicio(client, servicioParams3, NombreServicio.UPDATE_NODE);
	}

	private static void safeClose(InputStream is) throws IOException {
		if (is != null) {
			is.close();
		}
	}

	// TODO desde aqui CAMBIOS JOHAN
	public static void adicionarCategoriaMetadata(Header soapAuthHeader, long idDoc, int idCategoria,
			String nombreCategoria, Map<String, Object> metadatos, String nombreSubGrupo,
			Map<String, Object> subGrupoMetadatos, boolean isFirst, String wsdl)
			throws ServerSOAPFaultException, IOException, Exception {

		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no debe ser nulo");
		}
		if (wsdl == null) {
			throw new NullPointerException("El parametro wsdl no debe ser nulo");
		}
		if (idDoc <= 0) {
			throw new IllegalArgumentException("El paramtro idDoc no debe ser menor que cero");
		}

		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		DocumentManagement_Service documentService = new DocumentManagement_Service(newURLWSDL, qName);
		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();
		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);
		
		Node documento = new Node();

		try {
			//documento = client.getNode(idDoc);
			//documento = LlamadosWS.llamarGetNode(client, idDoc);
			ParametrosServicios servicioParams = new ParametrosServicios().withIdNode(idDoc);
			documento = (Node) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_NODE);
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException(
					"Error al llamar al servicio getNode de Content Server- ORIGINAL: " + e.getMessage(), e);
		}
		
		AttributeGroup templateCategoria = new AttributeGroup();
		try {
			//templateCategoria = client.getCategoryTemplate(idCategoria);
			//templateCategoria = LlamadosWS.llamarGetCategoryTemplate(client, idCategoria);
			ParametrosServicios servicioParams = new ParametrosServicios().withIdCategoria(idCategoria);
			templateCategoria = (AttributeGroup) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_CATEGORY_TEMPLATE);
			
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException(
					"Error al llamar al servicio getCategoryTemplate de Content Server- ORIGINAL: " + e.getMessage(), e);
		}		

		Metadata metadata = documento.getMetadata();

		// Busca el AttributeGroup que corresponde a la categoria
		// DocumentoGeneral
		AttributeGroup categoria = null;
		Boolean tieneCategoria = false;
		try {
			categoria = metadata.getAttributeGroups().stream()
					.filter(unaCategoria -> unaCategoria.getDisplayName().equals(nombreCategoria)).findFirst().get();
			tieneCategoria = true;
		} catch (NoSuchElementException e) {
			categoria = templateCategoria;

		}

		// Bloque actualizazion Metadatos
		// Se verifica que hallan sido enviados como parametro una lista de
		// metadatos para ingresar al bloque de actualización
		if (metadatos != null) {
			if (!metadatos.isEmpty()) {
				Iterator<Entry<String, Object>> iter = metadatos.entrySet().iterator();
				while (iter.hasNext()) {
					Entry<String, Object> unEntry = iter.next();
					String nombreCampoCs = unEntry.getKey();
					Object valorCampoCs = unEntry.getValue();
					DataValue unAtributo = null;
					try {

						// unAtributo = categoria.getValues().stream()
						// .filter(unCampo ->
						// unCampo.getDescription().equals(nombreCampoCs)).findFirst()
						// .get();

						Optional<DataValue> unAtributoOpt = categoria.getValues().stream()
								.filter(unCampo -> unCampo.getDescription().equals(nombreCampoCs)).findFirst();

						if (unAtributoOpt.isPresent()) {
							unAtributo = unAtributoOpt.get();
						}

						if (unAtributo instanceof StringValue) {
							StringValue unStringValue = (StringValue) unAtributo;
							String unValor = (String) valorCampoCs;
							unStringValue.getValues().clear();
							unStringValue.getValues().add(unValor);

						} else if (unAtributo instanceof IntegerValue) {
							IntegerValue unIntegerValue = (IntegerValue) unAtributo;
							Long unValor = (Long) valorCampoCs;
							unIntegerValue.getValues().clear();
							unIntegerValue.getValues().add(unValor);

						} else if (unAtributo instanceof BooleanValue) {
							BooleanValue unBooleanValue = (BooleanValue) unAtributo;
							Boolean unValor = (Boolean) valorCampoCs;
							unBooleanValue.getValues().clear();
							unBooleanValue.getValues().add(unValor);

						} else if (unAtributo instanceof DateValue) {
							DateValue unDateValue = (DateValue) unAtributo;
							Date unFecha = (Date) valorCampoCs;
							GregorianCalendar unCalendar = new GregorianCalendar();
							unCalendar.setTime(unFecha);
							XMLGregorianCalendar xmlCalendar;
							try {
								xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(unCalendar);
							} catch (DatatypeConfigurationException e) {
								throw new DateTimeException(
										"Error al inicializar un objeto de tipo XMLGregorianCalendar", e);
							}
							unDateValue.getValues().clear();
							unDateValue.getValues().add(xmlCalendar);
						}

					} catch (NoSuchElementException e) {
						throw new NoSuchElementException("No se encontro el campo: " + nombreCampoCs.toString());
					} catch (Exception e) {
						throw new Exception("Error en el metodo adicionarCategoriaMetadata", e);
					}

				}
			}
		}

		// Bloque para procesar metadatos de un subgrupo
		if (!StringUtils.isBlank(nombreSubGrupo)) {

			// Busca la tabla de datos que corresponde a los metadatos Firma
			// Integridad
			TableValue campoTablaFirma = null;

			try {
				campoTablaFirma = (TableValue) categoria.getValues().stream()
						.filter(unCampo -> unCampo.getDescription().equals(nombreSubGrupo)).findFirst().get();
			} catch (NoSuchElementException e) {
				throw new NoSuchElementException("La categoria (" + nombreCategoria
						+ ") no contiene el grupo de atributos (" + nombreSubGrupo + ")");
			}

			// Se obtiene del template de la categoria los datos iniciales
			// de la
			// tabla de datos Firma Integridad
			TableValue tCampoTablaFirma = (TableValue) templateCategoria.getValues().stream()
					.filter(unCampo -> unCampo.getDescription().equals(nombreSubGrupo)).findFirst().get();

			RowValue tFilaUnoTablaFirma = tCampoTablaFirma.getValues().get(0);

			Iterator<Entry<String, Object>> iter = subGrupoMetadatos.entrySet().iterator();
			DataValue tUnCampo = null;

			try {
				while (iter.hasNext()) {
					Entry<String, Object> unEntry = iter.next();

					String etiqueta = unEntry.getKey();

					tUnCampo = tFilaUnoTablaFirma.getValues().stream()
							.filter(unCampo -> unCampo.getDescription().equals(etiqueta)).findFirst().get();

					if (tUnCampo instanceof StringValue) {
						StringValue unStringValue = (StringValue) tUnCampo;
						unStringValue.getValues().add(unEntry.getValue().toString());
						tFilaUnoTablaFirma.getValues().add(unStringValue);
					}

				}
			} catch (NoSuchElementException e) {
				throw new NoSuchElementException("Error elemento" + e);
			}

			// En el siguiente bloque se agrega una nueva fila dentro de la
			// tabla real que corresponde a la categoria asociada al
			// documento

			// Dado que la categoria siempre se crea con una fila vacia se
			// procede a borrar la primera fila y luego a agregar el dato
			if (campoTablaFirma.getValues().size() == 1) {
				if (isPrimeraFilaVacia(campoTablaFirma)) {					
					campoTablaFirma.getValues().clear();
					campoTablaFirma.getValues().add(0, tFilaUnoTablaFirma);
				} else {
					if (!isFirst)						
						campoTablaFirma.getValues().add(tFilaUnoTablaFirma);
				}
			} else {				
				campoTablaFirma.getValues().add(tFilaUnoTablaFirma);
			}
		}
		// FIN bloque metadatos Subgrupo

		if (!tieneCategoria) {
			metadata.getAttributeGroups().add(categoria);
		}

		documento.setMetadata(metadata);
		try {
			//client.updateNode(documento);
			//LlamadosWS.llamarUpdateNode(client, documento);
			ParametrosServicios servicioParams2 = new ParametrosServicios().withNode(documento);
			LlamadosWS.llamarServicio(client, servicioParams2, NombreServicio.UPDATE_NODE);
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException("Error llamando al servicio updateNode de CS - ORIGINAL: - " + e.getMessage(), e);
		}

	}

	public static Metadata adicionarCategoriaMetadataCopia(Header soapAuthHeader, long idDoc, int idCategoria,
			String nombreCategoria, Map<String, Object> metadatos, Map<String, List<Map<String, Object>>> mapSubgrupos,
			boolean isFirst, String wsdl) throws ServerSOAPFaultException, MalformedURLException, InterruptedException {

		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		DocumentManagement_Service service = new DocumentManagement_Service(newURLWSDL, qName);

		DocumentManagement client = service.getBasicHttpBindingDocumentManagement();
		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);
		
		//Node documento = client.getNode(idDoc);
		//Node documento = LlamadosWS.llamarGetNode(client, idDoc);
		ParametrosServicios servicioParams = new ParametrosServicios().withIdNode(idDoc);
		Node documento = (Node) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_NODE);

		//AttributeGroup templateCategoria = client.getCategoryTemplate(idCategoria);
		//AttributeGroup templateCategoria = LlamadosWS.llamarGetCategoryTemplate(client, idCategoria);
		ParametrosServicios servicioParams2 = new ParametrosServicios().withIdCategoria(idCategoria);
		AttributeGroup templateCategoria = (AttributeGroup) LlamadosWS.llamarServicio(client, servicioParams2, NombreServicio.GET_CATEGORY_TEMPLATE);

		Metadata metadata = documento.getMetadata();

		// Busca el AttributeGroup que corresponde a la categoria
		// DocumentoGeneral
		AttributeGroup categoria = null;
		Boolean tieneCategoria = false;
		try {
			categoria = metadata.getAttributeGroups().stream()
					.filter(unaCategoria -> unaCategoria.getDisplayName().equals(nombreCategoria)).findFirst().get();
			tieneCategoria = true;
		} catch (NoSuchElementException e) {
			categoria = templateCategoria;

		}

		// Bloque actualizazion Metadatos
		// Se verifica que hallan sido enviados como parametro una lista de
		// metadatos para ingresar al bloque de actualización
		if (metadatos != null) {
			if (!metadatos.isEmpty()) {
				Iterator<Entry<String, Object>> iter = metadatos.entrySet().iterator();
				while (iter.hasNext()) {
					Entry<String, Object> unEntry = iter.next();
					String nombreCampoCs = unEntry.getKey();
					Object valorCampoCs = unEntry.getValue();
					DataValue unAtributo = null;
					try {

						unAtributo = categoria.getValues().stream()
								.filter(unCampo -> unCampo.getDescription().equals(nombreCampoCs)).findFirst().get();

						if (unAtributo instanceof StringValue) {
							StringValue unStringValue = (StringValue) unAtributo;
							String unValor = (String) valorCampoCs;
							unStringValue.getValues().clear();
							unStringValue.getValues().add(unValor);

						} else if (unAtributo instanceof IntegerValue) {
							IntegerValue unIntegerValue = (IntegerValue) unAtributo;
							Long unValor = (Long) valorCampoCs;
							unIntegerValue.getValues().clear();
							unIntegerValue.getValues().add(unValor);

						} else if (unAtributo instanceof DateValue) {
							DateValue unDateValue = (DateValue) unAtributo;
							Date unFecha = (Date) valorCampoCs;
							GregorianCalendar unCalendar = new GregorianCalendar();
							unCalendar.setTime(unFecha);
							XMLGregorianCalendar xmlCalendar;
							try {
								xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(unCalendar);
							} catch (DatatypeConfigurationException e) {
								throw new DateTimeException(
										"Error al inicializar un objeto de tipo XMLGregorianCalendar", e);
							}
							unDateValue.getValues().clear();
							unDateValue.getValues().add(xmlCalendar);
						}

					} catch (Exception e) {
						throw new NoSuchElementException(
								"No se puedo agregar el valor al campo: " + nombreCampoCs.toString());
					}

				}
			}
		}

		// AQUIIIII Bloque para procesar metadatos de un subgrupo

		Iterator<Entry<String, List<Map<String, Object>>>> iterSubGrupos = mapSubgrupos.entrySet().iterator();

		Map<String, Object> subGrupoMetadatos;
		List<Map<String, Object>> listaSubGrupos;
		while (iterSubGrupos.hasNext()) {
			Entry<String, List<Map<String, Object>>> elemento = iterSubGrupos.next();

			String nombreSubGrupo;
			nombreSubGrupo = elemento.getKey();
			listaSubGrupos = elemento.getValue();
			//System.out.println("SUUUUUUBGRUPO: " + nombreSubGrupo);

			for (Map<String, Object> map : listaSubGrupos) {
				subGrupoMetadatos = map;

				if (!StringUtils.isBlank(nombreSubGrupo)) {

					// Busca la tabla de datos que corresponde a los metadatos
					// Firma
					// Integridad
					TableValue campoTablaFirma = null;

					try {
						campoTablaFirma = (TableValue) categoria.getValues().stream()
								.filter(unCampo -> unCampo.getDescription().equals(nombreSubGrupo)).findFirst().get();
					} catch (NoSuchElementException e) {
						throw new NoSuchElementException("La categoria (" + nombreCategoria
								+ ") no contiene el grupo de atributos (" + nombreSubGrupo + ")");
					}

					// Se obtiene del template de la categoria los datos
					// iniciales
					// de la
					// tabla de datos Firma Integridad
					TableValue tCampoTablaFirma = (TableValue) templateCategoria.getValues().stream()
							.filter(unCampo -> unCampo.getDescription().equals(nombreSubGrupo)).findFirst().get();

					RowValue tFilaUnoTablaFirma = tCampoTablaFirma.getValues().get(0);

					Iterator<Entry<String, Object>> iter = subGrupoMetadatos.entrySet().iterator();
					DataValue tUnCampo = null;

					try {
						while (iter.hasNext()) {
							Entry<String, Object> unEntry = iter.next();

							String etiqueta = unEntry.getKey();

							//System.out.println("LLAVE: " + etiqueta);

							tUnCampo = tFilaUnoTablaFirma.getValues().stream()
									.filter(unCampo -> unCampo.getDescription().equals(etiqueta)).findFirst().get();

							if (tUnCampo instanceof StringValue) {
								StringValue unStringValue = (StringValue) tUnCampo;
								unStringValue.getValues().add(unEntry.getValue().toString());
								tFilaUnoTablaFirma.getValues().add(unStringValue);
							}

						}
					} catch (NoSuchElementException e) {
						throw new NoSuchElementException("Error elemento" + e);
					}

					// En el siguiente bloque se agrega una nueva fila dentro de
					// la
					// tabla real que corresponde a la categoria asociada al
					// documento

					// Dado que la categoria siempre se crea con una fila vacia
					// se
					// procede a borrar la primera fila y luego a agregar el
					// dato
					if (campoTablaFirma.getValues().size() == 1) {
						if (isPrimeraFilaVacia(campoTablaFirma)) {
							campoTablaFirma.getValues().clear();
							campoTablaFirma.getValues().add(0, tFilaUnoTablaFirma);
						} else {
							if (!isFirst)
								campoTablaFirma.getValues().add(tFilaUnoTablaFirma);
						}
					} else {
						campoTablaFirma.getValues().add(tFilaUnoTablaFirma);
					}
				}

			}
		}

		// FIN bloque metadatos Subgrupo

		if (!tieneCategoria) {
			metadata.getAttributeGroups().add(categoria);
		}

		documento.setMetadata(metadata);
		//client.updateNode(documento);
		//LlamadosWS.llamarUpdateNode(client, documento);
		ParametrosServicios servicioParams3 = new ParametrosServicios().withNode(documento);
		LlamadosWS.llamarServicio(client, servicioParams3, NombreServicio.UPDATE_NODE);

		return metadata;
	}

	public static void crearCategoria(Header soapAuthHeader, long idDoc, Metadata metadata, int idCategoria,
			String nombreCategoria, String wsdl) throws IOException, InterruptedException {

		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		DocumentManagement_Service documentService = new DocumentManagement_Service(newURLWSDL, qName);
		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();
		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);
		//Node documento = client.getNode(idDoc);
		//Node documento = LlamadosWS.llamarGetNode(client, idDoc);
		ParametrosServicios servicioParams = new ParametrosServicios().withIdNode(idDoc);
		Node documento = (Node) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_NODE);

		//AttributeGroup templateCategoria = client.getCategoryTemplate(idCategoria);
		//AttributeGroup templateCategoria = LlamadosWS.llamarGetCategoryTemplate(client, idCategoria);
		ParametrosServicios servicioParams2 = new ParametrosServicios().withIdCategoria(idCategoria);
		AttributeGroup templateCategoria = (AttributeGroup) LlamadosWS.llamarServicio(client, servicioParams2, NombreServicio.GET_CATEGORY_TEMPLATE);

		AttributeGroup categoria = null;
		try {
			categoria = metadata.getAttributeGroups().stream()
					.filter(unaCategoria -> unaCategoria.getDisplayName().equals(nombreCategoria)).findFirst().get();
		} catch (NoSuchElementException e) {
			categoria = templateCategoria;

		}

		metadata.getAttributeGroups().add(categoria);
		documento.setMetadata(metadata);
		//client.updateNode(documento);
		//LlamadosWS.llamarUpdateNode(client, documento);
		ParametrosServicios servicioParams3 = new ParametrosServicios().withNode(documento);
		LlamadosWS.llamarServicio(client, servicioParams3, NombreServicio.UPDATE_NODE);
	}

	public static String obtenerAtributoCategoria(Header soapAuthHeader, long idDoc, int idCategoria,
			String nombreCategoria, String nombreCampo, String wsdl) throws ServerSOAPFaultException, IOException, InterruptedException, SOAPException {

		DataValue unAtributo = null;
		String valorAtributo = "";

		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		DocumentManagement_Service documentService = new DocumentManagement_Service(newURLWSDL, qName);

		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();
		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);
		//Node documento = client.getNode(idDoc);
		//Node documento = llamarGetNode(client, idDoc);
		
		Node documento = new Node();

		try {
			//documento = client.getNode(idDoc);
			//documento = LlamadosWS.llamarGetNode(client, idDoc);
			ParametrosServicios servicioParams = new ParametrosServicios().withIdNode(idDoc);
			documento = (Node) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_NODE);
			
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException(
					"Error llamando al servicio getNode de Content Server - ORIGINAL: " + e.getMessage(), e);
		}	

		Metadata metadata = documento.getMetadata();
		AttributeGroup categoria = null;
		try {
			categoria = metadata.getAttributeGroups().stream()
					.filter(unaCategoria -> unaCategoria.getDisplayName().equals(nombreCategoria)).findFirst().get();
		} catch (NoSuchElementException e) {
			throw new NoSuchElementException(
					"El documento id doc = (" + idDoc + ") no tiene la categoria (" + nombreCategoria + ")");
		}

		unAtributo = categoria.getValues().stream().filter(unCampo -> unCampo.getDescription().equals(nombreCampo))
				.findFirst().get();

		if (unAtributo instanceof StringValue) {
			StringValue unStringValue = (StringValue) unAtributo;
			valorAtributo = unStringValue.getValues().get(0);

		}

		return valorAtributo;
	}

	public static void cargarDocumentoCS(Header soapAuthHeader, long idDoc, String nombreDoc, String nuevoNombreDoc,
			String filepath, String wsdl) throws SOAPException, IOException, InterruptedException {

		if (filepath == null) {
			throw new NullPointerException("El parametro filepath no puede ser nulo");
		}
		if (wsdl == null) {
			throw new NullPointerException("El parametro wsdl no puede ser nulo");
		}
		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no puede ser nulo");
		}
		if (filepath.isEmpty()) {
			throw new IllegalArgumentException("El parametro filepath no puede estar vacio");
		}
		if (wsdl.isEmpty()) {
			throw new IllegalArgumentException("El parametro wsdl no puede estar vacio");
		}
		if (idDoc <= 0) {
			throw new IllegalArgumentException("El valor idDoc no peude ser menor que cero");
		}

		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		DocumentManagement_Service documentService = new DocumentManagement_Service(newURLWSDL, qName);
		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();
		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);

		FileDataSource fileDataSource = null;
		fileDataSource = new FileDataSource(filepath + nombreDoc);

		DataHandler documento = new DataHandler(fileDataSource);
		Metadata metadata = null;
		// metadata.getAttributeGroups().add(arg0);
		Attachment attachment = null;
		if (!nombreDoc.isEmpty()) {
			try {
				attachment = cargarDocumento(nuevoNombreDoc, documento);
			} catch (IOException e1) {
				throw new IOException(
						"No pudo leerse el documento [" + idDoc + " - " + nombreDoc + "] de la ruta [" + filepath + "]",
						e1);
			}
			try {
				//client.addVersion(idDoc, metadata, attachment);
				//LlamadosWS.llamarAddVersion(client, idDoc, metadata, attachment);
				ParametrosServicios servicioParams = new ParametrosServicios().withIdDoc(idDoc).withMetadata(metadata).withAttachment(attachment);
				LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.ADD_VERSION);
			} catch (ServerSOAPFaultException e) {
				throw new SOAPException(
						"Error al llamar al servicio addVersion de Content Server - ORIGINAL: " + e.getMessage(), e);
			}

		}

	}

	public static String actualizarAtributoCategoria(Header soapAuthHeader, long idDoc, int idCategoria,
			String nombreCategoria, String nombreCampo, String nuevoValorAtributo, String wsdl)
			throws IOException, SOAPException, InterruptedException {

		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no debe ser nulo");
		}
		if (nombreCampo == null) {
			throw new NullPointerException("El parametro nombreCampo  no debe ser nulo");
		}
		if (nuevoValorAtributo == null) {
			throw new NullPointerException("El parametro nuevoValorAtributo  no debe ser nulo");
		}
		if (wsdl == null) {
			throw new NullPointerException("El parametro wsdl  no debe ser nulo");
		}
		if (wsdl.isEmpty()) {
			throw new IllegalArgumentException("El  parametro wsdl  no debe estar vacio");
		}
		if (nombreCampo.isEmpty()) {
			throw new IllegalArgumentException("El  parametro nombreCopia  no debe estar vacio");
		}
		if (nuevoValorAtributo.isEmpty()) {
			throw new IllegalArgumentException("El  parametro nuevoValorAtributo  no debe estar vacio");
		}
		if (idDoc <= 0) {
			throw new IllegalArgumentException("El parametro idDoc [" + idDoc + "]  no no es valido");
		}

		DataValue unAtributo = null;
		String valorAtributo = "";

		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		DocumentManagement client;
		try {
			DocumentManagement_Service documentService = new DocumentManagement_Service(newURLWSDL, qName);
			client = documentService.getBasicHttpBindingDocumentManagement();
			((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);
		} catch (Exception e) {
			throw new SOAPException("Error iniciando el servicio con la wsdl (" + newURLWSDL + ")", e);
		}
		Node documento = new Node();

		try {
			//documento = client.getNode(idDoc);
			//documento = LlamadosWS.llamarGetNode(client, idDoc);
			ParametrosServicios servicioParams = new ParametrosServicios().withIdNode(idDoc);
			documento = (Node) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_NODE);
			
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException(
					"Error llamando al servicio getNode de Content Server - ORIGINAL: " + e.getMessage(), e);
		}

		Metadata metadata = documento.getMetadata();
		AttributeGroup categoria = null;
		try {
			categoria = metadata.getAttributeGroups().stream()
					.filter(unaCategoria -> unaCategoria.getDisplayName().equals(nombreCategoria)).findFirst().get();
		} catch (NoSuchElementException e) {
			throw new NoSuchElementException(
					"El documento id doc = (" + idDoc + ") no tiene la categoria (" + nombreCategoria + ")");
		}

		unAtributo = categoria.getValues().stream().filter(unCampo -> unCampo.getDescription().equals(nombreCampo))
				.findFirst().get();

		if (unAtributo instanceof StringValue) {
			StringValue unStringValue = (StringValue) unAtributo;
			unStringValue.getValues().clear();
			unStringValue.getValues().add(nuevoValorAtributo);
			valorAtributo = unStringValue.getValues().get(0);

		}

		documento.setMetadata(metadata);

		try {
			//client.updateNode(documento);
			//LlamadosWS.llamarUpdateNode(client, documento);
			ParametrosServicios servicioParams2 = new ParametrosServicios().withNode(documento);
			LlamadosWS.llamarServicio(client, servicioParams2, NombreServicio.UPDATE_NODE);
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException(
					"Error llamando al servicio updateNode de Content Server - ORIGINAL: " + e.getMessage(), e);
		}

		return valorAtributo;
	}

	public static String actualizarAtributoCategoriaListaDestino(Header soapAuthHeader, long idDoc, int idCategoria,
			String nombreCategoria, String nombreSubgrupo, String nombreCampo, String nombreValorAnterior,
			String nuevoValorAtributo, String wsdl) throws IOException, SOAPException, InterruptedException {

		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no debe ser nulo");
		}
		if (nombreCampo == null) {
			throw new NullPointerException("El parametro nombreCopia  no debe ser nulo");
		}
		if (nuevoValorAtributo == null) {
			throw new NullPointerException("El parametro nuevoValorAtributo  no debe ser nulo");
		}
		if (wsdl == null) {
			throw new NullPointerException("El parametro wsdl  no debe ser nulo");
		}
		if (wsdl.isEmpty()) {
			throw new IllegalArgumentException("El  parametro wsdl  no debe estar vacio");
		}
		if (nombreCampo.isEmpty()) {
			throw new IllegalArgumentException("El  parametro nombreCopia  no debe estar vacio");
		}
		if (nuevoValorAtributo.isEmpty()) {
			throw new IllegalArgumentException("El  parametro nuevoValorAtributo  no debe estar vacio");
		}
		if (idDoc <= 0) {
			throw new IllegalArgumentException("El parametro idDoc [" + idDoc + "]  no no es valido");
		}

		// DataValue unAtributo = null;
		String valorAtributo = "";

		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		DocumentManagement client;
		try {
			DocumentManagement_Service documentService = new DocumentManagement_Service(newURLWSDL, qName);
			client = documentService.getBasicHttpBindingDocumentManagement();
			((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);
		} catch (Exception e) {
			throw new SOAPException("Error iniciando el servicio con la wsdl (" + newURLWSDL + ")", e);
		}
		//Node documento = null;
		Node documento = new Node();

		try {
			//documento = client.getNode(idDoc);
			//documento = LlamadosWS.llamarGetNode(client, idDoc);
			ParametrosServicios servicioParams = new ParametrosServicios().withIdNode(idDoc);
			documento = (Node) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_NODE);
			
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException(
					"Error llamando al servicio getNode de Content Server - ORIGINAL: " + e.getMessage(), e);
		}

		Metadata metadata = documento.getMetadata();
		AttributeGroup categoria = null;
		try {
			categoria = metadata.getAttributeGroups().stream()
					.filter(unaCategoria -> unaCategoria.getDisplayName().equals(nombreCategoria)).findFirst().get();
		} catch (NoSuchElementException e) {
			throw new NoSuchElementException(
					"El documento id doc = (" + idDoc + ") no tiene la categoria (" + nombreCategoria + ")");
		}

		if (!StringUtils.isBlank(nombreSubgrupo)) {

			// Busca la tabla de datos que corresponde a los metadatos Firma
			// Integridad
			TableValue campoTablaFirma = null;

			try {
				campoTablaFirma = (TableValue) categoria.getValues().stream()
						.filter(unCampo -> unCampo.getDescription().equals(nombreSubgrupo)).findFirst().get();

				for (RowValue row : campoTablaFirma.getValues()) {

					for (DataValue unAtributo : row.getValues()) {

						if (unAtributo instanceof StringValue) {
							StringValue unStringValue = (StringValue) unAtributo;

							if (nombreCampo.equals(unStringValue.getValues().get(0))
									&& unStringValue.getDescription().equals(nombreValorAnterior)) {
								unStringValue.getValues().clear();
								unStringValue.getValues().add(nuevoValorAtributo);
								valorAtributo = unStringValue.getValues().get(0);
							}

						}

					}

				}

			} catch (NoSuchElementException e) {
				throw new NoSuchElementException("La categoria (" + nombreCategoria
						+ ") no contiene el grupo de atributos (" + nombreSubgrupo + ")");
			}

		}

		documento.setMetadata(metadata);

		try {
			//client.updateNode(documento);
			//LlamadosWS.llamarUpdateNode(client, documento);
			ParametrosServicios servicioParams2 = new ParametrosServicios().withNode(documento);
			LlamadosWS.llamarServicio(client, servicioParams2, NombreServicio.UPDATE_NODE);
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException(
					"Error llamando al servicio updateNode de Content Server - ORIGINAL: " + e.getMessage(), e);
		}

		return valorAtributo;
	}



	public static void cambiarNombreDocumento(Header soapAuthHeader, long idDoc, String nuevoNombreDocumento,
			String wsdl) throws SOAPException, IOException, InterruptedException {

		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no debe ser nulo");
		}
		if (nuevoNombreDocumento == null) {
			throw new NullPointerException("El parametro nombreCopia  no debe ser nulo");
		}
		if (wsdl == null) {
			throw new NullPointerException("El parametro wsdl  no debe ser nulo");
		}
		if (wsdl.isEmpty()) {
			throw new IllegalArgumentException("El  parametro wsdl  no debe estar vacio");
		}
		if (nuevoNombreDocumento.isEmpty()) {
			throw new IllegalArgumentException("El  parametro nombreCopia  no debe estar vacio");
		}
		if (idDoc <= 0) {
			throw new IllegalArgumentException("El parametro idDoc [" + idDoc + "]  no no es valido");
		}

		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		DocumentManagement_Service documentService = new DocumentManagement_Service(newURLWSDL, qName);
		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();
		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);
		
		//Node documento = client.getNode(idDoc);
		Node documento = new Node();
		try {
			//documento = LlamadosWS.llamarGetNode(client, idDoc);
			ParametrosServicios servicioParams = new ParametrosServicios().withIdNode(idDoc);
			documento = (Node) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_NODE);
			
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException(
					"Error llamando al servicio getNode de Content Server - ORIGINAL: " + e.getMessage(), e);
		}		
		documento.setName(nuevoNombreDocumento);
		try {
			//client.updateNode(documento);
			//LlamadosWS.llamarUpdateNode(client, documento);
			ParametrosServicios servicioParams2 = new ParametrosServicios().withNode(documento);
			LlamadosWS.llamarServicio(client, servicioParams2, NombreServicio.UPDATE_NODE);
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException(
					"Error llamando al servicio updateNode de Content Server - ORIGINAL: " + e.getMessage(), e);
		}
	}

	public static Node copiarDocumento(Header soapAuthHeader, long idDoc, String nombreCopia, String wsdl)
			throws SOAPException, IOException, InterruptedException {

		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no debe ser nulo");
		}
		if (nombreCopia == null) {
			throw new NullPointerException("El parametro nombreCopia  no debe ser nulo");
		}
		if (wsdl == null) {
			throw new NullPointerException("El parametro wsdl  no debe ser nulo");
		}
		if (wsdl.isEmpty()) {
			throw new IllegalArgumentException("El  parametro wsdl  no debe estar vacio");
		}
		if (nombreCopia.isEmpty()) {
			throw new IllegalArgumentException("El  parametro nombreCopia  no debe estar vacio");
		}
		if (idDoc <= 0) {
			throw new IllegalArgumentException("El parametro idDoc [" + idDoc + "]  no no es valido");
		}

		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		DocumentManagement_Service documentService = new DocumentManagement_Service(newURLWSDL, qName);
		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();
		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);

		Node documentoCopia = null;
		try {
			//documentoCopia = client.copyNode(idDoc, 4222056, nombreCopia, null);
			//documentoCopia = LlamadosWS.llamarCopyNode(client, idDoc, 4222056, nombreCopia, null);
			//TODO Hay un número quemado en el idParent, REVISAR
			ParametrosServicios servicioParams = new ParametrosServicios().withIdDoc(idDoc).withIdParent(4222056).withNuevoNombre(nombreCopia).withCopyOptions(null);
			documentoCopia = (Node) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.COPY_NODE);
			
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException(
					"Error llamando al servicio copyNode de Content Server - ORIGINAL: " + e.getMessage(), e);
		}

		return documentoCopia;
	}

	public static boolean MoverDocumento(Header soapAuthHeader, long idDoc, long parentId, String wsdl)
			throws SOAPException, IOException, InterruptedException {

		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no debe ser nulo");
		}
		if (parentId <= 0) {
			throw new NullPointerException("El parametro parentId no es valido");
		}
		if (wsdl == null) {
			throw new NullPointerException("El parametro wsdl  no debe ser nulo");
		}
		if (wsdl.isEmpty()) {
			throw new IllegalArgumentException("El  parametro wsdl  no debe estar vacio");
		}
		if (idDoc <= 0) {
			throw new IllegalArgumentException("El parametro idDoc [" + idDoc + "]  no no es valido");
		}

		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		DocumentManagement_Service documentService = new DocumentManagement_Service(newURLWSDL, qName);
		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();
		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);

		boolean result = false;
		try {

			MoveOptions moveOptions = new MoveOptions();
			moveOptions.setAttrSourceType(AttributeSourceType.DESTINATION);

			//client.moveNode(idDoc, parentId, "", moveOptions);
			//LlamadosWS.llamarMoveNode(client, idDoc, parentId, "", moveOptions);
			ParametrosServicios servicioParams2 = new ParametrosServicios().withIdDoc(idDoc).withIdParent(parentId).withNuevoNombre("").withMoveOptions(moveOptions);
			LlamadosWS.llamarServicio(client, servicioParams2, NombreServicio.MOVE_NODE);
			result = true;

		} catch (ServerSOAPFaultException e) {
			throw new SOAPException(
					"Error llamando al servicio moveNode de Content Server - ORIGINAL: " + e.getMessage(), e);
		}

		return result;
	}

	public static void actualizarMetadataDocumento(Header soapAuthHeader, long idDoc, Metadata metadata, String wsdl)
			throws SOAPException, IOException, InterruptedException {

		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no debe ser nulo");
		}
		if (metadata == null) {
			throw new NullPointerException("El parametro metadata  no debe ser nulo");
		}
		if (wsdl == null) {
			throw new NullPointerException("El parametro wsdl  no debe ser nulo");
		}
		if (wsdl.isEmpty()) {
			throw new IllegalArgumentException("El  parametro wsdl  no debe estar vacio");
		}
		if (idDoc <= 0) {
			throw new IllegalArgumentException("El parametro idDoc [" + idDoc + "]  no no es valido");
		}

		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		DocumentManagement_Service documentService = new DocumentManagement_Service(newURLWSDL, qName);
		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();
		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);
		Node documento = new Node();

		try {
			//documento = client.getNode(idDoc);
			//documento = LlamadosWS.llamarGetNode(client, idDoc);
			ParametrosServicios servicioParams = new ParametrosServicios().withIdNode(idDoc);
			documento = (Node) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_NODE);
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException(
					"Error llamando al servicio getNode de Content Server - ORIGINAL: " + e.getMessage(), e);
		}

		documento.setMetadata(metadata);

		try {
			//client.updateNode(documento);
			//LlamadosWS.llamarUpdateNode(client, documento);
			ParametrosServicios servicioParams2 = new ParametrosServicios().withNode(documento);
			LlamadosWS.llamarServicio(client, servicioParams2, NombreServicio.UPDATE_NODE);
			
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException(
					"Error llamando al servicio updateNode de Content Server - ORIGINAL: " + e.getMessage(), e);
		}

	}

	public static void eliminarDocumento(Header soapAuthHeader, long idDoc, String wsdl)
			throws SOAPException, IOException, InterruptedException {

		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no debe ser nulo");
		}
		if (wsdl == null) {
			throw new NullPointerException("El parametro wsdl  no debe ser nulo");
		}
		if (wsdl.isEmpty()) {
			throw new IllegalArgumentException("El  parametro wsdl  no debe estar vacio");
		}
		if (idDoc <= 0) {
			throw new IllegalArgumentException("El parametro idDoc [" + idDoc + "]  no no es valido");
		}

		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		DocumentManagement_Service documentService = new DocumentManagement_Service(newURLWSDL, qName);
		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();
		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);

		try {
			//client.deleteNode(idDoc);
			//LlamadosWS.llamarDeleteNode(client, idDoc);
			ParametrosServicios servicioParams = new ParametrosServicios().withIdNode(idDoc);
			LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.DELETE_NODE);
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException(
					"Error llamando al servicio deleteNode de Content Server - ORIGINAL: " + e.getMessage(), e);
		}

	}

	public static Node getDocumentoById(Header soapAuthHeader, long idDoc, String wsdl)
			throws SOAPException, IOException, InterruptedException {

		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no debe ser nulo");
		}
		if (wsdl == null) {
			throw new NullPointerException("El parametro wsdl  no debe ser nulo");
		}
		if (wsdl.isEmpty()) {
			throw new IllegalArgumentException("El  parametro wsdl  no debe estar vacio");
		}
		if (idDoc <= 0) {
			throw new IllegalArgumentException("El parametro idDoc [" + idDoc + "]  no no es valido");
		}

		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		DocumentManagement_Service documentService = new DocumentManagement_Service(newURLWSDL, qName);
		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();
		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);

		try {
			//return client.getNode(idDoc);
			ParametrosServicios servicioParams = new ParametrosServicios().withIdNode(idDoc);
			Node nodo = (Node) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_NODE);
			//return LlamadosWS.llamarGetNode(client, idDoc);
			return nodo;
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException(
					"Error llamando al servicio getNode de Content Server - ORIGINAL: " + e.getMessage(), e);
		}
	}

	public static void eliminarVersionesDocumento(Header soapAuthHeader, long idDoc, int numberToKeep, String wsdl)
			throws SOAPException, IOException, InterruptedException {

		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no debe ser nulo");
		}
		if (wsdl == null) {
			throw new NullPointerException("El parametro wsdl  no debe ser nulo");
		}
		if (wsdl.isEmpty()) {
			throw new IllegalArgumentException("El  parametro wsdl  no debe estar vacio");
		}
		if (idDoc <= 0) {
			throw new IllegalArgumentException("El parametro idDoc [" + idDoc + "]  no no es valido");
		}
		if (numberToKeep < 0) {
			throw new IllegalArgumentException("El parametro numberToKeep [" + numberToKeep + "]  no no es valido");
		}

		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		DocumentManagement_Service documentService = new DocumentManagement_Service(newURLWSDL, qName);
		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();
		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);

		try {
			//client.purgeVersions(idDoc, numberToKeep);
			//LlamadosWS.llamarPurgeVersions(client, idDoc, numberToKeep);
			ParametrosServicios servicioParams2 = new ParametrosServicios().withIdDoc(idDoc).withNumberToKeep(numberToKeep);
			LlamadosWS.llamarServicio(client, servicioParams2, NombreServicio.PURGE_VERSIONS);
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException(
					"Error llamando al servicio purgeVersions de Content Server - ORIGINAL: " + e.getMessage(), e);
		}

	}

	public static Long obtenerDocumentoPorNombre(Header soapAuthHeader, long idParent, String nombreDoc, String wsdl)
			throws SOAPException, IOException, InterruptedException {

		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no puede ser nulo");
		}
		if (idParent < 1) {
			throw new IllegalArgumentException("El parametro idParent (" + idParent + ") no es valido");
		}
		if (nombreDoc.isEmpty()) {
			throw new IllegalArgumentException("El parametro nombreDoc (" + nombreDoc + ") no es valido");
		}

		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		DocumentManagement_Service documentService = new DocumentManagement_Service(newURLWSDL, qName);
		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();

		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);

		Long idDoc = null;

		try {
			//Node nodo = client.getNodeByName(idParent, nombreDoc);
			//Node nodo = LlamadosWS.llamarGetNodeByName(client, idParent, nombreDoc);
			ParametrosServicios servicioParams = new ParametrosServicios().withIdParent(idParent).withNombreDoc(nombreDoc);
			Node nodo = (Node) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_NODE_BY_NAME);
			
			//idDoc = nodo.getID();
			
			if (nodo == null) {
				//throw new NullPointerException("El servicio web getNodeByName no se encontró para los parametros:idParent="+idParent+", nombreDoc="+nombreDoc);
				System.out.println("Nodo Nulo");
				return idDoc;
			}
			else {
				System.out.println("Nodo get ID");
				idDoc = nodo.getID();
			}
			
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException(
					"Error al llamar al servicio getVersionContents de Content Server- ORIGINAL: " + e.getMessage(), e);
		}

		return idDoc;
	}

	public static long copiarDocCatDocumento(Header soapAuthHeader, long idDocumento, long idParent, String nuevoNombre, String tipoCopiadoDocumento,
			String wsdl) throws SOAPException, IOException, InterruptedException {

		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no debe ser nulo");
		}
		if (nuevoNombre == null) {
			throw new NullPointerException("El parametro nuevoNombre no debe ser nulo");
		}
		if (wsdl == null) {
			throw new NullPointerException("El parametro wsdl no debe ser nulo");
		}
		if (nuevoNombre.isEmpty()) {
			throw new NullPointerException("El parametro nuevoNombre no debe estar vacio");
		}
		if (wsdl.isEmpty()) {
			throw new NullPointerException("El parametro wsdl no debe estar vacio");
		}
		if (idDocumento <= 0) {
			throw new IllegalArgumentException("Id Documento (" + idDocumento + ") no valido");
		}
		if (idParent <= 0) {
			throw new IllegalArgumentException("Id Parent (" + idParent + ") no valido");
		}

		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		DocumentManagement_Service documentService = new DocumentManagement_Service(newURLWSDL, qName);
		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();

		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);

		CopyOptions copyOptions = new CopyOptions();
		// copyOptions.setCopyCurrent(true);
		//EVALUAR ENTRADA
		if(tipoCopiadoDocumento.isEmpty() || tipoCopiadoDocumento.equals(null)) {
			copyOptions.setAttrSourceType(AttributeSourceType.DESTINATION);
		}else if(tipoCopiadoDocumento.equalsIgnoreCase(AttributeSourceType.DESTINATION.toString())) {
			copyOptions.setAttrSourceType(AttributeSourceType.DESTINATION);
		}else if(tipoCopiadoDocumento.equalsIgnoreCase(AttributeSourceType.ORIGINAL.toString())) {
			copyOptions.setAttrSourceType(AttributeSourceType.ORIGINAL);
		}else if(tipoCopiadoDocumento.equalsIgnoreCase(AttributeSourceType.MERGE.toString())){
			copyOptions.setAttrSourceType(AttributeSourceType.MERGE);
		}else {
			copyOptions.setAttrSourceType(AttributeSourceType.DESTINATION);
		}
		Node documentoCopiado = null;

		try {
			//documentoCopiado = client.copyNode(idDocumento, idParent, nuevoNombre, copyOptions);
			//documentoCopiado = LlamadosWS.llamarCopyNode(client, idDocumento, idParent, nuevoNombre, copyOptions);
			ParametrosServicios servicioParams = new ParametrosServicios().withIdDoc(idDocumento).withIdParent(idParent).withNuevoNombre(nuevoNombre).withCopyOptions(copyOptions);
			documentoCopiado = (Node) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.COPY_NODE);
			
		} catch (SOAPFaultException e) {
			throw new SOAPException(
					"Error con el llamado al Servicio copyNode de Content Server - ORIGINAL: " + e.getMessage(), e);
		}

		return documentoCopiado.getID();
	}

	// cambios stiven

	public static List<Node> getDocumentosByIds(Header soapAuthHeader, List<Long> listaIdDoc, String wsdl)
			throws SOAPException, IOException, InterruptedException {

		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no debe ser nulo");
		}
		if (wsdl == null) {
			throw new NullPointerException("El parametro wsdl  no debe ser nulo");
		}
		if (listaIdDoc == null) {
			throw new NullPointerException("El parametro listaIdDoc  no debe ser nulo");
		}
		if (wsdl.isEmpty()) {
			throw new IllegalArgumentException("El  parametro wsdl  no debe estar vacio");
		}
		if (listaIdDoc.isEmpty()) {
			throw new IllegalArgumentException("El parametro listaIdDoc  no debe estar vacio");
		}

		List<Node> listaNodos = new ArrayList<>();

		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		DocumentManagement_Service documentService = new DocumentManagement_Service(newURLWSDL, qName);
		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();
		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);

		for (Long idDoc : listaIdDoc) {

			Node unNode = new Node();
			try {
				//unNode = client.getNode(idDoc);
				//unNode = LlamadosWS.llamarGetNode(client, idDoc);
				ParametrosServicios servicioParams = new ParametrosServicios().withIdNode(idDoc);
				unNode = (Node) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_NODE);
			} catch (ServerSOAPFaultException e) {
				String mensaje = "No pudo obtenerse el nodo que corresponde al id [" + idDoc + "] - ";
				throw new SOAPException(mensaje
						+ "Error con el llamado al Servicio getNode de Content Server - ORIGINAL: " + e.getMessage(),
						e);
			}
			listaNodos.add(unNode);
		}

		return listaNodos;
	}

	/**
	 * Obtiene documentos de Content Server
	 * 
	 * @param soapAuthHeader
	 *            parametro de autenticacion de Content Server
	 * @param listaIdDoc
	 *            listado de ids de documentos de Content Server
	 * @param directoryPath
	 *            directorio temportal
	 * @param wsdl
	 *            wsdl del servicio de Content Server
	 * @return List DataHandler una lista de contenidos de documentos
	 * @throws SOAPException
	 *             Si se presenta un error con los servicios de Content Server
	 * @throws IOException
	 *             Si se presenta un error al leer el archivo de la ruta
	 *             temporal
	 * @throws InterruptedException 
	 */
	public static List<DataHandler> obtenerDocumentos(Header soapAuthHeader, List<Long> listaIdDoc,
			String directoryPath, String wsdl) throws SOAPException, IOException, InterruptedException {

		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no puede ser nulo");
		}
		if (listaIdDoc == null) {
			throw new NullPointerException("El parametro listaIdDoc no puede ser nulo");
		}
		if (directoryPath == null) {
			throw new NullPointerException("El parametro directoryPath no puede ser nulo");
		}
		if (wsdl == null) {
			throw new NullPointerException("El parametro wsdl no puede ser nulo");
		}
		if (listaIdDoc.isEmpty()) {
			throw new IllegalArgumentException("El parametro listaIdDoc no debe estar vacio");
		}
		if (directoryPath.isEmpty()) {
			throw new IllegalArgumentException("El parametro directoryPath no debe estar vacio");
		}
		if (wsdl.isEmpty()) {
			throw new IllegalArgumentException("El parametro wsdl no debe estar vacio");
		}

		List<DataHandler> listaDocDataHandler = new ArrayList<>();

		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		DocumentManagement_Service documentService = new DocumentManagement_Service(newURLWSDL, qName);
		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();

		// Lamado al servicio de Obtener Contenido del Content Server
		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);

		for (Long idDoc : listaIdDoc) {

			byte[] documentoBytes = null;
			Attachment attachment = new Attachment();

			try {
				//attachment = client.getVersionContents(idDoc, 0);
				//attachment = LlamadosWS.llamarGetVersionContents(client, idDoc, 0L);
				ParametrosServicios servicioParams = new ParametrosServicios().withIdDoc(idDoc).withVersion(0L);
				attachment = (Attachment) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_VERSIONS_CONTENTS);
				
			} catch (ServerSOAPFaultException e) {
				throw new SOAPException("Error al llamar al servicio getVersionContents de Content Server", e);
			}

			documentoBytes = attachment.getContents();

			// Se crea un fileDataSource con el nombre del documento
			// Si la ruta del directorio temporal no se recibe como parametro se
			// envia establece la ruta por defecto
			FileDataSource fileDataSource = null;

			fileDataSource = new FileDataSource(directoryPath + "/" + attachment.getFileName());

			// Se copia el documento en Bytes detro del File de FileDataSource
			try {
				FileUtils.writeByteArrayToFile(fileDataSource.getFile(), documentoBytes);
			} catch (IOException e) {
				throw new IOException("Error al pasar del byte a File", e);
			}
			DataHandler docDataHandler = new DataHandler(fileDataSource);

			listaDocDataHandler.add(docDataHandler);

		}
		return listaDocDataHandler;

	}
	
	
	/**
	 * Metodo que permite actualiziar los permisos de un Nodo
	 * @param soapAuthHeader
	 * @param idDocumento
	 * @param idGrupo
	 * @param wsdl
	 * @throws SOAPException
	 * @throws IOException
	 */
	public static void actualizarPermisosNodo(Header soapAuthHeader, long idDocumento, long idGrupo, String wsdl)
			throws SOAPException, IOException {

		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no puede ser nulo");
		}
		if (idDocumento < 1) {
			throw new IllegalArgumentException("El parametro idDocumento (" + idDocumento + ") no es valido");
		}
		
		if (idGrupo < 1) {
			throw new IllegalArgumentException("El parametro idGrupo (" + idGrupo + ") no es valido");
		}

		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		DocumentManagement_Service documentService = new DocumentManagement_Service(newURLWSDL, qName);
		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();

		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);

				
		NodeRight nodeRight = new NodeRight();
		
		nodeRight.setType("OwnerGroup");
		nodeRight.setRightID(idGrupo);
		
		NodePermissions permissions = new NodePermissions();
		
		permissions.setAddItemsPermission(true);
		permissions.setDeletePermission(true);
		permissions.setDeleteVersionsPermission(true);
		permissions.setEditAttributesPermission(true);
		permissions.setModifyPermission(true);
		permissions.setReservePermission(true);
		permissions.setSeeContentsPermission(true);
		permissions.setSeePermission(true);
		permissions.setEditPermissionsPermission(true);
		
		nodeRight.setPermissions(permissions);
		
		try {
			//client.updateNodeRight(idDocumento, nodeRight);
			//LlamadosWS.llamarUpdateNodeRight(client, idDocumento, nodeRight);
			ParametrosServicios servicioParams = new ParametrosServicios().withIdDoc(idDocumento).withNodeRight(nodeRight);
			LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.UPDATE_NODE_RIGHT);
			
		}catch (Exception e) {
			e.getStackTrace();
		}
	}
	
	/**
	 * Obtiene el id de la carpeta contenedora de de un documento
	 * 
	 * @param soapAuthHeader que contiene el token de autenticaci�n del servicio
	 * @param idDoc          correspode al id del documento en Content Server
	 * @param wsdl           Corresponde a la url de la wsdl del servicio de
	 *                       documento de Content Server
	 * @return el id de la carpeta contenedora
	 * @throws SOAPException si se presenta un error de Content Server
	 * @throws IOException   si hay un error de comunicacion con el servicio web
	 */
	public static long ObtenerParentIdDoc(Header soapAuthHeader, long idDoc, String wsdl) throws SOAPException, IOException {

		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no puede ser nulo");
		}

		if (idDoc < 1) {
			throw new IllegalArgumentException("El parametro idDoc (" + idDoc + ") no es valido");
		}

		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		DocumentManagement_Service documentService = null;

		try {
			documentService = new DocumentManagement_Service(newURLWSDL, qName);
		} catch (WebServiceException e) {
			throw new SOAPException("Se presento un error en el DocumentManagement_Service en la URL: " + newURLWSDL
					+ " - " + e.getMessage(), e);

		}

		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();

		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);

		Node documento = null;

		try {
			//documento = client.getNode(idDoc);
			ParametrosServicios servicioParams = new ParametrosServicios().withIdNode(idDoc);
			documento = (Node) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_NODE);
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException(
					"Error al llamar al servicio getVersionContents de Content Server- ORIGINAL: " + e.getMessage(), e);
		}

		return documento.getParentID();

	}
		
	
}
