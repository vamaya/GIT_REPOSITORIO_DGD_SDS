package co.gov.banrep.iconecta.pf.circuito;

import javax.activation.DataHandler;
import javax.xml.ws.BindingProvider;

import com.sun.org.apache.xml.internal.security.utils.Base64;

import co.gov.banrep.iconecta.pf.circuito.entity.RespuestaObtenerDocumeto;
import co.gov.banrep.iconecta.pf.circuito.utils.DocumentoUtils;
import co.gov.banrep.iconecta.pf.cliente.PortafirmasBanRepWebService;
import co.gov.banrep.iconecta.pf.cliente.PortafirmasBanRepWebServiceService;
import co.gov.banrep.iconecta.pf.cliente.ResultResponse;
import co.gov.banrep.iconecta.pf.cliente.SignDocObject;
import co.gov.banrep.iconecta.pf.cliente.SignDocObject.METADATOS;

public final class DocumentoPortafirmas {
		
	private DocumentoPortafirmas() {
		throw new AssertionError();
	}
		
	
	/**
	 * 
	 * @param idDocumento
	 * @return
	 */
	public static RespuestaObtenerDocumeto obtenerDocumentoFirmando(String ticket, String idDocumento, String endpoint) {

		if (idDocumento == null) {
			throw new NullPointerException("El parametro idDocumento no puede ser nulo");
		}

		if (idDocumento.isEmpty()) {
			throw new IllegalArgumentException("El parametro iDocumento contiene una cadena vacia");
		}
		
		byte[] ticketBytes = ticket.getBytes();
		String ticket64 = Base64.encode(ticketBytes);

		SignDocObject documentoPF = ObtenerDocumentoPF(ticket64, idDocumento, true, endpoint);
		
		DataHandler contenido = null;
		METADATOS metadatos = null;
		
		if (documentoPF != null) {
			contenido = documentoPF.getCONTENIDO();
			metadatos = documentoPF.getMETADATOS();
			
			if(contenido == null){
				throw new NullPointerException("Contenido nulo del documento de Portafirmas - id: " + idDocumento);
			}
			
			if(metadatos == null){
				throw new NullPointerException("Metadatos nulos del documento de Portafirmas - id: " + idDocumento);
			}
			
		} else {
			throw new NullPointerException("Respuesta Null por parte de Portafirmas - Documento id: " + idDocumento);
		}
		
		//TODO bloque de prueba
		/*
		String raiz = "PRUEBA CAMBIO PF - ";
		List<Entry> listaMetadatos = metadatos.getEntry();
		System.out.println(raiz + "tamano lista: " + listaMetadatos.size());
		Iterator<Entry> iterMetadatos = listaMetadatos.iterator();
		while(iterMetadatos.hasNext()){
			Entry unEntry = iterMetadatos.next();
			String unLlave = unEntry.getKey();
			String unValor = unEntry.getValue();
			System.out.println(raiz + "una llave: " + unLlave);
			System.out.println(raiz + "un valor: " + unValor);
		}
		*/

		return new RespuestaObtenerDocumeto(contenido, DocumentoUtils.cargarMapMetadatos(metadatos));

	}
	
	/**
	 * 
	 * @param idDocumento
	 * @return
	 */
	public static RespuestaObtenerDocumeto obtenerCopiaCompulsada(String ticket, String idDocumento, String endpoint) {

		if (idDocumento == null) {
			throw new NullPointerException("El parametro idDocumento no puede ser nulo");
		}

		if (idDocumento.isEmpty()) {
			throw new IllegalArgumentException("El parametro iDocumento contiene una cadena vacia");
		}
		
		byte[] ticketBytes = ticket.getBytes();
		String ticket64 = Base64.encode(ticketBytes);

		SignDocObject documentoPF = ObtenerDocumentoPF(ticket64, idDocumento, false, endpoint);

		return new RespuestaObtenerDocumeto(documentoPF.getCONTENIDO(), DocumentoUtils.cargarMapMetadatos(documentoPF.getMETADATOS()));
	}

	private static SignDocObject ObtenerDocumentoPF(String ticket, String idDocumento, boolean tipoDocumento, String endpoint) {

		PortafirmasBanRepWebServiceService service = new PortafirmasBanRepWebServiceService();
		PortafirmasBanRepWebService port = service.getPortafirmasBanRepWebServicePort();
		
		((BindingProvider) port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);

		SignDocObject documentoPF = new SignDocObject();

		if (tipoDocumento) {
			documentoPF = port.downloadFile(ticket.getBytes(), idDocumento, 0);
		} else {
			documentoPF = port.downloadFile(ticket.getBytes(), idDocumento, 1);
		}
		
		if (documentoPF == null){
			throw new NullPointerException("Error en la operacion download file de portafirmas");
		}

		return documentoPF;

	}

	public static String borrarDocumento(String ticket, String idDocumento, String endpoint) throws Exception {

		if (ticket == null) {
			throw new NullPointerException("El parametro ticket no debe ser nulo");
		}
		if (idDocumento == null) {
			throw new NullPointerException("El parametro idDocumento no debe ser nulo");
		}
		if (endpoint == null) {
			throw new NullPointerException("El parametro endpoint no debe ser nulo");
		}
		if (ticket.isEmpty()) {
			throw new IllegalArgumentException("El parametro ticket no debe estar vacio");
		}
		if (idDocumento.isEmpty()) {
			throw new IllegalArgumentException("El parametro idDocumento no debe estar vacio");
		}
		if (endpoint.isEmpty()) {
			throw new IllegalArgumentException("El parametro endpoint no debe estar vacio");
		}

		PortafirmasBanRepWebServiceService service = new PortafirmasBanRepWebServiceService();
		PortafirmasBanRepWebService port = service.getPortafirmasBanRepWebServicePort();

		((BindingProvider) port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);

		ResultResponse respuesta = port.deleteDocument(ticket.getBytes(), idDocumento);

		if (respuesta != null) {
			return respuesta.getDescriptionCode();
		} else {
			return "CODIGO DE RESPUETA NO ENVIADO POR PORTAFIRMAS";
		}

	}



}
