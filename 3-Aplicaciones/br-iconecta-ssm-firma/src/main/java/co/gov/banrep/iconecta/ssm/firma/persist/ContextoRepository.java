package co.gov.banrep.iconecta.ssm.firma.persist;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface ContextoRepository extends CrudRepository<Contexto, Long> {
	
	List<Contexto> findByIdMaquina(String idMaquina);

}
