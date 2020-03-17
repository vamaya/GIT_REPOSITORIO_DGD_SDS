package co.gov.banrep.iconecta.cs.documento.utils;

import java.util.List;

import co.gov.banrep.iconecta.cs.cliente.document.Attachment;
import co.gov.banrep.iconecta.cs.cliente.document.CopyOptions;
import co.gov.banrep.iconecta.cs.cliente.document.DataValue;
//import co.gov.banrep.iconecta.cs.cliente.document.DocumentManagement;
import co.gov.banrep.iconecta.cs.cliente.document.GetNodesInContainerOptions;
import co.gov.banrep.iconecta.cs.cliente.document.Metadata;
import co.gov.banrep.iconecta.cs.cliente.document.MoveOptions;
import co.gov.banrep.iconecta.cs.cliente.document.Node;
import co.gov.banrep.iconecta.cs.cliente.document.NodeRight;

public class ParametrosServicios {
	//Todos los posibles parametros
	//DocumentManagement client;
	long idNode;
	Node nodo;
	long idDoc;
	int numberToKeep;
	long version;
	long idParent;
	//long idDocumento;
	String nombreDoc;
	String nuevoNombre;
	CopyOptions copyOptions;
	NodeRight nodeRight;
	Metadata metadata;
	Attachment attachment;
	String comment;
	boolean versionControl;
	long idCategoria;
	MoveOptions moveOptions;
	long contenedor;
	GetNodesInContainerOptions options;
	long idReport;
	List<DataValue> inputs;

	//Constructor vacío
	public ParametrosServicios() {
		super();
	}
	
	//Constructor con todos los parámetros
	public ParametrosServicios(long idNode, Node nodo, long idDoc, int numberToKeep, long version, long idParent,
			String nombreDoc, String nuevoNombre, CopyOptions copyOptions, NodeRight nodeRight, Metadata metadata,
			Attachment attachment, String comment, boolean versionControl, long idCategoria, MoveOptions moveOptions,
			long contenedor, GetNodesInContainerOptions options, long idReport, List<DataValue> inputs) {
		super();
		this.idNode = idNode;
		this.nodo = nodo;
		this.idDoc = idDoc;
		this.numberToKeep = numberToKeep;
		this.version = version;
		this.idParent = idParent;
		this.nombreDoc = nombreDoc;
		this.nuevoNombre = nuevoNombre;
		this.copyOptions = copyOptions;
		this.nodeRight = nodeRight;
		this.metadata = metadata;
		this.attachment = attachment;
		this.comment = comment;
		this.versionControl = versionControl;
		this.idCategoria = idCategoria;
		this.moveOptions = moveOptions;
		this.contenedor = contenedor;
		this.options = options;
		this.idReport = idReport;
		this.inputs = inputs;
	}

	//IdNode
	public ParametrosServicios withIdNode(long idNode) {
		return new ParametrosServicios(idNode, nodo, idDoc, numberToKeep, version, idParent, nombreDoc, nuevoNombre, copyOptions, nodeRight, metadata, attachment, comment, versionControl, idCategoria, moveOptions, contenedor, options, idReport, inputs);
	}

	//Node
	public ParametrosServicios withNode(Node nodo) {
		return new ParametrosServicios(idNode, nodo, idDoc, numberToKeep, version, idParent, nombreDoc, nuevoNombre, copyOptions, nodeRight, metadata, attachment, comment, versionControl, idCategoria, moveOptions, contenedor, options, idReport, inputs);
	}

	//IdDoc
	public ParametrosServicios withIdDoc(long idDoc) {
		return new ParametrosServicios(idNode, nodo, idDoc, numberToKeep, version, idParent, nombreDoc, nuevoNombre, copyOptions, nodeRight, metadata, attachment, comment, versionControl, idCategoria, moveOptions, contenedor, options, idReport, inputs);
	}

	//NumberToKeep
	public ParametrosServicios withNumberToKeep(int numberToKeep) {
		return new ParametrosServicios(idNode, nodo, idDoc, numberToKeep, version, idParent, nombreDoc, nuevoNombre, copyOptions, nodeRight, metadata, attachment, comment, versionControl, idCategoria, moveOptions, contenedor, options, idReport, inputs);
	}

	//Version
	public ParametrosServicios withVersion(long version) {
		return new ParametrosServicios(idNode, nodo, idDoc, numberToKeep, version, idParent, nombreDoc, nuevoNombre, copyOptions, nodeRight, metadata, attachment, comment, versionControl, idCategoria, moveOptions, contenedor, options, idReport, inputs);
	}

	//IdParent
	public ParametrosServicios withIdParent(long idParent) {
		return new ParametrosServicios(idNode, nodo, idDoc, numberToKeep, version, idParent, nombreDoc, nuevoNombre, copyOptions, nodeRight, metadata, attachment, comment, versionControl, idCategoria, moveOptions, contenedor, options, idReport, inputs);
	}

	//NombreDoc
	public ParametrosServicios withNombreDoc(String nombreDoc) {
		return new ParametrosServicios(idNode, nodo, idDoc, numberToKeep, version, idParent, nombreDoc, nuevoNombre, copyOptions, nodeRight, metadata, attachment, comment, versionControl, idCategoria, moveOptions, contenedor, options, idReport, inputs);
	}

	//NuevoNombre
	public ParametrosServicios withNuevoNombre(String nuevoNombre) {
		return new ParametrosServicios(idNode, nodo, idDoc, numberToKeep, version, idParent, nombreDoc, nuevoNombre, copyOptions, nodeRight, metadata, attachment, comment, versionControl, idCategoria, moveOptions, contenedor, options, idReport, inputs);
	}

	//CopyOptions
	public ParametrosServicios withCopyOptions(CopyOptions copyOptions) {
		return new ParametrosServicios(idNode, nodo, idDoc, numberToKeep, version, idParent, nombreDoc, nuevoNombre, copyOptions, nodeRight, metadata, attachment, comment, versionControl, idCategoria, moveOptions, contenedor, options, idReport, inputs);
	}

	//NodeRight
	public ParametrosServicios withNodeRight(NodeRight nodeRight) {
		return new ParametrosServicios(idNode, nodo, idDoc, numberToKeep, version, idParent, nombreDoc, nuevoNombre, copyOptions, nodeRight, metadata, attachment, comment, versionControl, idCategoria, moveOptions, contenedor, options, idReport, inputs);
	}

	//Metadata
	public ParametrosServicios withMetadata(Metadata metadata) {
		return new ParametrosServicios(idNode, nodo, idDoc, numberToKeep, version, idParent, nombreDoc, nuevoNombre, copyOptions, nodeRight, metadata, attachment, comment, versionControl, idCategoria, moveOptions, contenedor, options, idReport, inputs);
	}

	//Attachment
	public ParametrosServicios withAttachment(Attachment attachment) {
		return new ParametrosServicios(idNode, nodo, idDoc, numberToKeep, version, idParent, nombreDoc, nuevoNombre, copyOptions, nodeRight, metadata, attachment, comment, versionControl, idCategoria, moveOptions, contenedor, options, idReport, inputs);
	}

	//Comment
	public ParametrosServicios withComment(String comment) {
		return new ParametrosServicios(idNode, nodo, idDoc, numberToKeep, version, idParent, nombreDoc, nuevoNombre, copyOptions, nodeRight, metadata, attachment, comment, versionControl, idCategoria, moveOptions, contenedor, options, idReport, inputs);
	}

	//VersionControl
	public ParametrosServicios withVersionControl(boolean versionControl) {
		return new ParametrosServicios(idNode, nodo, idDoc, numberToKeep, version, idParent, nombreDoc, nuevoNombre, copyOptions, nodeRight, metadata, attachment, comment, versionControl, idCategoria, moveOptions, contenedor, options, idReport, inputs);
	}

	//IdCategoria
	public ParametrosServicios withIdCategoria(long idCategoria) {
		return new ParametrosServicios(idNode, nodo, idDoc, numberToKeep, version, idParent, nombreDoc, nuevoNombre, copyOptions, nodeRight, metadata, attachment, comment, versionControl, idCategoria, moveOptions, contenedor, options, idReport, inputs);
	}

	//MoveOptions
	public ParametrosServicios withMoveOptions(MoveOptions moveOptions) {
		return new ParametrosServicios(idNode, nodo, idDoc, numberToKeep, version, idParent, nombreDoc, nuevoNombre, copyOptions, nodeRight, metadata, attachment, comment, versionControl, idCategoria, moveOptions, contenedor, options, idReport, inputs);
	}

	//Contenedor
	public ParametrosServicios withContenedor(long contenedor) {
		return new ParametrosServicios(idNode, nodo, idDoc, numberToKeep, version, idParent, nombreDoc, nuevoNombre, copyOptions, nodeRight, metadata, attachment, comment, versionControl, idCategoria, moveOptions, contenedor, options, idReport, inputs);
	}

	//Options
	public ParametrosServicios withOptions(GetNodesInContainerOptions options) {
		return new ParametrosServicios(idNode, nodo, idDoc, numberToKeep, version, idParent, nombreDoc, nuevoNombre, copyOptions, nodeRight, metadata, attachment, comment, versionControl, idCategoria, moveOptions, contenedor, options, idReport, inputs);
	}

	//IdReport
	public ParametrosServicios withIdReport(long idReport) {
		return new ParametrosServicios(idNode, nodo, idDoc, numberToKeep, version, idParent, nombreDoc, nuevoNombre, copyOptions, nodeRight, metadata, attachment, comment, versionControl, idCategoria, moveOptions, contenedor, options, idReport, inputs);
	}

	//Inputs
	public ParametrosServicios withInputs(List<DataValue> inputs) {
		return new ParametrosServicios(idNode, nodo, idDoc, numberToKeep, version, idParent, nombreDoc, nuevoNombre, copyOptions, nodeRight, metadata, attachment, comment, versionControl, idCategoria, moveOptions, contenedor, options, idReport, inputs);
	}

	//Getters
	public long getIdNode() {
		return idNode;
	}

	public Node getNodo() {
		return nodo;
	}

	public long getIdDoc() {
		return idDoc;
	}

	public int getNumberToKeep() {
		return numberToKeep;
	}

	public long getVersion() {
		return version;
	}

	public long getIdParent() {
		return idParent;
	}

	public String getNombreDoc() {
		return nombreDoc;
	}

	public String getNuevoNombre() {
		return nuevoNombre;
	}

	public CopyOptions getCopyOptions() {
		return copyOptions;
	}

	public NodeRight getNodeRight() {
		return nodeRight;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public Attachment getAttachment() {
		return attachment;
	}

	public String getComment() {
		return comment;
	}

	public boolean isVersionControl() {
		return versionControl;
	}

	public long getIdCategoria() {
		return idCategoria;
	}

	public MoveOptions getMoveOptions() {
		return moveOptions;
	}

	public long getContenedor() {
		return contenedor;
	}

	public GetNodesInContainerOptions getOptions() {
		return options;
	}

	public long getIdReport() {
		return idReport;
	}

	public List<DataValue> getInputs() {
		return inputs;
	}


}
