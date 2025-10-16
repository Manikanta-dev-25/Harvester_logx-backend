package com.mani.respository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mani.Entity.TimeInterval;

public interface TimeIntervalRepo extends JpaRepository<TimeInterval, Long> {

}