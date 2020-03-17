package co.gov.banrep.iconecta.ssm.firma.utils;

import java.lang.reflect.MalformedParametersException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.statemachine.StateContext;

import co.gov.banrep.iconecta.cs.cliente.document.DataValue;
import co.gov.banrep.iconecta.cs.cliente.document.IntegerValue;
import co.gov.banrep.iconecta.cs.cliente.document.ReportResult;
import co.gov.banrep.iconecta.cs.cliente.document.RowValue;
import co.gov.banrep.iconecta.cs.cliente.document.StringValue;
import co.gov.banrep.iconecta.ssm.firma.entity.Categoria;
import co.gov.banrep.iconecta.ssm.firma.entity.CategoriaConSubGrupo;
import co.gov.banrep.iconecta.ssm.firma.entity.DocumentoSSM;
import co.gov.banrep.iconecta.ssm.firma.entity.EntradaCategoriaFirma;
import co.gov.banrep.iconecta.ssm.firma.entity.FirmanteSSM;
import co.gov.banrep.iconecta.ssm.firma.entity.Metadato;
import co.gov.banrep.iconecta.ssm.firma.enums.AtributosCategorias;
import co.gov.banrep.iconecta.ssm.firma.enums.Events;
import co.gov.banrep.iconecta.ssm.firma.enums.States;
import co.gov.banrep.iconecta.ssm.firma.params.CSCatDocumentoGralProps;
import co.gov.banrep.iconecta.ssm.firma.params.CSCatFirmaProps;
import co.gov.banrep.iconecta.ssm.firma.params.GlobalConsts;
import co.gov.banrep.iconecta.ssm.firma.params.PortafirmasProps;

public final class SSMUtils {
	
	private static final String SEPARADOR_ETIQUETA_PORTAFIRMAS = "X";

	private SSMUtils() {
		throw new AssertionError();
	}

	public static void cargarValoresCadenaCS(String cadenaStr, List<Long> valores)
			throws NoSuchElementException, NumberFormatException {

		if (org.springframework.util.StringUtils.isEmpty(cadenaStr)) {
			throw new IllegalArgumentException("La cadena de caracteres esta vacia");
		}

		if (valores == null) {
			throw new NullPointerException("las lista de valores no puede ser nula");
		}

		cadenaStr = cadenaStr.replace("{", "");
		cadenaStr = cadenaStr.replace("}", "");

		List<String> listado = new ArrayList<String>(Arrays.asList(cadenaStr.split(",")));

		Iterator<String> iter = listado.iterator();

		// Se utiliza dado que el CS en ocasiones puede enviar cadenas del
		// estilo{3402183,?,?} donde ? representa valores nulos
		boolean hayMasElementos = true;

		try {
			while (iter.hasNext() && hayMasElementos) {
				String unElementoStr = iter.next();
				unElementoStr = StringUtils.deleteWhitespace(unElementoStr);
				if (unElementoStr.equals("?")) {
					hayMasElementos = false;
				} else {
					Long unElmento = Long.parseLong(unElementoStr);
					valores.add(unElmento);
				}
			}
		} catch (NumberFormatException e) {
			throw new NumberFormatException("El formato de los ids de documentos no corresponde a un numero");
		} catch (NoSuchElementException e) {
			throw new NumberFormatException("El formato de los ids de documentos no corresponde a un numero");
		}

	}

	public static void extrarerIdFirmante(String cadenaStr, List<Long> valores) {

		if (org.springframework.util.StringUtils.isEmpty(cadenaStr)) {
			throw new IllegalArgumentException("La cadena de caracteres esta vacia");
		}

		if (valores == null) {
			throw new NullPointerException("las lista de valores no puede ser nula");
		}

		cadenaStr = cadenaStr.replace("{", "");
		cadenaStr = cadenaStr.replace("}", "");
		cadenaStr = cadenaStr.replace("'", "");

		List<String> listado = new ArrayList<String>(Arrays.asList(cadenaStr.split(",")));

		Iterator<String> iter = listado.iterator();

		// Se utiliza dado que el CS en ocasiones puede enviar cadenas del
		// estilo{3402183,?,?} donde ? representa valores nulos
		boolean hayMasElementos = true;

		try {
			while (iter.hasNext() && hayMasElementos) {
				String unElementoStr = iter.next();
				if (unElementoStr.equals("?")) {
					hayMasElementos = false;
				} else {
					unElementoStr = StringUtils.substringAfter(unElementoStr, "- ");
					unElementoStr = StringUtils.deleteWhitespace(unElementoStr);
					Long unElmento = Long.parseLong(unElementoStr);
					valores.add(unElmento);
				}
			}
		} catch (NumberFormatException e) {
			throw new NumberFormatException("El formato de los ids no corresponde a un numero");
		} catch (NoSuchElementException e) {
			throw new NumberFormatException("El formato de los ids no corresponde a un numero");
		}
	}

	public static String toStringFromList(List<Long> lista) {

		if (lista == null) {
			throw new NullPointerException("La lista a procesar no puede ser nula");
		}

		if (lista.isEmpty()) {
			throw new IndexOutOfBoundsException("la lista no puede estar vacia");
		}

		StringBuilder elementos = new StringBuilder();
		for (Long unElemento : lista) {
			elementos.append(unElemento.toString());
			elementos.append(" ");
		}
		return elementos.toString();
	}

	public static String toStringFromMap(Map<String, Object> mapa) {
		StringBuilder elementos = new StringBuilder();
		Iterator<Entry<String, Object>> iter = mapa.entrySet().iterator();
		elementos.append("{");
		while (iter.hasNext()) {
			Entry<String, Object> unEntry = iter.next();
			elementos.append("(");
			elementos.append(unEntry.getKey());
			elementos.append("-");
			elementos.append(unEntry.getValue());
			elementos.append("), ");
		}
		elementos.append("}");

		return elementos.toString();
	}

	public static String getRaizLog(StateContext<States, Events> context) {
		String machineId = context.getStateMachine().getId();
		States estadoActual = context.getTarget().getId();

		return GlobalConsts.PREFIJO_LOG + machineId + GlobalConsts.SUFIJO_LOG + estadoActual + " - ";
	}

	/**
	 * A partir del dato de nombre de documento de Portafirmas relaciona el Guid
	 * de portafirmas con el respectivo documento SSM
	 * 
	 * @param documentosSSM
	 *            lista de documentos SSM
	 * @param datosPF
	 *            cadena enviada por portafirmas que contiene el guid y el
	 *            nombre del documento
	 * @throws NoSuchElementException
	 *             si no hay documentos de portafirmas o SSM que contengan la
	 *             cadena del documento adjunto
	 * @throws MalformedParametersException
	 *             si los datos de la cadena enviada por porfafirmas no estan
	 *             bien formateados
	 */
	public static void cargarIdsDocumentosPF(Map<Long, DocumentoSSM> documentosSSM, String datosPF)
			throws NoSuchElementException, MalformedParametersException {

		List<String> datosDocumentoPF = null;

		datosDocumentoPF = new ArrayList<String>(Arrays.asList(datosPF.split("#")));

		if (datosDocumentoPF.isEmpty()) {
			throw new MalformedParametersException(
					"La cadena de datos de Portafirmas (" + datosPF + ") no se pudó formatear");
		}

		Map<String, String> documentosPF = new HashMap<String, String>();

		Iterator<String> iter = datosDocumentoPF.iterator();
		while (iter.hasNext()) {
			String datosUnidos = iter.next();
			String[] separados = datosUnidos.split("%");

			String guid = separados[0];
			String nombre = separados[1];

			documentosPF.put(guid, nombre);
		}

		// Para ingresar el valor de guid de portafirmas dentro del documento
		// SSM correspondiente
		// Se valida el valor de nombreModificado del documentoSSM corresponda
		// con el nombre del
		// Documento que fue respondido por el servicio de portafirmas
		for (int i = 1; i <= documentosSSM.size(); i++) {
			String valorAdjunto = GlobalConsts.NOMBRE_MODIFICADO_DOC_PREFIJO + i
					+ GlobalConsts.NOMBRE_MODIFICADO_DOC_SUFIJO;

			DocumentoSSM unDocumento;

			try {
				unDocumento = documentosSSM.entrySet().stream()
						.filter(unDoc -> unDoc.getValue().getNombreModificado().contains(valorAdjunto)).findFirst()
						.get().getValue();
			} catch (NoSuchElementException e) {
				throw new NoSuchElementException(
						"No se encuentra ningun documento que el el valor nombreModificado contenga: " + valorAdjunto);
			}

			String unGuidPF;
			try {
				unGuidPF = documentosPF.entrySet().stream().filter(unDocPF -> unDocPF.getValue().contains(valorAdjunto))
						.findFirst().get().getKey();
			} catch (NoSuchElementException e) {
				throw new NoSuchElementException(
						"No se encuentra ningun documento de Portafirmas que en su nombre contenga: " + valorAdjunto);
			}

			unDocumento.setIdPf(unGuidPF);
		}

	}

	public static void cargarMetadatosCatDocumento(DocumentoSSM documentoSsm,
			HashMap<String, String> metadatosPortafirmas, CSCatDocumentoGralProps etiquetasCS) {

		// Carge de metadatos
		Map<AtributosCategorias, Metadato> metadatos = new HashMap<AtributosCategorias, Metadato>();
		metadatos.put(AtributosCategorias.ESTADO,
				new Metadato(etiquetasCS.getMetadato().getEstado(), GlobalConsts.ESTADO_CS_VALOR));
		Categoria catDocumentoGeneral = new Categoria();
		catDocumentoGeneral.setMetadatos(metadatos);
		documentoSsm.setCatDocumentoGeneral(catDocumentoGeneral);
	}

	public static void cargarMetadatosCatFirma(DocumentoSSM documentoSsm, HashMap<String, String> metadatosPortafirmas,
			CSCatFirmaProps etiquetasCS, PortafirmasProps etiquetasPF) {

		CategoriaConSubGrupo catFirma = new CategoriaConSubGrupo();

		String msgError = "EL servicio download File de Portafirmas no retorno el valor (";

		List<EntradaCategoriaFirma> entradas = new ArrayList<EntradaCategoriaFirma>();
		
		etiquetasPF.getEtiqueta().getCantidadfirmantes();
		
		Optional<Entry<String, String>> cantidadFirmanteOpt = metadatosPortafirmas.entrySet().stream()
				.filter(unEntry -> unEntry.getKey().equals(etiquetasPF.getEtiqueta().getCantidadfirmantes())).findFirst();
		
		int cantidadFirmantes = 0;

		if (cantidadFirmanteOpt.isPresent()) {
			cantidadFirmantes = Integer.parseInt(cantidadFirmanteOpt.get().getValue());
		} else {
			throw new NoSuchElementException(msgError + etiquetasPF.getEtiqueta().getCantidadfirmantes() + ")");
		}
		
		//System.out.println("CANTIDAD FIRMANTES [" + cantidadFirmantes + "]");
		
		List<Map<AtributosCategorias, Metadato>> metadatosSubGrupo = new ArrayList<Map<AtributosCategorias, Metadato>>();
		
		for (int i = 1; i <= cantidadFirmantes; i++) {
			
			Map<AtributosCategorias, Metadato> unaFilaMetadatosSubGrupo = new HashMap<AtributosCategorias, Metadato>();
			
			String etiquetaFirmante = etiquetasPF.getEtiqueta().getNombrefirmante();
			//System.out.println("Etiqueta Antes [" + etiquetaFirmante + "]");
			String etiquetaDespuesFirmante = reemplazarIndiceEtiqueta(i, etiquetaFirmante);
			//System.out.println("Etiqueta Despues [" + etiquetaDespuesFirmante + "]");
			
			entradas.add(new EntradaCategoriaFirma(etiquetaDespuesFirmante,
					etiquetasCS.getSubgrupo().getNombreFirmante(), AtributosCategorias.NOMBRE_FIRMANTE));
			
			String etiquetaFecha = etiquetasPF.getEtiqueta().getFechafirma();
			//System.out.println("Etiqueta Antes [" + etiquetaFecha + "]");
			String etiquetaDespuesFecha = reemplazarIndiceEtiqueta(i, etiquetaFecha);
			//System.out.println("Etiqueta Despues [" + etiquetaDespuesFecha + "]");
			
			entradas.add(new EntradaCategoriaFirma(etiquetaDespuesFecha,
					etiquetasCS.getSubgrupo().getFechaFirma(), AtributosCategorias.FECHA_FIRMA));
			
			String etiquetaEntidad = etiquetasPF.getEtiqueta().getEntidad();
			//System.out.println("Etiqueta Antes [" + etiquetaEntidad + "]");
			String etiquetaDespuesEntidad = reemplazarIndiceEtiqueta(i, etiquetaEntidad);
			//System.out.println("Etiqueta Despues [" + etiquetaDespuesEntidad + "]");
			
			entradas.add(new EntradaCategoriaFirma(etiquetaDespuesEntidad,
					etiquetasCS.getSubgrupo().getEntidad(), AtributosCategorias.ENTIDAD));
			
			String etiquetaSerie = etiquetasPF.getEtiqueta().getSerie();
			//System.out.println("Etiqueta Antes [" + etiquetaSerie + "]");
			String etiquetaDespuesSerie = reemplazarIndiceEtiqueta(i, etiquetaSerie);
			//System.out.println("Etiqueta Despues [" + etiquetaDespuesSerie + "]");
			
			entradas.add(new EntradaCategoriaFirma(etiquetaDespuesSerie,
					etiquetasCS.getSubgrupo().getSerieCertificado(), AtributosCategorias.SERIE));
			
			String etiquetaHash = etiquetasPF.getEtiqueta().getHash();
			//System.out.println("Etiqueta Antes [" + etiquetaHash + "]");
			String etiquetaDespuesHash = reemplazarIndiceEtiqueta(i, etiquetaHash);
			//System.out.println("Etiqueta Despues [" + etiquetaDespuesHash + "]");
			
			entradas.add(new EntradaCategoriaFirma(etiquetaDespuesHash,
					etiquetasCS.getSubgrupo().getValorHash(), AtributosCategorias.HASH));
			
			String etiquetaAlgoritmo = etiquetasPF.getEtiqueta().getAlgoritmo();
			//System.out.println("Etiqueta Antes [" + etiquetaAlgoritmo + "]");
			String etiquetaDespuesAlgoritmo = reemplazarIndiceEtiqueta(i, etiquetaAlgoritmo);
			//System.out.println("Etiqueta Despues [" + etiquetaDespuesAlgoritmo + "]");
			
			entradas.add(new EntradaCategoriaFirma(etiquetaDespuesAlgoritmo,
					etiquetasCS.getSubgrupo().getAlgoritmoFirma(), AtributosCategorias.ALGORITMO));

			for (EntradaCategoriaFirma entrada : entradas) {
				cargarMetadatoFirma(unaFilaMetadatosSubGrupo, metadatosPortafirmas, entrada.getEtiquetaPF(),
						entrada.getEtiquetaCS(), entrada.getAtributoCS(), msgError);
			}

			// Se adiciona el metadato de tipo de firma que tiene un valor fijo que
			// solo corresponde a CS
			unaFilaMetadatosSubGrupo.put(AtributosCategorias.TIPO_FIRMA,
					new Metadato(etiquetasCS.getSubgrupo().getTipoFirma(), GlobalConsts.TIPO_FIRMA_CS_VALOR));
			
			//bloque pruebas valores
			/*unaFilaMetadatosSubGrupo.forEach((k, v) -> System.out.println("Etiqueta CS [ " + v.getEtiquetaCS()
					+ "] - etiquetaPF [" + v.getEtiquetaPF() + "] - valor [" + v.getValor() + "]"));*/
			
			metadatosSubGrupo.add(unaFilaMetadatosSubGrupo);
		}

		catFirma.setMetadatosSubGrupo(metadatosSubGrupo);
		documentoSsm.setCatFirma(catFirma);
	}

	private static void cargarMetadatoFirma(Map<AtributosCategorias, Metadato> metadatosSubGrupo,
			HashMap<String, String> metadatosPortafirmas, String etiquetaPortafirmas, String etiquetaCS,
			AtributosCategorias atributoCategoria, String msgError) {

		Optional<Entry<String, String>> metadatoOpt = metadatosPortafirmas.entrySet().stream()
				.filter(unEntry -> unEntry.getKey().equals(etiquetaPortafirmas)).findFirst();

		if (metadatoOpt.isPresent()) {
			String metadatoStr = metadatoOpt.get().getValue();

			Metadato metadatoCS = new Metadato(etiquetaCS, etiquetaPortafirmas, metadatoStr);
			metadatosSubGrupo.put(atributoCategoria, metadatoCS);
		} else {
			throw new NoSuchElementException(msgError + etiquetaPortafirmas + ")");
		}
	}
	
	private static String reemplazarIndiceEtiqueta(Integer indice, String etiqueta) {
				return StringUtils.replaceFirst(etiqueta, SEPARADOR_ETIQUETA_PORTAFIRMAS, indice.toString());
		
	}

	public static List<FirmanteSSM> obternerFirmantes(String strListaFirmantes, List<FirmanteSSM> firmantes)
			throws Exception {

		if (org.springframework.util.StringUtils.isEmpty(strListaFirmantes)) {
			throw new IllegalArgumentException("La cadena de caracteres strListaFirmantes esta vacia");
		}

		if (firmantes == null) {
			throw new NullPointerException("las lista de firmantes no puede ser nula");
		}

		List<FirmanteSSM> firmantesOperacion = new ArrayList<>();

		strListaFirmantes = strListaFirmantes.replace("{", "");
		strListaFirmantes = strListaFirmantes.replace("}", "");
		strListaFirmantes = strListaFirmantes.replace("'", "");

		
		//System.out.println("Lista firmantes: ["+strListaFirmantes+"]");
		
		List<String> listado = new ArrayList<String>(Arrays.asList(strListaFirmantes.split(",")));

		String separador = " - ";

		// Se utiliza dado que el CS en ocasiones puede enviar cadenas del
		// estilo{3402183,?,?} donde ? representa valores nulos

		//boolean hayMasElementos = true;
		
		//listado.forEach(unElemento -> System.out.println("Un firmante [" + unElemento + "]"));

		//Iterator<String> listadoIter = listado.iterator();
		//while (listadoIter.hasNext() && hayMasElementos) {
		for (String unElemento : listado) {
			
			if ( unElemento.equals("?") || unElemento.isEmpty() || unElemento.equals(" ") ) {
				//hayMasElementos = false;
				//System.out.println("Ya no hay más firmantes");
				break;
			} else {				
				// John Jairo Rojas Sabogal - DGD - Desarrollador
				String cargo = StringUtils.substringAfterLast(unElemento, separador);
				String nombreYDependencia = StringUtils.substringBeforeLast(unElemento, separador);
				// John Jairo Rojas Sabogal - DGD
				String nombre = StringUtils.substringBeforeLast(nombreYDependencia, separador);
				String dependencia = StringUtils.substringAfterLast(nombreYDependencia, separador);

				//System.out.println("Encontró al firmante: " +nombre+ "]");
				
				List<FirmanteSSM> firmantesNombre = firmantes.stream()
						.filter(unFirmante -> unFirmante.getNombre().contains(nombre)).collect(Collectors.toList());

				if (!firmantesNombre.isEmpty()) {

					List<FirmanteSSM> firmantesDependencia = firmantesNombre.stream()
							.filter(unFirmante -> unFirmante.getDependencia().contains(dependencia))
							.collect(Collectors.toList());

					if (!firmantesDependencia.isEmpty()) {
						Optional<FirmanteSSM> unFirmanteOpt = firmantesDependencia.stream()
								.filter(unFirmante -> unFirmante.getCargo().contains(cargo)).findFirst();

						if (unFirmanteOpt.isPresent()) {
							firmantesOperacion.add(unFirmanteOpt.get());
						}
					}

				}
			}

		}

		//firmantesOperacion.add(new FirmanteSSM(1, "John Jairo Rojas Sabogal", 1023889385, "jrojassa@banrep.gov.co",
		//		"Desarrolador", "DGD"));

		return firmantesOperacion;
	}

	public static void obtenerFirmantesReporte(List<FirmanteSSM> firmantes, ReportResult resultado) {

		List<RowValue> contents = resultado.getContents();

		Iterator<RowValue> iterContents = contents.iterator();
		while (iterContents.hasNext()) {
			RowValue unRow = iterContents.next();
			FirmanteSSM unFirmante = new FirmanteSSM();
			List<DataValue> values = unRow.getValues();
			
			// SEQ
			Optional<DataValue> seqOpt = values.stream().filter(unValue -> unValue.getKey().equals("SEQ")).findFirst();

			IntegerValue seq = null;
			if (seqOpt.isPresent()) {
				seq = (IntegerValue) seqOpt.get();
				List<Long> valores = seq.getValues();
				if (!valores.isEmpty()) {
					unFirmante.setSeq(valores.get(0));
				}
			}

			// NOMBRES
			Optional<DataValue> nombreOpt = values.stream().filter(unValue -> unValue.getKey().equals("NOMBRES"))
					.findFirst();

			StringValue nombre = null;
			if (nombreOpt.isPresent()) {
				nombre = (StringValue) nombreOpt.get();
				List<String> valores = nombre.getValues();
				if (!valores.isEmpty()) {
					unFirmante.setNombre(valores.get(0));
				}
			}

			// APELLIDOS
			Optional<DataValue> apellidoOpt = values.stream().filter(unValue -> unValue.getKey().equals("APELLIDOS"))
					.findFirst();

			StringValue apellido = null;
			if (apellidoOpt.isPresent()) {
				apellido = (StringValue) apellidoOpt.get();
				List<String> valores = apellido.getValues();
				if (!valores.isEmpty()) {
					// Se debe concatenar nombre y apellido
					String tempNombre = unFirmante.getNombre() + " " + valores.get(0);
					unFirmante.setNombre(tempNombre);
				}
			}
			
			//SIGLA = DEPENDENCIA
			Optional<DataValue> siglaOpt = values.stream().filter(unValue -> unValue.getKey().equals("SIGLA")).findFirst();
			
			StringValue sigla = null;
			if(siglaOpt.isPresent()){
				sigla = (StringValue) siglaOpt.get();
				List<String> valores = sigla.getValues();
				if(!valores.isEmpty()){
				unFirmante.setDependencia(valores.get(0));
				}
			}
			
			// CARGO
			Optional<DataValue> cargoOpt = values.stream().filter(unValue -> unValue.getKey().equals("CARGO"))
					.findFirst();

			StringValue cargo = null;
			if (cargoOpt.isPresent()) {
				cargo = (StringValue) cargoOpt.get();
				List<String> valores = cargo.getValues();
				if (!valores.isEmpty()) {
					unFirmante.setCargo(valores.get(0));
				}
			}

			// CEDULA
			Optional<DataValue> cedulaOpt = values.stream().filter(unValue -> unValue.getKey().equals("CEDULA"))
					.findFirst();

			IntegerValue cedula = null;
			if (cedulaOpt.isPresent()) {
				cedula = (IntegerValue) cedulaOpt.get();
				List<Long> valores = cedula.getValues();
				if (!valores.isEmpty()) {
					unFirmante.setCedula(valores.get(0));
				}
			}

			// CORREO_ELECTRONICO
			Optional<DataValue> correoOpt = values.stream()
					.filter(unValue -> unValue.getKey().equals("CORREO_ELECTRONICO")).findFirst();

			StringValue correo = null;
			if (correoOpt.isPresent()) {
				correo = (StringValue) correoOpt.get();
				List<String> valores = correo.getValues();
				if (!valores.isEmpty()) {
					unFirmante.setCorreo(valores.get(0));
				}
			}
			
			firmantes.add(unFirmante);

		}

	}

	public static FirmanteSSM obternerFirmanteByCedula(String idFirmante, List<FirmanteSSM> firmantes) {

		if (!org.springframework.util.StringUtils.hasText(idFirmante)) {
			throw new IllegalArgumentException("El parametro idFirmante no debe ser nulo");
		}
		if (firmantes == null) {
			throw new NullPointerException("El parametro firmantes no debe ser nulo");
		}
		if (firmantes.isEmpty()) {
			throw new IllegalArgumentException("El parametro firmantes no debe estar vacio");
		}
		
		Long cedula;
		
		try{
			cedula = Long.parseLong(idFirmante);
		}catch (Exception e){
			throw new IllegalArgumentException("El parametro idFirmante("+ idFirmante + ") no pudo ser convertido a tipo long",e);
		}
		
		Optional<FirmanteSSM> firmanteOpt = firmantes.stream().filter(unFirmante -> unFirmante.getCedula().equals(cedula)).findFirst();
		
		if(firmanteOpt.isPresent()){
			return firmanteOpt.get();
		}

		return null;
	}
	
	

}
