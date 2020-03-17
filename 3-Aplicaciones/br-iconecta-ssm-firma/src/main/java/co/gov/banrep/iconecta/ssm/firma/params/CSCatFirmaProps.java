package co.gov.banrep.iconecta.ssm.firma.params;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("cscatfirma")
public class CSCatFirmaProps {
	@NotNull
	private String id;
	@NotNull
	private String nombreCategoria;
	@NotNull
	private String grupoAtributos;
	
	private Subgrupo subgrupo = new Subgrupo();
	
	public static class Subgrupo {

		@NotNull
		private String nombreFirmante;
		@NotNull
		private String entidad;
		@NotNull
		private String algoritmoFirma;
		@NotNull
		private String serieCertificado;
		@NotNull
		private String fechaFirma;
		@NotNull
		private String valorHash;
		@NotNull
		private String tipoFirma;

		public String getNombreFirmante() {
			return nombreFirmante;
		}

		public void setNombreFirmante(String nombreFirmante) {
			this.nombreFirmante = nombreFirmante;
		}

		public String getEntidad() {
			return entidad;
		}

		public void setEntidad(String entidad) {
			this.entidad = entidad;
		}

		public String getAlgoritmoFirma() {
			return algoritmoFirma;
		}

		public void setAlgoritmoFirma(String algoritmoFirma) {
			this.algoritmoFirma = algoritmoFirma;
		}

		public String getSerieCertificado() {
			return serieCertificado;
		}

		public void setSerieCertificado(String serieCertificado) {
			this.serieCertificado = serieCertificado;
		}

		public String getFechaFirma() {
			return fechaFirma;
		}

		public void setFechaFirma(String fechaFirma) {
			this.fechaFirma = fechaFirma;
		}

		public String getValorHash() {
			return valorHash;
		}

		public void setValorHash(String valorHash) {
			this.valorHash = valorHash;
		}

		public String getTipoFirma() {
			return tipoFirma;
		}

		public void setTipoFirma(String tipoFirma) {
			this.tipoFirma = tipoFirma;
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

	public String getGrupoAtributos() {
		return grupoAtributos;
	}

	public void setGrupoAtributos(String grupoAtributos) {
		this.grupoAtributos = grupoAtributos;
	}

	public Subgrupo getSubgrupo() {
		return subgrupo;
	}

	public void setSubgrupo(Subgrupo subgrupo) {
		this.subgrupo = subgrupo;
	}
	
	
	
	
}
