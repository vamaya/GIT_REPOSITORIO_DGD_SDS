package co.gov.banrep.iconecta.ssm.correspondencia.entity;

public class FirmanteSSM {

	private long seq;
	private String nombre;
	private Long cedula;
	private String correo;
	private String cargo;
	private String dependencia;
	
	public FirmanteSSM(){}

	public FirmanteSSM(long seq, String nombre, long cedula, String correo, String cargo, String dependencia) {
		super();
		this.seq = seq;
		this.nombre = nombre;
		this.cedula = cedula;
		this.correo = correo;
		this.cargo = cargo;
		this.dependencia = dependencia;
	}

	public long getSeq() {
		return seq;
	}

	public void setSeq(long seq) {
		this.seq = seq;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public Long getCedula() {
		return cedula;
	}

	public void setCedula(Long cedula) {
		this.cedula = cedula;
	}

	public String getCargo() {
		return cargo;
	}

	public void setCargo(String cargo) {
		this.cargo = cargo;
	}

	public String getDependencia() {
		return dependencia;
	}

	public void setDependencia(String dependencia) {
		this.dependencia = dependencia;
	}
	
}
