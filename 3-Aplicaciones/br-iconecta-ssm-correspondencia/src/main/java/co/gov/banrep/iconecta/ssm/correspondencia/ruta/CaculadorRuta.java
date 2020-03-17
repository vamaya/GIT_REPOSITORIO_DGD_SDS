package co.gov.banrep.iconecta.ssm.correspondencia.ruta;

import java.util.List;

import co.gov.banrep.iconecta.ssm.correspondencia.params.GlobalProps;
import co.gov.banrep.iconecta.ssm.correspondencia.persist.CentroDistribucionRepository;
import co.gov.banrep.iconecta.ssm.correspondencia.persist.RutaDistribucionRepository;

public interface CaculadorRuta {

	public List<Posicion> getRutaRoles(String pcrOrigien, String cddOrigen, String pcrDestino, String cddDestino,
			String formaEntrega, String tipoEnvio, RutaDistribucionRepository repositorio, CentroDistribucionRepository centroDistribucionRepository, GlobalProps properties);
	
	

}
