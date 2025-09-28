package com.personal.truthmirror.repositories

import com.personal.truthmirror.entities.MoodLog
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

import java.time.LocalDate
import java.time.LocalDateTime

interface MoodLogRepository extends CrudRepository<MoodLog, Long> {
    @Query("SELECT m FROM MoodLog m WHERE m.userId = :userId AND m.timestamp BETWEEN :startDate AND :endDate ORDER BY m.timestamp ASC")
    List<MoodLog> findInRangeByUserIdOrderByTimestamp(@Param("userId") String userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate)

    List<MoodLog> findByUserIdOrderByTimestampDesc(String userId)

    MoodLog findByUserIdAndJournalEntryId(String userId, Long journalEntryId)

    @Query("SELECT m FROM MoodLog m WHERE m.userId = :userId AND CAST(m.timestamp AS date) = :date")
    MoodLog findByUserIdAndDate(@Param("userId") String userId, @Param("date") LocalDate date )
}