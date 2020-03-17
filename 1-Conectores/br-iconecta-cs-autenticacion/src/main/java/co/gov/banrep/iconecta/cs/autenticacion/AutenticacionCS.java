package co.gov.banrep.iconecta.cs.autenticacion;

import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.client.ClientTransportException;
import com.sun.xml.ws.developer.WSBindingProvider;
import com.sun.xml.ws.fault.ServerSOAPFaultException;

import co.gov.banrep.iconecta.cs.autenticacion.utils.LlamadosWS;
import co.gov.banrep.iconecta.cs.autenticacion.utils.NombreServicio;
import co.gov.banrep.iconecta.cs.autenticacion.utils.ParametrosServicios;
import co.gov.banrep.iconecta.cs.cliente.autenticacion.Authentication;
import co.gov.banrep.iconecta.cs.cliente.autenticacion.Authentication_Service;
import co.gov.banrep.iconecta.cs.cliente.autenticacion.OTAuthentication;



public class AutenticacionCS {
	
	private static final String ECM_API_NAMESPACE = "urn:api.ecm.opentext.com";
	private static final String NAMESPACE_URI = "urn:Core.service.livelink.opentext.com";
	private static final String LOCAL_PART = "Authentication";
	
	private String usuarioAdmin;
	private String password;
	private Header authenticationHeader;
	private String adminToken;
	private LocalDateTime expirationDate;
	private String urlWSDL;
	
	public AutenticacionCS(){}

	public String getUsuarioAdmin() {
		return usuarioAdmin;
	}

	public void setUsuarioAdmin(String usuarioAdmin) {
		this.usuarioAdmin = usuarioAdmin;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	};
		
	public Header getAuthenticationHeader() {
		return authenticationHeader;
	}
	public String getUrlWSDL() {
		return urlWSDL;
	}

	public void setUrlWSDL(String urlWSDL) {
		this.urlWSDL = urlWSDL;
	}
	
	public LocalDateTime getExpirationDate() {
		return expirationDate;
	}

	//TODO javadoc
	/**
	 * 
	 * @throws IOException
	 * @throws SOAPException 
	 */
	public void authenticate() throws IOException, SOAPException{
		
		
		//((WSBindingProvider) port).getRequestContext().put(WSBindingProvider.ENDPOINT_ADDRESS_PROPERTY, urlWSDL);
		//BindingProvider bp = (BindingProvider)client;
		//bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, urlWSDL);
		
		 QName qname = new QName(NAMESPACE_URI, LOCAL_PART);		
		 URL newEndPoint = new URL(urlWSDL);
		 
		 ParametrosServicios servicioParamsAut = new ParametrosServicios().withQName(qname).withURL(newEndPoint);
		 Authentication_Service service = (Authentication_Service) LlamadosWS.llamarServicio(null, servicioParamsAut, NombreServicio.AUTHENTICATION_SERVICE);
		 //Authentication_Service service = new Authentication_Service(newEndPoint, qname);
		 Authentication client = service.getBasicHttpBindingAuthentication();
		 		 		
		try {
			ParametrosServicios servicioParams = new ParametrosServicios().withUsuarioAdmin(usuarioAdmin).withPassword(password);
			adminToken = (String) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.AUTHENTICATE_USER);
			//adminToken = client.authenticateUser(usuarioAdmin, password);			
		} catch (ServerSOAPFaultException e) {
			throw new IOException("No se pudo realizar la autenticacion del usuario (" + usuarioAdmin + ")- ORIGINAL: " + e.getMessage(),e);
		} catch (ClientTransportException e) {
			throw new IOException("No se pudo realizar la autenticacion del usuario (" + usuarioAdmin + ")",e);
		}
		
		Header soapAuthHeader = getSoapHeader(adminToken);
		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);
		
		XMLGregorianCalendar xmlExpirationDate = null;
		
		try {
			ParametrosServicios servicioParams = new ParametrosServicios();
			xmlExpirationDate = (XMLGregorianCalendar) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_SESSION_EXPIRATION_DATE);
			//xmlExpirationDate = client.getSessionExpirationDate();
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException("No se pudo obtener la fecha de expiracion del usuario (" + usuarioAdmin
					+ ")- ORIGINAL: " + e.getMessage(), e);
		} catch (ClientTransportException e) {
			throw new IOException("No se pudo obtener la fecha de expiracion del usuario (" + usuarioAdmin + ")", e);
		}
		
		if (xmlExpirationDate != null) {
			Instant instant = xmlExpirationDate.toGregorianCalendar().toInstant();
			expirationDate = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
		} else {
			throw new NullPointerException("Fecha de expiracion nula");
		}
		
	}
	
	//TODO javadoc
	/**
	 * 
	 * @param user
	 * @return
	 * @throws IOException
	 * @throws SOAPException 
	 */
	public Header getUserSoapHeader(String user) throws IOException, SOAPException{
		String userToken = impersonateUserToken(user);		
		return getSoapHeader(userToken);
	}
	
	public String refreshSession(String token) throws IOException, SOAPException{
		
		QName qname = new QName(NAMESPACE_URI, LOCAL_PART);		
		URL newEndPoint = new URL(urlWSDL);
		ParametrosServicios servicioParamsAut = new ParametrosServicios().withQName(qname).withURL(newEndPoint);
		Authentication_Service service = (Authentication_Service) LlamadosWS.llamarServicio(null, servicioParamsAut, NombreServicio.AUTHENTICATION_SERVICE);
		//Authentication_Service service = new Authentication_Service(newEndPoint, qname);
		Authentication client = service.getBasicHttpBindingAuthentication();		

		((WSBindingProvider) client).setOutboundHeaders(getSoapHeader(token));
		
		//String nuevoToken = client.refreshToken();
		String nuevoToken = (String) LlamadosWS.llamarServicio(client, null, NombreServicio.REFRESH_TOKEN);
		
		return nuevoToken;
		
	}
		
	/**
	 * Retorna el header de autenticacion de la cuenta admin, si el token ya
	 * expiro se realiza re autenticacion.
	 * 
	 * @return El header que incluye el token de autenticacion
	 * @throws IOException
	 *             Si se presenta un error de comunicacion con los servicos de
	 *             Content Server
	 * @throws SOAPException
	 *             Si hay un error con los servicios de Content Server
	 */
	public Header getAdminSoapHeader() throws IOException, SOAPException {

		LocalDateTime LocalDateTime = java.time.LocalDateTime.now();

		// Si la fecha de expiración esta despues de la hora actual se solicita
		// autenticacion de nuevo
		if (expirationDate.isBefore(LocalDateTime.plusMinutes(5))) {
			System.out.println("Realizando Autenticacion");
			System.out.println("Fecha local: " + LocalDateTime);
			System.out.println("Fecha de expiracion: " + expirationDate);
			//adminToken = refreshSession(adminToken);
			authenticate();
		}

		return getSoapHeader(adminToken);
	}
	
	
	private String impersonateUserToken(String user) throws IOException, SOAPException{
		
		String userToken = "";
		
		QName qname = new QName(NAMESPACE_URI, LOCAL_PART);		
		URL newEndPoint = new URL(urlWSDL);		

		ParametrosServicios servicioParamsAut = new ParametrosServicios().withQName(qname).withURL(newEndPoint);
		Authentication_Service service = (Authentication_Service) LlamadosWS.llamarServicio(null, servicioParamsAut, NombreServicio.AUTHENTICATION_SERVICE);
		//Authentication_Service service = new Authentication_Service(newEndPoint, qname);
		Authentication client = service.getBasicHttpBindingAuthentication();		

		((WSBindingProvider) client).setOutboundHeaders(getSoapHeader(adminToken));
				
		try {
			//userToken = client.impersonateUser(user);
			ParametrosServicios servicioParamsTk = new ParametrosServicios().withUsuario(user);
			userToken = (String) LlamadosWS.llamarServicio(client, servicioParamsTk, NombreServicio.IMPERSONATE_USER);
			
		} catch (ServerSOAPFaultException e) {
			try {				
				authenticate();
				((WSBindingProvider) client).setOutboundHeaders(getSoapHeader(adminToken));
				userToken = client.impersonateUser(user);
			} catch (SocketException e1) {
				throw new SocketException("Error sesion Admin: " + e1.getMessage());
			} catch (Exception e1) {
				throw new SOAPException("Error sesion Admin ", e1);
			}
		} 

		return userToken;		
	}
	
	private Header getSoapHeader(String token) throws IOException, SOAPException{
		
		OTAuthentication otAuth = new OTAuthentication();
		otAuth.setAuthenticationToken(token);
				
		SOAPHeader SOAPHeader = null;
		SOAPHeaderElement otAuthElement = null;
		SOAPElement authTokenElement = null;
		
		try {
			SOAPHeader = MessageFactory.newInstance().createMessage().getSOAPPart().getEnvelope().getHeader();

			otAuthElement = SOAPHeader.addHeaderElement(new QName(ECM_API_NAMESPACE, "OTAuthentication"));

			authTokenElement = otAuthElement
					.addChildElement(new QName(ECM_API_NAMESPACE, "AuthenticationToken"));

			authTokenElement.addTextNode(otAuth.getAuthenticationToken());
		} catch (ServerSOAPFaultException e) {			
			
			
			try {				
				authenticate();
				SOAPHeader = MessageFactory.newInstance().createMessage().getSOAPPart().getEnvelope().getHeader();

				otAuthElement = SOAPHeader.addHeaderElement(new QName(ECM_API_NAMESPACE, "OTAuthentication"));

				authTokenElement = otAuthElement
						.addChildElement(new QName(ECM_API_NAMESPACE, "AuthenticationToken"));

				authTokenElement.addTextNode(otAuth.getAuthenticationToken());
			} catch (ServerSOAPFaultException | SOAPException e1) {
				throw new SOAPException("Error sesion admin- ORIGINAL: " + e1.getMessage(), e1);				
			}
		} 
	
		return Headers.create(otAuthElement);
		
	}
}
