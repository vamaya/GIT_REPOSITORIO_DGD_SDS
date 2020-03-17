package co.gov.banrep.iconecta.office.documento.entity;

public class ValoresSticker {

	private static final int twipPorPulgada = 2800;
	private static final int altoPaginaPts = 5920;
	private static final int anchoPaginaPts = 13680;
	private static final long margenInferior = 860L;
	private static final int espacioSaltoPagina = 500;

	public static int getEspacioSaltoPagina() {
		return espacioSaltoPagina;
	}

	public static int getAltopaginaPts() {
		return altoPaginaPts;
	}

	public static int getAnchopaginaPts() {
		return anchoPaginaPts;
	}

	public static int getTwipporpulgada() {
		return twipPorPulgada;
	}

	public static long getMargeninferior() {
		return margenInferior;
	}
}
