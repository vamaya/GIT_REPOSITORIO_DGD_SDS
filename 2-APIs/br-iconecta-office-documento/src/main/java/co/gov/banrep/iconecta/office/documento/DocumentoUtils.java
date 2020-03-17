package co.gov.banrep.iconecta.office.documento;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.poi.POIXMLProperties.CustomProperties;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFSDT;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBody;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDocument1;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STHeightRule;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STPageOrientation;

import co.gov.banrep.iconecta.office.documento.entity.Atributo;
import co.gov.banrep.iconecta.office.documento.entity.MetadatosPlantilla;
import co.gov.banrep.iconecta.office.documento.entity.Documento;
import co.gov.banrep.iconecta.office.documento.entity.Empleado;
import co.gov.banrep.iconecta.office.documento.entity.Sticker;
import co.gov.banrep.iconecta.office.documento.entity.Validaciones;
import co.gov.banrep.iconecta.office.documento.entity.ValoresSticker;
import co.gov.banrep.iconecta.office.documento.utils.AppUtils;
import co.gov.banrep.iconecta.office.documento.utils.Consts;

public final class DocumentoUtils {
	
	private static final String PAQUETE_SETTINGS = "/word/settings.xml";	

	private DocumentoUtils() {
		throw new AssertionError();
	};

	

	
	/**
	 * Leer Documento
	 * 
	 * @param rutaDocumento parametro que recibe la ubicación del archivo a leer
	 * @return parametro que retorna la categoria con los datos de la plantilla
	 * @throws IOException si no se encuentra documento a revisar
	 */
	public static MetadatosPlantilla leerDocumento(String raizLog, String rutaDocumento, Map<String, String> customs)
			throws IOException, NumberFormatException {

		//SE INICIALIZAN VARIABLES, NECESARIAS PARA EL USO DE WORD	
		String tipologia = null;
		XWPFWordExtractor word = null;
		XWPFDocument docx = null;
		FileInputStream file = null;
		
		//SE INICIALIZAN VARIABLES, NECESARIAS PARA EXTRAER Y GUARDAR LOS DATOS	
		MetadatosPlantilla categoria = new MetadatosPlantilla();
		Validaciones validacion = new Validaciones();	

		// SE ABRE EL ARCHIVO UBICADO EN AL RUTA ESPECIFICADA
		try {
			file = new FileInputStream(rutaDocumento);
			docx = new XWPFDocument(file);
			word = new XWPFWordExtractor(docx);
		} catch (IOException e) {
			System.out.println(raizLog + "ERROR - El documento no se encontraba en la ruta especificada");
			throw new IOException("El documento no se encontraba en la ruta especificada");
		} catch (IllegalArgumentException e) {
			System.out.println(raizLog + "ERROR - El documento está vacio o dañado");
			throw new IllegalArgumentException("El documento está vacio o dañado");
		}

		try {
			//SE REVISA SI EL DOCUMENTO TIENE EL TAG DE FIRMANTE, SI NO LO TIENE SE ASUME QUE NO FUE GENERADO A PARTIR DE LA PLANTILLA
			boolean val = validacion.validartags(docx);
			categoria.setEsGeneradoDesdePlantilla(val);
			//SI EL DOCUMENTO FUE GENERADO A PARTIR DE LA PLANTILLA SE EXTRAEN LOS DATOS
			if (val) {
				//DATOS EN COMÚN PARA CARTA Y MEMORANDO
				categoria.setAsunto(Documento.leerAsunto(docx, customs));
				tipologia = (Documento.leerTipologia(word, customs));
				categoria.setTipologia(tipologia);
				categoria.setCopias(Documento.leerCopias(docx, word, customs));
				categoria.setAnexosElectronicos(Documento.leerAnexosElectronicos(word, customs));
				categoria.setAnexosFisicos(Documento.leerAnexosFisicos(word, customs));
				categoria.setTipoDocumento(Documento.leerTipoDocumento(word, customs));
				categoria.setEsFondoIndependiente(Documento.leerEsFondoIndependiente(word, customs));
				categoria.setEsDobleFirmante(Documento.leerEsDobleFirmante(word, customs));
				categoria.setVersionPlantilla(Documento.leerVersionPlantilla(word, customs));
				
				if (tipologia.contentEquals(customs.get(Consts.TIPOLOGIA_PLANTILLA_CA))) {//DATOS PARA CARTA
					categoria.setIdiomaIngles(Documento.leerDocumentoIngles(word, customs));
					categoria.setDestinatarios(Documento.leerDestinatariosCA(docx, word, customs));
					categoria.setFirmantes(Documento.leerFirmantesCA(docx, word, customs));
					categoria.setPersonalizarDocumento(Documento.leerPersonalizarDocumento(word, customs));
					categoria.setTipoEnvio(Documento.leerTipoEnvioDocumento(word, customs));
					categoria.setEsCorreoCertificado(Documento.leerEsCorreoCertificado(word, customs));
					categoria.setEsImpresionArea(Documento.leerEsImpresionArea(word, customs));
					categoria.setCorreoCopia(Documento.leerCorreoCopia(word, customs));
				}else { //DATOS PARA MEMORANDO
					categoria.setDestinatarios(Documento.leerDestinatariosMe(docx, word, customs));
					categoria.setFirmantes(Documento.leerFirmantesME(docx, word, customs));
				}	
				

			} 
			
			// SE CIERRAN LOS ARCHIVOS ABIERTOS			
			word.close();
			docx.close();
			file.close();
			System.out.println("CERRÓ DOCS AL TERMINAR VALIDACIONES...");

		} catch (Exception e) {
			System.out.println(raizLog + "ERROR - No se pudieron leer los metadatos de la plantilla");
			throw new NoSuchElementException("No se pudieron leer los metadatos de la plantilla");
		}finally {
			// SE CIERRAN LOS ARCHIVOS ABIERTOS			
			word.close();
			docx.close();
			file.close();
			System.out.println("CERRÓ DOCS EN EL FINALLY...");
		}

		return categoria;
	}
	
	
	

	//TODO BORRAR SI SE DEFINE OBSOLETA
	/**
	 * Leer Documento
	 * @param documento parametro que recibe el archivo a leer
	 * @return parametro que returna la categoria con los datos de la plantilla
	 * @throws IOException si no se encuentra documento a revisar
	 */
	public static MetadatosPlantilla leerDocumento(String documento, Map<String, String> customs) throws IOException, NumberFormatException {	
		
		if (documento == null) {
			throw new NullPointerException("El parametro documento no debe ser nulo");
		}

		MetadatosPlantilla categoria = new MetadatosPlantilla();
		Validaciones validacion = new Validaciones();
		String tipologia = null;
		XWPFWordExtractor word = null;
		XWPFDocument docx = null;
		FileInputStream file = null;

		try {
			file = new FileInputStream(documento);
			docx = new XWPFDocument(file);
			word = new XWPFWordExtractor(docx);
		}catch (IOException e) {
			throw new IOException("El archivo [" + documento + "] no pudo ser leido");
		}catch (NotOfficeXmlFileException e) {
			throw new NotOfficeXmlFileException("El documento no es Office XML - "+ e.getMessage() );
		}catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("El archivo [" + documento + "] esta vacio o dañado - "+ e.getMessage());
		}
		
			
		try {
			// SE REVISA SI EL DOCUMENTO TIENE EL TAG DE FIRMANTE, SI NO LO TIENE SE ASUME
			// QUE NO FUE GENERADO A PARTIR DE LA PLANTILLA
			boolean val = validacion.validartags(docx);
			categoria.setEsGeneradoDesdePlantilla(val);

			if (val) {
				tipologia = (Documento.leerTipologia(word, customs));
				if (tipologia.contentEquals(customs.get(Consts.TIPOLOGIA_PLANTILLA_CA))) {
					categoria.setDestinatarios(Documento.leerDestinatariosCA(docx, word, customs));
					categoria.setAsunto(Documento.leerAsunto(docx, customs));
					categoria.setFirmantes(Documento.leerFirmantesCA(docx, word, customs));
					categoria.setCopias(Documento.leerCopias(docx, word, customs));
					categoria.setTipologia(Documento.leerTipologia(word, customs));
					categoria.setAnexosElectronicos(Documento.leerAnexosElectronicos(word, customs));
					categoria.setAnexosFisicos(Documento.leerAnexosFisicos(word, customs));
					categoria.setTipoDocumento(Documento.leerTipoDocumento(word, customs));
					categoria.setIdiomaIngles(Documento.leerDocumentoIngles(word, customs));
					categoria.setPersonalizarDocumento(Documento.leerPersonalizarDocumento(word, customs));
					categoria.setTipoEnvio(Documento.leerTipoEnvioDocumento(word, customs));
					categoria.setEsFondoIndependiente(Documento.leerEsFondoIndependiente(word, customs));
					categoria.setEsCorreoCertificado(Documento.leerEsCorreoCertificado(word, customs));
					categoria.setEsImpresionArea(Documento.leerEsImpresionArea(word, customs));
					categoria.setCorreoCopia(Documento.leerCorreoCopia(word, customs));
					categoria.setEsDobleFirmante(Documento.leerEsDobleFirmante(word, customs));
					categoria.setVersionPlantilla(Documento.leerVersionPlantilla(word, customs));
				} else {
					categoria.setDestinatarios(Documento.leerDestinatariosMe(docx, word, customs));
					categoria.setAsunto(Documento.leerAsunto(docx, customs));
					categoria.setFirmantes(Documento.leerFirmantesME(docx, word, customs));
					categoria.setCopias(Documento.leerCopias(docx, word, customs));
					categoria.setTipologia(Documento.leerTipologia(word, customs));
					categoria.setAnexosElectronicos(Documento.leerAnexosElectronicos(word, customs));
					categoria.setTipoDocumento(Documento.leerTipoDocumento(word, customs));
					categoria.setEsFondoIndependiente(Documento.leerEsFondoIndependiente(word, customs));
					categoria.setEsDobleFirmante(Documento.leerEsDobleFirmante(word, customs));
					categoria.setVersionPlantilla(Documento.leerVersionPlantilla(word, customs));

					if (categoria.getTipoDocumento().equals(Consts.TIPO_NORMAL)) {

						for (Empleado p : categoria.getDestinatarios()) {

							if (p.getRol().contains(Consts.TIPO_CONFIDENCIAL.toUpperCase())) {
								throw new NullPointerException("Error memorando normal con destinos confidenciales");
							}
						}
					}

					if (categoria.getTipoDocumento().equals(Consts.TIPO_CONFIDENCIAL)) {

						for (Empleado p : categoria.getDestinatarios()) {

							if (!p.getRol().contains(Consts.TIPO_CONFIDENCIAL.toUpperCase())) {
								throw new NullPointerException("Error memorando confidencial con destinos normales");
							}
						}
					}

				}

			} else {
				// throw new NullPointerException("Etiquetas no encontradas en documento,
				// validar version plantilla");
				System.out.println("El archivo [" + documento + "] no fue generado a partir de las plantillas de iConecta ");
			}

		} catch (NullPointerException e) {
			throw new NullPointerException("Documento:"+documento+" No puede ser leido, validar version de plantilla "+e);	
		} catch (NumberFormatException e) {
			throw new NumberFormatException("Documento: "+documento+" Hay caracteres no compatibles en la plantilla "+e);
		}finally {
			word.close();
			docx.close();
			file.close();
		}

		return categoria;
	}
	

	/**
	 * Verificar Versión
	 * @param documento parametro que recibe el archivo a leer
	 * @param version parametro que recibe la version del archivo a verificar
	 * @return pametro que returna un boolean indicando si la version es correcta o inc
	 * @throws Exception 
	 */
	public static boolean verificarVersion(String documento, String version, String version2, Map<String, String> customs) throws Exception {

		if (documento == null) {
			throw new NullPointerException("El parametro documento no debe ser nulo");
		}
		if (version == null) {
			throw new NullPointerException("El parametro version no debe ser nulo");
		}

		boolean valversion = false;
		XWPFDocument docx = null;
		XWPFWordExtractor word = null;
		FileInputStream file = null;

		try {
			file = new FileInputStream(documento);
			docx = new XWPFDocument(file);

			word = new XWPFWordExtractor(docx);

			CustomProperties info = word.getCustomProperties();
			
			String versionPlantillaStr = info.getProperty(customs.get(Consts.VERSION_PLANTILLA)).getLpwstr();//Vamaya añadido
			
			Double versionPlantilla = Double.parseDouble(versionPlantillaStr);//Vamaya añadido
			Double versionProp = Double.parseDouble(version);//Vamaya añadido
			Double versionProp2 = Double.parseDouble(version2);//Vamaya añadido
			
			if(versionPlantilla<2) { //Las versiones anteriores son tiene la esrtructura 1.X, las nuevas versiones tiene la estructura 2.X
				System.out.println("la version de la plantilla addin es [" + versionPlantillaStr + "] la version de la propiedad de correspondencia es [" + version + "]");
				
				if (versionPlantilla.equals(versionProp)) {
					valversion = true;
				}
				
			}else {
				System.out.println("la version de la plantilla addin es [" + versionPlantillaStr + "] la version de la propiedad de correspondencia es [" + version2 + "]");
				
				System.out.println("Se está trabajando con la nueva versión de Plantilla");
				
				if (versionPlantilla.equals(versionProp2)) {
					valversion = true;
				}
				
			}
			
			

		} catch (FileNotFoundException e) {
			throw new IOException("AppUtils " + documento + " No encontrada ", e);
		} catch(NullPointerException e){
			throw new NullPointerException("AppUtils " + documento + " No corresponde a la solicitada por el Sistema ");
		} catch(Exception e){
			throw new Exception("Error leyendo documento ");
		} finally {
			docx.close();
			word.close();
			safeClose(file);
		}

		return valversion;
	}

	
	
	/**
	 * 
	 * @param documento parametro que recibe el archivo a leer
	 * @param numeroRadicado parametro que recibe numeroRadicado generado por ContentServer(CS)
	 * @param fechaRadicado parametro que recibe fecha en que se genero el Radicado en CS
	 * @param cordenadas OPCIONAL- parametro que recibe las cordenadas de la tabla del encabezado para su edicion
	 * @throws IOException si no se puede leer el archivo de la plantilla
	 */
	public static void escribirDocumento(String raizLog, String documento, String numeroRadicado, Boolean esIdiomaIngles, Map<String, String> customs) throws IOException {

		if (documento == null) {
			throw new NullPointerException("El parametro documento no debe ser nulo");
		}
		if (numeroRadicado == null) {
			throw new NullPointerException("El parametro numero de radicado no debe ser nulo");
		}
		/*
		if (fechaRadicado == null) {
			throw new NullPointerException("El parametro fecha de radicado no debe ser nulo");
		}
		if (cordenadas == null) {
			throw new NullPointerException("El parametro cordenadas no debe ser nulo");
		}
		*/
		if (numeroRadicado.equals("")) {
			throw new IllegalArgumentException("El parametro nuemero de radicado no debe estar vacio");
		}
		

		//SE CARGAN LOS VALORES NECESARIOS PARA LLENAR EL ENCABEZADO DEL DOCUMENTO
		Map<String, Integer> cordenadas = new HashMap<>();
		
		DateFormat dateFormat = (!esIdiomaIngles)
				? new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("es","ES"))
				: new SimpleDateFormat("MMMM, d yyyy", Locale.ENGLISH);

		String fechaRadicado = dateFormat.format(new Date());

		cordenadas.put("header", 1);
		cordenadas.put("table", 0);
		cordenadas.put("row", 1);
		cordenadas.put("cell", 2);
		cordenadas.put("paragraph", 0);
		cordenadas.put("text", 0);
		
		XWPFDocument docx = null;
		FileOutputStream fileoutStream = null;
		FileInputStream file= null;
		Iterator<XWPFHeader> iterheader  = null;
		try {
			file = new FileInputStream(documento);
			docx = new XWPFDocument(file);

			iterheader = docx.getHeaderList().stream().filter(c -> c.getText().contains(customs.get(Consts.NUMERO_RADICADO)))
					.collect(Collectors.toList()).iterator();
			if(iterheader.hasNext() == false) {
				throw new NullPointerException("No se Encuentra NroRadicado en plantilla, valide version plantilla");
			}
			while(iterheader.hasNext()) {
				XWPFHeader uheader = iterheader.next();

				XWPFTable tabla = uheader.getTableArray(cordenadas.get("table"));
				List<XWPFTableRow> fila = tabla.getRows();
				Iterator<XWPFTableRow> iterfila = fila.iterator();
				while (iterfila.hasNext()) {
					XWPFTableRow fila1 = iterfila.next();
					XWPFTableCell celda = fila1.getCell(cordenadas.get("cell"));
					XWPFParagraph parrafo = celda.getParagraphArray(cordenadas.get("paragraph"));
					List<XWPFRun> run = parrafo.getRuns();
					Iterator<XWPFRun> iterrun = run.iterator();
					while (iterrun.hasNext()) {
						XWPFRun run1 = iterrun.next();
						String text = run1.getText(cordenadas.get("text"));
						if (text != null && text.contains(customs.get(Consts.NUMERO_RADICADO))) {
							text = text.replace(customs.get(Consts.NUMERO_RADICADO), numeroRadicado);
							run1.setText(text, 0);
						} else if (text != null && text.contains(customs.get(Consts.FECHA_RADICADO))) {
							text = text.replace(customs.get(Consts.FECHA_RADICADO), fechaRadicado);
							run1.setText(text, 0);
						}
					}

				}
			}
			
			//Vamaya: Bloque añadido para eliminar espacios dentro de una tabla con controladores de contenido
			boolean isSTD = false;
			
			for (XWPFTable tbl : docx.getTables()) {
				   for (XWPFTableRow row : tbl.getRows()) {
				      for (XWPFTableCell cell : row.getTableCells()) {
				    	  for (IBodyElement icel : cell.getBodyElements()) {
				    		  if(icel instanceof XWPFSDT) {
				    			  isSTD = true;
				    		  }
				    		  if (icel instanceof XWPFParagraph && isSTD) {
				    			  if(((XWPFParagraph) icel).getParagraphText().isEmpty() ) {
				    				  XWPFParagraph paragraph = ((XWPFParagraph) icel);
				    				  int pPos = cell.getParagraphs().indexOf(paragraph);		    				  
				    				  cell.removeParagraph(pPos);
				    				  isSTD = false;
				    			  }
				    		  }		    		  
				    	  }		    	  
				      }
				   }
			}
			//
			
			//Bloque que verifica la existencia de refencias externas en los documentos
			try {
				eliminarReferenciasExternas(raizLog, docx, documento);
			} catch (Exception e) {
				System.out.println(raizLog + "WARNING - Al documento [" + documento + "] NO se le pudieron eliminar las referencias externas");
				e.printStackTrace();
			}
			
			fileoutStream = new FileOutputStream(documento);
			docx.write(fileoutStream);

		} catch (FileNotFoundException e) {
			throw new IOException(raizLog + "ruta al documento [ " + documento + "] No encontrada o se encuentra en Uso", e);
		} finally {
			docx.close();
			safeClose(file);
			safeClose(fileoutStream);
			
		}
	}

	
	
	
	
	//TODO BORRAR POR SER OBSOLETA
	/**
	 * 
	 * @param documento parametro que recibe el archivo a leer
	 * @param numeroRadicado parametro que recibe numeroRadicado generado por ContentServer(CS)
	 * @param fechaRadicado parametro que recibe fecha en que se genero el Radicado en CS
	 * @param cordenadas OPCIONAL- parametro que recibe las cordenadas de la tabla del encabezado para su edicion
	 * @throws IOException si no se puede leer el archivo de la plantilla
	 */
	public static void escribirDocumento(String documento, String numeroRadicado, String fechaRadicado,
			Map<String, Integer> cordenadas,Map<String, String> customs) throws IOException {

		if (documento == null) {
			throw new NullPointerException("El parametro documento no debe ser nulo");
		}
		if (numeroRadicado == null) {
			throw new NullPointerException("El parametro numero de radicado no debe ser nulo");
		}
		if (fechaRadicado == null) {
			throw new NullPointerException("El parametro fecha de radicado no debe ser nulo");
		}
		if (cordenadas == null) {
			throw new NullPointerException("El parametro cordenadas no debe ser nulo");
		}
		if (numeroRadicado.equals("")) {
			throw new IllegalArgumentException("El parametro nuemero de radicado no debe estar vacio");
		}
		
		XWPFDocument docx = null;
		FileOutputStream fileoutStream = null;
		FileInputStream file= null;
		Iterator<XWPFHeader> iterheader  = null;
		try {
			file = new FileInputStream(documento);
			docx = new XWPFDocument(file);

			iterheader = docx.getHeaderList().stream().filter(c -> c.getText().contains(customs.get(Consts.NUMERO_RADICADO)))
					.collect(Collectors.toList()).iterator();
			if(iterheader.hasNext() == false) {
				throw new NullPointerException("No se Encuentra NroRadicado en plantilla, valide version plantilla");
			}
			while(iterheader.hasNext()) {
				XWPFHeader uheader = iterheader.next();

				XWPFTable tabla = uheader.getTableArray(cordenadas.get("table"));
				List<XWPFTableRow> fila = tabla.getRows();
				Iterator<XWPFTableRow> iterfila = fila.iterator();
				while (iterfila.hasNext()) {
					XWPFTableRow fila1 = iterfila.next();
					XWPFTableCell celda = fila1.getCell(cordenadas.get("cell"));
					XWPFParagraph parrafo = celda.getParagraphArray(cordenadas.get("paragraph"));
					List<XWPFRun> run = parrafo.getRuns();
					Iterator<XWPFRun> iterrun = run.iterator();
					while (iterrun.hasNext()) {
						XWPFRun run1 = iterrun.next();
						String text = run1.getText(cordenadas.get("text"));
						if (text != null && text.contains(customs.get(Consts.NUMERO_RADICADO))) {
							text = text.replace(customs.get(Consts.NUMERO_RADICADO), numeroRadicado);
							run1.setText(text, 0);
						} else if (text != null && text.contains(customs.get(Consts.FECHA_RADICADO))) {
							text = text.replace(customs.get(Consts.FECHA_RADICADO), fechaRadicado);
							run1.setText(text, 0);
						}
					}

				}
			}

			/*
			 * Convertir Archivo a PDF
			 */
			// PdfOptions options = PdfOptions.create();
			// OutputStream out = new FileOutputStream(new File("Memorando.pdf"));
			// PdfConverter.getInstance().convert(docx, out,options);
			
			//Vamaya: Bloque añadido para eliminar espacios dentro de una tabla con controladores de contenido
			boolean isSTD = false;
			
			for (XWPFTable tbl : docx.getTables()) {
				   for (XWPFTableRow row : tbl.getRows()) {
				      for (XWPFTableCell cell : row.getTableCells()) {
				    	  for (IBodyElement icel : cell.getBodyElements()) {
				    		  if(icel instanceof XWPFSDT) {
				    			  isSTD = true;
				    		  }
				    		  if (icel instanceof XWPFParagraph && isSTD) {
				    			  if(((XWPFParagraph) icel).getParagraphText().isEmpty() ) {
				    				  XWPFParagraph paragraph = ((XWPFParagraph) icel);
				    				  int pPos = cell.getParagraphs().indexOf(paragraph);		    				  
				    				  cell.removeParagraph(pPos);
				    				  isSTD = false;
				    			  }
				    		  }		    		  
				    	  }		    	  
				      }
				   }
			}
			//
			
			//Bloque que verifica la existencia de refencias externas en los documentos
			try {
				eliminarReferenciasExternas(docx, documento);
			} catch (Exception e) {
				System.out.println("WARNING - Al documento [" + documento + "] NO se le pudieron eliminar las refenrencias externas");
				e.printStackTrace();
			}
			
			fileoutStream = new FileOutputStream(documento);
			docx.write(fileoutStream);

		} catch (FileNotFoundException e) {
			throw new IOException("AppUtils " + documento + " No encontrada o se encuentra en Uso", e);
		} finally {
			docx.close();
			safeClose(file);
			safeClose(fileoutStream);
			
		}
	}

	
	
	/**
	 * Metodo para validar numero de radicado en la plantilla
	 * @param documento parametro que recibe el archivo a leer
	 * @throws IOException si no se puede leer el archivo de la plantilla
	 */
	public static boolean validarNumeroRadicado(String documento, Map<String, String> customs) throws IOException {
		
		boolean result = true;
		
		if (documento == null) {
			throw new NullPointerException("El parametro documento no debe ser nulo");
		}
		
		XWPFDocument docx = null;
		FileInputStream file = null;
		try {
			file = new FileInputStream(documento);
			docx = new XWPFDocument(file);

			Iterator<XWPFHeader> iterheader = docx.getHeaderList().stream().filter(c -> c.getText().contains(customs.get(Consts.NUMERO_RADICADO)))
					.collect(Collectors.toList()).iterator();
			if(iterheader.hasNext() == false) {
					result = false;
			}
			
		} catch (FileNotFoundException e) {
			throw new IOException("AppUtils " + documento + " No encontrada o se encuentra en Uso", e);
		} finally {
			docx.close();
			safeClose(file);
		}
		
		return result;
	}


	/**
	 * Método que genera el documento con los Stickers y retorna un DataHandler
	 * @param stickers parametro tipo Lista que contiene los Stickers a insertar en el documento
	 * @return handler variable tipo DataHandler que contiene un objeto tipo documento
	 * @throws IOException
	 */
	public static DataHandler generarDocumentoSticker(List<Sticker> stickers, String idWorkFLow, String ruta) throws IOException {

		//CREACIÓN DOCUMENTO
		XWPFDocument documentoSticker = new XWPFDocument();

		//CODIGO DE ORIENTACIÓN Y TAMAÑO DE PÁGINA
		confOrientacionyTamPag(documentoSticker, STPageOrientation.LANDSCAPE, ValoresSticker.getAnchopaginaPts(), ValoresSticker.getAltopaginaPts());
		
		//CÓDIGO MARGENES DE PÁGINA
		confMargenPag(documentoSticker, ValoresSticker.getMargeninferior());
		File file = null;
		FileOutputStream salida = null;
		try {
			file = new File(ruta + idWorkFLow+"documentoSticker.docx");
			salida = new FileOutputStream(file);

			System.out.println("Documento Creado Exitosamente");

			Iterator<Sticker> iterStickers = stickers.iterator();

			while (iterStickers.hasNext()) {
				Sticker unSticker = iterStickers.next();

				//CREACIÓN TABLA POI
				XWPFTable tablaSticker = documentoSticker.createTable();
				setTableAlignment(tablaSticker, STJc.CENTER);

				//CÓDIGO PARA CONTROL DE BORDES DE LA TABLA
				confBordesTbl(tablaSticker, STBorder.NONE, STBorder.NONE, STBorder.NONE, STBorder.NONE, STBorder.NONE, STBorder.NONE);

				System.out.println("Tabla creada en el documento" + "\n");

				List<Atributo> unaListaAtributos = unSticker.getAtributos();
				Iterator<Atributo> iterAtributos = unaListaAtributos.iterator();

				int filaActual = 0;
				XWPFParagraph textoTabla = null;

				while (iterAtributos.hasNext()) {

					Atributo unAtributo = iterAtributos.next();
					String etiqueta = unAtributo.getEtiqueta();
					String valor = unAtributo.getValor();
					int fila = unAtributo.getCoordenada().getFila();
					int columna = unAtributo.getCoordenada().getColumna();


					if(filaActual == fila) {

						XWPFTableRow primeraFila = tablaSticker.getRow(fila);
						textoTabla = primeraFila.getCell(columna).addParagraph();
						textoTabla.setAlignment(ParagraphAlignment.RIGHT);
						System.out.println(etiqueta + ":" + valor + " " + String.valueOf(fila) + " " + String.valueOf(columna));

						primeraFila.createCell();
						primeraFila.getCell(columna).removeParagraph(columna);
						setRun(textoTabla.createRun(), "Times New Roman", 12, valor, true, true);
						textoTabla.setSpacingBeforeLines(ValoresSticker.getEspacioSaltoPagina());
						filaActual = fila;

					}else if (filaActual != fila) {

						//CREACIÓN NUEVA FILA POI
						XWPFTableRow nuevaFila = tablaSticker.createRow();

						nuevaFila.setHeight((int)(ValoresSticker.getTwipporpulgada()*1/10)); // [set height 1/10 inch.]
						nuevaFila.getCtRow().getTrPr().getTrHeightArray(0).setHRule(STHeightRule.EXACT);

						XWPFParagraph textoTablaNueva = nuevaFila.getCell(columna).addParagraph();
						nuevaFila.createCell();
						nuevaFila.getCell(columna).removeParagraph(columna);
						setRun(textoTablaNueva.createRun(), "Times New Roman", 11, valor, false, false);

						System.out.println(etiqueta + ":" + valor + " " + String.valueOf(fila) + " " + String.valueOf(columna));

						filaActual = fila;
					}
				}

				//La siguiente condición verifica si existen más sticker en la lista con el fin de realizar un salto de página

				if(iterStickers.hasNext()) {
					textoTabla.setPageBreak(true);

					//

				}
			}
			documentoSticker.write(salida);

		}catch (FileNotFoundException e) {
			System.out.println("ERROR : El documento no puede ser creado o está en uso !");
			e.printStackTrace();
		}finally {
			documentoSticker.close();
			safeClose(salida);
			
		}

		FileDataSource source = new FileDataSource(ruta + idWorkFLow+"documentoSticker.docx");
		DataHandler handler = new DataHandler(source);
		return handler;
	}


	/**
	 * Método que configura la orientación y el tamaño de página del documento creado
	 * @param documentoSticker Documento a configurar
	 * @param orientacion Orientación de la página
	 * @param width Int, ancho de la página en puntos
	 * @param height Int, alto de la página en puntos
	 */
	public static void confOrientacionyTamPag(XWPFDocument documentoSticker, STPageOrientation.Enum orientacion, int ancho, int alto) {

		CTDocument1 tamDoc = documentoSticker.getDocument();
		CTBody cuerpoDoc = tamDoc.getBody();

		if (!cuerpoDoc.isSetSectPr()) {
			cuerpoDoc.addNewSectPr();
		}
		CTSectPr section = cuerpoDoc.getSectPr();

		if(!section.isSetPgSz()) {
			section.addNewPgSz();
		}
		CTPageSz pageSize = section.getPgSz();

		pageSize.setOrient(orientacion);
		pageSize.setW(BigInteger.valueOf(ancho));
		pageSize.setH(BigInteger.valueOf(alto));
	}

	/**
	 * Método que configura el margen inferior de las páginas del documento
	 * @param document Documento al cual se le van a configurar las márgenes
	 * @param margenInf, Long medida del margen inferior en puntos
	 */
	public static void confMargenPag(XWPFDocument documento, long margenInf) {

		CTSectPr sectPr = documento.getDocument().getBody().addNewSectPr();
		CTPageMar pageMar = sectPr.addNewPgMar();
		pageMar.setBottom(BigInteger.valueOf(margenInf));
	}

	/**
	 * Método que configura el estilo de los bordes de la tabla creada en el documento
	 * @param tabla
	 * @param borSup
	 * @param borInf
	 * @param borIzq
	 * @param borDer
	 * @param interH
	 * @param interV
	 */
	public static void confBordesTbl(XWPFTable tabla,STBorder.Enum borSup,STBorder.Enum borInf,STBorder.Enum borIzq,STBorder.Enum borDer,STBorder.Enum interH,STBorder.Enum interV) {

		// Controlador de bordes de la tabla
		CTTblPr tblpro = tabla.getCTTbl().getTblPr();

		// Añadir nuevos bordes externos y setearlos a "ninguno"
		CTTblBorders bordes = tblpro.addNewTblBorders();
		bordes.addNewTop().setVal(borInf);
		bordes.addNewBottom().setVal(borSup);
		bordes.addNewLeft().setVal(borInf);
		bordes.addNewRight().setVal(borDer);

		// Añadir nuevos bordes internos y setearlos a "ninguno"
		bordes.addNewInsideH().setVal(interH);
		bordes.addNewInsideV().setVal(interV);
	}


	/**
	 * Código para configurar las caracteristicas de estilo de texto en el documento
	 * @param run
	 * @param fontFamily 
	 * @param fontSize
	 * @param text
	 * @param bold
	 * @param addBreak
	 */
	private static void setRun(XWPFRun run, String fontFamily, int fontSize, String text, boolean bold, boolean addBreak) {
		run.setFontFamily(fontFamily);
		run.setFontSize(fontSize);
		run.setText(text);
		run.setBold(bold);
		if (addBreak) run.addBreak();
	}

	/**
	 * Método que ajusta la alineación de una tabla
	 * @param table
	 * @param justification
	 */
	private static void setTableAlignment(XWPFTable tabla, STJc.Enum justificacion) {
		CTTblPr tblPr = tabla.getCTTbl().getTblPr();
		CTJc jc = (tblPr.isSetJc() ? tblPr.getJc() : tblPr.addNewJc());
		jc.setVal(justificacion);
	}


	/**
	 * 
	 * @param documento parametro que recibe el archivo a leer
	 * @param imagen parametro que recibe imagen con firma a insertar en documento
	 * @throws IOException si no se puede leer el archivo de la plantilla
	 * @throws InvalidFormatException si la imagen no es valida o no se encuentra
	 */
	public static void insertarFirma(String documento, String imagen, Map<String, String> customs) throws IOException, InvalidFormatException {

		if (documento == null) {
			throw new NullPointerException("El parametro documento no debe ser nulo");
		}
		if (imagen == null) {
			throw new NullPointerException("El parametro imagen no debe ser nulo");
		}

		XWPFDocument docx = null;
		FileInputStream fileinput = null;
		FileOutputStream fileout = null;
		try {
			fileinput = new FileInputStream(documento);
			docx = new XWPFDocument(fileinput);
			Iterator<IBodyElement> elemns = docx.getBodyElements().stream().filter(c -> c instanceof XWPFTable)
					.collect(Collectors.toList()).iterator();
			while (elemns.hasNext()) {
				IBodyElement uelemns = elemns.next();

				XWPFTableRow row = ((XWPFTable) uelemns).getRow(0);
				Iterator<XWPFTableCell> itercel = row.getTableCells().iterator();
				while (itercel.hasNext()) {
					XWPFTableCell ucel = itercel.next();

					Iterator<IBodyElement> icel = ucel.getBodyElements().stream().filter(i -> i instanceof XWPFSDT)
							.collect(Collectors.toList()).iterator();

					while (icel.hasNext()) {
						IBodyElement uncel = icel.next();

						if (((XWPFSDT) uncel).getTag().contains(customs.get(Consts.FIRMA_FIRMANTE))) {
							XWPFParagraph parcel = ucel.addParagraph();
							parcel.setAlignment(ParagraphAlignment.CENTER);
							XWPFRun run = parcel.createRun();
							FileInputStream file = null;
							try {
								file = new FileInputStream(imagen);
								run.addPicture(file, XWPFDocument.PICTURE_TYPE_PNG, imagen,
										Units.toEMU(100), Units.toEMU(60));
							} catch (InvalidFormatException e) {
								throw new InvalidFormatException("Formato de Imagen no Compatible " + imagen + " debe ser (*.jgp - *.png)", e);
							} catch(FileNotFoundException e) {
								throw new IOException("Imagen " + imagen + " No encontrada", e);
							} finally {
								safeClose(file);
							}
						}
					}
				}
			}
			fileout = new FileOutputStream(documento);
			docx.write(fileout);

		} catch (FileNotFoundException e) {
			throw new IOException("AppUtils " + documento + " No encontrada ", e);
		} finally {
			docx.close();
			safeClose(fileinput);
			safeClose(fileout);
		}

	}
	
	/**
	 * Metodo para personalizar carta a partir de una tabla
	 * @param documento
	 * @param categoria
	 * @param nombreDoc
	 * @param customs
	 * @return
	 * @throws IOException
	 */
	/*@SuppressWarnings("resource")
	public static List<DataHandler> personalizarDocumentoCartaMasivo(String documento, MetadatosPlantilla categoria, String nombreDoc,
			Map<String, String> customs) throws IOException {

		if (documento == null) {
			throw new NullPointerException("El parametro documento no debe ser nulo");
		}
		if (categoria == null) {
			throw new NullPointerException("El parametro categoria no debe ser nulo");
		}
		if (nombreDoc == null) {
			throw new NullPointerException("El parametro nombreDoc no debe ser nulo");
		}

		List<DataHandler> listDataHandler = new ArrayList<>();

		int count = 0;
		XWPFDocument document1 = null;
		XWPFDocument doc = null;
		FileInputStream fis = null;
		FileInputStream fiso = null;
		FileOutputStream fos = null;

		try {

			for (Empleado per : categoria.getDestinatarios()) {

				try {
					fis = new FileInputStream(documento);
					fiso = new FileInputStream(documento);

				} catch (FileNotFoundException e) {
					throw new IOException("El documento [ " + documento + "] no pudo ser encontrado", e);
				}

				if (fis != null) {

					document1 = new XWPFDocument(fis);
					doc = new XWPFDocument(fiso);

					List<IBodyElement> bodyElements = document1.getBodyElements();
					int flag = 0;

					int espacios = 0;
					for (int i = 0; i < bodyElements.size(); i++) {
						IBodyElement element = bodyElements.get(i);
						if (element instanceof XWPFParagraph) {
							String texto = ((XWPFParagraph) element).getParagraphText();
							if (texto.contains(Consts.ETIQUETA_DESTINATARIO)) {
								espacios = i;
							}

						}

					}

					document1.removeBodyElement(espacios + 1);

					for (int i = 0; i < bodyElements.size(); i++) {
						IBodyElement element = bodyElements.get(i);

						if (element instanceof XWPFParagraph) {
							String text = ((XWPFParagraph) element).getParagraphText();

							if (text.contains(Consts.TITULO_TABLA)) {

								document1.removeBodyElement(i);

							}

						}

					}

					int table = 0;
					for (int i = 0; i < bodyElements.size(); i++) {
						IBodyElement element = bodyElements.get(i);
						if (element instanceof XWPFTable) {
							table = i;
						}
					}
					document1.removeBodyElement(table);
					document1.removeBodyElement(table - 1);
					document1.removeBodyElement(table - 2);

					for (int i = 0; i < bodyElements.size(); i++) {
						IBodyElement element = bodyElements.get(i);

						if (element instanceof XWPFParagraph) {
							String text = ((XWPFParagraph) element).getParagraphText();

							if (text.contains(Consts.ETIQUETA_DESTINATARIO)) {

								document1.removeBodyElement(i);
								XWPFParagraph p = document1.getParagraphArray(i);

								switch (flag) {
								case 0:
									XmlCursor cursor = p.getCTP().newCursor();
									XWPFParagraph newP = doc.createParagraph();
									newP.getCTP().setPPr(p.getCTP().getPPr());
									XWPFRun newR = newP.createRun();

									if (per.getNombre().isEmpty()) {

										String[] direccion = per.getRol().split(",");
										newR.setText(direccion[1]);

									} else {

										newR.setText(per.getCargo());
									}
									newR.setFontSize(12);
									XmlCursor c2 = newP.getCTP().newCursor();
									c2.moveXml(cursor);
									c2.dispose();
									cursor.removeXml();
									cursor.dispose();
									flag++;
									break;

								case 1:

									if (per.getNombre().isEmpty()) {
										
										XmlCursor cursor1 = p.getCTP().newCursor();
										XWPFParagraph newP1 = doc.createParagraph();
										newP1.getCTP().setPPr(p.getCTP().getPPr());
										XWPFRun newR1 = newP1.createRun();
										newR1.setText("");
										newR1.setFontSize(12);
										XmlCursor c21 = newP1.getCTP().newCursor();
										c21.moveXml(cursor1);
										c21.dispose();

										XmlCursor cursor2 = p.getCTP().newCursor();
										XWPFParagraph newP2 = doc.createParagraph();
										newP2.getCTP().setPPr(p.getCTP().getPPr());
										XWPFRun newR2 = newP2.createRun();
										newR2.setText("");
										newR2.setFontSize(12);
										XmlCursor c22 = newP2.getCTP().newCursor();
										c22.moveXml(cursor2);
										c22.dispose();

									} else {

										XmlCursor cursor1 = p.getCTP().newCursor();
										XWPFParagraph newP1 = doc.createParagraph();
										newP1.getCTP().setPPr(p.getCTP().getPPr());
										XWPFRun newR1 = newP1.createRun();
										newR1.setText(per.getDependencia());
										newR1.setFontSize(12);
										XmlCursor c21 = newP1.getCTP().newCursor();
										c21.moveXml(cursor1);
										c21.dispose();

										XmlCursor cursor2 = p.getCTP().newCursor();
										XWPFParagraph newP2 = doc.createParagraph();
										newP2.getCTP().setPPr(p.getCTP().getPPr());
										XWPFRun newR2 = newP2.createRun();
										String[] direccion = per.getRol().split(",");
										newR2.setText(direccion[1]);
										newR2.setFontSize(12);
										XmlCursor c22 = newP2.getCTP().newCursor();
										c22.moveXml(cursor2);
										c22.dispose();

										XmlCursor cursor3 = p.getCTP().newCursor();
										XWPFParagraph newP3 = doc.createParagraph();
										newP3.getCTP().setPPr(p.getCTP().getPPr());
										XWPFRun newR3 = newP3.createRun();
										newR3.setText("");
										newR3.setFontSize(12);
										XmlCursor c23 = newP3.getCTP().newCursor();
										c23.moveXml(cursor3);
										c23.dispose();
										cursor3.removeXml();
										cursor3.dispose();
										flag++;

									}

								}

							}

							if (text.contains(Consts.TITULO_DESTINATARIOS)) {

								XWPFParagraph p = document1.getParagraphArray(i);
								XmlCursor cursor = p.getCTP().newCursor();
								XWPFParagraph newP = doc.createParagraph();
								newP.getCTP().setPPr(p.getCTP().getPPr());
								XWPFRun newR = newP.createRun();

								if (per.getNombre().isEmpty()) {

									newR.setText(per.getDependencia());
									nombreDoc = per.getDependencia();

								} else {

									newR.setText(per.getNombre());
									nombreDoc = per.getNombre();
								}
								newR.setFontSize(12);
								XmlCursor c2 = newP.getCTP().newCursor();
								c2.moveXml(cursor);
								c2.dispose();
								cursor.removeXml();
								cursor.dispose();

							}
						}

					}
					
					nombreDoc = AppUtils.reemplazarCarateresEspeciales(nombreDoc, Consts.REEMPLAZO_CARACTERES_ESPECIALES);

					try {
						fos = new FileOutputStream(nombreDoc + "_" + count + ".docx");
					} catch (FileNotFoundException e) {
						throw new IOException("El documento [ " + documento + "] no pudo ser encontrado", e);
					}

					if (fos != null) {

						document1.write(fos);
						FileDataSource source = new FileDataSource(nombreDoc + "_" + count + ".docx");
						DataHandler handler = new DataHandler(source);

						listDataHandler.add(handler);

						System.out
								.println("Se realizo la creacion del Documento : " + nombreDoc + "_" + count + ".docx");

					}
					count++;
				}

				document1.close();
				doc.close();

			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			safeClose(fis);
			safeClose(fos);
		}

		return listDataHandler;

	}*/
	
	
	/**
	 * Metodo para personalizar carta a partir de una tabla
	 * @param nombreDocumentoOriginal
	 * @param categoria
	 * @param nombreDoc
	 * @param customs
	 * @return
	 * @throws IOException
	 */
	public static List<DataHandler> personalizarDocumentoCartaMasivo(String raizLog, String rutaDocumentoOriginal, MetadatosPlantilla metadatosPlantilla) throws IOException {
		//VALIDACIONES DE LOS PARÁMETROS DE ENTRADA
		if (rutaDocumentoOriginal == null) {
			throw new NullPointerException("El parametro documento no debe ser nulo");
		}
		if (metadatosPlantilla == null) {
			throw new NullPointerException("El parametro metadatosPlantilla no debe ser nulo");
		}
		
		//SE INICIALIZAN ALGUNAS VARIABLES
		String rutaDocSinExtension = rutaDocumentoOriginal.substring(0, rutaDocumentoOriginal.lastIndexOf(".")); //Ruta donde se ubica el documento original, sin la parte final de la extensión
		List<DataHandler> listDataHandler = new ArrayList<>();//Arreglo donde se van a alamacenar los documentos creados
		int count = 0;//Contador para saber la cantidad de destinos
		int flag = 1;//Bandera que indica el tag a modificar de los datos del usuario
		
		
		//SE INICIALIZAN LAS VARIABLES PARA ALMACENAR LOS DOCUMENTOS
		XWPFDocument docPersonalizado = null;
		XWPFDocument docOriginal = null;
		FileInputStream fisDocPersonalizado = null;
		FileInputStream fisDocOriginal = null;
		FileOutputStream fos = null;

		//SE PROCEDE A CREAR CADA DOC PERSONALIZADO
		try {
			//SE DEBE CREAR UN DOC POR DESTINO
			for (Empleado destino : metadatosPlantilla.getDestinatarios()) {
				
				List<Integer> listaUbicacionesEliminar = new ArrayList<Integer>();//Se almacenan todas las ubicaciones a eliminar
				
				//SE CREAN 2 FILE_INPUT_STREAM DEL DOCUEMNTO ORIGINAL
				try {
					fisDocPersonalizado = new FileInputStream(rutaDocumentoOriginal);
					fisDocOriginal = new FileInputStream(rutaDocumentoOriginal);

				} catch (FileNotFoundException e) {
					safeClose(fisDocPersonalizado);
					safeClose(fisDocOriginal);
					throw new IOException(raizLog+"El documento [ " + rutaDocumentoOriginal + "] no pudo ser encontrado", e);
				}

				//SE REVISA SI EL FILE_INPUT_STREAM SE CREÓ CORRECTAMENTE
				if (fisDocPersonalizado != null) {
					//SE CREAN OBJETOS DE APACHE POI PARA MANEJAR LOS DOCUMENTOS
					docPersonalizado = new XWPFDocument(fisDocPersonalizado);
					docOriginal = new XWPFDocument(fisDocOriginal);

					//SE GENERA LA LISTA CON LOS DIFERENTES TIPOS DE ELEMENTOS EN EL DOCUMENTO
					List<IBodyElement> bodyElements = docPersonalizado.getBodyElements();//Todos los elementos del doc                  		
					
					
					//SE ESCRIBEN LOS DATOS DE LOS DESTINATARIOS EN CADA DOC PERSONALIZADO
					for (int ubicacionElemento = 0; ubicacionElemento < bodyElements.size(); ubicacionElemento++) {
						IBodyElement element = bodyElements.get(ubicacionElemento);
						if (element instanceof XWPFParagraph && flag<5) {
							String text = ((XWPFParagraph) element).getParagraphText();
							if (text.contains(Consts.ETIQUETA_DESTINATARIO)) {	
								XWPFParagraph parrafo = docPersonalizado.getParagraphArray(ubicacionElemento);
								//SE AGREGAN DE ACUERDO A UNA BANDERA (SE ESPERAN MÁXIMO 4 PARRAFOS)
								switch (flag) {
								
								case 1://Para la primera linea a insertar
									XmlCursor cursorParrafo = parrafo.getCTP().newCursor();
									//XWPFParagraph newParrafo = docOriginal.createParagraph();
									XWPFParagraph newParrafo = docPersonalizado.createParagraph();
									newParrafo.getCTP().setPPr(parrafo.getCTP().getPPr());
									XmlCursor cursorFinal = newParrafo.getCTP().newCursor();
									XWPFRun newRun = newParrafo.createRun();

									if (destino.getNombre().isEmpty()) { // si no tiene nombre agrega la dependencia
										newRun.setText(destino.getDependencia());
									} else {
										newRun.setText(destino.getNombre());
									}
									newRun.setFontSize(12);	
									cursorFinal = newParrafo.getCTP().newCursor();
									cursorFinal.moveXml(cursorParrafo);
									cursorFinal.dispose();
									cursorParrafo.removeXml();
									cursorParrafo.dispose();
									flag++;
									break;
									
								case 2: //Para la segunda linea a insertar
									Boolean anadirTexto = true;
									cursorParrafo = parrafo.getCTP().newCursor();
									newParrafo = docOriginal.createParagraph();
									newParrafo.getCTP().setPPr(parrafo.getCTP().getPPr());
									newRun = newParrafo.createRun();

									if ( ( !destino.getNombre().isEmpty() && !destino.getCargo().isEmpty() ) || 
											( !destino.getDependencia().isEmpty() && !destino.getCargo().isEmpty() ) ) {
										newRun.setText(destino.getCargo());
									}else if (!destino.getNombre().isEmpty() && destino.getCargo().isEmpty() && !destino.getDependencia().isEmpty()) {
										newRun.setText(destino.getDependencia());
									}else if ( (!destino.getNombre().isEmpty() && destino.getCargo().isEmpty() && destino.getDependencia().isEmpty()) || 
											(destino.getNombre().isEmpty() && destino.getCargo().isEmpty() && !destino.getDependencia().isEmpty())) {
										String[] direccion = destino.getRol().split(",");
										if (direccion.length ==3) {
											newRun.setText(direccion[1].substring(0, direccion[1].length())+", "+direccion[2]);
										}else if (direccion.length ==2) {
											newRun.setText(direccion[1].substring(0, direccion[1].length()));
										}else {
											anadirTexto = false;
										}
									}else {
										anadirTexto = false;
									}

									
									if(anadirTexto) {
										newRun.setFontSize(12);
										cursorFinal = newParrafo.getCTP().newCursor();
										cursorFinal.moveXml(cursorParrafo);
										cursorFinal.dispose();
										cursorParrafo.removeXml();
										cursorParrafo.dispose();
										flag++;
										break;
									}else {									
										listaUbicacionesEliminar.add(ubicacionElemento);
										flag++;
										break;
									}
									
								case 3: //Para la tercera linea a insertar
									anadirTexto = true;
									cursorParrafo = parrafo.getCTP().newCursor();
									newParrafo = docOriginal.createParagraph();
									newParrafo.getCTP().setPPr(parrafo.getCTP().getPPr());
									newRun = newParrafo.createRun();

									if (!destino.getNombre().isEmpty() && !destino.getCargo().isEmpty() && !destino.getDependencia().isEmpty() ) {
										newRun.setText(destino.getDependencia());
									}
									else if ((!destino.getNombre().isEmpty() && !destino.getCargo().isEmpty() && destino.getDependencia().isEmpty()) ||
											(!destino.getNombre().isEmpty() && destino.getCargo().isEmpty() && !destino.getDependencia().isEmpty()) ||
											(destino.getNombre().isEmpty() && !destino.getCargo().isEmpty() && !destino.getDependencia().isEmpty())) {
										String[] direccion = destino.getRol().split(",");
										if (direccion.length ==3) {
											newRun.setText(direccion[1].substring(0, direccion[1].length())+", "+direccion[2]);
										}else if (direccion.length ==2) {
											newRun.setText(direccion[1].substring(0, direccion[1].length()));
										}else {
											anadirTexto = false;
											
										}
									}else {
										anadirTexto = false;
									}

									
									if(anadirTexto) {
										newRun.setFontSize(12);
										cursorFinal = newParrafo.getCTP().newCursor();
										cursorFinal.moveXml(cursorParrafo);
										cursorFinal.dispose();
										cursorParrafo.removeXml();
										cursorParrafo.dispose();
										flag++;
										break;
									}else {
										listaUbicacionesEliminar.add(ubicacionElemento);
										flag++;
										break;
									}
									
								
								case 4: //Para la cuarta linea a insertar
									anadirTexto = true;
									cursorParrafo = parrafo.getCTP().newCursor();
									newParrafo = docOriginal.createParagraph();
									newParrafo.getCTP().setPPr(parrafo.getCTP().getPPr());
									newRun = newParrafo.createRun();

									if (!destino.getNombre().isEmpty() && !destino.getCargo().isEmpty() && !destino.getDependencia().isEmpty() ) {
										String[] direccion = destino.getRol().split(",");
										if (direccion.length ==3) {
											newRun.setText(direccion[1].substring(0, direccion[1].length())+", "+direccion[2]);
										}else if (direccion.length ==2) {
											newRun.setText(direccion[1].substring(0, direccion[1].length()));
										}else {
											anadirTexto = false;
										}
									}else {
										anadirTexto = false;
									}

									if(anadirTexto) {
										newRun.setFontSize(12);
										cursorFinal = newParrafo.getCTP().newCursor();
										cursorFinal.moveXml(cursorParrafo);
										cursorFinal.dispose();
										cursorParrafo.removeXml();
										cursorParrafo.dispose();
										flag++;
										break;
									}else {									
										listaUbicacionesEliminar.add(ubicacionElemento);
										flag++;
										break;
									}
									

								}

							}
							
						}

						
					}
					
					//SE ELIMINAN LAS UBICACIONES VACIAS
					for(int pos=0; pos<listaUbicacionesEliminar.size();pos++) {
						docPersonalizado.removeBodyElement(listaUbicacionesEliminar.get(pos));
					}
					
					//Vamaya: Bloque añadido para eliminar espacios dentro de una tabla con controladores de contenido
					boolean isSTD = false;
					
					for (XWPFTable tbl : docPersonalizado.getTables()) {
						   for (XWPFTableRow row : tbl.getRows()) {
						      for (XWPFTableCell cell : row.getTableCells()) {
						    	  for (IBodyElement icel : cell.getBodyElements()) {
						    		  if(icel instanceof XWPFSDT) {
						    			  isSTD = true;
						    		  }
						    		  if (icel instanceof XWPFParagraph && isSTD) {
						    			  if(((XWPFParagraph) icel).getParagraphText().isEmpty() ) {
						    				  XWPFParagraph paragraph = ((XWPFParagraph) icel);
						    				  int pPos = cell.getParagraphs().indexOf(paragraph);		    				  
						    				  cell.removeParagraph(pPos);
						    				  isSTD = false;
						    			  }
						    		  }		    		  
						    	  }		    	  
						      }
						   }
					}
					//					
					String nombreDocCompleto = rutaDocSinExtension + "_" + count + ".docx";
					nombreDocCompleto = AppUtils.truncarNombreDocumento(nombreDocCompleto, Consts.TAMANO_MAXIMO_NOMBRE_ARCHIVO);
					
					try {
						fos = new FileOutputStream(nombreDocCompleto);
					} catch (FileNotFoundException e) {
						docPersonalizado.close();
						docOriginal.close();
						throw new IOException(raizLog+"El documento [" +nombreDocCompleto + "] no pudo ser leido", e);
					}

					if (fos != null) {

						docPersonalizado.write(fos);
						FileDataSource source = new FileDataSource(nombreDocCompleto);
						DataHandler handler = new DataHandler(source);
						
						handler = eliminarTablaDocPersonalizado(raizLog, nombreDocCompleto);

						listDataHandler.add(handler);

						System.out.println(raizLog+"Se realizo la creacion del Documento : " + nombreDocCompleto);

					}
					count++;
				}

				docPersonalizado.close();
				docOriginal.close();
				flag=1;//Se reinicia el valor de la bandera
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			safeClose(fisDocPersonalizado);
			safeClose(fos);
		}

		return listDataHandler;

	
	}
	
	
	
	/**
	 * 
	 * @param rutaDocPersonalizado
	 * @return
	 */
	private static DataHandler eliminarTablaDocPersonalizado(String raizLog, String rutaDocPersonalizado) {
				
		//SE INICIALIZAN LAS VARIABLES PARA ALMACENAR LOS DOCUMENTOS
		XWPFDocument docPersonalizado = null;
		FileInputStream fisDocPersonalizado = null;
		FileOutputStream fos = null;
		
		DataHandler handler = null;

		try {
				try {
					fisDocPersonalizado = new FileInputStream(rutaDocPersonalizado);
				} catch (FileNotFoundException e) {
					safeClose(fisDocPersonalizado);
					throw new IOException(raizLog+"El documento [ " + rutaDocPersonalizado + "] no pudo ser encontrado", e);
				}

				// SE REVISA SI EL FILE_INPUT_STREAM SE CREÓ CORRECTAMENTE
				if (fisDocPersonalizado != null) {
					// SE CREAN OBJETOS DE APACHE POI PARA MANEJAR LOS DOCUMENTOS
					docPersonalizado = new XWPFDocument(fisDocPersonalizado);

					// SE GENERA LA LISTA CON LOS DIFERENTES TIPOS DE ELEMENTOS EN EL DOCUMENTO
					List<IBodyElement> bodyElements = docPersonalizado.getBodyElements();// Todos los elementos del doc

					// SE BUSCA Y SE ELIMINA LA PALABRA "Según relación adjunta", EN LOS PARRAFOS
					// (Aparece al inicio)
					for (int ubicacionParrafo = 0; ubicacionParrafo < bodyElements.size(); ubicacionParrafo++) {
						IBodyElement parrafo = bodyElements.get(ubicacionParrafo);
						if (parrafo instanceof XWPFParagraph) {
							String textoParrafo = ((XWPFParagraph) parrafo).getParagraphText();
							if (textoParrafo.contains(Consts.TITULO_DESTINATARIOS)) {
							    docPersonalizado.removeBodyElement(ubicacionParrafo);//Se elimina la
								// ocurrencia
								break;// Sólo elimina la primer ocurrencia y se sale
							}
						}

					}

					// SE BUSCA Y SE ELIMINA LA PALABRA "Relación adjunta destinatarios carta"
					// (TITULO DE LA TABLA PARA COMBINAR DESTINOS), EN LOS PARRAFOS (Aparece al
					// final)
					for (int ubicacionParrafo = bodyElements.size() - 1; ubicacionParrafo >= 0; ubicacionParrafo--) {
						IBodyElement parrafo = bodyElements.get(ubicacionParrafo);
						if (parrafo instanceof XWPFParagraph) {
							String textoParrafo = ((XWPFParagraph) parrafo).getParagraphText();
							if (textoParrafo.contains(Consts.TITULO_TABLA)) {
								docPersonalizado.removeBodyElement(ubicacionParrafo);//Se elimina la
								// ocurrencia
								docPersonalizado.removeBodyElement(ubicacionParrafo-1);//Se elimina el
								// espacio anterior al titulo de la tabla
								break;// Sólo elimina la primer ocurrencia y se sale
							} else {
								docPersonalizado.removeBodyElement(ubicacionParrafo);//Se cualquier otro
								// parrafo anterior
							}
						} else {
							docPersonalizado.removeBodyElement(ubicacionParrafo);//Se cualquier otro
							// elemento anterior al titulo
						}

					}

					// Vamaya: Bloque añadido para eliminar espacios dentro de una tabla con
					// controladores de contenido
					boolean isSTD = false;

					for (XWPFTable tbl : docPersonalizado.getTables()) {
						for (XWPFTableRow row : tbl.getRows()) {
							for (XWPFTableCell cell : row.getTableCells()) {
								for (IBodyElement icel : cell.getBodyElements()) {
									if (icel instanceof XWPFSDT) {
										isSTD = true;
									}
									if (icel instanceof XWPFParagraph && isSTD) {
										if (((XWPFParagraph) icel).getParagraphText().isEmpty()) {
											XWPFParagraph paragraph = ((XWPFParagraph) icel);
											int pPos = cell.getParagraphs().indexOf(paragraph);
											cell.removeParagraph(pPos);
											isSTD = false;
										}
									}
								}
							}
						}
					}
					//
					
					try {
						fos = new FileOutputStream(rutaDocPersonalizado);
					} catch (FileNotFoundException e) {
						docPersonalizado.close();
						throw new IOException(raizLog+"El documento [" + rutaDocPersonalizado + "] no pudo ser leido", e);
					}

					if (fos != null) {

						docPersonalizado.write(fos);
						FileDataSource source = new FileDataSource(rutaDocPersonalizado);
						handler = new DataHandler(source);

						System.out.println(raizLog+"Se realizo la modificacion del Documento : " + rutaDocPersonalizado);

					}
				}

				docPersonalizado.close();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			safeClose(fisDocPersonalizado);
			safeClose(fos);
		}

		return handler;

	}
	
	
	
	
	//TODO OBSOLETO
	public static List<DataHandler> personalizarDocumento(String documento, MetadatosPlantilla categoria, String nombreDoc,
			Map<String, String> customs) throws IOException {

		if (documento == null) {
			throw new NullPointerException("El parametro documento no debe ser nulo");
		}
		if (categoria == null) {
			throw new NullPointerException("El parametro categoria no debe ser nulo");
		}
		if (nombreDoc == null) {
			throw new NullPointerException("El parametro nombreDoc no debe ser nulo");
		}

		List<DataHandler> listDataHandler = new ArrayList<>();

		int dest = categoria.getDestinatarios().size();
		int j = 0, count = 0;
		XWPFDocument document1 = null;
		FileInputStream fis = null;
		FileOutputStream fos = null;

		try {
			while (count < dest) {
				try {
					fis = new FileInputStream(documento);
				} catch (FileNotFoundException e) {
					throw new IOException("El documento [ " + documento + "] no pudo ser encontrado", e);
				} finally {
					safeClose(fis);
				}

				if (fis != null) {
					document1 = new XWPFDocument(fis);
					List<IBodyElement> bodyElements = document1.getBodyElements();
					while (j < dest) {
						if (j != count) {
							for (int i = 0; i < bodyElements.size(); i++) {
								IBodyElement element = bodyElements.get(i);

								if (element instanceof XWPFParagraph) {
									String text = ((XWPFParagraph) element).getParagraphText();

									if (text.contentEquals(customs.get(Consts.REFERENCIA_DESTINO) + j)) {
										// System.out.println(text);
										/* Boolean valor = */ document1.removeBodyElement(i);
										// System.out.println(valor);
										i = 0;
									}
								}
							}
						}
						j++;
					}
				}

				try {
					fos = new FileOutputStream(nombreDoc + "_" + count + ".docx");
				} catch (FileNotFoundException e) {
					throw new IOException("El documento [ " + documento + "] no pudo ser encontrado", e);
				} finally {
					safeClose(fos);
				}

				if (fos != null) {
					document1.write(fos);

					FileDataSource source = new FileDataSource(nombreDoc + "_" + count + ".docx");
					DataHandler handler = new DataHandler(source);
					listDataHandler.add(handler);

					System.out.println("Se realizo la creacion del Documento : " + nombreDoc + "_" + count + ".docx");
					document1.close();
				}
				count++;
				j = 0;
			}

		} catch (FileNotFoundException e) {
			throw new IOException("AppUtils " + documento + " No encontrada ", e);
		} finally {
			document1.close();
			safeClose(fis);
			safeClose(fos);
		}

		return listDataHandler;
	}
	
	// INICIO cambios Johan
	
	/**
	 * Método que genera el documento
	
	 */
	public static DataHandler generarDocumento(String contenidoDoc, String rutaTemporal) throws IOException {
		
		//CREACIÓN DOCUMENTO
		XWPFDocument documento = new XWPFDocument();
		
		//CODIGO DE ORIENTACIÓN Y TAMAÑO DE PÁGINA
		confOrientacionyTamPag(documento, STPageOrientation.LANDSCAPE, ValoresSticker.getAnchopaginaPts(), ValoresSticker.getAltopaginaPts());
		
		//CÓDIGO MARGENES DE PÁGINA
		confMargenPag(documento, ValoresSticker.getMargeninferior());
		FileOutputStream salida = null;
		try {
		File file = new File(rutaTemporal + "documentoSalida.docx")	;
		salida = new FileOutputStream(file);
		
		System.out.println("Documento Creado Exitosamente");
		
		XWPFParagraph parrafo = documento.createParagraph();
		
		//contenidoDoc="holaaa";
		XWPFRun r2 = parrafo.createRun();
		r2.setText(contenidoDoc);
		
		 documento.write(salida);

		}catch (Exception e) {
			System.out.println("ERROR:"+e);
		}finally {
			documento.close();
			safeClose(salida);
		}
		
		FileDataSource source = new FileDataSource(rutaTemporal + "documentoSticker.docx");
		DataHandler handler = new DataHandler(source);
		return handler;
	}
	
	
	private static void safeClose(FileOutputStream fis) {
		 if (fis != null) {
			 try {
				 fis.close();
			 } catch (IOException e) {
				 e.printStackTrace();
			 }
			 
		 }
	}
	
	private static void safeClose(FileInputStream fis) {
		 if (fis != null) {
			 try {
				 fis.close();
			 } catch (IOException e) {
				 e.printStackTrace();
			 }
			 
		 }
	}
	
	
	
	
	/**
	 * Elimina las referencia externas del documento
	 * 
	 * @param docx documento XWPFDocuemnto al cual se le van a eliminar las referencias externas
	 * @param nombreDoc String con el nombre del documento
	 * @throws Exception si se presenta algun problema eliminando las referencias
	 */
	private static void eliminarReferenciasExternas(String raizLog, XWPFDocument docx, String nombreDoc) throws Exception {

		XWPFWordExtractor extractor = new XWPFWordExtractor(docx);
		OPCPackage pkg = extractor.getPackage();
		ArrayList<PackagePart> parts = pkg.getParts();

		if (parts == null) {
			extractor.close();
			throw new NullPointerException("Metodo getParts() retorno nulo");
		}

		if (parts.isEmpty()) {
			extractor.close();
			throw new IndexOutOfBoundsException("Metodo getParts() retorno partes vacio");
		}

		// Se busca el paquete 'Settings' que es en donde se encuentran las referencias
		// externas que generan el bloqueo del Render
		Optional<PackagePart> partOpt = parts.stream()
				.filter(unPart -> unPart.getPartName().getName().equals(PAQUETE_SETTINGS)).findFirst();
		PackagePart part = null;

		if (partOpt.isPresent()) {
			part = partOpt.get();

			if (part.hasRelationships()) {
				System.out.println(raizLog + "El documento [" + nombreDoc + "] tiene relaciones externas");
				
				PackageRelationshipCollection relations = part.getRelationships();
				PackageRelationship relation = relations.getRelationship(0);

				// Se obtiene el tipo de la relacion existente, para ser usado en una nueva
				// relacion
				String type = relation.getRelationshipType();
				
				String target = relation.getTargetURI().getPath();
				System.out.println(raizLog + "El documento [" + nombreDoc + "] tiene relación externa a la ruta [" + target + "]");

				// Se hace el borrado de todas las relaciones de el paquete en particular
				part.clearRelationships();				

				// Dado que el documento queda corrupto, se debe general al menos una relacion
				// externa con un Target vacio para que no se intente buscar rutas externas
				part.addExternalRelationship("", type);
			}
		}
		extractor.close();
		System.out.println(raizLog + "Al documento [" + nombreDoc + "] se le eliminaron las referencias externas correctamente");
	}
	
	
	//TODO ELIMINAR POR OBSOLETO
	/**
	 * Elimina las referencia externas del documento
	 * 
	 * @param docx documento XWPFDocuemnto al cual se le van a eliminar las referencias externas
	 * @param nombreDoc String con el nombre del documento
	 * @throws Exception si se presenta algun problema eliminando las referencias
	 */
	private static void eliminarReferenciasExternas(XWPFDocument docx, String nombreDoc) throws Exception {

		XWPFWordExtractor extractor = new XWPFWordExtractor(docx);
		OPCPackage pkg = extractor.getPackage();
		ArrayList<PackagePart> parts = pkg.getParts();

		if (parts == null) {
			extractor.close();
			throw new NullPointerException("Metodo getParts() retorno nulo");
		}

		if (parts.isEmpty()) {
			extractor.close();
			throw new IndexOutOfBoundsException("Metodo getParts() retorno partes vacio");
		}

		// Se busca el paquete 'Settings' que es en donde se encuentran las referencias
		// externas que generan el bloqueo del Render
		Optional<PackagePart> partOpt = parts.stream()
				.filter(unPart -> unPart.getPartName().getName().equals(PAQUETE_SETTINGS)).findFirst();
		PackagePart part = null;

		if (partOpt.isPresent()) {
			part = partOpt.get();

			if (part.hasRelationships()) {
				System.out.println("El documento [" + nombreDoc + "] tiene relaciones externas");
				
				PackageRelationshipCollection relations = part.getRelationships();
				PackageRelationship relation = relations.getRelationship(0);

				// Se obtiene el tipo de la relacion existente, para ser usado en una nueva
				// relacion
				String type = relation.getRelationshipType();
				
				String target = relation.getTargetURI().getPath();
				System.out.println("El documento [" + nombreDoc + "] tiene relación externa a la ruta [" + target + "]");

				// Se hace el borrado de todas las relaciones de el paquete en particular
				part.clearRelationships();				

				// Dado que el documento queda corrupto, se debe general al menos una relacion
				// externa con un Target vacio para que no se intente buscar rutas externas
				part.addExternalRelationship("", type);
			}
		}
		extractor.close();
		System.out.println("Al documento [" + nombreDoc + "] se le eliminaron las referencias externas correctamente");
	}
	
}

