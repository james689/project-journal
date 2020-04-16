package utility;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * utility/helper methods.
 */
public class Utility {

    // converts a number of minutes into an hours and minutes string 
    // e.g. if mins = 75 then return value will be "1 hour 15 mins"
    public static String getHourMinDuration(int mins) {
        int durationHours = mins / 60;
        int durationMins = mins % 60;
        String hour = (durationHours == 1) ? "hour" : "hours";
        String minutes = (durationMins == 1) ? "minute" : "minutes";
        return durationHours + " " + hour + " " + durationMins + " " + minutes;
    }
    
    // converts a date in the form "dd/mm/yyyy" to the form needed for Mysql
    // and Java DB databases "yyyy-mm-dd"
    public static String convertToMysqlDateFormat(String aDate) {
        // see https://stackoverflow.com/questions/3469507/how-can-i-change-the-date-format-in-java
        DateTimeFormatter fIn = DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.UK);  // As a habit, specify the desired/expected locale, though in this case the locale is irrelevant.
        LocalDate ld = LocalDate.parse(aDate, fIn);
        DateTimeFormatter fOut = DateTimeFormatter.ofPattern("uuuu-MM-dd", Locale.UK);
        String output = ld.format(fOut);
        return output;
    }
    
    // converts a date in the form stored inside the database "yyyy-mm-dd" to
    // a form more convenient for the user "dd/mm/yyyy"
    public static String convertFromSQLDateFormat(String aDate) {
        // see https://stackoverflow.com/questions/3469507/how-can-i-change-the-date-format-in-java
        DateTimeFormatter fIn = DateTimeFormatter.ofPattern("uuuu-MM-dd", Locale.UK);  // As a habit, specify the desired/expected locale, though in this case the locale is irrelevant.
        LocalDate ld = LocalDate.parse(aDate, fIn);
        DateTimeFormatter fOut = DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.UK);
        String output = ld.format(fOut);
        return output;
    }
}
