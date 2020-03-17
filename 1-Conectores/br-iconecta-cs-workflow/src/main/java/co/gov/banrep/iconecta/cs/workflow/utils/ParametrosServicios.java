package co.gov.banrep.iconecta.cs.workflow.utils;

import java.util.List;

import co.gov.banrep.iconecta.cs.cliente.workflow.ProcessStartData;


public class ParametrosServicios {
	// Todos los posibles parametros
	long mapId;
	ProcessStartData startData;
	List<Long> listIdAdjuntos;
	List<Long> listMemberRoleId;
	long workid;
	

	// Constructor vacío
	public ParametrosServicios() {
		super();
	}

	// Constructor con todos los parámetros
	public ParametrosServicios(long mapId, ProcessStartData startData,
			List<Long> listIdAdjuntos, List<Long> listMemberRoleId, long workid) {
		super();
		this.mapId = mapId;
		this.startData = startData;
		this.listIdAdjuntos = listIdAdjuntos;
		this.listMemberRoleId = listMemberRoleId;
		this.workid = workid;
	}

	// MapId
	public ParametrosServicios withMapId(long mapId) {
		return new ParametrosServicios(mapId, startData, listIdAdjuntos, listMemberRoleId, workid);
	}
	
	// StartData
	public ParametrosServicios withStartData(ProcessStartData startData) {
		return new ParametrosServicios(mapId, startData, listIdAdjuntos, listMemberRoleId, workid);
	}
	
	// ListIdAdjuntos
	public ParametrosServicios withListIdAdjuntos(List<Long> listIdAdjuntos) {
		return new ParametrosServicios(mapId, startData, listIdAdjuntos, listMemberRoleId, workid);
	}
	
	// ListMemberRoleId
	public ParametrosServicios withListMemberRoleId(List<Long> listMemberRoleId) {
		return new ParametrosServicios(mapId, startData,listIdAdjuntos, listMemberRoleId, workid);
	}
	
	// WorkId
	public ParametrosServicios withWorkId(long workid) {
		return new ParametrosServicios(mapId, startData,listIdAdjuntos, listMemberRoleId, workid);
	}

	// Getters
	public long getMapId() {
		return mapId;
	}

	public ProcessStartData getStartData() {
		return startData;
	}

	public List<Long> getListIdAdjuntos() {
		return listIdAdjuntos;
	}

	public List<Long> getListMemberRoleId() {
		return listMemberRoleId;
	}

	public long getWorkid() {
		return workid;
	}
	
	
	

	
}
