package co.gov.banrep.iconecta.cs.workflow;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.developer.WSBindingProvider;
import com.sun.xml.ws.fault.ServerSOAPFaultException;

import co.gov.banrep.iconecta.cs.cliente.workflow.ApplicationData;
import co.gov.banrep.iconecta.cs.cliente.workflow.AttachmentData;
import co.gov.banrep.iconecta.cs.cliente.workflow.Attribute;
import co.gov.banrep.iconecta.cs.cliente.workflow.AttributeData;
import co.gov.banrep.iconecta.cs.cliente.workflow.BooleanAttribute;
import co.gov.banrep.iconecta.cs.cliente.workflow.DataValue;
import co.gov.banrep.iconecta.cs.cliente.workflow.DateAttribute;
import co.gov.banrep.iconecta.cs.cliente.workflow.FormData;
import co.gov.banrep.iconecta.cs.cliente.workflow.IntegerAttribute;
import co.gov.banrep.iconecta.cs.cliente.workflow.ItemReferenceAttribute;
import co.gov.banrep.iconecta.cs.cliente.workflow.MultiLineAttribute;
import co.gov.banrep.iconecta.cs.cliente.workflow.ProcessInstance;
import co.gov.banrep.iconecta.cs.cliente.workflow.ProcessStartData;
import co.gov.banrep.iconecta.cs.cliente.workflow.RealAttribute;
import co.gov.banrep.iconecta.cs.cliente.workflow.RowValue;
import co.gov.banrep.iconecta.cs.cliente.workflow.SetAttribute;
import co.gov.banrep.iconecta.cs.cliente.workflow.StringAttribute;
import co.gov.banrep.iconecta.cs.cliente.workflow.StringValue;
import co.gov.banrep.iconecta.cs.cliente.workflow.UserAttribute;
import co.gov.banrep.iconecta.cs.cliente.workflow.WorkflowService;
import co.gov.banrep.iconecta.cs.cliente.workflow.WorkflowService_Service;
import co.gov.banrep.iconecta.cs.workflow.utils.LlamadosWS;
import co.gov.banrep.iconecta.cs.workflow.utils.NombreServicio;
import co.gov.banrep.iconecta.cs.workflow.utils.ParametrosServicios;

public final class Workflow {

	private static String TITULO_WORKFLOW = "Notificacion";
	private static final String NAMESPACE_URI = "urn:WorkflowService.service.livelink.opentext.com";
	private static final String LOCAL_PART = "WorkflowService";
	
	//Constantes para el manejo de error
	private static final long TIEMPO_ESPERA_REINTENTO = 10000;
	private static final int CANTIDAD_REINTENTOS = 5;

	private Workflow() {
		throw new AssertionError();
	}

	/**
	 * 
	 * Inicia un workflow con los atributos pasados como parametro
	 * 
	 * @param soapAuthHeader
	 * @param mapId
	 *            id del mapa de workflow
	 * @param atributos
	 *            map que contiene los atributos de tipo atributo valor
	 * @return el id de la instancia de workflow iniciada
	 * @throws SOAPException
	 *             si se presenta un error en el llamado al servicio
	 * @throws NumberFormatException
	 *             si alguno de los atributos no corresponde al tipo de dato
	 *             esperado
	 * @throws IOException 
	 */
	public static long iniciarWorkflowConAtributos(Header soapAuthHeader, long mapId, String titulo, Map<String, ValorAtributo> atributos, String wsdl)
			throws SOAPException, NumberFormatException, NoSuchElementException, IOException {
		
		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no puede ser nulo");
		}
		
		if(mapId <= 0){
			throw new IllegalArgumentException("El parametro mapId (" + mapId + ") no es valido");
		}
		
		if(titulo == null){
			titulo = TITULO_WORKFLOW;
		}else if (titulo.isEmpty()){
			titulo = TITULO_WORKFLOW;
		}
		
		if (atributos == null){
			atributos = new HashMap<>();
		}
		
		QName qName = new QName(NAMESPACE_URI, LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		WorkflowService_Service service = new WorkflowService_Service(newURLWSDL, qName);
		WorkflowService client = service.getBasicHttpBindingWorkflowService();
		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);

		String unAtributoEtiqueta = "";
		String unAtributoValor = "";
		
		ProcessInstance processInstance;
		
		try {
			ParametrosServicios servicioParams = new ParametrosServicios().withMapId(mapId);
			ProcessStartData startData = (ProcessStartData) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_PROCESS_START_DATA);
		//ProcessStartData startData = client.getProcessStartData(mapId);
		startData.setTitle(titulo);

		Iterator<Entry<String, ValorAtributo>> iterAtributos = atributos.entrySet().iterator();

		AttributeData attributeData = (AttributeData) startData.getApplicationData().stream()
				.filter(unAttributeData -> unAttributeData instanceof AttributeData).findFirst().get();
		
		

			while (iterAtributos.hasNext()) {
				Entry<String, ValorAtributo> unAtributo = iterAtributos.next();
				
				String unTipoAtributoStr = unAtributo.getValue().getTipo().toString();	
				unAtributoEtiqueta = unAtributo.getKey();
				
				//Se valida si hay caracteres no alfanumericos en la etiqueta
				Pattern patron = Pattern.compile("[^a-zA-Z0-9_ ]");
				Matcher matcher = patron.matcher(unAtributoEtiqueta);
				final String unAtributoEtiquetaFinal = matcher.replaceAll("");
				
				unAtributoValor = unAtributo.getValue().getValor().toString();
				
				if (unTipoAtributoStr.equals(TipoAtributo.STRING.toString())) {
				
					
									
					StringAttribute unAtributoCS = (StringAttribute) attributeData.getAttributes().getAttributes()
							.stream().filter(unAttribute -> unAttribute.getDisplayName().equals(unAtributoEtiquetaFinal))
							.findFirst().get();
					
					unAtributoCS.getValues().clear();

					unAtributoCS.getValues().add(unAtributo.getValue().getValor().toString());
				} else if (unTipoAtributoStr.equals(TipoAtributo.USER.toString())) {

					UserAttribute unAtributoCS = (UserAttribute) attributeData.getAttributes().getAttributes().stream()
							.filter(unAttribute -> unAttribute.getDisplayName().equals(unAtributoEtiquetaFinal)).findFirst()
							.get();

					unAtributoCS.getValues().clear();

					unAtributoCS.getValues().add((Long) unAtributo.getValue().getValor());
				} else if (unTipoAtributoStr.equals(TipoAtributo.INTEGER.toString())) {
					
					IntegerAttribute unAtributoCS = (IntegerAttribute) attributeData.getAttributes().getAttributes().stream()
							.filter(unAttribute -> unAttribute.getDisplayName().equals(unAtributoEtiquetaFinal)).findFirst()
							.get();

					unAtributoCS.getValues().clear();

					unAtributoCS.getValues().add((Long) unAtributo.getValue().getValor());

				}else if (unTipoAtributoStr.equals(TipoAtributo.MULTILINE.toString())){
					
					MultiLineAttribute unAtributoCS = (MultiLineAttribute) attributeData.getAttributes().getAttributes()
							.stream().filter(unAttribute -> unAttribute.getDisplayName().equals(unAtributoEtiquetaFinal))
							.findFirst().get();

					unAtributoCS.getValues().add(unAtributo.getValue().getValor().toString());
					
				}
			
			}
			
			
			
			ParametrosServicios servicioParams2 = new ParametrosServicios().withListIdAdjuntos(null).withListMemberRoleId(null).withStartData(startData);
			processInstance = (ProcessInstance) LlamadosWS.llamarServicio(client, servicioParams2, NombreServicio.START_PROCESS);
			//processInstance = client.startProcess(startData, null, null);
		} catch (NumberFormatException e) {
			throw new NumberFormatException(
					"El Atributo [" + unAtributoEtiqueta + "] - [" + unAtributoValor + "] no corresponde al tipo esperado");
		} catch (NoSuchElementException e) {
			throw new NoSuchElementException(
					"El Atributo [" + unAtributoEtiqueta + "] - [" + unAtributoValor + "] no corresponde al tipo esperado");
		}catch (NullPointerException e) {
			throw new NullPointerException(
					"El valor para el  Atributo [" + unAtributoEtiqueta + "] esta vacio");
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException("Ha ocurrido un error en el Servicio de Inicio de Workflow - ORIGINAL: " + e.getMessage(), e);
		}
		
		if(processInstance == null) {
			return 0L;
		}
				
		return processInstance.getProcessID();
	}

	// TODO javadoc
	@SuppressWarnings("unchecked")
	public static <E> List<E> obtenerValoresAtributo(Header soapAuthHeader, long workid, String atributo, String wsdl)
			throws NoSuchElementException, SOAPException, IOException {

		QName qName = new QName(NAMESPACE_URI, LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		WorkflowService_Service service = new WorkflowService_Service(newURLWSDL, qName);
		WorkflowService client = service.getBasicHttpBindingWorkflowService();

		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);

		List<E> valores = new ArrayList<E>();

		try {
			ParametrosServicios servicioParams = new ParametrosServicios().withWorkId(workid);
			List<ApplicationData> datos = (List<ApplicationData>) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_PROCESS_DATA);
			//List<ApplicationData> datos = client.getProcessData(workid, workid);

			AttributeData datosAtributos = (AttributeData) datos.stream()
					.filter(unDato -> unDato instanceof AttributeData).findFirst().get();

			Attribute atributoWF = datosAtributos.getAttributes().getAttributes().stream()
					.filter(unAtributo -> unAtributo.getDisplayName().equals(atributo)).findFirst().get();

			if (atributoWF instanceof StringAttribute) {

				StringAttribute strAtributo = (StringAttribute) atributoWF;
				List<String> strValores = strAtributo.getValues();
				return valores = (List<E>) strValores;
			} else if (atributoWF instanceof UserAttribute) {

				UserAttribute userAtributo = (UserAttribute) atributoWF;
				List<Long> userValores = userAtributo.getValues();
				return valores = (List<E>) userValores;
			} else if (atributoWF instanceof IntegerAttribute) {

				IntegerAttribute intAtributo = (IntegerAttribute) atributoWF;
				List<Long> intValores = intAtributo.getValues();
				return valores = (List<E>) intValores;
			} else if (atributoWF instanceof BooleanAttribute) {

				BooleanAttribute boolAtributo = (BooleanAttribute) atributoWF;
				List<Boolean> boolValores = boolAtributo.getValues();
				return valores = (List<E>) boolValores;
			} else if (atributoWF instanceof DateAttribute) {

				DateAttribute fechaAtributo = (DateAttribute) atributoWF;
				List<XMLGregorianCalendar> fechaValores = fechaAtributo.getValidValues();
				List<Date> fechaDateValores = new ArrayList<Date>();
				Iterator<XMLGregorianCalendar> iter = fechaValores.iterator();
				while (iter.hasNext()) {
					XMLGregorianCalendar unaFechaXML = iter.next();
					fechaDateValores.add(unaFechaXML.toGregorianCalendar().getTime());
				}
				return valores = (List<E>) fechaDateValores;
			} else if (atributoWF instanceof RealAttribute) {

				RealAttribute realAtributo = (RealAttribute) atributoWF;
				List<Double> realValores = realAtributo.getValues();
				return valores = (List<E>) realValores;
			}
		} catch (NoSuchElementException e) {
			throw new NoSuchElementException(
					"No se pudo obtener el atributo :" + atributo + " de worflow id: " + workid);
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException("Error llamado a un servicio de Workflow - ORIGINAL: " + e.getMessage(), e);
		}

		return valores;

	}

	public static Long obtenerIdContenedorAdjuntos(Header soapAuthHeader, long workid, String wsdl) throws IOException {		
		
		QName qName = new QName(NAMESPACE_URI, LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		WorkflowService_Service service = new WorkflowService_Service(newURLWSDL, qName);
		WorkflowService client = service.getBasicHttpBindingWorkflowService();

		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);
		
		ParametrosServicios servicioParams = new ParametrosServicios().withWorkId(workid);
		@SuppressWarnings("unchecked")
		List<ApplicationData> datos = (List<ApplicationData>) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_PROCESS_DATA);
		//List<ApplicationData> datos = client.getProcessData(workid, workid);

		AttachmentData datosAdjuntos = (AttachmentData) datos.stream()
				.filter(unDato -> unDato instanceof AttachmentData).findFirst().get();

		long contenedor = datosAdjuntos.getContainerID();

		return contenedor;
	}
	
	//desde aqui cambios Johan
	public static long iniciarWorkflow(Header soapAuthHeader, long mapId, String titulo, String wsdl)
			throws SOAPException, NumberFormatException, NoSuchElementException, IOException {
		
		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no puede ser nulo");
		}
		
		if(mapId <= 0){
			throw new IllegalArgumentException("El parametro mapId (" + mapId + ") no es valido");
		}
		
		if(titulo == null){
			titulo = TITULO_WORKFLOW;
		}else if (titulo.isEmpty()){
			titulo = TITULO_WORKFLOW;
		}
			
		QName qName = new QName(NAMESPACE_URI, LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		WorkflowService_Service service = new WorkflowService_Service(newURLWSDL, qName);
		WorkflowService client = service.getBasicHttpBindingWorkflowService();

		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);

		String unAtributoEtiqueta = "";
		String unAtributoValor = "";
		
		ProcessInstance processInstance;
		
		try {
			
			ParametrosServicios servicioParams = new ParametrosServicios().withMapId(mapId);
			ProcessStartData startData = (ProcessStartData) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_PROCESS_START_DATA);
		//ProcessStartData startData = client.getProcessStartData(mapId);
		startData.setTitle(titulo);
		
		ParametrosServicios servicioParams3 = new ParametrosServicios().withListIdAdjuntos(null).withListMemberRoleId(null).withStartData(startData);
		processInstance = (ProcessInstance) LlamadosWS.llamarServicio(client, servicioParams3, NombreServicio.START_PROCESS);
			//processInstance = client.startProcess(startData, null, null);
		} catch (NumberFormatException e) {
			throw new NumberFormatException(
					"El Atributo [" + unAtributoEtiqueta + " - " + unAtributoValor + "] no corresponde al tipo esperado");
		} catch (NoSuchElementException e) {
			throw new NumberFormatException(
					"El Atributo [" + unAtributoEtiqueta + " - " + unAtributoValor + "] no corresponde al tipo esperado");
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException("Ha ocurrido un error en el Servicio de Inicio de Workflow - ORIGINAL: " + e.getMessage(), e);
		}

		return processInstance.getProcessID();
	}
	
	
	public static long iniciarWorkflowAtributosAdjuntos(Header soapAuthHeader, long mapId, String titulo, Map<String, ValorAtributo> atributos, List<Long> listIdAdjuntos, String wsdl)
			throws SOAPException, NumberFormatException, NoSuchElementException, IOException {
		
		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no puede ser nulo");
		}
		
		if(mapId <= 0){
			throw new IllegalArgumentException("El parametro mapId (" + mapId + ") no es valido");
		}
		
		if(titulo == null){
			titulo = TITULO_WORKFLOW;
		}else if (titulo.isEmpty()){
			titulo = TITULO_WORKFLOW;
		}
		
		if (atributos == null){
			atributos = new HashMap<>();
		}
		
		QName qName = new QName(NAMESPACE_URI, LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		WorkflowService_Service service = new WorkflowService_Service(newURLWSDL, qName);
		WorkflowService client = service.getBasicHttpBindingWorkflowService();

		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);

		String unAtributoEtiqueta = "";
		String unAtributoValor = "";
		
		ProcessInstance processInstance;
		
		try {
			
			ParametrosServicios servicioParams = new ParametrosServicios().withMapId(mapId);
			ProcessStartData startData = (ProcessStartData) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_PROCESS_START_DATA);

		//ProcessStartData startData = client.getProcessStartData(mapId);
		startData.setTitle(titulo);

		Iterator<Entry<String, ValorAtributo>> iterAtributos = atributos.entrySet().iterator();

		AttributeData attributeData = (AttributeData) startData.getApplicationData().stream()
				.filter(unAttributeData -> unAttributeData instanceof AttributeData).findFirst().get();
		
		
			while (iterAtributos.hasNext()) {
				Entry<String, ValorAtributo> unAtributo = iterAtributos.next();
				
				String unTipoAtributoStr = unAtributo.getValue().getTipo().toString();
				unAtributoEtiqueta = unAtributo.getKey();
				unAtributoValor = unAtributo.getValue().getValor().toString();
				
						
				//Se valida si hay caracteres no alfanumericos en la etiqueta
				Pattern patron = Pattern.compile("[^a-zA-Z0-9_ ]");
				Matcher matcher = patron.matcher(unAtributoEtiqueta);
				final String unAtributoEtiquetaFinal = matcher.replaceAll("");
				
				unAtributoValor = unAtributo.getValue().getValor().toString();
						
				
				if (unTipoAtributoStr.equals(TipoAtributo.STRING.toString())) {
					
					StringAttribute unAtributoCS = (StringAttribute) attributeData.getAttributes().getAttributes()
							.stream().filter(unAttribute -> unAttribute.getDisplayName().equals(unAtributoEtiquetaFinal))
							.findFirst().get();
					unAtributoCS.getValues().clear();
					
					if (unAtributo.getValue().isMultivalor()) {
						@SuppressWarnings("unchecked")
						List<Object> valores = (List<Object>) unAtributo.getValue().getValor();
						if (!valores.isEmpty()) {
							Iterator<Object> iterValores = valores.iterator();
							while (iterValores.hasNext()) {
								Object unValor = iterValores.next();
								unAtributoCS.getValues().add(unValor.toString());
							}
						}
					} else {
						unAtributoCS.getValues().add(unAtributo.getValue().getValor().toString());
					}
				} else if (unTipoAtributoStr.equals(TipoAtributo.USER.toString())) {

					UserAttribute unAtributoCS = (UserAttribute) attributeData.getAttributes().getAttributes().stream()
							.filter(unAttribute -> unAttribute.getDisplayName().equals(unAtributoEtiquetaFinal)).findFirst()
							.get();

					unAtributoCS.getValues().clear();

					unAtributoCS.getValues().add((Long) unAtributo.getValue().getValor());
				} else if (unTipoAtributoStr.equals(TipoAtributo.INTEGER.toString())) {
					
					IntegerAttribute unAtributoCS = (IntegerAttribute) attributeData.getAttributes().getAttributes().stream()
							.filter(unAttribute -> unAttribute.getDisplayName().equals(unAtributoEtiquetaFinal)).findFirst()
							.get();

					unAtributoCS.getValues().clear();

					unAtributoCS.getValues().add((Long) unAtributo.getValue().getValor());

				}else if (unTipoAtributoStr.equals(TipoAtributo.MULTILINE.toString())){
					
					MultiLineAttribute unAtributoCS = (MultiLineAttribute) attributeData.getAttributes().getAttributes()
							.stream().filter(unAttribute -> unAttribute.getDisplayName().equals(unAtributoEtiquetaFinal))
							.findFirst().get();

					unAtributoCS.getValues().add(unAtributo.getValue().getValor().toString());
					
				}else if (unTipoAtributoStr.equals(TipoAtributo.ITEM_REFERENCE.toString())){
					
					ItemReferenceAttribute unAtributoCS = (ItemReferenceAttribute) attributeData.getAttributes().getAttributes()
							.stream().filter(unAttribute -> unAttribute.getDisplayName().equals(unAtributoEtiquetaFinal))
							.findFirst().get();
					unAtributoCS.getValues().clear();
					
					if (unAtributo.getValue().isMultivalor()) {
						@SuppressWarnings("unchecked")
						List<Object> valores = (List<Object>) unAtributo.getValue().getValor();
						if (!valores.isEmpty()) {
							Iterator<Object> iterValores = valores.iterator();
							while (iterValores.hasNext()) {
								Long unValor = (Long) iterValores.next();
								unAtributoCS.getValues().add(unValor);
							}
						}

					} else {
						Long valor = (Long) unAtributo.getValue().getValor();
						unAtributoCS.getValues().add(valor);
					}
									
					
				}
			
			}
			
			ParametrosServicios servicioParams4 = new ParametrosServicios().withListIdAdjuntos(listIdAdjuntos).withListMemberRoleId(null).withStartData(startData);
			processInstance = (ProcessInstance) LlamadosWS.llamarServicio(client, servicioParams4, NombreServicio.START_PROCESS);
			//processInstance = client.startProcess(startData, listIdAdjuntos, null);
		} catch (NumberFormatException e) {
			throw new NumberFormatException(
					"El Atributo [" + unAtributoEtiqueta + " - " + unAtributoValor + "] no corresponde al tipo esperado");
		} catch (NoSuchElementException e) {
			throw new NoSuchElementException(
					"El Atributo [" + unAtributoEtiqueta + " - " + unAtributoValor + "] no fue encontrado");
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException("Ha ocurrido un error en el Servicio de Inicio de Workflow - ORIGINAL: " + e.getMessage(), e);
		}

		return processInstance.getProcessID();
	}
	
	
	public static List<String> obtenerValoresFormulario(Header soapAuthHeader, long workid, String atributo, String wsdl)
			throws NoSuchElementException, SOAPException, IOException {

		QName qName = new QName(NAMESPACE_URI, LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		WorkflowService_Service service = new WorkflowService_Service(newURLWSDL, qName);
		WorkflowService client = service.getBasicHttpBindingWorkflowService();

		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);

		
		List<String> listaFinal = new ArrayList<>();

		try {
			
			ParametrosServicios servicioParams = new ParametrosServicios().withWorkId(workid);
			@SuppressWarnings("unchecked")
			List<ApplicationData> datos = (List<ApplicationData>) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_PROCESS_DATA);
			//List<ApplicationData> datos = client.getProcessData(workid, workid);
			

			FormData datosForm = (FormData) datos.stream()
					.filter(unDato -> unDato instanceof  FormData).findFirst().get();
			
			
			SetAttribute atributoForm = (SetAttribute) datosForm.getForms().get(0).getData().getAttributes().stream()
					.filter(unAtributo -> unAtributo.getDisplayName().equals("Informaci�n RDI")).findFirst().get();
			
			
			List<RowValue> listFilaValores=atributoForm.getValues();
			
			for (RowValue rowValue : listFilaValores) {
				
				DataValue valor = rowValue.getValues().stream()
						.filter(unCampo -> unCampo.getDescription().equals("Dependencia Destino")).findFirst().get();
				
				if (valor instanceof StringValue) {
					StringValue unStringValue = (StringValue) valor;
					String valorFinal=unStringValue.getValues().get(0);
					 listaFinal.add(valorFinal);
				}
				
				
			}
			
			
		} catch (NoSuchElementException e) {
			throw new NoSuchElementException(
					"No se pudo obtener el atributo :" + atributo + " de worflow id: " + workid);
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException("Error llamado a un servicio de Workflow - ORIGINAL: " + e.getMessage(), e);
		}

		return listaFinal;

	}
	
	
	public static List<String> obtenerValoresFormularioPorSeccionAtributo(Header soapAuthHeader, long workid,  String seccion, String atributo, String wsdl)
			throws NoSuchElementException, SOAPException, IOException, InterruptedException {

		QName qName = new QName(NAMESPACE_URI, LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		WorkflowService_Service service = new WorkflowService_Service(newURLWSDL, qName);
		WorkflowService client = service.getBasicHttpBindingWorkflowService();

		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);

		
		List<String> listaFinal = new ArrayList<>();
		
		boolean error = true;
		int i = 1;

		try {
			
			ParametrosServicios servicioParams = new ParametrosServicios().withWorkId(workid);
			@SuppressWarnings("unchecked")
			List<ApplicationData> datos = (List<ApplicationData>) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_PROCESS_DATA);
			//List<ApplicationData> datos = client.getProcessData(workid, workid);
			

			FormData datosForm = (FormData) datos.stream()
					.filter(unDato -> unDato instanceof  FormData).findFirst().get();
			
			
			SetAttribute atributoForm = (SetAttribute) datosForm.getForms().get(0).getData().getAttributes().stream()
					.filter(unAtributo -> unAtributo.getDisplayName().equals(seccion)).findFirst().get();
			
			
			List<RowValue> listFilaValores=atributoForm.getValues();
			
			for (RowValue rowValue : listFilaValores) {
				
				DataValue valor = null;
				while (error == true && i <= CANTIDAD_REINTENTOS) {

					try {				
						valor = rowValue.getValues().stream()
								.filter(unCampo -> unCampo.getDescription().equals(atributo)).findFirst().get();
						error = false;
					}catch (NoSuchElementException e) {
						error = true;
						i++;
						if(i > CANTIDAD_REINTENTOS) {
							System.out.println("Fallaron todos los reintentos de ejecucion del metodo getVersionContents");
						}
						e.printStackTrace();				
						Thread.sleep(TIEMPO_ESPERA_REINTENTO);
					}
				}	
				
				if (valor instanceof StringValue) {
					StringValue unStringValue = (StringValue) valor;
					// String valorFinal=unStringValue.getValues().get(0);

					String valorFinal = "";

					List<String> values = unStringValue.getValues();
					if (!values.isEmpty()) {
						valorFinal = values.get(0);
					} else {
						throw new NoSuchElementException("No hay valor asociado a el atributo [" + unStringValue + "]");
					}
					
					 listaFinal.add(valorFinal);
				}
				
			}
			
		} catch (NoSuchElementException e) {
			throw new NoSuchElementException(
					"No se pudo obtener el atributo :" + atributo + " de worflow id: " + workid);
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException("Error llamado a un servicio de Workflow - ORIGINAL: " + e.getMessage(), e);
		}

		return listaFinal;

	}
	
	
	public static <E> List<RowValue> obtenerValoresFormularioPorSeccion(Header soapAuthHeader, long workid, String atributo, String wsdl)
			throws NoSuchElementException, SOAPException, IOException  {

		QName qName = new QName(NAMESPACE_URI, LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		WorkflowService_Service service = new WorkflowService_Service(newURLWSDL, qName);
		WorkflowService client = service.getBasicHttpBindingWorkflowService();

		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);
		
		List<RowValue> listFilaValores;


		try {
			
			ParametrosServicios servicioParams = new ParametrosServicios().withWorkId(workid);
			@SuppressWarnings("unchecked")
			List<ApplicationData> datos = (List<ApplicationData>) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_PROCESS_DATA);
			//List<ApplicationData> datos = client.getProcessData(workid, workid);
			

			FormData datosForm = (FormData) datos.stream()
					.filter(unDato -> unDato instanceof  FormData).findFirst().get();
		
			
			SetAttribute atributoForm = (SetAttribute) datosForm.getForms().get(0).getData().getAttributes().stream()
					.filter(unAtributo -> unAtributo.getDisplayName().equals(atributo)).findFirst().get();
			
			
			listFilaValores=atributoForm.getValues();
			
			 
		} catch (NoSuchElementException e) {
			throw new NoSuchElementException(
					"No se pudo obtener el atributo :" + atributo + " de worflow id: " + workid);
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException("Error llamado a un servicio de Workflow - ORIGINAL: " + e.getMessage(), e);
		}

		return listFilaValores;

	}
	
	public static String obtenerValorFormulario(Header soapAuthHeader, long workid, String atributo, String wsdl)
			throws NoSuchElementException, SOAPException, IOException {

		QName qName = new QName(NAMESPACE_URI, LOCAL_PART);
		URL newURLWSDL = new URL(wsdl);

		WorkflowService_Service service = new WorkflowService_Service(newURLWSDL, qName);
		WorkflowService client = service.getBasicHttpBindingWorkflowService();

		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);
		
		String valor="";

		try {
			
			ParametrosServicios servicioParams = new ParametrosServicios().withWorkId(workid);
			@SuppressWarnings("unchecked")
			List<ApplicationData> datos = (List<ApplicationData>) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.GET_PROCESS_DATA);
			//List<ApplicationData> datos = client.getProcessData(workid, workid);
			

			FormData datosForm = (FormData) datos.stream()
					.filter(unDato -> unDato instanceof  FormData).findFirst().get();
			
			
			StringAttribute atributoForm = (StringAttribute) datosForm.getForms().get(0).getData().getAttributes().stream()
					.filter(unAtributo -> unAtributo.getDisplayName().equals(atributo)).findFirst().get();
			
			valor=atributoForm.getValues().get(0);
			
		} catch (NoSuchElementException e) {
			throw new NoSuchElementException(
					"No se pudo obtener el atributo :" + atributo + " de worflow id: " + workid);
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException("Error llamado a un servicio de Workflow - ORIGINAL: " + e.getMessage(), e);
		}

		return valor;

	}
	
}
