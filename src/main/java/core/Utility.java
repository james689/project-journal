package core;

// This class contains utility/helper methods
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Utility {

    // converts minutes into hours and minutes e.g. if mins = 75 then
    // return value will be "1 hour 15 mins"
    public static String getHourMinDuration(int mins) {
        int durationHours = mins / 60;
        int durationMins = mins % 60;
        String hour = (durationHours == 1) ? "hour" : "hours";
        String minutes = (durationMins == 1) ? "minute" : "minutes";
        return durationHours + " " + hour + " " + durationMins + " " + minutes;
    }

    // converts a date in the form "dd/mm/yyyy" to the form needed for Mysql
    // "yyyy-mm-dd"
    public static String convertToMysqlDateFormat(String aDate) {
        // see https://stackoverflow.com/questions/3469507/how-can-i-change-the-date-format-in-java
        DateTimeFormatter fIn = DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.UK);  // As a habit, specify the desired/expected locale, though in this case the locale is irrelevant.
        LocalDate ld = LocalDate.parse(aDate, fIn);

        DateTimeFormatter fOut = DateTimeFormatter.ofPattern("uuuu/MM/dd", Locale.UK);
        String output = ld.format(fOut);
        
        String ret = output.replace('/','-');
        
        return ret;
    }
}
