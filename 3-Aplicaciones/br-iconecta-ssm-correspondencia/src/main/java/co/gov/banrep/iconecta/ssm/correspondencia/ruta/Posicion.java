package co.gov.banrep.iconecta.ssm.correspondencia.ruta;

public class Posicion {
	private int posicion;
	private String rol;
	
		
	public Posicion() {		
	}
	public Posicion(int posicion, String rol) {
		super();
		this.posicion = posicion;
		this.rol = rol;
	}
	public int getPosicion() {
		return posicion;
	}
	public void setPosicion(int posicion) {
		this.posicion = posicion;
	}
	public String getRol() {
		return rol;
	}
	public void setRol(String rol) {
		this.rol = rol;
	}
	
	

}
