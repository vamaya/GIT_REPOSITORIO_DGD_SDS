package co.gov.banrep.iconecta.ssm.correspondencia.ruta;

import java.util.ArrayList;
import java.util.List;

import co.gov.banrep.iconecta.ssm.correspondencia.params.GlobalProps;
import co.gov.banrep.iconecta.ssm.correspondencia.persist.CentroDistribucion;
import co.gov.banrep.iconecta.ssm.correspondencia.persist.CentroDistribucionRepository;
import co.gov.banrep.iconecta.ssm.correspondencia.persist.PasoDistribucion;
import co.gov.banrep.iconecta.ssm.correspondencia.persist.RutaDistribucion;
import co.gov.banrep.iconecta.ssm.correspondencia.persist.RutaDistribucionRepository;
import co.gov.banrep.iconecta.ssm.correspondencia.utils.SSMUtils;

public class CalculadorRutaDer implements CaculadorRuta {

	private static final String TIPOLOGIA_RUTA_DEFAULT = "DEFAULT";

	
	@Override
	public List<Posicion> getRutaRoles(String pcrOrigen, String cddOrigenStr, String pcrDestino, String cddDestinoStr,
			String formaEntrega, String tipoEnvio, RutaDistribucionRepository repositorio,
			CentroDistribucionRepository centroDistribucionRepo, GlobalProps properties) {

		List<Posicion> ruta = new ArrayList<Posicion>();

		// Se obtiene la posicion 1 como el rol que radico
		ruta.add(new Posicion(1, pcrOrigen));

		if (formaEntrega.equalsIgnoreCase(properties.getDerObjetoFisico())
				|| formaEntrega.equalsIgnoreCase(properties.getDerObjetoElectronicoyFisico())) {

			
			if (cddOrigenStr.equals(cddDestinoStr)) {

				ruta.add(new Posicion(2, cddDestinoStr));
				ruta.add(new Posicion(3, pcrDestino));
				
				//Si no se cumple la condicion anterior se debe consultar la ruta en la BD
			} else {

				CentroDistribucion cddOrigen = centroDistribucionRepo.findByNombre(cddOrigenStr);

				if (cddOrigen == null) {
					throw new NullPointerException(
							"No se encontro centro de distribucion en la BD para el valor [" + cddOrigenStr + "]");
				}

				CentroDistribucion cddDestino = centroDistribucionRepo.findByNombre(cddDestinoStr);

				if (cddDestino == null) {
					throw new NullPointerException(
							"No se encontro centro de distribucion en la BD para el valor [" + cddDestinoStr + "]");
				}

				// Se optiene la ruta de la Base de datos a partir de los ccds origden y destino
				List<RutaDistribucion> rutaBD = repositorio
						.findByCentroDistribucionOrigenAndCentroDistribucionDestino(cddOrigen, cddDestino);

				// Si no se encuentra ruta en la Base de datos se determina que la ruta va al
				// Cdd del destino y al rol destino
				if (rutaBD == null || rutaBD.isEmpty()) {

					System.out.println("No existe ruta en la BD par los valores origen [" + cddOrigenStr
							+ "] - destino [" + cddDestinoStr + "]");
					
					ruta.add(new Posicion(2, cddOrigenStr));
					ruta.add(new Posicion(3, cddDestinoStr));
					ruta.add(new Posicion(4, pcrDestino));

				} else {

					System.out.println("Se encontro una ruta en la BD par los valores origen [" + cddOrigenStr
							+ "] - destino [" + cddDestinoStr + "]");

					RutaDistribucion rutaDistribucionBD = SSMUtils.getRuta(rutaBD, TIPOLOGIA_RUTA_DEFAULT, properties);
					
					//Se establece como segunda posicion el cdd origen					
					ruta.add(new Posicion(2, cddOrigenStr));
					
					// Se van a establecen las posiciones a partir del 3 dado que la primera y
					// segunda posicion corresponden a pcr origen y cdd origen respectivamente
					int pos = 3;

					if (rutaDistribucionBD != null) {
						List<PasoDistribucion> pasosRutaBD = rutaDistribucionBD.getPasos();

						// Se cargan los roles de la BD en el listados de pasos de la ruta
						for (PasoDistribucion paso : pasosRutaBD) {
							ruta.add(new Posicion(pos, paso.getCentroDistribucion().getNombre()));
							pos++;
						}
					}

					// En las ultimas posiciones se adicionan el cdd destino y el pcr destino
					ruta.add(new Posicion(pos, cddDestinoStr));
					ruta.add(new Posicion(pos + 1, pcrDestino));

				}
			}

		}else {
			
			//Si el envio es electronico no hay un calculo de ruta especifico
			// Se envia directaamente al pcr destino			
			
			/*
			ruta.add(new Posicion(2, cddOrigenStr));
			ruta.add(new Posicion(3, cddDestinoStr));
			ruta.add(new Posicion(4, pcrDestino));
			*/
			ruta.add(new Posicion(2, pcrDestino));
			
		}

		return ruta;
	}

}
