import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.xml.sax.ContentHandler;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

import net.arnx.jsonic.JSON;

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
    JSONParser jsonParser = new JSONParser();
    
    System.out.println("Querying for Attachment...");
    
    try {
       
      // query for the 5 newest contacts      
      QueryResult queryResults = connection.query("SELECT Id, ParentId, Name, Body, BodyLength, ContentType FROM Attachment Where Id = '00P3800000hJuJE'");
        //QueryResult queryResults = connection.query("SELECT Id, ParentId, Name, CreatedDate, BodyLength, Body, ContentType FROM Attachment Where BodyLength >= 367000 And BodyLength <= 368000 And ContentType = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' And CreatedDate >= 2014-01-01T00:00:00Z And ParentId IN (Select Id From Facility_Lease_Agreement__c)  LIMIT 1");
        System.out.println("Result size: " + queryResults.getSize() + " Records Length: " + queryResults.getRecords().length);
    	
        if (queryResults.getSize() > 0) {
            boolean done = false;
            while (done == false) {
                for (int i=0;i<queryResults.getRecords().length;i++) {
                	
                	JSONObject jsonObject = new JSONObject();
                	
                    Attachment a = (Attachment)queryResults.getRecords()[i];

                    //String attachBody = a.getBody().toString();
                    //CAN READ THE EXCEL FILE CONTENTS
                    AutoDetectParser parser = new AutoDetectParser();
                    //StringWriter strWriter = new StringWriter();
                    ParseContext context = new ParseContext();
                    Metadata metadata = new Metadata();
                    BodyContentHandler handler = new BodyContentHandler();
                    InputStream stream = TikaInputStream.get(a.getBody());
                    parser.parse(stream, handler, metadata, context);
                    String handlerBody = handler.toString();
                    //System.out.println("HANDLER: " + handlerBody);

                    //HSSF POI LIBRARY
                    Workbook wb = WorkbookFactory.create(stream);
                    int loopLimit =  wb.getNumberOfSheets();
                    System.out.println("NUMBER OF SHEETS: " + loopLimit);
                    
                    //NEED TO CONVERT TO JSON OBJECT
                    //CREATE JSON OBJECT FOR EACH CLASSIFIED ATTACHMENT MODEL
                    //USE GJSON WHEN HAVE AN OBJECT CLASS: Gson g = new Gson(); Player p = g.fromJson(jsonString, Player.class)
    	    		ArrayList<object1Header> rowData = new ArrayList<object1Header>();

                    Main oHandler = new Main();
                    
            		for (int i1 = 0; i1 < loopLimit; i1++) {
            			Sheet sheet = wb.getSheetAt(i1);
            			if (sheet == null) {
            				continue;
            			}
                        System.out.println("SHEET NAME: " + sheet.getSheetName());
            			
                    	for(int j=sheet.getFirstRowNum(); j<=sheet.getLastRowNum(); j++) {
            	    		Row row = sheet.getRow(j);
            	    		if(row==null || j > 5) {
            	    			continue;
            	    		}
            	    		
            	    		if(j > 0){
            	    			object1Header data = oHandler.new object1Header();

                	    		for(int k=0; k<=row.getLastCellNum(); k++) {
                	    			
                	    			Cell cell = row.getCell(k);
                	    			if(cell!=null) {
                	    				//Object value = cellToObject(cell);
                                        System.out.println("CELL: " + cell);
                	    				//hasValues = hasValues || value!=null;
                	    				//rowData.add(cell);
                                        if(k == 0){
                    	    				data.Master = cell; 
                    	    				System.out.println("MASTER: " + cell);
                                        }
                                        else if(k == 1)
                	    				data.ShipTo = cell;
                                        else if(k == 2)
                	    				data.CustomerName = cell;
                                        else if(k == 3)
                	    				data.Address = cell;
                                        else if(k == 4)
                	    				data.OrderContact = cell;
                                        else if(k == 5)
                	    				data.SKU = cell;
                                        else if(k == 6)
                	    				data.Model = cell;
                                        else if(k == 7)
                	    				data.Quantity = cell;
                	    			} else {
                                        //rowData.add(null);
                                    }

                	    		}
                	    		
                	    		//GsonBuilder gsonBuilder = new GsonBuilder();
                	    		//gsonBuilder.registerTypeAdapter(ObjectHandler.object1Header.class, new BookSerialiser());
            	    			//String json = gson.toJson(data, ObjectHandler.object1Header.class);
                	    		//http://www.javacreed.com/gson-serialiser-example/
                	    		
                                System.out.println("DATA FOR ROW: " + ToStringBuilder.reflectionToString(data));

            	    			rowData.add(data);
            	    		}
            	    		
                            jsonObject.put(sheet.getSheetName(), rowData); 


                            //System.out.println("DATA FOR ROW: " + j);

                            /*
            	    		boolean hasValues = false;
            	    		ArrayList<Object> rowData = new ArrayList<Object>();
            	    		for(int k=0; k<=row.getLastCellNum(); k++) {
            	    			Cell cell = row.getCell(k);
            	    			if(cell!=null) {
            	    				//Object value = cellToObject(cell);
                                    System.out.println("CELL: " + cell);
            	    				//hasValues = hasValues || value!=null;
            	    				rowData.add(cell);
            	    			} else {
                                    rowData.add(null);
                                }
            	    		}
            	    		*/
                    	}
            			/*
            			ExcelWorksheet tmp = new ExcelWorksheet();
            			tmp.setName(sheet.getSheetName());
                    	for(int j=sheet.getFirstRowNum(); j<=sheet.getLastRowNum(); j++) {
            	    		Row row = sheet.getRow(j);
            	    		if(row==null) {
            	    			continue;
            	    		}
            	    		boolean hasValues = false;
            	    		ArrayList<Object> rowData = new ArrayList<Object>();
            	    		for(int k=0; k<=row.getLastCellNum(); k++) {
            	    			Cell cell = row.getCell(k);
            	    			if(cell!=null) {
            	    				Object value = cellToObject(cell);
            	    				hasValues = hasValues || value!=null;
            	    				rowData.add(value);
            	    			} else {
                                    rowData.add(null);
                                }
            	    		}
            	    		if(hasValues||!config.isOmitEmpty()) {
            					currentRowOffset++;
            	    			if (rowLimit > 0 && totalRowsAdded == rowLimit) {
            	    				break;
            					}
            					if (startRowOffset > 0 && currentRowOffset < startRowOffset) {
            	    				continue;
            					}
            	    			tmp.addRow(rowData);
            	    			totalRowsAdded++;
            	    		}
            	    	}
                    	if(config.isFillColumns()) {
                    		tmp.fillColumns();
                    	}
            			book.addExcelWorksheet(tmp);
            			*/
            		}
                    
                    System.out.println("Record Id: " + a.getId());
                    System.out.println("BODY HAS ADDRESS: " + handlerBody.contains("Address"));
                    System.out.println("BODY HAS SKU: " + handlerBody.contains("SKU"));
                    
                    //JSON TESTS
                    //jsonObject.put("ika", handler.toString()); 

                    System.out.println("BODY JSON: " + jsonObject.toString());
                    
                    //METADATA TESTS
                    String[] metadataNames = metadata.names();
                    for(String name : metadataNames) {
                       System.out.println(name + ": " + metadata.get(name));
                    }
                }
                if (queryResults.isDone() == true) {
                    done = true;
                } else {
                	queryResults = connection.queryMore(queryResults.getQueryLocator());
                }
            }
        } else {
            System.out.println("No Results");
        }

        
        /*
        if (queryResults.getSize() > 0) {
        for (int i=0;i<queryResults.getRecords().length;i++) {
          // cast the SObject to a strongly-typed Attachment
          Attachment a = (Attachment)queryResults.getRecords()[i];

          String attachBody = a.getBody().toString();

          System.out.println("BODY: " + attachBody);
          System.out.println("BODY HAS ADDRESS: " + attachBody.contains("Address"));

          //String filename = args[0];
          //TikaConfig tikaConfig = TikaConfig.getDefaultConfig();
          String contentType = new Tika().detect(attachBody);
          System.out.println("CONTENT TYPE: " + contentType );
          //OUTPUT: CONTENT TYPE: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
          

          //CAN READ THE EXCEL FILE CONTENTS
          AutoDetectParser parser = new AutoDetectParser();
          Metadata metadata = new Metadata();
          BodyContentHandler handler = new BodyContentHandler();
          InputStream stream = TikaInputStream.get(a.getBody());
          parser.parse(stream, handler, metadata);
          System.out.println("HANDLER: " + handler.toString());

          ///////
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
          //////
          //////
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
		/////
        }
      }
      */
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
      System.out.println("Result size: " + queryResults.getSize() + " Records Length: " + queryResults.getRecords().length);

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
 
	public class object1Header{
		public Cell Master;
		public Cell ShipTo;
		public Cell CustomerName;
		public Cell Address;
		public Cell OrderContact;
		public Cell SKU;
		public Cell Model;
		public Cell Quantity;
		
	}

	@Override
	public String toString() {
		return "Main []";
	}
}