package com.Cutch.bukkit.PermIconomy;

public class Period {
    public int year;
    public int month;
    public int day;
    public int second;
    public int minute;
    public int hour;
    public Period(String str)
    {
        String[] dateTime = str.trim().split(" ");
        if(dateTime.length == 1)
        {
            if(dateTime[0].contains(":"))
                parseTime(dateTime[0].split(":"));
            else
                parseDate(dateTime[0].split("/|\\\\"));
        }
        else
        {
            String[] dates = dateTime[0].split("/|\\\\");
            String[] times = dateTime[1].split(":");
            parseTime(times);
            parseDate(dates);
        }
    }
    public void parseTime(String[] str)
    {
        for(int i = str.length-1; i >= 0; i--)
        {
            try {
                if(i == 0)
                    hour = Integer.parseInt(str[i]);
                else if(i == 1)
                    minute = Integer.parseInt(str[i]);
                 else if(i == 2)
                    second = Integer.parseInt(str[i]);
            } catch(Exception e) { }
        }
    }
    public void parseDate(String[] str)
    {
        for(int i = 0; i < str.length; i++)
        {
            try {
                if(i == 0)
                    day = Integer.parseInt(str[i]);
                else if(i == 1)
                    month = Integer.parseInt(str[i]);
                else if(i == 2)
                    year = Integer.parseInt(str[i]);
            } catch(Exception e) { }
        }
    }
    @Override
    public String toString()
    {
        String str = "";
        if(day > 0)
            str += day + " day" + (day > 1?"s ":" ");
        if(month > 0)
            str += month + " month" + (month > 1?"s ":" ");
        if(year > 0)
            str += year + " year" + (year > 1?"s ":" ");
        if(hour > 0)
            str += hour + " hour" + (hour > 1?"s ":" ");
        if(minute > 0)
            str += minute + " minute" + (minute > 1?"s ":" ");
        if(second > 0)
            str += second + " second" + (second > 1?"s ":" ");
        if(str.endsWith(" "))
            str = str.substring(0, str.length()-1);
        return str;
    }
}
