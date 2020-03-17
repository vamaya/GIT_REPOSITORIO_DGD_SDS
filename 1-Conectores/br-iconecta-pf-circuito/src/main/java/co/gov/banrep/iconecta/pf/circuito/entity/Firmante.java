package co.gov.banrep.iconecta.pf.circuito.entity;

public final class Firmante {

	public final String nif;
	public final int daysLimit;
	public final int noticeDays;
	public final int idAction;
	public final int idActionDeadTask;
	public final int idOrganizationGroup;
	public final int idRol;
	public final boolean enableReassignTask;
	public final boolean enableSigndesatendida;
	public final boolean enableTimestamp;
	public final boolean enableAddDocs;
	public final boolean enableAddSigners;
	public final boolean enableDeleteSigners;
	public final boolean enableEditSigner;

	// Nif es el unico valor obligatorio
	public Firmante(String nif) {
		this.nif = nif;
		this.daysLimit = 1;
		this.noticeDays = 0;
		this.idAction = 1; // 1 Corresponde a la accion de firma
		this.idActionDeadTask = 1; // 1 corresponde al rechazo automatico despues del vencimiento
		this.idOrganizationGroup = 0; // 0 no corresponde a ningun id de organizaci�n
		this.idRol = 0; // TODO Consultar lo de master rol??
		this.enableReassignTask = false;
		this.enableSigndesatendida = false;
		this.enableTimestamp = true;
		this.enableAddDocs = false;
		this.enableAddSigners = false;
		this.enableDeleteSigners = true; // TODO porque entelgy estable true
											// cuando el doc dice que no se debe
											// hacer nada?
		this.enableEditSigner = false; // TODO porque entelgy estable false
										// cuando el doc dice que no se debe
										// hacer nada?
	}

	// Constructor que contiene todos los parametros
	private Firmante(String nif, int daysLimit, int noticeDays, int idAction, int idActionDeadTask,
			int idOrganizationGroup, int idRol, boolean enableReassignTask, boolean enableSigndesatendida,
			boolean enableTimestamp, boolean enableAddDocs, boolean enableAddSigners, boolean enableDeleteSigners,
			boolean enableEditSigner) {
		this.nif = nif;
		this.daysLimit = daysLimit;
		this.noticeDays = noticeDays;
		this.idAction = idAction;
		this.idActionDeadTask = idActionDeadTask;
		this.idOrganizationGroup = idOrganizationGroup;
		this.idRol = idRol;
		this.enableReassignTask = enableReassignTask;
		this.enableSigndesatendida = enableSigndesatendida;
		this.enableTimestamp = enableTimestamp;
		this.enableAddDocs = enableAddDocs;
		this.enableAddSigners = enableAddSigners;
		this.enableDeleteSigners = enableDeleteSigners;
		this.enableEditSigner = enableEditSigner;
	}

	public Firmante withDaysLimit(int daysLimit) {
		
		if (daysLimit < 1){
			throw new IllegalArgumentException("El valor dias limite ("+daysLimit+") no puede ser un n�mero negativo");
		}
		
		return new Firmante(nif, daysLimit, noticeDays, idAction, idActionDeadTask, idOrganizationGroup, idRol,
				enableReassignTask, enableSigndesatendida, enableTimestamp, enableAddDocs, enableAddSigners,
				enableDeleteSigners, enableEditSigner);
	}
	
	public Firmante withNoticeDays(int noticeDays){
		
		if (noticeDays < 1){
			throw new IllegalArgumentException("El valor dias de notificaci�n ("+noticeDays+") no puede ser un n�mero negativo");
		}
		
		if(noticeDays > this.daysLimit){
			throw new IllegalArgumentException("Los dias de notificacion no pueden ser mayores que el numero de dias limite");
		}
		
		return new Firmante(nif, daysLimit, noticeDays, idAction, idActionDeadTask, idOrganizationGroup, idRol,
				enableReassignTask, enableSigndesatendida, enableTimestamp, enableAddDocs, enableAddSigners,
				enableDeleteSigners, enableEditSigner);
	}
	
	public Firmante withIdAction (int idAction){		
		return new Firmante(nif, daysLimit, noticeDays, idAction, idActionDeadTask, idOrganizationGroup, idRol,
				enableReassignTask, enableSigndesatendida, enableTimestamp, enableAddDocs, enableAddSigners,
				enableDeleteSigners, enableEditSigner);		
	}
	
	public Firmante withIdActionDeadTask (int idActionDeadTask){
		return new Firmante(nif, daysLimit, noticeDays, idAction, idActionDeadTask, idOrganizationGroup, idRol,
				enableReassignTask, enableSigndesatendida, enableTimestamp, enableAddDocs, enableAddSigners,
				enableDeleteSigners, enableEditSigner);	
	}
	
	public Firmante withIdOrganizationGroup (int idOrganizationGroup){
		return new Firmante(nif, daysLimit, noticeDays, idAction, idActionDeadTask, idOrganizationGroup, idRol,
				enableReassignTask, enableSigndesatendida, enableTimestamp, enableAddDocs, enableAddSigners,
				enableDeleteSigners, enableEditSigner);	
	}
	
	public Firmante withiIdRol (int idRol){
		return new Firmante(nif, daysLimit, noticeDays, idAction, idActionDeadTask, idOrganizationGroup, idRol,
				enableReassignTask, enableSigndesatendida, enableTimestamp, enableAddDocs, enableAddSigners,
				enableDeleteSigners, enableEditSigner);	
	}
	
	public Firmante withEnableReassignTask (boolean enableReassignTask){
		return new Firmante(nif, daysLimit, noticeDays, idAction, idActionDeadTask, idOrganizationGroup, idRol,
				enableReassignTask, enableSigndesatendida, enableTimestamp, enableAddDocs, enableAddSigners,
				enableDeleteSigners, enableEditSigner);	
	}
	
	public Firmante withEnableSigndesatendida (boolean enableSigndesatendida){
		return new Firmante(nif, daysLimit, noticeDays, idAction, idActionDeadTask, idOrganizationGroup, idRol,
				enableReassignTask, enableSigndesatendida, enableTimestamp, enableAddDocs, enableAddSigners,
				enableDeleteSigners, enableEditSigner);	
	}
	
	public Firmante withEnableTimestamp (boolean enableTimestamp){
		return new Firmante(nif, daysLimit, noticeDays, idAction, idActionDeadTask, idOrganizationGroup, idRol,
				enableReassignTask, enableSigndesatendida, enableTimestamp, enableAddDocs, enableAddSigners,
				enableDeleteSigners, enableEditSigner);	
	}
	
	public Firmante withEnableAddDocs (boolean enableAddDocs){
		return new Firmante(nif, daysLimit, noticeDays, idAction, idActionDeadTask, idOrganizationGroup, idRol,
				enableReassignTask, enableSigndesatendida, enableTimestamp, enableAddDocs, enableAddSigners,
				enableDeleteSigners, enableEditSigner);	
	}
	
	public Firmante withEnableAddSigners (boolean enableAddSigners){
		return new Firmante(nif, daysLimit, noticeDays, idAction, idActionDeadTask, idOrganizationGroup, idRol,
				enableReassignTask, enableSigndesatendida, enableTimestamp, enableAddDocs, enableAddSigners,
				enableDeleteSigners, enableEditSigner);	
	}
	
	public Firmante withEnableDeleteSigners (boolean enableDeleteSigners){
		return new Firmante(nif, daysLimit, noticeDays, idAction, idActionDeadTask, idOrganizationGroup, idRol,
				enableReassignTask, enableSigndesatendida, enableTimestamp, enableAddDocs, enableAddSigners,
				enableDeleteSigners, enableEditSigner);	
	}
	
	public Firmante withEnableEditSigner (boolean enableEditSigner){
		return new Firmante(nif, daysLimit, noticeDays, idAction, idActionDeadTask, idOrganizationGroup, idRol,
				enableReassignTask, enableSigndesatendida, enableTimestamp, enableAddDocs, enableAddSigners,
				enableDeleteSigners, enableEditSigner);	
	}
	
	@Override
	public String toString(){
		return "Firmante con nif: "+nif; 
	}

}
