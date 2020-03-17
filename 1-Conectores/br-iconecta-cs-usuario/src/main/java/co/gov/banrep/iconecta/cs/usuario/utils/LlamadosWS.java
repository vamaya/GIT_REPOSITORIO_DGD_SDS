package co.gov.banrep.iconecta.cs.usuario.utils;

import javax.xml.ws.WebServiceException;

import com.sun.xml.ws.fault.ServerSOAPFaultException;

import co.gov.banrep.iconecta.cs.cliente.user.Member;
import co.gov.banrep.iconecta.cs.cliente.user.MemberService;
import co.gov.banrep.iconecta.cs.cliente.user.User;


public class LlamadosWS {
	//Constantes para el manejo del error 501 de CS
	private static final long TIEMPO_ESPERA_REINTENTO = 10000;
	private static final int CANTIDAD_REINTENTOS = 15;

public static Object llamarServicio(MemberService client, ParametrosServicios servicioParams, NombreServicio llave) {
	//Variables para control de reintentos
	boolean error = true;
	int i = 1;
	//Variables de retorno
	Member member = null;
	User usuario = null;
	Member grupo = null;

	while (error == true && i <= CANTIDAD_REINTENTOS) {


		try {
			System.out.println("Llamado al metodo: ["+ llave +"] en el intento: ["+i+"]");
			switch (llave) {
			case GET_MEMBER_BY_ID:
				member = client.getMemberById(servicioParams.getIdUsuario());
				error = false;
				return member;
			case GET_USER_BY_LOGIN_NAME:
				usuario = client.getUserByLoginName(servicioParams.getLoginName());
				error = false;
				return usuario;
			case GET_GROUP_BY_NAME:
				grupo = client.getGroupByName(servicioParams.getNombreGrupo());
				error = false;
				return grupo;
			case GET_MEMBER_BY_LOGIN_NAME:
				grupo = client.getMemberByLoginName(servicioParams.getNombreGrupo());
				error = false;
				return grupo;

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
