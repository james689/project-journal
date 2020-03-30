package core;

// This class contains utility/helper methods
public class Utility {
    // converts minutes into hours and minutes e.g. if mins = 75 then
    // return value will be "1 hour 15 mins"
    public static String getHourMinDuration(int mins) {
        int durationHours = mins / 60;
        int durationMins = mins % 60;
        String hour = (durationHours > 1) ? "hours" : "hour";
        String minutes = (durationMins == 1) ? "minute" : "minutes";
        return durationHours + " " + hour + " " + durationMins + " " + minutes;
    }
}
