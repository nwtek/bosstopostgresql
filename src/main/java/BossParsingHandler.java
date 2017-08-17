import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.sforce.soap.enterprise.QueryResult;
import com.sforce.soap.enterprise.sobject.Attachment;
import org.apache.poi.ss.usermodel.*;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStream;
import java.util.*;

public class BossParsingHandler {

    private static void resultPropertiesToObject(QueryResult queryResults) {
        JSONParser jsonParser = new JSONParser();

        try {
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

                            attachObject.attachmentparentid             = Utilities.quote(a.getParentId());
                            attachObject.attachmentid                   = Utilities.quote(a.getId());
                            attachObject.attachmentname                 = Utilities.quote(a.getName());
                            attachObject.numberofsheets                 = numberofsheets;
                            attachObject.attachmentsize                 = a.getBodyLength();

                            attachObject.date                           = Utilities.dateFormat(metadata.get("date"));
                            attachObject.extended_properties            = Utilities.quote(metadata.get("extended-properties"));
                            attachObject.dc_creator                     = Utilities.quote(metadata.get("dc:creator"));
                            attachObject.publisher                      = Utilities.quote(metadata.get("publisher"));
                            attachObject.author                         = Utilities.quote(metadata.get("Author"));
                            attachObject.application_name               = Utilities.quote(metadata.get("Application-Name"));
                            attachObject.application_version            = Double.valueOf(metadata.get("Application-Version"));
                            attachObject.isprotected                    = Boolean.getBoolean(metadata.get("protected"));
                            attachObject.content_type                   = Utilities.quote(metadata.get("Content-Type"));
                            attachObject.creation_date                  = Utilities.dateFormat(metadata.get("Creation-Date"));
                            attachObject.dcterms_created                = Utilities.dateFormat(metadata.get("dcterms:created"));
                            attachObject.dc_publisher                   = Utilities.quote(metadata.get("dc:publisher"));
                            attachObject.extended_properties_application = Utilities.quote(metadata.get("extended-properties:Application"));
                            attachObject.last_author                    = Utilities.quote(metadata.get("Last-Author"));
                            attachObject.extended_properties_company    = Utilities.quote(metadata.get("extended-properties:Company"));
                            attachObject.last_modified                  = Utilities.dateFormat(metadata.get("Last-Modified"));
                            attachObject.meta_save_date                 = Utilities.dateFormat(metadata.get("meta:save-date"));

                            attachmentProps.add(attachObject);
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

    public static String parseResults(Attachment a, ObjectHandler.AttachPropObject aProps){
        try{
                JSONObject jsonObject = new JSONObject();
                BossParsingHandler innerClass = new BossParsingHandler();

                //Attachment a = (Attachment)queryResults.getRecords()[i];

                //String attachBody = a.getBody().toString();
                //CAN READ THE EXCEL FILE CONTENTS
                AutoDetectParser parser = new AutoDetectParser();
                //StringWriter strWriter = new StringWriter();
                ParseContext context = new ParseContext();
                Metadata metadata = new Metadata();
                BodyContentHandler handler = new BodyContentHandler();

                /*
                    LOCAL FILE
                    InputStream fileStream = new FileInputStream("/Users/heran004/Desktop/COFFEE ORDER TRACKING.xlsx");
                    InputStream stream = TikaInputStream.get(fileStream);
                 */

                /* ATTACHMENT */
                InputStream stream = TikaInputStream.get(a.getBody());

                parser.parse(stream, handler, metadata, context);
                String handlerBody = handler.toString();
                //System.out.println("HANDLER: " + handlerBody);

                //HSSF POI LIBRARY
                Workbook wb = WorkbookFactory.create(stream);
                int loopLimit =  wb.getNumberOfSheets();

                System.out.println("NUMBER OF SHEETS: " + loopLimit);

                ArrayList<BossParsingHandler.Site> listSites = new ArrayList<BossParsingHandler.Site>();

                //s = SHEET
                for (int s = 0; s < loopLimit; s++) {
                    Sheet sheet = wb.getSheetAt(s);
                    if (sheet == null) {
                        continue;
                    }
                    System.out.println("*****SHEET NAME: " + sheet.getSheetName());

                    Map<Integer, String>            keys            = new HashMap<Integer, String>();

                    int consecutiveBreaks = 0;

                    //r = ROW
                    for(int r=sheet.getFirstRowNum(); r<=sheet.getLastRowNum(); r++) {
                        Row row = sheet.getRow(r);

                        if(consecutiveBreaks > 1)
                            continue;

                        Map<String, ArrayList<String>> mapValuesArray  = new HashMap<String, ArrayList<String>>();
                        Map<String, String>             mapSingleValue  = new HashMap<String, String>();

                        BossParsingHandler.Site         newSite = innerClass.new Site();

                        newSite.site_number                     = s;
                        newSite.attachment_props_sheet_name     = sheet.getSheetName();
                        newSite.attachment_props_sheet_number   = s+1;
                        newSite.attachment_created_date         = aProps.creation_date;
                        newSite.attachment_id                   = aProps.attachmentid;
                        newSite.attachment_parent_id            = aProps.attachmentparentid;
                        newSite.attachment_name                 = aProps.application_name;
                        newSite.attachment_props_author         = aProps.author;
                        newSite.attachment_props_publisher      = aProps.publisher;
                        newSite.attachment_props_sheet_count    = aProps.numberofsheets;

                        //newSite.attachment_created_date = a.getCreatedDate();

                        //c = CELL
                        for(int c=0; c<=row.getLastCellNum(); c++) {
                            Cell cell           = row.getCell(c);
                            String cellValue    = "";
                            if(cell != null) {
                                cellValue = cell.toString().trim();
                            }

                            if(c==0){
                                //SHOULD USE REGEX TO DETECT EMPTY FORMULA ('Order Entry sheet'!X2)
                                if(cellValue.isEmpty() || cellValue.contains("'!")){
                                    consecutiveBreaks ++;
                                    break;
                                }
                            }

                            if(!keys.containsKey(c)){
                                if(!cellValue.isEmpty())
                                keys.put(c, cellValue);
                            }
                            else{
                                if(!mapSingleValue.containsKey(keys.get(c)) && !mapValuesArray.containsKey(keys.get(c))){

                                    if(!cellValue.contains("'!"))
                                    mapSingleValue.put(keys.get(c), cellValue);

                                }
                                else if(mapValuesArray.containsKey(keys.get(c))){
                                    mapValuesArray.get(keys.get(c)).add(cellValue);
                                }
                                else{
                                    ArrayList<String> newArray = new ArrayList<String>();
                                    //DELIMINATE VALUE AND PROCESS AS AN ARRAY OF VALUES
                                    newArray.add(mapSingleValue.get(keys.get(c)));
                                    newArray.add(cellValue);

                                    mapSingleValue.remove(keys.get(c));

                                    mapValuesArray.put(keys.get(c), newArray);
                                }
                            }
                        }

                        if(!mapValuesArray.isEmpty()){
                            Gson gson = new Gson();

                            JsonElement objectCore          = gson.toJsonTree(newSite);
                            JsonElement objectFieldValues   = gson.toJsonTree(mapSingleValue);
                            JsonElement objectRelatedLists  = gson.toJsonTree(mapValuesArray);

                            objectCore.getAsJsonObject().add("Sheet Data", objectFieldValues);
                            objectFieldValues.getAsJsonObject().add("Sheet Arrays", objectRelatedLists);

                            String json = gson.toJson(objectCore);

                            System.out.println("LOGGING TO MONGO: " + MongoHandler.mongo("classification_one", json));
                        }

                    }

                }

    } catch (Exception e) {
            System.out.println("There was an error: " + e.getMessage());
            e.printStackTrace();
    }

    return "string";
}

public class Site{
    Integer site_number;
    String  classification;
    String  attachment_name;
    String  attachment_id;
    String  attachment_parent_id;
    String  attachment_created_date;
    String  attachment_props_author;
    String  attachment_props_publisher;
    Integer attachment_props_sheet_count;
    Integer attachment_props_sheet_number;
    String  attachment_props_sheet_name;

    Map<String, String> field_and_value;
    Map<String, ArrayList<String>> field_and_array;
}

public class fields_and_values{
    String field_name;
    String field_value;
    Set<String> field_value_array = new HashSet<String>();
}



/*
public static String parseUsingAutoDetect(String filename, TikaConfig tikaConfig,
        Metadata metadata) throws Exception {
        System.out.println("Handling using AutoDetectParser: [" + filename + "]");

        AutoDetectParser parser = new AutoDetectParser(tikaConfig);
        ContentHandler handler = new BodyContentHandler();
        TikaInputStream stream = TikaInputStream.get(new File(filename), metadata);
        parser.parse(stream, handler, metadata, new ParseContext());
        return handler.toString();
        }
}
*/

}
