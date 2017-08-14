import com.sforce.soap.enterprise.*;
import com.sforce.soap.enterprise.Error;
import com.sforce.soap.enterprise.sobject.Account;
import com.sforce.soap.enterprise.sobject.Attachment;
import com.sforce.soap.enterprise.sobject.Contact;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.xml.sax.ContentHandler;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

//import java.util.Date;

public class Main {

    static final String USERNAME = Creds.getUsrName();
    static final String PASSWORD = Creds.getPassWrd();
    static EnterpriseConnection connection;

    public static void main(String[] args) {

        ConnectorConfig config = new ConnectorConfig();
        config.setUsername(USERNAME);
        config.setPassword(PASSWORD);
        //config.setTraceMessage(true);

        try {

            connection = Connector.newConnection(config);

            // display some current settings
            System.out.println("Auth EndPoint: " + config.getAuthEndpoint());
            System.out.println("Service EndPoint: " + config.getServiceEndpoint());
            System.out.println("Username: " + config.getUsername());
            System.out.println("SessionId: " + config.getSessionId());

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


            /*
            QueryResult queryAggreementResults = connection.query("Select Id From Facility_Lease_Agreement__c Where CreatedDate >= 2014-01-01T00:00:00Z And Status__c = 'Installed/Complete' And RecordTypeId = '01250000000UJtZAAW' limit 500");
            System.out.println("Aggreements Size: " + queryAggreementResults.getSize() + " Records Length: " + queryAggreementResults.getRecords().length);

            //ArrayList<String> aggreementIds = new ArrayList<String>();
            String aggIds = new String();
            if (queryAggreementResults.getSize() > 0) {
                boolean done = false;
                while (done == false) {
                    for (int i = 0; i < queryAggreementResults.getRecords().length; i++) {
                        Facility_Lease_Agreement__c agreement = (Facility_Lease_Agreement__c) queryAggreementResults.getRecords()[i];
                        //aggreementIds.add("'"+agreement.getId()+"'");

                        if(aggIds.isEmpty())
                            aggIds = "('"+agreement.getId()+"'";
                        else
                            aggIds = aggIds + ",'"+agreement.getId()+"'";
                    }

                    if (queryAggreementResults.isDone() == true) {
                        done = true;
                        aggIds = aggIds + ")";
                    } else {
                        queryAggreementResults = connection.queryMore(queryAggreementResults.getQueryLocator());
                    }
                }
            }

            System.out.println("AGG IDS: " + aggIds);
            */

            //QueryResult queryResults = connection.query("SELECT Id, ParentId, Name, Body, BodyLength, ContentType FROM Attachment Where Id = '00P3800000hJuJE'");
            QueryResult queryResults = connection.query("SELECT Id, ParentId, Name, CreatedDate, Body, BodyLength, ContentType FROM Attachment Where BodyLength >= 200000 And BodyLength <= 400000 And ContentType = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' And ParentId IN (Select Id From Facility_Lease_Agreement__c Where CreatedDate >= 2015-01-01T00:00:00Z AND CreatedDate <= 2016-01-01T00:00:00Z And Status__c = 'Installed/Complete' And RecordTypeId = '01250000000UJtZAAW') limit 10");
            //QueryResult queryResults = connection.query("SELECT Id, ParentId, Name, CreatedDate, BodyLength, Body, ContentType FROM Attachment Where BodyLength >= 200000 And BodyLength <= 400000 And ContentType = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' And CreatedDate >= 2014-01-01T00:00:00Z And ParentId IN " + aggIds +" LIMIT 500");

            System.out.println("Attachment Size: " + queryResults.getSize() + " Records Length: " + queryResults.getRecords().length);

            ArrayList<ObjectHandler.AttachPropObject> attachmentProps = new ArrayList<>();
            if (queryResults.getSize() > 0) {
                boolean done = false;
                while (done == false) {
                    for (int i = 0; i < queryResults.getRecords().length; i++) {

                        JSONObject jsonObject = new JSONObject();

                        Attachment a = (Attachment) queryResults.getRecords()[i];

                        //String attachBody = a.getBody().toString();
                        //CAN READ THE EXCEL FILE CONTENTS
                        AutoDetectParser parser = new AutoDetectParser();
                        //StringWriter strWriter = new StringWriter();
                        //TIKA LIBRARIES
                        ParseContext context = new ParseContext();
                        Metadata metadata = new Metadata();
                        BodyContentHandler handler = new BodyContentHandler(-1);


                        InputStream stream = TikaInputStream.get(a.getBody());
                        parser.parse(stream, handler, metadata, context);
                        String handlerBody = handler.toString();
                        //System.out.println("HANDLER: " + handlerBody);

                        //HSSF POI LIBRARY
                        Workbook wb = WorkbookFactory.create(stream);
                        int numberofsheets = wb.getNumberOfSheets();
                        //System.out.println("NUMBER OF SHEETS: " + numberofsheets);


                        //NEED TO CONVERT TO JSON OBJECT
                        //CREATE JSON OBJECT FOR EACH CLASSIFIED ATTACHMENT MODEL
                        //USE GJSON WHEN HAVE AN OBJECT CLASS: Gson g = new Gson(); Player p = g.fromJson(jsonString, Player.class)
                        ArrayList<ObjectHandler.ShipToObject> rowData = new ArrayList<ObjectHandler.ShipToObject>();

                        ObjectHandler objHandler = new ObjectHandler();
/*
                        for (int i1 = 0; i1 < loopLimit; i1++) {
                            Sheet sheet = wb.getSheetAt(i1);
                            if (sheet == null) {
                                continue;
                            }
                            System.out.println("SHEET NAME: " + sheet.getSheetName());

                            for (int j = sheet.getFirstRowNum(); j <= sheet.getLastRowNum(); j++) {
                                Row row = sheet.getRow(j);
                                if (row == null || j > 5) {
                                    continue;
                                }

                                if (j > 0) {
                                    ObjectHandler.ShipToObject data = objHandler.new ShipToObject();

                                    for (int k = 0; k <= row.getLastCellNum(); k++) {

                                        Cell cell = row.getCell(k);
                                        if (cell != null) {
                                            //Object value = cellToObject(cell);
                                            System.out.println("CELL: " + cell);
                                            //hasValues = hasValues || value!=null;
                                            //rowData.add(cell);
                                            if (k == 0) {
                                                data.Master = cell;
                                                System.out.println("MASTER: " + cell);
                                            } else if (k == 1)
                                                data.ShipTo = cell;
                                            else if (k == 2)
                                                data.CustomerName = cell;
                                            else if (k == 3)
                                                data.Address = cell;
                                            else if (k == 4)
                                                data.OrderContact = cell;
                                            else if (k == 5)
                                                data.SKU = cell;
                                            else if (k == 6)
                                                data.Model = cell;
                                            else if (k == 7)
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
                            }
                        }
                        System.out.println("Record Id: " + a.getId());
                        System.out.println("BODY HAS ADDRESS: " + handlerBody.contains("Address"));
                        System.out.println("BODY HAS SKU: " + handlerBody.contains("SKU"));
                        //JSON TESTS
                        //jsonObject.put("ika", handler.toString());

                        System.out.println("BODY JSON: " + jsonObject.toString());

*/

                        //METADATA TESTS
                        String[] metadataNames = metadata.names();

                        ObjectHandler.AttachPropObject attachObject = objHandler.new AttachPropObject();


                        attachObject.attachmentparentid                 = Utilities.quote(a.getParentId());
                        attachObject.attachmentid                       = Utilities.quote(a.getId());
                        attachObject.attachmentname                     = Utilities.quote(a.getName());
                        attachObject.numberofsheets                     = numberofsheets;
                        attachObject.attachmentsize                     = a.getBodyLength();

                        attachObject.date                               = Utilities.dateFormat(metadata.get("date"));
                        attachObject.extended_properties                = Utilities.quote(metadata.get("extended-properties"));
                        attachObject.dc_creator                         = Utilities.quote(metadata.get("dc:creator"));
                        attachObject.publisher                          = Utilities.quote(metadata.get("publisher"));
                        attachObject.author                             = Utilities.quote(metadata.get("Author"));
                        attachObject.application_name                   = Utilities.quote(metadata.get("Application-Name"));
                        attachObject.application_version                = Double.valueOf(metadata.get("Application-Version"));
                        attachObject.isprotected                        = Boolean.getBoolean(metadata.get("protected"));
                        attachObject.content_type                       = Utilities.quote(metadata.get("Content-Type"));
                        attachObject.creation_date                      = Utilities.dateFormat(metadata.get("Creation-Date"));
                        attachObject.dcterms_created                    = Utilities.dateFormat(metadata.get("dcterms:created"));
                        attachObject.dc_publisher                       = Utilities.quote(metadata.get("dc:publisher"));
                        attachObject.extended_properties_application    = Utilities.quote(metadata.get("extended-properties:Application"));
                        attachObject.last_author                        = Utilities.quote(metadata.get("Last-Author"));
                        attachObject.extended_properties_company        = Utilities.quote(metadata.get("extended-properties:Company"));
                        attachObject.last_modified                      = Utilities.dateFormat(metadata.get("Last-Modified"));

                        //2015-01-06T18:27:58Z

                        /*
                        extended-properties:AppVersion: 12.0000
                        dcterms:modified: 2014-03-06T22:06:43Z
                        Last-Save-Date: 2014-03-06T22:06:43Z
                        meta:save-date: 2014-03-06T22:06:43Z
                        Application-Name: Microsoft Excel
                        modified: 2014-03-06T22:06:43Z
                        X-Parsed-By: org.apache.tika.parser.DefaultParser
                        creator: marie.cohen
                        meta:author: marie.cohen
                        meta:creation-date: 2012-05-02T15:44:45Z
                        meta:last-author: banba001
                        Creation-Date: 2012-05-02T15:44:45Z
                        custom:_NewReviewCycle:
                        */

                        attachmentProps.add(attachObject);

                        /*
                        for (String name : metadataNames) {
                            System.out.println(name + ": " + metadata.get(name));
                        }
                        */

                    }

                    if (queryResults.isDone() == true) {
                        done = true;
                    } else {
                        queryResults = connection.queryMore(queryResults.getQueryLocator());
                    }
                }

                //WRITE TO THE DATABASE
                System.out.println(PostgreSQLJDBC.attachProperties(attachmentProps));
            } else {
                System.out.println("No Results");
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

    // queries and displays the 5 newest contacts
    private static void queryContacts() {

        System.out.println("Querying for the 5 newest Contacts...");

        try {

            // query for the 5 newest contacts
            QueryResult queryResults = connection.query("SELECT Id, FirstName, LastName, Account.Name " +
                    "FROM Contact WHERE AccountId != NULL ORDER BY CreatedDate DESC LIMIT 5");
            System.out.println("Result size: " + queryResults.getSize() + " Records Length: " + queryResults.getRecords().length);

            if (queryResults.getSize() > 0) {
                for (int i = 0; i < queryResults.getRecords().length; i++) {
                    // cast the SObject to a strongly-typed Contact
                    Contact c = (Contact) queryResults.getRecords()[i];
                    System.out.println("Id: " + c.getId() + " - Name: " + c.getFirstName() + " " +
                            c.getLastName() + " - Account: " + c.getAccount().getName());
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
            for (int i = 0; i < 5; i++) {
                Account a = new Account();
                a.setName("Test Account " + i);
                records[i] = a;
            }

            // create the records in Salesforce.com
            SaveResult[] saveResults = connection.create(records);

            // check the returned results for any errors
            for (int i = 0; i < saveResults.length; i++) {
                if (saveResults[i].isSuccess()) {
                    System.out.println(i + ". Successfully created record - Id: " + saveResults[i].getId());
                } else {
                    Error[] errors = saveResults[i].getErrors();
                    for (int j = 0; j < errors.length; j++) {
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
                for (int i = 0; i < queryResults.getRecords().length; i++) {
                    // cast the SObject to a strongly-typed Account
                    Account a = (Account) queryResults.getRecords()[i];
                    System.out.println("Updating Id: " + a.getId() + " - Name: " + a.getName());
                    // modify the name of the Account
                    a.setName(a.getName() + " -- UPDATED");
                    records[i] = a;
                }
            }

            // update the records in Salesforce.com
            SaveResult[] saveResults = connection.update(records);

            // check the returned results for any errors
            for (int i = 0; i < saveResults.length; i++) {
                if (saveResults[i].isSuccess()) {
                    System.out.println(i + ". Successfully updated record - Id: " + saveResults[i].getId());
                } else {
                    Error[] errors = saveResults[i].getErrors();
                    for (int j = 0; j < errors.length; j++) {
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
                for (int i = 0; i < queryResults.getRecords().length; i++) {
                    // cast the SObject to a strongly-typed Account
                    Account a = (Account) queryResults.getRecords()[i];
                    // add the Account Id to the array to be deleted
                    ids[i] = a.getId();
                    System.out.println("Deleting Id: " + a.getId() + " - Name: " + a.getName());
                }
            }

            // delete the records in Salesforce.com by passing an array of Ids
            DeleteResult[] deleteResults = connection.delete(ids);

            // check the results for any errors
            for (int i = 0; i < deleteResults.length; i++) {
                if (deleteResults[i].isSuccess()) {
                    System.out.println(i + ". Successfully deleted record - Id: " + deleteResults[i].getId());
                } else {
                    Error[] errors = deleteResults[i].getErrors();
                    for (int j = 0; j < errors.length; j++) {
                        System.out.println("ERROR deleting record: " + errors[j].getMessage());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public String toString() {
        return "Main []";
    }
}