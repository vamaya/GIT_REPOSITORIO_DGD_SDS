package co.gov.banrep.iconecta.ssm.correspondencia.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.codehaus.stax2.XMLInputFactory2;

public class readXML {
	

	public static long getIdFolderXML(int year, int mes, String fondo, String carpeta, String ruta) {
		
		long id = 0L;
		 InputStream in = null;
		try {
		
			File file = new File(ruta);
	        in = new FileInputStream(file); 	
	    	JAXBContext jc = JAXBContext.newInstance(Periodos.class);    	
	    	
			XMLInputFactory xif = XMLInputFactory2.newFactory();        
			xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
			xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);                              
			
			XMLStreamReader xsr = xif.createXMLStreamReader(in, "UTF8");       
			Unmarshaller u = jc.createUnmarshaller();        
			Periodos periodos = (Periodos) u.unmarshal(xsr);
       
        for(Periodo p : periodos.getPeriodos()){
            
        	if(Integer.parseInt(p.getId())==year) {
        		
        		Fondo f = p.getFondos().stream().filter(unfondo -> unfondo.getId().equals(fondo)).findFirst().get();
        		Carpeta c = f.getCarpetas().stream().filter(uncarpeta -> uncarpeta.getId().equals(carpeta)).findFirst().get();
        		
        		switch(mes) {
        		case 1: 
        			id = Long.parseLong(c.getEne());
        			break;
        		case 2: 
        			id = Long.parseLong(c.getFeb());
        			break;
        		case 3: 
        			id = Long.parseLong(c.getMar());
        			break;
        		case 4: 
        			id = Long.parseLong(c.getAbr());
        			break;
        		case 5: 
        			id = Long.parseLong(c.getMay());
        			break;
        		case 6: 
        			id = Long.parseLong(c.getJun());
        			break;
        		case 7: 
        			id = Long.parseLong(c.getJul());
        			break;
        		case 8: 
        			id = Long.parseLong(c.getAgo());
        			break;
        		case 9: 
        			id = Long.parseLong(c.getSep());
        			break;
        		case 10: 
        			id = Long.parseLong(c.getOct());
        			break;
        		case 11: 
        			id = Long.parseLong(c.getNov());
        			break;
        		case 12: 
        			id = Long.parseLong(c.getDic());
        			break;
        	  default:
        		id=0L;
        	
        		}
        		
        	}
            
        }

    } catch (JAXBException | FileNotFoundException | XMLStreamException e) {
        e.printStackTrace();
    } finally {
		safeClose(in);
	}
		
		return id;
	}
	
	
	
public static String getTypeDcocumentXML(int year, int mes, String fondo, String carpeta, String ruta) {
		
		String result = "";
		InputStream in = null;
		try {
		
			File file = new File(ruta);
	        in = new FileInputStream(file);	
	    	JAXBContext jc = JAXBContext.newInstance(Periodos.class);    	
	    	
			XMLInputFactory xif = XMLInputFactory2.newFactory();        
			xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
			xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);                              
			
			XMLStreamReader xsr = xif.createXMLStreamReader(in, "UTF8");       
			Unmarshaller u = jc.createUnmarshaller();        
			Periodos periodos = (Periodos) u.unmarshal(xsr);

        for(Periodo p : periodos.getPeriodos()){
            
        	if(Integer.parseInt(p.getId())==year) {
        		
        		Fondo f = p.getFondos().stream().filter(unfondo -> unfondo.getId().equals(fondo)).findFirst().get();
        		Carpeta c = f.getCarpetas().stream().filter(uncarpeta -> uncarpeta.getId().equals(carpeta)).findFirst().get();
        		result = c.getTipoDocumental();
        		
        		
        	}
            
        }

    } catch (JAXBException | FileNotFoundException | XMLStreamException e) {
        e.printStackTrace();
    } finally {
		safeClose(in);
    }
		
		return result;
	}
	
public static String getSerieDocumentXML(int year, int mes, String fondo, String carpeta, String ruta) {
	
	String result = "";
	InputStream in = null;
	try {
	
		File file = new File(ruta);
        
        in = new FileInputStream(file);
		    	
    	JAXBContext jc = JAXBContext.newInstance(Periodos.class);    	
    	
		XMLInputFactory xif = XMLInputFactory2.newFactory();        
		xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
		xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);                              
		
		XMLStreamReader xsr = xif.createXMLStreamReader(in, "UTF8");       
		Unmarshaller u = jc.createUnmarshaller();        
		Periodos periodos = (Periodos) u.unmarshal(xsr);
   
    for(Periodo p : periodos.getPeriodos()){
        
    	if(Integer.parseInt(p.getId())==year) {
    		
    		Fondo f = p.getFondos().stream().filter(unfondo -> unfondo.getId().equals(fondo)).findFirst().get();
    		Carpeta c = f.getCarpetas().stream().filter(uncarpeta -> uncarpeta.getId().equals(carpeta)).findFirst().get();
    		result = c.getSerie();
    		
    		
    	}
        
    }

} catch (JAXBException | FileNotFoundException | XMLStreamException e) {
    e.printStackTrace();
}finally {
	safeClose(in);
}
	return result;
}


private static void safeClose(InputStream fis) {
	 if (fis != null) {
		 try {
			 fis.close();
		 } catch (IOException e) {
			 e.printStackTrace();
		 }
		 
	 }
}
	

}


