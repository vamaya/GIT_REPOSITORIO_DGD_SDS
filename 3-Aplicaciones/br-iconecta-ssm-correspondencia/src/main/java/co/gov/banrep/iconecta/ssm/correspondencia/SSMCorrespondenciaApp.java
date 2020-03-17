package co.gov.banrep.iconecta.ssm.correspondencia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class SSMCorrespondenciaApp extends SpringBootServletInitializer {

		
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(SSMCorrespondenciaApp.class);
	}
	
	public static void main(String[] args) {
		SpringApplication.run(SSMCorrespondenciaApp.class, args);
	}
	
	/*
	public static void main2(String[] args) {
	        
        try {
            InputStream doc = new FileInputStream(new File("Memorando_Ejemplo_1409.docx"));
            XWPFDocument document = new XWPFDocument(doc);
            PdfOptions options = PdfOptions.create();
            OutputStream out = new FileOutputStream(new File("Test.doc"));
            PdfConverter.getInstance().convert(document, out, options);
            
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
    */
	
	/*
    public void ConvertToPDF(String docPath, String pdfPath) {
   
    }
    */
    
	/*
	public static void main3(String[] args) {
	
		String strA = "Copia(s)";
				String		strB = "Copia(s)";
				
				if (strA.equals(strB)){
					System.out.println("es igual");
					} else {
						System.out.println("NO es igual");
					}

    }
	 */         
}
