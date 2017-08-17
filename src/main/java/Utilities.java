import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utilities{

    public static String dateFormat(String dateString) {
        DateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat returnFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dateFormated = new Date();

        String returnDateFormatedString = null;
        try {

            if(!dateString.isEmpty()){
                dateFormated = parseFormat.parse(dateString);
                //returnDateFormatedString = "'" + returnFormat.format(dateFormated) + "'";
                returnDateFormatedString = returnFormat.format(dateFormated);
            }

        }catch(Exception e){
            System.out.println("EXCEPTION: " + e);
        }

        return returnDateFormatedString;
    }

    public static String quote(String unquotedString){
        String quoted = null;

        if(unquotedString != null){
            if(!unquotedString.isEmpty()) {
                quoted = "'" + unquotedString.replaceAll("'", "") + "'";
            }
        }

        return quoted;
    }

}

