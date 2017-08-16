import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class PostgreSQLJDBC {

   public static void main( String args[] ) {
	   
	   //parseInsert();

	   
	   /*
	   Scanner reader = new Scanner(System.in);  // Reading from System.in
	   System.out.println("What would you like to do? (dbOpen | dbCreateTable | dbInsert | dbSelect | dbUpdate | dbDelete): ");
	   String input = reader.nextLine(); // Scans the next token of the input as an int.
	   System.out.println("Executing...");
	   

	   
	   switch (input){
		   case "dbInsert" 	: response = dbInsert();
		   case "dbOpen" 	: response = dbOpen();
		   case "dbUpdate" 	: response = dbUpdate();
		   case "dbDelete" 	: response = dbDelete();
		   case "dbSelect" 	: response = dbSelect();
		   default: response = "Not a recognized command";
	   }
	   
	   
	   */
	   
   	  //System.out.println("Response : " + attachProperties());

   }
   
   public static String dbOpen (){
      Connection c = null;
      try {
         Class.forName("org.postgresql.Driver");
         c = DriverManager
            .getConnection("jdbc:postgresql://localhost:5432/testdb","postgres", "123");
      } catch (Exception e) {
         e.printStackTrace();
         System.err.println(e.getClass().getName()+": "+e.getMessage());
         System.exit(0);
      }
      System.out.println("Opened database successfully");
      return "Opened database successfully";
   }
   
   public static String dbCreateTable() {
	      Connection c = null;
	      Statement stmt = null;
	      try {
	         Class.forName("org.postgresql.Driver");
	         c = DriverManager
	            .getConnection("jdbc:postgresql://localhost:5432/postgres","postgres", "123");
	         
	         System.out.println("Opened database successfully");

	         stmt = c.createStatement();
	         String sql = "CREATE TABLE COMPANY " +
	            "(ID INT PRIMARY KEY     NOT NULL," +
	            " NAME           TEXT    NOT NULL, " +
	            " AGE            INT     NOT NULL, " +
	            " ADDRESS        CHAR(50), " +
	            " SALARY         REAL)";
	         stmt.executeUpdate(sql);
	         stmt.close();
	         c.close();
	      } catch ( Exception e ) {
	         System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	         System.exit(0);
	      }
	      return "Table created successfully";
	   }
   
   public static String dbInsert() {
	      Connection c = null;
	      Statement stmt = null;
	      try {
	         Class.forName("org.postgresql.Driver");
	         c = DriverManager
	            .getConnection("jdbc:postgresql://localhost:5432/postgres","postgres", "123");
	         c.setAutoCommit(false);
	         System.out.println("Opened database successfully");

	         stmt = c.createStatement();
	         String sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) "
	            + "VALUES (1, 'Paul', 32, 'California', 20000.00 );";
	         stmt.executeUpdate(sql);

	         sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) "
	            + "VALUES (2, 'Allen', 25, 'Texas', 15000.00 );";
	         stmt.executeUpdate(sql);

	         sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) "
	            + "VALUES (3, 'Teddy', 23, 'Norway', 20000.00 );";
	         stmt.executeUpdate(sql);

	         sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) "
	            + "VALUES (4, 'Mark', 25, 'Rich-Mond ', 65000.00 );";
	         stmt.executeUpdate(sql);

	         stmt.close();
	         c.commit();
	         c.close();
	      } catch (Exception e) {
	         System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	         System.exit(0);
	      }
	      return "Records created successfully";
	   }

   public static String parseInsert() {
	      Connection c = null;
	      Statement stmt = null;
	      try {
	         Class.forName("org.postgresql.Driver");
	         c = DriverManager
	            .getConnection("jdbc:postgresql://localhost:5432/postgres","postgres", "123");
	         c.setAutoCommit(false);
	         System.out.println("Opened database successfully");

	         /*
	          SHOW ALL TABLES
	         DatabaseMetaData md = c.getMetaData();
	         ResultSet rs = md.getTables(null, null, "%", null);
	         while (rs.next()) {
	           System.out.println(rs.getString(3));
	         }
	         */
	         stmt = c.createStatement();

	         String sql = "INSERT INTO PARSETESTONE (SHIPTO, CUSTOMERNAME, ADDRESS, ORDERCONTACT, SKU, MODEL, QUANTITY) " 
	         + "VALUES ('Test Ship To 2', 'James Inc.', '123 E. James Street, Boulder, CO 98342', 'James Smith', '934BO', '2014 Model', 10);";
	         stmt.executeUpdate(sql);

	         stmt.close();
	         c.commit();
	         c.close();
	      } catch (Exception e) {
	         System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	         System.exit(0);
	      }
	      return "Records created successfully";
   }
   
   public static String attachProperties(ArrayList<ObjectHandler.AttachPropObject> attachmentProps) {
	      Connection c = null;
	      Statement stmt = null;
	      try {
			  Class.forName("org.postgresql.Driver");
			  c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/db-attachments", "heran004", "123");
			  c.setAutoCommit(false);
			  //System.out.println("Opened database successfully");

			  stmt = c.createStatement();

			  for (ObjectHandler.AttachPropObject obj: attachmentProps) {
			  String sql = "INSERT INTO attachment_classification("
					  + "attachment_name,"
					  + "attachment_id,"
					  + "extended_properties_company,"
					  + "last_modified,"
					  + "dcterms_modified,"
					  + "dcterms_created,"
					  + "last_save_date,"
					  + "protected,"
					  + "meta_save_date,"
					  + "application_name,"
					  + "modified,"
					  + "content_type,"
					  + "x_parsed_by,"
					  + "creator,"
					  + "meta_author,"
					  + "meta_creation_date,"
					  + "extended_properties_application,"
					  + "meta_last_author,"
					  + "creation_date,"
					  + "last_author,"
					  + "application_version,"
					  + "author,"
					  + "publisher,"
					  + "dc_publisher,"
					  + "attachment_number_of_sheets,"
					  + "parent_id,"
					  + "attachment_size)"
					  + "VALUES ("
					  + obj.attachmentname + ","
					  + obj.attachmentid + ","
					  + obj.extended_properties + ","
					  + obj.last_modified + ","
					  + obj.dcterms_modified + ","
					  + obj.dcterms_created + ","
					  + obj.last_save_date + ","
					  + obj.isprotected + ","
					  + obj.meta_save_date + ","
					  + obj.application_name + ","
					  + obj.modified + ","
					  + obj.content_type + ","
					  + obj.x_parsed_by + ","
					  + obj.creator + ","
					  + obj.meta_author + ","
					  + obj.meta_creation_date + ","
					  + obj.extended_properties_application + ","
					  + obj.meta_last_author + ","
					  + obj.creation_date + ","
					  + obj.last_author + ","
					  + obj.application_version + ","
					  + obj.author + ","
					  + obj.publisher + ","
					  + obj.dc_publisher + ","
					  + obj.numberofsheets + ","
					  + obj.attachmentparentid + ","
					  + obj.attachmentsize + ");";

					stmt.addBatch(sql);
			  }
			   //stmt.executeUpdate(sql);
			   stmt.executeBatch();
			
			   stmt.close();
			   c.commit();
			   c.close();

			} catch (Exception e) {
			   System.err.println("ERROR:" + e.getClass().getName()+" : "+ e.getMessage() );
			   System.exit(0);
			}
	      
			return "Records created successfully";
   }
   
   public static String dbSelect() {
	      Connection c = null;
	      Statement stmt = null;
	      try {
	         Class.forName("org.postgresql.Driver");
	         c = DriverManager
	            .getConnection("jdbc:postgresql://localhost:5432/postgres","postgres", "123");
	         c.setAutoCommit(false);
	         System.out.println("Opened database successfully");

	         stmt = c.createStatement();
	         ResultSet rs = stmt.executeQuery( "SELECT * FROM COMPANY;" );
	         while ( rs.next() ) {
	            int id = rs.getInt("id");
	            String  name = rs.getString("name");
	            int age  = rs.getInt("age");
	            String  address = rs.getString("address");
	            float salary = rs.getFloat("salary");
	            System.out.println( "ID = " + id );
	            System.out.println( "NAME = " + name );
	            System.out.println( "AGE = " + age );
	            System.out.println( "ADDRESS = " + address );
	            System.out.println( "SALARY = " + salary );
	            System.out.println();
	         }
	         rs.close();
	         stmt.close();
	         c.close();
	      } catch ( Exception e ) {
	         System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	         System.exit(0);
	      }
	      return "Operation done successfully";
	   }
   
   public static String dbUpdate() {
	      Connection c = null;
	      Statement stmt = null;
	      try {
	         Class.forName("org.postgresql.Driver");
	         c = DriverManager
	            .getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "123");
	         c.setAutoCommit(false);
	         System.out.println("Opened database successfully");

	         stmt = c.createStatement();
	         String sql = "UPDATE COMPANY set SALARY = 25000.00 where ID=1;";
	         stmt.executeUpdate(sql);
	         c.commit();

	         ResultSet rs = stmt.executeQuery( "SELECT * FROM COMPANY;" );
	         while ( rs.next() ) {
	            int id = rs.getInt("id");
	            String  name = rs.getString("name");
	            int age  = rs.getInt("age");
	            String  address = rs.getString("address");
	            float salary = rs.getFloat("salary");
	            System.out.println( "ID = " + id );
	            System.out.println( "NAME = " + name );
	            System.out.println( "AGE = " + age );
	            System.out.println( "ADDRESS = " + address );
	            System.out.println( "SALARY = " + salary );
	            System.out.println();
	         }
	         rs.close();
	         stmt.close();
	         c.close();
	      } catch ( Exception e ) {
	         System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	         System.exit(0);
	      }
	      return "Operation done successfully";
	   }
   
   public static String dbDelete() {
	      Connection c = null;
	      Statement stmt = null;
	      try {
	         Class.forName("org.postgresql.Driver");
	         c = DriverManager
	            .getConnection("jdbc:postgresql://localhost:5432/postgres","postgres", "123");
	         c.setAutoCommit(false);
	         System.out.println("Opened database successfully");

	         stmt = c.createStatement();
	         String sql = "DELETE from COMPANY where ID = 2;";
	         stmt.executeUpdate(sql);
	         c.commit();

	         ResultSet rs = stmt.executeQuery( "SELECT * FROM COMPANY;" );
	         while ( rs.next() ) {
	            int id = rs.getInt("id");
	            String  name = rs.getString("name");
	            int age  = rs.getInt("age");
	            String  address = rs.getString("address");
	            float salary = rs.getFloat("salary");
	            System.out.println( "ID = " + id );
	            System.out.println( "NAME = " + name );
	            System.out.println( "AGE = " + age );
	            System.out.println( "ADDRESS = " + address );
	            System.out.println( "SALARY = " + salary );
	            System.out.println();
	         }
	         rs.close();
	         stmt.close();
	         c.close();
	      } catch ( Exception e ) {
	         System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	         System.exit(0);
	      }
	      return "Operation done successfully";
	   }
}