package co.gov.banrep.iconecta.cs.documento;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.developer.WSBindingProvider;

import co.gov.banrep.iconecta.cs.cliente.document.DocumentManagement;
import co.gov.banrep.iconecta.cs.cliente.document.DocumentManagement_Service;
import co.gov.banrep.iconecta.cs.cliente.document.GetNodesInContainerOptions;
import co.gov.banrep.iconecta.cs.cliente.document.Node;
import co.gov.banrep.iconecta.cs.documento.utils.LlamadosWS;
import co.gov.banrep.iconecta.cs.documento.utils.NombreServicio;
import co.gov.banrep.iconecta.cs.documento.utils.ParametrosServicios;

public final class BusquedaDocumentos {
	
	private BusquedaDocumentos(){
		throw new AssertionError();
	}

	/*
	public static List<Long> obtenerIdsDocsContainer(Header soapAuthHeader, long contenedor) {
		DocumentManagement_Service documentService = new DocumentManagement_Service();
		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();
		
		List<Long> documentos = new ArrayList<Long>();

		// Lamado al servicio de Obtener Contenido del Content Server		
		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);
				
		//List<Node> nodos = client.getNodesInContainer(contenedor, new GetNodesInContainerOptions());		
		return documentos;		
	}
	*/
	
	public static List<Long> obtenerIdsDocsContainer(Header soapAuthHeader, long contenedor, String wsdl) throws IOException {
		
		if (wsdl == null) {
			throw new NullPointerException("El parametro wsdl  no debe ser nulo");
		}
		if (wsdl.isEmpty()) {
			throw new IllegalArgumentException("El parametro wsdl  no debe estar vacio");
		}
		
		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);
		
		DocumentManagement_Service documentService = new DocumentManagement_Service(newURLWSDL,qName);
		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();
		
		List<Long> documentos = new ArrayList<Long>();
		
		System.out.println("Id contenedor [" + contenedor + "]");

		// Lamado al servicio de Obtener Contenido del Content Server		
		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);
		
		GetNodesInContainerOptions options = new GetNodesInContainerOptions();
		
		options.setMaxDepth(1);
		options.setMaxResults(1);
				
		//List<Node> nodos = client.getNodesInContainer(contenedor, options);
		ParametrosServicios servicioParams = new ParametrosServicios().withContenedor(contenedor).withOptions(options);
		@SuppressWarnings("unchecked")
		List<Node> nodos = (List<Node>) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_NODES_IN_CONTAINER);
		
		System.out.println("Tamano nodos [" + nodos.size() + "]");
		
		Iterator<Node> iterNodes = nodos.iterator();
		while(iterNodes.hasNext()) {
			Node unNodo = iterNodes.next();
			documentos.add(unNodo.getID());
		}
		
		return documentos;		
	}

}
