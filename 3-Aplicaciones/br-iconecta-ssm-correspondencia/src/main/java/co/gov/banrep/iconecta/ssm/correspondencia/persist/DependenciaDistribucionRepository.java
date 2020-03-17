package co.gov.banrep.iconecta.ssm.correspondencia.persist;

import org.springframework.data.repository.CrudRepository;

public interface DependenciaDistribucionRepository extends CrudRepository<DependenciaDistribucion, Long>  { 

	DependenciaDistribucion findById(long id);
	
	DependenciaDistribucion findByNombre(String nombre);
}
