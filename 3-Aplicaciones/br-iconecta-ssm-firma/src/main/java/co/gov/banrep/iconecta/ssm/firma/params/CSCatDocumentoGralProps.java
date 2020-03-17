package co.gov.banrep.iconecta.ssm.firma.params;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("cscatdocumento")
// @Validated
public class CSCatDocumentoGralProps {
	@NotNull
	private String id;
	@NotNull
	private String nombreCategoria;
		
	private Metadato metadato = new Metadato();	

	public static class Metadato {

		@NotNull
		private String fechaDocumento;
		@NotNull
		private String estado;
		@NotNull
		private String tipoDocumental;
		@NotNull
		private String serie;
		
		public String getFechaDocumento() {
			return fechaDocumento;
		}

		public void setFechaDocumento(String fechaDocumento) {
			this.fechaDocumento = fechaDocumento;
		}

		public String getEstado() {
			return estado;
		}

		public void setEstado(String estado) {
			this.estado = estado;
		}

		public String getTipoDocumental() {
			return tipoDocumental;
		}

		public void setTipoDocumental(String tipoDocumental) {
			this.tipoDocumental = tipoDocumental;
		}

		public String getSerie() {
			return serie;
		}

		public void setSerie(String serie) {
			this.serie = serie;
		}
		
	}

	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNombreCategoria() {
		return nombreCategoria;
	}

	public void setNombreCategoria(String nombreCategoria) {
		this.nombreCategoria = nombreCategoria;
	}
	
	/*
	public String getGrupoAtributos() {
		return grupoAtributos;
	}

	public void setGrupoAtributos(String grupoAtributos) {
		this.grupoAtributos = grupoAtributos;
	}
	*/

	public Metadato getMetadato() {
		return metadato;
	}

	public void setMetadato(Metadato metadato) {
		this.metadato = metadato;
	}
	
}
