package co.gov.banrep.iconecta.ssm.firma.entity;

import java.nio.file.Path;

public class DocumentoSSM {
	
	private String nombre;
	private long idCs;
	private String idPf;
	//private DataHandler contenidoOriginal;
	private Path contenidoOriginalPath;
	//private String contenidoOriginalPath;
	//private DataHandler contenidoFirmando;
	//private DataHandler contenidoCopiaCompulsada;
	private String nombreModificado;
	
	//private Map<AtributosCategorias, Metadato> metadatos;
	//private Map<AtributosCategorias, Metadato> metadatosSubGrupo;
	
	private Categoria catDocumentoGeneral;
	private CategoriaConSubGrupo catFirma;
	
	@SuppressWarnings("unused")
	private DocumentoSSM() {}
	
	public DocumentoSSM(long idCs) {
		super();
		this.idCs = idCs;
	}

	/**
	 * @return the nombre
	 */
	public String getNombre() {
		return nombre;
	}
	/**
	 * @param nombre the nombre to set
	 */
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	
	
	public long getIdCs() {
		return idCs;
	}
	public void setIdCs(long idCs) {
		this.idCs = idCs;
	}
	/**
	 * @return the idPf
	 */
	public String getIdPf() {
		return idPf;
	}
	/**
	 * @param idPf the idPf to set
	 */
	public void setIdPf(String idPf) {
		this.idPf = idPf;
	}
	/**
	 * @return the contenidoFirmando
	 */
//	public DataHandler getContenidoFirmando() {
//		return contenidoFirmando;
//	}
	/**
	 * @param contenidoFirmando the contenidoFirmando to set
//	 */
//	public void setContenidoFirmando(DataHandler contenidoFirmando) {
//		this.contenidoFirmando = contenidoFirmando;
//	}

//	public DataHandler getContenidoOriginal() {
//		return contenidoOriginal;
//	}
	
	
//	public void setContenidoOriginal(DataHandler contenidoOriginal) {
//		this.contenidoOriginal = contenidoOriginal;
//	}
		
//	public DataHandler getContenidoCopiaCompulsada() {
//		return contenidoCopiaCompulsada;
//	}
//	public void setContenidoCopiaCompulsada(DataHandler contenidoCopiaCompulsada) {
//		this.contenidoCopiaCompulsada = contenidoCopiaCompulsada;
//	}
	public String getNombreModificado() {
		return nombreModificado;
	}
	
	public void setNombreModificado(String nombreModificado) {
		this.nombreModificado = nombreModificado;
	}


	public Categoria getCatDocumentoGeneral() {
		return catDocumentoGeneral;
	}


	public void setCatDocumentoGeneral(Categoria catDocumentoGeneral) {
		this.catDocumentoGeneral = catDocumentoGeneral;
	}


	public CategoriaConSubGrupo getCatFirma() {
		return catFirma;
	}


	public void setCatFirma(CategoriaConSubGrupo catFirma) {
		this.catFirma = catFirma;
	}

	public Path getContenidoOriginalPath() {
		return contenidoOriginalPath;
	}

	public void setContenidoOriginalPath(Path contenidoOriginalPath) {
		this.contenidoOriginalPath = contenidoOriginalPath;
	}
	
	/*
	public Path getContenidoOriginalPath() {
		return contenidoOriginalPath;
	}

	public void setContenidoOriginalPath(Path contenidoOriginalPath) {
		this.contenidoOriginalPath = contenidoOriginalPath;
	}
	
	*/
	
	
	
	
	
	/*
	public Map<AtributosCategorias, Metadato> getMetadatos() {
		return metadatos;
	}
	public void setMetadatos(Map<AtributosCategorias, Metadato> metadatos) {
		this.metadatos = metadatos;
	}
	public Map<AtributosCategorias, Metadato> getMetadatosSubGrupo() {
		return metadatosSubGrupo;
	}
	public void setMetadatosSubGrupo(Map<AtributosCategorias, Metadato> metadatoAbstracts) {
		this.metadatosSubGrupo = metadatoAbstracts;
	}
	*/
	
	/*
	public Map<String, String> getMetadatosCS(){
		Map<String, String> metadatosMap = new HashMap<String, String>();
		
		Iterator<Entry<AtributosCategorias, Metadato>> iter = metadatos.entrySet().iterator();
		while(iter.hasNext()){
			Metadato unMetadato = iter.next().getValue();
			String unaEtiqueta = unMetadato.getEtiquetaCS();
			String unValor = unMetadato.getValor();
			metadatosMap.put(unaEtiqueta, unValor);
		}
		
		return metadatosMap;
	}
	
	public Map<String, String> getMetadatosSubGrupoCS(){
		
		Map<String, String> metadatosMap = new HashMap<String, String>();
		
		Iterator<Entry<AtributosCategorias, Metadato>> iter = metadatosSubGrupo.entrySet().iterator();
		while(iter.hasNext()){
			Metadato unMetadato = iter.next().getValue();
			String unaEtiqueta = unMetadato.getEtiquetaCS();
			String unValor = unMetadato.getValor();
			metadatosMap.put(unaEtiqueta, unValor);
		}
		
		return metadatosMap;
		
	}
	*/


}
