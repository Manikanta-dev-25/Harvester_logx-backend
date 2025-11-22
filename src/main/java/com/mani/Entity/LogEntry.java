package com.mani.Entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String createdBy; 

    @Column(nullable = false)
    private String name;    

    @Column(nullable = false)
    private String phno;

    @Column(nullable = false)
    private String village;
    
    @Column(name = "log_date")
    private LocalDate logDate;

    private Double totalHours;
    private Double hourlyWage;
    private Double totalPrice;

    @OneToMany(mappedBy = "logEntry", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<TimeInterval> timeIntervals = new ArrayList<>();

    public LogEntry() {}

 
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    
    
    public LocalDate getLogDate() {
		return logDate;
	}

	public void setLogDate(LocalDate logDate) {
		this.logDate = logDate;
	}

	public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhno() { return phno; }
    public void setPhno(String phno) { this.phno = phno; }

    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }

    public Double getTotalHours() { return totalHours; }
    public void setTotalHours(Double totalHours) { this.totalHours = totalHours; }

    public Double getHourlyWage() { return hourlyWage; }
    public void setHourlyWage(Double hourlyWage) { this.hourlyWage = hourlyWage; }

    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }

    public List<TimeInterval> getTimeIntervals() { return timeIntervals; }
    public void setTimeIntervals(List<TimeInterval> timeIntervals) { this.timeIntervals = timeIntervals; }
}
