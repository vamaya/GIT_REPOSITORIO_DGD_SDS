package co.gov.banrep.iconecta.ssm.firma.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.soap.SOAPException;

import com.sun.xml.ws.api.message.Header;

import co.gov.banrep.iconecta.cs.workflow.ValorAtributo;
import co.gov.banrep.iconecta.cs.workflow.Workflow;

public class AccionExternaMemorando implements AccionExterna {
	
	private static final String PARAMETRO_WORK_ID = "workId";
	private static final String PARAMETRO_RESULTADO_FIRMA = "resultadoFirma";
	private static final String PARAMETRO_MENSAJE = "mensaje";
	private static final String PARAMETRO_USUARIO_CANCELA = "usuarioCancela";
	
	private static final String WORK_ID = "idWorkflow";
	private static final String MOTIVO_CANCELACION = "motivo";
	private static final String NOMBRE_USUARIO_CANCELA = "usuarioCancelaNombre";
	
	private static final String ENCODING = "UTF-8";	
	
	@Override
	public String ejecutar(Map<Object, Object> parametros, String url) throws IOException {

		if (parametros == null) {
			throw new NullPointerException("El parametro parametros no puede ser nulo");
		}

		if (url == null) {
			throw new NullPointerException("El parametro URL no puede ser nulo");
		}

		if (url.equals("")) {
			throw new IllegalArgumentException("El parametro URL no puede esta vacio");
		}

		return llamarDistribucionMemorando(parametros, url, false);

	}
	
	@Override
	public String cancelar(Map<Object, Object> parametros, String url) throws IOException {
		
		if (parametros == null) {
			throw new NullPointerException("El parametro parametros no puede ser nulo");
		}

		if (url == null) {
			throw new NullPointerException("El parametro URL no puede ser nulo");
		}

		if (url.equals("")) {
			throw new IllegalArgumentException("El parametro URL no puede esta vacio");
		}

		return llamarDistribucionMemorando(parametros, url, true);
	}
	
	private String llamarDistribucionMemorando(Map<Object, Object> parametros, String strURL, boolean isFirmaCancelada) throws IOException{		
		
		String workId =  "";
		
		if (parametros.containsKey(WORK_ID)) {
			workId = parametros.get(WORK_ID).toString();
		}

		String motivoCancelacion = "NO ESPECIFICADO";

		if (parametros.containsKey(MOTIVO_CANCELACION)) {
			motivoCancelacion = parametros.get(MOTIVO_CANCELACION).toString();
		}
		
		String usuarioCancela = "NO ESPECIFICADO";
		
		if (parametros.containsKey(NOMBRE_USUARIO_CANCELA)) {
			usuarioCancela = parametros.get(NOMBRE_USUARIO_CANCELA).toString();
		}
		
		if(workId.isEmpty()){
			throw new NullPointerException("EL valor idWorkflow no puede ser nulo");			
		}

		URL url = new URL(strURL);
		HashMap<String, Object> params;
		params = new LinkedHashMap<>();
		params.put(PARAMETRO_WORK_ID, workId);
		
		if (!isFirmaCancelada) {
			params.put(PARAMETRO_RESULTADO_FIRMA, true);
		} else {
			params.put(PARAMETRO_RESULTADO_FIRMA, false);
			params.put(PARAMETRO_MENSAJE, motivoCancelacion);
			params.put(PARAMETRO_USUARIO_CANCELA, usuarioCancela);	
		}
		
		StringBuilder postData = new StringBuilder();
		
		for(Map.Entry<String, Object> param : params.entrySet()){
			if(postData.length() != 0){
				postData.append('&');
			}
			postData.append(URLEncoder.encode(param.getKey(), ENCODING));
			postData.append('=');
			postData.append(URLEncoder.encode(String.valueOf(param.getValue()), ENCODING));
		}
		
		HttpURLConnection conn;
		
		byte[] postDataBytes = postData.toString().getBytes(ENCODING);
		
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
		conn.setDoOutput(true);
		
		OutputStream out = null;

		try {
			out = conn.getOutputStream();
			out.write(postDataBytes);
		} catch (IOException e) {
			throw new IOException("Error enviando la respuesta de firma", e);
		} finally {
			if (out != null) {
				safeClose(out);
			}
		}
		
		InputStream ins = null;
		try {
			ins = conn.getInputStream();
		} catch (IOException e) {
			throw new IOException("Error enviando la respuesta de firma", e);
		}finally {
			if (ins != null){
				safeClose(ins);
			}
		}
		
		InputStreamReader insr = null;
		try {
			insr = new InputStreamReader(ins, ENCODING);
		} catch (IOException e) {
			throw new IOException("Error enviando la respuesta de firma", e);
		}finally {
			if(insr != null){
				safeClose(insr);
			}
		}
		
		Reader in = null;
		try {
			in = new BufferedReader(insr);
		} catch (Exception e) {
			throw new IOException("Error enviando la respuesta de firma", e);
		}finally {
			if(in != null){
				safeClose(in);
			}
		}
		
		return in.toString();
		
	}

	@Override
	public String notificarError(Map<String, ValorAtributo> parametros, long mapId, String url, Header aut) {
		
		String result = "Fallo el envio de la notificacion";
		
		long idWorkflow;
		
		try {
			idWorkflow = Workflow.iniciarWorkflowConAtributos(aut,	mapId, "Notificacion Error ", parametros, url);
			result = "La notificacion se ha enviado con exito con Id: "  + idWorkflow;
		} catch (SOAPException e) {
			e.printStackTrace();
		} catch (NumberFormatException | NoSuchElementException | IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	private void safeClose(InputStream in) throws IOException{
		if(in != null){
			in.close();
		}
	}
	private void safeClose(InputStreamReader inr) throws IOException{
		if(inr != null){
			inr.close();
		}
	}
	
	private void safeClose(OutputStream out) throws IOException{
		if(out != null){
			out.close();
		}
	}
	
	private void safeClose(Reader re) throws IOException{
		if(re != null){
			re.close();
		}
	}
	




	
	
}
