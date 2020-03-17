package co.gov.banrep.iconecta.ssm.firma.respuestapf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cliente que permite la comunicacion por medio del protocolo HTTP con la 
 * Maquina de Estados
 *
 *  @author <a href="mailto:jjrojassa@banrep.gov.co">John Jairo Rojas S.</a>
 */
//@Service
public class SsmRestClient {

	public static final String PARAMETRO_KEY = "idCircuito";
	public static final String PARAMETRO_ESTADO_CIRCUITO = "estadoCircuito";
	public static final String PARAMETRO_ID_DOCUMENTO = "datosDocumentos";
	public static final String PARAMETRO_MOTIVO = "motivo";
	public static final String PARAMETRO_QUIEN_CANCELA = "usuarioCancela";

	private static final Logger log = LoggerFactory.getLogger(SsmRestClient.class);
	
	
	private SsmRestClient() {
		throw new AssertionError();
	}
	
	
	//public SsmRestClient() {}
	
	//@Async
	public static void actualizarSsmIdCircuit(Long idCircuito, ArrayList<String> datosDocumentos, String estadoCircuito, String motivo, String usuarioCancela, boolean espera)
			throws Exception {
		
		
		
		//Reduce la velocidad con la que se envian los datos a la maquina de estados
		if (espera) {
			
			String raizlog ="circuito [" + idCircuito + "] - "; 
			log.info(raizlog + "Entra a bloque espera");
			// Random aleatorio = new Random(System.currentTimeMillis());
			// int intAleatorio = aleatorio.nextInt(10);

			SecureRandom sr = null;

			try {
				sr = SecureRandom.getInstance("SHA1PRNG", "SUN");
			} catch (NoSuchAlgorithmException e) {
				log.warn(raizlog + "Error generando numero aleatorio - tiempo de espera 0", e);
			} catch (NoSuchProviderException e) {
				log.warn(raizlog + "Error generando numero aleatorio - tiempo de espera 0", e);
			}
			
			if (sr != null) {

				int intAleatorio = sr.nextInt(10);

				log.info(raizlog + "Numero aleatorio: " + intAleatorio);
				if (intAleatorio == 1) {
					log.info(raizlog + "Espera 5 segundos");
					Thread.sleep(5000);
				} else if (intAleatorio == 2) {
					log.info(raizlog + "Espera 10 segundos");
					Thread.sleep(10000);
				} else if (intAleatorio == 3) {
					log.info(raizlog + "Espera 15 segundos");
					Thread.sleep(15000);
				} else if (intAleatorio == 4) {
					log.info(raizlog + "Espera 20 segundos");
					Thread.sleep(20000);
				} else if (intAleatorio == 5) {
					log.info(raizlog + "Espera 25 segundos");
					Thread.sleep(25000);
				} else if (intAleatorio == 6) {
					log.info(raizlog + "Espera 30 segundos");
					Thread.sleep(30000);
				} else if (intAleatorio == 7) {
					log.info(raizlog + "Espera 35 segundos");
					Thread.sleep(35000);
				} else if (intAleatorio == 8) {
					log.info(raizlog + "Espera 40 segundos");
					Thread.sleep(40000);
				} else if (intAleatorio == 9) {
					log.info(raizlog + "Espera 45 segundos");
					Thread.sleep(45000);
				}
				
				log.info(raizlog + "Fin Espera");

			}
		}

		URL url = new URL(RespuestaPFProps.URL);
		HashMap<String, Object> params;
		params = new LinkedHashMap<String, Object>();
		params.put(PARAMETRO_KEY, idCircuito.toString());
		params.put(PARAMETRO_ESTADO_CIRCUITO, estadoCircuito);
		
		log.info("INICIO ENVIO RESPUESTA CIRCUITO - id Circuito: " + idCircuito);
		String raizLog = "Proceso [" + idCircuito + "] - ";
		log.info(raizLog + "Estado circuito: " + estadoCircuito);
		
		if (org.springframework.util.StringUtils.hasText(motivo)) {
			params.put(PARAMETRO_MOTIVO, motivo);
			log.info(raizLog + "Motivo: " + motivo); 
		}
		
		if (org.springframework.util.StringUtils.hasText(usuarioCancela)){
			params.put(PARAMETRO_QUIEN_CANCELA, usuarioCancela);
			log.info(raizLog + "Usuario que cancela: " + usuarioCancela); 
		}

		String joinedDatosDocumentos = StringUtils.join(datosDocumentos, '#');

		log.info(raizLog + "Traza recibida: " + joinedDatosDocumentos);

		params.put(PARAMETRO_ID_DOCUMENTO, joinedDatosDocumentos);

		StringBuilder postData = new StringBuilder();
		try {
			for (Map.Entry<String, Object> param : params.entrySet()) {
				if (postData.length() != 0) {
					postData.append('&');
				}
				postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
				postData.append('=');
				postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
			}
		} catch (Exception e) {
			log.error(raizLog + "ERROR coficidando respuesta");
		}

		HttpURLConnection conn = null;
		
		byte[] postDataBytes = postData.toString().getBytes("UTF-8");

		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
			conn.setDoOutput(true);
		} catch (Exception e) {			
			log.error("ERROR envio respuesta circuito" + idCircuito);
			throw new IOException("ERROR envio respuesta circuito" + idCircuito, e);
		}
		
		
		OutputStream out = null;
		try {
			out = conn.getOutputStream();
			out.write(postDataBytes);
		} catch (IOException e) {			
			log.error("ERROR envio respuesta circuito" + idCircuito);
			throw new IOException("ERROR envio respuesta circuito" + idCircuito, e);
		} finally {
			if (out != null) {
				safeClose(out);
			}
		}
		
		InputStream ins = null;		
		try {
			ins = conn.getInputStream();
		}  catch (IOException e) {			
			log.error("ERROR envio respuesta circuito" + idCircuito);
			throw new IOException("ERROR envio respuesta circuito" + idCircuito, e);
		} finally {
			if (ins != null) {
				safeClose(ins);
			}
		}
		
		InputStreamReader insr = null;		
		try {
			insr = new InputStreamReader(ins, "UTF-8");
		} catch (IOException e) {
			throw new IOException("ERROR envio respuesta circuito", e);
		} finally {
			if (insr != null) {
				safeClose(insr);
			}
		}
		
		Reader in = null;
		in = new BufferedReader(insr);

		if (log.isTraceEnabled()) {
			/*
			 * for (int c; (c = in.read()) >= 0;) { //System.out.print((char)
			 * c); }
			 */
			log.trace(raizLog + in.toString());
		}		
		
	}
	

	private static void safeClose(OutputStream out) throws IOException {
		if (out != null) {
			out.close();
		}
	}
	
	private static void safeClose(InputStream in) throws IOException {
		if (in != null) {
			in.close();
		}
	}

	private static void safeClose(Reader in) throws IOException {
		if (in != null) {
			in.close();
		}
	}
	
	
	/*
	private void safeClose(OutputStream out) throws IOException {
		if (out != null) {
			out.close();
		}
	}
	
	private void safeClose(InputStream in) throws IOException {
		if (in != null) {
			in.close();
		}
	}

	private void safeClose(Reader in) throws IOException {
		if (in != null) {
			in.close();
		}
	}
	*/
	
}
