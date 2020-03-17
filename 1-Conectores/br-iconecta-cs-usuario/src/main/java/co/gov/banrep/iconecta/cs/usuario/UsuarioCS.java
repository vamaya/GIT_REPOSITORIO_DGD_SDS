package co.gov.banrep.iconecta.cs.usuario;

import java.io.IOException;
import java.net.URL;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.developer.WSBindingProvider;
import com.sun.xml.ws.fault.ServerSOAPFaultException;

import co.gov.banrep.iconecta.cs.cliente.user.Member;
import co.gov.banrep.iconecta.cs.cliente.user.MemberService;
import co.gov.banrep.iconecta.cs.cliente.user.MemberService_Service;
import co.gov.banrep.iconecta.cs.cliente.user.User;
import co.gov.banrep.iconecta.cs.usuario.utils.LlamadosWS;
import co.gov.banrep.iconecta.cs.usuario.utils.NombreServicio;
import co.gov.banrep.iconecta.cs.usuario.utils.ParametrosServicios;

public final class UsuarioCS {
	
	private static final String NAMESPACE_URI = "urn:MemberService.service.livelink.opentext.com";
	private static final String LOCAL_PART = "MemberService";
	
	private UsuarioCS(){
		throw new AssertionError();
	}
	
	public static User getUsuario (Header header, long idUsuario, String wsdl) throws SOAPException, IOException{
		
		if (header == null) {
			throw new NullPointerException("El parametro soapAuthHeader no debe ser nulo");
		}		
		if (idUsuario < 0) {
			throw new IllegalArgumentException("Id de usuario (" + idUsuario + ") no validos");
		}
		
		QName qName = new QName(NAMESPACE_URI, LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);
		
		MemberService_Service memberService = new MemberService_Service(newURLWSDL, qName);
		MemberService client = memberService.getBasicHttpBindingMemberService();
		
		((WSBindingProvider) client).setOutboundHeaders(header);
		
		ParametrosServicios servicioParams = new ParametrosServicios().withIdUsuario(idUsuario);
		Member member = (Member) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_MEMBER_BY_ID);
		//Member member = client.getMemberById(idUsuario);		
		String loginName = member.getName();		
		User usuario = null;
		try {
			ParametrosServicios servicioParams2 = new ParametrosServicios().withLoginName(loginName);
			usuario = (User) LlamadosWS.llamarServicio(client, servicioParams2, NombreServicio.GET_USER_BY_LOGIN_NAME);
			//usuario = client.getUserByLoginName(loginName);
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException("Error al llamar al servicio getUserByLoginName de Content Server  - ORIGINAL: " + e.getMessage(), e);
		}
		
		return usuario;
		
	}
	
	//cambios johan
	public static Member getGrupoByNombre (Header header, String nombreGrupo, String wsdl) throws SOAPException, IOException {
		
		if (header == null) {
			throw new NullPointerException("El parametro soapAuthHeader no debe ser nulo");
		}
		
		if (nombreGrupo == null) {
			throw new IllegalArgumentException("Nombre grupo (" + nombreGrupo + ") no valido");
		}
		
		if (nombreGrupo.isEmpty()){
			throw new IllegalArgumentException("El parametro nombreGrupo no debe estar vacio");
		}
		
			QName qName = new QName(NAMESPACE_URI, LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);
		
		MemberService client = null;
		try {
			MemberService_Service memberService = new MemberService_Service(newURLWSDL, qName);
			client = memberService.getBasicHttpBindingMemberService();
		} catch (Exception e) {
			throw new SOAPException("No pudo inicializarse el servicio con la wsdl (" + newURLWSDL + ")",e);
		}
		
		((WSBindingProvider) client).setOutboundHeaders(header);
				
		Member grupo=null;
		try {
			ParametrosServicios servicioParams = new ParametrosServicios().withNombreGrupo(nombreGrupo);
			grupo = (Member) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_GROUP_BY_NAME);
			//grupo=client.getGroupByName(nombreGrupo);
			
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException("Error al llamar al servicio getUserByLoginName de Content Server  - ORIGINAL: " + e.getMessage(), e);
		}
		
		if(grupo == null){
			throw new NoSuchElementException("No se encontro el nombre del grupo (" + nombreGrupo + ")");			
		}
		
		return grupo;
		
	}
	
	public static Member getMemberByLoginName (Header header, String nombreGrupo, String wsdl) throws SOAPException, IOException {
		
		if (header == null) {
			throw new NullPointerException("El parametro soapAuthHeader no debe ser nulo");
		}
		
		if (nombreGrupo == null) {
			throw new IllegalArgumentException("Nombre grupo (" + nombreGrupo + ") no valido");
		}
		
			QName qName = new QName(NAMESPACE_URI, LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);
		
		MemberService client = null;
		try {
			MemberService_Service memberService = new MemberService_Service(newURLWSDL, qName);
			client = memberService.getBasicHttpBindingMemberService();
		} catch (Exception e) {
			throw new SOAPException("No pudo inicializarse el servicio con la wsdl (" + newURLWSDL + ")",e);
		}
		
		((WSBindingProvider) client).setOutboundHeaders(header);
				
		Member grupo=null;
		try {
			ParametrosServicios servicioParams = new ParametrosServicios().withNombreGrupo(nombreGrupo);
			grupo = (Member) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_MEMBER_BY_LOGIN_NAME);
			//grupo=client.getMemberByLoginName(nombreGrupo);
			
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException("Error al llamar al servicio getUserByLoginName de Content Server  - ORIGINAL: " + e.getMessage(), e);
		}
		
		if(grupo == null){
			throw new NoSuchElementException("No se encontro el nombre del grupo (" + nombreGrupo + ")");			
		}
		
		return grupo;
		
	}

}
