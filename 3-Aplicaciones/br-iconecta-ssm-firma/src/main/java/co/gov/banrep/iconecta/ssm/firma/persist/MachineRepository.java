package co.gov.banrep.iconecta.ssm.firma.persist;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface MachineRepository extends CrudRepository<Maquina, Long> {
	
	List<Maquina> findByIdMaquina(String idMaquina);
	
	List<Maquina> findByIdCircuito(int idCircuito);

}
