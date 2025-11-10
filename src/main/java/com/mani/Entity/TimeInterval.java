package com.mani.Entity;

import jakarta.persistence.*;

@Entity
public class TimeInterval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Time fields received as "hh:mm AM/PM" strings from frontend
    private String startTime;
    private String stopTime;

    // Duration in decimal hours, price as formatted string
    private Double duration;
    private String price;

    // Many intervals belong to one log entry
    @ManyToOne
    @JoinColumn(name = "log_entry_id")
    private LogEntry logEntry;

    // Constructors
    public TimeInterval() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getStopTime() {
        return stopTime;
    }

    public void setStopTime(String stopTime) {
        this.stopTime = stopTime;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public LogEntry getLogEntry() {
        return logEntry;
    }

    public void setLogEntry(LogEntry logEntry) {
        this.logEntry = logEntry;
    }
}
