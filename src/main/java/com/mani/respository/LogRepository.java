package com.mani.respository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mani.Entity.LogEntry;

public interface LogRepository extends JpaRepository<LogEntry, Long> {
	
	List<LogEntry> findByNameContainingIgnoreCase(String name);
	@Query("SELECT l FROM LogEntry l WHERE " +
		       "(:name IS NULL OR LOWER(l.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
		       "(:village IS NULL OR LOWER(l.village) LIKE LOWER(CONCAT('%', :village, '%'))) AND " +
		       "(:hourlyWage IS NULL OR l.hourlyWage = :hourlyWage)")
		List<LogEntry> filterByNameVillageWage(
		    @Param("name") String name
		);
	List<LogEntry> findByCreatedByIgnoreCase(String createdBy);

}
