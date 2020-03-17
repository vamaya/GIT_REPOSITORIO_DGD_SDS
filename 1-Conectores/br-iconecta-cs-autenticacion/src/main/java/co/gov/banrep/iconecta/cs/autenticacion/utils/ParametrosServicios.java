package co.gov.banrep.iconecta.cs.autenticacion.utils;

import java.net.URL;

import javax.xml.namespace.QName;

public class ParametrosServicios {
	// Todos los posibles parametros
	String usuarioAdmin;
	String usuario;
	String password;
	QName qname;
	URL newEndPoint;

	// Constructor vacío
	public ParametrosServicios() {
		super();
	}

	// Constructor con todos los parámetros
	public ParametrosServicios(String usuarioAdmin, String password, QName qname, URL newEndPoint, String usuario) {
		super();
		this.usuarioAdmin = usuarioAdmin;
		this.password = password;
		this.qname = qname;
		this.newEndPoint = newEndPoint;
		this.usuario = usuario;
	}

	// UsuarioAdmin
	public ParametrosServicios withUsuarioAdmin(String usuarioAdmin) {
		return new ParametrosServicios(usuarioAdmin, password, qname, newEndPoint, usuario);
	}

	// Usuario
	public ParametrosServicios withUsuario(String usuario) {
		return new ParametrosServicios(usuarioAdmin, password, qname, newEndPoint, usuario);
	}

	// Password
	public ParametrosServicios withPassword(String password) {
		return new ParametrosServicios(usuarioAdmin, password, qname, newEndPoint, usuario);
	}

	// Qname
	public ParametrosServicios withQName(QName qname) {
		return new ParametrosServicios(usuarioAdmin, password, qname, newEndPoint, usuario);
	}

	// URL
	public ParametrosServicios withURL(URL newEndPoint) {
		return new ParametrosServicios(usuarioAdmin, password, qname, newEndPoint, usuario);
	}

	// Getters
	public String getUsuarioAdmin() {
		return usuarioAdmin;
	}

	public String getUsuario() {
		return usuario;
	}

	public String getPassword() {
		return password;
	}

	public QName getQname() {
		return qname;
	}

	public URL getNewEndPoint() {
		return newEndPoint;
	}

}
