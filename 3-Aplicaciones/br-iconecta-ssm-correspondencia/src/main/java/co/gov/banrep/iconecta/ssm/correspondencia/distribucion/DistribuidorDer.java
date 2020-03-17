package co.gov.banrep.iconecta.ssm.correspondencia.distribucion;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.soap.SOAPException;

import com.sun.xml.ws.api.message.Header;

import co.gov.banrep.iconecta.cs.workflow.ValorAtributo;
import co.gov.banrep.iconecta.cs.workflow.Workflow;
import co.gov.banrep.iconecta.ssm.correspondencia.params.WorkflowProps;
import co.gov.banrep.iconecta.ssm.correspondencia.params.WsdlProps;

public class DistribuidorDer implements Distribuidor{

	/*
	@Autowired
	private Workflow workflow;
	*/
	
	@Override
	public void distribuirHilo(Header header ,String rolInicial, WorkflowProps workflowProps, WsdlProps wsdlProps,
			String formaEntrega, Map<String, ValorAtributo> atributos, String titulo, List<Long> listIdDocsAdjuntos) throws SOAPException, IOException {
		
		
		long mapId = workflowProps.getDistribucionDer().getId();
			
		Workflow.iniciarWorkflowAtributosAdjuntos(header, mapId, titulo, atributos, listIdDocsAdjuntos, wsdlProps.getWorkflow());
		
	}

}
