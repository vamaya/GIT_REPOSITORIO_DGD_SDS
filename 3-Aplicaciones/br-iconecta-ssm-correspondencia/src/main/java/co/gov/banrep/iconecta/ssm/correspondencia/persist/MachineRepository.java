package co.gov.banrep.iconecta.ssm.correspondencia.persist;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface MachineRepository extends CrudRepository<MaquinaCorrespondencia, Long> {
	
	List<MaquinaCorrespondencia> findByIdMaquina(String idMaquina);
	
}
