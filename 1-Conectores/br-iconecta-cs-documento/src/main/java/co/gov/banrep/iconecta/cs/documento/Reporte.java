package co.gov.banrep.iconecta.cs.documento;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.developer.WSBindingProvider;
import com.sun.xml.ws.fault.ServerSOAPFaultException;

import co.gov.banrep.iconecta.cs.cliente.document.DataValue;
import co.gov.banrep.iconecta.cs.cliente.document.DateValue;
import co.gov.banrep.iconecta.cs.cliente.document.DocumentManagement;
import co.gov.banrep.iconecta.cs.cliente.document.DocumentManagement_Service;
import co.gov.banrep.iconecta.cs.cliente.document.IntegerValue;
import co.gov.banrep.iconecta.cs.cliente.document.ReportResult;
import co.gov.banrep.iconecta.cs.cliente.document.RowValue;
import co.gov.banrep.iconecta.cs.cliente.document.StringValue;
import co.gov.banrep.iconecta.cs.documento.utils.LlamadosWS;
import co.gov.banrep.iconecta.cs.documento.utils.NombreServicio;
import co.gov.banrep.iconecta.cs.documento.utils.ParametrosServicios;

public final class Reporte {

	private Reporte() {
		throw new AssertionError();
	}

	public static ReportResult ejecutarReporteSinParametros(Header soapAuthHeader, long idReport, String wsdl)
			throws IOException, SOAPException {

		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no puede ser nulo");
		}
		if (idReport <= 0) {
			throw new IllegalArgumentException("El parametro idReport (" + idReport + ") no debe ser cero o negativo");
		}
		if (wsdl == null) {
			throw new NullPointerException("El parametro wsdl no puede ser nulo");
		}
		if (wsdl.isEmpty()) {
			throw new IllegalArgumentException("El parametro wsdl no debe ser vacio");
		}

		QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
		URL newUrlWsdl = new URL(wsdl);

		DocumentManagement_Service documentService = new DocumentManagement_Service(newUrlWsdl, qName);
		DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();

		((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);

		ReportResult resultado;
		try {
			//resultado = client.runReport(idReport, null);
			ParametrosServicios servicioParams = new ParametrosServicios().withIdReport(idReport).withInputs(null);
			resultado = (ReportResult) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.RUN_REPORT);
		} catch (ServerSOAPFaultException e) {
			throw new SOAPException("Error al llamar al servicio runReport de Content Server - id (" + idReport
					+ ") - ORIGINAL: " + e.getMessage(), e);
		}

		return resultado;
	}
	
	
	/**
	 * Metodo para generar archivo plano reporte empleados
	 * @param soapAuthHeader
	 * @param idReport
	 * @param wsdl
	 * @param fileName
	 * @return
	 * @throws IOException NO puede escribir el documento
	 * @throws SOAPException Servicio Web Content caido o no responde
	 */
	public static void generarInformeEMpleadosAddin(Header soapAuthHeader, long idReport, String wsdl, String fileName) throws IOException, SOAPException {
		
		if (soapAuthHeader == null) {
			throw new NullPointerException("El parametro soapAuthHeader no puede ser nulo");
		}
		if (idReport <= 0) {
			throw new IllegalArgumentException("El parametro idReport (" + idReport + ") no debe ser cero o negativo");
		}
		if (wsdl == null) {
			throw new NullPointerException("El parametro wsdl no puede ser nulo");
		}
		if (wsdl.isEmpty()) {
			throw new IllegalArgumentException("El parametro wsdl no debe ser vacio");
		}
		if (fileName == null) {
			throw new NullPointerException("El parametro fileName no puede ser nulo");
		}
		if (fileName.isEmpty()) {
			throw new IllegalArgumentException("El parametro fileName no debe ser vacio");
		}
		
		File csvFile = new File(fileName);

		BufferedWriter writer = null;
		FileOutputStream file = null;
		try {
			
			ReportResult rRes = new ReportResult();
			
			QName qName = new QName(ServiceCons.NAMESPACE_URI, ServiceCons.LOCAL_PART);
			URL newUrlWsdl = new URL(wsdl);

			DocumentManagement_Service documentService = new DocumentManagement_Service(newUrlWsdl, qName);
			DocumentManagement client = documentService.getBasicHttpBindingDocumentManagement();

			((WSBindingProvider) client).setOutboundHeaders(soapAuthHeader);
			
			
			try {
				//rRes = client.runReport(idReport, null);
				ParametrosServicios servicioParams = new ParametrosServicios().withIdReport(idReport).withInputs(null);
				rRes = (ReportResult) LlamadosWS.llamarServicio(client, servicioParams, NombreServicio.RUN_REPORT);
			} catch (ServerSOAPFaultException e) {
				throw new SOAPException(e);
			}
			// --------------- CREAR EL ARCHIVO
			file = new FileOutputStream(csvFile);
			writer = new BufferedWriter(new OutputStreamWriter(file, "UTF-8"));

			List<RowValue> rows = rRes.getContents();

			// Integer iTitulos = 0;
			// List<String> lsTitulos = new ArrayList<>();
			List<String> lsDatos = new ArrayList<>();

			for (RowValue row : rows) {
				List<DataValue> data = row.getValues();

				for (DataValue d : data) {
					Object objVal = getDataValue(d);
					lsDatos.add(objVal.toString());
				}

				/*
				 * if (iTitulos == 0) { writeLine(writer, lsTitulos); }
				 */
				// iTitulos = 1;
				writeLine(writer, lsDatos);
				lsDatos.clear();
								
				
			}
			
			writer.flush();
			writer.close();
			
		} catch (IOException e) {
			throw new IOException("Error generando reporte empleados: " + e.getMessage(), e);
		} finally {
			safeClose(file);
		}
				
	}
	
	
	private static Object getDataValue(DataValue d) {

		Object retVal = null;

		if (d instanceof IntegerValue) {

			IntegerValue ival = (IntegerValue) d;

			if (ival.getValues().size() > 0)

				retVal = ival.getValues().get(0);

		} else if (d instanceof DateValue) {

			DateValue dateVal = (DateValue) d;

			if (dateVal.getValues().size() > 0)

				retVal = dateVal.getValues().get(0).toString().substring(0, 10);

		} else if (d instanceof StringValue) {

			StringValue strVal = (StringValue) d;

			if (strVal.getValues().size() > 0)

				retVal = strVal.getValues().get(0);

		}

		if (retVal == null)
			retVal = new String();

		return retVal;

	}

	public static void writeLine(Writer w, List<String> values) throws IOException {
		writeLine(w, values, ';', ' ');
	}

	private static String followCVSformat(String value) {

		String result = value;
		if (result.contains("\"")) {
			result = result.replace("\"", "\"\"");
		}
		return result;

	}

	public static void writeLine(Writer w, List<String> values, char separators, char customQuote) throws IOException {

		boolean first = true;

		if (separators == ' ') {
			separators = ';'; 
		}

		StringBuilder sb = new StringBuilder();
		for (String value : values) {
			if (!first) {
				sb.append(separators);
			}
			if (customQuote == ' ') {
				sb.append(followCVSformat(value));
			} else {
				sb.append(customQuote).append(followCVSformat(value)).append(customQuote);
			}

			first = false;
		}
		sb.append("\n");
		w.append(sb.toString());

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
	
	/*
	private static void safeClose(BufferedWriter fis) {
		 if (fis != null) {
			 try {
				 fis.flush();
				 fis.close();
			 } catch (IOException e) {
				 e.printStackTrace();
			 }
			 
		 }
	}*/

}
