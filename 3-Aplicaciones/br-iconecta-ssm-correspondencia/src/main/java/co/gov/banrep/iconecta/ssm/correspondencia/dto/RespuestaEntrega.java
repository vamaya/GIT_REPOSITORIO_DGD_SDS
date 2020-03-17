package co.gov.banrep.iconecta.ssm.correspondencia.dto;

public class RespuestaEntrega {
	
	private String rolsigueinte;
	
	private boolean pasoCurrier;
	
	private boolean finalizacion;
	
	public RespuestaEntrega() {
		this.rolsigueinte = "";
		this.pasoCurrier = false;
		this.finalizacion = false;
	}

	public String getRolsigueinte() {
		return rolsigueinte;
	}

	public void setRolsigueinte(String rolsigueinte) {
		this.rolsigueinte = rolsigueinte;
	}

	public boolean getPasoCurrier() {
		return pasoCurrier;
	}

	public void setPasoCurrier(boolean pasoCurrier) {
		this.pasoCurrier = pasoCurrier;
	}

	public boolean getFinalizacion() {
		return finalizacion;
	}

	public void setFinalizacion(boolean finalizacion) {
		this.finalizacion = finalizacion;
	}
	
	
	

}
