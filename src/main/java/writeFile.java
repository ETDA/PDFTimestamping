import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class writeFile {
	private String fileLogName = "PDFTimestamp_log.txt";
	private Exception e; 
	
	public writeFile() {
		super();
	}

	public void readAndWrite(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String msError = sw.toString();
		readAndWrite(msError);
	}
	
	public void readAndWrite(String e) {
		File oldLog = new File(fileLogName);
		//dateTime
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		String msTime = dateFormat.format(cal.getTime());
		
		if (!oldLog.exists()) {
			try {
				oldLog.createNewFile();
			} catch (Exception eIO) {
				System.out.println(eIO);
			}
		} 
		//read&write
		try {
			BufferedWriter buf = new BufferedWriter(new FileWriter(oldLog, true));
			buf.newLine();
			buf.append(msTime);
			buf.newLine();
			buf.append(e);
			buf.newLine();
			buf.close();
			System.out.println("********End process**********");
			System.exit(0);	
		}	
			catch (IOException eIO) {
			System.out.println(eIO);
		} 	catch (Exception ex) {
			readAndWrite(ex);
		}
		
	}
}
