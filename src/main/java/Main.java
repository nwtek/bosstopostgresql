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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;

//import java.util.Date;

public class Main {

    static final String USERNAME = Creds.getUsrName();
    static final String PASSWORD = Creds.getPassWrd();
    static EnterpriseConnection connection;

    public static void main(String[] args) {

        /****
        ConnectorConfig config = new ConnectorConfig();
        config.setUsername(USERNAME);
        config.setPassword(PASSWORD);
         ****/
        //config.setTraceMessage(true);

        //try {

            /****connection = Connector.newConnection(config);****/

            /****
            // display some current settings
            System.out.println("Auth EndPoint: " + config.getAuthEndpoint());
            System.out.println("Service EndPoint: " + config.getServiceEndpoint());
            System.out.println("Username: " + config.getUsername());
            System.out.println("SessionId: " + config.getSessionId());
            *****/

            /*****System.out.println("Parsing Attachment..." + BossParsingHandler.parseResults());*****/

            // run the different examples
            System.out.println(queryAttachment("00P5000000OqCUhEAN"));


            //perform address validation
            //System.out.println("RESPONSE: " + addressValidation("some string"));

          /*
          queryContacts();
          createAccounts();
          updateAccounts();
          deleteAccounts();
          */

        //} catch (ConnectionException e1) {

            //System.out.println("ERROR: INVALID CREDENTIALS");
            //e1.printStackTrace();
        //}

    }

    public static Boolean connect(){
        ConnectorConfig config = new ConnectorConfig();
        config.setUsername(USERNAME);
        config.setPassword(PASSWORD);

        Boolean connected = false;

        try {
            connection = Connector.newConnection(config);
            connected = true;
        } catch (ConnectionException e1) {
            connected = true;
            System.out.println("ERROR: INVALID CREDENTIALS");
            //e1.printStackTrace();
        }

        return connected;
    }

    public static String queryAttachment(String attachmentId) {
        JSONParser jsonParser = new JSONParser();
        String parseResults = "No Results";

        if(!connect()){
            return "Bad Connection";
        }

        try {
                QueryResult queryResults = connection.query("SELECT Id, ParentId, Name, CreatedDate, Body, BodyLength, ContentType FROM Attachment Where ID = " + Utilities.quote(attachmentId));

                ArrayList<ObjectHandler.AttachPropObject> attachmentProps = new ArrayList<>();
                if (queryResults.getSize() > 0) {
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

                            ArrayList<ObjectHandler.ShipToObject> rowData = new ArrayList<ObjectHandler.ShipToObject>();

                            ObjectHandler objHandler = new ObjectHandler();

                            //METADATA TESTS
                            String[] metadataNames = metadata.names();

                            ObjectHandler.AttachPropObject attachObject = objHandler.new AttachPropObject();

                            attachObject.attachmentparentid             = a.getParentId();
                            attachObject.attachmentid                   = a.getId();
                            attachObject.attachmentname                 = a.getName();
                            attachObject.numberofsheets                 = numberofsheets;
                            attachObject.attachmentsize                 = a.getBodyLength();

                            attachObject.date                           = Utilities.dateFormat(metadata.get("date"));
                            attachObject.extended_properties            = metadata.get("extended-properties");
                            attachObject.dc_creator                     = metadata.get("dc:creator");
                            attachObject.publisher                      = metadata.get("publisher");
                            attachObject.author                         = metadata.get("Author");
                            attachObject.application_name               = metadata.get("Application-Name");
                            attachObject.application_version            = Double.valueOf(metadata.get("Application-Version"));
                            attachObject.isprotected                    = Boolean.getBoolean(metadata.get("protected"));
                            attachObject.content_type                   = metadata.get("Content-Type");
                            attachObject.creation_date                  = Utilities.dateFormat(metadata.get("Creation-Date"));
                            attachObject.dcterms_created                = Utilities.dateFormat(metadata.get("dcterms:created"));
                            attachObject.dc_publisher                   = metadata.get("dc:publisher");
                            attachObject.extended_properties_application = metadata.get("extended-properties:Application");
                            attachObject.last_author                     = metadata.get("Last-Author");
                            attachObject.extended_properties_company    = metadata.get("extended-properties:Company");
                            attachObject.last_modified                  = Utilities.dateFormat(metadata.get("Last-Modified"));
                            attachObject.meta_save_date                 = Utilities.dateFormat(metadata.get("meta:save-date"));

                            attachmentProps.add(attachObject);

                            parseResults = BossParsingHandler.parseResults(a, attachObject);

                        }
                    }

                    //WRITE TO THE POSTGRES DATABASE
                    //System.out.println(PostgreSQLJDBC.attachProperties(attachmentProps));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return parseResults;
    }

    private static void queryAttachments() {
        JSONParser jsonParser = new JSONParser();

        System.out.println("Querying for Attachment...");

        try {

            for(int m = 1; m< 6; m++) {
                int month = m;
                int nextmonth = (m + 1);

                //QueryResult queryResults = connection.query("SELECT Id, ParentId, Name, Body, BodyLength, ContentType FROM Attachment Where Id = '00P3800000hJuJE'");
                //QueryResult queryResults = connection.query("SELECT Id, ParentId, Name, CreatedDate, Body, BodyLength, ContentType FROM Attachment Where BodyLength >= 200000 And BodyLength <= 400000 And ContentType = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' And ParentId IN (Select Id From Facility_Lease_Agreement__c Where CreatedDate >= 2015-01-01T00:00:00Z AND CreatedDate <= 2016-01-01T00:00:00Z And Status__c = 'Installed/Complete' And RecordTypeId = '01250000000UJtZAAW') limit 10");
                QueryResult queryResults = connection.query("SELECT Id, ParentId, Name, CreatedDate, Body, BodyLength, ContentType FROM Attachment Where BodyLength >= 200000 And BodyLength <= 400000 And ContentType = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' And ParentId IN (Select Id From Facility_Lease_Agreement__c Where CreatedDate >= 2015-0" + month + "-01T00:00:00Z AND CreatedDate <= 2015-0" + nextmonth + "-01T00:00:00Z) limit 1");
                //QueryResult queryResults = connection.query("SELECT Id, ParentId, Name, CreatedDate, BodyLength, Body, ContentType FROM Attachment Where BodyLength >= 200000 And BodyLength <= 400000 And ContentType = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' And CreatedDate >= 2014-01-01T00:00:00Z And ParentId IN " + aggIds +" LIMIT 500");

                System.out.println("MONTH: " + month + " Result Count: " + queryResults.getSize());

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


                            /*
                            TODO:
                            REMOVE BODY FROM THE CLASSIFICATION QUERY TO SPEED UP (ONLY USED FOR SHEET COUNT)
                            CREATE PARSER FOR EACH SPREADSHEET
                            VERIFY ADDRESSES PULLED FROM SPREADSHEET
                            NEED TO CONVERT TO JSON OBJECT
                            CREATE JSON OBJECT FOR EACH CLASSIFIED ATTACHMENT MODEL
                            USE GJSON WHEN HAVE AN OBJECT CLASS: Gson g = new Gson(); Player p = g.fromJson(jsonString, Player.class)
                            */
                            ArrayList<ObjectHandler.ShipToObject> rowData = new ArrayList<ObjectHandler.ShipToObject>();

                            ObjectHandler objHandler = new ObjectHandler();

                            //METADATA TESTS
                            String[] metadataNames = metadata.names();

                            ObjectHandler.AttachPropObject attachObject = objHandler.new AttachPropObject();

                            attachObject.attachmentparentid             = a.getParentId();
                            attachObject.attachmentid                   = a.getId();
                            attachObject.attachmentname                 = a.getName();
                            attachObject.numberofsheets                 = numberofsheets;
                            attachObject.attachmentsize                 = a.getBodyLength();

                            attachObject.date                           = Utilities.dateFormat(metadata.get("date"));
                            attachObject.extended_properties            = metadata.get("extended-properties");
                            attachObject.dc_creator                     = metadata.get("dc:creator");
                            attachObject.publisher                      = metadata.get("publisher");
                            attachObject.author                         = metadata.get("Author");
                            attachObject.application_name               = metadata.get("Application-Name");
                            attachObject.application_version            = Double.valueOf(metadata.get("Application-Version"));
                            attachObject.isprotected                    = Boolean.getBoolean(metadata.get("protected"));
                            attachObject.content_type                   = metadata.get("Content-Type");
                            attachObject.creation_date                  = Utilities.dateFormat(metadata.get("Creation-Date"));
                            attachObject.dcterms_created                = Utilities.dateFormat(metadata.get("dcterms:created"));
                            attachObject.dc_publisher                   = metadata.get("dc:publisher");
                            attachObject.extended_properties_application = metadata.get("extended-properties:Application");
                            attachObject.last_author                     = metadata.get("Last-Author");
                            attachObject.extended_properties_company    = metadata.get("extended-properties:Company");
                            attachObject.last_modified                  = Utilities.dateFormat(metadata.get("Last-Modified"));
                            attachObject.meta_save_date                 = Utilities.dateFormat(metadata.get("meta:save-date"));

                            attachmentProps.add(attachObject);

                            System.out.println("Parsing Attachment..." + BossParsingHandler.parseResults(a, attachObject));

                        }

                        if (queryResults.isDone() == true) {
                            done = true;
                        } else {
                            queryResults = connection.queryMore(queryResults.getQueryLocator());
                        }
                    }

                    //WRITE TO THE POSTGRES DATABASE
                    //System.out.println(PostgreSQLJDBC.attachProperties(attachmentProps));

                } else {
                    System.out.println("No Results");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String addressValidation(String addressJSON){
    String response = "";

        try{

            URL url = new URL("http://esbd1dp01:8449/staples/apigwintdev1/v2/addressCleansing");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("X-IBM-Client-Id", "a8529741-bd88-42e2-8fe3-6c7ae6469251");
            con.setDoOutput(true);
            con.setDoInput(true);

            //String json = "{\"CustAddrCleansingRequest\":{\"CustAddress\":{\"CustAddrCleansing\":{\"Header\":{\"TransactionId\":\"7mRUcM3ww1\",\"TransType\":6,\"Company\":\"STAPLES\",\"Division\":\"ONLINE\",\"Group\":\"COPY&PRINT\",\"UserId\":\"OMNISOURCE\",\"ApplicationId\":\"NOOSH_OMNISOURCE\",\"ApplicationName\":\"OMNISOURCE\",\"DateTime\":20170802},\"Profile\":{\"Address\":[{\"AddrLine1\":\"719 2nd Ave\",\"AddrLine2\":\"\",\"AddrCity\":\"no city\",\"AddrState\":\"wa\",\"AddrZip\":\"98111\",\"AddrCountry\":\"USA\"}]}}}}}";

            OutputStream os = con.getOutputStream();
            os.write(addressJSON.getBytes("UTF-8"));
            os.flush();
            os.close();

            //PARSE THE RESPONSE
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            response = content.toString();

        }catch(Exception e){
            e.getStackTrace();
        }

        return response;
    }

   public static String getParamsString(Map<String, String> params) throws UnsupportedEncodingException{
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            result.append("&");
        }

        String resultString = result.toString();
        return resultString.length() > 0 ? resultString.substring(0, resultString.length() - 1) : resultString;
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