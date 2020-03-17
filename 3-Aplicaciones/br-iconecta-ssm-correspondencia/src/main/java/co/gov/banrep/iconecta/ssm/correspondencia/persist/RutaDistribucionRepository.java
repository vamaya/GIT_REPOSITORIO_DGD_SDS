package co.gov.banrep.iconecta.ssm.correspondencia.persist;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface RutaDistribucionRepository extends CrudRepository<RutaDistribucion, Long>  {
	
	List<RutaDistribucion> findByCentroDistribucionOrigenAndCentroDistribucionDestino(CentroDistribucion centroDistribucionOrigen, CentroDistribucion centroDistribucionDestino);
	
	RutaDistribucion findById(long id);

}
 