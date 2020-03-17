package co.gov.banrep.iconecta.ssm.firma.params;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("csworkflow")
@Component
public class CSWorkflowProps {
	
	private Notificacion notificacion = new Notificacion();
	private NotificacionAdmon notificacionAdmon = new NotificacionAdmon();
	private NotificacionInicial notificacionInicial = new NotificacionInicial();
	private NotificacionAlternativa notificacionAlternativa = new NotificacionAlternativa();

	public static class Notificacion{
		
		@NotNull
		private String mapId;
		@NotNull
		private String emailRemitente;
		
		public String getMapId() {
			return mapId;
		}
		public void setMapId(String mapId) {
			this.mapId = mapId;
		}
		public String getEmailRemitente() {
			return emailRemitente;
		}
		public void setEmailRemitente(String emailRemitente) {
			this.emailRemitente = emailRemitente;
		}	
		
		
	}
	
	public static class NotificacionAdmon{
		
		@NotNull
		private String mapId;
		@NotNull
		private String emailAdmon;
		
		public String getMapId() {
			return mapId;
		}
		public void setMapId(String mapId) {
			this.mapId = mapId;
		}
		public String getEmailAdmon() {
			return emailAdmon;
		}
		public void setEmailAdmon(String emailAdmon) {
			this.emailAdmon = emailAdmon;
		}		
		
	}
	
	public static class NotificacionInicial{
		
		@NotNull
		private long mapId;

		public long getMapId() {
			return mapId;
		}

		public void setMapId(long mapId) {
			this.mapId = mapId;
		}
				
	}
	
	public static class NotificacionAlternativa{
				
		private long mapId;

		public long getMapId() {
			return mapId;
		}

		public void setMapId(long mapId) {
			this.mapId = mapId;
		}
		
	}

	public Notificacion getNotificacion() {
		return notificacion;
	}

	public void setNotificacion(Notificacion notificacion) {
		this.notificacion = notificacion;
	}

	public NotificacionAdmon getNotificacionAdmon() {
		return notificacionAdmon;
	}

	public void setNotificacionAdmon(NotificacionAdmon notificacionAdmon) {
		this.notificacionAdmon = notificacionAdmon;
	}

	public NotificacionInicial getNotificacionInicial() {
		return notificacionInicial;
	}

	public void setNotificacionInicial(NotificacionInicial notificacionInicial) {
		this.notificacionInicial = notificacionInicial;
	}

	public NotificacionAlternativa getNotificacionAlternativa() {
		return notificacionAlternativa;
	}

	public void setNotificacionAlternativa(NotificacionAlternativa notificacionAlternativa) {
		this.notificacionAlternativa = notificacionAlternativa;
	}
	
	
}
