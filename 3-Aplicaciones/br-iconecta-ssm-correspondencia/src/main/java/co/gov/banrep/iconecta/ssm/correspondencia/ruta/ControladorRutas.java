package co.gov.banrep.iconecta.ssm.correspondencia.ruta;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import co.gov.banrep.iconecta.ssm.correspondencia.utils.Consts;

public class ControladorRutas {
	
	List<RutaHandler> rutas = new ArrayList<RutaHandler>();

	public List<RutaHandler> getRutas() {
		return rutas;
	}

	public void setRutas(List<RutaHandler> rutas) {
		this.rutas = rutas;
	}
	
	public RutaHandler getRutaByWorkId(long workId) {
		RutaHandler ruta = new RutaHandler();
		Optional<RutaHandler> unaRutaOpt = this.rutas.stream().filter(unaRuta -> unaRuta.getWorkIdDistribucion() == workId).findFirst();
		if(unaRutaOpt.isPresent()) {
			ruta = unaRutaOpt.get();			
		}else {
			throw new NoSuchElementException("No se encuntro una ruta para el id de distribucion [" + workId + "]" );
		}
		
		return ruta;
	}
	
	public void addRuta(long workIdDistribucion, List<Posicion> rutaList) {
		RutaHandler ruta = new RutaHandler();
		ruta.setWorkIdDistribucion(workIdDistribucion);
		
		/*
		for (int i = 1; i <= roles.size(); i++) {
			ruta.setPosicion(i, roles.get(i - 1));
		}
		*/
		ruta.setPosiciones(rutaList);
		
		this.rutas.add(ruta);

	}
	
	public void addRuta(long workIdDistribucion, List<Posicion> rutaList, Posicion posicionInicial, boolean isFisico) {
		RutaHandler ruta = new RutaHandler();
		ruta.setWorkIdDistribucion(workIdDistribucion);
				
		ruta.setPosiciones(rutaList);
		
		ruta.setAnterior(rutaList.get(0));
		ruta.setActual(posicionInicial);
		
		ruta.setEnvioFisico(isFisico);
		
		this.rutas.add(ruta);

	}
	
	public int getCantidadRutas() {
		return this.rutas.size();
	}

	public Posicion getSiguientePaso(long workidInstancia, String resultado) {
		
		RutaHandler ruta = getRutaByWorkId(workidInstancia);
		
		Posicion posicion = null;
		
		if( !resultado.equalsIgnoreCase(Consts.RUTA_RECHAZADA) ){
			posicion = ruta.getSiguientePos();
		}else {
			ruta.setFinalizo(true);
		}
		
		return posicion;
	}
	
	public boolean isAlgunaDistribucionPendiente() {
		Optional<RutaHandler> rutaOpt = rutas.stream().filter(unaRuta -> unaRuta.isFinalizo() == false).findFirst();

		if (rutaOpt.isPresent()) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isEnvioFisicoRuta(long workidInstancia) {
		
		RutaHandler ruta = getRutaByWorkId(workidInstancia);		
		
		return ruta.isEnvioFisico();
	}

}
