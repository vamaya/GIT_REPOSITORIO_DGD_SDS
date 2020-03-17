package co.gov.banrep.iconecta.ssm.firma;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.soap.SOAPException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.sun.xml.ws.api.message.Header;

import co.gov.banrep.iconecta.cs.autenticacion.AutenticacionCS;
import co.gov.banrep.iconecta.cs.cliente.document.ReportResult;
import co.gov.banrep.iconecta.cs.documento.Reporte;
import co.gov.banrep.iconecta.ssm.firma.entity.FirmanteSSM;
import co.gov.banrep.iconecta.ssm.firma.params.GlobalProps;
import co.gov.banrep.iconecta.ssm.firma.params.WsdlProps;
import co.gov.banrep.iconecta.ssm.firma.utils.SSMUtils;

/**
 * Maneja el evento de autenticacion al momento de inciar la maquina
 * 
 *  @author <a href="mailto:jjrojassa@banrep.gov.co">John Jairo Rojas S.</a>
 *
 */
@Component
public class ContextRefreshedListener implements ApplicationListener<ContextRefreshedEvent>{
	
	Logger log = LoggerFactory.getLogger(this.getClass());
	
	private List<FirmanteSSM> firmantes;
		
	@Autowired
	private AutenticacionCS autenticacionCS;
	
	@Autowired
	private WsdlProps wsdlProps;
	
	@Autowired
	private GlobalProps globalProperties;
	
	
	public void loadFirmantes(Header header, String wsdl) throws IOException, SOAPException{
		
		ReportResult resultado = Reporte.ejecutarReporteSinParametros(header, globalProperties.getIdConsultaFirmantes(), wsdl);
		//ReportResult resultado = Reporte.ejecutarReporteSinParametros(header, 4894097, wsdl);
		
		firmantes = new ArrayList<>();
		
		SSMUtils.obtenerFirmantesReporte(firmantes, resultado);		
	}
	
	public List<FirmanteSSM> getFirmantes() {
		return firmantes;
	}

	public void setFirmantes(List<FirmanteSSM> firmantes) {
		this.firmantes = firmantes;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
				
		try {
			autenticacionCS.authenticate();
			log.info("Autenticacion Aplicacion - Content Server exitosa");
		} catch (SOAPException | IOException e) {			
			log.error("Error de Autenticación entre la Aplicacion y Content Server");
			log.error("Revise los datos de las propiedades content dentro de los archivos de properties", e);
			throw new RuntimeException();
		}
		
		try {
			log.info("Obteniendo datos Firmantes");
			loadFirmantes(autenticacionCS.getAdminSoapHeader(),wsdlProps.getDocumento());
		} catch (IOException | SOAPException e) {
			log.error("Error Obteniendo datos Firmantes", e);			
			throw new RuntimeException();
		}
	}
}
