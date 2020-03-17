package co.gov.banrep.iconecta.ssm.correspondencia;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.soap.SOAPException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import co.gov.banrep.iconecta.cs.autenticacion.AutenticacionCS;
import co.gov.banrep.iconecta.ssm.correspondencia.entity.FirmanteSSM;
import co.gov.banrep.iconecta.ssm.correspondencia.params.GlobalProps;
import co.gov.banrep.iconecta.ssm.correspondencia.params.OfficeDocumentoProps;
import co.gov.banrep.iconecta.ssm.correspondencia.params.WsdlProps;
import co.gov.banrep.iconecta.ssm.correspondencia.persist.Mensaje;
import co.gov.banrep.iconecta.ssm.correspondencia.persist.MensajeRepository;
import co.gov.banrep.iconecta.ssm.correspondencia.utils.SSMUtils;

@Component   //anotación para correr la clase primero
@Scope(value=WebApplicationContext.SCOPE_APPLICATION)
public class ContextRefreshedListener implements ApplicationListener<ContextRefreshedEvent>{
	
	//private final Logger log = LoggerFactory.getLogger(ContextRefreshedListener.class);
	private final Logger log = LoggerFactory.getLogger(ContextRefreshedListener.class);
	
	private List<FirmanteSSM> listaFirmantes; 
	
	@Autowired
	private AutenticacionCS autenticacionCS;
	
	@Autowired
	private WsdlProps wsdlsProps;
	
	@Autowired
	private MensajeRepository mensajeRepository;
	
	public Iterable<Mensaje> mensajes;
	
	public Map<String,String> mapPropsOfficeDoc;
	
	@Autowired
	private OfficeDocumentoProps officeDocumentoProps;
	
	@Autowired
	private GlobalProps globalProperties;
	
	public void LoadFirmantes() throws IOException, SOAPException {
		String raizLog = "Iniciando APP - Context Refresh Listener - LoadFirmantes - ";
		listaFirmantes = SSMUtils.obtenerFirmantesReporte(raizLog, autenticacionCS, wsdlsProps, globalProperties);
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		try {
			log.info("Iniciando APP Maquina Estados Correspondencia - BanRep");
			autenticacionCS.authenticate();
			cargarMensajes();
			cargarPropiedadesOfficeDocumento();
			
			
		log.info("Autenticacion Aplicacion - Content Server exitosa");
		} catch (SOAPException | IOException e) {			
			log.error("ErrorCorrespondencia de Autenticación entre la Aplicacion y Content Server");
			log.error("Revise los datos de las propiedades content dentro de los archivos de properties", e);
			throw new RuntimeException();			
		}
		//restoreMachines();
		
		try {
			log.info("Obteniendo datos Firmantes");
			LoadFirmantes();
		} catch (IOException | SOAPException e) {
			log.error("Error Obteniendo datos Firmantes", e);
			throw new RuntimeException();
		}
	}
	
	private void cargarMensajes() {
		mensajes=mensajeRepository.findAll();
	}
	
	private void cargarPropiedadesOfficeDocumento() {
		
		mapPropsOfficeDoc=new HashMap<>();
		
		mapPropsOfficeDoc.put("numeroRadicado", officeDocumentoProps.getNumeroRadicado()); 
		mapPropsOfficeDoc.put("fechaRadicado", officeDocumentoProps.getFechaRadicado()); 
		mapPropsOfficeDoc.put("nombreDestino", officeDocumentoProps.getNombreDestino()); 
		mapPropsOfficeDoc.put("cargoDestino", officeDocumentoProps.getCargoDestino()); 
		mapPropsOfficeDoc.put("dependenciaDestino", officeDocumentoProps.getDependenciaDestino()); 
		mapPropsOfficeDoc.put("pcrDestino", officeDocumentoProps.getPcrDestino()); 
		mapPropsOfficeDoc.put("ciudadDestino", officeDocumentoProps.getCiudadDestino()); 
		mapPropsOfficeDoc.put("referenciaDestino", officeDocumentoProps.getReferenciaDestino()); 
		mapPropsOfficeDoc.put("asunto", officeDocumentoProps.getAsunto()); 
		mapPropsOfficeDoc.put("nombreFirmante", officeDocumentoProps.getNombreFirmante()); 
		mapPropsOfficeDoc.put("cargoFirmante", officeDocumentoProps.getCargoFirmante()); 
		mapPropsOfficeDoc.put("dependenciaFirmante", officeDocumentoProps.getDependenciaFirmante()); 
		mapPropsOfficeDoc.put("idFirmante", officeDocumentoProps.getIdFirmante()); 
		mapPropsOfficeDoc.put("sgrFirmante", officeDocumentoProps.getSgrFirmante()); 
		mapPropsOfficeDoc.put("firmaFirmante", officeDocumentoProps.getFirmaFirmante()); 
		mapPropsOfficeDoc.put("copias", officeDocumentoProps.getCopias()); 
		mapPropsOfficeDoc.put("pcrCopias", officeDocumentoProps.getPcrCopias()); 
		mapPropsOfficeDoc.put("tipologiaPlantilla", officeDocumentoProps.getTipologiaPlantilla()); 
		mapPropsOfficeDoc.put("tipoPlantilla", officeDocumentoProps.getTipoPlantilla()); 
		mapPropsOfficeDoc.put("personalizadaPlantilla", officeDocumentoProps.getPersonalizadaPlantilla()); 
		mapPropsOfficeDoc.put("idiomaPlantilla", officeDocumentoProps.getIdiomaPlantilla()); 
		mapPropsOfficeDoc.put("tipoEnvioPlantilla", officeDocumentoProps.getTipoEnvioPlantilla()); 
		mapPropsOfficeDoc.put("versionPlantilla", officeDocumentoProps.getVersionPlantilla()); 
		mapPropsOfficeDoc.put("anexosFisicos", officeDocumentoProps.getAnexosFisicos()); 
		mapPropsOfficeDoc.put("anexosElectronicos", officeDocumentoProps.getAnexosElectronicos()); 
		mapPropsOfficeDoc.put("tipologiaPlantillaCarta", officeDocumentoProps.getTipologiaPlantillaCarta()); 
		mapPropsOfficeDoc.put("esFondoIndependiente", officeDocumentoProps.getEsFondoIndependiente());
		mapPropsOfficeDoc.put("esCorreoCertificado", officeDocumentoProps.getEsCorreoCertificado());
		mapPropsOfficeDoc.put("esImpresionArea", officeDocumentoProps.getEsImpresionArea());
		mapPropsOfficeDoc.put("correoCopia", officeDocumentoProps.getCorreoCopia());
		mapPropsOfficeDoc.put("nombrePCR", officeDocumentoProps.getNombrePCR());
		mapPropsOfficeDoc.put("nombreCDD", officeDocumentoProps.getNombreCDD());
		mapPropsOfficeDoc.put("idPCR", officeDocumentoProps.getIdPCR());
		mapPropsOfficeDoc.put("idCDD", officeDocumentoProps.getIdCDD());
		mapPropsOfficeDoc.put("SegundoRemitente", officeDocumentoProps.getSegundoRemitente());
		
		
	}
	
	
	public Iterable<Mensaje> getMensajes() {
		return mensajes;
	}

	public void setMensajes(Iterable<Mensaje> mensajes) {
		this.mensajes = mensajes;
	}

	public Map<String, String> getMapPropsOfficeDoc() {
		return mapPropsOfficeDoc;
	}

	public void setMapPropsOfficeDoc(Map<String, String> mapPropsOfficeDoc) {
		this.mapPropsOfficeDoc = mapPropsOfficeDoc;
	}

	
	public List<FirmanteSSM> getListaFirmantes() {
		return listaFirmantes;
	}

	public void setListaFirmantes(List<FirmanteSSM> listaFirmantes) {
		this.listaFirmantes = listaFirmantes;
	}

}
