import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.language.LanguageIdentifier;
import org.apache.tika.language.LanguageProfile;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.ToXMLContentHandler;

import com.sforce.soap.enterprise.Connector;
import com.sforce.soap.enterprise.DeleteResult;
import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.Error;
import com.sforce.soap.enterprise.QueryResult;
import com.sforce.soap.enterprise.SaveResult;
import com.sforce.soap.enterprise.sobject.Account;
import com.sforce.soap.enterprise.sobject.Attachment;
import com.sforce.soap.enterprise.sobject.Contact;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class Main { 
  
static final String USERNAME = "";
static final String PASSWORD = "";
  static EnterpriseConnection connection;

  public static void main(String[] args) {

    ConnectorConfig config = new ConnectorConfig();
    config.setUsername(USERNAME);
    config.setPassword(PASSWORD);
    //config.setTraceMessage(true);
    
    try {
      
      connection = Connector.newConnection(config);
      
      // display some current settings
      System.out.println("Auth EndPoint: "+config.getAuthEndpoint());
      System.out.println("Service EndPoint: "+config.getServiceEndpoint());
      System.out.println("Username: "+config.getUsername());
      System.out.println("SessionId: "+config.getSessionId());
      
      // run the different examples
      queryAttachment();
      /*
      queryContacts();
      createAccounts();
      updateAccounts();
      deleteAccounts();
      */
      
      
    } catch (ConnectionException e1) {
        e1.printStackTrace();
    }  

  }
  
  
  // queries and displays the 5 newest contacts
  private static void queryAttachment() {
    
    System.out.println("Querying for Attachment...");
    
    try {
       
      // query for the 5 newest contacts      
      QueryResult queryResults = connection.query("SELECT Id, ParentId, Name, Body, BodyLength, ContentType FROM Attachment Where Id = '00P3800000hJuJE'");
      if (queryResults.getSize() > 0) {
        for (int i=0;i<queryResults.getRecords().length;i++) {
          // cast the SObject to a strongly-typed Attachment
          Attachment a = (Attachment)queryResults.getRecords()[i];

          //HOW CAN I READ THE EXCEL FILE CONTENTS????
          System.out.println("BODY: " + a.getBody().toString() );

          //String filename = args[0];
          //TikaConfig tikaConfig = TikaConfig.getDefaultConfig();
          String contentType = new Tika().detect(a.getBody());
          System.out.println("CONTENT TYPE: " + contentType );
          //OUTPUT: CONTENT TYPE: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
          
          AutoDetectParser parser = new AutoDetectParser();
          Metadata metadata = new Metadata();
          BodyContentHandler handler = new BodyContentHandler();
          InputStream stream = TikaInputStream.get(a.getBody());
          parser.parse(stream, handler, metadata);
          System.out.println("HANDLER: " + handler.toString());

          /*
          AutoDetectParser parser = new AutoDetectParser();
          Metadata metadata = new Metadata();
          try (InputStream stream = ContentHandlerExample.class.getResourceAsStream("test2.doc")) {
              parser.parse(stream, handler, metadata);
              return chunks;
          }
          
          Metadata metadata = new Metadata();

          metadata = new Metadata();
          String text = parseUsingAutoDetect("tst file", tikaConfig, metadata);
          System.out.println("Parsed Metadata: ");
          System.out.println(metadata);
          System.out.println("Parsed Text: ");
          System.out.println(text);
          */
          /*
          ByteArrayInputStream myxls = new ByteArrayInputStream(a.getBody());
          XSSFWorkbook wb = new XSSFWorkbook(myxls);
          //HSSFWorkbook wb     = new HSSFWorkbook(myxls);
          try{
        	  
        	  
		    XSSFSheet sheet = wb.getSheetAt(0);
		    XSSFRow row;
		    XSSFCell cell;

		    int rows; // No of rows
		    rows = sheet.getPhysicalNumberOfRows();

		    int cols = 0; // No of columns
		    int tmp = 0;

		    // This trick ensures that we get the data properly even if it doesn't start from first few rows
		    for(int i1 = 0; i1 < 10 || i1 < rows; i1++) {
		        row = sheet.getRow(i1);
		        if(row != null) {
		            tmp = sheet.getRow(i1).getPhysicalNumberOfCells();
		            if(tmp > cols) cols = tmp;
		        }
		    }

		    for(int r = 0; r < rows; r++) {
		        row = sheet.getRow(r);
		        if(row != null) {
		            for(int c = 0; c < cols; c++) {
		                cell = row.getCell((short)c);
		                if(cell != null) {
		                    // Your code here
		                }
		            }
		        }
		    }
		    
		} catch(Exception ioe) {
		    ioe.printStackTrace();
		}
		*/
        }
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    }    
    
  }

  
  public static String parseUsingAutoDetect(String filename, TikaConfig tikaConfig,
          Metadata metadata) throws Exception {
		System.out.println("Handling using AutoDetectParser: [" + filename + "]");
		
		AutoDetectParser parser = new AutoDetectParser(tikaConfig);
		ContentHandler handler = new BodyContentHandler();
		TikaInputStream stream = TikaInputStream.get(new File(filename), metadata);
		parser.parse(stream, handler, metadata, new ParseContext());
		return handler.toString();
}
  

  /*
  public String parseToStringExample() throws IOException, SAXException, TikaException {
	    Tika tika = new Tika();
	    try (InputStream stream = ParsingExample.class.getResourceAsStream("test.doc")) {
	        return tika.parseToString(stream);
	    }
	}
	*/
  
  // queries and displays the 5 newest contacts
  private static void queryContacts() {
    
    System.out.println("Querying for the 5 newest Contacts...");
    
    try {
       
      // query for the 5 newest contacts      
      QueryResult queryResults = connection.query("SELECT Id, FirstName, LastName, Account.Name " +
      		"FROM Contact WHERE AccountId != NULL ORDER BY CreatedDate DESC LIMIT 5");
      if (queryResults.getSize() > 0) {
        for (int i=0;i<queryResults.getRecords().length;i++) {
          // cast the SObject to a strongly-typed Contact
          Contact c = (Contact)queryResults.getRecords()[i];
          System.out.println("Id: " + c.getId() + " - Name: "+c.getFirstName()+" "+
              c.getLastName()+" - Account: "+c.getAccount().getName());
        }
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    }    
    
  }
  
  // create 5 test Accounts
  private static void createAccounts() {
    
    System.out.println("Creating 5 new test Accounts...");
    Account[] records = new Account[5];
    
    try {
       
      // create 5 test accounts
      for (int i=0;i<5;i++) {
        Account a = new Account();
        a.setName("Test Account "+i);
        records[i] = a;
      }
      
      // create the records in Salesforce.com
      SaveResult[] saveResults = connection.create(records);
      
      // check the returned results for any errors
      for (int i=0; i< saveResults.length; i++) {
        if (saveResults[i].isSuccess()) {
          System.out.println(i+". Successfully created record - Id: " + saveResults[i].getId());
        } else {
          Error[] errors = saveResults[i].getErrors();
          for (int j=0; j< errors.length; j++) {
            System.out.println("ERROR creating record: " + errors[j].getMessage());
          }
        }    
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    }    
    
  }
  
  // updates the 5 newly created Accounts
  private static void updateAccounts() {
    
    System.out.println("Update the 5 new test Accounts...");
    Account[] records = new Account[5];
    
    try {
       
      QueryResult queryResults = connection.query("SELECT Id, Name FROM Account ORDER BY " +
      		"CreatedDate DESC LIMIT 5");
      if (queryResults.getSize() > 0) {
        for (int i=0;i<queryResults.getRecords().length;i++) {
          // cast the SObject to a strongly-typed Account
          Account a = (Account)queryResults.getRecords()[i];
          System.out.println("Updating Id: " + a.getId() + " - Name: "+a.getName());
          // modify the name of the Account
          a.setName(a.getName()+" -- UPDATED");
          records[i] = a;
        }
      }
      
      // update the records in Salesforce.com
      SaveResult[] saveResults = connection.update(records);
      
      // check the returned results for any errors
      for (int i=0; i< saveResults.length; i++) {
        if (saveResults[i].isSuccess()) {
          System.out.println(i+". Successfully updated record - Id: " + saveResults[i].getId());
        } else {
          Error[] errors = saveResults[i].getErrors();
          for (int j=0; j< errors.length; j++) {
            System.out.println("ERROR updating record: " + errors[j].getMessage());
          }
        }    
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    }    
    
  }
  
  // delete the 5 newly created Account
  private static void deleteAccounts() {
    
    System.out.println("Deleting the 5 new test Accounts...");
    String[] ids = new String[5];
    
    try {
       
      QueryResult queryResults = connection.query("SELECT Id, Name FROM Account ORDER BY " +
      		"CreatedDate DESC LIMIT 5");
      if (queryResults.getSize() > 0) {
        for (int i=0;i<queryResults.getRecords().length;i++) {
          // cast the SObject to a strongly-typed Account
          Account a = (Account)queryResults.getRecords()[i];
          // add the Account Id to the array to be deleted
          ids[i] = a.getId();
          System.out.println("Deleting Id: " + a.getId() + " - Name: "+a.getName());
        }
      }
      
      // delete the records in Salesforce.com by passing an array of Ids
      DeleteResult[] deleteResults = connection.delete(ids);
      
      // check the results for any errors
      for (int i=0; i< deleteResults.length; i++) {
        if (deleteResults[i].isSuccess()) {
          System.out.println(i+". Successfully deleted record - Id: " + deleteResults[i].getId());
        } else {
          Error[] errors = deleteResults[i].getErrors();
          for (int j=0; j< errors.length; j++) {
            System.out.println("ERROR deleting record: " + errors[j].getMessage());
          }
        }    
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    }    
    
  }
 
}