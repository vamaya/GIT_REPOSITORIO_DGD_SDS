package co.gov.banrep.iconecta.ssm.firma.respuestapf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.codehaus.stax2.XMLInputFactory2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author jrojassa
 */

@WebService(serviceName = "EndCircuitJaxWsService", targetNamespace = "portafirmas", portName = "CircuitoResponsePort")
//@Component
public class EndCircuitJaxWsService {
	
	private final Logger log = LoggerFactory.getLogger(EndCircuitJaxWsService.class);
	
	//@Autowired
	//private SsmRestClient ssmRestClient;

    @WebMethod(operationName = "CircuitoResponse")
    public int CircuitoResponse(@WebParam(name = "hmCircuitResponse") String hmCircuitResponse) throws Exception {
        
             
        log.info("INICIO RESPUESTA RECIBIDA PORTAFIRMAS");
        
        log.trace("Respuesta Base64: " + hmCircuitResponse);       
        //byte[] decode = Base64.decode(hmCircuitResponse);
        byte[] decode =DatatypeConverter.parseBase64Binary(hmCircuitResponse);

        String respuestaStr = "";
        try {
            respuestaStr = new String(decode, "UTF-8");
        } catch (UnsupportedEncodingException e) {            
        	log.error("Error docodificando respuesta");
        	log.error("Respuesta Base 64: "+ hmCircuitResponse, e);
        }
        
        log.trace("Respuesta decodificada: " + respuestaStr);
        
        HmCircuitResponse response = null;

        try {
            response = unmarshal(respuestaStr);
        } catch (Exception e) {          
        	log.error("Error unmarshal respuesta");
        	log.error("Respuesta plana : "+ respuestaStr, e);
        	throw new Exception("Error unmarshal respuesta");
        }

        List<HmCircuitResponse.DocumentsInfo.Document> documentos = response.getDocumentsInfo().getDocument();
            
             
        ArrayList<String> datosDocumentos = new ArrayList<String>();
        
        Iterator<HmCircuitResponse.DocumentsInfo.Document> iter = documentos.iterator();
        while(iter.hasNext()){
            HmCircuitResponse.DocumentsInfo.Document unDocumento = iter.next();
            String unGuid = unDocumento.getGuid();
            String unNombre = unDocumento.getNombre();
            
            String parDatos = unGuid + "%" + unNombre;
            datosDocumentos.add(parDatos);
            log.debug("Dato recibido: " + parDatos);            
        }
        
        long idInstancia  = response.getIdinstancia();
        String estadoCircuito = response.getEstado();
        String motivo = response.getMotivo();
        Integer usuarioCancela = response.getNif();
                
        log.info("Id circuito respuesta recibida: " + idInstancia);
        
        //SsmRestClient client = new SsmRestClient();
        
        try {
            //SsmRestClient.actualizarSsmIdCircuit(idInstancia,datosDocumentos,estadoCircuito, motivo, usuarioCancela.toString());
        	log.info("Metodo asincrono actualizarSsmIdCircuit");
        	//client.actualizarSsmIdCircuit(idInstancia,datosDocumentos,estadoCircuito, motivo, usuarioCancela.toString());
        	llamarSSM(idInstancia,datosDocumentos,estadoCircuito,motivo, usuarioCancela.toString(), true);
        	log.info("FIN Metodo asincrono actualizarSsmIdCircuit");
        } catch (IOException e ) {           
        	log.error("Error enviado respuesta a la SSM  - id Circuto: " + idInstancia, e);
        	log.error("valor retornado a portafirmas: 1");
            return 1;
        }
        
        log.debug("valor retornado a portafirmas: 0");
        return 0;
    } 

    private Future<Void> llamarSSM(long idInstancia, ArrayList<String> datosDocumentos, String estadoCircuito, String motivo,
			String usuarioCancela, boolean espera)throws Exception {
		
    	CompletableFuture<Void> completableFuture = new CompletableFuture<>();
    	
    	ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 120, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    	    	
    	executor.submit(() ->{
    		try {
				SsmRestClient.actualizarSsmIdCircuit(idInstancia, datosDocumentos, estadoCircuito, motivo, usuarioCancela,espera);
			} catch (Exception e) {
				log.error("Error al hacer el llamado de la maquuina de estados : " + idInstancia,e);
			}
    		
    	});
    	
    	
		return completableFuture;
	}

	private static HmCircuitResponse unmarshal(String responseXml) throws Exception  {
        
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
    	
    	JAXBContext jc = JAXBContext.newInstance(HmCircuitResponse.class);    	
    	
    	HmCircuitResponse response;
		try {
			XMLInputFactory xif = XMLInputFactory2.newFactory();        
			xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
			xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);                              
			
			XMLStreamReader xsr = xif.createXMLStreamReader(in, "UTF8");       
			Unmarshaller u = jc.createUnmarshaller();        
			response = (HmCircuitResponse) u.unmarshal(xsr);
		} catch (FactoryConfigurationError | JAXBException e) {
			throw new Exception("Error unmarshal respuesta portafirmas", e);		
		}
        
       
        //Metodo antes del analisis de seguridad
    	/*
        JAXBContext jaxbContext = JAXBContext.newInstance(HmCircuitResponse.class);
        Unmarshaller u = jaxbContext.createUnmarshaller();
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setExpandEntityReferences(false);
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputSource inputSource = new InputSource(new StringReader(responseXml));
        Document document = db.parse(inputSource);        
        
        HmCircuitResponse response = (HmCircuitResponse) u.unmarshal(document);
        */

        return response;
    }
    
    private static void safeClose(InputStream in) throws IOException {
		if (in != null) {
			in.close();
		}
	}
    
    
    

}
