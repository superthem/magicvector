package cn.magicvector.common.basic.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtil {

    /** 距今日结束的剩余秒数。 */
    public static int getRestSecondsOfToday(){
        Calendar curDate = Calendar.getInstance();
        Calendar tommorowDate = new GregorianCalendar(curDate
                .get(Calendar.YEAR), curDate.get(Calendar.MONTH), curDate
                .get(Calendar.DATE) + 1, 0, 0, 0);
        return (int)(tommorowDate.getTimeInMillis() - curDate .getTimeInMillis()) / 1000;
    }

    /** 在基准时间上递增若干小时。 */
    public static Date getHoursLaterTime(Date source, int hours) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(source);
        cal.add(Calendar.HOUR_OF_DAY, hours);
        return cal.getTime();
    }

    /** 取该日零点零分零秒。 */
    public static Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        calendar.set(year, month, day, 0, 0, 0);
        return calendar.getTime();
    }

    /** 取该日二十三点五十九分五十九秒。 */
    public static Date getEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        calendar.set(year, month, day, 23, 59, 59);
        return calendar.getTime();
    }

    /** 格式化为短横线年月日字符串。 */
    public static String toDateStr(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(date);
    }

    /** 格式化为年月日时分秒字符串。 */
    public static String toFormatDatetimeStr(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }

    /** 按短日期格式解析为时间(如 2026-12-25)。 */
    public static Date toFormatDateStr(String date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return formatter.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 按自定义模式格式化输出。 */
    public static String format(Date date, String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(date);
    }

    /** 返回两个时间中较晚的一个。 */
    public static Date getMaxDate(Date d1, Date d2){
        return d1.compareTo(d2)>0? d1:d2;
    }

    /** 取输入日下一自然日零点。 */
    public static Date addOneDay(Date inputDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(inputDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    /**
     * 判断输入的candidate的时间是否达到了time所指定的时间精度。
     * time可以是"HH","HH:mm","HH:mm:ss"。例如 "12" 代表判断candidate的小时是否为12，
     * "12:00" 代表判断candidate的小时是否为12且分钟是否为0，以此类推。
     *
     * @param candidate 待比较的日期时间
     * @param time      指定的时间精度，格式如 "HH", "HH:mm", "HH:mm:ss"
     * @return 如果candidate的时间匹配time指定的精度，则返回true；否则返回false。
     */
    public static boolean isTimeReached(Date candidate, String time) {
        if (candidate == null || time == null || time.isEmpty()) {
            return false;
        }

        Calendar c = Calendar.getInstance();
        c.setTime(candidate);

        String[] parts = time.split(":");
        int inputHour = -1;
        int inputMinute = -1; // 使用 -1 表示该精度未被指定
        int inputSecond = -1;

        try {
            inputHour = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            // 如果解析失败，直接返回 false
            return false;
        }

        if (parts.length > 1) {
            try {
                inputMinute = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        if (parts.length > 2) {
            try {
                inputSecond = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                return false;
            }
        }

        // 比较小时
        if (c.get(Calendar.HOUR_OF_DAY) != inputHour) {
            return false;
        }

        // 如果指定了分钟，则比较分钟
        if (inputMinute != -1) {
            if (c.get(Calendar.MINUTE) != inputMinute) {
                return false;
            }

            // 如果还指定了秒，则比较秒
            if (inputSecond != -1) {
                if (c.get(Calendar.SECOND) != inputSecond) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 取指定日期的指定时间点，时间格式支持 "HH", "HH:mm", "HH:mm:ss"
     * @param candidate 基准日期
     * @param time 指定时间，例如 "12", "12:00", "12:32:33"
     * @return 指定日期的指定时间点
     */
    public static Date getSpecifiedTimeOfDate(Date candidate, String time) {
        if (time == null || time.isEmpty()) {
            throw new IllegalArgumentException("Time string cannot be null or empty");
        }

        // 解析输入的时间字符串
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = 0;
        int second = 0;

        if (parts.length > 1) {
            minute = Integer.parseInt(parts[1]);
        }
        if (parts.length > 2) {
            second = Integer.parseInt(parts[2]);
        }

        // 使用 Calendar 设置指定的时间
        Calendar cal = Calendar.getInstance();
        cal.setTime(candidate);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    /** 获取N年前当年一月一号零时。 */
    public static Date getFirstDayOfYearsAgo(int howManyYearsAgo) {
        // 获取当前时间的 Calendar 实例
        Calendar calendar = Calendar.getInstance();

        // 1. 年份减去 5 年
        calendar.add(Calendar.YEAR, -howManyYearsAgo);

        // 2. 将月份设置为 1 月 (注意：Calendar 中月份是从 0 开始的，0 代表 1 月)
        calendar.set(Calendar.MONTH, Calendar.JANUARY);

        // 3. 将日期设置为 1 号
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        // 4. 获取最终的 Date 对象
        return calendar.getTime();
    }

    /** 判断两个时间是否同一自然日。 */
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }


    /** 取二十四小时制的小时数。 */
    public static int getHour(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    /** 取当前时刻的分钟数。 */
    public static int getMinute(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MINUTE);
    }

}
