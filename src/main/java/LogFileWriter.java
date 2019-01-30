import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LogFileWriter {
	private String LogType = "1";// 1:system.out.print, 2:log file
	private String inputFile = null;
	private String tsaUrl = null;
	private String keystoreFile = null;
	private String fileLogName = "PDFTimestamp_log.txt";
	
	public LogFileWriter() {
		super();
	}
	
	public void setType_out(String LogType) {
		this.LogType = LogType;
	}
	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}
	public void setTsaUrl(String tsaUrl) {
		this.tsaUrl = tsaUrl;
	}
	public void setKeystoreFile(String keystoreFile) {
		this.keystoreFile = keystoreFile;
	}
	
	public void readAndWrite_DateAndMessage() {
		if(LogType.equals("2")) {
			File oldLog = new File(fileLogName);
			//dateTime
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Calendar cal = Calendar.getInstance();
			String msTime = dateFormat.format(cal.getTime());
			
			if (!oldLog.exists()) {
				try {
					oldLog.createNewFile();
				} catch (Exception e) {
					//Can't create file
					System.out.println(e);
					end();
				}
			}
			//read&write
			try {
				BufferedWriter buf = new BufferedWriter(new FileWriter(oldLog, true));
				buf.append(msTime);
				buf.newLine();
				buf.append("inputFile: "+inputFile+", tsaUrl: "+tsaUrl+", keystoreFile: "+keystoreFile);
				buf.newLine();
				buf.close();
			}catch (Exception e) {
				System.out.println(e);
				end();
			}
		}
	}
	public void readAndWrite_mesLog(String mesLog) {
		if(LogType.equals("2")){	
			File oldLog = new File(fileLogName);
			if (!oldLog.exists()) {
				try {
					readAndWrite_DateAndMessage();
				} catch (Exception e) {
					//Can't create file
					System.out.println(e);
					end();
				}
			}
			try {
				BufferedWriter buf = new BufferedWriter(new FileWriter(oldLog, true));
				buf.append(mesLog);
				buf.newLine();
				buf.close();
				end();
			}catch (Exception e) {
				System.out.println(e);
				end();
			}
		}else if(LogType.equals("1")){
			System.out.println(mesLog);
			end();
		}else {
			System.out.println("Input LogType support only 1,2 (for Console log, WriteLogFile)");
			end();
		}
	}
	
	public void excepToString(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String msError = sw.toString();
		readAndWrite_mesLog(msError);
	}
	
	public void end() {
		System.out.println("********End process**********");
		System.exit(0);
	}
}
