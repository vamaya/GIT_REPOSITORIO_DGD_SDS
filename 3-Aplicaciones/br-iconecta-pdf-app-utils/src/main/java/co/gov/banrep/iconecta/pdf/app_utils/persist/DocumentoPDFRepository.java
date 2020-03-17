package co.gov.banrep.iconecta.pdf.app_utils.persist;

import org.springframework.data.repository.CrudRepository;


public interface DocumentoPDFRepository extends CrudRepository<DocumentoPDF, Long>{
	
	DocumentoPDF findByIdworkflowAndIdDocumentoAndEstado(long idworkflow, long idDocumento, String estado);

	DocumentoPDF findByIdworkflowAndIdDocumento(long idWorkflow, long idDocumentoOriginal);

}
