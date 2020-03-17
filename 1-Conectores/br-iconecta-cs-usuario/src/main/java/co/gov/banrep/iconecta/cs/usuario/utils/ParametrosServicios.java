package co.gov.banrep.iconecta.cs.usuario.utils;

public class ParametrosServicios {
	// Todos los posibles parametros
	long idUsuario;
	String loginName;
	String nombreGrupo;

	// Constructor vacío
	public ParametrosServicios() {
		super();
	}

	// Constructor con todos los parámetros
	public ParametrosServicios(long idUsuario, String loginName, String nombreGrupo) {
		super();
		this.idUsuario = idUsuario;
		this.loginName = loginName;
		this.nombreGrupo = nombreGrupo;
	}

	// IdUsuario
	public ParametrosServicios withIdUsuario(long idUsuario) {
		return new ParametrosServicios(idUsuario, loginName, nombreGrupo);
	}

	// LoginName
	public ParametrosServicios withLoginName(String loginName) {
		return new ParametrosServicios(idUsuario, loginName, nombreGrupo);
	}

	// NombreGRupo
	public ParametrosServicios withNombreGrupo(String nombreGrupo) {
		return new ParametrosServicios(idUsuario, loginName, nombreGrupo);
	}
	
	//Getters
	public long getIdUsuario() {
		return idUsuario;
	}

	public String getLoginName() {
		return loginName;
	}

	public String getNombreGrupo() {
		return nombreGrupo;
	}
	
}
