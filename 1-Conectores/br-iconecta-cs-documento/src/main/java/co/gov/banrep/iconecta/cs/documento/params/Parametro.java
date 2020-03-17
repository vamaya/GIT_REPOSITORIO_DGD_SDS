package co.gov.banrep.iconecta.cs.documento.params;


import co.gov.banrep.iconecta.cs.cliente.document.Attachment;
import co.gov.banrep.iconecta.cs.cliente.document.AttributeGroup;
import co.gov.banrep.iconecta.cs.cliente.document.CopyOptions;
import co.gov.banrep.iconecta.cs.cliente.document.DocumentManagement;
import co.gov.banrep.iconecta.cs.cliente.document.Metadata;
import co.gov.banrep.iconecta.cs.cliente.document.MoveOptions;
import co.gov.banrep.iconecta.cs.cliente.document.Node;
import co.gov.banrep.iconecta.cs.cliente.document.NodeRight;


public class Parametro {
	
	private Attachment attachment;
	private AttributeGroup attributeGroup;
	private DocumentManagement client;
	private String comment;
	private CopyOptions copyOptions;
	private long idCategoria;
	private long idDoc;
	private long idNode;
	private long idParent;
	private Metadata metadata;
	private MoveOptions moveOptions;
	private NodeRight nodeRight;
	private Node nodo; 
	private String nombreDoc;
	private String nuevoNombre;
	private int numberToKeep;
	private String proceso;
	private long version;
	private boolean versionControl;
	
	
	public Parametro() {
		
	}
	
	public Parametro(DocumentManagement client, long idNode, String proceso) {
		this.client = client;
		this.idNode = idNode;	
		this.proceso = proceso;
	}
	
	public Parametro(DocumentManagement client, long idDoc, int numberToKeep, String proceso) {
		this.client = client;
		this.idDoc = idDoc;
		this.numberToKeep = numberToKeep;
		this.proceso = proceso;
	}
	
	public Parametro(DocumentManagement client, long idDoc, long version, String proceso) {
		this.client = client;
		this.idDoc = idDoc;
		this.version = version;
		this.proceso = proceso;
	}
	
	public Parametro(DocumentManagement client, long idDoc, long idParent, String nuevoNombre, CopyOptions copyOptions, String proceso) {
		this.client = client;
		this.idDoc = idDoc;
		this.idParent = idParent;
		this.nuevoNombre = nuevoNombre;
		this.copyOptions = copyOptions;
		this.proceso = proceso;
	}
	
	public Parametro(DocumentManagement client, long idDoc, long idParent, String nuevoNombre, MoveOptions moveOptions, String proceso) {
		this.client = client;
		this.idDoc = idDoc;
		this.idParent = idParent;
		this.nuevoNombre = nuevoNombre;
		this.moveOptions = moveOptions;	
		this.proceso = proceso;
	}
	
	public Parametro(DocumentManagement client, long idDoc, Metadata metadata, Attachment attachment, String proceso) {
		this.client = client;
		this.idDoc = idDoc;
		this.metadata = metadata;
		this.attachment = attachment;
		this.proceso = proceso;
	}
	
	public Parametro(DocumentManagement client, long idDoc, NodeRight nodeRight, String proceso) {
		this.client = client;
		this.idDoc = idDoc;
		this.nodeRight = nodeRight;
		this.proceso = proceso;
	}
	
	public Parametro(DocumentManagement client, long idParent, String nombreDoc, String proceso) {
		this.client = client;
		this.idParent = idParent;
		this.nombreDoc = nombreDoc;
		this.proceso = proceso;
	}
	
	public Parametro(DocumentManagement client, long idParent, String nombreDoc, String comment, boolean versionControl, Metadata metadata, Attachment attachment, String proceso) {
		this.client = client;
		this.idParent = idParent;
		this.nombreDoc = nombreDoc;
		this.comment = comment;
		this.versionControl = versionControl;
		this.metadata = metadata;
		this.attachment = attachment;	
		this.proceso = proceso;
	}
	
	public Parametro(DocumentManagement client, Node nodo, String proceso) {
		this.client = client;
		this.nodo = nodo;
		this.proceso = proceso;
	}

	public Attachment getAttachment() {
		return attachment;
	}

	public void setAttachment(Attachment attachment) {
		this.attachment = attachment;
	}

	public AttributeGroup getAttributeGroup() {
		return attributeGroup;
	}

	public void setAttributeGroup(AttributeGroup attributeGroup) {
		this.attributeGroup = attributeGroup;
	}

	public DocumentManagement getClient() {
		return client;
	}

	public void setClient(DocumentManagement client) {
		this.client = client;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public CopyOptions getCopyOptions() {
		return copyOptions;
	}

	public void setCopyOptions(CopyOptions copyOptions) {
		this.copyOptions = copyOptions;
	}

	public long getIdCategoria() {
		return idCategoria;
	}

	public void setIdCategoria(long idCategoria) {
		this.idCategoria = idCategoria;
	}

	public long getIdDoc() {
		return idDoc;
	}

	public void setIdDoc(long idDoc) {
		this.idDoc = idDoc;
	}

	public long getIdNode() {
		return idNode;
	}

	public void setIdNode(long idNode) {
		this.idNode = idNode;
	}

	public long getIdParent() {
		return idParent;
	}

	public void setIdParent(long idParent) {
		this.idParent = idParent;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	public MoveOptions getMoveOptions() {
		return moveOptions;
	}

	public void setMoveOptions(MoveOptions moveOptions) {
		this.moveOptions = moveOptions;
	}

	public NodeRight getNodeRight() {
		return nodeRight;
	}

	public void setNodeRight(NodeRight nodeRight) {
		this.nodeRight = nodeRight;
	}

	public Node getNodo() {
		return nodo;
	}

	public void setNodo(Node nodo) {
		this.nodo = nodo;
	}

	public String getNombreDoc() {
		return nombreDoc;
	}

	public void setNombreDoc(String nombreDoc) {
		this.nombreDoc = nombreDoc;
	}

	public String getNuevoNombre() {
		return nuevoNombre;
	}

	public void setNuevoNombre(String nuevoNombre) {
		this.nuevoNombre = nuevoNombre;
	}

	public int getNumberToKeep() {
		return numberToKeep;
	}

	public void setNumberToKeep(int numberToKeep) {
		this.numberToKeep = numberToKeep;
	}

	public String getProceso() {
		return proceso;
	}

	public void setProceso(String proceso) {
		this.proceso = proceso;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public boolean isVersionControl() {
		return versionControl;
	}

	public void setVersionControl(boolean versionControl) {
		this.versionControl = versionControl;
	}	

}
