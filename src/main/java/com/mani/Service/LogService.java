package com.mani.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mani.Entity.LogEntry;
import com.mani.Entity.TimeInterval;
import com.mani.respository.LogRepository;

@Service
public class LogService {

    @Autowired
    private LogRepository lr;

    public LogEntry SaveEntries(LogEntry le) {
        double total = 0.0;

        for (TimeInterval t : le.getTimeIntervals()) {
            t.setLogEntry(le); // always set parent reference

            if (t.getStartTime() == null || t.getStopTime() == null) {
                t.setDuration(0.0);
                continue;
            }

            double hours = Duration.between(t.getStartTime(), t.getStopTime()).toMinutes() / 60.0;

            if (hours < 0) {
                hours += 24.0;
            }

            t.setDuration(hours);
            total += hours;
        }

        le.setTotalHours(total);
        double hourlyWage = le.getHourlyWage() != null ? le.getHourlyWage() : 0.0;
        le.setTotalPrice(total * hourlyWage);
        le.setLogDate(LocalDate.now());
        return lr.save(le);
    }

    public List<LogEntry> getLogsByUser(String name) {
        System.out.println("view log method=====");
        return lr.findByNameContainingIgnoreCase(name.trim());
    }

    public List<LogEntry> filterLogs(String query, String village, Double hourlyWage) {
        return lr.filterByNameVillageWage(query);
    }

   
}