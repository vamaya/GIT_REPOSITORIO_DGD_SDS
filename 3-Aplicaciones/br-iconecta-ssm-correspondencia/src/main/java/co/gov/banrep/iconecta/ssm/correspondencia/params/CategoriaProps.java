package co.gov.banrep.iconecta.ssm.correspondencia.params;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("categoria")
@Component
public class CategoriaProps {
	
	@NotNull
	private String nombreCategoriaFirma;
	
	private Documento documento = new Documento();
	
	private Correspondencia correspondencia = new Correspondencia();
	
	public static class Documento {
		
		@NotNull
		private long id;
		@NotNull
		private String nombre;
		public long getId() {
			return id;
		}
		public void setId(long id) {
			this.id = id;
		}
		public String getNombre() {
			return nombre;
		}
		public void setNombre(String nombre) {
			this.nombre = nombre;
		}
						
	}
	
	public static class Correspondencia{
		
		@NotNull
		private int id;
		@NotNull
		private String nombre;
		@NotNull
		private String atributoTipologia;
		@NotNull
		private String atributoEstado;
		@NotNull
		private String atributoAsunto;
		@NotNull
		private String atributoSiglaRemitente;
		@NotNull
		private String atributoSiglasRemitente;
		@NotNull
		private String atributoFechaRadicacion;
		@NotNull
		private String atributoHoraRadicacion;
		@NotNull
		private String atributoNumeroRadicacion;
		@NotNull
		private String subgrupoOrigen;
		@NotNull
		private String subgrupoDestino;
		@NotNull
		private String subgrupoCopia;
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public String getNombre() {
			return nombre;
		}
		public void setNombre(String nombre) {
			this.nombre = nombre;
		}
		public String getAtributoTipologia() {
			return atributoTipologia;
		}
		public void setAtributoTipologia(String atributoTipologia) {
			this.atributoTipologia = atributoTipologia;
		}
		public String getAtributoEstado() {
			return atributoEstado;
		}
		public void setAtributoEstado(String atributoEstado) {
			this.atributoEstado = atributoEstado;
		}
		public String getAtributoAsunto() {
			return atributoAsunto;
		}
		public void setAtributoAsunto(String atributoAsunto) {
			this.atributoAsunto = atributoAsunto;
		}
		public String getAtributoSiglaRemitente() {
			return atributoSiglaRemitente;
		}
		public void setAtributoSiglaRemitente(String atributoSiglaRemitente) {
			this.atributoSiglaRemitente = atributoSiglaRemitente;
		}
		public String getAtributoSiglasRemitente() {
			return atributoSiglasRemitente;
		}
		public void setAtributoSiglasRemitente(String atributoSiglasRemitente) {
			this.atributoSiglasRemitente = atributoSiglasRemitente;
		}
		public String getAtributoFechaRadicacion() {
			return atributoFechaRadicacion;
		}
		public void setAtributoFechaRadicacion(String atributoFechaRadicacion) {
			this.atributoFechaRadicacion = atributoFechaRadicacion;
		}
		public String getAtributoHoraRadicacion() {
			return atributoHoraRadicacion;
		}
		public void setAtributoHoraRadicacion(String atributoHoraRadicacion) {
			this.atributoHoraRadicacion = atributoHoraRadicacion;
		}
		public String getAtributoNumeroRadicacion() {
			return atributoNumeroRadicacion;
		}
		public void setAtributoNumeroRadicacion(String atributoNumeroRadicacion) {
			this.atributoNumeroRadicacion = atributoNumeroRadicacion;
		}
		public String getSubgrupoOrigen() {
			return subgrupoOrigen;
		}
		public void setSubgrupoOrigen(String subgrupoOrigen) {
			this.subgrupoOrigen = subgrupoOrigen;
		}
		public String getSubgrupoDestino() {
			return subgrupoDestino;
		}
		public void setSubgrupoDestino(String subgrupoDestino) {
			this.subgrupoDestino = subgrupoDestino;
		}
		public String getSubgrupoCopia() {
			return subgrupoCopia;
		}
		public void setSubgrupoCopia(String subgrupoCopia) {
			this.subgrupoCopia = subgrupoCopia;
		}
		
		
		
	}

	public String getNombreCategoriaFirma() {
		return nombreCategoriaFirma;
	}

	public void setNombreCategoriaFirma(String nombreCategoriaFirma) {
		this.nombreCategoriaFirma = nombreCategoriaFirma;
	}

	public Documento getDocumento() {
		return documento;
	}

	public void setDocumento(Documento documento) {
		this.documento = documento;
	}

	public Correspondencia getCorrespondencia() {
		return correspondencia;
	}

	public void setCorrespondencia(Correspondencia correspondencia) {
		this.correspondencia = correspondencia;
	}
	
	

}
