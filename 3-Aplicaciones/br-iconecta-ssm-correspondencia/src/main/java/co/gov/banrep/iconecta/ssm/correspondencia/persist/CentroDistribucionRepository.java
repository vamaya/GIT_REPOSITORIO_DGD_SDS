package co.gov.banrep.iconecta.ssm.correspondencia.persist;

import org.springframework.data.repository.CrudRepository;

public interface CentroDistribucionRepository extends CrudRepository<CentroDistribucion, Long>  {
	
	CentroDistribucion findById(long id);
	
	CentroDistribucion findByNombre(String nombre);
	

}
