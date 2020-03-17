package co.gov.banrep.iconecta.pdf.app_utils.params;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("wsdlurl")
@Component
public class WsdlProps {
		
	@NotNull
	private String documento;
	@NotNull
	private String usuario;
	@NotNull
	private String workflow;
//	@NotNull
//	private String circuito;
	
	
	public String getDocumento() {
		return documento;
	}
	public void setDocumento(String documento) {
		this.documento = documento;
	}
	public String getUsuario() {
		return usuario;
	}
	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}
	public String getWorkflow() {
		return workflow;
	}
	public void setWorkflow(String workflow) {
		this.workflow = workflow;
	}
//	public void setCircuito(String circuito) {
//		this.circuito = circuito;
//	}
//	public String getCircuito() {
//		return circuito;
//	}
//	
}
