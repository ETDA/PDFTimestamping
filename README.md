# PDF Timestamping
  
โปรเจกต์ PDFTimestamping เป็นโปรเจคสำหรับทำ Timestamp บนไฟล์ PDF ที่ถูกพัฒนาด้วยภาษา JAVA โดยใช้ Library PDFBox ในการเรียก TSAClient จาก TSA (Time-Stamp Authority)

นอกจาก source code ตัวอย่างในการทำ Timestamp ทาง สพธอ. ยังมีการให้บริการ TSA  ด้วย โดยสามารถติดต่อขอรับการใช้บริการได้ทางเบอร์โทรศัพท์ 02-123-1234

## Prerequisites
- JDK 1.8
- Eclipse Oxygen with Maven 

## Maven Dependencies
- Apache PDFBox 2.0.7
- Bouncy Castle 1.59

## Getting started

### PDF Timestamping with username and password    
    
    /* Set TSA Parameter (Username , Password authen)
    * tsaUrl : TSA URL (ex. "http://test.time.teda.th") 
    * tsaUsername : TSA login username
    * tsaPassword : TSA login password
    */
    CreateSignedTimeStamp signing = new CreateSignedTimeStamp(tsaUrl,tsaUsername,tsaPassword);
    
    /* Set Input Instance  */
    File inFile = new File(inputFile);
    String name = inFile.getName();
    String substring = name.substring(0, name.lastIndexOf('.'));
    
    /* Set Output Instance */
    File outFile = new File(inFile.getParent(), substring + "_timestamped.pdf");
    
    /* Do Timestaping */
    signing.signDetached(inFile, outFile);

### PDF Timestamping with certificate authentication 
  
    /*Set TSA Parameter (Certificate authen)
    * tsaUrl : TSA URL (ex. "https://test.time.teda.th") 
    * keystoreFile : Keystore File (ex.Keystore.p12)
    * keystorePassword : Keystore file password
    * keystoreType : Keystore type (ex. PKCS12)
    */
    CreateSignedTimeStamp signing = new CreateSignedTimeStamp(tsaUrl,keystoreFile,keystorePassword,keystoreType);
    
    /*Set Input Instance*/  
    File inFile = new File(inputFile);
    String name = inFile.getName();
    String substring = name.substring(0, name.lastIndexOf('.'));
    
    /*Set Output Instance*/
    File outFile = new File(inFile.getParent(), substring + "_timestamped.pdf");
    
    /*Do Timestaping*/
    signing.signDetached(inFile, outFile);
    
  ### Timestamp Client
  
    /*
    * If Timestamp for PDF is not your choice here are guidance for TSAClient calling only 
    */		
    
    /*Create MD5 Instance with SHA-256 Algorithm*/
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
		
    /*Create TSAClient for certificate authen (we modified TSAClent class to support certificate authen method)*/
    TSAClient clientCert = new TSAClient(new URL(tsaUrl),keystoreFile,keystorePassword,keystoreType,digest);
		
    /*Create TSAClient for username and password ( modified TSAClent class to support certificate authen method)*/
    //TSAClient clientUsername = new TSAClient(new URL(tsaUrl),username, password, digest);
	
    /*Example data directly from byte*/
    byte[] data = "testcontent".getBytes("UTF-8");		
		
    /*get TimeStampToken*/ 
    byte[] clientByte =  clientCert.getTimeStampToken(messageDigest);
