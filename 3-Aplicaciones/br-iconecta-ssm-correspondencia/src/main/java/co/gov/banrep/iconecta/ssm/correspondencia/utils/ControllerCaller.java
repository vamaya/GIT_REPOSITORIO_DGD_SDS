package co.gov.banrep.iconecta.ssm.correspondencia.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.codehaus.stax2.XMLInputFactory2;

import co.gov.banrep.iconecta.ssm.correspondencia.xml.RespuestaXmlFirma;
import co.gov.banrep.iconecta.ssm.correspondencia.xml.RespuestaXmlPdfUtils;

public final class ControllerCaller {
	
	private static final String ENCODING = "UTF-8";	
	
	// NO SE PERMITEN INSTANCIAS DE ESTA CLASE
	private ControllerCaller() {
		throw new AssertionError();
	}
	
	
	
	/**
	 * 
	 * @param raizLog
	 * @param params
	 * @param strURLDestino
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public static String[] llamarAplicacionExterna(String raizLog, HashMap<String, Object> params, String strURLDestino, String type) throws Exception {
		
		//CREACIÓN DE LA URL DESTINO
		URL urlDestino = new URL(strURLDestino);
		System.out.println(raizLog + "Se crea la URL a partir de la dirección enviada");
		
		//LLENADO DE LA PARAMETROS
		StringBuilder postData = new StringBuilder();
		for(Map.Entry<String, Object> param : params.entrySet()){
			if(postData.length() != 0){
				postData.append('&');
			}
			postData.append(URLEncoder.encode(param.getKey(), ENCODING));
			postData.append('=');
			postData.append(URLEncoder.encode(String.valueOf(param.getValue()), ENCODING));
		}
		byte[] postDataBytes = postData.toString().getBytes(ENCODING); //se obtienen los bytes de la petición
		System.out.println(raizLog + "Se llenaron los parametros de la petición");
		
		//Se crea la conexión
		HttpURLConnection connection;
		
		connection = (HttpURLConnection) urlDestino.openConnection();
		connection.setRequestMethod(type);
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		connection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
		connection.setDoOutput(true);		
		
		//Salida
		DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream()); // This three lines is importy for POST method. I wrote preceding comment.
        dataOutputStream.write(postDataBytes);
        dataOutputStream.close();
        

        //Entrada (lo que responde la petición)
        InputStream inputStream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));        
        
        //Se revisa el codigo y el mensaje de respuesta de la petición        
        Integer codigoRespuesta = connection.getResponseCode();		
		System.out.println(raizLog + "El codigo recibido desde la petición HTTP es ["+codigoRespuesta+"]");
		String mensaje = connection.getResponseMessage();
		System.out.println(raizLog + "El mensaje recibido desde la petición HTTP es ["+mensaje+"]");
		
		
        	
		String strXmlResponse = null;
		//Se valida que no exista error
		if(codigoRespuesta==200) {
			//Se pasa la respuesta recibida a un String
			String line = "";
	        strXmlResponse = "";
	        while ((line = reader.readLine()) != null) {
	        	strXmlResponse += line;
	        	}
	        
		}else {//La respuesta no fué exitosa, se debe hacer reintentos
			System.out.println(raizLog + "El llamado a la petición HTTP no fué exitoso");
		}
        
		inputStream.close();
		
		String[] respuestaLlamadoHttpStrings = new String[3];
		
		respuestaLlamadoHttpStrings[0]=codigoRespuesta.toString();
		respuestaLlamadoHttpStrings[1]=mensaje;
		respuestaLlamadoHttpStrings[2]=strXmlResponse;
		
        return respuestaLlamadoHttpStrings;

	}
	
	/**
	 * 
	 * @param raizLog
	 * @param params
	 * @param strURLDestino
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public static RespuestaXmlPdfUtils llamarAplicacionPdfUtils(String raizLog, HashMap<String, Object> params, String strURLDestino, String type) throws Exception {
		
		//LLAMADO HTTP GENERICO
		String[] respuestaLlamadoHttpStrings = llamarAplicacionExterna(raizLog, params, strURLDestino, type);
		
		Integer codigoRespuesta = Integer.parseInt(respuestaLlamadoHttpStrings[0]);
		String mensaje = respuestaLlamadoHttpStrings[1];
		String strXmlResponse = respuestaLlamadoHttpStrings[2];
		
		RespuestaXmlPdfUtils xmlResponse = null;
		
		//Se valida que no exista error
		if(codigoRespuesta==200) {
	        //Se convierte la respuesta a un elemento XML
	        xmlResponse = unmarshallPdfUtils(strXmlResponse);
		}else {//La respuesta no fué exitosa, se debe hacer reintentos
			xmlResponse = new RespuestaXmlPdfUtils(false, mensaje, codigoRespuesta.toString());
			System.out.println(raizLog + "El llamado a la petición HTTP no fué exitoso");
		}        
		
        return xmlResponse;

	}
	
	
	/**
	 * 
	 * @param raizLog
	 * @param params
	 * @param strURLDestino
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public static RespuestaXmlFirma llamarAplicacionFirma(String raizLog, HashMap<String, Object> params, String strURLDestino, String type) throws Exception {
		
		//LLAMADO HTTP GENERICO
		String[] respuestaLlamadoHttpStrings = llamarAplicacionExterna(raizLog, params, strURLDestino, type);
		
		Integer codigoRespuesta = Integer.parseInt(respuestaLlamadoHttpStrings[0]);
		String mensaje = respuestaLlamadoHttpStrings[1];
		String strXmlResponse = respuestaLlamadoHttpStrings[2];
		
		RespuestaXmlFirma xmlResponse = null;
		
		//Se valida que no exista error
		if(codigoRespuesta==200) {
	        //Se convierte la respuesta a un elemento XML
	        xmlResponse = unmarshallFirma(strXmlResponse);
		}else {//La respuesta no fué exitosa, se debe hacer reintentos
			xmlResponse = new RespuestaXmlFirma(false, mensaje, codigoRespuesta.toString(), codigoRespuesta.toString());
			System.out.println(raizLog + "El llamado a la petición HTTP no fué exitoso");
		}        
		
        return xmlResponse;

	}
	
	
	/**
	 * 
	 * @param raizLog
	 * @param params
	 * @param strURLDestino
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public static String[] llamarAplicacionFirmaEvaluarFirmante(String raizLog, HashMap<String, Object> params, String strURLDestino, String type) throws Exception {
		
		//LLAMADO HTTP GENERICO
		String[] respuestaLlamadoHttpStrings = llamarAplicacionExterna(raizLog, params, strURLDestino, type);
		
        return respuestaLlamadoHttpStrings;

	}
	
	/**
	 * 
	 * @param responseXml
	 * @return
	 * @throws Exception
	 */
	public static RespuestaXmlPdfUtils unmarshallPdfUtils(String responseXml) throws Exception{
		
		if(responseXml == null){
    		throw new NullPointerException("Response XML no puede ser nulo");
    	}
    	
    	if(responseXml.isEmpty()){
    		throw new IllegalArgumentException("Resposne XML no puede esta vacio");
    	}
    	
 
    	InputStream in = null;
		try {
			in = new ByteArrayInputStream(responseXml.getBytes("UTF8"));
		} catch (IOException e) {
			throw new IOException("Error convirtiendo a bytes respuesta PDFUtils", e);		
		}finally {
			if (in != null) {
				safeClose(in);
			}
		}    	
    	
    	JAXBContext jc = JAXBContext.newInstance(RespuestaXmlPdfUtils.class);    	
    	
    	RespuestaXmlPdfUtils response;
		try {
			XMLInputFactory xif = XMLInputFactory2.newFactory();        
			xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
			xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);                              
			
			XMLStreamReader xsr = xif.createXMLStreamReader(in, "UTF8");       
			Unmarshaller u = jc.createUnmarshaller();        
			response = (RespuestaXmlPdfUtils) u.unmarshal(xsr);
		} catch (FactoryConfigurationError | JAXBException e) {
			throw new Exception("Error unmarshal respuesta portafirmas", e);		
		}

        return response;
		
	}

	
	 
	 
	 
		/**
		 * 
		 * @param responseXml
		 * @return
		 * @throws Exception
		 */
		public static RespuestaXmlFirma unmarshallFirma(String responseXml) throws Exception{
			
			if(responseXml == null){
	    		throw new NullPointerException("Response XML no puede ser nulo");
	    	}
	    	
	    	if(responseXml.isEmpty()){
	    		throw new IllegalArgumentException("Resposne XML no puede esta vacio");
	    	}
	    	
	 
	    	InputStream in = null;
			try {
				in = new ByteArrayInputStream(responseXml.getBytes("UTF8"));
			} catch (IOException e) {
				throw new IOException("Error convirtiendo a bytes respuesta Portafirmas", e);		
			}finally {
				if (in != null) {
					safeClose(in);
				}
			}    	
	    	
	    	JAXBContext jc = JAXBContext.newInstance(RespuestaXmlFirma.class);    	
	    	
	    	RespuestaXmlFirma response;
			try {
				XMLInputFactory xif = XMLInputFactory2.newFactory();        
				xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
				xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);                              
				
				XMLStreamReader xsr = xif.createXMLStreamReader(in, "UTF8");       
				Unmarshaller u = jc.createUnmarshaller();        
				response = (RespuestaXmlFirma) u.unmarshal(xsr);
			} catch (FactoryConfigurationError | JAXBException e) {
				throw new Exception("Error unmarshal respuesta portafirmas", e);		
			}

	        return response;
			
		}

		
		
		 private static void safeClose(InputStream in) throws IOException {
				if (in != null) {
					in.close();
				}
			}
		 
		 
		 
	 
}
