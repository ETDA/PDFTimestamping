import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;

public class main {
	public static void main(String[] args) // throws IOException, GeneralSecurityException
	{

		/**** Sample Input for username password authen ****/
		/*
		 * username/password String inputFile = resources/pdfA3.pdf; String tsaUrl =
		 * "http://test.time.teda.th"; String tsaUsername = ""; String tsaPassword = "";
		 */

		/*
		 * String inputFile = args[0]; String tsaUrl = args[1]; String tsaUsername =
		 * args[2]; String tsaPassword = args[3];
		 */

		/**** Sample Input for certificate authen ****/
		/*
		 * username/password String inputFile = resources/pdfA3.pdf; String tsaUrl =
		 * "https://test.time.teda.th"; String keystoreFile = ""; String
		 * keystorePassword = ""; String keystoreType = "";
		 */
		try {
			String inputFile = args[0];
			String tsaUrl = args[1];
			String keystoreFile = args[2];
			String keystorePassword = args[3];
			String keystoreType = args[4];

			// sign PDF (Certificate authen)
			CreateSignedTimeStamp signing = new CreateSignedTimeStamp(tsaUrl, keystoreFile, keystorePassword,
					keystoreType);

			// sign PDF (Username , Password authen)
			// CreateSignedTimeStamp signing = new
			// CreateSignedTimeStamp(tsaUrl,tsaUsername,tsaPassword);

			File inFile = new File(inputFile);
			String name = inFile.getName();
			String substring = name.substring(0, name.lastIndexOf('.'));
			String msDone = "********TimeStamp Done**********";
			File outFile = new File(inFile.getParent(), substring + "_timestamped.pdf");
			try {
				signing.signDetached(inFile, outFile);
				writeFile wrFile = new writeFile();
				wrFile.readAndWrite(msDone);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				writeFile wrFile = new writeFile();
				wrFile.readAndWrite(e);
			}
		} catch (Exception ex) {
			writeFile wrFile = new writeFile();
			wrFile.readAndWrite(ex);
		}
		// System.out.println("********TimeStamp Done**********");
	}
}
