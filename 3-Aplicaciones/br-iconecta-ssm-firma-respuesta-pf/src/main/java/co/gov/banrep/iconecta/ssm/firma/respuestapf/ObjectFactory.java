package co.gov.banrep.iconecta.ssm.firma.respuestapf;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the co.gov.banrep.demofirma.entity package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 *  @author <a href="mailto:jjrojassa@banrep.gov.co">John Jairo Rojas S.</a>
 * 
 */
//@Component
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: co.gov.banrep.demofirma.entity
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link HmCircuitResponse }
     * 
     */
    public HmCircuitResponse createHmCircuitResponse() {
        return new HmCircuitResponse();
    }

    /**
     * Create an instance of {@link HmCircuitResponse.DocumentsInfo }
     * 
     */
    public HmCircuitResponse.DocumentsInfo createHmCircuitResponseDocumentsInfo() {
        return new HmCircuitResponse.DocumentsInfo();
    }

    /**
     * Create an instance of {@link HmCircuitResponse.DocumentsInfo.Document }
     * 
     */
    public HmCircuitResponse.DocumentsInfo.Document createHmCircuitResponseDocumentsInfoDocument() {
        return new HmCircuitResponse.DocumentsInfo.Document();
    }

    /**
     * Create an instance of {@link HmCircuitResponse.DocumentsInfo.Document.Metadatos }
     * 
     */
    public HmCircuitResponse.DocumentsInfo.Document.Metadatos createHmCircuitResponseDocumentsInfoDocumentMetadatos() {
        return new HmCircuitResponse.DocumentsInfo.Document.Metadatos();
    }

    /**
     * Create an instance of {@link HmCircuitResponse.MetadatosDocument }
     * 
     */
    public HmCircuitResponse.MetadatosDocument createHmCircuitResponseMetadatosDocument() {
        return new HmCircuitResponse.MetadatosDocument();
    }

    /**
     * Create an instance of {@link HmCircuitResponse.DocumentsInfo.Document.Metadatos.Metadato }
     * 
     */
    public HmCircuitResponse.DocumentsInfo.Document.Metadatos.Metadato createHmCircuitResponseDocumentsInfoDocumentMetadatosMetadato() {
        return new HmCircuitResponse.DocumentsInfo.Document.Metadatos.Metadato();
    }

}