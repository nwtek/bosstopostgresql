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
        public String extended_properties           = null;
        public String extended_properties_company   = null;
        public String last_modified                 = null;
        public String dcterms_modified              = null;
        public String date                        = null;
        public String dcterms_created             = null;
        public String last_save_date              = null;
        public String meta_save_date              = null;
        public String meta_creation_date          = null;
        public String creation_date               = null;
        public Boolean isprotected              = false;
        public String application_name          = null;
        public String modified                  = null;
        public String content_type              = null;
        public String x_parsed_by               = null;
        public String creator                   = null;
        public String meta_author               = null;
        public String extended_properties_application = null;
        public String meta_last_author          = null;
        public String last_author               = null;
        public Double application_version       = null;
        public String author                    = null;
        public String publisher                 = null;
        public String dc_publisher              = null;
        public String dc_creator                = null;
        public Integer numberofsheets           = null;
        public String attachmentid              = null;
        public String attachmentparentid        = null;
        public Integer attachmentsize           = 0;
        public String attachmentname            = null;
    }
}

