package co.gov.banrep.iconecta.cs.autenticacion.utils;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.WebServiceException;

import com.sun.xml.ws.fault.ServerSOAPFaultException;

import co.gov.banrep.iconecta.cs.cliente.autenticacion.Authentication;
import co.gov.banrep.iconecta.cs.cliente.autenticacion.Authentication_Service;

public class LlamadosWS {
	
	//Constantes para el manejo del error 501 de CS
	private static final long TIEMPO_ESPERA_REINTENTO = 10000;
	private static final int CANTIDAD_REINTENTOS = 15;

public static Object llamarServicio(Authentication client, ParametrosServicios servicioParams, NombreServicio llave) {
	//Variables para control de reintentos
	boolean error = true;
	int i = 1;
	//Variables de retorno
	String adminToken = null;
	String userToken = null;
	XMLGregorianCalendar xmlExpirationDate = null;
	Authentication_Service service = null;

	while (error == true && i <= CANTIDAD_REINTENTOS) {


		try {
			System.out.println("Llamado al metodo: ["+ llave +"] en el intento: ["+i+"]");
			switch (llave) {
			case AUTHENTICATE_USER:
				adminToken = client.authenticateUser(servicioParams.getUsuarioAdmin(), servicioParams.getPassword());	
				error = false;
				return adminToken;
			case GET_SESSION_EXPIRATION_DATE:
				xmlExpirationDate = client.getSessionExpirationDate();
				error = false;
				return xmlExpirationDate;
			case AUTHENTICATION_SERVICE:
				service = new Authentication_Service(servicioParams.getNewEndPoint(), servicioParams.getQname());
				error = false;
				return service;
			case IMPERSONATE_USER:
				userToken = client.impersonateUser(servicioParams.getUsuario());
				return userToken;
			case REFRESH_TOKEN:
				userToken = client.refreshToken();
				return userToken;

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
				System.out.println("ERROR SERVICIO WEB- Fallaron todos los reintentos de ejecucion del metodo ["+ llave +"]");
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
