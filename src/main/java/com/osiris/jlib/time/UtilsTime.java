/*
 * Copyright (C) 2018 Velocity Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.osiris.jlib.time;

public class UtilsTime {

    /**
     * Example input: 60012387000ms<br>
     * Example output: 1 year 10 months 24 days 23 hours 17 minutes 15 seconds <br>
     */
    public String getFormattedString(long ms) {
        StringBuilder s = new StringBuilder();
        // years, months, days, hours, minutes, seconds
        int years = 0, months = 0, days = 0, hours = 0, minutes = 0, seconds = 0;
        while(ms > 31556952000L){ // 1 year in ms
            ms -= 31556952000L;
            years++;
        }
        while(ms > 2629800000L){ // 1 month in ms
            ms -= 2629800000L;
            months++;
        }
        while(ms > 86400000){ // 1 day in ms
            ms -= 86400000;
            days++;
        }
        while(ms > 3600000){ // 1 hour in ms
            ms -= 3600000;
            hours++;
        }
        while(ms > 60000){ // 1 minute in ms
            ms -= 60000;
            minutes++;
        }
        while(ms > 1000){ // 1 second in ms
            ms -= 1000;
            seconds++;
        }
        if(years > 0)
            s.append(years).append((years > 1 ? " years " : " year "));
        if(months > 0)
            s.append(months).append((months > 1 ? " months " : " month "));
        if(days > 0)
            s.append(days).append((days > 1 ? " days " : " day "));
        if(hours > 0)
            s.append(hours).append((hours > 1 ? " hours " : " hour "));
        if(minutes > 0)
            s.append(minutes).append((minutes > 1 ? " minutes " : " minute "));
        if(seconds > 0){
            seconds++; // Since the last remaining second doesn't get added in the loop above
            s.append(seconds).append((seconds > 1 ? " seconds " : " second "));
        }
        return s.toString();
    }

}
