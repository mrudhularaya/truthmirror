package com.personal.truthmirror.repositories

import com.personal.truthmirror.entities.JournalEntry
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

import java.time.LocalDate

interface JournalEntryRepository extends CrudRepository<JournalEntry, Long>{

    List<JournalEntry> findByUserIdOrderByTimestampDesc(String userId)

    JournalEntry findByUserIdAndId(String userId, Long id)

    @Query("SELECT j FROM JournalEntry j WHERE j.userId = :userId AND CAST(j.timestamp AS date) = :date")
    JournalEntry findByUserIdAndDate(@Param("userId") String userId, @Param("date") LocalDate date)
}
