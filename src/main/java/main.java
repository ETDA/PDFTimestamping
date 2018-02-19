import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;

public class main {
    public static void main(String[] args) throws IOException, GeneralSecurityException
    { 	       	
    	   	
		/**** Sample Input ****/		
    	/*
		 * String inputFile = resources/pdfA3.pdf;
    	String tsaUrl = "http://test.time.teda.th";
    	String tsaUsername = ""; 
    	String tsaPassword = "";  
		*/
    	
    	String inputFile = args[0];
    	String tsaUrl = args[1];
    	String tsaUsername = args[2]; 
    	String tsaPassword = args[3];    	   	    	
    	
        // sign PDF
        CreateSignedTimeStamp signing = new CreateSignedTimeStamp(tsaUrl,tsaUsername,tsaPassword);

        File inFile = new File(inputFile);
        String name = inFile.getName();
        String substring = name.substring(0, name.lastIndexOf('.'));

        File outFile = new File(inFile.getParent(), substring + "_timestamped.pdf");
        signing.signDetached(inFile, outFile);
        

		System.out.println("********TimeStamp Done**********");
    }
}
