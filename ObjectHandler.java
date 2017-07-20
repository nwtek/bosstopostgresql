import org.apache.poi.ss.usermodel.Cell;

public class ObjectHandler {

    public class ShipToObject {
        public Cell Master;
        public Cell ShipTo;
        public Cell CustomerName;
        public Cell Address;
        public Cell OrderContact;
        public Cell SKU;
        public Cell Model;
        public Cell Quantity;
    }

    public class AttachPropObject {
        public String extended_properties;
        public String last_modified;
        public String dcterms_modified;
        public String dcterms_created;
        public String last_save_date;
        public Boolean isprotected = false;
        public String meta_save_date;
        public String application_name;
        public String modified;
        public String content_type;
        public String x_parsed_by;
        public String creator;
        public String meta_author;
        public String meta_creation_date;
        public String extended_properties_application;
        public String meta_last_author;
        public String creation_date;
        public String last_author;
        public String application_version;
        public String author;
        public String publisher;
        public String dc_publisher;
        public Integer numberofsheets;
        public String attachmentid;
        public String attachmentparentid;
        public Integer attachmentsize = 0;
    }
}

