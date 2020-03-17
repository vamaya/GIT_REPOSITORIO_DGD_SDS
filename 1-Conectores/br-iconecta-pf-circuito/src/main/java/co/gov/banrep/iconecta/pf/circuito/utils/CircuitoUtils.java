package co.gov.banrep.iconecta.pf.circuito.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import co.gov.banrep.iconecta.pf.circuito.entity.Documento;
import co.gov.banrep.iconecta.pf.circuito.entity.Firmante;
import co.gov.banrep.iconecta.pf.cliente.ScircuitSignersWS;
import co.gov.banrep.iconecta.pf.cliente.SignDocObject;

public final class CircuitoUtils {
	
	private CircuitoUtils(){
		throw new AssertionError();
	}
	
	public static List<ScircuitSignersWS> cargarFirmantesWs(List<Firmante> firmantes){
		
		if (firmantes == null){
			throw new NullPointerException("El parametro Firmantes no puede ser nulo");			
		}
		
		List<ScircuitSignersWS> firmantesWs = new ArrayList<ScircuitSignersWS>();
		
		Iterator<Firmante> iterFirmantes = firmantes.iterator();		
		while(iterFirmantes.hasNext()){
			ScircuitSignersWS unFirmanteWs = new ScircuitSignersWS();
			Firmante unFirmante = iterFirmantes.next();
			
			unFirmanteWs.setNif(unFirmante.nif);
			unFirmanteWs.setIdAction(unFirmante.idAction);
			unFirmanteWs.setAddDocs(booleanToInt(unFirmante.enableAddDocs));
			unFirmanteWs.setAddSigners(booleanToInt(unFirmante.enableAddSigners));
			unFirmanteWs.setDeleteSigner(booleanToInt(unFirmante.enableDeleteSigners));
			unFirmanteWs.setDiasLimite(unFirmante.daysLimit);
			unFirmanteWs.setEditSigner(booleanToInt(unFirmante.enableEditSigner));
			unFirmanteWs.setIdActionDeadTask(unFirmante.idActionDeadTask);
			unFirmanteWs.setIdOrganizationGroup(unFirmante.idOrganizationGroup);
			//TODO Para que inicie el circuito se tiene que enviar el idRol nulo
			//unFirmanteWs.setIdRol(unFirmante.idRol);
			unFirmanteWs.setIdRol(null);
			unFirmanteWs.setNoticeDays(unFirmante.noticeDays);
			unFirmanteWs.setReassignTask(booleanToInt(unFirmante.enableReassignTask));
			unFirmanteWs.setSigndesatendida(booleanToInt(unFirmante.enableSigndesatendida));
			unFirmanteWs.setTimestamp(booleanToInt(unFirmante.enableTimestamp));
			
			firmantesWs.add(unFirmanteWs);
		}
		
		return firmantesWs;
	}
	
	

	public static List<SignDocObject> CargarDocumentosWS(List<Documento> documentos) {
		
		if(documentos == null){
			throw new NullPointerException("El parametro documentos no puede ser nulo");
		}
		
		List<SignDocObject> documentosWs = new ArrayList<SignDocObject>();
		
		Iterator<Documento> iterDocumentos = documentos.iterator();
		while(iterDocumentos.hasNext()){
			SignDocObject unDocumentoWs = new SignDocObject();
			Documento unDocumento = iterDocumentos.next();
			
			unDocumentoWs.setNOMBRE(unDocumento.nombre);
			unDocumentoWs.setCONTENIDO(unDocumento.contenido);
			
			documentosWs.add(unDocumentoWs);
		}
		
		return documentosWs;
	}
	
	public static int booleanToInt(boolean value){
		return value ? 1 : 0;
	}


}
