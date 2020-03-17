package co.gov.banrep.iconecta.cs.documento.utils;

import java.util.List;

import javax.xml.ws.WebServiceException;

import com.sun.xml.ws.fault.ServerSOAPFaultException;

import co.gov.banrep.iconecta.cs.cliente.document.Attachment;
import co.gov.banrep.iconecta.cs.cliente.document.AttributeGroup;
//import co.gov.banrep.iconecta.cs.cliente.document.CopyOptions;
import co.gov.banrep.iconecta.cs.cliente.document.DocumentManagement;
//import co.gov.banrep.iconecta.cs.cliente.document.Metadata;
//import co.gov.banrep.iconecta.cs.cliente.document.MoveOptions;
import co.gov.banrep.iconecta.cs.cliente.document.Node;
//import co.gov.banrep.iconecta.cs.cliente.document.NodeRight;
import co.gov.banrep.iconecta.cs.cliente.document.ReportResult;

public class LlamadosWS {
	
	//Constantes para el manejo del error 501 de CS
		private static final long TIEMPO_ESPERA_REINTENTO = 10000;
		private static final int CANTIDAD_REINTENTOS = 15;
	
	public static Object llamarServicio(DocumentManagement client, ParametrosServicios servicioParams, NombreServicio llave) {
		//Variables para control de reintentos
		boolean error = true;
		int i = 1;
		//Variables de retorno
		Node nodo = null;
		Attachment attachment = null;
		AttributeGroup categoryTemplate = null;
		List<Node> nodos = null;
		ReportResult resultado = null;

		while (error == true && i <= CANTIDAD_REINTENTOS) {


			try {
				System.out.println("Llamado al metodo: ["+ llave +"] en el intento: ["+i+"]");
				switch (llave) {
				case GET_NODE:
					nodo = client.getNode(servicioParams.getIdNode());
					error = false;
					return nodo;
				case UPDATE_NODE:
					client.updateNode(servicioParams.getNodo());
					error = false;
					return null;
				case PURGE_VERSIONS:
					client.purgeVersions(servicioParams.getIdDoc(), servicioParams.getNumberToKeep());
					error = false;
					return null;
				case GET_VERSIONS_CONTENTS:
					attachment = client.getVersionContents(servicioParams.getIdDoc(), servicioParams.getVersion());
					error = false;
					return attachment;
				case GET_NODE_BY_NAME:
					nodo = client.getNodeByName(servicioParams.getIdParent(), servicioParams.getNombreDoc());
					error = false;
					return nodo;
				case COPY_NODE:
					nodo = client.copyNode(servicioParams.getIdDoc(), servicioParams.getIdParent(), servicioParams.getNuevoNombre(), servicioParams.getCopyOptions());
					error = false;
					return nodo;
				case UPDATE_NODE_RIGHT:
					client.updateNodeRight(servicioParams.getIdDoc(), servicioParams.getNodeRight());
					error = false;
					return null;
				case ADD_VERSION:
					client.addVersion(servicioParams.getIdDoc(), servicioParams.getMetadata(), servicioParams.getAttachment());
					error = false;
					return null;
				case RENAME_NODE:
					client.renameNode(servicioParams.getIdDoc(), servicioParams.getNombreDoc());
					error = false;
					return null;
				case CREATE_DOCUMENT:
					nodo = client.createDocument(servicioParams.getIdParent(), servicioParams.getNombreDoc(), servicioParams.getComment(), servicioParams.isVersionControl(), servicioParams.getMetadata(), servicioParams.getAttachment());
					error = false;
					return nodo;
				case GET_CATEGORY_TEMPLATE:
					categoryTemplate = client.getCategoryTemplate(servicioParams.getIdCategoria());
					error = false;
					return categoryTemplate;
				case MOVE_NODE:
					client.moveNode(servicioParams.getIdDoc(), servicioParams.getIdParent(), servicioParams.getNuevoNombre(), servicioParams.getMoveOptions());
					error = false;
					return null;
				case DELETE_NODE:
					client.deleteNode(servicioParams.getIdNode());
					error = false;
					return null;
				case GET_NODES_IN_CONTAINER:
					nodos = client.getNodesInContainer(servicioParams.getContenedor(), servicioParams.getOptions());
					error = false;
					return nodos;
				case RUN_REPORT:
					resultado = client.runReport(servicioParams.getIdReport(), servicioParams.getInputs());
					error = false;
					return resultado;

				default:
					return null;
				}

			}catch (WebServiceException e) {
				if(e instanceof ServerSOAPFaultException) {
					throw e;
				}
				error = true;
				i++;
	
				if(i > CANTIDAD_REINTENTOS) {
					System.out.println("ERROR SERVICIO WEB - Fallaron todos los reintentos de ejecucion del metodo ["+ llave +"]");
					e.printStackTrace();//Al llegar al máximo número de reintentos se imprime la traza completa del error			
				}else {
					System.out.println("ERROR SERVICIO WEB - Fallo el llamado al metodo [" + llave + "] - Se procede a realizar el reintento ["+ i +"]");
					System.out.println(e.getMessage());//No se quiere imprimir la traza completa de la excepción	
				}	
				
				try {
					Thread.sleep(TIEMPO_ESPERA_REINTENTO);
				} catch (InterruptedException e1) {
					System.out.println("ERROR en el tiempo de espera del reintento");
					e1.printStackTrace();
				}
			}
		}
		//? se añade porque el método exige retorno
		return null;
	}
	
}
