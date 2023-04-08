package ts.andrey.logic;

public class ThisWeekNumber {
    private static final int WEEK_NUMBER = 11;

    public ThisWeekNumber() {
//        Calendar c = Calendar.getInstance();
//        c.setTime(new Date());
//        week = c.get(Calendar.WEEK_OF_YEAR);
//        week = 11;
    }

    public int getWeek() {
        return WEEK_NUMBER;
    }
}
