package co.gov.banrep.iconecta.office.documento.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.POIXMLProperties.CustomProperties;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.IRunElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFSDT;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import co.gov.banrep.iconecta.office.documento.utils.Consts;
import co.gov.banrep.iconecta.office.documento.utils.AppUtils;

/**
 * @author jhernaro900814094
 *
 */
public class Documento {

	private Documento() {
		throw new AssertionError();
	}
	
	/*
	 * Leer Destinatarios Documento ME
	 */
	public static List<Empleado> leerDestinatariosMe(XWPFDocument docx, XWPFWordExtractor word,Map<String,String> customs) {

		int cons = 0;
		Empleado empleado = new Empleado();
		List<Empleado> listDestinatario = new ArrayList<Empleado>();

		for (IBodyElement elem : docx.getBodyElements()) {
			if (elem instanceof XWPFSDT) {
				String text = ((XWPFSDT) elem).getTag();
				/*
				 * Leer Destinatarios AppUtils ME
				 */
				if (text.contains(cons + customs.get(Consts.NOMBRE_DESTINO))) {
					empleado.setNombre(((XWPFSDT) elem).getContent().getText());
				} else if (text.contains(cons + customs.get(Consts.CARGO_DESTINO))) {
					empleado.setCargo(((XWPFSDT) elem).getContent().getText());
				} else if (text.contains(cons + customs.get(Consts.DEPENDENCIA_DESTINO))) {
					empleado.setDependencia(((XWPFSDT) elem).getContent().getText());

					CustomProperties info = word.getCustomProperties();
					String rol = String.format("%02d", cons) +customs.get(Consts.PCR_DESTINO);
					empleado.setRol(info.getProperty(rol).getLpwstr());

					cons++;
					listDestinatario.add(empleado);
					empleado = new Empleado();
				}

			}
		}
		return listDestinatario;
	}


		/*
		 * Leer Asunto
		 */
	public static String leerAsunto(XWPFDocument docx, Map<String, String> customs) {

		String asunto = null;

		Iterator<IBodyElement> elemas = docx.getBodyElements().stream().filter(t -> t instanceof XWPFTable)
				.collect(Collectors.toList()).iterator(); // elemas -> lista de objetos de tipo tabla en el documento
		
		while (elemas.hasNext() && asunto == null) {
			IBodyElement uelemas = elemas.next();

			List<XWPFTableRow> row = ((XWPFTable) uelemas).getRows();
			Iterator<XWPFTableRow> iterrow = row.iterator();
			while (iterrow.hasNext()) {
				XWPFTableRow urow = iterrow.next();

				XWPFTableCell cel = urow.getCell(1);
				if (!cel.equals(null)) { //Protección: para verificar que no viene nulo
					for (IBodyElement icel : cel.getBodyElements()) {
						if (icel instanceof XWPFSDT) {
							if (((XWPFSDT) icel).getTag().contains(customs.get(Consts.ASUNTO))) {
								asunto = (((XWPFSDT) icel).getContent().getText());
							}
						}
					}
				}
			}
		}
		return asunto;
	}

	

	/*
	 * Leer Firmantes Documento CA
	 */
	public static List<Empleado> leerFirmantesCA(XWPFDocument docx, XWPFWordExtractor word, Map<String,String> customs) {
		Empleado empleado = new Empleado();
		List<Empleado> listFirmante = new ArrayList<Empleado>();

		Iterator<IBodyElement> elemas = docx.getBodyElements().stream().filter(t -> t instanceof XWPFTable)
				.collect(Collectors.toList()).iterator();

		while (elemas.hasNext()) {
			IBodyElement uelemas = elemas.next();

			List<XWPFTableRow> row = ((XWPFTable) uelemas).getRows();
			Iterator<XWPFTableRow> iterrow = row.iterator();
			while (iterrow.hasNext()) {

				XWPFTableRow urow = iterrow.next();
				int tamanoCeldas = urow.getCtRow().sizeOfTcArray();

				for (int i = 0; i < tamanoCeldas; i++) {
					XWPFTableCell cel = urow.getCell(i);
					if (!cel.equals(null)) {

						for (IBodyElement icel : cel.getBodyElements()) {
							if (icel instanceof XWPFSDT) {
								if (((XWPFSDT) icel).getTag()
										.contains(String.format("%02d", i) + customs.get(Consts.NOMBRE_FIRMANTE))) {
									empleado.setNombre(((XWPFSDT) icel).getContent().getText());
									listFirmante.add(empleado);
									empleado = new Empleado();
								} else if (((XWPFSDT) icel).getTag()
										.contains(String.format("%02d", i) + customs.get(Consts.CARGO_FIRMANTE))) {
									empleado = listFirmante.get(i);
									empleado.setCargo(((XWPFSDT) icel).getContent().getText());
									listFirmante.set(i, empleado);
									empleado = new Empleado();
								} else if (((XWPFSDT) icel).getTag()
										.contains(String.format("%02d", i) + customs.get(Consts.DEPENDENCIA_FIRMANTE))) {
									empleado = listFirmante.get(i);
									empleado.setDependencia(((XWPFSDT) icel).getContent().getText());

									CustomProperties info = word.getCustomProperties();

									String rol = String.format("%02d", i) + customs.get(Consts.ID_FIRMANTE);
									String sgr = String.format("%02d", i) + customs.get(Consts.SGR_FIRMANTE);

									empleado.setRol(info.getProperty(rol).getLpwstr());
									empleado.setSiglaremitente(info.getProperty(sgr).getLpwstr());

									/*
									if (empleado.getSiglaremitente().equals(Consts.SIGLA_FIRMANTE)) {
										throw new NumberFormatException(" - La sigla del firmante no es valida");
									}
									*/

									if (i < 1) { // solo interesan estos datos para el primer firmante

										//LLAVES DE BÚSQUEDA
										String nombrePCRLlave = String.format("%02d", i) + customs.get(Consts.NOMBRE_PCR);
										String nombreCDDLLave = String.format("%02d", i) + customs.get(Consts.NOMBRE_CDD);
										String idPCRLlave = String.format("%02d", i) + customs.get(Consts.ID_PCR);
										String idCDDLlave = String.format("%02d", i) + customs.get(Consts.ID_CDD);
										
										//INICIALIZACIÓN DE LAS VARIABLES
										String nombrePCR = info.getProperty(nombrePCRLlave).getLpwstr();
										String nombreCDD = info.getProperty(nombreCDDLLave).getLpwstr();
										String idPCR = info.getProperty(idPCRLlave).getLpwstr();
										String idCDD = info.getProperty(idCDDLlave).getLpwstr();

										if (!StringUtils.isNumeric(idPCR)
												|| StringUtils.isEmpty(idPCR)
												) {
											idPCR = "0";
											/*
											throw new NumberFormatException(
													" - El firmante no cuenta con IdPCR o IdCDD valido, se debe revisar plantilla.  ATRIBUTOS:  idPCR = "
															+ info.getProperty(idPCRLlave).getLpwstr() + " - idCDD = "
															+ info.getProperty(idCDDLlave).getLpwstr());
															*/
										}
										
										if (!StringUtils.isNumeric(idCDD)
												||StringUtils.isEmpty(idCDD)
												) {
											idCDD = "0";
										}

										empleado.setNombrePCR(nombrePCR);
										empleado.setNombreCDD(nombreCDD);
										empleado.setIdPCR(Long.parseLong(idPCR));
										empleado.setIdCDD(Long.parseLong(idCDD));

									}

									listFirmante.set(i, empleado);
									empleado = new Empleado();
								}
							}
						}

					}

				}
			}

		}

		return listFirmante;
	}
	


	/*
	 * Leer Firmantes Documento ME
	 */
	public static List<Empleado> leerFirmantesME(XWPFDocument docx, XWPFWordExtractor word, Map<String,String> customs) {

		Empleado empleado = new Empleado();
		List<Empleado> listFirmante = new ArrayList<Empleado>();

		Iterator<IBodyElement> elemas = docx.getBodyElements().stream().filter(t -> t instanceof XWPFTable)
				.collect(Collectors.toList()).iterator();

		while (elemas.hasNext()) {
			IBodyElement uelemas = elemas.next();

			List<XWPFTableRow> row = ((XWPFTable) uelemas).getRows();
			Iterator<XWPFTableRow> iterrow = row.iterator();
			while (iterrow.hasNext()) {

				XWPFTableRow urow = iterrow.next();
				int tamanoCeldas = urow.getCtRow().sizeOfTcArray();

				for (int i = 0; i < tamanoCeldas; i++) {
					XWPFTableCell cel = urow.getCell(i);
					if (!cel.equals(null)) {

						for (IBodyElement icel : cel.getBodyElements()) {
							if (icel instanceof XWPFSDT) {
								if (((XWPFSDT) icel).getTag()
										.contains(String.format("%02d", i) + customs.get(Consts.NOMBRE_FIRMANTE))) {
									empleado.setNombre(((XWPFSDT) icel).getContent().getText());
									listFirmante.add(empleado);
									empleado = new Empleado();
								} else if (((XWPFSDT) icel).getTag()
										.contains(String.format("%02d", i) + customs.get(Consts.CARGO_FIRMANTE))) {
									empleado = listFirmante.get(i);
									empleado.setCargo(((XWPFSDT) icel).getContent().getText());
									listFirmante.set(i, empleado);
									empleado = new Empleado();
								} else if (((XWPFSDT) icel).getTag()
										.contains(String.format("%02d", i) + customs.get(Consts.DEPENDENCIA_FIRMANTE))) {
									empleado = listFirmante.get(i);
									empleado.setDependencia(((XWPFSDT) icel).getContent().getText());

									CustomProperties info = word.getCustomProperties();
									String rol = String.format("%02d", i) + customs.get(Consts.ID_FIRMANTE);
									String sgr = String.format("%02d", i) + customs.get(Consts.SGR_FIRMANTE);

									empleado.setRol(info.getProperty(rol).getLpwstr());
									empleado.setSiglaremitente(info.getProperty(sgr).getLpwstr());

									/*
									if (empleado.getSiglaremitente().equals(Consts.SIGLA_FIRMANTE)) {
										throw new NumberFormatException(" - La sigla del firmante no es valida");
									}
									*/

									listFirmante.set(i, empleado);
									empleado = new Empleado();
								}
							}
						}

					}
				}
			}
		}

		return listFirmante;
	}
	

		
		/*
		 * Leer Copias Documento
		 */
		public static List<Empleado> leerCopias(XWPFDocument docx,XWPFWordExtractor word, Map<String,String> customs){
			int cons = 0;
			String delimeter = ",";
			
			String sepNombre = "\31"; //Separador para el atributo "Nombre" en las copias
			String sepDep = "\31"+"\31"; //Separador para el atributo "Dependencia" en las copias
			
			Empleado empleado = new Empleado();
			List<Empleado> listCopias = new ArrayList<Empleado>();

			Iterator<IBodyElement> elemns = docx.getBodyElements().stream().filter(c -> c instanceof XWPFSDT)
					.collect(Collectors.toList()).iterator();
			while (elemns.hasNext()) {
				IBodyElement uelemns = elemns.next();
				if (uelemns != null && ((XWPFSDT) uelemns).getTag().contains(String.format("%02d", cons) + customs.get(Consts.COPIAS))) {
					String cop = ((XWPFSDT) uelemns).getContent().getText();
					
					
					String[] copias = cop.split(delimeter);
					
					 if ( copias.length==3 )
	                 {
						 empleado.setNombre(copias[0]);
					     empleado.setCargo(copias[1].replaceFirst(" ", ""));
						 empleado.setDependencia(copias[2].replaceFirst(" ", ""));                       
	                 }
	                 else if ( copias.length == 2 )
	                 {
	                     if ( copias[0].contains(sepDep) )
	                     {
	                    	 empleado.setNombre("");
	    				     empleado.setCargo(copias[1].replaceFirst(" ", ""));
	    					 empleado.setDependencia(copias[0]);    
	                     } else if (copias[0].contains(sepNombre) && copias[1].contains(sepDep) )
	                     {
	                    	 empleado.setNombre(copias[0]);
	    				     empleado.setCargo("");
	    					 empleado.setDependencia(copias[1].replaceFirst(" ", ""));    
	                     }
	                     else
	                     {
	                    	 empleado.setNombre(copias[0]);
	    				     empleado.setCargo(copias[1].replaceFirst(" ", ""));
	    					 empleado.setDependencia("");    
	                     }

	                 }else if( copias.length == 1 )
	                 {
	                     if (copias[0].contains(sepDep))
	                     {
	                    	 empleado.setNombre("");
	    				     empleado.setCargo("");
	    					 empleado.setDependencia(copias[0]);    
	                     }
	                     else
	                     {
	                    	 empleado.setNombre(copias[0]);
	    				     empleado.setCargo("");
	    					 empleado.setDependencia("");    
	                     }
	                 }
					 
					 CustomProperties info = word.getCustomProperties();
						String rol = String.format("%02d", cons) + customs.get(Consts.PCR_COPIAS);
						
						empleado.setRol(info.getProperty(rol).getLpwstr());

						cons++;
						listCopias.add(empleado);
						empleado = new Empleado();

				}
			}
			return listCopias;
		}

	/*
	 * Leer Tipologia Documento
	 */
	public static String leerTipologia(XWPFWordExtractor word,Map<String,String> customs) {

		String tipologia;

		CustomProperties info = word.getCustomProperties();

		tipologia = (info.getProperty(customs.get(Consts.TIPOLOGIA_PLANTILLA)).getLpwstr());

		return tipologia;
	}
	
	public static void cargarListado(String texto, int posicion, Empleado p) {

		if (posicion == 0) {
			p.setNombre(texto);
		} else if (posicion == 1) {
			p.setCargo(texto);
		} else if (posicion == 2) {
			p.setDependencia(texto);
		} else if (posicion == 3) {
			p.setRol(texto);
		}

	}

	/**
	 * Leer Destinatarios Documento CA
	 */
	public static List<Empleado> leerDestinatariosCA(XWPFDocument docx, XWPFWordExtractor word,
			Map<String, String> customs) {

		int cons = 0;
		Empleado empleado = new Empleado();
		List<Empleado> listDestinatario = new ArrayList<Empleado>();

		String personalizado = leerPersonalizarDocumento(word, customs);

		if (personalizado.equals("True")) {

			try {

				List<XWPFTable> table = docx.getTables();

				for (int i = table.size() - 1; i < table.size(); i++) {
					XWPFTable xwpfTable = table.get(i);
					List<XWPFTableRow> row = xwpfTable.getRows();
					boolean flag = false;
					for (XWPFTableRow xwpfTableRow : row) {
						if (flag) {
							List<XWPFTableCell> cell = xwpfTableRow.getTableCells();
							for (int j = 0; j < cell.size(); j++) {
								XWPFTableCell xwpfTableCell = cell.get(j);
								if (xwpfTableCell != null) {
									String texto = xwpfTableCell.getText();
									cargarListado(texto, j, empleado);
								}
							}
							cons++;
							listDestinatario.add(empleado);
							empleado = new Empleado();
						}
						flag= true;
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {

			Iterator<IBodyElement> elemns = docx.getBodyElements().stream().filter(t -> t instanceof XWPFParagraph)
					.collect(Collectors.toList()).iterator();

			while (elemns.hasNext()) {
				IBodyElement unelemns = elemns.next();

				Iterator<IRunElement> tag = ((XWPFParagraph) unelemns).getIRuns().stream()
						.filter(p -> p instanceof XWPFSDT).collect(Collectors.toList()).iterator();

				while (tag.hasNext()) {
					IRunElement untag = tag.next();

					if (untag instanceof XWPFSDT) {
						String text = ((XWPFSDT) untag).getTag();
						/*
						 * Leer Destinatarios AppUtils ME
						 */
						if (text.contains(cons + customs.get(Consts.NOMBRE_DESTINO))) {
							String nombre = ((XWPFSDT) untag).getContent().getText();
							nombre = AppUtils.reemplazarCarateresEspeciales(nombre, Consts.REEMPLAZO_CARACTERES_ESPECIALES);
							empleado.setNombre(nombre);
						} else if (text.contains(cons + customs.get(Consts.CARGO_DESTINO))) {
							String cargo = ((XWPFSDT) untag).getContent().getText();
							cargo = AppUtils.reemplazarCarateresEspeciales(cargo, Consts.REEMPLAZO_CARACTERES_ESPECIALES);
							empleado.setCargo(cargo);
						} else if (text.contains(cons + customs.get(Consts.DEPENDENCIA_DESTINO))) {
							String dependencia = ((XWPFSDT) untag).getContent().getText();
							dependencia = AppUtils.reemplazarCarateresEspeciales(dependencia, Consts.REEMPLAZO_CARACTERES_ESPECIALES);
							empleado.setDependencia(dependencia);
						} else if (text.contains(cons + customs.get(Consts.CIUDAD_DESTINO))) {

							CustomProperties info = word.getCustomProperties();
							String rol = String.format("%02d", cons) + customs.get(Consts.PCR_DESTINO);
							empleado.setRol(info.getProperty(rol).getLpwstr());

							cons++;
							listDestinatario.add(empleado);
							empleado = new Empleado();
						}

					}
				}
			}

		}
		return listDestinatario;
	}

	/*
	 * Leer AnexosFisicos Documento
	 */
	public static String leerAnexosFisicos (XWPFWordExtractor word,Map<String, String> customs) {

		String anexos;

		CustomProperties info = word.getCustomProperties();

		anexos = info.getProperty(customs.get(Consts.ANEXOS_FISICOS)).getLpwstr();			

		return anexos;
	}

	/*
	 * Leer AnexosElectronicos Documento
	 */
	public static String leerAnexosElectronicos (XWPFWordExtractor word,Map<String,String> customs){

		String anexos;

		CustomProperties info = word.getCustomProperties();

		anexos = info.getProperty(customs.get(Consts.ANEXOS_ELECTRONICOS)).getLpwstr();			

		return anexos;
	}

	/*
	 * Leer Tipo Documento
	 */
	public static String leerTipoDocumento (XWPFWordExtractor word,Map<String,String> customs){

		String tipoDocumento;

		CustomProperties info = word.getCustomProperties();

		tipoDocumento = info.getProperty(customs.get(Consts.TIPO_PLANTILLA)).getLpwstr();			

		return tipoDocumento;
	}

	
	
	/*
	 * Leer propiedad DobleFirmante Documento
	 */
	public static Boolean leerEsDobleFirmante (XWPFWordExtractor word,Map<String,String> customs){

		Boolean dobleFirmante;

		CustomProperties info = word.getCustomProperties();

		dobleFirmante = Boolean.valueOf(info.getProperty(customs.get(Consts.ES_DOBLE_FIRMANTE)).getLpwstr());	

		return dobleFirmante;
	}
	
	
	/*
	 * Leer Version Plantilla Documento
	 */
	public static String leerVersionPlantilla (XWPFWordExtractor word,Map<String,String> customs){

		String versionPlantilla;

		CustomProperties info = word.getCustomProperties();

		versionPlantilla = info.getProperty(customs.get(Consts.VERSION_PLANTILLA)).getLpwstr();	

		return versionPlantilla;
	}
	
	
	/*
	 * Leer Personalizar Documento
	 */
	public static String leerPersonalizarDocumento (XWPFWordExtractor word,Map<String,String> customs){

		String personalizada;

		CustomProperties info = word.getCustomProperties();

		personalizada = info.getProperty(customs.get(Consts.PERSONALIZADA_PLANTILLA)).getLpwstr();			

		return personalizada;
	}

	/*
	 * Leer Documento Ingles
	 */
	public static String leerDocumentoIngles (XWPFWordExtractor word,Map<String,String> customs){

		String idioma;

		CustomProperties info = word.getCustomProperties();

		idioma = info.getProperty(customs.get(Consts.IDIOMA_PLANTILLA)).getLpwstr();			

		return idioma;
	}
	
	/*
	 * Leer TipoEnvio Documento
	 */
	public static String leerTipoEnvioDocumento (XWPFWordExtractor word,Map<String, String> customs){

		String envio;

		CustomProperties info = word.getCustomProperties();

		envio = info.getProperty(customs.get(Consts.TIPO_ENVIO_PLANTILLA)).getLpwstr();			

		return envio;
	}
	
	public static Boolean leerEsFondoIndependiente (XWPFWordExtractor word,Map<String, String> customs){
		
		Boolean esFondoIndependiente;
		
		CustomProperties info = word.getCustomProperties();
		
		esFondoIndependiente = Boolean.valueOf(info.getProperty(customs.get(Consts.ES_FONDO_INDEPENDIENTE)).getLpwstr());			
		
		return esFondoIndependiente;
	}
	
	public static Boolean leerEsCorreoCertificado (XWPFWordExtractor word,Map<String, String> customs){
		
		Boolean esCorreoCertificado;
		
		CustomProperties info = word.getCustomProperties();
		
		esCorreoCertificado = Boolean.valueOf(info.getProperty(customs.get(Consts.ES_CORREO_CERTIFICADO)).getLpwstr());			
		
		return esCorreoCertificado;
	}
	
	public static Boolean leerEsImpresionArea (XWPFWordExtractor word,Map<String, String> customs){
		
		Boolean esImpresionArea;
		
		CustomProperties info = word.getCustomProperties();
		
		esImpresionArea = Boolean.valueOf(info.getProperty(customs.get(Consts.ES_IMPRESION_AREA)).getLpwstr());			
		
		return esImpresionArea;
	}
	
	public static String leerCorreoCopia (XWPFWordExtractor word,Map<String, String> customs){
		
		String correoCopia;
		
		CustomProperties info = word.getCustomProperties();
		
		correoCopia = info.getProperty(customs.get(Consts.CORREO_COPIA)).getLpwstr();
		
		System.out.println("Correo copia a buzon corporativo antes: ["+correoCopia+"]");
		//Vamaya - Se elimina el salto de linea en los correos copia (También se realizó ajuste en Add-In, pero no en la versión  que tiene instalada la mayoría del banco)
		correoCopia = correoCopia.replace(Consts.CARACTER_RETORNO_CORREO_COPIA, "");
		System.out.println("Correo copia a buzon corporativo después: ["+correoCopia+"]");
		
		return correoCopia;
	}
	
}
