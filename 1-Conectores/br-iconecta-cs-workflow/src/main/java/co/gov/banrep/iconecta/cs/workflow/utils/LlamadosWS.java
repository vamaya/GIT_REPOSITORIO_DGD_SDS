package co.gov.banrep.iconecta.cs.workflow.utils;

import java.util.List;

import javax.xml.ws.WebServiceException;

import com.sun.xml.ws.fault.ServerSOAPFaultException;

import co.gov.banrep.iconecta.cs.cliente.workflow.ApplicationData;
import co.gov.banrep.iconecta.cs.cliente.workflow.ProcessInstance;
import co.gov.banrep.iconecta.cs.cliente.workflow.ProcessStartData;
import co.gov.banrep.iconecta.cs.cliente.workflow.WorkflowService;

public class LlamadosWS {

	//Constantes para el manejo del error 501 de CS
	private static final long TIEMPO_ESPERA_REINTENTO = 10000;
	private static final int CANTIDAD_REINTENTOS = 15;

public static Object llamarServicio(WorkflowService client, ParametrosServicios servicioParams, NombreServicio llave) {
	//Variables para control de reintentos
	boolean error = true;
	int i = 1;
	//Variables de retorno
	ProcessStartData startData = null;
	ProcessInstance processInstance = null;
	List<ApplicationData> datos = null;

	while (error == true && i <= CANTIDAD_REINTENTOS) {


		try {
			System.out.println("Llamado al metodo: ["+ llave +"] en el intento: ["+i+"]");
			switch (llave) {
			case GET_PROCESS_START_DATA:
				startData = client.getProcessStartData(servicioParams.getMapId());	
				error = false;
				return startData;
			case START_PROCESS:
				processInstance = client.startProcess(servicioParams.getStartData(), servicioParams.getListIdAdjuntos(), servicioParams.getListMemberRoleId());
				error = false;
				return processInstance;
			case GET_PROCESS_DATA:
				datos = client.getProcessData(servicioParams.getWorkid(), servicioParams.getWorkid());
				error = false;
				return datos;

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
