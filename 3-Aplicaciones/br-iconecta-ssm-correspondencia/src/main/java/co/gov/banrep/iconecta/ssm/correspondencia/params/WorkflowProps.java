package co.gov.banrep.iconecta.ssm.correspondencia.params;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("workflow")
@Component
public class WorkflowProps {
	
	@NotNull
	private long notificacionCorrespondencia;
	@NotNull
	private long generarNumeroRadicado;
	@NotNull
	private long generarNumeroRadicadoFondoIndependiente;
	@NotNull
	private long generarNumeroRadicadoDer;

	private long idNotificacionRadicadoDer;
	//@NotNull
	//private long distribucionRdiRde;
	private DistribucionRdiRde distribucionRdiRde = new DistribucionRdiRde();
	// TODO NOT NULL CARTA- DER
	private long distribucionMemorando;
	@NotNull
	private long distribucionMemorandoConfidencial;
	
	
	private DistribucionCarta distribucionCarta = new DistribucionCarta();	
	//private long distribucionCartaConfidencial;
	
	
	//private long distribucionDer;
	private DistribucionDer distribucionDer = new DistribucionDer();
	// TODO NOT NULL CARTA- DER
	// private long id;
	
	
	
	public static class DistribucionCarta{
		private long id;
		private long idConfidencial;
		private atributoWF atributoWF = new atributoWF();
		
		public static class atributoWF{
			private String numeroRadicado;
			private String tipologia;
			private String asunto;
			private String conexionCs;
			private String esHilos;
			private String nombreDependencia;
			private String solicitante;
			private String fechaRadicacion;
			private String workid;
			private String destino;
			private String esFisico;
			private String rolDestino;
			private String esCorreoCertificado;
			private String idDocumentoAdjunto;
			private String nombreDestino;
			private String entidadDestino;
			private String esMensajeria;
			private String correoCopia;
			private String idPCR;
			private String idCDD;
			
			public String getNumeroRadicado() {
				return numeroRadicado;
			}
			public void setNumeroRadicado(String numeroRadicado) {
				this.numeroRadicado = numeroRadicado;
			}
			public String getTipologia() {
				return tipologia;
			}
			public void setTipologia(String tipologia) {
				this.tipologia = tipologia;
			}
			public String getAsunto() {
				return asunto;
			}
			public void setAsunto(String asunto) {
				this.asunto = asunto;
			}
			public String getConexionCs() {
				return conexionCs;
			}
			public void setConexionCs(String conexionCs) {
				this.conexionCs = conexionCs;
			}
			public String getEsHilos() {
				return esHilos;
			}
			public void setEsHilos(String esHilos) {
				this.esHilos = esHilos;
			}
			public String getNombreDependencia() {
				return nombreDependencia;
			}
			public void setNombreDependencia(String nombreDependencia) {
				this.nombreDependencia = nombreDependencia;
			}
			public String getSolicitante() {
				return solicitante;
			}
			public void setSolicitante(String solicitante) {
				this.solicitante = solicitante;
			}
			public String getFechaRadicacion() {
				return fechaRadicacion;
			}
			public void setFechaRadicacion(String fechaRadicacion) {
				this.fechaRadicacion = fechaRadicacion;
			}
			public String getWorkid() {
				return workid;
			}
			public void setWorkid(String workid) {
				this.workid = workid;
			}
			public String getDestino() {
				return destino;
			}
			public void setDestino(String destino) {
				this.destino = destino;
			}
			public String getEsFisico() {
				return esFisico;
			}
			public void setEsFisico(String esFisico) {
				this.esFisico = esFisico;
			}
			public String getRolDestino() {
				return rolDestino;
			}
			public void setRolDestino(String rolDestino) {
				this.rolDestino = rolDestino;
			}
			public String getEsCorreoCertificado() {
				return esCorreoCertificado;
			}
			public void setEsCorreoCertificado(String esCorreoCertificado) {
				this.esCorreoCertificado = esCorreoCertificado;
			}
			public String getIdDocumentoAdjunto() {
				return idDocumentoAdjunto;
			}
			public void setIdDocumentoAdjunto(String idDocumentoAdjunto) {
				this.idDocumentoAdjunto = idDocumentoAdjunto;
			}
			public String getNombreDestino() {
				return nombreDestino;
			}
			public void setNombreDestino(String nombreDestino) {
				this.nombreDestino = nombreDestino;
			}
			public String getEntidadDestino() {
				return entidadDestino;
			}
			public void setEntidadDestino(String entidadDestino) {
				this.entidadDestino = entidadDestino;
			}
			public String getEsMensajeria() {
				return esMensajeria;
			}
			public void setEsMensajeria(String esMensajeria) {
				this.esMensajeria = esMensajeria;
			}
			public String getCorreoCopia() {
				return correoCopia;
			}
			public void setCorreoCopia(String correoCopia) {
				this.correoCopia = correoCopia;
			}
			public String getIdPCR() {
				return idPCR;
			}
			public void setIdPCR(String idPCR) {
				this.idPCR = idPCR;
			}
			public String getIdCDD() {
				return idCDD;
			}
			public void setIdCDD(String idCDD) {
				this.idCDD = idCDD;
			}
			
			
		}
		
		public long getId() {
			return id;
		}
		public void setId(long id) {
			this.id = id;
		}
		public atributoWF getAtributoWF() {
			return atributoWF;
		}
		public void setAtributoWF(atributoWF atributoWF) {
			this.atributoWF = atributoWF;
		}
		public long getIdConfidencial() {
			return idConfidencial;
		}
		public void setIdConfidencial(long idConfidencia) {
			this.idConfidencial = idConfidencia;
		}
		
		
	}
	
	public static class DistribucionRdiRde{
		
		private long id;
		
		private AtributoWF atributoWF = new AtributoWF(); 
		
		
		public static class AtributoWF{
			
			private String estado;
			private String tipologia;
			private String workid;
			private String codigoRuta;
			private String asunto;
			private String cantidad;
			private String descripcion;
			private String destino;
			private String rolOrigen;
			private String rolDestino;
			private String numeroRadicado;
			private String esDirecto;
			
			public String getEstado() {
				return estado;
			}
			public void setEstado(String estado) {
				this.estado = estado;
			}
			public String getTipologia() {
				return tipologia;
			}
			public void setTipologia(String tipologia) {
				this.tipologia = tipologia;
			}
			public String getWorkid() {
				return workid;
			}
			public void setWorkid(String workid) {
				this.workid = workid;
			}
			public String getCodigoRuta() {
				return codigoRuta;
			}
			public void setCodigoRuta(String codigoRuta) {
				this.codigoRuta = codigoRuta;
			}
			public String getAsunto() {
				return asunto;
			}
			public void setAsunto(String asunto) {
				this.asunto = asunto;
			}
			public String getCantidad() {
				return cantidad;
			}
			public void setCantidad(String cantidad) {
				this.cantidad = cantidad;
			}
			public String getDescripcion() {
				return descripcion;
			}
			public void setDescripcion(String descripcion) {
				this.descripcion = descripcion;
			}
			public String getDestino() {
				return destino;
			}
			public void setDestino(String destino) {
				this.destino = destino;
			}
			public String getRolOrigen() {
				return rolOrigen;
			}
			public void setRolOrigen(String rolOrigen) {
				this.rolOrigen = rolOrigen;
			}
			public String getRolDestino() {
				return rolDestino;
			}
			public void setRolDestino(String rolDestino) {
				this.rolDestino = rolDestino;
			}
			public String getNumeroRadicado() {
				return numeroRadicado;
			}
			public void setNumeroRadicado(String numeroRadicado) {
				this.numeroRadicado = numeroRadicado;
			}
			public String getEsDirecto() {
				return esDirecto;
			}
			public void setEsDirecto(String esDirecto) {
				this.esDirecto = esDirecto;
			}
			
		}


		public long getId() {
			return id;
		}
		public void setId(long id) {
			this.id = id;
		}
		public AtributoWF getAtributoWF() {
			return atributoWF;
		}
		public void setAtributoWF(AtributoWF atributoWF) {
			this.atributoWF = atributoWF;
		}
		
	}
	
	public static class DistribucionDer{
		
		private long id;
		private AtributoWF atributoWF = new AtributoWF();
		
		public static class AtributoWF{
			
			private String tipoComunicacion;
			//private String rolDestino;
			//private String rolDestinoNombre;
			//Nuevos atributos - Rutas DER
			private String rolSiguienteId;
			private String rolSiguienteNombre;
			
			private String despendenciaDestino;
			private String remitente;
			private String numeroRadicado;
			private String esEmail;
			private String rolOrigen;
			private String formaEntrega;
			private String anexos;
			private String codigoRuta;
			private String workId;
			private String tipoEnvio;
			private String esElectronico;
			private String usuarioSolicitante;
			private String fechaRadicacion;
			private String pbxOrigen;
			private String tramiteDCIN;
			
			public String getTipoComunicacion() {
				return tipoComunicacion;
			}
			public void setTipoComunicacion(String tipoComunicacion) {
				this.tipoComunicacion = tipoComunicacion;
			}
			
			/*
			public String getRolDestino() {
				return rolDestino;
			}
			public void setRolDestino(String rolDestino) {
				this.rolDestino = rolDestino;
			}
			*/
			
			public String getDespendenciaDestino() {
				return despendenciaDestino;
			}
			public void setDespendenciaDestino(String despendenciaDestino) {
				this.despendenciaDestino = despendenciaDestino;
			}
			public String getRemitente() {
				return remitente;
			}
			public void setRemitente(String remitente) {
				this.remitente = remitente;
			}
			public String getNumeroRadicado() {
				return numeroRadicado;
			}
			public void setNumeroRadicado(String numeroRadicado) {
				this.numeroRadicado = numeroRadicado;
			}
			public String getEsEmail() {
				return esEmail;
			}
			public void setEsEmail(String esEmail) {
				this.esEmail = esEmail;
			}
			public String getRolOrigen() {
				return rolOrigen;
			}
			public void setRolOrigen(String rolOrigen) {
				this.rolOrigen = rolOrigen;
			}
			public String getFormaEntrega() {
				return formaEntrega;
			}
			public void setFormaEntrega(String formaEntrega) {
				this.formaEntrega = formaEntrega;
			}
			public String getAnexos() {
				return anexos;
			}
			public void setAnexos(String anexos) {
				this.anexos = anexos;
			}
			public String getCodigoRuta() {
				return codigoRuta;
			}
			public void setCodigoRuta(String codigoRuta) {
				this.codigoRuta = codigoRuta;
			}
			public String getWorkId() {
				return workId;
			}
			public void setWorkId(String workId) {
				this.workId = workId;
			}
			public String getTipoEnvio() {
				return tipoEnvio;
			}
			public void setTipoEnvio(String tipoEnvio) {
				this.tipoEnvio = tipoEnvio;
			}
			public String getEsElectronico() {
				return esElectronico;
			}
			public void setEsElectronico(String esElectronico) {
				this.esElectronico = esElectronico;
			}
			public String getUsuarioSolicitante() {
				return usuarioSolicitante;
			}
			public void setUsuarioSolicitante(String usuarioSolicitante) {
				this.usuarioSolicitante = usuarioSolicitante;
			}
			public String getFechaRadicacion() {
				return fechaRadicacion;
			}
			public void setFechaRadicacion(String fechaRadicacion) {
				this.fechaRadicacion = fechaRadicacion;
			}
		
			public String getPbxOrigen() {
				return pbxOrigen;
			}
			public void setPbxOrigen(String pbxOrigen) {
				this.pbxOrigen = pbxOrigen;
			}
			public String getTramiteDCIN() {
				return tramiteDCIN;
			}
			public void setTramiteDCIN(String tramiteDCIN) {
				this.tramiteDCIN = tramiteDCIN;
			}
			public String getRolSiguienteId() {
				return rolSiguienteId;
			}
			public void setRolSiguienteId(String rolSiguienteId) {
				this.rolSiguienteId = rolSiguienteId;
			}
			public String getRolSiguienteNombre() {
				return rolSiguienteNombre;
			}
			public void setRolSiguienteNombre(String rolSiguienteNombre) {
				this.rolSiguienteNombre = rolSiguienteNombre;
			}
						
			
		}

		public long getId() {
			return id;
		}
		public void setId(long id) {
			this.id = id;
		}
		public AtributoWF getAtributoWF() {
			return atributoWF;
		}
		public void setAtributoWF(AtributoWF atributoWF) {
			this.atributoWF = atributoWF;
		}
		
		
	}
	
	private Atributo atributo = new Atributo();
	
	public static class Atributo{
		
		@NotNull
		private String rolDestino;
		@NotNull
		private String numeroRadicado;
		@NotNull
		private String idDocumentoOriginal;
		@NotNull
		private String nombreDestinatario;
		@NotNull
		private String idWorkflow;
		@NotNull
		private String idSolicitante;
		@NotNull
		private String asunto;
		@NotNull
		private String comentarios;
		@NotNull
		private String mensajeria;
		@NotNull
		private String numeroRadicadoCancelacion;
		
		
		public String getRolDestino() {
			return rolDestino;
		}
		public void setRolDestino(String rolDestino) {
			this.rolDestino = rolDestino;
		}		
		public String getNumeroRadicado() {
			return numeroRadicado;
		}
		public void setNumeroRadicado(String numeroRadicado) {
			this.numeroRadicado = numeroRadicado;
		}		
		public String getIdDocumentoOriginal() {
			return idDocumentoOriginal;
		}
		public void setIdDocumentoOriginal(String idDocumentoOriginal) {
			this.idDocumentoOriginal = idDocumentoOriginal;
		}
		public String getNombreDestinatario() {
			return nombreDestinatario;
		}
		public void setNombreDestinatario(String nombreDestinatario) {
			this.nombreDestinatario = nombreDestinatario;
		}
		public String getIdWorkflow() {
			return idWorkflow;
		}
		public void setIdWorkflow(String idWorkflow) {
			this.idWorkflow = idWorkflow;
		}
		public String getIdSolicitante() {
			return idSolicitante;
		}
		public void setIdSolicitante(String idSolicitante) {
			this.idSolicitante = idSolicitante;
		}
		public String getAsunto() {
			return asunto;
		}
		public void setAsunto(String asunto) {
			this.asunto = asunto;
		}
		public String getComentarios() {
			return comentarios;
		}
		public void setComentarios(String comentarios) {
			this.comentarios = comentarios;
		}
		public String getMensajeria() {
			return mensajeria;
		}
		public void setMensajeria(String mensajeria) {
			this.mensajeria = mensajeria;
		}
		public String getNumeroRadicadoCancelacion() {
			return numeroRadicadoCancelacion;
		}
		public void setNumeroRadicadoCancelacion(String numeroRadicadoCancelacion) {
			this.numeroRadicadoCancelacion = numeroRadicadoCancelacion;
		}
		
		
		
	}
	
	
	public long getNotificacionCorrespondencia() {
		return notificacionCorrespondencia;
	}
	public void setNotificacionCorrespondencia(long notificacionCorrespondencia) {
		this.notificacionCorrespondencia = notificacionCorrespondencia;
	}
	public long getGenerarNumeroRadicado() {
		return generarNumeroRadicado;
	}
	public void setGenerarNumeroRadicado(long generarNumeroRadicado) {
		this.generarNumeroRadicado = generarNumeroRadicado;
	}
	public long getGenerarNumeroRadicadoFondoIndependiente() {
		return generarNumeroRadicadoFondoIndependiente;
	}
	public void setGenerarNumeroRadicadoFondoIndependiente(long generarNumeroRadicadoFondoIndependiente) {
		this.generarNumeroRadicadoFondoIndependiente = generarNumeroRadicadoFondoIndependiente;
	}
	public long getGenerarNumeroRadicadoDer() {
		return generarNumeroRadicadoDer;
	}
	public void setGenerarNumeroRadicadoDer(long generarNumeroRadicadoDer) {
		this.generarNumeroRadicadoDer = generarNumeroRadicadoDer;
	}	
	
	public long getDistribucionMemorando() {
		return distribucionMemorando;
	}
	public DistribucionRdiRde getDistribucionRdiRde() {
		return distribucionRdiRde;
	}
	public void setDistribucionRdiRde(DistribucionRdiRde distribucionRdiRde) {
		this.distribucionRdiRde = distribucionRdiRde;
	}
	public void setDistribucionMemorando(long distribucionMemorando) {
		this.distribucionMemorando = distribucionMemorando;
	}
	public long getDistribucionMemorandoConfidencial() {
		return distribucionMemorandoConfidencial;
	}
	public void setDistribucionMemorandoConfidencial(long distribucionMemorandoConfidencial) {
		this.distribucionMemorandoConfidencial = distribucionMemorandoConfidencial;
	}	
	public Atributo getAtributo() {
		return atributo;
	}
	public DistribucionDer getDistribucionDer() {
		return distribucionDer;
	}
	public void setDistribucionDer(DistribucionDer distribucionDer) {
		this.distribucionDer = distribucionDer;
	}
	public void setAtributo(Atributo atributo) {
		this.atributo = atributo;
	}
	public DistribucionCarta getDistribucionCarta() {
		return distribucionCarta;
	}
	public void setDistribucionCarta(DistribucionCarta distribucionCarta) {
		this.distribucionCarta = distribucionCarta;
	}
	public long getIdNotificacionRadicadoDer() {
		return idNotificacionRadicadoDer;
	}
	public void setIdNotificacionRadicadoDer(long idNotificacionRadicadoDer) {
		this.idNotificacionRadicadoDer = idNotificacionRadicadoDer;
	}
	
	

}
