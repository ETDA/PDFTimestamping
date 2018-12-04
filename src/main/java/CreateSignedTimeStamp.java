
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.bouncycastle.tsp.TSPException;


/**
 * An example for timestamp-singing a PDF for PADeS-Specification. The document will be extended by
 * a signed TimeStamp (another kind of signature) (Signed TimeStamp and Hash-Value of the document
 * are signed by a Time Stamp Authority (TSA)).
 *
 * @author Thomas Chojecki
 * @author Vakhtang Koroghlishvili
 * @author John Hewson
 * @author Alexis Suter
 */


public class CreateSignedTimeStamp implements SignatureInterface {
	  private static final Log LOG = LogFactory.getLog(CreateSignedTimeStamp.class);
    private  String tsaUrl;
    private String tsaUsername ;
    private  String tsaPassword;
    public   String keystorePath;
    public  String keystorePassword;
    public  String keystoreType;
    /**
     * Initialize the signed timestamp creator
     * 
     * @param tsaUrl The url where TS-Request will be done.
     */
    public CreateSignedTimeStamp(String tsaUrl,String tsaUsername,String tsaPassword)
    {
        this.tsaUrl = tsaUrl;
        this.tsaUsername = tsaUsername;
        this.tsaPassword = tsaPassword;
    }
    
    public CreateSignedTimeStamp(String tsaUrl,String keystorePath,String keystorePassword,String keystoreType )
    {
        this.tsaUrl = tsaUrl;
        this.keystorePath = keystorePath;
        this.keystorePassword = keystorePassword;
        this.keystoreType = keystoreType;
    }
    
    /**
     * Signs the given PDF file. Alters the original file on disk.
     * 
     * @param file the PDF file to sign
     * @throws IOException if the file could not be read or written
     */
    public void signDetached(File file) throws IOException
    {
        signDetached(file, file);
    }
    
    /**
     * Signs the given PDF file.
     * 
     * @param inFile input PDF file
     * @param outFile output PDF file
     * @throws IOException if the input file could not be read
     */
    public void signDetached(File inFile, File outFile) throws IOException
    {
        if (inFile == null || !inFile.exists())
        {
            throw new FileNotFoundException("Document for signing does not exist");
        }

        // sign
        try (PDDocument doc = PDDocument.load(inFile);
             FileOutputStream fos = new FileOutputStream(outFile))
        {
            signDetached(doc, fos);
        }catch(Exception e) {
        	throw e;
        }
    }
    
    /**
     * Prepares the TimeStamp-Signature and starts the saving-process.
     * 
     * @param document given Pdf
     * @param output Where the file will be written
     * @throws IOException
     */
    public void signDetached(PDDocument document, OutputStream output) throws IOException
    {
        int accessPermissions = SigUtils.getMDPPermission(document);
        if (accessPermissions == 1)
        {
            throw new IllegalStateException(
                    "No changes to the document are permitted due to DocMDP transform parameters dictionary");
        }

        // create signature dictionary
        PDSignature signature = new PDSignature();
        signature.setType(COSName.DOC_TIME_STAMP);
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        signature.setSubFilter(COSName.getPDFName("ETSI.RFC3161"));

        // No certification allowed because /Reference not allowed in signature directory
        // see ETSI EN 319 142-1 Part 1 and ETSI TS 102 778-4
        // http://www.etsi.org/deliver/etsi_en%5C319100_319199%5C31914201%5C01.01.00_30%5Cen_31914201v010100v.pdf
        // http://www.etsi.org/deliver/etsi_ts/102700_102799/10277804/01.01.01_60/ts_10277804v010101p.pdf

        // register signature dictionary and sign interface
        document.addSignature(signature, this);

        // write incremental (only for signing purpose)
        document.saveIncremental(output);
    }
    
    @Override
    public byte[] sign(InputStream content) throws IOException
    {
        ValidationTimeStamp validation;
        try
        {
        	if(tsaUsername != null && tsaPassword  != null)
        	{ 
        		validation = new ValidationTimeStamp(tsaUrl,tsaUsername,tsaPassword);
        		return validation.getTimeStampToken(content);
            }
        	else if(tsaUrl != null && keystorePath  != null && keystorePassword  != null && keystoreType  != null ) {
        		validation = new ValidationTimeStamp(tsaUrl,keystorePath,keystorePassword,keystoreType);
        		return validation.getTimeStampToken(content);
        	}
        }
        catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyManagementException | KeyStoreException | CertificateException | TSPException e)
        {
            //LOG.error("Hashing-Algorithm not found for TimeStamping", e);
        	//write log file
        	writeFile wrFile = new writeFile();
			wrFile.excepToString(e);
        } 
        return new byte[] {};
    }

    
    
}
