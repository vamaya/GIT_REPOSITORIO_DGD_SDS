package co.gov.banrep.iconecta.cs.documento;

import java.io.IOException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;

import co.gov.banrep.iconecta.cs.cliente.document.AttributeSourceType;
import co.gov.banrep.iconecta.cs.cliente.document.CopyOptions;
import co.gov.banrep.iconecta.cs.cliente.document.DocumentManagement;
import co.gov.banrep.iconecta.cs.cliente.document.DocumentManagement_Service;
import co.gov.banrep.iconecta.cs.cliente.document.Node;
import co.gov.banrep.iconecta.cs.documento.utils.LlamadosWS;
import co.gov.banrep.iconecta.cs.documento.utils.NombreServicio;
import co.gov.banrep.iconecta.cs.documento.utils.ParametrosServicios;

import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.developer.WSBindingProvider;
import com.sun.xml.ws.fault.ServerSOAPFaultException;

public final class Nodo {
	
	private DocumentManagement client;

	public Nodo(String wsdl) throws IOException, InterruptedException {
		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		try {

			DocumentManagement_Service documentService = new DocumentManagement_Service(newURLWSDL, qName);
			this.client = documentService.getBasicHttpBindingDocumentManagement();

		} catch (WebServiceException e) {
			if (e instanceof ServerSOAPFaultException) {
				throw e;
			}

			long tiempoEspera = 10000;
			Thread.sleep(tiempoEspera);

			DocumentManagement_Service documentService = new DocumentManagement_Service(newURLWSDL, qName);
			this.client = documentService.getBasicHttpBindingDocumentManagement();

		}

	}
	
	/*	
	private Nodo(){
		throw new AssertionError();
	}
	*/
	
	public long copiarNodo(Header soapAuthHeader, long idDocumento, long idParent, String nuevoNombre) throws SOAPException, IOException{
		
		if(idDocumento <= 0){
			throw new IllegalArgumentException("Id Documento (" + idDocumento + ") no valido");
		}		
		if(idParent <= 0){
			throw new IllegalArgumentException("Id Parent (" + idParent + ") no valido");
		}
		if(soapAuthHeader == null){
			throw new NullPointerException("El parametro soapAuthHeader no debe ser nulo");
		}
		
		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);
		
		
		CopyOptions copyOptions = new CopyOptions();
		//copyOptions.setCopyCurrent(true);
		copyOptions.setAttrSourceType(AttributeSourceType.DESTINATION);
		Node documentoCopiado = null;
				
		try {
			//documentoCopiado = client.copyNode(idDocumento, idParent, nuevoNombre, copyOptions);
			ParametrosServicios servicioParams = new ParametrosServicios().withIdDoc(idDocumento).withIdParent(idParent).withNuevoNombre(nuevoNombre).withCopyOptions(copyOptions);
			documentoCopiado = (Node) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.COPY_NODE);
		} catch (SOAPFaultException e) {
			throw new SOAPException("Error con el llamado al Servicio copyNode de Content Server - ORIGINAL: " + e.getMessage(), e);
		}
	
		return documentoCopiado.getID();
	}

}
