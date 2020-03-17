package co.gov.banrep.iconecta.office.documento.utils;

import org.apache.commons.lang3.StringUtils;

public final class AppUtils {
	
	private AppUtils() {
		throw new AssertionError();
	}
	
	public static String reemplazarCarateresEspeciales(String cadena, String reemplazo) {

		if (cadena != null) {
			if (!cadena.isEmpty()) {
				cadena = cadena.replaceAll("[:^/./\\:*?\"<>|]", reemplazo);
			}
		}

		return cadena;
	}
	
	public static String truncarString(String cadena, int tamanoMaximo) {

		if (!StringUtils.isEmpty(cadena)) {
			int tamanoActual = cadena.length();

			if (tamanoActual > tamanoMaximo) {
				cadena = cadena.substring(0, tamanoMaximo);
			}

		}

		return cadena;
	}
	
	public static String truncarNombreDocumento(String nombreDoc, int tamanoMaximo) {

		int tamanoActual = nombreDoc.length();

		if (tamanoActual > tamanoMaximo) {

			String nombreSinExtension = StringUtils.substringBeforeLast(nombreDoc, ".");
			String extension = StringUtils.substringAfterLast(nombreDoc, ".");

			String nombreSinExtensionTruncado = truncarString(nombreSinExtension, tamanoMaximo);

			String nombreTruncado = nombreSinExtensionTruncado + "." + extension;

			return nombreTruncado;

		}

		return nombreDoc;
	}

}
