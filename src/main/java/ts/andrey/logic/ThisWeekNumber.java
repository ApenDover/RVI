package ts.andrey.logic;

import java.util.Calendar;
import java.util.Date;

public class ThisWeekNumber {
    private int week;

    public ThisWeekNumber() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        week = c.get(Calendar.WEEK_OF_YEAR);
        week = 11;
    }

    public int getWeek() {
        return week;
    }
}
