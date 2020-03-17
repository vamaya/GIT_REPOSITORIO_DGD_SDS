package co.gov.banrep.iconecta.pf.circuito;

import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;
import co.gov.banrep.iconecta.pf.circuito.entity.Circuito;
import co.gov.banrep.iconecta.pf.circuito.utils.CircuitoUtils;
import co.gov.banrep.iconecta.pf.cliente.PortafirmasBanRepWebService;
import co.gov.banrep.iconecta.pf.cliente.PortafirmasBanRepWebServiceService;
import co.gov.banrep.iconecta.pf.cliente.ResultWSResponse;
import co.gov.banrep.iconecta.pf.cliente.ScircuitSignersWS;
import co.gov.banrep.iconecta.pf.cliente.SignDocObject;
import com.sun.org.apache.xml.internal.security.utils.Base64;

public final class CircuitoPortafirmas {
	
	private static final String NAMESPACE_URI = "http://webservices.portafirmaswslib.esigna.indenova.com/";
	private static final String LOCAL_PART = "PortafirmasBanRepWebServiceService";
	
	private static String ticketBase64 = "";	
	private static String respuestaServicio = "";
	private static String codigoRespuestaServicio = "";

	private CircuitoPortafirmas() {
		throw new AssertionError();
	}

	public static Integer iniciarCircuito(String ticket, String nifAdmin, String url, Circuito circuito, String wsdl) throws Exception {
		
		if(circuito == null){
			throw new NullPointerException("El parametro circuito no puede ser nulo");
		}
		
		if(ticket == null || ticket.isEmpty()){
			throw new NullPointerException("El parametro ticket no puede ser nulo");
		}	
		
		if(nifAdmin == null || nifAdmin.isEmpty()){
			throw new NullPointerException("El parametro circuito no puede ser nulo");
		}
		
		byte[] ticketBytes = ticket.getBytes();
		String ticket64 = Base64.encode(ticketBytes);

		List<ScircuitSignersWS> firmantesWs = CircuitoUtils.cargarFirmantesWs(circuito.firmantes);
		List<SignDocObject> documentosWs = CircuitoUtils.CargarDocumentosWS(circuito.documentos);
		
		QName qName = new QName(NAMESPACE_URI, LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		PortafirmasBanRepWebServiceService service = new PortafirmasBanRepWebServiceService(newURLWSDL, qName);
		PortafirmasBanRepWebService port = service.getPortafirmasBanRepWebServicePort();
		
		ResultWSResponse response = null;
		
		response = port.initCircuitBySigners(ticket64.getBytes(), circuito.asunto, circuito.idOrgGroup,
					CircuitoUtils.booleanToInt(circuito.enableNoticeEnd), circuito.valuesForm, documentosWs,
					circuito.nameFolder, url, firmantesWs, nifAdmin);		
				
		codigoRespuestaServicio = response.getResponse().getCode();
		respuestaServicio =  response.getResponse().getDescriptionCode();

		if (codigoRespuestaServicio.equals("000")) {
			String instanceStr = response.getCircuitInstance();
			int instance;
			try {
				instance = Integer.valueOf(instanceStr);
			} catch (NumberFormatException e) {
				throw new NumberFormatException("EL id (" + instanceStr + ") no es un id de instancia de circuito valido");
			}
			return instance;
		}
		return -1;
	}
		

	/**
	 * @return the respuestaServicio
	 */
	public static String getRespuestaServicio() {
		return respuestaServicio;
	}

	/**
	 * @return the codigoRespuestaServicio
	 */
	public static String getCodigoRespuestaServicio() {
		return codigoRespuestaServicio;
	}


	public static String getTicketBase64() {
		return ticketBase64;
	}


	public static void setTicketBase64(String ticketBase64) {
		CircuitoPortafirmas.ticketBase64 = ticketBase64;
	}
	
	
	
	
}
