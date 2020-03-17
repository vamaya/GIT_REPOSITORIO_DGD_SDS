package co.gov.banrep.iconecta.ssm.correspondencia.persist;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface PruebaEntregaRepository extends CrudRepository<PruebaEntrega, Long> {
	
	List<PruebaEntrega> findByIdMaquinaAndNumeroRadicadoAndDestino(String idMaquina, String numeroRadicado, String destino);
	
	List<PruebaEntrega> findByIdMaquinaAndNumeroRadicado(String idMaquina, String numeroRadicado);

}
