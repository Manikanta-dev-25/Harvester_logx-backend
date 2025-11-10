package com.mani.Service;

import com.mani.Entity.LogEntry;
import com.mani.Entity.TimeInterval;
import com.mani.respository.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class LogService {

    @Autowired
    private LogRepository lr;

    public LogEntry SaveEntries(LogEntry le) {
        double total = 0.0;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");

        for (TimeInterval t : le.getTimeIntervals()) {
            t.setLogEntry(le); // always set parent reference

            try {
                LocalTime start = LocalTime.parse(t.getStartTime(), formatter);
                LocalTime stop = LocalTime.parse(t.getStopTime(), formatter);

                double hours = Duration.between(start, stop).toMinutes() / 60.0;
                if (hours < 0) hours += 24.0;

                t.setDuration(hours);

                double rate = le.getHourlyWage() != null ? le.getHourlyWage() : 0.0;
                t.setPrice(String.format("%.2f", hours * rate));

                total += hours;
            } catch (Exception e) {
                t.setDuration(0.0);
                t.setPrice("0.00");
            }
        }

        le.setTotalHours(total);
        double hourlyWage = le.getHourlyWage() != null ? le.getHourlyWage() : 0.0;
        le.setTotalPrice(total * hourlyWage);
        le.setLogDate(LocalDate.now());

        return lr.save(le);
    }

    public List<LogEntry> getLogsByUser(String name) {
        System.out.println("view log method=====");
        return lr.findByCreatedByIgnoreCase(name.trim());
    }

    public List<LogEntry> filterLogs(String query, String village, Double hourlyWage) {
        return lr.filterByNameVillageWage(query);
    }
}
