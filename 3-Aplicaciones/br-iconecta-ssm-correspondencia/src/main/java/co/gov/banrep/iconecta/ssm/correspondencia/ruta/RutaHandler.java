package co.gov.banrep.iconecta.ssm.correspondencia.ruta;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RutaHandler {
	
	private long workIdDistribucion;	
	private List<Posicion> posiciones = new ArrayList<Posicion>();
	private Posicion actual;
	private Posicion anterior;
	private boolean finalizo = false;
	private boolean isEnvioFisico=false;
	
	public RutaHandler() {}
	
	
	public long getWorkIdDistribucion() {
		return workIdDistribucion;
	}
	public void setWorkIdDistribucion(long workIdDistribucion) {
		this.workIdDistribucion = workIdDistribucion;
	}	
	public Posicion getActual() {
		return actual;
	}
	public void setActual(Posicion actual) {
		this.actual = actual;
	}
	public Posicion getAnterior() {
		return anterior;
	}
	public void setAnterior(Posicion anterior) {
		this.anterior = anterior;
	}
	
	public boolean isFinalizo() {
		return finalizo;
	}
	
	
	public void setFinalizo(boolean finalizo) {
		this.finalizo = finalizo;
	}


	/*
	public void setSiguiente(Posicion siguiente) {
		this.siguiente = siguiente;
	}
	*/
	public List<Posicion> getPosiciones() {
		return posiciones;
	}
	public void setPosiciones(List<Posicion> posiciones) {
		this.posiciones = posiciones;
	}
	
	public void setPosicion(int posicion, String rol) {
		this.posiciones.add(new Posicion(posicion, rol));
	}
	
	public Posicion getSiguientePos() {
		
		int indiceActual = actual.getPosicion();
		
		System.out.println("Indice actual [" + indiceActual + "]");
		
		Optional<Posicion> optPos = posiciones.stream().filter(unPos -> unPos.getPosicion() == (indiceActual +1)).findFirst();
		
		Posicion pos = null;
		
		if(optPos.isPresent()) {
			System.out.println("Encontro posicion");
			pos = optPos.get();
		}else {
			finalizo = true;
			return null;
		}
		
		System.out.println("Pos [" + pos.getPosicion() + "] - [" +pos.getRol() + "]");
		
		anterior = actual;
		actual = pos;
		
		System.out.println("anterior [" + anterior.getPosicion() + "] - [" +anterior.getRol() + "]");
		System.out.println("actual [" + actual.getPosicion() + "] - [" +actual.getRol() + "]");
		
		return actual;
	}
	
	@Override
	public String toString() {
		String rutaCompleta = "";
		
		for (Posicion posicion: this.posiciones) {
			rutaCompleta = rutaCompleta +  "[" + posicion.getPosicion() + "]-[" + posicion.getRol() + "] ";
		}
				
		return "workid distribucion [" + workIdDistribucion + "] - " + rutaCompleta;
	}


	public boolean isEnvioFisico() {
		return isEnvioFisico;
	}


	public void setEnvioFisico(boolean isEnvioFisico) {
		this.isEnvioFisico = isEnvioFisico;
	}	

}
