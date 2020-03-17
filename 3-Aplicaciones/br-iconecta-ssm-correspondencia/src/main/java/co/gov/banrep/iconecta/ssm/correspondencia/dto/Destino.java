package co.gov.banrep.iconecta.ssm.correspondencia.dto;

public class Destino {
	
	private String nombre;
	private String cargo;
	private String dependenciaDestino;
	private String pcrDestino;
	private String cddDestino;
	private String descripcionObjeto;
	private Long cantidad;
	private String pais;
	private String ciudad;
	private String direccion;
	private String entidad;
	private String telefono;
	private String formaEntregaDer;
	
	
		
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getCargo() {
		return cargo;
	}
	public void setCargo(String cargo) {
		this.cargo = cargo;
	}
	public String getDependenciaDestino() {
		return dependenciaDestino;
	}
	public void setDependenciaDestino(String dependenciaDestino) {
		this.dependenciaDestino = dependenciaDestino;
	}
	public String getPcrDestino() {
		return pcrDestino;
	}
	public void setPcrDestino(String pcrDestino) {
		this.pcrDestino = pcrDestino;
	}
	public String getCddDestino() {
		return cddDestino;
	}
	public void setCddDestino(String cddDestino) {
		this.cddDestino = cddDestino;
	}
	public String getDescripcionObjeto() {
		return descripcionObjeto;
	}
	public void setDescripcionObjeto(String descripcionObjeto) {
		this.descripcionObjeto = descripcionObjeto;
	}
	public Long getCantidad() {
		return cantidad;
	}
	public void setCantidad(Long cantidad) {
		this.cantidad = cantidad;
	}
	public String getPais() {
		return pais;
	}
	public void setPais(String pais) {
		this.pais = pais;
	}
	public String getCiudad() {
		return ciudad;
	}
	public void setCiudad(String ciudad) {
		this.ciudad = ciudad;
	}
	public String getDireccion() {
		return direccion;
	}
	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}
	public String getEntidad() {
		return entidad;
	}
	public void setEntidad(String entidad) {
		this.entidad = entidad;
	}
	public String getTelefono() {
		return telefono;
	}
	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}
	public String getFormaEntregaDer() {
		return formaEntregaDer;
	}
	public void setFormaEntregaDer(String formaEntregaDer) {
		this.formaEntregaDer = formaEntregaDer;
	}
	
public Destino getDestino(Destino d, String dependenciaDestino) {
		
		Destino des = new Destino();
		des.setNombre(d.nombre);
		des.setCargo(d.getCargo());
		des.setDependenciaDestino(dependenciaDestino);
		des.setPcrDestino(d.getPcrDestino()); 
		des.setCddDestino(d.getCddDestino());
		des.setDescripcionObjeto(d.getDescripcionObjeto());
		des.setCantidad(d.getCantidad());
		des.setPais(d.getPais());
		des.setCiudad(d.getCiudad());
		des.setDireccion(d.getDireccion());
		des.setEntidad(d.getEntidad());
		des.setTelefono(d.getTelefono());
		des.setFormaEntregaDer(d.getFormaEntregaDer());
		
		return des;
	}
}
