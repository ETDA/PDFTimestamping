
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.UnrecoverableKeyException;
import java.security.cert.CRL;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.util.Store;

/**
 * An example for timestamp-singing a PDF for PADeS-Specification. The document
 * will be extended by a signed TimeStamp (another kind of signature) (Signed
 * TimeStamp and Hash-Value of the document are signed by a Time Stamp Authority
 * (TSA)).
 *
 * @author Thomas Chojecki
 * @author Vakhtang Koroghlishvili
 * @author John Hewson
 * @author Alexis Suter
 */

public class CreateSignedTimeStamp implements SignatureInterface {
	private static final Log LOG = LogFactory.getLog(CreateSignedTimeStamp.class);
	private String tsaUrl;
	private String tsaUsername;
	private String tsaPassword;
	public String keystorePath;
	public String keystorePassword;
	public String keystoreType;
	private String logType;
	private byte[] token;

	/**
	 * Initialize the signed timestamp creator
	 * 
	 * @param tsaUrl The url where TS-Request will be done.
	 */
	public CreateSignedTimeStamp(String tsaUrl, String tsaUsername, String tsaPassword) {
		this.tsaUrl = tsaUrl;
		this.tsaUsername = tsaUsername;
		this.tsaPassword = tsaPassword;
	}

	public CreateSignedTimeStamp(String tsaUrl, String keystorePath, String keystorePassword, String keystoreType,
			String logType) {
		this.tsaUrl = tsaUrl;
		this.keystorePath = keystorePath;
		this.keystorePassword = keystorePassword;
		this.keystoreType = keystoreType;
		this.logType = logType;
	}

	/**
	 * Signs the given PDF file. Alters the original file on disk.
	 * 
	 * @param file the PDF file to sign
	 * @throws IOException if the file could not be read or written
	 */
	public void signDetached(File file) throws IOException {
		signDetached(file, file);
	}

	/**
	 * Signs the given PDF file.
	 * 
	 * @param inFile  input PDF file
	 * @param outFile output PDF file
	 * @throws IOException if the input file could not be read
	 */
	public void signDetached(File inFile, File outFile) throws IOException {
		if (inFile == null || !inFile.exists()) {
			throw new FileNotFoundException("Document for signing does not exist");
		}

		// sign
		InputStream is;
		try (PDDocument doc = PDDocument.load(inFile); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

			signDetached(doc, bos);
			doc.close();
			is = new ByteArrayInputStream(bos.toByteArray());

		} catch (Exception e) {
			throw e;
		}

		PDDocument doc = PDDocument.load(is);
		FileOutputStream fos = new FileOutputStream(outFile);
		makeLTV(doc);
		doc.saveIncremental(fos);
	}

	/**
	 * Prepares the TimeStamp-Signature and starts the saving-process.
	 * 
	 * @param document given Pdf
	 * @param output   Where the file will be written
	 * @throws IOException
	 */
	public void signDetached(PDDocument document, OutputStream output) throws IOException {
		
		int accessPermissions = SigUtils.getMDPPermission(document);
		if (accessPermissions == 1) {
			throw new IllegalStateException(
					"No changes to the document are permitted due to DocMDP transform parameters dictionary");
		}

		// create signature dictionary
		PDSignature signature = new PDSignature();
		signature.setType(COSName.DOC_TIME_STAMP);
		signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
		signature.setSubFilter(COSName.getPDFName("ETSI.RFC3161"));

		// No certification allowed because /Reference not allowed in signature
		// directory
		// see ETSI EN 319 142-1 Part 1 and ETSI TS 102 778-4
		// http://www.etsi.org/deliver/etsi_en%5C319100_319199%5C31914201%5C01.01.00_30%5Cen_31914201v010100v.pdf
		// http://www.etsi.org/deliver/etsi_ts/102700_102799/10277804/01.01.01_60/ts_10277804v010101p.pdf

		// register signature dictionary and sign interface
		document.addSignature(signature, this);

		// write incremental (only for signing purpose)
		document.saveIncremental(output);
	}

	@Override
	public byte[] sign(InputStream content) throws IOException {
		ValidationTimeStamp validation;
		try {
			if (tsaUsername != null && tsaPassword != null) {
				validation = new ValidationTimeStamp(tsaUrl, tsaUsername, tsaPassword);
				token = validation.getTimeStampToken(content);
				return token;
			} else if (tsaUrl != null && keystorePath != null && keystorePassword != null && keystoreType != null) {
				validation = new ValidationTimeStamp(tsaUrl, keystorePath, keystorePassword, keystoreType);
				token = validation.getTimeStampToken(content);
				return token;
			}
		} catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyManagementException | KeyStoreException
				| CertificateException | TSPException e) {
			// LOG.error("Hashing-Algorithm not found for TimeStamping", e);
			// write log file
			LogFileWriter wrFile = new LogFileWriter();
			wrFile.setType_out(logType);
			wrFile.excepToString(e);
		}
		return new byte[] {};
	}

	private void makeLTV(PDDocument doc) {
		try {
			CMSSignedData data = new CMSSignedData(token);
	        TimeStampToken timeToken = new TimeStampToken(data);
			
			COSDictionary catalogDict = doc.getDocumentCatalog().getCOSObject();
			catalogDict.setNeedToBeUpdated(true);

			Store certificatesStore = timeToken.getCertificates();

			Collection matches = certificatesStore.getMatches(timeToken.getSID());
			X509CertificateHolder certificateHolder = (X509CertificateHolder) matches.iterator().next();
			X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(certificateHolder);

			ArrayList<X509CertificateHolder> listCertDatFirm = new ArrayList(certificatesStore.getMatches(null));

			certificate.getIssuerDN().getName();

			byte[][] certs = new byte[listCertDatFirm.size()][];
			Map<Principal, X509Certificate> certificates = new HashMap<Principal, X509Certificate>();
			for (int i = 0; i < listCertDatFirm.size(); i++) {
				X509CertificateHolder certHolder = listCertDatFirm.get(i);
				X509Certificate certificateTemp = new JcaX509CertificateConverter().getCertificate(certHolder);
				certificates.put(certificateTemp.getSubjectDN(), certificateTemp);

				certs[i] = certificateTemp.getEncoded();
			}

			// Assign byte array for storing certificate in DSS Store.
			List<CRL> crlList = new ArrayList<CRL>();
			List<OCSPResp> ocspList = new ArrayList<OCSPResp>();
			for (Map.Entry<Principal, X509Certificate> entry : certificates.entrySet()) {
				X509Certificate cert = entry.getValue();
				if (!cert.getIssuerDN().equals(cert.getSubjectDN())) {
					X509Certificate issuerCert = certificates.get(cert.getIssuerDN());
					if(issuerCert != null) {
						OCSPResp ocspResp;
						ocspResp = new GetOcspResp().getOcspResp(cert, issuerCert);
						if (ocspResp != null) {
							ocspList.add(ocspResp);
						}
					}
					crlList.addAll(new DssHelper().readCRLsFromCert(cert));
				}
			}
			byte[][] crls = new byte[crlList.size()][];
			for (int i = 0; i < crlList.size(); i++) {
				crls[i] = ((X509CRL) crlList.get(i)).getEncoded();
			}
			byte[][] ocsps = new byte[ocspList.size()][];
			for (int i = 0; i < ocspList.size(); i++) {
				ocsps[i] = ocspList.get(i).getEncoded();
			}
			Iterable<byte[]> certifiates = Arrays.asList(certs);
			COSDictionary dss = new DssHelper().createDssDictionary(certifiates, Arrays.asList(crls),
					Arrays.asList(ocsps));
			catalogDict.setItem(COSName.getPDFName("DSS"), dss);

		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
